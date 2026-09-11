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
    ([p__19560]
      (let [map__19561 p__19560
            map__19561 (if (seq? map__19561)
                         (if (next map__19561)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19561))
                           (if (seq map__19561) (first map__19561) {}))
                         map__19561)
            cluster (get map__19561 :cluster)
            olookup (get map__19561 :olookup)
            resolved-conf (get map__19561 :resolved-conf)
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
    ([p__19563]
      (let [map__19564 p__19563
            map__19564 (if (seq? map__19564)
                         (if (next map__19564)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19564))
                           (if (seq map__19564) (first map__19564) {}))
                         map__19564)
            cluster (get map__19564 :cluster)
            olookup (get map__19564 :olookup)]
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
    ([p__19567]
      (let [map__19568 p__19567
            map__19568 (if (seq? map__19568)
                         (if (next map__19568)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19568))
                           (if (seq map__19568) (first map__19568) {}))
                         map__19568)
            db (get map__19568 :db)
            log (get map__19568 :log)]
        (> (:indexBasisT db) (:t (log/last-tree-tx log))))))
  (reset-meta!
    #'index-includes-some-log-tail?
    (assoc
      {:arglists (clojure.core/list [{:keys ['db 'log]}]), :column (int 1)}
      :name
      'index-includes-some-log-tail?
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed]: Reconnection prefers the greater transaction basis;
  ;; ties retain the first argument, so this is not an index-revision chooser.
  ;; [inferred] Keeping a newer in-memory basis avoids replaying already accepted
  ;; transactions merely because the durable index is still behind.
  (defn most-current-db
    ([& dbs]
      (let [basis (fn basis ([db] (if db (long (.basisT ^datomic.Database db)) -1)))]
        (reduce
          (fn fn__19572
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
  ;; ATOMIC-NOTE [observed]: Index and log heads are captured separately, then
  ;; log/catchup seeks from the selected Db's nextT. This is not a full user-data
  ;; materialization and not an atomic multi-reference read. Current peer startup
  ;; calls this once before notifier creation and again before starting delivery.
  (defn load-db-from-basis
    ([cluster-db-conn olookup db-id & p__19575]
      (let [vec__19576 p__19575
            basis-db (nth vec__19576 (int 0) nil)
            return-log? (nth vec__19576 (int 1) nil)
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
