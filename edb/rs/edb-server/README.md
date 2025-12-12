EDB Server

Overview
- Single-node HTTP server wrapping `SqliteTransactor` with a background worker and a broadcast channel for tx-reports.
- Env vars:
  - `EDB_SQLITE` (default `target/edb.sqlite`)
  - `EDB_BIND` (default `127.0.0.1:8080`)

Endpoints
- POST `/transact`
  - Body: JSON array of ops, e.g.
    [
      ["add", "temp:u", ":user/id", "U-1"],
      ["add", "temp:u", ":user/name", "Alice"]
    ]
  - Response: tx-report JSON with `t`, `tx_eid`, `primitives`, `meta`.

- POST `/submit-envelope`
  - Body: `{ "unsigned_b64": "...", "sig_b64": "..." }`
  - Verifies Ed25519 signature, enforces linear parent==head, applies tx, broadcasts report.

- GET `/db`
  - Returns `{ "t": <current log seq> }` from the worker.

- POST `/sync`
  - Body: `{ "t": <target> }`
  - Blocks until current basis >= target; returns `{ "t": <current> }`.

- GET `/heads`
  - Returns array of head tx ids as hex strings.

- GET `/tx/{txid}`
  - Returns `{ tx_id, unsigned_b64, sig_b64, author_pk_hex, authored_at }` for the envelope.

- GET `/subscribe`
  - Server-Sent Events stream of tx-reports (JSON-serialized), powered by `tokio::broadcast`.

Examples
```
curl -s X POST localhost:8080/transact -d '[ ["add","temp:u",":user/id","U-1"] ]' -H 'content-type: application/json'

curl -s localhost:8080/db

curl -N localhost:8080/subscribe
```

Notes
- The server owns a single background transactor to serialize writes; GET endpoints route through the worker for consistency.
- JSON tx grammar supports add/retract/cas, tempids, lookup refs, tx-meta, and map-form sugar.

