//! Allocation metadata is authenticated by the exact transaction hash, not by
//! a mutable head or an unauthenticated list of currently occupied schema IDs.
use crate::log_generation::LineageTransactionContent;
use crate::peer::ExactEndpoint;
use crate::postgres::{
    postgres_error, read_authenticated_log_range, visit_authenticated_log_range,
};
use crate::reserved_allocation::ReservedAllocation;
use crate::sql_io::GenericClient;
use crate::{
    DB_EXCISE, DB_EXCISE_BEFORE, DB_EXCISE_BEFORE_T, DB_TX_INSTANT, Database, ErrorCategory,
    SemanticError, Value, decode_genesis, sha256,
};
use std::collections::{BTreeMap, BTreeSet};

fn fault(code: &'static str, message: &'static str) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

/// Retains only cutoff metadata for completed requests. Date cutoffs use an
/// ordered sweep, not a transaction-by-request nested scan.
#[derive(Default)]
struct LegacyExcisionProof {
    pending: BTreeSet<(u64, u64)>,
    owners: BTreeSet<u64>,
    values: BTreeMap<(u64, u32), Value>,
    before_t: u64,
    dated: BTreeSet<(u64, i64)>,
    dates: BTreeMap<i64, usize>,
}

impl LegacyExcisionProof {
    fn new(pending: BTreeSet<(u64, u64)>) -> Self {
        Self {
            owners: pending.iter().map(|(_, e)| *e).collect(),
            pending,
            ..Self::default()
        }
    }
    fn observe(&mut self, transaction: &crate::DurableTransaction) -> Result<(), SemanticError> {
        for datom in &transaction.tx_data {
            if !self.owners.contains(&datom.entity)
                || ![DB_EXCISE_BEFORE as u32, DB_EXCISE_BEFORE_T as u32].contains(&datom.attribute)
            {
                continue;
            }
            let key = (datom.entity, datom.attribute);
            if datom.added {
                self.values.insert(key, datom.value.clone());
            } else if self.values.get(&key) == Some(&datom.value) {
                self.values.remove(&key);
            }
        }
        for datom in &transaction.tx_data {
            if datom.added
                && datom.attribute == DB_EXCISE as u32
                && self.pending.remove(&(transaction.basis_t, datom.entity))
            {
                let t = transaction.basis_t;
                let before_t = self.values.get(&(datom.entity, DB_EXCISE_BEFORE_T as u32));
                let before = self.values.get(&(datom.entity, DB_EXCISE_BEFORE as u32));
                match (before_t, before) {
                    (None, None) => self.before_t = self.before_t.max(t),
                    (Some(Value::Long(value)), None) => {
                        self.before_t = self
                            .before_t
                            .max(t.min(crate::database::normalize_excision_before_t(*value)?));
                    }
                    (None, Some(Value::Instant(instant))) => {
                        if self.dated.insert((t, *instant)) {
                            *self.dates.entry(*instant).or_default() += 1;
                        }
                    }
                    _ => {
                        return Err(fault(
                            "allocation/excision-cutoff",
                            "completed excision has invalid protected cutoff facts",
                        ));
                    }
                }
            }
        }
        Ok(())
    }
    fn has_cutoffs(&self) -> bool {
        self.before_t > 0 || !self.dated.is_empty()
    }
    fn includes(&mut self, transaction: &crate::DurableTransaction) -> Result<bool, SemanticError> {
        while self
            .dated
            .first()
            .is_some_and(|(t, _)| *t <= transaction.basis_t)
        {
            let (_, date) = self.dated.pop_first().expect("present");
            let count = self.dates.get_mut(&date).expect("date count");
            *count -= 1;
            if *count == 0 {
                self.dates.remove(&date);
            }
        }
        if transaction.basis_t < self.before_t {
            return Ok(true);
        }
        let Some((&before, _)) = self.dates.last_key_value() else {
            return Ok(false);
        };
        let instant = transaction
            .tx_data
            .iter()
            .find_map(|d| {
                if d.added && d.attribute == DB_TX_INSTANT as u32 {
                    if let Value::Instant(t) = d.value {
                        return Some(t);
                    }
                }
                None
            })
            .ok_or_else(|| {
                fault(
                    "allocation/excision-clock",
                    "legacy cutoff proof lacks transaction time",
                )
            })?;
        Ok(instant < before)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{Datom, DurableTransaction, t_to_tx};

    fn transaction(t: u64, instant: i64, facts: &[(u64, u32, Value, bool)]) -> DurableTransaction {
        let tx = t_to_tx(t).unwrap();
        let mut tx_data = vec![Datom {
            entity: tx,
            attribute: DB_TX_INSTANT as u32,
            value: Value::Instant(instant),
            tx,
            added: true,
        }];
        tx_data.extend(facts.iter().map(|(e, a, v, added)| Datom {
            entity: *e,
            attribute: *a,
            value: v.clone(),
            tx,
            added: *added,
        }));
        DurableTransaction {
            database_id: "proof".into(),
            basis_t: t,
            previous_hash: [0; 32],
            eidx_frontier: 1000 + t * 100_000,
            tempids: BTreeMap::new(),
            tx_data,
        }
    }

    #[test]
    fn late_excision_with_early_cutoff_does_not_reserve_later_growth() {
        let mut proof = LegacyExcisionProof::new(BTreeSet::from([(10, 200)]));
        proof
            .observe(&transaction(
                1,
                1,
                &[(200, DB_EXCISE_BEFORE_T as u32, Value::Long(2), true)],
            ))
            .unwrap();
        proof
            .observe(&transaction(
                10,
                10,
                &[(200, DB_EXCISE as u32, Value::Ref(201), true)],
            ))
            .unwrap();
        // A later edit of request metadata cannot reinterpret its frozen cutoff.
        proof
            .observe(&transaction(
                11,
                11,
                &[(200, DB_EXCISE_BEFORE_T as u32, Value::Long(11), true)],
            ))
            .unwrap();
        assert!(proof.pending.is_empty());
        assert!(proof.includes(&transaction(1, 1, &[])).unwrap());
        assert!(!proof.includes(&transaction(2, 2, &[])).unwrap());
        assert!(!proof.includes(&transaction(9, 9, &[])).unwrap());
    }

    #[test]
    fn instant_cutoff_union_is_exclusive_and_capped_by_request_basis() {
        let mut proof = LegacyExcisionProof::new(BTreeSet::from([(3, 200), (5, 201)]));
        proof
            .observe(&transaction(
                3,
                30,
                &[
                    (200, DB_EXCISE_BEFORE as u32, Value::Instant(50), true),
                    (200, DB_EXCISE as u32, Value::Ref(300), true),
                ],
            ))
            .unwrap();
        proof
            .observe(&transaction(
                5,
                50,
                &[
                    (201, DB_EXCISE_BEFORE as u32, Value::Instant(20), true),
                    (201, DB_EXCISE as u32, Value::Ref(301), true),
                ],
            ))
            .unwrap();
        assert!(proof.includes(&transaction(1, 10, &[])).unwrap());
        assert!(proof.includes(&transaction(2, 20, &[])).unwrap());
        // Request 3 no longer applies, and request 5's time bound is exclusive.
        assert!(!proof.includes(&transaction(3, 30, &[])).unwrap());
        assert!(!proof.includes(&transaction(5, 50, &[])).unwrap());
    }
}

/// Gen0 retains its old allocator until an explicit generation conversion.
/// Native v2 endpoints need one terminal envelope; v1 endpoints require a
/// streamed, one-time legacy proof before their next v2 commit checkpoints it.
pub(crate) fn load_reserved_allocation<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    endpoint: ExactEndpoint,
) -> Result<Option<ReservedAllocation>, SemanticError> {
    if endpoint.generation == 0 {
        return Ok(None);
    }
    let generation = i64::try_from(endpoint.generation).map_err(|_| {
        fault(
            "allocation/generation",
            "allocation generation exceeds SQL range",
        )
    })?;
    let basis = i64::try_from(endpoint.basis_t)
        .map_err(|_| fault("allocation/basis", "allocation basis exceeds SQL range"))?;
    if endpoint.basis_t > 0 {
        let row = client.query_opt(
            "SELECT previous_hash FROM atomic_generation_transactions WHERE database_id=$1 AND generation=$2 AND basis_t=$3",
            &[&database_id, &generation, &basis],
        ).map_err(|e| postgres_error("postgres/allocation-terminal", e))?
            .ok_or_else(|| fault("allocation/missing-terminal", "allocation endpoint has no canonical transaction"))?;
        let previous: Vec<u8> = row.get(0);
        let previous = previous
            .try_into()
            .map_err(|_| fault("allocation/hash", "invalid allocation predecessor hash"))?;
        let mut terminal = read_authenticated_log_range(
            client,
            database_id,
            endpoint.generation,
            endpoint.basis_t - 1,
            endpoint.basis_t,
            previous,
        )?;
        let terminal = terminal.pop().expect("authenticated exact single row");
        if terminal.tx_hash != endpoint.tx_hash
            || terminal.state_hash != endpoint.state_hash
            || terminal.transaction.eidx_frontier != endpoint.eidx_frontier
        {
            return Err(fault(
                "allocation/endpoint-mismatch",
                "allocation content does not match the immutable endpoint",
            ));
        }
        let content = LineageTransactionContent::decode(&terminal.payload)?;
        if let Some(frontier) = content.reserved_frontier {
            return Ok(Some(ReservedAllocation::from_frontier(
                frontier,
                endpoint.eidx_frontier,
            )?));
        }
    }
    let row = client
        .query_opt(
            "SELECT genesis, genesis_hash FROM atomic_databases WHERE database_id=$1",
            &[&database_id],
        )
        .map_err(|e| postgres_error("postgres/allocation-genesis", e))?
        .ok_or_else(|| {
            fault(
                "allocation/missing-genesis",
                "allocation proof has no genesis",
            )
        })?;
    let genesis: Vec<u8> = row.get(0);
    let stored_hash: Vec<u8> = row.get(1);
    let genesis_hash = sha256(&genesis);
    if stored_hash != genesis_hash {
        return Err(fault(
            "allocation/genesis-hash",
            "allocation genesis checksum mismatch",
        ));
    }
    let database = Database::from_genesis(decode_genesis(&genesis)?)?;
    let rows = client.query("SELECT request_t, request_entity FROM atomic_completed_excision_requests WHERE database_id=$1 AND generation=$2 ORDER BY request_t, request_entity", &[&database_id, &generation])
        .map_err(|e| postgres_error("postgres/allocation-excision", e))?;
    let mut excisions = LegacyExcisionProof::new(
        rows.into_iter()
            .map(|row| {
                let t = u64::try_from(row.get::<_, i64>(0)).map_err(|_| {
                    fault(
                        "allocation/excision-basis",
                        "invalid completed excision basis",
                    )
                })?;
                let entity = u64::try_from(row.get::<_, i64>(1)).map_err(|_| {
                    fault(
                        "allocation/excision-entity",
                        "invalid completed excision entity",
                    )
                })?;
                Ok((t, entity))
            })
            .collect::<Result<BTreeSet<_>, SemanticError>>()?,
    );
    let mut reserved = ReservedAllocation::initial();
    reserved.observe_datoms(&database.datoms(crate::View::History, crate::IndexOrder::Eavt))?;
    let mut last_hash = genesis_hash;
    let mut ordinary_frontier = database.eidx_frontier();
    visit_authenticated_log_range(
        client,
        database_id,
        endpoint.generation,
        0,
        endpoint.basis_t,
        genesis_hash,
        |entry| {
            if LineageTransactionContent::decode(&entry.payload)?
                .reserved_frontier
                .is_some()
            {
                return Err(fault(
                    "allocation/version-regression",
                    "a legacy allocation endpoint follows versioned allocation content",
                ));
            }
            reserved.observe_transaction(&entry.transaction)?;
            excisions.observe(&entry.transaction)?;
            ordinary_frontier = entry.transaction.eidx_frontier;
            last_hash = entry.tx_hash;
            Ok(())
        },
    )?;
    if last_hash != endpoint.tx_hash || ordinary_frontier != endpoint.eidx_frontier {
        return Err(fault(
            "allocation/proof-endpoint",
            "allocation proof does not reach the requested immutable endpoint",
        ));
    }
    // Completed physical excision may erase explicit reference-only low IDs.
    // Protected request facts reconstruct the exclusive cutoff. A second,
    // bounded-memory stream reserves only the prefix that could lose witnesses,
    // not today's potentially much larger ordinary frontier. This is solely
    // legacy upgrade work; subsequent v2 commits checkpoint the result.
    if excisions.has_cutoffs() {
        let mut floor = database.eidx_frontier();
        visit_authenticated_log_range(
            client,
            database_id,
            endpoint.generation,
            0,
            endpoint.basis_t,
            genesis_hash,
            |entry| {
                if excisions.includes(&entry.transaction)? {
                    floor = entry.transaction.eidx_frontier;
                }
                Ok(())
            },
        )?;
        reserved.reserve_legacy_excision_floor(floor)?;
    }
    if !excisions.pending.is_empty() {
        if excisions
            .pending
            .iter()
            .any(|(t, _)| *t <= endpoint.basis_t)
        {
            return Err(fault(
                "allocation/excision-proof",
                "completed excision lacks its protected request assertion",
            ));
        }
        // A historical prefix preceding a later legacy excision lacks that
        // cutoff's facts inside its prefix. Keep its whole old issued range
        // reserved rather than guessing erased identities from future data.
        reserved.reserve_legacy_excision_floor(endpoint.eidx_frontier)?;
    }
    Ok(Some(ReservedAllocation::from_frontier(
        reserved.frontier(),
        endpoint.eidx_frontier,
    )?))
}
