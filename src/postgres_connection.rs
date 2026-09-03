use crate::{ErrorCategory, SemanticError};
use native_tls::{Certificate, Protocol, TlsConnector};
use postgres::config::{Host, SslMode};
use postgres::{Client, Config, NoTls};
use postgres_native_tls::{MakeTlsConnector, set_postgresql_alpn};
use std::fmt;
use std::sync::Arc;
use std::time::Duration;

/// One concrete PostgreSQL connection policy shared by every runtime role.
///
/// The legacy string constructors remain available and deliberately select
/// `plaintext`. Production callers can instead select `require_tls`, which
/// forces PostgreSQL TLS negotiation even if the parameter string says
/// `sslmode=disable`. TLS 1.2 is the minimum; server certificates and
/// hostnames are always verified, and there is intentionally no "accept
/// invalid certificate" switch.
#[derive(Clone, Eq, PartialEq)]
pub struct PostgresConnectionConfig {
    parameters: Arc<str>,
    root_certificates: Option<Arc<[Vec<u8>]>>,
}

impl fmt::Debug for PostgresConnectionConfig {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter
            .debug_struct("PostgresConnectionConfig")
            .field(
                "transport",
                &if self.tls_required() {
                    "verified-tls"
                } else {
                    "plaintext"
                },
            )
            .field(
                "custom_root_certificates",
                &self
                    .root_certificates
                    .as_ref()
                    .map_or(0, |certificates| certificates.len()),
            )
            .finish_non_exhaustive()
    }
}

impl PostgresConnectionConfig {
    /// Preserve the existing local/development connection behavior.
    pub fn plaintext(parameters: impl Into<String>) -> Self {
        Self {
            parameters: Arc::from(parameters.into()),
            root_certificates: None,
        }
    }

    /// Require a verified TLS session using the platform trust store.
    pub fn require_tls(parameters: impl Into<String>) -> Self {
        Self {
            parameters: Arc::from(parameters.into()),
            root_certificates: Some(Arc::default()),
        }
    }

    /// Add a PEM-encoded trust root used in addition to platform roots.
    ///
    /// This accepts one certificate per call so callers can construct the
    /// same explicit trust set they would place in a Datomic/JVM trust store.
    pub fn with_root_certificate_pem(
        mut self,
        certificate_pem: impl AsRef<[u8]>,
    ) -> Result<Self, SemanticError> {
        let Some(certificates) = &self.root_certificates else {
            return Err(SemanticError::incorrect(
                "postgres/tls-not-enabled",
                "root certificates require a TLS-enabled PostgreSQL connection",
            ));
        };
        let certificate_pem = certificate_pem.as_ref();
        Certificate::from_pem(certificate_pem).map_err(|_| invalid_root_certificate())?;
        let mut updated = certificates.iter().cloned().collect::<Vec<_>>();
        updated.push(certificate_pem.to_vec());
        self.root_certificates = Some(updated.into());
        Ok(self)
    }

    pub fn tls_required(&self) -> bool {
        self.root_certificates.is_some()
    }

    /// Open a caller-owned PostgreSQL client under this transport policy.
    pub fn connect(&self) -> Result<Client, SemanticError> {
        self.connect_for("postgres/connect")
    }

    pub(crate) fn connect_for(&self, operation: &'static str) -> Result<Client, SemanticError> {
        self.connect_for_with_timeout(operation, None)
    }

    /// Open a connection whose socket-level attempt cannot exceed the caller's
    /// remaining operation deadline. `None` preserves the configured/default
    /// PostgreSQL timeout for constructors without an outer deadline.
    pub(crate) fn connect_for_with_timeout(
        &self,
        operation: &'static str,
        timeout: Option<Duration>,
    ) -> Result<Client, SemanticError> {
        let mut config = self.parameters.parse::<Config>().map_err(|_| {
            SemanticError::incorrect(
                "postgres/invalid-connection-config",
                "invalid PostgreSQL connection configuration",
            )
            .detail("operation", operation)
        })?;
        if let Some(timeout) = timeout {
            config.connect_timeout(timeout);
        }
        let Some(root_certificates) = &self.root_certificates else {
            return config
                .connect(NoTls)
                .map_err(|error| crate::postgres::postgres_error(operation, error));
        };
        if config
            .get_hosts()
            .iter()
            .any(|host| matches!(host, Host::Unix(_)))
        {
            return Err(SemanticError::incorrect(
                "postgres/tls-requires-tcp",
                "PostgreSQL TLS requires a TCP host, not a Unix-domain socket",
            )
            .detail("operation", operation));
        }
        config.ssl_mode(SslMode::Require);

        let mut builder = TlsConnector::builder();
        builder.min_protocol_version(Some(Protocol::Tlsv12));
        set_postgresql_alpn(&mut builder);
        for certificate_pem in root_certificates.iter() {
            let certificate =
                Certificate::from_pem(certificate_pem).map_err(|_| invalid_root_certificate())?;
            builder.add_root_certificate(certificate);
        }
        let connector = builder.build().map_err(|_| {
            SemanticError::incorrect(
                "postgres/invalid-tls-config",
                "invalid PostgreSQL TLS configuration",
            )
            .detail("operation", operation)
        })?;
        config
            .connect(MakeTlsConnector::new(connector))
            .map_err(|_| {
                SemanticError::new(
                    ErrorCategory::Unavailable,
                    "postgres/tls-connect",
                    "verified PostgreSQL TLS connection failed",
                )
                .detail("operation", operation)
                .detail("postgres_transport", "true")
            })
    }
}

fn invalid_root_certificate() -> SemanticError {
    SemanticError::incorrect(
        "postgres/invalid-tls-root-certificate",
        "PostgreSQL TLS root certificate is not valid PEM",
    )
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::time::Instant;

    #[test]
    fn caller_deadline_caps_an_unreachable_postgres_host() {
        // TEST-NET-1 is reserved for documentation and must not host a server.
        // Some CI networks reject it immediately; a blackholed route exercises
        // the configured timeout. Both outcomes must remain inside the bound.
        let config = PostgresConnectionConfig::plaintext(
            "host=192.0.2.1 port=9 user=deadline dbname=deadline",
        );
        let started = Instant::now();
        let error = match config
            .connect_for_with_timeout("postgres/deadline-witness", Some(Duration::from_millis(50)))
        {
            Ok(_) => panic!("unreachable host unexpectedly accepted PostgreSQL"),
            Err(error) => error,
        };
        let elapsed = started.elapsed();
        assert!(crate::postgres::is_postgres_connection_error(&error));
        assert!(
            elapsed < Duration::from_millis(300),
            "unreachable host exceeded the caller's deadline envelope: {elapsed:?}"
        );
    }
}
