use std::collections::HashMap;

use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_tx::{normalize_grammar, DbView, SimpleAllocator, UniquenessResult, Value};

#[derive(Default, Clone)]
struct MockDb {
    attrs: HashMap<String, Attribute>,
    current: HashMap<(i64, String), Value>,
    unique_idx: HashMap<(String, String), i64>,
}

impl DbView for MockDb {
    fn lookup_by_unique(&self, attr_ident: &str, v: &Value) -> Option<i64> {
        let k = value_key(v);
        self.unique_idx.get(&(attr_ident.to_string(), k)).copied()
    }
    fn current_value(&self, e: i64, attr_ident: &str) -> Option<Value> {
        self.current.get(&(e, attr_ident.to_string())).cloned()
    }
    fn uniqueness_check(&self, attr_ident: &str, v: &Value) -> UniquenessResult {
        match self.lookup_by_unique(attr_ident, v) {
            Some(e) => UniquenessResult::Present(e),
            None => UniquenessResult::Absent,
        }
    }
    fn get_attr(&self, ident: &str) -> Option<Attribute> { self.attrs.get(ident).cloned() }
    fn entity_attrs(&self, _e: i64) -> Vec<(String, Value)> { vec![] }
}

fn value_key(v: &Value) -> String {
    match v {
        Value::Long(x) => format!("L:{}", x),
        Value::Double(x) => format!("D:{:?}", x),
        Value::Boolean(b) => format!("B:{}", b),
        Value::String(s) => format!("S:{}", s),
        Value::Keyword(s) => format!("K:{}", s),
        Value::Uuid(u) => format!("U:{}", u),
        Value::Instant(x) => format!("I:{}", x),
        Value::Ref(x) => format!("R:{}", x),
        Value::Bytes(b) => format!("X:{}", b.len()),
        Value::Uint8(x) => format!("U8:{}", x),
        Value::Bigint(s) => format!("BI:{}", s),
        Value::Decimal(s) => format!("BD:{}", s),
    }
}

fn attr(ident: &str, vt: edb_encoding::ValueType, card: AttrCardinality, unique: AttrUnique) -> Attribute {
    Attribute { ident: ident.to_string(), value_type: vt, cardinality: card, unique, is_component: false, no_history: false, doc: None, aliases: vec![] }
}

#[test]
fn cas_success_replaces_value() {
    let mut db = MockDb::default();
    db.attrs.insert(":user/name".into(), attr(":user/name", edb_encoding::ValueType::String, AttrCardinality::One, AttrUnique::None));
    db.current.insert((1, ":user/name".into()), Value::String("Old".into()));

    let ops_json = vec![serde_json::json!(["cas", 1, ":user/name", "Old", "New"])];
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = edb_tx::allocator::TempResolver::new();
    let norm = normalize_grammar(&db, &mut alloc, &mut temps, &ops_json, None);
    let report = edb_tx::normalize_and_validate(&db, &norm.ops, &mut SimpleAllocator::new(2000)).expect("ok");
    assert_eq!(report.primitives.len(), 2);
    assert!(!report.primitives[0].added);
    assert_eq!(report.primitives[0].v, Value::String("Old".into()));
    assert!(report.primitives[1].added);
    assert_eq!(report.primitives[1].v, Value::String("New".into()));
}

#[test]
fn cas_conflict_errors() {
    let mut db = MockDb::default();
    db.attrs.insert(":user/name".into(), attr(":user/name", edb_encoding::ValueType::String, AttrCardinality::One, AttrUnique::None));
    db.current.insert((1, ":user/name".into()), Value::String("Old".into()));

    let ops_json = vec![serde_json::json!(["cas", 1, ":user/name", "Wrong", "New"])];
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = edb_tx::allocator::TempResolver::new();
    let norm = normalize_grammar(&db, &mut alloc, &mut temps, &ops_json, None);
    let err = edb_tx::normalize_and_validate(&db, &norm.ops, &mut SimpleAllocator::new(2000)).unwrap_err();
    let msg = format!("{}", err);
    assert!(msg.contains("cas conflict"));
}
