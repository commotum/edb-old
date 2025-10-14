use std::collections::HashMap;

use edb_schema::{AttrCardinality, AttrUnique, Attribute, Catalog};
use edb_tx::{allocator::TempResolver, normalize_and_validate, normalize_grammar_ops, DbView, EntidAllocator, SimpleAllocator, TxOp, UniquenessResult, Value};
use serde_json::json;

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
    fn get_attr(&self, ident: &str) -> Option<Attribute> {
        self.attrs.get(ident).cloned()
    }
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
fn cardinality_one_implicit_retract() {
    let mut db = MockDb::default();
    db.attrs.insert(
        ":user/name".into(),
        attr(":user/name", edb_encoding::ValueType::String, AttrCardinality::One, AttrUnique::None),
    );
    db.current.insert((1, ":user/name".into()), Value::String("Old".into()));

    let ops_json = vec![json!(["add", 1, ":user/name", "New"])];
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = TempResolver::new();
    let ops: Vec<TxOp> = normalize_grammar_ops(&db, &mut alloc, &mut temps, &ops_json);
    let mut alloc2 = SimpleAllocator::new(2000);
    let report = normalize_and_validate(&db, &ops, &mut alloc2).expect("ok");
    // Expect retract Old then add New
    assert_eq!(report.primitives.len(), 2);
    assert_eq!(report.primitives[0].added, false);
    assert_eq!(report.primitives[0].e, 1);
    assert_eq!(report.primitives[0].a, ":user/name");
    assert_eq!(report.primitives[1].added, true);
    assert_eq!(report.primitives[1].v, Value::String("New".into()));
}

#[test]
fn type_mismatch_errors() {
    let mut db = MockDb::default();
    db.attrs.insert(
        ":user/age".into(),
        attr(":user/age", edb_encoding::ValueType::Long, AttrCardinality::One, AttrUnique::None),
    );
    let ops_json = vec![json!(["add", 1, ":user/age", "thirty"])];
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = TempResolver::new();
    let ops: Vec<TxOp> = normalize_grammar_ops(&db, &mut alloc, &mut temps, &ops_json);
    let mut alloc2 = SimpleAllocator::new(2000);
    let err = normalize_and_validate(&db, &ops, &mut alloc2).unwrap_err();
    let msg = format!("{}", err);
    assert!(msg.contains("type mismatch"));
}

#[test]
fn lookup_ref_resolves() {
    let mut db = MockDb::default();
    db.attrs.insert(
        ":user/id".into(),
        attr(":user/id", edb_encoding::ValueType::String, AttrCardinality::One, AttrUnique::Identity),
    );
    db.attrs.insert(
        ":user/friend".into(),
        attr(":user/friend", edb_encoding::ValueType::Ref, AttrCardinality::One, AttrUnique::None),
    );
    // Unique identity mapping
    db.unique_idx.insert((":user/id".into(), "S:42".into()), 7);

    let ops_json = vec![json!(["add", 1, ":user/friend", ["lookup", ":user/id", "42"]])];
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = TempResolver::new();
    let ops: Vec<TxOp> = normalize_grammar_ops(&db, &mut alloc, &mut temps, &ops_json);
    let mut alloc2 = SimpleAllocator::new(2000);
    let report = normalize_and_validate(&db, &ops, &mut alloc2).expect("ok");
    assert_eq!(report.primitives.len(), 1);
    assert_eq!(report.primitives[0].e, 1);
    assert_eq!(report.primitives[0].a, ":user/friend");
    assert_eq!(report.primitives[0].v, Value::Ref(7));
}

#[test]
fn unique_identity_upsert_binds_tempid() {
    let mut db = MockDb::default();
    db.attrs.insert(
        ":user/id".into(),
        attr(":user/id", edb_encoding::ValueType::String, AttrCardinality::One, AttrUnique::Identity),
    );
    // Existing entity with id "U-1" is entid 5
    db.unique_idx.insert((":user/id".into(), "S:U-1".into()), 5);

    // Transact add with tempid for same unique identity; should bind tempid to 5
    let ops_json = vec![json!(["add", "temp:t1", ":user/id", "U-1"])];
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = TempResolver::new();
    let ops: Vec<TxOp> = normalize_grammar_ops(&db, &mut alloc, &mut temps, &ops_json);
    let mut alloc2 = SimpleAllocator::new(2000);
    let report = normalize_and_validate(&db, &ops, &mut alloc2).expect("ok");
    // tempid should resolve to existing entid (5)
    assert!(report.tempids.iter().any(|(t, e)| t.0 == "t1" && *e == 5));
}

#[test]
fn unique_value_conflict_is_error() {
    let mut db = MockDb::default();
    db.attrs.insert(
        ":user/email".into(),
        attr(":user/email", edb_encoding::ValueType::String, AttrCardinality::One, AttrUnique::Value),
    );
    // unique/value mapping
    db.unique_idx.insert((":user/email".into(), "S:foo@example.com".into()), 8);

    // Attempt to assert same value for a different entity
    let ops_json = vec![json!(["add", 9, ":user/email", "foo@example.com"])];
    let mut alloc = SimpleAllocator::new(1000);
    let mut temps = TempResolver::new();
    let ops: Vec<TxOp> = normalize_grammar_ops(&db, &mut alloc, &mut temps, &ops_json);
    let mut alloc2 = SimpleAllocator::new(2000);
    let err = normalize_and_validate(&db, &ops, &mut alloc2).unwrap_err();
    let msg = format!("{}", err);
    assert!(msg.contains("unique value conflict"));
}
