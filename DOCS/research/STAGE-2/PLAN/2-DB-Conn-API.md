2. Db/Conn API + caching layer (snapshot reads, attribute caches).

  Embedded Db/Conn API + snapshots/as‑of/since/history + attr cache

  - REFERENCE/datomic-reference/time_in_datomic.md — snapshot semantics, as‑of/since/history grounding.
  - REFERENCE/datomic-reference/operation/tutorial/4_read.md — how db values are used for reads in practice.
  - REFERENCE/datomic-reference/operation/tutorial/6_read_revisited.md — patterns around db values and repeated reads.
  - REFERENCE/datomic-reference/operation/tutorial/8_history.md — history db usage from the app perspective.
  - REFERENCE/datomic-reference/indexes/2_index_model.md — why immutable indexes make caching/snapshot reads fast.
  - REFERENCE/datomic-reference/best_practices.md — performance/caching guidance and read patterns.