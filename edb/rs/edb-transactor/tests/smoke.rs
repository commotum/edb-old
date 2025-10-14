use std::path::PathBuf;

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
fn smoke_apply_tx_and_current_state() {
    let path = tmp_db_path("edb_smoke");
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
    let a_user_friend = Attribute {
        ident: ":user/friend".into(),
        value_type: ValueType::Ref,
        cardinality: AttrCardinality::One,
        unique: AttrUnique::None,
        is_component: false,
        no_history: false,
        doc: None,
        aliases: vec![],
    };
    txr.install_attribute(&a_user_id).unwrap();
    txr.install_attribute(&a_user_name).unwrap();
    txr.install_attribute(&a_user_friend).unwrap();

    // Tx1: create two users via tempids
    let ops1 = vec![
        json!(["add", "temp:u1", ":user/id", "U-1"]),
        json!(["add", "temp:u1", ":user/name", "Alice"]),
        json!(["add", "temp:u2", ":user/id", "U-2"]),
        json!(["add", "temp:u2", ":user/name", "Bob"]),
    ];
    let rep1 = txr.apply_tx(&ops1).expect("tx1");
    assert!(rep1.t.unwrap() > 0);

    // Fetch entids by unique_idx
    let e1: i64 = txr
        .conn
        .query_row(
            "SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2",
            params![":user/id", "S:U-1"],
            |r| r.get(0),
        )
        .unwrap();
    let e2: i64 = txr
        .conn
        .query_row(
            "SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2",
            params![":user/id", "S:U-2"],
            |r| r.get(0),
        )
        .unwrap();
    assert!(e1 != e2);

    // Tx2: set friend of U-2 to lookup(U-1)
    let ops2 = vec![json!(["add", e2, ":user/friend", ["lookup", ":user/id", "U-1"]])];
    let rep2 = txr.apply_tx(&ops2).expect("tx2");
    assert!(rep2.t.unwrap() > rep1.t.unwrap());
    // Verify friend ref
    let vjson: String = txr
        .conn
        .query_row(
            "SELECT vjson FROM current WHERE e=?1 AND a=?2",
            params![e2, ":user/friend"],
            |r| r.get(0),
        )
        .unwrap();
    let v: edb_tx::model::Value = serde_json::from_str(&vjson).unwrap();
    assert_eq!(v, edb_tx::model::Value::Ref(e1));

    // Tx3: change name for U-1
    let ops3 = vec![json!(["add", e1, ":user/name", "Alicia"])];
    let rep3 = txr.apply_tx(&ops3).expect("tx3");
    assert!(rep3.t.unwrap() > rep2.t.unwrap());
    let name_json: String = txr
        .conn
        .query_row(
            "SELECT vjson FROM current WHERE e=?1 AND a=?2",
            params![e1, ":user/name"],
            |r| r.get(0),
        )
        .unwrap();
    let name_val: edb_tx::model::Value = serde_json::from_str(&name_json).unwrap();
    assert_eq!(name_val, edb_tx::model::Value::String("Alicia".into()));

    // Subscribe to tx-reports and apply another tx
    let rx = txr.subscribe();
    let ops4 = vec![json!(["add", e2, ":user/name", "Bobby"])];
    let rep4 = txr.apply_tx(&ops4).expect("tx4");
    let got = rx.recv().unwrap();
    assert_eq!(got.t, rep4.t);

    // Wipe current/unique_idx and replay from log, then verify
    txr.conn.execute("DELETE FROM current", []).unwrap();
    txr.conn.execute("DELETE FROM unique_idx", []).unwrap();
    txr.replay_current_from_log().expect("replay");
    let name1: String = txr
        .conn
        .query_row(
            "SELECT vjson FROM current WHERE e=?1 AND a=?2",
            params![e1, ":user/name"],
            |r| r.get(0),
        )
        .unwrap();
    let name2: String = txr
        .conn
        .query_row(
            "SELECT vjson FROM current WHERE e=?1 AND a=?2",
            params![e2, ":user/name"],
            |r| r.get(0),
        )
        .unwrap();
    assert_eq!(serde_json::from_str::<edb_tx::model::Value>(&name1).unwrap(), edb_tx::model::Value::String("Alicia".into()));
    assert_eq!(serde_json::from_str::<edb_tx::model::Value>(&name2).unwrap(), edb_tx::model::Value::String("Bobby".into()));
}
