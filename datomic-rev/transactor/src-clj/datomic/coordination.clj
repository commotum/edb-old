(do
  (clojure.core/in-ns 'datomic.coordination)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.cache :as 'cache]
        ['datomic.catalog :as 'catalog]
        ['datomic.cluster :as 'cluster]
        ['datomic.cluster-stack :as 'cluster-stack]
        ['datomic.codec :as 'codec]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.error :as 'error]
        ['datomic.h2 :as 'h2]
        ['datomic.io :as 'io]
        ['datomic.kv-cluster :as 'kvc]
        ['datomic.kv-sql :as 'kvsql]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.coordination 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.coordination))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.cache :as 'cache]
          ['datomic.catalog :as 'catalog]
          ['datomic.cluster :as 'cluster]
          ['datomic.cluster-stack :as 'cluster-stack]
          ['datomic.codec :as 'codec]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.error :as 'error]
          ['datomic.h2 :as 'h2]
          ['datomic.io :as 'io]
          ['datomic.kv-cluster :as 'kvc]
          ['datomic.kv-sql :as 'kvsql]
          ['datomic.slf4j :as 'logger]))))
  (def DEFAULT_HORNET_PORT 4334)
  (reset-meta!
    #'DEFAULT_HORNET_PORT
    (assoc {:const true, :column (int 1)} :name 'DEFAULT_HORNET_PORT :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.coordination" "devspec") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.coordination" "devspec") (atom nil))
  (.setMeta (clojure.lang.RT/var "datomic.coordination" "init-protocol") {:column (int 1)})
  (let [v__5792__auto__ #'init-protocol]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.coordination" "init-protocol") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.coordination" "init-protocol")
        (clojure.lang.MultiFn.
          "init-protocol"
          (fn fn__16968 ([cluster_map data_dir] (:protocol cluster_map)))
          :default
          #'clojure.core/global-hierarchy))
      #'init-protocol))
  (def init-dev
   (fn init_dev
     ([cluster_map data_dir]
       (let [spec (assoc cluster_map :data-dir (str data_dir "/db"))]
         (compare-and-set! devspec nil (h2/init-tcp spec))))))
  (reset-meta!
    #'init-dev
    (assoc
      {:arglists (clojure.core/list ['cluster-map 'data-dir]), :column (int 1)}
      :name
      'init-dev
      :ns
      *ns*))
  (defmethod
    init-protocol
    :limited-edition
    fn__16974
    ([cluster_map data_dir] (init-dev cluster_map data_dir)))
  (defmethod init-protocol :default fn__16976 ([args data_dir] nil))
  (def create-dev-cluster
   (fn create_dev_cluster
     ([cluster_conf]
       (kvc/kv-cluster
         (let [temp__5802__auto__ (deref devspec)]
           (if temp__5802__auto__
             (let [spec temp__5802__auto__] (kvsql/from-spec (h2/local-jdbc-spec spec)))
             (kvsql/from-spec (h2/remote-jdbc-spec cluster_conf))))
         cluster_conf))))
  (reset-meta!
    #'create-dev-cluster
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'create-dev-cluster
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.coordination" "create-cluster") {:column (int 1)})
  (let [v__5792__auto__ #'create-cluster]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.coordination" "create-cluster") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.coordination" "create-cluster")
        (clojure.lang.MultiFn.
          "create-cluster"
          :protocol
          :default
          #'clojure.core/global-hierarchy))
      #'create-cluster))
  (defmethod
    create-cluster
    :limited-edition
    fn__16984
    ([cluster_conf] (create-dev-cluster cluster_conf)))
  (defmethod
    create-cluster
    :default
    fn__16986
    ([cluster_conf]
      (error/arg
        :db.error/unsupported-protocol
        (str "Unsupported protocol " (:protocol cluster_conf)))))
  (def create-db-cluster
   (fn create_db_cluster
     ([cluster_conf]
       (when-not (:db-id cluster_conf)
         (throw
           (java.lang.AssertionError.
             (str "Assert failed: " (pr-str (clojure.core/list :db-id 'cluster-conf))))))
       (let [cluster (create-cluster cluster_conf)
             temp__5802__auto__ (deref cluster-stack/kv-cache-ref)]
         (if temp__5802__auto__
           (let [kv_cache temp__5802__auto__]
             (cluster-stack/cluster-with-cache cluster kv_cache {:get-fallback-msec 5}))
           cluster)))))
  (reset-meta!
    #'create-db-cluster
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'create-db-cluster
      :ns
      *ns*))
  (def create-system-cluster
   (fn create_system_cluster ([cluster_conf] (create-cluster (dissoc cluster_conf :db-id)))))
  (reset-meta!
    #'create-system-cluster
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'create-system-cluster
      :ns
      *ns*))
  (def pod-key "pod-coord")
  (reset-meta! #'pod-key (assoc {:column (int 1)} :name 'pod-key :ns *ns*))
  (def standby-key "pod-standby")
  (reset-meta! #'standby-key (assoc {:column (int 1)} :name 'standby-key :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.coordination" "heartbeat-keys") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.coordination" "heartbeat-keys") #{pod-key standby-key})
  (.setMeta (clojure.lang.RT/var "datomic.coordination" "keys-by-role") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.coordination" "keys-by-role")
    {:active pod-key, :standby standby-key})
  (def PEER_VERSION 2)
  (reset-meta! #'PEER_VERSION (assoc {:const true, :column (int 1)} :name 'PEER_VERSION :ns *ns*))
  (def create-heartbeat
   (fn create_heartbeat
     ([p__16991]
       (let [map__16992 p__16991
             map__16992 (if (seq? map__16992)
                          (if (next map__16992)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16992))
                            (if (seq map__16992) (first map__16992) {}))
                          map__16992)
             host (get map__16992 :host)
             alt_host (get map__16992 :alt-host)
             port (get map__16992 :port)
             username (get map__16992 :username)
             password (get map__16992 :password)
             version (get map__16992 :version)
             encrypt_channel (get map__16992 :encrypt-channel)
             timestamp (get map__16992 :timestamp)]
         [host alt_host port username password timestamp version encrypt_channel 2]))))
  (reset-meta!
    #'create-heartbeat
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys
           ['host 'alt-host 'port 'username 'password 'version 'encrypt-channel 'timestamp]}]),
       :column (int 1)}
      :name
      'create-heartbeat
      :ns
      *ns*))
  (def heartbeat->endpoint
   (fn heartbeat__GT_endpoint
     ([p__16994]
       (let [vec__16995 p__16994
             host (nth vec__16995 (int 0) nil)
             alt_host (nth vec__16995 (int 1) nil)
             port (nth vec__16995 (int 2) nil)
             username (nth vec__16995 (int 3) nil)
             password (nth vec__16995 (int 4) nil)
             timestamp (nth vec__16995 (int 5) nil)
             version (nth vec__16995 (int 6) nil)
             encrypt_channel (nth vec__16995 (int 7) nil)
             peer_version (nth vec__16995 (int 8) nil)]
         {:alt-host alt_host,
          :peer-version (or peer_version 1),
          :password password,
          :username username,
          :port port,
          :host host,
          :version version,
          :timestamp timestamp,
          :encrypt-channel (if (nil? version) true encrypt_channel)}))))
  (reset-meta!
    #'heartbeat->endpoint
    (assoc
      {:arglists
       (clojure.core/list
         [['host
           'alt-host
           'port
           'username
           'password
           'timestamp
           'version
           'encrypt-channel
           'peer-version]]),
       :column (int 1)}
      :name
      'heartbeat->endpoint
      :ns
      *ns*))
  (defn lookup-endpoint
    ([cluster k]
      (let [m_17000 {:event :coord/lookup-endpoint, :k k}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.coordination")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_17000 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [temp__5804__auto__ (deref (cluster/get-ref cluster k))]
                                      (when temp__5804__auto__
                                        (let [pret temp__5804__auto__]
                                          (heartbeat->endpoint (read-string (:key pret))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_17001 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_17002 (logger/format-as-msec (long elapsed_17001))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_17000 :msec msec_17002 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.coordination")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (reset-meta!
    #'lookup-endpoint
    (assoc
      {:arglists (clojure.core/list ['cluster 'k]), :column (int 1)}
      :name
      'lookup-endpoint
      :ns
      *ns*))
  (defn lookup-transactor-endpoint ([cluster] (lookup-endpoint cluster pod-key)))
  (reset-meta!
    #'lookup-transactor-endpoint
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      'lookup-transactor-endpoint
      :ns
      *ns*))
  (def vc-password
   (fn vc_password
     ([peer_password]
       (codec/bytes->string
         (codec/encode-64
           (.digest
             (java.security.MessageDigest/getInstance "MD5")
             (.getBytes ^java.lang.String peer_password)))))))
  (reset-meta!
    #'vc-password
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'peer-password {:tag 'String})]), :column (int 1)}
      :name
      'vc-password
      :ns
      *ns*))
  (defn endpoint->server-spec
    ([endpoint repair?]
      (let [temp__5804__auto__ (:vc-port endpoint)]
        (when temp__5804__auto__
          (let [port temp__5804__auto__]
            {:servers (str (:host endpoint) ":" (:vc-port endpoint)),
             :allowed-clients (into #{} (remove nil?) [(:host endpoint) (:alt-host endpoint)]),
             :max-value-bytes 1000000,
             :metric-prefix :Valcache,
             :repair? repair?,
             :ttl 0})))))
  (reset-meta!
    #'endpoint->server-spec
    (assoc
      {:arglists (clojure.core/list ['endpoint 'repair?]), :column (int 1)}
      :name
      'endpoint->server-spec
      :ns
      *ns*))
  (def allowed-valcache-client?
   (fn allowed_valcache_client_QMARK_
     ([server_specs client]
       (or
         (= client "127.0.0.1")
         (boolean
           (some
             (fn fn__17016 ([p1__17015#] (contains? (:allowed-clients p1__17015#) client)))
             server_specs))))))
  (reset-meta!
    #'allowed-valcache-client?
    (assoc
      {:arglists (clojure.core/list ['server-specs 'client]), :column (int 1)}
      :name
      'allowed-valcache-client?
      :ns
      *ns*))
  (defn check-peer-version
    ([endpoint]
      (when-not (:peer-version endpoint)
        (error/arg
          :db.error/read-transactor-location-failed
          "Could not read transactor location from storage"
          endpoint))
      (when-not (= 2 (:peer-version endpoint))
        (error/arg
          :db.error/peer-transactor-mismatch
          (str
            "Peer version "
            (config/property "datomic.version")
            " is not compatible with transactor version "
            (:version endpoint))))))
  (reset-meta!
    #'check-peer-version
    (assoc
      {:arglists (clojure.core/list ['endpoint]), :column (int 1)}
      :name
      'check-peer-version
      :ns
      *ns*))
  (defn lookup-compatible-transactor-endpoint
    ([cluster]
      (let [endpoint (lookup-transactor-endpoint cluster)]
        (check-peer-version endpoint)
        endpoint)))
  (reset-meta!
    #'lookup-compatible-transactor-endpoint
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      'lookup-compatible-transactor-endpoint
      :ns
      *ns*))
  (def cluster-conf->resolved-conf
   (fn cluster_conf__GT_resolved_conf
     ([cluster_conf]
       (let [temp__5804__auto__ (catalog/parse-db-conf
                                  (get
                                    (catalog/get-catalog (create-system-cluster cluster_conf))
                                    (:db-name cluster_conf)))]
         (when temp__5804__auto__
           (let [db_specific temp__5804__auto__] (merge cluster_conf db_specific)))))))
  (reset-meta!
    #'cluster-conf->resolved-conf
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'cluster-conf->resolved-conf
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.coordination" "db-cache") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.coordination" "db-cache")
    (cache/lookup-cache
      (cache/fn->lookup cluster-conf->resolved-conf)
      (cache/create-write-limited 100 1)))
  (def resolve-db-name (fn resolve_db_name ([cluster_conf] (get db-cache cluster_conf))))
  (reset-meta!
    #'resolve-db-name
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'resolve-db-name
      :ns
      *ns*)))