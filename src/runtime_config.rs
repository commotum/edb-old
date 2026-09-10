//! Shared environment configuration for supported executables and applications.
//! Credentials stay in the caller's environment, never in command-line flags or
//! diagnostics. This selects the existing PostgreSQL transport/I/O policy.
use crate::{PostgresConnectionConfig, PostgresIoPolicy, SemanticError};
use std::time::Duration;

/// Read `ATOMIC_POSTGRES_URL` and an explicit `ATOMIC_POSTGRES_TRANSPORT`
/// (`tls` or `plaintext`). TLS validates certificates and hostnames; optional
/// `ATOMIC_POSTGRES_TLS_ROOT` supplies one additional PEM trust root.
///
/// Optional positive integer settings: `ATOMIC_CONNECT_TIMEOUT_MS`,
/// `ATOMIC_STATEMENT_TIMEOUT_MS`, `ATOMIC_LOCK_TIMEOUT_MS`,
/// `ATOMIC_TCP_USER_TIMEOUT_MS`, `ATOMIC_KEEPALIVES_IDLE_SECONDS`,
/// `ATOMIC_KEEPALIVES_INTERVAL_SECONDS`, `ATOMIC_KEEPALIVES_RETRIES`.
/// `ATOMIC_KEEPALIVES` accepts only `true` or `false`. Omitted I/O settings
/// preserve the library/driver policy; no universal production deadline is added.
pub fn postgres_config_from_env() -> Result<PostgresConnectionConfig, SemanticError> {
    config_from(|key| match std::env::var(key) {
        Ok(value) => Ok(Some(value)),
        Err(std::env::VarError::NotPresent) => Ok(None),
        Err(_) => Err(invalid("config/environment", key)),
    })
}

fn invalid(code: &'static str, key: &'static str) -> SemanticError {
    SemanticError::incorrect(code, format!("missing or invalid {key}"))
}

fn config_from(
    lookup: impl Fn(&'static str) -> Result<Option<String>, SemanticError>,
) -> Result<PostgresConnectionConfig, SemanticError> {
    let required = |key| {
        lookup(key)?
            .filter(|value| !value.is_empty())
            .ok_or_else(|| invalid("config/environment", key))
    };
    let parameters = required("ATOMIC_POSTGRES_URL")?;
    let mut config = match required("ATOMIC_POSTGRES_TRANSPORT")?.as_str() {
        "tls" => PostgresConnectionConfig::require_tls(parameters),
        "plaintext" => PostgresConnectionConfig::plaintext(parameters),
        _ => {
            return Err(invalid(
                "config/postgres-transport",
                "ATOMIC_POSTGRES_TRANSPORT",
            ));
        }
    };
    if let Some(path) = lookup("ATOMIC_POSTGRES_TLS_ROOT")? {
        if !config.tls_required() {
            return Err(invalid(
                "config/tls-root-requires-tls",
                "ATOMIC_POSTGRES_TLS_ROOT",
            ));
        }
        let bytes = std::fs::read(path)
            .map_err(|_| invalid("config/tls-root-read", "ATOMIC_POSTGRES_TLS_ROOT"))?;
        config = config.with_root_certificate_pem(bytes)?;
    }
    let number = |key| -> Result<Option<u64>, SemanticError> {
        lookup(key)?
            .map(|value| {
                value
                    .parse::<u64>()
                    .ok()
                    .filter(|n| *n > 0)
                    .ok_or_else(|| invalid("config/positive-integer", key))
            })
            .transpose()
    };
    let keepalives = lookup("ATOMIC_KEEPALIVES")?
        .map(|value| match value.as_str() {
            "true" => Ok(true),
            "false" => Ok(false),
            _ => Err(invalid("config/boolean", "ATOMIC_KEEPALIVES")),
        })
        .transpose()?;
    config.with_io_policy(PostgresIoPolicy {
        connect_timeout: number("ATOMIC_CONNECT_TIMEOUT_MS")?.map(Duration::from_millis),
        statement_timeout: number("ATOMIC_STATEMENT_TIMEOUT_MS")?.map(Duration::from_millis),
        lock_timeout: number("ATOMIC_LOCK_TIMEOUT_MS")?.map(Duration::from_millis),
        tcp_user_timeout: number("ATOMIC_TCP_USER_TIMEOUT_MS")?.map(Duration::from_millis),
        keepalives,
        keepalives_idle: number("ATOMIC_KEEPALIVES_IDLE_SECONDS")?.map(Duration::from_secs),
        keepalives_interval: number("ATOMIC_KEEPALIVES_INTERVAL_SECONDS")?.map(Duration::from_secs),
        keepalives_retries: number("ATOMIC_KEEPALIVES_RETRIES")?
            .map(|value| {
                u32::try_from(value)
                    .map_err(|_| invalid("config/positive-integer", "ATOMIC_KEEPALIVES_RETRIES"))
            })
            .transpose()?,
    })
}

#[cfg(test)]
mod tests {
    use super::*;

    fn parse(entries: &[(&str, &str)]) -> Result<PostgresConnectionConfig, SemanticError> {
        config_from(|key| {
            Ok(entries
                .iter()
                .find(|(k, _)| *k == key)
                .map(|(_, v)| v.to_string()))
        })
    }

    #[test]
    fn explicit_transport_and_no_secret_diagnostics() {
        let secret = "host=private-host user=private-user password=private-password";
        assert!(parse(&[("ATOMIC_POSTGRES_URL", secret)]).is_err());
        for transport in ["tls", "plaintext"] {
            let config = parse(&[
                ("ATOMIC_POSTGRES_URL", secret),
                ("ATOMIC_POSTGRES_TRANSPORT", transport),
            ])
            .unwrap();
            assert_eq!(config.tls_required(), transport == "tls");
            assert!(!format!("{config:?}").contains("private-"));
        }
        let error = parse(&[
            ("ATOMIC_POSTGRES_URL", secret),
            ("ATOMIC_POSTGRES_TRANSPORT", "private-secret"),
        ])
        .unwrap_err();
        assert!(!format!("{error:?}").contains("private-"));
    }

    #[test]
    fn shares_io_policy_and_rejects_invalid_values() {
        let mut entries = vec![
            ("ATOMIC_POSTGRES_URL", "host=localhost"),
            ("ATOMIC_POSTGRES_TRANSPORT", "plaintext"),
        ];
        entries.push(("ATOMIC_STATEMENT_TIMEOUT_MS", "12"));
        entries.push(("ATOMIC_KEEPALIVES", "false"));
        let config = parse(&entries).unwrap();
        assert_eq!(
            config.io_policy().statement_timeout,
            Some(Duration::from_millis(12))
        );
        assert_eq!(config.io_policy().keepalives, Some(false));
        for value in ["0", "-1", "secret", "18446744073709551616"] {
            entries[2].1 = value;
            assert!(parse(&entries).is_err());
        }
        assert!(
            parse(&[
                ("ATOMIC_POSTGRES_URL", "host=localhost"),
                ("ATOMIC_POSTGRES_TRANSPORT", "plaintext"),
                ("ATOMIC_POSTGRES_TLS_ROOT", "/secret-path")
            ])
            .is_err()
        );
    }
}
