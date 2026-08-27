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
    (assoc {:const true, :column 1} :name 'DEFAULT_HORNET_PORT :ns *ns*))
  (def devspec (atom nil))
  (defmulti init-protocol (fn fn__11659 ([cluster_map data_dir] (:protocol cluster_map))))
  (defn init-dev
    ([cluster_map data_dir]
      (let [spec (assoc cluster_map :data-dir (str data_dir "/db"))]
        (compare-and-set! devspec nil (h2/init-tcp spec)))))
  (defmethod
    init-protocol
    :limited-edition
    fn__11665
    ([cluster_map data_dir] (init-dev cluster_map data_dir)))
  (defmethod init-protocol :default fn__11667 ([args data_dir] nil))
  (defn create-dev-cluster
    ([cluster_conf]
      (kvc/kv-cluster
        (let [temp__5455__auto__ (deref devspec)]
          (if temp__5455__auto__
            (let [spec temp__5455__auto__] (kvsql/from-spec (h2/local-jdbc-spec spec)))
            (kvsql/from-spec (h2/remote-jdbc-spec cluster_conf))))
        cluster_conf)))
  (defmulti create-cluster :protocol)
  (defmethod
    create-cluster
    :limited-edition
    fn__11675
    ([cluster_conf] (create-dev-cluster cluster_conf)))
  (defmethod
    create-cluster
    :default
    fn__11677
    ([cluster_conf]
      (error/arg
        :db.error/unsupported-protocol
        (str "Unsupported protocol " (:protocol cluster_conf)))))
  (defn create-db-cluster
    ([cluster_conf]
      (when-not (:db-id cluster_conf)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list :db-id 'cluster-conf))))))
      (let [cluster (create-cluster cluster_conf)
            temp__5455__auto__ (deref cluster-stack/kv-cache-ref)]
        (if temp__5455__auto__
          (let [kv_cache temp__5455__auto__]
            (cluster-stack/cluster-with-cache cluster kv_cache {:get-fallback-msec 5}))
          cluster))))
  (defn create-system-cluster ([cluster_conf] (create-cluster (dissoc cluster_conf :db-id))))
  (def pod-key "pod-coord")
  (def standby-key "pod-standby")
  (def heartbeat-keys #{pod-key standby-key})
  (def keys-by-role {:active pod-key, :standby standby-key})
  (def PEER_VERSION 2)
  (reset-meta! #'PEER_VERSION (assoc {:const true, :column 1} :name 'PEER_VERSION :ns *ns*))
  (defn create-heartbeat
    ([p__11682]
      (let [map__11683 p__11682
            map__11683 (if (seq? map__11683)
                         (clojure.lang.PersistentHashMap/create (seq map__11683))
                         map__11683)
            host (get map__11683 :host)
            alt_host (get map__11683 :alt-host)
            port (get map__11683 :port)
            username (get map__11683 :username)
            password (get map__11683 :password)
            version (get map__11683 :version)
            encrypt_channel (get map__11683 :encrypt-channel)
            timestamp (get map__11683 :timestamp)]
        [host alt_host port username password timestamp version encrypt_channel 2])))
  (defn heartbeat->endpoint
    ([p__11685]
      (let [vec__11686 p__11685
            host (nth vec__11686 (int 0) nil)
            alt_host (nth vec__11686 (int 1) nil)
            port (nth vec__11686 (int 2) nil)
            username (nth vec__11686 (int 3) nil)
            password (nth vec__11686 (int 4) nil)
            timestamp (nth vec__11686 (int 5) nil)
            version (nth vec__11686 (int 6) nil)
            encrypt_channel (nth vec__11686 (int 7) nil)
            peer_version (nth vec__11686 (int 8) nil)]
        {:alt-host alt_host,
         :peer-version (or peer_version 1),
         :password password,
         :username username,
         :port port,
         :host host,
         :version version,
         :timestamp timestamp,
         :encrypt-channel (if (nil? version) true encrypt_channel)})))
  (defn lookup-endpoint
    ([cluster k]
      (let [m_11691 {:event :coord/lookup-endpoint, :k k}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.coordination")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_11691 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (let [temp__5457__auto__ (deref (cluster/get-ref cluster k))]
                                      (when temp__5457__auto__
                                        (let [pret temp__5457__auto__]
                                          (heartbeat->endpoint (read-string (:key pret))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_11692 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_11693 (logger/format-as-msec (long elapsed_11692))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_11691 :msec msec_11693 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.coordination")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn lookup-transactor-endpoint ([cluster] (lookup-endpoint cluster pod-key)))
  (defn vc-password
    ([peer_password]
      (codec/bytes->string
        (codec/encode-64
          (.digest
            (java.security.MessageDigest/getInstance "MD5")
            (.getBytes ^java.lang.String peer_password))))))
  (defn endpoint->server-spec
    ([endpoint repair?]
      (let [temp__5457__auto__ (:vc-port endpoint)]
        (when temp__5457__auto__
          (let [port temp__5457__auto__]
            {:servers (str (:host endpoint) ":" (:vc-port endpoint)),
             :allowed-clients (into #{} (remove nil?) [(:host endpoint) (:alt-host endpoint)]),
             :max-value-bytes 1000000,
             :metric-prefix :Valcache,
             :repair? repair?,
             :ttl 0})))))
  (defn allowed-valcache-client?
    ([server_specs client]
      (or
        (= client "127.0.0.1")
        (boolean
          (some
            (fn fn__11707 ([p1__11706#] (contains? (:allowed-clients p1__11706#) client)))
            server_specs)))))
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
  (defn lookup-compatible-transactor-endpoint
    ([cluster]
      (let [endpoint (lookup-transactor-endpoint cluster)]
        (check-peer-version endpoint)
        endpoint)))
  (defn cluster-conf->resolved-conf
    ([cluster_conf]
      (let [temp__5457__auto__ (catalog/parse-db-conf
                                 (get
                                   (catalog/get-catalog (create-system-cluster cluster_conf))
                                   (:db-name cluster_conf)))]
        (when temp__5457__auto__
          (let [db_specific temp__5457__auto__] (merge cluster_conf db_specific))))))
  (def db-cache
   (cache/lookup-cache
     (cache/fn->lookup cluster-conf->resolved-conf)
     (cache/create-write-limited 100 1)))
  (defn resolve-db-name ([cluster_conf] (get db-cache cluster_conf))))