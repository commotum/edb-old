use atomic_core::persistent_tree::{self, TreeConfig, TreeNode};
use atomic_core::{
    Attribute, BackupPoint, Cardinality, Datom, Digest, EntityRef, IndexOrder, Keyword, Peer,
    PersistentTreeManifest, PortableBackup, PostgresIndexer, PostgresMigrator, PostgresStore,
    TransactionRequest, TxOp, USER_PARTITION, Value, ValueType, View, make_eid, sha256,
};
use postgres::{Client, NoTls};
use std::fs;
use std::path::{Path, PathBuf};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const NAME: u32 = 1000;
const COUNT: u32 = 1001;
const LINK: u32 = 1002;
const TIMEOUT: Duration = Duration::from_secs(30);

fn unique(label: &str) -> String {
    format!(
        "{label}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn hex(hash: &Digest) -> String {
    hash.iter().map(|byte| format!("{byte:02x}")).collect()
}

fn object(directory: &Path, hash: &Digest) -> Vec<u8> {
    let payload = fs::read(directory.join("objects").join(hex(hash))).unwrap();
    assert_eq!(sha256(&payload), *hash);
    payload
}

fn publish(directory: &Path, payload: &[u8]) -> Digest {
    let hash = sha256(payload);
    fs::write(directory.join("objects").join(hex(&hash)), payload).unwrap();
    hash
}

fn checksum(payload: &mut [u8]) {
    let at = payload.len() - 32;
    let hash = sha256(&payload[..at]);
    payload[at..].copy_from_slice(&hash);
}

fn digest_at(bytes: &[u8], at: usize) -> Digest {
    bytes[at..at + 32].try_into().unwrap()
}

/// These test helpers deliberately rewrite the public portable binary
/// envelope, updating every parent checksum/reference. No product decoder or
/// SQL corruption is needed to make an internally valid but false index.
fn root_offsets(bytes: &[u8]) -> (usize, usize) {
    assert_eq!(&bytes[..4], b"ATBK");
    assert_eq!(u16::from_be_bytes(bytes[4..6].try_into().unwrap()), 4);
    let lineage = u32::from_be_bytes(bytes[14..18].try_into().unwrap()) as usize;
    let head = 18 + lineage + 8 + 8 + 32;
    let requests = head + 64;
    let tree_tag = head + 128;
    assert_eq!(bytes[tree_tag], 1);
    (requests, tree_tag + 1)
}

fn request_base_offset(bytes: &[u8]) -> usize {
    assert_eq!(&bytes[..4], b"ATRQ");
    assert_eq!(u16::from_be_bytes(bytes[4..6].try_into().unwrap()), 3);
    let lineage = u32::from_be_bytes(bytes[6..10].try_into().unwrap()) as usize;
    let kind = 10 + lineage + 8 + 8 + 4 * 32;
    assert_eq!(
        bytes[kind], 2,
        "fixture must contain a native exact db-before receipt"
    );
    assert_eq!(bytes[kind + 1], 1);
    kind + 2
}

fn tree_datoms(
    directory: &Path,
    manifest: &PersistentTreeManifest,
    order: IndexOrder,
    history: bool,
) -> Vec<Datom> {
    let root = manifest.tree(order, history).unwrap();
    let mut datoms = Vec::new();
    persistent_tree::validate_tree_streaming(&root.descriptor, |hash| {
        let bytes = object(directory, hash);
        if let TreeNode::Leaf(leaf) = persistent_tree::decode_tree_node(hash, &bytes)? {
            datoms.extend((0..leaf.len()).map(|index| leaf.datom(index).unwrap()));
        }
        Ok(bytes)
    })
    .unwrap();
    datoms.sort_by(|left, right| left.cmp_in(right, order));
    datoms
}

fn forge_tree(
    directory: &Path,
    original: Digest,
    order: IndexOrder,
    history: bool,
    fabricated: bool,
) -> Digest {
    let mut manifest = PersistentTreeManifest::decode(&object(directory, &original)).unwrap();
    let mut datoms = tree_datoms(directory, &manifest, order, history);
    if fabricated {
        datoms.push(Datom {
            entity: make_eid(USER_PARTITION, 999_999).unwrap(),
            attribute: LINK,
            value: Value::Ref(make_eid(USER_PARTITION, 999_998).unwrap()),
            tx: atomic_core::t_to_tx(manifest.basis_t).unwrap(),
            added: true,
        });
    } else {
        let at = datoms
            .iter()
            .position(|datom| datom.attribute == LINK)
            .expect("fixture has a ref fact in every index");
        datoms.remove(at);
    }
    datoms.sort_by(|left, right| left.cmp_in(right, order));
    let build =
        persistent_tree::build_tree(order, history, datoms, &TreeConfig::default()).unwrap();
    persistent_tree::validate_tree(&build.descriptor, &build.nodes).unwrap();
    let root = manifest
        .trees
        .iter_mut()
        .find(|tree| tree.descriptor.order == order && tree.descriptor.history == history)
        .unwrap();
    root.descriptor = build.descriptor;
    root.root_bytes = build.stats.root_bytes;
    for (_, payload) in build.nodes.iter() {
        publish(directory, payload);
    }
    publish(directory, &manifest.encode().unwrap())
}

struct Fixture {
    postgres: String,
    directory: tempfile::TempDir,
    point: BackupPoint,
    root_path: PathBuf,
    root: Vec<u8>,
    entity: u64,
    first_t: u64,
}

impl Fixture {
    fn new() -> Option<Self> {
        let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
            eprintln!("SKIP backup semantic integrity: ATOMIC_POSTGRES_URL is unset");
            return None;
        };
        PostgresMigrator::connect(&postgres)
            .unwrap()
            .migrate()
            .unwrap();
        let source = unique("backup_semantic");
        let mut schema = atomic_core::Schema::new();
        schema
            .install(Attribute::new(
                NAME,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        let mut count = Attribute::new(
            COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        );
        count.indexed = true;
        count.no_history = true;
        schema.install(count).unwrap();
        let mut link = Attribute::new(
            LINK,
            Keyword::new("item", "link"),
            ValueType::Ref,
            Cardinality::One,
        );
        link.indexed = true;
        schema.install(link).unwrap();
        PostgresStore::connect(&postgres)
            .unwrap()
            .create_database(&source, schema)
            .unwrap();
        let writer = common::start_service(&postgres, &source);
        let first = writer
            .client()
            .transact(
                TransactionRequest::new(
                    "seed",
                    vec![
                        TxOp::Add {
                            entity: EntityRef::Temp("item".into()),
                            attribute: NAME,
                            value: Value::String("first".into()).into(),
                        },
                        TxOp::Add {
                            entity: EntityRef::Temp("item".into()),
                            attribute: COUNT,
                            value: Value::Long(10).into(),
                        },
                        TxOp::Add {
                            entity: EntityRef::Temp("item".into()),
                            attribute: LINK,
                            value: atomic_core::TxValue::Entity(EntityRef::Temp("item".into())),
                        },
                    ],
                )
                .with_tx_instant(1000),
                TIMEOUT,
            )
            .unwrap();
        let entity = first.tempids["item"];
        writer.shutdown();
        PostgresIndexer::connect(&postgres, &source)
            .unwrap()
            .consolidate()
            .unwrap();
        let writer = common::start_service(&postgres, &source);
        writer
            .client()
            .transact(
                TransactionRequest::new(
                    "update",
                    vec![TxOp::Add {
                        entity: EntityRef::Id(entity),
                        attribute: COUNT,
                        value: Value::Long(20).into(),
                    }],
                )
                .comparing_basis(first.basis_t)
                .with_tx_instant(2000),
                TIMEOUT,
            )
            .unwrap();
        writer.shutdown();
        PostgresIndexer::connect(&postgres, &source)
            .unwrap()
            .consolidate()
            .unwrap();
        let directory = tempfile::tempdir().unwrap();
        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt;
            fs::set_permissions(directory.path(), fs::Permissions::from_mode(0o700)).unwrap();
        }
        let point = PortableBackup::connect(&postgres)
            .unwrap()
            .backup_database(&source, directory.path())
            .unwrap();
        let root_path = directory.path().join("snapshots").join(format!(
            "{:020}-g{:020}.atbk",
            point.basis_t, point.log_generation
        ));
        let root = fs::read(&root_path).unwrap();
        Some(Self {
            postgres,
            directory,
            point,
            root_path,
            root,
            entity,
            first_t: first.basis_t,
        })
    }

    fn verify(&self) -> Result<atomic_core::BackupVerification, atomic_core::SemanticError> {
        PortableBackup::verify_backup(self.directory.path(), self.point.basis_t, true)
    }

    fn install_main(&self, hash: Digest) {
        let (_, at) = root_offsets(&self.root);
        let mut root = self.root.clone();
        root[at..at + 32].copy_from_slice(&hash);
        checksum(&mut root);
        fs::write(&self.root_path, root).unwrap();
    }

    fn install_archive(&self, original_request: &[u8], hash: Digest) {
        let mut request = original_request.to_vec();
        let at = request_base_offset(&request);
        request[at..at + 32].copy_from_slice(&hash);
        checksum(&mut request);
        let hash = publish(self.directory.path(), &request);
        let mut root = self.root.clone();
        let (at, _) = root_offsets(&root);
        root[at..at + 32].copy_from_slice(&hash);
        checksum(&mut root);
        fs::write(&self.root_path, root).unwrap();
    }
}

#[test]
fn hash_valid_forged_main_indexes_fail_semantic_verification_and_restore() {
    let Some(fixture) = Fixture::new() else {
        return;
    };
    fixture.verify().unwrap();
    let (_, at) = root_offsets(&fixture.root);
    let original = digest_at(&fixture.root, at);
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            for fabricated in [false, true] {
                let forged = forge_tree(
                    fixture.directory.path(),
                    original,
                    order,
                    history,
                    fabricated,
                );
                fixture.install_main(forged);
                // Hashes, graph routing, envelope bindings and leaf presence
                // all pass: only the semantic proof can reject these values.
                PortableBackup::verify_backup_presence(
                    fixture.directory.path(),
                    fixture.point.basis_t,
                )
                .unwrap();
                let error = fixture.verify().unwrap_err();
                assert_eq!(
                    error.code, "backup/tree-semantic-mismatch",
                    "{order:?} history={history} fabricated={fabricated}: {error}"
                );
                assert_eq!(error.details["order"], format!("{order:?}"));
                assert_eq!(error.details["history"], history.to_string());
            }
        }
    }
    let target = unique("reject_forged_main");
    let error = PortableBackup::connect(&fixture.postgres)
        .unwrap()
        .restore_backup(fixture.directory.path(), fixture.point.basis_t, &target)
        .unwrap_err();
    assert_eq!(error.code, "backup/tree-semantic-mismatch");
    let count: i64 = Client::connect(&fixture.postgres, NoTls)
        .unwrap()
        .query_one(
            "SELECT count(*) FROM atomic_databases WHERE database_id=$1",
            &[&target],
        )
        .unwrap()
        .get(0);
    assert_eq!(count, 0);
    fs::write(&fixture.root_path, &fixture.root).unwrap();
    fixture.verify().unwrap();
}

#[test]
fn hash_valid_forged_request_base_archives_fail_at_their_exact_earlier_basis() {
    let Some(fixture) = Fixture::new() else {
        return;
    };
    let (request_at, _) = root_offsets(&fixture.root);
    let request = object(
        fixture.directory.path(),
        &digest_at(&fixture.root, request_at),
    );
    let original = digest_at(&request, request_base_offset(&request));
    let archive =
        PersistentTreeManifest::decode(&object(fixture.directory.path(), &original)).unwrap();
    assert_eq!(archive.basis_t, fixture.first_t);
    assert!(archive.basis_t < fixture.point.basis_t);
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let forged = forge_tree(fixture.directory.path(), original, order, history, true);
            fixture.install_archive(&request, forged);
            PortableBackup::verify_backup_presence(fixture.directory.path(), fixture.point.basis_t)
                .unwrap();
            let error = fixture.verify().unwrap_err();
            assert_eq!(
                error.code, "backup/tree-semantic-mismatch",
                "{order:?} history={history}: {error}"
            );
            assert_eq!(error.details["basis_t"], fixture.first_t.to_string());
            assert_eq!(error.details["order"], format!("{order:?}"));
        }
    }
    let error = PortableBackup::connect(&fixture.postgres)
        .unwrap()
        .restore_backup(
            fixture.directory.path(),
            fixture.point.basis_t,
            &unique("reject_forged_archive"),
        )
        .unwrap_err();
    assert_eq!(error.code, "backup/tree-semantic-mismatch");
    fs::write(&fixture.root_path, &fixture.root).unwrap();
    fixture.verify().unwrap();
}

#[test]
fn pending_avet_accepts_justified_phases_but_cannot_hide_fabricated_data() {
    let Some(fixture) = Fixture::new() else {
        return;
    };
    let (_, at) = root_offsets(&fixture.root);
    let original = digest_at(&fixture.root, at);
    let original_manifest =
        PersistentTreeManifest::decode(&object(fixture.directory.path(), &original)).unwrap();
    let count = tree_datoms(
        fixture.directory.path(),
        &original_manifest,
        IndexOrder::Avet,
        false,
    )
    .iter()
    .filter(|datom| datom.attribute == LINK)
    .count() as u64;
    assert!(count > 0);
    for history in [false, true] {
        let mut pending = original_manifest.clone();
        pending.index_basis_t = pending.basis_t - 1;
        pending.pending_avet = vec![atomic_core::AvetProjectionWork {
            attribute: LINK,
            adding: true,
            history,
            clearing: true,
            offset: 0,
        }];
        let pending_hash = publish(fixture.directory.path(), &pending.encode().unwrap());
        fixture.install_main(pending_hash);
        fixture.verify().unwrap();
        for changed_history in [false, true] {
            let forged = forge_tree(
                fixture.directory.path(),
                pending_hash,
                IndexOrder::Avet,
                changed_history,
                true,
            );
            fixture.install_main(forged);
            let error = fixture.verify().unwrap_err();
            assert_eq!(error.code, "backup/tree-semantic-mismatch");
            assert_eq!(error.details["history"], changed_history.to_string());
        }
    }
    // A completed copy prefix may wait for its next phase; the same physical
    // rows with a shorter claimed offset must fail despite valid hashes.
    let mut copying = original_manifest;
    copying.index_basis_t = copying.basis_t - 1;
    copying.pending_avet = vec![atomic_core::AvetProjectionWork {
        attribute: LINK,
        adding: true,
        history: false,
        clearing: false,
        offset: count,
    }];
    fixture.install_main(publish(
        fixture.directory.path(),
        &copying.encode().unwrap(),
    ));
    fixture.verify().unwrap();
    copying.pending_avet[0].offset = 0;
    fixture.install_main(publish(
        fixture.directory.path(),
        &copying.encode().unwrap(),
    ));
    assert_eq!(
        fixture.verify().unwrap_err().code,
        "backup/tree-semantic-mismatch"
    );
    fs::write(&fixture.root_path, &fixture.root).unwrap();
    fixture.verify().unwrap();
}

#[test]
fn legitimate_no_history_backup_restores_and_preserves_exact_receipt_retry() {
    let Some(fixture) = Fixture::new() else {
        return;
    };
    let started = std::time::Instant::now();
    let verified = fixture.verify().unwrap();
    eprintln!(
        "deep backup semantic proof: basis={}, objects_read={}, replay_current_datoms={}, replay_history_datoms={}, elapsed_ms={}",
        fixture.point.basis_t,
        verified.objects_read,
        verified
            .database
            .datoms(View::Current, IndexOrder::Eavt)
            .len(),
        verified
            .database
            .datoms(View::History, IndexOrder::Eavt)
            .len(),
        started.elapsed().as_millis()
    );
    assert_eq!(
        verified.database.values(fixture.entity, COUNT),
        vec![&Value::Long(20)]
    );
    let (_, at) = root_offsets(&fixture.root);
    let manifest = PersistentTreeManifest::decode(&object(
        fixture.directory.path(),
        &digest_at(&fixture.root, at),
    ))
    .unwrap();
    assert!(
        verified
            .database
            .datoms(View::History, IndexOrder::Eavt)
            .iter()
            .any(|datom| datom.attribute == COUNT && datom.value == Value::Long(10))
    );
    assert!(
        !tree_datoms(fixture.directory.path(), &manifest, IndexOrder::Eavt, true)
            .iter()
            .any(|datom| datom.attribute == COUNT && datom.value == Value::Long(10)),
        "fixture must exercise physically omitted noHistory information"
    );

    let target_schema = unique("backup_semantic_restore");
    Client::connect(&fixture.postgres, NoTls)
        .unwrap()
        .batch_execute(&format!("CREATE SCHEMA {target_schema}"))
        .unwrap();
    let scoped = if fixture.postgres.starts_with("postgres://")
        || fixture.postgres.starts_with("postgresql://")
    {
        format!(
            "{}{}options=-csearch_path%3D{target_schema}",
            fixture.postgres,
            if fixture.postgres.contains('?') {
                '&'
            } else {
                '?'
            }
        )
    } else {
        format!(
            "{} options='-c search_path={target_schema}'",
            fixture.postgres
        )
    };
    PostgresMigrator::connect(&scoped)
        .unwrap()
        .migrate()
        .unwrap();
    let target = unique("restored_semantic");
    let restored = PortableBackup::connect(&scoped)
        .unwrap()
        .restore_backup(fixture.directory.path(), fixture.point.basis_t, &target)
        .unwrap();
    common::assert_same_information(&restored, &verified.database);
    let peer = Peer::connect(&scoped, &target, 4).unwrap();
    assert_eq!(
        peer.db().values(fixture.entity, COUNT).unwrap(),
        vec![Value::Long(20)]
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    let writer = common::start_service(&scoped, &target);
    let receipt = writer
        .client()
        .transact(
            TransactionRequest::new(
                "update",
                vec![TxOp::Add {
                    entity: EntityRef::Id(fixture.entity),
                    attribute: COUNT,
                    value: Value::Long(20).into(),
                }],
            )
            .comparing_basis(fixture.first_t)
            .with_tx_instant(2000),
            TIMEOUT,
        )
        .unwrap();
    assert!(receipt.replayed);
    assert_eq!(
        receipt.db_before.values(fixture.entity, COUNT).unwrap(),
        vec![Value::Long(10)]
    );
    assert_eq!(
        receipt.db_after.values(fixture.entity, COUNT).unwrap(),
        vec![Value::Long(20)]
    );
    writer.shutdown();
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
}
