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
    fn__20714
    ([p__20713]
      (let [map__20715 p__20713
            map__20715 (if (seq? map__20715)
                         (if (next map__20715)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20715))
                           (if (seq map__20715) (first map__20715) {}))
                         map__20715)
            v (get map__20715 :v)]
        (monitor/add-stat :ValcacheGetInFlight 1)
        (.duplicate ^java.nio.ByteBuffer v))))
  (deftype
    ValcachePutsPoolImpl
    [limit puts pool]
    datomic.valcache.puts_pool.PutsPool
    java.lang.AutoCloseable
    (get-queued-put
      [this k]
      (let [temp__5804__auto__ (get puts k)]
        (when temp__5804__auto__
          (let [map__20721 temp__5804__auto__
                map__20721 (if (seq? map__20721)
                             (if (next map__20721)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__20721))
                               (if (seq map__20721) (first map__20721) {}))
                             map__20721)
                data (get map__20721 :data)
                thunk (get map__20721 :thunk)]
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
                map__20718 (.putIfAbsent ^java.util.concurrent.ConcurrentMap puts k new_data)
                map__20718 (if (seq? map__20718)
                             (if (next map__20718)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__20718))
                               (if (seq map__20718) (first map__20718) {}))
                             map__20718)
                thunk (get map__20718 :thunk)]
            (if thunk
              (do (monitor/add-stat :ValcachePutInFlight 1) (deref thunk))
              (let [start (java.lang.System/nanoTime)]
                (deliver
                  new_thunk
                  (common/pfuture
                    pool
                    (fn fn__20719
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
    ([p__20728]
      (let [map__20729 p__20728
            map__20729 (if (seq? map__20729)
                         (if (next map__20729)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20729))
                           (if (seq map__20729) (first map__20729) {}))
                         map__20729)
            limit (get map__20729 :limit)
            threads (get map__20729 :threads)
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
                       (let [G__20731 (java.lang.Thread.
                                        ^java.lang.Runnable runnable
                                        (str (gensym "valcache-direct-") (swap! idx inc)))]
                         (.setDaemon ^java.lang.Thread G__20731 (boolean (.booleanValue true)))
                         G__20731))))
            puts (java.util.concurrent.ConcurrentHashMap.)]
        (datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl. limit puts exec)))
    ([]
      (create-valcache-puts-pool
        {:limit 1000, :threads (config/property "datomic.valcachePutsPool")})))
  (defonce valcache-puts-pool (delay (create-valcache-puts-pool))))