//! Reader liveness and coalesced final-owner cleanup. No timer revokes a live
//! reader: revocation requires exclusive acquisition of its session lock.
use super::*;
use crate::storage::Reference;
use std::ops::Bound::{Excluded, Unbounded};
use std::sync::Weak;

const SESSION_MAGIC: &[u8; 5] = b"ATRS\x01";
const CLEANUP_BATCH: usize = 32;

pub(super) struct ReaderSession {
    pub(super) key: String,
    revision: u64,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
struct SessionRecord {
    created_ms: u64,
    revoked_ms: Option<u64>,
}
impl SessionRecord {
    fn encode(self) -> Vec<u8> {
        let mut bytes = Vec::with_capacity(22);
        bytes.extend_from_slice(SESSION_MAGIC);
        bytes.push(u8::from(self.revoked_ms.is_some()));
        bytes.extend_from_slice(&self.created_ms.to_be_bytes());
        bytes.extend_from_slice(&self.revoked_ms.unwrap_or(0).to_be_bytes());
        bytes
    }
    fn decode(bytes: &[u8]) -> Result<Self, SemanticError> {
        if bytes.len() != 22 || &bytes[..5] != SESSION_MAGIC || bytes[5] > 1 {
            return Err(fault(
                "storage/read-session-format",
                "Invalid reader-session descriptor",
            ));
        }
        let created_ms = u64::from_be_bytes(bytes[6..14].try_into().unwrap());
        let revoked = u64::from_be_bytes(bytes[14..22].try_into().unwrap());
        if (bytes[5] == 0 && revoked != 0) || (bytes[5] == 1 && revoked < created_ms) {
            return Err(fault(
                "storage/read-session-format",
                "Invalid reader-session times",
            ));
        }
        Ok(Self {
            created_ms,
            revoked_ms: (bytes[5] == 1).then_some(revoked),
        })
    }
}

impl ReaderSession {
    pub(super) fn start(store: &mut PgBlockStore) -> Result<Arc<Self>, SemanticError> {
        let key = format!("sessions/read/{:032x}", crate::uuid_v7()?);
        store.lock_session_shared(&key)?;
        let now = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map_err(|_| fault("storage/read-session-time", "Clock precedes Unix epoch"))?
            .as_millis();
        let created_ms = u64::try_from(now).map_err(|_| {
            fault(
                "storage/read-session-time",
                "Clock exceeds reader-session range",
            )
        })?;
        let result = super::super::ownership::publish_refs(
            store,
            &[RefCondition {
                key: key.clone(),
                expected: None,
            }],
            &[RefChange {
                key: key.clone(),
                value: Some(
                    SessionRecord {
                        created_ms,
                        revoked_ms: None,
                    }
                    .encode(),
                ),
            }],
        );
        match result {
            Ok(BatchOutcome::Applied(refs)) => {
                let revision = refs
                    .into_iter()
                    .find(|(name, _)| name == &key)
                    .ok_or_else(|| {
                        fault(
                            "storage/read-session-result",
                            "Session publication omitted its reference",
                        )
                    })?
                    .1
                    .revision;
                Ok(Arc::new(Self { key, revision }))
            }
            result => {
                let _ = store.unlock_session_shared(&key);
                match result {
                    Err(error) => Err(error),
                    _ => Err(SemanticError::conflict(
                        "storage/read-session-conflict",
                        "Reader-session identity already exists",
                    )),
                }
            }
        }
    }
    pub(super) fn condition(&self) -> RefCondition {
        RefCondition {
            key: self.key.clone(),
            expected: Some(self.revision),
        }
    }
    pub(super) fn pin_prefix(&self) -> String {
        format!(
            "pins/read/{}/",
            self.key
                .strip_prefix("sessions/read/")
                .expect("issued session key")
        )
    }
    /// Called only while the shared session lock is held. A new transport may
    /// recover an unrevoked session, but never resurrect a revoked one.
    pub(super) fn validate(&self, store: &mut PgBlockStore) -> Result<(), SemanticError> {
        let reference = store.read_ref(&self.key)?.ok_or_else(revoked)?;
        let bytes = reference.value.as_deref().ok_or_else(revoked)?;
        if reference.revision != self.revision || SessionRecord::decode(bytes)?.revoked_ms.is_some()
        {
            return Err(revoked());
        }
        Ok(())
    }
}

struct PinRecord {
    revision: u64,
    value: Weak<RootPin>,
}
#[derive(Default)]
pub(super) struct PinCleanup {
    records: BTreeMap<String, PinRecord>,
    cursor: Option<String>,
    scheduled: bool,
    dirty: bool,
    retry_delay_ms: u64,
    #[cfg(test)]
    before_release: Option<Box<dyn FnOnce() -> Result<(), SemanticError> + Send>>,
}
impl BlockStoreResource {
    pub(super) fn register_pin(&self, pin: &Arc<RootPin>) {
        self.cleanup
            .lock()
            .unwrap_or_else(|e| e.into_inner())
            .records
            .insert(
                pin.key.clone(),
                PinRecord {
                    revision: pin.revision,
                    value: Arc::downgrade(pin),
                },
            );
    }
    pub(super) fn forget_pin(&self, key: &str) {
        self.cleanup
            .lock()
            .unwrap_or_else(|e| e.into_inner())
            .records
            .remove(key);
    }
    /// Each reader has at most one queued cleanup job, backed by its existing
    /// retained executor resource. Registry entries are allocated at capture,
    /// not Drop, and clones add no entries. No unbounded queue of Drop jobs.
    pub(super) fn schedule_pin_cleanup(self: &Arc<Self>) {
        let mut state = self.cleanup.lock().unwrap_or_else(|e| e.into_inner());
        state.dirty = true;
        if state.scheduled {
            return;
        }
        state.scheduled = true;
        state.cursor = None;
        state.dirty = false;
        drop(state);
        self.enqueue_cleanup();
    }
    fn enqueue_cleanup(self: &Arc<Self>) {
        let resource = Arc::clone(self);
        self.executor.cleanup(move || {
            let result = resource.cleanup_batch();
            let mut state = resource.cleanup.lock().unwrap_or_else(|e| e.into_inner());
            let mut retry_delay_ms = 0;
            let again = match result {
                Ok(true) => {
                    state.retry_delay_ms = 0;
                    true
                }
                Ok(false) if state.dirty => {
                    state.cursor = None;
                    state.dirty = false;
                    state.retry_delay_ms = 0;
                    true
                }
                Err(error) if error.code == "storage/read-pin-contention" => {
                    // The failed page is still pending. Nothing may drop or
                    // capture again on this idle reader, so retain its one
                    // reserved cleanup job instead of losing the release.
                    state.cursor = None;
                    state.dirty = false;
                    state.retry_delay_ms = state.retry_delay_ms.saturating_mul(2).clamp(1, 50);
                    retry_delay_ms = state.retry_delay_ms;
                    true
                }
                _ => {
                    state.scheduled = false;
                    state.cursor = None;
                    state.retry_delay_ms = 0;
                    false
                }
            };
            drop(state);
            if again {
                if retry_delay_ms != 0 {
                    // Only the blocking cleanup worker waits. Capped backoff
                    // avoids spinning during sustained GC contention; Drop
                    // still does no I/O, waiting, or thread creation.
                    std::thread::sleep(std::time::Duration::from_millis(retry_delay_ms));
                }
                resource.enqueue_cleanup();
            }
        });
    }
    pub(super) fn flush_released(self: &Arc<Self>) -> Result<(), SemanticError> {
        // Admission itself drains one bounded page. A blocked/failing provider
        // cannot be accompanied by unlimited new captures and abandoned pins.
        match self.cleanup_batch() {
            Ok(true) => self.schedule_pin_cleanup(),
            Ok(false) => {}
            Err(error) if error.code == "storage/read-pin-contention" => {
                self.schedule_pin_cleanup();
                return Err(SemanticError::conflict(
                    "storage/capture-gc-conflict",
                    "GC changed during pending read-pin cleanup",
                ));
            }
            Err(error) => return Err(error),
        }
        Ok(())
    }
    fn cleanup_batch(&self) -> Result<bool, SemanticError> {
        let (dead, more) = {
            let mut state = self.cleanup.lock().unwrap_or_else(|e| e.into_inner());
            let lower = state
                .cursor
                .as_ref()
                .map_or(Unbounded, |key| Excluded(key.clone()));
            let page = state
                .records
                .range((lower, Unbounded))
                .take(CLEANUP_BATCH)
                .map(|(key, record)| {
                    (
                        key.clone(),
                        record.revision,
                        record.value.strong_count() == 0,
                    )
                })
                .collect::<Vec<_>>();
            let more = page.len() == CLEANUP_BATCH;
            state.cursor = page.last().map(|(key, _, _)| key.clone());
            if !more {
                state.cursor = None;
            }
            (
                page.into_iter()
                    .filter(|(_, _, dead)| *dead)
                    .map(|(key, revision, _)| (key, revision))
                    .collect::<Vec<_>>(),
                more,
            )
        };
        if dead.is_empty() {
            return Ok(more);
        }
        let mut store = self.lock();
        #[cfg(not(test))]
        release_pin_refs(&mut store, self.session.condition(), &dead)?;
        #[cfg(test)]
        {
            let before_release = self
                .cleanup
                .lock()
                .unwrap_or_else(|e| e.into_inner())
                .before_release
                .take();
            release_pin_refs_with(&mut store, self.session.condition(), &dead, || {
                if let Some(hook) = before_release {
                    hook()?;
                }
                Ok(())
            })?;
        }
        drop(store);
        let mut state = self.cleanup.lock().unwrap_or_else(|e| e.into_inner());
        for (key, _) in dead {
            state.records.remove(&key);
        }
        Ok(more)
    }
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct SessionReap {
    pub revoked: bool,
    pub pins_removed: usize,
    pub complete: bool,
}

/// First remove root ownership through the barrier, then physically forget
/// only these one-use pin tokens. Retry also handles a prior committed release
/// whose acknowledgement/physical cleanup was interrupted.
pub(super) fn release_pin_refs(
    store: &mut PgBlockStore,
    session: RefCondition,
    pins: &[(String, u64)],
) -> Result<(), SemanticError> {
    release_pin_refs_with(store, session, pins, || Ok(()))
}

fn release_pin_refs_with(
    store: &mut PgBlockStore,
    session: RefCondition,
    pins: &[(String, u64)],
    before_publish: impl FnOnce() -> Result<(), SemanticError>,
) -> Result<(), SemanticError> {
    let gc = store.read_ref("system/gc")?;
    let mut guards = vec![
        session.clone(),
        RefCondition {
            key: "system/gc".into(),
            expected: gc.map(|r| r.revision),
        },
    ];
    let mut changes = Vec::new();
    let mut tombstones = Vec::new();
    for (key, revision) in pins {
        match store.read_ref(key)? {
            None => {}
            Some(found) if found.value.is_none() => tombstones.push((key.clone(), found.revision)),
            Some(found) if found.revision == *revision => {
                guards.push(RefCondition {
                    key: key.clone(),
                    expected: Some(*revision),
                });
                changes.push(RefChange {
                    key: key.clone(),
                    value: None,
                });
            }
            Some(_) => {
                return Err(SemanticError::conflict(
                    "storage/read-pin-changed",
                    "Released pin has an unexpected revision",
                ));
            }
        }
    }
    if !changes.is_empty() {
        before_publish()?;
        let result = match super::super::ownership::publish_refs(store, &guards, &changes) {
            Err(error) if error.code == "storage/ownership-contention" => {
                return Err(release_conflict(store, &guards)?);
            }
            result => result?,
        };
        let BatchOutcome::Applied(refs) = result else {
            return Err(release_conflict(store, &guards)?);
        };
        for change in changes {
            let reference = refs
                .iter()
                .find(|(key, _)| key == &change.key)
                .ok_or_else(|| {
                    fault(
                        "storage/read-pin-result",
                        "Release omitted its pin reference",
                    )
                })?;
            tombstones.push((change.key, reference.1.revision));
        }
    }
    if tombstones.is_empty() {
        return Ok(());
    }
    let gc = store.read_ref("system/gc")?;
    let mut guards = vec![
        session,
        RefCondition {
            key: "system/gc".into(),
            expected: gc.map(|r| r.revision),
        },
    ];
    guards.extend(tombstones.iter().map(|(key, revision)| RefCondition {
        key: key.clone(),
        expected: Some(*revision),
    }));
    let keys = tombstones
        .into_iter()
        .map(|(key, _)| key)
        .collect::<Vec<_>>();
    match store.forget_ephemeral_refs(&keys, &guards)? {
        crate::storage::Guarded::Applied(_) => Ok(()),
        crate::storage::Guarded::Conflict(_) => Err(release_conflict(store, &guards)?),
    }
}

/// Retry only contention with unchanged session and pin authority. This check
/// grants no write permission: the next attempt captures and CASes fresh guards.
fn release_conflict(
    store: &mut PgBlockStore,
    guards: &[RefCondition],
) -> Result<SemanticError, SemanticError> {
    for guard in guards.iter().filter(|guard| guard.key != "system/gc") {
        if store.read_ref(&guard.key)?.as_ref().map(|r| r.revision) != guard.expected {
            return Ok(SemanticError::conflict(
                "storage/read-pin-changed",
                "Pin or session authority changed during cleanup",
            ));
        }
    }
    Ok(SemanticError::conflict(
        "storage/read-pin-contention",
        "GC or ownership contention interrupted read-pin cleanup",
    ))
}

/// One bounded orphan-session step. The caller enumerates session references;
/// this helper never scans database trees or interprets any pin payload.
pub(crate) fn reap_read_session(
    store: &mut PgBlockStore,
    key: &str,
    observed: &Reference,
    now_ms: u64,
    grace_ms: u64,
    maximum_pins: usize,
) -> Result<SessionReap, SemanticError> {
    let suffix = key
        .strip_prefix("sessions/read/")
        .filter(|suffix| {
            suffix.len() == 32
                && suffix
                    .bytes()
                    .all(|b| b.is_ascii_hexdigit() && !b.is_ascii_uppercase())
        })
        .ok_or_else(|| fault("storage/read-session-key", "Invalid reader-session key"))?;
    if !(1..=32).contains(&maximum_pins) {
        return Err(SemanticError::incorrect(
            "storage/read-session-batch",
            "Session pin batches must contain 1–32 entries",
        ));
    }
    if !store.try_lock_session_exclusive(key)? {
        return Ok(SessionReap::default());
    }
    let result = (|| {
        let Some(mut current) = store.read_ref(key)? else {
            return Ok(SessionReap {
                complete: true,
                ..Default::default()
            });
        };
        if current.revision != observed.revision {
            return Ok(SessionReap::default());
        }
        let Some(bytes) = current.value.as_deref() else {
            forget_session(store, key, current.revision)?;
            return Ok(SessionReap {
                complete: true,
                ..Default::default()
            });
        };
        let mut record = SessionRecord::decode(bytes)?;
        let mut report = SessionReap::default();
        if record.revoked_ms.is_none() {
            record.revoked_ms = Some(now_ms.max(record.created_ms));
            let outcome = super::super::ownership::publish_refs(
                store,
                &[RefCondition {
                    key: key.into(),
                    expected: Some(current.revision),
                }],
                &[RefChange {
                    key: key.into(),
                    value: Some(record.encode()),
                }],
            )?;
            let BatchOutcome::Applied(refs) = outcome else {
                return Ok(report);
            };
            current = refs
                .into_iter()
                .find(|(name, _)| name == key)
                .ok_or_else(|| {
                    fault(
                        "storage/read-session-result",
                        "Revocation omitted its reference",
                    )
                })?
                .1;
            report.revoked = true;
        }
        let prefix = format!("pins/read/{suffix}/");
        // Include tombstones left by interrupted physical cleanup. The prefix
        // itself is never a pin key, so it is a safe exclusive lower bound.
        let pins = store
            .list_refs(Some(&prefix), maximum_pins)?
            .into_iter()
            .take_while(|(name, _)| name.starts_with(&prefix))
            .collect::<Vec<_>>();
        // An empty session retains no cold-readable value. Otherwise retain
        // every pin for the full post-revocation reconnect grace.
        if !pins.is_empty() && now_ms.saturating_sub(record.revoked_ms.unwrap()) < grace_ms {
            return Ok(report);
        }
        report.pins_removed = pins
            .iter()
            .filter(|(_, reference)| reference.value.is_some())
            .count();
        release_pin_refs(
            store,
            RefCondition {
                key: key.into(),
                expected: Some(current.revision),
            },
            &pins
                .iter()
                .map(|(name, pin)| (name.clone(), pin.revision))
                .collect::<Vec<_>>(),
        )?;
        report.complete = pins.len() < maximum_pins;
        if report.complete {
            forget_session(store, key, current.revision)?;
        }
        Ok(report)
    })();
    let unlock = store.unlock_session_exclusive(key);
    match result {
        Err(error) => Err(error),
        Ok(value) => unlock.map(|()| value),
    }
}

/// The exclusive session lock remains held until this exact-revision removal
/// commits. Session tokens are never reused; a late pin creator must still
/// match the now-absent session revision.
fn forget_session(store: &mut PgBlockStore, key: &str, revision: u64) -> Result<(), SemanticError> {
    let gc = store.read_ref("system/gc")?;
    let guards = [
        RefCondition {
            key: key.into(),
            expected: Some(revision),
        },
        RefCondition {
            key: "system/gc".into(),
            expected: gc.map(|r| r.revision),
        },
    ];
    match store.forget_ephemeral_refs(&[key.to_owned()], &guards)? {
        crate::storage::Guarded::Applied(_) => Ok(()),
        crate::storage::Guarded::Conflict(_) => Err(SemanticError::conflict(
            "storage/read-session-changed",
            "Session cleanup authority changed",
        )),
    }
}

fn revoked() -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unavailable,
        "storage/read-session-revoked",
        "Reader session was revoked; reconnect using a newly authorized database value",
    )
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::storage::BlockDatabase;
    use std::time::{Duration, Instant};

    fn until(mut check: impl FnMut() -> bool) {
        let end = Instant::now() + Duration::from_secs(5);
        while !check() {
            assert!(
                Instant::now() < end,
                "bounded session cleanup did not finish"
            );
            std::thread::sleep(Duration::from_millis(10));
        }
    }

    #[test]
    fn shared_pin_final_drop_cleans_up_off_thread_while_reader_remains_live() {
        let Some(f) = super::super::protection_tests::fixture() else {
            return;
        };
        let database = BlockDatabase::create(&f.config, "drop-pins", Schema::new()).unwrap();
        let reader = BlockReader::connect(&f.config, BlockReadConfig::default()).unwrap();
        let snapshot = reader.capture(&database.reference_key()).unwrap();
        let pin = snapshot.pin_key().unwrap().to_owned();
        let cursor = snapshot.cursor(false, IndexOrder::Eavt);
        let clones = (0..40).map(|_| snapshot.clone()).collect::<Vec<_>>();
        let mut admin = PgBlockStore::connect(&f.config).unwrap();
        assert_eq!(
            admin
                .list_live_refs(&reader.live().store.session.pin_prefix(), None, 128)
                .unwrap()
                .len(),
            1
        );
        drop(clones);
        drop(snapshot);
        assert!(
            admin.read_ref(&pin).unwrap().unwrap().value.is_some(),
            "cursor retains exact owner"
        );

        // If final Drop performed SQL, it would deadlock behind this held
        // reader I/O lock. The dropping thread must finish independently.
        let held_io = reader.live().store.lock();
        let (done, receive) = std::sync::mpsc::channel();
        let dropping = std::thread::spawn(move || {
            drop(cursor);
            done.send(()).unwrap();
        });
        let dropped = receive.recv_timeout(Duration::from_secs(2));
        drop(held_io);
        dropping.join().unwrap();
        dropped.expect("final cursor Drop must not wait for PostgreSQL I/O");
        until(|| admin.read_ref(&pin).unwrap().is_none());
        assert!(
            !admin
                .try_lock_session_exclusive(&reader.live().store.session.key)
                .unwrap(),
            "reader still owns its liveness lock"
        );
        assert!(reader.capture(&database.reference_key()).is_ok());
    }

    #[test]
    fn observer_drop_retries_a_gc_epoch_race_without_another_reader_operation() {
        let Some(f) = super::super::protection_tests::fixture() else {
            return;
        };
        let database = BlockDatabase::create(&f.config, "drop-observer-gc", Schema::new()).unwrap();
        let reader = BlockReader::connect(&f.config, BlockReadConfig::default()).unwrap();
        let snapshot = reader.capture(&database.reference_key()).unwrap();
        let observer = reader
            .pin_report_observer(&database.reference_key(), database.route, &snapshot)
            .unwrap();
        let pin = observer.condition().key;
        let mut admin = PgBlockStore::connect(&f.config).unwrap();
        let forced = Arc::new(std::sync::atomic::AtomicBool::new(false));
        let forced_worker = Arc::clone(&forced);
        let config = f.config.clone();
        reader.live().store.cleanup.lock().unwrap().before_release = Some(Box::new(move || {
            // The cleanup has already read its GC guard. Seal a real epoch
            // before its ownership CAS, not a synthetic returned error.
            let mut collector = crate::storage::ownership::BlockCollector::connect(&config)?;
            collector.advance(Duration::ZERO, 1)?;
            forced_worker.store(true, Ordering::Release);
            Ok(())
        }));
        drop(observer);
        // Leave the reader idle: no capture, explicit release, or second Drop
        // can accidentally kick the lost cleanup job back into motion.
        until(|| admin.read_ref(&pin).unwrap().is_none());
        assert!(forced.load(Ordering::Acquire));
        assert!(
            admin
                .read_ref(snapshot.pin_key().unwrap())
                .unwrap()
                .unwrap()
                .value
                .is_some(),
            "held immutable values retain their own independent pin"
        );
        assert!(
            !admin
                .try_lock_session_exclusive(&reader.live().store.session.key)
                .unwrap(),
            "retry does not revoke the live reader"
        );
        let session = reader.live().store.session.condition();
        assert_eq!(
            release_conflict(&mut admin, std::slice::from_ref(&session))
                .unwrap()
                .code,
            "storage/read-pin-contention"
        );
        let changed = RefCondition {
            expected: None,
            ..session
        };
        assert_eq!(
            release_conflict(&mut admin, &[changed]).unwrap().code,
            "storage/read-pin-changed",
            "lost session authority is terminal, not a retryable GC race"
        );
        assert!(reader.capture(&database.reference_key()).is_ok());
    }

    #[test]
    fn abandoned_session_requires_revocation_then_grace_and_bounds_pin_drain() {
        let Some(mut f) = super::super::protection_tests::fixture() else {
            return;
        };
        let database = BlockDatabase::create(&f.config, "abandoned-pins", Schema::new()).unwrap();
        let limits = BlockReadConfig {
            cache_entries: 0,
            cache_bytes: 0,
            ..Default::default()
        };
        let reader = BlockReader::connect(&f.config, limits.clone()).unwrap();
        let snapshots = (0..7)
            .map(|_| reader.capture(&database.reference_key()).unwrap())
            .collect::<Vec<_>>();
        let borrowed_reader = BlockReader::connect(&f.config, limits).unwrap();
        let borrowed = borrowed_reader.adopt_snapshot(&snapshots[0]);
        let key = reader.live().store.session.key.clone();
        let prefix = reader.live().store.session.pin_prefix();
        let mut gc = PgBlockStore::connect(&f.config).unwrap();
        let active = gc.read_ref(&key).unwrap().unwrap();
        let now = SessionRecord::decode(active.value.as_deref().unwrap())
            .unwrap()
            .created_ms
            + 100;
        assert_eq!(
            reap_read_session(&mut gc, &key, &active, now, 50, 3).unwrap(),
            SessionReap::default()
        );

        // Terminate exactly the backend holding this fixture session's lock,
        // not a shared server/role or unrelated connection.
        let hash = crate::sha256(format!("atomic-storage\0{}\0session/{key}", f.schema).as_bytes());
        let lock = u64::from_be_bytes(hash[..8].try_into().unwrap());
        let rows = f.admin.query(
            "SELECT pg_catalog.pg_terminate_backend(pid) FROM pg_catalog.pg_locks WHERE locktype='advisory' AND classid::bigint=$1 AND objid::bigint=$2 AND objsubid=1 AND mode='ShareLock' AND granted AND pid<>pg_catalog.pg_backend_pid()",
            &[&((lock >> 32) as i64), &((lock & 0xffff_ffff) as i64)],
        ).unwrap();
        assert_eq!(rows.len(), 1);
        assert!(rows[0].get::<_, bool>(0));
        until(|| {
            reap_read_session(&mut gc, &key, &active, now, 50, 3)
                .unwrap()
                .revoked
        });
        assert_eq!(gc.list_live_refs(&prefix, None, 128).unwrap().len(), 7);
        assert_eq!(
            reader.reconnect().unwrap_err().code,
            "storage/read-session-revoked"
        );
        assert_eq!(
            borrowed
                .read_object(borrowed.storage_root_id())
                .unwrap_err()
                .code,
            "storage/read-session-revoked"
        );
        // Already decoded, exported data is not forcibly erased.
        let local = crate::OperationContext::new(crate::OperationKind::Application);
        {
            let _scope = local.enter();
            assert_eq!(borrowed.basis_t(), snapshots[0].basis_t());
        }
        assert_eq!(local.snapshot().sql_calls, 0);
        let revoked = gc.read_ref(&key).unwrap().unwrap();
        assert_eq!(
            reap_read_session(&mut gc, &key, &revoked, now + 49, 50, 3)
                .unwrap()
                .pins_removed,
            0
        );
        let mut removed = 0;
        let mut batches = 0;
        loop {
            let progress = reap_read_session(&mut gc, &key, &revoked, now + 50, 50, 3).unwrap();
            assert!(progress.pins_removed <= 3);
            removed += progress.pins_removed;
            batches += 1;
            if progress.complete {
                break;
            }
            assert!(batches <= 4);
        }
        assert_eq!(removed, 7);
        assert_eq!(batches, 3);
        assert!(
            gc.read_ref(&key).unwrap().is_none(),
            "one-use session key is reclaimed"
        );
        assert!(gc.list_live_refs(&prefix, None, 128).unwrap().is_empty());
        assert!(
            gc.list_refs(Some(&prefix), 1)
                .unwrap()
                .first()
                .is_none_or(|(name, _)| !name.starts_with(&prefix)),
            "one-use pin tombstones are also reclaimed"
        );
        eprintln!(
            "READER_SESSION pins={removed} batches={batches} max_pins_per_batch=3 grace_ms=50"
        );
    }

    #[test]
    fn session_descriptor_is_fixed_versioned_and_monotone() {
        for record in [
            SessionRecord {
                created_ms: 7,
                revoked_ms: None,
            },
            SessionRecord {
                created_ms: 7,
                revoked_ms: Some(9),
            },
        ] {
            let bytes = record.encode();
            assert_eq!(SessionRecord::decode(&bytes).unwrap(), record);
            assert!(SessionRecord::decode(&bytes[..21]).is_err());
            let mut invalid = bytes.clone();
            invalid.push(0);
            assert!(SessionRecord::decode(&invalid).is_err());
            invalid = bytes;
            invalid[4] = 2;
            assert!(SessionRecord::decode(&invalid).is_err());
        }
        assert!(
            SessionRecord::decode(
                &SessionRecord {
                    created_ms: 9,
                    revoked_ms: Some(7)
                }
                .encode()
            )
            .is_err()
        );
    }
}
