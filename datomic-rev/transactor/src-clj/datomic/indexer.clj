(do
  (clojure.core/in-ns 'datomic.indexer)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.math :as 'math]
        ['datomic.memory :as 'memory]
        ['datomic.memory-size :as 'size]
        ['datomic.monitor :as 'monitor]
        ['datomic.slf4j :as 'logger]
        ['datomic.config :as 'config]
        'datomic.btset
        'datomic.db)))
  (when-not (.equals 'datomic.indexer 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.indexer))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.math :as 'math]
          ['datomic.memory :as 'memory]
          ['datomic.memory-size :as 'size]
          ['datomic.monitor :as 'monitor]
          ['datomic.slf4j :as 'logger]
          ['datomic.config :as 'config]
          'datomic.btset
          'datomic.db))))
  (defonce QueueDatabaseIndex {})
  (defprotocol QueueDatabaseIndex (memory-threshold-request-index [db]))
  (defonce Indexer {})
  (defprotocol
    Indexer
    (memidx-total [_])
    (memidx-limit-exceeded? [_])
    (queue-db-index-job [_ db-name])
    (inc-memidx-usage [_ db-name bytes])
    (db-total [_ db-name])
    (indexing-completed [_ db-name])
    (indexing-started [_ db-name])
    (notify-txdata [_ db-name txdata])
    (queue-index-jobs [_])
    (remove-database [_ db-name]))
  (defn maxf
    ([f x y & more] (reduce (partial maxf f) (maxf x y) more))
    ([f x y] (if (< (^clojure.lang.IFn f x) (^clojure.lang.IFn f y)) y x))
    ([f x] x)
    ([f] nil))
  (reset-meta!
    #'maxf
    (assoc
      {:private true,
       :arglists (clojure.core/list ['f] ['f 'x] ['f 'x 'y] ['f 'x 'y '& 'more]),
       :column 1}
      :name
      'maxf
      :ns
      *ns*))
  (deftype
    IndexerImpl
    [databases memidx_max_fn memidx_threshold_fn memidx_usage]
    datomic.monitor.Metrics
    datomic.indexer.Indexer
    (queue-index-jobs
      [this]
      (when (< (* 0.5 (^clojure.lang.IFn memidx_max_fn)) (memidx-total this))
        (let [temp__5804__auto__ (apply maxf (partial db-total this) (keys databases))]
          (when temp__5804__auto__
            (let [dbname temp__5804__auto__]
              (memory-threshold-request-index (get databases dbname)))))))
    (queue-db-index-job
      [this dbname]
      (when (< (^clojure.lang.IFn memidx_threshold_fn) (db-total this dbname))
        (memory-threshold-request-index (get databases dbname))))
    (indexing-started
      [this dbname]
      (swap!
        memidx_usage
        (fn fn__24659
          ([usage]
            (let [temp__5802__auto__ (get-in usage [:memidx dbname])]
              (if temp__5802__auto__
                (let [size temp__5802__auto__]
                  (assoc-in (assoc-in usage [:memidx dbname] 0) [:indexing dbname] size))
                usage))))))
    (indexing-completed
      [this dbname]
      (swap!
        memidx_usage
        (fn fn__24656
          ([usage]
            (let [indexing (or (get-in usage [:indexing dbname]) 0)]
              (update (assoc-in usage [:indexing dbname] 0) :total - indexing))))))
    (inc-memidx-usage
      [this dbname bytes]
      (swap!
        memidx_usage
        (fn fn__24654
          ([usage] (update (update-in usage [:memidx dbname] (fnil + 0) bytes) :total + bytes)))))
    (remove-database
      [this dbname]
      (swap!
        memidx_usage
        (fn fn__24650
          ([usage]
            (let [memidx (or (get-in usage [:memidx dbname]) 0)
                  indexing (or (get-in usage [:indexing dbname]) 0)]
              (update
                (update-in (update-in usage [:memidx] dissoc dbname) [:indexing] dissoc dbname)
                :total
                -
                memidx
                indexing))))))
    (memidx-limit-exceeded? [this] (< (^clojure.lang.IFn memidx_max_fn) (memidx-total this)))
    (memidx-total [this] (:total (deref memidx_usage)))
    (db-total
      [this db_name]
      (let [usage (deref memidx_usage)]
        (+ (get-in usage [:memidx db_name] 0) (get-in usage [:indexing db_name] 0))))
    (notify-txdata
      [this dbname txdata]
      (do
        (let [size (+ (size/memory-size txdata) (* 32 (count txdata)))]
          (inc-memidx-usage this dbname size))
        (queue-db-index-job this dbname)
        (queue-index-jobs this)))
    (metrics [this] (merge {:MemoryIndexMB (quot (memidx-total this) (* 1000 1000))})))
  (clojure.core/import 'datomic.indexer.IndexerImpl)
  (defn ->IndexerImpl
    ([databases memidx_max_fn memidx_threshold_fn memidx_usage]
      (datomic.indexer.IndexerImpl. databases memidx_max_fn memidx_threshold_fn memidx_usage)))
  (defn create-indexer
    ([& p__24667]
      (let [map__24668 p__24667
            map__24668 (if (seq? map__24668)
                         (if (next map__24668)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24668))
                           (if (seq map__24668) (first map__24668) {}))
                         map__24668)
            databases (get map__24668 :databases)
            memidx_max_fn (get map__24668 :memidx-max-fn)
            memidx_threshold_fn (get map__24668 :memidx-threshold-fn)
            memidx_max_fn (or
                            memidx_max_fn
                            (fn fn__24669 ([] (config/property "datomic.memoryIndexMax"))))
            memidx_threshold_fn (or
                                  memidx_threshold_fn
                                  (fn fn__24671
                                    ([] (config/property "datomic.memoryIndexThreshold"))))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.indexer")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:event :indexer/create, :memidx-max (^clojure.lang.IFn memidx_max_fn)})))
          nil)
        (datomic.indexer.IndexerImpl.
          databases
          memidx_max_fn
          memidx_threshold_fn
          (atom {:total 0}))))))