(do
  (clojure.core/in-ns 'datomic.domain)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.domain)
    {:doc
     "Domain storage lookups and object caching. Composes immutable value-store reads, Fressian deserialization, repair retries, in-flight request sharing, and the process-wide object cache used by database indexes and logs."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.java.io :as 'io]
        ['datomic.cache :as 'cache]
        ['datomic.cluster :as 'cluster]
        ['datomic.clusterfs :as 'clusterfs]
        ['datomic.config :as 'config]
        ['datomic.extension-resolver :as 'ext-resolver]
        ['datomic.fressian :as 'fressian]
        ['datomic.require :as 'req]
        ['datomic.log :as 'log]
        ['datomic.fulltext :as 'fulltext]
        ['datomic.garbage.fressian :as 'gf]
        ['datomic.slf4j :as 'logger]
        ['datomic.memory-size :as 'size]
        ['datomic.monitor :as 'monitor]
        ['datomic.summary :as 'summary]
        ['datomic.measure.io-stats :as 'io-stats]
        ['datomic.measure.io-trace :as 'io-trace])))
  (when-not (.equals 'datomic.domain 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.domain))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.java.io :as 'io]
          ['datomic.cache :as 'cache]
          ['datomic.cluster :as 'cluster]
          ['datomic.clusterfs :as 'clusterfs]
          ['datomic.config :as 'config]
          ['datomic.extension-resolver :as 'ext-resolver]
          ['datomic.fressian :as 'fressian]
          ['datomic.require :as 'req]
          ['datomic.log :as 'log]
          ['datomic.fulltext :as 'fulltext]
          ['datomic.garbage.fressian :as 'gf]
          ['datomic.slf4j :as 'logger]
          ['datomic.memory-size :as 'size]
          ['datomic.monitor :as 'monitor]
          ['datomic.summary :as 'summary]
          ['datomic.measure.io-stats :as 'io-stats]
          ['datomic.measure.io-trace :as 'io-trace]))))
  (set! *warn-on-reflection* true)
  (req/maybe-require 'datomic.coordination-ext)
  (size/extend-memory-size*
    datomic.index.TransposedData
    (fn datomic_index_TransposedData
      (^long [td]
        (+
          (+
            (+
              (+ 48 (size/memory-size (.-eas ^datomic.index.TransposedData td)))
              (size/memory-size (.-vs ^datomic.index.TransposedData td)))
            (size/memory-size (.-ts ^datomic.index.TransposedData td)))
          (size/memory-size (.-ops ^datomic.index.TransposedData td)))))
    datomic.index.RootNode
    (fn datomic_index_RootNode
      (^long [root]
        (+
          (+
            (+ 32 (size/memory-size (.-keydata ^datomic.index.RootNode root)))
            (size/memory-size (.-dirids ^datomic.index.RootNode root)))
          (* 8 (count (.-dirs ^datomic.index.RootNode root))))))
    datomic.index.DirNode
    (fn datomic_index_DirNode
      (^long [dir]
        (-> (+ 48 (size/memory-size (.-keydata ^datomic.index.DirNode dir)))
         (+ (size/memory-size (.-segids ^datomic.index.DirNode dir)))
         (+ (size/memory-size (.-offsets ^datomic.index.DirNode dir)))
         (+ (size/memory-size (.-counts ^datomic.index.DirNode dir)))
         (+ (* 8 (count (.-segs ^datomic.index.DirNode dir)))))))
    datomic.btset.BTSet
    (fn datomic_btset_BTSet
      (^long [d]
        (loop [size 0 iter (.seek ^datomic.btset.BTSet d)]
          (if iter
            (recur
              (+ size (size/memory-size (.get ^datomic.iter.Iter iter)))
              (.next ^datomic.iter.Iter iter))
            size))))
    datomic.db.Datum
    (fn datomic_db_Datum (^long [d] (+ 40 (size/memory-size (.v ^datomic.db.Datum d))))))
  (.setMeta (clojure.lang.RT/var "datomic.domain" "common-read-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.domain" "common-read-handlers")
    (merge log/read-handlers clusterfs/read-handlers fulltext/read-handlers gf/read-handlers))
  (defn uncached-lookup-factory
    "Creates an object lookup that reads immutable values directly from storage and deserializes them with the handlers shared by logs, indexes, fulltext data, and cluster files."
    ([cluster]
      (cache/lookup-transformer
        (cluster/uncached-val-lookup cluster)
        :key-fn
        cluster/uuid->val-key
        :val-fn
        (fressian/val->obj common-read-handlers))))
  (reset-meta!
    #'uncached-lookup-factory
    (assoc
      {:arglists (clojure.core/list ['cluster]),
       :doc
       "Creates an object lookup that reads immutable values directly from storage and deserializes them with the handlers shared by logs, indexes, fulltext data, and cluster files.",
       :column (int 1)}
      :name
      'uncached-lookup-factory
      :ns
      *ns*))
  (defn create-object-cache
    "Creates the weighted in-process cache for deserialized immutable storage values. The cache reserves headroom and accounts for the measured size of both keys and values."
    ([cache-bytes]
      (do
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.domain")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process {:event :cache/create, :cache-bytes cache-bytes})))
          nil)
        (cache/create-scaled-weight-limited
          (long (* 0.9 cache-bytes))
          (fn fn__17300 ([k v] (long (+ (size/memory-size k) (size/memory-size v)))))
          1000)))
    ([] (create-object-cache (config/property "datomic.objectCacheMax"))))
  (reset-meta!
    #'create-object-cache
    (assoc
      {:arglists (clojure.core/list [] ['cache-bytes]),
       :doc
       "Creates the weighted in-process cache for deserialized immutable storage values. The cache reserves headroom and accounts for the measured size of both keys and values.",
       :column (int 1)}
      :name
      'create-object-cache
      :ns
      *ns*))
  (deftype
    ValcachePoller
    [cluster valcache_group_config server_specs_ref timer]
    datomic.summary.Summary
    clojure.lang.IDeref
    java.lang.AutoCloseable
    (summary
      [this]
      (assoc
        valcache_group_config
        :server-specs
        (logger/redact (deref server_specs_ref) #{:password})))
    (deref [this] (deref server_specs_ref))
    (^void close [this] (do (.close ^java.lang.AutoCloseable timer) nil)))
  (clojure.core/import 'datomic.domain.ValcachePoller)
  (defn ->ValcachePoller
    ([cluster valcache_group_config server_specs_ref timer]
      (datomic.domain.ValcachePoller. cluster valcache_group_config server_specs_ref timer)))
  (reset-meta!
    #'->ValcachePoller
    (assoc
      {:arglists (clojure.core/list ['cluster 'valcache-group-config 'server-specs-ref 'timer]),
       :column (int 1)}
      :name
      '->ValcachePoller
      :ns
      *ns*))
  (defmethod
    print-method
    datomic.domain.ValcachePoller
    fn__17308
    ([o w]
      (summary/write w (str (assoc (summary/summary o) :type datomic.domain.ValcachePoller)))))
  (defmethod print-dup datomic.domain.ValcachePoller fn__17310 ([o w] (print-method o w)))
  (.setMeta (clojure.lang.RT/var "datomic.domain" "cache-delay") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.domain" "cache-delay") (delay (create-object-cache)))
  (defn system-cache
    "Returns the lazily initialized process-wide object cache."
    ([] (deref cache-delay)))
  (reset-meta!
    #'system-cache
    (assoc
      {:arglists (clojure.core/list []),
       :doc "Returns the lazily initialized process-wide object cache.",
       :column (int 1)}
      :name
      'system-cache
      :ns
      *ns*))
  (defn peer-object-lookup
    "Creates a peer lookup that shares concurrent loads, deserializes stored values, caches the resulting immutable objects, and records cache and segment-load statistics."
    ([val_lookup read_lookup object_cache]
      (let [load_counter (fn load_counter ([ctr] (keyword (str (name ctr) "-load"))))]
        (cache/lookup-cache
          (cache/lookup-with-inflight-cache
            (cache/lookup-transformer
              val_lookup
              :key-fn
              cluster/uuid->val-key
              :val-fn
              (fressian/val->obj read_lookup)))
          object_cache
          (fn fn__17317
            ([k h_or_m]
              (io-stats/inc! :ocache)
              (let [index_counter io-stats/*io-index*
                    seg_type_counter io-stats/*io-seg-type*
                    leaf? (and (not= seg_type_counter :dir) index_counter)]
                (when leaf? (io-stats/inc! index_counter))
                (when (and seg_type_counter (not= seg_type_counter :dir))
                  (io-stats/inc! seg_type_counter))
                (when (= :miss h_or_m)
                  (cond
                    leaf? (io-stats/inc! (^clojure.lang.IFn load_counter index_counter))
                    seg_type_counter (do
                                       (when (= seg_type_counter :dir)
                                         (monitor/add-stat :DirLoads 1))
                                       (io-stats/inc!
                                         (^clojure.lang.IFn load_counter seg_type_counter))))))
              (monitor/add-stat
                :ObjectCache
                (let [G__17318 h_or_m] (case G__17318 :miss 0 :hit 1))))))))
    ([val_lookup read_lookup] (peer-object-lookup val_lookup read_lookup (system-cache))))
  (reset-meta!
    #'peer-object-lookup
    (assoc
      {:arglists
       (clojure.core/list ['val-lookup 'read-lookup] ['val-lookup 'read-lookup 'object-cache]),
       :doc
       "Creates a peer lookup that shares concurrent loads, deserializes stored values, caches the resulting immutable objects, and records cache and segment-load statistics.",
       :column (int 1)}
      :name
      'peer-object-lookup
      :ns
      *ns*))
  (defn preload-extension-resolver!
    ([]
      (when (io/resource ext-resolver/config-resource)
        (ext-resolver/preload! ext-resolver/config-resource)
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.domain")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process {:event :extension-resolver/preload!})))
          nil))))
  (reset-meta!
    #'preload-extension-resolver!
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'preload-extension-resolver!
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.domain" "defressian") {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.domain" "defressian")
    (fressian/val->obj common-read-handlers))
  (defn deserialize ([m] (defressian (:buf m))))
  (reset-meta!
    #'deserialize
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'deserialize :ns *ns*))
  (deftype
    DeserializingRepairingLookup
    [cluster]
    clojure.lang.ILookup
    (valAt
      [this k not_found]
      (let [k (str k) v (deref (cluster/get-val cluster k))]
        (when (:buf v)
          (try
            (deserialize v)
            (catch
              java.lang.Throwable
              t
              (let [v (deref
                        (cluster/get-val2
                          cluster
                          k
                          #:datomic.core2.val-store.opts{:reset-cache true}))]
                (if (:buf v)
                  (try
                    (deserialize v)
                    (catch java.lang.Throwable t (cache/report-val-fn-fail t (:buf v) k)))
                  not_found)))))))
    (valAt [this k] (.valAt this k nil)))
  (clojure.core/import 'datomic.domain.DeserializingRepairingLookup)
  (defn ->DeserializingRepairingLookup
    ([cluster] (datomic.domain.DeserializingRepairingLookup. cluster)))
  (reset-meta!
    #'->DeserializingRepairingLookup
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      '->DeserializingRepairingLookup
      :ns
      *ns*))
  (defn deserializing-repairing-lookup
    "Creates a storage lookup that retries a failed deserialization after refreshing the underlying value-store cache."
    ([cluster] (->DeserializingRepairingLookup cluster)))
  (reset-meta!
    #'deserializing-repairing-lookup
    (assoc
      {:arglists (clojure.core/list ['cluster]),
       :doc
       "Creates a storage lookup that retries a failed deserialization after refreshing the underlying value-store cache.",
       :column (int 1)}
      :name
      'deserializing-repairing-lookup
      :ns
      *ns*))
  (defn lookup-with-object-cache
    "Wraps an object lookup with in-process caching and records cache hits, misses, index loads, and segment loads."
    ([lookup object_cache]
      (let [load_counter (fn load_counter ([ctr] (keyword (str (name ctr) "-load"))))]
        (cache/lookup-cache
          lookup
          object_cache
          (fn fn__17335
            ([k h_or_m]
              (io-stats/inc! :ocache)
              (let [index_counter io-stats/*io-index*
                    seg_type_counter io-stats/*io-seg-type*
                    leaf? (and (not= seg_type_counter :dir) index_counter)]
                (io-trace/note! k index_counter)
                (when leaf? (io-stats/inc! index_counter))
                (when (and seg_type_counter (not= seg_type_counter :dir))
                  (io-stats/inc! seg_type_counter))
                (when (= :miss h_or_m)
                  (cond
                    leaf? (io-stats/inc! (^clojure.lang.IFn load_counter index_counter))
                    seg_type_counter (do
                                       (when (= seg_type_counter :dir)
                                         (monitor/add-stat :DirLoads 1))
                                       (io-stats/inc!
                                         (^clojure.lang.IFn load_counter seg_type_counter))))))
              (monitor/add-stat
                :ObjectCache
                (let [G__17336 h_or_m] (case G__17336 :miss 0 :hit 1))))))))
    ([lookup] (lookup-with-object-cache lookup (system-cache))))
  (reset-meta!
    #'lookup-with-object-cache
    (assoc
      {:arglists (clojure.core/list ['lookup] ['lookup 'object-cache]),
       :doc
       "Wraps an object lookup with in-process caching and records cache hits, misses, index loads, and segment loads.",
       :column (int 1)}
      :name
      'lookup-with-object-cache
      :ns
      *ns*))
  (defn system-cache-olookup
    ([cluster]
      (lookup-with-object-cache
        (cache/lookup-with-inflight-cache (deserializing-repairing-lookup cluster)))))
  (reset-meta!
    #'system-cache-olookup
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      'system-cache-olookup
      :ns
      *ns*)))
