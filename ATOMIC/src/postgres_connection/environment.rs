//! Shared environment configuration for supported executables and applications.
//! Credentials stay in the caller's environment, never in command-line flags or
//! diagnostics. This selects the existing PostgreSQL transport/I/O policy.
use crate::{PostgresConnectionConfig, PostgresIoPolicy, SemanticError};
use std::time::Duration;

/// Read `ATOMIC_POSTGRES_URL`, whose `sslmode` selects the transport. TCP
/// defaults to verified TLS; `sslmode=disable` explicitly selects plaintext. Optional
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
    if lookup("ATOMIC_POSTGRES_TRANSPORT")?.is_some() {
        return Err(invalid(
            "config/use-postgres-sslmode",
            "ATOMIC_POSTGRES_TRANSPORT",
        ));
    }
    let mut config = PostgresConnectionConfig::parse(parameters)?;
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
    config = config.with_io_policy(PostgresIoPolicy {
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
    })?;
    let ssd_directory = lookup("ATOMIC_SSD_CACHE_DIR")?;
    let ssd_entries = number("ATOMIC_SSD_CACHE_ENTRIES")?;
    let ssd_bytes = number("ATOMIC_SSD_CACHE_BYTES")?;
    if let Some(directory) = ssd_directory {
        if directory.is_empty() {
            return Err(invalid("config/environment", "ATOMIC_SSD_CACHE_DIR"));
        }
        let defaults = crate::SsdCacheLimits::default();
        let max_entries = ssd_entries
            .map(usize::try_from)
            .transpose()
            .map_err(|_| invalid("config/positive-integer", "ATOMIC_SSD_CACHE_ENTRIES"))?
            .unwrap_or(defaults.max_entries);
        config = config.with_ssd_cache(crate::SsdCacheConfig {
            directory: directory.into(),
            limits: crate::SsdCacheLimits {
                max_entries,
                max_bytes: ssd_bytes.unwrap_or(defaults.max_bytes),
            },
        });
    } else if ssd_entries.is_some() || ssd_bytes.is_some() {
        return Err(invalid(
            "config/ssd-directory-required",
            "ATOMIC_SSD_CACHE_DIR",
        ));
    }
    Ok(config)
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
    fn ssd_requires_opt_in_directory_and_positive_budgets() {
        let mut entries = vec![("ATOMIC_POSTGRES_URL", "host=localhost sslmode=disable")];
        assert!(parse(&entries).unwrap().ssd_cache_config().is_none());
        entries.push(("ATOMIC_SSD_CACHE_ENTRIES", "12"));
        assert!(parse(&entries).is_err());
        entries.push(("ATOMIC_SSD_CACHE_DIR", "/private-cache"));
        assert_eq!(
            parse(&entries)
                .unwrap()
                .ssd_cache_config()
                .unwrap()
                .limits
                .max_entries,
            12
        );
        entries.push(("ATOMIC_SSD_CACHE_BYTES", "0"));
        assert!(parse(&entries).is_err());
    }

    #[test]
    fn explicit_transport_and_no_secret_diagnostics() {
        let secret = "host=private-host user=private-user password=private-password";
        assert!(
            parse(&[("ATOMIC_POSTGRES_URL", secret)])
                .unwrap()
                .tls_required()
        );
        for transport in ["require", "disable"] {
            let parameters = format!("{secret} sslmode={transport}");
            let config = parse(&[("ATOMIC_POSTGRES_URL", &parameters)]).unwrap();
            assert_eq!(config.tls_required(), transport == "require");
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
        let mut entries = vec![("ATOMIC_POSTGRES_URL", "host=localhost sslmode=disable")];
        entries.push(("ATOMIC_STATEMENT_TIMEOUT_MS", "12"));
        entries.push(("ATOMIC_KEEPALIVES", "false"));
        let config = parse(&entries).unwrap();
        assert_eq!(
            config.io_policy().statement_timeout,
            Some(Duration::from_millis(12))
        );
        assert_eq!(config.io_policy().keepalives, Some(false));
        for value in ["0", "-1", "secret", "18446744073709551616"] {
            entries[1].1 = value;
            assert!(parse(&entries).is_err());
        }
        assert!(
            parse(&[
                ("ATOMIC_POSTGRES_URL", "host=localhost sslmode=disable"),
                ("ATOMIC_POSTGRES_TLS_ROOT", "/secret-path")
            ])
            .is_err()
        );
    }
}
