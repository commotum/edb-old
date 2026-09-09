//! Bounded zero-retention GC experiment for an existing stopped disposable DB.
//! Required: ATOMIC_POSTGRES_URL, ATOMIC_DATABASE_ID, ATOMIC_ALLOW_DISPOSABLE_GC=1.
//! The actual PostgreSQL catalog name must start with atomic_goal6_. GC is
//! global within the selected installation schema: coordinate all its users.
//! Optional positive ATOMIC_GC_MAX_BATCHES (128), ATOMIC_GC_WALL_SECONDS (120)
//! apply separately before/after releasing the old snapshot. The wall budget
//! is checked between calls, not advertised as SQL/Rust preemption.
//! Production guidance remains RECOMMENDED_GARBAGE_COLLECTION_AGE (30 days).
//! This deliberately creates one Long marker under existing attribute 1002.
//! Quiescence means no currently eligible work, not all old roots reclaimed:
//! exact receipt archives and held snapshots deliberately retain their nodes.
use atomic_core::{
    CapacityLimits, Cardinality, DatabaseValue, Datom, EntityRef, GarbageInventory, IndexBoundary,
    IndexComponents, Peer, PostgresConnectionConfig, PostgresIndexer, PostgresIoPolicy,
    PostgresOperator, RECOMMENDED_GARBAGE_COLLECTION_AGE, TransactionRequest, TransactionService,
    TransactionServiceConfig, TxOp, Value, ValueType,
};
use postgres::Client;
use std::collections::BTreeMap;
use std::error::Error;
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

type Result<T> = std::result::Result<T, Box<dyn Error>>;
const ATTRIBUTE: u32 = 1_002;
const SAMPLE: usize = 16;
const WAIT: Duration = Duration::from_secs(120);

fn main() -> Result<()> {
    if std::env::var("ATOMIC_ALLOW_DISPOSABLE_GC").as_deref() != Ok("1") {
        return Err("zero-retention GC requires ATOMIC_ALLOW_DISPOSABLE_GC=1".into());
    }
    let postgres = std::env::var("ATOMIC_POSTGRES_URL")?;
    let database = std::env::var("ATOMIC_DATABASE_ID")?;
    let max_batches = positive("ATOMIC_GC_MAX_BATCHES", 128)?;
    let wall = Duration::from_secs(positive("ATOMIC_GC_WALL_SECONDS", 120)?);
    let config =
        PostgresConnectionConfig::plaintext(&postgres).with_io_policy(PostgresIoPolicy {
            connect_timeout: Some(Duration::from_secs(2)),
            statement_timeout: Some(Duration::from_secs(30)),
            lock_timeout: Some(Duration::from_secs(5)),
            ..PostgresIoPolicy::default()
        })?;
    let mut sql = config.connect()?;
    let row = sql.query_one("SELECT current_database(), current_schema()", &[])?;
    let catalog: String = row.get(0);
    if !catalog.starts_with("atomic_goal6_") {
        return Err("refusing GC outside an atomic_goal6_ disposable PostgreSQL catalog".into());
    }
    let schema = row
        .get::<_, Option<String>>(1)
        .ok_or("GC connection search_path has no installation schema")?;
    require_stopped(&mut sql)?;
    let began = Instant::now();
    println!(
        "gc_target_catalog={catalog} schema={schema} database={database} retention_seconds=0 production_recommended_seconds={} max_batches_per_window={max_batches} wall_seconds_per_window={} memory={}",
        RECOMMENDED_GARBAGE_COLLECTION_AGE.as_secs(),
        wall.as_secs(),
        memory()?
    );

    // No cache retention and raw cursors prevent a post-GC verification from
    // succeeding merely because the sampled leaves were already cached.
    let old_peer = Peer::connect_configured_with_cache_limits(&config, &database, 0, 0)?;
    let old = old_peer.database_value();
    let attribute = old.schema().attribute(ATTRIBUTE)?;
    if attribute.value_type != ValueType::Long || attribute.cardinality != Cardinality::One {
        return Err("GC workflow requires existing cardinality-one Long attribute 1002".into());
    }
    let old_sample = sample(&old, SAMPLE)?;
    let old_history_sample = sample(&old.clone().history(), SAMPLE)?;
    if old_sample.is_empty() {
        return Err("GC workflow requires existing application facts under attribute 1002".into());
    }
    let old_hashes = old_peer.pinned_manifest_hashes();
    if old_hashes.len() != 1 {
        return Err("expected one initial native physical snapshot root".into());
    }
    let old_manifest = old_hashes[0];
    let old_basis = old.basis_t();
    let stamp = SystemTime::now().duration_since(UNIX_EPOCH)?.as_micros();
    let marker_value = i64::try_from(stamp)?;
    let writer = TransactionService::start_configured(
        TransactionServiceConfig {
            connection: postgres.clone(),
            database_id: database.clone(),
            holder_id: format!("gc-workflow-{}-{stamp}", std::process::id()),
            lease_duration: Duration::from_secs(15),
            renew_interval: Duration::from_secs(1),
            queue_capacity: 4,
            capacity_limits: CapacityLimits::default(),
        },
        config.clone(),
    )?;
    let report = writer.client().transact(
        TransactionRequest::new(
            format!("gc-marker-{stamp}"),
            vec![TxOp::Add {
                entity: EntityRef::Temp("gc-marker".into()),
                attribute: ATTRIBUTE,
                value: Value::Long(marker_value).into(),
            }],
        ),
        WAIT,
    )?;
    let marker_entity = report.tempids["gc-marker"];
    let current_basis = report.basis_t;
    drop(report); // Receipts must not accidentally retain the old physical pin.
    PostgresIndexer::connect_configured(&config, &database)?.consolidate()?;
    let current_peer = Peer::connect_configured_with_cache_limits(&config, &database, 0, 0)?;
    let current = current_peer.sync_index(current_basis, WAIT)?;
    writer.shutdown();
    require_stopped(&mut sql)?;
    assert_eq!(current.basis_t(), current_basis);
    assert_eq!(current_basis, old_basis + 1);
    assert!(
        !current_peer
            .pinned_manifest_hashes()
            .contains(&old_manifest)
    );
    let current_sample = sample(&current, old_sample.len())?;
    assert_eq!(current_sample, old_sample);
    assert_marker(&old, marker_entity, None)?;
    assert_marker(&current, marker_entity, Some(marker_value))?;
    println!(
        "gc_snapshot_prepared old_t={old_basis} current_t={current_basis} current_sample={} history_sample={} memory={}",
        old_sample.len(),
        old_history_sample.len(),
        memory()?
    );
    let mut operator = PostgresOperator::connect_configured(&config)?;
    let pinned = window(&mut operator, &mut sql, "old-pinned", max_batches, wall)?;
    let protected: bool = sql.query_one(
        "SELECT EXISTS (SELECT 1 FROM atomic_tree_publications WHERE database_id=$1 AND manifest_hash=$2)",
        &[&database, &&old_manifest[..]],
    )?.get(0);
    assert!(protected, "GC retired the connected old physical snapshot");
    assert_eq!(sample(&old, old_sample.len())?, old_sample);
    assert_eq!(
        sample(&old.clone().history(), old_history_sample.len())?,
        old_history_sample
    );
    assert_eq!(sample(&current, current_sample.len())?, current_sample);
    assert_marker(&old, marker_entity, None)?;
    assert_marker(&current, marker_entity, Some(marker_value))?;
    assert_eq!(old_peer.load_stats().compatibility_materializations, 0);
    println!(
        "gc_old_snapshot_verified=true native_load={:?} cache={:?}",
        old_peer.load_stats(),
        old_peer.cache_stats()
    );

    drop(old);
    drop(old_peer);
    let released = window(&mut operator, &mut sql, "old-released", max_batches, wall)?;
    assert_eq!(sample(&current, current_sample.len())?, current_sample);
    assert_marker(&current, marker_entity, Some(marker_value))?;
    // Independent lazy reopen after both windows verifies durable information,
    // not just a retained in-process endpoint.
    let reopened = Peer::connect_configured_with_cache_limits(&config, &database, 0, 0)?;
    let value = reopened.database_value();
    assert_eq!(value.basis_t(), current_basis);
    assert_eq!(sample(&value, current_sample.len())?, current_sample);
    assert_eq!(
        sample(&value.clone().history(), old_history_sample.len())?,
        old_history_sample
    );
    assert_marker(&value, marker_entity, Some(marker_value))?;
    assert_eq!(current_peer.load_stats().compatibility_materializations, 0);
    assert_eq!(reopened.load_stats().compatibility_materializations, 0);
    require_stopped(&mut sql)?;
    let old_still_published: bool = sql.query_one(
        "SELECT EXISTS (SELECT 1 FROM atomic_tree_publications WHERE database_id=$1 AND manifest_hash=$2)",
        &[&database, &&old_manifest[..]],
    )?.get(0);
    let receipt_bindings: i64 = sql.query_one(
        "SELECT count(*) FROM atomic_generation_request_bases base JOIN atomic_heads head ON head.database_id=base.database_id AND head.log_generation=base.generation WHERE base.base_manifest_hash=$1",
        &[&&old_manifest[..]],
    )?.get(0);
    let blocked_prefixes = receipt_blocked_prefixes(&mut sql)?;
    println!(
        "gc_snapshot_safety=passed old_pinned_quiescent={pinned} old_released_quiescent={released} receipt_blocked_retirement_prefixes={blocked_prefixes} old_manifest_still_published={old_still_published} old_manifest_current_receipt_bindings={receipt_bindings} snapshot_release_reclamation_observed={} elapsed_ms={} memory={} current_load={:?} reopened_load={:?}",
        !old_still_published,
        began.elapsed().as_millis(),
        memory()?,
        current_peer.load_stats(),
        reopened.load_stats()
    );
    if !pinned || !released {
        return Err(
            "GC window reached its batch/wall cap; samples verified but collection is incomplete"
                .into(),
        );
    }
    if blocked_prefixes > 0 {
        return Err("GC is retention-blocked by active receipt bases; snapshot samples passed but reclamation is not established".into());
    }
    Ok(())
}

fn positive(name: &str, default: u64) -> Result<u64> {
    let value = std::env::var(name).map_or(Ok(default), |value| value.parse::<u64>())?;
    if value == 0 {
        return Err(format!("{name} must be positive").into());
    }
    Ok(value)
}

fn require_stopped(sql: &mut Client) -> Result<()> {
    let active: bool = sql.query_one(
        "SELECT EXISTS (SELECT 1 FROM atomic_transactor_leases WHERE expires_at>clock_timestamp())", &[],
    )?.get(0);
    if active {
        return Err(
            "global GC requires every writer in the selected catalog/schema to be stopped".into(),
        );
    }
    Ok(())
}

fn sample(value: &DatabaseValue, limit: usize) -> Result<Vec<Datom>> {
    let mut result = Vec::new();
    for datom in value
        .seek_cursor(&IndexBoundary::Aevt(IndexComponents::One(ATTRIBUTE)))?
        .take(limit)
    {
        let datom = datom?;
        if datom.attribute != ATTRIBUTE {
            break;
        }
        result.push(datom);
    }
    Ok(result)
}

fn assert_marker(value: &DatabaseValue, entity: u64, expected: Option<i64>) -> Result<()> {
    for view in [value.clone(), value.clone().history()] {
        let actual = view
            .seek_cursor(&IndexBoundary::Eavt(IndexComponents::Two(
                entity, ATTRIBUTE,
            )))?
            .next()
            .transpose()?;
        let actual = actual.filter(|datom| datom.entity == entity && datom.attribute == ATTRIBUTE);
        match expected {
            None => assert!(actual.is_none()),
            Some(expected) => {
                let datom = actual.ok_or("current/history marker disappeared")?;
                assert!(datom.added);
                assert_eq!(datom.value, Value::Long(expected));
            }
        }
    }
    Ok(())
}

fn pending_folds(sql: &mut Client) -> Result<(i64, i64)> {
    let row = sql.query_one(
        "SELECT count(*), coalesce(sum((SELECT count(*) FROM atomic_tree_delta_nodes n WHERE n.manifest_hash=h.manifest_hash)),0)::bigint FROM atomic_tree_delta_headers h JOIN atomic_tree_publications p ON p.manifest_hash=h.manifest_hash WHERE h.delta_state=2", &[],
    )?;
    Ok((row.get(0), row.get(1)))
}

fn counts(inventory: &GarbageInventory) -> BTreeMap<&'static str, u64> {
    BTreeMap::from([
        ("segments", inventory.segment_hashes.len() as u64),
        ("programs", inventory.program_hashes.len() as u64),
        (
            "publication_steps",
            inventory.tree_publications.len() as u64,
        ),
        ("intent_steps", inventory.tree_build_intents.len() as u64),
        (
            "receipt_conversion_steps",
            inventory.receipt_archive_conversions.len() as u64,
        ),
        (
            "receipt_conversion_node_reads",
            inventory
                .receipt_archive_conversions
                .iter()
                .map(|entry| entry.node_reads)
                .sum(),
        ),
        (
            "receipt_conversion_node_read_bytes",
            inventory
                .receipt_archive_conversions
                .iter()
                .map(|entry| entry.node_read_bytes)
                .sum(),
        ),
        (
            "receipt_conversion_hashes",
            inventory
                .receipt_archive_conversions
                .iter()
                .map(|entry| entry.hashes_processed)
                .sum(),
        ),
        (
            "receipt_conversion_retired_rows",
            inventory
                .receipt_archive_conversions
                .iter()
                .map(|entry| entry.retired_rows_drained)
                .sum(),
        ),
        ("manifests", inventory.tree_manifest_hashes.len() as u64),
        ("tree_nodes", inventory.tree_node_hashes.len() as u64),
        (
            "archive_steps",
            inventory.request_base_archives.len() as u64,
        ),
        (
            "archive_rows",
            inventory
                .request_base_archives
                .iter()
                .map(|entry| entry.rows_removed)
                .sum(),
        ),
        ("log_steps", inventory.log_generations.len() as u64),
        (
            "log_rows",
            inventory
                .log_generations
                .iter()
                .map(|entry| entry.rows_removed)
                .sum(),
        ),
        (
            "semantic_roots",
            inventory.semantic_commitment_roots.len() as u64,
        ),
        (
            "semantic_nodes",
            inventory.semantic_commitment_node_hashes.len() as u64,
        ),
    ])
}

fn storage_counts(sql: &mut Client) -> Result<BTreeMap<String, i64>> {
    // Observational SQL only; all writes/collection use public native APIs.
    // These whole-table counts are explicit broad operation measurements.
    let mut result = BTreeMap::new();
    for table in [
        "atomic_tree_publications",
        "atomic_tree_manifests",
        "atomic_tree_nodes",
        "atomic_tree_garbage_nodes",
        "atomic_tree_retirements",
        "atomic_tree_build_intents",
        "atomic_request_base_archives",
        "atomic_request_base_archive_nodes",
        "atomic_receipt_archive_conversions",
        "atomic_receipt_archive_frontier",
        "atomic_log_generations",
        "atomic_generation_transactions",
        "atomic_transaction_contents",
        "atomic_programs",
        "atomic_semantic_commitment_roots",
        "atomic_semantic_commitment_nodes",
    ] {
        result.insert(
            table.into(),
            sql.query_one(&format!("SELECT count(*) FROM {table}"), &[])?
                .get(0),
        );
    }
    for table in [
        "atomic_tree_nodes",
        "atomic_transaction_contents",
        "atomic_programs",
        "atomic_semantic_commitment_nodes",
    ] {
        result.insert(
            format!("{table}_payload_bytes"),
            sql.query_one(
                &format!("SELECT coalesce(sum(octet_length(payload)),0)::bigint FROM {table}"),
                &[],
            )?
            .get(0),
        );
    }
    result.insert(
        "current_generation_receipt_base_roots".into(),
        sql.query_one(
            "SELECT count(DISTINCT base.base_manifest_hash) FROM atomic_generation_request_bases base JOIN atomic_heads head ON head.database_id=base.database_id AND head.log_generation=base.generation", &[],
        )?.get(0),
    );
    result.insert(
        "receipt_blocked_oldest_publications".into(),
        receipt_blocked_prefixes(sql)?,
    );
    Ok(result)
}

fn receipt_blocked_prefixes(sql: &mut Client) -> Result<i64> {
    Ok(sql.query_one(
        "SELECT count(*) FROM atomic_tree_retirements r JOIN atomic_tree_publications p USING(database_id,publication_revision,manifest_hash) WHERE NOT EXISTS (SELECT 1 FROM atomic_tree_publications older WHERE older.database_id=p.database_id AND older.publication_revision<p.publication_revision) AND EXISTS (SELECT 1 FROM atomic_generation_request_bases base JOIN atomic_heads head ON head.database_id=p.database_id AND head.log_generation=p.log_generation WHERE base.base_manifest_hash=p.manifest_hash)", &[],
    )?.get(0))
}

fn window(
    operator: &mut PostgresOperator,
    sql: &mut Client,
    name: &str,
    max_batches: u64,
    wall: Duration,
) -> Result<bool> {
    require_stopped(sql)?;
    let started = Instant::now();
    println!(
        "gc_window={name} before_counts={:?} memory={}",
        storage_counts(sql)?,
        memory()?
    );
    let mut totals = BTreeMap::<&str, u64>::new();
    let mut batches = 0;
    let mut quiescent = false;
    let mut max_batch_ms = 0;
    while batches < max_batches && started.elapsed() < wall {
        require_stopped(sql)?;
        let preview = operator.garbage_inventory(Duration::ZERO)?;
        let fold_before = pending_folds(sql)?;
        if counts(&preview).values().all(|count| *count == 0) && fold_before.0 == 0 {
            quiescent = true;
            break;
        }
        let batch = Instant::now();
        let applied = operator.collect_garbage(Duration::ZERO)?;
        assert!(applied.applied);
        let elapsed_ms = batch.elapsed().as_millis();
        max_batch_ms = max_batch_ms.max(elapsed_ms);
        batches += 1;
        let changed = counts(&applied);
        for (kind, count) in &changed {
            *totals.entry(kind).or_default() += count;
        }
        println!(
            "gc_window={name} batch={batches} collect_ms={elapsed_ms} changed={changed:?} log_phases={:?} conversion_phases={:?} folds_before={fold_before:?} folds_after={:?} memory={}",
            applied.log_generations,
            applied.receipt_archive_conversions,
            pending_folds(sql)?,
            memory()?
        );
    }
    // A final bounded preview distinguishes a cap reached on the last useful
    // batch from incomplete work. Inventory omits publication folding, so its
    // SQL cursor must also be empty before saying eligible work is quiescent.
    require_stopped(sql)?;
    let remaining = counts(&operator.garbage_inventory(Duration::ZERO)?);
    let folds = pending_folds(sql)?;
    quiescent |= remaining.values().all(|count| *count == 0) && folds.0 == 0;
    println!(
        "gc_window={name} quiescent={quiescent} batches={batches} elapsed_ms={} max_batch_ms={max_batch_ms} totals={totals:?} remaining_preview={remaining:?} pending_folds={folds:?} after_counts={:?} memory={}",
        started.elapsed().as_millis(),
        storage_counts(sql)?,
        memory()?
    );
    Ok(quiescent)
}

fn memory() -> Result<String> {
    Ok(std::fs::read_to_string("/proc/self/status")?
        .lines()
        .filter(|line| line.starts_with("VmRSS:") || line.starts_with("VmHWM:"))
        .map(str::trim)
        .collect::<Vec<_>>()
        .join(","))
}
