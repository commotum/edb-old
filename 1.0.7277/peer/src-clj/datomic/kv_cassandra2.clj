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
        (let [temp__5457__auto__ (cass/cql-select session table key cql-keys consistent?)]
          (when temp__5457__auto__
            (let [ret temp__5457__auto__
                  ret (let [temp__5455__auto__ (:val ret)]
                        (if temp__5455__auto__ (let [v temp__5455__auto__] (assoc ret :v v)) ret))
                  m (and (:map ret) (read-string (:map ret)))
                  ret (merge (dissoc ret :map :val) m)]
              ret)))
        (binding [cassv/*retry* kv/*retry*] (cassv/get-value session table key))))
    (put
      [this p__20617]
      (let [map__20619 p__20617
            map__20619 (if (seq? map__20619)
                         (clojure.lang.PersistentHashMap/create (seq map__20619))
                         map__20619)
            v_map map__20619
            id (clojure.core/get map__20619 :id)
            rev (clojure.core/get map__20619 :rev)
            v (clojure.core/get map__20619 :v)
            ensure (clojure.core/get map__20619 :ensure)]
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
      (let [map__20629 endpoint
            map__20629 (if (seq? map__20629)
                         (clojure.lang.PersistentHashMap/create (seq map__20629))
                         map__20629)
            host (clojure.core/get map__20629 :host)
            provided_cluster (clojure.core/get map__20629 :cluster)
            table (clojure.core/get map__20629 :table)
            cluster_callback (clojure.core/get map__20629 :cluster-callback)
            port (or (:port endpoint) 9042)
            user (or (:user endpoint) "")
            password (or (:password endpoint) "")
            ssl (or (:ssl endpoint) false)
            map__20630 (locking cluster-sessions
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
            map__20630 (if (seq? map__20630)
                         (clojure.lang.PersistentHashMap/create (seq map__20630))
                         map__20630)
            cluster (clojure.core/get map__20630 :cluster)
            session (clojure.core/get map__20630 :session)]
        (datomic.kv_cassandra2.KVCassandra2. cluster session table)))))