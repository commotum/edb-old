//! Shared transaction-local partition policy. No entity IDs are allocated here.
use crate::{EntityRef, Schema, SemanticError, TxOp, TxValue, eid_to_eidx, eid_to_part};
use std::collections::{BTreeMap, BTreeSet};

enum Affinity {
    Temp(String),
    Bits(u32),
}

pub(crate) struct PartitionPolicy {
    forced: BTreeMap<String, u32>,
    affinity: BTreeMap<String, Affinity>,
    system: BTreeSet<String>,
    resolved: BTreeMap<String, u32>,
}

impl PartitionPolicy {
    pub(crate) fn new(
        ops: &[TxOp],
        names: &BTreeSet<String>,
        schema: &Schema,
        mut resolve: impl FnMut(&EntityRef) -> Result<u64, SemanticError>,
    ) -> Result<Self, SemanticError> {
        let mut result = Self {
            forced: BTreeMap::new(),
            affinity: BTreeMap::new(),
            system: BTreeSet::new(),
            resolved: BTreeMap::new(),
        };
        for op in ops {
            match op {
                TxOp::Add {
                    entity: EntityRef::Temp(name),
                    attribute,
                    ..
                } if matches!(
                    u64::from(*attribute),
                    crate::DB_VALUE_TYPE
                        | crate::DB_CARDINALITY
                        | crate::DB_UNIQUE
                        | crate::DB_IS_COMPONENT
                        | crate::DB_INDEX
                        | crate::DB_NO_HISTORY
                        | crate::DB_FULLTEXT
                        | crate::DB_TUPLE_TYPE
                        | crate::DB_TUPLE_TYPES
                        | crate::DB_TUPLE_ATTRS
                        | crate::DB_ATTR_PREDS
                        | crate::DB_TUPLE_DISCONTINUED
                ) =>
                {
                    result.system.insert(name.clone());
                }
                TxOp::Add {
                    attribute,
                    value: TxValue::Entity(EntityRef::Temp(name)),
                    ..
                } if matches!(
                    u64::from(*attribute),
                    crate::DB_INSTALL_PARTITION
                        | crate::DB_INSTALL_ATTRIBUTE
                        | crate::DB_ALTER_ATTRIBUTE
                ) =>
                {
                    result.system.insert(name.clone());
                }
                _ => {}
            }
        }
        for op in ops {
            match op {
                TxOp::ForcePartition { tempid, partition } => {
                    require_tempid(names, tempid)?;
                    let bits = partition_bits(resolve(partition)?)?;
                    schema.validate_partition_bits(bits)?;
                    if result.system.contains(tempid) {
                        continue;
                    }
                    if result
                        .forced
                        .insert(tempid.clone(), bits)
                        .is_some_and(|prior| prior != bits)
                    {
                        return Err(conflict("one tempid requests distinct forced partitions"));
                    }
                }
                TxOp::MatchPartition { tempid, entity } => {
                    require_tempid(names, tempid)?;
                    let affinity = if let EntityRef::Temp(name) = entity {
                        require_tempid(names, name)?;
                        Affinity::Temp(name.clone())
                    } else {
                        Affinity::Bits(eid_to_part(resolve(entity)?)?)
                    };
                    if result.system.contains(tempid) {
                        continue;
                    }
                    if let Some(prior) = result.affinity.get(tempid) {
                        let equal = match (prior, &affinity) {
                            (Affinity::Temp(a), Affinity::Temp(b)) => a == b,
                            (Affinity::Bits(a), Affinity::Bits(b)) => a == b,
                            _ => false,
                        };
                        if !equal {
                            return Err(conflict(
                                "one tempid requests distinct partition affinities",
                            ));
                        }
                    } else {
                        result.affinity.insert(tempid.clone(), affinity);
                    }
                }
                _ => {}
            }
        }
        // Check policies even when upsert later makes allocation unnecessary.
        for name in names {
            if result.system.contains(name)
                || result.forced.contains_key(name)
                || result.affinity.contains_key(name)
            {
                result.resolve_partition(name)?;
            }
        }
        Ok(result)
    }

    fn resolve_partition(&mut self, name: &str) -> Result<u32, SemanticError> {
        let mut seen = BTreeSet::new();
        let mut path = Vec::new();
        let mut current = name;
        // Cache the whole traversed suffix, so a long chain is visited once
        // rather than once per tempid. Iteration also bounds stack usage.
        let bits = loop {
            if let Some(bits) = self.resolved.get(current) {
                break *bits;
            }
            if self.system.contains(current) {
                break crate::DB_PARTITION;
            }
            if let Some(bits) = self.forced.get(current) {
                break *bits;
            }
            path.push(current);
            if !seen.insert(current) {
                return Err(SemanticError::incorrect(
                    "transaction/partition-affinity-cycle",
                    "partition affinity contains an unanchored cycle",
                ));
            }
            match self.affinity.get(current) {
                Some(Affinity::Temp(next)) => {
                    current = next;
                }
                // Recovered matching excludes reserved system/tx targets.
                Some(Affinity::Bits(bits)) if *bits >= crate::USER_PARTITION => break *bits,
                _ => break crate::USER_PARTITION,
            }
        };
        for name in path {
            self.resolved.insert(name.to_owned(), bits);
        }
        self.resolved.insert(name.to_owned(), bits);
        Ok(bits)
    }

    pub(crate) fn explicit_partition(&self, name: &str) -> Result<Option<u32>, SemanticError> {
        if self.system.contains(name)
            || self.forced.contains_key(name)
            || self.affinity.contains_key(name)
        {
            Ok(Some(
                *self
                    .resolved
                    .get(name)
                    .expect("every explicit partition policy was resolved"),
            ))
        } else {
            Ok(None)
        }
    }
}

fn require_tempid(names: &BTreeSet<String>, name: &str) -> Result<(), SemanticError> {
    if names.contains(name) {
        Ok(())
    } else {
        Err(SemanticError::incorrect(
            "transaction/partition-tempid-not-an-entity",
            format!("partition policy tempid {name} has no entity position"),
        ))
    }
}

pub(crate) fn partition_bits(entity: u64) -> Result<u32, SemanticError> {
    let bits = eid_to_part(entity)?;
    let index = eid_to_eidx(entity)?;
    if bits == crate::DB_PARTITION {
        u32::try_from(index)
            .ok()
            .filter(|bits| *bits <= crate::MAX_PARTITION)
            .ok_or_else(|| {
                SemanticError::incorrect(
                    "transaction/not-a-partition",
                    "partition entity is outside partition bit range",
                )
            })
    } else if index == 0 {
        Ok(bits)
    } else {
        Err(SemanticError::incorrect(
            "transaction/not-a-partition",
            "partition must name a system entity or an implicit partition base",
        ))
    }
}

fn conflict(message: &str) -> SemanticError {
    SemanticError::conflict("transaction/partition-conflict", message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{Attribute, Cardinality, Database, DurableTransaction, Keyword, Value, ValueType};

    #[test]
    fn long_affinity_chains_resolve_once_without_recursive_stack_growth() {
        let names: BTreeSet<_> = (0..4096).map(|index| format!("p{index:04}")).collect();
        let ordered: Vec<_> = names.iter().cloned().collect();
        let mut ops: Vec<_> = ordered
            .windows(2)
            .map(|pair| TxOp::MatchPartition {
                tempid: pair[0].clone(),
                entity: EntityRef::Temp(pair[1].clone()),
            })
            .collect();
        let partition = crate::implicit_part(500).unwrap();
        ops.push(TxOp::ForcePartition {
            tempid: ordered.last().unwrap().clone(),
            partition: EntityRef::Id(partition),
        });
        let policy = PartitionPolicy::new(&ops, &names, &Schema::new(), |entity| match entity {
            EntityRef::Id(entity) => Ok(*entity),
            _ => unreachable!(),
        })
        .unwrap();
        assert_eq!(policy.resolved.len(), names.len());
        for name in names {
            assert_eq!(
                policy.explicit_partition(&name).unwrap(),
                Some(eid_to_part(partition).unwrap())
            );
        }
    }

    #[test]
    fn later_partition_install_uses_the_already_captured_ident() {
        let before = Database::new(Schema::new()).unwrap();
        let named = before
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("p".into()),
                        attribute: crate::DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("partition-test", "later")).into(),
                    },
                    TxOp::ForcePartition {
                        tempid: "p".into(),
                        partition: EntityRef::Id(crate::DB_PART_DB),
                    },
                ],
                1,
            )
            .unwrap();
        let install = [TxOp::Add {
            entity: EntityRef::Id(crate::DB_PART_DB),
            attribute: crate::DB_INSTALL_PARTITION as u32,
            value: Value::Ref(named.tempids["p"]).into(),
        }];
        let eager = named.db_after.with(&install, 2).unwrap();
        let native = named.db_after.database_value().with(&install, 2).unwrap();
        assert_eq!(native.db_after.schema(), eager.db_after.schema());
        assert_eq!(native.tx_data, eager.tx_data);
        assert!(
            native
                .db_after
                .schema()
                .partitions()
                .any(|(id, _)| u64::from(id) == named.tempids["p"])
        );
    }

    #[test]
    fn recovery_checks_partition_membership_and_one_global_allocation_sequence() {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("partition-test", "value"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
        schema
            .install(
                Attribute::new(
                    1_002,
                    Keyword::new("partition-test", "tuple"),
                    ValueType::Tuple,
                    Cardinality::One,
                )
                .tuple(crate::TupleSpec::Homogeneous(ValueType::Ref)),
            )
            .unwrap();
        schema
            .install(Attribute::new(
                1_001,
                Keyword::new("partition-test", "ref"),
                ValueType::Ref,
                Cardinality::One,
            ))
            .unwrap();
        let before = Database::new(schema).unwrap();
        let ops: Vec<_> = ["a", "b"]
            .into_iter()
            .enumerate()
            .flat_map(|(index, name)| {
                [
                    TxOp::Add {
                        entity: EntityRef::Temp(name.into()),
                        attribute: 1_000,
                        value: Value::Long(index as i64).into(),
                    },
                    TxOp::ForcePartition {
                        tempid: name.into(),
                        partition: EntityRef::Id(crate::implicit_part(index as u32).unwrap()),
                    },
                ]
            })
            .chain([
                TxOp::Add {
                    entity: EntityRef::Temp("a".into()),
                    attribute: 1_001,
                    value: Value::Ref(crate::DB_PART_USER).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("a".into()),
                    attribute: 1_002,
                    value: Value::Tuple(vec![Some(Value::Ref(crate::DB_PART_USER)), None]).into(),
                },
            ])
            .collect();
        let report = before.with(&ops, 10).unwrap();
        let transaction = DurableTransaction {
            database_id: "partition-recovery".into(),
            basis_t: report.db_after.basis_t(),
            previous_hash: [0; 32],
            eidx_frontier: report.db_after.eidx_frontier(),
            tempids: report.tempids,
            tx_data: report.tx_data,
        };
        before
            .apply_committed(&transaction)
            .unwrap()
            .validate_invariants()
            .unwrap();
        let lineage = crate::log_generation::LineageTransactionContent::from_transaction(
            "01234567-89ab-4def-8123-456789abcdef",
            before.eidx_frontier(),
            &transaction,
        )
        .unwrap();
        let decoded =
            crate::log_generation::LineageTransactionContent::decode(&lineage.encode().unwrap())
                .unwrap();
        before
            .apply_committed(&decoded.to_transaction([0; 32]))
            .unwrap()
            .validate_invariants()
            .unwrap();

        let mut duplicate = transaction.clone();
        duplicate.tempids.insert(
            "b".into(),
            crate::make_eid(
                eid_to_part(transaction.tempids["b"]).unwrap(),
                eid_to_eidx(transaction.tempids["a"]).unwrap(),
            )
            .unwrap(),
        );
        assert_eq!(
            before.apply_committed(&duplicate).unwrap_err().code,
            "recovery/duplicate-tempid-index"
        );

        let mut unknown = transaction.clone();
        unknown.tempids.insert(
            "a".into(),
            crate::make_eid(777, eid_to_eidx(transaction.tempids["a"]).unwrap()).unwrap(),
        );
        assert_eq!(
            before.apply_committed(&unknown).unwrap_err().code,
            "recovery/invalid-tempid-partition"
        );

        let mut reference = transaction.clone();
        reference
            .tx_data
            .iter_mut()
            .find(|datom| datom.attribute == 1_001)
            .unwrap()
            .value = Value::Ref(crate::make_eid(777, 42).unwrap());
        assert_eq!(
            before.apply_committed(&reference).unwrap_err().code,
            "kernel/invalid-stored-partition"
        );

        let unwitnessed = crate::make_eid(
            eid_to_part(crate::implicit_part(2).unwrap()).unwrap(),
            eid_to_eidx(transaction.tempids["a"]).unwrap(),
        )
        .unwrap();
        for (attribute, value) in [
            (1_001, Value::Ref(unwitnessed)),
            (
                1_002,
                Value::Tuple(vec![Some(Value::Ref(unwitnessed)), None]),
            ),
        ] {
            let mut hidden = transaction.clone();
            hidden
                .tx_data
                .iter_mut()
                .find(|datom| datom.attribute == attribute)
                .unwrap()
                .value = value;
            assert_eq!(
                before.apply_committed(&hidden).unwrap_err().code,
                "recovery/unwitnessed-entity-allocation"
            );
        }
        let mut hidden_entity = transaction;
        hidden_entity
            .tx_data
            .iter_mut()
            .find(|datom| datom.attribute == 1_000)
            .unwrap()
            .entity = unwitnessed;
        hidden_entity
            .tx_data
            .sort_by(|a, b| a.cmp_in(b, crate::IndexOrder::Eavt));
        assert_eq!(
            before.apply_committed(&hidden_entity).unwrap_err().code,
            "recovery/unwitnessed-entity-allocation"
        );
    }
}
