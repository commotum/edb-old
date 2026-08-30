(do
  (clojure.core/in-ns 'datomic.db-io)
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
  (defn storage-resources
    ([uri]
      (let [cluster_conf (uri/parse uri)
            resolved_conf (coord/resolve-db-name cluster_conf)
            cluster (coord/create-db-cluster resolved_conf)
            olookup (domain/system-cache-olookup cluster)]
        {:cluster-conf cluster_conf,
         :resolved-conf resolved_conf,
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
  (def index-db
   (fn index_db
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
             resolved_conf (get map__18514 :resolved-conf)
             db_id (:db-id resolved_conf)
             idxroot (:key (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))]
         (db/db db_id (index/load-index olookup idxroot))))))
  (reset-meta!
    #'index-db
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup 'resolved-conf]}]), :column (int 1)}
      :name
      'index-db
      :ns
      *ns*))
  (def log
   (fn log
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
         (log/find-log cluster olookup)))))
  (reset-meta!
    #'log
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]}]), :column (int 1)}
      :name
      'log
      :ns
      *ns*))
  (defn db-resources
    ([cr] (let [db (index-db cr) log (log cr) db (:db (log/catchup db log))] {:db db, :log log})))
  (reset-meta!
    #'db-resources
    (assoc {:arglists (clojure.core/list ['cr]), :column (int 1)} :name 'db-resources :ns *ns*))
  (def index-includes-some-log-tail?
   (fn index_includes_some_log_tail_QMARK_
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
         (> (:indexBasisT db) (:t (log/last-tree-tx log)))))))
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
  (def load-db-from-basis
   (fn load_db_from_basis
     ([cluster_db_conn olookup db_id & p__18528]
       (let [vec__18529 p__18528
             basis_db (nth vec__18529 (int 0) nil)
             return_log? (nth vec__18529 (int 1) nil)
             idxroot (index/find-index-root-id cluster_db_conn)
             log (log/find-log cluster_db_conn olookup)
             db (db/db db_id (index/load-index olookup idxroot))
             db (most-current-db basis_db db)
             db (:db (log/catchup db log true))]
         (if return_log?
           {:db db, :log (log/->LogValue db olookup (log/get-root-id log) (:memlog db))}
           db)))))
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