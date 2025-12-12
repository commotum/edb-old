use axum::{routing::{get, post}, Router, extract::{State, Path}, Json};
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
}

async fn get_metrics(State(state): State<AppState>) -> Result<Json<MetricsResp>, (axum::http::StatusCode, String)> {
    let tx_count = state.metrics.tx_count.load(Ordering::Relaxed);
    let total_ms = state.metrics.tx_total_ms.load(Ordering::Relaxed);
    let last_t = state.metrics.last_t.load(Ordering::Relaxed);
    let avg = if tx_count == 0 { 0.0 } else { (total_ms as f64) / (tx_count as f64) };
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
enum Op { Eq, Ge, Gt, Le, Lt, Between }

#[derive(Deserialize, Clone)]
struct WhereAV {
    a: String,
    #[serde(default = "default_eq")]
    op: Op,
    value: Option<serde_json::Value>,
    ge: Option<serde_json::Value>,
    lt: Option<serde_json::Value>,
    le: Option<serde_json::Value>,
    gt: Option<serde_json::Value>,
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

fn run_query(conn: &rusqlite::Connection, db_path: &str, req: &QueryReq) -> Result<Vec<i64>, String> {
    use edb_encoding::{ValueType, encode_scalar};
    // Resolve value type for attr
    let mut acc: Option<std::collections::BTreeSet<i64>> = None;
    for w in &req.r#where {
        let vt_i: Option<i64> = conn
            .query_row("SELECT vt FROM attrs WHERE ident=?1", rusqlite::params![&w.a], |r| r.get(0))
            .optional()
            .map_err(|e| e.to_string())?;
        let vt = vt_i.ok_or_else(|| format!("unknown attribute {}", w.a)).and_then(|i| map_vt(i).ok_or("unsupported vt".into()))?;
        let mut idx = edb_index::AvetIndexer::open(db_path).map_err(|e| e.to_string())?;
        let encode = |val: &serde_json::Value| -> Result<Vec<u8>, String> { encode_scalar(vt, val).map_err(|e| e) };
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
        }
        let set: std::collections::BTreeSet<i64> = rows.into_iter().collect();
        acc = Some(match acc { Some(prev) => prev.intersection(&set).cloned().collect(), None => set });
    }
    Ok(acc.unwrap_or_default().into_iter().collect())
}

fn map_vt(i: i64) -> Option<edb_encoding::ValueType> {
    match i {
        1 => Some(edb_encoding::ValueType::Long),
        2 => Some(edb_encoding::ValueType::Double),
        3 => Some(edb_encoding::ValueType::Boolean),
        4 => Some(edb_encoding::ValueType::String),
        5 => Some(edb_encoding::ValueType::Keyword),
        6 => Some(edb_encoding::ValueType::Uuid),
        7 => Some(edb_encoding::ValueType::Instant),
        8 => Some(edb_encoding::ValueType::Ref),
        9 => Some(edb_encoding::ValueType::Bytes),
        10 => Some(edb_encoding::ValueType::Uint8),
        11 => Some(edb_encoding::ValueType::Bigint),
        12 => Some(edb_encoding::ValueType::Decimal),
        13 => Some(edb_encoding::ValueType::Float32),
        14 => Some(edb_encoding::ValueType::Float16),
        15 => Some(edb_encoding::ValueType::Bfloat16),
        _ => None,
    }
}
