-- Copy-on-write authoritative log generations.
--
-- Generation zero remains the exact alias-bound v3 payload representation in
-- atomic_transactions/atomic_requests. Those bytes are never relabelled as a
-- lineage format. New databases and every excision/restore candidate use
-- shared lineage-bound ATLC v1 content plus small generation memberships.

DROP TABLE IF EXISTS atomic_active_programs;
DROP TABLE IF EXISTS atomic_program_versions;
DROP TABLE IF EXISTS atomic_excisions;

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
    generation BIGINT NOT NULL CHECK (generation > 0),
    lineage_id TEXT NOT NULL,
    -- 0=new database, 1=excision, 2=same-lineage point restore.
    build_kind SMALLINT NOT NULL CHECK (build_kind BETWEEN 0 AND 2),
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
    -- This authenticates the frozen execution projection while the build is
    -- resumable. It is deliberately deleted at activation: component closure
    -- and schema-derived classifications are not permanent excision audit.
    frozen_plan_hash BYTEA CHECK (
        frozen_plan_hash IS NULL OR octet_length(frozen_plan_hash) = 32
    ),
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

-- Immutable logical transaction values are content-addressed independently
-- of a physical generation. This is the SQL analogue of Datomic's immutable
-- log values: an excision generation reuses every unaffected value instead of
-- copying a generation-stamped envelope for every transaction.
CREATE TABLE atomic_transaction_contents (
    content_hash BYTEA PRIMARY KEY CHECK (octet_length(content_hash) = 32),
    lineage_id TEXT NOT NULL,
    basis_t BIGINT NOT NULL CHECK (basis_t > 0),
    eidx_frontier BIGINT NOT NULL CHECK (eidx_frontier > 0),
    envelope_version SMALLINT NOT NULL DEFAULT 1 CHECK (envelope_version = 1),
    payload BYTEA NOT NULL CHECK (octet_length(payload) >= 58),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    FOREIGN KEY (lineage_id) REFERENCES atomic_databases(lineage_id)
);

-- Small immutable generation membership and chain commitments. The payload
-- has no physical generation or predecessor, so downstream memberships may
-- change without duplicating the logical content they name.
CREATE TABLE atomic_generation_transactions (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    basis_t BIGINT NOT NULL CHECK (basis_t > 0),
    previous_hash BYTEA NOT NULL CHECK (octet_length(previous_hash) = 32),
    tx_hash BYTEA NOT NULL CHECK (octet_length(tx_hash) = 32),
    content_hash BYTEA NOT NULL CHECK (octet_length(content_hash) = 32),
    state_hash BYTEA NOT NULL CHECK (
        octet_length(state_hash) = 32
        AND state_hash <> decode(repeat('00', 32), 'hex')
    ),
    eidx_frontier BIGINT NOT NULL CHECK (eidx_frontier > 0),
    committed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation, basis_t),
    UNIQUE (database_id, generation, tx_hash),
    UNIQUE (database_id, generation, basis_t, tx_hash),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generations(database_id, generation),
    FOREIGN KEY (content_hash)
        REFERENCES atomic_transaction_contents(content_hash)
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

-- Original tempid spellings are part of an ordinary transaction receipt, not
-- of canonical immutable transaction content. Keep them only with the active
-- generation's idempotency row. COW replay writes a tombstone request and no
-- receipt names, so retiring/collecting the old generation erases them.
CREATE TABLE atomic_generation_request_tempids (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    request_key_hash BYTEA NOT NULL CHECK (octet_length(request_key_hash) = 32),
    tempid_name TEXT NOT NULL,
    entity_id BIGINT NOT NULL CHECK (entity_id >= 0),
    PRIMARY KEY (database_id, generation, request_key_hash, tempid_name),
    FOREIGN KEY (database_id, generation, request_key_hash)
        REFERENCES atomic_generation_requests(database_id, generation, request_key_hash)
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

-- Exact immutable completion index for sync-excise. The only retained request
-- identity is already present in the permanent A=15 audit facts. In
-- particular, this table must not retain the frozen component extent or a
-- dictionary-testable hash of that derived closure.
CREATE TABLE atomic_completed_excision_requests (
    database_id TEXT NOT NULL,
    request_t BIGINT NOT NULL CHECK (request_t > 0),
    request_entity BIGINT NOT NULL CHECK (request_entity >= 0),
    generation BIGINT NOT NULL CHECK (generation > 0),
    completed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation, request_t, request_entity),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generations(database_id, generation)
);

-- The exact completion set is assembled while a rewritten log is inactive.
-- Excision copies its predecessor set and its newly frozen requests in
-- bounded, restart-safe cursor batches. Restore streams the authenticated set
-- from its archive. Publication later consists only of the marker below.
CREATE TABLE atomic_log_generation_completion_stages (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    -- 0=copy predecessor, 1=copy current requests/archive, 2=sealed.
    phase SMALLINT NOT NULL CHECK (phase BETWEEN 0 AND 2),
    cursor_t BIGINT CHECK (cursor_t IS NULL OR cursor_t > 0),
    cursor_entity BIGINT CHECK (cursor_entity IS NULL OR cursor_entity >= 0),
    row_count BIGINT NOT NULL DEFAULT 0 CHECK (row_count >= 0),
    sealed_at TIMESTAMPTZ,
    PRIMARY KEY (database_id, generation),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generation_builds(database_id, generation),
    CHECK ((phase = 2 AND sealed_at IS NOT NULL)
        OR (phase < 2 AND sealed_at IS NULL)),
    CHECK ((cursor_t IS NULL) = (cursor_entity IS NULL))
);

-- Completion rows may be prepared while a candidate is inactive. This single
-- marker is the root-last visibility point used by sync-excise; readers join
-- it only through the active head generation.
CREATE TABLE atomic_log_generation_completions (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    completed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generation_activations(database_id, generation)
);

CREATE TABLE atomic_log_generation_retirements (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation >= 0),
    successor_generation BIGINT NOT NULL CHECK (successor_generation > 0),
    retired_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    collecting_at TIMESTAMPTZ,
    PRIMARY KEY (database_id, generation),
    UNIQUE (database_id, successor_generation),
    CHECK (generation <> successor_generation),
    FOREIGN KEY (database_id, successor_generation)
        REFERENCES atomic_log_generation_activations(database_id, generation)
);

-- Bounded membership draining records the shared ATLC hashes it detached.
-- A later bounded phase deletes only globally unreferenced content; reused
-- content simply loses this candidate mark and remains immutable.
CREATE TABLE atomic_log_generation_garbage_contents (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    content_hash BYTEA NOT NULL CHECK (octet_length(content_hash) = 32),
    PRIMARY KEY (database_id, generation, content_hash),
    FOREIGN KEY (content_hash)
        REFERENCES atomic_transaction_contents(content_hash)
);

-- Durable phase cursor for bounded retired-generation collection. Admission
-- closes under the generation's exclusive advisory pin before this row is
-- created; every later invocation drains at most its caller-supplied bound.
CREATE TABLE atomic_log_generation_collection_progress (
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation >= 0),
    phase SMALLINT NOT NULL DEFAULT 0 CHECK (phase BETWEEN 0 AND 11),
    started_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generation_retirements(database_id, generation)
        ON DELETE CASCADE
);

-- One session-level lock coordinate per logical lineage/generation. Immutable
-- peer values and active backups take SHARE before exposure; the collector
-- takes EXCLUSIVE before permanently closing admission and draining rows.
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
               'atomic/log-generation-pin/v1/' || lineage_id || '/' ||
               candidate_generation::text,
               4707476001900298240::bigint
           )
      FROM atomic_databases
     WHERE database_id = candidate_database_id
       AND candidate_generation >= 0
$$;

REVOKE ALL ON FUNCTION atomic_log_generation_pin_key(TEXT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_log_generation_pin_key(TEXT, BIGINT) TO CURRENT_USER;

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
    IF NOT FOUND OR NOT EXISTS (
        SELECT 1 FROM atomic_transaction_contents c
         WHERE c.content_hash = NEW.content_hash
           AND c.lineage_id = build_lineage
           AND c.basis_t = NEW.basis_t
           AND c.eidx_frontier = NEW.eidx_frontier
    ) THEN
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
    IF NEW.through_basis_t = 0 THEN
        SELECT genesis_hash INTO terminal_hash
          FROM atomic_databases WHERE database_id = NEW.database_id;
        terminal_state := NEW.state_hash;
        terminal_frontier := NEW.eidx_frontier;
    ELSE
        SELECT t.tx_hash, t.state_hash, t.eidx_frontier
          INTO terminal_hash, terminal_state, terminal_frontier
          FROM atomic_generation_transactions t
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
        END IF;
        IF source_hash IS NULL OR source_hash <> NEW.source_head_hash THEN
            RAISE EXCEPTION 'Atomic generation checkpoint disagrees with its source prefix'
                USING ERRCODE = '23503';
        END IF;
    ELSE
        -- Restore rows were authenticated against the portable manifest by
        -- Rust before staging. They intentionally need not exist on the
        -- target's current branch or at its current basis.
        IF NEW.source_head_hash <> build_row.restore_head_hash THEN
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

-- A restore into a brand-new catalog stages an authenticated positive
-- generation before any head exists. The ordinary basis-zero creation path
-- remains unchanged; this narrow owner-only path is checked again by the
-- initial-generation activation function below.
CREATE OR REPLACE FUNCTION atomic_validate_head_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    expected_hash BYTEA;
    relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF NEW.log_generation > 0
       AND pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14'
       AND current_user = relation_owner
       AND EXISTS (
            SELECT 1
              FROM atomic_log_generations g
              JOIN atomic_log_generation_builds b
                ON b.database_id = g.database_id AND b.generation = g.generation
              JOIN atomic_log_generation_checkpoints c
                ON c.database_id = g.database_id AND c.generation = g.generation
             WHERE g.database_id = NEW.database_id
               AND g.generation = NEW.log_generation
               AND g.build_kind = 0
               AND c.through_basis_t = NEW.basis_t
               AND c.head_hash = NEW.tx_hash
       ) THEN
        RETURN NEW;
    END IF;
    IF NEW.basis_t <> 0 THEN
        RAISE EXCEPTION 'Atomic initial head must be basis zero'
            USING ERRCODE = '23514';
    END IF;
    SELECT genesis_hash INTO expected_hash
      FROM atomic_databases
     WHERE database_id = NEW.database_id;
    IF NOT FOUND OR NEW.tx_hash <> expected_hash THEN
        RAISE EXCEPTION 'Atomic basis-zero head must identify database genesis'
            USING ERRCODE = '23514';
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
-- age-gated collection functions are the only mutation escape hatches. The
-- transaction-local marker is not an application capability: runtime roles
-- are not relation owners and therefore cannot manufacture this path.
CREATE OR REPLACE FUNCTION atomic_reject_log_generation_gc_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF pg_catalog.current_setting('atomic.log_generation_gc', true) = 'v14'
       AND current_user = relation_owner THEN
        IF TG_OP = 'DELETE' THEN
            RETURN OLD;
        END IF;
        IF TG_OP = 'UPDATE'
           AND TG_TABLE_NAME = 'atomic_log_generation_retirements'
           AND (to_jsonb(NEW) - 'collecting_at') =
               (to_jsonb(OLD) - 'collecting_at')
           AND OLD.collecting_at IS NULL
           AND NEW.collecting_at IS NOT NULL THEN
            RETURN NEW;
        END IF;
    END IF;
    RAISE EXCEPTION 'Atomic committed log-generation records are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE TRIGGER atomic_log_generations_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generations
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
CREATE TRIGGER atomic_transaction_contents_immutable
BEFORE UPDATE OR DELETE ON atomic_transaction_contents
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
CREATE TRIGGER atomic_generation_transactions_immutable
BEFORE UPDATE OR DELETE ON atomic_generation_transactions
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
CREATE TRIGGER atomic_generation_requests_immutable
BEFORE UPDATE OR DELETE ON atomic_generation_requests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
CREATE TRIGGER atomic_generation_request_tempids_immutable
BEFORE UPDATE OR DELETE ON atomic_generation_request_tempids
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
CREATE TRIGGER atomic_log_generation_activations_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_activations
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_completed_excision_requests_immutable
BEFORE UPDATE OR DELETE ON atomic_completed_excision_requests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
CREATE TRIGGER atomic_log_generation_completions_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_completions
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
CREATE TRIGGER atomic_log_generation_retirements_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_retirements
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

-- Generation zero is the old alias-bound representation. Once retired it is
-- subject to the same pin/age fence as native memberships, including its
-- legacy flat derived manifests which otherwise retain transaction FKs.
DROP TRIGGER atomic_transactions_immutable ON atomic_transactions;
CREATE TRIGGER atomic_transactions_immutable
BEFORE UPDATE OR DELETE ON atomic_transactions
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
DROP TRIGGER atomic_requests_immutable ON atomic_requests;
CREATE TRIGGER atomic_requests_immutable
BEFORE UPDATE OR DELETE ON atomic_requests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
DROP TRIGGER atomic_index_manifests_immutable ON atomic_index_manifests;
CREATE TRIGGER atomic_index_manifests_immutable
BEFORE UPDATE OR DELETE ON atomic_index_manifests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();
DROP TRIGGER atomic_index_publications_immutable ON atomic_index_publications;
CREATE TRIGGER atomic_index_publications_immutable
BEFORE UPDATE OR DELETE ON atomic_index_publications
FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE OR REPLACE FUNCTION atomic_reject_generation_staging_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF TG_OP IN ('UPDATE', 'DELETE')
       AND pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14'
       AND current_user = relation_owner THEN
        IF TG_OP = 'DELETE' THEN
            RETURN OLD;
        END IF;
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'Atomic generation staging records are immutable outside activation'
        USING ERRCODE = '55000';
END;
$$;

CREATE TRIGGER atomic_log_generation_builds_staging_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_builds
FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();
CREATE TRIGGER atomic_generation_excision_predicates_staging_immutable
BEFORE UPDATE OR DELETE ON atomic_generation_excision_predicates
FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();
CREATE TRIGGER atomic_log_generation_checkpoints_staging_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_checkpoints
FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();
CREATE TRIGGER atomic_log_generation_completion_stages_staging_immutable
BEFORE UPDATE OR DELETE ON atomic_log_generation_completion_stages
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
        IF NEW.lineage_id IS DISTINCT FROM durable_lineage OR NOT (
            EXISTS (
                SELECT 1 FROM atomic_heads h
                 WHERE h.database_id = NEW.database_id
                   AND h.log_generation = NEW.log_generation
                   AND h.basis_t >= NEW.basis_t
            )
            OR EXISTS (
                SELECT 1 FROM atomic_log_generation_checkpoints c
                 WHERE c.database_id = NEW.database_id
                   AND c.generation = NEW.log_generation
                   AND c.through_basis_t = NEW.basis_t
                   AND c.head_hash = NEW.tx_hash
                   AND c.state_hash = NEW.state_hash
            )
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
    current_publication_generation BIGINT;
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

    SELECT publication_revision, basis_t, log_generation
      INTO current_revision, current_basis, current_publication_generation
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
        IF NEW.log_generation = current_publication_generation
           AND NEW.basis_t < current_basis THEN
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
                 OR head_basis < NEW.basis_t
                 OR (head_basis = NEW.basis_t AND head_hash <> NEW.tx_hash) THEN
        RAISE EXCEPTION 'Atomic tree publication is not in the active log generation'
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

-- v13 conservatively retained programs named by mutable operator aliases.
-- Those aliases are gone: authenticated per-generation temporal references
-- (including fixed transitive program dependencies) are now the sole mark
-- authority. The Rust v14 migration hook populates the mark set before it
-- flips atomic_program_reference_state.complete.
CREATE OR REPLACE FUNCTION atomic_collect_program_garbage(
    older_than_millis BIGINT,
    maximum_programs BIGINT
)
RETURNS SETOF BYTEA
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF older_than_millis < 0 OR maximum_programs < 1 OR maximum_programs > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic program garbage boundary'
            USING ERRCODE = '22023';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_program_reference_state
         WHERE singleton AND complete AND problem_code IS NULL
    ) THEN
        RETURN;
    END IF;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    RETURN QUERY
    WITH candidates AS MATERIALIZED (
        SELECT c.program_hash
          FROM atomic_program_gc_candidates c
         WHERE c.candidate_at < clock_timestamp()
                                - older_than_millis * interval '1 millisecond'
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_program_generation_refs r
                    WHERE r.program_hash = c.program_hash
               )
         ORDER BY c.candidate_at, c.program_hash
         LIMIT maximum_programs
         FOR UPDATE OF c SKIP LOCKED
    )
    DELETE FROM atomic_programs p
     USING candidates c
     WHERE p.program_hash = c.program_hash
       AND NOT EXISTS (
               SELECT 1 FROM atomic_program_generation_refs r
                WHERE r.program_hash = p.program_hash
           )
    RETURNING p.program_hash;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
END;
$$;

REVOKE ALL ON FUNCTION atomic_collect_program_garbage(BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_program_garbage(BIGINT, BIGINT) TO CURRENT_USER;

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

-- Advance at most one bounded cursor batch while assembling an excision
-- candidate's exact sync-excise completion set. Source completion rows are
-- immutable once their marker exists, and the frozen predicate table is
-- immutable for the life of this build, so the two lexicographic cursors are
-- sufficient for crash-safe resumption without rescanning history.
CREATE OR REPLACE FUNCTION atomic_stage_excision_completions(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    maximum_rows BIGINT
)
RETURNS TABLE(rows_advanced BIGINT, is_sealed BOOLEAN)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    generation_row atomic_log_generations%ROWTYPE;
    build_row atomic_log_generation_builds%ROWTYPE;
    stage_row atomic_log_generation_completion_stages%ROWTYPE;
    completion_row RECORD;
    processed BIGINT := 0;
    inserted_rows BIGINT := 0;
    inserted_one BIGINT;
BEGIN
    IF maximum_rows < 1 OR maximum_rows > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic completion staging boundary'
            USING ERRCODE = '22023';
    END IF;
    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    SELECT * INTO build_row
      FROM atomic_log_generation_builds
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    IF generation_row.generation IS NULL OR generation_row.build_kind <> 1
       OR build_row.generation IS NULL OR EXISTS (
            SELECT 1 FROM atomic_log_generation_activations
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
       ) THEN
        RAISE EXCEPTION 'Atomic completion staging requires an inactive excision build'
            USING ERRCODE = '55000';
    END IF;
    IF build_row.source_generation > 0 AND NOT EXISTS (
        SELECT 1 FROM atomic_log_generation_completions
         WHERE database_id = candidate_database_id
           AND generation = build_row.source_generation
    ) THEN
        RAISE EXCEPTION 'Atomic excision source has not completed its own publication'
            USING ERRCODE = '55000';
    END IF;

    INSERT INTO atomic_log_generation_completion_stages
           (database_id, generation, phase)
    VALUES (candidate_database_id, candidate_generation, 0)
    ON CONFLICT DO NOTHING;
    SELECT * INTO stage_row
      FROM atomic_log_generation_completion_stages
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       FOR UPDATE;
    IF stage_row.phase = 2 THEN
        rows_advanced := 0;
        is_sealed := true;
        RETURN NEXT;
        RETURN;
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    IF stage_row.phase = 0 THEN
        IF build_row.source_generation = 0 THEN
            UPDATE atomic_log_generation_completion_stages
               SET phase = 1, cursor_t = NULL, cursor_entity = NULL
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
        ELSE
            FOR completion_row IN
                SELECT request_t, request_entity
                  FROM atomic_completed_excision_requests
                 WHERE database_id = candidate_database_id
                   AND generation = build_row.source_generation
                   AND (stage_row.cursor_t IS NULL OR
                        (request_t, request_entity) >
                        (stage_row.cursor_t, stage_row.cursor_entity))
                 ORDER BY request_t, request_entity
                 LIMIT maximum_rows
            LOOP
                INSERT INTO atomic_completed_excision_requests
                       (database_id, request_t, request_entity, generation)
                VALUES (candidate_database_id, completion_row.request_t,
                        completion_row.request_entity, candidate_generation)
                ON CONFLICT DO NOTHING;
                GET DIAGNOSTICS inserted_one = ROW_COUNT;
                inserted_rows := inserted_rows + inserted_one;
                processed := processed + 1;
                stage_row.cursor_t := completion_row.request_t;
                stage_row.cursor_entity := completion_row.request_entity;
            END LOOP;
            UPDATE atomic_log_generation_completion_stages
               SET cursor_t = stage_row.cursor_t,
                   cursor_entity = stage_row.cursor_entity,
                   row_count = row_count + inserted_rows,
                   phase = CASE WHEN processed < maximum_rows THEN 1 ELSE 0 END
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
            IF processed < maximum_rows THEN
                UPDATE atomic_log_generation_completion_stages
                   SET cursor_t = NULL, cursor_entity = NULL
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation;
            END IF;
        END IF;
    ELSE
        FOR completion_row IN
            SELECT request_t, request_entity
              FROM atomic_generation_excision_predicates
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
               AND (stage_row.cursor_t IS NULL OR
                    (request_t, request_entity) >
                    (stage_row.cursor_t, stage_row.cursor_entity))
             ORDER BY request_t, request_entity
             LIMIT maximum_rows
        LOOP
            INSERT INTO atomic_completed_excision_requests
                   (database_id, request_t, request_entity, generation)
            VALUES (candidate_database_id, completion_row.request_t,
                    completion_row.request_entity, candidate_generation)
            ON CONFLICT DO NOTHING;
            GET DIAGNOSTICS inserted_one = ROW_COUNT;
            inserted_rows := inserted_rows + inserted_one;
            processed := processed + 1;
            stage_row.cursor_t := completion_row.request_t;
            stage_row.cursor_entity := completion_row.request_entity;
        END LOOP;
        UPDATE atomic_log_generation_completion_stages
           SET cursor_t = stage_row.cursor_t,
               cursor_entity = stage_row.cursor_entity,
               row_count = row_count + inserted_rows,
               phase = CASE WHEN processed < maximum_rows THEN 2 ELSE 1 END,
               sealed_at = CASE WHEN processed < maximum_rows
                                THEN clock_timestamp() ELSE NULL END
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
    END IF;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);

    SELECT phase = 2 INTO is_sealed
      FROM atomic_log_generation_completion_stages
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    rows_advanced := processed;
    RETURN NEXT;
END;
$$;

-- Restore streams an already-authenticated completion set in bounded arrays.
-- Its archive manifest supplies the exact expected cardinality; duplicate
-- retry batches are harmless and cannot inflate the durable count.
CREATE OR REPLACE FUNCTION atomic_stage_restore_completions(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    request_ts BIGINT[],
    request_entities BIGINT[]
)
RETURNS BIGINT
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    input_count BIGINT;
    inserted_count BIGINT;
BEGIN
    input_count := cardinality(request_ts);
    IF input_count IS NULL OR input_count <> cardinality(request_entities)
       OR input_count > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic restore completion batch'
            USING ERRCODE = '22023';
    END IF;
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_log_generations g
          JOIN atomic_log_generation_builds b
            ON b.database_id = g.database_id AND b.generation = g.generation
         WHERE g.database_id = candidate_database_id
           AND g.generation = candidate_generation
           AND g.build_kind IN (0, 2)
           AND (g.build_kind = 2 OR b.frozen_plan_hash IS NOT NULL)
    ) OR EXISTS (
        SELECT 1 FROM atomic_log_generation_activations
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic restore completion staging requires an inactive restore build'
            USING ERRCODE = '55000';
    END IF;
    INSERT INTO atomic_log_generation_completion_stages
           (database_id, generation, phase)
    VALUES (candidate_database_id, candidate_generation, 1)
    ON CONFLICT DO NOTHING;
    PERFORM 1 FROM atomic_log_generation_completion_stages
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND phase = 1
     FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic restore completion set is already sealed'
            USING ERRCODE = '55000';
    END IF;
    WITH supplied AS (
        SELECT request_t, request_entity
          FROM unnest(request_ts, request_entities)
               AS u(request_t, request_entity)
    ), inserted AS (
        INSERT INTO atomic_completed_excision_requests
               (database_id, request_t, request_entity, generation)
        SELECT candidate_database_id, request_t, request_entity,
               candidate_generation
          FROM supplied
         WHERE request_t > 0 AND request_entity >= 0
        ON CONFLICT DO NOTHING
        RETURNING 1
    )
    SELECT count(*) INTO inserted_count FROM inserted;
    IF inserted_count <> input_count AND EXISTS (
        SELECT 1 FROM unnest(request_ts, request_entities)
             AS u(request_t, request_entity)
         WHERE request_t <= 0 OR request_entity < 0
    ) THEN
        RAISE EXCEPTION 'Invalid Atomic restore completion identity'
            USING ERRCODE = '22023';
    END IF;
    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    UPDATE atomic_log_generation_completion_stages
       SET row_count = row_count + inserted_count
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
    RETURN inserted_count;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_seal_restore_completions(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    expected_count BIGINT
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF expected_count < 0 OR NOT EXISTS (
        SELECT 1
          FROM atomic_log_generation_completion_stages s
          JOIN atomic_log_generations g
            ON g.database_id = s.database_id AND g.generation = s.generation
          JOIN atomic_log_generation_builds b
            ON b.database_id = s.database_id AND b.generation = s.generation
         WHERE s.database_id = candidate_database_id
           AND s.generation = candidate_generation
           AND s.phase = 1
           AND s.row_count = expected_count
           AND g.build_kind IN (0, 2)
           AND (g.build_kind = 2 OR b.frozen_plan_hash IS NOT NULL)
    ) THEN
        RAISE EXCEPTION 'Atomic restore completion set is incomplete'
            USING ERRCODE = '23503';
    END IF;
    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    UPDATE atomic_log_generation_completion_stages
       SET phase = 2, sealed_at = clock_timestamp()
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
END;
$$;

-- Root-last restore into a brand-new alias. All immutable rows, program
-- values, and the exact completion set are durable before this transaction;
-- inserting the first head plus its activation/marker is constant work.
CREATE OR REPLACE FUNCTION atomic_activate_initial_log_generation(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    candidate_basis BIGINT,
    candidate_head_hash BYTEA,
    candidate_state_hash BYTEA
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    builder_pin_key BIGINT;
BEGIN
    PERFORM database_id
      FROM atomic_databases
     WHERE database_id = candidate_database_id
       FOR UPDATE;
    IF NOT FOUND OR EXISTS (
        SELECT 1 FROM atomic_heads WHERE database_id = candidate_database_id
    ) THEN
        RAISE EXCEPTION 'Atomic initial restore target is already published or missing'
            USING ERRCODE = '55000';
    END IF;
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_log_generations g
          JOIN atomic_log_generation_builds b
            ON b.database_id = g.database_id AND b.generation = g.generation
          JOIN atomic_log_generation_checkpoints c
            ON c.database_id = g.database_id AND c.generation = g.generation
          JOIN atomic_log_generation_completion_stages s
            ON s.database_id = g.database_id AND s.generation = g.generation
         WHERE g.database_id = candidate_database_id
           AND g.generation = candidate_generation
           AND g.build_kind = 0
           AND b.source_generation IS NULL
           AND b.frozen_plan_hash IS NOT NULL
           AND c.through_basis_t = candidate_basis
           AND c.head_hash = candidate_head_hash
           AND c.state_hash = candidate_state_hash
           AND s.phase = 2
    ) THEN
        RAISE EXCEPTION 'Atomic initial restore candidate is incomplete'
            USING ERRCODE = '23503';
    END IF;
    SELECT atomic_tree_database_build_pin_key(candidate_database_id)
      INTO builder_pin_key;
    IF builder_pin_key IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock(builder_pin_key) THEN
        RAISE EXCEPTION 'Atomic initial restore is blocked by a live builder'
            USING ERRCODE = '55006';
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    INSERT INTO atomic_heads(database_id, basis_t, tx_hash, log_generation)
    VALUES (candidate_database_id, candidate_basis, candidate_head_hash,
            candidate_generation);
    INSERT INTO atomic_log_generation_activations
           (database_id, generation, prior_generation, prior_basis_t,
            basis_t, head_hash, state_hash, manifest_hash)
    VALUES (candidate_database_id, candidate_generation, 0, 0,
            candidate_basis, candidate_head_hash, candidate_state_hash, NULL);
    INSERT INTO atomic_log_generation_completions(database_id, generation)
    VALUES (candidate_database_id, candidate_generation);
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
END;
$$;

-- The only write-fencing step is a small conditional root change. Tree
-- membership application and excision completion happen separately after the
-- new log is visible, so an O(delta) physical-index transition never extends
-- transactor unavailability.
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
    source_pin_key BIGINT;
    builder_pin_key BIGINT;
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
    IF generation_row.build_kind = 2 THEN
        IF EXISTS (
            SELECT 1 FROM atomic_transactor_leases
             WHERE lease_scope = candidate_database_id
               AND expires_at > clock_timestamp()
        ) THEN
            RAISE EXCEPTION 'Atomic point restore requires transactors to be stopped'
                USING ERRCODE = '55006';
        END IF;
        -- Durable build rows are resumability ledgers, not liveness evidence.
        -- Active tree/log builders hold the database-scoped shared session
        -- pin; stale intent/build rows after a crash therefore cannot wedge a
        -- restore forever.
        SELECT atomic_tree_database_build_pin_key(candidate_database_id)
          INTO builder_pin_key;
        IF builder_pin_key IS NULL
           OR NOT pg_catalog.pg_try_advisory_xact_lock(builder_pin_key) THEN
            RAISE EXCEPTION 'Atomic point restore is blocked by a live generation or tree builder'
                USING ERRCODE = '55006';
        END IF;
        SELECT atomic_log_generation_pin_key(candidate_database_id, current_generation)
          INTO source_pin_key;
        IF source_pin_key IS NULL
           OR NOT pg_catalog.pg_try_advisory_xact_lock(source_pin_key) THEN
            RAISE EXCEPTION 'Atomic point restore is blocked by a live peer or backup generation pin'
                USING ERRCODE = '55006';
        END IF;
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
    IF NOT EXISTS (
        SELECT 1 FROM atomic_log_generation_completion_stages s
         WHERE s.database_id = candidate_database_id
           AND s.generation = candidate_generation
           AND s.phase = 2
           AND s.sealed_at IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Atomic generation activation requires a sealed completion set'
            USING ERRCODE = '23503';
    END IF;
    IF generation_row.build_kind = 1 THEN
        IF candidate_manifest_hash IS NULL THEN
            IF EXISTS (
                SELECT 1 FROM atomic_tree_publications
                 WHERE database_id = candidate_database_id
                   AND log_generation = current_generation
            ) THEN
                RAISE EXCEPTION 'Atomic indexed excision requires a staged successor tree'
                    USING ERRCODE = '23503';
            END IF;
        ELSE
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
                RAISE EXCEPTION 'Atomic excision activation requires a staged authenticated tree'
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
        END IF;
    ELSIF candidate_manifest_hash IS NOT NULL THEN
        RAISE EXCEPTION 'Atomic log-only restore must not claim a tree root'
            USING ERRCODE = '23514';
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
    IF generation_row.build_kind = 2 THEN
        -- Restore prepared its exact completion set under the inactive
        -- generation. One root marker makes that set active with the head.
        INSERT INTO atomic_log_generation_completions(database_id, generation)
        VALUES (candidate_database_id, candidate_generation);
    END IF;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
END;
$$;

-- Publish the already-staged excision tree outside the transactor head fence,
-- then expose its already-sealed completion set with one root marker. A crash
-- before this function leaves a visible excised log but no false completion;
-- retry is idempotent and publication is independent of history length.
CREATE OR REPLACE FUNCTION atomic_complete_excision_generation(
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
    activation_row atomic_log_generation_activations%ROWTYPE;
    generation_row atomic_log_generations%ROWTYPE;
    candidate_revision BIGINT;
BEGIN
    SELECT * INTO activation_row
      FROM atomic_log_generation_activations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    IF activation_row.generation IS NULL OR generation_row.build_kind <> 1
       OR activation_row.manifest_hash IS DISTINCT FROM candidate_manifest_hash
       OR NOT EXISTS (
            SELECT 1 FROM atomic_log_generation_completion_stages
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
               AND phase = 2
       )
       OR NOT EXISTS (
            SELECT 1 FROM atomic_heads
             WHERE database_id = candidate_database_id
               AND log_generation = candidate_generation
       ) THEN
        RAISE EXCEPTION 'Atomic excision generation is not the active staged completion'
            USING ERRCODE = '40001';
    END IF;
    IF candidate_manifest_hash IS NOT NULL THEN
        SELECT publication_revision INTO candidate_revision
          FROM atomic_tree_manifests
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
           AND basis_t = activation_row.basis_t
           AND tx_hash = activation_row.head_hash
           AND state_hash = activation_row.state_hash
           AND manifest_hash = candidate_manifest_hash;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Atomic excision completion has no staged authenticated tree'
                USING ERRCODE = '23503';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM atomic_tree_publications
             WHERE database_id = candidate_database_id
               AND publication_revision = candidate_revision
               AND manifest_hash = candidate_manifest_hash
               AND log_generation = candidate_generation
        ) THEN
            INSERT INTO atomic_tree_publications
                   (database_id, publication_revision, basis_t, tx_hash,
                    manifest_hash, log_generation)
            VALUES (candidate_database_id, candidate_revision, activation_row.basis_t,
                    activation_row.head_hash, candidate_manifest_hash,
                    candidate_generation);
        END IF;
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    INSERT INTO atomic_log_generation_completions(database_id, generation)
    VALUES (candidate_database_id, candidate_generation)
    ON CONFLICT DO NOTHING;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
END;
$$;

-- Completed-build metadata is removable coordination, not logical history.
-- Each call drains no more than the requested number of rows; once the root
-- marker exists, cleanup may proceed independently of sync-excise readers.
CREATE OR REPLACE FUNCTION atomic_cleanup_log_generation_build(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    maximum_rows BIGINT
)
RETURNS TABLE(rows_removed BIGINT, is_complete BOOLEAN)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    removed BIGINT;
BEGIN
    IF maximum_rows < 1 OR maximum_rows > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic generation build cleanup boundary'
            USING ERRCODE = '22023';
    END IF;
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_log_generation_activations a
          JOIN atomic_log_generation_completions c
            ON c.database_id = a.database_id AND c.generation = a.generation
         WHERE a.database_id = candidate_database_id
           AND a.generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic generation build is not durably complete'
            USING ERRCODE = '55000';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_log_generation_builds
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    ) THEN
        rows_removed := 0;
        is_complete := true;
        RETURN NEXT;
        RETURN;
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    WITH victims AS (
        SELECT ctid FROM atomic_generation_excision_predicates
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
         ORDER BY request_t, request_entity
         LIMIT maximum_rows
    )
    DELETE FROM atomic_generation_excision_predicates p
     USING victims v WHERE p.ctid = v.ctid;
    GET DIAGNOSTICS removed = ROW_COUNT;
    IF removed > 0 THEN
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        rows_removed := removed;
        is_complete := false;
        RETURN NEXT;
        RETURN;
    END IF;

    WITH victims AS (
        SELECT ctid FROM atomic_log_generation_checkpoints
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
         ORDER BY through_basis_t
         LIMIT maximum_rows
    )
    DELETE FROM atomic_log_generation_checkpoints c
     USING victims v WHERE c.ctid = v.ctid;
    GET DIAGNOSTICS removed = ROW_COUNT;
    IF removed > 0 THEN
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        rows_removed := removed;
        is_complete := false;
        RETURN NEXT;
        RETURN;
    END IF;

    DELETE FROM atomic_log_generation_completion_stages
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    GET DIAGNOSTICS removed = ROW_COUNT;
    IF removed > 0 THEN
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        rows_removed := removed;
        is_complete := false;
        RETURN NEXT;
        RETURN;
    END IF;

    DELETE FROM atomic_log_generation_builds
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    GET DIAGNOSTICS removed = ROW_COUNT;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
    rows_removed := removed;
    is_complete := true;
    RETURN NEXT;
END;
$$;

-- Drain one retired physical log generation in small restart-safe steps. A
-- peer Database value or in-flight backup holds the matching shared session
-- pin. The first successful exclusive claim permanently closes new admission
-- through collecting_at; subsequent calls reacquire exclusive only to perform
-- one bounded phase. Tree roots must be collected first because they are the
-- remaining authenticated readers of these log coordinates.
CREATE OR REPLACE FUNCTION atomic_collect_log_generation(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    older_than_millis BIGINT,
    maximum_rows BIGINT
)
RETURNS TABLE(rows_removed BIGINT, collection_phase SMALLINT, is_complete BOOLEAN)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    retirement_row atomic_log_generation_retirements%ROWTYPE;
    progress_phase SMALLINT;
    generation_pin_key BIGINT;
    removed BIGINT := 0;
    ignored BIGINT;
    content_candidate RECORD;
BEGIN
    IF candidate_generation < 0 OR older_than_millis < 0
       OR maximum_rows < 1 OR maximum_rows > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic log-generation garbage boundary'
            USING ERRCODE = '22023';
    END IF;
    SELECT * INTO retirement_row
      FROM atomic_log_generation_retirements
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic log generation is not retired'
            USING ERRCODE = '23503';
    END IF;
    IF retirement_row.retired_at + older_than_millis * interval '1 millisecond'
       > clock_timestamp() THEN
        RAISE EXCEPTION 'Atomic log generation has not reached its retention age'
            USING ERRCODE = '55000';
    END IF;
    IF EXISTS (
        SELECT 1 FROM atomic_heads
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Active Atomic log generation cannot be collected'
            USING ERRCODE = '55000';
    END IF;

    SELECT atomic_log_generation_pin_key(candidate_database_id, candidate_generation)
      INTO generation_pin_key;
    IF generation_pin_key IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock(generation_pin_key) THEN
        RAISE EXCEPTION 'Atomic log generation is pinned by a peer or backup'
            USING ERRCODE = '55006';
    END IF;

    -- These ledgers are consumed by their own bounded collectors. Numeric
    -- generation columns deliberately have no cascading FK: collection must
    -- prove every physical reader is gone before removing authoritative bytes.
    IF EXISTS (
        SELECT 1 FROM atomic_tree_build_intents
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_manifests
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_publications
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_retirements
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_log_generation_builds
         WHERE database_id = candidate_database_id
           AND (generation = candidate_generation
                OR source_generation = candidate_generation)
    ) OR EXISTS (
        SELECT 1 FROM atomic_log_generation_retirements
         WHERE database_id = candidate_database_id
           AND successor_generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic log generation still has a derived root or build dependency'
            USING ERRCODE = '55000';
    END IF;

    PERFORM set_config('atomic.log_generation_gc', 'v14', true);
    IF retirement_row.collecting_at IS NULL THEN
        UPDATE atomic_log_generation_retirements
           SET collecting_at = clock_timestamp()
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
    END IF;
    INSERT INTO atomic_log_generation_collection_progress
           (database_id, generation)
    VALUES (candidate_database_id, candidate_generation)
    ON CONFLICT DO NOTHING;
    SELECT phase INTO progress_phase
      FROM atomic_log_generation_collection_progress
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
     FOR UPDATE;

    IF progress_phase = 0 THEN
        IF candidate_generation = 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_index_publications
                 WHERE database_id = candidate_database_id
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_index_publications p
             USING victims v WHERE p.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 1 THEN
        IF candidate_generation = 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_index_manifests
                 WHERE database_id = candidate_database_id
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_index_manifests m
             USING victims v WHERE m.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 2 THEN
        IF candidate_generation > 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_generation_request_tempids
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                 ORDER BY request_key_hash, tempid_name
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_generation_request_tempids t
             USING victims v WHERE t.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 3 THEN
        IF candidate_generation = 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_requests
                 WHERE database_id = candidate_database_id
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_requests r
             USING victims v WHERE r.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        ELSE
            WITH victims AS (
                SELECT ctid FROM atomic_generation_requests
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_generation_requests r
             USING victims v WHERE r.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 4 THEN
        IF candidate_generation = 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_transactions
                 WHERE database_id = candidate_database_id
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_transactions t
             USING victims v WHERE t.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        ELSE
            WITH victims AS MATERIALIZED (
                SELECT ctid, content_hash
                  FROM atomic_generation_transactions
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                 ORDER BY basis_t
                 LIMIT maximum_rows
            ), marked AS (
                INSERT INTO atomic_log_generation_garbage_contents
                       (database_id, generation, content_hash)
                SELECT candidate_database_id, candidate_generation, content_hash
                  FROM victims
                ON CONFLICT DO NOTHING
                RETURNING 1
            ), deleted AS (
                DELETE FROM atomic_generation_transactions t
                 USING victims v WHERE t.ctid = v.ctid
                RETURNING 1
            )
            SELECT count(*), (SELECT count(*) FROM marked)
              INTO removed, ignored FROM deleted;
        END IF;
    ELSIF progress_phase = 5 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_program_generation_refs
             WHERE database_id = candidate_database_id
               AND log_generation = candidate_generation
             ORDER BY program_hash
             LIMIT maximum_rows
        )
        DELETE FROM atomic_program_generation_refs r
         USING victims v WHERE r.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 6 THEN
        IF candidate_generation > 0 THEN
            FOR content_candidate IN
                SELECT content_hash
                  FROM atomic_log_generation_garbage_contents
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                 ORDER BY content_hash
                 LIMIT maximum_rows
                 FOR UPDATE SKIP LOCKED
            LOOP
                DELETE FROM atomic_log_generation_garbage_contents
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                   AND content_hash = content_candidate.content_hash;
                IF NOT EXISTS (
                    SELECT 1 FROM atomic_generation_transactions
                     WHERE content_hash = content_candidate.content_hash
                ) AND NOT EXISTS (
                    SELECT 1 FROM atomic_log_generation_garbage_contents
                     WHERE content_hash = content_candidate.content_hash
                ) THEN
                    DELETE FROM atomic_transaction_contents
                     WHERE content_hash = content_candidate.content_hash;
                END IF;
                removed := removed + 1;
            END LOOP;
        END IF;
    ELSIF progress_phase = 7 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_completed_excision_requests
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             ORDER BY request_t, request_entity
             LIMIT maximum_rows
        )
        DELETE FROM atomic_completed_excision_requests r
         USING victims v WHERE r.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 8 THEN
        DELETE FROM atomic_log_generation_completions
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 9 THEN
        IF candidate_generation > 0 THEN
            DELETE FROM atomic_log_generation_activations
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 10 THEN
        IF candidate_generation > 0 THEN
            DELETE FROM atomic_log_generations
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSE
        DELETE FROM atomic_log_generation_retirements
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        GET DIAGNOSTICS removed = ROW_COUNT;
        PERFORM set_config('atomic.log_generation_gc', 'off', true);
        rows_removed := removed;
        collection_phase := 11;
        is_complete := true;
        RETURN NEXT;
        RETURN;
    END IF;

    IF removed = 0 THEN
        UPDATE atomic_log_generation_collection_progress
           SET phase = phase + 1, updated_at = clock_timestamp()
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        progress_phase := progress_phase + 1;
    ELSE
        UPDATE atomic_log_generation_collection_progress
           SET updated_at = clock_timestamp()
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
    END IF;
    PERFORM set_config('atomic.log_generation_gc', 'off', true);
    rows_removed := removed;
    collection_phase := progress_phase;
    is_complete := false;
    RETURN NEXT;
END;
$$;

REVOKE ALL ON FUNCTION atomic_activate_log_generation(
    TEXT, BIGINT, BIGINT, BYTEA, BYTEA, BYTEA
) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_activate_log_generation(
    TEXT, BIGINT, BIGINT, BYTEA, BYTEA, BYTEA
) TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_activate_initial_log_generation(
    TEXT, BIGINT, BIGINT, BYTEA, BYTEA
) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_activate_initial_log_generation(
    TEXT, BIGINT, BIGINT, BYTEA, BYTEA
) TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_complete_excision_generation(TEXT, BIGINT, BYTEA) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_complete_excision_generation(TEXT, BIGINT, BYTEA)
TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_stage_excision_completions(TEXT, BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_stage_excision_completions(TEXT, BIGINT, BIGINT)
TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_stage_restore_completions(TEXT, BIGINT, BIGINT[], BIGINT[])
FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_stage_restore_completions(TEXT, BIGINT, BIGINT[], BIGINT[])
TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_seal_restore_completions(TEXT, BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_seal_restore_completions(TEXT, BIGINT, BIGINT)
TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_cleanup_log_generation_build(TEXT, BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_cleanup_log_generation_build(TEXT, BIGINT, BIGINT)
TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_collect_log_generation(TEXT, BIGINT, BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_log_generation(TEXT, BIGINT, BIGINT, BIGINT)
TO CURRENT_USER;

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generations FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_builds FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_transaction_contents FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_generation_transactions FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_generation_requests FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_generation_request_tempids FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_generation_excision_predicates FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_checkpoints FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_completions FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_activations FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_completed_excision_requests FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_retirements FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_completion_stages FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_collection_progress FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_log_generation_garbage_contents FROM PUBLIC;
