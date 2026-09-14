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
  (defn index-db
    ([p__17059]
      (let [map__17060 p__17059
            map__17060 (if (seq? map__17060)
                         (clojure.lang.PersistentHashMap/create (seq map__17060))
                         map__17060)
            cluster (get map__17060 :cluster)
            olookup (get map__17060 :olookup)
            resolved_conf (get map__17060 :resolved-conf)
            db_id (:db-id resolved_conf)
            idxroot (:key (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))]
        (db/db db_id (index/load-index olookup idxroot)))))
  (defn log
    ([p__17062]
      (let [map__17063 p__17062
            map__17063 (if (seq? map__17063)
                         (clojure.lang.PersistentHashMap/create (seq map__17063))
                         map__17063)
            cluster (get map__17063 :cluster)
            olookup (get map__17063 :olookup)]
        (log/find-log cluster olookup))))
  (defn db-resources
    ([cr] (let [db (index-db cr) log (log cr) db (:db (log/catchup db log))] {:db db, :log log})))
  (defn index-includes-some-log-tail?
    ([p__17066]
      (let [map__17067 p__17066
            map__17067 (if (seq? map__17067)
                         (clojure.lang.PersistentHashMap/create (seq map__17067))
                         map__17067)
            db (get map__17067 :db)
            log (get map__17067 :log)]
        (> (:indexBasisT db) (:t (log/last-tree-tx log))))))
  (defn most-current-db
    ([& dbs]
      (let [basis (fn basis ([db] (if db (long (.basisT ^datomic.Database db)) -1)))]
        (reduce
          (fn fn__17071
            ([db1 db2]
              (if (< (^clojure.lang.IFn basis db1) (^clojure.lang.IFn basis db2)) db2 db1)))
          nil
          dbs))))
  (reset-meta!
    #'most-current-db
    (assoc
      {:private true, :arglists (clojure.core/list ['& 'dbs]), :column 1}
      :name
      'most-current-db
      :ns
      *ns*))
  (defn load-db-from-basis
    ([cluster_db_conn olookup db_id & p__17074]
      (let [vec__17075 p__17074
            basis_db (nth vec__17075 (int 0) nil)
            idxroot (index/find-index-root-id cluster_db_conn)
            log (log/find-log cluster_db_conn olookup)
            db (db/db db_id (index/load-index olookup idxroot))
            db (most-current-db basis_db db)]
        (:db (log/catchup db log true))))))