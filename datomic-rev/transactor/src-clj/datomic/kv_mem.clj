(do
  (clojure.core/in-ns 'datomic.kv-mem)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require ['datomic.kv-store :as 'kv])
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.concurrent.ConcurrentMap)
      (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)))
  (when-not (.equals 'datomic.kv-mem 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-mem))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require ['datomic.kv-store :as 'kv])
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.concurrent.ConcurrentMap)
        (clojure.core/import 'java.util.concurrent.ConcurrentHashMap))))
  (set! *warn-on-reflection* true)
  (deftype
    KVMem
    [m]
    datomic.kv_store.KVStore
    (close [this] nil)
    (delete [this key consistent?] (do (.remove ^java.util.Map m key) :ok))
    (get [this key consistent?] (.get ^java.util.Map m key))
    (put
      [this val_map]
      (when (let [put_when (fn put_when
                             ([k val emap]
                               (let [oldv (.get ^java.util.Map m k)]
                                 (when (= emap (select-keys oldv (keys emap)))
                                   (or
                                     (.replace ^java.util.concurrent.ConcurrentMap m k oldv val)
                                     (recur k val emap))))))
                  map__20123 val_map
                  map__20123 (if (seq? map__20123)
                               (if (next map__20123)
                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                   (to-array map__20123))
                                 (if (seq map__20123) (first map__20123) {}))
                               map__20123)
                  id (clojure.core/get map__20123 :id)
                  ensure (clojure.core/get map__20123 :ensure)
                  val_map (dissoc val_map :ensure)]
              (cond
                (:rev ensure) (^clojure.lang.IFn put_when id val_map ensure)
                :else (do (nil? (.putIfAbsent ^java.util.concurrent.ConcurrentMap m id val_map)))))
        :ok)))
  (clojure.core/import 'datomic.kv_mem.KVMem)
  (defn ->KVMem ([m] (datomic.kv_mem.KVMem. m)))
  (reset-meta!
    #'->KVMem
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name '->KVMem :ns *ns*))
  (defn kv-mem ([m] (datomic.kv_mem.KVMem. m)))
  (reset-meta!
    #'kv-mem
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'kv-mem :ns *ns*)))