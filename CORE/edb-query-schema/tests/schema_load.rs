use edb_encoding::ValueType;
use edb_query_schema::{SchemaCatalog, SchemaError, SchemaLookup};
use edb_schema::{AttrCardinality, AttrUnique};
use rusqlite::{params, Connection};

fn setup_db(conn: &Connection) {
    conn.execute_batch(
        r#"
        CREATE TABLE attrs(
          ident TEXT PRIMARY KEY,
          vt INTEGER NOT NULL,
          card INTEGER NOT NULL,
          uniq INTEGER NOT NULL,
          is_component INTEGER NOT NULL,
          no_history INTEGER NOT NULL,
          doc TEXT
        );
        CREATE TABLE aliases(
          alias TEXT PRIMARY KEY,
          target TEXT NOT NULL
        );
        "#,
    )
    .unwrap();
}

#[test]
fn load_schema_with_aliases() {
    let conn = Connection::open_in_memory().unwrap();
    setup_db(&conn);

    conn.execute(
        "INSERT INTO attrs(ident,vt,card,uniq,is_component,no_history,doc) VALUES(?1,?2,?3,?4,?5,?6,?7)",
        params![":user/id", ValueType::String as i64, 1, 1, 0, 0, Option::<String>::None],
    )
    .unwrap();
    conn.execute(
        "INSERT INTO attrs(ident,vt,card,uniq,is_component,no_history,doc) VALUES(?1,?2,?3,?4,?5,?6,?7)",
        params![":user/name", ValueType::String as i64, 1, 0, 0, 0, Option::<String>::None],
    )
    .unwrap();
    conn.execute(
        "INSERT INTO aliases(alias,target) VALUES(?1,?2)",
        params![":user/uid", ":user/id"],
    )
    .unwrap();

    let schema = SchemaCatalog::load(&conn).expect("schema");
    let attr = schema.attribute(":user/uid").expect("alias attr");
    assert_eq!(attr.ident, ":user/id");
    assert_eq!(attr.cardinality, AttrCardinality::One);
    assert_eq!(attr.unique, AttrUnique::Identity);
    assert_eq!(attr.value_type, ValueType::String);
    assert_eq!(schema.canonical_ident(":user/uid"), Some(":user/id".to_string()));

    let aliases = schema.aliases_for(":user/id");
    assert!(aliases.contains(&":user/uid".to_string()));
}

#[test]
fn load_schema_unknown_value_type_errors() {
    let conn = Connection::open_in_memory().unwrap();
    setup_db(&conn);
    conn.execute(
        "INSERT INTO attrs(ident,vt,card,uniq,is_component,no_history,doc) VALUES(?1,?2,?3,?4,?5,?6,?7)",
        params![":bad/vt", 99, 1, 0, 0, 0, Option::<String>::None],
    )
    .unwrap();

    let err = SchemaCatalog::load(&conn).expect_err("should error");
    match err {
        SchemaError::UnknownValueType(99) => {}
        other => panic!("unexpected error: {other:?}"),
    }
}
