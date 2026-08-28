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
      (let [temp__5804__auto__ (cass/cql-select session table key cql-keys consistent?)]
        (when temp__5804__auto__
          (let [ret temp__5804__auto__
                ret (let [temp__5802__auto__ (:val ret)]
                      (if temp__5802__auto__ (let [v temp__5802__auto__] (assoc ret :v v)) ret))
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val) m)]
            ret))))
    (put
      [this p__32752]
      (let [map__32754 p__32752
            map__32754 (if (seq? map__32754)
                         (if (next map__32754)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32754))
                           (if (seq map__32754) (first map__32754) {}))
                         map__32754)
            v_map map__32754
            id (clojure.core/get map__32754 :id)
            rev (clojure.core/get map__32754 :rev)
            v (clojure.core/get map__32754 :v)
            ensure (clojure.core/get map__32754 :ensure)]
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
      (let [map__32762 endpoint
            map__32762 (if (seq? map__32762)
                         (if (next map__32762)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32762))
                           (if (seq map__32762) (first map__32762) {}))
                         map__32762)
            host (clojure.core/get map__32762 :host)
            provided_cluster (clojure.core/get map__32762 :cluster)
            table (clojure.core/get map__32762 :table)
            cluster_callback (clojure.core/get map__32762 :cluster-callback)
            port (or (:port endpoint) 9042)
            user (or (:user endpoint) "")
            password (or (:password endpoint) "")
            ssl (or (:ssl endpoint) false)
            map__32763 (let [lockee__5782__auto__ cluster-sessions
                             locklocal__5783__auto__ lockee__5782__auto__]
                         (monitor-enter locklocal__5783__auto__)
                         (try
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
                               m))
                           (finally (do (monitor-exit locklocal__5783__auto__) nil))))
            map__32763 (if (seq? map__32763)
                         (if (next map__32763)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32763))
                           (if (seq map__32763) (first map__32763) {}))
                         map__32763)
            cluster (clojure.core/get map__32763 :cluster)
            session (clojure.core/get map__32763 :session)]
        (datomic.kv_cassandra.KVCassandra. cluster session table)))))