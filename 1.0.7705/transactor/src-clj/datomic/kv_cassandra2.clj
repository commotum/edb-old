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
        (let [temp__5825__auto__ (cass/cql-select session table key cql-keys consistent?)]
          (when temp__5825__auto__
            (let [ret temp__5825__auto__
                  ret (let [temp__5823__auto__ (:val ret)]
                        (if temp__5823__auto__ (let [v temp__5823__auto__] (assoc ret :v v)) ret))
                  m (and (:map ret) (read-string (:map ret)))
                  ret (merge (dissoc ret :map :val) m)]
              ret)))
        (binding [cassv/*retry* kv/*retry*] (cassv/get-value session table key))))
    (put
      [this p__27128]
      (let [map__27130 p__27128
            map__27130 (if (seq? map__27130)
                         (if (next map__27130)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27130))
                           (if (seq map__27130) (first map__27130) {}))
                         map__27130)
            v_map map__27130
            id (clojure.core/get map__27130 :id)
            rev (clojure.core/get map__27130 :rev)
            v (clojure.core/get map__27130 :v)
            ensure (clojure.core/get map__27130 :ensure)]
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
      (let [map__27140 endpoint
            map__27140 (if (seq? map__27140)
                         (if (next map__27140)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27140))
                           (if (seq map__27140) (first map__27140) {}))
                         map__27140)
            host (clojure.core/get map__27140 :host)
            provided_cluster (clojure.core/get map__27140 :cluster)
            table (clojure.core/get map__27140 :table)
            cluster_callback (clojure.core/get map__27140 :cluster-callback)
            port (or (:port endpoint) 9042)
            user (or (:user endpoint) "")
            password (or (:password endpoint) "")
            ssl (or (:ssl endpoint) false)
            map__27141 (locking cluster-sessions
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
            map__27141 (if (seq? map__27141)
                         (if (next map__27141)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27141))
                           (if (seq map__27141) (first map__27141) {}))
                         map__27141)
            cluster (clojure.core/get map__27141 :cluster)
            session (clojure.core/get map__27141 :session)]
        (datomic.kv_cassandra2.KVCassandra2. cluster session table))))
  (reset-meta!
    #'kv-cassandra
    (assoc
      {:arglists (clojure.core/list ['endpoint]), :column (int 1)}
      :name
      'kv-cassandra
      :ns
      *ns*)))
