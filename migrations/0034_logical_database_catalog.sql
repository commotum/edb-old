-- Public names are mutable routing metadata. Canonical database IDs/lineages
-- never move and issued IDs survive physical reclamation as tombstones.
CREATE TABLE atomic_database_identities (
    database_id TEXT PRIMARY KEY CHECK (database_id <> ''),
    lineage_id TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    retired_at TIMESTAMPTZ
);
CREATE TABLE atomic_database_names (
    name TEXT PRIMARY KEY CHECK (name <> ''),
    database_id TEXT UNIQUE NOT NULL REFERENCES atomic_database_identities(database_id)
);
CREATE INDEX atomic_database_identities_retired ON atomic_database_identities(database_id)
    WHERE retired_at IS NOT NULL;
INSERT INTO atomic_database_identities(database_id,lineage_id,created_at)
    SELECT database_id,lineage_id,created_at FROM atomic_databases;
INSERT INTO atomic_database_names(name,database_id)
    SELECT database_id,database_id FROM atomic_databases;

-- Serialize publication with retirement through the issued identity. A head
-- UPDATE already owns its head row; independent index publication must NOT
-- acquire it, because its canonical-database lock would invert the writer's
-- head-before-content-FK order. The identity lock also covers headless restore
-- and rejects an old REPEATABLE READ snapshot after retirement.
CREATE FUNCTION atomic_require_active_publication()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
DECLARE published_database TEXT;
BEGIN
    IF TG_TABLE_NAME='atomic_fulltext_projections' THEN
        SELECT database_id INTO published_database FROM atomic_tree_manifests WHERE manifest_hash=NEW.manifest_hash;
    ELSE
        published_database:=NEW.database_id;
    END IF;
    PERFORM 1 FROM atomic_database_identities
        WHERE database_id=published_database AND retired_at IS NULL FOR SHARE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic database identity is retired or unregistered' USING ERRCODE='55000';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER atomic_heads_active BEFORE INSERT OR UPDATE ON atomic_heads
    FOR EACH ROW EXECUTE FUNCTION atomic_require_active_publication();
CREATE TRIGGER atomic_tree_publications_active BEFORE INSERT ON atomic_tree_publications
    FOR EACH ROW EXECUTE FUNCTION atomic_require_active_publication();
CREATE TRIGGER atomic_index_publications_active BEFORE INSERT ON atomic_index_publications
    FOR EACH ROW EXECUTE FUNCTION atomic_require_active_publication();
CREATE TRIGGER atomic_fulltext_projections_active BEFORE INSERT ON atomic_fulltext_projections
    FOR EACH ROW EXECUTE FUNCTION atomic_require_active_publication();
DO $$ BEGIN
    EXECUTE format('ALTER FUNCTION %I.atomic_require_active_publication() SET search_path TO %I, pg_catalog, pg_temp', current_schema(), current_schema());
END $$;
REVOKE ALL ON atomic_database_identities,atomic_database_names FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_require_active_publication() FROM PUBLIC;
