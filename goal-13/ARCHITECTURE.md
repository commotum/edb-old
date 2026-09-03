# Goal 13 architecture contract

This contract is deliberately narrower than a generic database index design.
It maps the useful Datomic Pro 1.0.7705 peer/index shape to one concrete Rust
and PostgreSQL implementation, while keeping the eager `Database` as a pure
semantic oracle.

## Source-backed invariants

- A database value is immutable and persistent. A transaction is an atomic
  functional successor, and captured old values do not change
  (`datomic_pro_docs/00_start_here/00_introduction.md:69-78`).
- EAVT, AEVT, AVET, and VAET are ordered sets. E/A/V sort ascending; T sorts
  descending. Pro AVET contains indexed/unique attributes and VAET contains
  reference attributes (`datomic_pro_docs/06_indexes/01_index_model.md:8-75`).
- Durable indexes are immutable, shallow, wide trees of independently cached
  segments. A seek normally needs one directory and one leaf read, databases
  may exceed memory, and immutable values may be cached without invalidation
  (`00_introduction.md:79-100`, `01_index_model.md:79-105`).
- Recent transactions form a distinct ordered memory-index tier. It is rebuilt
  from the authoritative log, merged with durable tiers at read time, and may
  be discarded only after adopting a durable root that covers it. If indexing
  cannot keep up, writes slow rather than silently evicting uncovered data
  (`02_background_indexing.md:13-25`). Recovered `datomic.btset` is a
  persistent 16-way path-copy search tree, and `Db.acceptDataCheck` inserts raw
  events into four such trees rather than rebuilding sorted vectors
  (`1.0.7705/peer/src-clj/datomic/btset.clj:157-416`,
  `db.clj:4748-4806`).
- The recovered leaf representation is column-oriented `TransposedData`; a
  `RootNode` carries sparse directory keys and immutable directory ids; a
  `DirNode` carries sparse segment keys, ids, offsets, and counts. Directory
  and segment values load on demand and are cached separately from the value
  itself (`1.0.7705/peer/src-clj/datomic/index.clj:144-319`).
- Those parent keys are not naively copied full first datoms. Recovered
  `make-sparse-lt`, `strdiff`/`vecdiff`/`mindiff`, `sparse-datom`, and
  `sparse-e-xf` deliberately synthesize the shortest useful separator at
  E/A/V boundaries before serializing directories and roots
  (`index.clj:2725-3158`). This is a material space/performance choice: full
  copies of legal large values in every parent reference create a false tree
  capacity limit.
- Recovered seeks route root -> directory -> segment, while recovered merge
  code rewrites affected ranges and reuses aligned old nodes
  (`index.clj:1254-1617`, `2949-3987`). `Db` merges memory, indexing,
  mid-index, durable current, and durable history tiers
  (`db.clj:4723-4805`, `5035-5074`).
- Index publication is content-first and root-last. The recovered root carries
  an independent incrementing `:rev`, and visibility conditionally advances
  the physical root reference at its expected revision
  (`index.clj:6250-6465`). Adoption replays any log tail that arrived after the
  candidate build, then atomically swaps the whole database value and retries
  on a race (`adopter.clj:13-26`). Startup likewise loads a durable root and
  catches it up from the log; notifications are never authority
  (`db_io.clj:97-100`, `141-155`).
- Transaction reports are connection observation state, not database-value
  history. Recovered `txReportQueue` creates that state only on request and
  `removeTxReportQueue` removes it (`peer.clj:650-715,727-741`).
- Superseded immutable nodes are retired only after successful root
  publication. Datomic's operational contract is age-delayed GC so lagging or
  long-lived consumers retain their roots; explicit native snapshot pins are
  a stronger Rust/PostgreSQL safety aid, not a recovered Datomic requirement
  (`capacity_planning.md:275-286`).

## Native representation

The production tree has exactly three stored levels, matching the recovered
shape rather than introducing a general recursive B-tree abstraction:

1. `TreeRoot`: resident with a snapshot; identifies one order/current-history
   pair and contains sparse lower bounds plus immutable directory hashes.
2. `TreeDirectory`: loaded by hash; contains sparse lower bounds, immutable
   leaf hashes, and logical counts.
3. `LeafSegment`: loaded by hash; stores datoms in E/A/V/T/op columns and
   reconstructs owned `Datom` values at the API boundary.

Nodes use an independent, explicitly versioned canonical envelope and a
domain-separated SHA-256 content identity. Child references carry a sparse
separator key, immutable child hash, and logical count; the next separator (or
tree end) is the implicit exclusive upper route, as in the recovered shape.
Separators must satisfy `previous child last < separator <= child first` and
use source-shaped string/tuple minimum differences rather than duplicating
large full values. Decoding checks kind, order, history flag, strict routing,
non-overlap, child identity/count, and separator containment before content
becomes observable. Empty indexes have a canonical empty root and no synthetic
empty leaves. A component-presence mask distinguishes an omitted lower-bound
component from a real numeric zero; this is essential for native system entity
`0` and for suppressing descending-T comparison after a sparse E/A/V prefix,
the Rust representation of recovered `make-sparse-lt`. Encoded byte counts are
measured for storage/cache accounting,
not misrepresented as a field present in every recovered child reference.

Limits are byte limits first and datom/fanout limits second. A single encoded
datom larger than the ordinary leaf target is allowed as one bounded
oversize leaf up to the canonical value limit; it cannot make the resident
cache unbounded. Root and directory fanout are fixed configuration values with
safe defaults near Datomic's wide-tree intent, not semantic API knobs.

PostgreSQL has one concrete immutable node table and append-only physical
publication revisions scoped to a database. Several revisions may describe
the same logical transaction basis; the highest usable authenticated revision
is the physical root reference. The canonical manifest authenticates its
positive revision, all eight roots (four orders x current/history), the
authoritative transaction hash, allocation frontier, and packing-independent
semantic commitment. It does not duplicate schema or ident vectors: those are
derived from ordinary authenticated tree datoms. Nodes are inserted and
verified first; the manifest and conditional next-revision publication are
committed last. Version-1 flat manifests remain readable only as a
migration/rebuild input and are not the production peer representation after
this goal.

## Recent tier and consolidation

A recent tier is an immutable value containing a structurally shared chunked
log of contiguous authenticated transactions after `base_t`, four 16-way
persistent raw-event trees (EAVT, AEVT, AVET, VAET), measured byte/datom
counts, and its endpoint hash. Schema and ident projections are derived views,
not additional durable/recent authorities. Reads capture one snapshot and
lazily merge the relevant durable and recent cursors. Current reads collapse
add/retract pairs; the authoritative log and recent tier retain every event.
`:db/noHistory` is not a retroactive semantic
erase: an indexing job may omit eligible retract/assert pairs only when the
attribute is `noHistory` in that job's endpoint database. Recovered
`filter-nohist-pairs` runs over each affected segment after merging old and new
data; it has no base-transaction cutoff. A pair may therefore straddle the
durable/recent boundary, and an older adjacent pair may be forgotten when its
segment is rebuilt, but no global retroactive sweep occurs merely because the
flag changed. Already-published omissions cannot be resurrected, so physical
retained history depends on indexing schedule. Tail data is never evicted.
Crossing the soft bound requests consolidation; crossing the hard
bound prevents further peer advancement (and ultimately applies write
backpressure) until a covering root can be adopted.

Consolidation seeks the already ordered recent trees, finds affected key
ranges, reads only intersecting old directories/leaves, streams their merge,
splits by canonical byte/count bounds, and reuses every untouched child hash.
It writes immutable children before conditionally publishing the next root
revision. A losing/interrupted builder leaves unreachable content but no
visible database value. Broad work for excision and installing a new AVET
attribute is an explicit documented exception, not hidden behind a sublinear
claim.

## Snapshots, cache, and connection

`PeerSnapshot` owns one immutable `Arc<PeerState>`: database id, basis/hash,
durable revision and roots, recent tier, derived schema/ident view, excision
generation, and root pins. `PeerIndexCursor` opens without directory/leaf I/O,
streams one sought path at a time, and retains `Arc` node handles, so cache
eviction or connection advancement cannot invalidate it. Goal 14 owns making
this exact lazy access path the shared query/pull database-value seam; it is a
semantic index boundary, not a storage-backend abstraction.

All `Peer` clones share one `Arc<PeerCore>`, one byte-and-count-bounded
hash-keyed node cache, one serialized/coalesced updater, and one atomically
published `Arc<PeerState>`. Cache weight includes the encoded footprint and
decoded recursive allocations; cursors own cache values through eviction.
Basis, transaction hash, roots, recent tier, and generation never live in
separately published mutable fields. The optional transaction-report queue is
deliberately separate connection observation state: it is absent by default,
starts empty when enabled, and is filled only after complete successor-state
publication. Each public operation captures the state once. PostgreSQL
notifications may wake sync, but head plus contiguous authenticated log
catch-up decides state.

## State commitment

The transaction commitment cannot be a physical manifest hash: durable
packing is delayed, physical `:db/noHistory` filtering is job-dependent, and
equivalent trees may be packed differently. Version 2 therefore commits to
the canonical logical *current* information set with a deterministic
persistent Merkle search tree, plus basis and allocation frontier; the
separate transaction hash authenticates complete chronology. Successors update
only changed Merkle paths and recovery can recompute the same root. Legacy-v1
rows retain an explicit verification path. A published physical tree remains
bound to that authoritative commitment by the fenced builder and manifest;
accessed paths are content-authenticated, but derived metadata alone is not a
cryptographic proof that every unvisited leaf denotes the semantic root. Do
not advertise such a proof unless subtree witnesses are added. A mere row
count, tail counter, or XOR checksum remains unacceptable.

## Measurable acceptance witnesses

- Build receipts report tail datoms/bytes, affected ranges, node reads/writes,
  encoded bytes, reused child hashes, and maximum depth. A localized update to
  a large tree rewrites bounded paths and demonstrably reuses untouched nodes.
- Recovery reports manifest/root/directory/leaf rows and bytes. Cold open
  reads the resident roots and only the authenticated paths required to derive
  schema/ident metadata; it does not read unrelated application leaves. A
  point seek reads at most one directory and one leaf per durable tier/order;
  a repeated seek hits cache.
- Cache gauges expose current/peak entries and bytes plus hits, misses,
  evictions, and coalesced loads. Resident bytes never exceed the configured
  ceiling, including unusually large values.
- Recent-tier gauges expose current/peak datoms and bytes and base/tail basis.
  Tests prove adoption before discard and explicit backpressure at the hard
  bound.
- A fault after one valid tail row leaves the complete published peer state
  unchanged. Many cloned peers see monotonic all-old or all-new states while
  retained snapshots remain exact through consolidation and cache pressure.
- Long-history tests prove no allocation proportional to basis and lazy
  range/point reads. Real PostgreSQL gates fail if they did not actually run.

## Remaining compatibility paths and owners

- `Database` and `IndexRoots::build` duplicate, clone, sort, and revalidate
  whole current/history values on transitions. They remain the Stage 2/3 pure
  oracle; production access moves to `PeerSnapshot` in Stages 4-5.
- `PostgresIndexer::consolidate` uses shallow native nodes and affected-range
  copy-on-write. Only the first native root is an intentional full build; flat
  indexes remain a read-only migration fallback.
- `load_manifest_database` still fetches every flat-format segment when a
  caller explicitly requests an eager compatibility `Database`. Native peer
  open/adoption and `PeerSnapshot` seeks do not use it.
- Legacy `SegmentCache` remains only on that compatibility route. Native child
  nodes use the shared byte-and-count-bounded immutable cache.
- `Peer` clones share one state cell; tail and root adoption construct a full
  successor and publish it only after every fallible check. Transaction report
  registration remains separate by design, as in the recovered connection.
- The eager pure oracle still owns complete collections. Goal 16, not this peer
  goal, removes it from the production transactor after Goal 14 establishes
  the common lazy semantic access seam.

## Deliberate deviations

- PostgreSQL replaces Datomic's generic storage/cache protocols. There is no
  native backend trait.
- Rust `Arc` ownership replaces JVM weak references inside immutable values;
  eviction is explicit and byte bounded. Optional root leases/pins strengthen
  the source's age-only GC protection.
- Native canonical encoding is not Fressian compatible. Recovered structural
  boundaries and columnar layout are retained; JVM class names are not bytes
  on disk.
- The eager `Database` survives as an independent oracle, not as the
  production peer representation. This deliberately avoids contaminating the
  simple semantic model with I/O and cache state.
