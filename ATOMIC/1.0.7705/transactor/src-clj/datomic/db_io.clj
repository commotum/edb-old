(do
  (clojure.core/in-ns 'datomic.db-io)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.db-io)
    {:doc
     "Loads database roots, index segments, log segments, and recent transaction tails through the configured cache and storage stack, producing immutable database values at a consistent basis."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.cluster :as 'cluster]
        ['datomic.coordination :as 'coord]
        ['datomic.db :as 'db]
        ['datomic.domain :as 'domain]
        ['datomic.index :as 'index]
        ['datomic.log :as 'log]
        ['datomic.uri :as 'uri])
      (clojure.core/import 'datomic.Database)))
  (when-not (.equals 'datomic.db-io 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.db-io))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.cluster :as 'cluster]
          ['datomic.coordination :as 'coord]
          ['datomic.db :as 'db]
          ['datomic.domain :as 'domain]
          ['datomic.index :as 'index]
          ['datomic.log :as 'log]
          ['datomic.uri :as 'uri])
        (clojure.core/import 'datomic.Database))))
  ;; Resolves a database URI into its system configuration, database-scoped store, and cached lookup.
  (defn storage-resources
    ([uri]
      (let [cluster-conf (uri/parse uri)
            resolved-conf (coord/resolve-db-name cluster-conf)
            cluster (coord/create-db-cluster resolved-conf)
            olookup (domain/system-cache-olookup cluster)]
        {:cluster-conf cluster-conf,
         :resolved-conf resolved-conf,
         :cluster cluster,
         :olookup olookup})))
  (reset-meta!
    #'storage-resources
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'storage-resources
      :ns
      *ns*))
  ;; Loads the immutable index root named by the database's current index reference.
  (defn index-db
    ([p__18513]
      (let [map__18514 p__18513
            map__18514 (if (seq? map__18514)
                         (if (next map__18514)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18514))
                           (if (seq map__18514) (first map__18514) {}))
                         map__18514)
            cluster (get map__18514 :cluster)
            olookup (get map__18514 :olookup)
            resolved-conf (get map__18514 :resolved-conf)
            db-id (:db-id resolved-conf)
            idxroot (:key (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))]
        (db/db db-id (index/load-index olookup idxroot)))))
  (reset-meta!
    #'index-db
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup 'resolved-conf]}]), :column (int 1)}
      :name
      'index-db
      :ns
      *ns*))
  ;; Opens the database's durable log through the same immutable object lookup used by its index.
  (defn log
    ([p__18516]
      (let [map__18517 p__18516
            map__18517 (if (seq? map__18517)
                         (if (next map__18517)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18517))
                           (if (seq map__18517) (first map__18517) {}))
                         map__18517)
            cluster (get map__18517 :cluster)
            olookup (get map__18517 :olookup)]
        (log/find-log cluster olookup))))
  (reset-meta!
    #'log
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]}]), :column (int 1)}
      :name
      'log
      :ns
      *ns*))
  ;; Loads the durable index and log, then catches the index up through the log tail.
  (defn db-resources
    ([cr] (let [db (index-db cr) log (log cr) db (:db (log/catchup db log))] {:db db, :log log})))
  (reset-meta!
    #'db-resources
    (assoc {:arglists (clojure.core/list ['cr]), :column (int 1)} :name 'db-resources :ns *ns*))
  ;; Detects a restored or compacted index whose basis already covers transactions still in the tail.
  (defn index-includes-some-log-tail?
    ([p__18520]
      (let [map__18521 p__18520
            map__18521 (if (seq? map__18521)
                         (if (next map__18521)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18521))
                           (if (seq map__18521) (first map__18521) {}))
                         map__18521)
            db (get map__18521 :db)
            log (get map__18521 :log)]
        (> (:indexBasisT db) (:t (log/last-tree-tx log))))))
  (reset-meta!
    #'index-includes-some-log-tail?
    (assoc
      {:arglists (clojure.core/list [{:keys ['db 'log]}]), :column (int 1)}
      :name
      'index-includes-some-log-tail?
      :ns
      *ns*))
  (defn most-current-db
    ([& dbs]
      (let [basis (fn basis ([db] (if db (long (.basisT ^datomic.Database db)) -1)))]
        (reduce
          (fn fn__18525
            ([db1 db2]
              (if (< (^clojure.lang.IFn basis db1) (^clojure.lang.IFn basis db2)) db2 db1)))
          nil
          dbs))))
  (reset-meta!
    #'most-current-db
    (assoc
      {:private true, :arglists (clojure.core/list ['& 'dbs]), :column (int 1)}
      :name
      'most-current-db
      :ns
      *ns*))
  ;; Rebuilds the most current immutable database from an index root and the subsequent log tail.
  ;; An optional basis database avoids moving backward when the caller already holds newer state.
  (defn load-db-from-basis
    ([cluster-db-conn olookup db-id & p__18528]
      (let [vec__18529 p__18528
            basis-db (nth vec__18529 (int 0) nil)
            return-log? (nth vec__18529 (int 1) nil)
            idxroot (index/find-index-root-id cluster-db-conn)
            log (log/find-log cluster-db-conn olookup)
            db (db/db db-id (index/load-index olookup idxroot))
            db (most-current-db basis-db db)
            db (:db (log/catchup db log true))]
        (if return-log?
          {:db db, :log (log/->LogValue db olookup (log/get-root-id log) (:memlog db))}
          db))))
  (reset-meta!
    #'load-db-from-basis
    (assoc
      {:arglists
       (clojure.core/list ['cluster-db-conn 'olookup 'db-id '& ['basis-db 'return-log?]]),
       :column (int 1)}
      :name
      'load-db-from-basis
      :ns
      *ns*)))
