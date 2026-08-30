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
    fn__17738
    ([p__17737]
      (let [map__17739 p__17737
            map__17739 (if (seq? map__17739)
                         (if (next map__17739)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17739))
                           (if (seq map__17739) (first map__17739) {}))
                         map__17739)
            v (get map__17739 :v)]
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
          (let [map__17745 temp__5804__auto__
                map__17745 (if (seq? map__17745)
                             (if (next map__17745)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__17745))
                               (if (seq map__17745) (first map__17745) {}))
                             map__17745)
                data (get map__17745 :data)
                thunk (get map__17745 :thunk)]
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
                map__17742 (.putIfAbsent ^java.util.concurrent.ConcurrentMap puts k new_data)
                map__17742 (if (seq? map__17742)
                             (if (next map__17742)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__17742))
                               (if (seq map__17742) (first map__17742) {}))
                             map__17742)
                thunk (get map__17742 :thunk)]
            (if thunk
              (do (monitor/add-stat :ValcachePutInFlight 1) (deref thunk))
              (let [start (java.lang.System/nanoTime)]
                (deliver
                  new_thunk
                  (common/pfuture
                    pool
                    (fn fn__17743
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
    ([p__17752]
      (let [map__17753 p__17752
            map__17753 (if (seq? map__17753)
                         (if (next map__17753)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17753))
                           (if (seq map__17753) (first map__17753) {}))
                         map__17753)
            limit (get map__17753 :limit)
            threads (get map__17753 :threads)
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
                       (let [G__17755 (java.lang.Thread.
                                        ^java.lang.Runnable runnable
                                        (str (gensym "valcache-direct-") (swap! idx inc)))]
                         (.setDaemon ^java.lang.Thread G__17755 (boolean (.booleanValue true)))
                         G__17755))))
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
  (let [v__6812__auto__ #'valcache-puts-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.valcache.puts-pool-impl" "valcache-puts-pool")
        {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.valcache.puts-pool-impl" "valcache-puts-pool")
        (delay (create-valcache-puts-pool)))
      #'valcache-puts-pool)))