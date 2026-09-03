-- Copy-on-write authoritative log generations.
--
-- Generation zero remains the exact alias-bound v3 payload representation in
-- atomic_transactions/atomic_requests. Those bytes are never relabelled as a
-- lineage format. New databases and every excision/restore candidate use the
-- lineage-bound ATLG v1 envelope in the append-only tables below.

DROP TABLE IF EXISTS atomic_active_programs;
DROP TABLE IF EXISTS atomic_program_versions;
DROP TABLE IF EXISTS atomic_excisions;

CREATE SEQUENCE atomic_log_generation_ids AS BIGINT MINVALUE 1 NO CYCLE;

ALTER TABLE atomic_databases
    ADD CONSTRAINT atomic_databases_id_lineage_key
        UNIQUE (database_id, lineage_id);

ALTER TABLE atomic_heads
    ADD COLUMN log_generation BIGINT NOT NULL DEFAULT 0
        CHECK (log_generation >= 0);

-- The old mutable mirror could disagree with the head. Preserve its read
-- shape as a projection while making the active head the sole publication.
DROP TABLE atomic_database_generations;
CREATE VIEW atomic_database_generations AS
SELECT database_id, log_generation AS excision_generation
  FROM atomic_heads;

CREATE TABLE atomic_log_generations (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL DEFAULT nextval('atomic_log_generation_ids')
        CHECK (generation > 0),
    lineage_id TEXT NOT NULL,
    -- 0=new database, 1=excision, 2=same-lineage point restore.
    build_kind SMALLINT NOT NULL CHECK (build_kind BETWEEN 0 AND 2),
    request_set_hash BYTEA NOT NULL CHECK (octet_length(request_set_hash) = 32),
    request_count BIGINT NOT NULL CHECK (request_count >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation),
    FOREIGN KEY (database_id, lineage_id)
        REFERENCES atomic_databases(database_id, lineage_id),
    CHECK ((build_kind = 0 AND request_count = 0)
        OR (build_kind = 1 AND request_count > 0)
        OR (build_kind = 2 AND request_count = 0))
);

-- Removable build coordination. Unlike permanent A=15 audit information,
-- these source hashes must not survive a successful privacy cutover.
CREATE TABLE atomic_log_generation_builds (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    source_generation BIGINT CHECK (source_generation >= 0),
    captured_basis_t BIGINT NOT NULL CHECK (captured_basis_t >= 0),
    captured_head_hash BYTEA NOT NULL CHECK (octet_length(captured_head_hash) = 32),
    restore_manifest_hash BYTEA CHECK (
        restore_manifest_hash IS NULL OR octet_length(restore_manifest_hash) = 32
    ),
    restore_basis_t BIGINT CHECK (restore_basis_t IS NULL OR restore_basis_t >= 0),
    restore_head_hash BYTEA CHECK (
        restore_head_hash IS NULL OR octet_length(restore_head_hash) = 32
    ),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generations(database_id, generation),
    CHECK ((source_generation IS NULL AND restore_manifest_hash IS NULL
            AND restore_basis_t IS NULL AND restore_head_hash IS NULL)
        OR (source_generation IS NOT NULL AND restore_manifest_hash IS NULL
            AND restore_basis_t IS NULL AND restore_head_hash IS NULL)
        OR (source_generation IS NOT NULL AND restore_manifest_hash IS NOT NULL
            AND restore_basis_t IS NOT NULL AND restore_head_hash IS NOT NULL))
);

CREATE TABLE atomic_generation_transactions (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    lineage_id TEXT NOT NULL,
    basis_t BIGINT NOT NULL CHECK (basis_t > 0),
    previous_hash BYTEA NOT NULL CHECK (octet_length(previous_hash) = 32),
    tx_hash BYTEA NOT NULL CHECK (octet_length(tx_hash) = 32),
    state_hash BYTEA NOT NULL CHECK (
        octet_length(state_hash) = 32
        AND state_hash <> decode(repeat('00', 32), 'hex')
    ),
    eidx_frontier BIGINT NOT NULL CHECK (eidx_frontier > 0),
    envelope_version SMALLINT NOT NULL DEFAULT 1 CHECK (envelope_version = 1),
    payload BYTEA NOT NULL CHECK (octet_length(payload) >= 58),
    committed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation, basis_t),
    UNIQUE (database_id, generation, tx_hash),
    UNIQUE (database_id, generation, basis_t, tx_hash),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generations(database_id, generation)
);

CREATE TABLE atomic_generation_source_links (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    basis_t BIGINT NOT NULL CHECK (basis_t > 0),
    source_tx_hash BYTEA NOT NULL CHECK (octet_length(source_tx_hash) = 32),
    PRIMARY KEY (database_id, generation, basis_t),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generation_builds(database_id, generation),
    FOREIGN KEY (database_id, generation, basis_t)
        REFERENCES atomic_generation_transactions(database_id, generation, basis_t)
);

-- The caller-supplied request key is deliberately absent. A lineage-scoped
-- digest supports idempotent lookup without retaining possible PII.
CREATE TABLE atomic_generation_requests (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    request_key_hash BYTEA NOT NULL CHECK (octet_length(request_key_hash) = 32),
    request_digest BYTEA NOT NULL CHECK (octet_length(request_digest) = 32),
    -- 0=COW replay tombstone, 1=ordinary active-generation request.
    request_kind SMALLINT NOT NULL CHECK (request_kind IN (0, 1)),
    basis_t BIGINT NOT NULL CHECK (basis_t > 0),
    tx_hash BYTEA NOT NULL CHECK (octet_length(tx_hash) = 32),
    committed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation, request_key_hash),
    UNIQUE (database_id, generation, basis_t),
    FOREIGN KEY (database_id, generation, basis_t, tx_hash)
        REFERENCES atomic_generation_transactions(database_id, generation, basis_t, tx_hash)
);

-- This is a frozen execution projection of ordinary A=15 request facts. The
-- protected A=15 datoms remain in the authoritative log as the permanent
-- user-visible audit trail.
CREATE TABLE atomic_generation_excision_predicates (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    request_entity BIGINT NOT NULL CHECK (request_entity >= 0),
    request_t BIGINT NOT NULL CHECK (request_t > 0),
    target_id BIGINT NOT NULL CHECK (target_id >= 0),
    target_kind SMALLINT NOT NULL CHECK (target_kind IN (0, 1)),
    requested_cutoff_kind SMALLINT NOT NULL CHECK (requested_cutoff_kind BETWEEN 0 AND 2),
    requested_cutoff_value BIGINT,
    effective_before_t BIGINT NOT NULL CHECK (effective_before_t >= 0),
    requested_attributes BIGINT[] NOT NULL,
    component_extent BIGINT[] NOT NULL,
    reference_attributes INTEGER[] NOT NULL,
    protected_entity_target BOOLEAN NOT NULL,
    predicate_hash BYTEA NOT NULL CHECK (octet_length(predicate_hash) = 32),
    PRIMARY KEY (database_id, generation, request_t, request_entity),
    UNIQUE (database_id, generation, predicate_hash),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generations(database_id, generation),
    CHECK ((requested_cutoff_kind = 0 AND requested_cutoff_value IS NULL)
        OR (requested_cutoff_kind IN (1, 2) AND requested_cutoff_value IS NOT NULL))
);

-- Catch-up is append-only. Each checkpoint authenticates a complete prefix of
-- the candidate and its exact source prefix; a newer ordinary transaction can
-- be appended without mutating earlier build metadata.
CREATE TABLE atomic_log_generation_checkpoints (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    through_basis_t BIGINT NOT NULL CHECK (through_basis_t >= 0),
    head_hash BYTEA NOT NULL CHECK (octet_length(head_hash) = 32),
    state_hash BYTEA NOT NULL CHECK (octet_length(state_hash) = 32),
    source_head_hash BYTEA CHECK (
        source_head_hash IS NULL OR octet_length(source_head_hash) = 32
    ),
    eidx_frontier BIGINT NOT NULL CHECK (eidx_frontier > 0),
    removed_datoms BIGINT NOT NULL CHECK (removed_datoms >= 0),
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation, through_basis_t),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generation_builds(database_id, generation)
);

CREATE TABLE atomic_log_generation_activations (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    prior_generation BIGINT NOT NULL CHECK (prior_generation >= 0),
    prior_basis_t BIGINT NOT NULL CHECK (prior_basis_t >= 0),
    basis_t BIGINT NOT NULL CHECK (basis_t >= 0),
    head_hash BYTEA NOT NULL CHECK (octet_length(head_hash) = 32),
    state_hash BYTEA NOT NULL CHECK (octet_length(state_hash) = 32),
    manifest_hash BYTEA CHECK (manifest_hash IS NULL OR octet_length(manifest_hash) = 32),
    activated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generations(database_id, generation),
    CHECK (prior_generation <> generation)
);

-- Exact immutable completion index for sync-excise. A physical generation
-- number alone cannot say which requests it incorporated.
CREATE TABLE atomic_completed_excision_requests (
    database_id TEXT NOT NULL,
    request_t BIGINT NOT NULL CHECK (request_t > 0),
    request_entity BIGINT NOT NULL CHECK (request_entity >= 0),
    generation BIGINT NOT NULL CHECK (generation > 0),
    predicate_hash BYTEA NOT NULL CHECK (octet_length(predicate_hash) = 32),
    completed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, request_t, request_entity),
    FOREIGN KEY (database_id, generation, request_t, request_entity)
        REFERENCES atomic_generation_excision_predicates(
            database_id, generation, request_t, request_entity
        ),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generation_activations(database_id, generation)
);

CREATE TABLE atomic_log_generation_retirements (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation >= 0),
    successor_generation BIGINT NOT NULL CHECK (successor_generation > 0),
    retired_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation),
    UNIQUE (database_id, successor_generation),
    CHECK (generation <> successor_generation),
    FOREIGN KEY (database_id, successor_generation)
        REFERENCES atomic_log_generation_activations(database_id, generation)
);

CREATE OR REPLACE FUNCTION atomic_validate_generation_transaction_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    build_lineage TEXT;
    expected_previous BYTEA;
BEGIN
    SELECT lineage_id INTO build_lineage
      FROM atomic_log_generations
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation;
    IF NOT FOUND OR NEW.lineage_id <> build_lineage THEN
        RAISE EXCEPTION 'Atomic generation transaction has no matching lineage build'
            USING ERRCODE = '23503';
    END IF;
    IF NEW.basis_t = 1 THEN
        SELECT genesis_hash INTO expected_previous
          FROM atomic_databases
         WHERE database_id = NEW.database_id;
    ELSE
        SELECT tx_hash INTO expected_previous
          FROM atomic_generation_transactions
         WHERE database_id = NEW.database_id
           AND generation = NEW.generation
           AND basis_t = NEW.basis_t - 1;
    END IF;
    IF NOT FOUND OR NEW.previous_hash <> expected_previous THEN
        RAISE EXCEPTION 'Atomic generation transaction does not extend its immutable prefix'
            USING ERRCODE = '40001';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_generation_transactions_validate_insert
BEFORE INSERT ON atomic_generation_transactions
FOR EACH ROW EXECUTE FUNCTION atomic_validate_generation_transaction_insert();

CREATE OR REPLACE FUNCTION atomic_validate_generation_request_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM atomic_generation_transactions
         WHERE database_id = NEW.database_id
           AND generation = NEW.generation
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic generation request does not identify its transaction'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_generation_requests_validate_insert
BEFORE INSERT ON atomic_generation_requests
FOR EACH ROW EXECUTE FUNCTION atomic_validate_generation_request_insert();

CREATE OR REPLACE FUNCTION atomic_validate_generation_checkpoint_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    build_row atomic_log_generation_builds%ROWTYPE;
    generation_row atomic_log_generations%ROWTYPE;
    stored_count BIGINT;
    terminal_hash BYTEA;
    terminal_state BYTEA;
    terminal_frontier BIGINT;
    terminal_source BYTEA;
    source_hash BYTEA;
BEGIN
    SELECT * INTO build_row
      FROM atomic_log_generation_builds
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic generation checkpoint has no active build'
            USING ERRCODE = '23503';
    END IF;
    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation;
    IF NOT FOUND
       OR (generation_row.build_kind IN (0, 1)
           AND NEW.through_basis_t < build_row.captured_basis_t)
       OR (generation_row.build_kind = 2
           AND NEW.through_basis_t <> build_row.restore_basis_t)
       OR (generation_row.build_kind = 0 AND build_row.source_generation IS NOT NULL)
       OR (generation_row.build_kind = 1 AND build_row.source_generation IS NULL)
       OR (generation_row.build_kind = 2
           AND (build_row.source_generation IS NULL
                OR build_row.restore_manifest_hash IS NULL)) THEN
        RAISE EXCEPTION 'Atomic generation checkpoint precedes its frozen capture'
            USING ERRCODE = '23514';
    END IF;
    SELECT count(*) INTO stored_count
      FROM atomic_generation_excision_predicates
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation;
    IF stored_count <> generation_row.request_count THEN
        RAISE EXCEPTION 'Atomic generation checkpoint has an incomplete request set'
            USING ERRCODE = '23503';
    END IF;
    SELECT count(*) INTO stored_count
      FROM atomic_generation_transactions
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation
       AND basis_t <= NEW.through_basis_t;
    IF stored_count <> NEW.through_basis_t THEN
        RAISE EXCEPTION 'Atomic generation checkpoint has a noncontiguous transaction prefix'
            USING ERRCODE = '23503';
    END IF;
    SELECT count(*) INTO stored_count
      FROM atomic_generation_source_links
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation
       AND basis_t <= NEW.through_basis_t;
    IF (generation_row.build_kind = 0 AND stored_count <> 0)
       OR (generation_row.build_kind <> 0 AND stored_count <> NEW.through_basis_t) THEN
        RAISE EXCEPTION 'Atomic generation checkpoint has incomplete source links'
            USING ERRCODE = '23503';
    END IF;
    IF NEW.through_basis_t = 0 THEN
        SELECT genesis_hash INTO terminal_hash
          FROM atomic_databases WHERE database_id = NEW.database_id;
        terminal_state := NEW.state_hash;
        terminal_frontier := NEW.eidx_frontier;
        terminal_source := NULL;
    ELSE
        SELECT t.tx_hash, t.state_hash, t.eidx_frontier, s.source_tx_hash
          INTO terminal_hash, terminal_state, terminal_frontier, terminal_source
          FROM atomic_generation_transactions t
          LEFT JOIN atomic_generation_source_links s
            ON s.database_id = t.database_id
           AND s.generation = t.generation
           AND s.basis_t = t.basis_t
         WHERE t.database_id = NEW.database_id
           AND t.generation = NEW.generation
           AND t.basis_t = NEW.through_basis_t;
    END IF;
    IF terminal_hash IS NULL OR terminal_hash <> NEW.head_hash
                 OR terminal_state <> NEW.state_hash
                 OR terminal_frontier <> NEW.eidx_frontier THEN
        RAISE EXCEPTION 'Atomic generation checkpoint disagrees with its terminal transaction'
            USING ERRCODE = '23503';
    END IF;

    IF generation_row.build_kind = 0 THEN
        IF NEW.source_head_hash IS NOT NULL THEN
            RAISE EXCEPTION 'Initial Atomic generation cannot claim a source head'
                USING ERRCODE = '23514';
        END IF;
    ELSIF generation_row.build_kind = 1 THEN
        IF build_row.source_generation = 0 THEN
            IF NEW.through_basis_t = 0 THEN
                SELECT genesis_hash INTO source_hash
                  FROM atomic_databases WHERE database_id = NEW.database_id;
            ELSE
                SELECT tx_hash INTO source_hash
                  FROM atomic_transactions
                 WHERE database_id = NEW.database_id
                   AND basis_t = NEW.through_basis_t;
            END IF;
            IF EXISTS (
                SELECT 1
                  FROM atomic_generation_source_links l
                  LEFT JOIN atomic_transactions s
                    ON s.database_id = l.database_id AND s.basis_t = l.basis_t
                 WHERE l.database_id = NEW.database_id
                   AND l.generation = NEW.generation
                   AND l.basis_t <= NEW.through_basis_t
                   AND s.tx_hash IS DISTINCT FROM l.source_tx_hash
            ) THEN
                RAISE EXCEPTION 'Atomic generation source links disagree with legacy history'
                    USING ERRCODE = '23503';
            END IF;
        ELSE
            IF NEW.through_basis_t = 0 THEN
                SELECT genesis_hash INTO source_hash
                  FROM atomic_databases WHERE database_id = NEW.database_id;
            ELSE
                SELECT tx_hash INTO source_hash
                  FROM atomic_generation_transactions
                 WHERE database_id = NEW.database_id
                   AND generation = build_row.source_generation
                   AND basis_t = NEW.through_basis_t;
            END IF;
            IF EXISTS (
                SELECT 1
                  FROM atomic_generation_source_links l
                  LEFT JOIN atomic_generation_transactions s
                    ON s.database_id = l.database_id
                   AND s.generation = build_row.source_generation
                   AND s.basis_t = l.basis_t
                 WHERE l.database_id = NEW.database_id
                   AND l.generation = NEW.generation
                   AND l.basis_t <= NEW.through_basis_t
                   AND s.tx_hash IS DISTINCT FROM l.source_tx_hash
            ) THEN
                RAISE EXCEPTION 'Atomic generation source links disagree with lineage history'
                    USING ERRCODE = '23503';
            END IF;
        END IF;
        IF source_hash IS NULL OR source_hash <> NEW.source_head_hash
                     OR (NEW.through_basis_t > 0 AND terminal_source <> source_hash) THEN
            RAISE EXCEPTION 'Atomic generation checkpoint disagrees with its source prefix'
                USING ERRCODE = '23503';
        END IF;
    ELSE
        -- Restore rows were authenticated against the portable manifest by
        -- Rust before staging. They intentionally need not exist on the
        -- target's current branch or at its current basis.
        IF NEW.source_head_hash <> build_row.restore_head_hash
           OR (NEW.through_basis_t > 0 AND terminal_source <> NEW.source_head_hash) THEN
            RAISE EXCEPTION 'Atomic restore checkpoint disagrees with its portable source root'
                USING ERRCODE = '23503';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_log_generation_checkpoints_validate_insert
BEFORE INSERT ON atomic_log_generation_checkpoints
FOR EACH ROW EXECUTE FUNCTION atomic_validate_generation_checkpoint_insert();

-- Activation is the sole legal cross-generation head mutation. Same-generation
-- ordinary head advancement remains governed by the usual transaction/request
-- completeness rule below.
CREATE OR REPLACE FUNCTION atomic_validate_head_advance()
RETURNS trigger
LANGUAGE plpgsql
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
            SELECT 1 FROM atomic_generation_requests
             WHERE database_id = NEW.database_id
               AND generation = NEW.log_generation
               AND basis_t = NEW.basis_t
               AND tx_hash = NEW.tx_hash
        ) THEN
            RAISE EXCEPTION 'Atomic head does not identify a complete lineage transaction publication'
                USING ERRCODE = '23503';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_validate_transaction_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    current_basis BIGINT;
    current_hash BYTEA;
    current_generation BIGINT;
BEGIN
    SELECT basis_t, tx_hash, log_generation
      INTO current_basis, current_hash, current_generation
      FROM atomic_heads WHERE database_id = NEW.database_id;
    IF NOT FOUND OR current_generation <> 0 THEN
        RAISE EXCEPTION 'Legacy Atomic transactions require the active generation-zero head'
            USING ERRCODE = '40001';
    END IF;
    IF NEW.basis_t <> current_basis + 1 OR NEW.previous_hash <> current_hash THEN
        RAISE EXCEPTION 'Atomic transaction does not extend the current head'
            USING ERRCODE = '40001';
    END IF;
    IF NEW.state_hash = decode(repeat('00', 32), 'hex') THEN
        RAISE EXCEPTION 'New Atomic transactions require a state commitment'
            USING ERRCODE = '55000';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_require_published_transaction()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM atomic_heads
         WHERE database_id = NEW.database_id
           AND log_generation = 0
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic legacy transaction was not published by its commit'
            USING ERRCODE = '40001';
    END IF;
    RETURN NULL;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_require_published_generation_transaction()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    -- Inactive content-first build rows are deliberately not visible. Once a
    -- generation is active, every later inserted transaction must publish.
    IF EXISTS (
        SELECT 1 FROM atomic_log_generation_activations
         WHERE database_id = NEW.database_id AND generation = NEW.generation
    ) AND NOT EXISTS (
        SELECT 1 FROM atomic_heads
         WHERE database_id = NEW.database_id
           AND log_generation = NEW.generation
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic lineage transaction was not published by its commit'
            USING ERRCODE = '40001';
    END IF;
    RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER atomic_generation_transactions_require_publication
AFTER INSERT ON atomic_generation_transactions
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION atomic_require_published_generation_transaction();

-- Every generation table is append-only. Narrow owner-only activation and
-- later age-gated collection functions are the only mutation escape hatches.
CREATE TRIGGER atomic_log_generations_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generations
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();
CREATE TRIGGER atomic_generation_transactions_immutable
BEFORE UPDATE OR DELETE ON atomic_generation_transactions
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();
CREATE TRIGGER atomic_generation_requests_immutable
BEFORE UPDATE OR DELETE ON atomic_generation_requests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();
CREATE TRIGGER atomic_generation_excision_predicates_immutable
BEFORE UPDATE OR DELETE ON atomic_generation_excision_predicates
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();
CREATE TRIGGER atomic_log_generation_activations_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_activations
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();
CREATE TRIGGER atomic_completed_excision_requests_immutable
BEFORE UPDATE OR DELETE ON atomic_completed_excision_requests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();
CREATE TRIGGER atomic_log_generation_retirements_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_retirements
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

CREATE OR REPLACE FUNCTION atomic_reject_generation_staging_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF TG_OP = 'DELETE'
       AND pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14'
       AND current_user = relation_owner THEN
        RETURN OLD;
    END IF;
    RAISE EXCEPTION 'Atomic generation staging records are immutable outside activation'
        USING ERRCODE = '55000';
END;
$$;

CREATE TRIGGER atomic_log_generation_builds_staging_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_builds
FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();
CREATE TRIGGER atomic_generation_source_links_staging_immutable
BEFORE UPDATE OR DELETE ON atomic_generation_source_links
FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();
CREATE TRIGGER atomic_log_generation_checkpoints_staging_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_checkpoints
FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();

-- Persistent trees remain derived values, but a published tree must name the
-- same log generation as the active head. Generation-zero ATIM envelopes keep
-- their exact alias identity; positive generations carry the stable lineage.
ALTER TABLE atomic_tree_manifests
    DROP CONSTRAINT atomic_tree_manifests_database_id_basis_t_tx_hash_fkey;
ALTER TABLE atomic_tree_manifests
    ADD COLUMN log_generation BIGINT NOT NULL DEFAULT 0
        CHECK (log_generation >= 0),
    ADD COLUMN lineage_id TEXT,
    ADD CONSTRAINT atomic_tree_manifests_generation_identity CHECK (
        (log_generation = 0 AND lineage_id IS NULL)
        OR (log_generation > 0 AND lineage_id IS NOT NULL)
    );
ALTER TABLE atomic_tree_publications
    ADD COLUMN log_generation BIGINT NOT NULL DEFAULT 0
        CHECK (log_generation >= 0);
ALTER TABLE atomic_tree_retirements
    ADD COLUMN log_generation BIGINT NOT NULL DEFAULT 0
        CHECK (log_generation >= 0);

CREATE INDEX atomic_tree_publications_generation_revision
    ON atomic_tree_publications(database_id, log_generation, publication_revision DESC);

CREATE OR REPLACE FUNCTION atomic_validate_tree_manifest_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    authoritative_state BYTEA;
    durable_lineage TEXT;
BEGIN
    SELECT lineage_id INTO durable_lineage
      FROM atomic_databases WHERE database_id = NEW.database_id;
    IF NOT FOUND OR NEW.excision_generation <> NEW.log_generation THEN
        RAISE EXCEPTION 'Atomic tree manifest has invalid generation metadata'
            USING ERRCODE = '23503';
    END IF;
    IF NEW.log_generation = 0 THEN
        SELECT state_hash INTO authoritative_state
          FROM atomic_transactions
         WHERE database_id = NEW.database_id
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash;
        IF NEW.lineage_id IS NOT NULL THEN
            RAISE EXCEPTION 'Legacy Atomic tree manifest cannot claim lineage encoding'
                USING ERRCODE = '23514';
        END IF;
    ELSE
        SELECT state_hash INTO authoritative_state
          FROM atomic_generation_transactions
         WHERE database_id = NEW.database_id
           AND generation = NEW.log_generation
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash;
        IF NEW.lineage_id IS DISTINCT FROM durable_lineage OR NOT EXISTS (
            SELECT 1 FROM atomic_log_generation_checkpoints c
             WHERE c.database_id = NEW.database_id
               AND c.generation = NEW.log_generation
               AND c.through_basis_t = NEW.basis_t
               AND c.head_hash = NEW.tx_hash
               AND c.state_hash = NEW.state_hash
        ) THEN
            RAISE EXCEPTION 'Atomic tree manifest does not identify a complete lineage build'
                USING ERRCODE = '23503';
        END IF;
    END IF;
    IF NOT FOUND OR authoritative_state <> NEW.state_hash
                 OR NEW.state_hash = decode(repeat('00', 32), 'hex') THEN
        RAISE EXCEPTION 'Atomic tree manifest does not identify one committed state'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_validate_tree_publication_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    current_revision BIGINT;
    current_basis BIGINT;
    valid_roots BIGINT;
    head_generation BIGINT;
    head_basis BIGINT;
    head_hash BYTEA;
    relation_owner NAME;
BEGIN
    PERFORM database_id
      FROM atomic_databases
     WHERE database_id = NEW.database_id
       FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree publication database does not exist'
            USING ERRCODE = '23503';
    END IF;

    SELECT publication_revision, basis_t
      INTO current_revision, current_basis
      FROM atomic_tree_publications
     WHERE database_id = NEW.database_id
     ORDER BY publication_revision DESC
     LIMIT 1;
    IF FOUND THEN
        IF current_revision = 9223372036854775807 THEN
            RAISE EXCEPTION 'Atomic tree publication revision is exhausted'
                USING ERRCODE = '54000';
        END IF;
        IF NEW.publication_revision <> current_revision + 1 THEN
            RAISE EXCEPTION 'Atomic tree publication must advance exactly one revision'
                USING ERRCODE = '40001';
        END IF;
        IF NEW.basis_t < current_basis THEN
            SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
              FROM pg_catalog.pg_class WHERE oid = TG_RELID;
            IF pg_catalog.current_setting('atomic.log_generation_activation', true) <> 'v14'
               OR current_user <> relation_owner THEN
                RAISE EXCEPTION 'Atomic tree publication basis cannot regress'
                    USING ERRCODE = '40001';
            END IF;
        END IF;
    ELSIF NEW.publication_revision <> 1 THEN
        RAISE EXCEPTION 'First Atomic tree publication must use revision one'
            USING ERRCODE = '40001';
    END IF;

    SELECT log_generation, basis_t, tx_hash
      INTO head_generation, head_basis, head_hash
      FROM atomic_heads WHERE database_id = NEW.database_id;
    IF NOT FOUND OR head_generation <> NEW.log_generation
                 OR head_basis <> NEW.basis_t OR head_hash <> NEW.tx_hash THEN
        RAISE EXCEPTION 'Atomic tree publication does not name the active log head'
            USING ERRCODE = '40001';
    END IF;

    SELECT count(*) INTO valid_roots
      FROM atomic_tree_manifest_roots r
      JOIN atomic_tree_nodes n ON n.node_hash = r.root_hash
     WHERE r.manifest_hash = NEW.manifest_hash
       AND r.encoded_bytes = octet_length(n.payload);
    IF valid_roots <> 8 THEN
        RAISE EXCEPTION 'Atomic tree publication requires eight present roots'
            USING ERRCODE = '23503';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_tree_manifests m
         WHERE m.database_id = NEW.database_id
           AND m.publication_revision = NEW.publication_revision
           AND m.basis_t = NEW.basis_t
           AND m.tx_hash = NEW.tx_hash
           AND m.manifest_hash = NEW.manifest_hash
           AND m.log_generation = NEW.log_generation
    ) THEN
        RAISE EXCEPTION 'Atomic tree publication is stale or unauthenticated'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_fill_tree_retirement_generation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    SELECT log_generation INTO NEW.log_generation
      FROM atomic_tree_publications
     WHERE database_id = NEW.database_id
       AND publication_revision = NEW.publication_revision
       AND manifest_hash = NEW.manifest_hash;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree retirement has no matching publication'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_tree_retirements_fill_generation
BEFORE INSERT ON atomic_tree_retirements
FOR EACH ROW EXECUTE FUNCTION atomic_fill_tree_retirement_generation();

CREATE OR REPLACE FUNCTION atomic_scrub_excision_tree_predecessor()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14' THEN
        NEW.predecessor_manifest_hash := NULL;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_tree_publication_states_scrub_excision_predecessor
BEFORE INSERT ON atomic_tree_publication_states
FOR EACH ROW EXECUTE FUNCTION atomic_scrub_excision_tree_predecessor();

CREATE OR REPLACE FUNCTION atomic_publish_tree(
    candidate_database_id TEXT,
    candidate_revision BIGINT,
    candidate_basis BIGINT,
    candidate_tx_hash BYTEA,
    candidate_manifest_hash BYTEA
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    candidate_generation BIGINT;
BEGIN
    SELECT log_generation INTO candidate_generation
      FROM atomic_heads WHERE database_id = candidate_database_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree publication database does not exist'
            USING ERRCODE = '23503';
    END IF;
    INSERT INTO atomic_tree_publications
           (database_id, publication_revision, basis_t, tx_hash, manifest_hash,
            log_generation)
    VALUES (candidate_database_id, candidate_revision, candidate_basis,
            candidate_tx_hash, candidate_manifest_hash, candidate_generation);
END;
$$;

-- One bounded final transaction invokes this owner-only function. All large
-- log/tree values already exist; the row lock covers only source-head
-- comparison, generation publication, and root publication.
CREATE OR REPLACE FUNCTION atomic_activate_log_generation(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    candidate_basis BIGINT,
    candidate_head_hash BYTEA,
    candidate_state_hash BYTEA,
    candidate_manifest_hash BYTEA
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    build_row atomic_log_generation_builds%ROWTYPE;
    generation_row atomic_log_generations%ROWTYPE;
    current_generation BIGINT;
    current_basis BIGINT;
    current_hash BYTEA;
    candidate_revision BIGINT;
BEGIN
    SELECT * INTO build_row
      FROM atomic_log_generation_builds
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic generation has no active build'
            USING ERRCODE = '23503';
    END IF;
    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    IF NOT FOUND OR generation_row.build_kind = 0 THEN
        RAISE EXCEPTION 'Atomic generation is not an activatable rewrite'
            USING ERRCODE = '23503';
    END IF;
    SELECT log_generation, basis_t, tx_hash
      INTO current_generation, current_basis, current_hash
      FROM atomic_heads
     WHERE database_id = candidate_database_id
       FOR UPDATE;
    IF NOT FOUND OR current_generation <> build_row.source_generation
       OR (generation_row.build_kind = 2
           AND (current_basis <> build_row.captured_basis_t
                OR current_hash <> build_row.captured_head_hash)) THEN
        RAISE EXCEPTION 'Atomic source head changed after generation capture'
            USING ERRCODE = '40001';
    END IF;
    IF generation_row.build_kind = 2 AND EXISTS (
        SELECT 1 FROM atomic_transactor_leases
         WHERE lease_scope = candidate_database_id
           AND expires_at > clock_timestamp()
    ) THEN
        RAISE EXCEPTION 'Atomic point restore requires the transactor to be stopped'
            USING ERRCODE = '55006';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_log_generation_checkpoints c
         WHERE c.database_id = candidate_database_id
           AND c.generation = candidate_generation
           AND c.through_basis_t = candidate_basis
           AND c.head_hash = candidate_head_hash
           AND c.state_hash = candidate_state_hash
           AND (generation_row.build_kind = 2
                OR (c.source_head_hash = current_hash AND c.through_basis_t = current_basis))
    ) THEN
        RAISE EXCEPTION 'Atomic generation activation has no matching complete checkpoint'
            USING ERRCODE = '23503';
    END IF;
    SELECT publication_revision INTO candidate_revision
      FROM atomic_tree_manifests m
     WHERE m.database_id = candidate_database_id
       AND m.log_generation = candidate_generation
       AND m.basis_t = candidate_basis
       AND m.tx_hash = candidate_head_hash
       AND m.state_hash = candidate_state_hash
       AND m.manifest_hash = candidate_manifest_hash;
    IF NOT FOUND OR NOT EXISTS (
        SELECT 1 FROM atomic_tree_delta_headers
         WHERE manifest_hash = candidate_manifest_hash
    ) THEN
        RAISE EXCEPTION 'Atomic generation activation requires a staged authenticated tree'
            USING ERRCODE = '23503';
    END IF;
    IF candidate_revision <> COALESCE((
        SELECT max(publication_revision) + 1
          FROM atomic_tree_publications
         WHERE database_id = candidate_database_id
    ), 1) THEN
        RAISE EXCEPTION 'Atomic staged tree lost its publication revision race'
            USING ERRCODE = '40001';
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    UPDATE atomic_heads
       SET log_generation = candidate_generation,
           basis_t = candidate_basis,
           tx_hash = candidate_head_hash
     WHERE database_id = candidate_database_id;
    INSERT INTO atomic_log_generation_activations
           (database_id, generation, prior_generation, prior_basis_t,
            basis_t, head_hash, state_hash, manifest_hash)
    VALUES (candidate_database_id, candidate_generation, current_generation,
            current_basis, candidate_basis, candidate_head_hash,
            candidate_state_hash, candidate_manifest_hash);
    INSERT INTO atomic_log_generation_retirements
           (database_id, generation, successor_generation)
    VALUES (candidate_database_id, current_generation, candidate_generation);
    INSERT INTO atomic_completed_excision_requests
           (database_id, request_t, request_entity, generation, predicate_hash)
    SELECT database_id, request_t, request_entity, generation, predicate_hash
      FROM atomic_generation_excision_predicates
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    INSERT INTO atomic_tree_publications
           (database_id, publication_revision, basis_t, tx_hash, manifest_hash,
            log_generation)
    VALUES (candidate_database_id, candidate_revision, candidate_basis,
            candidate_head_hash, candidate_manifest_hash, candidate_generation);
    DELETE FROM atomic_generation_source_links
     WHERE database_id = candidate_database_id AND generation = candidate_generation;
    DELETE FROM atomic_log_generation_checkpoints
     WHERE database_id = candidate_database_id AND generation = candidate_generation;
    DELETE FROM atomic_log_generation_builds
     WHERE database_id = candidate_database_id AND generation = candidate_generation;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
END;
$$;

REVOKE ALL ON FUNCTION atomic_activate_log_generation(
    TEXT, BIGINT, BIGINT, BYTEA, BYTEA, BYTEA
) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_activate_log_generation(
    TEXT, BIGINT, BIGINT, BYTEA, BYTEA, BYTEA
) TO CURRENT_USER;

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generations FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_builds FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_generation_transactions FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_generation_source_links FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_generation_requests FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_generation_excision_predicates FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_checkpoints FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_activations FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_completed_excision_requests FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_retirements FROM PUBLIC;
