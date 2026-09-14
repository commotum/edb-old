(do
  (clojure.core/in-ns 'datomic.indexer)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.indexer)
    {:doc
     "Tracks memory-index growth and schedules transactor background-indexing work. Usage is accounted separately for datoms still in memory and datoms currently being merged. Per-database thresholds request an index job, while the process-wide limit prioritizes the database consuming the most memory."})
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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol QueueDatabaseIndex (memory-threshold-request-index [db]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.indexer" "QueueDatabaseIndex")
      (assoc
        (assoc
          protocol_metadata__7463
          :doc
          "Requests a background indexing job for a database whose memory index reached its threshold.")
        :name
        'QueueDatabaseIndex
        :ns
        *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memory-threshold-request-index
                                        {:arglists (clojure.core/list ['db])}),
                                      :arglists (clojure.core/list ['db]),
                                      :doc "Queues a background indexing request for db."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "QueueDatabaseIndex"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "memory-threshold-request-index")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol
      Indexer
      (memidx-total [_] "Total size of memidx and indexing across all dbs")
      (memidx-limit-exceeded? [_] "Memory index limit exceeded?")
      (queue-db-index-job [_ db-name] "Queue indexing job for a particular db, if needed")
      (inc-memidx-usage [_ db-name bytes] "Inc memidx usage of named db")
      (db-total [_ db-name] "Size of memidx and indexing for a particular db")
      (indexing-completed [_ db-name] "Notify indexer that indexing job completed")
      (indexing-started [_ db-name] "Notify indexer that indexing job started")
      (notify-txdata [_ db-name txdata] "Inform indexer of data added to a database.")
      (queue-index-jobs [_] "Queue jobs for database that need them.")
      (remove-database [_ db-name] "Remove database from indexer"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.indexer" "Indexer")
      (assoc
        (assoc
          protocol_metadata__7466
          :doc
          "Accounts for memory-index growth and coordinates per-database background indexing requests.")
        :name
        'Indexer
        :ns
        *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memidx-total
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Total size of memidx and indexing across all dbs"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "memidx-total")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*)))
    (let [protocol_signature__7469 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memidx-limit-exceeded?
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Memory index limit exceeded?"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7470 (with-meta
                                       (:name protocol_signature__7469)
                                       protocol_signature__7469)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "memidx-limit-exceeded?")
        (assoc protocol_signature__7469 :name protocol_method_name__7470 :ns *ns*)))
    (let [protocol_signature__7471 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'queue-db-index-job
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Queue indexing job for a particular db, if needed"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7472 (with-meta
                                       (:name protocol_signature__7471)
                                       protocol_signature__7471)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "queue-db-index-job")
        (assoc protocol_signature__7471 :name protocol_method_name__7472 :ns *ns*)))
    (let [protocol_signature__7473 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'inc-memidx-usage
                                        {:arglists (clojure.core/list ['_ 'db-name 'bytes])}),
                                      :arglists (clojure.core/list ['_ 'db-name 'bytes]),
                                      :doc "Inc memidx usage of named db"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "inc-memidx-usage")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*)))
    (let [protocol_signature__7475 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'db-total
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Size of memidx and indexing for a particular db"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "db-total")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*)))
    (let [protocol_signature__7477 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'indexing-completed
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Notify indexer that indexing job completed"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7478 (with-meta
                                       (:name protocol_signature__7477)
                                       protocol_signature__7477)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "indexing-completed")
        (assoc protocol_signature__7477 :name protocol_method_name__7478 :ns *ns*)))
    (let [protocol_signature__7479 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'indexing-started
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Notify indexer that indexing job started"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7480 (with-meta
                                       (:name protocol_signature__7479)
                                       protocol_signature__7479)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "indexing-started")
        (assoc protocol_signature__7479 :name protocol_method_name__7480 :ns *ns*)))
    (let [protocol_signature__7481 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'notify-txdata
                                        {:arglists (clojure.core/list ['_ 'db-name 'txdata])}),
                                      :arglists (clojure.core/list ['_ 'db-name 'txdata]),
                                      :doc "Inform indexer of data added to a database."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7482 (with-meta
                                       (:name protocol_signature__7481)
                                       protocol_signature__7481)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "notify-txdata")
        (assoc protocol_signature__7481 :name protocol_method_name__7482 :ns *ns*)))
    (let [protocol_signature__7483 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'queue-index-jobs
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Queue jobs for database that need them."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7484 (with-meta
                                       (:name protocol_signature__7483)
                                       protocol_signature__7483)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "queue-index-jobs")
        (assoc protocol_signature__7483 :name protocol_method_name__7484 :ns *ns*)))
    (let [protocol_signature__7485 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'remove-database
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Remove database from indexer"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7486 (with-meta
                                       (:name protocol_signature__7485)
                                       protocol_signature__7485)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "remove-database")
        (assoc protocol_signature__7485 :name protocol_method_name__7486 :ns *ns*))))
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
       :column (int 1)}
      :name
      'maxf
      :ns
      *ns*))
  (deftype
    IndexerImpl
    [databases memidx_max_fn memidx_threshold_fn memidx_usage]
    datomic.monitor.Metrics
    datomic.indexer.Indexer
    ;; ATOMIC-NOTE [observed/adaptation] Above half the process-wide maximum, select the largest database by
    ;; frozen-plus-live usage; the per-database threshold is independent. This
    ;; does not establish native multi-database fairness: Atomic services have
    ;; explicit individual memory/work policies.
    (queue-index-jobs
      [this]
      (when (< (* 0.5 (^clojure.lang.IFn memidx_max_fn)) (memidx-total this))
        (let [temp__5825__auto__ (apply maxf (partial db-total this) (keys databases))]
          (when temp__5825__auto__
            (let [dbname temp__5825__auto__]
              (memory-threshold-request-index (get databases dbname)))))))
    (queue-db-index-job
      [this dbname]
      (when (< (^clojure.lang.IFn memidx_threshold_fn) (db-total this dbname))
        (memory-threshold-request-index (get databases dbname))))
    ;; ATOMIC-NOTE [observed/adaptation] Transfer the frozen charge into :indexing without reducing :total:
    ;; the job still owns those datoms. Later transactions accrue in :memidx.
    ;; Completion subtracts only the frozen charge, preserving backpressure on
    ;; new arrivals. Native begin_job/publish_completed tracks this by basis.
    (indexing-started
      [this dbname]
      (swap!
        memidx_usage
        (fn fn__28110
          ([usage]
            (let [temp__5823__auto__ (get-in usage [:memidx dbname])]
              (if temp__5823__auto__
                (let [size temp__5823__auto__]
                  (assoc-in (assoc-in usage [:memidx dbname] 0) [:indexing dbname] size))
                usage))))))
    (indexing-completed
      [this dbname]
      (swap!
        memidx_usage
        (fn fn__28107
          ([usage]
            (let [indexing (or (get-in usage [:indexing dbname]) 0)]
              (update (assoc-in usage [:indexing dbname] 0) :total - indexing))))))
    (inc-memidx-usage
      [this dbname bytes]
      (swap!
        memidx_usage
        (fn fn__28105
          ([usage] (update (update-in usage [:memidx dbname] (fnil + 0) bytes) :total + bytes)))))
    (remove-database
      [this dbname]
      (swap!
        memidx_usage
        (fn fn__28101
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
          (inc-memidx-usage this dbname (long size)))
        (queue-db-index-job this dbname)
        (queue-index-jobs this)))
    (metrics [this] (merge {:MemoryIndexMB (quot (memidx-total this) (* 1000 1000))})))
  (clojure.core/import 'datomic.indexer.IndexerImpl)
  (defn ->IndexerImpl
    ([databases memidx_max_fn memidx_threshold_fn memidx_usage]
      (datomic.indexer.IndexerImpl. databases memidx_max_fn memidx_threshold_fn memidx_usage)))
  (reset-meta!
    #'->IndexerImpl
    (assoc
      {:arglists
       (clojure.core/list ['databases 'memidx-max-fn 'memidx-threshold-fn 'memidx-usage]),
       :column (int 1)}
      :name
      '->IndexerImpl
      :ns
      *ns*))
  (defn create-indexer
    ([& p__28118]
      (let [map__28119 p__28118
            map__28119 (if (seq? map__28119)
                         (if (next map__28119)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__28119))
                           (if (seq map__28119) (first map__28119) {}))
                         map__28119)
            databases (get map__28119 :databases)
            memidx_max_fn (get map__28119 :memidx-max-fn)
            memidx_threshold_fn (get map__28119 :memidx-threshold-fn)
            memidx_max_fn (or
                            memidx_max_fn
                            (fn fn__28120 ([] (config/property "datomic.memoryIndexMax"))))
            memidx_threshold_fn (or
                                  memidx_threshold_fn
                                  (fn fn__28122
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
          (atom {:total 0})))))
  (reset-meta!
    #'create-indexer
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['& {:keys ['databases 'memidx-max-fn 'memidx-threshold-fn]}]
           {:tag 'datomic.indexer.IndexerImpl})),
       :doc
       "Creates an index scheduler for databases. memidx-threshold-fn controls per-database scheduling and memidx-max-fn controls process-wide pressure; both default to the corresponding Datomic configuration properties.",
       :column (int 1)}
      :name
      'create-indexer
      :ns
      *ns*)))
