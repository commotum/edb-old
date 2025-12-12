use edb_encoding::ValueType;
use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_transactor::SqliteTransactor;
use rusqlite::params;
use serde_json::json;

fn tmp_db_path(name: &str) -> String {
    let mut p = std::env::temp_dir();
    p.push(format!("{}_{}.db", name, std::process::id()));
    p.to_string_lossy().to_string()
}

#[test]
fn alias_write_and_read() {
    let path = tmp_db_path("edb_alias");
    let mut txr = SqliteTransactor::open(&path).expect("open");

    // Install schema
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
    txr.install_attribute(&a_user_id).unwrap();
    txr.install_attribute(&a_user_name).unwrap();

    // Install alias :user/fullName -> :user/name
    txr.install_alias(":user/fullName", ":user/name").expect("alias");

    // Transact using the alias
    let ops = vec![
        json!(["add", "temp:u", ":user/id", "U-42"]),
        json!(["add", "temp:u", ":user/fullName", "Alice"]),
    ];
    let _rep = txr.apply_tx(&ops).expect("tx");

    // Fetch entid and verify stored under canonical ident
    let e: i64 = txr
        .conn
        .query_row(
            "SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2",
            params![":user/id", "S:U-42"],
            |r| r.get(0),
        )
        .unwrap();
    let got: String = txr
        .conn
        .query_row(
            "SELECT vjson FROM current WHERE e=?1 AND a=?2",
            params![e, ":user/name"],
            |r| r.get(0),
        )
        .unwrap();
    let v: edb_tx::model::Value = serde_json::from_str(&got).unwrap();
    assert_eq!(v, edb_tx::model::Value::String("Alice".into()));
}

#[test]
fn alias_validation_errors() {
    let path = tmp_db_path("edb_alias_err");
    let txr = SqliteTransactor::open(&path).expect("open");

    // Install two attrs
    let a_name = Attribute {
        ident: ":user/name".into(),
        value_type: ValueType::String,
        cardinality: AttrCardinality::One,
        unique: AttrUnique::None,
        is_component: false,
        no_history: false,
        doc: None,
        aliases: vec![],
    };
    let a_age = Attribute {
        ident: ":user/age".into(),
        value_type: ValueType::Long,
        cardinality: AttrCardinality::One,
        unique: AttrUnique::None,
        is_component: false,
        no_history: false,
        doc: None,
        aliases: vec![],
    };
    txr.install_attribute(&a_name).unwrap();
    txr.install_attribute(&a_age).unwrap();

    // alias==target should error
    assert!(txr.install_alias(":user/name", ":user/name").is_err());
    // alias colliding with existing ident should error
    assert!(txr.install_alias(":user/age", ":user/name").is_err());
}

