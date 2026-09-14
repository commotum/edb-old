(do
  (clojure.core/in-ns 'datomic.cache)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.cache)
    {:doc
     "Composes immutable segment lookup caches, coordinates concurrent misses, performs read-ahead, and records I/O by cache tier. Segment identifiers name immutable values, so cached entries remain valid for their lifetime."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['remove])
      (clojure.core/require
        ['datomic.cache.caffeine :as 'caffeine]
        ['datomic.cache.impl :as 'impl]
        ['datomic.config :as 'config]
        ['datomic.common :as 'common]
        ['datomic.io :as 'dio]
        ['datomic.measure.io-stats :as 'io-stats]
        ['datomic.monitor :as 'monitor])
      (clojure.core/import 'java.lang.AutoCloseable)
      (clojure.core/import 'java.util.concurrent.ConcurrentMap)
      (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
      (clojure.core/import 'java.util.concurrent.ExecutorService)
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.cache 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cache))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['remove])
        (clojure.core/require
          ['datomic.cache.caffeine :as 'caffeine]
          ['datomic.cache.impl :as 'impl]
          ['datomic.config :as 'config]
          ['datomic.common :as 'common]
          ['datomic.io :as 'dio]
          ['datomic.measure.io-stats :as 'io-stats]
          ['datomic.monitor :as 'monitor])
        (clojure.core/import 'java.lang.AutoCloseable)
        (clojure.core/import 'java.util.concurrent.ConcurrentMap)
        (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
        (clojure.core/import 'java.util.concurrent.ExecutorService)
        (clojure.core/import 'java.nio.ByteBuffer))))
  (set! *warn-on-reflection* true)
  (.setMeta (clojure.lang.RT/var "datomic.cache" "fast-count") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "fast-count")
    (clojure.lang.RT/var "datomic.cache.impl" "fast-count"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "cache-keys") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "cache-keys")
    (clojure.lang.RT/var "datomic.cache.impl" "cache-keys"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "put") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "put")
    (clojure.lang.RT/var "datomic.cache.impl" "put"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "remove") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "remove")
    (clojure.lang.RT/var "datomic.cache.impl" "remove"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "clear") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "clear")
    (clojure.lang.RT/var "datomic.cache.impl" "clear"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "read-ahead-pool") {:column (int 1)})
  (let [v__6837__auto__ #'read-ahead-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.cache" "read-ahead-pool") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.cache" "read-ahead-pool")
        (delay
          (common/thread-pool
            {:nthreads (max (config/property "datomic.readAheadPool") 1), :name "read-ahead"})))
      #'read-ahead-pool))
  (definterface
    ICachedLookup
    (^java.lang.Object valAtUncached [^java.lang.Object arg0 ^java.lang.Object arg1])
    (^java.lang.Object getFromCache [^java.lang.Object arg0 ^java.lang.Object arg1]))
  (clojure.core/import 'datomic.cache.ICachedLookup)
  (defn get-from-cache
    ([m k nf]
      (if (instance? datomic.cache.ICachedLookup m)
        (.getFromCache ^datomic.cache.ICachedLookup m k nf)
        (get m k nf))))
  (reset-meta!
    #'get-from-cache
    (assoc
      {:arglists (clojure.core/list ['m 'k 'nf]), :column (int 1)}
      :name
      'get-from-cache
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.cache" "read-ahead-pool-prop")
    {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "read-ahead-pool-prop")
    (delay (config/property "datomic.readAheadPool")))
  ;; Schedules a background lookup only when read-ahead is enabled and the value is not cached.
  (defn read-ahead
    ([lookup k]
      (when (< 0 (deref read-ahead-pool-prop))
        (with-bindings*
          (io-stats/bare-bindings)
          (fn fn__9842
            ([]
              (when-not (get-from-cache lookup k nil)
                (let [f (bound-fn [] (common/getx lookup k))]
                  (.submit (deref read-ahead-pool) ^java.util.concurrent.Callable f)))))))))
  (reset-meta!
    #'read-ahead
    (assoc
      {:arglists (clojure.core/list ['lookup 'k]), :column (int 1)}
      :name
      'read-ahead
      :ns
      *ns*))
  (defn get-uncached
    ([m k nf]
      (if (instance? datomic.cache.ICachedLookup m)
        (.valAtUncached ^datomic.cache.ICachedLookup m k nf)
        (get m k nf))))
  (reset-meta!
    #'get-uncached
    (assoc
      {:arglists (clojure.core/list ['m 'k 'nf]), :column (int 1)}
      :name
      'get-uncached
      :ns
      *ns*))
  (defn getx-uncached
    ([m k]
      (if (instance? datomic.cache.ICachedLookup m)
        (let [e (.valAtUncached ^datomic.cache.ICachedLookup m k :datomic.cache/getx-sentinel-42)]
          (if (not (= e :datomic.cache/getx-sentinel-42))
            e
            (do (throw (java.lang.Exception. (str "Key not found: " k))) nil)))
        (common/getx m k))))
  (reset-meta!
    #'getx-uncached
    (assoc {:arglists (clojure.core/list ['m 'k]), :column (int 1)} :name 'getx-uncached :ns *ns*))
  (defn report-val-fn-fail
    ([t raw k]
      (let [desc (merge {:key k} (or (dio/describe-bbuf raw) {:class (class raw)}))]
        (throw (ex-info (str "Unable to convert data: " desc) desc t)))
      nil))
  (reset-meta!
    #'report-val-fn-fail
    (assoc
      {:arglists (clojure.core/list ['t 'raw 'k]), :column (int 1)}
      :name
      'report-val-fn-fail
      :ns
      *ns*))
  (defn lookup-transformer
    ([m & p__9851]
      (let [map__9852 p__9851
            map__9852 (if (seq? map__9852)
                        (if (next map__9852)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9852))
                          (if (seq map__9852) (first map__9852) {}))
                        map__9852)
            key_fn (get map__9852 :key-fn identity)
            val_fn (get map__9852 :val-fn identity)
            try_val_fn (fn try_val_fn
                         ([raw k]
                           (try
                             (^clojure.lang.IFn val_fn raw)
                             (catch java.lang.Throwable t (report-val-fn-fail t raw k)))))]
        (reify
          clojure.lang.ILookup
          datomic.cache.ICachedLookup
          (valAtUncached
            [this k not_found]
            (let [ret (get-uncached m (^clojure.lang.IFn key_fn k) not_found)]
              (if (= ret not_found) not_found (^clojure.lang.IFn try_val_fn ret k))))
          (getFromCache
            [this k not_found]
            (let [ret (get-from-cache m (^clojure.lang.IFn key_fn k) not_found)]
              (if (= ret not_found) not_found (^clojure.lang.IFn try_val_fn ret k))))
          (valAt
            [this k not_found]
            (let [ret (get m (^clojure.lang.IFn key_fn k) not_found)]
              (if (= ret not_found) not_found (^clojure.lang.IFn try_val_fn ret k))))
          (valAt [this k] (.valAt this k nil))))))
  (reset-meta!
    #'lookup-transformer
    (assoc
      {:arglists
       (clojure.core/list
         ['m '& {:keys ['key-fn 'val-fn], :or {'key-fn 'identity, 'val-fn 'identity}}]),
       :column (int 1)}
      :name
      'lookup-transformer
      :ns
      *ns*))
  (defn safe-lookup-transformer
    ([m & p__9858]
      (let [map__9859 p__9858
            map__9859 (if (seq? map__9859)
                        (if (next map__9859)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9859))
                          (if (seq map__9859) (first map__9859) {}))
                        map__9859)
            key_fn (get map__9859 :key-fn identity)
            val_fn (get map__9859 :val-fn identity)
            try_val_fn (fn try_val_fn
                         ([raw k not_found]
                           (try
                             (^clojure.lang.IFn val_fn (^clojure.lang.IFn key_fn k) raw not_found)
                             (catch java.lang.Throwable t (report-val-fn-fail t raw k)))))]
        (reify
          clojure.lang.ILookup
          (valAt
            [this k not_found]
            (let [ret (get m (^clojure.lang.IFn key_fn k) not_found)]
              (if (= ret not_found) not_found (^clojure.lang.IFn try_val_fn ret k not_found))))
          (valAt [this k] (.valAt this k nil))))))
  (reset-meta!
    #'safe-lookup-transformer
    (assoc
      {:arglists
       (clojure.core/list
         ['m '& {:keys ['key-fn 'val-fn], :or {'key-fn 'identity, 'val-fn 'identity}}]),
       :column (int 1)}
      :name
      'safe-lookup-transformer
      :ns
      *ns*))
  ;; Wraps a backing lookup with a read-through cache and an optional hit/miss observer.
  (defn lookup-cache
    ([m cache f]
      (reify
        datomic.cache.impl.FastCount
        datomic.cache.impl.CachePut
        clojure.lang.ILookup
        datomic.cache.impl.CacheRemove
        datomic.cache.ICachedLookup
        (valAtUncached
          [this k not_found]
          (let [ret (get cache k)]
            (when f (^clojure.lang.IFn f k (if ret :hit :miss)))
            (if ret ret (get m k not_found))))
        (getFromCache
          [this k not_found]
          (let [ret (get cache k)]
            (when f (^clojure.lang.IFn f k (if ret :hit :miss)))
            (if ret ret not_found)))
        (valAt
          [this k not_found]
          (let [ret (get cache k)]
            (when f (^clojure.lang.IFn f k (if ret :hit :miss)))
            (if ret
              ret
              (let [v (get m k not_found)] (when-not (= v not_found) (put cache k v)) v))))
        (valAt [this k] (.valAt this k nil))
        (clear [this] (clear cache))
        (remove [this k] (remove cache k))
        (put [this k v] (put cache k v))
        (fast-count [this] (fast-count cache))))
    ([m cache] (lookup-cache m cache nil)))
  (reset-meta!
    #'lookup-cache
    (assoc
      {:arglists (clojure.core/list ['m 'cache] ['m 'cache 'f]), :column (int 1)}
      :name
      'lookup-cache
      :ns
      *ns*))
  ;; Coalesces concurrent misses for the same key so the backing lookup executes once.
  (defn lookup-with-inflight-cache
    ([m]
      (let [in_flight (java.util.concurrent.ConcurrentHashMap.)]
        (reify
          clojure.lang.ILookup
          (valAt
            [this k not_found]
            (let [thunk (delay
                          (try (get m k not_found) (finally (.remove ^java.util.Map in_flight k))))
                  temp__5823__auto__ (.putIfAbsent
                                       ^java.util.concurrent.ConcurrentMap in_flight
                                       k
                                       thunk)]
              (if temp__5823__auto__
                (let [existing temp__5823__auto__
                      now (java.lang.System/nanoTime)
                      ret (deref existing)]
                  (io-stats/inc! :inflight-lookup-ns (- (java.lang.System/nanoTime) now))
                  ret)
                (deref thunk))))
          (valAt [this k] (.valAt this k nil))))))
  (reset-meta!
    #'lookup-with-inflight-cache
    (assoc
      {:arglists (clojure.core/list ['m]), :column (int 1)}
      :name
      'lookup-with-inflight-cache
      :ns
      *ns*))
  (defn fn->lookup
    ([f]
      (reify
        clojure.lang.ILookup
        (valAt [this k _] (^clojure.lang.IFn f k))
        (valAt [this k] (^clojure.lang.IFn f k)))))
  (reset-meta!
    #'fn->lookup
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name 'fn->lookup :ns *ns*))
  ;; Reads from the first lookup and falls through to the second only when the key is absent.
  (defn double-lookup
    ([m1 m2]
      (reify
        clojure.lang.ILookup
        (valAt
          [this k not_found]
          (let [ret (get m1 k not_found)] (if (= ret not_found) (get m2 k not_found) ret)))
        (valAt [this k] (.valAt this k nil)))))
  (reset-meta!
    #'double-lookup
    (assoc
      {:arglists (clojure.core/list ['m1 'm2]), :column (int 1)}
      :name
      'double-lookup
      :ns
      *ns*))
  ;; Reads through two cache tiers, repairs the first tier from the second, and writes both tiers.
  (defn repairing-cache-stack
    ([p__9880]
      (let [map__9881 p__9880
            map__9881 (if (seq? map__9881)
                        (if (next map__9881)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9881))
                          (if (seq map__9881) (first map__9881) {}))
                        map__9881)
            cache_1 (get map__9881 :cache-1)
            cache_2 (get map__9881 :cache-2)
            close_cache_1? (get map__9881 :close-cache-1? true)
            close_cache_2? (get map__9881 :close-cache-2? true)
            on_repair (get map__9881 :on-repair)]
        (reify
          datomic.cache.impl.CachePut
          clojure.lang.ILookup
          java.lang.AutoCloseable
          (valAt
            [this k not_found]
            (let [v (get cache_1 k not_found)]
              (if (= v not_found)
                (let [v (get cache_2 k not_found)]
                  (when-not (= v not_found) (put cache_1 k v) (^clojure.lang.IFn on_repair k))
                  v)
                v)))
          (valAt [this k] (.valAt this k nil))
          (put [this k v] (do (put cache_1 k v) (put cache_2 k v)))
          (^void close
            [this]
            (do
              (when close_cache_1? (.close ^java.lang.AutoCloseable cache_1))
              (when close_cache_2? (.close ^java.lang.AutoCloseable cache_2) nil)
              nil))))))
  (reset-meta!
    #'repairing-cache-stack
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys
           [(.withMeta 'cache-1 {:tag 'AutoCloseable})
            (.withMeta 'cache-2 {:tag 'AutoCloseable})
            'close-cache-1?
            'close-cache-2?
            'on-repair],
           :or {'close-cache-1? true, 'close-cache-2? true}}]),
       :column (int 1)}
      :name
      'repairing-cache-stack
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "create-write-limited") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "create-write-limited")
    (clojure.lang.RT/var "datomic.cache.caffeine" "create-write-limited"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "create-limited") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "create-limited")
    (clojure.lang.RT/var "datomic.cache.caffeine" "create-limited"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "create-soft-limited") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "create-soft-limited")
    (clojure.lang.RT/var "datomic.cache.caffeine" "create-soft-limited"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "create-scaled-weight-limited") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "create-scaled-weight-limited")
    (clojure.lang.RT/var "datomic.cache.caffeine" "create-scaled-weight-limited"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "create-computing") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "create-computing")
    (clojure.lang.RT/var "datomic.cache.caffeine" "create-computing"))
  (.setMeta (clojure.lang.RT/var "datomic.cache" "create-response-map") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cache" "create-response-map")
    (clojure.lang.RT/var "datomic.cache.caffeine" "create-response-map")))
