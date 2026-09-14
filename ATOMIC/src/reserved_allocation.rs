//! Retained high-water state for partition-zero entity issuance.
//!
//! This is allocation history, not derived schema state: an acknowledged
//! tempid or a typed reference can reserve an identity without leaving any
//! current entity facts. Callers retain this state through immutable values,
//! canonical replay, and physical excision. The ordinary frontier remains an
//! upper bound, but is never the starting point for reserved allocation.

use crate::{
    DB_PARTITION, Datom, DurableTransaction, INITIAL_EIDX_FRONTIER, MAX_EIDX, SemanticError, Value,
    eid_to_eidx, eid_to_part, make_eid,
};

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) struct ReservedAllocation {
    frontier: u64,
}

impl ReservedAllocation {
    pub(crate) const fn initial() -> Self {
        Self {
            frontier: INITIAL_EIDX_FRONTIER,
        }
    }

    /// Reconstitute an authenticated current checkpoint.
    pub(crate) fn from_frontier(
        frontier: u64,
        ordinary_frontier: u64,
    ) -> Result<Self, SemanticError> {
        crate::model::identity::validate_frontier(frontier)?;
        crate::model::identity::validate_frontier(ordinary_frontier)?;
        if frontier > ordinary_frontier {
            return Err(SemanticError::incorrect(
                "allocation/reserved-frontier-out-of-range",
                "reserved allocation frontier exceeds the ordinary issued frontier",
            ));
        }
        Ok(Self { frontier })
    }

    pub(crate) const fn frontier(self) -> u64 {
        self.frontier
    }

    /// Reserve every previously observed partition-zero ID, including ones
    /// above schema/partition installation capacity. Clamping would permit
    /// reuse or hide genuine exhaustion.
    pub(crate) fn observe_entity(&mut self, entity: u64) -> Result<(), SemanticError> {
        if eid_to_part(entity)? == DB_PARTITION {
            self.frontier = self.frontier.max(eid_to_eidx(entity)? + 1);
        }
        Ok(())
    }

    pub(crate) fn observe_attribute(&mut self, attribute: u32) -> Result<(), SemanticError> {
        self.observe_entity(u64::from(attribute))
    }

    /// Only typed references are identities. Numeric values, including tuple
    /// numeric slots, must not consume reserved IDs. The explicit stack keeps
    /// programmatically constructed nested values off the Rust call stack.
    pub(crate) fn observe_value(&mut self, value: &Value) -> Result<(), SemanticError> {
        let mut next = *self;
        let mut pending = Vec::new();
        let mut current = Some(value);
        loop {
            if let Some(value) = current.take() {
                match value {
                    Value::Ref(entity) => next.observe_entity(*entity)?,
                    Value::Tuple(slots) => pending.push(slots.iter().flatten()),
                    _ => {}
                }
            }
            loop {
                let Some(values) = pending.last_mut() else {
                    *self = next;
                    return Ok(());
                };
                if let Some(value) = values.next() {
                    current = Some(value);
                    break;
                }
                pending.pop();
            }
        }
    }

    pub(crate) fn observe_datoms<'a>(
        &mut self,
        datoms: impl IntoIterator<Item = &'a Datom>,
    ) -> Result<(), SemanticError> {
        let mut next = *self;
        for datom in datoms {
            next.observe_entity(datom.entity)?;
            next.observe_attribute(datom.attribute)?;
            next.observe_value(&datom.value)?;
        }
        *self = next;
        Ok(())
    }

    /// Replay includes receipt-only allocation witnesses, not just datoms.
    pub(crate) fn observe_transaction(
        &mut self,
        transaction: &DurableTransaction,
    ) -> Result<(), SemanticError> {
        let mut next = *self;
        next.observe_datoms(&transaction.tx_data)?;
        for entity in transaction.tempids.values() {
            next.observe_entity(*entity)?;
        }
        *self = next;
        Ok(())
    }

    /// Issue from the retained reserved cursor. Schema/named-partition range
    /// checks remain with their owning validators. Before publication callers
    /// raise the ordinary frontier to at least `self.frontier()`.
    pub(crate) fn allocate(&mut self) -> Result<u64, SemanticError> {
        if self.frontier > MAX_EIDX {
            return Err(SemanticError::incorrect(
                "allocation/reserved-exhausted",
                "reserved entity-index space is exhausted",
            ));
        }
        let entity = make_eid(DB_PARTITION, self.frontier)?;
        self.frontier += 1;
        Ok(entity)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{DB_TX_INSTANT, USER_PARTITION, t_to_tx};

    #[test]
    fn reserved_cursor_is_independent_of_ordinary_growth() {
        let mut state = ReservedAllocation::from_frontier(1_000, 2_000_000).unwrap();
        assert_eq!(state.allocate().unwrap(), 1_000);
        assert_eq!(state.allocate().unwrap(), 1_001);
        assert_eq!(state.frontier(), 1_002);
        assert!(ReservedAllocation::from_frontier(state.frontier(), 2_000_000).is_ok());
    }

    #[test]
    fn observations_include_attributes_and_nested_refs_not_numeric_values() {
        let mut state = ReservedAllocation::initial();
        let datom = Datom {
            entity: make_eid(USER_PARTITION, 20_000).unwrap(),
            attribute: 1_100,
            value: Value::Tuple(vec![
                None,
                Some(Value::Long(900_000)),
                Some(Value::Tuple(vec![Some(Value::Ref(1_200))])),
            ]),
            tx: t_to_tx(1).unwrap(),
            added: false,
        };
        state.observe_datoms([&datom]).unwrap();
        assert_eq!(state.frontier(), 1_201);
        state.observe_entity(1_300).unwrap();
        assert_eq!(state.allocate().unwrap(), 1_301);
    }

    #[test]
    fn receipt_only_ids_survive_an_empty_material_change() {
        let tx = t_to_tx(1).unwrap();
        let transaction = DurableTransaction {
            database_id: "test".into(),
            basis_t: 1,
            previous_hash: [0; 32],
            eidx_frontier: 50_000,
            tempids: [
                ("reserved".into(), 1_750),
                ("user".into(), make_eid(USER_PARTITION, 40_000).unwrap()),
                ("tx".into(), tx),
            ]
            .into_iter()
            .collect(),
            tx_data: vec![Datom {
                entity: tx,
                attribute: DB_TX_INSTANT as u32,
                value: Value::Instant(1),
                tx,
                added: true,
            }],
        };
        let mut state = ReservedAllocation::initial();
        state.observe_transaction(&transaction).unwrap();
        assert_eq!(state.allocate().unwrap(), 1_751);
    }

    #[test]
    fn maximum_identity_is_retained_without_wrap_or_reuse() {
        let mut state = ReservedAllocation::from_frontier(MAX_EIDX, MAX_EIDX + 1).unwrap();
        assert_eq!(state.allocate().unwrap(), MAX_EIDX);
        assert_eq!(state.frontier(), MAX_EIDX + 1);
        assert_eq!(
            state.allocate().unwrap_err().code,
            "allocation/reserved-exhausted"
        );
        assert_eq!(state.frontier(), MAX_EIDX + 1);
    }

    #[test]
    fn invalid_observations_and_checkpoints_do_not_change_state() {
        let mut state = ReservedAllocation::initial();
        let invalid = Value::Tuple(vec![Some(Value::Ref(1_500)), Some(Value::Ref(u64::MAX))]);
        assert!(state.observe_value(&invalid).is_err());
        assert_eq!(state, ReservedAllocation::initial());
        assert_eq!(state, ReservedAllocation::initial());
        assert!(ReservedAllocation::from_frontier(1_001, 1_000).is_err());
        assert!(ReservedAllocation::from_frontier(999, 1_000).is_err());
    }

    #[test]
    fn nested_observation_uses_a_bounded_call_stack() {
        std::thread::Builder::new()
            .stack_size(128 * 1024)
            .spawn(|| {
                let mut value = Value::Ref(1_750);
                for _ in 0..8_192 {
                    value = Value::Tuple(vec![Some(value)]);
                }
                let mut state = ReservedAllocation::initial();
                state.observe_value(&value).unwrap();
                assert_eq!(state.frontier(), 1_751);
                // These intentionally non-storable nested tuples test only
                // observation. Tear down the test's owned input iteratively.
                while let Value::Tuple(mut slots) = value {
                    value = slots.pop().unwrap().unwrap();
                }
            })
            .unwrap()
            .join()
            .unwrap();
    }
}
