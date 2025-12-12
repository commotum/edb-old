# MyCloud — Hero Application for EDB (Extensible Database)

**MyCloud** is the flagship, document‑centric application that showcases the capabilities of **EDB (Extensible Database)**: a time‑traveling, append‑only store with Datalog + Pull, schema‑as‑data, and first‑class peer‑to‑peer sync. MyCloud focuses on user experience and collaboration while relying on EDB for core data mechanics, storage, querying, and sync.

---

## Why MyCloud exists

MyCloud demonstrates how everyday users can create, organize, and share living documents while retaining the guarantees and power of a serious database: immutable history, reproducible queries, offline‑first operation, and evolvable schemas. It is the **hero app** for EDB—an opinionated product layered cleanly over EDB’s engine.

---

## Core concepts

### HyperDocs, Blocks, Tags, Questions & Answers
- **HyperDoc**: a top‑level document entity.
- **Block**: an owned sub‑entity within a HyperDoc (headings, sections, lists, media, etc.).
- **Tag**: a domain attribute used for classification or structure (e.g., `:doc/title`, `:doc/tags`).
- **Question/Answer**: the logical notion of an attribute (**Question**) and its value(s) (**Answer**).

**EDB mapping**
- Tags and Questions are **attributes** defined by schema: `:db/valueType`, `:db/cardinality`, `:db/unique`, `:db/doc`, `:db/alias`, `:db/noHistory`.  
- Blocks are modeled via **component relationships** (`:db/isComponent true`) so ownership and lifecycle are explicit.  
- HyperDocs are ordinary entities that aggregate Blocks and Tags.  
This embraces EDB’s **universal schema**: any entity may have any attribute, and schema evolves *growth‑only*.

---

## Data model (inheriting EDB)

MyCloud adopts EDB’s information model:

- **Datom**: ⟨E, A, V, Tx, Op⟩ where Op ∈ {add, retract}. Entities are point‑in‑time associative views over datoms.  
- **Transactions**: sets of changes applied atomically; each transaction has a **tx entity** and a **signed envelope** (author key, tx‑instant, parents, body).  
- **Time travel**: `as‑of`, `since`, and `history` views produce reproducible results over immutable database values.  
- **Sync**: replication is a **signed DAG** of transactions (advertise heads, push/pull, verify, apply).  
These mechanics are provided by EDB and surfaced in MyCloud’s UX (history, attribution, conflict surfacing).

---

## Schema & evolution philosophy

MyCloud leans on EDB’s **schema‑as‑data** to describe the characteristics of attributes (types, cardinality, uniqueness, ownership, history), and follows a strict **growth‑only** approach: add, alias, and deprecate—never remove or repurpose names. When an attribute requires a domain‑specific type, **we add a new value type** rather than forcing data into an ill‑fitting existing type. (Example: colors use `uint8` channels instead of overloading `long`.)

See also
- Value types (scalars): `docs/research/value-types.md`
- Requirements — Schema & Catalog: `docs/research/EDB-REQUIREMENTS.md`

---

## Value types & composite values (tuples)

EDB supports a canonical set of scalar value types and **tuple/composite values** for small, fixed‑arity records. MyCloud uses these to represent structured “values” (e.g., colors, geo points, ranges) while keeping **cardinality** at the attribute level.  
- **New scalar example:** `:db.type/uint8` (0..255), a single byte with numeric sort.  
- **Composite example:** `:db.type/tuple` with per‑slot types and optional labels for ergonomic Pull rendering.

### Example: Color as RGBA (tuple of `uint8`)
**Schema (EDN)**
```clojure
{:db/ident       :style/color
 :db/valueType   :db.type/tuple
 :db/cardinality :db.cardinality/one
 :db/tupleTypes  [:db.type/uint8 :db.type/uint8 :db.type/uint8 :db.type/uint8]
 :db/tupleLabels [:r :g :b :a]
 :db/default     [0 0 0 255]
 :db/doc         "RGBA; each channel 0..255 (00–FF); a defaults to 255"}
```

**Transact (JSON)**

```json
{"tx":[{"db/id":"temp-1","doc/title":"Welcome","style/color":[120,40,255,128]}]}
```

**Pull**

```clojure
[:doc/title {:style/color [:r :g :b :a]}]
;; → {:doc/title "Welcome"
;;    :style/color {:r 120 :g 40 :b 255 :a 128}}
```

Tuples are encoded canonically for ordering and hashing; labeled accessors like `(tuple/get ?c :r ?r)` are lowered to positional access at query time.

See also
- Tuple encoding spec: `docs/research/tuple-encoding.md`
- Value types (scalars): `docs/research/value-types.md`

---

## Query & composition

MyCloud builds features on **Datalog + Pull**:

* Use Datalog for relations, filters, and aggregates; use Pull to materialize structured entity trees (documents with nested blocks/tags).
* Pull supports forward/reverse navigation, nesting, wildcards, recursion limits, `:as/:default/:limit/:xform`.
* MyCloud composes views and exports using Pull patterns so rendering is decoupled from storage layout.

See also
- Requirements — Query & Pull: `docs/research/EDB-REQUIREMENTS.md`

---

## Identity & uniqueness

Documents and other public objects use **unique identity attributes** (e.g., `:doc/id`) to enable idempotent upserts and stable references. EDB enforces uniqueness on `(a, v)` with appropriate indexes; conflicts are detected deterministically at transact/merge time.

---

## Storage & indexing

MyCloud runs wherever EDB runs:

* **Local**: embedded on‑device with SQLite for offline‑first authoring.
* **Hosted**: Postgres for team workspaces and shared datasets.

EDB maintains EAVT/AVET/AEVT/VAET covering indexes with compact, typed encodings. Tuples encode to sortable bytes; optional generated columns can accelerate slot‑wise filters. MyCloud benefits from these index strategies without custom storage code.

See also
- Indexing strategy: `docs/research/indexing-strategy.md`

---

## Sync, offline, and collaboration

MyCloud’s collaboration model is EDB’s P2P sync:

* **Signed DAG** of transactions (author key, timestamp, parents, body), heads advertisement, push/pull by differences, signature verification.
* **Conflict handling** via unique constraints and clear surfacing in the UI; **snapshots** accelerate catch‑up.
* **Feature negotiation** allows new types/encodings (e.g., `tuple`, `tuple-enc:v1`, `uint8`) to roll out safely.

See also
- P2P sync MVP: `docs/research/p2p-sync-mvp.md`

---

## Security & integrity

EDB provides TLS in transit, optional at‑rest encryption (backend‑specific), **signed transaction envelopes**, deterministic validation (types, cardinality, uniqueness), and a sandbox for deterministic tx functions (WASM). MyCloud inherits these guarantees and exposes them through audit/history views and sharing workflows.

---

## Observability & operations

EDB exposes metrics (index merge latency, query latencies, sync status) and tx‑report subscriptions. MyCloud builds on these for activity feeds, diagnostics, and backup/restore flows.

---

## MVP features (MyCloud)

1. Create/edit **HyperDocs** with **Blocks** and **Tags** (component modeling).
2. Schema authoring via UI: define attributes (type, cardinality, uniqueness, docs).
3. Offline‑first editing; background sync; conflict surfacing.
4. History/time‑travel views (`as‑of`, `since`, `history`) and replay.
5. Search, filters, and views built on Datalog + Pull.
6. Export/import using Pull patterns (JSON/EDN).

---

## Design tenets

* **Layering**: MyCloud is a product; EDB is the engine. No shadow engines.
* **Growth‑only**: evolve via new attributes, aliases, and **new value types/structs** instead of twisting old ones.
* **Ubiquity**: rely on EDB’s SQLite/Postgres portability and HTTP/gRPC transports.
* **Determinism**: signed envelopes, canonical encodings, and reproducible queries underpin collaboration at scale.

---

## See also
- Requirements overview: `docs/research/EDB-REQUIREMENTS.md`
- Value types (scalars): `docs/research/value-types.md`
- Tuple encoding: `docs/research/tuple-encoding.md`
- Indexing strategy: `docs/research/indexing-strategy.md`
- P2P sync MVP: `docs/research/p2p-sync-mvp.md`
