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
fn since_and_history_views() {
    let path = tmp_db_path("edb_time_views");
    let mut txr = SqliteTransactor::open(&path).expect("open");

    // Install minimal schema: identity and a name attribute
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

    // Tx1: create a user with name "Alice"
    let ops1 = vec![
        json!(["add", "temp:u1", ":user/id", "U-1"]),
        json!(["add", "temp:u1", ":user/name", "Alice"]),
    ];
    let rep1 = txr.apply_tx(&ops1).expect("tx1");
    let t1 = rep1.t.unwrap();

    // Resolve entid via unique_idx
    let e1: i64 = txr
        .conn
        .query_row(
            "SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2",
            params![":user/id", "S:U-1"],
            |r| r.get(0),
        )
        .unwrap();

    // Tx2: change name to "Alicia"
    let ops2 = vec![json!(["add", e1, ":user/name", "Alicia"])];
    let rep2 = txr.apply_tx(&ops2).expect("tx2");
    let _t2 = rep2.t.unwrap();

    // Since(t1) should see the changed name only
    let since = txr.since_entity_attrs(e1, t1).expect("since");
    assert_eq!(since.len(), 1);
    assert_eq!(since[0].0, ":user/name");
    assert_eq!(since[0].1, edb_tx::model::Value::String("Alicia".into()));

    // Tx3: retract name
    let ops3 = vec![json!(["retract", e1, ":user/name", null])];
    let rep3 = txr.apply_tx(&ops3).expect("tx3");
    let _t3 = rep3.t.unwrap();

    // Since(t1) after retraction should be empty (net effect since t1 is no name)
    let since2 = txr.since_entity_attrs(e1, t1).expect("since2");
    assert!(since2.is_empty());

    // History should include all primitives for the entity in order
    let hist = txr.history_entity(e1).expect("history");
    assert!(hist.len() >= 3); // add name, retract old on change, add new, retract in tx3
    // Last primitive should be the retraction of :user/name
    let last = hist.last().unwrap();
    assert!(!last.added);
    assert_eq!(last.a, ":user/name");
}

