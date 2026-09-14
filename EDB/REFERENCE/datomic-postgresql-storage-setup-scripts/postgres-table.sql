-- Table: mycloud_kvs

-- DROP TABLE mycloud_kvs;

CREATE TABLE mycloud_kvs
(
 id text NOT NULL,
 rev integer,
 map text,
 val bytea,
 CONSTRAINT pk_id PRIMARY KEY (id )
)
WITH (
 OIDS=FALSE
);
ALTER TABLE mycloud_kvs
 OWNER TO postgres;
GRANT ALL ON TABLE mycloud_kvs TO postgres;
GRANT ALL ON TABLE mycloud_kvs TO public;
