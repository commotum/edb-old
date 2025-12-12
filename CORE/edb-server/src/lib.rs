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
    Pull(i64, Vec<edb_pull::AttrSpec>, tokio::sync::oneshot::Sender<Result<serde_json::Value, String>>),
}

#[derive(Serialize)]
struct BasisResp { t: i64 }

#[derive(Deserialize)]
struct PullReq { eid: i64, specs: Vec<edb_pull::AttrSpec> }

pub async fn build_router(db_path: String) -> Router {
    let (cmd_tx, mut cmd_rx) = tokio::sync::mpsc::channel::<Command>(128);
    let (bcast, _b_rx) = tokio::sync::broadcast::channel::<String>(128);
    let state = AppState { db_path: Arc::new(db_path.clone()), cmd_tx: cmd_tx.clone(), bcast: bcast.clone() };

    // Background worker
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
                Command::Pull(eid, specs, tx) => {
                    let puller = edb_pull::Puller::new(&txr.conn);
                    let res = puller.pull_entity(eid, &specs).map_err(|e| e.to_string());
                    let _ = tx.send(res);
                }
            }
        }
    });

    Router::new()
        .route("/transact", post(post_transact))
        .route("/pull", post(post_pull))
        .with_state(state)
}

fn as_500<E: std::fmt::Display>(e: E) -> (axum::http::StatusCode, String) {
    (axum::http::StatusCode::INTERNAL_SERVER_ERROR, e.to_string())
}

async fn post_transact(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Result<Json<serde_json::Value>, (axum::http::StatusCode, String)> {
    let ops = match body.as_array() { Some(a) => a.clone(), None => return Err((axum::http::StatusCode::BAD_REQUEST, "expected array".into())) };
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::Transact(ops, tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => Ok(Json(val)),
        Err(e) => Err(as_500(e)),
    }
}

async fn post_pull(State(state): State<AppState>, Json(req): Json<PullReq>) -> Result<Json<serde_json::Value>, (axum::http::StatusCode, String)> {
    let (tx, rx) = tokio::sync::oneshot::channel();
    state.cmd_tx.send(Command::Pull(req.eid, req.specs, tx)).await.map_err(as_500)?;
    match rx.await.map_err(as_500)? {
        Ok(val) => Ok(Json(val)),
        Err(e) => Err(as_500(e)),
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use edb_encoding::ValueType;
    use edb_schema::{Attribute, AttrCardinality, AttrUnique};

    fn tmp_db_path(name: &str) -> String {
        let mut p = std::env::temp_dir();
        p.push(format!("{}_{}.db", name, std::process::id()));
        p.to_string_lossy().to_string()
    }

    #[tokio::test]
    async fn unit_pull_endpoint() {
        let path = tmp_db_path("srv_pull");
        // seed db
        let mut txr = SqliteTransactor::open(&path).expect("open");
        let a_id = Attribute { ident: ":user/id".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::Identity, is_component: false, no_history: false, doc: None, aliases: vec![] };
        let a_name = Attribute { ident: ":user/name".into(), value_type: ValueType::String, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
        txr.install_attribute(&a_id).unwrap();
        txr.install_attribute(&a_name).unwrap();
        let ops = vec![serde_json::json!(["add","temp:u",":user/id","UU"]), serde_json::json!(["add","temp:u",":user/name","Neo"])];
        let _ = txr.apply_tx(&ops).unwrap();
        let eid: i64 = txr.conn.query_row("SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2", rusqlite::params![":user/id","S:UU"], |r| r.get(0)).unwrap();
        drop(txr);
        // build state + worker
        let (cmd_tx, mut cmd_rx) = tokio::sync::mpsc::channel::<Command>(128);
        let (bcast, _b_rx) = tokio::sync::broadcast::channel::<String>(128);
        let state = AppState { db_path: Arc::new(path.clone()), cmd_tx: cmd_tx.clone(), bcast: bcast.clone() };
        tokio::spawn(async move {
            let mut txr = SqliteTransactor::open(&path).expect("open transactor");
            while let Some(cmd) = cmd_rx.recv().await {
                match cmd {
                    Command::Transact(ops, tx) => {
                        let res = txr.apply_tx(&ops)
                            .map(|rep| { let _ = bcast.send(serde_json::to_string(&rep).unwrap()); serde_json::to_value(rep).unwrap() })
                            .map_err(|e| e.to_string());
                        let _ = tx.send(res);
                    }
                    Command::Pull(eid, specs, tx) => {
                        let puller = edb_pull::Puller::new(&txr.conn);
                        let res = puller.pull_entity(eid, &specs).map_err(|e| e.to_string());
                        let _ = tx.send(res);
                    }
                }
            }
        });
        // call post_pull directly
        let specs = vec![edb_pull::AttrSpec::Attr(":user/name".into())];
        let req = PullReq { eid, specs };
        let resp = super::post_pull(State(state), Json(req)).await.unwrap();
        let v = resp.0;
        assert_eq!(v.get(":user/name").unwrap(), &serde_json::json!("Neo"));
    }
}
