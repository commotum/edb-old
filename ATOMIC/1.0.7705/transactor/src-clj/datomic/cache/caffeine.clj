(do
  (clojure.core/in-ns 'datomic.cache.caffeine)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.cache.caffeine)
    {:doc
     "Caffeine-backed object caches. Adapts native Cache and LoadingCache instances to Datomic's lookup, mutation, key-enumeration, metrics, and memory-size protocols, and provides constructors for size-, expiry-, soft-reference-, weight-, and computation-bounded caches."})
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
     (fn fn__9779
       ([coll] (long (.estimatedSize ^com.github.benmanes.caffeine.cache.Cache coll))))})
  (extend
    com.github.benmanes.caffeine.cache.Cache
    impl/CachePut
    {:put (fn fn__9781 ([c k v] (.put ^com.github.benmanes.caffeine.cache.Cache c k v) nil))})
  (extend
    com.github.benmanes.caffeine.cache.Cache
    impl/CacheRemove
    {:remove
     (fn fn__9783
       ([c k]
         (let [v (.getIfPresent ^com.github.benmanes.caffeine.cache.Cache c k)]
           (.invalidate ^com.github.benmanes.caffeine.cache.Cache c k)
           v))),
     :clear (fn fn__9785 ([c] (.invalidateAll ^com.github.benmanes.caffeine.cache.Cache c) nil))})
  (def CAFFEINE_ENTRY_OVERHEAD 82)
  (reset-meta!
    #'CAFFEINE_ENTRY_OVERHEAD
    (assoc {:private true, :const true, :column (int 1)} :name 'CAFFEINE_ENTRY_OVERHEAD :ns *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      CacheGet
      (cache-get [_ k] "Returns the cached value for k. Loading caches compute a missing value through their CacheLoader."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cache.caffeine" "CacheGet")
      (assoc
        (assoc protocol_metadata__7463 :doc "Lookup operation shared by plain and loading Caffeine caches.")
        :name
        'CacheGet
        :ns
        *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'cache-get
                                        {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc
                                      "Returns the cached value for k. Loading caches compute a missing value through their CacheLoader."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cache.caffeine" "CacheGet"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cache.caffeine" "cache-get")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (extend
    com.github.benmanes.caffeine.cache.Cache
    CacheGet
    {:cache-get (fn fn__9805 ([this k] (.getIfPresent this k)))})
  (extend
    com.github.benmanes.caffeine.cache.LoadingCache
    CacheGet
    {:cache-get (fn fn__9807 ([this k] (.get this k)))})
  (deftype
    WrappedCCache
    [cache]
    datomic.cache.impl.FastCount
    datomic.cache.impl.CachePut
    clojure.lang.ILookup
    datomic.monitor.Metrics
    datomic.cache.impl.CacheRemove
    datomic.memory_size.MemorySize_STAR_
    datomic.cache.impl.CacheKeys
    (metrics [this] {:ObjectCacheCount (impl/fast-count cache)})
    (memory-size*
      [this]
      (reduce
        (fn fn__9810 ([n k] (+ (+ (+ n 82) (size/memory-size k)) (size/memory-size (get this k)))))
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
  (reset-meta!
    #'->WrappedCCache
    (assoc
      {:arglists (clojure.core/list ['cache]),
       :doc
       "Wraps a Caffeine cache with Datomic cache protocols, Clojure lookup, object-count metrics, key enumeration, and estimated memory sizing. Memory estimates include 82 bytes of entry overhead plus measured key and value sizes.",
       :column (int 1)}
      :name
      '->WrappedCCache
      :ns
      *ns*))
  (defn adapt-caffeine-cache ([cache] (->WrappedCCache cache)))
  (reset-meta!
    #'adapt-caffeine-cache
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'cache {:tag 'Cache})]),
       :doc "Adapts a native Caffeine Cache to the Datomic cache interfaces.",
       :column (int 1)}
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
  (reset-meta!
    #'create-write-limited
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'num-entries {:tag 'long})]
         [(.withMeta 'num-entries {:tag 'long}) (.withMeta 'timeout-minutes {:tag 'long})]),
       :doc
       "Creates a cache bounded by num-entries. The two-argument form also expires an entry timeout-minutes after it is written, regardless of later reads.",
       :column (int 1)}
      :name
      'create-write-limited
      :ns
      *ns*))
  (defn create-limited
    ([^long num_entries ^long timeout_minutes]
      (adapt-caffeine-cache
        (.build
          (.maximumSize
            (.expireAfterAccess (Caffeine/newBuilder) (long timeout_minutes) TimeUnit/MINUTES)
            (long num_entries)))))
    ([^long num_entries]
      (adapt-caffeine-cache (.build (.maximumSize (Caffeine/newBuilder) (long num_entries))))))
  (reset-meta!
    #'create-limited
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'num-entries {:tag 'long})]
         [(.withMeta 'num-entries {:tag 'long}) (.withMeta 'timeout-minutes {:tag 'long})]),
       :doc
       "Creates a cache bounded by num-entries. The two-argument form expires an entry after timeout-minutes without access; reading or writing the entry resets its access deadline.",
       :column (int 1)}
      :name
      'create-limited
      :ns
      *ns*))
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
  (reset-meta!
    #'create-soft-limited
    (assoc
      {:arglists
       (clojure.core/list
         []
         [(.withMeta 'num-entries {:tag 'long})]
         [(.withMeta 'num-entries {:tag 'long}) (.withMeta 'timeout-minutes {:tag 'long})]),
       :doc
       "Creates a cache whose values are held by soft references. Optional arguments add a maximum entry count and expiration after timeout-minutes without access.",
       :column (int 1)}
      :name
      'create-soft-limited
      :ns
      *ns*))
  (defn create-weight-limited
    ([weight weigh-fn]
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
              (^int weigh [this k v] (.intValue (^clojure.lang.IFn weigh-fn k v)))))))))
  (reset-meta!
    #'create-weight-limited
    (assoc
      {:arglists (clojure.core/list ['weight 'weigh-fn]),
       :doc
       "Creates a cache whose total entry weight is bounded by weight. weigh-fn receives each key and value and must return a numeric entry weight convertible to int. The maximum weight must be greater than zero and less than Integer/MAX_VALUE.",
       :column (int 1)}
      :name
      'create-weight-limited
      :ns
      *ns*))
  (defn create-scaled-weight-limited
    ([weight weigh-fn divisor]
      (let [scaled-weight (long (java.lang.Math/ceil (double (/ weight divisor))))
            scaled-weigh-fn (fn scaled-weigh-fn
                              ([k v]
                                (long
                                  (java.lang.Math/ceil
                                    (double
                                      (/ (^clojure.lang.IFn weigh-fn k v) divisor))))))]
        (create-weight-limited (long scaled-weight) scaled-weigh-fn))))
  (reset-meta!
    #'create-scaled-weight-limited
    (assoc
      {:arglists (clojure.core/list ['weight 'weigh-fn 'divisor]),
       :doc
       "Creates a weight-bounded cache after dividing both the maximum and each computed entry weight by divisor and rounding each quotient upward. Scaling allows large raw sizes to fit the constructor's integer maximum-weight bound.",
       :column (int 1)}
      :name
      'create-scaled-weight-limited
      :ns
      *ns*))
  (defn create-computing
    ([load-fn max-size]
      (adapt-caffeine-cache
        (.build
          (.maximumSize (Caffeine/newBuilder) (long ^java.lang.Number max-size))
          (reify
            com.github.benmanes.caffeine.cache.CacheLoader
            (load [this k] (^clojure.lang.IFn load-fn k)))))))
  (reset-meta!
    #'create-computing
    (assoc
      {:arglists (clojure.core/list ['load-fn 'max-size]),
       :doc
       "Creates a loading cache bounded by max-size. A lookup for an absent key invokes load-fn through the Caffeine CacheLoader and caches the returned value.",
       :column (int 1)}
      :name
      'create-computing
      :ns
      *ns*))
  (defn create-response-map
    ([^long timeout_minutes]
      (adapt-caffeine-cache
        (.build
          (.expireAfterWrite (Caffeine/newBuilder) (long timeout_minutes) TimeUnit/MINUTES)))))
  (reset-meta!
    #'create-response-map
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'timeout-minutes {:tag 'long})]),
       :doc
       "Creates a response cache whose entries expire timeout-minutes after they are written.",
       :column (int 1)}
      :name
      'create-response-map
      :ns
      *ns*)))
