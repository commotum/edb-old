//! Temporary, session-owned program roots. Binding a hash in a committed
//! transaction transfers retention to the ordinary immutable database graph.
use crate::storage::snapshot::RootCapture;
use crate::storage::{BlockReadConfig, BlockReader};
use crate::{PostgresConnectionConfig, Program, ProgramHash, SemanticError};

/// Keeps a deployed program and its fixed dependencies alive until they are
/// bound by a committed transaction. Keep this handle through acknowledgement.
/// Dropping an unbound handle permits collection; cleanup uses the same bounded
/// asynchronous session machinery as captured database values.
pub struct ProgramDeployment {
    capture: RootCapture,
}

impl std::fmt::Debug for ProgramDeployment {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_struct("ProgramDeployment")
            .field("hash", &self.hash())
            .finish_non_exhaustive()
    }
}

impl ProgramDeployment {
    pub fn hash(&self) -> ProgramHash {
        self.capture.root_id()
    }

    /// Explicit blocking release. Ordinary Drop never performs PostgreSQL I/O
    /// on the calling thread. Committed bindings need no deployment handle.
    pub fn release(self) -> Result<(), SemanticError> {
        self.capture.release()
    }
}

pub(crate) fn deploy(
    config: &PostgresConnectionConfig,
    program: &Program,
    control: &crate::MaintenanceControl,
) -> Result<ProgramDeployment, SemanticError> {
    control.check()?;
    let bytes = crate::encode_program(program)?;
    let reader = BlockReader::connect(config, BlockReadConfig::default())?;
    Ok(ProgramDeployment {
        capture: reader.stage_pinned_program(&bytes, control)?,
    })
}
