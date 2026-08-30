(do
  (clojure.core/in-ns 'datomic.cache)
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
  (def fast-count (clojure.lang.RT/var "datomic.cache.impl" "fast-count"))
  (def cache-keys (clojure.lang.RT/var "datomic.cache.impl" "cache-keys"))
  (def put (clojure.lang.RT/var "datomic.cache.impl" "put"))
  (def remove (clojure.lang.RT/var "datomic.cache.impl" "remove"))
  (def clear (clojure.lang.RT/var "datomic.cache.impl" "clear"))
  (defonce read-ahead-pool
   (delay
     (common/thread-pool
       {:nthreads (max (config/property "datomic.readAheadPool") 1), :name "read-ahead"})))
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
  (def read-ahead-pool-prop (delay (config/property "datomic.readAheadPool")))
  (reset-meta!
    #'read-ahead-pool-prop
    (assoc {:private true, :column 1} :name 'read-ahead-pool-prop :ns *ns*))
  (defn read-ahead
    ([lookup k]
      (when (< 0 (deref read-ahead-pool-prop))
        (with-bindings*
          (io-stats/bare-bindings)
          (fn fn__9384
            ([]
              (when-not (get-from-cache lookup k nil)
                (let [f (bound-fn [] (common/getx lookup k))]
                  (.submit (deref read-ahead-pool) ^java.util.concurrent.Callable f)))))))))
  (defn get-uncached
    ([m k nf]
      (if (instance? datomic.cache.ICachedLookup m)
        (.valAtUncached ^datomic.cache.ICachedLookup m k nf)
        (get m k nf))))
  (defn getx-uncached
    ([m k]
      (if (instance? datomic.cache.ICachedLookup m)
        (let [e (.valAtUncached ^datomic.cache.ICachedLookup m k :datomic.cache/getx-sentinel-42)]
          (if (not (= e :datomic.cache/getx-sentinel-42))
            e
            (do (throw (java.lang.Exception. (str "Key not found: " k))) nil)))
        (common/getx m k))))
  (defn report-val-fn-fail
    ([t raw k]
      (let [desc (merge {:key k} (or (dio/describe-bbuf raw) {:class (class raw)}))]
        (throw (ex-info (str "Unable to convert data: " desc) desc t)))
      nil))
  (defn lookup-transformer
    ([m & p__9393]
      (let [map__9394 p__9393
            map__9394 (if (seq? map__9394)
                        (clojure.lang.PersistentHashMap/create (seq map__9394))
                        map__9394)
            key_fn (get map__9394 :key-fn identity)
            val_fn (get map__9394 :val-fn identity)
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
  (defn safe-lookup-transformer
    ([m & p__9400]
      (let [map__9401 p__9400
            map__9401 (if (seq? map__9401)
                        (clojure.lang.PersistentHashMap/create (seq map__9401))
                        map__9401)
            key_fn (get map__9401 :key-fn identity)
            val_fn (get map__9401 :val-fn identity)
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
  (defn lookup-with-inflight-cache
    ([m]
      (let [in_flight (java.util.concurrent.ConcurrentHashMap.)]
        (reify
          clojure.lang.ILookup
          (valAt
            [this k not_found]
            (let [thunk (delay
                          (try (get m k not_found) (finally (.remove ^java.util.Map in_flight k))))
                  temp__5455__auto__ (.putIfAbsent
                                       ^java.util.concurrent.ConcurrentMap in_flight
                                       k
                                       thunk)]
              (if temp__5455__auto__
                (let [existing temp__5455__auto__
                      now (java.lang.System/nanoTime)
                      ret (deref existing)]
                  (io-stats/inc! :inflight-lookup-ns (- (java.lang.System/nanoTime) now))
                  ret)
                (deref thunk))))
          (valAt [this k] (.valAt this k nil))))))
  (defn fn->lookup
    ([f]
      (reify
        clojure.lang.ILookup
        (valAt [this k _] (^clojure.lang.IFn f k))
        (valAt [this k] (^clojure.lang.IFn f k)))))
  (defn double-lookup
    ([m1 m2]
      (reify
        clojure.lang.ILookup
        (valAt
          [this k not_found]
          (let [ret (get m1 k not_found)] (if (= ret not_found) (get m2 k not_found) ret)))
        (valAt [this k] (.valAt this k nil)))))
  (defn repairing-cache-stack
    ([p__9422]
      (let [map__9423 p__9422
            map__9423 (if (seq? map__9423)
                        (clojure.lang.PersistentHashMap/create (seq map__9423))
                        map__9423)
            cache_1 (get map__9423 :cache-1)
            cache_2 (get map__9423 :cache-2)
            close_cache_1? (get map__9423 :close-cache-1? true)
            close_cache_2? (get map__9423 :close-cache-2? true)
            on_repair (get map__9423 :on-repair)]
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
              (when close_cache_1? (.close ^java.lang.AutoCloseable cache_1) nil)
              (when close_cache_2? (.close ^java.lang.AutoCloseable cache_2) nil)
              nil))))))
  (def create-write-limited (clojure.lang.RT/var "datomic.cache.caffeine" "create-write-limited"))
  (def create-limited (clojure.lang.RT/var "datomic.cache.caffeine" "create-limited"))
  (def create-soft-limited (clojure.lang.RT/var "datomic.cache.caffeine" "create-soft-limited"))
  (def create-scaled-weight-limited
   (clojure.lang.RT/var "datomic.cache.caffeine" "create-scaled-weight-limited"))
  (def create-computing (clojure.lang.RT/var "datomic.cache.caffeine" "create-computing"))
  (def create-response-map (clojure.lang.RT/var "datomic.cache.caffeine" "create-response-map")))