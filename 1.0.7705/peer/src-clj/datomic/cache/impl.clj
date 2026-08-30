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
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      FastCount
      (fast-count [_] "Count a collection, preferring speed over exact accuracy."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cache.impl" "FastCount")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'FastCount :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'fast-count {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Count a collection, preferring speed over exact accuracy."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cache.impl" "FastCount"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cache.impl" "fast-count")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (let [protocol_metadata__7434 {:column (int 1)}]
    (defprotocol CacheKeys (cache-keys [_] "Returns cache keys"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cache.impl" "CacheKeys")
      (assoc (assoc protocol_metadata__7434 :doc nil) :name 'CacheKeys :ns *ns*))
    (let [protocol_signature__7435 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'cache-keys {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Returns cache keys"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cache.impl" "CacheKeys"))
          protocol_method_name__7436 (with-meta
                                       (:name protocol_signature__7435)
                                       protocol_signature__7435)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cache.impl" "cache-keys")
        (assoc protocol_signature__7435 :name protocol_method_name__7436 :ns *ns*))))
  (let [protocol_metadata__7437 {:column (int 1)}]
    (defprotocol CachePut (put [c k v] "Put item into the cache. No useful return value."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cache.impl" "CachePut")
      (assoc (assoc protocol_metadata__7437 :doc nil) :name 'CachePut :ns *ns*))
    (let [protocol_signature__7438 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'put {:arglists (clojure.core/list ['c 'k 'v])}),
                                      :arglists (clojure.core/list ['c 'k 'v]),
                                      :doc "Put item into the cache. No useful return value."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cache.impl" "CachePut"))
          protocol_method_name__7439 (with-meta
                                       (:name protocol_signature__7438)
                                       protocol_signature__7438)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cache.impl" "put")
        (assoc protocol_signature__7438 :name protocol_method_name__7439 :ns *ns*))))
  (let [protocol_metadata__7440 {:column (int 1)}]
    (defprotocol
      CacheRemove
      (remove [c k] "Remove item from cache, returning it.")
      (clear [c] "Remove all items from cache"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cache.impl" "CacheRemove")
      (assoc (assoc protocol_metadata__7440 :doc nil) :name 'CacheRemove :ns *ns*))
    (let [protocol_signature__7441 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'remove {:arglists (clojure.core/list ['c 'k])}),
                                      :arglists (clojure.core/list ['c 'k]),
                                      :doc "Remove item from cache, returning it."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cache.impl" "CacheRemove"))
          protocol_method_name__7442 (with-meta
                                       (:name protocol_signature__7441)
                                       protocol_signature__7441)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cache.impl" "remove")
        (assoc protocol_signature__7441 :name protocol_method_name__7442 :ns *ns*)))
    (let [protocol_signature__7443 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'clear {:arglists (clojure.core/list ['c])}),
                                      :arglists (clojure.core/list ['c]),
                                      :doc "Remove all items from cache"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cache.impl" "CacheRemove"))
          protocol_method_name__7444 (with-meta
                                       (:name protocol_signature__7443)
                                       protocol_signature__7443)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cache.impl" "clear")
        (assoc protocol_signature__7443 :name protocol_method_name__7444 :ns *ns*))))
  (extend
    java.lang.Object
    FastCount
    {:fast-count (fn fn__10115 ([coll] (java.lang.Integer/valueOf (int (count coll)))))})
  (extend
    java.util.concurrent.ConcurrentMap
    CachePut
    {:put (fn fn__10117 ([c k v] (.put ^java.util.Map c k v)))})
  (extend
    java.util.concurrent.ConcurrentMap
    CacheRemove
    {:remove (fn fn__10119 ([c k] (.remove ^java.util.Map c k))),
     :clear (fn fn__10121 ([c] (.clear ^java.util.Map c) nil))}))