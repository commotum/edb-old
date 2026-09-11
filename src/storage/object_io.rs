//! Immutable canonical object I/O shared by live storage and portable repositories.
use super::{ObjectId, PgBlockStore};
use crate::{ErrorCategory, SemanticError};

/// Read one required immutable object. Implementations authenticate its identity
/// and apply input admission before returning canonical bytes; missing objects
/// are errors, never an empty payload. No reference or publication authority is
/// implied by implementing this interface.
pub trait ObjectReader {
    fn read_object(&mut self, id: ObjectId) -> Result<Vec<u8>, SemanticError>;
}

/// Persist canonical immutable bytes and return their content identity. Mutable
/// references, live build protection and repository publication remain separate.
pub trait ObjectWriter: ObjectReader {
    fn put_object(&mut self, bytes: &[u8]) -> Result<ObjectId, SemanticError>;

    /// A bounded ordered batch, including duplicate inputs. Providers may
    /// coalesce I/O, but must authenticate existing content before success.
    /// Failure may leave unreferenced objects; publication remains separate.
    fn put_objects(&mut self, objects: &[&[u8]]) -> Result<Vec<ObjectId>, SemanticError> {
        validate_write_batch(objects)?;
        objects.iter().map(|bytes| self.put_object(bytes)).collect()
    }
}

pub(crate) const MAX_WRITE_BATCH_OBJECTS: usize = 128;
pub(crate) fn validate_write_batch(objects: &[&[u8]]) -> Result<(), SemanticError> {
    let bytes = objects
        .iter()
        .try_fold(0usize, |total, bytes| total.checked_add(bytes.len()));
    if objects.len() > MAX_WRITE_BATCH_OBJECTS
        || bytes.is_none_or(|bytes| bytes > super::root::MAX_BLOCK_BYTES)
    {
        return Err(SemanticError::incorrect(
            "storage/write-batch-limit",
            "Object batch exceeds 128 objects or 64 MiB of canonical bytes",
        ));
    }
    Ok(())
}

impl ObjectReader for PgBlockStore {
    fn read_object(&mut self, id: ObjectId) -> Result<Vec<u8>, SemanticError> {
        self.get(id)?.ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Fault,
                "storage/missing-object",
                "Required immutable object is absent",
            )
        })
    }
}
impl ObjectWriter for PgBlockStore {
    fn put_object(&mut self, bytes: &[u8]) -> Result<ObjectId, SemanticError> {
        self.put(bytes)
    }
    fn put_objects(&mut self, objects: &[&[u8]]) -> Result<Vec<ObjectId>, SemanticError> {
        self.put_many(objects)
    }
}

impl<F> ObjectReader for F
where
    F: FnMut(ObjectId) -> Result<Vec<u8>, SemanticError>,
{
    fn read_object(&mut self, id: ObjectId) -> Result<Vec<u8>, SemanticError> {
        self(id)
    }
}
