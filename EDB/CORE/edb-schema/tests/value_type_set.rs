use edb_schema::ValueTypeSet;
use edb_encoding::ValueType;

#[test]
fn value_type_set_basic_ops() {
    let mut set = ValueTypeSet::none();
    assert!(set.is_empty());
    set.insert(ValueType::Long);
    set.insert(ValueType::Double);
    assert!(set.contains(ValueType::Long));
    assert!(set.contains(ValueType::Double));
    assert_eq!(set.len(), 2);
    assert!(set.is_only_numeric());

    let other = ValueTypeSet::of_one(ValueType::Instant);
    let union = set.union(&other);
    assert!(union.contains(ValueType::Instant));
    assert_eq!(union.len(), 3);
    let intersection = union.intersection(&set);
    assert_eq!(intersection.len(), 2);
    let diff = union.difference(&set);
    assert_eq!(diff.len(), 1);
    assert!(diff.contains(ValueType::Instant));
}
