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
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol QueueDatabaseIndex (memory-threshold-request-index [db]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.indexer" "QueueDatabaseIndex")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'QueueDatabaseIndex :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memory-threshold-request-index
                                        {:arglists (clojure.core/list ['db])}),
                                      :arglists (clojure.core/list ['db]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "QueueDatabaseIndex"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "memory-threshold-request-index")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*))))
  (let [protocol_metadata__7423 {:column (int 1)}]
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
      (assoc (assoc protocol_metadata__7423 :doc nil) :name 'Indexer :ns *ns*))
    (let [protocol_signature__7424 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memidx-total
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Total size of memidx and indexing across all dbs"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7425 (with-meta
                                       (:name protocol_signature__7424)
                                       protocol_signature__7424)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "memidx-total")
        (assoc protocol_signature__7424 :name protocol_method_name__7425 :ns *ns*)))
    (let [protocol_signature__7426 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memidx-limit-exceeded?
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Memory index limit exceeded?"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7427 (with-meta
                                       (:name protocol_signature__7426)
                                       protocol_signature__7426)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "memidx-limit-exceeded?")
        (assoc protocol_signature__7426 :name protocol_method_name__7427 :ns *ns*)))
    (let [protocol_signature__7428 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'queue-db-index-job
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Queue indexing job for a particular db, if needed"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7429 (with-meta
                                       (:name protocol_signature__7428)
                                       protocol_signature__7428)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "queue-db-index-job")
        (assoc protocol_signature__7428 :name protocol_method_name__7429 :ns *ns*)))
    (let [protocol_signature__7430 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'inc-memidx-usage
                                        {:arglists (clojure.core/list ['_ 'db-name 'bytes])}),
                                      :arglists (clojure.core/list ['_ 'db-name 'bytes]),
                                      :doc "Inc memidx usage of named db"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7431 (with-meta
                                       (:name protocol_signature__7430)
                                       protocol_signature__7430)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "inc-memidx-usage")
        (assoc protocol_signature__7430 :name protocol_method_name__7431 :ns *ns*)))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'db-total
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Size of memidx and indexing for a particular db"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "db-total")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*)))
    (let [protocol_signature__7434 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'indexing-completed
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Notify indexer that indexing job completed"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7435 (with-meta
                                       (:name protocol_signature__7434)
                                       protocol_signature__7434)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "indexing-completed")
        (assoc protocol_signature__7434 :name protocol_method_name__7435 :ns *ns*)))
    (let [protocol_signature__7436 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'indexing-started
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Notify indexer that indexing job started"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7437 (with-meta
                                       (:name protocol_signature__7436)
                                       protocol_signature__7436)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "indexing-started")
        (assoc protocol_signature__7436 :name protocol_method_name__7437 :ns *ns*)))
    (let [protocol_signature__7438 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'notify-txdata
                                        {:arglists (clojure.core/list ['_ 'db-name 'txdata])}),
                                      :arglists (clojure.core/list ['_ 'db-name 'txdata]),
                                      :doc "Inform indexer of data added to a database."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7439 (with-meta
                                       (:name protocol_signature__7438)
                                       protocol_signature__7438)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "notify-txdata")
        (assoc protocol_signature__7438 :name protocol_method_name__7439 :ns *ns*)))
    (let [protocol_signature__7440 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'queue-index-jobs
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Queue jobs for database that need them."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7441 (with-meta
                                       (:name protocol_signature__7440)
                                       protocol_signature__7440)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "queue-index-jobs")
        (assoc protocol_signature__7440 :name protocol_method_name__7441 :ns *ns*)))
    (let [protocol_signature__7442 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'remove-database
                                        {:arglists (clojure.core/list ['_ 'db-name])}),
                                      :arglists (clojure.core/list ['_ 'db-name]),
                                      :doc "Remove database from indexer"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.indexer" "Indexer"))
          protocol_method_name__7443 (with-meta
                                       (:name protocol_signature__7442)
                                       protocol_signature__7442)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.indexer" "remove-database")
        (assoc protocol_signature__7442 :name protocol_method_name__7443 :ns *ns*))))
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