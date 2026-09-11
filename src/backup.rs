//! Differential current-format backup of Rust-owned immutable objects.
//! Portable repositories are file sinks/readers, not another live engine.
use crate::storage::root::{Block, DatabaseRoot, DatabaseValueRoot};
use crate::storage::{
    BlockDatabase, BlockReadConfig, BlockReader, IndexDescriptor, ObjectId, PgBlockStore,
    SnapshotMetadata,
};
use crate::storage::{ObjectReader, ObjectWriter};
use crate::{Database, Digest, MaintenanceControl, PostgresConnectionConfig, SemanticError};
use std::collections::BTreeSet;
use std::path::{Path, PathBuf};
#[path = "backup_restore.rs"]
mod backup_restore;
#[path = "backup_repository.rs"]
pub(crate) mod repository;
#[path = "backup_verify.rs"]
mod verification;
use repository::fault;
pub(crate) use verification::{verify_read_point, walk_repository};
const POINT_KIND: u16 = 60;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct BackupPoint {
    pub lineage_id: String,
    pub log_generation: u64,
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
/// The durable result of activating an immutable backup point.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct RestoreResult {
    pub point: BackupPoint,
    pub target: String,
    pub activated_root: Digest,
}
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
#[doc(hidden)]
pub enum BackupFault {
    #[default]
    None,
    AfterPublicationCaptured,
    AfterFirstObjectStaged,
    AfterObjects,
    AfterManifestStaged,
    AfterManifestPublished,
}
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
#[doc(hidden)]
pub enum RestoreFault {
    #[default]
    None,
    AfterFirstContentInserted,
    BeforeCommit,
    AfterCommitBeforeResponse,
    AfterTreePublication,
}
pub struct PortableBackup {
    pub(crate) connection: PostgresConnectionConfig,
    pub(crate) maintenance: MaintenanceControl,
}
#[derive(Clone, Debug)]
pub(crate) struct ReadPoint {
    pub point: BackupPoint,
    pub publication: ObjectId,
    pub value: DatabaseValueRoot,
    pub open_object_reads: u64,
    pub open_object_bytes: u64,
}
impl PortableBackup {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::parse(connection)?)
    }
    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        drop(PgBlockStore::connect(connection)?);
        Ok(Self {
            connection: connection.clone(),
            maintenance: MaintenanceControl::default(),
        })
    }
    pub fn with_maintenance_control(mut self, control: MaintenanceControl) -> Self {
        self.maintenance = control;
        self
    }
    pub fn backup_database(
        &mut self,
        name: &str,
        directory: &Path,
    ) -> Result<BackupPoint, SemanticError> {
        self.backup_database_with_fault(name, directory, BackupFault::None)
    }
    #[doc(hidden)]
    pub fn backup_database_with_fault(
        &mut self,
        name: &str,
        directory: &Path,
        fault: BackupFault,
    ) -> Result<BackupPoint, SemanticError> {
        self.capture(name, directory, fault, || {})
    }
    #[doc(hidden)]
    pub fn backup_database_with_capture_probe<F: FnOnce()>(
        &mut self,
        name: &str,
        directory: &Path,
        probe: F,
    ) -> Result<BackupPoint, SemanticError> {
        self.capture(name, directory, BackupFault::None, probe)
    }
    fn capture<F: FnOnce()>(
        &mut self,
        name: &str,
        directory: &Path,
        fault_at: BackupFault,
        probe: F,
    ) -> Result<BackupPoint, SemanticError> {
        self.maintenance.check()?;
        let directory = repository::anchor(directory)?;
        repository::admit(&directory, true)?;
        let database = BlockDatabase::resolve(&self.connection, name)?;
        let reader = BlockReader::connect(&self.connection, BlockReadConfig::default())?;
        let capture = reader.capture_reference(&database.reference_key())?;
        probe();
        inject(
            fault_at,
            BackupFault::AfterPublicationCaptured,
            "backup/after-publication-captured",
        )?;
        let mut source = PgBlockStore::connect(&self.connection)?;
        let publication_id = capture.root_id();
        let publication =
            DatabaseRoot::decode(&publication_id, &source.read_object(publication_id)?)?;
        let metadata_id = publication
            .metadata
            .ok_or_else(|| fault("backup/metadata", "Publication lacks metadata"))?;
        let metadata = SnapshotMetadata::decode(&metadata_id, &source.read_object(metadata_id)?)?;
        if metadata.identity != publication.identity || metadata.basis != publication.basis {
            return Err(fault(
                "backup/coordinate",
                "Publication and metadata disagree",
            ));
        }
        repository::claim(&directory, publication.identity)?;
        if repository::point_path(&directory, metadata.generation, publication.basis)
            .try_exists()
            .map_err(repository::io)?
        {
            let prior = load_selected(&directory, Some(metadata.generation), publication.basis)?;
            same_information(&publication, &load_publication(&directory, &prior)?)?;
            let reused = walk_repository(
                &directory,
                &[prior.point.manifest_hash],
                &self.maintenance,
                false,
            )?;
            return Ok(BackupPoint {
                objects_reused: reused,
                ..prior.point
            });
        }
        let mut copy = RepositoryCopy {
            directory: directory.clone(),
            source,
            written: 0,
            reused: 0,
        };
        let mut seen = BTreeSet::new();
        let mut pending = vec![publication_id];
        while let Some(id) = pending.pop() {
            self.maintenance.check()?;
            if !seen.insert(id) {
                continue;
            }
            let bytes = copy.read_object(id)?;
            pending.extend(crate::storage::ownership::object_children(
                &mut copy, id, &bytes,
            )?);
            copy.put_object(&bytes)?;
            if seen.len() == 1 {
                inject(
                    fault_at,
                    BackupFault::AfterFirstObjectStaged,
                    "backup/after-first-object-staged",
                )?;
            }
            if seen.len() % 128 == 0 {
                self.maintenance.after_batch()?;
            }
        }
        self.maintenance.after_batch()?;
        inject(fault_at, BackupFault::AfterObjects, "backup/after-objects")?;
        let mut payload = publication.identity.to_vec();
        payload.extend_from_slice(&metadata.generation.to_be_bytes());
        payload.extend_from_slice(&publication.basis.to_be_bytes());
        let manifest = Block {
            kind: POINT_KIND,
            links: vec![publication_id],
            payload,
        }
        .encode()?;
        let manifest_hash = copy.put_object(&manifest)?;
        self.maintenance.after_batch()?;
        inject(
            fault_at,
            BackupFault::AfterManifestStaged,
            "backup/after-manifest-staged",
        )?;
        let point = if repository::publish_point(
            &directory,
            metadata.generation,
            publication.basis,
            &manifest,
        )? {
            BackupPoint {
                lineage_id: crate::storage::engine::identity_string(publication.identity),
                log_generation: metadata.generation,
                basis_t: publication.basis,
                manifest_hash,
                objects_written: copy.written,
                objects_reused: copy.reused,
            }
        } else {
            let prior = load_selected(&directory, Some(metadata.generation), publication.basis)?;
            same_information(&publication, &load_publication(&directory, &prior)?)?;
            BackupPoint {
                objects_written: copy.written,
                objects_reused: copy.reused,
                ..prior.point
            }
        };
        inject(
            fault_at,
            BackupFault::AfterManifestPublished,
            "backup/after-manifest-published",
        )?;
        Ok(point)
    }
    pub fn list_backup_points(directory: &Path) -> Result<Vec<BackupPoint>, SemanticError> {
        let directory = repository::anchor(directory)?;
        repository::admit(&directory, false)?;
        let mut result = Vec::new();
        for entry in std::fs::read_dir(directory.join("points")).map_err(repository::io)? {
            let entry = entry.map_err(repository::io)?;
            let name = entry.file_name();
            let name = name
                .to_str()
                .ok_or_else(|| fault("backup/point-name", "Point name is not UTF8"))?;
            if name.starts_with(".atomic-stage-") {
                continue;
            }
            let (generation, basis) = parse_point_name(name)?;
            result.push(point_header(&directory, generation, basis)?.0);
        }
        result.sort_by_key(|point| (point.basis_t, point.log_generation));
        Ok(result)
    }
    pub fn list_backups(directory: &Path) -> Result<Vec<u64>, SemanticError> {
        let mut result: Vec<_> = Self::list_backup_points(directory)?
            .into_iter()
            .map(|p| p.basis_t)
            .collect();
        result.dedup();
        Ok(result)
    }
    pub fn verify_backup_presence(
        directory: &Path,
        basis: u64,
    ) -> Result<BackupPoint, SemanticError> {
        let point = load_selected(directory, None, basis)?;
        walk_repository(
            directory,
            &[point.point.manifest_hash],
            &MaintenanceControl::default(),
            false,
        )?;
        Ok(point.point)
    }
    pub fn verify_backup_point_presence(
        directory: &Path,
        basis: u64,
        generation: u64,
    ) -> Result<BackupPoint, SemanticError> {
        let point = load_selected(directory, Some(generation), basis)?;
        walk_repository(
            directory,
            &[point.point.manifest_hash],
            &MaintenanceControl::default(),
            false,
        )?;
        Ok(point.point)
    }
    pub fn verify_backup(
        directory: &Path,
        basis: u64,
        deep: bool,
    ) -> Result<BackupVerification, SemanticError> {
        verify_read_point(
            directory,
            load_selected(directory, None, basis)?,
            deep,
            &MaintenanceControl::default(),
        )
    }
    pub fn verify_backup_point(
        directory: &Path,
        basis: u64,
        generation: u64,
        deep: bool,
    ) -> Result<BackupVerification, SemanticError> {
        verify_read_point(
            directory,
            load_selected(directory, Some(generation), basis)?,
            deep,
            &MaintenanceControl::default(),
        )
    }
}

struct RepositoryCopy {
    directory: PathBuf,
    source: PgBlockStore,
    written: usize,
    reused: usize,
}
impl ObjectReader for RepositoryCopy {
    fn read_object(&mut self, id: ObjectId) -> Result<Vec<u8>, SemanticError> {
        if repository::contains(&self.directory, id)? {
            read_object(&self.directory, id)
        } else {
            self.source.read_object(id)
        }
    }
}
impl ObjectWriter for RepositoryCopy {
    fn put_object(&mut self, bytes: &[u8]) -> Result<ObjectId, SemanticError> {
        let (id, written) = repository::put_object(&self.directory, bytes)?;
        if written {
            self.written += 1;
        } else {
            self.reused += 1;
        }
        Ok(id)
    }
}
pub(crate) fn read_object(directory: &Path, id: Digest) -> Result<Vec<u8>, SemanticError> {
    repository::read_object(directory, id)
}
pub(crate) fn load_publication(
    directory: &Path,
    point: &ReadPoint,
) -> Result<DatabaseRoot, SemanticError> {
    DatabaseRoot::decode(
        &point.publication,
        &read_object(directory, point.publication)?,
    )
}
pub(crate) fn open_read_point(
    directory: &Path,
    selection: Option<&BackupPoint>,
) -> Result<ReadPoint, SemanticError> {
    let expected;
    let point = match selection {
        Some(point) => point,
        None => {
            expected = PortableBackup::list_backup_points(directory)?
                .pop()
                .ok_or_else(|| fault("backup/no-points", "No completed backup point"))?;
            &expected
        }
    };
    let actual = load_selected(directory, Some(point.log_generation), point.basis_t)?;
    if actual.point.manifest_hash != point.manifest_hash
        || actual.point.lineage_id != point.lineage_id
    {
        return Err(fault(
            "backup/selected-point",
            "Selected point differs from the repository",
        ));
    }
    Ok(actual)
}
pub(crate) fn load_selected(
    directory: &Path,
    generation: Option<u64>,
    basis: u64,
) -> Result<ReadPoint, SemanticError> {
    repository::admit(directory, false)?;
    let generation = match generation {
        Some(generation) => generation,
        None => PortableBackup::list_backup_points(directory)?
            .into_iter()
            .filter(|point| point.basis_t == basis)
            .map(|point| point.log_generation)
            .max()
            .ok_or_else(|| fault("backup/no-point", "Requested backup basis is absent"))?,
    };
    let (point, links, identity) = point_header(directory, generation, basis)?;
    let mut open_object_reads = 0u64;
    let mut open_object_bytes = 0u64;
    let mut read = |id| {
        let bytes = read_object(directory, id)?;
        open_object_reads += 1;
        open_object_bytes += bytes.len() as u64;
        Ok::<_, SemanticError>(bytes)
    };
    let publication = DatabaseRoot::decode(&links[0], &read(links[0])?)?;
    let value = DatabaseValueRoot::from(&publication);
    if publication.identity != identity || publication.basis != basis {
        return Err(fault(
            "backup/manifest-coordinate",
            "Backup roots disagree on committed coordinates",
        ));
    }
    let metadata_id = value
        .metadata
        .ok_or_else(|| fault("backup/metadata", "Value lacks metadata"))?;
    let metadata = SnapshotMetadata::decode(&metadata_id, &read(metadata_id)?)?;
    let index_id = value
        .indexes
        .ok_or_else(|| fault("backup/read-index", "Value lacks indexes"))?;
    let indexes = IndexDescriptor::decode(&index_id, &read(index_id)?)?;
    if metadata.identity != identity
        || metadata.basis != basis
        || metadata.generation != generation
        || indexes.identity != identity
        || indexes.basis > basis
        || indexes.generation != generation
    {
        return Err(fault(
            "backup/read-coordinate",
            "Backup index and metadata disagree with the captured publication",
        ));
    }
    Ok(ReadPoint {
        point,
        publication: links[0],
        value,
        open_object_reads,
        open_object_bytes,
    })
}

fn point_header(
    directory: &Path,
    generation: u64,
    basis: u64,
) -> Result<(BackupPoint, [ObjectId; 1], [u8; 16]), SemanticError> {
    let bytes =
        repository::read_bounded(&repository::point_path(directory, generation, basis), 4096)?;
    let manifest_hash = crate::sha256(&bytes);
    let block = Block::decode(&manifest_hash, &bytes)?;
    if block.kind != POINT_KIND || block.links.len() != 1 || block.payload.len() != 32 {
        return Err(fault(
            "backup/manifest-format",
            "Expected a current single-root backup point",
        ));
    }
    let identity: [u8; 16] = block.payload[..16].try_into().unwrap();
    if u64::from_be_bytes(block.payload[16..24].try_into().unwrap()) != generation
        || u64::from_be_bytes(block.payload[24..32].try_into().unwrap()) != basis
    {
        return Err(fault(
            "backup/manifest-coordinate",
            "Point filename and content disagree",
        ));
    }
    repository::verify_claim(directory, identity)?;
    if identity == [0; 16] {
        return Err(fault("backup/lineage", "Backup lineage is empty"));
    }
    Ok((
        BackupPoint {
            lineage_id: crate::storage::engine::identity_string(identity),
            log_generation: generation,
            basis_t: basis,
            manifest_hash,
            objects_written: 0,
            objects_reused: 0,
        },
        [block.links[0]],
        identity,
    ))
}
fn same_information(left: &DatabaseRoot, right: &DatabaseRoot) -> Result<(), SemanticError> {
    if left.identity != right.identity
        || left.basis != right.basis
        || left.log != right.log
        || left.metadata != right.metadata
        || left.receipts != right.receipts
    {
        Err(fault(
            "backup/point-conflict",
            "Existing point has different committed information",
        ))
    } else {
        Ok(())
    }
}
fn parse_point_name(name: &str) -> Result<(u64, u64), SemanticError> {
    let bad = || fault("backup/point-name", "Invalid current-format point filename");
    let bytes = name.as_bytes();
    if bytes.len() != 38
        || bytes[16] != b'-'
        || !bytes.ends_with(b".root")
        || !bytes[..16]
            .iter()
            .chain(&bytes[17..33])
            .all(|b| b.is_ascii_digit() || (b'a'..=b'f').contains(b))
    {
        return Err(bad());
    }
    Ok((
        u64::from_str_radix(&name[..16], 16).map_err(|_| bad())?,
        u64::from_str_radix(&name[17..33], 16).map_err(|_| bad())?,
    ))
}
fn inject(
    actual: BackupFault,
    expected: BackupFault,
    code: &'static str,
) -> Result<(), SemanticError> {
    if actual != expected {
        return Ok(());
    }
    let category = if actual == BackupFault::AfterManifestPublished {
        crate::ErrorCategory::UnknownOutcome
    } else {
        crate::ErrorCategory::Fault
    };
    Err(SemanticError::new(
        category,
        code,
        "Injected backup interruption",
    ))
}
