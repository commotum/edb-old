use super::*;
use crate::{
    Attribute, Cardinality, INITIAL_EIDX_FRONTIER, Keyword, USER_PARTITION, Value, make_eid,
};
use std::collections::BTreeMap;

fn schema(indexed: bool) -> Schema {
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        1_000,
        Keyword::new("poll", "value"),
        ValueType::Long,
        Cardinality::One,
    );
    attribute.indexed = indexed;
    schema.install(attribute).unwrap();
    schema
}

fn transaction(t: u64, previous_hash: Digest, tx_data: Vec<Datom>) -> DurableTransaction {
    DurableTransaction {
        database_id: "polling".into(),
        basis_t: t,
        previous_hash,
        eidx_frontier: INITIAL_EIDX_FRONTIER.max(1_024),
        tempids: BTreeMap::new(),
        tx_data,
    }
}

fn tombstones(count: usize) -> RecentTier {
    let transaction = transaction(
        11,
        [0; 32],
        (0..count)
            .map(|index| Datom {
                entity: make_eid(USER_PARTITION, index as u64 + 1).unwrap(),
                attribute: 1_000,
                value: Value::Long(index as i64),
                tx: t_to_tx(11).unwrap(),
                added: false,
            })
            .collect(),
    );
    RecentTier::new(
        "polling",
        10,
        [0; 32],
        [transaction],
        EndpointProjection::new(schema(true)),
        RecentLimits::default(),
    )
    .unwrap()
}

fn cursor(tier: &RecentTier, reverse: bool) -> RecentCursor {
    if reverse {
        tier.reverse_boundary_cursor(
            false,
            &NormalizedIndexBoundary::Eavt(IndexComponents::Empty),
        )
    } else {
        tier.boundary_cursor(
            false,
            &NormalizedIndexBoundary::Eavt(IndexComponents::Empty),
        )
    }
}

#[test]
fn recent_hidden_tombstones_are_polled_before_the_entire_prefix_is_consumed() {
    for count in [128, 512] {
        let tier = tombstones(count);
        for reverse in [false, true] {
            let mut actual = cursor(&tier, reverse);
            let mut polls = 0;
            assert!(
                actual
                    .next_with_poll(&mut || {
                        polls += 1;
                        Ok(polls <= 32)
                    })
                    .unwrap()
                    .is_none()
            );
            assert_eq!(polls, 33);
            assert!(actual.stats().datoms_examined <= 32);
            assert!(actual.stats().datoms_examined < count as u64);
            assert_eq!(actual.stats().datoms_yielded, 0);
            assert!(
                actual
                    .next_with_poll(&mut || panic!("stopped cursor must fuse"))
                    .unwrap()
                    .is_none()
            );
            assert!(actual.next().is_none());
            let mut full = cursor(&tier, reverse);
            assert!(full.next().is_none());
            assert_eq!(full.stats().datoms_examined, count as u64);
        }
    }
}

#[test]
fn recent_current_group_interruption_never_publishes_partial_truth() {
    let mut prior = [0; 32];
    let mut transactions = Vec::new();
    for offset in 0..128 {
        let t = 11 + offset;
        let transaction = transaction(
            t,
            prior,
            vec![Datom {
                entity: make_eid(USER_PARTITION, 1).unwrap(),
                attribute: 1_000,
                value: Value::Long(7),
                tx: t_to_tx(t).unwrap(),
                added: offset % 2 == 0,
            }],
        );
        prior = transaction_hash(&encode_transaction(&transaction).unwrap());
        transactions.push(transaction);
    }
    let tier = RecentTier::new(
        "polling",
        10,
        [0; 32],
        transactions,
        EndpointProjection::new(schema(true)),
        RecentLimits::default(),
    )
    .unwrap();
    for reverse in [false, true] {
        for fail in [false, true] {
            let mut actual = cursor(&tier, reverse);
            let mut polls = 0;
            let result = actual.next_with_poll(&mut || {
                polls += 1;
                if polls <= 32 {
                    Ok(true)
                } else if fail {
                    Err(SemanticError::new(
                        ErrorCategory::Busy,
                        "test/poll-limit",
                        "bounded read",
                    ))
                } else {
                    Ok(false)
                }
            });
            if fail {
                assert_eq!(result.unwrap_err().code, "test/poll-limit");
            } else {
                assert!(result.unwrap().is_none());
            }
            assert_eq!(polls, 33);
            assert!(actual.stats().datoms_examined <= 32);
            assert_eq!(actual.stats().datoms_yielded, 0);
            assert!((1..=32).contains(&actual.stats().max_group_datoms));
            assert!(actual.current_output.is_empty());
            assert!(actual.pending.is_none());
            assert!(
                actual
                    .next_with_poll(&mut || panic!("interrupted group must fuse"))
                    .unwrap()
                    .is_none()
            );
        }
        let mut complete = cursor(&tier, reverse);
        assert!(complete.next().is_none());
        assert_eq!(complete.stats().datoms_examined, 128);
        assert_eq!(complete.stats().max_group_datoms, 128);
    }
}

#[test]
fn recent_index_membership_rejections_remain_cooperative() {
    let tier = tombstones(512);
    // A schema change can leave old AVET nodes shared while its current
    // projection disables membership. No data is rewritten for this view.
    let mut actual = RecentCursor::new_prefix(
        tier.indexes.get(IndexOrder::Avet),
        Arc::new(EndpointProjection::new(schema(false))),
        true,
        IndexPrefix::Avet {
            attribute: 1_000,
            value: None,
            entity: None,
        },
    );
    let mut polls = 0;
    assert!(
        actual
            .next_with_poll(&mut || {
                polls += 1;
                Ok(polls <= 32)
            })
            .unwrap()
            .is_none()
    );
    assert_eq!(polls, 33);
    assert!(actual.stats().datoms_examined <= 32);
    assert!(
        actual
            .next_with_poll(&mut || panic!("rejected-member cursor must fuse"))
            .unwrap()
            .is_none()
    );
}
