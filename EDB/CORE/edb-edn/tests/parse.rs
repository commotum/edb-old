use edb_edn::{parse_query, parse_value};
use edb_edn::query::{Direction, FindSpec, Limit, PullPattern, QueryInput, ReturnMapSpec, WhereClause};

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
    let q = parse_query("{:find [?e ?name] :keys [e name] :where [[?e :user/name ?name]]}").expect("query");
    match q.find_spec {
        FindSpec::FindRel(ref elems) => assert_eq!(elems.len(), 2),
        other => panic!("expected find-rel, got {other:?}"),
    }
    match q.return_map {
        Some(ReturnMapSpec::Keys(ref keys)) => assert_eq!(keys.len(), 2),
        other => panic!("expected return map, got {other:?}"),
    }
}

#[test]
fn parse_edn_query_in_bindings_and_rules() {
    let q = parse_query("[:find ?e :in $ ?name [?age ...] [[?e ?a]] % pattern :where [rule-name ?e ?name]]").expect("query");
    assert_eq!(q.inputs.len(), 6);
    assert!(matches!(q.inputs[0], QueryInput::SrcVar(_)));
    assert!(matches!(q.inputs[1], QueryInput::Binding(_)));
    assert!(matches!(q.inputs[2], QueryInput::Binding(_)));
    assert!(matches!(q.inputs[3], QueryInput::Binding(_)));
    assert!(matches!(q.inputs[4], QueryInput::RulesVar));
    assert!(matches!(q.inputs[5], QueryInput::PatternName(_)));
    assert!(matches!(q.where_clauses[0], WhereClause::RuleExpr(_)));
}

#[test]
fn parse_edn_query_return_maps() {
    let q = parse_query("[:find ?e ?name :strs \"e\" \"name\" :where [?e :user/name ?name]]").expect("query");
    match q.return_map {
        Some(ReturnMapSpec::Strs(ref strs)) => assert_eq!(strs.len(), 2),
        other => panic!("expected return map, got {other:?}"),
    }
}

#[test]
fn parse_edn_query_pull_pattern_name() {
    let q = parse_query("[:find (pull ?e pattern) :in $ pattern :where [?e :user/id 1]]").expect("query");
    let elem = match q.find_spec {
        FindSpec::FindRel(ref elems) => elems.first().expect("elem"),
        _ => panic!("expected find-rel"),
    };
    match elem {
        edb_edn::query::Element::Pull(pull) => match &pull.pattern {
            PullPattern::Named(_) => {}
            other => panic!("expected named pattern, got {other:?}"),
        },
        other => panic!("expected pull element, got {other:?}"),
    }
}

#[test]
fn parse_edn_query_order_forms() {
    let q = parse_query("[:find ?e :order ?e (desc ?e) (asc ?e) :where [?e :user/id 1]]").expect("query");
    let order = q.order.expect("order");
    assert_eq!(order.len(), 3);
    assert!(matches!(order[0].0, Direction::Ascending));
    assert!(matches!(order[1].0, Direction::Descending));
    assert!(matches!(order[2].0, Direction::Ascending));
}

#[test]
fn parse_edn_query_limit_forms() {
    let q = parse_query("[:find ?e :limit 10 :where [?e :user/id 1]]").expect("query");
    assert!(matches!(q.limit, Limit::Fixed(10)));

    let q = parse_query("[:find ?e :limit nil :where [?e :user/id 1]]").expect("query");
    assert!(matches!(q.limit, Limit::None));

    let q = parse_query("[:find ?e :in ?lim :limit ?lim :where [?e :user/id 1]]").expect("query");
    match q.limit {
        Limit::Variable(ref var) => assert_eq!(var.to_string(), "?lim"),
        other => panic!("expected limit variable, got {other:?}"),
    }
}

#[test]
fn parse_edn_query_limit_zero_rejected() {
    let res = parse_query("[:find ?e :limit 0 :where [?e :user/id 1]]");
    assert!(res.is_err());
}
