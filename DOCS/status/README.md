Status Overview

- Purpose: snapshot of current capabilities, open gaps, and sprint focus.
- Audience: engineers and PMs; keep concise and current. Link deeper specs in DOCS/spec.

What’s Done (high level)
- Encoding + tuples (canonical); tx grammar + validation (add/retract/CAS, tempids, lookup refs, uniqueness, implicit retract);
- Durable log + monotonic t + replay; schema catalog + aliases; unique enforcement;
- Indexes: EAVT/AEVT/AVET/VAET with multi‑segment roots and simple compaction;
- Envelope v1 (linear mode): CBOR unsigned, Ed25519, TxId, storage (tx_envelopes/tx_edges/heads), submit/apply + negatives;
- API server: transact, submit‑envelope, db, sync(t), heads, get envelope, subscribe (SSE tx‑reports), metrics, pull;
- Pull (MVP): forward/reverse attrs, nested, :as and :default.

What’s Left (MVP next)
- Pull: cardinality‑many and recursion limit; polish options;
- Query skeleton: JSON AST, AVET pushdowns for ranges, joins via EAVT/AEVT, result projector; POST /q;
- Observability: merge/compaction latency + tracing spans; minimal /health;
- Index polish: more seek tests; simple retention tuning.

Links
- Plan/spec: ../spec/EDB-PLAN.md
- Build order: ../spec/BUILD_ORDER.md
- Research: ../research/
