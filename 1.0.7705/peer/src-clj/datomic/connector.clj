(do
  (clojure.core/in-ns 'datomic.connector)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.artemis-client :as 'aclient]
        ['datomic.slf4j :as 'logger]
        ['datomic.aws-detect :as 'aws-detect]
        ['datomic.cache :as 'cache]
        ['datomic.config :as 'config]
        ['datomic.transaction :as 'tx]
        ['datomic.common :as 'common]
        ['datomic.error :as 'error]
        ['datomic.coordination :as 'coord]
        ['datomic.catalog :as 'catalog]
        ['datomic.cleanup :as 'cleanup]
        ['datomic.uri :as 'uri]
        ['datomic.queue :as 'queue]
        ['datomic.promise :as 'promise])
      (clojure.core/import 'datomic.db.IDbImpl)
      (clojure.core/import 'java.util.Map)
      (clojure.core/import 'java.lang.ref.WeakReference)))
  (when-not (.equals 'datomic.connector 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.connector))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.artemis-client :as 'aclient]
          ['datomic.slf4j :as 'logger]
          ['datomic.aws-detect :as 'aws-detect]
          ['datomic.cache :as 'cache]
          ['datomic.config :as 'config]
          ['datomic.transaction :as 'tx]
          ['datomic.common :as 'common]
          ['datomic.error :as 'error]
          ['datomic.coordination :as 'coord]
          ['datomic.catalog :as 'catalog]
          ['datomic.cleanup :as 'cleanup]
          ['datomic.uri :as 'uri]
          ['datomic.queue :as 'queue]
          ['datomic.promise :as 'promise])
        (clojure.core/import 'datomic.db.IDbImpl)
        (clojure.core/import 'java.util.Map)
        (clojure.core/import 'java.lang.ref.WeakReference))))
  (set! *warn-on-reflection* true)
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol Startable (start [_] "Idempotently start a task, returning a future."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.connector" "Startable")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'Startable :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'start {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Idempotently start a task, returning a future."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.connector" "Startable"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "start")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (let [protocol_metadata__7434 {:column (int 1)}]
    (defprotocol
      NotificationHandler
      (notify-sync [_ id] "Sync completed")
      (notify-data [_ msg] "Transaction completed. msg has id/data/tempids/io-stats")
      (notify-error [_ id error] "Transaction id failed, with error")
      (notify-db [_ db] "New database value. Used when catching up during connect or recovery.")
      (notify-index [_] "Database has a new index in storage."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.connector" "NotificationHandler")
      (assoc (assoc protocol_metadata__7434 :doc nil) :name 'NotificationHandler :ns *ns*))
    (let [protocol_signature__7435 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'notify-sync
                                        {:arglists (clojure.core/list ['_ 'id])}),
                                      :arglists (clojure.core/list ['_ 'id]),
                                      :doc "Sync completed"}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "NotificationHandler"))
          protocol_method_name__7436 (with-meta
                                       (:name protocol_signature__7435)
                                       protocol_signature__7435)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-sync")
        (assoc protocol_signature__7435 :name protocol_method_name__7436 :ns *ns*)))
    (let [protocol_signature__7437 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'notify-data
                                        {:arglists (clojure.core/list ['_ 'msg])}),
                                      :arglists (clojure.core/list ['_ 'msg]),
                                      :doc
                                      "Transaction completed. msg has id/data/tempids/io-stats"}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "NotificationHandler"))
          protocol_method_name__7438 (with-meta
                                       (:name protocol_signature__7437)
                                       protocol_signature__7437)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-data")
        (assoc protocol_signature__7437 :name protocol_method_name__7438 :ns *ns*)))
    (let [protocol_signature__7439 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'notify-error
                                        {:arglists (clojure.core/list ['_ 'id 'error])}),
                                      :arglists (clojure.core/list ['_ 'id 'error]),
                                      :doc "Transaction id failed, with error"}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "NotificationHandler"))
          protocol_method_name__7440 (with-meta
                                       (:name protocol_signature__7439)
                                       protocol_signature__7439)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-error")
        (assoc protocol_signature__7439 :name protocol_method_name__7440 :ns *ns*)))
    (let [protocol_signature__7441 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'notify-db
                                        {:arglists (clojure.core/list ['_ 'db])}),
                                      :arglists (clojure.core/list ['_ 'db]),
                                      :doc
                                      "New database value. Used when catching up during connect or recovery."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "NotificationHandler"))
          protocol_method_name__7442 (with-meta
                                       (:name protocol_signature__7441)
                                       protocol_signature__7441)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-db")
        (assoc protocol_signature__7441 :name protocol_method_name__7442 :ns *ns*)))
    (let [protocol_signature__7443 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'notify-index
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Database has a new index in storage."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "NotificationHandler"))
          protocol_method_name__7444 (with-meta
                                       (:name protocol_signature__7443)
                                       protocol_signature__7443)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-index")
        (assoc protocol_signature__7443 :name protocol_method_name__7444 :ns *ns*))))
  (let [protocol_metadata__7445 {:column (int 1)}]
    (defprotocol
      TransactorConnector
      (endpoint [_] "Return the transactor endpoint or nil.")
      (admin-request*
        [_ request arg timeout-msec]
        "Send request to transactor. Returns map that will have :db/error and :message if error.")
      (create-notifier
        [_ handler failure-handler]
        "Create notifier, but do not start processing. Returns value implements Startable and AsyncShutdown")
      (start-updater
        [_ update-queue push-handler failure-handler]
        "Start updater, taking transactions from update-queue and queuing them to the transactor. Return value implements AsyncShutdown. Calls failure-handler with no args if update put fails. Calls push-handler (a NotificationHandler) with any errors that occur prior to remote call"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.connector" "TransactorConnector")
      (assoc (assoc protocol_metadata__7445 :doc nil) :name 'TransactorConnector :ns *ns*))
    (let [protocol_signature__7446 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'endpoint {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Return the transactor endpoint or nil."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "TransactorConnector"))
          protocol_method_name__7447 (with-meta
                                       (:name protocol_signature__7446)
                                       protocol_signature__7446)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "endpoint")
        (assoc protocol_signature__7446 :name protocol_method_name__7447 :ns *ns*)))
    (let [protocol_signature__7448 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'admin-request*
                                        {:arglists
                                         (clojure.core/list ['_ 'request 'arg 'timeout-msec])}),
                                      :arglists
                                      (clojure.core/list ['_ 'request 'arg 'timeout-msec]),
                                      :doc
                                      "Send request to transactor. Returns map that will have :db/error and :message if error."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "TransactorConnector"))
          protocol_method_name__7449 (with-meta
                                       (:name protocol_signature__7448)
                                       protocol_signature__7448)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "admin-request*")
        (assoc protocol_signature__7448 :name protocol_method_name__7449 :ns *ns*)))
    (let [protocol_signature__7450 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'create-notifier
                                        {:arglists
                                         (clojure.core/list ['_ 'handler 'failure-handler])}),
                                      :arglists (clojure.core/list ['_ 'handler 'failure-handler]),
                                      :doc
                                      "Create notifier, but do not start processing. Returns value implements Startable and AsyncShutdown"}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "TransactorConnector"))
          protocol_method_name__7451 (with-meta
                                       (:name protocol_signature__7450)
                                       protocol_signature__7450)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "create-notifier")
        (assoc protocol_signature__7450 :name protocol_method_name__7451 :ns *ns*)))
    (let [protocol_signature__7452 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'start-updater
                                        {:arglists
                                         (clojure.core/list
                                           ['_ 'update-queue 'push-handler 'failure-handler])}),
                                      :arglists
                                      (clojure.core/list
                                        ['_ 'update-queue 'push-handler 'failure-handler]),
                                      :doc
                                      "Start updater, taking transactions from update-queue and queuing them to the transactor. Return value implements AsyncShutdown. Calls failure-handler with no args if update put fails. Calls push-handler (a NotificationHandler) with any errors that occur prior to remote call"}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "TransactorConnector"))
          protocol_method_name__7453 (with-meta
                                       (:name protocol_signature__7452)
                                       protocol_signature__7452)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "start-updater")
        (assoc protocol_signature__7452 :name protocol_method_name__7453 :ns *ns*))))
  (def admin-request
   (fn admin_request
     ([connector request arg timeout_msec]
       (let [result (admin-request* connector request arg timeout_msec)]
         (when (:db/error result) (throw (ex-info (:message result) (dissoc result :message))))
         (:value result)))
     ([connector request arg] (admin-request connector request arg 60000))))
  (reset-meta!
    #'admin-request
    (assoc
      {:arglists
       (clojure.core/list ['connector 'request 'arg] ['connector 'request 'arg 'timeout-msec]),
       :column (int 1)}
      :name
      'admin-request
      :ns
      *ns*))
  (def endpoint-error
   (fn endpoint_error
     ([p__20007 cause]
       (let [map__20008 p__20007
             map__20008 (if (seq? map__20008)
                          (if (next map__20008)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__20008))
                            (if (seq map__20008) (first map__20008) {}))
                          map__20008)
             endpoint map__20008
             host (get map__20008 :host)
             alt_host (get map__20008 :alt-host)
             port (get map__20008 :port)]
         (ex-info
           (str
             "Error communicating with HOST "
             host
             (if alt_host (str " or ALT_HOST " alt_host) "")
             " on PORT "
             port)
           (logger/redact endpoint #{:password})
           cause)))))
  (reset-meta!
    #'endpoint-error
    (assoc
      {:arglists (clojure.core/list [{:keys ['host 'alt-host 'port], :as 'endpoint} 'cause]),
       :column (int 1)}
      :name
      'endpoint-error
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.connector" "sfb-cache") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.connector" "sfb-cache") (cache/create-soft-limited 10))
  (defn stop-all-connectors
    ([]
      (loop [seq_20010 (seq (cache/cache-keys sfb-cache)) chunk_20011 nil count_20012 0 i_20013 0]
        (if (< i_20013 count_20012)
          (let [k (.nth ^clojure.lang.Indexed chunk_20011 (int i_20013))]
            (let [temp__5804__auto__ (cache/remove sfb-cache k)]
              (when temp__5804__auto__
                (let [sfb temp__5804__auto__] (deref (common/async-shutdown sfb)))))
            (recur seq_20010 chunk_20011 count_20012 (inc i_20013)))
          (let [temp__5804__auto__ (seq seq_20010)]
            (when temp__5804__auto__
              (let [seq_20010 temp__5804__auto__]
                (if (chunked-seq? seq_20010)
                  (let [c__6065__auto__ (chunk-first seq_20010)]
                    (recur
                      (chunk-rest seq_20010)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [k (first seq_20010)]
                    (let [temp__5804__auto__ (cache/remove sfb-cache k)]
                      (when temp__5804__auto__
                        (let [sfb temp__5804__auto__] (deref (common/async-shutdown sfb)))))
                    (recur (next seq_20010) nil 0 0))))))))))
  (reset-meta!
    #'stop-all-connectors
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'stop-all-connectors
      :ns
      *ns*))
  (def try-hornet-connect
   (fn try_hornet_connect
     ([conn_factory conn_args session_args]
       (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
         (when (.isDebugEnabled ^org.slf4j.Logger logger)
           (.debug
             ^org.slf4j.Logger logger
             (logger/process {:event :peer/hornet-connect, :host (:host conn_args)})))
         nil)
       (let [cache_key [conn_factory conn_args session_args]]
         (try
           (let [temp__5802__auto__ (get sfb-cache cache_key)]
             (if temp__5802__auto__
               (let [bundle temp__5802__auto__]
                 (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                     (.debug
                       ^org.slf4j.Logger logger
                       (logger/process
                         {:event :peer/hornet-reuse-factory, :host (:host conn_args)})))
                   nil)
                 bundle)
               (let [connector (apply
                                 aclient/create-connector
                                 conn_factory
                                 :verifyHost
                                 false
                                 :trustStorePath
                                 "datomic/transactor-trust.jks"
                                 :trustStorePassword
                                 "transactor"
                                 (mapcat identity conn_args))
                     bundle (aclient/create-session-factory connector session_args)]
                 (cleanup/register-cleanup
                   (deref cleanup/shared-manager-ref)
                   bundle
                   (fn fn__20019
                     ([] ((.-cleanup ^datomic.artemis_client.SessionFactoryBundle bundle)))))
                 (cache/put sfb-cache cache_key bundle)
                 bundle)))
           (catch
             java.lang.Throwable
             e
             (do
               (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                 (when (.isDebugEnabled ^org.slf4j.Logger logger)
                   (.debug
                     ^org.slf4j.Logger logger
                     (logger/process
                       {:event :peer/hornet-connect-failed, :host (:host conn_args)})))
                 nil)
               e)))))))
  (reset-meta!
    #'try-hornet-connect
    (assoc
      {:private true,
       :arglists (clojure.core/list ['conn-factory 'conn-args 'session-args]),
       :column (int 1)}
      :name
      'try-hornet-connect
      :ns
      *ns*))
  (def host-order
   (fn host_order
     ([host alt_host]
       (cond
         (nil? host) [alt_host]
         (nil? alt_host) [host]
         :default (do
                    (remove
                      nil?
                      (if (aws-detect/running-in-ec2?) [host alt_host] [alt_host host])))))))
  (reset-meta!
    #'host-order
    (assoc
      {:private true, :arglists (clojure.core/list ['host 'alt-host]), :column (int 1)}
      :name
      'host-order
      :ns
      *ns*))
  (def create-hornet-factory
   (fn create_hornet_factory
     ([p__20024 ttl]
       (let [map__20025 p__20024
             map__20025 (if (seq? map__20025)
                          (if (next map__20025)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__20025))
                            (if (seq map__20025) (first map__20025) {}))
                          map__20025)
             endpoint map__20025
             host (get map__20025 :host)
             port (get map__20025 :port)
             alt_host (get map__20025 :alt-host)
             encrypt_channel (get map__20025 :encrypt-channel)
             conn_args {:port port,
                        :sslEnabled encrypt_channel,
                        :keyStorePath "datomic/transactor-key.jks",
                        :keyStorePassword "transactor"}
             session_args {:ttl ttl}
             G__20029 (host-order host alt_host)
             vec__20030 G__20029
             seq__20031 (seq vec__20030)
             first__20032 (first seq__20031)
             seq__20031 (next seq__20031)
             host first__20032
             more seq__20031]
         (loop [G__20029 G__20029]
           (let [vec__20033 G__20029
                 seq__20034 (seq vec__20033)
                 first__20035 (first seq__20034)
                 seq__20034 (next seq__20034)
                 host first__20035
                 more seq__20034
                 result (try-hornet-connect
                          aclient/netty-connector-factory
                          (assoc conn_args :host host)
                          session_args)]
             (if (instance? java.lang.Throwable result)
               (if more (recur more) (do (throw (endpoint-error endpoint result)) nil))
               result)))))))
  (reset-meta!
    #'create-hornet-factory
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [{:keys ['host 'port 'alt-host 'encrypt-channel], :as 'endpoint} 'ttl]),
       :column (int 1)}
      :name
      'create-hornet-factory
      :ns
      *ns*))
  (deftype
    HornetNotifier
    [push_handler_ref session result_queue hornet_consumer starter cleanup]
    datomic.common.AsyncShutdown
    datomic.connector.Startable
    (start [this] (deref starter))
    (async-shutdown
      [this]
      (future-call
        (fn fn__20038
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8798__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")
                        ex t__8798__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (datomic.monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__8798__auto__)
                  nil))))))))
  (clojure.core/import 'datomic.connector.HornetNotifier)
  (def ->HornetNotifier
   (fn __GT_HornetNotifier
     ([push_handler_ref session result_queue hornet_consumer starter cleanup]
       (datomic.connector.HornetNotifier.
         push_handler_ref
         session
         result_queue
         hornet_consumer
         starter
         cleanup))))
  (reset-meta!
    #'->HornetNotifier
    (assoc
      {:arglists
       (clojure.core/list
         ['push-handler-ref 'session 'result-queue 'hornet-consumer 'starter 'cleanup]),
       :column (int 1)}
      :name
      '->HornetNotifier
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.connector" "notify") {:column (int 1)})
  (let [v__5792__auto__ #'notify]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.connector" "notify") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.connector" "notify")
        (clojure.lang.MultiFn.
          "notify"
          (fn fn__20045 ([m conn] (tx/peer-message-type m)))
          :default
          #'clojure.core/global-hierarchy))
      #'notify))
  (defmethod notify :tx fn__20050 ([msg conn] (notify-data conn msg)))
  (defmethod notify :error fn__20052 ([msg conn] (notify-error conn (:id msg) msg)))
  (defmethod notify :index fn__20054 ([msg conn] (notify-index conn)))
  (defmethod notify :sync fn__20056 ([msg conn] (notify-sync conn (:id msg))))
  (def create-hornet-notifier
   (fn create_hornet_notifier
     ([push_handler session result_queue hornet_consumer failure_handler]
       (let [push_handler_ref (java.lang.ref.WeakReference. push_handler)
             cleanup_ref (promise)
             done_ref (promise)
             starter (delay
                       (future-call
                         (fn fn__20059
                           ([]
                             (try
                               (let [m_20060 {:event :connector/notify-loop}
                                     ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.connector")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_20060 :phase :begin))))
                                                       nil)
                                     start__8553__auto__ (java.lang.System/nanoTime)
                                     result__8554__auto__ (try
                                                            {:returned
                                                             (try
                                                               (try
                                                                 (loop 
                                                                   [msges
                                                                    (queue/take hornet_consumer)]
                                                                   (when
                                                                     (and
                                                                       msges
                                                                       (not (realized? done_ref)))
                                                                     (let 
                                                                       [temp__5804__auto__
                                                                        (.get
                                                                          ^java.lang.ref.Reference push_handler_ref)]
                                                                       (when
                                                                         temp__5804__auto__
                                                                         (let 
                                                                           [push_handler
                                                                            temp__5804__auto__]
                                                                           (loop 
                                                                             [seq_20065
                                                                              (seq
                                                                                (aclient/read-batch
                                                                                  msges
                                                                                  tx/read-handlers))
                                                                              chunk_20066 nil
                                                                              count_20067 0
                                                                              i_20068 0]
                                                                             (if
                                                                               (<
                                                                                 i_20068
                                                                                 count_20067)
                                                                               (let 
                                                                                 [msg
                                                                                  (.nth
                                                                                    ^clojure.lang.Indexed chunk_20066
                                                                                    (int i_20068))]
                                                                                 (notify
                                                                                   msg
                                                                                   push_handler)
                                                                                 (recur
                                                                                   seq_20065
                                                                                   chunk_20066
                                                                                   count_20067
                                                                                   (inc i_20068)))
                                                                               (let 
                                                                                 [temp__5804__auto__
                                                                                  (seq seq_20065)]
                                                                                 (when
                                                                                   temp__5804__auto__
                                                                                   (let 
                                                                                     [seq_20065
                                                                                      temp__5804__auto__]
                                                                                     (if
                                                                                       (chunked-seq?
                                                                                         seq_20065)
                                                                                       (let 
                                                                                         [c__6065__auto__
                                                                                          (chunk-first
                                                                                            seq_20065)]
                                                                                         (recur
                                                                                           (chunk-rest
                                                                                             seq_20065)
                                                                                           c__6065__auto__
                                                                                           (int
                                                                                             (count
                                                                                               c__6065__auto__))
                                                                                           (int
                                                                                             0)))
                                                                                       (let 
                                                                                         [msg
                                                                                          (first
                                                                                            seq_20065)]
                                                                                         (notify
                                                                                           msg
                                                                                           push_handler)
                                                                                         (recur
                                                                                           (next
                                                                                             seq_20065)
                                                                                           nil
                                                                                           0
                                                                                           0))))))))
                                                                           (recur
                                                                             (queue/take
                                                                               hornet_consumer)))))))
                                                                 (catch
                                                                   java.lang.Throwable
                                                                   t
                                                                   (when-not
                                                                     (instance?
                                                                       java.lang.InterruptedException
                                                                       t)
                                                                     (^clojure.lang.IFn failure_handler
                                                                       t)
                                                                     (throw ^java.lang.Throwable t)
                                                                     nil)))
                                                               (finally ((deref cleanup_ref))))}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8555__auto__
                                                              {:threw t__8555__auto__}))
                                     elapsed_20061 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8553__auto__)
                                     msec_20062 (logger/format-as-msec (long elapsed_20061))]
                                 (let [endmsg__8556__auto__ (merge
                                                              (assoc
                                                                m_20060
                                                                :msec
                                                                msec_20062
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8554__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8554__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.connector")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8556__auto__)))
                                   nil)
                                 (if (contains? result__8554__auto__ :returned)
                                   (:returned result__8554__auto__)
                                   (do (throw (:threw result__8554__auto__)) nil)))
                               (catch
                                 java.lang.Throwable
                                 t__8798__auto__
                                 (do
                                   (let [logger (org.slf4j.LoggerFactory/getLogger
                                                  "datomic.connector")
                                         ex t__8798__auto__]
                                     (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                       (.warn
                                         ^org.slf4j.Logger logger
                                         (logger/process "error executing future")
                                         ^java.lang.Throwable ex)
                                       (logger/caused-by logger ex))
                                     nil)
                                   (datomic.monitor/alarm :UnhandledException)
                                   (throw ^java.lang.Throwable t__8798__auto__)
                                   nil)))))))
             cleanup (error/runonce
                       (fn fn__20081
                         ([]
                           (let [m_20082 {:event :connector/notify-cleanup}
                                 ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                "datomic.connector")]
                                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                     (.debug
                                                       ^org.slf4j.Logger logger
                                                       (logger/process
                                                         (assoc m_20082 :phase :begin))))
                                                   nil)
                                 start__8553__auto__ (java.lang.System/nanoTime)
                                 result__8554__auto__ (try
                                                        (do
                                                          (deliver done_ref true)
                                                          (common/sync-shutdown hornet_consumer)
                                                          (aclient/delete-queue
                                                            session
                                                            result_queue)
                                                          {:returned
                                                           (common/sync-shutdown session)})
                                                        (catch
                                                          java.lang.Throwable
                                                          t__8555__auto__
                                                          {:threw t__8555__auto__}))
                                 elapsed_20083 (- (java.lang.System/nanoTime) start__8553__auto__)
                                 msec_20084 (logger/format-as-msec (long elapsed_20083))]
                             (let [endmsg__8556__auto__ (merge
                                                          (assoc
                                                            m_20082
                                                            :msec
                                                            msec_20084
                                                            :phase
                                                            :end)
                                                          (when (:threw result__8554__auto__)
                                                            {:threw
                                                             (class
                                                               (:threw result__8554__auto__))}))
                                   logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                               (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                 (.debug
                                   ^org.slf4j.Logger logger
                                   (logger/process endmsg__8556__auto__)))
                               nil)
                             (if (contains? result__8554__auto__ :returned)
                               (:returned result__8554__auto__)
                               (do (throw (:threw result__8554__auto__)) nil))))))]
         (deliver cleanup_ref cleanup)
         (datomic.connector.HornetNotifier.
           push_handler_ref
           session
           result_queue
           hornet_consumer
           starter
           cleanup)))))
  (reset-meta!
    #'create-hornet-notifier
    (assoc
      {:arglists
       (clojure.core/list
         ['push-handler 'session 'result-queue 'hornet-consumer 'failure-handler]),
       :column (int 1)}
      :name
      'create-hornet-notifier
      :ns
      *ns*))
  (deftype
    TransactorHornetConnector
    [cluster_conf transactor_endpoint hornet_factory]
    datomic.connector.TransactorConnector
    datomic.common.AsyncShutdown
    (async-shutdown [this] nil)
    (endpoint [this] transactor_endpoint)
    (start-updater
      [this unsent_updates_queue push_handler failure_handler]
      (let [map__20115 cluster_conf
            map__20115 (if (seq? map__20115)
                         (if (next map__20115)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20115))
                           (if (seq map__20115) (first map__20115) {}))
                         map__20115)
            db_id (get map__20115 :db-id)
            session (aclient/start-session
                      hornet_factory
                      transactor_endpoint
                      :on-failure
                      failure_handler
                      :pre-acknowledge
                      true)
            hornet_producer (aclient/create-producer session (tx/submit-address db_id))
            handlers (tx/write-handlers true)
            fut (future-call
                  (fn fn__20116
                    ([]
                      (try
                        (let [m_20117 {:event :connector/updater-loop}
                              ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.connector")]
                                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                  (.debug
                                                    ^org.slf4j.Logger logger
                                                    (logger/process
                                                      (assoc m_20117 :phase :begin))))
                                                nil)
                              start__8553__auto__ (java.lang.System/nanoTime)
                              result__8554__auto__ (try
                                                     {:returned
                                                      (try
                                                        (loop []
                                                          (let [tx
                                                                (queue/take unsent_updates_queue)
                                                                msg
                                                                (try
                                                                  (aclient/create-fressian-message
                                                                    session
                                                                    handlers
                                                                    tx
                                                                    false)
                                                                  (catch java.lang.Throwable e e))]
                                                            (when
                                                              (instance? java.lang.Throwable msg)
                                                              (notify-error
                                                                push_handler
                                                                (:id tx)
                                                                msg)
                                                              (recur))))
                                                        (catch
                                                          java.lang.InterruptedException
                                                          _
                                                          nil))}
                                                     (catch
                                                       java.lang.Throwable
                                                       t__8555__auto__
                                                       {:threw t__8555__auto__}))
                              elapsed_20118 (- (java.lang.System/nanoTime) start__8553__auto__)
                              msec_20119 (logger/format-as-msec (long elapsed_20118))]
                          (let [endmsg__8556__auto__ (merge
                                                       (assoc m_20117 :msec msec_20119 :phase :end)
                                                       (when (:threw result__8554__auto__)
                                                         {:threw
                                                          (class (:threw result__8554__auto__))}))
                                logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                              (.debug
                                ^org.slf4j.Logger logger
                                (logger/process endmsg__8556__auto__)))
                            nil)
                          (if (contains? result__8554__auto__ :returned)
                            (:returned result__8554__auto__)
                            (do (throw (:threw result__8554__auto__)) nil)))
                        (catch
                          java.lang.Throwable
                          t__8798__auto__
                          (do
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")
                                  ex t__8798__auto__]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process "error executing future")
                                  ^java.lang.Throwable ex)
                                (logger/caused-by logger ex))
                              nil)
                            (datomic.monitor/alarm :UnhandledException)
                            (throw ^java.lang.Throwable t__8798__auto__)
                            nil))))))
            cleanup (delay
                      (let [m_20134 {:event :connector/updater-cleanup}
                            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                           "datomic.connector")]
                                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                (.debug
                                                  ^org.slf4j.Logger logger
                                                  (logger/process (assoc m_20134 :phase :begin))))
                                              nil)
                            start__8553__auto__ (java.lang.System/nanoTime)
                            result__8554__auto__ (try
                                                   (do
                                                     (future-cancel fut)
                                                     (deref
                                                       (common/async-shutdown hornet_producer))
                                                     {:returned
                                                      (deref (common/async-shutdown session))})
                                                   (catch
                                                     java.lang.Throwable
                                                     t__8555__auto__
                                                     {:threw t__8555__auto__}))
                            elapsed_20135 (- (java.lang.System/nanoTime) start__8553__auto__)
                            msec_20136 (logger/format-as-msec (long elapsed_20135))]
                        (let [endmsg__8556__auto__ (merge
                                                     (assoc m_20134 :msec msec_20136 :phase :end)
                                                     (when (:threw result__8554__auto__)
                                                       {:threw
                                                        (class (:threw result__8554__auto__))}))
                              logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                            (.debug
                              ^org.slf4j.Logger logger
                              (logger/process endmsg__8556__auto__)))
                          nil)
                        (if (contains? result__8554__auto__ :returned)
                          (:returned result__8554__auto__)
                          (do (throw (:threw result__8554__auto__)) nil))))]
        (reify
          datomic.common.AsyncShutdown
          (async-shutdown [this] (promise/delivered (deref cleanup))))))
    (create-notifier
      [this push_handler failure_handler]
      (let [map__20096 cluster_conf
            map__20096 (if (seq? map__20096)
                         (if (next map__20096)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20096))
                           (if (seq map__20096) (first map__20096) {}))
                         map__20096)
            db_id (get map__20096 :db-id)
            session (aclient/start-session
                      hornet_factory
                      transactor_endpoint
                      :on-failure
                      failure_handler
                      :pre-acknowledge
                      true)
            unused (error/runonce
                     (fn fn__20097
                       ([]
                         (try
                           (common/sync-shutdown session)
                           (catch
                             java.lang.Throwable
                             t__8509__auto__
                             (error/report t__8509__auto__)))
                         nil)))]
        (try
          (let [result_queue (aclient/create-temporary-queue session (tx/push-address db_id))
                unused (error/runonce
                         (fn fn__20101
                           ([]
                             (try
                               ((partial aclient/delete-queue session) result_queue)
                               (catch
                                 java.lang.Throwable
                                 t__8509__auto__
                                 (error/report t__8509__auto__)))
                             (^clojure.lang.IFn unused))))]
            (try
              (let [hornet_consumer (aclient/create-consumer session result_queue)
                    unused (error/runonce
                             (fn fn__20105
                               ([]
                                 (try
                                   (common/sync-shutdown hornet_consumer)
                                   (catch
                                     java.lang.Throwable
                                     t__8509__auto__
                                     (error/report t__8509__auto__)))
                                 (^clojure.lang.IFn unused))))]
                (try
                  (create-hornet-notifier
                    push_handler
                    session
                    result_queue
                    hornet_consumer
                    failure_handler)
                  (catch
                    java.lang.Throwable
                    t__8510__auto__
                    (do
                      (try
                        (common/sync-shutdown hornet_consumer)
                        (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
                      (throw ^java.lang.Throwable t__8510__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__8510__auto__
                (do
                  (try
                    ((partial aclient/delete-queue session) result_queue)
                    (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
                  (throw ^java.lang.Throwable t__8510__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__8510__auto__
            (do
              (try
                (common/sync-shutdown session)
                (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
              (throw ^java.lang.Throwable t__8510__auto__)
              nil)))))
    (admin-request*
      [this request arg timeout_msec]
      (let [timeout (java.lang.Object.)
            result (try
                     (let [session (aclient/start-session
                                     hornet_factory
                                     transactor_endpoint
                                     :pre-acknowledge
                                     true)]
                       (try
                         (let [rpc_client (aclient/create-rpc-client
                                            session
                                            "admin.request"
                                            "admin.response")]
                           (try
                             (deref
                               (aclient/rpc-request
                                 rpc_client
                                 (seq
                                   (concat (clojure.core/list request) (clojure.core/list arg))))
                               timeout_msec
                               timeout)
                             (finally (deref (common/async-shutdown rpc_client)))))
                         (finally (deref (common/async-shutdown session)))))
                     (catch
                       java.lang.Throwable
                       t
                       (do
                         (cache/clear sfb-cache)
                         (throw (endpoint-error transactor_endpoint t))
                         nil)))]
        (cond
          (= timeout result) (do
                               (cache/clear sfb-cache)
                               {:db/error :peer/request-timed-out,
                                :message "Transactor request timed out",
                                :request request,
                                :result result})
          (:failed result) {:db/error :peer/request-failed,
                            :message (:failed result),
                            :request request,
                            :result result}
          :default (do
                     (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                         (.debug
                           ^org.slf4j.Logger logger
                           (logger/process
                             {:event :peer/admin-request,
                              :request request,
                              :return (:value result)})))
                       nil)
                     result)))))
  (clojure.core/import 'datomic.connector.TransactorHornetConnector)
  (def ->TransactorHornetConnector
   (fn __GT_TransactorHornetConnector
     ([cluster_conf transactor_endpoint hornet_factory]
       (datomic.connector.TransactorHornetConnector.
         cluster_conf
         transactor_endpoint
         hornet_factory))))
  (reset-meta!
    #'->TransactorHornetConnector
    (assoc
      {:arglists (clojure.core/list ['cluster-conf 'transactor-endpoint 'hornet-factory]),
       :column (int 1)}
      :name
      '->TransactorHornetConnector
      :ns
      *ns*))
  (def create-transactor-hornet-connector
   (fn create_transactor_hornet_connector
     ([cluster_conf endpoint ttl]
       (let [hornet_factory (create-hornet-factory endpoint ttl)]
         (datomic.connector.TransactorHornetConnector. cluster_conf endpoint hornet_factory)))
     ([cluster_conf endpoint]
       (create-transactor-hornet-connector
         cluster_conf
         endpoint
         (config/property "datomic.peerConnectionTTLMsec")))))
  (reset-meta!
    #'create-transactor-hornet-connector
    (assoc
      {:arglists (clojure.core/list ['cluster-conf 'endpoint] ['cluster-conf 'endpoint 'ttl]),
       :column (int 1)}
      :name
      'create-transactor-hornet-connector
      :ns
      *ns*)))