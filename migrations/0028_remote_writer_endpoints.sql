-- Discovery contains routing metadata only, never authentication secrets.
-- Lease expiry/takeover makes old records undiscoverable without cleanup races.
CREATE TABLE atomic_remote_writer_endpoints (
    database_id TEXT PRIMARY KEY REFERENCES atomic_databases(database_id),
    lineage_id TEXT NOT NULL,
    holder_id TEXT NOT NULL,
    lease_epoch BIGINT NOT NULL CHECK (lease_epoch>0),
    instance_id BYTEA NOT NULL CHECK (octet_length(instance_id)=32),
    network_address TEXT NOT NULL CHECK (octet_length(network_address) BETWEEN 3 AND 128),
    tls_server_name TEXT NOT NULL CHECK (octet_length(tls_server_name) BETWEEN 1 AND 253),
    protocol_version INTEGER NOT NULL CHECK (protocol_version=1)
);
CREATE FUNCTION atomic_validate_remote_writer_endpoint()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_databases d WHERE d.database_id=NEW.database_id AND d.lineage_id=NEW.lineage_id) THEN
        RAISE EXCEPTION 'Atomic remote endpoint lineage mismatch' USING ERRCODE='23514';
    END IF;
    PERFORM 1 FROM atomic_transactor_leases l
      WHERE l.lease_scope=NEW.database_id AND l.holder_id=NEW.holder_id
        AND l.epoch=NEW.lease_epoch AND l.expires_at>clock_timestamp() FOR SHARE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic remote endpoint lease is not current' USING ERRCODE='55000';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER atomic_remote_writer_endpoint_validate BEFORE INSERT OR UPDATE
ON atomic_remote_writer_endpoints FOR EACH ROW EXECUTE FUNCTION atomic_validate_remote_writer_endpoint();

-- Peers can discover live routing metadata without acquiring access to the
-- writer lease relation or being allowed to register/replace an endpoint.
CREATE FUNCTION atomic_discover_remote_writer(requested_database TEXT, requested_lineage TEXT)
RETURNS TABLE(holder_id TEXT,lease_epoch BIGINT,instance_id BYTEA,network_address TEXT,tls_server_name TEXT,protocol_version INTEGER)
LANGUAGE SQL SECURITY DEFINER SET search_path FROM CURRENT AS $$
    SELECT e.holder_id,e.lease_epoch,e.instance_id,e.network_address,e.tls_server_name,e.protocol_version
      FROM atomic_remote_writer_endpoints e JOIN atomic_databases d USING(database_id)
      JOIN atomic_transactor_leases l ON l.lease_scope=e.database_id
     WHERE e.database_id=requested_database AND e.lineage_id=requested_lineage
       AND d.lineage_id=e.lineage_id AND l.holder_id=e.holder_id
       AND l.epoch=e.lease_epoch AND l.expires_at>clock_timestamp()
$$;
REVOKE ALL ON atomic_remote_writer_endpoints FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_validate_remote_writer_endpoint(),atomic_discover_remote_writer(TEXT,TEXT) FROM PUBLIC;
