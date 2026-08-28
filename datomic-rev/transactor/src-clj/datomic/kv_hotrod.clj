(do
  (clojure.core/in-ns 'datomic.kv-hotrod)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require ['datomic.kv-store :as 'kv] ['datomic.io :as 'io])
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.kv-hotrod 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-hotrod))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require ['datomic.kv-store :as 'kv] ['datomic.io :as 'io])
        (clojure.core/import 'java.nio.ByteBuffer))))
  (set! *warn-on-reflection* true)
  (def FORCE (delay (into-array [org.infinispan.client.hotrod.Flag/FORCE_RETURN_VALUE])))
  (def PROPS
   (doto
     (java.util.Properties.)
     (.setProperty "infinispan.client.hotrod.key_size_estimate" "128")
     (.setProperty "infinispan.client.hotrod.value_size_estimate" "64000")
     (.setProperty "infinispan.client.hotrod.socket_timeout" "10000")
     (.setProperty "infinispan.client.hotrod.connect_timeout" "10000")))
  (def managers (atom {}))
  (deftype
    KVHotRod
    [cache]
    datomic.kv_store.KVStore
    (close [this] nil)
    (delete [this key consistent?] (do (.remove ^java.util.Map cache key) :ok))
    (get
      [this key consistent?]
      (let [temp__5804__auto__ (.get ^java.util.Map cache key)]
        (when temp__5804__auto__
          (let [ret temp__5804__auto__
                ret (let [temp__5802__auto__ (:v ret)]
                      (if temp__5802__auto__
                        (let [v temp__5802__auto__] (assoc ret :v (ByteBuffer/wrap ^bytes v)))
                        ret))]
            ret))))
    (put
      [this val_map]
      (when (let [put_when (fn put_when
                             ([k val emap]
                               (let [temp__5804__auto__ (.getVersioned
                                                          ^org.infinispan.client.hotrod.RemoteCache cache
                                                          k)]
                                 (when temp__5804__auto__
                                   (let [vv temp__5804__auto__
                                         ver (.getVersion
                                               ^org.infinispan.client.hotrod.VersionedValue vv)
                                         oldv (.getValue
                                                ^org.infinispan.client.hotrod.VersionedValue vv)]
                                     (when (= emap (select-keys oldv (keys emap)))
                                       (or
                                         (.replaceWithVersion
                                           ^org.infinispan.client.hotrod.RemoteCache cache
                                           k
                                           val
                                           (long ver))
                                         (recur k val emap))))))))
                  map__33162 val_map
                  map__33162 (if (seq? map__33162)
                               (if (next map__33162)
                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                   (to-array map__33162))
                                 (if (seq map__33162) (first map__33162) {}))
                               map__33162)
                  id (clojure.core/get map__33162 :id)
                  ensure (clojure.core/get map__33162 :ensure)
                  val_map (dissoc val_map :ensure)
                  v (:v val_map)
                  val_map (if v (assoc val_map :v (io/alias-buf-bytes v)) val_map)]
              (cond
                (= ensure {:id nil}) (nil?
                                       (.putIfAbsent
                                         (.withFlags
                                           ^org.infinispan.client.hotrod.RemoteCache cache
                                           (deref FORCE))
                                         id
                                         val_map))
                ensure (^clojure.lang.IFn put_when id val_map ensure)
                :else (do (.put ^java.util.Map cache id val_map) true)))
        :ok)))
  (clojure.core/import 'datomic.kv_hotrod.KVHotRod)
  (defn ->KVHotRod ([cache] (datomic.kv_hotrod.KVHotRod. cache)))
  (defn kv-infinispan
    ([endpoint]
      (let [map__33173 endpoint
            map__33173 (if (seq? map__33173)
                         (if (next map__33173)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__33173))
                           (if (seq map__33173) (first map__33173) {}))
                         map__33173)
            host (clojure.core/get map__33173 :host)
            port (clojure.core/get map__33173 :port)
            manager (let [lockee__5782__auto__ managers
                          locklocal__5783__auto__ lockee__5782__auto__]
                      (monitor-enter locklocal__5783__auto__)
                      (try
                        (or
                          (clojure.core/get (deref managers) endpoint)
                          (let [m (org.infinispan.client.hotrod.RemoteCacheManager.
                                    (str host)
                                    (int port))]
                            (swap! managers assoc endpoint m)
                            m))
                        (finally (do (monitor-exit locklocal__5783__auto__) nil))))]
        (datomic.kv_hotrod.KVHotRod.
          (.getCache ^org.infinispan.client.hotrod.RemoteCacheManager manager "datomic"))))))