# EDB Signed Transaction Envelope & DAG Spec v1

**Status:** Draft / implement-now
**Primary goal:** A transaction can be represented as **canonical bytes**, **signed**, **hashed**, **stored**, **replicated**, and **replayed** deterministically across runtimes.

## 0. Design constraints

This spec assumes (and depends on) three core invariants you already committed to elsewhere:

1. **Value encoding is canonical** for all supported value types (scalars + tuples).
2. **Transaction semantics are deterministic** given a db state (basis) and a tx body.
3. The **transaction log is append-only**; time travel derives from replay/history.

This spec defines the “unit of replication” and “unit of durability” for the log.

---

## 1. Definitions

### 1.1 Terms

* **Author keypair:** Ed25519 keypair. Public key identifies an author/device.
* **Unsigned envelope:** Canonical serialized bytes of the envelope fields **excluding** the signature.
* **Signature:** Ed25519 signature over the unsigned envelope bytes.
* **TxId:** Content hash of the unsigned envelope bytes.
* **Parents:** Set of TxIds this tx claims as causal predecessors.
* **DAG:** Directed acyclic graph formed by parent links.
* **Heads:** TxIds with no known children in the local store.
* **Features:** Explicit capability flags required to interpret/apply the tx correctly.

### 1.2 Normative language

* **MUST / MUST NOT / SHOULD / MAY** are used in the RFC sense.

---

## 2. What the envelope is for

The envelope provides:

1. **Tamper evidence**: Any change to tx content invalidates the signature and TxId.
2. **Stable identity**: TxId is a content hash → dedupe + efficient sync.
3. **Portable authorship**: Author public key is cryptographically bound to tx contents.
4. **Causality**: Parents define what the author built on; enables offline-first DAG replication.
5. **Feature-safe evolution**: New value types/encodings are gated behind explicit features.

---

## 3. Cryptography primitives

### 3.1 Signature

* Algorithm: **Ed25519**
* Signature length: **64 bytes**
* Public key length: **32 bytes**

### 3.2 Hash

Pick one and make it *the* canonical TxId hash.

* **RECOMMENDED:** SHA-256 (ubiquitous, stable, boring)

**This spec assumes SHA-256**:

* `TxId = SHA256(unsigned_envelope_bytes)`
* TxId length: **32 bytes**

> If you choose BLAKE3, treat this as a versioned change (either envelope version bump, or a feature flag like `txid:blake3` that is enforced strictly).

---

## 4. Canonical serialization

### 4.1 Canonical encoding for signing/hashing

**Unsigned envelope bytes MUST be encoded in Canonical CBOR.**

Rationale:

* JSON is human-friendly but hard to canonicalize safely (unless you adopt JCS everywhere).
* Canonical CBOR gives you stable bytes across implementations.

**Rule:** Do not sign raw JSON. JSON may exist only as a debug view.

### 4.2 Detached signature

The signature is **detached**:

* You store/transmit:

  * `unsigned_envelope_bytes`
  * `author_signature_bytes`

You do **not** include the signature inside the bytes that are signed.

---

## 5. Envelope v1 format

### 5.1 Logical model

A transaction envelope v1 consists of:

* Versioning and typing
* Authorship
* Causality (parents)
* Features (capabilities required)
* Optional authored timestamp (not used for ordering)
* Canonical transaction body

### 5.2 Unsigned envelope schema (v1)

Represent the unsigned envelope as a **CBOR array** with **fixed field order**:

```
UnsignedEnvelopeV1 := [
  magic,          ; text
  version,        ; int
  parents,        ; [TxId...]
  features,       ; [Feature...]
  author_pubkey,  ; bytes(32)
  authored_at,    ; null | uint (micros since unix epoch)
  tx_body         ; [Op...]
]
```

#### Field requirements

* `magic` MUST equal `"edb.tx"`
* `version` MUST equal `1`
* `parents` MUST be a list of TxIds (each 32 bytes)
* `features` MUST be a list of UTF-8 strings
* `author_pubkey` MUST be 32 bytes (Ed25519 pubkey)
* `authored_at` MAY be null, or an unsigned integer (micros since Unix epoch)
* `tx_body` MUST be a list of canonical operations (see §6)

### 5.3 Canonicalization rules (MUST)

To ensure “same logical tx → same bytes”:

**Parents**

* MUST be treated as a **set**
* MUST be deduplicated
* MUST be sorted lexicographically by raw TxId bytes (ascending)

**Features**

* MUST be treated as a **set**
* MUST be deduplicated
* MUST be sorted lexicographically by UTF-8 bytes (ascending)

**Authored timestamp**

* If present, MUST be an unsigned integer micros since Unix epoch
* MUST NOT be used for ordering (ordering is by DAG/toposort + local `t`)

---

## 6. Transaction body canonical form

### 6.1 Guiding rule: signing must not depend on input ordering

For the signed envelope to be stable across clients, the tx body must be canonical even if the user supplied it in a different order.

So:

* The tx body MUST be canonicalized (normalized) before signing.
* Ops MUST be ordered deterministically.

### 6.2 Supported ops (MVP)

Each op is a CBOR array:

#### Add

```
["add",  E, A, V]
```

#### Retract (specific value)

```
["retract", E, A, V]
```

#### Retract (all values for attribute)

```
["retract-all", E, A]
```

#### Compare-And-Set (CAS)

```
["cas", E, A, expected_or_null, newV]
```

#### Retract entity

```
["retract-entity", E]
```

> Note: You can replace these string opcodes with small integers later for size/perf; if you do, it’s an envelope-versioned change (or you add a new envelope version). For now strings are fine and clearer.

### 6.3 Entity reference type `E`

`E` MUST be one of:

1. **Entid** (integer ≥ 0)
2. **Tempid** (byte string of fixed length; RECOMMENDED 16 bytes UUID)
3. **Lookup ref** (CBOR array):

   ```
   ["lookup", unique_attr_ident, unique_value]
   ```

This exactly supports:

* existing entity id
* new entity creation via tempids
* stable references via unique identity attributes

### 6.4 Attribute reference type `A`

For envelope v1, **A MUST be the attribute ident string**, e.g. `":doc/title"`.

Rationale:

* Stable across replicas
* Avoids “attribute entid stability” questions in early P2P
* Fits your “schema-as-data” philosophy

### 6.5 Value type `V`

Values MUST be represented in a deterministic, schema-consistent way.

**Rule:** A value in a tx MUST be encoded in the canonical representation implied by the attribute’s `:db/valueType`.

Practical encoding rule for v1:

* For value types with clean CBOR representations:

  * long/uint8 → CBOR integer
  * boolean → CBOR bool
  * string → CBOR text (must be NFC-normalized)
  * bytes → CBOR byte string (raw)
  * uuid → CBOR text (canonical string) OR bytes(16) (pick one and stick to it)
  * instant → CBOR integer epoch micros OR text ISO-8601 (pick one canonical form)
  * tuple → CBOR byte string containing the tuple canonical bytes (recommended), OR CBOR array of slot values if you can guarantee canonicalization

If you want the *cleanest, least ambiguous* approach across runtimes:

> **RECOMMENDED:** represent `V` as **byte strings of EDB canonical value bytes** (from your value codec), and keep tx ops as “typed bytes”.
> That makes the envelope independent of float quirks and locks semantics tightly.

### 6.6 Canonical ordering of ops

Tx semantics should not depend on the user’s provided order (otherwise you can’t canonicalize safely).

Therefore, after normalization, the tx body MUST be sorted by a stable ordering rule.

**Canonical op order rule (v1):**

* Sort ops by the lexicographic ordering of their canonical CBOR encoding bytes (ascending).

This implies:

* identical logical ops always land in the same order
* the tx body is stable regardless of input ordering

### 6.7 Disallow order-dependent ambiguity (MVP MUST)

To make sorting safe, normalization MUST reject (or deterministically collapse) cases where ordering would change meaning.

Minimum rules that keep you sane early:

* For **cardinality-one** attributes:

  * tx MUST NOT contain multiple conflicting adds for the same `(E,A)`
  * (either reject, or collapse deterministically; rejecting is simplest)
* For **CAS**:

  * tx MUST NOT contain more than one CAS op for the same `(E,A)`
  * CAS checks MUST be evaluated against **db-before** (not sequentially within-tx)
* Tempid resolution MUST be deterministic given the tx and basis.

---

## 7. Computing TxId and signature

Given:

* `unsigned_envelope_bytes = CanonicalCBOR(UnsignedEnvelopeV1)`

Compute:

* `tx_id = SHA256(unsigned_envelope_bytes)`
* `sig  = Ed25519.Sign(author_private_key, unsigned_envelope_bytes)`

Verification:

* Parse unsigned envelope bytes → fields
* Verify:

  * `magic == "edb.tx"`
  * `version == 1`
  * `Ed25519.Verify(author_pubkey, unsigned_envelope_bytes, sig) == true`
  * `tx_id == SHA256(unsigned_envelope_bytes)` (if tx_id provided separately)

---

## 8. DAG semantics (parents, heads, ordering)

### 8.1 Parents

* `parents` defines the tx’s causal dependencies.
* A tx MAY have:

  * 0 parents (genesis)
  * 1 parent (the common/fast path; linear chain)
  * multiple parents (branch merge / “I built on multiple heads”)

### 8.2 Heads tracking

A node maintains a set `HEADS`.

When a new tx arrives:

1. Store it if validly signed (signature check is the admission gate)
2. Update heads:

   * remove each parent from `HEADS` (if present)
   * add `tx_id` to `HEADS`

### 8.3 Applying txs deterministically from the DAG

For any set of txs, to compute a deterministic apply order:

1. Perform a **topological sort** of the DAG
2. When multiple txs are simultaneously “ready” (all parents applied), break ties by:

   * ascending `tx_id` lexicographic order

This gives a deterministic total order from a shared DAG.

### 8.4 Local `t` assignment

* `t` is **local and derived**: increment as you apply txs in your chosen total order.
* `t` is NOT signed and NOT part of the TxId.

This aligns with:

* “causality in the DAG”
* “total order for queryability locally”

---

## 9. Authorship model

### 9.1 What authorship means

* The **author_pubkey** identifies the device/agent that asserted the tx.
* The signature proves:

  * the tx body and parents were authored (or at least signed) by that key
  * the tx has not been tampered with

### 9.2 What authorship does NOT mean

Authorship is **not** authorization.

* The system MAY still reject a tx due to schema/uniqueness/validation rules.
* Trust policy is separate (see below).

### 9.3 Trust policy (implementation-required)

A replica MUST have a policy for unknown keys:

Choose one early:

**Policy A (strict):** reject txs from unknown author keys (do not store).
**Policy B (store-but-don’t-apply):** store envelopes, but do not apply until key is trusted.
**Policy C (optimistic):** accept and apply any signed tx; trust is enforced at the application layer.

For “implement ASAP” with fewer moving parts:

* Start with **Policy A or C**.
* Policy B is better long-term but adds state (“pending txs”).

### 9.4 Exposing authorship to clients/apps

Even if you do not store author as a datom, the engine MUST expose author info in:

* tx reports
* log inspection endpoints

So applications like MyCloud can do per-block attribution by:

* `(block datoms) → tx → author_pubkey`

This matches your decision to keep non-`:db/txInstant` metadata out of EDB’s core system schema in MVP.

---

## 10. Feature negotiation and unknown features

### 10.1 Features field purpose

Features are how you prevent silent divergence when new value types/encodings appear.

Examples:

* `"uint8"`
* `"tuple"`
* `"tuple-enc:v1"`
* `"value-enc:v1"`
* `"envelope:v1"` (optional; redundant with version)

### 10.2 Receiver behavior (MUST)

If a receiver cannot interpret some feature required by the tx:

* It MUST NOT apply the tx.
* It MAY store it (for later upgrade).
* It SHOULD communicate the missing features to the sender during sync.

---

## 11. Where this fits in the build order

This is the “implement ASAP” mapping:

### Step 1: Core Value Encoding + Datom Types

Needed because the envelope’s tx body must be deterministic across runtimes, especially for tuples and float canonicalization.

### Step 2: Transaction Model + Validation Engine

Needed because:

* map form → primitive ops
* tempids / lookup refs normalization
* deterministic validation rules that do not depend on op ordering

### Step 3: Append‑Only Log + t Assignment

This is where the envelope becomes real:

* the log record is: `(tx_id, unsigned_bytes, sig)`
* append stores the envelope
* apply assigns local `t`
* tx report emits `tx_id`, `t`, and `author_pubkey`

### Step 12: P2P Sync MVP

This is where parents/heads/features become network-critical:

* advertise heads
* fetch missing txs by TxId
* verify sig
* apply in topo order + constraints

---

# 12. Implement-it-now checklist

Here’s the shortest path to a working system that won’t paint you into a corner.

## 12.1 Minimal data types

Implement:

* `TxId = [u8; 32]` (SHA-256)
* `AuthorPubKey = [u8; 32]`
* `Signature = [u8; 64]`
* `TempId = [u8; 16]` (UUID bytes recommended)
* `UnsignedEnvelopeBytes = Vec<u8>` (canonical CBOR)

## 12.2 Functions

1. `normalize_tx(input_tx, schema, db_before) -> canonical_ops`
2. `build_unsigned_envelope(author_pk, parents, features, authored_at, canonical_ops) -> bytes`
3. `tx_id(bytes) -> TxId`
4. `sign(bytes, author_sk) -> Signature`
5. `verify(bytes, sig, author_pk) -> bool`
6. `store_envelope(tx_id, bytes, sig, parents, author_pk, features)`
7. `apply_envelope(tx_id) -> (t, tx_report)`

## 12.3 Storage (SQLite-first, minimal tables)

* `tx_envelopes`

  * `tx_id BLOB PRIMARY KEY`
  * `unsigned_bytes BLOB NOT NULL`
  * `sig BLOB NOT NULL`
  * `author_pk BLOB NOT NULL`
  * `applied INTEGER NOT NULL DEFAULT 0`

* `tx_edges` (optional but useful)

  * `child_tx_id BLOB`
  * `parent_tx_id BLOB`

* `heads`

  * `tx_id BLOB PRIMARY KEY`

You can maintain `heads` transactionally on insert.

## 12.4 API surface (minimum)

* `GET /heads`

* `POST /transact`

  * client sends `unsigned_bytes` + `sig`
  * server verifies + stores + applies (or stores pending)
  * returns `{tx_id, t, author_pk, ...}`

* `GET /tx/{tx_id}`

  * returns `unsigned_bytes` + `sig`

This already gives you:

* signed log
* verifiable authorship
* chain mode now, DAG later

---

# 13. Recommended “v1 now, v2 later” simplifications

To implement quickly without losing the architecture:

### Start in “linear mode”

* Require `parents == current_heads` (typically exactly 1 head)
* If parent mismatch → return “stale basis” error
* This gives you single-writer correctness immediately

Then later:

* Allow branches
* Allow multi-head
* Apply topo ordering + tie-breaker

### Keep authored_at optional and informational

* Store it for UI only
* Keep `:db/txInstant` as writer-assigned

---

If you want, next I can also provide:

1. A **concrete example** envelope (fields + CBOR diagnostic notation + computed tx_id)
2. A **test vector plan** (cross-language signing/hashing conformance)
3. A **minimal “heads exchange” sync protocol** that sits cleanly on top of this envelope format
