use crate::{Datom, Digest, IndexOrder, PostgresConnectionConfig, SemanticError};
use std::collections::BTreeMap;
use std::time::Duration;

#[path = "block_index_operations.rs"]
mod block_index_operations;
#[path = "block_operations.rs"]
mod block_operations;
pub use block_index_operations::IndexMaintenanceReceipt;
#[path = "database_reclamation.rs"]
mod database_reclamation;
#[path = "program_deployment.rs"]
mod program_deployment;
pub use database_reclamation::RetiredDatabaseReclamation;
#[path = "excision_worker.rs"]
pub(crate) mod excision_worker;
pub use excision_worker::{ExcisionConfig, ExcisionProgress};

/// Routine retention for retired immutable structures. Shorter ages are an
/// explicit operator choice and can disrupt long-running readers.
pub const RECOMMENDED_GARBAGE_COLLECTION_AGE: Duration = Duration::from_secs(30 * 24 * 60 * 60);
/// Maximum graph, metadata and object steps advanced by one operator call.
pub const MAX_COLLECTION_STEPS: usize = 4096;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct IntegrityProblem {
    pub code: String,
    pub message: String,
}

/// Coordinates refer to one captured publication. Optional counts are only
/// populated by explicit deep inspection; None never means zero.
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct OperationalMetrics {
    pub basis_t: u64,
    pub index_basis_t: u64,
    pub index_lag: u64,
    pub generation: u64,
    pub publication_revision: u64,
    pub transactions: u64,
    pub indexed_current_datoms: u64,
    pub indexed_history_datoms: u64,
    pub pending_avet_projections: u64,
    pub transaction_bytes: Option<u64>,
    pub requests: Option<u64>,
    pub current_datoms: Option<u64>,
    pub history_datoms: Option<u64>,
    pub reachable_objects: Option<u64>,
    pub reachable_bytes: Option<u64>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct IntegrityReport {
    pub database_id: String,
    pub metrics: OperationalMetrics,
    pub problems: Vec<IntegrityProblem>,
}

/// Read-only preview describes provider metadata and the durable collector
/// checkpoint, not a speculative deletion list. Applied results describe one
/// bounded advance; totals are None rather than rescanning the whole provider.
#[derive(Clone, Debug)]
pub struct GarbageInventory {
    pub phase: Option<crate::storage::ownership::CollectionPhase>,
    pub sealed_epoch: Option<u64>,
    pub minimum_age: Duration,
    pub stored_objects: Option<u64>,
    pub stored_bytes: Option<u64>,
    pub pending_events: Option<u64>,
    pub collection: crate::storage::ownership::CollectionStats,
    /// One bounded authorization-pruning page after a settled cycle. Its
    /// ownership deltas, if any, are folded by the next collection cycle.
    pub authorization: Option<crate::storage::ownership::AuthorizationMaintenance>,
    /// Report bridges retired after a settled cycle; their objects become
    /// eligible for age-respecting collection in a subsequent cycle.
    pub report_handoffs: Option<crate::storage::ownership::ReportHandoffMaintenance>,
    pub applied: bool,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ExcisionFault {
    None,
    AfterCapture,
    AfterCandidateStaged,
    AfterActivation,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ExcisionReceipt {
    pub database_id: String,
    pub source_generation: u64,
    pub generation: u64,
    pub basis_t: u64,
    pub request_count: u64,
    pub removed_datoms: u64,
    pub old_head_hash: Digest,
    pub new_head_hash: Digest,
    pub resumed: bool,
}

impl IntegrityReport {
    pub fn healthy(&self) -> bool {
        self.problems.is_empty()
    }
}

pub struct PostgresOperator {
    connection: PostgresConnectionConfig,
    maintenance: crate::MaintenanceControl,
    fulltext_build_limits: crate::FulltextBuildLimits,
    tree_config: crate::index::tree::TreeConfig,
}

impl PostgresOperator {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::parse(connection)?)
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        crate::storage::PgBlockStore::connect(connection)?;
        Ok(Self {
            connection: connection.clone(),
            maintenance: crate::MaintenanceControl::default(),
            fulltext_build_limits: crate::FulltextBuildLimits::default(),
            tree_config: crate::index::tree::TreeConfig::default(),
        })
    }

    pub fn with_maintenance_control(mut self, control: crate::MaintenanceControl) -> Self {
        self.maintenance = control;
        self
    }

    /// Configure physical tree construction for consolidation and explicit
    /// current-index recovery. Existing untouched nodes remain shared; this
    /// does not change logical value limits or the independent search builder.
    pub fn with_tree_config(
        mut self,
        config: crate::index::tree::TreeConfig,
    ) -> Result<Self, SemanticError> {
        config.validate()?;
        self.tree_config = config;
        Ok(self)
    }

    /// Configure search-build admission for consolidation, explicit current
    /// index recovery, search rebuild and excision. Defaults are unchanged.
    pub fn with_fulltext_build_limits(
        mut self,
        limits: crate::FulltextBuildLimits,
    ) -> Result<Self, SemanticError> {
        limits.validate()?;
        self.fulltext_build_limits = limits;
        Ok(self)
    }

    /// Validate and protect immutable native code before binding its hash in a
    /// transaction. Fixed dependencies must already exist; the complete code
    /// closure is authenticated and protected under one collector epoch.
    /// Staging survives for the collection grace period; no live handle is needed.
    pub fn deploy_program(
        &mut self,
        program: &crate::Program,
    ) -> Result<crate::ProgramHash, SemanticError> {
        program_deployment::deploy(&self.connection, program, &self.maintenance)
    }

    /// Stage a program closure and bind its ident in one application operation.
    /// Dependencies can be supplied in any order. Exact retries return the
    /// committed outcome before touching deployment dependencies.
    pub fn install_program(
        &mut self,
        client: &crate::TransactionClient,
        request_key: &str,
        ident: crate::Keyword,
        program: &crate::Program,
        dependencies: &[crate::Program],
        timeout: Duration,
    ) -> Result<crate::ServiceTransactionReport, SemanticError> {
        program_deployment::install(
            self,
            client,
            request_key,
            ident,
            program,
            dependencies,
            timeout,
        )
    }

    /// Explicitly consolidate a captured transaction prefix. Damaged current
    /// indexes may be reconstructed from the authenticated canonical log;
    /// this never drops old receipt or read-authorization roots.
    pub fn consolidate_database(
        &mut self,
        database_id: &str,
    ) -> Result<IndexMaintenanceReceipt, SemanticError> {
        block_index_operations::maintain(
            &self.connection,
            database_id,
            false,
            None,
            &self.maintenance,
            &self.fulltext_build_limits,
            &self.tree_config,
        )
    }

    /// Rebuild derived search with no predecessor dependency. An explicit
    /// expected index guards the exact repair target; no old objects are
    /// physically removed, including objects retained by captured readers.
    pub fn rebuild_fulltext(
        &mut self,
        database_id: &str,
        expected_index: Option<Digest>,
    ) -> Result<IndexMaintenanceReceipt, SemanticError> {
        block_index_operations::maintain(
            &self.connection,
            database_id,
            true,
            expected_index,
            &self.maintenance,
            &self.fulltext_build_limits,
            &self.tree_config,
        )
    }

    /// Authenticate one immutable root. Deep mode additionally validates all
    /// retained object links, log replay, exact receipts and index projections.
    pub fn inspect_database(
        &mut self,
        database_id: &str,
        deep_derived: bool,
    ) -> Result<IntegrityReport, SemanticError> {
        block_operations::inspect(
            &self.connection,
            database_id,
            deep_derived,
            &self.maintenance,
        )
    }

    /// Read-only provider metadata and collector status; never starts a cycle.
    pub fn garbage_inventory(
        &mut self,
        older_than: Duration,
    ) -> Result<GarbageInventory, SemanticError> {
        block_operations::inventory(&self.connection, older_than, &self.maintenance)
    }

    /// Advance bounded, resumable collection. Complete means this sealed cycle
    /// finished, not global quiescence or revocation of still-owned values.
    pub fn collect_garbage(
        &mut self,
        older_than: Duration,
    ) -> Result<GarbageInventory, SemanticError> {
        block_operations::collect(&self.connection, older_than, &self.maintenance)
    }

    /// Process ordinary A=15 (`:db/excise`) facts through a background,
    /// copy-on-write log generation. A backup remains strongly recommended,
    /// matching Datomic's operational guidance, but it is not an authorization
    /// token and is therefore absent from this semantic API.
    /// `database_id` is the stable ID returned by the catalog, not a mutable name.
    pub fn process_excision_requests(
        &mut self,
        database_id: &str,
    ) -> Result<ExcisionReceipt, SemanticError> {
        self.process_excision_requests_with_fault(database_id, ExcisionFault::None)
    }

    #[doc(hidden)]
    pub fn process_excision_requests_with_fault(
        &mut self,
        database_id: &str,
        fault_point: ExcisionFault,
    ) -> Result<ExcisionReceipt, SemanticError> {
        crate::transactor::excision_operator::process_requests(
            &self.connection,
            database_id,
            fault_point,
            &self.maintenance,
            &self.fulltext_build_limits,
        )
    }

    /// True only when every A=15 request at or before `through_t` is reflected
    /// by the active generation and its root-last completion marker.
    pub fn sync_excise(
        &mut self,
        database_id: &str,
        through_t: u64,
    ) -> Result<bool, SemanticError> {
        crate::storage::excision::sync_requests(
            &self.connection,
            database_id,
            through_t,
            &self.maintenance,
        )
    }
}

pub(crate) fn derive_index_projection(
    database: &crate::Database,
    source: &[Datom],
    order: IndexOrder,
) -> Result<Vec<Datom>, SemanticError> {
    let mut datoms = source
        .iter()
        .filter_map(|datom| {
            let included = match order {
                IndexOrder::Eavt | IndexOrder::Aevt => Ok(true),
                IndexOrder::Avet => database
                    .schema()
                    .attribute(datom.attribute)
                    .map(|attribute| attribute.indexed || attribute.unique.is_some()),
                IndexOrder::Vaet => database
                    .schema()
                    .attribute(datom.attribute)
                    .map(|attribute| attribute.value_type == crate::ValueType::Ref),
            };
            match included {
                Ok(true) => Some(Ok(datom.clone())),
                Ok(false) => None,
                Err(error) => Some(Err(error)),
            }
        })
        .collect::<Result<Vec<_>, _>>()?;
    datoms.sort_by(|left, right| left.cmp_in(right, order));
    Ok(datoms)
}

pub(crate) fn same_stored_datoms(left: &[Datom], right: &[Datom]) -> bool {
    left.len() == right.len()
        && left
            .iter()
            .zip(right)
            .all(|(left, right)| same_stored_datom(left, right))
}

fn same_stored_datom(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
        && left.tx == right.tx
        && left.added == right.added
}

/// Prove that physical EAVT history is an authenticated-log projection, not
/// merely a self-consistent alternative tree. Within one logical E/A/V group,
/// each contiguous omitted run must reduce to empty by repeatedly deleting a
/// retraction followed by an assertion. This admits the nested omissions that
/// successive consolidation jobs can expose (`R R A A`), while rejecting a
/// fabricated fact, a one-sided omission, or an omission across a retained
/// fact. Every matched retraction must also have a noHistory=true opportunity
/// at a transaction boundary on or after it. This proves permission, not the
/// exact scheduler instant: Datomic documents no precise time at which an
/// eligible pair is physically forgotten.
pub(crate) fn validate_physical_history_projection(
    replayed: &[Datom],
    physical: &[Datom],
    through_t: u64,
) -> Result<(), SemanticError> {
    let opportunities = no_history_opportunities(replayed, through_t)?;
    let mut replay_offset = 0_usize;
    let mut physical_offset = 0_usize;

    while replay_offset < replayed.len() {
        let group_start = replay_offset;
        replay_offset += 1;
        while replay_offset < replayed.len()
            && same_logical_eav(&replayed[group_start], &replayed[replay_offset])
        {
            replay_offset += 1;
        }
        let group = &replayed[group_start..replay_offset];

        if let Some(actual) = physical.get(physical_offset)
            && logical_eav_cmp(actual, &group[0]).is_lt()
        {
            return Err(integrity_fault(
                "physical EAVT history contains a fact absent from authoritative replay",
            ));
        }

        let mut expected_in_group = 0_usize;
        let mut missing_start = 0_usize;
        while let Some(actual) = physical.get(physical_offset) {
            match logical_eav_cmp(actual, &group[0]) {
                std::cmp::Ordering::Less => {
                    return Err(integrity_fault(
                        "physical EAVT history contains a fact absent from authoritative replay",
                    ));
                }
                std::cmp::Ordering::Greater => break,
                std::cmp::Ordering::Equal => {}
            }

            while expected_in_group < group.len()
                && !same_stored_datom(&group[expected_in_group], actual)
            {
                expected_in_group += 1;
            }
            if expected_in_group == group.len() {
                return Err(integrity_fault(
                    "physical EAVT history contains a fact absent from authoritative replay",
                ));
            }
            validate_history_omission(
                &group[missing_start..expected_in_group],
                &opportunities,
                through_t,
            )?;
            expected_in_group += 1;
            missing_start = expected_in_group;
            physical_offset += 1;
        }
        validate_history_omission(&group[missing_start..], &opportunities, through_t)?;
    }

    if physical_offset != physical.len() {
        return Err(integrity_fault(
            "physical EAVT history has trailing facts absent from authoritative replay",
        ));
    }
    Ok(())
}

fn same_logical_eav(left: &Datom, right: &Datom) -> bool {
    logical_eav_cmp(left, right).is_eq()
}

fn logical_eav_cmp(left: &Datom, right: &Datom) -> std::cmp::Ordering {
    left.entity
        .cmp(&right.entity)
        .then(left.attribute.cmp(&right.attribute))
        .then_with(|| left.value.index_cmp(&right.value))
}

#[derive(Clone, Copy, Debug, Default)]
struct NoHistoryTransactionFold {
    asserted: Option<bool>,
    saw_retraction: bool,
}

/// Precompute inclusive transaction intervals in which each attribute had
/// `:db/noHistory` enabled. Schema facts in one transaction are folded as one
/// declarative change: an asserted boolean is the resulting value, while a
/// retraction-only transaction falls back to false. In particular, retracting
/// `false` never means `true`.
fn no_history_opportunities(
    replayed: &[Datom],
    through_t: u64,
) -> Result<BTreeMap<u32, Vec<(u64, u64)>>, SemanticError> {
    let mut transaction_folds = BTreeMap::<(u32, u64), NoHistoryTransactionFold>::new();
    for datom in replayed {
        if datom.attribute != crate::DB_NO_HISTORY as u32 {
            continue;
        }
        let attribute = crate::schema_eid_to_attr_id(datom.entity).map_err(|_| {
            integrity_fault("authoritative noHistory schema fact has an invalid schema entity")
        })?;
        let value = match datom.value {
            crate::Value::Bool(value) => value,
            _ => {
                return Err(integrity_fault(
                    "authoritative noHistory schema history has a non-boolean value",
                ));
            }
        };
        let at = crate::tx_to_t(datom.tx)?;
        let fold = transaction_folds.entry((attribute, at)).or_default();
        if datom.added {
            if fold.asserted.is_some_and(|asserted| asserted != value) {
                return Err(integrity_fault(
                    "authoritative noHistory schema transaction asserts conflicting values",
                ));
            }
            fold.asserted = Some(value);
        } else {
            fold.saw_retraction = true;
        }
    }

    let mut changes = BTreeMap::<u32, Vec<(u64, bool)>>::new();
    for ((attribute, at), fold) in transaction_folds {
        let enabled = fold.asserted.unwrap_or(false);
        debug_assert!(fold.asserted.is_some() || fold.saw_retraction);
        changes.entry(attribute).or_default().push((at, enabled));
    }

    let mut intervals = BTreeMap::<u32, Vec<(u64, u64)>>::new();
    for (attribute, changes) in changes {
        let mut enabled_from = None::<u64>;
        let mut attribute_intervals = Vec::new();
        for (at, enabled) in changes {
            if at > through_t {
                break;
            }
            if !enabled {
                if let Some(start) = enabled_from.take() {
                    attribute_intervals.push((start, at.saturating_sub(1)));
                }
            } else if enabled_from.is_none() {
                enabled_from = Some(at);
            }
        }
        if let Some(start) = enabled_from {
            attribute_intervals.push((start, through_t));
        }
        if !attribute_intervals.is_empty() {
            intervals.insert(attribute, attribute_intervals);
        }
    }
    Ok(intervals)
}

fn validate_history_omission(
    omitted: &[Datom],
    opportunities: &BTreeMap<u32, Vec<(u64, u64)>>,
    through_t: u64,
) -> Result<(), SemanticError> {
    let mut retractions = Vec::<&Datom>::new();
    for datom in omitted {
        if !datom.added {
            retractions.push(datom);
            continue;
        }
        let Some(retraction) = retractions.pop() else {
            return Err(integrity_fault(
                "physical EAVT history omission is not reducible noHistory history",
            ));
        };
        let retraction_t = crate::tx_to_t(retraction.tx)?;
        let permitted = opportunities
            .get(&retraction.attribute)
            .is_some_and(|intervals| {
                let candidate = intervals.partition_point(|(_, end)| *end < retraction_t);
                intervals
                    .get(candidate)
                    .is_some_and(|(start, _)| *start <= through_t)
            });
        if !permitted {
            return Err(integrity_fault(
                "physical EAVT history omission had no noHistory=true opportunity",
            ));
        }
    }
    if !retractions.is_empty() {
        return Err(integrity_fault(
            "physical EAVT history omits an unpaired authoritative retraction",
        ));
    }
    Ok(())
}

fn integrity_fault(message: impl Into<String>) -> SemanticError {
    SemanticError::new(
        crate::ErrorCategory::Fault,
        "integrity/tree-history-projection-mismatch",
        message,
    )
}

#[cfg(test)]
mod history_projection_tests {
    use super::*;
    use crate::Value;
    use bigdecimal::BigDecimal;
    use std::str::FromStr;

    const USER_ATTRIBUTE: u32 = 100;

    fn datom(entity: u64, attribute: u32, value: Value, t: u64, added: bool) -> Datom {
        Datom {
            entity,
            attribute,
            value,
            tx: crate::t_to_tx(t).unwrap(),
            added,
        }
    }

    fn no_history(value: bool, t: u64, added: bool) -> Datom {
        datom(
            u64::from(USER_ATTRIBUTE),
            crate::DB_NO_HISTORY as u32,
            Value::Bool(value),
            t,
            added,
        )
    }

    fn canonical(mut datoms: Vec<Datom>) -> Vec<Datom> {
        datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        datoms
    }

    #[test]
    fn nested_no_history_omission_is_reducible_across_repeated_jobs() {
        let decimal = |spelling: &str| Value::BigDec(BigDecimal::from_str(spelling).unwrap());
        let replayed = canonical(vec![
            // Newest-to-oldest within one logical E/A/V group is R R A A.
            // Removing the inner pair exposes the outer pair to a later job.
            datom(1_000, USER_ATTRIBUTE, decimal("1.0"), 4, false),
            datom(1_000, USER_ATTRIBUTE, decimal("1.00"), 3, false),
            datom(1_000, USER_ATTRIBUTE, decimal("1.00"), 2, true),
            datom(1_000, USER_ATTRIBUTE, decimal("1.0"), 1, true),
            no_history(true, 3, true),
        ]);
        let physical = replayed
            .iter()
            .filter(|datom| datom.attribute == crate::DB_NO_HISTORY as u32)
            .cloned()
            .collect::<Vec<_>>();

        validate_physical_history_projection(&replayed, &physical, 5).unwrap();
    }

    #[test]
    fn physical_history_must_be_an_exact_replay_subsequence() {
        let replayed = canonical(vec![
            datom(1_000, USER_ATTRIBUTE, Value::Long(7), 2, false),
            datom(1_000, USER_ATTRIBUTE, Value::Long(7), 1, true),
            no_history(true, 2, true),
        ]);
        validate_physical_history_projection(&replayed, &replayed, 3).unwrap();

        let mut fabricated = replayed.clone();
        fabricated.push(datom(2_000, USER_ATTRIBUTE, Value::Long(9), 1, true));
        fabricated.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        assert_eq!(
            validate_physical_history_projection(&replayed, &fabricated, 3)
                .unwrap_err()
                .code,
            "integrity/tree-history-projection-mismatch"
        );

        let one_sided = replayed
            .iter()
            .filter(|datom| !(datom.attribute == USER_ATTRIBUTE && datom.added))
            .cloned()
            .collect::<Vec<_>>();
        assert_eq!(
            validate_physical_history_projection(&replayed, &one_sided, 3)
                .unwrap_err()
                .code,
            "integrity/tree-history-projection-mismatch"
        );
    }

    #[test]
    fn retraction_only_no_history_change_never_enables_omission() {
        let replayed = canonical(vec![
            datom(1_000, USER_ATTRIBUTE, Value::Long(7), 2, false),
            datom(1_000, USER_ATTRIBUTE, Value::Long(7), 1, true),
            // A standalone retraction of false yields the cardinality-one
            // default, false. It is not an assertion of true.
            no_history(false, 2, false),
        ]);
        let physical = replayed
            .iter()
            .filter(|datom| datom.attribute == crate::DB_NO_HISTORY as u32)
            .cloned()
            .collect::<Vec<_>>();
        assert_eq!(
            validate_physical_history_projection(&replayed, &physical, 3)
                .unwrap_err()
                .code,
            "integrity/tree-history-projection-mismatch"
        );
    }
}
