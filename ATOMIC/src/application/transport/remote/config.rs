//! Environment/file loading for the supported verified-TLS transaction path.
//! Secrets are not command-line arguments or diagnostic values.
use crate::{ErrorCategory, RemoteAuthToken, RemoteClientConfig, SemanticError};
use std::fs::OpenOptions;
use std::io::Read;
use std::os::unix::fs::{MetadataExt, OpenOptionsExt};

const MAX_PEM: usize = 1024 * 1024;
fn error(code: &'static str) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Incorrect,
        code,
        "invalid or unavailable remote credential configuration",
    )
}
fn required(name: &str) -> Result<String, SemanticError> {
    std::env::var(name)
        .ok()
        .filter(|s| !s.is_empty())
        .ok_or_else(|| error("config/remote-environment"))
}
fn read_file(path: &str, secret: bool, limit: usize) -> Result<Vec<u8>, SemanticError> {
    let file = OpenOptions::new()
        .read(true)
        .custom_flags(libc::O_NOFOLLOW | libc::O_NONBLOCK)
        .open(path)
        .map_err(|_| error("config/remote-file"))?;
    let metadata = file.metadata().map_err(|_| error("config/remote-file"))?;
    // Regular files only: neither a FIFO nor a socket may stall configuration.
    if !metadata.is_file() || metadata.len() > limit as u64 {
        return Err(error("config/remote-file"));
    }
    // SAFETY: geteuid has no memory safety preconditions.
    if secret && (metadata.uid() != unsafe { libc::geteuid() } || metadata.mode() & 0o077 != 0) {
        return Err(error("config/remote-private-file"));
    }
    let mut bytes = Vec::new();
    file.take((limit + 1) as u64)
        .read_to_end(&mut bytes)
        .map_err(|_| error("config/remote-file"))?;
    if bytes.len() > limit {
        return Err(error("config/remote-file"));
    }
    Ok(bytes)
}
fn token() -> Result<RemoteAuthToken, SemanticError> {
    let bytes = read_file(&required("ATOMIC_REMOTE_TOKEN_FILE")?, true, 1024)?;
    let text = std::str::from_utf8(&bytes).map_err(|_| error("config/remote-token"))?;
    RemoteAuthToken::from_hex(text.trim()).map_err(|_| error("config/remote-token"))
}

/// Read owned private `ATOMIC_REMOTE_TOKEN_FILE` (64 hex digits) and
/// `ATOMIC_REMOTE_TLS_KEY` (PKCS8 PEM), plus public `ATOMIC_REMOTE_TLS_CERT`
/// (PEM chain). No plaintext transaction listener is available.
pub fn remote_server_credentials_from_env()
-> Result<(native_tls::Identity, RemoteAuthToken), SemanticError> {
    let cert = read_file(&required("ATOMIC_REMOTE_TLS_CERT")?, false, MAX_PEM)?;
    let key = read_file(&required("ATOMIC_REMOTE_TLS_KEY")?, true, MAX_PEM)?;
    let identity = native_tls::Identity::from_pkcs8(&cert, &key)
        .map_err(|_| error("config/remote-tls-identity"))?;
    Ok((identity, token()?))
}

/// Read owned private `ATOMIC_REMOTE_TOKEN_FILE`; optionally add the public
/// PEM trust anchor `ATOMIC_REMOTE_TLS_ROOT` to verified system TLS roots.
/// Server-name validation is mandatory and uses authenticated discovery data.
pub fn remote_client_config_from_env() -> Result<RemoteClientConfig, SemanticError> {
    let mut config = RemoteClientConfig::new(token()?)?;
    match std::env::var("ATOMIC_REMOTE_TLS_ROOT") {
        Ok(path) if !path.is_empty() => {
            let root = read_file(&path, false, MAX_PEM)?;
            config = config
                .with_root_certificate_pem(&root)
                .map_err(|_| error("config/remote-tls-root"))?;
        }
        Err(std::env::VarError::NotPresent) => {}
        _ => return Err(error("config/remote-environment")),
    }
    Ok(config)
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::Write;
    use std::os::unix::fs::{PermissionsExt, symlink};
    #[test]
    fn credential_files_are_bounded_owned_regular_private_and_redacted() {
        let mut file = tempfile::NamedTempFile::new().unwrap();
        file.write_all(b"secret-credential-marker").unwrap();
        let path = file.path().to_str().unwrap();
        assert_eq!(
            read_file(path, true, 128).unwrap(),
            b"secret-credential-marker"
        );
        assert!(read_file(path, true, 4).is_err());
        std::fs::set_permissions(path, std::fs::Permissions::from_mode(0o644)).unwrap();
        let error = read_file(path, true, 128).unwrap_err();
        assert!(!format!("{error:?}").contains("secret-credential-marker"));
        assert!(read_file(path, false, 128).is_ok());
        let directory = tempfile::tempdir().unwrap();
        let link = directory.path().join("link");
        symlink(path, &link).unwrap();
        assert!(read_file(link.to_str().unwrap(), false, 128).is_err());
        assert!(read_file(directory.path().to_str().unwrap(), false, 128).is_err());
    }
}
