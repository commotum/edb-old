//! Restore may reuse a verified semantic value only after exact target proof.
use atomic_core::{
    Attribute, AvetProjectionWork, BackupPoint, Cardinality, Database, Digest, EntityRef,
    ErrorCategory, IndexOrder, IndexSegment, Keyword, Peer, PersistentTreeManifest, PortableBackup,
    PostgresIndexer, PostgresMigrator, PostgresStore, RestoreFault, Schema, TransactionRequest,
    TxOp, Value, ValueType, View, encode_index_segment, sha256,
};
use bigdecimal::BigDecimal;
use postgres::{Client, NoTls};
use std::collections::BTreeMap;
use std::fs;
use std::path::{Path, PathBuf};
use std::str::FromStr;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const DECIMAL: u32 = 1000;
const DOUBLE: u32 = 1001;
const FLOAT: u32 = 1002;
const MARKER: u32 = 1003;
const WAIT: Duration = Duration::from_secs(30);

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn isolated(connection: &str) -> String {
    let schema = unique("restore_proof");
    Client::connect(connection, NoTls)
        .unwrap()
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let scoped = if connection.trim_start().starts_with("postgres://")
        || connection.trim_start().starts_with("postgresql://")
    {
        let separator = if connection.contains('?') { '&' } else { '?' };
        format!("{connection}{separator}options=-csearch_path%3D{schema}")
    } else {
        format!("{connection} options='-c search_path={schema}'")
    };
    PostgresMigrator::connect(&scoped)
        .unwrap()
        .migrate()
        .unwrap();
    scoped
}

fn private_directory() -> tempfile::TempDir {
    let directory = tempfile::tempdir().unwrap();
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt as _;
        fs::set_permissions(directory.path(), fs::Permissions::from_mode(0o700)).unwrap();
    }
    directory
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    for (id, name, value_type) in [
        (DECIMAL, "decimal", ValueType::BigDec),
        (DOUBLE, "double", ValueType::Double),
        (FLOAT, "float", ValueType::Float),
        (MARKER, "marker", ValueType::Long),
    ] {
        let mut attribute = Attribute::new(
            id,
            Keyword::new("proof", name),
            value_type,
            Cardinality::One,
        );
        attribute.indexed = id == DECIMAL;
        schema.install(attribute).unwrap();
    }
    schema
}

fn add(name: &str, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp(name.into()),
        attribute,
        value: value.into(),
    }
}

fn hex(hash: &[u8]) -> String {
    hash.iter().map(|byte| format!("{byte:02x}")).collect()
}

fn root_path(directory: &Path, point: &BackupPoint) -> PathBuf {
    directory.join("snapshots").join(format!(
        "{:020}-g{:020}.atbk",
        point.basis_t, point.log_generation
    ))
}

fn root_tree_offset(root: &[u8]) -> usize {
    assert_eq!(&root[..4], b"ATBK");
    assert_eq!(u16::from_be_bytes(root[4..6].try_into().unwrap()), 4);
    let lineage_len = u32::from_be_bytes(root[14..18].try_into().unwrap()) as usize;
    let tag = 18 + lineage_len + 8 + 8 + 32 + 4 * 32;
    assert_eq!(root[tag], 1, "fixture contains a main physical tree");
    tag + 1
}

fn checksum(bytes: &mut [u8]) {
    let at = bytes.len() - 32;
    let hash = sha256(&bytes[..at]);
    bytes[at..].copy_from_slice(&hash);
}

fn encoded_datoms(
    datoms: Vec<atomic_core::Datom>,
    order: IndexOrder,
    history: bool,
) -> Vec<Vec<u8>> {
    // Native tree and legacy flat-segment tie ordering intentionally differ.
    // Encode one datum at a time, preserving the native sequence and every
    // stored representation without imposing the legacy segment comparator.
    datoms
        .into_iter()
        .map(|datom| {
            encode_index_segment(&IndexSegment {
                order,
                history,
                datoms: vec![datom],
            })
            .unwrap()
        })
        .collect()
}

fn information(database: &Database) -> Vec<Vec<Vec<u8>>> {
    [View::Current, View::History]
        .into_iter()
        .flat_map(|view| {
            [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ]
            .map(|order| encoded_datoms(database.datoms(view, order), order, view == View::History))
        })
        .collect()
}

struct Fixture {
    source_connection: String,
    target_connection: String,
    source: String,
    target: String,
    directory: tempfile::TempDir,
    base: BackupPoint,
    point: BackupPoint,
    expected: Database,
    seed_expected: Database,
    seed_request: TransactionRequest,
    entities: BTreeMap<String, u64>,
}

impl Fixture {
    fn new(with_schema_tail: bool) -> Option<Self> {
        let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
            eprintln!("SKIP restore target proof: ATOMIC_POSTGRES_URL is unset");
            return None;
        };
        let source_connection = isolated(&connection);
        let target_connection = isolated(&connection);
        let source = unique("proof_source");
        let target = unique("proof_target");
        PostgresStore::connect(&source_connection)
            .unwrap()
            .create_database(&source, schema())
            .unwrap();
        let writer = common::start_service(&source_connection, &source);
        let seed_request = TransactionRequest::new(
            "exact-stored-seed",
            vec![
                add(
                    "negative",
                    DECIMAL,
                    Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
                ),
                add("negative", DOUBLE, Value::Double(-0.0)),
                add("negative", FLOAT, Value::Float(-0.0)),
                add(
                    "positive",
                    DECIMAL,
                    Value::BigDec(BigDecimal::from_str("1.0").unwrap()),
                ),
                add("positive", DOUBLE, Value::Double(0.0)),
                add("positive", FLOAT, Value::Float(0.0)),
                // NaN is legal on these non-unique ordinary attributes.
                add("nan", DOUBLE, Value::Double(f64::NAN)),
                add("nan", FLOAT, Value::Float(f32::NAN)),
            ],
        )
        .with_tx_instant(1000);
        let seeded = writer
            .client()
            .transact(seed_request.clone(), WAIT)
            .unwrap();
        let entities = seeded.tempids.clone();
        drop(seeded);
        PostgresIndexer::connect(&source_connection, &source)
            .unwrap()
            .consolidate()
            .unwrap();
        let directory = private_directory();
        let mut backup = PortableBackup::connect(&source_connection).unwrap();
        let base = backup.backup_database(&source, directory.path()).unwrap();
        let seed_expected = PortableBackup::verify_backup(directory.path(), base.basis_t, true)
            .unwrap()
            .database;
        if with_schema_tail {
            let mut changed = schema().attribute(DECIMAL).unwrap().clone();
            changed.indexed = false;
            writer
                .client()
                .transact(
                    TransactionRequest::new(
                        "disable-decimal-index",
                        vec![TxOp::AlterAttribute(changed)],
                    )
                    .with_tx_instant(2000),
                    WAIT,
                )
                .unwrap();
        }
        writer.shutdown();
        let mut point = backup.backup_database(&source, directory.path()).unwrap();
        if with_schema_tail {
            assert!(base.basis_t < point.basis_t);
            // Select the earlier, semantically valid accelerator explicitly.
            // A pending add/clear phase is legal under the indexed BASE
            // schema, but not under the unindexed HEAD schema. This makes
            // accidental substitution of the final schema observable.
            let base_root = fs::read(root_path(directory.path(), &base)).unwrap();
            let at = root_tree_offset(&base_root);
            let base_hash = &base_root[at..at + 32];
            let mut tree = PersistentTreeManifest::decode(
                &fs::read(directory.path().join("objects").join(hex(base_hash))).unwrap(),
            )
            .unwrap();
            assert_eq!(tree.basis_t, base.basis_t);
            tree.index_basis_t = tree.basis_t - 1;
            tree.pending_avet = vec![AvetProjectionWork {
                attribute: DECIMAL,
                adding: true,
                history: false,
                clearing: true,
                offset: 0,
            }];
            let payload = tree.encode().unwrap();
            let hash = sha256(&payload);
            fs::write(directory.path().join("objects").join(hex(&hash)), payload).unwrap();
            let path = root_path(directory.path(), &point);
            let mut root = fs::read(&path).unwrap();
            let at = root_tree_offset(&root);
            root[at..at + 32].copy_from_slice(&hash);
            checksum(&mut root);
            point.manifest_hash = sha256(&root);
            fs::write(path, root).unwrap();
        }
        let expected = PortableBackup::verify_backup(directory.path(), point.basis_t, true)
            .unwrap()
            .database;
        Some(Self {
            source_connection,
            target_connection,
            source,
            target,
            directory,
            base,
            point,
            expected,
            seed_expected,
            seed_request,
            entities,
        })
    }

    fn head(&self) -> (i64, i64, Vec<u8>) {
        let row = Client::connect(&self.target_connection, NoTls)
            .unwrap()
            .query_one(
                "SELECT log_generation, basis_t, tx_hash FROM atomic_heads WHERE database_id=$1",
                &[&self.target],
            )
            .unwrap();
        (row.get(0), row.get(1), row.get(2))
    }

    fn assert_values(&self, restored: &Database) {
        assert_eq!(information(restored), information(&self.expected));
        for (name, scale) in [("negative", 2), ("positive", 1)] {
            let entity = self.entities[name];
            let [Value::BigDec(value)] = restored.values(entity, DECIMAL).as_slice() else {
                panic!("missing decimal value");
            };
            assert_eq!(value.fractional_digit_count(), scale);
        }
        // The storage codec intentionally normalizes signed zeros and NaN
        // payloads. Preserve those exact persisted bytes, not raw input bits.
        for (name, f64_bits, f32_bits) in [
            ("negative", 0.0_f64.to_bits(), 0.0_f32.to_bits()),
            ("positive", 0.0_f64.to_bits(), 0.0_f32.to_bits()),
            ("nan", f64::NAN.to_bits(), f32::NAN.to_bits()),
        ] {
            let entity = self.entities[name];
            let values = restored.values(entity, DOUBLE);
            let [Value::Double(value)] = values.as_slice() else {
                panic!("missing double value");
            };
            assert_eq!(value.to_bits(), f64_bits);
            let values = restored.values(entity, FLOAT);
            let [Value::Float(value)] = values.as_slice() else {
                panic!("missing float value");
            };
            assert_eq!(value.to_bits(), f32_bits);
        }
    }
}

#[test]
fn old_base_schema_and_exact_values_survive_generation_rebinding_and_ambiguous_retry() {
    let Some(fixture) = Fixture::new(true) else {
        return;
    };
    assert!(
        fixture
            .seed_expected
            .schema()
            .attribute(DECIMAL)
            .unwrap()
            .indexed
    );
    assert!(
        !fixture
            .expected
            .schema()
            .attribute(DECIMAL)
            .unwrap()
            .indexed
    );
    let mut restore = PortableBackup::connect(&fixture.target_connection).unwrap();
    restore
        .restore_backup(
            fixture.directory.path(),
            fixture.base.basis_t,
            &fixture.target,
        )
        .unwrap();
    let first_generation = fixture.head().0;
    let interrupted = restore
        .restore_backup_with_fault(
            fixture.directory.path(),
            fixture.point.basis_t,
            &fixture.target,
            RestoreFault::AfterCommitBeforeResponse,
        )
        .unwrap_err();
    assert_eq!(interrupted.category, ErrorCategory::Interrupted);
    let committed_head = fixture.head();
    assert_eq!(committed_head.0, first_generation + 1);
    assert_ne!(committed_head.0 as u64, fixture.point.log_generation);
    let restored = restore
        .restore_backup(
            fixture.directory.path(),
            fixture.point.basis_t,
            &fixture.target,
        )
        .unwrap();
    assert_eq!(fixture.head(), committed_head);
    fixture.assert_values(&restored);

    let peer = Peer::connect(&fixture.target_connection, &fixture.target, 8).unwrap();
    assert_eq!(peer.database_value().basis_t(), fixture.point.basis_t);
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    let writer = common::start_service(&fixture.target_connection, &fixture.target);
    let replay = writer
        .client()
        .transact(fixture.seed_request.clone(), WAIT)
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, fixture.base.basis_t);
    assert_eq!(replay.tempids, fixture.entities);
    let replay_bytes = encoded_datoms(
        replay.db_after.datoms(IndexOrder::Eavt).unwrap(),
        IndexOrder::Eavt,
        false,
    );
    assert_eq!(replay_bytes, information(&fixture.seed_expected)[0]);
    writer.shutdown();
    assert_eq!(fixture.head(), committed_head);
}

fn membership_hash(
    lineage: &str,
    generation: u64,
    basis: u64,
    previous: &[u8],
    content: &[u8],
    state: &[u8],
    frontier: u64,
) -> Digest {
    let mut bytes = b"atomic/generation-membership/v1\0".to_vec();
    bytes.extend_from_slice(lineage.as_bytes());
    bytes.extend_from_slice(&generation.to_be_bytes());
    bytes.extend_from_slice(&basis.to_be_bytes());
    bytes.extend_from_slice(previous);
    bytes.extend_from_slice(content);
    bytes.extend_from_slice(state);
    bytes.extend_from_slice(&frontier.to_be_bytes());
    sha256(&bytes)
}

#[test]
fn decimal_scale_only_payload_change_cannot_reuse_target_proof() {
    let Some(fixture) = Fixture::new(false) else {
        return;
    };
    let mut restore = PortableBackup::connect(&fixture.target_connection).unwrap();
    restore
        .restore_backup(
            fixture.directory.path(),
            fixture.point.basis_t,
            &fixture.target,
        )
        .unwrap();
    let original_head = fixture.head();
    let mut catalog = Client::connect(&fixture.target_connection, NoTls).unwrap();
    let row = catalog
        .query_one(
            "SELECT d.lineage_id, t.previous_hash, t.content_hash, t.state_hash, \
                    t.eidx_frontier, c.payload \
               FROM atomic_databases d JOIN atomic_heads h USING (database_id) \
               JOIN atomic_generation_transactions t \
                 ON t.database_id=h.database_id AND t.generation=h.log_generation \
                AND t.basis_t=h.basis_t \
               JOIN atomic_transaction_contents c ON c.content_hash=t.content_hash \
              WHERE d.database_id=$1",
            &[&fixture.target],
        )
        .unwrap();
    let lineage: String = row.get(0);
    let previous: Vec<u8> = row.get(1);
    let original_content_hash: Vec<u8> = row.get(2);
    let state: Vec<u8> = row.get(3);
    let frontier: i64 = row.get(4);
    let original_payload: Vec<u8> = row.get(5);
    assert_eq!(
        membership_hash(
            &lineage,
            original_head.0 as u64,
            original_head.1 as u64,
            &previous,
            &original_content_hash,
            &state,
            frontier as u64,
        )
        .as_slice(),
        original_head.2
    );
    assert_eq!(sha256(&original_payload).as_slice(), original_content_hash);

    // Canonical scalar encoding: BigDec tag, i64 scale, u32 coefficient-byte
    // count, signed coefficient. Equal-size 100*10^-2 -> 10*10^-1 changes
    // storage identity without changing Value's numeric PartialEq result.
    let decimal = |scale: i64, coefficient: u8| {
        let mut bytes = vec![0];
        bytes.extend_from_slice(&scale.to_be_bytes());
        bytes.extend_from_slice(&1_u32.to_be_bytes());
        bytes.push(coefficient);
        bytes
    };
    let before = decimal(2, 100);
    let after = decimal(1, 10);
    assert_eq!(
        Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
        Value::BigDec(BigDecimal::from_str("1.0").unwrap())
    );
    let positions = original_payload
        .windows(before.len())
        .enumerate()
        .filter_map(|(at, bytes)| (bytes == before).then_some(at))
        .collect::<Vec<_>>();
    assert_eq!(positions.len(), 1, "mutate exactly one decimal datum");
    let mut changed_payload = original_payload.clone();
    changed_payload[positions[0]..positions[0] + before.len()].copy_from_slice(&after);
    checksum(&mut changed_payload);
    let changed_content_hash = sha256(&changed_payload);
    let changed_tx_hash = membership_hash(
        &lineage,
        original_head.0 as u64,
        original_head.1 as u64,
        &previous,
        &changed_content_hash,
        &state,
        frontier as u64,
    );
    common::with_replica_triggers_disabled(&mut catalog, |catalog| {
        let mut transaction = catalog.transaction()?;
        assert_eq!(transaction.execute(
            "UPDATE atomic_transaction_contents SET content_hash=$2, payload=$3 WHERE content_hash=$1",
            &[&original_content_hash, &&changed_content_hash[..], &changed_payload],
        )?, 1);
        assert_eq!(transaction.execute(
            "UPDATE atomic_generation_transactions SET content_hash=$4, tx_hash=$5 \
             WHERE database_id=$1 AND generation=$2 AND basis_t=$3",
            &[&fixture.target, &original_head.0, &original_head.1, &&changed_content_hash[..], &&changed_tx_hash[..]],
        )?, 1);
        assert_eq!(transaction.execute(
            "UPDATE atomic_generation_requests SET tx_hash=$4 \
             WHERE database_id=$1 AND generation=$2 AND basis_t=$3",
            &[&fixture.target, &original_head.0, &original_head.1, &&changed_tx_hash[..]],
        )?, 1);
        assert_eq!(transaction.execute(
            "UPDATE atomic_heads SET tx_hash=$2 WHERE database_id=$1",
            &[&fixture.target, &&changed_tx_hash[..]],
        )?, 1);
        transaction.commit()
    }).unwrap();

    // Prove the mutation is canonical/hash-valid, not merely a corrupt byte
    // blob that any decoder would reject. Its unchanged state claim is false.
    let changed_copy = private_directory();
    PortableBackup::connect(&fixture.target_connection)
        .unwrap()
        .backup_database(&fixture.target, changed_copy.path())
        .unwrap();
    PortableBackup::verify_backup_presence(changed_copy.path(), fixture.point.basis_t).unwrap();
    assert_eq!(
        PortableBackup::verify_backup(changed_copy.path(), fixture.point.basis_t, true)
            .unwrap_err()
            .code,
        "backup/state-commitment"
    );
    assert_eq!(
        restore
            .restore_backup(
                changed_copy.path(),
                fixture.point.basis_t,
                &unique("reject_false_source"),
            )
            .unwrap_err()
            .code,
        "backup/state-commitment",
        "a hash-valid false source must fail semantic verification before target proof"
    );

    // Nonmatching targets can legitimately be replaced. They must not be
    // mistaken for the already-proved target and returned with altered bytes.
    let repaired = restore
        .restore_backup(
            fixture.directory.path(),
            fixture.point.basis_t,
            &fixture.target,
        )
        .unwrap();
    assert_eq!(fixture.head().0, original_head.0 + 1);
    fixture.assert_values(&repaired);
    let payload: Vec<u8> = catalog
        .query_one(
            "SELECT c.payload FROM atomic_heads h \
             JOIN atomic_generation_transactions t \
               ON t.database_id=h.database_id AND t.generation=h.log_generation \
              AND t.basis_t=h.basis_t \
             JOIN atomic_transaction_contents c ON c.content_hash=t.content_hash \
             WHERE h.database_id=$1",
            &[&fixture.target],
        )
        .unwrap()
        .get(0);
    assert_eq!(payload, original_payload);
    let source_payload: Vec<u8> = Client::connect(&fixture.source_connection, NoTls)
        .unwrap()
        .query_one(
            "SELECT c.payload FROM atomic_heads h \
             JOIN atomic_generation_transactions t \
               ON t.database_id=h.database_id AND t.generation=h.log_generation \
              AND t.basis_t=h.basis_t \
             JOIN atomic_transaction_contents c ON c.content_hash=t.content_hash \
             WHERE h.database_id=$1",
            &[&fixture.source],
        )
        .unwrap()
        .get(0);
    assert_eq!(source_payload, original_payload, "source remains untouched");
}

#[test]
fn completion_head_advance_fails_instead_of_returning_a_stale_verified_value() {
    let Some(fixture) = Fixture::new(false) else {
        return;
    };
    let mut restore = PortableBackup::connect(&fixture.target_connection).unwrap();
    let mut advanced = None;
    let error = restore
        .restore_backup_with_completion_probe(
            fixture.directory.path(),
            fixture.point.basis_t,
            &fixture.target,
            || {
                let writer = common::start_service(&fixture.target_connection, &fixture.target);
                let report = writer
                    .client()
                    .transact(
                        TransactionRequest::new(
                            "advance-after-restore-proof",
                            vec![add("after-proof", MARKER, Value::Long(77))],
                        )
                        .with_tx_instant(2000),
                        WAIT,
                    )
                    .unwrap();
                advanced = Some((report.basis_t, report.tempids["after-proof"]));
                writer.shutdown();
            },
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Fault, "backup/restore-target-changed")
    );
    let (basis, entity) = advanced.expect("completion probe executed");
    assert_eq!(basis, fixture.point.basis_t + 1);
    assert_eq!(fixture.head().1 as u64, basis);
    let reopened = Peer::connect(&fixture.target_connection, &fixture.target, 8).unwrap();
    assert_eq!(
        reopened.database_value().values(entity, MARKER).unwrap(),
        vec![Value::Long(77)]
    );
    assert_eq!(reopened.load_stats().compatibility_materializations, 0);
    PortableBackup::verify_backup(fixture.directory.path(), fixture.point.basis_t, true).unwrap();
}
