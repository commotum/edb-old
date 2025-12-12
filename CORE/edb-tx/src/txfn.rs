use crate::model::TxOp;
use crate::traits::DbView;
use crate::validate::TxError;
use crate::model::Value;

pub trait TxFunction {
    fn apply(&self, db: &dyn DbView, args: &[Value]) -> Result<Vec<TxOp>, TxError>;
}

pub type TxFnRegistry = std::collections::HashMap<String, Box<dyn TxFunction + Send + Sync>>;

