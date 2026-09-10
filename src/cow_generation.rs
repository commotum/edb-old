//! Pure construction of a lineage-bound copy-on-write log generation.
//!
//! SQL owns staging, catch-up checkpoints, and the final active-head CAS. This
//! module owns the deterministic transformation of already authenticated
//! source rows. Keeping that boundary pure makes fault/retry behavior easy to
//! test and keeps `Database::with` free of operational state.

use crate::excision::{ExcisionPlan, PlannedExcisionPredicate};
use crate::log_generation::{
    LineageTransactionContent, generation_transaction_hash, request_key_hash,
    tombstone_request_digest,
};
use crate::reserved_allocation::ReservedAllocation;
use crate::state_commitment::checkpoint_state_hash;
use crate::{
    Database, Digest, DurableTransaction, ErrorCategory, SemanticError, decode_transaction, sha256,
};
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
    pub(crate) state_hash: Digest,
    /// Exact bytes selected from the active source generation. Content and its
    /// small membership commitment are authenticated before filtering.
    pub(crate) encoding: SourceLogEncoding,
    pub(crate) transaction: DurableTransaction,
    pub(crate) request: SourceRequest,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) enum SourceLogEncoding {
    Legacy(Vec<u8>),
    Lineage {
        generation: u64,
        content_hash: Digest,
        payload: Vec<u8>,
    },
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct GenerationLogRow {
    pub(crate) basis_t: u64,
    pub(crate) previous_hash: Digest,
    pub(crate) tx_hash: Digest,
    pub(crate) content_hash: Digest,
    pub(crate) state_hash: Digest,
    pub(crate) eidx_frontier: u64,
    pub(crate) payload: Vec<u8>,
    pub(crate) request_key_hash: Digest,
    pub(crate) request_digest: Digest,
}

#[cfg(test)]
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
    source_previous_hash: Digest,
    removed_datoms: u64,
    source_reserved: ReservedAllocation,
    output_reserved: ReservedAllocation,
    source_v2: bool,
    output_v2: bool,
    legacy_excision_floor: ReservedAllocation,
    legacy_excision_before_t: u64,
}

#[derive(Clone, Debug)]
pub(crate) struct GenerationRewriteOutcome {
    pub(crate) removed_datoms: u64,
    pub(crate) head_hash: Digest,
}

impl GenerationRewriter {
    pub(crate) fn for_excision(
        lineage_id: &str,
        generation: u64,
        genesis_hash: Digest,
        source_database: &Database,
        completed_requests: &BTreeSet<(u64, u64)>,
    ) -> Result<Self, SemanticError> {
        let requests = source_database.pending_excision_requests_after(0)?;
        let completed = requests
            .iter()
            .filter(|request| {
                completed_requests.contains(&(request.request_t, request.request_entity))
            })
            .collect::<Vec<_>>();
        if completed.len() != completed_requests.len() {
            return Err(fault(
                "excision/source-completion-proof",
                "completed excision lacks its protected request facts",
            ));
        }
        let legacy_excision_before_t = completed
            .iter()
            .map(|request| source_database.excision_before_t(request))
            .max()
            .unwrap_or(0);
        let pending = requests
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
        let mut reserved = ReservedAllocation::initial();
        reserved.observe_datoms(source_database.genesis_datoms())?;
        Ok(Self {
            lineage_id: lineage_id.to_owned(),
            generation,
            expected_basis: source_database.basis_t(),
            database: Database::from_genesis(source_database.genesis_datoms().to_vec())?,
            plan,
            previous_hash: genesis_hash,
            source_previous_hash: genesis_hash,
            removed_datoms: 0,
            source_reserved: reserved,
            output_reserved: reserved,
            source_v2: false,
            output_v2: false,
            legacy_excision_floor: ReservedAllocation::initial(),
            legacy_excision_before_t,
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
        if source.transaction.basis_t != basis_t
            || source.transaction.previous_hash != self.source_previous_hash
            || source.tx_hash == [0; 32]
        {
            return Err(fault(
                "excision/source-prefix",
                "source transaction rows are not a canonical contiguous prefix",
            ));
        }
        let mut source_checkpoint = None;
        let authenticated_transaction = match &source.encoding {
            SourceLogEncoding::Legacy(payload) => {
                if sha256(payload) != source.tx_hash {
                    return Err(fault(
                        "excision/source-payload",
                        "legacy source content hash does not match its transaction row",
                    ));
                }
                decode_transaction(payload)?
            }
            SourceLogEncoding::Lineage {
                generation,
                content_hash,
                payload,
            } => {
                if sha256(payload) != *content_hash {
                    return Err(fault(
                        "excision/source-payload",
                        "lineage source content hash does not match its immutable value",
                    ));
                }
                let content = LineageTransactionContent::decode(payload)?;
                let expected_membership = generation_transaction_hash(
                    &content.lineage_id,
                    *generation,
                    content.basis_t,
                    source.transaction.previous_hash,
                    *content_hash,
                    source.state_hash,
                    content.eidx_frontier,
                )?;
                if expected_membership != source.tx_hash {
                    return Err(fault(
                        "excision/source-membership",
                        "lineage source membership commitment is invalid",
                    ));
                }
                if content.reserved_frontier.is_some() {
                    source_checkpoint =
                        Some(content.validate_reserved_transition(self.source_reserved)?);
                } else if self.source_v2 {
                    return Err(fault(
                        "excision/source-allocation-version",
                        "source allocation format regresses from ATLC v2 to v1",
                    ));
                }
                let mut transaction = content.to_transaction(source.transaction.previous_hash);
                transaction.database_id = source.transaction.database_id.clone();
                transaction
            }
        };
        if authenticated_transaction != source.transaction {
            return Err(fault(
                "excision/source-payload",
                "source payload does not canonically encode the supplied transaction",
            ));
        }
        let mut source_reserved = self.source_reserved;
        if let Some(checkpoint) = source_checkpoint {
            source_reserved = checkpoint;
            self.source_v2 = true;
            // A first authenticated modern checkpoint supersedes the legacy
            // uncertainty bound. Already emitted v2 state still cannot regress.
            self.legacy_excision_floor = ReservedAllocation::initial();
        } else {
            source_reserved.observe_transaction(&source.transaction)?;
            if basis_t < self.legacy_excision_before_t {
                // Resolve the exact frozen cutoff from the complete source.
                // Preserve the uncertain prefix, not later ordinary growth.
                self.legacy_excision_floor
                    .reserve_legacy_excision_floor(source.transaction.eidx_frontier)?;
            }
        }
        let before = source.transaction.tx_data.len();
        let mut filtered = self.plan.filter_transaction(&source.transaction);
        self.removed_datoms = self
            .removed_datoms
            .checked_add((before - filtered.tx_data.len()) as u64)
            .ok_or_else(|| fault("excision/removed-count", "removed datom count overflow"))?;
        // Tempid names and request keys are request/response conveniences, not
        // database information. Content keeps only canonical numeric
        // allocation witnesses, including one whose every datom was excised.
        // Excluding generation and predecessor is what lets unaffected
        // immutable content survive a COW rewrite by identity.
        let current_frontier = self.database.eidx_frontier();
        let prior_reserved = self.output_reserved;
        let mut surviving_reserved = prior_reserved;
        surviving_reserved.observe_transaction(&filtered)?;
        let output_v2 = self.output_v2
            || source_checkpoint.is_some()
            || source_reserved.frontier() > surviving_reserved.frontier();
        let after_reserved = if output_v2 {
            ReservedAllocation::from_frontier(
                prior_reserved
                    .frontier()
                    .max(source_reserved.frontier())
                    .max(self.legacy_excision_floor.frontier()),
                filtered.eidx_frontier,
            )?
        } else {
            surviving_reserved
        };
        let content = if output_v2 {
            LineageTransactionContent::from_transaction_v2(
                &self.lineage_id,
                current_frontier,
                prior_reserved,
                after_reserved,
                &filtered,
            )?
        } else {
            LineageTransactionContent::from_transaction(
                &self.lineage_id,
                current_frontier,
                &filtered,
            )?
        };
        let payload = content.encode()?;
        let content_hash = sha256(&payload);
        filtered = content.to_transaction(self.previous_hash);
        self.database = if output_v2 {
            self.database.apply_committed_with_reserved_allocation(
                &filtered,
                prior_reserved,
                after_reserved,
                true,
            )?
        } else {
            self.database.apply_excised_committed(&filtered)?
        };
        let state_hash = checkpoint_state_hash(&self.database)?;
        let tx_hash = generation_transaction_hash(
            &self.lineage_id,
            self.generation,
            basis_t,
            self.previous_hash,
            content_hash,
            state_hash,
            filtered.eidx_frontier,
        )?;
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
            content_hash,
            state_hash,
            eidx_frontier: filtered.eidx_frontier,
            payload,
            request_key_hash,
            request_digest,
        };
        self.previous_hash = tx_hash;
        self.source_previous_hash = source.tx_hash;
        self.source_reserved = source_reserved;
        self.output_reserved = after_reserved;
        self.output_v2 = output_v2;
        Ok(row)
    }

    pub(crate) fn frozen_predicates(&self) -> Vec<PlannedExcisionPredicate> {
        self.plan.frozen_predicates()
    }

    pub(crate) fn request_set_hash(&self) -> Digest {
        self.plan.request_set_hash()
    }

    pub(crate) fn current_basis(&self) -> u64 {
        self.database.basis_t()
    }

    pub(crate) fn current_head_hash(&self) -> Digest {
        self.previous_hash
    }

    pub(crate) fn current_source_hash(&self) -> Digest {
        self.source_previous_hash
    }

    pub(crate) fn current_state_hash(&self) -> Result<Digest, SemanticError> {
        checkpoint_state_hash(&self.database)
    }

    pub(crate) fn current_eidx_frontier(&self) -> u64 {
        self.database.eidx_frontier()
    }

    pub(crate) fn current_database(&self) -> &Database {
        &self.database
    }

    pub(crate) fn removed_datoms(&self) -> u64 {
        self.removed_datoms
    }

    pub(crate) fn expect_through(&mut self, basis_t: u64) {
        self.expected_basis = basis_t;
    }

    pub(crate) fn finish(self) -> Result<GenerationRewriteOutcome, SemanticError> {
        self.ensure_complete()?;
        Ok(GenerationRewriteOutcome {
            removed_datoms: self.removed_datoms,
            head_hash: self.previous_hash,
        })
    }

    /// Independently audit a complete candidate before making it visible.
    pub(crate) fn validate_complete(&self) -> Result<(), SemanticError> {
        self.ensure_complete()?;
        self.database.validate_invariants()
    }

    fn ensure_complete(&self) -> Result<(), SemanticError> {
        if self.database.basis_t() != self.expected_basis {
            return Err(fault(
                "excision/source-prefix",
                "source rows do not cover the complete captured database prefix",
            ));
        }
        Ok(())
    }

    #[cfg(test)]
    fn finish_test(self, rows: Vec<GenerationLogRow>) -> Result<GenerationRewrite, SemanticError> {
        self.validate_complete()?;
        let state_hash = checkpoint_state_hash(&self.database)?;
        Ok(GenerationRewrite {
            database: self.database,
            rows,
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
#[cfg(test)]
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
    rewriter.finish_test(rows)
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::log_generation::LineageTransactionContent;
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
            state_hash: checkpoint_state_hash(&report.db_after).unwrap(),
            encoding: SourceLogEncoding::Legacy(payload),
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
            let decoded = LineageTransactionContent::decode(&destination.payload).unwrap();
            assert_eq!(decoded.lineage_id, LINEAGE);
            let transaction = decoded.to_transaction(destination.previous_hash);
            assert!(
                transaction
                    .tempids
                    .keys()
                    .all(|name| name.starts_with("allocation-"))
            );
            assert_ne!(destination.request_digest, source.request.request_digest);
            assert!(
                !destination
                    .payload
                    .windows("customer@example.test".len())
                    .any(|window| window == b"customer@example.test")
            );
        }

        // A fresh process can replay only the rewritten bytes. The allocation
        // whose sole user datom was removed remains issued and the next
        // transaction cannot reuse its entity id.
        let mut restarted = Database::from_genesis(source.genesis_datoms().to_vec()).unwrap();
        for row in &rewritten.rows {
            let transaction = LineageTransactionContent::decode(&row.payload)
                .unwrap()
                .to_transaction(row.previous_hash);
            restarted = restarted.apply_excised_committed(&transaction).unwrap();
        }
        assert!(restarted.same_information_as(&rewritten.database));
        let next = restarted
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("next".into()),
                    attribute: SECRET,
                    value: TxValue::Scalar(Value::String("still-here".into())),
                }],
                400,
            )
            .unwrap();
        assert_ne!(next.tempids["next"], target);
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

    #[test]
    fn legacy_reference_only_claim_survives_cow_and_starts_monotone_v2_content() {
        // A genuine old allocator issues ordinary receipt-only IDs past a
        // low DB-partition target that has never been an entity or a tempid.
        let modern_bootstrap = Database::bootstrap().unwrap();
        let bootstrap = Database::from_genesis(modern_bootstrap.genesis_datoms().to_vec()).unwrap();
        let genesis_hash = sha256(&encode_genesis(bootstrap.genesis_datoms()).unwrap());
        let installed = bootstrap
            .with(
                &[TxOp::InstallAttribute(Attribute::new(
                    SECRET,
                    Keyword::new("claim", "ref"),
                    ValueType::Ref,
                    Cardinality::One,
                ))],
                100,
            )
            .unwrap();
        let first = source_row(&installed, genesis_hash, "schema");
        let receipt_only = installed
            .db_after
            .with(
                &(0..800)
                    .map(|index| TxOp::RetractEntity(EntityRef::Temp(format!("issued-{index}"))))
                    .collect::<Vec<_>>(),
                200,
            )
            .unwrap();
        let second = source_row(&receipt_only, first.tx_hash, "receipts");
        let reference = receipt_only
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("owner".into()),
                    attribute: SECRET,
                    value: TxValue::Entity(EntityRef::Id(1_750)),
                }],
                300,
            )
            .unwrap();
        let owner = reference.tempids["owner"];
        let third = source_row(&reference, second.tx_hash, "reference");
        let requested = reference
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: DB_EXCISE as u32,
                    value: TxValue::Entity(EntityRef::Id(owner)),
                }],
                400,
            )
            .unwrap();
        let fourth = source_row(&requested, third.tx_hash, "excise");
        let rows = vec![first, second, third, fourth];
        let rewritten = rewrite_excision_generation(
            LINEAGE,
            7,
            genesis_hash,
            &requested.db_after,
            &rows,
            &BTreeSet::new(),
        )
        .unwrap();
        assert_eq!(rewritten.removed_datoms, 1);
        assert!(rewritten.database.values(owner, SECRET).is_empty());
        let contents = rewritten
            .rows
            .iter()
            .map(|row| LineageTransactionContent::decode(&row.payload).unwrap())
            .collect::<Vec<_>>();
        assert_eq!(
            contents
                .iter()
                .map(LineageTransactionContent::version)
                .collect::<Vec<_>>(),
            vec![1, 1, 2, 2]
        );
        assert_eq!(contents[2].reserved_frontier, Some(1_751));
        assert_eq!(contents[3].reserved_frontier, Some(1_751));
        assert!(
            !contents[2]
                .tx_data
                .iter()
                .any(|datom| datom.value == Value::Ref(1_750))
        );
        // Restart solely from published content; no original values or source
        // generation are available to rescue the reference-only claim.
        let mut restarted = Database::from_genesis(bootstrap.genesis_datoms().to_vec()).unwrap();
        let mut reserved = ReservedAllocation::initial();
        reserved.observe_datoms(bootstrap.genesis_datoms()).unwrap();
        for (row, content) in rewritten.rows.iter().zip(&contents) {
            let transaction = content.to_transaction(row.previous_hash);
            if content.reserved_frontier.is_some() {
                let after = content.validate_reserved_transition(reserved).unwrap();
                restarted = restarted
                    .apply_committed_with_reserved_allocation(&transaction, reserved, after, true)
                    .unwrap();
                reserved = after;
            } else {
                restarted = restarted.apply_excised_committed(&transaction).unwrap();
                reserved.observe_transaction(&transaction).unwrap();
            }
        }
        assert_eq!(restarted.reserved_allocation().unwrap().frontier(), 1_751);
        assert_eq!(reserved.allocate().unwrap(), 1_751);
        assert!(restarted.same_information_as(&rewritten.database));
    }

    #[test]
    fn successor_generation_reuses_every_unaffected_content_value() {
        let (source, genesis_hash, rows, _, first_request_entity) = source_with_request();
        let first =
            rewrite_excision_generation(LINEAGE, 7, genesis_hash, &source, &rows, &BTreeSet::new())
                .unwrap();
        assert_eq!(first.head_hash, first.rows.last().unwrap().tx_hash);
        assert_eq!(first.state_hash, first.rows.last().unwrap().state_hash);

        let protected_target = crate::make_eid(crate::DB_PARTITION, 500).unwrap();
        let request = first
            .database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("do-not-copy-this-name".into()),
                    attribute: DB_EXCISE as u32,
                    value: TxValue::Entity(EntityRef::Id(protected_target)),
                }],
                400,
            )
            .unwrap();
        let prior_frontier = first.database.eidx_frontier();
        let raw = DurableTransaction {
            database_id: LINEAGE.into(),
            basis_t: request.db_after.basis_t(),
            previous_hash: first.head_hash,
            eidx_frontier: request.db_after.eidx_frontier(),
            tempids: request.tempids.clone(),
            tx_data: request.tx_data.clone(),
        };
        let content =
            LineageTransactionContent::from_transaction(LINEAGE, prior_frontier, &raw).unwrap();
        let payload = content.encode().unwrap();
        let content_hash = sha256(&payload);
        let state_hash = checkpoint_state_hash(&request.db_after).unwrap();
        let tx_hash = generation_transaction_hash(
            LINEAGE,
            7,
            raw.basis_t,
            first.head_hash,
            content_hash,
            state_hash,
            raw.eidx_frontier,
        )
        .unwrap();

        let mut positive_rows = first
            .rows
            .iter()
            .map(|row| SourceLogRow {
                tx_hash: row.tx_hash,
                state_hash: row.state_hash,
                encoding: SourceLogEncoding::Lineage {
                    generation: 7,
                    content_hash: row.content_hash,
                    payload: row.payload.clone(),
                },
                transaction: LineageTransactionContent::decode(&row.payload)
                    .unwrap()
                    .to_transaction(row.previous_hash),
                request: SourceRequest {
                    key: SourceRequestKey::Digest(row.request_key_hash),
                    request_digest: row.request_digest,
                },
            })
            .collect::<Vec<_>>();
        positive_rows.push(SourceLogRow {
            tx_hash,
            state_hash,
            encoding: SourceLogEncoding::Lineage {
                generation: 7,
                content_hash,
                payload: payload.clone(),
            },
            transaction: content.to_transaction(first.head_hash),
            request: SourceRequest {
                key: SourceRequestKey::Digest(request_key_hash(LINEAGE, "protected").unwrap()),
                request_digest: sha256(b"protected"),
            },
        });

        let second = rewrite_excision_generation(
            LINEAGE,
            8,
            genesis_hash,
            &request.db_after,
            &positive_rows,
            &BTreeSet::from([(3, first_request_entity)]),
        )
        .unwrap();
        assert_eq!(second.removed_datoms, 0);
        assert_eq!(
            second
                .rows
                .iter()
                .map(|row| row.content_hash)
                .collect::<Vec<_>>(),
            positive_rows
                .iter()
                .map(|row| match &row.encoding {
                    SourceLogEncoding::Lineage { content_hash, .. } => *content_hash,
                    SourceLogEncoding::Legacy(_) => unreachable!(),
                })
                .collect::<Vec<_>>()
        );
        assert!(second.rows.iter().all(|row| {
            !row.payload
                .windows("do-not-copy-this-name".len())
                .any(|window| window == b"do-not-copy-this-name")
        }));
    }
}
