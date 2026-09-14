//! Database/request-scoped response faults; absent from production.
use std::collections::BTreeMap;
use std::sync::Mutex;

/// A deterministic seam around the response-observation boundary. Production
/// contains no injectable path; crate tests key one fault to one database and
/// request so parallel tests cannot alter ordinary work.
#[cfg(test)]
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum CommitObservationFault {
    AbsentUnknownOutcome,
    BeforePublication,
    AfterCommitBeforeResponse,
}

#[cfg(test)]
fn observation_faults() -> &'static Mutex<BTreeMap<(String, String), CommitObservationFault>> {
    static FAULTS: std::sync::OnceLock<Mutex<BTreeMap<(String, String), CommitObservationFault>>> =
        std::sync::OnceLock::new();
    FAULTS.get_or_init(|| Mutex::new(BTreeMap::new()))
}

#[cfg(test)]
pub(super) fn arm_observation_fault(
    database_id: &str,
    request_key: &str,
    fault: CommitObservationFault,
) {
    observation_faults()
        .lock()
        .expect("observation fault mutex poisoned")
        .insert((database_id.to_owned(), request_key.to_owned()), fault);
}

#[cfg(test)]
pub(crate) fn take_observation_fault(
    database_id: &str,
    request_key: &str,
) -> Option<CommitObservationFault> {
    observation_faults()
        .lock()
        .expect("observation fault mutex poisoned")
        .remove(&(database_id.to_owned(), request_key.to_owned()))
}
