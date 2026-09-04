-- Restored native idempotency records must retain the exact physical base
-- from which their db-before value can be reopened.  These roots are receipt
-- authority, not the current accelerator: putting them in the ordinary
-- publication revision chain would perturb peer/indexer selection and its
-- single live-set fold.  The archive below is deliberately PostgreSQL-only,
-- generation-qualified, immutable, and visible only through an exact
-- request binding.

-- Advisory locks are local to a PostgreSQL database, not to an installation
-- schema. Portable restore intentionally preserves lineage, so the v1 key
-- falsely coupled two otherwise independent Atomic catalogs installed in
-- separate schemas of the same database. The catalog relation OID is stable
-- across schema rename and scopes every generation reader/collector to its
-- actual installation without weakening any in-catalog fence.
CREATE OR REPLACE FUNCTION atomic_log_generation_pin_key(
    candidate_database_id TEXT,
    candidate_generation BIGINT
)
RETURNS BIGINT
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
    SELECT pg_catalog.hashtextextended(
               'atomic/log-generation-pin/v2/' ||
               'atomic_databases'::regclass::oid::text || '/' ||
               lineage_id || '/' || candidate_generation::text,
               4707476001900298240::bigint
           )
      FROM atomic_databases
     WHERE database_id = candidate_database_id
       AND candidate_generation >= 0
$$;

CREATE TABLE atomic_request_base_archives (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    archive_revision BIGINT NOT NULL CHECK (archive_revision > 0),
    basis_t BIGINT NOT NULL CHECK (basis_t >= 0),
    tx_hash BYTEA NOT NULL CHECK (octet_length(tx_hash) = 32),
    state_hash BYTEA NOT NULL CHECK (octet_length(state_hash) = 32),
    eidx_frontier BIGINT NOT NULL CHECK (eidx_frontier > 0),
    manifest_version SMALLINT NOT NULL CHECK (manifest_version = 4),
    manifest_hash BYTEA PRIMARY KEY CHECK (octet_length(manifest_hash) = 32),
    payload BYTEA NOT NULL CHECK (octet_length(payload) > 0),
    expected_node_count BIGINT NOT NULL CHECK (expected_node_count > 0),
    node_set_hash BYTEA NOT NULL CHECK (octet_length(node_set_hash) = 32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    UNIQUE (database_id, generation, archive_revision),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generations(database_id, generation)
);

-- Planned hashes are installed before node upload.  They are both a durable
-- resumability ledger and a fail-closed liveness pin; no FK to the node value
-- is intentional because the content-first upload follows this root.
CREATE TABLE atomic_request_base_archive_nodes (
    manifest_hash BYTEA NOT NULL
        REFERENCES atomic_request_base_archives(manifest_hash),
    node_hash BYTEA NOT NULL CHECK (octet_length(node_hash) = 32),
    PRIMARY KEY (manifest_hash, node_hash)
);

CREATE INDEX atomic_request_base_archive_nodes_node
    ON atomic_request_base_archive_nodes(node_hash);

CREATE TABLE atomic_request_base_archive_roots (
    manifest_hash BYTEA NOT NULL
        REFERENCES atomic_request_base_archives(manifest_hash),
    index_order SMALLINT NOT NULL CHECK (index_order BETWEEN 0 AND 3),
    history BOOLEAN NOT NULL,
    root_hash BYTEA NOT NULL REFERENCES atomic_tree_nodes(node_hash),
    datom_count BIGINT NOT NULL CHECK (datom_count >= 0),
    encoded_bytes BIGINT NOT NULL CHECK (encoded_bytes > 0),
    PRIMARY KEY (manifest_hash, index_order, history)
);

CREATE INDEX atomic_request_base_archive_roots_node
    ON atomic_request_base_archive_roots(root_hash);

-- This one-row marker is the archive publication root.  Partial headers,
-- plans, uploads, and root projections are never reader-visible.
CREATE TABLE atomic_request_base_archive_completions (
    manifest_hash BYTEA PRIMARY KEY
        REFERENCES atomic_request_base_archives(manifest_hash),
    completed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE OR REPLACE FUNCTION atomic_request_base_archive_build_live(
    candidate_database_id TEXT,
    candidate_generation BIGINT
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
    SELECT EXISTS (
        SELECT 1
          FROM atomic_log_generations g
          JOIN atomic_log_generation_builds b
            ON b.database_id = g.database_id AND b.generation = g.generation
          LEFT JOIN atomic_heads h ON h.database_id = g.database_id
         WHERE g.database_id = candidate_database_id
           AND g.generation = candidate_generation
           AND g.build_kind IN (0, 2)
           AND NOT EXISTS (
               SELECT 1 FROM atomic_log_generation_activations a
                WHERE a.database_id = g.database_id
                  AND a.generation = g.generation
           )
           AND NOT EXISTS (
               SELECT 1 FROM atomic_log_generation_abandonment_progress p
                WHERE p.database_id = g.database_id
                  AND p.generation = g.generation
           )
           AND (
               (g.build_kind = 0
                AND b.source_generation IS NULL
                AND b.frozen_plan_hash IS NOT NULL
                AND h.database_id IS NULL)
               OR
               (g.build_kind = 2
                AND b.source_generation = h.log_generation
                AND b.captured_basis_t = h.basis_t
                AND b.captured_head_hash = h.tx_hash
                AND b.restore_manifest_hash IS NOT NULL
                AND b.restore_basis_t IS NOT NULL
                AND b.restore_head_hash IS NOT NULL)
           )
    )
$$;

CREATE OR REPLACE FUNCTION atomic_validate_request_base_archive_insert()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
BEGIN
    IF pg_catalog.sha256(NEW.payload) <> NEW.manifest_hash
       OR NOT atomic_request_base_archive_build_live(
           NEW.database_id, NEW.generation
       )
       OR NOT EXISTS (
           SELECT 1 FROM atomic_semantic_commitment_roots semantic
            WHERE semantic.database_id = NEW.database_id
              AND semantic.generation = NEW.generation
              AND semantic.basis_t = NEW.basis_t
              AND semantic.tx_hash = NEW.tx_hash
              AND semantic.state_hash = NEW.state_hash
              AND semantic.eidx_frontier = NEW.eidx_frontier
              AND semantic.commitment_version = 2
       ) THEN
        RAISE EXCEPTION 'Atomic request-base archive is unauthenticated or has no live restore owner'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_request_base_archives_validate_insert
BEFORE INSERT ON atomic_request_base_archives
FOR EACH ROW EXECUTE FUNCTION atomic_validate_request_base_archive_insert();

CREATE OR REPLACE FUNCTION atomic_reject_completed_request_base_archive_insert()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM atomic_request_base_archive_completions complete
         WHERE complete.manifest_hash = NEW.manifest_hash
    ) THEN
        RAISE EXCEPTION 'Atomic completed request-base archive is immutable'
            USING ERRCODE = '55000';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_request_base_archive_nodes_open_insert
BEFORE INSERT ON atomic_request_base_archive_nodes
FOR EACH ROW EXECUTE FUNCTION atomic_reject_completed_request_base_archive_insert();
CREATE TRIGGER atomic_request_base_archive_roots_open_insert
BEFORE INSERT ON atomic_request_base_archive_roots
FOR EACH ROW EXECUTE FUNCTION atomic_reject_completed_request_base_archive_insert();

CREATE OR REPLACE FUNCTION atomic_complete_request_base_archive(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    candidate_manifest_hash BYTEA
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    archive atomic_request_base_archives%ROWTYPE;
    planned BIGINT;
    valid_nodes BIGINT;
    valid_roots BIGINT;
    actual_set_hash BYTEA;
BEGIN
    SELECT * INTO archive
      FROM atomic_request_base_archives
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND manifest_hash = candidate_manifest_hash
     FOR UPDATE;
    IF NOT FOUND
       OR NOT atomic_request_base_archive_build_live(
           candidate_database_id, candidate_generation
       ) THEN
        RAISE EXCEPTION 'Atomic request-base archive lost its restore owner'
            USING ERRCODE = '55000';
    END IF;

    SELECT count(*),
           pg_catalog.sha256(
               convert_to('atomic/request-base-archive-node-set/v1', 'UTF8')
               || decode('00', 'hex')
               || int8send(count(*))
               || COALESCE(string_agg(node_hash, ''::bytea ORDER BY node_hash), ''::bytea)
           )
      INTO planned, actual_set_hash
      FROM atomic_request_base_archive_nodes
     WHERE manifest_hash = candidate_manifest_hash;
    SELECT count(*) INTO valid_nodes
      FROM atomic_request_base_archive_nodes planned
      JOIN atomic_tree_nodes stored ON stored.node_hash = planned.node_hash
     WHERE planned.manifest_hash = candidate_manifest_hash
       AND pg_catalog.sha256(stored.payload) = planned.node_hash;
    SELECT count(*) INTO valid_roots
      FROM atomic_request_base_archive_roots root
      JOIN atomic_tree_nodes stored ON stored.node_hash = root.root_hash
      JOIN atomic_request_base_archive_nodes planned
        ON planned.manifest_hash = root.manifest_hash
       AND planned.node_hash = root.root_hash
     WHERE root.manifest_hash = candidate_manifest_hash
       AND root.encoded_bytes = octet_length(stored.payload);
    IF planned <> archive.expected_node_count
       OR valid_nodes <> archive.expected_node_count
       OR actual_set_hash <> archive.node_set_hash
       OR valid_roots <> 8 THEN
        RAISE EXCEPTION 'Atomic request-base archive closure is incomplete or inconsistent'
            USING ERRCODE = '23514';
    END IF;
    INSERT INTO atomic_request_base_archive_completions(manifest_hash)
    VALUES (candidate_manifest_hash)
    ON CONFLICT DO NOTHING;
END;
$$;

-- A request binding may name either a normal publication made by the native
-- writer or an archive publication made only by restore.  A conditional FK
-- cannot express that sum type; the immutable binding trigger below is the
-- exact relational authority for both variants.
ALTER TABLE atomic_generation_request_bases
    DROP CONSTRAINT atomic_generation_request_bases_base_manifest_hash_fkey;

CREATE OR REPLACE FUNCTION atomic_validate_generation_request_base_insert()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
DECLARE
    lock_key BIGINT;
BEGIN
    lock_key := (('x' || encode(substring(NEW.base_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
                # 4707465863597391872::bigint;
    PERFORM pg_catalog.pg_advisory_xact_lock_shared(lock_key);

    IF NOT EXISTS (
        SELECT 1
          FROM atomic_generation_requests request
         WHERE request.database_id = NEW.database_id
           AND request.generation = NEW.generation
           AND request.request_key_hash = NEW.request_key_hash
           AND request.request_kind = 2
           AND (
               EXISTS (
                   SELECT 1
                     FROM atomic_tree_publications publication
                     JOIN atomic_tree_manifests manifest
                       ON manifest.manifest_hash = publication.manifest_hash
                      AND manifest.database_id = publication.database_id
                      AND manifest.publication_revision = publication.publication_revision
                      AND manifest.basis_t = publication.basis_t
                      AND manifest.tx_hash = publication.tx_hash
                      AND manifest.log_generation = publication.log_generation
                     JOIN atomic_semantic_commitment_roots semantic
                       ON semantic.database_id = manifest.database_id
                      AND semantic.generation = manifest.log_generation
                      AND semantic.basis_t = manifest.basis_t
                      AND semantic.tx_hash = manifest.tx_hash
                      AND semantic.state_hash = manifest.state_hash
                      AND semantic.eidx_frontier = manifest.eidx_frontier
                      AND semantic.commitment_version = 2
                    WHERE publication.manifest_hash = NEW.base_manifest_hash
                      AND publication.database_id = request.database_id
                      AND publication.log_generation = request.generation
                      AND publication.basis_t <= request.basis_t - 1
                      AND manifest.manifest_version = 4
                      AND NOT EXISTS (
                          SELECT 1 FROM atomic_tree_retirement_progress progress
                           WHERE progress.manifest_hash = publication.manifest_hash
                      )
               )
               OR EXISTS (
                   SELECT 1
                     FROM atomic_request_base_archives archive
                     JOIN atomic_request_base_archive_completions complete
                       ON complete.manifest_hash = archive.manifest_hash
                     JOIN atomic_semantic_commitment_roots semantic
                       ON semantic.database_id = archive.database_id
                      AND semantic.generation = archive.generation
                      AND semantic.basis_t = archive.basis_t
                      AND semantic.tx_hash = archive.tx_hash
                      AND semantic.state_hash = archive.state_hash
                      AND semantic.eidx_frontier = archive.eidx_frontier
                      AND semantic.commitment_version = 2
                    WHERE archive.manifest_hash = NEW.base_manifest_hash
                      AND archive.database_id = request.database_id
                      AND archive.generation = request.generation
                      AND archive.basis_t <= request.basis_t - 1
                      AND archive.manifest_version = 4
                      AND (atomic_request_base_archive_build_live(
                               archive.database_id, archive.generation
                           ) OR EXISTS (
                               SELECT 1 FROM atomic_heads head
                                WHERE head.database_id = archive.database_id
                                  AND head.log_generation = archive.generation
                           ))
               )
           )
    ) THEN
        RAISE EXCEPTION 'Atomic native request base is absent, late, retired, or unauthenticated'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

-- Root pins and generation pins serialize this bounded release with exact
-- receipt readers.  The owning generation must already be retired or carry
-- an irreversible abandonment claim; a resumable restore remains untouched.
CREATE OR REPLACE FUNCTION atomic_collect_request_base_archive(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    candidate_manifest_hash BYTEA,
    older_than_millis BIGINT,
    maximum_nodes BIGINT
)
RETURNS TABLE(rows_removed BIGINT, is_complete BOOLEAN)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    archive_row atomic_request_base_archives%ROWTYPE;
    generation_row atomic_log_generations%ROWTYPE;
    build_row atomic_log_generation_builds%ROWTYPE;
    retirement_row atomic_log_generation_retirements%ROWTYPE;
    generation_lock BIGINT;
    manifest_lock BIGINT;
    builder_lock BIGINT;
    worker_lock BIGINT;
    restore_lock BIGINT;
    durable_lineage TEXT;
    durable_genesis_hash BYTEA;
    already_claimed BOOLEAN := false;
    abandoned BOOLEAN := false;
    removed BIGINT := 0;
    one_removed BIGINT := 0;
BEGIN
    IF candidate_generation <= 0 OR octet_length(candidate_manifest_hash) <> 32
       OR older_than_millis < 0 OR maximum_nodes < 1 OR maximum_nodes > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic request-base archive garbage boundary'
            USING ERRCODE = '22023';
    END IF;
    SELECT * INTO archive_row
      FROM atomic_request_base_archives
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND manifest_hash = candidate_manifest_hash
     FOR UPDATE;
    IF NOT FOUND THEN
        RETURN QUERY SELECT 0::bigint, true;
        RETURN;
    END IF;
    SELECT atomic_log_generation_pin_key(candidate_database_id, candidate_generation)
      INTO generation_lock;
    manifest_lock := (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
                     # 4707465863597391872::bigint;
    IF generation_lock IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock(generation_lock)
       OR NOT pg_catalog.pg_try_advisory_xact_lock(manifest_lock) THEN
        RAISE EXCEPTION 'Atomic request-base archive is pinned by a live reader'
            USING ERRCODE = '55006';
    END IF;

    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND build_kind IN (0, 2)
     FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic request-base archive lost its generation owner'
            USING ERRCODE = '23503';
    END IF;
    SELECT * INTO build_row
      FROM atomic_log_generation_builds
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
     FOR UPDATE;
    SELECT * INTO retirement_row
      FROM atomic_log_generation_retirements
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
     FOR UPDATE;
    IF EXISTS (
        SELECT 1 FROM atomic_heads
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Active Atomic request-base archive cannot be collected'
            USING ERRCODE = '55000';
    END IF;

    IF retirement_row.generation IS NOT NULL THEN
        IF retirement_row.collecting_at IS NULL
           AND retirement_row.retired_at
               + older_than_millis * interval '1 millisecond' > clock_timestamp() THEN
            RAISE EXCEPTION 'Atomic request-base archive has not reached its retention age'
                USING ERRCODE = '55000';
        END IF;
    ELSE
        abandoned := true;
        IF build_row.generation IS NULL
           OR EXISTS (
               SELECT 1 FROM atomic_log_generation_activations
                WHERE database_id = candidate_database_id
                  AND generation = candidate_generation
           ) THEN
            RAISE EXCEPTION 'Atomic request-base archive generation is not an inactive restore'
                USING ERRCODE = '55000';
        END IF;
        SELECT EXISTS (
            SELECT 1 FROM atomic_log_generation_abandonment_progress
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
        ) INTO already_claimed;
        SELECT lineage_id, genesis_hash
          INTO durable_lineage, durable_genesis_hash
          FROM atomic_databases WHERE database_id = candidate_database_id;
        IF generation_row.build_kind = 0 AND (
            build_row.source_generation IS NOT NULL
            OR build_row.captured_basis_t <> 0
            OR build_row.captured_head_hash <> durable_genesis_hash
            OR build_row.frozen_plan_hash IS NULL
            OR build_row.restore_manifest_hash IS NOT NULL
            OR build_row.restore_basis_t IS NOT NULL
            OR build_row.restore_head_hash IS NOT NULL
            OR EXISTS (SELECT 1 FROM atomic_heads WHERE database_id = candidate_database_id)
            OR EXISTS (
                SELECT 1 FROM atomic_log_generations other
                 WHERE other.database_id = candidate_database_id
                   AND other.generation <> candidate_generation
            )
        ) THEN
            RAISE EXCEPTION 'Atomic initial restore is not an isolated authenticated headless build'
                USING ERRCODE = '55000';
        ELSIF generation_row.build_kind = 2
              AND build_row.source_generation IS NULL THEN
            RAISE EXCEPTION 'Atomic point restore abandonment has no captured source generation'
                USING ERRCODE = '23503';
        END IF;
        IF NOT already_claimed AND build_row.source_generation IS NOT NULL AND EXISTS (
            SELECT 1 FROM atomic_heads
             WHERE database_id = candidate_database_id
               AND log_generation = build_row.source_generation
        ) THEN
            RAISE EXCEPTION 'Atomic inactive restore remains exactly resumable'
                USING ERRCODE = '55000';
        END IF;
        IF NOT already_claimed AND generation_row.created_at
           + older_than_millis * interval '1 millisecond' > clock_timestamp() THEN
            RAISE EXCEPTION 'Atomic inactive restore has not reached its retention age'
                USING ERRCODE = '55000';
        END IF;
        SELECT atomic_tree_database_build_pin_key(candidate_database_id)
          INTO builder_lock;
        worker_lock := hashtextextended(
            'atomic/excision-worker/v1/' || durable_lineage,
            4707476001900298240::bigint
        );
        restore_lock := hashtextextended(
            'atomic/restore/' || candidate_database_id,
            0
        );
        IF builder_lock IS NULL OR worker_lock IS NULL OR restore_lock IS NULL
           OR NOT pg_catalog.pg_try_advisory_xact_lock(builder_lock)
           OR NOT pg_catalog.pg_try_advisory_xact_lock(worker_lock)
           OR NOT pg_catalog.pg_try_advisory_xact_lock(restore_lock) THEN
            RAISE EXCEPTION 'Atomic inactive restore is pinned by a live builder or reader'
                USING ERRCODE = '55006';
        END IF;
    END IF;

    PERFORM set_config('atomic.log_generation_gc', 'v14', true);
    PERFORM set_config('atomic.request_base_archive_gc', 'v20', true);
    IF abandoned THEN
        PERFORM set_config('atomic.log_generation_activation', 'v14', true);
        INSERT INTO atomic_log_generation_abandonment_progress(database_id, generation)
        VALUES (candidate_database_id, candidate_generation)
        ON CONFLICT DO NOTHING;
    ELSE
        IF retirement_row.collecting_at IS NULL THEN
            UPDATE atomic_log_generation_retirements
               SET collecting_at = clock_timestamp()
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
        END IF;
        INSERT INTO atomic_log_generation_collection_progress(database_id, generation)
        VALUES (candidate_database_id, candidate_generation)
        ON CONFLICT DO NOTHING;
    END IF;
    WITH victims AS MATERIALIZED (
        SELECT base.ctid
          FROM atomic_generation_request_bases base
         WHERE base.database_id = candidate_database_id
           AND base.generation = candidate_generation
           AND base.base_manifest_hash = candidate_manifest_hash
         ORDER BY base.request_key_hash
         LIMIT maximum_nodes
         FOR UPDATE
    )
    DELETE FROM atomic_generation_request_bases base
     USING victims
     WHERE base.ctid = victims.ctid;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    IF EXISTS (
        SELECT 1 FROM atomic_generation_request_bases
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
           AND base_manifest_hash = candidate_manifest_hash
    ) THEN
        PERFORM set_config('atomic.request_base_archive_gc', 'off', true);
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        PERFORM set_config('atomic.log_generation_gc', 'off', true);
        RETURN QUERY SELECT removed, false;
        RETURN;
    END IF;
    DELETE FROM atomic_request_base_archive_completions
     WHERE manifest_hash = candidate_manifest_hash;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    INSERT INTO atomic_tree_garbage_nodes(node_hash, marked_at)
    SELECT planned.node_hash, clock_timestamp()
      FROM atomic_request_base_archive_nodes planned
      JOIN atomic_tree_nodes stored ON stored.node_hash = planned.node_hash
     WHERE planned.manifest_hash = candidate_manifest_hash
     ORDER BY planned.node_hash LIMIT maximum_nodes
    ON CONFLICT (node_hash) DO NOTHING;
    WITH batch AS MATERIALIZED (
        SELECT node_hash FROM atomic_request_base_archive_nodes
         WHERE manifest_hash = candidate_manifest_hash
         ORDER BY node_hash LIMIT maximum_nodes FOR UPDATE
    )
    DELETE FROM atomic_request_base_archive_nodes node
     USING batch
     WHERE node.manifest_hash = candidate_manifest_hash
       AND node.node_hash = batch.node_hash;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    IF EXISTS (
        SELECT 1 FROM atomic_request_base_archive_nodes
         WHERE manifest_hash = candidate_manifest_hash
    ) THEN
        PERFORM set_config('atomic.request_base_archive_gc', 'off', true);
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        PERFORM set_config('atomic.log_generation_gc', 'off', true);
        RETURN QUERY SELECT removed, false;
        RETURN;
    END IF;
    DELETE FROM atomic_request_base_archive_roots
     WHERE manifest_hash = candidate_manifest_hash;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    DELETE FROM atomic_request_base_archives
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND manifest_hash = candidate_manifest_hash;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    PERFORM set_config('atomic.request_base_archive_gc', 'off', true);
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
    PERFORM set_config('atomic.log_generation_gc', 'off', true);
    RETURN QUERY SELECT removed, true;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_reject_request_base_archive_mutation()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
DECLARE
    relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF TG_OP = 'DELETE'
       AND current_user = relation_owner
       AND pg_catalog.current_setting('atomic.request_base_archive_gc', true) = 'v20' THEN
        RETURN OLD;
    END IF;
    RAISE EXCEPTION 'Atomic request-base archive records are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE TRIGGER atomic_request_base_archives_immutable
BEFORE UPDATE OR DELETE ON atomic_request_base_archives
FOR EACH ROW EXECUTE FUNCTION atomic_reject_request_base_archive_mutation();
CREATE TRIGGER atomic_request_base_archive_nodes_immutable
BEFORE UPDATE OR DELETE ON atomic_request_base_archive_nodes
FOR EACH ROW EXECUTE FUNCTION atomic_reject_request_base_archive_mutation();
CREATE TRIGGER atomic_request_base_archive_roots_immutable
BEFORE UPDATE OR DELETE ON atomic_request_base_archive_roots
FOR EACH ROW EXECUTE FUNCTION atomic_reject_request_base_archive_mutation();
CREATE TRIGGER atomic_request_base_archive_completions_immutable
BEFORE UPDATE OR DELETE ON atomic_request_base_archive_completions
FOR EACH ROW EXECUTE FUNCTION atomic_reject_request_base_archive_mutation();

-- Raw node deletion must honor both complete and partial archive plans.  A
-- planned hash is installed before its value, so this is safe across crashes
-- at every content-first upload boundary.
CREATE OR REPLACE FUNCTION atomic_collect_tree_garbage(
    maximum_nodes BIGINT
)
RETURNS SETOF BYTEA
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF maximum_nodes < 1 OR maximum_nodes > 4096 THEN
        RAISE EXCEPTION 'Atomic tree garbage batch must be between 1 and 4096'
            USING ERRCODE = '22023';
    END IF;
    IF EXISTS (
        SELECT 1 FROM (
            SELECT DISTINCT ON (database_id) database_id, manifest_hash
              FROM atomic_tree_publications
             ORDER BY database_id, publication_revision DESC
        ) current_root
        LEFT JOIN atomic_tree_live_sets live USING (database_id)
        WHERE live.database_id IS NULL
           OR live.manifest_hash <> current_root.manifest_hash
           OR NOT live.complete
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_manifest_roots root
        JOIN atomic_tree_live_sets live ON live.manifest_hash = root.manifest_hash
        WHERE live.complete AND NOT EXISTS (
            SELECT 1 FROM atomic_tree_live_nodes node
             WHERE node.database_id = live.database_id
               AND node.node_hash = root.root_hash
        )
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_retirements WHERE NOT garbage_complete
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_publications publication
         WHERE EXISTS (
             SELECT 1 FROM atomic_tree_publications newer
              WHERE newer.database_id = publication.database_id
                AND newer.publication_revision > publication.publication_revision
         ) AND NOT EXISTS (
             SELECT 1 FROM atomic_tree_retirements retirement
              WHERE retirement.database_id = publication.database_id
                AND retirement.publication_revision = publication.publication_revision
                AND retirement.manifest_hash = publication.manifest_hash
         )
    ) THEN
        RETURN;
    END IF;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    RETURN QUERY
    WITH candidates AS MATERIALIZED (
        SELECT garbage.node_hash
          FROM atomic_tree_garbage_nodes garbage
         WHERE NOT EXISTS (SELECT 1 FROM atomic_tree_live_nodes live
                            WHERE live.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_tree_retired_nodes retired
                            WHERE retired.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_tree_delta_nodes delta
                            WHERE delta.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intent_nodes intent
                            WHERE intent.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_tree_manifest_roots root
                            WHERE root.root_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_nodes archive
                            WHERE archive.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_roots archive_root
                            WHERE archive_root.root_hash = garbage.node_hash)
         ORDER BY garbage.node_hash
         LIMIT maximum_nodes FOR UPDATE OF garbage SKIP LOCKED
    )
    DELETE FROM atomic_tree_nodes node USING candidates
     WHERE node.node_hash = candidates.node_hash
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_live_nodes live
                        WHERE live.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_retired_nodes retired
                        WHERE retired.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_delta_nodes delta
                        WHERE delta.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intent_nodes intent
                        WHERE intent.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_manifest_roots root
                        WHERE root.root_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_nodes archive
                        WHERE archive.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_roots archive_root
                        WHERE archive_root.root_hash = node.node_hash)
    RETURNING node.node_hash;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
END;
$$;

REVOKE ALL ON FUNCTION atomic_request_base_archive_build_live(TEXT, BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_validate_request_base_archive_insert() FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_reject_completed_request_base_archive_insert() FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_complete_request_base_archive(TEXT, BIGINT, BYTEA) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_collect_request_base_archive(TEXT, BIGINT, BYTEA, BIGINT, BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_reject_request_base_archive_mutation() FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_validate_generation_request_base_insert() FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_collect_tree_garbage(BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_complete_request_base_archive(TEXT, BIGINT, BYTEA) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION atomic_collect_request_base_archive(TEXT, BIGINT, BYTEA, BIGINT, BIGINT) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_garbage(BIGINT) TO CURRENT_USER;

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_request_base_archives FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_request_base_archive_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_request_base_archive_roots FROM PUBLIC;
REVOKE INSERT, UPDATE, DELETE, TRUNCATE ON atomic_request_base_archive_completions FROM PUBLIC;
