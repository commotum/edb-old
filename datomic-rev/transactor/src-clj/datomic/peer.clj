(do
  (clojure.core/in-ns 'datomic.peer)
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
  (declare ->ConnectionState)
  (declare map->ConnectionState)
  (defrecord
    ConnectionState
    [connector notifier updater cleanup]
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (future-call
        (fn fn__18867
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8829__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")
                        ex t__8829__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__8829__auto__)
                  nil))))))))
  (clojure.core/import 'datomic.peer.ConnectionState)
  (defn ->ConnectionState
    ([connector notifier updater cleanup]
      (datomic.peer.ConnectionState. connector notifier updater cleanup)))
  (defn map->ConnectionState
    ([m__7972__auto__]
      (ConnectionState/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (defn get-cstate
    ([state_ref or_else]
      (let [state (deref (deref state_ref) 0 or_else)]
        (if (= state :datomic.peer/shutdown)
          (error/state :db.error/connection-released "The connection has been released.")
          state))))
  (defonce RemoteConnection {})
  (defprotocol
    RemoteConnection
    (get-cluster [_])
    (get-olookup [_])
    (create-connection-state [_ cluster-conf endpoint mode]))
  (defn await-tx-result
    ([prom]
      (let [result (try
                     (deref prom (config/property "datomic.txTimeoutMsec") prom)
                     (catch java.lang.Throwable t t))]
        (if (= prom result)
          (error/raise :db.error/transaction-timeout "Transaction timed out.")
          prom))))
  (def connection-lock (java.lang.Object.))
  (def connection-cache (cache/create-soft-limited))
  (defonce TWatcher {})
  (defprotocol
    TWatcher
    (sync-t [_ t])
    (sync-background-t [_ btype t])
    (wait-for-future-t [_ t])
    (release-pending-syncs [_ new-db]))
  (deftype
    TWatcherImpl
    [q db_ref f lck]
    datomic.peer.TWatcher
    (release-pending-syncs
      [this new_db]
      (let [lockee__5782__auto__ lck locklocal__5783__auto__ lockee__5782__auto__]
        (monitor-enter locklocal__5783__auto__)
        (try
          (do
            (let [G__19005 (.peek ^java.util.Queue q)
                  map__19006 G__19005
                  map__19006 (if (seq? map__19006)
                               (if (next map__19006)
                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                   (to-array map__19006))
                                 (if (seq map__19006) (first map__19006) {}))
                               map__19006)
                  t (get map__19006 :t)
                  prom (get map__19006 :prom)]
              (loop [G__19005 G__19005]
                (let [map__19007 G__19005
                      map__19007 (if (seq? map__19007)
                                   (if (next map__19007)
                                     (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                       (to-array map__19007))
                                     (if (seq map__19007) (first map__19007) {}))
                                   map__19007)
                      t (get map__19007 :t)
                      prom (get map__19007 :prom)]
                  (when (and t (<= t (^clojure.lang.IFn f new_db)))
                    (deliver prom new_db)
                    (.remove ^java.util.Queue q)
                    (recur (.peek ^java.util.Queue q))))))
            nil)
          (finally (do (monitor-exit locklocal__5783__auto__) nil)))))
    (sync-background-t
      [this btype t_or_tx]
      (let [t (db/eid->eidx (long ^java.lang.Number t_or_tx)) db (deref db_ref)]
        (if (and db (<= t (^clojure.lang.IFn f db)))
          (promise/delivered db)
          (let [lockee__5782__auto__ lck locklocal__5783__auto__ lockee__5782__auto__]
            (monitor-enter locklocal__5783__auto__)
            (try
              (cond
                (and db (<= t (^clojure.lang.IFn f db))) (promise/delivered db)
                (and db (<= t (:basisT db))) (let [temp__5802__auto__ (let 
                                                                        [G__19003
                                                                         (db/ts-needing-index
                                                                           db
                                                                           btype
                                                                           (long t))
                                                                         G__19003
                                                                         (some-> G__19003 (seq))]
                                                                        (when-not
                                                                          (nil? G__19003)
                                                                          (apply max G__19003)))]
                                               (if temp__5802__auto__
                                                 (let [actual_t temp__5802__auto__]
                                                   (wait-for-future-t this actual_t))
                                                 (promise/delivered db)))
                :default (do (wait-for-future-t this (long t))))
              (finally (do (monitor-exit locklocal__5783__auto__) nil)))))))
    (sync-t
      [this t_or_tx]
      (let [t (db/eid->eidx (long ^java.lang.Number t_or_tx)) db (deref db_ref)]
        (if (and db (<= t (^clojure.lang.IFn f db)))
          (promise/delivered db)
          (let [lockee__5782__auto__ lck locklocal__5783__auto__ lockee__5782__auto__]
            (monitor-enter locklocal__5783__auto__)
            (try
              (if (and db (<= t (^clojure.lang.IFn f db)))
                (promise/delivered db)
                (wait-for-future-t this (long t)))
              (finally (do (monitor-exit locklocal__5783__auto__) nil)))))))
    (wait-for-future-t
      [this t]
      (let [prom (promise/settable-future)] (.add ^java.util.Queue q {:t t, :prom prom}) prom)))
  (clojure.core/import 'datomic.peer.TWatcherImpl)
  (defn ->TWatcherImpl ([q db_ref f lck] (datomic.peer.TWatcherImpl. q db_ref f lck)))
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
  (defn accept-new-data
    ([db data]
      (let [nextT (.nextT ^datomic.Database db)
            temp__5802__auto__ (seq
                                 (drop-while
                                   (fn fn__19029
                                     ([p1__19028#]
                                       (< (.getT ^datomic.impl.db.IDatum p1__19028#) nextT)))
                                   data))]
        (if temp__5802__auto__
          (let [newdata temp__5802__auto__]
            (.acceptDataCheck ^datomic.db.IDbImpl db newdata false))
          db))))
  (reset-meta!
    #'accept-new-data
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'data]),
       :column 1}
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
  (defn peer-queue-exceeded
    ([]
      (let [msg__8493__auto__ "Peer work queue limit exceeded, transactor may be unavailable."]
        (clojure.lang.ExceptionInfo.
          (str :db.error/peer-queue-exceeded " " msg__8493__auto__)
          (error/anomalize
            (assoc nil :db/error :db.error/peer-queue-exceeded)
            clojure.lang.ExceptionInfo
            msg__8493__auto__)))))
  (defn fail-pending-txes
    ([unsent_updates_queue pending_txes]
      (queue/clear unsent_updates_queue)
      (loop [seq_19036 (seq (cache/cache-keys pending_txes))
             chunk_19037 nil
             count_19038 0
             i_19039 0]
        (if (< i_19039 count_19038)
          (let [k (.nth ^clojure.lang.Indexed chunk_19037 (int i_19039))]
            (let [temp__5804__auto__ (cache/remove pending_txes k)]
              (when temp__5804__auto__
                (let [prom temp__5804__auto__] (deliver prom (transactor-unavailable)))))
            (recur seq_19036 chunk_19037 count_19038 (inc i_19039)))
          (let [temp__5804__auto__ (seq seq_19036)]
            (when temp__5804__auto__
              (let [seq_19036 temp__5804__auto__]
                (if (chunked-seq? seq_19036)
                  (let [c__6065__auto__ (chunk-first seq_19036)]
                    (recur
                      (chunk-rest seq_19036)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [k (first seq_19036)]
                    (let [temp__5804__auto__ (cache/remove pending_txes k)]
                      (when temp__5804__auto__
                        (let [prom temp__5804__auto__] (deliver prom (transactor-unavailable)))))
                    (recur (next seq_19036) nil 0 0))))))))))
  (def start-kv-cache-delay (delay (cluster-stack/start-kv-cache)))
  (reset-meta!
    #'start-kv-cache-delay
    (assoc {:private true, :column 1} :name 'start-kv-cache-delay :ns *ns*))
  (declare stop-connection)
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
        (fn fn__19104
          ([]
            (try
              (let [idxroot (index/find-index-root-id cluster)
                    idx (index/load-index olookup idxroot)
                    adopt_db (db/db db_id idx)
                    temp__5802__auto__ (adopter/adopt-index db_ref adopt_db)]
                (if temp__5802__auto__
                  (let [map__19105 temp__5802__auto__
                        map__19105 (if (seq? map__19105)
                                     (if (next map__19105)
                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                         (to-array map__19105))
                                       (if (seq map__19105) (first map__19105) {}))
                                     map__19105)
                        basis_db (get map__19105 :basis-db)
                        adopted_db (get map__19105 :adopted-db)
                        msec (get map__19105 :msec)
                        iterations (get map__19105 :iterations)]
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
                t__8829__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")
                        ex t__8829__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__8829__auto__)
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
      (let [map__19096 msg
            map__19096 (if (seq? map__19096)
                         (if (next map__19096)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19096))
                           (if (seq map__19096) (first map__19096) {}))
                         map__19096)
            id (get map__19096 :id)
            data (get map__19096 :data)
            tempids (get map__19096 :tempids)
            io_stats (get map__19096 :io-stats)
            start (java.lang.System/currentTimeMillis)
            prom (cache/remove pending_txes id)
            map__19097 (meta prom)
            map__19097 (if (seq? map__19097)
                         (if (next map__19097)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19097))
                           (if (seq map__19097) (first map__19097) {}))
                         map__19097)
            io_context (get map__19097 :io-context)
            old_db (deref db_ref)
            new_db (let [m_19098 {:event :peer/accept-new, :id id}
                         ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                        "datomic.peer")]
                                           (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                             (.debug
                                               ^org.slf4j.Logger logger
                                               (logger/process (assoc m_19098 :phase :begin))))
                                           nil)
                         start__8584__auto__ (java.lang.System/nanoTime)
                         result__8585__auto__ (try
                                                {:returned (swap! db_ref accept-new-data data)}
                                                (catch
                                                  java.lang.Throwable
                                                  t__8586__auto__
                                                  {:threw t__8586__auto__}))
                         elapsed_19099 (- (java.lang.System/nanoTime) start__8584__auto__)
                         msec_19100 (logger/format-as-msec (long elapsed_19099))]
                     (monitor/add-stat :PeerAcceptNewMsec msec_19100)
                     (let [endmsg__8587__auto__ (merge
                                                  (assoc m_19098 :msec msec_19100 :phase :end)
                                                  (when (:threw result__8585__auto__)
                                                    {:threw
                                                     (class (:threw result__8585__auto__))}))
                           logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                         (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                       nil)
                     (if (contains? result__8585__auto__ :returned)
                       (:returned result__8585__auto__)
                       (do (throw (:threw result__8585__auto__)) nil)))
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
        (let [temp__5802__auto__ (:connector (get-cstate state_ref nil))]
          (when temp__5802__auto__
            (let [connector temp__5802__auto__]
              (conn/admin-request connector :request-gc {:db-id db_id, :older-than older_than}))))
        nil))
    (^void removeTxReportQueue [this] (do (reset! tx_report_queue nil) nil))
    (^java.util.concurrent.BlockingQueue txReportQueue
      [this]
      (or
        (deref tx_report_queue)
        (swap!
          tx_report_queue
          (fn fn__19094
            ([p1__19047#]
              (if p1__19047# p1__19047# (java.util.concurrent.LinkedBlockingQueue.)))))))
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
        (let [temp__5802__auto__ (:connector (get-cstate state_ref nil))]
          (if temp__5802__auto__
            (let [connector temp__5802__auto__]
              (conn/admin-request connector :request-index db_id)
              true)
            (do (throw (transactor-unavailable)) nil)))))
    (^void release
      [this]
      (do
        (let [lockee__5782__auto__ connection-lock locklocal__5783__auto__ lockee__5782__auto__]
          (monitor-enter locklocal__5783__auto__)
          (try
            (loop [seq_19086 (seq (cache/cache-keys connection-cache))
                   chunk_19087 nil
                   count_19088 0
                   i_19089 0]
              (if (< i_19089 count_19088)
                (let [k (.nth ^clojure.lang.Indexed chunk_19087 (int i_19089))]
                  (when (= this (get connection-cache k)) (cache/remove connection-cache k))
                  (recur seq_19086 chunk_19087 count_19088 (inc i_19089)))
                (let [temp__5804__auto__ (seq seq_19086)]
                  (when temp__5804__auto__
                    (let [seq_19086 temp__5804__auto__]
                      (if (chunked-seq? seq_19086)
                        (let [c__6065__auto__ (chunk-first seq_19086)]
                          (recur
                            (chunk-rest seq_19086)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [k (first seq_19086)]
                          (when (= this (get connection-cache k))
                            (cache/remove connection-cache k))
                          (recur (next seq_19086) nil 0 0))))))))
            (finally (do (monitor-exit locklocal__5783__auto__) nil))))
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
      (let [temp__5802__auto__ (deref state_ref 10000 nil)]
        (if temp__5802__auto__
          (let [o temp__5802__auto__] (recon/reconnect o))
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
        (let [m_19049 {:event :peer/create-connection-impl,
                       :cluster-conf (uri/loggable-cluster-conf cluster_conf)}
              ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_19049 :phase :begin))))
                                nil)
              start__8584__auto__ (java.lang.System/nanoTime)
              result__8585__auto__ (try
                                     {:returned
                                      (let [conn_ref (java.lang.ref.WeakReference. this)
                                            recon (delay
                                                    (let [temp__5804__auto__ (.get
                                                                               ^java.lang.ref.Reference conn_ref)]
                                                      (when temp__5804__auto__
                                                        (let [conn temp__5804__auto__]
                                                          (recon/reconnect conn)))))
                                            failure_handler (error/runonce
                                                              (fn 
                                                                fn__19056
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
                                            (let [lockee__5782__auto__ connection-lock
                                                  locklocal__5783__auto__ lockee__5782__auto__]
                                              (monitor-enter locklocal__5783__auto__)
                                              (try
                                                (stop-connection cluster_conf)
                                                (finally
                                                  (do
                                                    (monitor-exit locklocal__5783__auto__)
                                                    nil)))))
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
                                                        (fn fn__19063
                                                          ([]
                                                            (try
                                                              (^clojure.lang.IFn shutdown notifier)
                                                              (catch
                                                                java.lang.Throwable
                                                                t__8540__auto__
                                                                (error/report t__8540__auto__)))
                                                            nil)))]
                                          (try
                                            (let [_ (conn/notify-db
                                                      this
                                                      (^clojure.lang.IFn load_db db))
                                                  cleanup (error/runonce
                                                            (fn 
                                                              fn__19068
                                                              ([]
                                                                (try
                                                                  (identity _)
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8540__auto__
                                                                    (error/report
                                                                      t__8540__auto__)))
                                                                (^clojure.lang.IFn cleanup))))]
                                              (try
                                                (let [updater (conn/start-updater
                                                                connector
                                                                unsent_updates_queue
                                                                this
                                                                failure_handler)
                                                      cleanup (error/runonce
                                                                (fn 
                                                                  fn__19072
                                                                  ([]
                                                                    (try
                                                                      (^clojure.lang.IFn shutdown
                                                                        updater)
                                                                      (catch
                                                                        java.lang.Throwable
                                                                        t__8540__auto__
                                                                        (error/report
                                                                          t__8540__auto__)))
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
                                                      t__8541__auto__
                                                      (do
                                                        (try
                                                          (^clojure.lang.IFn shutdown updater)
                                                          (catch
                                                            java.lang.Throwable
                                                            t__8540__auto__
                                                            (error/report t__8540__auto__)))
                                                        (throw
                                                          ^java.lang.Throwable t__8541__auto__)
                                                        nil))))
                                                (catch
                                                  java.lang.Throwable
                                                  t__8541__auto__
                                                  (do
                                                    (try
                                                      (identity _)
                                                      (catch
                                                        java.lang.Throwable
                                                        t__8540__auto__
                                                        (error/report t__8540__auto__)))
                                                    (throw ^java.lang.Throwable t__8541__auto__)
                                                    nil))))
                                            (catch
                                              java.lang.Throwable
                                              t__8541__auto__
                                              (do
                                                (try
                                                  (^clojure.lang.IFn shutdown notifier)
                                                  (catch
                                                    java.lang.Throwable
                                                    t__8540__auto__
                                                    (error/report t__8540__auto__)))
                                                (throw ^java.lang.Throwable t__8541__auto__)
                                                nil)))))}
                                     (catch
                                       java.lang.Throwable
                                       t__8586__auto__
                                       {:threw t__8586__auto__}))
              elapsed_19050 (- (java.lang.System/nanoTime) start__8584__auto__)
              msec_19051 (logger/format-as-msec (long elapsed_19050))]
          (let [endmsg__8587__auto__ (merge
                                       (assoc m_19049 :msec msec_19051 :phase :end)
                                       (when (:threw result__8585__auto__)
                                         {:threw (class (:threw result__8585__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
            nil)
          (if (contains? result__8585__auto__ :returned)
            (:returned result__8585__auto__)
            (do (throw (:threw result__8585__auto__)) nil)))))
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
  (def MAX_UNSENT_QUEUE 128)
  (reset-meta!
    #'MAX_UNSENT_QUEUE
    (assoc {:const true, :column 1} :name 'MAX_UNSENT_QUEUE :ns *ns*))
  (def MAX_LUCENE_QUEUE 128)
  (reset-meta!
    #'MAX_LUCENE_QUEUE
    (assoc {:const true, :column 1} :name 'MAX_LUCENE_QUEUE :ns *ns*))
  (defn integrate-lucene
    ([db_ref q]
      (loop [tx (queue/take q)]
        (when-not (= tx :done)
          (let [vec__19127 (loop [txes [tx] ntx (queue/poll q)]
                             (if (or (= ntx :done) (nil? ntx))
                               [txes ntx]
                               (recur (conj txes ntx) (queue/poll q))))
                txes (nth vec__19127 (int 0) nil)
                ntx (nth vec__19127 (int 1) nil)
                db (deref db_ref)
                oft (:fulltext (:memidx db))
                nft (ftindex/update-fulltext
                      oft
                      (filter
                        (fn fn__19133
                          ([p1__19126#]
                            (db/fulltext?
                              db
                              (java.lang.Integer/valueOf
                                (int (.getA ^datomic.impl.db.IDatum p1__19126#))))))
                        (apply concat txes)))]
            (swap! db_ref assoc-in [:memidx :fulltext] nft)
            (monitor/add-stat :PeerFulltextBatch (java.lang.Integer/valueOf (int (count txes))))
            (when-not (= ntx :done) (recur (queue/take q))))))))
  (defn create-connection
    ([cluster_conf]
      (deref start-kv-cache-delay)
      (let [unsent_updates_queue (java.util.concurrent.ArrayBlockingQueue. (int 128))
            lucene_queue (java.util.concurrent.ArrayBlockingQueue. (int 128))
            state_ref (promise)
            cluster (coord/create-db-cluster cluster_conf)
            system_cluster (coord/create-system-cluster cluster_conf)
            olookup (domain/system-cache-olookup cluster)
            pending_txes (cache/create-response-map 60)
            db_ref (atom nil)
            conn (datomic.peer.Connection.
                   (common/getx cluster_conf :db-id)
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
                                     cluster_conf
                                     new_endpoint
                                     mode))))))
            state (^clojure.lang.IFn reconnect_fn :initial)]
        (deliver
          state_ref
          (recon/reconnector-ref
            :state
            state
            :reconnect
            (fn fn__19139
              ([]
                (common/retry-fn
                  (partial reconnect_fn :reconnect)
                  :pred
                  (fn fn__19140
                    ([p1__19136#] (not (instance? datomic.peer.ConnectionState p1__19136#))))
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
            (fn fn__19144
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
  (def local-dbs (atom {}))
  (reset-meta! #'local-dbs (assoc {:private true, :column 1} :name 'local-dbs :ns *ns*))
  (declare delete-local-database)
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
          (fn fn__19153
            ([p1__19147#]
              (if p1__19147# p1__19147# (java.util.concurrent.LinkedBlockingQueue.)))))))
    (^datomic.ListenableFuture transactAsync
      [this ^java.util.List txdata]
      (if (realized? released)
        (error/state :db.error/connection-released "The connection has been released.")
        (let [lockee__5782__auto__ this locklocal__5783__auto__ lockee__5782__auto__]
          (monitor-enter locklocal__5783__auto__)
          (try
            (let [db (deref db_ref)
                  report (try (db/with-tx+opts db txdata nil) (catch java.lang.Throwable e e))
                  txrq (deref tx_report_queue)]
              (when-not (instance? java.lang.Throwable report)
                (reset! db_ref (:db-after report))
                (when txrq (.add ^java.util.Queue txrq report))
                (release-pending-syncs tx_watcher (:db-after report)))
              (promise/delivered report))
            (finally (do (monitor-exit locklocal__5783__auto__) nil))))))
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
    (^datomic.Log log
      [this]
      (let [db (deref db_ref) memlog (:memlog db)]
        (reify
          datomic.Log
          (^java.lang.Iterable txRange [this start end] (log/tx-range memlog db start end)))))
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
  (defn local-connection
    ([dbname]
      (let [db (db/bootstrap-db) db_ref (atom db)]
        (datomic.peer.LocalConnection.
          dbname
          db_ref
          (atom nil)
          (promise)
          (create-t-watcher :basisT db_ref)))))
  (defn connect-local-database
    ([dbname]
      (let [dbs (deref local-dbs)]
        (or
          (get dbs dbname)
          (error/raise :db.error/db-not-found (str "Could not find " dbname " in catalog"))))))
  (defn create-local-database
    ([dbname uri]
      (let [created (atom false)]
        (swap!
          local-dbs
          (fn fn__19165
            ([dbs]
              (if (get dbs dbname)
                dbs
                (do (reset! created true) (assoc dbs dbname (local-connection dbname)))))))
        (deref created))))
  (defn delete-local-database
    ([dbname]
      (let [deleted (atom false)]
        (swap!
          local-dbs
          (fn fn__19168
            ([dbs]
              (let [temp__5802__auto__ (get dbs dbname)]
                (if temp__5802__auto__
                  (let [conn temp__5802__auto__]
                    (deliver (.-released ^datomic.peer.LocalConnection conn) true)
                    (reset! deleted true)
                    (dissoc dbs dbname))
                  dbs)))))
        (deref deleted))))
  (defn rename-local-database
    ([dbname newname]
      (swap!
        local-dbs
        (fn fn__19172
          ([dbs]
            (let [temp__5802__auto__ (get dbs dbname)]
              (if temp__5802__auto__
                (let [db temp__5802__auto__]
                  (if (get dbs newname)
                    (error/raise :db.error/db-exists (str newname " already exists in catalog"))
                    (assoc (dissoc dbs dbname) newname db)))
                (error/raise
                  :db.error/db-not-found
                  (str "Could not find " dbname " in catalog")))))))
      true))
  (defn shutdown
    ([shutdown_clojure]
      (let [lockee__5782__auto__ connection-lock locklocal__5783__auto__ lockee__5782__auto__]
        (monitor-enter locklocal__5783__auto__)
        (try
          (loop [seq_19177 (seq (cache/cache-keys connection-cache))
                 chunk_19178 nil
                 count_19179 0
                 i_19180 0]
            (if (< i_19180 count_19179)
              (let [k (.nth ^clojure.lang.Indexed chunk_19178 (int i_19180))]
                (let [temp__5804__auto__ (cache/remove connection-cache k)]
                  (when temp__5804__auto__
                    (let [conn temp__5804__auto__] (deref (common/async-shutdown conn)))))
                (recur seq_19177 chunk_19178 count_19179 (inc i_19180)))
              (let [temp__5804__auto__ (seq seq_19177)]
                (when temp__5804__auto__
                  (let [seq_19177 temp__5804__auto__]
                    (if (chunked-seq? seq_19177)
                      (let [c__6065__auto__ (chunk-first seq_19177)]
                        (recur
                          (chunk-rest seq_19177)
                          c__6065__auto__
                          (int (count c__6065__auto__))
                          (int 0)))
                      (let [k (first seq_19177)]
                        (let [temp__5804__auto__ (cache/remove connection-cache k)]
                          (when temp__5804__auto__
                            (let [conn temp__5804__auto__] (deref (common/async-shutdown conn)))))
                        (recur (next seq_19177) nil 0 0))))))))
          (finally (do (monitor-exit locklocal__5783__auto__) nil))))
      (conn/stop-all-connectors)
      (reset! cluster-stack/kv-cache-ref nil)
      (when shutdown_clojure (shutdown-agents))))
  (defn stop-connection
    ([p__19189]
      (let [map__19190 p__19189
            map__19190 (if (seq? map__19190)
                         (if (next map__19190)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19190))
                           (if (seq map__19190) (first map__19190) {}))
                         map__19190)
            resolved_cluster_conf map__19190
            db_id (get map__19190 :db-id)]
        (when-not :db-id
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str :db-id)))))
        (let [temp__5804__auto__ (get connection-cache resolved_cluster_conf)]
          (when temp__5804__auto__
            (let [conn temp__5804__auto__] (.release ^datomic.Connection conn))))
        (cache/clear coord/db-cache))))
  (defn get-connection
    ([cluster_conf]
      (let [m_19193 {:event :peer/get-connection,
                     :cluster-conf (uri/loggable-cluster-conf cluster_conf)}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_19193 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [temp__5802__auto__ (coord/resolve-db-name cluster_conf)]
                                      (if temp__5802__auto__
                                        (let [map__19197 temp__5802__auto__
                                              map__19197 (if (seq? map__19197)
                                                           (if
                                                             (next map__19197)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__19197))
                                                             (if
                                                               (seq map__19197)
                                                               (first map__19197)
                                                               {}))
                                                           map__19197)
                                              resolved_cluster_conf map__19197
                                              db_id (get map__19197 :db-id)
                                              lockee__5782__auto__ connection-lock
                                              locklocal__5783__auto__ lockee__5782__auto__]
                                          (monitor-enter locklocal__5783__auto__)
                                          (try
                                            (let [temp__5802__auto__ (get
                                                                       connection-cache
                                                                       resolved_cluster_conf)]
                                              (if temp__5802__auto__
                                                (let [conn temp__5802__auto__] conn)
                                                (let [conn (create-connection
                                                             resolved_cluster_conf)]
                                                  (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.peer")]
                                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                      (.info
                                                        ^org.slf4j.Logger logger
                                                        (logger/process
                                                          (merge
                                                            {:event :peer/cache-connection}
                                                            (uri/loggable-cluster-conf
                                                              resolved_cluster_conf)))))
                                                    nil)
                                                  (cache/put
                                                    connection-cache
                                                    resolved_cluster_conf
                                                    conn)
                                                  conn)))
                                            (finally
                                              (do (monitor-exit locklocal__5783__auto__) nil))))
                                        (do
                                          (throw
                                            (java.lang.RuntimeException.
                                              (str
                                                "Could not find "
                                                (:db-name cluster_conf)
                                                " in catalog")))
                                          1)))}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_19194 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_19195 (logger/format-as-msec (long elapsed_19194))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_19193 :msec msec_19195 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (defn revert-to-log-version-1
    ([p__19210]
      (let [map__19211 p__19210
            map__19211 (if (seq? map__19211)
                         (if (next map__19211)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19211))
                           (if (seq map__19211) (first map__19211) {}))
                         map__19211)
            db_uri (get map__19211 :db-uri)
            cluster (coord/create-db-cluster (coord/resolve-db-name (uri/parse db_uri)))]
        (log/convert-log-version cluster 1))))
  (defn db ([conn] (.db ^datomic.Connection conn)))
  (defn transact ([conn txdata] (.transact ^datomic.Connection conn ^java.util.List txdata)))
  (defn transact-all
    ([conn txdata]
      (loop [seq_19215 (seq txdata) chunk_19216 nil count_19217 0 i_19218 0]
        (if (< i_19218 count_19217)
          (let [tx (.nth ^clojure.lang.Indexed chunk_19216 (int i_19218))]
            (deref (.transact ^datomic.Connection conn ^java.util.List tx))
            (recur seq_19215 chunk_19216 count_19217 (inc i_19218)))
          (let [temp__5804__auto__ (seq seq_19215)]
            (when temp__5804__auto__
              (let [seq_19215 temp__5804__auto__]
                (if (chunked-seq? seq_19215)
                  (let [c__6065__auto__ (chunk-first seq_19215)]
                    (recur
                      (chunk-rest seq_19215)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [tx (first seq_19215)]
                    (deref (.transact ^datomic.Connection conn ^java.util.List tx))
                    (recur (next seq_19215) nil 0 0))))))))
      :ok))
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
  (defn seq-iterable
    ([aseq] (reify java.lang.Iterable (^java.util.Iterator iterator [this] (seq-iterator aseq)))))
  (defn submit-data-script ([conn f] (transact-all conn (io/read-all (jio/reader f)))))
  (defmethod pprint/simple-dispatch datomic.db.Db fn__19229 ([x] (pr x)))
  (def initialize
   (delay
     (when (and
             (config/edition-has-feature? :monitor/metrics)
             (not=
               (config/property "datomic.metricsCallback")
               'datomic.aws-monitor/cloudwatch-reporter))
       (clojure.core/require 'datomic.process-monitor)
       ((resolve 'datomic.process-monitor/start-metrics))
       (cast2slf4j/redirect)
       (domain/preload-extension-resolver!))))
  (defn ensure-schema-level
    ([conn]
      (let [db_before (db conn)
            schema_level (:schema-level db_before)
            temp__5804__auto__ (seq (db/tx-data-for-latest-schema-level db_before))]
        (when temp__5804__auto__
          (let [txes temp__5804__auto__]
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
            (loop [seq_19234 (seq txes) chunk_19235 nil count_19236 0 i_19237 0]
              (if (< i_19237 count_19236)
                (let [tx (.nth ^clojure.lang.Indexed chunk_19235 (int i_19237))]
                  (transact conn tx)
                  (recur seq_19234 chunk_19235 count_19236 (inc i_19237)))
                (let [temp__5804__auto__ (seq seq_19234)]
                  (when temp__5804__auto__
                    (let [seq_19234 temp__5804__auto__]
                      (if (chunked-seq? seq_19234)
                        (let [c__6065__auto__ (chunk-first seq_19234)]
                          (recur
                            (chunk-rest seq_19234)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [tx (first seq_19234)]
                          (transact conn tx)
                          (recur (next seq_19234) nil 0 0)))))))))
          nil))))
  (defn connect-uri
    ([uri]
      (deref initialize)
      (let [cluster_conf (uri/parse-db uri) protocol (:protocol cluster_conf)]
        (if (= protocol :mem)
          (connect-local-database (:db-name cluster_conf))
          (get-connection cluster_conf)))))
  (defn administer-system
    ([p__19243]
      (let [map__19244 p__19243
            map__19244 (if (seq? map__19244)
                         (if (next map__19244)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19244))
                           (if (seq map__19244) (first map__19244) {}))
                         map__19244)
            uri (get map__19244 :uri)
            action (get map__19244 :action)]
        (when (nil? action) (throw (java.lang.IllegalArgumentException. "Invalid options map.")))
        (when (and (= action :upgrade-schema) (nil? uri))
          (throw (java.lang.IllegalArgumentException. "Invalid options map.")))
        (if (and (= action :upgrade-schema) uri)
          (let [conn (connect-uri uri)] (ensure-schema-level conn) :completed)
          (if (= action :release-object-cache)
            (let [system_cache (domain/system-cache)] (cache/clear system_cache) :completed)
            (do
              (when :else (throw (java.lang.IllegalArgumentException. "Invalid options map.")))
              nil))))))
  (defn send-admin-request
    ([cluster_conf request arg]
      (let [m_19248 {:event :peer/transactor-admin-request,
                     :cluster (uri/loggable-cluster-conf cluster_conf),
                     :request request,
                     :arg arg}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_19248 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [endpoint (or
                                                     (coord/lookup-compatible-transactor-endpoint
                                                       (coord/create-system-cluster cluster_conf))
                                                     (error/raise
                                                       :db.error/transactor-not-registered
                                                       "No transactor registered"))]
                                      (conn/admin-request
                                        (conn/create-transactor-hornet-connector
                                          cluster_conf
                                          endpoint)
                                        request
                                        arg))}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_19249 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_19250 (logger/format-as-msec (long elapsed_19249))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_19248 :msec msec_19250 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.peer")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (defn create-database
    ([uri desc]
      (let [cluster_conf (uri/parse-db uri)
            db_name (:db-name cluster_conf)
            uri (:uri cluster_conf)
            protocol (:protocol cluster_conf)]
        (if (= protocol :mem)
          (create-local-database db_name uri)
          (let [result (send-admin-request
                         cluster_conf
                         :create-database
                         (assoc desc :db-name db_name))]
            (cond
              (:created result) true
              (:exists result) false
              :default (do
                         (error/raise
                           :db.error/create-database-failed
                           "Unable to create database"
                           result)))))))
    ([uri] (create-database uri nil)))
  (defn delete-database
    ([uri]
      (let [cluster_conf (uri/parse-db uri)
            db_name (:db-name cluster_conf)
            protocol (:protocol cluster_conf)]
        (if (= protocol :mem)
          (delete-local-database db_name)
          (let [temp__5802__auto__ (coord/resolve-db-name cluster_conf)]
            (if temp__5802__auto__
              (let [map__19260 temp__5802__auto__
                    map__19260 (if (seq? map__19260)
                                 (if (next map__19260)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__19260))
                                   (if (seq map__19260) (first map__19260) {}))
                                 map__19260)
                    resolved_cluster_conf map__19260
                    db_id (get map__19260 :db-id)
                    lockee__5782__auto__ connection-lock
                    locklocal__5783__auto__ lockee__5782__auto__]
                (monitor-enter locklocal__5783__auto__)
                (try
                  (do
                    (stop-connection resolved_cluster_conf)
                    (contains?
                      (send-admin-request cluster_conf :delete-database {:db-name db_name})
                      :deleted))
                  (finally (do (monitor-exit locklocal__5783__auto__) nil))))
              false))))))
  (defn rename-database
    ([uri new_name]
      (let [cluster_conf (uri/parse-db uri)
            db_name (:db-name cluster_conf)
            protocol (:protocol cluster_conf)]
        (if (= protocol :mem)
          (rename-local-database db_name new_name)
          (let [result (send-admin-request
                         cluster_conf
                         :rename-database
                         {:db-name db_name, :new-name new_name})]
            (if (:renamed-to result)
              true
              (error/raise
                :db.error/rename-database-failed
                "Unable to rename database"
                result)))))))
  (defn get-catalog
    ([uri]
      (let [cluster_conf (uri/parse uri) protocol (:protocol cluster_conf)]
        (keys
          (if (= protocol :mem)
            (deref local-dbs)
            (dissoc
              (catalog/get-catalog (coord/create-system-cluster cluster_conf))
              :datomic/rev
              :datomic/deleted))))))
  (defn undelete-database
    ([db_id uri]
      (let [cluster_conf (uri/parse uri)
            cluster (coord/create-system-cluster cluster_conf)
            db_name (:db-name cluster_conf)]
        (if db_name (catalog/undelete-database cluster db_id db_name) {:no-db-name uri}))))
  (defn get-database-names
    ([uri]
      (let [cluster_conf (uri/parse uri)]
        (if (instance? java.util.Map uri)
          (when-not (= (or (:db-name cluster_conf) "*") "*")
            (error/raise
              :db.error/invalid-db-uri
              "Invalid URI. Note :db-name should be omitted from connection map"))
          (when-not (= (:db-name cluster_conf) "*")
            (error/raise
              :db.error/invalid-db-uri
              "Invalid URI. Note URI must have '*' in place of database name.")))
        (get-catalog uri))))
  (defn tx-group-endpoint
    ([uri k] (coord/lookup-endpoint (coord/create-system-cluster (uri/parse uri)) k)))
  (defn transactor-endpoint ([uri] (tx-group-endpoint uri coord/pod-key)))
  (defn t->tx (^long [^long t] (db/make-eid 3 t)))
  (defn resolve-tempid
    ([db tempids tempid] (get tempids (if (string? tempid) tempid (db/resolve-id db tempid))))))