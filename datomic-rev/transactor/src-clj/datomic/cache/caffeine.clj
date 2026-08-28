(do
  (clojure.core/in-ns 'datomic.cache.caffeine)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.cache.impl :as 'impl]
        ['datomic.memory-size :as 'size]
        ['datomic.monitor :as 'monitor])
      (clojure.core/import 'com.github.benmanes.caffeine.cache.Cache)
      (clojure.core/import 'com.github.benmanes.caffeine.cache.CacheLoader)
      (clojure.core/import 'com.github.benmanes.caffeine.cache.Caffeine)
      (clojure.core/import 'com.github.benmanes.caffeine.cache.LoadingCache)
      (clojure.core/import 'com.github.benmanes.caffeine.cache.Weigher)
      (clojure.core/import 'java.util.concurrent.TimeUnit)))
  (when-not (.equals 'datomic.cache.caffeine 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cache.caffeine))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.cache.impl :as 'impl]
          ['datomic.memory-size :as 'size]
          ['datomic.monitor :as 'monitor])
        (clojure.core/import 'com.github.benmanes.caffeine.cache.Cache)
        (clojure.core/import 'com.github.benmanes.caffeine.cache.CacheLoader)
        (clojure.core/import 'com.github.benmanes.caffeine.cache.Caffeine)
        (clojure.core/import 'com.github.benmanes.caffeine.cache.LoadingCache)
        (clojure.core/import 'com.github.benmanes.caffeine.cache.Weigher)
        (clojure.core/import 'java.util.concurrent.TimeUnit))))
  (set! *warn-on-reflection* true)
  (extend
    com.github.benmanes.caffeine.cache.Cache
    impl/FastCount
    {:fast-count
     (fn fn__11805
       ([coll] (long (.estimatedSize ^com.github.benmanes.caffeine.cache.Cache coll))))})
  (extend
    com.github.benmanes.caffeine.cache.Cache
    impl/CachePut
    {:put (fn fn__11807 ([c k v] (.put ^com.github.benmanes.caffeine.cache.Cache c k v) nil))})
  (extend
    com.github.benmanes.caffeine.cache.Cache
    impl/CacheRemove
    {:remove
     (fn fn__11809
       ([c k]
         (let [v (.getIfPresent ^com.github.benmanes.caffeine.cache.Cache c k)]
           (.invalidate ^com.github.benmanes.caffeine.cache.Cache c k)
           v))),
     :clear (fn fn__11811 ([c] (.invalidateAll ^com.github.benmanes.caffeine.cache.Cache c) nil))})
  (def CAFFEINE_ENTRY_OVERHEAD 82)
  (reset-meta!
    #'CAFFEINE_ENTRY_OVERHEAD
    (assoc {:private true, :const true, :column 1} :name 'CAFFEINE_ENTRY_OVERHEAD :ns *ns*))
  (defonce CacheGet {})
  (defprotocol CacheGet (cache-get [_ k]))
  (extend
    com.github.benmanes.caffeine.cache.Cache
    CacheGet
    {:cache-get (fn fn__11831 ([this k] (.getIfPresent this k)))})
  (extend
    com.github.benmanes.caffeine.cache.LoadingCache
    CacheGet
    {:cache-get (fn fn__11833 ([this k] (.get this k)))})
  (deftype
    WrappedCCache
    [cache]
    datomic.cache.impl.FastCount
    datomic.cache.impl.CachePut
    clojure.lang.ILookup
    datomic.monitor.Metrics
    datomic.cache.impl.CacheRemove
    datomic.memory_size.MemorySize
    datomic.cache.impl.CacheKeys
    (metrics [this] {:ObjectCacheCount (impl/fast-count cache)})
    (memory-size
      [this]
      (reduce
        (fn fn__11836
          ([n k] (+ (+ (+ n 82) (size/memory-size k)) (size/memory-size (get this k)))))
        0
        (impl/cache-keys this)))
    (cache-keys [this] (keys (.asMap ^com.github.benmanes.caffeine.cache.Cache cache)))
    (fast-count [this] (impl/fast-count cache))
    (clear [this] (impl/clear cache))
    (remove [this k] (impl/remove cache k))
    (put [this k v] (impl/put cache k v))
    (valAt [this k nf] (let [v (cache-get cache k)] (if (nil? v) nf v)))
    (valAt [this k] (cache-get cache k)))
  (clojure.core/import 'datomic.cache.caffeine.WrappedCCache)
  (defn ->WrappedCCache ([cache] (datomic.cache.caffeine.WrappedCCache. cache)))
  (defn adapt-caffeine-cache ([cache] (->WrappedCCache cache)))
  (reset-meta!
    #'adapt-caffeine-cache
    (assoc
      {:private true, :arglists (clojure.core/list [(.withMeta 'cache {:tag 'Cache})]), :column 1}
      :name
      'adapt-caffeine-cache
      :ns
      *ns*))
  (defn create-write-limited
    ([^long num_entries ^long timeout_minutes]
      (adapt-caffeine-cache
        (.build
          (.maximumSize
            (.expireAfterWrite (Caffeine/newBuilder) (long timeout_minutes) TimeUnit/MINUTES)
            (long num_entries)))))
    ([^long num_entries]
      (adapt-caffeine-cache (.build (.maximumSize (Caffeine/newBuilder) (long num_entries))))))
  (defn create-limited
    ([^long num_entries ^long timeout_minutes]
      (adapt-caffeine-cache
        (.build
          (.maximumSize
            (.expireAfterAccess (Caffeine/newBuilder) (long timeout_minutes) TimeUnit/MINUTES)
            (long num_entries)))))
    ([^long num_entries]
      (adapt-caffeine-cache (.build (.maximumSize (Caffeine/newBuilder) (long num_entries))))))
  (defn create-soft-limited
    ([^long num_entries ^long timeout_minutes]
      (adapt-caffeine-cache
        (.build
          (.softValues
            (.maximumSize
              (.expireAfterAccess (Caffeine/newBuilder) (long timeout_minutes) TimeUnit/MINUTES)
              (long num_entries))))))
    ([^long num_entries]
      (adapt-caffeine-cache
        (.build (.softValues (.maximumSize (Caffeine/newBuilder) (long num_entries))))))
    ([] (adapt-caffeine-cache (.build (.softValues (Caffeine/newBuilder))))))
  (defn create-weight-limited
    ([weight f]
      (when-not (< 0 weight (java.lang.Integer/valueOf (int java.lang.Integer/MAX_VALUE)))
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list '< 0 'weight 'Integer/MAX_VALUE))))))
      (adapt-caffeine-cache
        (.build
          (.weigher
            (.maximumWeight (Caffeine/newBuilder) (long ^java.lang.Number weight))
            (reify
              com.github.benmanes.caffeine.cache.Weigher
              (^int weigh [this k v] (.intValue (^clojure.lang.IFn f k v)))))))))
  (defn create-scaled-weight-limited
    ([weight f divisor]
      (let [scaled_weight (long (java.lang.Math/ceil (double (/ weight divisor))))
            scaled_f (fn scaled_f
                       ([k v]
                         (long
                           (java.lang.Math/ceil (double (/ (^clojure.lang.IFn f k v) divisor))))))]
        (create-weight-limited (long scaled_weight) scaled_f))))
  (defn create-computing
    ([f max_size]
      (adapt-caffeine-cache
        (.build
          (.maximumSize (Caffeine/newBuilder) (long ^java.lang.Number max_size))
          (reify
            com.github.benmanes.caffeine.cache.CacheLoader
            (load [this k] (^clojure.lang.IFn f k)))))))
  (defn create-response-map
    ([^long timeout_minutes]
      (adapt-caffeine-cache
        (.build
          (.expireAfterWrite (Caffeine/newBuilder) (long timeout_minutes) TimeUnit/MINUTES))))))