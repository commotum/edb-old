(do
  (clojure.core/in-ns 'datomic.coordination)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.coordination)
    {:doc
     "Coordinates database identity and process discovery through storage. Transactors publish versioned heartbeat endpoints; peers resolve compatible endpoints and validate protocol versions before connecting."})
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
          ['datomic.io :as 'io]
          ['datomic.kv-cluster :as 'kvc]
          ['datomic.kv-sql :as 'kvsql]
          ['datomic.slf4j :as 'logger]))))
  (def DEFAULT_HORNET_PORT 4334)
  (reset-meta!
    #'DEFAULT_HORNET_PORT
    (assoc {:const true, :column (int 1)} :name 'DEFAULT_HORNET_PORT :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.coordination" "init-protocol") {:column (int 1)})
  (let [v__5813__auto__ #'init-protocol]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.coordination" "init-protocol") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.coordination" "init-protocol")
        (clojure.lang.MultiFn.
          "init-protocol"
          (fn fn__10824 ([cluster_map data_dir] (:protocol cluster_map)))
          :default
          #'clojure.core/global-hierarchy))
      #'init-protocol))
  (defmethod init-protocol :default fn__10829 ([args data_dir] nil))
  (.setMeta (clojure.lang.RT/var "datomic.coordination" "create-cluster") {:column (int 1)})
  (let [v__5813__auto__ #'create-cluster]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
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
    :default
    fn__10835
    ([cluster-conf]
      (error/arg
        :db.error/unsupported-protocol
        (str "Unsupported protocol " (:protocol cluster-conf)))))
  (defn create-db-cluster
    ([cluster-conf]
      (when-not (:db-id cluster-conf)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list :db-id 'cluster-conf))))))
      (let [cluster (create-cluster cluster-conf)
            temp__5823__auto__ (deref cluster-stack/kv-cache-ref)]
        (if temp__5823__auto__
          (let [kv_cache temp__5823__auto__]
            (cluster-stack/cluster-with-cache cluster kv_cache {:get-fallback-msec 5}))
          cluster))))
  (reset-meta!
    #'create-db-cluster
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'create-db-cluster
      :ns
      *ns*))
  (defn create-system-cluster ([cluster-conf] (create-cluster (dissoc cluster-conf :db-id))))
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
  ;; Encodes the published transactor endpoint in a versioned storage representation.
  (defn create-heartbeat
    ([p__10840]
      (let [map__10841 p__10840
            map__10841 (if (seq? map__10841)
                         (if (next map__10841)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__10841))
                           (if (seq map__10841) (first map__10841) {}))
                         map__10841)
            host (get map__10841 :host)
            alt_host (get map__10841 :alt-host)
            port (get map__10841 :port)
            username (get map__10841 :username)
            password (get map__10841 :password)
            version (get map__10841 :version)
            encrypt_channel (get map__10841 :encrypt-channel)
            timestamp (get map__10841 :timestamp)]
        [host alt_host port username password timestamp version encrypt_channel 2])))
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
  ;; Decodes current and legacy heartbeat representations into a connection endpoint.
  (defn heartbeat->endpoint
    ([p__10843]
      (let [vec__10844 p__10843
            host (nth vec__10844 (int 0) nil)
            alt_host (nth vec__10844 (int 1) nil)
            port (nth vec__10844 (int 2) nil)
            username (nth vec__10844 (int 3) nil)
            password (nth vec__10844 (int 4) nil)
            timestamp (nth vec__10844 (int 5) nil)
            version (nth vec__10844 (int 6) nil)
            encrypt_channel (nth vec__10844 (int 7) nil)
            peer_version (nth vec__10844 (int 8) nil)]
        {:alt-host alt_host,
         :peer-version (or peer_version 1),
         :password password,
         :username username,
         :port port,
         :host host,
         :version version,
         :timestamp timestamp,
         :encrypt-channel (if (nil? version) true encrypt_channel)})))
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
  ;; Reads a revisioned heartbeat from storage and returns its endpoint description.
  (defn lookup-endpoint
    ([cluster k]
      (let [m_10849 {:event :coord/lookup-endpoint, :k k}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.coordination")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_10849 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned
                                    (let [temp__5825__auto__ (deref (cluster/get-ref cluster k))]
                                      (when temp__5825__auto__
                                        (let [pret temp__5825__auto__]
                                          (heartbeat->endpoint (read-string (:key pret))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8601__auto__
                                     {:threw t__8601__auto__}))
            elapsed_10850 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_10851 (logger/format-as-msec (long elapsed_10850))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_10849 :msec msec_10851 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.coordination")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
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
  (defn vc-password
    ([peer-password]
      (codec/bytes->string
        (codec/encode-64
          (.digest
            (java.security.MessageDigest/getInstance "MD5")
            (.getBytes ^java.lang.String peer-password))))))
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
      (let [temp__5825__auto__ (:vc-port endpoint)]
        (when temp__5825__auto__
          (let [port temp__5825__auto__]
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
  (defn allowed-valcache-client?
    ([server-specs client]
      (or
        (= client "127.0.0.1")
        (boolean
          (some
            (fn fn__10865 ([p1__10864#] (contains? (:allowed-clients p1__10864#) client)))
            server-specs)))))
  (reset-meta!
    #'allowed-valcache-client?
    (assoc
      {:arglists (clojure.core/list ['server-specs 'client]), :column (int 1)}
      :name
      'allowed-valcache-client?
      :ns
      *ns*))
  ;; Rejects endpoints whose published wire protocol cannot serve this Peer.
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
  ;; Resolves the active transactor and validates compatibility before a connection is attempted.
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
  (defn cluster-conf->resolved-conf
    ([cluster-conf]
      (let [temp__5825__auto__ (catalog/parse-db-conf
                                 (get
                                   (catalog/get-catalog (create-system-cluster cluster-conf))
                                   (:db-name cluster-conf)))]
        (when temp__5825__auto__
          (let [db-specific temp__5825__auto__] (merge cluster-conf db-specific))))))
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
  (defn resolve-db-name ([cluster-conf] (get db-cache cluster-conf)))
  (reset-meta!
    #'resolve-db-name
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'resolve-db-name
      :ns
      *ns*)))
