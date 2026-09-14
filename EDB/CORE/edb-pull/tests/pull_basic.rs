use edb_encoding::ValueType;
use edb_schema::{Attribute, AttrCardinality, AttrUnique};
use edb_transactor::SqliteTransactor;
use edb_pull::{Puller, AttrSpec};
use serde_json::json;

fn tmp_db_path(name: &str) -> String {
    let mut p = std::env::temp_dir();
    p.push(format!("{}_{}.db", name, std::process::id()));
    p.to_string_lossy().to_string()
}

#[test]
fn pull_forward_and_reverse() {
    let path = tmp_db_path("pull");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    // Schema
    let a_user_id = Attribute { ident: ":user/id".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::Identity, is_component: false, no_history: false, doc: None, aliases: vec![] };
    let a_user_name = Attribute { ident: ":user/name".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
    let a_user_friend = Attribute { ident: ":user/friend".into(), value_type: ValueType::Ref, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
    let a_user_nick = Attribute { ident: ":user/nickname".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
    txr.install_attribute(&a_user_id).unwrap();
    txr.install_attribute(&a_user_name).unwrap();
    txr.install_attribute(&a_user_friend).unwrap();
    txr.install_attribute(&a_user_nick).unwrap();

    // Tx1: two users
    let ops1 = vec![
        json!(["add", "temp:u1", ":user/id", "U1"]),
        json!(["add", "temp:u1", ":user/name", "Alice"]),
        json!(["add", "temp:u2", ":user/id", "U2"]),
        json!(["add", "temp:u2", ":user/name", "Bob"]),
    ];
    let rep1 = txr.apply_tx(&ops1).expect("tx1");
    assert!(rep1.t.unwrap() > 0);
    // Lookup ids
    let e1: i64 = txr.conn.query_row("SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2", rusqlite::params![":user/id", "S:U1"], |r| r.get(0)).unwrap();
    let e2: i64 = txr.conn.query_row("SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2", rusqlite::params![":user/id", "S:U2"], |r| r.get(0)).unwrap();
    // Friend link Bob -> Alice
    let ops2 = vec![json!(["add", e2, ":user/friend", e1])];
    txr.apply_tx(&ops2).expect("tx2");

    // Pull
    let puller = Puller::new_with_path(&txr.conn, &path).with_limit(Some(100));
    let pat = vec![AttrSpec::Attr(":user/name".into()), AttrSpec::Reverse(":user/friend".into())];
    let alice = puller.pull_entity(e1, &pat).expect("pull");
    // name present
    assert_eq!(alice.get(":user/name").unwrap(), &json!("Alice"));
    // reverse friend: Bob references Alice
    let rev = alice.get("_:user/friend").unwrap().as_array().unwrap().clone();
    assert_eq!(rev, vec![json!(e2)]);

    // Nested forward: { :user/friend [:user/name] } on Bob
    let pat2 = vec![AttrSpec::Nested { attr: ":user/friend".into(), specs: vec![AttrSpec::Attr(":user/name".into())] }];
    let bob = puller.pull_entity(e2, &pat2).expect("pull2");
    let friend = bob.get(":user/friend").unwrap().as_object().unwrap();
    assert_eq!(friend.get(":user/name").unwrap(), &json!("Alice"));

    // Alias and default: alias :user/name as :name, and provide default for missing attr
    let pat3 = vec![
        AttrSpec::AttrAs { attr: ":user/name".into(), alias: ":name".into() },
        AttrSpec::AttrDefault { attr: ":user/nickname".into(), default: json!("none") },
    ];
    let alice2 = puller.pull_entity(e1, &pat3).expect("pull3");
    assert_eq!(alice2.get(":name").unwrap(), &json!("Alice"));
    assert_eq!(alice2.get(":user/nickname").unwrap(), &json!("none"));
}
