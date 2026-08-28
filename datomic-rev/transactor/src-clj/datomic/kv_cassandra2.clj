(do
  (clojure.core/in-ns 'datomic.kv-cassandra2)
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
  (def cluster-sessions (atom {}))
  (def cql-keys [:id2 :rev :map :val :chunks])
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
      [this p__26797]
      (let [map__26799 p__26797
            map__26799 (if (seq? map__26799)
                         (if (next map__26799)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26799))
                           (if (seq map__26799) (first map__26799) {}))
                         map__26799)
            v_map map__26799
            id (clojure.core/get map__26799 :id)
            rev (clojure.core/get map__26799 :rev)
            v (clojure.core/get map__26799 :v)
            ensure (clojure.core/get map__26799 :ensure)]
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
  (defn kv-cassandra
    ([endpoint]
      (let [map__26809 endpoint
            map__26809 (if (seq? map__26809)
                         (if (next map__26809)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26809))
                           (if (seq map__26809) (first map__26809) {}))
                         map__26809)
            host (clojure.core/get map__26809 :host)
            provided_cluster (clojure.core/get map__26809 :cluster)
            table (clojure.core/get map__26809 :table)
            cluster_callback (clojure.core/get map__26809 :cluster-callback)
            port (or (:port endpoint) 9042)
            user (or (:user endpoint) "")
            password (or (:password endpoint) "")
            ssl (or (:ssl endpoint) false)
            map__26810 (let [lockee__5782__auto__ cluster-sessions
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
            map__26810 (if (seq? map__26810)
                         (if (next map__26810)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26810))
                           (if (seq map__26810) (first map__26810) {}))
                         map__26810)
            cluster (clojure.core/get map__26810 :cluster)
            session (clojure.core/get map__26810 :session)]
        (datomic.kv_cassandra2.KVCassandra2. cluster session table)))))