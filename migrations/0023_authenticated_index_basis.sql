-- ATIM v6 authenticates the physical index basis independently from the
-- logical transaction basis. V4 and V5 remain readable: immutable manifests
-- retain their original hashes, while every new publication is V6.
ALTER TABLE atomic_tree_manifests
    DROP CONSTRAINT atomic_tree_manifests_manifest_version_check;

ALTER TABLE atomic_tree_manifests
    ADD CONSTRAINT atomic_tree_manifests_manifest_version_check
    CHECK (manifest_version IN (4, 5, 6));

-- Keep the relational projection nullable for immutable predecessor formats:
-- V4/V5 never authenticated this value. Every V6 row carries the exact
-- envelope value and SQL independently rejects an impossible future basis.
ALTER TABLE atomic_tree_manifests
    ADD COLUMN index_basis_t BIGINT,
    ADD CONSTRAINT atomic_tree_manifests_index_basis_check CHECK (
        (manifest_version IN (4, 5) AND index_basis_t IS NULL)
        OR (manifest_version = 6 AND index_basis_t IS NOT NULL
            AND index_basis_t >= 0 AND index_basis_t <= basis_t)
    );

ALTER TABLE atomic_request_base_archives
    DROP CONSTRAINT atomic_request_base_archives_manifest_version_check;

ALTER TABLE atomic_request_base_archives
    ADD CONSTRAINT atomic_request_base_archives_manifest_version_check
    CHECK (manifest_version IN (4, 5, 6));

-- Physical progress is monotonic within one log generation. Intermediate
-- same-basis AVET roots retain the predecessor value and the final root may
-- advance it; no direct SQL publisher may move it backwards or reintroduce a
-- pre-V6 publication after an authenticated V6 coordinate exists.
CREATE FUNCTION atomic_validate_tree_index_basis_progress()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    candidate_index_basis BIGINT;
    current_index_basis BIGINT;
    current_generation BIGINT;
BEGIN
    PERFORM database_id
      FROM atomic_databases
     WHERE database_id = NEW.database_id
       FOR UPDATE;
    IF NOT FOUND THEN
        RETURN NEW;
    END IF;

    SELECT manifest.index_basis_t INTO candidate_index_basis
      FROM atomic_tree_manifests manifest
     WHERE manifest.database_id = NEW.database_id
       AND manifest.publication_revision = NEW.publication_revision
       AND manifest.basis_t = NEW.basis_t
       AND manifest.tx_hash = NEW.tx_hash
       AND manifest.manifest_hash = NEW.manifest_hash
       AND manifest.log_generation = NEW.log_generation;
    IF NOT FOUND THEN
        RETURN NEW;
    END IF;

    SELECT publication.log_generation, manifest.index_basis_t
      INTO current_generation, current_index_basis
      FROM atomic_tree_publications publication
      JOIN atomic_tree_manifests manifest
        ON manifest.manifest_hash = publication.manifest_hash
       AND manifest.database_id = publication.database_id
       AND manifest.publication_revision = publication.publication_revision
       AND manifest.basis_t = publication.basis_t
       AND manifest.tx_hash = publication.tx_hash
       AND manifest.log_generation = publication.log_generation
     WHERE publication.database_id = NEW.database_id
     ORDER BY publication.publication_revision DESC
     LIMIT 1;
    IF FOUND AND current_generation = NEW.log_generation
       AND current_index_basis IS NOT NULL
       AND (candidate_index_basis IS NULL
            OR candidate_index_basis < current_index_basis) THEN
        RAISE EXCEPTION 'Atomic tree publication index basis cannot regress'
            USING ERRCODE = '40001';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_tree_publications_index_basis_monotonic
BEFORE INSERT ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_index_basis_progress();

REVOKE ALL ON FUNCTION atomic_validate_tree_index_basis_progress() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_validate_tree_index_basis_progress() TO CURRENT_USER;

-- Migration 0021 installed this request-base fence with a literal V4/V5
-- format test. Preserve its exact authenticated live-or-archive closure while
-- admitting V6 manifests. The index basis itself stays inside the checksummed
-- payload; SQL never gets a second, unauthenticated progress coordinate.
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
                      AND manifest.manifest_version IN (4, 5, 6)
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
                      AND archive.manifest_version IN (4, 5, 6)
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
