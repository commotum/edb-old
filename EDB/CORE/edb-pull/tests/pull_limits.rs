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
fn nested_limit_and_component_default() {
    let path = tmp_db_path("pull_limits");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    // Schema: user with friends (many refs) and address component (one ref, is_component)
    let a_user_id = Attribute { ident: ":user/id".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::Identity, is_component: false, no_history: false, doc: None, aliases: vec![] };
    let a_user_name = Attribute { ident: ":user/name".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
    let a_user_friend = Attribute { ident: ":user/friend".into(), value_type: ValueType::Ref, cardinality: AttrCardinality::Many, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
    let a_user_addr = Attribute { ident: ":user/address".into(), value_type: ValueType::Ref, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: true, no_history: false, doc: None, aliases: vec![] };
    let a_addr_city = Attribute { ident: ":addr/city".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
    txr.install_attribute(&a_user_id).unwrap();
    txr.install_attribute(&a_user_name).unwrap();
    txr.install_attribute(&a_user_friend).unwrap();
    txr.install_attribute(&a_user_addr).unwrap();
    txr.install_attribute(&a_addr_city).unwrap();

    // Tx: three users and an address
    let ops = vec![
        json!(["add", "temp:u1", ":user/id", "U1"]),
        json!(["add", "temp:u1", ":user/name", "Alice"]),
        json!(["add", "temp:u2", ":user/id", "U2"]),
        json!(["add", "temp:u2", ":user/name", "Bob"]),
        json!(["add", "temp:u3", ":user/id", "U3"]),
        json!(["add", "temp:u3", ":user/name", "Carol"]),
        json!(["add", "temp:addr", ":addr/city", "NYC"]),
    ];
    let rep = txr.apply_tx(&ops).expect("tx");
    let _t = rep.t.unwrap();
    // Lookup ids
    let e1: i64 = txr.conn.query_row("SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2", rusqlite::params![":user/id", "S:U1"], |r| r.get(0)).unwrap();
    let e2: i64 = txr.conn.query_row("SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2", rusqlite::params![":user/id", "S:U2"], |r| r.get(0)).unwrap();
    let e3: i64 = txr.conn.query_row("SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2", rusqlite::params![":user/id", "S:U3"], |r| r.get(0)).unwrap();
    let addr: i64 = txr.conn.query_row("SELECT e FROM current WHERE a=?1 LIMIT 1", rusqlite::params![":addr/city"], |r| r.get(0)).unwrap();
    // Two users (U2, U3) friend U1, so reverse of :user/friend on U1 yields two
    txr.apply_tx(&[json!(["add", e2, ":user/friend", e1]), json!(["add", e3, ":user/friend", e1])]).unwrap();
    // Add address component to U1
    txr.apply_tx(&[json!(["add", e1, ":user/address", addr])]).unwrap();

    // Pull with per-spec limit on friends
    let puller = Puller::new_with_path(&txr.conn, &path);
    let spec = vec![AttrSpec::ReverseNestedLimit { attr: ":user/friend".into(), specs: vec![AttrSpec::Attr(":user/name".into())], limit: Some(1), max_depth: None }];
    let out = puller.pull_entity(e1, &spec).unwrap();
    let friends = out.get("_:user/friend").unwrap().as_array().unwrap();
    assert_eq!(friends.len(), 1);

    // Component-default expansion: pulling :user/address returns a nested map with :addr/city
    let spec2 = vec![AttrSpec::Attr(":user/address".into())];
    let out2 = puller.pull_entity(e1, &spec2).unwrap();
    let addr_obj = out2.get(":user/address").unwrap().as_object().unwrap();
    assert_eq!(addr_obj.get(":addr/city").unwrap(), &json!("NYC"));
}
