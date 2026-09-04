-- A native transaction report is an immutable database value at the exact
-- db-before/db-after coordinates, not an as-of filter over whichever root is
-- newest when an acknowledgement is retried.  Native service requests bind
-- their db-before to one published, authenticated tree base.  The binding is
-- small and immutable; retaining the named publication is what makes bounded
-- receipt reconstruction possible after restart.

-- 0 = COW replay tombstone, 1 = pre-binding ordinary request (creation,
-- restore, and the compatibility writer), 2 = native ordinary request whose
-- head publication requires an exact tree-base binding.  Migration 17 does
-- not relabel existing rows: the bounded writer switches new commits to kind
-- 2 only when it can insert the binding in the same transaction.
ALTER TABLE atomic_generation_requests
    DROP CONSTRAINT atomic_generation_requests_request_kind_check,
    ADD CONSTRAINT atomic_generation_requests_request_kind_check
        CHECK (request_kind IN (0, 1, 2));

CREATE TABLE atomic_generation_request_bases (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    request_key_hash BYTEA NOT NULL CHECK (octet_length(request_key_hash) = 32),
    base_manifest_hash BYTEA NOT NULL CHECK (octet_length(base_manifest_hash) = 32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation, request_key_hash),
    FOREIGN KEY (database_id, generation, request_key_hash)
        REFERENCES atomic_generation_requests
                   (database_id, generation, request_key_hash)
        ON DELETE CASCADE,
    FOREIGN KEY (base_manifest_hash)
        REFERENCES atomic_tree_publications(manifest_hash)
);

CREATE INDEX atomic_generation_request_bases_manifest
    ON atomic_generation_request_bases(base_manifest_hash);

-- Serialize binding with the root collector using the same advisory-lock
-- coordinate as connected immutable snapshots.  The relational joins repeat
-- the reader's authentication boundary: one publication, one canonical
-- manifest, one generation-qualified log/semantic coordinate, and a base no
-- later than the request's db-before.
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
          JOIN atomic_tree_publications publication
            ON publication.manifest_hash = NEW.base_manifest_hash
           AND publication.database_id = request.database_id
           AND publication.log_generation = request.generation
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
         WHERE request.database_id = NEW.database_id
           AND request.generation = NEW.generation
           AND request.request_key_hash = NEW.request_key_hash
           AND request.request_kind = 2
           AND publication.basis_t <= request.basis_t - 1
           AND manifest.manifest_version = 4
           AND NOT EXISTS (
                SELECT 1
                  FROM atomic_tree_retirement_progress progress
                 WHERE progress.manifest_hash = NEW.base_manifest_hash
           )
    ) THEN
        RAISE EXCEPTION 'Atomic native request base is absent, late, retired, or unauthenticated'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_generation_request_bases_validate_insert
BEFORE INSERT ON atomic_generation_request_bases
FOR EACH ROW EXECUTE FUNCTION atomic_validate_generation_request_base_insert();

-- Bindings are permanent while their request generation is active.  Once a
-- generation is inactive, the existing root collector may release a binding
-- immediately before it removes that publication.  This ordering avoids a
-- cycle: generation GC already requires tree publications to disappear
-- before its later request-row phase.
CREATE OR REPLACE FUNCTION atomic_reject_generation_request_base_mutation()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
DECLARE
    relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner)
      INTO relation_owner
      FROM pg_catalog.pg_class
     WHERE oid = TG_RELID;
    IF TG_OP = 'DELETE'
       AND current_user = relation_owner
       AND (
            pg_catalog.current_setting('atomic.tree_gc_active', true) = 'v13'
            OR pg_catalog.current_setting('atomic.log_generation_gc', true) = 'v14'
       ) THEN
        RETURN OLD;
    END IF;
    RAISE EXCEPTION 'Atomic request base bindings are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE TRIGGER atomic_generation_request_bases_immutable
BEFORE UPDATE OR DELETE ON atomic_generation_request_bases
FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_request_base_mutation();

-- Keep migration 13 byte-for-byte intact.  The wrapper obtains its exclusive
-- lock before testing the durable pin, then delegates to the original bounded
-- collector under the same re-entrant transaction lock.
ALTER FUNCTION atomic_collect_tree_retirement(TEXT, BIGINT, BYTEA, BIGINT, BIGINT)
    RENAME TO atomic_collect_tree_retirement_unbound_v13;

CREATE OR REPLACE FUNCTION atomic_collect_tree_retirement(
    candidate_database_id TEXT,
    candidate_revision BIGINT,
    candidate_manifest_hash BYTEA,
    older_than_millis BIGINT,
    maximum_nodes BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    lock_key BIGINT;
BEGIN
    IF octet_length(candidate_manifest_hash) <> 32
       OR older_than_millis < 0
       OR maximum_nodes < 1
       OR maximum_nodes > 4096 THEN
        RETURN FALSE;
    END IF;
    lock_key := (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
                # 4707465863597391872::bigint;
    IF NOT pg_catalog.pg_try_advisory_xact_lock(lock_key) THEN
        RETURN FALSE;
    END IF;
    IF EXISTS (
        SELECT 1
          FROM atomic_generation_request_bases base
          JOIN atomic_tree_publications publication
            ON publication.manifest_hash = base.base_manifest_hash
          JOIN atomic_heads head
            ON head.database_id = publication.database_id
           AND head.log_generation = publication.log_generation
         WHERE base.base_manifest_hash = candidate_manifest_hash
           AND publication.database_id = candidate_database_id
           AND publication.publication_revision = candidate_revision
    ) THEN
        RETURN FALSE;
    END IF;
    RETURN atomic_collect_tree_retirement_unbound_v13(
        candidate_database_id,
        candidate_revision,
        candidate_manifest_hash,
        older_than_millis,
        maximum_nodes
    );
END;
$$;

-- This trigger runs only after the v13 collector has authenticated, claimed,
-- and drained the retirement.  Active bindings were rejected by the wrapper;
-- inactive-generation bindings can now be released without weakening any
-- resolvable request or racing a connected historical snapshot.
CREATE OR REPLACE FUNCTION atomic_release_inactive_request_bases()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
DECLARE
    relation_owner NAME;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM atomic_generation_request_bases
         WHERE base_manifest_hash = OLD.manifest_hash
    ) THEN
        RETURN OLD;
    END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner)
      INTO relation_owner
      FROM pg_catalog.pg_class
     WHERE oid = TG_RELID;
    IF current_user <> relation_owner
       OR pg_catalog.current_setting('atomic.tree_gc_active', true) <> 'v13' THEN
        RAISE EXCEPTION 'Atomic request base can be released only by tree garbage collection'
            USING ERRCODE = '55000';
    END IF;
    IF EXISTS (
        SELECT 1 FROM atomic_heads
         WHERE database_id = OLD.database_id
           AND log_generation = OLD.log_generation
    ) THEN
        RAISE EXCEPTION 'Atomic active request base cannot be retired'
            USING ERRCODE = '55000';
    END IF;
    DELETE FROM atomic_generation_request_bases
     WHERE base_manifest_hash = OLD.manifest_hash;
    RETURN OLD;
END;
$$;

CREATE TRIGGER atomic_tree_publications_release_request_bases
BEFORE DELETE ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_release_inactive_request_bases();

-- A kind-2 request is not a complete publication until its exact db-before
-- base is durable.  Kinds 0/1 retain their migration-14 behavior so creation,
-- COW replay, restore, and the not-yet-switched compatibility writer continue
-- to function while the bounded writer is integrated in a later commit.
CREATE OR REPLACE FUNCTION atomic_validate_head_advance()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
DECLARE
    relation_owner NAME;
BEGIN
    IF NEW.database_id <> OLD.database_id THEN
        RAISE EXCEPTION 'Atomic head database identity is immutable'
            USING ERRCODE = '55000';
    END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF NEW.log_generation <> OLD.log_generation THEN
        IF pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14'
           AND current_user = relation_owner THEN
            RETURN NEW;
        END IF;
        RAISE EXCEPTION 'Atomic log generation can change only through verified activation'
            USING ERRCODE = '55000';
    END IF;
    IF NEW.basis_t <> OLD.basis_t + 1 THEN
        RAISE EXCEPTION 'Atomic head must advance by exactly one basis'
            USING ERRCODE = '40001';
    END IF;
    IF NEW.log_generation = 0 THEN
        IF NOT EXISTS (
            SELECT 1 FROM atomic_transactions
             WHERE database_id = NEW.database_id
               AND basis_t = NEW.basis_t
               AND previous_hash = OLD.tx_hash
               AND tx_hash = NEW.tx_hash
        ) OR NOT EXISTS (
            SELECT 1 FROM atomic_requests
             WHERE database_id = NEW.database_id
               AND basis_t = NEW.basis_t
               AND tx_hash = NEW.tx_hash
        ) THEN
            RAISE EXCEPTION 'Atomic head does not identify a complete legacy transaction publication'
                USING ERRCODE = '23503';
        END IF;
    ELSE
        IF NOT EXISTS (
            SELECT 1 FROM atomic_generation_transactions
             WHERE database_id = NEW.database_id
               AND generation = NEW.log_generation
               AND basis_t = NEW.basis_t
               AND previous_hash = OLD.tx_hash
               AND tx_hash = NEW.tx_hash
        ) OR NOT EXISTS (
            SELECT 1
              FROM atomic_generation_requests request
             WHERE request.database_id = NEW.database_id
               AND request.generation = NEW.log_generation
               AND request.basis_t = NEW.basis_t
               AND request.tx_hash = NEW.tx_hash
               AND (
                    request.request_kind IN (0, 1)
                    OR (
                        request.request_kind = 2
                        AND EXISTS (
                            SELECT 1
                              FROM atomic_generation_request_bases base
                             WHERE base.database_id = request.database_id
                               AND base.generation = request.generation
                               AND base.request_key_hash = request.request_key_hash
                        )
                    )
               )
        ) THEN
            RAISE EXCEPTION 'Atomic head does not identify a complete lineage transaction publication'
                USING ERRCODE = '23503';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

REVOKE ALL ON FUNCTION atomic_validate_generation_request_base_insert() FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_reject_generation_request_base_mutation() FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_collect_tree_retirement_unbound_v13(TEXT, BIGINT, BYTEA, BIGINT, BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_collect_tree_retirement(TEXT, BIGINT, BYTEA, BIGINT, BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_release_inactive_request_bases() FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_validate_head_advance() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_retirement(TEXT, BIGINT, BYTEA, BIGINT, BIGINT) TO CURRENT_USER;

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_generation_request_bases FROM PUBLIC;
