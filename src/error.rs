use std::collections::BTreeMap;
use std::error::Error;
use std::fmt;

/// Stable top-level failure classes. Storage-specific retry information will
/// be added at the service boundary; the semantic kernel mainly emits
/// `Incorrect` and `Conflict`.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ErrorCategory {
    Incorrect,
    Forbidden,
    Unsupported,
    NotFound,
    Conflict,
    Busy,
    Unavailable,
    Interrupted,
    Fault,
    UnknownOutcome,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct SemanticError {
    pub category: ErrorCategory,
    pub code: &'static str,
    pub message: String,
    pub details: BTreeMap<String, String>,
}

impl SemanticError {
    pub fn incorrect(code: &'static str, message: impl Into<String>) -> Self {
        Self::new(ErrorCategory::Incorrect, code, message)
    }

    pub fn conflict(code: &'static str, message: impl Into<String>) -> Self {
        Self::new(ErrorCategory::Conflict, code, message)
    }

    pub fn new(category: ErrorCategory, code: &'static str, message: impl Into<String>) -> Self {
        Self {
            category,
            code,
            message: message.into(),
            details: BTreeMap::new(),
        }
    }

    pub fn detail(mut self, key: impl Into<String>, value: impl Into<String>) -> Self {
        self.details.insert(key.into(), value.into());
        self
    }
}

impl fmt::Display for SemanticError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}: {}", self.code, self.message)
    }
}

impl Error for SemanticError {}
