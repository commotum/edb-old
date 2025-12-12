  You are working in the EDB repo. Here’s the current state and what to do next.

  What’s implemented

  - Encoding and tuples
      - Canonical encode/decode for value types (long, double with NaN/−0 normalization, boolean, string NFC, keyword, uuid, instant, ref, bytes, uint8; plus BigInt/Decimal,
        float32/16/bfloat16) in edb/rs/edb-encoding.
      - Tuple v1 (homogeneous) with header + V* lexicographic order.
  - Schema and transactions
      - Attribute model (ident, valueType, cardinality, unique, isComponent, noHistory, doc, aliases) in edb/rs/edb-schema.
      - JSON tx grammar (add/retract/cas, tx-fn expansion, tx-meta, map-form sugar), validation (type checks, unique identity upsert, unique value reject, cardinality-one implicit
        retract, CAS), retract-entity cascade via DbView in edb/rs/edb-tx.
      - Tests for grammar, CAS, implicit retract, map-form, tx-fns all pass.
  - Store and transactor (SQLite)
      - Segments, roots (CAS), append-only log in edb/rs/edb-store-sqlite.
      - Transactor apply_tx writes current + unique_idx, appends log, assigns local t, txInstant monotonic bump, tx-reports; replay_current_from_log; as_of_entity_attrs in edb/rs/
        edb-transactor.
      - Tests cover smoke, replay, tx meta/as-of.
  - Indexes
      - EAVT with merge threshold (maybe_merge) + EA/EAV seek helpers.
      - AVET (A,V,E,T): equality and range scans implemented and tested for long, double, instant, BigInt, Decimal.
      - VAET (reverse refs) with scan_v tested.
  - Envelope v1 (linear mode)
      - edb/rs/edb-envelope: unsigned CBOR envelope (canonical op ordering), TxId SHA‑256, Ed25519 sign/verify, TxOps conversion.
      - edb/rs/edb-transactor: submit_envelope(unsigned, sig) verifies, enforces parent==head, stores envelope+edges, updates heads, applies tx, logs JSON for replay.
      - DDL: tx_envelopes, tx_edges, heads.
      - Envelope test passes.
  - HTTP server (MVP)
      - edb/rs/edb-server with endpoints:
          - POST /transact (JSON ops) -> TxReport
          - POST /submit-envelope (unsigned_b64, sig_b64) -> TxReport
          - GET /db -> {t}
          - POST /sync -> {t} (blocks until basis ≥ requested t)
          - GET /heads -> [hex txids]
          - GET /tx/{txid} -> unsigned_b64, sig_b64, author_pk_hex, authored_at
          - GET /subscribe -> continuous SSE stream that polls basis t and emits {"t": cur} on change
      - Config env vars: EDB_SQLITE (default target/edb.sqlite), EDB_BIND (default 127.0.0.1:8080)
  - Decision notes
      - JSON at API edge (strict mapping); always sign canonical CBOR envelopes (never JSON).
      - String prefix scans are not supported via AVET due to length-prefixing; equality and bounded ranges are supported; prefix goes to future full-text.

  All tests currently pass across the workspace.

  What to build next (ordered plan)

  1. DB‑level time views
      - Implement db-level constructors for as‑of, since, history (beyond per-entity helpers) with a consistent API surface.
      - Add tests covering multiple transactions and verifying view correctness.
  2. /subscribe streaming from tx reports
      - Replace poll-based SSE with true tx report streaming:
          - Create a long‑lived background transactor (or a background thread) and a broadcast channel of tx-reports.
          - Keep the transactor object in a Send+Sync wrapper (e.g., run it in a background thread and interact via message channels) so axum handlers remain Send.
          - Update /subscribe to stream from the broadcast channel (tokio::sync::broadcast or mpsc+fanout).
          - Ensure server still supports /transact and /submit-envelope by sending messages to the background transactor.
      - Add an SSE client test (optional) or a manual test recipe.
  3. Schema alias writer
      - Add install_alias(alias, target) in the transactor, update aliases table, and write tests.
      - Confirm alias resolution already used in get_attr still works.
  4. Observability
      - Add basic metrics (merge counts/latency, tx count/latency, basis t) and log records (tx_id, t, author_pk).
      - Expose metrics endpoint (e.g., /metrics JSON) if simple to wire.
  5. Query/Pull scaffolding
      - Define query AST and algebrizer interfaces; add planner stubs that push ranges to AVET and joins via EAVT/AEVT.
      - Pull pattern normalization and options (:as/:default/:limit), reverse navigation; no engine yet.
  6. Index polish (optional next)
      - Adopt multi‑segment root and a simple compaction policy.
      - Add small tests for EAVT scan helpers (force merge with low threshold).

  Constraints and reminders

  - Keep JSON mapping strict (ints > 2^53 as strings; BigInt/Decimal as canonical strings; NaN/+Inf/−Inf as strings; base64 for bytes; keywords as “:ns/name”).
  - Always sign/verify canonical CBOR envelope, not JSON.
  - Avoid adding non‑Send types into axum state; use background workers + channels where needed.

  How to run

  - Build all: cargo test
  - Run server: EDB_SQLITE=target/edb.sqlite EDB_BIND=127.0.0.1:8080 cargo run -p edb-server

  Deliverables for this session

  - Add DB-level as‑of/since/history constructors + tests.
  - Implement proper tx-report streaming for /subscribe (background transactor + broadcast).
  - Add install_alias(alias, target) + tests.
  - Optional: add /metrics basics (counts/latencies).
  - Leave the server endpoints documented; keep existing tests green.