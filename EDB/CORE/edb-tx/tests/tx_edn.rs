use std::collections::HashMap;

use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_tx::{normalize_edn, DbView, SimpleAllocator, TxOp, UniquenessResult, Value};

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

fn attr(ident: &str, vt: edb_encoding::ValueType, card: AttrCardinality) -> Attribute {
    Attribute {
        ident: ident.to_string(),
        value_type: vt,
        cardinality: card,
        unique: AttrUnique::None,
        is_component: false,
        no_history: false,
        doc: None,
        aliases: vec![],
    }
}

#[test]
fn edn_add_list_form() {
    let mut db = MockDb::default();
    db.attrs.insert(":user/name".into(), attr(":user/name", edb_encoding::ValueType::String, AttrCardinality::One));
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = edb_tx::allocator::TempResolver::new();
    let norm = normalize_edn(&db, &mut alloc, &mut temps, "[[:db/add 1 :user/name \"Alice\"]]").expect("edn");
    assert_eq!(norm.ops.len(), 1);
    match &norm.ops[0] {
        TxOp::Add { e, a, v } => {
            match e { edb_tx::model::EntityRef::Entid(id) => assert_eq!(*id, 1), _ => panic!() }
            assert_eq!(a, ":user/name");
            assert_eq!(*v, Value::String("Alice".into()));
        }
        _ => panic!(),
    }
}

#[test]
fn edn_map_form_cardinality_many() {
    let mut db = MockDb::default();
    db.attrs.insert(":user/tag".into(), attr(":user/tag", edb_encoding::ValueType::String, AttrCardinality::Many));
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = edb_tx::allocator::TempResolver::new();
    let norm = normalize_edn(&db, &mut alloc, &mut temps, "[{:db/id 1 :user/tag [\"a\" \"b\"]}]").expect("edn");
    assert_eq!(norm.ops.len(), 2);
    for op in norm.ops {
        match op {
            TxOp::Add { e, a, v } => {
                match e { edb_tx::model::EntityRef::Entid(id) => assert_eq!(id, 1), _ => panic!() }
                assert_eq!(a, ":user/tag");
                match v { Value::String(_) => {}, _ => panic!() }
            }
            _ => panic!(),
        }
    }
}
