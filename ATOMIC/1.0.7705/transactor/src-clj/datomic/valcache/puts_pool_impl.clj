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
    fn__20632
    ([p__20631]
      (let [map__20633 p__20631
            map__20633 (if (seq? map__20633)
                         (if (next map__20633)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20633))
                           (if (seq map__20633) (first map__20633) {}))
                         map__20633)
            v (get map__20633 :v)]
        (monitor/add-stat :ValcacheGetInFlight 1)
        (.duplicate ^java.nio.ByteBuffer v))))
  (deftype
    ValcachePutsPoolImpl
    [limit puts pool]
    datomic.valcache.puts_pool.PutsPool
    java.lang.AutoCloseable
    (get-queued-put
      [this k]
      (let [temp__5825__auto__ (get puts k)]
        (when temp__5825__auto__
          (let [map__20639 temp__5825__auto__
                map__20639 (if (seq? map__20639)
                             (if (next map__20639)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__20639))
                               (if (seq map__20639) (first map__20639) {}))
                             map__20639)
                data (get map__20639 :data)
                thunk (get map__20639 :thunk)]
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
                map__20636 (.putIfAbsent ^java.util.concurrent.ConcurrentMap puts k new_data)
                map__20636 (if (seq? map__20636)
                             (if (next map__20636)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__20636))
                               (if (seq map__20636) (first map__20636) {}))
                             map__20636)
                thunk (get map__20636 :thunk)]
            (if thunk
              (do (monitor/add-stat :ValcachePutInFlight 1) (deref thunk))
              (let [start (java.lang.System/nanoTime)]
                (deliver
                  new_thunk
                  (common/pfuture
                    pool
                    (fn fn__20637
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
  (reset-meta!
    #'->ValcachePutsPoolImpl
    (assoc
      {:arglists (clojure.core/list ['limit 'puts 'pool]), :column (int 1)}
      :name
      '->ValcachePutsPoolImpl
      :ns
      *ns*))
  (defn create-valcache-puts-pool
    ([p__20646]
      (let [map__20647 p__20646
            map__20647 (if (seq? map__20647)
                         (if (next map__20647)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20647))
                           (if (seq map__20647) (first map__20647) {}))
                         map__20647)
            limit (get map__20647 :limit)
            threads (get map__20647 :threads)
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
                       (let [G__20649 (java.lang.Thread.
                                        ^java.lang.Runnable runnable
                                        (str (gensym "valcache-direct-") (swap! idx inc)))]
                         (.setDaemon ^java.lang.Thread G__20649 (boolean (.booleanValue true)))
                         G__20649))))
            puts (java.util.concurrent.ConcurrentHashMap.)]
        (datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl. limit puts exec)))
    ([]
      (create-valcache-puts-pool
        {:limit 1000, :threads (config/property "datomic.valcachePutsPool")})))
  (reset-meta!
    #'create-valcache-puts-pool
    (assoc
      {:arglists (clojure.core/list [] [{:keys ['limit 'threads]}]), :column (int 1)}
      :name
      'create-valcache-puts-pool
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.valcache.puts-pool-impl" "valcache-puts-pool")
    {:column (int 1)})
  (let [v__6837__auto__ #'valcache-puts-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.valcache.puts-pool-impl" "valcache-puts-pool")
        {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.valcache.puts-pool-impl" "valcache-puts-pool")
        (delay (create-valcache-puts-pool)))
      #'valcache-puts-pool)))