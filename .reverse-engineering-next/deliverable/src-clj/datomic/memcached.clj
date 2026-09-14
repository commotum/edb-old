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
  (defn wrap-metrics
    ([f record_kv succ fail]
      (let [start (java.lang.System/nanoTime)]
        (fn fn__9939
          ([result]
            (let [v (^clojure.lang.IFn f result)]
              (^clojure.lang.IFn record_kv
                (if v succ fail)
                (long (- (java.lang.System/nanoTime) start)))
              v))))))
  (reset-meta!
    #'wrap-metrics
    (assoc
      {:private true, :arglists (clojure.core/list ['f 'record-kv 'succ 'fail]), :column 1}
      :name
      'wrap-metrics
      :ns
      *ns*))
  (defn safe-deref ([fut] (when-not (.isCancelled ^java.util.concurrent.Future fut) (deref fut))))
  (reset-meta!
    #'safe-deref
    (assoc
      {:private true, :arglists (clojure.core/list [(.withMeta 'fut {:tag 'Future})]), :column 1}
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
                     :ex t}))
                nil)
              nil)
            nil)))))
  (reset-meta!
    #'put-result-handler
    (assoc
      {:private true, :arglists (clojure.core/list ['fut]), :column 1}
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
  (defn memcached-client-supports-autodiscovery?
    ([]
      (try
        (java.lang.Class/forName "datomic.spy.memcached.ClientMode")
        (catch java.lang.ClassNotFoundException _ nil))))
  (reset-meta!
    #'memcached-client-supports-autodiscovery?
    (assoc
      {:private true, :arglists (clojure.core/list []), :column 1}
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
       :column 1}
      :name
      'set-client-mode*
      :ns
      *ns*))
  (defn configure-auto-discovery
    ([builder auto_discovery config_timeout_msec]
      (cond
        auto_discovery (set-client-mode* builder auto_discovery config_timeout_msec)
        (memcached-client-supports-autodiscovery?) (set-client-mode*
                                                     builder
                                                     auto_discovery
                                                     config_timeout_msec)
        :else (do builder))))
  (reset-meta!
    #'configure-auto-discovery
    (assoc
      {:tag datomic.spy.memcached.ConnectionFactoryBuilder,
       :private true,
       :arglists
       (clojure.core/list
         [(.withMeta 'builder {:tag 'ConnectionFactoryBuilder})
          'auto-discovery
          'config-timeout-msec]),
       :column 1}
      :name
      'configure-auto-discovery
      :ns
      *ns*))
  (def SPY_BYTEARRAY_FLAGS 2048)
  (def SPY_MAX_SIZE (long (* (* 20 1024) 1024)))
  (def legacy-transcoder
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
  (defn factory*
    ([p__9952]
      (let [map__9953 p__9952
            map__9953 (if (seq? map__9953)
                        (clojure.lang.PersistentHashMap/create (seq map__9953))
                        map__9953)
            timeout_msec (get map__9953 :timeout-msec 10)
            config_timeout_msec (get map__9953 :config-timeout-msec 100)
            username (get map__9953 :username)
            password (get map__9953 :password)
            auto_discovery (get map__9953 :auto-discovery)
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
       :column 1}
      :name
      'factory*
      :ns
      *ns*))
  (def factory factory*)
  (defn create-client
    ([p__9956]
      (let [map__9957 p__9956
            map__9957 (if (seq? map__9957)
                        (clojure.lang.PersistentHashMap/create (seq map__9957))
                        map__9957)
            args map__9957
            servers (get map__9957 :servers)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.memcached")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process {:event :memcached/connect, :servers servers}))
            nil)
          nil)
        (datomic.spy.memcached.MemcachedClient.
          (factory args)
          (AddrUtil/getAddresses ^java.lang.String servers)))))
  (defn fits-in-memcached? ([v] (<= (.remaining ^java.nio.Buffer v) 1000000)))
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
    (assoc {:private true, :column 1} :name 'local-memcached-metric-names :ns *ns*))
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
    (assoc {:private true, :column 1} :name 'memcached-metric-names :ns *ns*))
  (defonce RecoveringClientImpl {})
  (defprotocol
    RecoveringClientImpl
    (rc-shutdown [_])
    (rc-get [_ k])
    (rc-set [_ k ttl v])
    (rc-reset-if-crashed [_]))
  (deftype
    RecoveringClient
    [client_ref create_client sem]
    datomic.memcached.RecoveringClientImpl
    (rc-set
      [this k ttl v]
      (let [G__10027 (.set (deref client_ref) ^java.lang.String k (int (long ttl)) v)]
        (.addListener
          ^datomic.spy.memcached.internal.OperationFuture G__10027
          (op-listener
            (fn fn__10028
              ([p1__10023#]
                (try
                  (deref p1__10023#)
                  (catch java.lang.Throwable t (rc-reset-if-crashed this)))))))
        G__10027))
    (rc-reset-if-crashed
      [this]
      (when (not (.isAlive (.getConnection (deref client_ref))))
        (when (.tryAcquire ^java.util.concurrent.Semaphore sem)
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.memcached")]
            (when (.isWarnEnabled ^org.slf4j.Logger logger)
              (.warn
                ^org.slf4j.Logger logger
                (logger/process {:event :datomic.memcached/reset-spy-client}))
              nil)
            nil)
          (future-call
            (fn fn__10025
              ([]
                (try
                  (let [old (deref client_ref)]
                    (reset! client_ref (^clojure.lang.IFn create_client))
                    (.shutdown ^datomic.spy.memcached.MemcachedClient old)
                    nil)
                  (finally (do (.release ^java.util.concurrent.Semaphore sem) nil)))))))))
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
        (finally (do (.release ^java.util.concurrent.Semaphore sem) nil)))))
  (clojure.core/import 'datomic.memcached.RecoveringClient)
  (defn ->RecoveringClient
    ([client_ref create_client sem]
      (datomic.memcached.RecoveringClient. client_ref create_client sem)))
  (defn create-recovering-client
    ([create_client]
      (datomic.memcached.RecoveringClient.
        (atom (^clojure.lang.IFn create_client))
        create_client
        (java.util.concurrent.Semaphore. (int 1)))))
  (defn create-cache
    ([p__10035]
      (let [map__10036 p__10035
            map__10036 (if (seq? map__10036)
                         (clojure.lang.PersistentHashMap/create (seq map__10036))
                         map__10036)
            args map__10036
            shutdown_client? (get map__10036 :shutdown-client? true)
            record_kv (get map__10036 :record-kv monitor/add-stat)
            client (get map__10036 :client)
            metric_names (get map__10036 :metric-names memcached-metric-names)
            vec__10037 metric_names
            io_counter (nth vec__10037 (int 0) nil)
            io_latency (nth vec__10037 (int 1) nil)
            hit_counter (nth vec__10037 (int 2) nil)
            get_succeeded (nth vec__10037 (int 3) nil)
            get_failed (nth vec__10037 (int 4) nil)
            get_missed (nth vec__10037 (int 5) nil)
            get_timeout (nth vec__10037 (int 6) nil)
            get_queue_full (nth vec__10037 (int 7) nil)
            put_succeeded (nth vec__10037 (int 8) nil)
            put_failed (nth vec__10037 (int 9) nil)
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
                (let [G__10053 (rc-set
                                 client
                                 k
                                 (java.lang.Integer/valueOf (int ttl))
                                 (if (instance? java.nio.ByteBuffer v) (io/alias-buf-bytes v) v))]
                  (.addListener
                    ^datomic.spy.memcached.internal.OperationFuture G__10053
                    (op-listener
                      (wrap-metrics put-result-handler record_kv put_succeeded put_failed)))
                  G__10053)
                (catch java.lang.Exception e nil))
              (do
                (^clojure.lang.IFn record_kv :MemcacheItemTooLarge 1)
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.memcached")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info
                      ^org.slf4j.Logger logger
                      (logger/process {:event :memcached/item-too-large, :key k}))
                    nil)
                  nil))))
          (valAt
            [this k not_found]
            (do
              (io-stats/inc! io_counter)
              (let [record_latencies (fn record_latencies
                                       ([nanos p__10044]
                                         (let [vec__10046 p__10044
                                               v (nth vec__10046 (int 0) nil)
                                               ex (nth vec__10046 (int 1) nil)]
                                           (when-not v
                                             (^clojure.lang.IFn record_kv get_failed nanos))
                                           (io-stats/inc!
                                             io_latency
                                             (long ^java.lang.Number nanos))
                                           (let [temp__5457__auto__ (cond
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
                                             (when temp__5457__auto__
                                               (let [k temp__5457__auto__]
                                                 (^clojure.lang.IFn record_kv k nanos)))))))
                    vec__10041 (let [start__9163__auto__ (java.lang.System/nanoTime)
                                     result__9164__auto__ (try
                                                            (try
                                                              [(rc-get client k)]
                                                              (catch
                                                                java.lang.Throwable
                                                                t
                                                                [nil t]))
                                                            (catch java.lang.Throwable e e))]
                                 (^clojure.lang.IFn record_latencies
                                   (long (- (java.lang.System/nanoTime) start__9163__auto__))
                                   result__9164__auto__)
                                 (common/return-or-throw result__9164__auto__))
                    result (nth vec__10041 (int 0) nil)
                    ex (nth vec__10041 (int 1) nil)]
                (^clojure.lang.IFn record_kv hit_counter (if result 1 0))
                (cond
                  ex (do (throw ^java.lang.Throwable ex) nil)
                  (nil? result) not_found
                  (instance? bytes_class result) (ByteBuffer/wrap ^bytes result)
                  :else (do result)))))
          (valAt [this k] (.valAt this k nil))
          (^void close [this] (do (when shutdown_client? (rc-shutdown client)) nil))))))
  (defn start-local-memcached-from-config
    ([]
      (let [temp__5457__auto__ (config/local-memcached-args)]
        (when temp__5457__auto__
          (let [local_memcached_args temp__5457__auto__]
            (if (config/folsom?)
              (require/require-and-run
                'datomic.memcached.prod-266/create-cache
                local_memcached_args)
              (create-cache
                {:client
                 (create-recovering-client
                   (fn fn__10058 ([] (create-client local_memcached_args)))),
                 :metric-names local-memcached-metric-names})))))))
  (defn start-memcached-from-config
    ([]
      (let [temp__5457__auto__ (config/memcached-args)]
        (when temp__5457__auto__
          (let [memcached_args temp__5457__auto__]
            (if (config/folsom?)
              (require/require-and-run 'datomic.memcached.prod-266/create-cache memcached_args)
              (create-cache
                {:client
                 (create-recovering-client (fn fn__10062 ([] (create-client memcached_args)))),
                 :metric-names memcached-metric-names}))))))))