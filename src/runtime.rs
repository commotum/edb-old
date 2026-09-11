//! Shared runtime configuration, observations and driver-error boundaries.
//! No SQL engine, schema migration or durable database policy lives here.
use crate::{ErrorCategory, ProgramLimits, SemanticError};

/// Cumulative canonical-program decoding work and bounded accounted cache
/// footprint. Hits cross no new decode or validation boundary.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct ProgramCacheStats {
    pub hits: u64,
    pub misses: u64,
    pub decodes: u64,
    pub validations: u64,
    pub evictions: u64,
    pub current_entries: usize,
    pub current_bytes: usize,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct TransactorLease {
    pub database_id: String,
    pub holder_id: String,
    pub epoch: u64,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct CapacityLimits {
    pub max_transaction_ops: usize,
    pub max_transaction_bytes: usize,
    pub max_history_transactions: u64,
    pub max_transaction_read_datoms: u64,
    pub max_transaction_read_bytes: u64,
    pub writer_tree_cache_entries: usize,
    pub writer_tree_cache_bytes: usize,
    pub program: ProgramLimits,
}

/// Representation-level state retained by one block writer.
///
/// Counts describe semantic objects, not allocator RSS. Eager-value counters
/// remain zero on the production path; the explicit pure oracle is separate.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct WriterResidencyStats {
    pub eager_database_values: usize,
    pub eager_current_facts: usize,
    pub eager_history_datoms: usize,
    pub recent_datoms: u64,
    pub recent_accounted_bytes: u64,
    pub tree_cache_entries: usize,
    pub tree_cache_bytes: usize,
    /// Child references retained by the eight decoded native roots, outside
    /// the discardable node cache.
    pub resident_tree_root_children: usize,
    /// Estimated decoded root allocations, including child vectors and
    /// recursively owned routing-key data. Canonical payload bytes are not
    /// retained here and are not included in this figure.
    pub resident_tree_root_estimated_bytes: u64,
    /// Resident authenticated schema/ident projections. These are expected to
    /// scale with metadata cardinality, so their size is explicit rather than
    /// hidden inside the database-size-independent writer claim.
    pub resident_schema_attributes: usize,
    pub resident_schema_information_datoms: usize,
    pub resident_schema_estimated_bytes: u64,
    pub resident_ident_names: usize,
    pub resident_ident_entities: usize,
    pub resident_ident_estimated_bytes: u64,
    pub native_root_reads: u64,
    pub native_directory_reads: u64,
    pub native_leaf_reads: u64,
    pub publication_revision: u64,
    /// Logical datoms delivered across the complete last committed
    /// transaction, from persisted-function expansion through assessment
    /// and successor validation/predicates.
    /// This is not a physical PostgreSQL/tree-node I/O counter.
    pub last_transaction_read_datoms: u64,
    /// Deterministic retained width of those delivered logical datoms.
    pub last_transaction_read_bytes: u64,
    /// Actual descriptor/dependency visits in the last fresh transaction's
    /// assessment, including unchanged-schema validation and tuple helpers.
    /// These are process-local work counts, not persisted bytes or RSS.
    pub last_schema_transition_attributes: u64,
    pub last_schema_projection_attributes: u64,
    pub last_schema_validation_attributes: u64,
    pub last_schema_validation_predicates: u64,
    pub last_schema_validation_tuple_types: u64,
    pub last_schema_validation_tuple_constituents: u64,
    pub last_schema_validation_installations: u64,
    pub last_schema_reuses: u64,
    pub last_schema_dependency_lookups: u64,
    pub last_schema_dependency_edges: u64,
    pub last_schema_composite_candidates: u64,
    /// Successful datoms delivered by underlying exact-prefix cursors. Memo
    /// replays increase logical reads but leave this source count unchanged.
    pub last_transaction_source_read_datoms: u64,
    pub last_transaction_source_read_bytes: u64,
    pub last_transaction_prefix_memo_hits: u64,
    pub last_transaction_prefix_memo_misses: u64,
    pub last_transaction_prefix_memo_admissions: u64,
    pub last_transaction_prefix_memo_rejections: u64,
    pub last_transaction_prefix_memo_peak_entries: usize,
    pub last_transaction_prefix_memo_peak_bytes: u64,
    /// Exact native cursor source work during the last committed operation.
    /// SQL counts are successful immutable-node rows, and byte counts are
    /// canonical node payloads rather than PostgreSQL/wire overhead.
    pub last_native_cursor_ranges: u64,
    pub last_native_cache_hits: u64,
    pub last_native_cache_misses: u64,
    pub last_native_sql_root_reads: u64,
    pub last_native_sql_directory_reads: u64,
    pub last_native_sql_leaf_reads: u64,
    pub last_native_sql_reads: u64,
    pub last_native_sql_read_bytes: u64,
    pub last_native_recent_datoms_examined: u64,
    pub last_native_recent_datoms_yielded: u64,
}

impl Default for CapacityLimits {
    fn default() -> Self {
        Self {
            max_transaction_ops: 100_000,
            max_transaction_bytes: 64 * 1024 * 1024,
            max_history_transactions: i64::MAX as u64,
            max_transaction_read_datoms: 1_000_000,
            max_transaction_read_bytes: 64 * 1024 * 1024,
            writer_tree_cache_entries: 4_096,
            writer_tree_cache_bytes: 64 * 1024 * 1024,
            program: ProgramLimits::default(),
        }
    }
}

pub(crate) fn postgres_error(code: &'static str, error: postgres::Error) -> SemanticError {
    let database_error = error.as_db_error();
    let sqlstate = database_error.map(|error| error.code().code());
    // PostgreSQL can report restart/failover as a server SQLSTATE before the
    // socket disappears. Those are transport availability, not corrupt SQL.
    let transport = sqlstate.is_none()
        || sqlstate.is_some_and(|state| {
            state.starts_with("08") || matches!(state, "57P01" | "57P02" | "57P03")
        });
    let category = match sqlstate {
        Some("23505" | "40001" | "40P01") => ErrorCategory::Conflict,
        Some("42501") => ErrorCategory::Forbidden,
        Some("57014") => ErrorCategory::Interrupted,
        Some("55P03") => ErrorCategory::Busy,
        Some(state) if state.starts_with("08") || matches!(state, "57P01" | "57P02" | "57P03") => {
            ErrorCategory::Unavailable
        }
        Some(_) => ErrorCategory::Fault,
        None => ErrorCategory::Unavailable,
    };
    let mut semantic = if let Some(database_error) = database_error {
        let mut semantic = SemanticError::new(
            category,
            code,
            sanitize_postgres_message(database_error.message()),
        )
        .detail("postgres_sqlstate", database_error.code().code());
        for (name, value) in [
            ("postgres_constraint", database_error.constraint()),
            ("postgres_schema", database_error.schema()),
            ("postgres_table", database_error.table()),
            ("postgres_column", database_error.column()),
            ("postgres_routine", database_error.routine()),
        ] {
            if let Some(value) = value {
                semantic = semantic.detail(name, sanitize_postgres_identifier(value));
            }
        }
        semantic
    } else {
        // Driver/transport Display strings can contain connection locators.
        // The stable operation code and transport marker retain actionable
        // structure without echoing credentials or DSNs.
        SemanticError::new(category, code, "PostgreSQL transport error")
    };
    if transport {
        semantic = semantic.detail("postgres_transport", "true");
    }
    semantic
}

fn sanitize_postgres_identifier(value: &str) -> String {
    value
        .chars()
        .filter(|character| !character.is_control())
        .take(128)
        .collect()
}

fn sanitize_postgres_message(value: &str) -> String {
    let mut output = String::with_capacity(value.len().min(512));
    let mut quoted = None;
    for character in value.chars() {
        if output.len() >= 512 {
            break;
        }
        if let Some(delimiter) = quoted {
            if character == delimiter {
                quoted = None;
                output.push_str("<redacted>");
            }
            continue;
        }
        if matches!(character, '\'' | '"') {
            quoted = Some(character);
        } else if character.is_control() {
            output.push(' ');
        } else {
            output.push(character);
        }
    }
    if quoted.is_some() {
        output.push_str("<redacted>");
    }
    let output = output.trim();
    if output.is_empty() {
        "PostgreSQL server error".to_owned()
    } else {
        output.to_owned()
    }
}

pub(crate) fn is_postgres_connection_error(error: &SemanticError) -> bool {
    error.category == ErrorCategory::Unavailable
        && error
            .details
            .get("postgres_transport")
            .is_some_and(|value| value == "true")
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn postgres_diagnostics_redact_quoted_values_and_control_text() {
        assert_eq!(
            sanitize_postgres_message(
                "duplicate key value violates unique constraint \"secret@example.com\"\n"
            ),
            "duplicate key value violates unique constraint <redacted>"
        );
        assert_eq!(sanitize_postgres_identifier("safe\nname"), "safename");
        assert_eq!(sanitize_postgres_message("\"unterminated"), "<redacted>");
    }
}
