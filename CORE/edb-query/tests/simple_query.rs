use edb_encoding::ValueType;
use edb_query::execute_simple_edn;
use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_transactor::SqliteTransactor;

fn tmp_db_path(name: &str) -> String {
    let mut p = std::env::temp_dir();
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_nanos();
    p.push(format!("{}_{}_{}.db", name, std::process::id(), now));
    p.to_string_lossy().to_string()
}

#[test]
fn simple_find_entity_by_attr_value() {
    let path = tmp_db_path("edn_simple_query");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    let a_user_id = Attribute { ident: ":user/id".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::Identity, is_component: false, no_history: false, doc: None, aliases: vec![] };
    let a_noise = Attribute { ident: ":noise/x".into(), value_type: ValueType::Long, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
    txr.install_attribute(&a_user_id).unwrap();
    txr.install_attribute(&a_noise).unwrap();

    let mut ops: Vec<serde_json::Value> = Vec::new();
    ops.push(serde_json::json!(["add", "temp:u1", ":user/id", "U1"]));
    for i in 0..1200 { ops.push(serde_json::json!(["add", format!("temp:n{i}"), ":noise/x", i])); }
    txr.apply_tx(&ops).expect("tx");
    let expected_e: i64 = txr
        .conn
        .query_row(
            "SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2",
            rusqlite::params![":user/id", "S:U1"],
            |r| r.get(0),
        )
        .expect("lookup");

    let rows = execute_simple_edn(&path, "[:find ?e :where [?e :user/id \"U1\"]]").expect("query");
    assert!(rows.contains(&expected_e));
}
