(do
  (clojure.core/in-ns 'datomic.valcache.puts-pool-impl)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
      (clojure.core/import 'java.util.concurrent.ConcurrentMap)
      (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
      (clojure.core/import 'java.util.concurrent.ThreadFactory)
      (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor)
      (clojure.core/import 'java.util.concurrent.TimeUnit)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.monitor :as 'monitor]
        ['datomic.valcache.puts-pool :as 'puts-pool])))
  (when-not (.equals 'datomic.valcache.puts-pool-impl 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.valcache.puts-pool-impl))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
        (clojure.core/import 'java.util.concurrent.ConcurrentMap)
        (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
        (clojure.core/import 'java.util.concurrent.ThreadFactory)
        (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor)
        (clojure.core/import 'java.util.concurrent.TimeUnit)
        (clojure.core/require
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.monitor :as 'monitor]
          ['datomic.valcache.puts-pool :as 'puts-pool]))))
  (set! *warn-on-reflection* true)
  (defmethod
    puts-pool/get-from-put
    :bbuf
    fn__9878
    ([p__9877]
      (let [map__9879 p__9877
            map__9879 (if (seq? map__9879)
                        (clojure.lang.PersistentHashMap/create (seq map__9879))
                        map__9879)
            v (get map__9879 :v)]
        (monitor/add-stat :ValcacheGetInFlight 1)
        (.duplicate ^java.nio.ByteBuffer v))))
  (deftype
    ValcachePutsPoolImpl
    [limit puts pool]
    datomic.valcache.puts_pool.PutsPool
    java.lang.AutoCloseable
    (get-queued-put
      [this k]
      (let [temp__5457__auto__ (get puts k)]
        (when temp__5457__auto__
          (let [map__9885 temp__5457__auto__
                map__9885 (if (seq? map__9885)
                            (clojure.lang.PersistentHashMap/create (seq map__9885))
                            map__9885)
                data (get map__9885 :data)
                thunk (get map__9885 :thunk)]
            {:data data, :fut (deref thunk)}))))
    (submit
      [this k data f]
      (do
        (monitor/add-stat
          :ValcachePutQueueLength
          (java.lang.Integer/valueOf (int (.size ^java.util.Map puts))))
        (if (and
              (not (.isShutdown ^java.util.concurrent.ThreadPoolExecutor pool))
              (<= (.size ^java.util.Map puts) limit))
          (let [new_thunk (promise)
                new_data {:data data, :thunk new_thunk}
                map__9882 (.putIfAbsent ^java.util.concurrent.ConcurrentMap puts k new_data)
                map__9882 (if (seq? map__9882)
                            (clojure.lang.PersistentHashMap/create (seq map__9882))
                            map__9882)
                thunk (get map__9882 :thunk)]
            (if thunk
              (do (monitor/add-stat :ValcachePutInFlight 1) (deref thunk))
              (let [start (java.lang.System/nanoTime)]
                (deliver
                  new_thunk
                  (common/pfuture
                    pool
                    (fn fn__9883
                      ([]
                        (try
                          (when (^clojure.lang.IFn f)
                            (monitor/add-stat
                              :ValcachePutNsec
                              (long (- (java.lang.System/nanoTime) start))))
                          (finally (.remove ^java.util.Map puts k)))))))
                (deref new_thunk))))
          (do (monitor/add-stat :ValcachePutFailFull 1) nil))))
    (^void close [this] (do (.shutdown ^java.util.concurrent.ThreadPoolExecutor pool) nil)))
  (clojure.core/import 'datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl)
  (defn ->ValcachePutsPoolImpl
    ([limit puts pool] (datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl. limit puts pool)))
  (defn create-valcache-puts-pool
    ([p__9892]
      (let [map__9893 p__9892
            map__9893 (if (seq? map__9893)
                        (clojure.lang.PersistentHashMap/create (seq map__9893))
                        map__9893)
            limit (get map__9893 :limit)
            threads (get map__9893 :threads)
            idx (atom 0)
            exec (java.util.concurrent.ThreadPoolExecutor.
                   (int threads)
                   (int threads)
                   0
                   TimeUnit/MILLISECONDS
                   (java.util.concurrent.LinkedBlockingQueue.)
                   (reify
                     java.util.concurrent.ThreadFactory
                     (^java.lang.Thread newThread
                       [this ^java.lang.Runnable runnable]
                       (let [G__9895 (java.lang.Thread.
                                       ^java.lang.Runnable runnable
                                       (str (gensym "valcache-direct-") (swap! idx inc)))]
                         (.setDaemon ^java.lang.Thread G__9895 (boolean (.booleanValue true)))
                         G__9895))))
            puts (java.util.concurrent.ConcurrentHashMap.)]
        (datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl. limit puts exec)))
    ([]
      (create-valcache-puts-pool
        {:limit 1000, :threads (config/property "datomic.valcachePutsPool")})))
  (defonce valcache-puts-pool (delay (create-valcache-puts-pool))))