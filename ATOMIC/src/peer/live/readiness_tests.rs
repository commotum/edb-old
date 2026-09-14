//! Target-coordinate witnesses with manual canonical index adoption.
//! No background worker or wall-clock latency assertion can make them pass.
use super::*;
use crate::index::tree::TreeConfig;
use crate::storage::{BlockDatabase, IndexDescriptor, PgBlockStore};
use crate::{
    Attribute, AttributeName, BlockTransactor, BlockWriterOptions, Cardinality, EntityRef, Keyword,
    Schema, TransactionRequest, TxOp, Unique, Value, ValueType,
};

const INDEXED_LATER: u32 = 1000;
const UNIQUE_LATER: u32 = 1001;

struct Fixture {
    admin: postgres::Client,
    schema: String,
    config: PostgresConnectionConfig,
}

impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}

fn fixture() -> Option<(Fixture, BlockTransactor, Peer)> {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP target readiness PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!("peer_readiness_{:032x}", crate::uuid_v7().unwrap());
    let mut admin = postgres::Client::connect(&connection, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let scoped = if connection.starts_with("postgres://") || connection.starts_with("postgresql://")
    {
        format!(
            "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
            if connection.contains('?') { '&' } else { '?' }
        )
    } else {
        format!("{connection} options='-csearch_path={schema},pg_catalog'")
    };
    let config = PostgresConnectionConfig::plaintext(scoped);
    PgBlockStore::install(&config).unwrap();
    let mut application = Schema::new();
    for (id, name) in [(INDEXED_LATER, "indexed"), (UNIQUE_LATER, "unique")] {
        application
            .install(Attribute::new(
                id,
                Keyword::new("readiness", name),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
    }
    let database = BlockDatabase::create(&config, "target", application).unwrap();
    let writer = BlockTransactor::claim(
        &config,
        database,
        BlockWriterOptions {
            lease_duration: Duration::from_secs(120),
            ..Default::default()
        },
    )
    .unwrap();
    let peer = Peer::connect_configured(&config, "target", 32).unwrap();
    Some((
        Fixture {
            admin,
            schema,
            config,
        },
        writer,
        peer,
    ))
}

fn commit(writer: &mut BlockTransactor, key: &str, ops: Vec<TxOp>) -> u64 {
    writer
        .transact(&TransactionRequest::new(key, ops))
        .unwrap()
        .basis_t
}

fn step(fixture: &Fixture, writer: &mut BlockTransactor) -> IndexDescriptor {
    let input = writer.index_input().unwrap();
    let mut store = PgBlockStore::connect(&fixture.config).unwrap();
    let prepared = input.prepare(&mut store, &TreeConfig::default()).unwrap();
    let descriptor = prepared.descriptor().clone();
    writer.adopt_index(prepared).unwrap();
    descriptor
}

fn finish_projection(fixture: &Fixture, writer: &mut BlockTransactor) -> IndexDescriptor {
    loop {
        let descriptor = step(fixture, writer);
        if descriptor.pending_avet.is_empty() && descriptor.avet_work.is_empty() {
            return descriptor;
        }
    }
}

fn assert_schema_pending(peer: &Peer, target: u64) {
    assert_eq!(
        peer.sync_schema(target, Duration::ZERO).unwrap_err().code,
        "peer/sync-schema-timeout"
    );
}

fn assert_index_pending(peer: &Peer, target: u64) {
    assert_eq!(
        peer.sync_index(target, Duration::ZERO).unwrap_err().code,
        "peer/sync-index-timeout"
    );
}

#[test]
fn readiness_ignores_later_work_but_keeps_earlier_partial_checkpoints() {
    let Some((fixture, mut writer, peer)) = fixture() else {
        return;
    };
    let seed = commit(
        &mut writer,
        "seed",
        [INDEXED_LATER, UNIQUE_LATER]
            .into_iter()
            .map(|attribute| TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute,
                value: Value::Long(7).into(),
            })
            .collect(),
    );
    assert_eq!(finish_projection(&fixture, &mut writer).basis, seed);
    let held = peer.sync_index(seed, Duration::ZERO).unwrap();

    let mut attribute = held.schema().attribute(INDEXED_LATER).unwrap().clone();
    attribute.indexed = true;
    let enable = commit(
        &mut writer,
        "enable",
        vec![TxOp::AlterAttribute(attribute.clone())],
    );
    let newer = peer.sync_schema(seed, Duration::ZERO).unwrap();
    assert_eq!(newer.basis_t(), enable);
    assert!(!newer.has_avet(&AttributeName::Id(INDEXED_LATER)).unwrap());
    assert_schema_pending(&peer, enable);
    assert_index_pending(&peer, enable);

    // The physical descriptor can reach enable while its AVET work is partial.
    let partial = step(&fixture, &mut writer);
    assert_eq!(partial.basis, enable);
    assert_eq!(partial.pending_avet, vec![INDEXED_LATER]);
    assert!(!partial.avet_work.is_empty());
    peer.sync_schema(seed, Duration::ZERO).unwrap();
    peer.sync_index(seed, Duration::ZERO).unwrap();
    assert_schema_pending(&peer, enable);
    assert_index_pending(&peer, enable);

    // A newer cycle in the tail cannot mask the older unfinished checkpoint.
    let mut disabled = attribute.clone();
    disabled.indexed = false;
    commit(
        &mut writer,
        "disable-in-tail",
        vec![TxOp::AlterAttribute(disabled.clone())],
    );
    let reenable = commit(
        &mut writer,
        "reenable-in-tail",
        vec![TxOp::AlterAttribute(attribute)],
    );
    assert_schema_pending(&peer, enable);
    assert_index_pending(&peer, enable);
    peer.sync_schema(seed, Duration::ZERO).unwrap();
    assert_eq!(finish_projection(&fixture, &mut writer).basis, enable);

    // Old work is now complete; the newer enable remains independently pending.
    peer.sync_schema(enable, Duration::ZERO).unwrap();
    peer.sync_index(enable, Duration::ZERO).unwrap();
    assert_schema_pending(&peer, reenable);
    let next_partial = step(&fixture, &mut writer);
    assert_eq!(next_partial.basis, reenable);
    assert_eq!(next_partial.pending_avet, vec![INDEXED_LATER]);
    peer.sync_schema(enable, Duration::ZERO).unwrap();
    peer.sync_index(enable, Duration::ZERO).unwrap();
    assert_schema_pending(&peer, reenable);
    assert_index_pending(&peer, reenable);
    finish_projection(&fixture, &mut writer);
    peer.sync_schema(reenable, Duration::ZERO).unwrap();
    peer.sync_index(reenable, Duration::ZERO).unwrap();

    // Physical AVET removal is index work, not an asynchronous schema enable.
    let disable = commit(
        &mut writer,
        "disable-index",
        vec![TxOp::AlterAttribute(disabled)],
    );
    let removal = step(&fixture, &mut writer);
    assert!(removal.avet_work.iter().any(|work| !work.adding));
    peer.sync_schema(disable, Duration::ZERO).unwrap();
    peer.sync_index(reenable, Duration::ZERO).unwrap();
    assert_index_pending(&peer, disable);
    finish_projection(&fixture, &mut writer);
    peer.sync_index(disable, Duration::ZERO).unwrap();

    // Historical values require ready AVET before uniqueness may be enabled.
    // Do not turn this admission rule into a fictitious unique-only backfill.
    let mut unique = held.schema().attribute(UNIQUE_LATER).unwrap().clone();
    unique.unique = Some(Unique::Value);
    assert_eq!(
        writer
            .transact(&TransactionRequest::new(
                "illegal-unique",
                vec![TxOp::AlterAttribute(unique.clone())],
            ))
            .unwrap_err()
            .code,
        "schema/unique-requires-avet"
    );
    let mut indexed = held.schema().attribute(UNIQUE_LATER).unwrap().clone();
    indexed.indexed = true;
    let indexed_t = commit(
        &mut writer,
        "index-before-unique",
        vec![TxOp::AlterAttribute(indexed)],
    );
    peer.sync_schema(disable, Duration::ZERO).unwrap();
    assert_schema_pending(&peer, indexed_t);
    let unique_partial = step(&fixture, &mut writer);
    assert_eq!(unique_partial.pending_avet, vec![UNIQUE_LATER]);
    peer.sync_index(disable, Duration::ZERO).unwrap();
    assert_index_pending(&peer, indexed_t);
    finish_projection(&fixture, &mut writer);
    peer.sync_schema(indexed_t, Duration::ZERO).unwrap();

    // Atomically replace the index flag with uniqueness. Effective membership
    // never turns off, so this is synchronously ready, not another backfill.
    let unique_t = commit(
        &mut writer,
        "enable-unique",
        vec![TxOp::AlterAttribute(unique)],
    );
    assert!(
        peer.sync_schema(unique_t, Duration::ZERO)
            .unwrap()
            .has_avet(&AttributeName::Id(UNIQUE_LATER))
            .unwrap()
    );
    let before_unique = peer.snapshot().native;
    assert!(
        !super::avet_transition_after(
            &before_unique.database_value(),
            UNIQUE_LATER,
            unique_t,
            true,
            indexed_t,
        )
        .unwrap(),
        "the original effective membership transition remains indexed_t"
    );
    assert!(step(&fixture, &mut writer).avet_work.is_empty());
    peer.sync_index(unique_t, Duration::ZERO).unwrap();

    assert_eq!(held.basis_t(), seed);
    assert!(!held.has_avet(&AttributeName::Id(INDEXED_LATER)).unwrap());
    writer.release().unwrap();
}
