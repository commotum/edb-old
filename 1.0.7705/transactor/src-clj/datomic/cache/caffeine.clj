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
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol CacheGet (cache-get [_ k]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cache.caffeine" "CacheGet")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'CacheGet :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'cache-get
                                        {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cache.caffeine" "CacheGet"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cache.caffeine" "cache-get")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
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
      {:arglists (clojure.core/list ['cache]), :column (int 1)}
      :name
      '->WrappedCCache
      :ns
      *ns*))
  (def adapt-caffeine-cache (fn adapt_caffeine_cache ([cache] (->WrappedCCache cache))))
  (reset-meta!
    #'adapt-caffeine-cache
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'cache {:tag 'Cache})]),
       :column (int 1)}
      :name
      'adapt-caffeine-cache
      :ns
      *ns*))
  (def create-write-limited
   (fn create_write_limited
     ([^long num_entries ^long timeout_minutes]
       (adapt-caffeine-cache
         (.build
           (.maximumSize
             (.expireAfterWrite (Caffeine/newBuilder) (long timeout_minutes) TimeUnit/MINUTES)
             (long num_entries)))))
     ([^long num_entries]
       (adapt-caffeine-cache (.build (.maximumSize (Caffeine/newBuilder) (long num_entries)))))))
  (reset-meta!
    #'create-write-limited
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'num-entries {:tag 'long})]
         [(.withMeta 'num-entries {:tag 'long}) (.withMeta 'timeout-minutes {:tag 'long})]),
       :column (int 1)}
      :name
      'create-write-limited
      :ns
      *ns*))
  (def create-limited
   (fn create_limited
     ([^long num_entries ^long timeout_minutes]
       (adapt-caffeine-cache
         (.build
           (.maximumSize
             (.expireAfterAccess (Caffeine/newBuilder) (long timeout_minutes) TimeUnit/MINUTES)
             (long num_entries)))))
     ([^long num_entries]
       (adapt-caffeine-cache (.build (.maximumSize (Caffeine/newBuilder) (long num_entries)))))))
  (reset-meta!
    #'create-limited
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'num-entries {:tag 'long})]
         [(.withMeta 'num-entries {:tag 'long}) (.withMeta 'timeout-minutes {:tag 'long})]),
       :column (int 1)}
      :name
      'create-limited
      :ns
      *ns*))
  (def create-soft-limited
   (fn create_soft_limited
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
     ([] (adapt-caffeine-cache (.build (.softValues (Caffeine/newBuilder)))))))
  (reset-meta!
    #'create-soft-limited
    (assoc
      {:arglists
       (clojure.core/list
         []
         [(.withMeta 'num-entries {:tag 'long})]
         [(.withMeta 'num-entries {:tag 'long}) (.withMeta 'timeout-minutes {:tag 'long})]),
       :column (int 1)}
      :name
      'create-soft-limited
      :ns
      *ns*))
  (defn create-weight-limited
    ([weight f]
      (when-not (< 0 weight (java.lang.Integer/valueOf (int java.lang.Integer/MAX_VALUE)))
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list '< 0 'weight 'Integer/MAX_VALUE))))))
      ((.maximumWeight (Caffeine/newBuilder) (long ^java.lang.Number weight))
        (.build
          (.weigher
            (reify
              com.github.benmanes.caffeine.cache.Weigher
              (^int weigh [this k v] (.intValue (^clojure.lang.IFn f k v))))
            (if (instance?
                  clojure.lang.IFn
                  (reify
                    com.github.benmanes.caffeine.cache.Weigher
                    (^int weigh [this k v] (.intValue (^clojure.lang.IFn f k v)))))
              (instance?
                com.github.benmanes.caffeine.cache.Weigher
                (reify
                  com.github.benmanes.caffeine.cache.Weigher
                  (^int weigh [this k v] (.intValue (^clojure.lang.IFn f k v)))))
              (reify
                com.github.benmanes.caffeine.cache.Weigher
                (^int weigh [this k v] (.intValue (^clojure.lang.IFn f k v))))))))))
  (reset-meta!
    #'create-weight-limited
    (assoc
      {:arglists (clojure.core/list ['weight 'f]), :column (int 1)}
      :name
      'create-weight-limited
      :ns
      *ns*))
  (defn create-scaled-weight-limited
    ([weight f divisor]
      (let [scaled_weight (long (java.lang.Math/ceil (double (/ weight divisor))))
            scaled_f (fn scaled_f
                       ([k v]
                         (long
                           (java.lang.Math/ceil (double (/ (^clojure.lang.IFn f k v) divisor))))))]
        (create-weight-limited (long scaled_weight) scaled_f))))
  (reset-meta!
    #'create-scaled-weight-limited
    (assoc
      {:arglists (clojure.core/list ['weight 'f 'divisor]), :column (int 1)}
      :name
      'create-scaled-weight-limited
      :ns
      *ns*))
  (def create-computing
   (fn create_computing
     ([f max_size]
       ((.maximumSize (Caffeine/newBuilder) (long ^java.lang.Number max_size))
         (.build
           (reify
             com.github.benmanes.caffeine.cache.CacheLoader
             (load [this k] (^clojure.lang.IFn f k)))
           (if (instance?
                 clojure.lang.IFn
                 (reify
                   com.github.benmanes.caffeine.cache.CacheLoader
                   (load [this k] (^clojure.lang.IFn f k))))
             (instance?
               com.github.benmanes.caffeine.cache.CacheLoader
               (reify
                 com.github.benmanes.caffeine.cache.CacheLoader
                 (load [this k] (^clojure.lang.IFn f k))))
             (reify
               com.github.benmanes.caffeine.cache.CacheLoader
               (load [this k] (^clojure.lang.IFn f k)))))))))
  (reset-meta!
    #'create-computing
    (assoc
      {:arglists (clojure.core/list ['f 'max-size]), :column (int 1)}
      :name
      'create-computing
      :ns
      *ns*))
  (def create-response-map
   (fn create_response_map
     ([^long timeout_minutes]
       (adapt-caffeine-cache
         (.build
           (.expireAfterWrite (Caffeine/newBuilder) (long timeout_minutes) TimeUnit/MINUTES))))))
  (reset-meta!
    #'create-response-map
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'timeout-minutes {:tag 'long})]), :column (int 1)}
      :name
      'create-response-map
      :ns
      *ns*)))