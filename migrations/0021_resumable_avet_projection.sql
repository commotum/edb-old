-- Version 5 authenticates fixed-size pending AVET projection coordinates in
-- the immutable manifest. Version 4 remains readable so an in-place upgrade
-- can finish roots published by the preceding binary.
ALTER TABLE atomic_tree_manifests
    DROP CONSTRAINT atomic_tree_manifests_manifest_version_check;

ALTER TABLE atomic_tree_manifests
    ADD CONSTRAINT atomic_tree_manifests_manifest_version_check
    CHECK (manifest_version IN (4, 5));

ALTER TABLE atomic_request_base_archives
    DROP CONSTRAINT atomic_request_base_archives_manifest_version_check;

ALTER TABLE atomic_request_base_archives
    ADD CONSTRAINT atomic_request_base_archives_manifest_version_check
    CHECK (manifest_version IN (4, 5));

-- Migration 0020 installed this insertion fence with a literal v4 test. Keep
-- the exact authenticated live-or-archive closure while allowing both the
-- readable predecessor codec and the new pending-projection codec.
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
                      AND manifest.manifest_version IN (4, 5)
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
                      AND archive.manifest_version IN (4, 5)
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
