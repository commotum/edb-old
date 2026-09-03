use crate::state_commitment::checkpoint_state_hash;
use crate::{
    Database, Digest, DurableTransaction, ErrorCategory, PostgresStore, Program, SemanticError,
    decode_genesis, decode_program, decode_transaction, encode_genesis, encode_transaction, sha256,
    transaction_hash,
};
use postgres::{Client, IsolationLevel, NoTls};
use std::collections::{BTreeMap, BTreeSet};
use std::fs::{self, OpenOptions};
use std::io::Write;
use std::path::{Path, PathBuf};

const MAGIC: &[u8; 4] = b"ATBK";
const VERSION: u16 = 2;
const MAX_ITEMS: usize = 1_000_000;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct BackupPoint {
    pub database_id: String,
    pub basis_t: u64,
    pub manifest_hash: Digest,
    pub objects_written: usize,
    pub objects_reused: usize,
}

#[derive(Clone, Debug)]
pub struct BackupVerification {
    pub point: BackupPoint,
    pub database: Database,
    pub objects_read: usize,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct RequestRow {
    key: String,
    digest: Digest,
    basis: u64,
    tx_hash: Digest,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct ProgramRow {
    hash: Digest,
    kind: i16,
    arity: i16,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct VersionRow {
    name: String,
    version: u64,
    hash: Digest,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct Manifest {
    database_id: String,
    basis: u64,
    genesis_hash: Digest,
    transactions: Vec<Digest>,
    requests: Vec<RequestRow>,
    programs: Vec<ProgramRow>,
    versions: Vec<VersionRow>,
    active: Vec<VersionRow>,
}

pub struct PortableBackup {
    connection: String,
    client: Client,
}

impl PortableBackup {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        let client = Client::connect(connection, NoTls)
            .map_err(|error| crate::postgres::postgres_error("backup/connect", error))?;
        Ok(Self {
            connection: connection.into(),
            client,
        })
    }

    pub fn backup_database(
        &mut self,
        database_id: &str,
        directory: &Path,
    ) -> Result<BackupPoint, SemanticError> {
        prepare_directory(directory, database_id)?;
        let mut transaction = self
            .client
            .build_transaction()
            .isolation_level(IsolationLevel::RepeatableRead)
            .read_only(true)
            .start()
            .map_err(|error| crate::postgres::postgres_error("backup/snapshot", error))?;
        let catalog = transaction
            .query_opt(
                "SELECT genesis, genesis_hash FROM atomic_databases \
                 WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/catalog", error))?
            .ok_or_else(|| not_found(database_id))?;
        let genesis: Vec<u8> = catalog.get(0);
        let genesis_hash = digest(catalog.get(1), "genesis hash")?;
        let decoded_genesis = decode_genesis(&genesis)?;
        if sha256(&genesis) != genesis_hash || encode_genesis(&decoded_genesis)? != genesis {
            return Err(fault(
                "backup/genesis-corrupt",
                "genesis is not canonical or hash-valid",
            ));
        }
        let mut temporal_programs = BTreeSet::new();
        for datom in &decoded_genesis {
            collect_function_hashes(&datom.value, &mut temporal_programs);
        }
        let mut reconstructed = Database::from_genesis(decoded_genesis)?;
        let head = transaction
            .query_one(
                "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/head", error))?;
        let basis = unsigned(head.get(0), "head basis")?;
        let head_hash = digest(head.get(1), "head hash")?;
        let tx_rows = transaction
            .query(
                "SELECT basis_t, tx_hash, payload FROM atomic_transactions \
                 WHERE database_id = $1 AND basis_t <= $2 ORDER BY basis_t",
                &[&database_id, &(basis as i64)],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/transactions", error))?;
        if tx_rows.len() != basis as usize {
            return Err(fault(
                "backup/incomplete-chain",
                "snapshot transaction chain is incomplete",
            ));
        }
        let mut objects = vec![(genesis_hash, genesis)];
        let mut transactions = Vec::with_capacity(tx_rows.len());
        let mut previous = genesis_hash;
        for (offset, row) in tx_rows.into_iter().enumerate() {
            let row_basis = unsigned(row.get(0), "transaction basis")?;
            let hash = digest(row.get(1), "transaction hash")?;
            let payload: Vec<u8> = row.get(2);
            let decoded = decode_transaction(&payload)?;
            for datom in &decoded.tx_data {
                collect_function_hashes(&datom.value, &mut temporal_programs);
            }
            if row_basis != offset as u64 + 1
                || decoded.database_id != database_id
                || decoded.basis_t != row_basis
                || decoded.previous_hash != previous
                || transaction_hash(&payload) != hash
            {
                return Err(fault(
                    "backup/invalid-chain",
                    format!("invalid transaction {row_basis}"),
                ));
            }
            reconstructed = reconstructed.apply_committed(&decoded)?;
            previous = hash;
            transactions.push(hash);
            objects.push((hash, payload));
        }
        if head_hash != previous {
            return Err(fault(
                "backup/head-mismatch",
                "head does not match snapshot chain",
            ));
        }
        if reconstructed.basis_t() != basis {
            return Err(fault(
                "backup/basis-mismatch",
                "semantically reconstructed snapshot does not reach its observed head",
            ));
        }
        let requests = transaction
            .query(
                "SELECT request_key, request_digest, basis_t, tx_hash FROM atomic_requests \
                 WHERE database_id = $1 AND basis_t <= $2 ORDER BY basis_t",
                &[&database_id, &(basis as i64)],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/requests", error))?
            .into_iter()
            .map(|row| {
                Ok(RequestRow {
                    key: row.get(0),
                    digest: digest(row.get(1), "request digest")?,
                    basis: unsigned(row.get(2), "request basis")?,
                    tx_hash: digest(row.get(3), "request transaction hash")?,
                })
            })
            .collect::<Result<Vec<_>, SemanticError>>()?;
        let program_rows = transaction
            .query(
                "SELECT DISTINCT p.program_hash, p.kind, p.arity, p.payload \
                 FROM atomic_program_versions v JOIN atomic_programs p USING (program_hash) \
                 WHERE v.database_id = $1 ORDER BY p.program_hash",
                &[&database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/programs", error))?;
        let mut program_objects = BTreeMap::new();
        for row in program_rows {
            let (program, payload) = validated_program_row(&row)?;
            program_objects.insert(program.hash, (program, payload));
        }
        // Database functions are temporal database information. Preserve
        // every content hash reachable from history, including superseded
        // bindings needed by as-of values; legacy operator aliases are only
        // an additional source, never the authority.
        for hash in temporal_programs {
            if program_objects.contains_key(&hash) {
                continue;
            }
            let row = transaction
                .query_opt(
                    "SELECT program_hash, kind, arity, payload FROM atomic_programs \
                     WHERE program_hash = $1",
                    &[&&hash[..]],
                )
                .map_err(|error| crate::postgres::postgres_error("backup/program", error))?
                .ok_or_else(|| {
                    fault(
                        "backup/missing-program",
                        format!(
                            "temporal :db/fn binding refers to missing program {}",
                            hex(&hash)
                        ),
                    )
                })?;
            let (program, payload) = validated_program_row(&row)?;
            program_objects.insert(hash, (program, payload));
        }
        let mut programs = Vec::with_capacity(program_objects.len());
        for (hash, (program, payload)) in program_objects {
            debug_assert_eq!(hash, program.hash);
            programs.push(program);
            objects.push((hash, payload));
        }
        let versions = read_versions(&mut transaction, database_id, "atomic_program_versions")?;
        let active = read_versions(&mut transaction, database_id, "atomic_active_programs")?;
        transaction
            .commit()
            .map_err(|error| crate::postgres::postgres_error("backup/snapshot-commit", error))?;

        let mut written = 0;
        let mut reused = 0;
        for (hash, bytes) in objects {
            if write_object(directory, hash, &bytes)? {
                written += 1;
            } else {
                reused += 1;
            }
        }
        let manifest = Manifest {
            database_id: database_id.into(),
            basis,
            genesis_hash,
            transactions,
            requests,
            programs,
            versions,
            active,
        };
        let encoded = encode_manifest(&manifest)?;
        let manifest_hash = sha256(&encoded);
        let snapshot_path = snapshots(directory).join(format!("{basis:020}.atbk"));
        write_exact(&snapshot_path, &encoded)?;
        Ok(BackupPoint {
            database_id: database_id.into(),
            basis_t: basis,
            manifest_hash,
            objects_written: written,
            objects_reused: reused,
        })
    }

    pub fn list_backups(directory: &Path) -> Result<Vec<u64>, SemanticError> {
        let mut points = Vec::new();
        for entry in fs::read_dir(snapshots(directory)).map_err(io_error("backup/list"))? {
            let entry = entry.map_err(io_error("backup/list-entry"))?;
            if entry.path().extension().and_then(|value| value.to_str()) == Some("atbk") {
                let bytes = fs::read(entry.path()).map_err(io_error("backup/list-read"))?;
                points.push(decode_manifest(&bytes)?.basis);
            }
        }
        points.sort_unstable();
        points.dedup();
        Ok(points)
    }

    pub fn verify_backup(
        directory: &Path,
        basis: u64,
        deep: bool,
    ) -> Result<BackupVerification, SemanticError> {
        let manifest_bytes = fs::read(snapshots(directory).join(format!("{basis:020}.atbk")))
            .map_err(io_error("backup/manifest-read"))?;
        let manifest_hash = sha256(&manifest_bytes);
        let manifest = decode_manifest(&manifest_bytes)?;
        let claim =
            fs::read_to_string(directory.join("CLAIM")).map_err(io_error("backup/claim-read"))?;
        if claim != manifest.database_id {
            return Err(fault(
                "backup/claim-mismatch",
                "backup claim and snapshot identity differ",
            ));
        }
        let genesis = read_object(directory, manifest.genesis_hash)?;
        let decoded_genesis = decode_genesis(&genesis)?;
        let mut required_programs = BTreeSet::new();
        for datom in &decoded_genesis {
            collect_function_hashes(&datom.value, &mut required_programs);
        }
        let mut database = Database::from_genesis(decoded_genesis)?;
        let mut previous = manifest.genesis_hash;
        let mut objects_read = 1;
        for (offset, hash) in manifest.transactions.iter().enumerate() {
            let payload = read_object(directory, *hash)?;
            objects_read += 1;
            let transaction = decode_transaction(&payload)?;
            for datom in &transaction.tx_data {
                collect_function_hashes(&datom.value, &mut required_programs);
            }
            if transaction.database_id != manifest.database_id
                || transaction.basis_t != offset as u64 + 1
                || transaction.previous_hash != previous
                || transaction_hash(&payload) != *hash
            {
                return Err(fault(
                    "backup/invalid-chain",
                    "backup transaction chain is invalid",
                ));
            }
            previous = *hash;
            database = database.apply_committed(&transaction)?;
        }
        if database.basis_t() != manifest.basis {
            return Err(fault(
                "backup/basis-mismatch",
                "backup reconstruction has wrong basis",
            ));
        }
        for request in &manifest.requests {
            if request.basis == 0
                || manifest.transactions.get(request.basis as usize - 1) != Some(&request.tx_hash)
            {
                return Err(fault(
                    "backup/request-mismatch",
                    "request does not name a backup transaction",
                ));
            }
        }
        let declared_programs: BTreeSet<_> = manifest
            .programs
            .iter()
            .map(|program| program.hash)
            .collect();
        required_programs.extend(manifest.versions.iter().map(|version| version.hash));
        required_programs.extend(manifest.active.iter().map(|version| version.hash));
        if let Some(missing) = required_programs.difference(&declared_programs).next() {
            return Err(fault(
                "backup/missing-program",
                format!("backup manifest omits required program {}", hex(missing)),
            ));
        }
        if deep {
            for program in &manifest.programs {
                let bytes = read_object(directory, program.hash)?;
                objects_read += 1;
                let decoded = decode_program(&bytes)?;
                if program_kind(&decoded) != program.kind
                    || i16::from(decoded.arity) != program.arity
                {
                    return Err(fault(
                        "backup/program-metadata",
                        "program metadata mismatch",
                    ));
                }
            }
        }
        Ok(BackupVerification {
            point: BackupPoint {
                database_id: manifest.database_id,
                basis_t: manifest.basis,
                manifest_hash,
                objects_written: 0,
                objects_reused: 0,
            },
            database,
            objects_read,
        })
    }

    pub fn restore_backup(
        &mut self,
        directory: &Path,
        basis: u64,
        target_database_id: &str,
    ) -> Result<Database, SemanticError> {
        if target_database_id.is_empty() {
            return Err(SemanticError::incorrect(
                "backup/empty-target",
                "restore target cannot be empty",
            ));
        }
        let verification = Self::verify_backup(directory, basis, true)?;
        let manifest_bytes = fs::read(snapshots(directory).join(format!("{basis:020}.atbk")))
            .map_err(io_error("backup/manifest-read"))?;
        let manifest = decode_manifest(&manifest_bytes)?;
        let mut migrator = PostgresStore::connect(&self.connection)?;
        migrator.migrate()?;
        drop(migrator);
        let genesis = read_object(directory, manifest.genesis_hash)?;
        let genesis_hash = sha256(&genesis);
        let mut restored_database = Database::from_genesis(decode_genesis(&genesis)?)?;
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| crate::postgres::postgres_error("backup/restore-begin", error))?;
        transaction
            .batch_execute("SET LOCAL session_replication_role = replica")
            .map_err(|error| crate::postgres::postgres_error("backup/restore-privilege", error))?;
        if transaction
            .query_opt(
                "SELECT 1 FROM atomic_databases WHERE database_id = $1",
                &[&target_database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-target", error))?
            .is_some()
        {
            return Err(SemanticError::new(
                ErrorCategory::Conflict,
                "backup/target-exists",
                "restore target already exists",
            ));
        }
        transaction
            .execute(
                "INSERT INTO atomic_databases (database_id, genesis, genesis_hash) \
                 VALUES ($1, $2, $3)",
                &[&target_database_id, &&genesis[..], &&genesis_hash[..]],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-catalog", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_heads (database_id, basis_t, tx_hash) VALUES ($1, 0, $2)",
                &[&target_database_id, &&genesis_hash[..]],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-head", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_database_generations (database_id) VALUES ($1)",
                &[&target_database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-generation", error))?;
        let mut previous = genesis_hash;
        for (offset, source_hash) in manifest.transactions.iter().enumerate() {
            let source = decode_transaction(&read_object(directory, *source_hash)?)?;
            let restored = DurableTransaction {
                database_id: target_database_id.into(),
                basis_t: source.basis_t,
                previous_hash: previous,
                eidx_frontier: source.eidx_frontier,
                tempids: source.tempids,
                tx_data: source.tx_data,
            };
            let payload = encode_transaction(&restored)?;
            let hash = transaction_hash(&payload);
            restored_database = restored_database.apply_committed(&restored)?;
            let state_hash = checkpoint_state_hash(&restored_database)?;
            let basis_sql = restored.basis_t as i64;
            transaction
                .execute(
                    "INSERT INTO atomic_transactions \
                     (database_id, basis_t, previous_hash, tx_hash, payload, state_hash) \
                     VALUES ($1, $2, $3, $4, $5, $6)",
                    &[
                        &target_database_id,
                        &basis_sql,
                        &&previous[..],
                        &&hash[..],
                        &&payload[..],
                        &&state_hash[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-transaction", error)
                })?;
            let request = manifest
                .requests
                .iter()
                .find(|request| request.basis == restored.basis_t)
                .ok_or_else(|| {
                    fault(
                        "backup/missing-request",
                        format!("basis {} has no request", restored.basis_t),
                    )
                })?;
            transaction
                .execute(
                    "INSERT INTO atomic_requests \
                     (database_id, request_key, request_digest, basis_t, tx_hash) \
                     VALUES ($1, $2, $3, $4, $5)",
                    &[
                        &target_database_id,
                        &request.key,
                        &&request.digest[..],
                        &basis_sql,
                        &&hash[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-request", error)
                })?;
            transaction
                .execute(
                    "UPDATE atomic_heads SET basis_t = $2, tx_hash = $3 \
                     WHERE database_id = $1",
                    &[&target_database_id, &basis_sql, &&hash[..]],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-publish", error)
                })?;
            previous = hash;
            debug_assert_eq!(offset as u64 + 1, restored.basis_t);
        }
        for program in &manifest.programs {
            let payload = read_object(directory, program.hash)?;
            transaction
                .execute(
                    "INSERT INTO atomic_programs (program_hash, kind, arity, payload) \
                     VALUES ($1, $2, $3, $4) ON CONFLICT (program_hash) DO NOTHING",
                    &[
                        &&program.hash[..],
                        &program.kind,
                        &program.arity,
                        &&payload[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-program", error)
                })?;
        }
        for version in &manifest.versions {
            let version_sql = version.version as i64;
            transaction
                .execute(
                    "INSERT INTO atomic_program_versions (database_id, name, version, program_hash) \
                     VALUES ($1, $2, $3, $4)",
                    &[&target_database_id, &version.name, &version_sql, &&version.hash[..]],
                )
                .map_err(|error| crate::postgres::postgres_error("backup/restore-program-version", error))?;
        }
        for active in &manifest.active {
            let version_sql = active.version as i64;
            transaction
                .execute(
                    "INSERT INTO atomic_active_programs (database_id, name, version, program_hash) \
                     VALUES ($1, $2, $3, $4)",
                    &[
                        &target_database_id,
                        &active.name,
                        &version_sql,
                        &&active.hash[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-active-program", error)
                })?;
        }
        transaction
            .batch_execute("SET LOCAL session_replication_role = origin")
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-trigger-mode", error)
            })?;
        transaction
            .commit()
            .map_err(|error| crate::postgres::postgres_error("backup/restore-commit", error))?;
        let mut store = PostgresStore::connect(&self.connection)?;
        let restored = store.recover(target_database_id)?;
        if !restored.same_information_as(&verification.database) {
            return Err(fault(
                "backup/restore-verify",
                "restored database information differs from the verified backup point",
            ));
        }
        Ok(restored)
    }
}

fn read_versions<C: postgres::GenericClient>(
    client: &mut C,
    database_id: &str,
    table: &str,
) -> Result<Vec<VersionRow>, SemanticError> {
    let sql = format!(
        "SELECT name, version, program_hash FROM {table} WHERE database_id = $1 ORDER BY name, version"
    );
    client
        .query(&sql, &[&database_id])
        .map_err(|error| crate::postgres::postgres_error("backup/program-versions", error))?
        .into_iter()
        .map(|row| {
            Ok(VersionRow {
                name: row.get(0),
                version: unsigned(row.get(1), "program version")?,
                hash: digest(row.get(2), "program version hash")?,
            })
        })
        .collect()
}

fn prepare_directory(directory: &Path, database_id: &str) -> Result<(), SemanticError> {
    fs::create_dir_all(objects(directory)).map_err(io_error("backup/create-objects"))?;
    fs::create_dir_all(snapshots(directory)).map_err(io_error("backup/create-snapshots"))?;
    let claim = directory.join("CLAIM");
    if claim.exists() {
        if fs::read_to_string(&claim).map_err(io_error("backup/read-claim"))? != database_id {
            return Err(SemanticError::new(
                ErrorCategory::Conflict,
                "backup/claim-conflict",
                "backup directory is claimed by another database",
            ));
        }
    } else {
        write_exact(&claim, database_id.as_bytes())?;
    }
    Ok(())
}

fn write_object(directory: &Path, hash: Digest, bytes: &[u8]) -> Result<bool, SemanticError> {
    if sha256(bytes) != hash {
        return Err(fault(
            "backup/object-hash",
            "object bytes do not match their name",
        ));
    }
    let path = objects(directory).join(hex(&hash));
    if path.exists() {
        if fs::read(&path).map_err(io_error("backup/read-existing-object"))? != bytes {
            return Err(fault(
                "backup/object-conflict",
                "existing object has different bytes",
            ));
        }
        Ok(false)
    } else {
        write_exact(&path, bytes)?;
        Ok(true)
    }
}

fn write_exact(path: &Path, bytes: &[u8]) -> Result<(), SemanticError> {
    match OpenOptions::new().write(true).create_new(true).open(path) {
        Ok(mut file) => {
            file.write_all(bytes).map_err(io_error("backup/write"))?;
            file.sync_all().map_err(io_error("backup/sync"))
        }
        Err(error) if error.kind() == std::io::ErrorKind::AlreadyExists => {
            if fs::read(path).map_err(io_error("backup/read-existing"))? == bytes {
                Ok(())
            } else {
                Err(fault(
                    "backup/file-conflict",
                    "backup file already exists with different bytes",
                ))
            }
        }
        Err(error) => Err(io_error("backup/create")(error)),
    }
}

fn read_object(directory: &Path, hash: Digest) -> Result<Vec<u8>, SemanticError> {
    let bytes =
        fs::read(objects(directory).join(hex(&hash))).map_err(io_error("backup/object-read"))?;
    if sha256(&bytes) != hash {
        return Err(fault(
            "backup/object-corrupt",
            format!("object {} failed its hash", hex(&hash)),
        ));
    }
    Ok(bytes)
}

fn objects(directory: &Path) -> PathBuf {
    directory.join("objects")
}

fn snapshots(directory: &Path) -> PathBuf {
    directory.join("snapshots")
}

fn encode_manifest(manifest: &Manifest) -> Result<Vec<u8>, SemanticError> {
    let mut body = Vec::new();
    put_string(&mut body, &manifest.database_id)?;
    put_u64(&mut body, manifest.basis);
    body.extend_from_slice(&manifest.genesis_hash);
    put_hashes(&mut body, &manifest.transactions)?;
    put_u32(&mut body, manifest.requests.len())?;
    for request in &manifest.requests {
        put_string(&mut body, &request.key)?;
        body.extend_from_slice(&request.digest);
        put_u64(&mut body, request.basis);
        body.extend_from_slice(&request.tx_hash);
    }
    put_u32(&mut body, manifest.programs.len())?;
    for program in &manifest.programs {
        body.extend_from_slice(&program.hash);
        body.extend_from_slice(&program.kind.to_be_bytes());
        body.extend_from_slice(&program.arity.to_be_bytes());
    }
    put_versions(&mut body, &manifest.versions)?;
    put_versions(&mut body, &manifest.active)?;
    let mut bytes = Vec::new();
    bytes.extend_from_slice(MAGIC);
    bytes.extend_from_slice(&VERSION.to_be_bytes());
    bytes.extend_from_slice(&(body.len() as u64).to_be_bytes());
    bytes.extend_from_slice(&body);
    let checksum = sha256(&bytes);
    bytes.extend_from_slice(&checksum);
    Ok(bytes)
}

fn decode_manifest(bytes: &[u8]) -> Result<Manifest, SemanticError> {
    if bytes.len() < 46 || &bytes[..4] != MAGIC {
        return Err(fault(
            "backup/manifest-header",
            "backup manifest header is invalid",
        ));
    }
    if u16::from_be_bytes([bytes[4], bytes[5]]) != VERSION {
        return Err(SemanticError::new(
            ErrorCategory::Unsupported,
            "backup/manifest-version",
            "unsupported backup manifest version",
        ));
    }
    let len = u64::from_be_bytes(bytes[6..14].try_into().expect("fixed slice")) as usize;
    if bytes.len() != 14 + len + 32 || sha256(&bytes[..14 + len]).as_slice() != &bytes[14 + len..] {
        return Err(fault(
            "backup/manifest-checksum",
            "backup manifest length or checksum is invalid",
        ));
    }
    let mut cursor = Cursor::new(&bytes[14..14 + len]);
    let database_id = cursor.string()?;
    let basis = cursor.u64()?;
    let genesis_hash = cursor.digest()?;
    let transactions = cursor.hashes()?;
    let request_count = cursor.count()?;
    let mut requests = Vec::with_capacity(request_count);
    for _ in 0..request_count {
        requests.push(RequestRow {
            key: cursor.string()?,
            digest: cursor.digest()?,
            basis: cursor.u64()?,
            tx_hash: cursor.digest()?,
        });
    }
    let program_count = cursor.count()?;
    let mut programs = Vec::with_capacity(program_count);
    for _ in 0..program_count {
        programs.push(ProgramRow {
            hash: cursor.digest()?,
            kind: cursor.i16()?,
            arity: cursor.i16()?,
        });
    }
    let versions = cursor.versions()?;
    let active = cursor.versions()?;
    cursor.finish()?;
    let manifest = Manifest {
        database_id,
        basis,
        genesis_hash,
        transactions,
        requests,
        programs,
        versions,
        active,
    };
    if manifest.transactions.len() != basis as usize || encode_manifest(&manifest)? != bytes {
        return Err(fault(
            "backup/noncanonical-manifest",
            "backup manifest is not canonical",
        ));
    }
    Ok(manifest)
}

fn put_versions(output: &mut Vec<u8>, values: &[VersionRow]) -> Result<(), SemanticError> {
    put_u32(output, values.len())?;
    for value in values {
        put_string(output, &value.name)?;
        put_u64(output, value.version);
        output.extend_from_slice(&value.hash);
    }
    Ok(())
}

fn put_hashes(output: &mut Vec<u8>, values: &[Digest]) -> Result<(), SemanticError> {
    put_u32(output, values.len())?;
    for value in values {
        output.extend_from_slice(value);
    }
    Ok(())
}

fn put_string(output: &mut Vec<u8>, value: &str) -> Result<(), SemanticError> {
    put_u32(output, value.len())?;
    output.extend_from_slice(value.as_bytes());
    Ok(())
}

fn put_u32(output: &mut Vec<u8>, value: usize) -> Result<(), SemanticError> {
    let value = u32::try_from(value).map_err(|_| {
        SemanticError::incorrect("backup/too-large", "backup collection is too large")
    })?;
    output.extend_from_slice(&value.to_be_bytes());
    Ok(())
}

fn put_u64(output: &mut Vec<u8>, value: u64) {
    output.extend_from_slice(&value.to_be_bytes());
}

struct Cursor<'a> {
    bytes: &'a [u8],
    at: usize,
}

impl<'a> Cursor<'a> {
    fn new(bytes: &'a [u8]) -> Self {
        Self { bytes, at: 0 }
    }
    fn take(&mut self, len: usize) -> Result<&'a [u8], SemanticError> {
        let end = self
            .at
            .checked_add(len)
            .ok_or_else(|| fault("backup/manifest-length", "manifest length overflow"))?;
        let bytes = self
            .bytes
            .get(self.at..end)
            .ok_or_else(|| fault("backup/manifest-truncated", "manifest is truncated"))?;
        self.at = end;
        Ok(bytes)
    }
    fn u64(&mut self) -> Result<u64, SemanticError> {
        Ok(u64::from_be_bytes(self.take(8)?.try_into().expect("fixed")))
    }
    fn i16(&mut self) -> Result<i16, SemanticError> {
        Ok(i16::from_be_bytes(self.take(2)?.try_into().expect("fixed")))
    }
    fn digest(&mut self) -> Result<Digest, SemanticError> {
        self.take(32)?
            .try_into()
            .map_err(|_| fault("backup/manifest-truncated", "digest truncated"))
    }
    fn count(&mut self) -> Result<usize, SemanticError> {
        let count = u32::from_be_bytes(self.take(4)?.try_into().expect("fixed")) as usize;
        if count > MAX_ITEMS {
            Err(fault(
                "backup/manifest-count",
                "manifest count exceeds limit",
            ))
        } else {
            Ok(count)
        }
    }
    fn string(&mut self) -> Result<String, SemanticError> {
        let len = self.count()?;
        String::from_utf8(self.take(len)?.to_vec())
            .map_err(|_| fault("backup/manifest-utf8", "manifest string is not UTF-8"))
    }
    fn hashes(&mut self) -> Result<Vec<Digest>, SemanticError> {
        let n = self.count()?;
        (0..n).map(|_| self.digest()).collect()
    }
    fn versions(&mut self) -> Result<Vec<VersionRow>, SemanticError> {
        let n = self.count()?;
        (0..n)
            .map(|_| {
                Ok(VersionRow {
                    name: self.string()?,
                    version: self.u64()?,
                    hash: self.digest()?,
                })
            })
            .collect()
    }
    fn finish(&self) -> Result<(), SemanticError> {
        if self.at == self.bytes.len() {
            Ok(())
        } else {
            Err(fault(
                "backup/manifest-trailing",
                "manifest has trailing bytes",
            ))
        }
    }
}

fn program_kind(program: &Program) -> i16 {
    match program.kind {
        crate::ProgramKind::Transaction => 0,
        crate::ProgramKind::AttributePredicate => 1,
        crate::ProgramKind::Query => 2,
        crate::ProgramKind::EntityPredicate => 3,
    }
}

fn validated_program_row(row: &postgres::Row) -> Result<(ProgramRow, Vec<u8>), SemanticError> {
    let hash = digest(row.get(0), "program hash")?;
    let kind: i16 = row.get(1);
    let arity: i16 = row.get(2);
    let payload: Vec<u8> = row.get(3);
    let decoded = decode_program(&payload)?;
    if sha256(&payload) != hash
        || program_kind(&decoded) != kind
        || i16::from(decoded.arity) != arity
    {
        return Err(fault(
            "backup/program-corrupt",
            "program content or metadata is invalid",
        ));
    }
    Ok((ProgramRow { hash, kind, arity }, payload))
}

fn collect_function_hashes(value: &crate::Value, output: &mut BTreeSet<Digest>) {
    match value {
        crate::Value::Function(hash) => {
            output.insert(*hash);
        }
        crate::Value::Tuple(values) => {
            for value in values.iter().flatten() {
                collect_function_hashes(value, output);
            }
        }
        _ => {}
    }
}

fn digest(bytes: Vec<u8>, label: &str) -> Result<Digest, SemanticError> {
    bytes
        .try_into()
        .map_err(|_| fault("backup/digest", format!("{label} is not 32 bytes")))
}

fn unsigned(value: i64, label: &str) -> Result<u64, SemanticError> {
    u64::try_from(value).map_err(|_| fault("backup/negative", format!("{label} is negative")))
}

fn hex(bytes: &Digest) -> String {
    bytes.iter().map(|byte| format!("{byte:02x}")).collect()
}
fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}
fn not_found(database_id: &str) -> SemanticError {
    SemanticError::new(
        ErrorCategory::NotFound,
        "backup/database-not-found",
        format!("database {database_id} does not exist"),
    )
}
fn io_error(code: &'static str) -> impl FnOnce(std::io::Error) -> SemanticError {
    move |error| SemanticError::new(ErrorCategory::Unavailable, code, error.to_string())
}
