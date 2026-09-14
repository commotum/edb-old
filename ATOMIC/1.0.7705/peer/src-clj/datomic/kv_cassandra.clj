(do
  (clojure.core/in-ns 'datomic.kv-cassandra)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.kv-cassandra)
    {:doc
     "Cassandra KVStore adapter for the original cass protocol. Uses conditional CQL updates for revisioned references and shares cluster sessions by endpoint configuration."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require
        ['datomic.cassandra :as 'cass]
        ['datomic.config :as 'config]
        ['datomic.kv-store :as 'kv]
        ['datomic.io :as 'io]
        ['datomic.require :as 'req]
        ['datomic.error :as 'error]
        ['datomic.slf4j :as 'logger]
        ['clojure.string :as 'string])
      (clojure.core/import 'com.datastax.driver.core.Cluster)))
  (when-not (.equals 'datomic.kv-cassandra 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-cassandra))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require
          ['datomic.cassandra :as 'cass]
          ['datomic.config :as 'config]
          ['datomic.kv-store :as 'kv]
          ['datomic.io :as 'io]
          ['datomic.require :as 'req]
          ['datomic.error :as 'error]
          ['datomic.slf4j :as 'logger]
          ['clojure.string :as 'string])
        (clojure.core/import 'com.datastax.driver.core.Cluster))))
  (set! *warn-on-reflection* true)
  (defn retry-policy ([] com.datastax.driver.core.policies.DefaultRetryPolicy/INSTANCE))
  (reset-meta!
    #'retry-policy
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'retry-policy :ns *ns*))
  (def cql-keys [:id :rev :map :val])
  (reset-meta! #'cql-keys (assoc {:column (int 1)} :name 'cql-keys :ns *ns*))
  (deftype
    KVCassandra
    [cluster session table]
    datomic.kv_store.KVStore
    (close [this] nil)
    (delete [this key consistent?] (do (cass/cql-delete session table key cql-keys) :ok))
    (get
      [this key consistent?]
      (let [temp__5804__auto__ (cass/cql-select session table key cql-keys consistent?)]
        (when temp__5804__auto__
          (let [ret temp__5804__auto__
                ret (let [temp__5802__auto__ (:val ret)]
                      (if temp__5802__auto__ (let [v temp__5802__auto__] (assoc ret :v v)) ret))
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val) m)]
            ret))))
    (put
      [this p__22642]
      (let [map__22644 p__22642
            map__22644 (if (seq? map__22644)
                         (if (next map__22644)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22644))
                           (if (seq map__22644) (first map__22644) {}))
                         map__22644)
            v_map map__22644
            id (clojure.core/get map__22644 :id)
            rev (clojure.core/get map__22644 :rev)
            v (clojure.core/get map__22644 :v)
            ensure (clojure.core/get map__22644 :ensure)]
        (when (let [m (dissoc v_map :id :rev :v :ensure)
                    val_map {:id id,
                             :rev rev,
                             :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                             :val v}]
                (if ensure
                  (if (= ensure {:id nil})
                    (cass/cql-insert session table cql-keys val_map true)
                    (cass/cql-update session table (:rev ensure) cql-keys val_map))
                  (cass/cql-insert session table cql-keys val_map false)))
          :ok))))
  (clojure.core/import 'datomic.kv_cassandra.KVCassandra)
  (defn ->KVCassandra
    ([cluster session table] (datomic.kv_cassandra.KVCassandra. cluster session table)))
  (reset-meta!
    #'->KVCassandra
    (assoc
      {:arglists (clojure.core/list ['cluster 'session 'table]), :column (int 1)}
      :name
      '->KVCassandra
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.kv-cassandra" "cluster-sessions") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.kv-cassandra" "cluster-sessions") (atom {}))
  (defn kv-cassandra
    ([endpoint]
      (let [map__22652 endpoint
            map__22652 (if (seq? map__22652)
                         (if (next map__22652)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22652))
                           (if (seq map__22652) (first map__22652) {}))
                         map__22652)
            host (clojure.core/get map__22652 :host)
            provided_cluster (clojure.core/get map__22652 :cluster)
            table (clojure.core/get map__22652 :table)
            cluster_callback (clojure.core/get map__22652 :cluster-callback)
            port (or (:port endpoint) 9042)
            user (or (:user endpoint) "")
            password (or (:password endpoint) "")
            ssl (or (:ssl endpoint) false)
            map__22653 (locking cluster-sessions
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
            map__22653 (if (seq? map__22653)
                         (if (next map__22653)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22653))
                           (if (seq map__22653) (first map__22653) {}))
                         map__22653)
            cluster (clojure.core/get map__22653 :cluster)
            session (clojure.core/get map__22653 :session)]
        (datomic.kv_cassandra.KVCassandra. cluster session table))))
  (reset-meta!
    #'kv-cassandra
    (assoc
      {:arglists (clojure.core/list ['endpoint]), :column (int 1)}
      :name
      'kv-cassandra
      :ns
      *ns*)))
