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
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol QueueDatabaseIndex (memory-threshold-request-index [db]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.indexer" "QueueDatabaseIndex")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'QueueDatabaseIndex :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memory-threshold-request-index
                                        {:arglists (clojure.core/list ['db])}),
                                      :arglists (clojure.core/list ['db]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "QueueDatabaseIndex"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "memory-threshold-request-index")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (let [protocol_metadata__7434 {:column (int 1)}]
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
      (assoc (assoc protocol_metadata__7434 :doc nil) :name 'Indexer :ns *ns*))
    (let [protocol_signature__7435 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memidx-total
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Total size of memidx and indexing across all dbs"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7436 (with-meta
                                       (:name protocol_signature__7435)
                                       protocol_signature__7435)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "memidx-total")
        (assoc protocol_signature__7435 :name protocol_method_name__7436 :ns *ns*)))
    (let [protocol_signature__7437 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memidx-limit-exceeded?
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Memory index limit exceeded?"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7438 (with-meta
                                       (:name protocol_signature__7437)
                                       protocol_signature__7437)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "memidx-limit-exceeded?")
        (assoc protocol_signature__7437 :name protocol_method_name__7438 :ns *ns*)))
    (let [protocol_signature__7439 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'queue-db-index-job
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Queue indexing job for a particular db, if needed"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7440 (with-meta
                                       (:name protocol_signature__7439)
                                       protocol_signature__7439)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "queue-db-index-job")
        (assoc protocol_signature__7439 :name protocol_method_name__7440 :ns *ns*)))
    (let [protocol_signature__7441 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'inc-memidx-usage
                                        {:arglists (clojure.core/list ['_ 'db-name 'bytes])}),
                                      :arglists (clojure.core/list ['_ 'db-name 'bytes]),
                                      :doc "Inc memidx usage of named db"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7442 (with-meta
                                       (:name protocol_signature__7441)
                                       protocol_signature__7441)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "inc-memidx-usage")
        (assoc protocol_signature__7441 :name protocol_method_name__7442 :ns *ns*)))
    (let [protocol_signature__7443 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'db-total
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Size of memidx and indexing for a particular db"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7444 (with-meta
                                       (:name protocol_signature__7443)
                                       protocol_signature__7443)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "db-total")
        (assoc protocol_signature__7443 :name protocol_method_name__7444 :ns *ns*)))
    (let [protocol_signature__7445 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'indexing-completed
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Notify indexer that indexing job completed"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7446 (with-meta
                                       (:name protocol_signature__7445)
                                       protocol_signature__7445)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "indexing-completed")
        (assoc protocol_signature__7445 :name protocol_method_name__7446 :ns *ns*)))
    (let [protocol_signature__7447 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'indexing-started
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Notify indexer that indexing job started"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7448 (with-meta
                                       (:name protocol_signature__7447)
                                       protocol_signature__7447)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "indexing-started")
        (assoc protocol_signature__7447 :name protocol_method_name__7448 :ns *ns*)))
    (let [protocol_signature__7449 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'notify-txdata
                                        {:arglists (clojure.core/list ['_ 'db-name 'txdata])}),
                                      :arglists (clojure.core/list ['_ 'db-name 'txdata]),
                                      :doc "Inform indexer of data added to a database."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7450 (with-meta
                                       (:name protocol_signature__7449)
                                       protocol_signature__7449)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "notify-txdata")
        (assoc protocol_signature__7449 :name protocol_method_name__7450 :ns *ns*)))
    (let [protocol_signature__7451 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'queue-index-jobs
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Queue jobs for database that need them."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7452 (with-meta
                                       (:name protocol_signature__7451)
                                       protocol_signature__7451)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "queue-index-jobs")
        (assoc protocol_signature__7451 :name protocol_method_name__7452 :ns *ns*)))
    (let [protocol_signature__7453 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'remove-database
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Remove database from indexer"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7454 (with-meta
                                       (:name protocol_signature__7453)
                                       protocol_signature__7453)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "remove-database")
        (assoc protocol_signature__7453 :name protocol_method_name__7454 :ns *ns*))))
  (def maxf
   (fn maxf
     ([f x y & more] (reduce (partial maxf f) (maxf x y) more))
     ([f x y] (if (< (^clojure.lang.IFn f x) (^clojure.lang.IFn f y)) y x))
     ([f x] x)
     ([f] nil)))
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
  (def ->IndexerImpl
   (fn __GT_IndexerImpl
     ([databases memidx_max_fn memidx_threshold_fn memidx_usage]
       (datomic.indexer.IndexerImpl. databases memidx_max_fn memidx_threshold_fn memidx_usage))))
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
  (def create-indexer
   (fn create_indexer
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
           (atom {:total 0}))))))
  (reset-meta!
    #'create-indexer
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['& {:keys ['databases 'memidx-max-fn 'memidx-threshold-fn]}]
           {:tag 'datomic.indexer.IndexerImpl})),
       :column (int 1)}
      :name
      'create-indexer
      :ns
      *ns*)))