//! Bounded diagnostic of one existing database's ordinary consolidation.
//! ATOMIC_PROFILE_NO_PUBLISH=1 uses the supported AfterSegments fault so the
//! same immutable base/tail can be measured repeatedly without moving a head.
use atomic_core::{IndexBuildFault, PostgresIndexer};
use std::time::Instant;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let connection = std::env::var("ATOMIC_POSTGRES_URL")?;
    let database = std::env::var("ATOMIC_DATABASE_ID")?;
    let no_publish = std::env::var_os("ATOMIC_PROFILE_NO_PUBLISH").is_some();
    let mut indexer = PostgresIndexer::connect(&connection, database)?;
    let start = Instant::now();
    let result = indexer.consolidate_with_fault(if no_publish {
        IndexBuildFault::AfterSegments
    } else {
        IndexBuildFault::None
    });
    println!(
        "index_profile_elapsed_ms={} result={result:?}",
        start.elapsed().as_millis()
    );
    if let Ok(status) = std::fs::read_to_string("/proc/self/status") {
        for line in status
            .lines()
            .filter(|line| line.starts_with("VmRSS:") || line.starts_with("VmHWM:"))
        {
            println!("index_profile_memory {line}");
        }
    }
    match result {
        Err(error) if no_publish && error.code == "index/injected-failure" => Ok(()),
        result => result.map(|_| ()).map_err(Into::into),
    }
}
