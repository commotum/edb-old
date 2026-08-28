(do
  (clojure.core/in-ns 'datomic.cache.impl)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['remove])
      (clojure.core/import 'java.util.concurrent.ConcurrentMap)))
  (when-not (.equals 'datomic.cache.impl 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cache.impl))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['remove])
        (clojure.core/import 'java.util.concurrent.ConcurrentMap))))
  (defonce FastCount {})
  (defprotocol FastCount (fast-count [_]))
  (defonce CacheKeys {})
  (defprotocol CacheKeys (cache-keys [_]))
  (defonce CachePut {})
  (defprotocol CachePut (put [c k v]))
  (defonce CacheRemove {})
  (defprotocol CacheRemove (remove [c k]) (clear [c]))
  (extend
    java.lang.Object
    FastCount
    {:fast-count (fn fn__358 ([coll] (java.lang.Integer/valueOf (int (count coll)))))})
  (extend
    java.util.concurrent.ConcurrentMap
    CachePut
    {:put (fn fn__360 ([c k v] (.put ^java.util.Map c k v)))})
  (extend
    java.util.concurrent.ConcurrentMap
    CacheRemove
    {:remove (fn fn__362 ([c k] (.remove ^java.util.Map c k))),
     :clear (fn fn__364 ([c] (.clear ^java.util.Map c) nil))}))