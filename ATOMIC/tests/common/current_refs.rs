//! Small observation helpers for current opaque-store fixtures. SQL faults in
//! the caller lock one resolved reference; no alternate engine codec lives here.
#![allow(dead_code)]
use atomic_core::storage::{PgBlockStore, root::DatabaseRoot};
use atomic_core::{DatabaseCatalog, PostgresConnectionConfig};

pub fn root_key(connection: &str, name: &str) -> String {
    let entry = DatabaseCatalog::connect(connection)
        .unwrap()
        .resolve(name)
        .unwrap();
    format!("databases/{}", entry.database_id)
}
pub fn root(connection: &str, name: &str) -> DatabaseRoot {
    let key = root_key(connection, name);
    let mut store =
        PgBlockStore::connect(&PostgresConnectionConfig::plaintext(connection)).unwrap();
    let reference = store.read_ref(&key).unwrap().unwrap();
    let id = reference.value.as_deref().unwrap().try_into().unwrap();
    DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap()).unwrap()
}
