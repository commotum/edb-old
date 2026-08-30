(do
  (clojure.core/in-ns 'datomic.kv-cache)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.cache :as 'cache]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.monitor :as 'monitor]
        ['datomic.require :as 'req])))
  (when-not (.equals 'datomic.kv-cache 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-cache))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.cache :as 'cache]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.monitor :as 'monitor]
          ['datomic.require :as 'req]))))
  (set! *warn-on-reflection* true)
  (req/maybe-require 'datomic.memcached 'datomic.valcache-direct)
  (.setMeta
    (clojure.lang.RT/var "datomic.kv-cache" "kv-cache-ref")
    {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.kv-cache" "kv-cache-ref")
    (let [G__23303 (atom nil)]
      (add-watch G__23303 :datomic.kv-cache/closer common/closing-watch)
      G__23303))
  (defn shutdown ([] (reset! kv-cache-ref nil)))
  (reset-meta!
    #'shutdown
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'shutdown :ns *ns*))
  (defn get-kv-cache-ref ([] kv-cache-ref))
  (reset-meta!
    #'get-kv-cache-ref
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'get-kv-cache-ref :ns *ns*))
  (defn start-kv-cache
    ([]
      (let [record (fn record ([_] (monitor/add-stat :CacheStackRepair 1)))
            combine (fn combine
                      ([c2 c1]
                        (if (and c1 c2)
                          (cache/repairing-cache-stack
                            {:cache-1 c1, :cache-2 c2, :on-repair record})
                          (or c1 c2))))
            memcached ((resolve 'datomic.memcached/start-memcached-from-config))
            valcache_args (config/valcache-args)
            valcache (when valcache_args ((resolve 'datomic.valcache-direct/create) valcache_args))
            local_memcached ((resolve 'datomic.memcached/start-local-memcached-from-config))
            stack (reduce combine nil [memcached valcache local_memcached])]
        (reset! kv-cache-ref stack))))
  (reset-meta!
    #'start-kv-cache
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'start-kv-cache :ns *ns*)))