use super::*;
use crate::{
    Attribute, Cardinality, EntityRef, Keyword, PostgresConnectionConfig, Schema,
    TransactionRequest, TxOp, ValueType,
};
use std::time::{Duration, Instant};
const TEXT: u32 = 1000;
const COMPONENT: u32 = 1001;
const LINK: u32 = 1002;
const WAIT: Duration = Duration::from_secs(60);

#[test]
fn completion_observation_does_not_hide_pending_requests() {
    let Some(f) = fixture() else { return };
    let mut writer =
        crate::BlockTransactor::claim(&f.config, f.database.clone(), Default::default()).unwrap();
    writer
        .transact(&TransactionRequest::new("sync-source", seed()))
        .unwrap();
    let reader = BlockReader::connect(&f.config, Default::default()).unwrap();
    let requested = writer
        .transact(&TransactionRequest::new("sync-request", request()))
        .unwrap();
    let snapshot = reader.capture(&f.database.reference_key()).unwrap();
    assert_eq!(snapshot.basis_t(), requested.basis_t);
    assert!(!sync_complete(&snapshot, requested.basis_t).unwrap());
    writer.release().unwrap();
    let mut operator = crate::PostgresOperator::connect_configured(&f.config).unwrap();
    let route = identity_string(f.database.route);
    operator.process_excision_requests(&route).unwrap();
    assert!(operator.sync_excise(&route, requested.basis_t).unwrap());
    assert_eq!(
        requested.db_before.values(eid(4000), TEXT).unwrap(),
        vec![Value::String("private needle root".into())]
    );
}

fn schema() -> Schema {
    let mut s = Schema::new();
    for a in [
        Attribute::new(
            TEXT,
            Keyword::new("private", "text"),
            ValueType::String,
            Cardinality::One,
        )
        .fulltext(),
        Attribute::new(
            COMPONENT,
            Keyword::new("private", "child"),
            ValueType::Ref,
            Cardinality::One,
        )
        .component(),
        Attribute::new(
            LINK,
            Keyword::new("private", "link"),
            ValueType::Ref,
            Cardinality::Many,
        ),
    ] {
        s.install(a).unwrap();
    }
    s
}
fn eid(n: u64) -> u64 {
    let position = [4000, 4001, 4002, 4100, 4101, 4102, 4500]
        .iter()
        .position(|label| *label == n)
        .expect("fixture entity label");
    crate::make_eid(crate::USER_PARTITION, 1003 + position as u64).unwrap()
}
fn allocate_entities() -> Vec<TxOp> {
    [4000, 4001, 4002, 4100, 4101, 4102, 4500]
        .into_iter()
        .map(|label| TxOp::RetractEntity(EntityRef::Temp(format!("slot-{label}"))))
        .collect()
}
fn check_allocations(tempids: &BTreeMap<String, u64>) {
    for label in [4000, 4001, 4002, 4100, 4101, 4102, 4500] {
        assert_eq!(tempids[&format!("slot-{label}")], eid(label));
    }
}
fn add(e: u64, a: u32, v: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(e),
        attribute: a,
        value: v.into(),
    }
}
fn seed() -> Vec<TxOp> {
    vec![
        add(eid(4000), TEXT, Value::String("private needle root".into())),
        add(
            eid(4001),
            TEXT,
            Value::String("private needle child".into()),
        ),
        add(eid(4000), COMPONENT, Value::Ref(eid(4001))),
        add(eid(4002), LINK, Value::Ref(eid(4000))),
    ]
}
fn request() -> Vec<TxOp> {
    vec![add(
        eid(4100),
        crate::DB_EXCISE as u32,
        Value::Ref(eid(4000)),
    )]
}

#[test]
fn cursor_predicate_matches_pure_component_cutoff_and_keeper_oracle() {
    let allocated = Database::new(schema())
        .unwrap()
        .with(&allocate_entities(), 1)
        .unwrap();
    check_allocations(&allocated.tempids);
    let first = allocated.db_after.with(&seed(), 10).unwrap();
    let second = first
        .db_after
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(eid(4000)),
                attribute: COMPONENT,
                value: Some(Value::Ref(eid(4001)).into()),
            }],
            20,
        )
        .unwrap();
    let mut requests = request();
    requests.extend([
        add(
            eid(4101),
            crate::DB_EXCISE as u32,
            Value::Ref(u64::from(TEXT)),
        ),
        add(
            eid(4101),
            crate::DB_EXCISE_BEFORE as u32,
            Value::Instant(11),
        ),
        add(
            eid(4102),
            crate::DB_EXCISE as u32,
            Value::Ref(crate::DB_DOC),
        ),
    ]);
    let source = second.db_after.with(&requests, 30).unwrap().db_after;
    let old =
        ExcisionPlan::from_requests(&source, source.pending_excision_requests_after(0).unwrap())
            .unwrap();
    let new = plan_value(
        &source.database_value(),
        &BTreeSet::new(),
        64 * 1024 * 1024,
        &mut || Ok(()),
    )
    .unwrap();
    assert_eq!(old.frozen_predicates(), new.frozen_predicates());
    for datom in source.datoms(View::History, IndexOrder::Eavt) {
        assert_eq!(old.removes(&datom), new.removes(&datom));
    }
    assert!(
        new.frozen_predicates()[0].extent.contains(&eid(4001)),
        "retracted component edge remains historical ownership"
    );
    assert_eq!(
        plan_value(
            &source.database_value(),
            &BTreeSet::new(),
            1,
            &mut || Ok(())
        )
        .unwrap_err()
        .category,
        ErrorCategory::Busy
    );
    assert_eq!(
        plan_value(
            &source.database_value(),
            &BTreeSet::new(),
            64 * 1024 * 1024,
            &mut || Err(SemanticError::new(
                ErrorCategory::Interrupted,
                "test/cancel",
                "cancelled"
            ))
        )
        .unwrap_err()
        .category,
        ErrorCategory::Interrupted
    );
}

#[test]
fn checkpoint_codec_preserves_only_explicit_links_and_rejects_trailing_data() {
    let state = Checkpoint {
        identity: [2; 16],
        generation: 1,
        phase: 2,
        source: [3; 32],
        plan: [4; 32],
        candidate: [5; 32],
        initial: [6; 32],
        authorization: [7; 32],
        receipts: Some([8; 32]),
        after: Some([9; 32]),
        progress: ExcisionProgress::default(),
    };
    let bytes = state.encode().unwrap();
    let block = Block::decode(&sha256(&bytes), &bytes).unwrap();
    assert_eq!(
        block.links,
        vec![[3; 32], [4; 32], [5; 32], [6; 32], [7; 32], [8; 32]]
    );
    let mut input = Input::new(&[0, 0]);
    assert!(input.u64().is_err());
    let mut bytes = vec![];
    u64_into(&mut bytes, 2);
    u64_into(&mut bytes, 9);
    u64_into(&mut bytes, 8);
    assert!(Input::new(&bytes).set().is_err());
    let tomb = Block {
        kind: TOMBSTONE_KIND,
        links: vec![],
        payload: [&[2u8; 16][..], &1u64.to_be_bytes(), &3u64.to_be_bytes()].concat(),
    }
    .encode()
    .unwrap();
    assert_eq!(
        reject_tombstone_bytes(sha256(&tomb), &tomb, &[2; 16])
            .unwrap_err()
            .code,
        "postgres/idempotency-predates-excision"
    );
    assert_eq!(
        reject_tombstone_bytes(sha256(&tomb), &tomb, &[3; 16])
            .unwrap_err()
            .code,
        "excision/tombstone"
    );
}

struct Fixture {
    admin: postgres::Client,
    schema: String,
    config: PostgresConnectionConfig,
    url: String,
    database: BlockDatabase,
}
impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}
fn fixture() -> Option<Fixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block excision: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!("block_excision_{:032x}", crate::uuid_v7().unwrap());
    let mut admin = postgres::Client::connect(&url, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let url = if url.starts_with("postgres://") || url.starts_with("postgresql://") {
        format!(
            "{url}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
            if url.contains('?') { '&' } else { '?' }
        )
    } else {
        format!("{url} options='-csearch_path={schema},pg_catalog'")
    };
    let config = PostgresConnectionConfig::plaintext(&url);
    PgBlockStore::install(&config).unwrap();
    let database = BlockDatabase::create(&config, "private", self::schema()).unwrap();
    // Issue real IDs through normal tempid allocation; labels are only fixture
    // shorthand. Explicit unissued numeric IDs are intentionally not accepted.
    let mut writer =
        crate::BlockTransactor::claim(&config, database.clone(), Default::default()).unwrap();
    let allocated = writer
        .transact(&TransactionRequest::new(
            "allocate-fixture",
            allocate_entities(),
        ))
        .unwrap();
    check_allocations(&allocated.tempids);
    drop(allocated);
    writer.release().unwrap();
    Some(Fixture {
        admin,
        schema,
        config,
        url,
        database,
    })
}
fn service_config(f: &Fixture) -> crate::TransactionServiceConfig {
    crate::TransactionServiceConfig {
        connection: f.config.clone(),
        database_id: "private".into(),
        holder_id: "excision-test".into(),
        lease_duration: Duration::from_secs(20),
        renew_interval: Duration::from_millis(50),
        queue_capacity: 8,
        capacity_limits: Default::default(),
    }
}
fn options() -> crate::ServiceOptions {
    crate::ServiceOptions {
        excision: ExcisionConfig {
            log_batch_transactions: 2,
            ..Default::default()
        },
        indexing: crate::BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
        ..Default::default()
    }
}
fn assert_excised(value: &DatabaseValue) {
    for history in [false, true] {
        let db = if history {
            value.clone().history()
        } else {
            value.clone()
        };
        for d in db.datoms(IndexOrder::Eavt).unwrap() {
            assert!(![eid(4000), eid(4001)].contains(&d.entity));
            assert!(
                !matches!(d.value,Value::Ref(e) if [eid(4000),eid(4001)].contains(&e))
                    || d.attribute == crate::DB_EXCISE as u32
            );
        }
    }
    assert!(
        value
            .fulltext(TEXT, "needle", &crate::FulltextOptions::default())
            .unwrap()
            .hits
            .is_empty()
    );
}

#[test]
fn automatic_excision_sanitizes_history_search_retries_and_reopen_but_not_held_values() {
    let Some(f) = fixture() else { return };
    let service =
        crate::TransactionService::start_with_options(service_config(&f), options()).unwrap();
    let client = service.client();
    let original = TransactionRequest::new("private-key", seed());
    let first = client.transact(original.clone(), WAIT).unwrap();
    let held = first.db_after.clone();
    let reference = held.snapshot_reference().unwrap();
    let peer = crate::Peer::connect(&f.url, "private", 64).unwrap();
    let started = Instant::now();
    let requested = client
        .transact(TransactionRequest::new("erase", request()), WAIT)
        .unwrap();
    let current = peer.sync_excise(requested.basis_t, WAIT).unwrap();
    assert_excised(&current);
    assert_eq!(
        held.values(eid(4000), TEXT).unwrap(),
        vec![Value::String("private needle root".into())]
    );
    assert!(reference.open(&f.config, 64, 1024 * 1024).is_err());
    assert_eq!(
        client.transact(original.clone(), WAIT).unwrap_err().code,
        "postgres/idempotency-predates-excision"
    );
    let stats = client.background_indexing_stats();
    assert!(stats.excision.complete);
    assert!(stats.excision.fresh_write_pause_nanos > 0);
    assert!(stats.excision.rewritten_transactions >= requested.basis_t);
    assert!(stats.excision_failure.is_none());
    let log = current.block_snapshot().unwrap().log();
    for transaction in log.tx_range(None, None).unwrap() {
        for d in transaction.unwrap().data {
            assert!(![eid(4000), eid(4001)].contains(&d.entity));
        }
    }
    eprintln!(
        "BLOCK_EXCISION complete_ms={} fresh_pause_ns={} rewritten={} source_bytes={} peak_accounted={}",
        started.elapsed().as_millis(),
        stats.excision.fresh_write_pause_nanos,
        stats.excision.rewritten_transactions,
        stats.excision.source_payload_bytes,
        stats.excision.peak_admitted_bytes
    );
    service.shutdown();
    let restarted =
        crate::TransactionService::start_with_options(service_config(&f), options()).unwrap();
    assert_eq!(
        restarted
            .client()
            .transact(original, WAIT)
            .unwrap_err()
            .code,
        "postgres/idempotency-predates-excision"
    );
    let fresh = TransactionRequest::new(
        "after",
        vec![add(
            eid(4000),
            TEXT,
            Value::String("new public value".into()),
        )],
    );
    let after = restarted.client().transact(fresh.clone(), WAIT).unwrap();
    assert!(restarted.client().transact(fresh, WAIT).unwrap().replayed);
    assert_eq!(
        after.db_after.values(eid(4000), TEXT).unwrap(),
        vec![Value::String("new public value".into())]
    );
    restarted.shutdown();
}

#[test]
fn rewrite_checkpoint_uses_forward_source_reads_and_retries_admission_without_progress() {
    let Some(f) = fixture() else { return };
    let mut writer =
        crate::BlockTransactor::claim(&f.config, f.database.clone(), Default::default()).unwrap();
    writer
        .transact(&TransactionRequest::new("source", seed()))
        .unwrap();
    for n in 0..126 {
        writer
            .transact(&TransactionRequest::new(
                format!("empty-{n}"),
                Vec::<TxOp>::new(),
            ))
            .unwrap();
    }
    let requested = writer
        .transact(&TransactionRequest::new("erase", request()))
        .unwrap();
    assert_eq!(requested.basis_t, 130);
    let reader = BlockReader::connect(&f.config, Default::default()).unwrap();
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let mut job = ExcisionJob::open(
        reader.clone(),
        &mut store,
        f.database.clone(),
        ExcisionConfig {
            log_batch_transactions: 130,
            ..Default::default()
        },
        &mut || Ok(()),
    )
    .unwrap()
    .unwrap();
    let checkpoint = store.read_ref(&work_key(&f.database.route)).unwrap();
    let candidate = job.state.candidate;
    let normal_limit = job.config.max_admitted_bytes;
    job.config.max_admitted_bytes = 1024 * 1024;
    assert_eq!(
        job.step(&mut store, &mut || Ok(())).unwrap_err().code,
        "storage/log-read-limit"
    );
    assert_eq!(
        store.read_ref(&work_key(&f.database.route)).unwrap(),
        checkpoint
    );
    assert_eq!(job.state.candidate, candidate);
    assert_eq!(job.progress().rewritten_transactions, 0);
    job.config.max_admitted_bytes = normal_limit;
    let reads_before = reader.read_stats();
    let operation = crate::OperationContext::new(crate::OperationKind::Administration);
    {
        let _scope = operation.enter();
        assert!(!job.step(&mut store, &mut || Ok(())).unwrap());
    }
    let reads = reader.read_stats().object_reads - reads_before.object_reads;
    assert_eq!(
        reads, 132,
        "130 entries plus two sealed source pages; tail already captured"
    );
    assert_eq!(job.progress().rewritten_transactions, 130);
    assert_eq!(
        job.state.phase, 1,
        "rewrite checkpoint still precedes index preparation"
    );
    assert!(job.progress().removed_datoms > 0);
    let current = reader
        .capture(&f.database.reference_key())
        .unwrap()
        .database_value();
    assert_eq!(
        current.values(eid(4000), TEXT).unwrap(),
        [Value::String("private needle root".into())]
    );
    let candidate = value(&mut store, job.state.candidate).unwrap();
    let candidate_log = LogRoot::open(&mut store, candidate.log.unwrap()).unwrap();
    let mut range = candidate_log.range(&mut store, 1, 131).unwrap();
    let mut bases = Vec::new();
    while let Some(record) = range.next_record() {
        let record = record.unwrap();
        bases.push(record.entry.basis_t);
        assert!(
            !record
                .entry
                .tx_data
                .iter()
                .any(|d| d.entity == eid(4000) && d.attribute == TEXT)
        );
    }
    assert_eq!(bases, (1..=130).collect::<Vec<_>>());
    eprintln!(
        "EXCISION_FORWARD basis=130 source_object_reads={reads} sql_calls={}",
        operation.snapshot().sql_calls
    );
    writer.release().unwrap();
}

#[test]
fn cancelled_checkpoint_resumes_after_writer_restart_and_retains_source_prefix_progress() {
    let Some(f) = fixture() else { return };
    let mut writer =
        crate::BlockTransactor::claim(&f.config, f.database.clone(), Default::default()).unwrap();
    let original = TransactionRequest::new("source", seed());
    let first = writer.transact(&original).unwrap();
    writer
        .transact(&TransactionRequest::new("erase", request()))
        .unwrap();
    let reader = BlockReader::connect(&f.config, Default::default()).unwrap();
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let cfg = ExcisionConfig {
        log_batch_transactions: 1,
        ..Default::default()
    };
    let mut job = ExcisionJob::open(
        reader.clone(),
        &mut store,
        f.database.clone(),
        cfg,
        &mut || Ok(()),
    )
    .unwrap()
    .unwrap();
    assert!(!job.step(&mut store, &mut || Ok(())).unwrap());
    assert_eq!(job.progress().rewritten_transactions, 1);
    let before = store.read_ref(&work_key(&f.database.identity)).unwrap();
    assert_eq!(
        job.step(&mut store, &mut || Err(SemanticError::new(
            ErrorCategory::Interrupted,
            "test/cancel",
            "cancelled"
        )))
        .unwrap_err()
        .category,
        ErrorCategory::Interrupted
    );
    assert_eq!(
        store.read_ref(&work_key(&f.database.identity)).unwrap(),
        before
    );
    drop(job);
    writer.release().unwrap();
    let mut writer =
        crate::BlockTransactor::claim(&f.config, f.database.clone(), Default::default()).unwrap();
    // A suffix committed while the admitted job was stopped must be appended,
    // not cause the completed prefix to be replayed or a fresh request lost.
    writer
        .transact(&TransactionRequest::new(
            "suffix",
            vec![add(eid(4500), TEXT, Value::String("unrelated".into()))],
        ))
        .unwrap();
    let mut job = ExcisionJob::open(
        reader.clone(),
        &mut store,
        f.database.clone(),
        cfg,
        &mut || Ok(()),
    )
    .unwrap()
    .unwrap();
    assert_eq!(job.progress().rewritten_transactions, 1);
    for _ in 0..100 {
        if job.step(&mut store, &mut || Ok(())).unwrap() {
            break;
        }
    }
    let prepared = job.prepared(&mut store).unwrap();
    writer.adopt_excision(prepared).unwrap();
    let current = reader
        .capture(&f.database.reference_key())
        .unwrap()
        .database_value();
    assert_excised(&current);
    assert_eq!(
        current.values(eid(4500), TEXT).unwrap(),
        vec![Value::String("unrelated".into())]
    );
    assert_eq!(
        writer.transact(&original).unwrap_err().code,
        "postgres/idempotency-predates-excision"
    );
    assert_eq!(first.db_after.values(eid(4000), TEXT).unwrap().len(), 1);
    assert!(
        store
            .read_ref(&work_key(&f.database.identity))
            .unwrap()
            .unwrap()
            .value
            .is_none()
    );
    writer.release().unwrap();
}

#[test]
fn failed_automatic_admission_leaves_ordinary_writes_available_and_request_pending() {
    let Some(f) = fixture() else { return };
    let mut config = options();
    config.excision.max_admitted_bytes = 1;
    let service =
        crate::TransactionService::start_with_options(service_config(&f), config).unwrap();
    let client = service.client();
    client
        .transact(TransactionRequest::new("source", seed()), WAIT)
        .unwrap();
    let requested = client
        .transact(TransactionRequest::new("erase", request()), WAIT)
        .unwrap();
    let deadline = Instant::now() + WAIT;
    loop {
        let stats = client.background_indexing_stats();
        if stats.excision_failure.is_some() {
            assert!(!stats.excision.complete);
            break;
        }
        assert!(Instant::now() < deadline);
        std::thread::sleep(Duration::from_millis(5));
    }
    assert!(client.is_available());
    client
        .transact(
            TransactionRequest::new(
                "still-writing",
                vec![add(eid(4500), TEXT, Value::String("available".into()))],
            ),
            WAIT,
        )
        .unwrap();
    assert!(
        crate::Peer::connect(&f.url, "private", 64)
            .unwrap()
            .sync_excise(requested.basis_t, Duration::ZERO)
            .is_err()
    );
    service.shutdown();
    let resumed =
        crate::TransactionService::start_with_options(service_config(&f), options()).unwrap();
    let peer = crate::Peer::connect(&f.url, "private", 64).unwrap();
    assert_excised(&peer.sync_excise(requested.basis_t, WAIT).unwrap());
    resumed.shutdown();
}

#[test]
fn public_operator_uses_same_checkpoint_resume_and_atomic_completion() {
    let Some(f) = fixture() else { return };
    let mut writer =
        crate::BlockTransactor::claim(&f.config, f.database.clone(), Default::default()).unwrap();
    writer
        .transact(&TransactionRequest::new("source", seed()))
        .unwrap();
    let receipt = writer
        .transact(&TransactionRequest::new("erase", request()))
        .unwrap();
    let target = receipt.db_after.basis_t();
    let mut operator = crate::PostgresOperator::connect_configured(&f.config).unwrap();
    assert_eq!(
        operator
            .process_excision_requests(&identity_string(f.database.identity))
            .unwrap_err()
            .code,
        "storage/writer-active"
    );
    assert!(
        !operator
            .sync_excise(&identity_string(f.database.identity), target)
            .unwrap()
    );
    writer.release().unwrap();
    for point in [
        crate::ExcisionFault::AfterCapture,
        crate::ExcisionFault::AfterCandidateStaged,
    ] {
        assert_eq!(
            operator
                .process_excision_requests_with_fault(&identity_string(f.database.identity), point)
                .unwrap_err()
                .code,
            "excision/injected-fault"
        );
        assert!(
            !operator
                .sync_excise(&identity_string(f.database.identity), target)
                .unwrap()
        );
    }
    assert_eq!(
        operator
            .process_excision_requests_with_fault(
                &identity_string(f.database.identity),
                crate::ExcisionFault::AfterActivation
            )
            .unwrap_err()
            .code,
        "excision/injected-fault"
    );
    assert!(
        operator
            .sync_excise(&identity_string(f.database.identity), target)
            .unwrap()
    );
    let noop = operator
        .process_excision_requests(&identity_string(f.database.identity))
        .unwrap();
    assert!(noop.resumed);
    assert_eq!(noop.generation, 1);
    assert_eq!(noop.request_count, 0);
    assert_eq!(noop.old_head_hash, noop.new_head_hash);
    let reader = BlockReader::connect(&f.config, Default::default()).unwrap();
    assert_excised(
        &reader
            .capture(&f.database.reference_key())
            .unwrap()
            .database_value(),
    );
    let mut writer =
        crate::BlockTransactor::claim(&f.config, f.database.clone(), Default::default()).unwrap();
    assert_eq!(
        writer
            .transact(&TransactionRequest::new("source", seed()))
            .unwrap_err()
            .code,
        "postgres/idempotency-predates-excision"
    );
    writer.release().unwrap();
}

#[test]
fn programs_and_search_survive_grace_then_excision_releases_expired_plaintext() {
    use crate::storage::ownership::{BlockCollector, CollectionPhase};
    use crate::{Instruction, Program, ProgramKind, encode_program};
    let Some(f) = fixture() else { return };
    let begin = Instant::now();
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let code = |value: Value| Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(value),
            Instruction::Pop,
            Instruction::Return,
        ],
    };
    let secret = store
        .put(&encode_program(&code(Value::String("private-program-plaintext".into()))).unwrap())
        .unwrap();
    let parent = store
        .put(&encode_program(&code(Value::Function(secret))).unwrap())
        .unwrap();
    let safe = store
        .put(&encode_program(&code(Value::Long(77))).unwrap())
        .unwrap();
    let orphan = store
        .put(&encode_program(&code(Value::Long(88))).unwrap())
        .unwrap();
    let mut writer =
        crate::BlockTransactor::claim(&f.config, f.database.clone(), Default::default()).unwrap();
    let mut attr = Attribute::new(
        1003,
        Keyword::new("private", "code"),
        ValueType::Function,
        Cardinality::One,
    );
    attr.no_history = true;
    writer
        .transact(&TransactionRequest::new(
            "code-schema",
            vec![TxOp::InstallAttribute(attr)],
        ))
        .unwrap();
    let mut facts = seed();
    facts.push(add(eid(4000), 1003, Value::Function(parent)));
    // Keep a genuinely live program after the selected entity is excised.
    facts.push(add(eid(4500), 1003, Value::Function(safe)));
    writer
        .transact(&TransactionRequest::new("program-source", facts))
        .unwrap();
    let index = writer
        .index_input()
        .unwrap()
        .prepare(&mut store, &TreeConfig::default())
        .unwrap();
    writer.adopt_index(index).unwrap();
    let reader = BlockReader::connect(&f.config, Default::default()).unwrap();
    let tree_held = reader.capture(&f.database.reference_key()).unwrap();
    let attachment = tree_held.index_descriptor().fulltext.unwrap();
    let mut collector = BlockCollector::connect(&f.config).unwrap();
    let retired_handoffs = std::cell::Cell::new(0);
    let mut collect = |age| {
        let mut removed = 0;
        for _ in 0..128 {
            let progress = collector.advance(age, 4096).unwrap();
            removed += progress.stats.objects_removed;
            if progress.phase == CollectionPhase::Complete {
                // Raw collection follows published roots. Like the operator's
                // Complete hook, retire unused report roots explicitly; their
                // release events are folded by the next collection cycle.
                let handoffs = collector.prune_report_handoffs(age, 32).unwrap();
                retired_handoffs.set(retired_handoffs.get() + handoffs.removed);
                return removed;
            }
        }
        panic!("small excision retention fixture exceeded collection step bound");
    };
    collect(Duration::from_secs(3600));
    assert!(
        store.get(orphan).unwrap().is_none(),
        "never-published fenced upload has no retirement-age guarantee"
    );
    collect(Duration::ZERO);
    assert!(store.get(orphan).unwrap().is_none());
    for id in [parent, secret, safe, attachment] {
        assert!(
            store.get(id).unwrap().is_some(),
            "live tree/log/dependency/search owner was lost"
        );
    }
    // noHistory removes the old function value from both physical fact trees,
    // but the canonical log must retain its program and transitive dependency.
    writer
        .transact(&TransactionRequest::new(
            "replace-code",
            vec![add(eid(4000), 1003, Value::Function(safe))],
        ))
        .unwrap();
    let index = writer
        .index_input()
        .unwrap()
        .prepare(&mut store, &TreeConfig::default())
        .unwrap();
    writer.adopt_index(index).unwrap();
    drop(tree_held);
    let held = reader.capture(&f.database.reference_key()).unwrap();
    assert!(
        held.database_value()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap()
            .iter()
            .all(|d| d.value != Value::Function(parent))
    );
    collect(Duration::ZERO);
    assert!(
        store.get(parent).unwrap().is_some(),
        "noHistory is not canonical log excision"
    );
    assert!(
        store.get(secret).unwrap().is_some(),
        "transitive dependency survives through log"
    );
    let reference = held.database_value().snapshot_reference().unwrap();
    writer
        .transact(&TransactionRequest::new("erase-programs", request()))
        .unwrap();
    writer.release().unwrap();
    let mut operator = crate::PostgresOperator::connect_configured(&f.config).unwrap();
    let erased = operator
        .process_excision_requests(&identity_string(f.database.identity))
        .unwrap();
    assert!(erased.removed_datoms > 0);
    assert_eq!(erased.request_count, 1);
    collect(Duration::from_secs(3600));
    assert!(
        store.get(parent).unwrap().is_some(),
        "the grace period retains the old generation"
    );
    assert!(store.get(secret).unwrap().is_some());
    assert!(
        !held
            .database_value()
            .fulltext(TEXT, "needle", &Default::default())
            .unwrap()
            .hits
            .is_empty()
    );
    assert!(reference.open(&f.config, 64, 1024 * 1024).is_err());
    let current = reader.capture(&f.database.reference_key()).unwrap();
    assert_excised(&current.database_value());
    assert!(
        current
            .database_value()
            .fulltext(TEXT, "needle", &Default::default())
            .unwrap()
            .hits
            .is_empty()
    );
    assert!(store.get(safe).unwrap().is_some());
    drop(held);
    collect(Duration::from_secs(3600));
    for id in [parent, secret, attachment] {
        assert!(
            store.get(id).unwrap().is_some(),
            "recently retired published object respects its age horizon"
        );
    }
    // Explicitly expire the grace period and finish incremental collection.
    let deadline = Instant::now() + Duration::from_secs(10);
    let mut removed = 0;
    loop {
        removed += collect(Duration::ZERO);
        if store.get(parent).unwrap().is_none()
            && store.get(secret).unwrap().is_none()
            && store.get(attachment).unwrap().is_none()
        {
            break;
        }
        assert!(
            Instant::now() < deadline,
            "expired excised program/search plaintext remains owned"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
    assert_eq!(
        retired_handoffs.get(),
        1,
        "the unobserved pre-excision report publication was retired"
    );
    assert!(
        store.get(safe).unwrap().is_some(),
        "live successor code must not be reclaimed"
    );
    assert!(
        current
            .database_value()
            .fulltext(TEXT, "needle", &Default::default())
            .unwrap()
            .hits
            .is_empty()
    );
    eprintln!(
        "BLOCK_EXCISION_PROGRAM_GC complete_ms={} removed_after_release={removed} removed_datoms={}",
        begin.elapsed().as_millis(),
        erased.removed_datoms
    );
}
