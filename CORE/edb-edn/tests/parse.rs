use edb_edn::{parse_query, parse_value};
use edb_edn::query::FindSpec;

#[test]
fn parse_edn_value_basic() {
    let v = parse_value("[1 2 3]").expect("value");
    match v {
        edb_edn::Value::Vector(items) => {
            assert_eq!(items.len(), 3);
            assert_eq!(items[0], edb_edn::Value::Integer(1));
            assert_eq!(items[1], edb_edn::Value::Integer(2));
            assert_eq!(items[2], edb_edn::Value::Integer(3));
        }
        other => panic!("expected vector, got {other:?}"),
    }
}

#[test]
fn parse_edn_query_basic() {
    let q = parse_query("[:find ?e :where [?e :user/id \"U1\"]]").expect("query");
    assert_eq!(q.where_clauses.len(), 1);
}

#[test]
fn parse_edn_decimal_basic() {
    let v = parse_value("12.5M").expect("value");
    match v {
        edb_edn::Value::Decimal(_) => {}
        other => panic!("expected decimal, got {other:?}"),
    }
}

#[test]
fn parse_edn_query_map_form() {
    let q = parse_query("{:find [?e ?name] :where [[?e :user/name ?name]]}").expect("query");
    match q.find_spec {
        FindSpec::FindRel(ref elems) => assert_eq!(elems.len(), 2),
        other => panic!("expected find-rel, got {other:?}"),
    }
}
