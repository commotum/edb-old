(do
  (clojure.core/in-ns 'datomic.memcached)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common :refer (clojure.core/list 'with-nano-time)]
        ['datomic.config :as 'config]
        ['datomic.config-ext :as 'cfext]
        ['datomic.monitor :as 'monitor]
        ['datomic.require :as 'require]
        ['datomic.io :as 'io]
        ['datomic.slf4j :as 'logger]
        ['datomic.cache.impl :as 'cache-impl]
        ['datomic.measure.io-stats :as 'io-stats])
      (clojure.core/import 'java.lang.AutoCloseable)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'datomic.spy.memcached.MemcachedClient)
      (clojure.core/import 'datomic.spy.memcached.AddrUtil)
      (clojure.core/import 'datomic.spy.memcached.KetamaConnectionFactory)
      (clojure.core/import 'datomic.spy.memcached.ConnectionFactoryBuilder)
      (clojure.core/import 'datomic.spy.memcached.ConnectionFactoryBuilder$Locator)
      (clojure.core/import 'datomic.spy.memcached.ConnectionFactoryBuilder$Protocol)
      (clojure.core/import 'datomic.spy.memcached.FailureMode)
      (clojure.core/import 'datomic.spy.memcached.DefaultHashAlgorithm)
      (clojure.core/import 'datomic.spy.memcached.OperationTimeoutException)
      (clojure.core/import 'datomic.spy.memcached.CachedData)
      (clojure.core/import 'datomic.spy.memcached.ConnectionFactory)
      (clojure.core/import 'datomic.spy.memcached.internal.OperationCompletionListener)
      (clojure.core/import 'datomic.spy.memcached.internal.OperationFuture)
      (clojure.core/import 'datomic.spy.memcached.transcoders.Transcoder)
      (clojure.core/import 'datomic.spy.memcached.auth.AuthDescriptor)
      (clojure.core/import 'datomic.spy.memcached.auth.PlainCallbackHandler)
      (clojure.core/import 'java.net.InetSocketAddress)
      (clojure.core/import 'java.net.InetAddress)
      (clojure.core/import 'java.util.concurrent.ExecutionException)
      (clojure.core/import 'java.util.concurrent.Future)
      (clojure.core/import 'java.util.concurrent.Semaphore)
      (clojure.core/import 'java.util.concurrent.TimeUnit)))
  (when-not (.equals 'datomic.memcached 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.memcached))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common :refer (clojure.core/list 'with-nano-time)]
          ['datomic.config :as 'config]
          ['datomic.config-ext :as 'cfext]
          ['datomic.monitor :as 'monitor]
          ['datomic.require :as 'require]
          ['datomic.io :as 'io]
          ['datomic.slf4j :as 'logger]
          ['datomic.cache.impl :as 'cache-impl]
          ['datomic.measure.io-stats :as 'io-stats])
        (clojure.core/import 'java.lang.AutoCloseable)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'datomic.spy.memcached.MemcachedClient)
        (clojure.core/import 'datomic.spy.memcached.AddrUtil)
        (clojure.core/import 'datomic.spy.memcached.KetamaConnectionFactory)
        (clojure.core/import 'datomic.spy.memcached.ConnectionFactoryBuilder)
        (clojure.core/import 'datomic.spy.memcached.ConnectionFactoryBuilder$Locator)
        (clojure.core/import 'datomic.spy.memcached.ConnectionFactoryBuilder$Protocol)
        (clojure.core/import 'datomic.spy.memcached.FailureMode)
        (clojure.core/import 'datomic.spy.memcached.DefaultHashAlgorithm)
        (clojure.core/import 'datomic.spy.memcached.OperationTimeoutException)
        (clojure.core/import 'datomic.spy.memcached.CachedData)
        (clojure.core/import 'datomic.spy.memcached.ConnectionFactory)
        (clojure.core/import 'datomic.spy.memcached.internal.OperationCompletionListener)
        (clojure.core/import 'datomic.spy.memcached.internal.OperationFuture)
        (clojure.core/import 'datomic.spy.memcached.transcoders.Transcoder)
        (clojure.core/import 'datomic.spy.memcached.auth.AuthDescriptor)
        (clojure.core/import 'datomic.spy.memcached.auth.PlainCallbackHandler)
        (clojure.core/import 'java.net.InetSocketAddress)
        (clojure.core/import 'java.net.InetAddress)
        (clojure.core/import 'java.util.concurrent.ExecutionException)
        (clojure.core/import 'java.util.concurrent.Future)
        (clojure.core/import 'java.util.concurrent.Semaphore)
        (clojure.core/import 'java.util.concurrent.TimeUnit))))
  (set! *warn-on-reflection* true)
  (def wrap-metrics
   (fn wrap_metrics
     ([f record_kv succ fail]
       (let [start (java.lang.System/nanoTime)]
         (fn fn__30923
           ([result]
             (let [v (^clojure.lang.IFn f result)]
               (^clojure.lang.IFn record_kv
                 (if v succ fail)
                 (long (- (java.lang.System/nanoTime) start)))
               v)))))))
  (reset-meta!
    #'wrap-metrics
    (assoc
      {:private true, :arglists (clojure.core/list ['f 'record-kv 'succ 'fail]), :column (int 1)}
      :name
      'wrap-metrics
      :ns
      *ns*))
  (def safe-deref
   (fn safe_deref ([fut] (when-not (.isCancelled ^java.util.concurrent.Future fut) (deref fut)))))
  (reset-meta!
    #'safe-deref
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'fut {:tag 'Future})]),
       :column (int 1)}
      :name
      'safe-deref
      :ns
      *ns*))
  (defn put-result-handler
    ([fut]
      (try
        (safe-deref fut)
        (catch
          java.lang.Throwable
          t
          (do
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.memcached")]
              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                (.debug
                  ^org.slf4j.Logger logger
                  (logger/process
                    {:msg
                     (str "Cache put-result-handler error: " (.getMessage ^java.lang.Throwable t)),
                     :ex t})))
              nil)
            nil)))))
  (reset-meta!
    #'put-result-handler
    (assoc
      {:private true, :arglists (clojure.core/list ['fut]), :column (int 1)}
      :name
      'put-result-handler
      :ns
      *ns*))
  (def op-listener
   (fn op_listener
     ([f]
       (reify
         datomic.spy.memcached.internal.OperationCompletionListener
         (^void onComplete
           [this ^java.util.concurrent.Future fut]
           (do (^clojure.lang.IFn f fut) nil))))))
  (reset-meta!
    #'op-listener
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta ['f] {:tag 'datomic.spy.memcached.internal.OperationCompletionListener})),
       :column (int 1)}
      :name
      'op-listener
      :ns
      *ns*))
  (defn memcached-client-supports-autodiscovery?
    ([]
      (try
        (java.lang.Class/forName "datomic.spy.memcached.ClientMode")
        (catch java.lang.ClassNotFoundException _ nil))))
  (reset-meta!
    #'memcached-client-supports-autodiscovery?
    (assoc
      {:private true, :arglists (clojure.core/list []), :column (int 1)}
      :name
      'memcached-client-supports-autodiscovery?
      :ns
      *ns*))
  (def set-client-mode*
   (fn set_client_mode_STAR_
     ([builder auto_discovery config_timeout_msec]
       (let [client_mode (java.lang.Enum/valueOf
                           (java.lang.Class/forName "datomic.spy.memcached.ClientMode")
                           (if auto_discovery "Dynamic" "Static"))
             set_client_mode_method (.getDeclaredMethod
                                      (java.lang.Class/forName
                                        "datomic.spy.memcached.ConnectionFactoryBuilder")
                                      "setClientMode"
                                      (into-array
                                        [(java.lang.Class/forName
                                           "datomic.spy.memcached.ClientMode")]))
             set_config_op_timeout_method (.getDeclaredMethod
                                            (java.lang.Class/forName
                                              "datomic.spy.memcached.ConnectionFactoryBuilder")
                                            "setConfigOpTimeout"
                                            (into-array [java.lang.Long/TYPE]))]
         (.invoke
           ^java.lang.reflect.Method set_client_mode_method
           builder
           (into-array [client_mode]))
         (.invoke
           ^java.lang.reflect.Method set_config_op_timeout_method
           builder
           (into-array [config_timeout_msec]))))))
  (reset-meta!
    #'set-client-mode*
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [(.withMeta 'builder {:tag 'ConnectionFactoryBuilder})
          'auto-discovery
          'config-timeout-msec]),
       :column (int 1)}
      :name
      'set-client-mode*
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.memcached" "configure-auto-discovery")
    {:tag datomic.spy.memcached.ConnectionFactoryBuilder,
     :private true,
     :arglists
     (clojure.core/list
       [(.withMeta 'builder {:tag 'ConnectionFactoryBuilder})
        'auto-discovery
        'config-timeout-msec]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.memcached" "configure-auto-discovery")
    (fn configure_auto_discovery
      ([builder auto_discovery config_timeout_msec]
        (cond
          auto_discovery (set-client-mode* builder auto_discovery config_timeout_msec)
          (memcached-client-supports-autodiscovery?) (set-client-mode*
                                                       builder
                                                       auto_discovery
                                                       config_timeout_msec)
          :else (do builder)))))
  (def SPY_BYTEARRAY_FLAGS 2048)
  (reset-meta! #'SPY_BYTEARRAY_FLAGS (assoc {:column (int 1)} :name 'SPY_BYTEARRAY_FLAGS :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.memcached" "SPY_MAX_SIZE") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.memcached" "SPY_MAX_SIZE") (long (* (* 20 1024) 1024)))
  (.setMeta (clojure.lang.RT/var "datomic.memcached" "legacy-transcoder") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.memcached" "legacy-transcoder")
    (reify
      datomic.spy.memcached.transcoders.Transcoder
      (^int getMaxSize [this] (.intValue SPY_MAX_SIZE))
      (decode
        [this ^datomic.spy.memcached.CachedData cd]
        (.getData ^datomic.spy.memcached.CachedData cd))
      (^datomic.spy.memcached.CachedData encode
        [this bs]
        (datomic.spy.memcached.CachedData. (int SPY_BYTEARRAY_FLAGS) ^bytes bs (int SPY_MAX_SIZE)))
      (^boolean asyncDecode [this ^datomic.spy.memcached.CachedData _] (.booleanValue false))))
  (def factory*
   (fn factory_STAR_
     ([p__30936]
       (let [map__30937 p__30936
             map__30937 (if (seq? map__30937)
                          (if (next map__30937)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__30937))
                            (if (seq map__30937) (first map__30937) {}))
                          map__30937)
             timeout_msec (get map__30937 :timeout-msec 10)
             config_timeout_msec (get map__30937 :config-timeout-msec 100)
             username (get map__30937 :username)
             password (get map__30937 :password)
             auto_discovery (get map__30937 :auto-discovery)
             fact (configure-auto-discovery
                    (.setTranscoder
                      (.setAuthWaitTime
                        (.setOpQueueMaxBlockTime
                          (.setOpTimeout
                            (.setHashAlg
                              (.setFailureMode
                                (.setLocatorType
                                  (.setProtocol
                                    (datomic.spy.memcached.ConnectionFactoryBuilder.)
                                    ConnectionFactoryBuilder$Protocol/BINARY)
                                  ConnectionFactoryBuilder$Locator/CONSISTENT)
                                FailureMode/Redistribute)
                              DefaultHashAlgorithm/KETAMA_HASH)
                            (long ^java.lang.Number timeout_msec))
                          (long ^java.lang.Number timeout_msec))
                        (long ^java.lang.Number timeout_msec))
                      legacy-transcoder)
                    auto_discovery
                    config_timeout_msec)]
         (when (and username password)
           (.setAuthDescriptor
             ^datomic.spy.memcached.ConnectionFactoryBuilder fact
             (datomic.spy.memcached.auth.AuthDescriptor.
               (into-array java.lang.String ["PLAIN"])
               (datomic.spy.memcached.auth.PlainCallbackHandler.
                 ^java.lang.String username
                 ^java.lang.String password))))
         (.build ^datomic.spy.memcached.ConnectionFactoryBuilder fact)))))
  (reset-meta!
    #'factory*
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [{:keys ['timeout-msec 'config-timeout-msec 'username 'password 'auto-discovery],
           :or {'timeout-msec 10, 'config-timeout-msec 100}}]),
       :column (int 1)}
      :name
      'factory*
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.memcached" "factory") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.memcached" "factory") factory*)
  (def create-client
   (fn create_client
     ([p__30940]
       (let [map__30941 p__30940
             map__30941 (if (seq? map__30941)
                          (if (next map__30941)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__30941))
                            (if (seq map__30941) (first map__30941) {}))
                          map__30941)
             args map__30941
             servers (get map__30941 :servers)]
         (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.memcached")]
           (when (.isInfoEnabled ^org.slf4j.Logger logger)
             (.info
               ^org.slf4j.Logger logger
               (logger/process {:event :memcached/connect, :servers servers})))
           nil)
         (datomic.spy.memcached.MemcachedClient.
           (factory args)
           (AddrUtil/getAddresses ^java.lang.String servers))))))
  (reset-meta!
    #'create-client
    (assoc
      {:arglists (clojure.core/list [{:keys [(.withMeta 'servers {:tag 'String})], :as 'args}]),
       :column (int 1)}
      :name
      'create-client
      :ns
      *ns*))
  (def fits-in-memcached?
   (fn fits_in_memcached_QMARK_ ([v] (<= (.remaining ^java.nio.Buffer v) 1000000))))
  (reset-meta!
    #'fits-in-memcached?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'v {:tag 'ByteBuffer})]), :column (int 1)}
      :name
      'fits-in-memcached?
      :ns
      *ns*))
  (def local-memcached-metric-names
   [:local-memcached
    :local-memcached-ns
    :LocalMemcache
    :LocalMemcachedGetSucceededNsec
    :LocalMemcachedGetFailedNsec
    :LocalMemcachedGetMissedNsec
    :LocalMemcachedGetTimeoutNsec
    :LocalMemcachedGetQueueFullNsec
    :LocalMemcachedPutSucceededNsec
    :LocalMemcachedPutFailedNsec])
  (reset-meta!
    #'local-memcached-metric-names
    (assoc {:private true, :column (int 1)} :name 'local-memcached-metric-names :ns *ns*))
  (def memcached-metric-names
   [:memcached
    :memcached-ns
    :Memcache
    :MemcachedGetSucceededNsec
    :MemcachedGetFailedNsec
    :MemcachedGetMissedNsec
    :MemcachedGetTimeoutNsec
    :MemcachedGetQueueFullNsec
    :MemcachedPutSucceededNsec
    :MemcachedPutFailedNsec])
  (reset-meta!
    #'memcached-metric-names
    (assoc {:private true, :column (int 1)} :name 'memcached-metric-names :ns *ns*))
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol
      RecoveringClientImpl
      (rc-shutdown [_])
      (rc-get [_ k])
      (^datomic.spy.memcached.internal.OperationFuture rc-set [_ k ttl v])
      (rc-reset-if-crashed [_]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.memcached" "RecoveringClientImpl")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'RecoveringClientImpl :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'rc-shutdown
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.memcached"
                                       "RecoveringClientImpl"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memcached" "rc-shutdown")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*)))
    (let [protocol_signature__7423 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'rc-get {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.memcached"
                                       "RecoveringClientImpl"))
          protocol_method_name__7424 (with-meta
                                       (:name protocol_signature__7423)
                                       protocol_signature__7423)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memcached" "rc-get")
        (assoc protocol_signature__7423 :name protocol_method_name__7424 :ns *ns*)))
    (let [protocol_signature__7425 (assoc
                                     {:tag 'datomic.spy.memcached.internal.OperationFuture,
                                      :name
                                      (.withMeta
                                        'rc-set
                                        {:arglists (clojure.core/list ['_ 'k 'ttl 'v])}),
                                      :arglists (clojure.core/list ['_ 'k 'ttl 'v]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.memcached"
                                       "RecoveringClientImpl"))
          protocol_method_name__7426 (with-meta
                                       (:name protocol_signature__7425)
                                       protocol_signature__7425)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memcached" "rc-set")
        (assoc protocol_signature__7425 :name protocol_method_name__7426 :ns *ns*)))
    (let [protocol_signature__7427 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'rc-reset-if-crashed
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.memcached"
                                       "RecoveringClientImpl"))
          protocol_method_name__7428 (with-meta
                                       (:name protocol_signature__7427)
                                       protocol_signature__7427)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memcached" "rc-reset-if-crashed")
        (assoc protocol_signature__7427 :name protocol_method_name__7428 :ns *ns*))))
  (deftype
    RecoveringClient
    [client_ref create_client sem]
    datomic.memcached.RecoveringClientImpl
    (rc-set
      [this k ttl v]
      (let [G__31011 (.set (deref client_ref) ^java.lang.String k (int (long ttl)) v)]
        (.addListener
          ^datomic.spy.memcached.internal.OperationFuture G__31011
          (op-listener
            (fn fn__31012
              ([p1__31007#]
                (try
                  (deref p1__31007#)
                  (catch java.lang.Throwable t (rc-reset-if-crashed this)))))))
        G__31011))
    (rc-reset-if-crashed
      [this]
      (when (not (.isAlive (.getConnection (deref client_ref))))
        (when (.tryAcquire ^java.util.concurrent.Semaphore sem)
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.memcached")]
            (when (.isWarnEnabled ^org.slf4j.Logger logger)
              (.warn
                ^org.slf4j.Logger logger
                (logger/process {:event :datomic.memcached/reset-spy-client})))
            nil)
          (future-call
            (fn fn__31009
              ([]
                (try
                  (let [old (deref client_ref)]
                    (reset! client_ref (^clojure.lang.IFn create_client))
                    (.shutdown ^datomic.spy.memcached.MemcachedClient old)
                    nil)
                  (finally (.release ^java.util.concurrent.Semaphore sem)))))))))
    (rc-get
      [this k]
      (try
        (.get (deref client_ref) ^java.lang.String k)
        (catch
          java.lang.Throwable
          t
          (do (rc-reset-if-crashed this) (throw ^java.lang.Throwable t) nil))))
    (rc-shutdown
      [this]
      (try
        (do (.acquire ^java.util.concurrent.Semaphore sem) (.shutdown (deref client_ref)) nil)
        (finally (.release ^java.util.concurrent.Semaphore sem)))))
  (clojure.core/import 'datomic.memcached.RecoveringClient)
  (def ->RecoveringClient
   (fn __GT_RecoveringClient
     ([client_ref create_client sem]
       (datomic.memcached.RecoveringClient. client_ref create_client sem))))
  (reset-meta!
    #'->RecoveringClient
    (assoc
      {:arglists (clojure.core/list ['client-ref 'create-client 'sem]), :column (int 1)}
      :name
      '->RecoveringClient
      :ns
      *ns*))
  (def create-recovering-client
   (fn create_recovering_client
     ([create_client]
       (datomic.memcached.RecoveringClient.
         (atom (^clojure.lang.IFn create_client))
         create_client
         (java.util.concurrent.Semaphore. (int 1))))))
  (reset-meta!
    #'create-recovering-client
    (assoc
      {:arglists
       (clojure.core/list (.withMeta ['create-client] {:tag 'datomic.memcached.RecoveringClient})),
       :column (int 1)}
      :name
      'create-recovering-client
      :ns
      *ns*))
  (def create-cache
   (fn create_cache
     ([p__31019]
       (let [map__31020 p__31019
             map__31020 (if (seq? map__31020)
                          (if (next map__31020)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31020))
                            (if (seq map__31020) (first map__31020) {}))
                          map__31020)
             args map__31020
             shutdown_client? (get map__31020 :shutdown-client? true)
             record_kv (get map__31020 :record-kv monitor/add-stat)
             client (get map__31020 :client)
             metric_names (get map__31020 :metric-names memcached-metric-names)
             vec__31021 metric_names
             io_counter (nth vec__31021 (int 0) nil)
             io_latency (nth vec__31021 (int 1) nil)
             hit_counter (nth vec__31021 (int 2) nil)
             get_succeeded (nth vec__31021 (int 3) nil)
             get_failed (nth vec__31021 (int 4) nil)
             get_missed (nth vec__31021 (int 5) nil)
             get_timeout (nth vec__31021 (int 6) nil)
             get_queue_full (nth vec__31021 (int 7) nil)
             put_succeeded (nth vec__31021 (int 8) nil)
             put_failed (nth vec__31021 (int 9) nil)
             ttl (-> (config/property "datomic.memcachedExpirationDays")
                  (* 24)
                  (* 60)
                  (* 60)
                  (int))
             bytes_class (java.lang.Class/forName "[B")]
         (reify
           datomic.cache.impl.CachePut
           clojure.lang.ILookup
           java.lang.AutoCloseable
           (put
             [this k v]
             (if (fits-in-memcached? v)
               (try
                 (let [G__31037 (rc-set
                                  client
                                  k
                                  (java.lang.Integer/valueOf (int ttl))
                                  (if (instance? java.nio.ByteBuffer v) (io/alias-buf-bytes v) v))]
                   (.addListener
                     ^datomic.spy.memcached.internal.OperationFuture G__31037
                     (op-listener
                       (wrap-metrics put-result-handler record_kv put_succeeded put_failed)))
                   G__31037)
                 (catch java.lang.Exception e nil))
               (do
                 (^clojure.lang.IFn record_kv :MemcacheItemTooLarge 1)
                 (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.memcached")]
                   (when (.isInfoEnabled ^org.slf4j.Logger logger)
                     (.info
                       ^org.slf4j.Logger logger
                       (logger/process {:event :memcached/item-too-large, :key k})))
                   nil))))
           (valAt
             [this k not_found]
             (do
               (io-stats/inc! io_counter)
               (let [record_latencies (fn record_latencies
                                        ([nanos p__31028]
                                          (let [vec__31030 p__31028
                                                v (nth vec__31030 (int 0) nil)
                                                ex (nth vec__31030 (int 1) nil)]
                                            (when-not v
                                              (^clojure.lang.IFn record_kv get_failed nanos))
                                            (io-stats/inc!
                                              io_latency
                                              (long ^java.lang.Number nanos))
                                            (let [temp__5804__auto__ (cond
                                                                       v get_succeeded
                                                                       (nil? ex) get_missed
                                                                       (instance?
                                                                         datomic.spy.memcached.OperationTimeoutException
                                                                         ex)
                                                                       get_timeout
                                                                       (instance?
                                                                         java.lang.IllegalStateException
                                                                         ex)
                                                                       (do get_queue_full))]
                                              (when temp__5804__auto__
                                                (let [k temp__5804__auto__]
                                                  (^clojure.lang.IFn record_kv k nanos)))))))
                     vec__31025 (let [start__8845__auto__ (java.lang.System/nanoTime)
                                      result__8846__auto__ (try
                                                             (try
                                                               [(rc-get client k)]
                                                               (catch
                                                                 java.lang.Throwable
                                                                 t
                                                                 [nil t]))
                                                             (catch java.lang.Throwable e e))]
                                  (^clojure.lang.IFn record_latencies
                                    (long (- (java.lang.System/nanoTime) start__8845__auto__))
                                    result__8846__auto__)
                                  (common/return-or-throw result__8846__auto__))
                     result (nth vec__31025 (int 0) nil)
                     ex (nth vec__31025 (int 1) nil)]
                 (^clojure.lang.IFn record_kv hit_counter (if result 1 0))
                 (when ex (throw ^java.lang.Throwable ex))
                 (cond
                   (nil? result) not_found
                   (instance? bytes_class result) (ByteBuffer/wrap ^bytes result)
                   :else (do result)))))
           (valAt [this k] (.valAt this k nil))
           (^void close [this] (do (when shutdown_client? (rc-shutdown client)) nil)))))))
  (reset-meta!
    #'create-cache
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [{:keys ['shutdown-client? 'record-kv 'client 'metric-names],
             :or
             {'shutdown-client? true,
              'record-kv 'monitor/add-stat,
              'metric-names 'memcached-metric-names},
             :as 'args}]
           {:tag 'java.lang.AutoCloseable})),
       :column (int 1)}
      :name
      'create-cache
      :ns
      *ns*))
  (defn start-local-memcached-from-config
    ([]
      (let [temp__5804__auto__ (config/local-memcached-args)]
        (when temp__5804__auto__
          (let [local_memcached_args temp__5804__auto__]
            (if (config/folsom?)
              (require/require-and-run
                'datomic.memcached.prod-266/create-cache
                local_memcached_args)
              (create-cache
                {:client
                 (create-recovering-client
                   (fn fn__31042 ([] (create-client local_memcached_args)))),
                 :metric-names local-memcached-metric-names})))))))
  (reset-meta!
    #'start-local-memcached-from-config
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'start-local-memcached-from-config
      :ns
      *ns*))
  (defn start-memcached-from-config
    ([]
      (let [temp__5804__auto__ (config/memcached-args)]
        (when temp__5804__auto__
          (let [memcached_args temp__5804__auto__]
            (if (config/folsom?)
              (require/require-and-run 'datomic.memcached.prod-266/create-cache memcached_args)
              (create-cache
                {:client
                 (create-recovering-client (fn fn__31046 ([] (create-client memcached_args)))),
                 :metric-names memcached-metric-names})))))))
  (reset-meta!
    #'start-memcached-from-config
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'start-memcached-from-config
      :ns
      *ns*)))