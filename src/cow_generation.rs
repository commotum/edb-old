//! Pure construction of a lineage-bound copy-on-write log generation.
//!
//! SQL owns staging, catch-up checkpoints, and the final active-head CAS. This
//! module owns the deterministic transformation of already authenticated
//! source rows. Keeping that boundary pure makes fault/retry behavior easy to
//! test and keeps `Database::with` free of operational state.

use crate::excision::{ExcisionPlan, PlannedExcisionPredicate};
use crate::log_generation::{LineageTransaction, request_key_hash, tombstone_request_digest};
use crate::state_commitment::checkpoint_state_hash;
use crate::{Database, Digest, DurableTransaction, ErrorCategory, SemanticError, sha256};
use std::collections::BTreeSet;

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) enum SourceRequestKey {
    LegacyPlaintext(String),
    Digest(Digest),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct SourceRequest {
    pub(crate) key: SourceRequestKey,
    pub(crate) request_digest: Digest,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct SourceLogRow {
    pub(crate) tx_hash: Digest,
    pub(crate) transaction: DurableTransaction,
    pub(crate) request: SourceRequest,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct GenerationLogRow {
    pub(crate) basis_t: u64,
    pub(crate) previous_hash: Digest,
    pub(crate) tx_hash: Digest,
    pub(crate) state_hash: Digest,
    pub(crate) eidx_frontier: u64,
    pub(crate) payload: Vec<u8>,
    pub(crate) request_key_hash: Digest,
    pub(crate) request_digest: Digest,
}

#[derive(Clone, Debug)]
pub(crate) struct GenerationRewrite {
    pub(crate) database: Database,
    pub(crate) rows: Vec<GenerationLogRow>,
    pub(crate) predicates: Vec<PlannedExcisionPredicate>,
    pub(crate) request_set_hash: Digest,
    pub(crate) removed_datoms: u64,
    pub(crate) head_hash: Digest,
    pub(crate) state_hash: Digest,
}

/// Row-at-a-time rewrite state used by PostgreSQL staging. The final source
/// database is retained only to freeze the predicate plan; authoritative log
/// payloads and rewritten payloads need not be accumulated in a second pair
/// of database-sized vectors.
pub(crate) struct GenerationRewriter {
    lineage_id: String,
    generation: u64,
    expected_basis: u64,
    database: Database,
    plan: ExcisionPlan,
    previous_hash: Digest,
    removed_datoms: u64,
}

#[derive(Clone, Debug)]
pub(crate) struct GenerationRewriteOutcome {
    pub(crate) database: Database,
    pub(crate) predicates: Vec<PlannedExcisionPredicate>,
    pub(crate) request_set_hash: Digest,
    pub(crate) removed_datoms: u64,
    pub(crate) head_hash: Digest,
    pub(crate) state_hash: Digest,
}

impl GenerationRewriter {
    pub(crate) fn for_excision(
        lineage_id: &str,
        generation: u64,
        genesis_hash: Digest,
        source_database: &Database,
        completed_requests: &BTreeSet<(u64, u64)>,
    ) -> Result<Self, SemanticError> {
        let pending = source_database
            .pending_excision_requests_after(0)?
            .into_iter()
            .filter(|request| {
                !completed_requests.contains(&(request.request_t, request.request_entity))
            })
            .collect();
        let plan = ExcisionPlan::from_requests(source_database, pending)?;
        if plan.is_empty() {
            return Err(SemanticError::new(
                ErrorCategory::Incorrect,
                "excision/no-pending-requests",
                "database has no pending transactional excision requests",
            ));
        }
        Ok(Self {
            lineage_id: lineage_id.to_owned(),
            generation,
            expected_basis: source_database.basis_t(),
            database: Database::from_genesis(source_database.genesis_datoms().to_vec())?,
            plan,
            previous_hash: genesis_hash,
            removed_datoms: 0,
        })
    }

    pub(crate) fn rewrite_row(
        &mut self,
        source: SourceLogRow,
    ) -> Result<GenerationLogRow, SemanticError> {
        let basis_t = self.database.basis_t().checked_add(1).ok_or_else(|| {
            fault(
                "excision/source-size",
                "source transaction basis overflows u64",
            )
        })?;
        if source.transaction.basis_t != basis_t || source.tx_hash == [0; 32] {
            return Err(fault(
                "excision/source-prefix",
                "source transaction rows are not a canonical contiguous prefix",
            ));
        }
        let before = source.transaction.tx_data.len();
        let mut filtered = self.plan.filter_transaction(&source.transaction);
        self.removed_datoms = self
            .removed_datoms
            .checked_add((before - filtered.tx_data.len()) as u64)
            .ok_or_else(|| fault("excision/removed-count", "removed datom count overflow"))?;
        filtered.database_id.clone_from(&self.lineage_id);
        filtered.previous_hash = self.previous_hash;
        // Tempid names and request keys are request/response conveniences, not
        // database information. Historical copies can contain PII, so a COW
        // privacy generation deliberately retains only issued entity ids in
        // datoms and hashes the idempotency coordinate below.
        filtered.tempids.clear();
        let envelope = LineageTransaction {
            lineage_id: self.lineage_id.clone(),
            generation: self.generation,
            transaction: filtered.clone(),
        };
        let payload = envelope.encode()?;
        let tx_hash = sha256(&payload);
        self.database = self.database.apply_excised_committed(&filtered)?;
        let state_hash = checkpoint_state_hash(&self.database)?;
        let request_key_hash = match source.request.key {
            SourceRequestKey::LegacyPlaintext(key) => request_key_hash(&self.lineage_id, &key)?,
            SourceRequestKey::Digest(hash) => hash,
        };
        let request_digest =
            tombstone_request_digest(&self.lineage_id, self.generation, basis_t, request_key_hash)?;
        let row = GenerationLogRow {
            basis_t,
            previous_hash: self.previous_hash,
            tx_hash,
            state_hash,
            eidx_frontier: filtered.eidx_frontier,
            payload,
            request_key_hash,
            request_digest,
        };
        self.previous_hash = tx_hash;
        Ok(row)
    }

    pub(crate) fn finish(self) -> Result<GenerationRewriteOutcome, SemanticError> {
        if self.database.basis_t() != self.expected_basis {
            return Err(fault(
                "excision/source-prefix",
                "source rows do not cover the complete captured database prefix",
            ));
        }
        let state_hash = checkpoint_state_hash(&self.database)?;
        Ok(GenerationRewriteOutcome {
            database: self.database,
            predicates: self.plan.frozen_predicates(),
            request_set_hash: self.plan.request_set_hash(),
            removed_datoms: self.removed_datoms,
            head_hash: self.previous_hash,
            state_hash,
        })
    }
}

/// Rebuild one full source prefix under one frozen predicate set.
///
/// `completed_requests` names A=15 assertions already reflected by the source
/// generation. Every other currently asserted request is frozen against the
/// supplied source database. The source rows must cover exactly bases 1..=t.
pub(crate) fn rewrite_excision_generation(
    lineage_id: &str,
    generation: u64,
    genesis_hash: Digest,
    source_database: &Database,
    source_rows: &[SourceLogRow],
    completed_requests: &BTreeSet<(u64, u64)>,
) -> Result<GenerationRewrite, SemanticError> {
    let mut rewriter = GenerationRewriter::for_excision(
        lineage_id,
        generation,
        genesis_hash,
        source_database,
        completed_requests,
    )?;
    let mut rows = Vec::with_capacity(source_rows.len());
    for source in source_rows {
        rows.push(rewriter.rewrite_row(source.clone())?);
    }
    let outcome = rewriter.finish()?;
    Ok(GenerationRewrite {
        database: outcome.database,
        rows,
        predicates: outcome.predicates,
        request_set_hash: outcome.request_set_hash,
        removed_datoms: outcome.removed_datoms,
        head_hash: outcome.head_hash,
        state_hash: outcome.state_hash,
    })
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::log_generation::LineageTransaction;
    use crate::{
        Attribute, Cardinality, DB_EXCISE, EntityRef, IndexOrder, Keyword, TxOp, TxReport, TxValue,
        Value, ValueType, View, encode_genesis, encode_transaction, transaction_hash,
    };

    const LINEAGE: &str = "01234567-89ab-4def-8123-456789abcdef";
    const SECRET: u32 = 1_000;

    fn source_row(report: &TxReport, previous_hash: Digest, key: &str) -> SourceLogRow {
        let transaction = DurableTransaction {
            database_id: "mutable-alias".into(),
            basis_t: report.db_after.basis_t(),
            previous_hash,
            eidx_frontier: report.db_after.eidx_frontier(),
            tempids: report.tempids.clone(),
            tx_data: report.tx_data.clone(),
        };
        let payload = encode_transaction(&transaction).unwrap();
        SourceLogRow {
            tx_hash: transaction_hash(&payload),
            transaction,
            request: SourceRequest {
                key: SourceRequestKey::LegacyPlaintext(key.into()),
                request_digest: sha256(key.as_bytes()),
            },
        }
    }

    fn source_with_request() -> (Database, Digest, Vec<SourceLogRow>, u64, u64) {
        let bootstrap = Database::bootstrap().unwrap();
        let genesis_hash = sha256(&encode_genesis(bootstrap.genesis_datoms()).unwrap());
        let install = bootstrap
            .with(
                &[TxOp::InstallAttribute(Attribute::new(
                    SECRET,
                    Keyword::new("person", "secret"),
                    ValueType::String,
                    Cardinality::One,
                ))],
                100,
            )
            .unwrap();
        let first = source_row(&install, genesis_hash, "install");
        let asserted = install
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("customer@example.test".into()),
                    attribute: SECRET,
                    value: TxValue::Scalar(Value::String("remove-me".into())),
                }],
                200,
            )
            .unwrap();
        let target = asserted.tempids["customer@example.test"];
        let second = source_row(&asserted, first.tx_hash, "customer@example.test/create");
        let requested = asserted
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("privacy-ticket@example.test".into()),
                    attribute: DB_EXCISE as u32,
                    value: TxValue::Entity(EntityRef::Id(target)),
                }],
                300,
            )
            .unwrap();
        let request_entity = requested.tempids["privacy-ticket@example.test"];
        let third = source_row(
            &requested,
            second.tx_hash,
            "customer@example.test/privacy-ticket",
        );
        (
            requested.db_after,
            genesis_hash,
            vec![first, second, third],
            target,
            request_entity,
        )
    }

    #[test]
    fn cow_rewrite_uses_transactional_request_and_drops_plaintext_metadata() {
        let (source, genesis_hash, rows, target, request_entity) = source_with_request();
        let rewritten =
            rewrite_excision_generation(LINEAGE, 7, genesis_hash, &source, &rows, &BTreeSet::new())
                .unwrap();
        assert_eq!(rewritten.rows.len(), rows.len());
        assert_eq!(rewritten.removed_datoms, 1);
        assert!(rewritten.database.values(target, SECRET).is_empty());
        assert!(
            !rewritten
                .database
                .datoms(View::History, IndexOrder::Eavt)
                .iter()
                .any(|datom| datom.value == Value::String("remove-me".into()))
        );
        assert_eq!(rewritten.predicates.len(), 1);
        assert_eq!(
            rewritten.predicates[0].request.request_entity,
            request_entity
        );
        assert_ne!(rewritten.request_set_hash, [0; 32]);

        for (source, destination) in rows.iter().zip(&rewritten.rows) {
            let decoded = LineageTransaction::decode(&destination.payload).unwrap();
            assert_eq!(decoded.lineage_id, LINEAGE);
            assert_eq!(decoded.generation, 7);
            assert!(decoded.transaction.tempids.is_empty());
            assert_ne!(destination.request_digest, source.request.request_digest);
            assert!(
                !destination
                    .payload
                    .windows("customer@example.test".len())
                    .any(|window| window == b"customer@example.test")
            );
        }
    }

    #[test]
    fn exact_completed_request_identity_is_not_reapplied() {
        let (source, genesis_hash, rows, _, request_entity) = source_with_request();
        let completed = BTreeSet::from([(3, request_entity)]);
        let error =
            rewrite_excision_generation(LINEAGE, 8, genesis_hash, &source, &rows, &completed)
                .unwrap_err();
        assert_eq!(error.code, "excision/no-pending-requests");
    }
}
