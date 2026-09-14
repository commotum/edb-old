(do
  (clojure.core/in-ns 'datomic.kv-cassandra2)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.kv-cassandra2)
    {:doc
     "Cassandra KVStore adapter for chunked cass2 values. Uses conditional CQL updates for references, splits large immutable values, and shares driver sessions by endpoint configuration."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require
        ['datomic.callback :as 'cb]
        ['datomic.cassandra :as 'cass]
        ['datomic.cassandra-values :as 'cassv]
        ['datomic.kv-store :as 'kv]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'com.datastax.driver.core.Cluster)
      (clojure.core/import 'com.datastax.driver.core.Session)))
  (when-not (.equals 'datomic.kv-cassandra2 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-cassandra2))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require
          ['datomic.callback :as 'cb]
          ['datomic.cassandra :as 'cass]
          ['datomic.cassandra-values :as 'cassv]
          ['datomic.kv-store :as 'kv]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'com.datastax.driver.core.Cluster)
        (clojure.core/import 'com.datastax.driver.core.Session))))
  (set! *warn-on-reflection* true)
  (.setMeta (clojure.lang.RT/var "datomic.kv-cassandra2" "cluster-sessions") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.kv-cassandra2" "cluster-sessions") (atom {}))
  (def cql-keys [:id2 :rev :map :val :chunks])
  (reset-meta! #'cql-keys (assoc {:column (int 1)} :name 'cql-keys :ns *ns*))
  (deftype
    KVCassandra2
    [cluster session table]
    datomic.kv_store.KVStore
    (close [this] nil)
    (delete
      [this key consistent?]
      (do
        (if consistent?
          (cass/cql-delete session table key cql-keys)
          (cassv/delete-value session table key))
        :ok))
    (get
      [this key consistent?]
      (if consistent?
        (let [temp__5804__auto__ (cass/cql-select session table key cql-keys consistent?)]
          (when temp__5804__auto__
            (let [ret temp__5804__auto__
                  ret (let [temp__5802__auto__ (:val ret)]
                        (if temp__5802__auto__ (let [v temp__5802__auto__] (assoc ret :v v)) ret))
                  m (and (:map ret) (read-string (:map ret)))
                  ret (merge (dissoc ret :map :val) m)]
              ret)))
        (binding [cassv/*retry* kv/*retry*] (cassv/get-value session table key))))
    (put
      [this p__23272]
      (let [map__23274 p__23272
            map__23274 (if (seq? map__23274)
                         (if (next map__23274)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__23274))
                           (if (seq map__23274) (first map__23274) {}))
                         map__23274)
            v_map map__23274
            id (clojure.core/get map__23274 :id)
            rev (clojure.core/get map__23274 :rev)
            v (clojure.core/get map__23274 :v)
            ensure (clojure.core/get map__23274 :ensure)]
        (when (let [m (dissoc v_map :id :rev :v :ensure)
                    val_map {:id2 id,
                             :rev rev,
                             :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                             :val v}]
                (if ensure
                  (if (= ensure {:id nil})
                    (cass/cql-insert session table cql-keys val_map true)
                    (cass/cql-update session table (:rev ensure) cql-keys val_map))
                  (binding [cassv/*retry* kv/*retry*] (cassv/put-value session table v_map))))
          :ok))))
  (clojure.core/import 'datomic.kv_cassandra2.KVCassandra2)
  (defn ->KVCassandra2
    ([cluster session table] (datomic.kv_cassandra2.KVCassandra2. cluster session table)))
  (reset-meta!
    #'->KVCassandra2
    (assoc
      {:arglists (clojure.core/list ['cluster 'session 'table]), :column (int 1)}
      :name
      '->KVCassandra2
      :ns
      *ns*))
  (defn kv-cassandra
    ([endpoint]
      (let [map__23284 endpoint
            map__23284 (if (seq? map__23284)
                         (if (next map__23284)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__23284))
                           (if (seq map__23284) (first map__23284) {}))
                         map__23284)
            host (clojure.core/get map__23284 :host)
            provided_cluster (clojure.core/get map__23284 :cluster)
            table (clojure.core/get map__23284 :table)
            cluster_callback (clojure.core/get map__23284 :cluster-callback)
            port (or (:port endpoint) 9042)
            user (or (:user endpoint) "")
            password (or (:password endpoint) "")
            ssl (or (:ssl endpoint) false)
            map__23285 (locking cluster-sessions
                        (or
                          (clojure.core/get (deref cluster-sessions) endpoint)
                          (let [c (or
                                    provided_cluster
                                    (cass/cluster-from-callback endpoint)
                                    (cond->
                                      (Cluster/builder)
                                      true
                                      (.addContactPoint ^java.lang.String host)
                                      true
                                      (.withPort (int ^java.lang.Number port))
                                      true
                                      (.withCredentials
                                        ^java.lang.String user
                                        ^java.lang.String password)
                                      ssl
                                      (.withSSL)
                                      true
                                      (.build)))
                                s (.connect ^com.datastax.driver.core.Cluster c)
                                m {:cluster c, :session s}]
                            (swap! cluster-sessions assoc endpoint m)
                            m)))
            map__23285 (if (seq? map__23285)
                         (if (next map__23285)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__23285))
                           (if (seq map__23285) (first map__23285) {}))
                         map__23285)
            cluster (clojure.core/get map__23285 :cluster)
            session (clojure.core/get map__23285 :session)]
        (datomic.kv_cassandra2.KVCassandra2. cluster session table))))
  (reset-meta!
    #'kv-cassandra
    (assoc
      {:arglists (clojure.core/list ['endpoint]), :column (int 1)}
      :name
      'kv-cassandra
      :ns
      *ns*)))
