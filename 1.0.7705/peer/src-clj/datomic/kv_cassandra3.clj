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
  (.setMeta (clojure.lang.RT/var "datomic.kv-cassandra3" "sessions") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.kv-cassandra3" "sessions") (atom {}))
  (def cql-keys [:id2 :rev :map :val :chunks])
  (reset-meta! #'cql-keys (assoc {:column (int 1)} :name 'cql-keys :ns *ns*))
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
      [this p__15032]
      (let [map__15034 p__15032
            map__15034 (if (seq? map__15034)
                         (if (next map__15034)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15034))
                           (if (seq map__15034) (first map__15034) {}))
                         map__15034)
            v_map map__15034
            id (clojure.core/get map__15034 :id)
            rev (clojure.core/get map__15034 :rev)
            v (clojure.core/get map__15034 :v)
            ensure (clojure.core/get map__15034 :ensure)]
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
  (reset-meta!
    #'->KVCassandra3
    (assoc
      {:arglists (clojure.core/list ['session 'table]), :column (int 1)}
      :name
      '->KVCassandra3
      :ns
      *ns*))
  (defn kv-cassandra
    ([endpoint]
      (let [map__15044 endpoint
            map__15044 (if (seq? map__15044)
                         (if (next map__15044)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15044))
                           (if (seq map__15044) (first map__15044) {}))
                         map__15044)
            host (clojure.core/get map__15044 :host)
            table (clojure.core/get map__15044 :table)
            provided_session (clojure.core/get map__15044 :session)
            port (or (:port endpoint) 9042)
            user (:user endpoint)
            password (:password endpoint)
            local_datacenter (or (:local-datacenter endpoint) "datacenter1")
            ssl (or (:ssl endpoint) false)
            map__15045 (locking sessions
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
            map__15045 (if (seq? map__15045)
                         (if (next map__15045)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15045))
                           (if (seq map__15045) (first map__15045) {}))
                         map__15045)
            session (clojure.core/get map__15045 :session)]
        (datomic.kv_cassandra3.KVCassandra3. session table))))
  (reset-meta!
    #'kv-cassandra
    (assoc
      {:arglists (clojure.core/list ['endpoint]), :column (int 1)}
      :name
      'kv-cassandra
      :ns
      *ns*)))