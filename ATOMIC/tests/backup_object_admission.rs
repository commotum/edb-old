//! Bound file admission independently of cache size, through the real reader.
#![cfg(target_os = "linux")]
mod common;
use atomic_core::storage::root::DatabaseRoot;
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::fs;
use std::process::Command;
use std::time::Instant;

const CHILD: &str = "ATOMIC_BACKUP_ADMISSION_CHILD";
const TEST: &str = "offline_large_values_work_and_corrupt_sparse_files_do_not_inflate_memory";
const LARGE_VALUE_BYTES: usize = 2 * 1024 * 1024;

fn peak_rss_kib() -> i64 {
    let mut usage = std::mem::MaybeUninit::<libc::rusage>::zeroed();
    // Only this process's resource accounting is read.
    assert_eq!(
        unsafe { libc::getrusage(libc::RUSAGE_SELF, usage.as_mut_ptr()) },
        0
    );
    unsafe { usage.assume_init().ru_maxrss }
}

fn child_rejects_oversized_object(repository: &std::ffi::OsStr) {
    // This isolated process cannot pressure the host if allocation regresses.
    let cap = libc::rlimit {
        rlim_cur: 384 * 1024 * 1024,
        rlim_max: 384 * 1024 * 1024,
    };
    let core = libc::rlimit {
        rlim_cur: 0,
        rlim_max: 0,
    };
    assert_eq!(unsafe { libc::setrlimit(libc::RLIMIT_AS, &cap) }, 0);
    assert_eq!(unsafe { libc::setrlimit(libc::RLIMIT_CORE, &core) }, 0);
    let before = peak_rss_kib();
    let error = BackupConnection::open_configured(
        std::path::Path::new(repository),
        None,
        BackupReadConfig {
            cache_entries: 0,
            cache_bytes: 0,
        },
    )
    .err()
    .expect("corrupt sparse tree must not open");
    assert_eq!(error.code, "backup/file-size");
    let growth = peak_rss_kib() - before;
    assert!(
        growth < 16 * 1024,
        "96 MiB corrupt object inflated peak RSS by {growth} KiB"
    );
    eprintln!("96 MiB sparse corruption rejected: peak RSS growth {growth} KiB, cache=0");
}

#[test]
fn offline_large_values_work_and_corrupt_sparse_files_do_not_inflate_memory() {
    if let Some(repository) = std::env::var_os(CHILD) {
        child_rejects_oversized_object(&repository);
        return;
    }
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP backup admission: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let start = Instant::now();
    let fixture = common::PostgresFixture::new(&url, "backup_admission");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "text"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let created = BlockDatabase::create(&config, "admission", schema).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let root_id: Digest = store
        .read_ref(&created.reference_key())
        .unwrap()
        .unwrap()
        .value
        .unwrap()
        .try_into()
        .unwrap();
    let initial = DatabaseRoot::decode(&root_id, &store.get(root_id).unwrap().unwrap()).unwrap();
    let root_hash = initial.metadata.unwrap();
    let service = common::start_service(&fixture.connection, "admission");
    let temporary = tempfile::tempdir().unwrap();
    let small_repository = temporary.path().join("small");
    let large_repository = temporary.path().join("large");
    PortableBackup::connect(&fixture.connection)
        .unwrap()
        .backup_database("admission", &small_repository)
        .unwrap();
    let report = common::transact(
        &service,
        "large",
        initial.basis,
        &[TxOp::Add {
            entity: EntityRef::Temp("large".into()),
            attribute: 1000,
            value: Value::String("x".repeat(LARGE_VALUE_BYTES)).into(),
        }],
        1000,
    );
    service.shutdown();
    PortableBackup::connect(&fixture.connection)
        .unwrap()
        .backup_database("admission", &large_repository)
        .unwrap();
    let entity = report.tempids["large"];
    drop((report, store, created, fixture));

    // Valid objects much larger than this cache budget must remain readable,
    // including the canonical log envelope as well as native leaves.
    let offline = BackupConnection::open_configured(
        &large_repository,
        None,
        BackupReadConfig {
            cache_entries: 1,
            cache_bytes: 1024,
        },
    )
    .unwrap();
    assert_eq!(
        offline.db().values(entity, 1000).unwrap(),
        vec![Value::String("x".repeat(LARGE_VALUE_BYTES))]
    );
    let transactions = offline
        .log()
        .tx_range(Some(TimePoint::T(2)), None)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert!(transactions.iter().flat_map(|tx| &tx.data).any(|datom|
        datom.entity == entity && matches!(&datom.value, Value::String(value) if value.len() == LARGE_VALUE_BYTES)));
    assert!(offline.cache_stats().current_bytes <= 1024);
    drop(offline);

    // Corrupt one exact selected metadata object; admission must fail before
    // allocating its sparse physical payload, independently of cache limits.
    let root = small_repository.join("objects").join(
        root_hash
            .iter()
            .map(|byte| format!("{byte:02x}"))
            .collect::<String>(),
    );
    fs::OpenOptions::new()
        .write(true)
        .open(root)
        .unwrap()
        .set_len(96 * 1024 * 1024)
        .unwrap();
    let output = Command::new(std::env::current_exe().unwrap())
        .args(["--exact", TEST, "--nocapture", "--test-threads=1"])
        .env(CHILD, &small_repository)
        .env_remove("ATOMIC_POSTGRES_URL")
        .env_remove("ATOMIC_POSTGRES_TRANSPORT")
        .output()
        .unwrap();
    assert!(
        output.status.success(),
        "{}\n{}",
        String::from_utf8_lossy(&output.stdout),
        String::from_utf8_lossy(&output.stderr)
    );
    eprintln!("{}", String::from_utf8_lossy(&output.stderr));
    eprintln!(
        "create/transact/backup + source-deleted large read + sparse corruption: {:?}",
        start.elapsed()
    );
}
