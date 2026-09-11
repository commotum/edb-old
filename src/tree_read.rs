//! Shared tree transfer diagnostics and preparation admission.
use crate::SemanticError;

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct NodeBlockReadStats {
    pub compressed_hits: u64,
    pub canonical_reads: u64,
    pub corrupt_projections: u64,
    pub canonical_bytes: u64,
    /// Returned physical payload bytes, excluding metadata and wire framing.
    pub physical_read_bytes: u64,
    pub decode_elapsed_nanos: u64,
}

pub(crate) fn validate_index_preparation_parallelism(workers: usize) -> Result<(), SemanticError> {
    if !(1..=8).contains(&workers) {
        return Err(SemanticError::incorrect(
            "index/invalid-preparation-parallelism",
            "index edit preparation parallelism must be between one and eight",
        ));
    }
    Ok(())
}
