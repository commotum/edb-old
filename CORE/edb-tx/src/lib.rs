pub mod model;
pub mod traits;
pub mod allocator;
pub mod validate;
pub mod grammar;
pub mod txfn;

pub use allocator::{EntidAllocator, SimpleAllocator};
pub use model::{TempId, TxOp, TxPrimitive, TxReport, Value};
pub use traits::{DbView, UniquenessResult};
pub use validate::{normalize_and_validate, TxError};
pub use grammar::{normalize_grammar, Normalized};
pub use txfn::{TxFnRegistry, TxFunction};

// Back-compat wrapper returning only ops (no tx meta, no tx-fns)
pub fn normalize_grammar_ops(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut allocator::TempResolver,
    ops: &[serde_json::Value],
) -> Vec<TxOp> {
    normalize_grammar(db, alloc, temps, ops, None).ops
}
