(do
  (clojure.core/in-ns 'datomic.kv-cassandra3)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require
        ['datomic.cassandra-v4 :as 'cass]
        ['datomic.cassandra-values-v4 :as 'cassv]
        ['datomic.kv-store :as 'kv])
      (clojure.core/import 'com.datastax.oss.driver.api.core.cql.SyncCqlSession)
      (clojure.core/import 'com.datastax.oss.driver.api.core.CqlSessionBuilder)
      (clojure.core/import 'java.net.InetSocketAddress)
      (clojure.core/import 'javax.net.ssl.SSLContext)))
  (when-not (.equals 'datomic.kv-cassandra3 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-cassandra3))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require
          ['datomic.cassandra-v4 :as 'cass]
          ['datomic.cassandra-values-v4 :as 'cassv]
          ['datomic.kv-store :as 'kv])
        (clojure.core/import 'com.datastax.oss.driver.api.core.cql.SyncCqlSession)
        (clojure.core/import 'com.datastax.oss.driver.api.core.CqlSessionBuilder)
        (clojure.core/import 'java.net.InetSocketAddress)
        (clojure.core/import 'javax.net.ssl.SSLContext))))
  (set! *warn-on-reflection* true)
  (def sessions (atom {}))
  (def cql-keys [:id2 :rev :map :val :chunks])
  (deftype
    KVCassandra3
    [session table]
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
      [this p__23644]
      (let [map__23646 p__23644
            map__23646 (if (seq? map__23646)
                         (clojure.lang.PersistentHashMap/create (seq map__23646))
                         map__23646)
            v_map map__23646
            id (clojure.core/get map__23646 :id)
            rev (clojure.core/get map__23646 :rev)
            v (clojure.core/get map__23646 :v)
            ensure (clojure.core/get map__23646 :ensure)]
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
  (clojure.core/import 'datomic.kv_cassandra3.KVCassandra3)
  (defn ->KVCassandra3 ([session table] (datomic.kv_cassandra3.KVCassandra3. session table)))
  (defn kv-cassandra
    ([endpoint]
      (let [map__23656 endpoint
            map__23656 (if (seq? map__23656)
                         (clojure.lang.PersistentHashMap/create (seq map__23656))
                         map__23656)
            host (clojure.core/get map__23656 :host)
            table (clojure.core/get map__23656 :table)
            provided_session (clojure.core/get map__23656 :session)
            port (or (:port endpoint) 9042)
            user (:user endpoint)
            password (:password endpoint)
            local_datacenter (or (:local-datacenter endpoint) "datacenter1")
            ssl (or (:ssl endpoint) false)
            map__23657 (locking sessions
                        (or
                          (clojure.core/get (deref sessions) endpoint)
                          (let [s (or
                                    provided_session
                                    (cass/session-from-callback endpoint)
                                    (let [sb (com.datastax.oss.driver.api.core.CqlSessionBuilder.)]
                                      (cond->
                                        (.withLocalDatacenter
                                          (.addContactPoint
                                            ^com.datastax.oss.driver.api.core.session.SessionBuilder sb
                                            (InetSocketAddress/createUnresolved
                                              ^java.lang.String host
                                              (int ^java.lang.Number port)))
                                          ^java.lang.String local_datacenter)
                                        (and user password)
                                        (.withAuthCredentials (str user) (str password))
                                        ssl
                                        (.withSslContext (SSLContext/getDefault))
                                        true
                                        (.build))))
                                m {:session s}]
                            (swap! sessions assoc endpoint m)
                            m)))
            map__23657 (if (seq? map__23657)
                         (clojure.lang.PersistentHashMap/create (seq map__23657))
                         map__23657)
            session (clojure.core/get map__23657 :session)]
        (datomic.kv_cassandra3.KVCassandra3. session table)))))