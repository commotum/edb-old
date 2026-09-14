use axum::{routing::{get, post}, Router, extract::{State, Path}, Json, body::Bytes};
use axum::response::sse::{Sse, Event};
use base64::Engine;
use rusqlite::OptionalExtension;
use serde::{Deserialize, Serialize};
use std::sync::{Arc, atomic::{AtomicU64, AtomicI64, Ordering}};
use edb_transactor::SqliteTransactor;
use tokio_stream::wrappers::BroadcastStream;
use futures::StreamExt;
use std::time::Instant;

#[derive(Clone)]
struct AppState {
    db_path: Arc<String>,
    cmd_tx: tokio::sync::mpsc::Sender<Command>,
    bcast: tokio::sync::broadcast::Sender<String>,
    metrics: Arc<Metrics>,
}

enum Command {
    Transact(Vec<serde_json::Value>, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    TransactEdn(String, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    SubmitEnv(Vec<u8>, Vec<u8>, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    GetDb(tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    GetHeads(tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    GetTxEnvelope(Vec<u8>, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    Pull(i64, Vec<edb_pull::AttrSpec>, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    Query(QueryReq, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
}

struct Metrics {
    tx_count: AtomicU64,
    tx_total_ms: AtomicU64,
    last_t: AtomicI64,
    merges_total: AtomicU64,
    compactions_total: AtomicU64,
    eavt_segments: AtomicU64,
    aevt_segments: AtomicU64,
    avet_segments: AtomicU64,
    vaet_segments: AtomicU64,
    merge_ms_total: AtomicU64,
    compaction_ms_total: AtomicU64,
    merge_events: AtomicU64,
    compaction_events: AtomicU64,
}

#[derive(Deserialize)]
struct SyncReq { t: i64 }

#[derive(Serialize)]
struct SyncResp { t: i64 }

#[derive(Serialize)]
struct BasisResp { t: i64 }

#[tokio::main]
async fn main() {
    // Read DB path from env or default
    let db_path = std::env::var("EDB_SQLITE").unwrap_or_else(|_| "target/edb.sqlite".to_string());
    let (cmd_tx, mut cmd_rx) = tokio::sync::mpsc::channel::<Command>(128);
    let (bcast, _b_rx) = tokio::sync::broadcast::channel::<String>(128);
    let metrics = Arc::new(Metrics {
        tx_count: AtomicU64::new(0),
        tx_total_ms: AtomicU64::new(0),
        last_t: AtomicI64::new(0),
        merges_total: AtomicU64::new(0),
        compactions_total: AtomicU64::new(0),
        eavt_segments: AtomicU64::new(0),
        aevt_segments: AtomicU64::new(0),
        avet_segments: AtomicU64::new(0),
        vaet_segments: AtomicU64::new(0),
        merge_ms_total: AtomicU64::new(0),
        compaction_ms_total: AtomicU64::new(0),
        merge_events: AtomicU64::new(0),
        compaction_events: AtomicU64::new(0),
    });
    let state = AppState { db_path: Arc::new(db_path.clone()), cmd_tx: cmd_tx.clone(), bcast: bcast.clone(), metrics: metrics.clone() };

    // Background worker: single transactor applying commands and broadcasting tx-reports
    tokio::spawn(async move {
        let mut txr = SqliteTransactor::open(&db_path).expect("open transactor");
        while let Some(cmd) = cmd_rx.recv().await {
            match cmd {
                Command::Transact(ops, tx) => {
                    let start = Instant::now();
                    let res = txr.apply_tx(&ops)
                        .map(|rep| {
                            let _ = bcast.send(serde_json::to_string(&rep).unwrap());
                            if let Some(t) = rep.t { metrics.last_t.store(t, Ordering::Relaxed); }
                            metrics.tx_count.fetch_add(1, Ordering::Relaxed);
                            metrics.tx_total_ms.fetch_add(start.elapsed().as_millis() as u64, Ordering::Relaxed);
                            if let Ok(stats) = txr.index_stats() {
                                update_index_metrics(&metrics, stats);
                            }
                            serde_json::to_value(rep).unwrap()
                        })
                        .map_err(|e| e.to_string());
                    let _ = tx.send(res);
                }
                Command::TransactEdn(input, tx) => {
                    let start = Instant::now();
                    let res = txr.apply_tx_edn(&input)
                        .map(|rep| {
                            let _ = bcast.send(serde_json::to_string(&rep).unwrap());
                            if let Some(t) = rep.t { metrics.last_t.store(t, Ordering::Relaxed); }
                            metrics.tx_count.fetch_add(1, Ordering::Relaxed);
                            metrics.tx_total_ms.fetch_add(start.elapsed().as_millis() as u64, Ordering::Relaxed);
                            if let Ok(stats) = txr.index_stats() {
                                update_index_metrics(&metrics, stats);
                            }
                            serde_json::to_value(rep).unwrap()
                        })
                        .map_err(|e| e.to_string());
                    let _ = tx.send(res);
                }
                Command::SubmitEnv(unsigned, sig, tx) => {
                    let start = Instant::now();
                    let res = txr.submit_envelope(&unsigned, &sig)
                        .map(|rep| {
                            let _ = bcast.send(serde_json::to_string(&rep).unwrap());
                            if let Some(t) = rep.t { metrics.last_t.store(t, Ordering::Relaxed); }
                            metrics.tx_count.fetch_add(1, Ordering::Relaxed);
                            metrics.tx_total_ms.fetch_add(start.elapsed().as_millis() as u64, Ordering::Relaxed);
                            if let Ok(stats) = txr.index_stats() {
                                update_index_metrics(&metrics, stats);
                            }
                            serde_json::to_value(rep).unwrap()
                        })
                        .map_err(|e| e.to_string());
                    let _ = tx.send(res);
                }
                Command::GetDb(tx) => {
                    let cur_t: i64 = txr
                        .conn
                        .query_row("SELECT seq FROM log ORDER BY seq DESC LIMIT 1", [], |r| r.get(0))
                        .unwrap_or(0);
                    let _ = tx.send(Ok(serde_json::json!({"t": cur_t})));
                }
                Command::GetHeads(tx) => {
                    let mut stmt = match txr.conn.prepare("SELECT tx_id FROM heads") {
                        Ok(s) => s,
                        Err(e) => { let _ = tx.send(Err(e.to_string())); continue; }
                    };
                    let rows = match stmt.query_map([], |r| r.get::<_, Vec<u8>>(0)) {
                        Ok(r) => r,
                        Err(e) => { let _ = tx.send(Err(e.to_string())); continue; }
                    };
                    let mut out: Vec<String> = Vec::new();
                    let mut err: Option<String> = None;
                    for row in rows {
                        match row {
                            Ok(b) => out.push(hex::encode(b)),
                            Err(e) => { err = Some(e.to_string()); break; }
                        }
                    }
                    if let Some(e) = err { let _ = tx.send(Err(e)); } else { let _ = tx.send(Ok(serde_json::to_value(out).unwrap())); }
                }
                Command::GetTxEnvelope(id_bytes, tx) => {
                    let mut stmt = match txr.conn.prepare("SELECT unsigned, sig, author_pk, authored_at FROM tx_envelopes WHERE tx_id=?1") {
                        Ok(s) => s,
                        Err(e) => { let _ = tx.send(Err(e.to_string())); continue; }
                    };
                    let row = stmt.query_row([&id_bytes], |r| Ok((
                        r.get::<_, Vec<u8>>(0)?,
                        r.get::<_, Vec<u8>>(1)?,
                        r.get::<_, Vec<u8>>(2)?,
                        r.get::<_, Option<i64>>(3)?
                    )));
                    match row {
                        Ok((unsigned, sig, author_pk, authored_at)) => {
                            let resp = serde_json::json!({
                                "tx_id": hex::encode(&id_bytes),
                                "unsigned_b64": base64::engine::general_purpose::STANDARD_NO_PAD.encode(unsigned),
                                "sig_b64": base64::engine::general_purpose::STANDARD_NO_PAD.encode(sig),
                                "author_pk_hex": hex::encode(author_pk),
                                "authored_at": authored_at,
                            });
                            let _ = tx.send(Ok(resp));
                        }
                        Err(_) => { let _ = tx.send(Err("not found".into())); }
                    }
                }
                Command::Pull(eid, specs, tx) => {
    let puller = edb_pull::Puller::new_with_path(&txr.conn, &db_path);
                    let res = puller.pull_entity(eid, &specs)
                        .map_err(|e| e.to_string())
                        .map(|v| v);
                    let _ = tx.send(res);
                }
                Command::Query(req, tx) => {
                    let res = run_query(&txr.conn, &db_path, &req).map_err(|e| e.to_string())
                        .map(|rows| serde_json::json!({"rows": rows}));
                    let _ = tx.send(res);
                }
            }
        }
    });

    let app = Router::new()
        .route("/transact", post(post_transact))
        .route("/transact-edn", post(post_transact_edn))
        .route("/submit-envelope", post(post_submit_envelope))
        .route("/db", get(get_db))
        .route("/sync", post(post_sync))
        .route("/heads", get(get_heads))
        .route("/tx/:txid", get(get_tx_envelope))
        .route("/pull", post(post_pull))
        .route("/q", post(post_query))
        .route("/health", get(get_health))
        .route("/subscribe", get(get_subscribe))
        .route("/metrics", get(get_metrics))
        .with_state(state);

    let addr = std::env::var("EDB_BIND").unwrap_or_else(|_| "127.0.0.1:8080".to_string());
    println!("Serving on {addr}");
    let listener = tokio::net::TcpListener::bind(&addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}

async fn post_transact(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Result<Json<serde_json::Value>, (axum::http::StatusCode, String)> {
    // Expect body to be an array of ops
    let ops = match body.as_array() { Some(a) => a.clone(), None => return Err((axum::http::StatusCode::BAD_REQUEST, "expected array".into())) };
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::Transact(ops, tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => Ok(Json(val)),
        Err(e) => Err(as_500(e)),
    }
}

async fn post_transact_edn(State(state): State<AppState>, body: Bytes) -> Result<Json<serde_json::Value>, (axum::http::StatusCode, String)> {
    let input = std::str::from_utf8(&body).map_err(|e| (axum::http::StatusCode::BAD_REQUEST, e.to_string()))?;
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::TransactEdn(input.to_string(), tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => Ok(Json(val)),
        Err(e) => Err(as_500(e)),
    }
}

async fn get_db(State(state): State<AppState>) -> Result<Json<BasisResp>, (axum::http::StatusCode, String)> {
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::GetDb(tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => {
            let t = val.get("t").and_then(|v| v.as_i64()).unwrap_or(0);
            Ok(Json(BasisResp { t }))
        }
        Err(e) => Err(as_500(e)),
    }
}

async fn post_sync(State(state): State<AppState>, Json(req): Json<SyncReq>) -> Result<Json<SyncResp>, axum::http::StatusCode> {
    let target = req.t;
    loop {
        let txr = SqliteTransactor::open(&state.db_path).map_err(|_| axum::http::StatusCode::INTERNAL_SERVER_ERROR).unwrap();
        let cur: i64 = txr.conn.query_row("SELECT seq FROM log ORDER BY seq DESC LIMIT 1", [], |r| r.get(0)).unwrap_or(0);
        if cur >= target { return Ok(Json(SyncResp { t: cur })); }
        tokio::time::sleep(std::time::Duration::from_millis(50)).await;
    }
}

async fn get_heads(State(state): State<AppState>) -> Result<Json<Vec<String>>, (axum::http::StatusCode, String)> {
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::GetHeads(tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => {
            // Expect a JSON array of strings
            let mut out = Vec::new();
            if let Some(arr) = val.as_array() { for v in arr { if let Some(s) = v.as_str() { out.push(s.to_string()); } } }
            Ok(Json(out))
        }
        Err(e) => Err(as_500(e)),
    }
}

async fn get_tx_envelope(State(state): State<AppState>, Path(txid_hex): Path<String>) -> Result<Json<serde_json::Value>, (axum::http::StatusCode, String)> {
    let id_bytes = hex::decode(&txid_hex).map_err(|e| (axum::http::StatusCode::BAD_REQUEST, e.to_string()))?;
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::GetTxEnvelope(id_bytes, tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => Ok(Json(val)),
        Err(e) if e == "not found" => Err((axum::http::StatusCode::NOT_FOUND, e)),
        Err(e) => Err(as_500(e)),
    }
}

fn as_500<E: std::fmt::Display>(e: E) -> (axum::http::StatusCode, String) {
    (axum::http::StatusCode::INTERNAL_SERVER_ERROR, e.to_string())
}

#[derive(Deserialize)]
struct SubmitEnvReq { unsigned_b64: String, sig_b64: String }

async fn post_submit_envelope(State(state): State<AppState>, Json(req): Json<SubmitEnvReq>) -> Result<Json<serde_json::Value>, (axum::http::StatusCode, String)> {
    let unsigned = base64::engine::general_purpose::STANDARD_NO_PAD.decode(req.unsigned_b64.as_bytes()).map_err(as_500)?;
    let sig = base64::engine::general_purpose::STANDARD_NO_PAD.decode(req.sig_b64.as_bytes()).map_err(as_500)?;
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::SubmitEnv(unsigned, sig, tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => Ok(Json(val)),
        Err(e) => Err(as_500(e)),
    }
}

async fn get_subscribe(State(state): State<AppState>) -> Sse<impl futures::Stream<Item = Result<Event, std::convert::Infallible>>> {
    let rx = state.bcast.subscribe();
    let stream = BroadcastStream::new(rx).filter_map(|item| async move {
        match item { Ok(s) => Some(Ok::<Event, std::convert::Infallible>(Event::default().data(s))), Err(_) => None }
    });
    Sse::new(stream)
}

#[derive(Serialize)]
struct MetricsResp {
    tx_count: u64,
    avg_tx_ms: f64,
    last_t: i64,
    merges_total: u64,
    compactions_total: u64,
    eavt_segments: u64,
    aevt_segments: u64,
    avet_segments: u64,
    vaet_segments: u64,
    merge_ms_avg: f64,
    compaction_ms_avg: f64,
}

async fn get_metrics(State(state): State<AppState>) -> Result<Json<MetricsResp>, (axum::http::StatusCode, String)> {
    let tx_count = state.metrics.tx_count.load(Ordering::Relaxed);
    let total_ms = state.metrics.tx_total_ms.load(Ordering::Relaxed);
    let last_t = state.metrics.last_t.load(Ordering::Relaxed);
    let avg = if tx_count == 0 { 0.0 } else { (total_ms as f64) / (tx_count as f64) };
    let merge_ms_total = state.metrics.merge_ms_total.load(Ordering::Relaxed);
    let merge_events = state.metrics.merge_events.load(Ordering::Relaxed);
    let compaction_ms_total = state.metrics.compaction_ms_total.load(Ordering::Relaxed);
    let compaction_events = state.metrics.compaction_events.load(Ordering::Relaxed);
    let merge_ms_avg = if merge_events == 0 { 0.0 } else { (merge_ms_total as f64) / (merge_events as f64) };
    let compaction_ms_avg = if compaction_events == 0 { 0.0 } else { (compaction_ms_total as f64) / (compaction_events as f64) };
    Ok(Json(MetricsResp {
        tx_count,
        avg_tx_ms: avg,
        last_t,
        merges_total: state.metrics.merges_total.load(Ordering::Relaxed),
        compactions_total: state.metrics.compactions_total.load(Ordering::Relaxed),
        eavt_segments: state.metrics.eavt_segments.load(Ordering::Relaxed),
        aevt_segments: state.metrics.aevt_segments.load(Ordering::Relaxed),
        avet_segments: state.metrics.avet_segments.load(Ordering::Relaxed),
        vaet_segments: state.metrics.vaet_segments.load(Ordering::Relaxed),
        merge_ms_avg,
        compaction_ms_avg,
    }))
}

fn update_index_metrics(metrics: &Metrics, stats: edb_transactor::IndexStats) {
    let prev_e = metrics.eavt_segments.swap(stats.eavt as u64, Ordering::Relaxed);
    let prev_aevt = metrics.aevt_segments.swap(stats.aevt as u64, Ordering::Relaxed);
    let prev_av = metrics.avet_segments.swap(stats.avet as u64, Ordering::Relaxed);
    let prev_v = metrics.vaet_segments.swap(stats.vaet as u64, Ordering::Relaxed);
    adjust(metrics, stats.eavt as u64, prev_e);
    adjust(metrics, stats.aevt as u64, prev_aevt);
    adjust(metrics, stats.avet as u64, prev_av);
    adjust(metrics, stats.vaet as u64, prev_v);
    // Sync cumulative latency totals/events from transactor
    metrics.merge_ms_total.store(stats.merges_ms_total, Ordering::Relaxed);
    metrics.compaction_ms_total.store(stats.compactions_ms_total, Ordering::Relaxed);
    metrics.merge_events.store(stats.merges_events, Ordering::Relaxed);
    metrics.compaction_events.store(stats.compactions_events, Ordering::Relaxed);
}

fn adjust(metrics: &Metrics, newc: u64, oldc: u64) {
    if newc > oldc { metrics.merges_total.fetch_add(newc - oldc, Ordering::Relaxed); }
    if newc < oldc { metrics.compactions_total.fetch_add(oldc - newc, Ordering::Relaxed); }
}
#[derive(Deserialize)]
struct PullReq { eid: i64, specs: Vec<edb_pull::AttrSpec> }

async fn post_pull(State(state): State<AppState>, Json(req): Json<PullReq>) -> Result<Json<serde_json::Value>, (axum::http::StatusCode, String)> {
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::Pull(req.eid, req.specs, tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => Ok(Json(val)),
        Err(e) => Err(as_500(e)),
    }
}

#[derive(Deserialize, Clone)]
#[serde(rename_all = "lowercase")]
enum Op { Eq, Ge, Gt, Le, Lt, Between, Has }

#[derive(Deserialize, Clone)]
struct WhereAV {
    a: String,
    #[serde(default = "default_eq")]
    op: Op,
    value: Option<serde_json::Value>,
    ge: Option<serde_json::Value>,
    lt: Option<serde_json::Value>,
    #[allow(dead_code)]
    le: Option<serde_json::Value>,
    #[allow(dead_code)]
    gt: Option<serde_json::Value>,
    #[serde(default)]
    var: Option<String>,
    #[serde(default)]
    ref_var: Option<String>,
}

fn default_eq() -> Op { Op::Eq }

#[derive(Deserialize, Clone)]
struct QueryReq {
    #[serde(rename = "where")]
    r#where: Vec<WhereAV>,
}

async fn post_query(State(state): State<AppState>, Json(req): Json<QueryReq>) -> Result<Json<serde_json::Value>, (axum::http::StatusCode, String)> {
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::Query(req, tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => Ok(Json(val)),
        Err(e) => Err(as_500(e)),
    }
}

async fn get_health(State(state): State<AppState>) -> Result<Json<serde_json::Value>, (axum::http::StatusCode, String)> {
    let txr = SqliteTransactor::open(&state.db_path).map_err(as_500)?;
    let cur_t: i64 = txr.conn.query_row("SELECT seq FROM log ORDER BY seq DESC LIMIT 1", [], |r| r.get(0)).unwrap_or(0);
    Ok(Json(serde_json::json!({"ok": true, "t": cur_t})))
}

fn run_query(conn: &rusqlite::Connection, db_path: &str, req: &QueryReq) -> Result<serde_json::Value, String> {
    use edb_encoding::{ValueType, encode_scalar};
    // Planner supports value constraints and a single ref-var join producing pairs
    let mut acc: Option<std::collections::BTreeSet<i64>> = None;
    let mut var_sets: std::collections::BTreeMap<String, std::collections::BTreeSet<i64>> = std::collections::BTreeMap::new();
    let mut join_pairs: Option<Vec<(i64,i64)>> = None;
    for w in &req.r#where {
        // Allow 'has' (exists) using AEVT, which does not require a value type
        if matches!(w.op, Op::Has) {
            let aidx = edb_index::AevtIndexer::open(db_path).map_err(|e| e.to_string())?;
            let mut rows: Vec<i64> = Vec::new();
            let mut seen = std::collections::BTreeSet::<(i64, String)>::new();
            for d in aidx.scan_a(&w.a).map_err(|e| e.to_string())? {
                let key = (d.e, d.v_b64.clone());
                if !seen.contains(&key) { // first is latest (T desc within group)
                    if d.added { rows.push(d.e); }
                    seen.insert(key);
                }
            }
            let set: std::collections::BTreeSet<i64> = rows.into_iter().collect();
            if let Some(var) = &w.var { let entry = var_sets.entry(var.clone()).or_insert_with(std::collections::BTreeSet::new); if entry.is_empty() { *entry = set.clone(); } else { *entry = entry.intersection(&set).cloned().collect(); } }
            acc = Some(match acc { Some(prev) => prev.intersection(&set).cloned().collect(), None => set });
            continue;
        }

        let vt_i: Option<i64> = conn
            .query_row("SELECT vt FROM attrs WHERE ident=?1", rusqlite::params![&w.a], |r| r.get(0))
            .optional()
            .map_err(|e| e.to_string())?;
        let vt = vt_i.ok_or_else(|| format!("unknown attribute {}", w.a)).and_then(|i| map_vt(i).ok_or("unsupported vt".into()))?;
        let idx = edb_index::AvetIndexer::open(db_path).map_err(|e| e.to_string())?;
        let encode = |val: &serde_json::Value| -> Result<Vec<u8>, String> { encode_scalar(vt, val).map_err(|e| e) };
        // Handle ref-var join clause
        if let Some(parent_var) = &w.ref_var {
            if vt != ValueType::Ref { return Err("ref_var join requires ref-typed attribute".into()); }
            let child_var = w.var.clone().unwrap_or_else(|| "?e2".to_string());
            let parent_set = var_sets.get(parent_var).cloned().ok_or("unknown ref_var in join")?;
            let mut pairs: Vec<(i64,i64)> = Vec::new();
            for pe in parent_set.iter().cloned() {
                let vb = edb_encoding::encode_scalar(ValueType::Ref, &serde_json::json!(pe)).map_err(|e| e)?;
                for d in idx.scan_av_eq(&w.a, &vb).map_err(|e| e.to_string())? {
                    pairs.push((pe, d.e));
                }
            }
            // Optional filter by child var constraints computed earlier
            if let Some(child_filter) = var_sets.get(&child_var) {
                pairs.retain(|(_pe, ce)| child_filter.contains(ce));
            }
            join_pairs = Some(match join_pairs { Some(prev) => {
                let set_prev: std::collections::BTreeSet<(i64,i64)> = prev.into_iter().collect();
                pairs.into_iter().filter(|p| set_prev.contains(p)).collect()
            }, None => pairs });
            // Populate/update child var set
            let mut child_set: std::collections::BTreeSet<i64> = std::collections::BTreeSet::new();
            if let Some(ref jp) = join_pairs { for (_pe, ce) in jp { child_set.insert(*ce); } }
            var_sets.insert(child_var.clone(), child_set);
            continue;
        }

        let mut rows: Vec<i64> = Vec::new();
        match w.op {
            Op::Eq => {
                let v = w.value.as_ref().ok_or("missing value for eq")?;
                let vb = encode(v)?;
                for d in idx.scan_av_eq(&w.a, &vb).map_err(|e| e.to_string())? { rows.push(d.e); }
            }
            Op::Between => {
                let ge_v = w.ge.as_ref().ok_or("missing ge")?; let lt_v = w.lt.as_ref().ok_or("missing lt")?;
                let ge_b = encode(ge_v)?; let lt_b = encode(lt_v)?;
                for d in idx.scan_av_range(&w.a, &ge_b, Some(&lt_b)).map_err(|e| e.to_string())? { rows.push(d.e); }
            }
            Op::Ge => {
                let ge_v = w.ge.as_ref().or(w.value.as_ref()).ok_or("missing ge")?;
                let ge_b = encode(ge_v)?;
                for d in idx.scan_av_range(&w.a, &ge_b, None).map_err(|e| e.to_string())? { rows.push(d.e); }
            }
            Op::Gt | Op::Le | Op::Lt => { return Err("operators gt/le/lt not yet implemented".into()); }
            Op::Has => unreachable!("handled earlier"),
        }
        let set: std::collections::BTreeSet<i64> = rows.into_iter().collect();
        if let Some(var) = &w.var {
            let entry = var_sets.entry(var.clone()).or_insert_with(std::collections::BTreeSet::new);
            if entry.is_empty() { *entry = set.clone(); } else { *entry = entry.intersection(&set).cloned().collect(); }
        }
        acc = Some(match acc { Some(prev) => prev.intersection(&set).cloned().collect(), None => set });
    }
    if let Some(pairs) = join_pairs {
        let rows: Vec<serde_json::Value> = pairs.into_iter().map(|(a,b)| serde_json::json!([a,b])).collect();
        return Ok(serde_json::Value::Array(rows));
    }
    let rows: Vec<serde_json::Value> = acc.unwrap_or_default().into_iter().map(|e| serde_json::json!(e)).collect();
    Ok(serde_json::Value::Array(rows))
}

fn map_vt(i: i64) -> Option<edb_encoding::ValueType> {
    edb_encoding::ValueType::try_from(i).ok()
}

#[cfg(test)]
mod tests {
    use super::*;
    use edb_schema::{Attribute, AttrCardinality, AttrUnique};
    use edb_encoding::ValueType;

    fn tmp_db_path(name: &str) -> String {
        let mut p = std::env::temp_dir();
        let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_nanos();
        p.push(format!("{}_{}_{}.db", name, std::process::id(), now));
        p.to_string_lossy().to_string()
    }

    #[test]
    fn query_join_basic() {
        let path = tmp_db_path("q_join");
        let mut txr = SqliteTransactor::open(&path).expect("open");
        // Schema
        let a_user_id = Attribute { ident: ":user/id".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::Identity, is_component: false, no_history: false, doc: None, aliases: vec![] };
        let a_user_name = Attribute { ident: ":user/name".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
        let a_post_author = Attribute { ident: ":post/author".into(), value_type: ValueType::Ref, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
        let a_noise = Attribute { ident: ":noise/x".into(), value_type: ValueType::Long, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
        txr.install_attribute(&a_user_id).unwrap();
        txr.install_attribute(&a_user_name).unwrap();
        txr.install_attribute(&a_post_author).unwrap();
        txr.install_attribute(&a_noise).unwrap();

        // Tx1: create user and trigger AVET merge via noise
        let mut ops1: Vec<serde_json::Value> = Vec::new();
        ops1.push(serde_json::json!(["add", "temp:u1", ":user/id", "U1"]));
        ops1.push(serde_json::json!(["add", "temp:u1", ":user/name", "Alice"]));
        for i in 0..1200 { ops1.push(serde_json::json!(["add", format!("temp:n1_{i}"), ":noise/x", i])); }
        txr.apply_tx(&ops1).expect("tx1");

        // Lookup eids
        let u_alice: i64 = txr.conn.query_row(
            "SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2",
            rusqlite::params![":user/id", "S:U1"],
            |r| r.get(0),
        ).unwrap();
        // Tx2: add post for Alice and trigger another AVET merge via noise
        let mut ops2: Vec<serde_json::Value> = Vec::new();
        ops2.push(serde_json::json!(["add", "temp:p1", ":post/author", u_alice]));
        for i in 0..1200 { ops2.push(serde_json::json!(["add", format!("temp:n2_{i}"), ":noise/x", i])); }
        txr.apply_tx(&ops2).expect("tx2");
        let p1: i64 = txr.conn.query_row(
            "SELECT e FROM current WHERE a=?1 AND vjson=?2 LIMIT 1",
            rusqlite::params![":post/author", format!("{{\"Ref\":{}}}", u_alice)],
            |r| r.get(0),
        ).unwrap();

        // Query: users named Alice join posts by that user
        // Sanity check AVET eq before running /q planner
        {
            use edb_encoding::encode_scalar;
            let av = edb_index::AvetIndexer::open(&path).unwrap();
            let vb = encode_scalar(edb_encoding::ValueType::String, &serde_json::json!("Alice")).unwrap();
            let rows = av.scan_av_eq(":user/name", &vb).unwrap();
            eprintln!("avet(:user/name='Alice') count={} eids={:?}", rows.len(), rows.iter().map(|d| d.e).collect::<Vec<_>>());
            let vb_id = encode_scalar(edb_encoding::ValueType::String, &serde_json::json!("U1")).unwrap();
            let rows_id = av.scan_av_eq(":user/id", &vb_id).unwrap();
            eprintln!("avet(:user/id='U1') count={} eids={:?}", rows_id.len(), rows_id.iter().map(|d| d.e).collect::<Vec<_>>());
            let vb2 = encode_scalar(edb_encoding::ValueType::Ref, &serde_json::json!(u_alice)).unwrap();
            let rows2 = av.scan_av_eq(":post/author", &vb2).unwrap();
            eprintln!("avet(:post/author=Ref({})) count={}", u_alice, rows2.len());
        }
        let req = QueryReq {
            r#where: vec![
                WhereAV { a: ":user/name".into(), op: Op::Eq, value: Some(serde_json::json!("Alice")), ge: None, lt: None, le: None, gt: None, var: Some("?u".into()), ref_var: None },
                WhereAV { a: ":post/author".into(), op: Op::Eq, value: None, ge: None, lt: None, le: None, gt: None, var: Some("?p".into()), ref_var: Some("?u".into()) },
            ],
        };
        let rows_json = run_query(&txr.conn, &path, &req).expect("query");
        let arr = rows_json.as_array().expect("array");
        assert!(!arr.is_empty());
        let pair = arr[0].as_array().expect("pair");
        assert_eq!(pair[0].as_i64().unwrap(), u_alice);
        assert_eq!(pair[1].as_i64().unwrap(), p1);

        // Existence clause using AEVT (has attribute :user/name)
        let req2 = QueryReq {
            r#where: vec![
                WhereAV { a: ":user/name".into(), op: Op::Has, value: None, ge: None, lt: None, le: None, gt: None, var: Some("?u".into()), ref_var: None },
            ],
        };
        let rows_json2 = run_query(&txr.conn, &path, &req2).expect("query2");
        let arr2 = rows_json2.as_array().expect("array2");
        assert!(arr2.iter().any(|v| v.as_i64() == Some(u_alice)));
    }
}
