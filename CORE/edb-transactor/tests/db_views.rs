use edb_encoding::ValueType;
use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_transactor::SqliteTransactor;
use serde_json::json;

fn tmp_db_path(name: &str) -> String {
    let mut p = std::env::temp_dir();
    p.push(format!("{}_{}.db", name, std::process::id()));
    p.to_string_lossy().to_string()
}

#[test]
fn db_view_constructors_work() {
    let path = tmp_db_path("edb_dbviews");
    let mut txr = SqliteTransactor::open(&path).expect("open");

    // Schema
    let a_id = Attribute { ident: ":user/id".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::Identity, is_component: false, no_history: false, doc: None, aliases: vec![] };
    let a_name = Attribute { ident: ":user/name".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
    txr.install_attribute(&a_id).unwrap();
    txr.install_attribute(&a_name).unwrap();

    // Tx1
    let rep1 = txr.apply_tx(&[json!(["add", "temp:e", ":user/id", "U"]), json!(["add", "temp:e", ":user/name", "A"])].to_vec()).unwrap();
    let t1 = rep1.t.unwrap();
    // Resolve entid via unique_idx
    let e: i64 = txr
        .conn
        .query_row(
            "SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2",
            rusqlite::params![":user/id", "S:U"],
            |r| r.get(0),
        )
        .unwrap();
    // Use db() wrapper to fetch attrs
    let db = txr.db();
    let mut attrs = db.entity_attrs(e).unwrap();
    attrs.sort_by(|a,b| a.0.cmp(&b.0));
    assert_eq!(attrs.len(), 2);

    // Tx2 change name
    txr.apply_tx(&[json!(["add", e, ":user/name", "B"])].to_vec()).unwrap();

    // as_of(t1) sees original name
    let asof = txr.as_of(t1);
    let mut attrs_asof = asof.entity_attrs(e).unwrap();
    attrs_asof.sort_by(|a,b| a.0.cmp(&b.0));
    let name = attrs_asof.iter().find(|(k,_)| k == ":user/name").unwrap().1.clone();
    assert_eq!(name, edb_tx::model::Value::String("A".into()));

    // since(t1) sees changed name
    let since = txr.since(t1);
    let attrs_since = since.entity_attrs(e).unwrap();
    assert_eq!(attrs_since.len(), 1);
    assert_eq!(attrs_since[0].0, ":user/name");
    assert_eq!(attrs_since[0].1, edb_tx::model::Value::String("B".into()));

    // history_db().history_entity has at least two entries
    let hist = txr.history_db().history_entity(e).unwrap();
    assert!(hist.len() >= 2);
}
