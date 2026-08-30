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
    (assoc {:private true, :const true, :column 1} :name 'max-bytes :ns *ns*))
  (defn fits-in-cache? ([v] (<= (.remaining ^java.nio.Buffer v) 1000000)))
  (reset-meta!
    #'fits-in-cache?
    (assoc
      {:private true, :arglists (clojure.core/list [(.withMeta 'v {:tag 'ByteBuffer})]), :column 1}
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
          (fn fn__9918
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
                (logger/process {:event :valcache/item-too-large, :max-bytes 1000000, :key k}))
              nil)
            nil)
          nil)))
    (valAt
      [this k not_found]
      (do
        (io-stats/inc! :valcache)
        (let [record_latencies (fn record_latencies
                                 ([nanos p__9910]
                                   (let [vec__9912 p__9910
                                         v (nth vec__9912 (int 0) nil)
                                         ex (nth vec__9912 (int 1) nil)
                                         k (if v
                                             :ValcacheGetSucceededNsec
                                             (if (nil? ex)
                                               :ValcacheGetMissedNsec
                                               :ValcacheGetExceptionNsec))]
                                     (monitor/add-stat k nanos)
                                     (io-stats/inc! :valcache-ns (long ^java.lang.Number nanos)))))
              vec__9907 (let [temp__5455__auto__ (puts-pool/get-from-queued-put puts_pool k)]
                          (if temp__5455__auto__
                            (let [ret temp__5455__auto__] [ret])
                            (let [start__9163__auto__ (java.lang.System/nanoTime)
                                  result__9164__auto__ (try
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
                                (long (- (java.lang.System/nanoTime) start__9163__auto__))
                                result__9164__auto__)
                              (common/return-or-throw result__9164__auto__))))
              ret (nth vec__9907 (int 0) nil)
              ex (nth vec__9907 (int 1) nil)]
          (monitor/add-stat :Valcache (if ret 1 0))
          (cond ex (do (throw ^java.lang.Throwable ex) nil) (nil? ret) not_found :else (do ret)))))
    (valAt [this k] (.valAt this k nil))
    (^void close [this] (do (^clojure.lang.IFn shutdown_fn) nil)))
  (clojure.core/import 'datomic.valcache_direct.ValcacheDirect)
  (defn ->ValcacheDirect
    ([root shutdown_fn puts_pool]
      (datomic.valcache_direct.ValcacheDirect. root shutdown_fn puts_pool)))
  (defn create
    ([p__9927]
      (let [map__9928 p__9927
            map__9928 (if (seq? map__9928)
                        (clojure.lang.PersistentHashMap/create (seq map__9928))
                        map__9928)
            args map__9928
            path (get map__9928 :path)
            puts_pool (get map__9928 :puts-pool)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache-direct")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process {:event :valcache-direct/start, :root path}))
            nil)
          nil)
        (let [shutdown_fn (vc/direct-init args)]
          (datomic.valcache_direct.ValcacheDirect.
            path
            shutdown_fn
            (or puts_pool (deref puts-pool-impl/valcache-puts-pool))))))))