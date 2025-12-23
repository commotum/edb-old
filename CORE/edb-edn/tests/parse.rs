use edb_edn::{parse_query, parse_value};

#[test]
fn parse_edn_value_basic() {
    let v = parse_value("[1 2 3]").expect("value");
    match v {
        edn::Value::Vector(items) => {
            assert_eq!(items.len(), 3);
            assert_eq!(items[0], edn::Value::Integer(1));
            assert_eq!(items[1], edn::Value::Integer(2));
            assert_eq!(items[2], edn::Value::Integer(3));
        }
        other => panic!("expected vector, got {other:?}"),
    }
}

#[test]
fn parse_edn_query_basic() {
    let q = parse_query("[:find ?e :where [?e :user/id \"U1\"]]").expect("query");
    assert_eq!(q.where_clauses.len(), 1);
}
