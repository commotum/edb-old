(do
  (clojure.core/in-ns 'datomic.kv-cassandra)
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
  (def cql-keys [:id :rev :map :val])
  (deftype
    KVCassandra
    [cluster session table]
    datomic.kv_store.KVStore
    (close [this] nil)
    (delete [this key consistent?] (do (cass/cql-delete session table key cql-keys) :ok))
    (get
      [this key consistent?]
      (let [temp__5457__auto__ (cass/cql-select session table key cql-keys consistent?)]
        (when temp__5457__auto__
          (let [ret temp__5457__auto__
                ret (let [temp__5455__auto__ (:val ret)]
                      (if temp__5455__auto__ (let [v temp__5455__auto__] (assoc ret :v v)) ret))
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val) m)]
            ret))))
    (put
      [this p__17768]
      (let [map__17770 p__17768
            map__17770 (if (seq? map__17770)
                         (clojure.lang.PersistentHashMap/create (seq map__17770))
                         map__17770)
            v_map map__17770
            id (clojure.core/get map__17770 :id)
            rev (clojure.core/get map__17770 :rev)
            v (clojure.core/get map__17770 :v)
            ensure (clojure.core/get map__17770 :ensure)]
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
  (def cluster-sessions (atom {}))
  (defn kv-cassandra
    ([endpoint]
      (let [map__17778 endpoint
            map__17778 (if (seq? map__17778)
                         (clojure.lang.PersistentHashMap/create (seq map__17778))
                         map__17778)
            host (clojure.core/get map__17778 :host)
            provided_cluster (clojure.core/get map__17778 :cluster)
            table (clojure.core/get map__17778 :table)
            cluster_callback (clojure.core/get map__17778 :cluster-callback)
            port (or (:port endpoint) 9042)
            user (or (:user endpoint) "")
            password (or (:password endpoint) "")
            ssl (or (:ssl endpoint) false)
            map__17779 (locking cluster-sessions
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
            map__17779 (if (seq? map__17779)
                         (clojure.lang.PersistentHashMap/create (seq map__17779))
                         map__17779)
            cluster (clojure.core/get map__17779 :cluster)
            session (clojure.core/get map__17779 :session)]
        (datomic.kv_cassandra.KVCassandra. cluster session table)))))