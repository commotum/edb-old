(do
  (clojure.core/in-ns 'datomic.peer)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.peer)
    {:doc
     "Peer connection lifecycle, immutable database snapshots, transaction submission, live-index integration, transaction reports, and transactor reconnection. Live connections are thread-safe, cached by resolved database configuration, and intended to be long lived. Storage-only connections are uncached fixed snapshots."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.java.io :as 'jio]
        ['clojure.pprint :as 'pprint]
        ['clojure.string :as 'str]
        ['datomic.adopter :as 'adopter]
        ['datomic.cache :as 'cache]
        ['datomic.cleanup :as 'cleanup]
        ['datomic.cluster :as 'cluster]
        ['datomic.cluster-stack :as 'cluster-stack]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.db :as 'db]
        ['datomic.db-io :as 'db-io]
        ['datomic.domain :as 'domain]
        ['datomic.index :as 'index]
        ['datomic.io :as 'io]
        ['datomic.log :as 'log]
        ['datomic.math :as 'math]
        ['datomic.memory :as 'memory]
        ['datomic.monitor :as 'monitor]
        ['datomic.promise :as 'promise]
        ['datomic.queue :as 'queue]
        ['datomic.reconnector2 :as 'recon]
        ['datomic.transaction :as 'tx]
        ['datomic.slf4j :as 'logger]
        ['datomic.uri :as 'uri]
        ['datomic.error :as 'error]
        ['datomic.coordination :as 'coord]
        ['datomic.connector :as 'conn]
        ['datomic.require :as 'req]
        ['datomic.catalog :as 'catalog]
        ['datomic.fulltext-index :as 'ftindex]
        ['datomic.cast2slf4j :as 'cast2slf4j])
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.db.IDbImpl)
      (clojure.core/import 'java.lang.ref.WeakReference)
      (clojure.core/import 'java.net.URI)
      (clojure.core/import 'java.util.Map)
      (clojure.core/import 'java.util.Queue)
      (clojure.core/import 'java.util.concurrent.ArrayBlockingQueue)
      (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)))
  (when-not (.equals 'datomic.peer 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.peer))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.java.io :as 'jio]
          ['clojure.pprint :as 'pprint]
          ['clojure.string :as 'str]
          ['datomic.adopter :as 'adopter]
          ['datomic.cache :as 'cache]
          ['datomic.cleanup :as 'cleanup]
          ['datomic.cluster :as 'cluster]
          ['datomic.cluster-stack :as 'cluster-stack]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.db :as 'db]
          ['datomic.db-io :as 'db-io]
          ['datomic.domain :as 'domain]
          ['datomic.index :as 'index]
          ['datomic.io :as 'io]
          ['datomic.log :as 'log]
          ['datomic.math :as 'math]
          ['datomic.memory :as 'memory]
          ['datomic.monitor :as 'monitor]
          ['datomic.promise :as 'promise]
          ['datomic.queue :as 'queue]
          ['datomic.reconnector2 :as 'recon]
          ['datomic.transaction :as 'tx]
          ['datomic.slf4j :as 'logger]
          ['datomic.uri :as 'uri]
          ['datomic.error :as 'error]
          ['datomic.coordination :as 'coord]
          ['datomic.connector :as 'conn]
          ['datomic.require :as 'req]
          ['datomic.catalog :as 'catalog]
          ['datomic.fulltext-index :as 'ftindex]
          ['datomic.cast2slf4j :as 'cast2slf4j])
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.db.IDbImpl)
        (clojure.core/import 'java.lang.ref.WeakReference)
        (clojure.core/import 'java.net.URI)
        (clojure.core/import 'java.util.Map)
        (clojure.core/import 'java.util.Queue)
        (clojure.core/import 'java.util.concurrent.ArrayBlockingQueue)
        (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue))))
  (set! *warn-on-reflection* true)
  (.setMeta
    (clojure.lang.RT/var "datomic.peer" "->ConnectionState")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.peer" "map->ConnectionState")
    {:declared true, :column (int 1)})
  (defrecord
    ConnectionState
    [connector notifier updater cleanup]
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (future-call
        (fn fn__19175
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8765__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")
                        ex t__8765__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__8765__auto__)
                  nil))))))))
  (clojure.core/import 'datomic.peer.ConnectionState)
  (defn ->ConnectionState
    ([connector notifier updater cleanup]
      (datomic.peer.ConnectionState. connector notifier updater cleanup)))
  (reset-meta!
    #'->ConnectionState
    (assoc
      {:arglists (clojure.core/list ['connector 'notifier 'updater 'cleanup]), :column (int 1)}
      :name
      '->ConnectionState
      :ns
      *ns*))
  (defn map->ConnectionState
    ([m__8001__auto__]
      (ConnectionState/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->ConnectionState
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
      :name
      'map->ConnectionState
      :ns
      *ns*))
  (defn get-cstate
    ([state_ref or_else]
      (let [state (deref (deref state_ref) 0 or_else)]
        (if (= state :datomic.peer/shutdown)
          (error/state :db.error/connection-released "The connection has been released.")
          state))))
  (reset-meta!
    #'get-cstate
    (assoc
      {:arglists (clojure.core/list ['state-ref 'or-else]), :column (int 1)}
      :name
      'get-cstate
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      RemoteConnection
      (get-cluster [_])
      (get-olookup [_])
      (create-connection-state
        [_ cluster-conf endpoint mode]
        "mode is :initial for first connect from this peer, :reconnect after"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.peer" "RemoteConnection")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'RemoteConnection :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-cluster
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.peer" "RemoteConnection"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.peer" "get-cluster")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-olookup
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.peer" "RemoteConnection"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.peer" "get-olookup")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*)))
    (let [protocol_signature__7468 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'create-connection-state
                                        {:arglists
                                         (clojure.core/list ['_ 'cluster-conf 'endpoint 'mode])}),
                                      :arglists
                                      (clojure.core/list ['_ 'cluster-conf 'endpoint 'mode]),
                                      :doc
                                      "mode is :initial for first connect from this peer, :reconnect after"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.peer" "RemoteConnection"))
          protocol_method_name__7469 (with-meta
                                       (:name protocol_signature__7468)
                                       protocol_signature__7468)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.peer" "create-connection-state")
        (assoc protocol_signature__7468 :name protocol_method_name__7469 :ns *ns*))))
  (defn await-tx-result
    ([prom]
      (let [result (try
                     (deref prom (config/property "datomic.txTimeoutMsec") prom)
                     (catch java.lang.Throwable t t))]
        (if (= prom result)
          (error/raise :db.error/transaction-timeout "Transaction timed out.")
          prom))))
  (reset-meta!
    #'await-tx-result
    (assoc
      {:arglists (clojure.core/list ['prom]), :column (int 1)}
      :name
      'await-tx-result
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.peer" "connection-lock") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.peer" "connection-lock") (java.lang.Object.))
  (.setMeta (clojure.lang.RT/var "datomic.peer" "connection-cache") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.peer" "connection-cache") (cache/create-soft-limited))
  (let [protocol_metadata__7470 {:column (int 1)}]
    (defprotocol
      TWatcher
      (sync-t [_ t] "Returns future that will get a database where (<= t (f db))")
      (sync-background-t
        [_ btype t]
        "Returns future that will get a database where background jobs requested up\nto t are completed.  Note that this may be before indexing is complete to t.")
      (wait-for-future-t [_ t] "Return a promise that will be delivered by release-pending-syncs.")
      (release-pending-syncs [_ new-db] "Release any pending syncs on this db value"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.peer" "TWatcher")
      (assoc (assoc protocol_metadata__7470 :doc nil) :name 'TWatcher :ns *ns*))
    (let [protocol_signature__7471 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'sync-t {:arglists (clojure.core/list ['_ 't])}),
                                      :arglists (clojure.core/list ['_ 't]),
                                      :doc
                                      "Returns future that will get a database where (<= t (f db))"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.peer" "TWatcher"))
          protocol_method_name__7472 (with-meta
                                       (:name protocol_signature__7471)
                                       protocol_signature__7471)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.peer" "sync-t")
        (assoc protocol_signature__7471 :name protocol_method_name__7472 :ns *ns*)))
    (let [protocol_signature__7473 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'sync-background-t
                                        {:arglists (clojure.core/list ['_ 'btype 't])}),
                                      :arglists (clojure.core/list ['_ 'btype 't]),
                                      :doc
                                      "Returns future that will get a database where background jobs requested up\nto t are completed.  Note that this may be before indexing is complete to t."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.peer" "TWatcher"))
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.peer" "sync-background-t")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*)))
    (let [protocol_signature__7475 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'wait-for-future-t
                                        {:arglists (clojure.core/list ['_ 't])}),
                                      :arglists (clojure.core/list ['_ 't]),
                                      :doc
                                      "Return a promise that will be delivered by release-pending-syncs."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.peer" "TWatcher"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.peer" "wait-for-future-t")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*)))
    (let [protocol_signature__7477 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'release-pending-syncs
                                        {:arglists (clojure.core/list ['_ 'new-db])}),
                                      :arglists (clojure.core/list ['_ 'new-db]),
                                      :doc "Release any pending syncs on this db value"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.peer" "TWatcher"))
          protocol_method_name__7478 (with-meta
                                       (:name protocol_signature__7477)
                                       protocol_signature__7477)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.peer" "release-pending-syncs")
        (assoc protocol_signature__7477 :name protocol_method_name__7478 :ns *ns*))))
  (deftype
    TWatcherImpl
    [q db_ref f lck]
    datomic.peer.TWatcher
    (release-pending-syncs
      [this new_db]
      (locking lck
       (do
         (let [G__19313 (.peek ^java.util.Queue q)
               map__19314 G__19313
               map__19314 (if (seq? map__19314)
                            (if (next map__19314)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__19314))
                              (if (seq map__19314) (first map__19314) {}))
                            map__19314)
               t (get map__19314 :t)
               prom (get map__19314 :prom)]
           (loop [G__19313 G__19313]
             (let [map__19315 G__19313
                   map__19315 (if (seq? map__19315)
                                (if (next map__19315)
                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                    (to-array map__19315))
                                  (if (seq map__19315) (first map__19315) {}))
                                map__19315)
                   t (get map__19315 :t)
                   prom (get map__19315 :prom)]
               (when (and t (<= t (^clojure.lang.IFn f new_db)))
                 (deliver prom new_db)
                 (.remove ^java.util.Queue q)
                 (recur (.peek ^java.util.Queue q))))))
         nil)))
    (sync-background-t
      [this btype t_or_tx]
      (let [t (db/eid->eidx (long ^java.lang.Number t_or_tx)) db (deref db_ref)]
        (if (and db (<= t (^clojure.lang.IFn f db)))
          (promise/delivered db)
          (locking lck
           (cond
             (and db (<= t (^clojure.lang.IFn f db))) (promise/delivered db)
             (and db (<= t (:basisT db))) (let [temp__5823__auto__ (let
                                                                     [G__19311
                                                                      (db/ts-needing-index
                                                                        db
                                                                        btype
                                                                        (long t))
                                                                      G__19311
                                                                      (some-> G__19311 (seq))]
                                                                     (when-not
                                                                       (nil? G__19311)
                                                                       (apply max G__19311)))]
                                            (if temp__5823__auto__
                                              (let [actual_t temp__5823__auto__]
                                                (wait-for-future-t this actual_t))
                                              (promise/delivered db)))
             :default (do (wait-for-future-t this (long t))))))))
    (sync-t
      [this t_or_tx]
      (let [t (db/eid->eidx (long ^java.lang.Number t_or_tx)) db (deref db_ref)]
        (if (and db (<= t (^clojure.lang.IFn f db)))
          (promise/delivered db)
          (locking lck
           (if (and db (<= t (^clojure.lang.IFn f db)))
             (promise/delivered db)
             (wait-for-future-t this (long t)))))))
    (wait-for-future-t
      [this t]
      (let [prom (promise/settable-future)] (.add ^java.util.Queue q {:t t, :prom prom}) prom)))
  (clojure.core/import 'datomic.peer.TWatcherImpl)
  (defn ->TWatcherImpl ([q db_ref f lck] (datomic.peer.TWatcherImpl. q db_ref f lck)))
  (reset-meta!
    #'->TWatcherImpl
    (assoc
      {:arglists (clojure.core/list ['q 'db-ref 'f 'lck]), :column (int 1)}
      :name
      '->TWatcherImpl
      :ns
      *ns*))
  (defn create-t-watcher
    ([f db_ref]
      (datomic.peer.TWatcherImpl.
        (java.util.PriorityQueue.
          (int 11)
          (reify
            java.util.Comparator
            (^int compare [this o1 o2] (clojure.lang.Util/compare (:t o1) (:t o2)))))
        db_ref
        f
        (java.lang.Object.))))
  (reset-meta!
    #'create-t-watcher
    (assoc
      {:arglists (clojure.core/list ['f 'db-ref]), :column (int 1)}
      :name
      'create-t-watcher
      :ns
      *ns*))
  (defn accept-new-data
    ([db data]
      (let [nextT (.nextT ^datomic.Database db)
            temp__5823__auto__ (seq
                                 (drop-while
                                   (fn fn__19337
                                     ([p1__19336#]
                                       (< (.getT ^datomic.impl.db.IDatum p1__19336#) nextT)))
                                   data))]
        (if temp__5823__auto__
          (let [newdata temp__5823__auto__]
            (.acceptDataCheck ^datomic.db.IDbImpl db newdata false))
          db))))
  (reset-meta!
    #'accept-new-data
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'data]),
       :column (int 1)}
      :name
      'accept-new-data
      :ns
      *ns*))
  (defn transactor-unavailable
    ([]
      (let [msg "Transactor not available"]
        (ex-info
          msg
          #:cognitect.anomalies{:category :cognitect.anomalies/unavailable, :message msg}))))
  (reset-meta!
    #'transactor-unavailable
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'transactor-unavailable
      :ns
      *ns*))
  (defn peer-queue-exceeded
    ([]
      (let [msg__8527__auto__ "Peer work queue limit exceeded, transactor may be unavailable."]
        (clojure.lang.ExceptionInfo.
          (str :db.error/peer-queue-exceeded " " msg__8527__auto__)
          (error/anomalize
            (assoc nil :db/error :db.error/peer-queue-exceeded)
            clojure.lang.ExceptionInfo
            msg__8527__auto__)))))
  (reset-meta!
    #'peer-queue-exceeded
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'peer-queue-exceeded
      :ns
      *ns*))
  (defn fail-pending-txes
    ([unsent_updates_queue pending_txes]
      (queue/clear unsent_updates_queue)
      (loop [seq_19344 (seq (cache/cache-keys pending_txes))
             chunk_19345 nil
             count_19346 0
             i_19347 0]
        (if (< i_19347 count_19346)
          (let [k (.nth ^clojure.lang.Indexed chunk_19345 (int i_19347))]
            (let [temp__5825__auto__ (cache/remove pending_txes k)]
              (when temp__5825__auto__
                (let [prom temp__5825__auto__] (deliver prom (transactor-unavailable)))))
            (recur seq_19344 chunk_19345 count_19346 (inc i_19347)))
          (let [temp__5825__auto__ (seq seq_19344)]
            (when temp__5825__auto__
              (let [seq_19344 temp__5825__auto__]
                (if (chunked-seq? seq_19344)
                  (let [c__6090__auto__ (chunk-first seq_19344)]
                    (recur
                      (chunk-rest seq_19344)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [k (first seq_19344)]
                    (let [temp__5825__auto__ (cache/remove pending_txes k)]
                      (when temp__5825__auto__
                        (let [prom temp__5825__auto__] (deliver prom (transactor-unavailable)))))
                    (recur (next seq_19344) nil 0 0))))))))))
  (reset-meta!
    #'fail-pending-txes
    (assoc
      {:arglists (clojure.core/list ['unsent-updates-queue 'pending-txes]), :column (int 1)}
      :name
      'fail-pending-txes
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.peer" "start-kv-cache-delay")
    {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.peer" "start-kv-cache-delay")
    (delay (cluster-stack/start-kv-cache)))
  (.setMeta
    (clojure.lang.RT/var "datomic.peer" "stop-connection")
    {:declared true, :column (int 1)})
  (deftype
    Connection
    [db_id
     cluster
     olookup
     state_ref
     db_ref
     pending_txes
     unsent_updates_queue
     lucene_queue
     tx_report_queue
     tx_watcher
     idx_watcher
     bg_watcher]
    java.lang.Object
    datomic.peer.RemoteConnection
    datomic.reconnector2.Reconnectable
    datomic.monitor.Metrics
    datomic.Connection
    datomic.common.AsyncShutdown
    datomic.connector.NotificationHandler
    (notify-index
      [this]
      (future-call
        (fn fn__19412
          ([]
            (try
              (let [idxroot (index/find-index-root-id cluster)
                    idx (index/load-index olookup idxroot)
                    adopt_db (db/db db_id idx)
                    temp__5823__auto__ (adopter/adopt-index db_ref adopt_db)]
                (if temp__5823__auto__
                  (let [map__19413 temp__5823__auto__
                        map__19413 (if (seq? map__19413)
                                     (if (next map__19413)
                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                         (to-array map__19413))
                                       (if (seq map__19413) (first map__19413) {}))
                                     map__19413)
                        basis_db (get map__19413 :basis-db)
                        adopted_db (get map__19413 :adopted-db)
                        msec (get map__19413 :msec)
                        iterations (get map__19413 :iterations)]
                    (release-pending-syncs tx_watcher adopted_db)
                    (release-pending-syncs idx_watcher adopted_db)
                    (release-pending-syncs bg_watcher adopted_db)
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                        (.info
                          ^org.slf4j.Logger logger
                          (logger/process
                            {:event :peer/adopt-completed,
                             :previous-root-id (:index-root-id basis_db),
                             :previous-t (:indexBasisT basis_db),
                             :root-id (:index-root-id adopted_db),
                             :t (:indexBasisT adopted_db),
                             :msec msec,
                             :iterations iterations})))
                      nil))
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process
                          {:event :peer/adopt-failed,
                           :root-id (:index-root-id adopt_db),
                           :t (:indexBasisT adopt_db)})))
                    nil)))
              (catch
                java.lang.Throwable
                t__8765__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")
                        ex t__8765__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__8765__auto__)
                  nil)))))))
    (notify-db
      [this db]
      (do
        (reset! db_ref db)
        (release-pending-syncs tx_watcher db)
        (release-pending-syncs idx_watcher db)
        (release-pending-syncs bg_watcher db)))
    (notify-error
      [this id o]
      (let [prom (cache/remove pending_txes id)]
        (when prom
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug
                ^org.slf4j.Logger logger
                (logger/process {:event :peer/transact, :uuid id, :phase :end})))
            nil)
          (deliver
            prom
            (if (instance? java.lang.Throwable o) o (error/deserialize-exception o))))))
    (notify-data
      [this msg]
      (let [map__19404 msg
            map__19404 (if (seq? map__19404)
                         (if (next map__19404)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19404))
                           (if (seq map__19404) (first map__19404) {}))
                         map__19404)
            id (get map__19404 :id)
            data (get map__19404 :data)
            tempids (get map__19404 :tempids)
            io_stats (get map__19404 :io-stats)
            start (java.lang.System/currentTimeMillis)
            prom (cache/remove pending_txes id)
            map__19405 (meta prom)
            map__19405 (if (seq? map__19405)
                         (if (next map__19405)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19405))
                           (if (seq map__19405) (first map__19405) {}))
                         map__19405)
            io_context (get map__19405 :io-context)
            old_db (deref db_ref)
            new_db (let [m_19406 {:event :peer/accept-new, :id id}
                         ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                        "datomic.peer")]
                                           (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                             (.debug
                                               ^org.slf4j.Logger logger
                                               (logger/process (assoc m_19406 :phase :begin))))
                                           nil)
                         start__8599__auto__ (java.lang.System/nanoTime)
                         result__8600__auto__ (try
                                                {:returned (swap! db_ref accept-new-data data)}
                                                (catch
                                                  java.lang.Throwable
                                                  t__8601__auto__
                                                  {:threw t__8601__auto__}))
                         elapsed_19407 (- (java.lang.System/nanoTime) start__8599__auto__)
                         msec_19408 (logger/format-as-msec (long elapsed_19407))]
                     (monitor/add-stat :PeerAcceptNewMsec msec_19408)
                     (let [endmsg__8602__auto__ (merge
                                                  (assoc m_19406 :msec msec_19408 :phase :end)
                                                  (when (:threw result__8600__auto__)
                                                    {:threw
                                                     (class (:threw result__8600__auto__))}))
                           logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                         (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                       nil)
                     (if (contains? result__8600__auto__ :returned)
                       (:returned result__8600__auto__)
                       (do (throw (:threw result__8600__auto__)) nil)))
            txrq (deref tx_report_queue)
            report (when (or prom txrq)
                     (cond->
                       {:db-before old_db, :db-after new_db, :tx-data data, :tempids tempids}
                       io_context
                       (assoc :io-stats io_stats)))]
        (queue/put lucene_queue data)
        (when prom
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info
                ^org.slf4j.Logger logger
                (logger/process {:event :peer/transact, :uuid id, :phase :end})))
            nil)
          (deliver prom report))
        (when txrq (.add ^java.util.Queue txrq report))
        (release-pending-syncs tx_watcher new_db)
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:event :peer/notify-data,
                 :msec (long (- (java.lang.System/currentTimeMillis) start)),
                 :id id})))
          nil)))
    (notify-sync
      [this id]
      (let [prom (cache/remove pending_txes id)]
        (when prom
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug
                ^org.slf4j.Logger logger
                (logger/process {:event :peer/sync, :uuid id, :phase :end})))
            nil)
          (deliver prom (deref db_ref)))))
    (^void gcStorage
      [this ^java.util.Date older_than]
      (do
        (let [temp__5823__auto__ (:connector (get-cstate state_ref nil))]
          (when temp__5823__auto__
            (let [connector temp__5823__auto__]
              (conn/admin-request connector :request-gc {:db-id db_id, :older-than older_than}))))
        nil))
    (^void removeTxReportQueue [this] (do (reset! tx_report_queue nil) nil))
    (^java.util.concurrent.BlockingQueue txReportQueue
      [this]
      (or
        (deref tx_report_queue)
        (swap!
          tx_report_queue
          (fn fn__19402
            ([p1__19355#]
              (if p1__19355# p1__19355# (java.util.concurrent.LinkedBlockingQueue.)))))))
    (^datomic.ListenableFuture transactAsync
      [this ^java.util.List txdata options]
      (if (get-cstate state_ref nil)
        (try
          (let [tx_procargs (tx/create-procargs txdata options)
                sync_key (common/getx tx_procargs :id)
                p (with-meta (promise/settable-future) options)]
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process {:event :peer/transact, :uuid sync_key, :phase :start})))
              nil)
            (cache/put pending_txes sync_key p)
            (when-not (queue/put unsent_updates_queue tx_procargs)
              (deliver p (peer-queue-exceeded)))
            p)
          (catch java.lang.Throwable t (promise/delivered t)))
        (do (throw (transactor-unavailable)) nil)))
    (^datomic.ListenableFuture transactAsync
      [this ^java.util.List txdata]
      (.transactAsync this ^java.util.List txdata nil))
    (^datomic.ListenableFuture transact
      [this ^java.util.List txdata options]
      (await-tx-result (.transactAsync this ^java.util.List txdata options)))
    (^datomic.ListenableFuture transact
      [this ^java.util.List txdata]
      (await-tx-result (.transactAsync this ^java.util.List txdata)))
    (^datomic.ListenableFuture syncExcise
      [this ^long t]
      (sync-background-t bg_watcher :excise (long t)))
    (^datomic.ListenableFuture syncSchema
      [this ^long t]
      (sync-background-t bg_watcher :schema (long t)))
    (^datomic.ListenableFuture syncIndex [this ^long t] (sync-t idx_watcher (long t)))
    (^datomic.ListenableFuture sync [this ^long t] (sync-t tx_watcher (long t)))
    (^datomic.ListenableFuture sync
      [this]
      (if (get-cstate state_ref nil)
        (let [s {:id (common/rand-uuid), :type :sync} sync_key (:id s) p (promise/settable-future)]
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug
                ^org.slf4j.Logger logger
                (logger/process {:event :peer/sync, :uuid sync_key, :phase :start})))
            nil)
          (cache/put pending_txes sync_key p)
          (when-not (queue/put unsent_updates_queue s) (deliver p (peer-queue-exceeded)))
          p)
        (do (throw (transactor-unavailable)) nil)))
    (^datomic.Log log [this] (log/create-log-val cluster olookup (deref db_ref)))
    (^datomic.Database db [this] (do (get-cstate state_ref nil) (deref db_ref)))
    (^boolean requestIndex
      [this]
      (.booleanValue
        (let [temp__5823__auto__ (:connector (get-cstate state_ref nil))]
          (if temp__5823__auto__
            (let [connector temp__5823__auto__]
              (conn/admin-request connector :request-index db_id)
              true)
            (do (throw (transactor-unavailable)) nil)))))
    (^void release
      [this]
      (do
        (locking connection-lock
         (loop [seq_19394 (seq (cache/cache-keys connection-cache))
                chunk_19395 nil
                count_19396 0
                i_19397 0]
           (if (< i_19397 count_19396)
             (let [k (.nth ^clojure.lang.Indexed chunk_19395 (int i_19397))]
               (when (= this (get connection-cache k)) (cache/remove connection-cache k))
               (recur seq_19394 chunk_19395 count_19396 (inc i_19397)))
             (let [temp__5825__auto__ (seq seq_19394)]
               (when temp__5825__auto__
                 (let [seq_19394 temp__5825__auto__]
                   (if (chunked-seq? seq_19394)
                     (let [c__6090__auto__ (chunk-first seq_19394)]
                       (recur
                         (chunk-rest seq_19394)
                         c__6090__auto__
                         (int (count c__6090__auto__))
                         (int 0)))
                     (let [k (first seq_19394)]
                       (when (= this (get connection-cache k)) (cache/remove connection-cache k))
                       (recur (next seq_19394) nil 0 0)))))))))
        (deref (common/async-shutdown this))
        nil))
    (metrics
      [this]
      (let [db (deref db_ref)]
        (merge
          {:unsent-updates-queue (java.lang.Integer/valueOf (int (count unsent_updates_queue))),
           :pending-txes (cache/fast-count pending_txes)}
          (when db
            {:next-t (long (.nextT ^datomic.Database db)),
             :basis-t (long (.basisT ^datomic.Database db)),
             :index-rev (:index-rev db)}))))
    (^java.lang.String toString [this] (str (assoc (monitor/metrics this) :db-id db_id)))
    (reconnect
      [this]
      (let [temp__5823__auto__ (deref state_ref 10000 nil)]
        (if temp__5823__auto__
          (let [o temp__5823__auto__] (recon/reconnect o))
          (do (throw (java.lang.IllegalStateException. "Peer reconnector setup failed.")) nil))))
    (create-connection-state
      [this cluster_conf endpoint mode]
      (do
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                (merge
                  {:event :peer/connect-transactor}
                  (select-keys endpoint [:host :alt-host :port :version])))))
          nil)
        (let [m_19357 {:event :peer/create-connection-impl,
                       :cluster-conf (uri/loggable-cluster-conf cluster_conf)}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_19357 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
                                     {:returned
                                      (let [conn_ref (java.lang.ref.WeakReference. this)
                                            recon (delay
                                                    (let [temp__5825__auto__ (.get
                                                                               ^java.lang.ref.Reference conn_ref)]
                                                      (when temp__5825__auto__
                                                        (let [conn temp__5825__auto__]
                                                          (recon/reconnect conn)))))
                                            failure_handler (error/runonce
                                                              (fn
                                                                fn__19364
                                                                ([ex]
                                                                  (let
                                                                    [logger
                                                                     (org.slf4j.LoggerFactory/getLogger
                                                                       "datomic.peer")
                                                                     ex ex]
                                                                    (when
                                                                      (.isInfoEnabled
                                                                        ^org.slf4j.Logger logger)
                                                                      (.info
                                                                        ^org.slf4j.Logger logger
                                                                        (logger/process
                                                                          {:event
                                                                           :peer/transactor-connection-failed})
                                                                        ex)
                                                                      (logger/caused-by logger ex))
                                                                    nil)
                                                                  (deref recon))))
                                            shutdown (comp deref common/async-shutdown)
                                            connector (conn/create-transactor-hornet-connector
                                                        cluster_conf
                                                        endpoint)
                                            start_result (conn/admin-request*
                                                           connector
                                                           :start-database
                                                           db_id
                                                           (if (= mode :initial) 60000 5000))]
                                        (when (:db/error start_result)
                                          (when (=
                                                  (:message start_result)
                                                  "database does not exist")
                                            (locking connection-lock
                                             (stop-connection cluster_conf)))
                                          (throw
                                            (ex-info
                                              (:message start_result)
                                              (dissoc start_result :message))))
                                        (let [_ nil
                                              load_db (fn load_db
                                                        ([basis_db]
                                                          (db-io/load-db-from-basis
                                                            cluster
                                                            olookup
                                                            (common/getx cluster_conf :db-id)
                                                            basis_db)))
                                              db (^clojure.lang.IFn load_db (deref db_ref))
                                              notifier (conn/create-notifier
                                                         connector
                                                         this
                                                         failure_handler)
                                              cleanup (error/runonce
                                                        (fn fn__19371
                                                          ([]
                                                            (try
                                                              (^clojure.lang.IFn shutdown notifier)
                                                              (catch
                                                                java.lang.Throwable
                                                                t__8574__auto__
                                                                (error/report t__8574__auto__)))
                                                            nil)))]
                                          (try
                                            (let [_ (conn/notify-db
                                                      this
                                                      (^clojure.lang.IFn load_db db))
                                                  cleanup (error/runonce
                                                            (fn
                                                              fn__19376
                                                              ([]
                                                                (try
                                                                  (identity _)
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8574__auto__
                                                                    (error/report
                                                                      t__8574__auto__)))
                                                                (^clojure.lang.IFn cleanup))))]
                                              (try
                                                (let [updater (conn/start-updater
                                                                connector
                                                                unsent_updates_queue
                                                                this
                                                                failure_handler)
                                                      cleanup (error/runonce
                                                                (fn
                                                                  fn__19380
                                                                  ([]
                                                                    (try
                                                                      (^clojure.lang.IFn shutdown
                                                                        updater)
                                                                      (catch
                                                                        java.lang.Throwable
                                                                        t__8574__auto__
                                                                        (error/report
                                                                          t__8574__auto__)))
                                                                    (^clojure.lang.IFn cleanup))))]
                                                  (try
                                                    (do
                                                      (conn/start notifier)
                                                      (let [state
                                                            (datomic.peer.ConnectionState.
                                                              connector
                                                              notifier
                                                              updater
                                                              cleanup)]
                                                        (cleanup/register-cleanup
                                                          (deref cleanup/shared-manager-ref)
                                                          state
                                                          cleanup)
                                                        state))
                                                    (catch
                                                      java.lang.Throwable
                                                      t__8575__auto__
                                                      (do
                                                        (try
                                                          (^clojure.lang.IFn shutdown updater)
                                                          (catch
                                                            java.lang.Throwable
                                                            t__8574__auto__
                                                            (error/report t__8574__auto__)))
                                                        (throw
                                                          ^java.lang.Throwable t__8575__auto__)
                                                        nil))))
                                                (catch
                                                  java.lang.Throwable
                                                  t__8575__auto__
                                                  (do
                                                    (try
                                                      (identity _)
                                                      (catch
                                                        java.lang.Throwable
                                                        t__8574__auto__
                                                        (error/report t__8574__auto__)))
                                                    (throw ^java.lang.Throwable t__8575__auto__)
                                                    nil))))
                                            (catch
                                              java.lang.Throwable
                                              t__8575__auto__
                                              (do
                                                (try
                                                  (^clojure.lang.IFn shutdown notifier)
                                                  (catch
                                                    java.lang.Throwable
                                                    t__8574__auto__
                                                    (error/report t__8574__auto__)))
                                                (throw ^java.lang.Throwable t__8575__auto__)
                                                nil)))))}
                                     (catch
                                       java.lang.Throwable
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_19358 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_19359 (logger/format-as-msec (long elapsed_19358))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_19357 :msec msec_19359 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (do (throw (:threw result__8600__auto__)) nil)))))
    (get-olookup [this] olookup)
    (get-cluster [this] cluster)
    (async-shutdown
      [this]
      (do
        (cluster/close cluster)
        (when-not (queue/offer lucene_queue :done 1000)
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
            (when (.isWarnEnabled ^org.slf4j.Logger logger)
              (.warn
                ^org.slf4j.Logger logger
                (logger/process "Timed out shutting down lucene integration thread")))
            nil))
        (common/async-shutdown (deref state_ref)))))
  (clojure.core/import 'datomic.peer.Connection)
  (defn ->Connection
    ([db_id
      cluster
      olookup
      state_ref
      db_ref
      pending_txes
      unsent_updates_queue
      lucene_queue
      tx_report_queue
      tx_watcher
      idx_watcher
      bg_watcher]
      (datomic.peer.Connection.
        db_id
        cluster
        olookup
        state_ref
        db_ref
        pending_txes
        unsent_updates_queue
        lucene_queue
        tx_report_queue
        tx_watcher
        idx_watcher
        bg_watcher)))
  (reset-meta!
    #'->Connection
    (assoc
      {:arglists
       (clojure.core/list
         ['db-id
          'cluster
          'olookup
          'state-ref
          'db-ref
          'pending-txes
          'unsent-updates-queue
          'lucene-queue
          'tx-report-queue
          'tx-watcher
          'idx-watcher
          'bg-watcher]),
       :column (int 1)}
      :name
      '->Connection
      :ns
      *ns*))
  (defn unsupported-operation
    ([op] (error/arg :db.error/read-only-connection (str op " unsupported"))))
  (reset-meta!
    #'unsupported-operation
    (assoc
      {:private true, :arglists (clojure.core/list ['op]), :column (int 1)}
      :name
      'unsupported-operation
      :ns
      *ns*))
  ;; Immutable storage-backed connection used for backup and read-only URIs.
  ;; Transaction submission, report queues, indexing requests, and storage GC are unavailable.
  (deftype
    StorageOnlyConnection
    [db-id cluster db log]
    datomic.Connection
    datomic.common.AsyncShutdown
    (^void gcStorage
      [this ^java.util.Date older-than]
      (do (unsupported-operation "gcStorage") nil))
    (^void removeTxReportQueue [this] (do (unsupported-operation "removeTxReportQueue") nil))
    (^java.util.concurrent.BlockingQueue txReportQueue
      [this]
      (unsupported-operation "txReportQueue"))
    (^datomic.ListenableFuture transactAsync
      [this ^java.util.List txdata options]
      (unsupported-operation "transactAsync"))
    (^datomic.ListenableFuture transactAsync
      [this ^java.util.List txdata]
      (unsupported-operation "transactAsync"))
    (^datomic.ListenableFuture transact
      [this ^java.util.List txdata options]
      (unsupported-operation "transact"))
    (^datomic.ListenableFuture transact
      [this ^java.util.List txdata]
      (unsupported-operation "transact"))
    (^boolean requestIndex [this] (.booleanValue (unsupported-operation "requestIndex")))
    (^datomic.ListenableFuture syncExcise
      [this ^long t]
      (if (<= t (:basisT db)) (promise/delivered db) (unsupported-operation "syncExcise")))
    (^datomic.ListenableFuture syncSchema
      [this ^long t]
      (if (<= t (:basisT db)) (promise/delivered db) (unsupported-operation "syncSchema")))
    (^datomic.ListenableFuture syncIndex
      [this ^long t]
      (if (<= t (:basisT db)) (promise/delivered db) (unsupported-operation "syncIndex")))
    (^datomic.ListenableFuture sync
      [this ^long t]
      (if (<= t (:basisT db)) (promise/delivered db) (unsupported-operation "sync")))
    (^datomic.ListenableFuture sync [this] (unsupported-operation "sync"))
    (^datomic.Log log [this] ^datomic.Log log)
    (^datomic.Database db [this] ^datomic.Database db)
    (^void release [this] (do (common/async-shutdown this) nil))
    (async-shutdown [this] (when cluster (cluster/close cluster))))
  (clojure.core/import 'datomic.peer.StorageOnlyConnection)
  (defn ->StorageOnlyConnection
    ([db-id cluster db log] (datomic.peer.StorageOnlyConnection. db-id cluster db log)))
  (reset-meta!
    #'->StorageOnlyConnection
    (assoc
      {:arglists (clojure.core/list ['db-id 'cluster 'db 'log]), :column (int 1)}
      :name
      '->StorageOnlyConnection
      :ns
      *ns*))
  (def MAX_UNSENT_QUEUE 128)
  (reset-meta!
    #'MAX_UNSENT_QUEUE
    (assoc {:const true, :column (int 1)} :name 'MAX_UNSENT_QUEUE :ns *ns*))
  (def MAX_LUCENE_QUEUE 128)
  (reset-meta!
    #'MAX_LUCENE_QUEUE
    (assoc {:const true, :column (int 1)} :name 'MAX_LUCENE_QUEUE :ns *ns*))
  (defn integrate-lucene
    ([db_ref q]
      (loop [tx (queue/take q)]
        (when-not (= tx :done)
          (let [vec__19441 (loop [txes [tx] ntx (queue/poll q)]
                             (if (or (= ntx :done) (nil? ntx))
                               [txes ntx]
                               (recur (conj txes ntx) (queue/poll q))))
                txes (nth vec__19441 (int 0) nil)
                ntx (nth vec__19441 (int 1) nil)
                db (deref db_ref)
                oft (:fulltext (:memidx db))
                nft (ftindex/update-fulltext
                      oft
                      (filter
                        (fn fn__19447
                          ([p1__19440#]
                            (db/fulltext?
                              db
                              (java.lang.Integer/valueOf
                                (int (.getA ^datomic.impl.db.IDatum p1__19440#))))))
                        (apply concat txes)))]
            (swap! db_ref assoc-in [:memidx :fulltext] nft)
            (monitor/add-stat :PeerFulltextBatch (java.lang.Integer/valueOf (int (count txes))))
            (when-not (= ntx :done) (recur (queue/take q))))))))
  (reset-meta!
    #'integrate-lucene
    (assoc
      {:arglists (clojure.core/list ['db-ref 'q]), :column (int 1)}
      :name
      'integrate-lucene
      :ns
      *ns*))
  ;; Builds a long-lived Peer connection, installs bounded work queues, and reconnects indefinitely
  ;; as the active transactor endpoint changes.
  (defn create-connection
    ([cluster-conf]
      (let [unsent_updates_queue (java.util.concurrent.ArrayBlockingQueue. (int 128))
            lucene_queue (java.util.concurrent.ArrayBlockingQueue. (int 128))
            state_ref (promise)
            cluster (coord/create-db-cluster cluster-conf)
            system_cluster (coord/create-system-cluster cluster-conf)
            olookup (domain/system-cache-olookup cluster)
            pending_txes (cache/create-response-map 60)
            db_ref (atom nil)
            conn (datomic.peer.Connection.
                   (common/getx cluster-conf :db-id)
                   cluster
                   olookup
                   state_ref
                   db_ref
                   pending_txes
                   unsent_updates_queue
                   lucene_queue
                   (atom nil)
                   (create-t-watcher :basisT db_ref)
                   (create-t-watcher :indexBasisT db_ref)
                   (create-t-watcher :indexBasisT db_ref))
            endpoint_ref (atom nil)
            backoff (math/create-exponential {:x1 1, :y1 1000, :x2 10, :y2 120000})
            reconnect_fn (fn reconnect_fn
                           ([mode]
                             (fail-pending-txes unsent_updates_queue pending_txes)
                             (let [new_endpoint (coord/lookup-compatible-transactor-endpoint
                                                  system_cluster)]
                               (if (= new_endpoint (deref endpoint_ref))
                                 {:old-endpoint
                                  (select-keys new_endpoint [:host :port :timestamp])}
                                 (do
                                   (reset! endpoint_ref new_endpoint)
                                   (create-connection-state
                                     conn
                                     cluster-conf
                                     new_endpoint
                                     mode))))))
            state (^clojure.lang.IFn reconnect_fn :initial)]
        (deliver
          state_ref
          (recon/reconnector-ref
            :state
            state
            :reconnect
            (fn fn__19453
              ([]
                (common/retry-fn
                  (partial reconnect_fn :reconnect)
                  :pred
                  (fn fn__19454
                    ([p1__19450#] (not (instance? datomic.peer.ConnectionState p1__19450#))))
                  :backoff
                  1000
                  :log-retry
                  common/log-retry
                  :max-retries
                  (long java.lang.Long/MAX_VALUE))))
            :cleanup
            common/async-shutdown
            :shutdown-state
            :datomic.peer/shutdown))
        (doto
          (java.lang.Thread.
            (fn fn__19458
              ([]
                (try
                  (do
                    (integrate-lucene db_ref lucene_queue)
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                        (.info
                          ^org.slf4j.Logger logger
                          (logger/process "Shutting down lucene integration thread")))
                      nil))
                  (catch
                    java.lang.Throwable
                    t
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer") ex t]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn
                          ^org.slf4j.Logger logger
                          (logger/process "Peer fulltext integration failed")
                          ^java.lang.Throwable ex)
                        (logger/caused-by logger ex))
                      nil)))))
            "Datomic Fulltext Integration")
          (.setDaemon (boolean (.booleanValue true)))
          (.start))
        conn)))
  (reset-meta!
    #'create-connection
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'create-connection
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.peer" "local-dbs") {:private true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.peer" "local-dbs") (atom {}))
  (.setMeta
    (clojure.lang.RT/var "datomic.peer" "delete-local-database")
    {:declared true, :column (int 1)})
  (deftype
    LocalLog
    [db]
    datomic.Log
    (^java.lang.Iterable txRange [this start end] (log/tx-range (:memlog db) db start end)))
  (clojure.core/import 'datomic.peer.LocalLog)
  (defn ->LocalLog ([db] (datomic.peer.LocalLog. db)))
  (reset-meta!
    #'->LocalLog
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name '->LocalLog :ns *ns*))
  (deftype
    LocalConnection
    [dbname db_ref tx_report_queue released tx_watcher]
    datomic.Connection
    (^void gcStorage [this ^java.util.Date older_than] nil)
    (^void removeTxReportQueue [this] (do (reset! tx_report_queue nil) nil))
    (^java.util.concurrent.BlockingQueue txReportQueue
      [this]
      (or
        (deref tx_report_queue)
        (swap!
          tx_report_queue
          (fn fn__19470
            ([p1__19466#]
              (if p1__19466# p1__19466# (java.util.concurrent.LinkedBlockingQueue.)))))))
    (^datomic.ListenableFuture transactAsync
      [this ^java.util.List txdata]
      (if (realized? released)
        (error/state :db.error/connection-released "The connection has been released.")
        (locking this
         (let [db (deref db_ref)
               report (try (db/with-tx+opts db txdata nil) (catch java.lang.Throwable e e))
               txrq (deref tx_report_queue)]
           (when-not (instance? java.lang.Throwable report)
             (reset! db_ref (:db-after report))
             (when txrq (.add ^java.util.Queue txrq report))
             (release-pending-syncs tx_watcher (:db-after report)))
           (promise/delivered report)))))
    (^datomic.ListenableFuture transactAsync
      [this ^java.util.List txdata _]
      (.transactAsync this ^java.util.List txdata))
    (^datomic.ListenableFuture transact
      [this ^java.util.List txdata]
      (await-tx-result (.transactAsync this ^java.util.List txdata)))
    (^datomic.ListenableFuture transact
      [this ^java.util.List txdata _]
      (.transact this ^java.util.List txdata))
    (^datomic.ListenableFuture syncExcise
      [this ^long t]
      (sync-background-t tx_watcher :excise (long t)))
    (^datomic.ListenableFuture syncSchema
      [this ^long t]
      (sync-background-t tx_watcher :schema (long t)))
    (^datomic.ListenableFuture syncIndex [this ^long t] (sync-t tx_watcher (long t)))
    (^datomic.ListenableFuture sync [this ^long t] (sync-t tx_watcher (long t)))
    (^datomic.ListenableFuture sync
      [this]
      (if (realized? released)
        (error/state :db.error/connection-released "The connection has been released.")
        (promise/delivered (deref db_ref))))
    (^datomic.Log log [this] (->LocalLog (deref db_ref)))
    (^datomic.Database db
      [this]
      (if (realized? released)
        (error/state :db.error/connection-released "The connection has been released.")
        (deref db_ref)))
    (^boolean requestIndex [this] (.booleanValue true))
    (^void release [this] (do (delete-local-database dbname) nil)))
  (clojure.core/import 'datomic.peer.LocalConnection)
  (defn ->LocalConnection
    ([dbname db_ref tx_report_queue released tx_watcher]
      (datomic.peer.LocalConnection. dbname db_ref tx_report_queue released tx_watcher)))
  (reset-meta!
    #'->LocalConnection
    (assoc
      {:arglists (clojure.core/list ['dbname 'db-ref 'tx-report-queue 'released 'tx-watcher]),
       :column (int 1)}
      :name
      '->LocalConnection
      :ns
      *ns*))
  (defn local-connection
    ([dbname]
      (let [db (db/bootstrap-db) db_ref (atom db)]
        (datomic.peer.LocalConnection.
          dbname
          db_ref
          (atom nil)
          (promise)
          (create-t-watcher :basisT db_ref)))))
  (reset-meta!
    #'local-connection
    (assoc
      {:arglists (clojure.core/list ['dbname]), :column (int 1)}
      :name
      'local-connection
      :ns
      *ns*))
  (defn connect-local-database
    ([dbname]
      (let [dbs (deref local-dbs)]
        (or
          (get dbs dbname)
          (error/raise :db.error/db-not-found (str "Could not find " dbname " in catalog"))))))
  (reset-meta!
    #'connect-local-database
    (assoc
      {:arglists (clojure.core/list ['dbname]), :column (int 1)}
      :name
      'connect-local-database
      :ns
      *ns*))
  (defn create-local-database
    ([dbname uri]
      (let [created (atom false)]
        (swap!
          local-dbs
          (fn fn__19482
            ([dbs]
              (if (get dbs dbname)
                dbs
                (do (reset! created true) (assoc dbs dbname (local-connection dbname)))))))
        (deref created))))
  (reset-meta!
    #'create-local-database
    (assoc
      {:arglists (clojure.core/list ['dbname 'uri]), :column (int 1)}
      :name
      'create-local-database
      :ns
      *ns*))
  (defn delete-local-database
    ([dbname]
      (let [deleted (atom false)]
        (swap!
          local-dbs
          (fn fn__19485
            ([dbs]
              (let [temp__5823__auto__ (get dbs dbname)]
                (if temp__5823__auto__
                  (let [conn temp__5823__auto__]
                    (deliver (.-released ^datomic.peer.LocalConnection conn) true)
                    (reset! deleted true)
                    (dissoc dbs dbname))
                  dbs)))))
        (deref deleted))))
  (reset-meta!
    #'delete-local-database
    (assoc
      {:arglists (clojure.core/list ['dbname]), :column (int 1)}
      :name
      'delete-local-database
      :ns
      *ns*))
  (defn rename-local-database
    ([dbname newname]
      (swap!
        local-dbs
        (fn fn__19489
          ([dbs]
            (let [temp__5823__auto__ (get dbs dbname)]
              (if temp__5823__auto__
                (let [db temp__5823__auto__]
                  (if (get dbs newname)
                    (error/raise :db.error/db-exists (str newname " already exists in catalog"))
                    (assoc (dissoc dbs dbname) newname db)))
                (error/raise
                  :db.error/db-not-found
                  (str "Could not find " dbname " in catalog")))))))
      true))
  (reset-meta!
    #'rename-local-database
    (assoc
      {:arglists (clojure.core/list ['dbname 'newname]), :column (int 1)}
      :name
      'rename-local-database
      :ns
      *ns*))
  ;; Captures the current in-memory value behind the storage-only connection contract.
  (defn read-only-local-database
    ([cluster-conf]
      (let [db-name (:db-name cluster-conf)
            conn (connect-local-database db-name)
            db (deref (.-db-ref ^datomic.peer.LocalConnection conn))
            log (->LocalLog db)]
        (->StorageOnlyConnection db-name nil db log))))
  (reset-meta!
    #'read-only-local-database
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'read-only-local-database
      :ns
      *ns*))
  ;; Releases every cached connection and connector; optionally shuts down Clojure agent pools.
  (defn shutdown
    ([shutdown-clojure]
      (locking connection-lock
       (loop [seq_19495 (seq (cache/cache-keys connection-cache))
              chunk_19496 nil
              count_19497 0
              i_19498 0]
         (if (< i_19498 count_19497)
           (let [k (.nth ^clojure.lang.Indexed chunk_19496 (int i_19498))]
             (let [temp__5825__auto__ (cache/remove connection-cache k)]
               (when temp__5825__auto__
                 (let [conn temp__5825__auto__] (deref (common/async-shutdown conn)))))
             (recur seq_19495 chunk_19496 count_19497 (inc i_19498)))
           (let [temp__5825__auto__ (seq seq_19495)]
             (when temp__5825__auto__
               (let [seq_19495 temp__5825__auto__]
                 (if (chunked-seq? seq_19495)
                   (let [c__6090__auto__ (chunk-first seq_19495)]
                     (recur
                       (chunk-rest seq_19495)
                       c__6090__auto__
                       (int (count c__6090__auto__))
                       (int 0)))
                   (let [k (first seq_19495)]
                     (let [temp__5825__auto__ (cache/remove connection-cache k)]
                       (when temp__5825__auto__
                         (let [conn temp__5825__auto__] (deref (common/async-shutdown conn)))))
                     (recur (next seq_19495) nil 0 0)))))))))
      (conn/stop-all-connectors)
      (reset! cluster-stack/kv-cache-ref nil)
      (when shutdown-clojure (shutdown-agents))))
  (reset-meta!
    #'shutdown
    (assoc
      {:arglists (clojure.core/list ['shutdown-clojure]), :column (int 1)}
      :name
      'shutdown
      :ns
      *ns*))
  (defn stop-connection
    ([p__19507]
      (let [map__19508 p__19507
            map__19508 (if (seq? map__19508)
                         (if (next map__19508)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19508))
                           (if (seq map__19508) (first map__19508) {}))
                         map__19508)
            resolved-cluster-conf map__19508
            db-id (get map__19508 :db-id)]
        (when-not :db-id
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str :db-id)))))
        (let [temp__5825__auto__ (get connection-cache resolved-cluster-conf)]
          (when temp__5825__auto__
            (let [conn temp__5825__auto__] (.release ^datomic.Connection conn))))
        (cache/clear coord/db-cache))))
  (reset-meta!
    #'stop-connection
    (assoc
      {:arglists (clojure.core/list [{:keys [:db-id], :as 'resolved-cluster-conf}]),
       :column (int 1)}
      :name
      'stop-connection
      :ns
      *ns*))
  ;; Returns the cached connection for a resolved database, creating it once under the connection lock.
  (defn get-connection
    ([cluster-conf]
      (let [m_19511 {:event :peer/get-connection,
                     :cluster-conf (uri/loggable-cluster-conf cluster-conf)}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_19511 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned
                                    (let [temp__5823__auto__ (coord/resolve-db-name cluster-conf)]
                                      (if temp__5823__auto__
                                        (let [map__19515 temp__5823__auto__
                                              map__19515 (if (seq? map__19515)
                                                           (if
                                                             (next map__19515)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__19515))
                                                             (if
                                                               (seq map__19515)
                                                               (first map__19515)
                                                               {}))
                                                           map__19515)
                                              resolved-cluster-conf map__19515
                                              db-id (get map__19515 :db-id)]
                                          (locking connection-lock
                                           (let [temp__5823__auto__ (get
                                                                      connection-cache
                                                                      resolved-cluster-conf)]
                                             (if temp__5823__auto__
                                               (let [conn temp__5823__auto__] conn)
                                               (let [conn (create-connection
                                                            resolved-cluster-conf)]
                                                 (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                "datomic.peer")]
                                                   (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                     (.info
                                                       ^org.slf4j.Logger logger
                                                       (logger/process
                                                         (merge
                                                           {:event :peer/cache-connection}
                                                           (uri/loggable-cluster-conf
                                                             resolved-cluster-conf)))))
                                                   nil)
                                                 (cache/put
                                                   connection-cache
                                                   resolved-cluster-conf
                                                   conn)
                                                 conn)))))
                                        (do
                                          (throw
                                            (java.lang.RuntimeException.
                                              (str
                                                "Could not find "
                                                (:db-name cluster-conf)
                                                " in catalog")))
                                          1)))}
                                   (catch
                                     java.lang.Throwable
                                     t__8601__auto__
                                     {:threw t__8601__auto__}))
            elapsed_19512 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_19513 (logger/format-as-msec (long elapsed_19512))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_19511 :msec msec_19513 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
  (reset-meta!
    #'get-connection
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'get-connection
      :ns
      *ns*))
  ;; Loads a fixed backup snapshot as a storage-only connection.
  (defn connect-to-backup
    ([backup-uri t]
      (let [map__19528 (req/require-and-run 'datomic.backup/load-database backup-uri t)
            map__19528 (if (seq? map__19528)
                         (if (next map__19528)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19528))
                           (if (seq map__19528) (first map__19528) {}))
                         map__19528)
            db-id (get map__19528 :db-id)
            db (get map__19528 :db)
            log (get map__19528 :log)]
        (->StorageOnlyConnection db-id nil db log))))
  (reset-meta!
    #'connect-to-backup
    (assoc
      {:arglists (clojure.core/list ['backup-uri 't]), :column (int 1)}
      :name
      'connect-to-backup
      :ns
      *ns*))
  ;; Opens an uncached, fixed snapshot at the latest durable basis without contacting a transactor.
  (defn connect-to-storage
    ([cluster-conf]
      (let [map__19530 (coord/resolve-db-name cluster-conf)
            map__19530 (if (seq? map__19530)
                         (if (next map__19530)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19530))
                           (if (seq map__19530) (first map__19530) {}))
                         map__19530)
            resolved-conf map__19530
            db-id (get map__19530 :db-id)
            cluster (coord/create-db-cluster resolved-conf)
            olookup (domain/system-cache-olookup cluster)
            map__19531 (db-io/load-db-from-basis cluster olookup db-id nil true)
            map__19531 (if (seq? map__19531)
                         (if (next map__19531)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19531))
                           (if (seq map__19531) (first map__19531) {}))
                         map__19531)
            db (get map__19531 :db)
            log (get map__19531 :log)]
        (->StorageOnlyConnection db-id cluster db log))))
  (reset-meta!
    #'connect-to-storage
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'connect-to-storage
      :ns
      *ns*))
  (defn revert-to-log-version-1
    ([{:keys [db-uri]}]
      (let [cluster (coord/create-db-cluster (coord/resolve-db-name (uri/parse db-uri)))]
        (log/convert-log-version cluster 1))))
  (reset-meta!
    #'revert-to-log-version-1
    (assoc
      {:arglists (clojure.core/list [{:keys ['db-uri]}]), :column (int 1)}
      :doc
      "Converts the persistent log for db-uri to format version 1 for downgrade compatibility with software predating log version 2. A log-version-2 transactor will upgrade the database again when it is used. URI resolution, storage access, and conversion failures propagate."
      :name
      'revert-to-log-version-1
      :ns
      *ns*))
  (defn db ([conn] (.db ^datomic.Connection conn)))
  (reset-meta!
    #'db
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'conn {:tag 'datomic.Connection})]),
       :column (int 1)}
      :name
      'db
      :ns
      *ns*))
  (defn transact ([conn txdata] (.transact ^datomic.Connection conn ^java.util.List txdata)))
  (reset-meta!
    #'transact
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'conn {:tag 'datomic.Connection}) 'txdata]),
       :column (int 1)}
      :name
      'transact
      :ns
      *ns*))
  (defn transact-all
    ([conn txdata]
      (loop [seq_19538 (seq txdata) chunk_19539 nil count_19540 0 i_19541 0]
        (if (< i_19541 count_19540)
          (let [tx (.nth ^clojure.lang.Indexed chunk_19539 (int i_19541))]
            (deref (.transact ^datomic.Connection conn ^java.util.List tx))
            (recur seq_19538 chunk_19539 count_19540 (inc i_19541)))
          (let [temp__5825__auto__ (seq seq_19538)]
            (when temp__5825__auto__
              (let [seq_19538 temp__5825__auto__]
                (if (chunked-seq? seq_19538)
                  (let [c__6090__auto__ (chunk-first seq_19538)]
                    (recur
                      (chunk-rest seq_19538)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [tx (first seq_19538)]
                    (deref (.transact ^datomic.Connection conn ^java.util.List tx))
                    (recur (next seq_19538) nil 0 0))))))))
      :ok))
  (reset-meta!
    #'transact-all
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'conn {:tag 'datomic.Connection}) 'txdata]),
       :column (int 1)}
      :name
      'transact-all
      :ns
      *ns*))
  (defn seq-iterator
    ([aseq]
      (let [a (atom aseq)]
        (reify
          java.util.Iterator
          (next
            [this]
            (do
              (when (not (.hasNext this)) (throw (java.util.NoSuchElementException.)))
              (let [current (first (deref a))] (swap! a next) current)))
          (^boolean hasNext [this] (boolean (deref a)))))))
  (reset-meta!
    #'seq-iterator
    (assoc {:arglists (clojure.core/list ['aseq]), :column (int 1)} :name 'seq-iterator :ns *ns*))
  (defn seq-iterable
    ([aseq] (reify java.lang.Iterable (^java.util.Iterator iterator [this] (seq-iterator aseq)))))
  (reset-meta!
    #'seq-iterable
    (assoc {:arglists (clojure.core/list ['aseq]), :column (int 1)} :name 'seq-iterable :ns *ns*))
  (defn submit-data-script ([conn f] (transact-all conn (io/read-all (jio/reader f)))))
  (reset-meta!
    #'submit-data-script
    (assoc
      {:arglists (clojure.core/list ['conn 'f]), :column (int 1)}
      :name
      'submit-data-script
      :ns
      *ns*))
  (defmethod pprint/simple-dispatch datomic.db.Db fn__19552 ([x] (pr x)))
  (.setMeta (clojure.lang.RT/var "datomic.peer" "initialize") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.peer" "initialize")
    (delay
      (when (and
              (config/edition-has-feature? :monitor/metrics)
              (not=
                (config/property "datomic.metricsCallback")
                'datomic.aws-monitor/cloudwatch-reporter))
        (clojure.core/require 'datomic.process-monitor)
        ((resolve 'datomic.process-monitor/start-metrics))
        (cast2slf4j/redirect)
        (domain/preload-extension-resolver!)
        (deref start-kv-cache-delay))))
  (defn ensure-schema-level
    ([conn]
      (let [db_before (db conn)
            schema_level (:schema-level db_before)
            temp__5825__auto__ (seq (db/tx-data-for-latest-schema-level db_before))]
        (when temp__5825__auto__
          (let [txes temp__5825__auto__]
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process
                    {:event :peer/ensure-schema-level,
                     :db (:id db_before),
                     :from schema_level,
                     :to (+ schema_level (count txes))})))
              nil)
            (loop [seq_19557 (seq txes) chunk_19558 nil count_19559 0 i_19560 0]
              (if (< i_19560 count_19559)
                (let [tx (.nth ^clojure.lang.Indexed chunk_19558 (int i_19560))]
                  (transact conn tx)
                  (recur seq_19557 chunk_19558 count_19559 (inc i_19560)))
                (let [temp__5825__auto__ (seq seq_19557)]
                  (when temp__5825__auto__
                    (let [seq_19557 temp__5825__auto__]
                      (if (chunked-seq? seq_19557)
                        (let [c__6090__auto__ (chunk-first seq_19557)]
                          (recur
                            (chunk-rest seq_19557)
                            c__6090__auto__
                            (int (count c__6090__auto__))
                            (int 0)))
                        (let [tx (first seq_19557)]
                          (transact conn tx)
                          (recur (next seq_19557) nil 0 0)))))))))))))
  (reset-meta!
    #'ensure-schema-level
    (assoc
      {:arglists (clojure.core/list ['conn]), :column (int 1)}
      :name
      'ensure-schema-level
      :ns
      *ns*))
  ;; Selects live, memory, backup, or direct-storage connection behavior from the parsed URI.
  (defn connect-uri
    ([uri]
      (deref initialize)
      (let [cluster-conf (uri/parse-db uri) protocol (:protocol cluster-conf)]
        (cond
          (= protocol :backup) (connect-to-backup (:backup-uri cluster-conf) (:t cluster-conf))
          (= protocol :mem) (if (:read-only cluster-conf)
                              (read-only-local-database cluster-conf)
                              (connect-local-database (:db-name cluster-conf)))
          (:read-only cluster-conf) (connect-to-storage cluster-conf)
          :else (do (get-connection cluster-conf))))))
  (reset-meta!
    #'connect-uri
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'connect-uri :ns *ns*))
  (defn administer-system
    ([p__19566]
      (let [map__19567 p__19566
            map__19567 (if (seq? map__19567)
                         (if (next map__19567)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19567))
                           (if (seq map__19567) (first map__19567) {}))
                         map__19567)
            uri (get map__19567 :uri)
            action (get map__19567 :action)]
        (when (nil? action) (throw (java.lang.IllegalArgumentException. "Invalid options map.")))
        (when (and (= action :upgrade-schema) (nil? uri))
          (throw (java.lang.IllegalArgumentException. "Invalid options map.")))
        (if (and (= action :upgrade-schema) uri)
          (let [cluster-conf (uri/parse uri)
                _ (when (= (:protocol cluster-conf) :backup)
                    (error/arg
                      :db.error/unsupported-protocol
                      (str "Unsupported protocol " (:protocol cluster-conf))))
                conn (connect-uri uri)]
            (ensure-schema-level conn)
            :completed)
          (if (= action :release-object-cache)
            (let [system_cache (domain/system-cache)] (cache/clear system_cache) :completed)
            (do
              (when :else (throw (java.lang.IllegalArgumentException. "Invalid options map.")))
              nil))))))
  (reset-meta!
    #'administer-system
    (assoc
      {:arglists (clojure.core/list [{:keys ['uri 'action]}]), :column (int 1)}
      :name
      'administer-system
      :ns
      *ns*))
  ;; Sends a database administration command to the active transactor endpoint.
  (defn send-admin-request
    ([cluster-conf request arg]
      (let [m_19571 {:event :peer/transactor-admin-request,
                     :cluster (uri/loggable-cluster-conf cluster-conf),
                     :request request,
                     :arg arg}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_19571 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned
                                    (let [endpoint (or
                                                     (coord/lookup-compatible-transactor-endpoint
                                                       (coord/create-system-cluster cluster-conf))
                                                     (error/raise
                                                       :db.error/transactor-not-registered
                                                       "No transactor registered"))]
                                      (conn/admin-request
                                        (conn/create-transactor-hornet-connector
                                          cluster-conf
                                          endpoint)
                                        request
                                        arg))}
                                   (catch
                                     java.lang.Throwable
                                     t__8601__auto__
                                     {:threw t__8601__auto__}))
            elapsed_19572 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_19573 (logger/format-as-msec (long elapsed_19572))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_19571 :msec msec_19573 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
  (reset-meta!
    #'send-admin-request
    (assoc
      {:arglists (clojure.core/list ['cluster-conf 'request 'arg]), :column (int 1)}
      :name
      'send-admin-request
      :ns
      *ns*))
  ;; Creates a catalog entry and initial database value; returns false when the name already exists.
  (defn create-database
    ([uri desc]
      (let [cluster-conf (uri/parse-db uri)
            db-name (:db-name cluster-conf)
            uri (:uri cluster-conf)
            protocol (:protocol cluster-conf)]
        (if (= protocol :mem)
          (create-local-database db-name uri)
          (let [result (send-admin-request
                         cluster-conf
                         :create-database
                         (assoc desc :db-name db-name))]
            (cond
              (:created result) true
              (:exists result) false
              :default (do
                         (error/raise
                           :db.error/create-database-failed
                           "Unable to create database"
                           result)))))))
    ([uri] (create-database uri nil)))
  (reset-meta!
    #'create-database
    (assoc
      {:arglists (clojure.core/list ['uri] ['uri 'desc]), :column (int 1)}
      :name
      'create-database
      :ns
      *ns*))
  ;; Removes a database from the live catalog after releasing its cached Peer connection.
  (defn delete-database
    ([uri]
      (let [cluster-conf (uri/parse-db uri)
            db-name (:db-name cluster-conf)
            protocol (:protocol cluster-conf)]
        (if (= protocol :mem)
          (delete-local-database db-name)
          (let [temp__5823__auto__ (coord/resolve-db-name cluster-conf)]
            (if temp__5823__auto__
              (let [map__19583 temp__5823__auto__
                    map__19583 (if (seq? map__19583)
                                 (if (next map__19583)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__19583))
                                   (if (seq map__19583) (first map__19583) {}))
                                 map__19583)
                    resolved-cluster-conf map__19583
                    db-id (get map__19583 :db-id)]
                (locking connection-lock
                 (do
                   (stop-connection resolved-cluster-conf)
                   (contains?
                     (send-admin-request cluster-conf :delete-database {:db-name db-name})
                     :deleted))))
              false))))))
  (reset-meta!
    #'delete-database
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'delete-database
      :ns
      *ns*))
  ;; Changes a database catalog name while preserving its database identity.
  (defn rename-database
    ([uri new-name]
      (let [cluster-conf (uri/parse-db uri)
            db-name (:db-name cluster-conf)
            protocol (:protocol cluster-conf)]
        (if (= protocol :mem)
          (rename-local-database db-name new-name)
          (let [result (send-admin-request
                         cluster-conf
                         :rename-database
                         {:db-name db-name, :new-name new-name})]
            (if (:renamed-to result)
              true
              (error/raise
                :db.error/rename-database-failed
                "Unable to rename database"
                result)))))))
  (reset-meta!
    #'rename-database
    (assoc
      {:arglists (clojure.core/list ['uri (.withMeta 'new-name {:tag 'String})]), :column (int 1)}
      :name
      'rename-database
      :ns
      *ns*))
  (defn get-catalog
    ([uri]
      (let [cluster-conf (uri/parse uri) protocol (:protocol cluster-conf)]
        (keys
          (if (= protocol :mem)
            (deref local-dbs)
            (dissoc
              (catalog/get-catalog (coord/create-system-cluster cluster-conf))
              :datomic/rev
              :datomic/deleted))))))
  (reset-meta!
    #'get-catalog
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'get-catalog :ns *ns*))
  (defn undelete-database
    ([db-id uri]
      (let [cluster-conf (uri/parse uri)
            cluster (coord/create-system-cluster cluster-conf)
            db-name (:db-name cluster-conf)]
        (if db-name (catalog/undelete-database cluster db-id db-name) {:no-db-name uri}))))
  (reset-meta!
    #'undelete-database
    (assoc
      {:arglists (clojure.core/list ['db-id 'uri]), :column (int 1)}
      :name
      'undelete-database
      :ns
      *ns*))
  (defn get-database-names
    ([uri]
      (let [cluster-conf (uri/parse uri)]
        (when (= (:protocol cluster-conf) :backup)
          (error/arg
            :db.error/unsupported-protocol
            (str "Unsupported protocol " (:protocol cluster-conf))))
        (if (instance? java.util.Map uri)
          (when-not (= (or (:db-name cluster-conf) "*") "*")
            (error/raise
              :db.error/invalid-db-uri
              "Invalid URI. Note :db-name should be omitted from connection map"))
          (when-not (= (:db-name cluster-conf) "*")
            (error/raise
              :db.error/invalid-db-uri
              "Invalid URI. Note URI must have '*' in place of database name.")))
        (get-catalog uri))))
  (reset-meta!
    #'get-database-names
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'get-database-names
      :ns
      *ns*))
  ;; Lists available snapshots and their read-only connection URIs.
  (defn list-backups ([backup-uri] (req/require-and-run 'datomic.backup/list-backups backup-uri)))
  (reset-meta!
    #'list-backups
    (assoc
      {:arglists (clojure.core/list ['backup-uri]), :column (int 1)}
      :name
      'list-backups
      :ns
      *ns*))
  (defn tx-group-endpoint
    ([uri k] (coord/lookup-endpoint (coord/create-system-cluster (uri/parse uri)) k)))
  (reset-meta!
    #'tx-group-endpoint
    (assoc
      {:arglists (clojure.core/list ['uri 'k]), :column (int 1)}
      :name
      'tx-group-endpoint
      :ns
      *ns*))
  (defn transactor-endpoint ([uri] (tx-group-endpoint uri coord/pod-key)))
  (reset-meta!
    #'transactor-endpoint
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'transactor-endpoint
      :ns
      *ns*))
  (defn t->tx (^long [^long t] (db/make-eid 3 t)))
  (reset-meta!
    #'t->tx
    (assoc
      {:arglists (clojure.core/list (.withMeta [(.withMeta 't {:tag 'long})] {:tag 'long})),
       :column (int 1)}
      :name
      't->tx
      :ns
      *ns*))
  (defn resolve-tempid
    ([db tempids tempid] (get tempids (if (string? tempid) tempid (db/resolve-id db tempid)))))
  (reset-meta!
    #'resolve-tempid
    (assoc
      {:arglists (clojure.core/list ['db 'tempids 'tempid]), :column (int 1)}
      :name
      'resolve-tempid
      :ns
      *ns*)))
