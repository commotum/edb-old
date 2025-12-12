use std::collections::HashMap;

use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_tx::{normalize_grammar, DbView, SimpleAllocator, TxFnRegistry, TxFunction, UniquenessResult, Value};

#[derive(Default, Clone)]
struct MockDb {
    attrs: HashMap<String, Attribute>,
}

impl DbView for MockDb {
    fn lookup_by_unique(&self, _attr_ident: &str, _v: &Value) -> Option<i64> { None }
    fn current_value(&self, _e: i64, _attr_ident: &str) -> Option<Value> { None }
    fn uniqueness_check(&self, _attr_ident: &str, _v: &Value) -> UniquenessResult { UniquenessResult::Absent }
    fn get_attr(&self, ident: &str) -> Option<Attribute> { self.attrs.get(ident).cloned() }
    fn entity_attrs(&self, _e: i64) -> Vec<(String, Value)> { vec![] }
}

fn attr(ident: &str, vt: edb_encoding::ValueType, card: AttrCardinality, unique: AttrUnique) -> Attribute {
    Attribute { ident: ident.to_string(), value_type: vt, cardinality: card, unique, is_component: false, no_history: false, doc: None, aliases: vec![] }
}

#[test]
fn map_form_normalizes_to_adds() {
    let mut db = MockDb::default();
    db.attrs.insert(":user/name".into(), attr(":user/name", edb_encoding::ValueType::String, AttrCardinality::One, AttrUnique::None));
    let op = serde_json::json!({"db/id": 1, ":user/name": "Alice"});
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = edb_tx::allocator::TempResolver::new();
    let norm = normalize_grammar(&db, &mut alloc, &mut temps, &[op], None);
    assert_eq!(norm.ops.len(), 1);
    if let edb_tx::TxOp::Add { e, a, v } = &norm.ops[0] {
        match e { edb_tx::model::EntityRef::Entid(id) => assert_eq!(*id, 1), _ => panic!() }
        assert_eq!(a, ":user/name");
        assert_eq!(*v, Value::String("Alice".into()));
    } else { panic!() }
}

struct AddNameFn;
impl TxFunction for AddNameFn {
    fn apply(&self, _db: &dyn edb_tx::DbView, _args: &[Value]) -> Result<Vec<edb_tx::TxOp>, edb_tx::validate::TxError> {
        Ok(vec![edb_tx::TxOp::Add { e: edb_tx::model::EntityRef::Entid(1), a: ":user/name".into(), v: Value::String("FromFn".into()) }])
    }
}

#[test]
fn tx_fn_expands_to_ops() {
    let mut db = MockDb::default();
    db.attrs.insert(":user/name".into(), attr(":user/name", edb_encoding::ValueType::String, AttrCardinality::One, AttrUnique::None));
    let ops = vec![serde_json::json!(["tx-fn", ":demo/add"])];
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = edb_tx::allocator::TempResolver::new();
    let mut reg: TxFnRegistry = TxFnRegistry::new();
    reg.insert(":demo/add".into(), Box::new(AddNameFn));
    let norm = normalize_grammar(&db, &mut alloc, &mut temps, &ops, Some(&reg));
    assert_eq!(norm.ops.len(), 1);
    if let edb_tx::TxOp::Add { e, a, v } = &norm.ops[0] {
        match e { edb_tx::model::EntityRef::Entid(id) => assert_eq!(*id, 1), _ => panic!() }
        assert_eq!(a, ":user/name");
        assert_eq!(*v, Value::String("FromFn".into()));
    } else { panic!() }
}

