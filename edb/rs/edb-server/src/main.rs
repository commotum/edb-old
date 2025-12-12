use axum::{routing::{get, post}, Router, extract::{State, Path}, Json};
use axum::response::sse::{Sse, Event};
use base64::Engine;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use edb_transactor::SqliteTransactor;
use tokio_stream::wrappers::BroadcastStream;
use futures::StreamExt;

#[derive(Clone)]
struct AppState {
    db_path: Arc<String>,
    cmd_tx: tokio::sync::mpsc::Sender<Command>,
    bcast: tokio::sync::broadcast::Sender<String>,
}

enum Command {
    Transact(Vec<serde_json::Value>, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    SubmitEnv(Vec<u8>, Vec<u8>, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    GetDb(tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    GetHeads(tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
    GetTxEnvelope(Vec<u8>, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
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
    let state = AppState { db_path: Arc::new(db_path.clone()), cmd_tx: cmd_tx.clone(), bcast: bcast.clone() };

    // Background worker: single transactor applying commands and broadcasting tx-reports
    tokio::spawn(async move {
        let mut txr = SqliteTransactor::open(&db_path).expect("open transactor");
        while let Some(cmd) = cmd_rx.recv().await {
            match cmd {
                Command::Transact(ops, tx) => {
                    let res = txr.apply_tx(&ops)
                        .map(|rep| { let _ = bcast.send(serde_json::to_string(&rep).unwrap()); serde_json::to_value(rep).unwrap() })
                        .map_err(|e| e.to_string());
                    let _ = tx.send(res);
                }
                Command::SubmitEnv(unsigned, sig, tx) => {
                    let res = txr.submit_envelope(&unsigned, &sig)
                        .map(|rep| { let _ = bcast.send(serde_json::to_string(&rep).unwrap()); serde_json::to_value(rep).unwrap() })
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
        .route("/subscribe", get(get_subscribe))
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
