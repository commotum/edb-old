//! Reclaimable report continuity across excision generations.
//!
//! The current observer anchor and connected peers own interest tokens; captured
//! database values do not. Handoff g retains the last publication in g and the interest token for
//! g+1, never its own token: a lagging peer keeps intermediate handoffs alive
//! without making the chain retain itself after the peer advances or drops.
use super::root::{Block, DatabaseRoot};
use super::{BatchOutcome, ObjectId, PgBlockStore, RefChange, RefCondition};
use crate::{ErrorCategory, SemanticError};
use std::collections::BTreeMap;

const TOKEN_KIND: u16 = 90;
const HANDOFF_KIND: u16 = 91;
const PREFIX: &str = "handoffs/";
const CURSOR: &str = "ownership/handoff-prune";

#[derive(Clone, Debug, Default)]
pub struct ReportHandoffMaintenance {
    pub examined: usize,
    pub removed: usize,
    pub unsettled: bool,
    pub complete: bool,
}

pub(crate) fn token_bytes(
    route: [u8; 16],
    identity: [u8; 16],
    generation: u64,
) -> Result<Vec<u8>, SemanticError> {
    if route == [0; 16] || identity == [0; 16] {
        return Err(invalid(
            "Report interest requires an issued route and lineage",
        ));
    }
    let mut payload = Vec::with_capacity(40);
    payload.extend_from_slice(&route);
    payload.extend_from_slice(&identity);
    payload.extend_from_slice(&generation.to_be_bytes());
    Block {
        kind: TOKEN_KIND,
        links: Vec::new(),
        payload,
    }
    .encode()
}

pub(crate) fn token_id(
    route: [u8; 16],
    identity: [u8; 16],
    generation: u64,
) -> Result<ObjectId, SemanticError> {
    Ok(crate::sha256(&token_bytes(route, identity, generation)?))
}

pub(crate) fn handoff_key(route: [u8; 16], generation: u64) -> String {
    format!(
        "{PREFIX}{}/{generation:016x}",
        super::engine::identity_string(route)
    )
}

pub(crate) fn observer_key(route: [u8; 16]) -> String {
    format!("observers/{}", super::engine::identity_string(route))
}

pub(crate) fn stage_token(
    store: &mut PgBlockStore,
    route: [u8; 16],
    identity: [u8; 16],
    generation: u64,
) -> Result<ObjectId, SemanticError> {
    if store.write_protection().is_none() {
        return Err(invalid("Report interest staging requires write protection"));
    }
    store.put(&token_bytes(route, identity, generation)?)
}

/// Create/restore prepares this alongside its exact database-root transition.
/// Peers only pin the precreated token: their object privilege remains SELECT.
pub(crate) fn prepare_observer_anchor(
    store: &mut PgBlockStore,
    route: [u8; 16],
    identity: [u8; 16],
    generation: u64,
) -> Result<(RefCondition, RefChange), SemanticError> {
    let key = observer_key(route);
    let previous = store.read_ref(&key)?;
    if previous.as_ref().is_some_and(|r| r.value.is_none()) {
        return Err(SemanticError::conflict(
            "storage/report-handoff-conflict",
            "A retired observer route cannot be reactivated",
        ));
    }
    let id = stage_token(store, route, identity, generation)?;
    Ok((
        RefCondition {
            key: key.clone(),
            expected: previous.map(|r| r.revision),
        },
        RefChange {
            key,
            value: Some(id.to_vec()),
        },
    ))
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct ReportHandoff {
    pub route: [u8; 16],
    pub identity: [u8; 16],
    pub generation: u64,
    pub basis: u64,
    pub publication: ObjectId,
    pub next_token: ObjectId,
}

impl ReportHandoff {
    fn encode(&self) -> Result<Vec<u8>, SemanticError> {
        self.validate()?;
        let mut payload = Vec::with_capacity(48);
        payload.extend_from_slice(&self.route);
        payload.extend_from_slice(&self.identity);
        payload.extend_from_slice(&self.generation.to_be_bytes());
        payload.extend_from_slice(&self.basis.to_be_bytes());
        Block {
            kind: HANDOFF_KIND,
            links: vec![self.publication, self.next_token],
            payload,
        }
        .encode()
    }

    pub(crate) fn decode(id: &ObjectId, bytes: &[u8]) -> Result<Self, SemanticError> {
        // Fixed-size codec admission precedes Block's payload/link allocations.
        if bytes.len() != 20 + 64 + 48 {
            return Err(invalid("Invalid report handoff length"));
        }
        let block = Block::decode(id, bytes)?;
        if block.kind != HANDOFF_KIND || block.links.len() != 2 || block.payload.len() != 48 {
            return Err(invalid("Invalid report handoff shape"));
        }
        let result = Self {
            route: block.payload[..16].try_into().unwrap(),
            identity: block.payload[16..32].try_into().unwrap(),
            generation: u64::from_be_bytes(block.payload[32..40].try_into().unwrap()),
            basis: u64::from_be_bytes(block.payload[40..48].try_into().unwrap()),
            publication: block.links[0],
            next_token: block.links[1],
        };
        result.validate()?;
        Ok(result)
    }

    fn validate(&self) -> Result<(), SemanticError> {
        let next = self
            .generation
            .checked_add(1)
            .ok_or_else(|| invalid("Report generation overflows"))?;
        if self.next_token != token_id(self.route, self.identity, next)? {
            return Err(invalid(
                "Report handoff must retain the next generation's interest token",
            ));
        }
        Ok(())
    }
}

/// Stage under the caller's root/lease/GC write protection, then join the
/// returned reference transition to that exact excision activation batch.
pub(crate) fn prepare_handoff(
    store: &mut PgBlockStore,
    route: [u8; 16],
    identity: [u8; 16],
    generation: u64,
    basis: u64,
    publication: ObjectId,
) -> Result<(Vec<RefCondition>, Vec<RefChange>), SemanticError> {
    if store.write_protection().is_none() {
        return Err(invalid("Report handoff staging requires write protection"));
    }
    let key = handoff_key(route, generation);
    // Neither a previous activation nor its pruned tombstone can be recreated.
    if store.read_ref(&key)?.is_some() {
        return Err(SemanticError::conflict(
            "storage/report-handoff-conflict",
            "Report generation was already handed off",
        ));
    }
    let root = DatabaseRoot::decode(&publication, &required(store, publication)?)?;
    let metadata_id = root
        .metadata
        .ok_or_else(|| invalid("Report publication lacks generation metadata"))?;
    let metadata = super::SnapshotMetadata::decode(&metadata_id, &required(store, metadata_id)?)?;
    if (root.identity, root.basis) != (metadata.identity, metadata.basis)
        || (root.identity, metadata.generation, root.basis) != (identity, generation, basis)
    {
        return Err(invalid("Report handoff differs from its final publication"));
    }
    let next = generation
        .checked_add(1)
        .ok_or_else(|| invalid("Report generation overflows"))?;
    let anchor_key = observer_key(route);
    let anchor = store.read_ref(&anchor_key)?;
    let current_token = token_id(route, identity, generation)?;
    if anchor.as_ref().and_then(|r| r.value.as_deref()) != Some(&current_token[..]) {
        return Err(SemanticError::conflict(
            "storage/report-handoff-conflict",
            "Current report interest anchor differs from the excision generation",
        ));
    }
    let next_token = stage_token(store, route, identity, next)?;
    let id = store.put(
        &ReportHandoff {
            route,
            identity,
            generation,
            basis,
            publication,
            next_token,
        }
        .encode()?,
    )?;
    Ok((
        vec![
            RefCondition {
                key: key.clone(),
                expected: None,
            },
            RefCondition {
                key: anchor_key.clone(),
                expected: anchor.map(|r| r.revision),
            },
        ],
        vec![
            RefChange {
                key,
                value: Some(id.to_vec()),
            },
            RefChange {
                key: anchor_key,
                value: Some(next_token.to_vec()),
            },
        ],
    ))
}

pub(crate) fn prune(
    store: &mut PgBlockStore,
    maximum: usize,
    control: &mut dyn FnMut() -> Result<(), SemanticError>,
) -> Result<ReportHandoffMaintenance, SemanticError> {
    if !(1..=32).contains(&maximum) {
        return Err(SemanticError::incorrect(
            "storage/report-prune-limit",
            "Report handoff pruning pages must contain 1–32 entries",
        ));
    }
    control()?;
    let cursor = store.read_ref(CURSOR)?;
    let after = cursor
        .as_ref()
        .and_then(|r| r.value.as_deref())
        .unwrap_or(&[]);
    let after = std::str::from_utf8(after).map_err(|_| invalid("Invalid report handoff cursor"))?;
    if !after.is_empty() && (!after.starts_with(PREFIX) || after.len() > 1024) {
        return Err(invalid("Invalid report handoff cursor namespace"));
    }
    let page = store.list_live_refs(PREFIX, (!after.is_empty()).then_some(after), maximum)?;
    let mut report = ReportHandoffMaintenance {
        complete: page.len() < maximum,
        ..Default::default()
    };
    let mut guards = BTreeMap::from([(CURSOR.to_owned(), cursor.as_ref().map(|r| r.revision))]);
    let mut changes = Vec::with_capacity(maximum + 1);
    let mut last = String::new();
    for (key, reference) in page {
        control()?;
        let id: ObjectId = reference
            .value
            .as_deref()
            .unwrap()
            .try_into()
            .map_err(|_| invalid("Invalid report handoff reference"))?;
        let handoff = ReportHandoff::decode(&id, &required(store, id)?)?;
        if key != handoff_key(handoff.route, handoff.generation) {
            return Err(invalid("Report handoff differs from its reference key"));
        }
        report.examined += 1;
        let interest = token_id(handoff.route, handoff.identity, handoff.generation)?;
        let Some((count, proof)) = super::ownership::settled_count(store, interest)? else {
            report.unsettled = true;
            report.complete = false;
            report.removed = 0;
            return Ok(report);
        };
        for condition in proof {
            if let Some(old) = guards.insert(condition.key, condition.expected)
                && old != condition.expected
            {
                return Err(stale());
            }
        }
        if count == 0 {
            guards.insert(key.clone(), Some(reference.revision));
            changes.push(RefChange {
                key: key.clone(),
                value: None,
            });
            report.removed += 1;
        }
        last = key;
    }
    changes.push(RefChange {
        key: CURSOR.into(),
        value: Some(if report.complete {
            Vec::new()
        } else {
            last.into_bytes()
        }),
    });
    control()?;
    let guards = guards
        .into_iter()
        .map(|(key, expected)| RefCondition { key, expected })
        .collect::<Vec<_>>();
    match super::ownership::publish_refs(store, &guards, &changes)? {
        BatchOutcome::Applied(_) => Ok(report),
        BatchOutcome::Conflict(_) => Err(stale()),
    }
}

fn required(store: &mut PgBlockStore, id: ObjectId) -> Result<Vec<u8>, SemanticError> {
    store
        .get(id)?
        .ok_or_else(|| invalid("A report handoff object is absent"))
}
fn invalid(message: &str) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, "storage/report-handoff", message)
}
fn stale() -> SemanticError {
    SemanticError::conflict(
        "storage/collector-stale",
        "Report handoff ownership proof changed",
    )
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn interest_tokens_bind_route_lineage_and_generation_without_graph_edges() {
        let id = token_id([1; 16], [2; 16], 3).unwrap();
        for (route, identity, generation) in [
            ([4; 16], [2; 16], 3),
            ([1; 16], [4; 16], 3),
            ([1; 16], [2; 16], 4),
        ] {
            assert_ne!(id, token_id(route, identity, generation).unwrap());
        }
        let token = Block::decode(&id, &token_bytes([1; 16], [2; 16], 3).unwrap()).unwrap();
        assert!(token.links.is_empty());
        assert!(token_id([0; 16], [2; 16], 3).is_err());
    }

    #[test]
    fn handoffs_are_bounded_and_cannot_keep_their_own_interest_alive() {
        let handoff = ReportHandoff {
            route: [1; 16],
            identity: [2; 16],
            generation: 3,
            basis: 7,
            publication: [4; 32],
            next_token: token_id([1; 16], [2; 16], 4).unwrap(),
        };
        let bytes = handoff.encode().unwrap();
        assert_eq!(
            ReportHandoff::decode(&crate::sha256(&bytes), &bytes).unwrap(),
            handoff
        );
        for size in [0, bytes.len() - 1, bytes.len() + 1, 4096] {
            let bad = vec![0; size];
            assert!(ReportHandoff::decode(&crate::sha256(&bad), &bad).is_err());
        }
        let mut own = handoff;
        own.next_token = token_id(own.route, own.identity, own.generation).unwrap();
        assert!(own.encode().is_err());
        let mut forged = Block::decode(&crate::sha256(&bytes), &bytes).unwrap();
        forged.links[1] = own.next_token;
        let forged = forged.encode().unwrap();
        assert!(ReportHandoff::decode(&crate::sha256(&forged), &forged).is_err());
    }
}
