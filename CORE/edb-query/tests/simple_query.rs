use edb_encoding::ValueType;
use edb_query::execute_edn;
use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_transactor::SqliteTransactor;

fn tmp_db_path(name: &str) -> String {
    let mut p = std::env::temp_dir();
    let now = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap()
        .as_nanos();
    p.push(format!("{}_{}_{}.db", name, std::process::id(), now));
    p.to_string_lossy().to_string()
}

fn seed_user(txr: &mut SqliteTransactor) -> i64 {
    let a_user_id = Attribute {
        ident: ":user/id".into(),
        value_type: ValueType::String,
        cardinality: AttrCardinality::One,
        unique: AttrUnique::Identity,
        is_component: false,
        no_history: false,
        doc: None,
        aliases: vec![],
    };
    let a_user_name = Attribute {
        ident: ":user/name".into(),
        value_type: ValueType::String,
        cardinality: AttrCardinality::One,
        unique: AttrUnique::None,
        is_component: false,
        no_history: false,
        doc: None,
        aliases: vec![],
    };
    let a_noise = Attribute {
        ident: ":noise/x".into(),
        value_type: ValueType::Long,
        cardinality: AttrCardinality::One,
        unique: AttrUnique::None,
        is_component: false,
        no_history: false,
        doc: None,
        aliases: vec![],
    };
    txr.install_attribute(&a_user_id).unwrap();
    txr.install_attribute(&a_user_name).unwrap();
    txr.install_attribute(&a_noise).unwrap();

    let mut ops: Vec<serde_json::Value> = Vec::new();
    ops.push(serde_json::json!(["add", "temp:u1", ":user/id", "U1"]));
    ops.push(serde_json::json!(["add", "temp:u1", ":user/name", "Alice"]));
    for i in 0..1200 {
        ops.push(serde_json::json!(["add", format!("temp:n{i}"), ":noise/x", i]));
    }
    txr.apply_tx(&ops).expect("tx");

    txr.conn
        .query_row(
            "SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2",
            rusqlite::params![":user/id", "S:U1"],
            |r| r.get(0),
        )
        .expect("lookup")
}

#[test]
fn simple_find_entity_by_attr_value() {
    let path = tmp_db_path("edn_simple_query");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    let expected_e = seed_user(&mut txr);

    let res = execute_edn(&path, "[:find ?e :where [?e :user/id \"U1\"]]").expect("query");
    let rows = res.as_array().expect("rows");
    assert!(rows.iter().any(|row| row.as_array().and_then(|r| r.first()).and_then(|v| v.as_i64()) == Some(expected_e)));
}

#[test]
fn find_entity_with_two_clauses() {
    let path = tmp_db_path("edn_two_clauses");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    let expected_e = seed_user(&mut txr);

    let res = execute_edn(
        &path,
        "[:find ?e :where [?e :user/id \"U1\"] [?e :user/name \"Alice\"]]",
    )
    .expect("query");
    let rows = res.as_array().expect("rows");
    assert_eq!(rows.len(), 1);
    assert_eq!(rows[0].as_array().and_then(|r| r.first()).and_then(|v| v.as_i64()), Some(expected_e));
}

#[test]
fn find_value_variable() {
    let path = tmp_db_path("edn_value_var");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    seed_user(&mut txr);

    let res = execute_edn(&path, "[:find ?name :where [?e :user/name ?name]]").expect("query");
    let rows = res.as_array().expect("rows");
    assert!(rows.iter().any(|row| row.as_array().and_then(|r| r.first()).and_then(|v| v.as_str()) == Some("Alice")));
}

#[test]
fn find_entity_with_placeholder() {
    let path = tmp_db_path("edn_placeholder");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    let expected_e = seed_user(&mut txr);

    let res = execute_edn(&path, "[:find ?e :where [?e :user/name _]]").expect("query");
    let rows = res.as_array().expect("rows");
    assert!(rows.iter().any(|row| row.as_array().and_then(|r| r.first()).and_then(|v| v.as_i64()) == Some(expected_e)));
}
