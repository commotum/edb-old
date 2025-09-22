# EDB P2P Sync — MVP Plan

Goal
- Peer‑to‑peer sync that preserves Datomic‑class replay/time travel, runs on ubiquitous transports, and works offline.

Model
- Log: append‑only sequence of signed transactions; each tx includes: author key, timestamp (logical), parents (hashes), and body (add/retract datoms + metadata).
- Identity: Ed25519 keypair per device; optional user identity binding.
- Ordering: causal via parent links; total order per replica via local t; merge resolves by topological order + constraints.

Replication
- Transport: HTTP/gRPC as baseline; optional local discovery (mDNS/WebRTC) for LAN.
- Protocol: request/response by hash ranges; advertise heads; fetch missing txs; verify signatures; apply to local log.
- Security: TLS in transit; optional payload encryption (per‑db key wrapped for peers).

Constraints & Conflicts
- Uniqueness: per‑attr unique indexes enforce at merge; conflicting txs cause retry or higher‑level resolution policy.
- Tx functions: deterministic, sandboxed (WASM), validated at apply.
- Partitions: optional sharding by partition ID; concurrent writers per partition.

Observability
- Tx‑reports emitted on apply; subscriptions for peers/clients.
- Checkpoints/snapshots for fast catch‑up.

MVP Tasks
- [ ] Define tx envelope format (CBOR/JSON + detached signatures)
- [ ] Implement local append‑only log (SQLite/Postgres table)
- [ ] Heads/parents management (DAG)
- [ ] Simple HTTP sync (push/pull by head diff)
- [ ] Signature verification (Ed25519)
- [ ] Unique index enforcement on merge
- [ ] Tx‑report subscription API
- [ ] Basic recovery from partial writes / power loss

Open Questions
- Clock: hybrid logical timestamps vs. server sequencing for “t”?
- Access control: per‑namespace/attribute ACL distribution in P2P?
- Large payloads: out‑of‑band blob store + content addresses.
