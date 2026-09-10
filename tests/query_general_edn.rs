use atomic_core::edn::{EdnValue, read_edn};
use atomic_core::edn_query::{EdnQueryArgument, parse_query_edn};
use atomic_core::edn_value::{edn_to_query_value, edn_to_value, query_value_to_edn};
use atomic_core::*;

fn data(text: &str) -> EdnQueryArgument {
    EdnQueryArgument::Data(read_edn(text).unwrap())
}

fn run(text: &str, arguments: &[EdnQueryArgument]) -> EdnValue {
    let bound = parse_query_edn(text).unwrap().bind(arguments).unwrap();
    let result = bound.execute(&QueryControl::default(), None).unwrap();
    bound.result_to_edn(&result.result).unwrap()
}

#[test]
fn general_inputs_remain_parameters_with_exact_nested_data() {
    let template = parse_query_edn("[:find ?value . :in ?value]").unwrap();
    let nil = template.bind(&[data("nil")]).unwrap();
    let text =
        r#"{[1 2] #{\λ #app/item {:amount 123456789012345678901N :price 1.250M}} :empty []}"#;
    let nested = template.bind(&[data(text)]).unwrap();
    assert_eq!(nil.query(), nested.query());
    assert_eq!(nil.inputs.len(), 1);
    assert!(nil.query().clauses.is_empty());
    assert!(matches!(
        &nil.inputs[0],
        QueryInput::General(QueryValue::Nil)
    ));
    assert!(nil.sources.is_empty());
    assert!(nested.sources.is_empty());
    let result = nested.execute(&QueryControl::default(), None).unwrap();
    assert_eq!(
        nested.result_to_edn(&result.result).unwrap(),
        read_edn(text).unwrap()
    );
    let prepared = PreparedQuery::new(nested.query()).unwrap();
    assert_eq!(
        prepared
            .execute(&[], &nil.inputs, &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Nil)),
    );
}

#[test]
fn seven_column_relations_keep_nested_cells_rules_and_literal_prefixes() {
    let rows = data(
        r#"[[[1 2] :a nil 4 5 {:name "Alice"} #app/id 7]
                        [[3 4] :b nil 4 5 {:name "Bob"} #app/id 8]]"#,
    );
    assert_eq!(
        run(
            "[:find ?person ?tag :in $rows :where [$rows [1 2] _ _ _ _ ?person ?tag]]",
            std::slice::from_ref(&rows)
        ),
        read_edn(r#"#{[{:name "Alice"} #app/id 7]}"#).unwrap(),
    );
    let rules = data("[[(row-info ?id ?person ?tag) [?id _ _ _ _ ?person ?tag]]]");
    assert_eq!(
        run(
            "[:find ?person :in $rows % :where ($rows row-info [3 4] ?person _)]",
            &[rows, rules]
        ),
        read_edn(r#"#{[{:name "Bob"}]}"#).unwrap(),
    );
}

#[test]
fn general_literals_and_source_free_nested_queries_need_no_synthetic_source() {
    let text = r#"[:find ?value . :in :where
        [(q [:find ?v . :in ?v] {:letters #{\a \b} :long [1 2 3 4 5 6 7 8 9]}) ?value]]"#;
    let bound = parse_query_edn(text).unwrap().bind(&[]).unwrap();
    assert!(bound.sources.is_empty());
    let result = bound.execute(&QueryControl::default(), None).unwrap();
    assert_eq!(
        bound.result_to_edn(&result.result).unwrap(),
        read_edn(r#"{:letters #{\a \b} :long [1 2 3 4 5 6 7 8 9]}"#).unwrap(),
    );
}

#[test]
fn pure_native_callback_accepts_and_returns_general_data_without_database() {
    let mut extensions = QueryExtensions::new();
    extensions.register_pure("app/wrap", |args, _control| {
        let [value] = args else {
            return Err(SemanticError::incorrect(
                "app/arity",
                "wrap requires one value",
            ));
        };
        Ok(QueryValue::Map(vec![(
            QueryValue::Scalar(Value::Keyword(Keyword::unqualified("result"))),
            value.clone(),
        )]))
    });
    let bound =
        parse_query_edn("[:find ?wrapped . :in ?input :where [(app/wrap ?input) ?wrapped]]")
            .unwrap()
            .bind(&[data(r#"#{nil \x #app/item {:a 1}}"#)])
            .unwrap();
    assert!(bound.sources.is_empty());
    let result = bound
        .execute(&QueryControl::default(), Some(&extensions))
        .unwrap();
    assert_eq!(
        bound.result_to_edn(&result.result).unwrap(),
        read_edn(r#"{:result #{nil \x #app/item {:a 1}}}"#).unwrap(),
    );
    let nested = parse_query_edn("[:find ?wrapped . :in :where [(q [:find ?out . :in :where [(app/wrap nil) ?out]]) ?wrapped]]")
        .unwrap().bind(&[]).unwrap();
    assert!(nested.sources.is_empty());
    let result = nested
        .execute(&QueryControl::default(), Some(&extensions))
        .unwrap();
    assert_eq!(
        nested.result_to_edn(&result.result).unwrap(),
        read_edn("{:result nil}").unwrap()
    );
}

#[test]
fn set_input_is_a_collection_but_not_a_positional_tuple_or_relation_row() {
    let EdnValue::Vector(values) = run("[:find [?v ...] :in [?v ...]]", &[data("#{1 2}")]) else {
        panic!("collection result");
    };
    assert_eq!(EdnValue::Set(values), read_edn("#{1 2}").unwrap());
    for query in [
        "[:find ?v :in [?v]]",
        "[:find ?v :in [[?v]]]",
        "[:find ?v :in $rows :where [$rows ?v]]",
    ] {
        let argument = if query.contains("[[?v]]") || query.contains("$rows") {
            data("[#{1}]")
        } else {
            data("#{1}")
        };
        assert_eq!(
            parse_query_edn(query)
                .unwrap()
                .bind(&[argument])
                .unwrap_err()
                .code,
            "edn/query-input-shape"
        );
    }
}

#[test]
fn known_scalar_tags_keep_validation_while_custom_tags_remain_inert_data() {
    let invalid = EdnValue::Tagged(Symbol::unqualified("uuid"), Box::new(EdnValue::Long(1)));
    assert!(edn_to_query_value(&invalid).is_err());
    let custom = read_edn("#app/object {:run (send-email \"Alice\")}").unwrap();
    assert_eq!(
        query_value_to_edn(&edn_to_query_value(&custom).unwrap()).unwrap(),
        custom
    );
    // Stored-value admission remains a separate, deliberately narrower contract.
    assert!(edn_to_value(&custom).is_err());
}

#[test]
fn get_else_preserves_a_general_default_without_weakening_nil_or_schema_rules() {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("person", "year"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let database = Database::new(schema).unwrap();
    let template = parse_query_edn(
        "[:find ?result . :in $ ?default :where [(get-else $ 2000 :person/year ?default) ?result]]",
    )
    .unwrap();
    let fallback = r#"{:reason #app/missing :year :tags #{:unknown} :marker \?}"#;
    let bound = template
        .bind(&[
            EdnQueryArgument::Source(QuerySourceValue::Database(database.database_value())),
            data(fallback),
        ])
        .unwrap();
    let result = bound.execute(&QueryControl::default(), None).unwrap();
    assert_eq!(
        bound.result_to_edn(&result.result).unwrap(),
        read_edn(fallback).unwrap()
    );
    let nil = template
        .bind(&[
            EdnQueryArgument::Source(QuerySourceValue::Database(database.database_value())),
            data("nil"),
        ])
        .unwrap();
    assert_eq!(
        nil.execute(&QueryControl::default(), None)
            .unwrap_err()
            .code,
        "query/get-else-nil-default"
    );
}
