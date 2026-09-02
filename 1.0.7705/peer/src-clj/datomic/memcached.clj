(do
  (clojure.core/in-ns 'datomic.memcached)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.memcached)
    {:doc
     "Memcached adapter for immutable Datomic segments. Supports fixed server lists, ElastiCache node discovery, optional SASL credentials, size limits, asynchronous writes, automatic client replacement after connection failure, and cache-tier metrics."})
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
  (defn wrap-metrics
    ([f record_kv succ fail]
      (let [start (java.lang.System/nanoTime)]
        (fn fn__20696
          ([result]
            (let [v (^clojure.lang.IFn f result)]
              (^clojure.lang.IFn record_kv
                (if v succ fail)
                (long (- (java.lang.System/nanoTime) start)))
              v))))))
  (reset-meta!
    #'wrap-metrics
    (assoc
      {:private true, :arglists (clojure.core/list ['f 'record-kv 'succ 'fail]), :column (int 1)}
      :name
      'wrap-metrics
      :ns
      *ns*))
  (defn safe-deref ([fut] (when-not (.isCancelled ^java.util.concurrent.Future fut) (deref fut))))
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
  (defn op-listener
    ([f]
      (reify
        datomic.spy.memcached.internal.OperationCompletionListener
        (^void onComplete
          [this ^java.util.concurrent.Future fut]
          (do (^clojure.lang.IFn f fut) nil)))))
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
  (defn set-client-mode*
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
          (into-array [config_timeout_msec])))))
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
  ;; Configure the binary protocol, consistent hashing, request timeouts,
  ;; failure redistribution, optional discovery, and optional SASL authentication.
  (defn factory*
    ([p__20709]
      (let [map__20710 p__20709
            map__20710 (if (seq? map__20710)
                         (if (next map__20710)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20710))
                           (if (seq map__20710) (first map__20710) {}))
                         map__20710)
            timeout_msec (get map__20710 :timeout-msec 10)
            config_timeout_msec (get map__20710 :config-timeout-msec 100)
            username (get map__20710 :username)
            password (get map__20710 :password)
            auto_discovery (get map__20710 :auto-discovery)
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
        (.build ^datomic.spy.memcached.ConnectionFactoryBuilder fact))))
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
  ;; Connect a client to the comma- or space-delimited server list in :servers.
  (defn create-client
    ([p__20713]
      (let [map__20714 p__20713
            map__20714 (if (seq? map__20714)
                         (if (next map__20714)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20714))
                           (if (seq map__20714) (first map__20714) {}))
                         map__20714)
            args map__20714
            servers (get map__20714 :servers)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.memcached")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process {:event :memcached/connect, :servers servers})))
          nil)
        (datomic.spy.memcached.MemcachedClient.
          (factory args)
          (AddrUtil/getAddresses ^java.lang.String servers)))))
  (reset-meta!
    #'create-client
    (assoc
      {:arglists (clojure.core/list [{:keys [(.withMeta 'servers {:tag 'String})], :as 'args}]),
       :column (int 1)}
      :name
      'create-client
      :ns
      *ns*))
  (defn fits-in-memcached? ([v] (<= (.remaining ^java.nio.Buffer v) 1000000)))
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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      RecoveringClientImpl
      (rc-shutdown [_])
      (rc-get [_ k])
      (^datomic.spy.memcached.internal.OperationFuture rc-set [_ k ttl v])
      (rc-reset-if-crashed [_]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.memcached" "RecoveringClientImpl")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'RecoveringClientImpl :ns *ns*))
    (let [protocol_signature__7464 (assoc
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
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memcached" "rc-shutdown")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'rc-get {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.memcached"
                                       "RecoveringClientImpl"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memcached" "rc-get")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*)))
    (let [protocol_signature__7468 (assoc
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
          protocol_method_name__7469 (with-meta
                                       (:name protocol_signature__7468)
                                       protocol_signature__7468)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memcached" "rc-set")
        (assoc protocol_signature__7468 :name protocol_method_name__7469 :ns *ns*)))
    (let [protocol_signature__7470 (assoc
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
          protocol_method_name__7471 (with-meta
                                       (:name protocol_signature__7470)
                                       protocol_signature__7470)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memcached" "rc-reset-if-crashed")
        (assoc protocol_signature__7470 :name protocol_method_name__7471 :ns *ns*))))
  ;; Recreate a failed client once per outage. The semaphore serializes reset
  ;; and shutdown while ordinary reads continue to use the current client.
  (deftype
    RecoveringClient
    [client_ref create_client sem]
    datomic.memcached.RecoveringClientImpl
    (rc-set
      [this k ttl v]
      (let [G__20784 (.set (deref client_ref) ^java.lang.String k (int (long ttl)) v)]
        (.addListener
          ^datomic.spy.memcached.internal.OperationFuture G__20784
          (op-listener
            (fn fn__20785
              ([p1__20780#]
                (try
                  (deref p1__20780#)
                  (catch java.lang.Throwable t (rc-reset-if-crashed this)))))))
        G__20784))
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
            (fn fn__20782
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
  (defn ->RecoveringClient
    ([client_ref create_client sem]
      (datomic.memcached.RecoveringClient. client_ref create_client sem)))
  (reset-meta!
    #'->RecoveringClient
    (assoc
      {:arglists (clojure.core/list ['client-ref 'create-client 'sem]), :column (int 1)}
      :name
      '->RecoveringClient
      :ns
      *ns*))
  (defn create-recovering-client
    ([create_client]
      (datomic.memcached.RecoveringClient.
        (atom (^clojure.lang.IFn create_client))
        create_client
        (java.util.concurrent.Semaphore. (int 1)))))
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
  ;; Present Memcached as an immutable-value cache. Gets are synchronous and
  ;; measured; puts are asynchronous and values above one megabyte are skipped.
  (defn create-cache
    ([p__20792]
      (let [map__20793 p__20792
            map__20793 (if (seq? map__20793)
                         (if (next map__20793)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20793))
                           (if (seq map__20793) (first map__20793) {}))
                         map__20793)
            args map__20793
            shutdown_client? (get map__20793 :shutdown-client? true)
            record_kv (get map__20793 :record-kv monitor/add-stat)
            client (get map__20793 :client)
            metric_names (get map__20793 :metric-names memcached-metric-names)
            vec__20794 metric_names
            io_counter (nth vec__20794 (int 0) nil)
            io_latency (nth vec__20794 (int 1) nil)
            hit_counter (nth vec__20794 (int 2) nil)
            get_succeeded (nth vec__20794 (int 3) nil)
            get_failed (nth vec__20794 (int 4) nil)
            get_missed (nth vec__20794 (int 5) nil)
            get_timeout (nth vec__20794 (int 6) nil)
            get_queue_full (nth vec__20794 (int 7) nil)
            put_succeeded (nth vec__20794 (int 8) nil)
            put_failed (nth vec__20794 (int 9) nil)
            ttl (-> (config/property "datomic.memcachedExpirationDays") (* 24) (* 60) (* 60) (int))
            bytes_class (java.lang.Class/forName "[B")]
        (reify
          datomic.cache.impl.CachePut
          clojure.lang.ILookup
          java.lang.AutoCloseable
          (put
            [this k v]
            (if (fits-in-memcached? v)
              (try
                (let [G__20810 (rc-set
                                 client
                                 k
                                 (java.lang.Integer/valueOf (int ttl))
                                 (if (instance? java.nio.ByteBuffer v) (io/alias-buf-bytes v) v))]
                  (.addListener
                    ^datomic.spy.memcached.internal.OperationFuture G__20810
                    (op-listener
                      (wrap-metrics put-result-handler record_kv put_succeeded put_failed)))
                  G__20810)
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
                                       ([nanos p__20801]
                                         (let [vec__20803 p__20801
                                               v (nth vec__20803 (int 0) nil)
                                               ex (nth vec__20803 (int 1) nil)]
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
                    vec__20798 (let [start__8814__auto__ (java.lang.System/nanoTime)
                                     result__8815__auto__ (try
                                                            (try
                                                              [(rc-get client k)]
                                                              (catch
                                                                java.lang.Throwable
                                                                t
                                                                [nil t]))
                                                            (catch java.lang.Throwable e e))]
                                 (^clojure.lang.IFn record_latencies
                                   (long (- (java.lang.System/nanoTime) start__8814__auto__))
                                   result__8815__auto__)
                                 (common/return-or-throw result__8815__auto__))
                    result (nth vec__20798 (int 0) nil)
                    ex (nth vec__20798 (int 1) nil)]
                (^clojure.lang.IFn record_kv hit_counter (if result 1 0))
                (when ex (throw ^java.lang.Throwable ex))
                (cond
                  (nil? result) not_found
                  (instance? bytes_class result) (ByteBuffer/wrap ^bytes result)
                  :else (do result)))))
          (valAt [this k] (.valAt this k nil))
          (^void close [this] (do (when shutdown_client? (rc-shutdown client)) nil))))))
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
  ;; Start the process-local Memcached tier when local cache properties are present.
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
                   (fn fn__20815 ([] (create-client local_memcached_args)))),
                 :metric-names local-memcached-metric-names})))))))
  (reset-meta!
    #'start-local-memcached-from-config
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'start-local-memcached-from-config
      :ns
      *ns*))
  ;; Start the shared Memcached tier when external cache properties are present.
  (defn start-memcached-from-config
    ([]
      (let [temp__5804__auto__ (config/memcached-args)]
        (when temp__5804__auto__
          (let [memcached_args temp__5804__auto__]
            (if (config/folsom?)
              (require/require-and-run 'datomic.memcached.prod-266/create-cache memcached_args)
              (create-cache
                {:client
                 (create-recovering-client (fn fn__20819 ([] (create-client memcached_args)))),
                 :metric-names memcached-metric-names})))))))
  (reset-meta!
    #'start-memcached-from-config
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'start-memcached-from-config
      :ns
      *ns*)))
