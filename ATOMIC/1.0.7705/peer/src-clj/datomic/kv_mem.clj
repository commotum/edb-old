(do
  (clojure.core/in-ns 'datomic.kv-mem)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.kv-mem)
    {:doc
     "In-process KVStore implementation for transient databases. Values live only for the lifetime of the process and conditional reference writes use ConcurrentMap compare-and-replace operations."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require ['datomic.io :as 'dio] ['datomic.kv-store :as 'kv])
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
      (clojure.core/import 'java.util.concurrent.ConcurrentMap)))
  (when-not (.equals 'datomic.kv-mem 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-mem))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require ['datomic.io :as 'dio] ['datomic.kv-store :as 'kv])
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
        (clojure.core/import 'java.util.concurrent.ConcurrentMap))))
  (set! *warn-on-reflection* true)
  ;; Store read-only buffer aliases and implement revision checks with atomic
  ;; ConcurrentMap replacement. A conditional mismatch leaves the prior entry intact.
  (deftype
    KVMem
    [m]
    datomic.kv_store.KVStore
    (close [this] nil)
    (delete [this key consistent?] (do (.remove ^java.util.Map m key) :ok))
    (get
      [this key consistent?]
      (let [entry (.get ^java.util.Map m key) v (:v entry)]
        (cond-> entry (some? v) (assoc :v (.duplicate ^java.nio.ByteBuffer v)))))
    (put
      [this val_map]
      (do
        (when-not (not (and (:v val_map) (:ensure val_map)))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                "KVStore/put with both :v and :ensure is undefined behavior."
                "\n"
                (pr-str
                  (clojure.core/list
                    'not
                    (clojure.core/list
                      'and
                      (clojure.core/list :v 'val-map)
                      (clojure.core/list :ensure 'val-map))))))))
        (when (let [put_when (fn put_when
                               ([k val emap]
                                 (let [oldv (.get ^java.util.Map m k)]
                                   (when (= emap (select-keys oldv (keys emap)))
                                     (or
                                       (.replace ^java.util.concurrent.ConcurrentMap m k oldv val)
                                       (recur k val emap))))))
                    map__17672 val_map
                    map__17672 (if (seq? map__17672)
                                 (if (next map__17672)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__17672))
                                   (if (seq map__17672) (first map__17672) {}))
                                 map__17672)
                    id (clojure.core/get map__17672 :id)
                    ensure (clojure.core/get map__17672 :ensure)
                    v (clojure.core/get map__17672 :v)
                    val_map (dissoc val_map :ensure)
                    val_map (cond->
                              val_map
                              (some? v)
                              (assoc :v (.asReadOnlyBuffer (dio/byte-source->buffer v))))]
                (cond
                  (:rev ensure) (^clojure.lang.IFn put_when id val_map ensure)
                  :else (do
                          (nil? (.putIfAbsent ^java.util.concurrent.ConcurrentMap m id val_map)))))
          :ok))))
  (clojure.core/import 'datomic.kv_mem.KVMem)
  (defn ->KVMem ([m] (datomic.kv_mem.KVMem. m)))
  (reset-meta!
    #'->KVMem
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name '->KVMem :ns *ns*))
  (defn kv-mem
    ([m] (datomic.kv_mem.KVMem. m))
    ([] (kv-mem (java.util.concurrent.ConcurrentHashMap.))))
  (reset-meta!
    #'kv-mem
    (assoc {:arglists (clojure.core/list [] ['m]), :column (int 1)} :name 'kv-mem :ns *ns*)))
