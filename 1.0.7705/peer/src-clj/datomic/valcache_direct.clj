(do
  (clojure.core/in-ns 'datomic.valcache-direct)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.async :as 'async]
        ['datomic.cache.impl :as 'cache-impl]
        ['datomic.common :as 'common :refer (clojure.core/list 'with-nano-time)]
        ['datomic.config :as 'config]
        ['datomic.config-ext :as 'cfext]
        ['datomic.slf4j :as 'logger]
        ['datomic.monitor :as 'monitor]
        ['datomic.valcache :as 'vc]
        ['datomic.valcache.puts-pool :as 'puts-pool]
        ['datomic.valcache.puts-pool-impl :as 'puts-pool-impl]
        ['datomic.measure.io-stats :as 'io-stats])
      (clojure.core/import 'clojure.lang.ILookup)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.lang.AutoCloseable)
      (clojure.core/import 'java.util.concurrent.ExecutorService)
      (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
      (clojure.core/import 'java.util.concurrent.ThreadFactory)
      (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor)
      (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor$DiscardPolicy)
      (clojure.core/import 'java.util.concurrent.TimeUnit)))
  (when-not (.equals 'datomic.valcache-direct 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.valcache-direct))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.async :as 'async]
          ['datomic.cache.impl :as 'cache-impl]
          ['datomic.common :as 'common :refer (clojure.core/list 'with-nano-time)]
          ['datomic.config :as 'config]
          ['datomic.config-ext :as 'cfext]
          ['datomic.slf4j :as 'logger]
          ['datomic.monitor :as 'monitor]
          ['datomic.valcache :as 'vc]
          ['datomic.valcache.puts-pool :as 'puts-pool]
          ['datomic.valcache.puts-pool-impl :as 'puts-pool-impl]
          ['datomic.measure.io-stats :as 'io-stats])
        (clojure.core/import 'clojure.lang.ILookup)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.lang.AutoCloseable)
        (clojure.core/import 'java.util.concurrent.ExecutorService)
        (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
        (clojure.core/import 'java.util.concurrent.ThreadFactory)
        (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor)
        (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor$DiscardPolicy)
        (clojure.core/import 'java.util.concurrent.TimeUnit))))
  (set! *warn-on-reflection* true)
  (def max-bytes 1000000)
  (reset-meta!
    #'max-bytes
    (assoc {:private true, :const true, :column (int 1)} :name 'max-bytes :ns *ns*))
  (def fits-in-cache? (fn fits_in_cache_QMARK_ ([v] (<= (.remaining ^java.nio.Buffer v) 1000000))))
  (reset-meta!
    #'fits-in-cache?
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'v {:tag 'ByteBuffer})]),
       :column (int 1)}
      :name
      'fits-in-cache?
      :ns
      *ns*))
  (deftype
    ValcacheDirect
    [root shutdown_fn puts_pool]
    clojure.lang.ILookup
    datomic.cache.impl.CachePut
    java.lang.AutoCloseable
    (put
      [this k v]
      (if (fits-in-cache? v)
        (puts-pool/submit
          puts_pool
          k
          {:source :bbuf, :v v}
          (fn fn__22999
            ([]
              (let [start (java.lang.System/nanoTime) result (vc/direct-put root k v)]
                (if result
                  (monitor/add-stat
                    :ValcacheWriteNsec
                    (long (- (java.lang.System/nanoTime) start)))
                  (monitor/add-stat :ValcachePutFailException 1))
                result))))
        (do
          (monitor/add-stat :ValcachePutFailSize 1)
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache-direct")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info
                ^org.slf4j.Logger logger
                (logger/process {:event :valcache/item-too-large, :max-bytes 1000000, :key k})))
            nil)
          nil)))
    (valAt
      [this k not_found]
      (do
        (io-stats/inc! :valcache)
        (let [record_latencies (fn record_latencies
                                 ([nanos p__22991]
                                   (let [vec__22993 p__22991
                                         v (nth vec__22993 (int 0) nil)
                                         ex (nth vec__22993 (int 1) nil)
                                         k (if v
                                             :ValcacheGetSucceededNsec
                                             (if (nil? ex)
                                               :ValcacheGetMissedNsec
                                               :ValcacheGetExceptionNsec))]
                                     (monitor/add-stat k nanos)
                                     (io-stats/inc! :valcache-ns (long ^java.lang.Number nanos)))))
              vec__22988 (let [temp__5802__auto__ (puts-pool/get-from-queued-put puts_pool k)]
                           (if temp__5802__auto__
                             (let [ret temp__5802__auto__] [ret])
                             (let [start__8814__auto__ (java.lang.System/nanoTime)
                                   result__8815__auto__ (try
                                                          (try
                                                            [(vc/direct-get root k)]
                                                            (catch
                                                              java.lang.Throwable
                                                              t
                                                              (do
                                                                (let 
                                                                  [logger
                                                                   (org.slf4j.LoggerFactory/getLogger
                                                                     "datomic.valcache-direct")
                                                                   ex t]
                                                                  (when
                                                                    (.isInfoEnabled
                                                                      ^org.slf4j.Logger logger)
                                                                    (.info
                                                                      ^org.slf4j.Logger logger
                                                                      (logger/process
                                                                        {:event
                                                                         :valcache/get-exception})
                                                                      ^java.lang.Throwable ex)
                                                                    (logger/caused-by logger ex))
                                                                  nil)
                                                                [nil t])))
                                                          (catch java.lang.Throwable e e))]
                               (^clojure.lang.IFn record_latencies
                                 (long (- (java.lang.System/nanoTime) start__8814__auto__))
                                 result__8815__auto__)
                               (common/return-or-throw result__8815__auto__))))
              ret (nth vec__22988 (int 0) nil)
              ex (nth vec__22988 (int 1) nil)]
          (monitor/add-stat :Valcache (if ret 1 0))
          (when ex (throw ^java.lang.Throwable ex))
          (cond (nil? ret) not_found :else (do ret)))))
    (valAt [this k] (.valAt this k nil))
    (^void close [this] (do (^clojure.lang.IFn shutdown_fn) nil)))
  (clojure.core/import 'datomic.valcache_direct.ValcacheDirect)
  (def ->ValcacheDirect
   (fn __GT_ValcacheDirect
     ([root shutdown_fn puts_pool]
       (datomic.valcache_direct.ValcacheDirect. root shutdown_fn puts_pool))))
  (reset-meta!
    #'->ValcacheDirect
    (assoc
      {:arglists (clojure.core/list ['root 'shutdown-fn 'puts-pool]), :column (int 1)}
      :name
      '->ValcacheDirect
      :ns
      *ns*))
  (def create
   (fn create
     ([p__23008]
       (let [map__23009 p__23008
             map__23009 (if (seq? map__23009)
                          (if (next map__23009)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__23009))
                            (if (seq map__23009) (first map__23009) {}))
                          map__23009)
             args map__23009
             path (get map__23009 :path)
             puts_pool (get map__23009 :puts-pool)]
         (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache-direct")]
           (when (.isInfoEnabled ^org.slf4j.Logger logger)
             (.info
               ^org.slf4j.Logger logger
               (logger/process {:event :valcache-direct/start, :root path})))
           nil)
         (let [shutdown_fn (vc/direct-init args)]
           (datomic.valcache_direct.ValcacheDirect.
             path
             shutdown_fn
             (or puts_pool (deref puts-pool-impl/valcache-puts-pool))))))))
  (reset-meta!
    #'create
    (assoc
      {:arglists (clojure.core/list [{:keys ['path 'puts-pool], :as 'args}]), :column (int 1)}
      :name
      'create
      :ns
      *ns*)))