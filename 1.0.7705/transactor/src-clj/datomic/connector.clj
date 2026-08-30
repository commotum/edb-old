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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Startable (start [_] "Idempotently start a task, returning a future."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.connector" "Startable")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Startable :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'start {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Idempotently start a task, returning a future."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.connector" "Startable"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "start")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol
      NotificationHandler
      (notify-sync [_ id] "Sync completed")
      (notify-data [_ msg] "Transaction completed. msg has id/data/tempids/io-stats")
      (notify-error [_ id error] "Transaction id failed, with error")
      (notify-db [_ db] "New database value. Used when catching up during connect or recovery.")
      (notify-index [_] "Database has a new index in storage."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.connector" "NotificationHandler")
      (assoc (assoc protocol_metadata__7466 :doc nil) :name 'NotificationHandler :ns *ns*))
    (let [protocol_signature__7467 (assoc
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
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-sync")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*)))
    (let [protocol_signature__7469 (assoc
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
          protocol_method_name__7470 (with-meta
                                       (:name protocol_signature__7469)
                                       protocol_signature__7469)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-data")
        (assoc protocol_signature__7469 :name protocol_method_name__7470 :ns *ns*)))
    (let [protocol_signature__7471 (assoc
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
          protocol_method_name__7472 (with-meta
                                       (:name protocol_signature__7471)
                                       protocol_signature__7471)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-error")
        (assoc protocol_signature__7471 :name protocol_method_name__7472 :ns *ns*)))
    (let [protocol_signature__7473 (assoc
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
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-db")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*)))
    (let [protocol_signature__7475 (assoc
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
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "notify-index")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*))))
  (let [protocol_metadata__7477 {:column (int 1)}]
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
      (assoc (assoc protocol_metadata__7477 :doc nil) :name 'TransactorConnector :ns *ns*))
    (let [protocol_signature__7478 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'endpoint {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Return the transactor endpoint or nil."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.connector"
                                       "TransactorConnector"))
          protocol_method_name__7479 (with-meta
                                       (:name protocol_signature__7478)
                                       protocol_signature__7478)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "endpoint")
        (assoc protocol_signature__7478 :name protocol_method_name__7479 :ns *ns*)))
    (let [protocol_signature__7480 (assoc
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
          protocol_method_name__7481 (with-meta
                                       (:name protocol_signature__7480)
                                       protocol_signature__7480)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "admin-request*")
        (assoc protocol_signature__7480 :name protocol_method_name__7481 :ns *ns*)))
    (let [protocol_signature__7482 (assoc
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
          protocol_method_name__7483 (with-meta
                                       (:name protocol_signature__7482)
                                       protocol_signature__7482)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "create-notifier")
        (assoc protocol_signature__7482 :name protocol_method_name__7483 :ns *ns*)))
    (let [protocol_signature__7484 (assoc
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
          protocol_method_name__7485 (with-meta
                                       (:name protocol_signature__7484)
                                       protocol_signature__7484)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.connector" "start-updater")
        (assoc protocol_signature__7484 :name protocol_method_name__7485 :ns *ns*))))
  (defn admin-request
    ([connector request arg timeout_msec]
      (let [result (admin-request* connector request arg timeout_msec)]
        (when (:db/error result) (throw (ex-info (:message result) (dissoc result :message))))
        (:value result)))
    ([connector request arg] (admin-request connector request arg 60000)))
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
  (defn endpoint-error
    ([p__18992 cause]
      (let [map__18993 p__18992
            map__18993 (if (seq? map__18993)
                         (if (next map__18993)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18993))
                           (if (seq map__18993) (first map__18993) {}))
                         map__18993)
            endpoint map__18993
            host (get map__18993 :host)
            alt_host (get map__18993 :alt-host)
            port (get map__18993 :port)]
        (ex-info
          (str
            "Error communicating with HOST "
            host
            (if alt_host (str " or ALT_HOST " alt_host) "")
            " on PORT "
            port)
          (logger/redact endpoint #{:password})
          cause))))
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
      (loop [seq_18995 (seq (cache/cache-keys sfb-cache)) chunk_18996 nil count_18997 0 i_18998 0]
        (if (< i_18998 count_18997)
          (let [k (.nth ^clojure.lang.Indexed chunk_18996 (int i_18998))]
            (let [temp__5825__auto__ (cache/remove sfb-cache k)]
              (when temp__5825__auto__
                (let [sfb temp__5825__auto__] (deref (common/async-shutdown sfb)))))
            (recur seq_18995 chunk_18996 count_18997 (inc i_18998)))
          (let [temp__5825__auto__ (seq seq_18995)]
            (when temp__5825__auto__
              (let [seq_18995 temp__5825__auto__]
                (if (chunked-seq? seq_18995)
                  (let [c__6090__auto__ (chunk-first seq_18995)]
                    (recur
                      (chunk-rest seq_18995)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [k (first seq_18995)]
                    (let [temp__5825__auto__ (cache/remove sfb-cache k)]
                      (when temp__5825__auto__
                        (let [sfb temp__5825__auto__] (deref (common/async-shutdown sfb)))))
                    (recur (next seq_18995) nil 0 0))))))))))
  (reset-meta!
    #'stop-all-connectors
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'stop-all-connectors
      :ns
      *ns*))
  (defn try-hornet-connect
    ([conn_factory conn_args session_args]
      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
        (when (.isDebugEnabled ^org.slf4j.Logger logger)
          (.debug
            ^org.slf4j.Logger logger
            (logger/process {:event :peer/hornet-connect, :host (:host conn_args)})))
        nil)
      (let [cache_key [conn_factory conn_args session_args]]
        (try
          (let [temp__5823__auto__ (get sfb-cache cache_key)]
            (if temp__5823__auto__
              (let [bundle temp__5823__auto__]
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
                  (fn fn__19004
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
              e))))))
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
  (defn host-order
    ([host alt_host]
      (cond
        (nil? host) [alt_host]
        (nil? alt_host) [host]
        :default (do
                   (remove
                     nil?
                     (if (aws-detect/running-in-ec2?) [host alt_host] [alt_host host]))))))
  (reset-meta!
    #'host-order
    (assoc
      {:private true, :arglists (clojure.core/list ['host 'alt-host]), :column (int 1)}
      :name
      'host-order
      :ns
      *ns*))
  (defn create-hornet-factory
    ([p__19009 ttl]
      (let [map__19010 p__19009
            map__19010 (if (seq? map__19010)
                         (if (next map__19010)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19010))
                           (if (seq map__19010) (first map__19010) {}))
                         map__19010)
            endpoint map__19010
            host (get map__19010 :host)
            port (get map__19010 :port)
            alt_host (get map__19010 :alt-host)
            encrypt_channel (get map__19010 :encrypt-channel)
            conn_args {:port port,
                       :sslEnabled encrypt_channel,
                       :keyStorePath "datomic/transactor-key.jks",
                       :keyStorePassword "transactor"}
            session_args {:ttl ttl}
            G__19014 (host-order host alt_host)
            vec__19015 G__19014
            seq__19016 (seq vec__19015)
            first__19017 (first seq__19016)
            seq__19016 (next seq__19016)
            host first__19017
            more seq__19016]
        (loop [G__19014 G__19014]
          (let [vec__19018 G__19014
                seq__19019 (seq vec__19018)
                first__19020 (first seq__19019)
                seq__19019 (next seq__19019)
                host first__19020
                more seq__19019
                result (try-hornet-connect
                         aclient/netty-connector-factory
                         (assoc conn_args :host host)
                         session_args)]
            (if (instance? java.lang.Throwable result)
              (if more (recur more) (do (throw (endpoint-error endpoint result)) nil))
              result))))))
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
        (fn fn__19023
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8765__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")
                        ex t__8765__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (datomic.monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__8765__auto__)
                  nil))))))))
  (clojure.core/import 'datomic.connector.HornetNotifier)
  (defn ->HornetNotifier
    ([push_handler_ref session result_queue hornet_consumer starter cleanup]
      (datomic.connector.HornetNotifier.
        push_handler_ref
        session
        result_queue
        hornet_consumer
        starter
        cleanup)))
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
  (let [v__5813__auto__ #'notify]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.connector" "notify") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.connector" "notify")
        (clojure.lang.MultiFn.
          "notify"
          (fn fn__19030 ([m conn] (tx/peer-message-type m)))
          :default
          #'clojure.core/global-hierarchy))
      #'notify))
  (defmethod notify :tx fn__19035 ([msg conn] (notify-data conn msg)))
  (defmethod notify :error fn__19037 ([msg conn] (notify-error conn (:id msg) msg)))
  (defmethod notify :index fn__19039 ([msg conn] (notify-index conn)))
  (defmethod notify :sync fn__19041 ([msg conn] (notify-sync conn (:id msg))))
  (defn create-hornet-notifier
    ([push_handler session result_queue hornet_consumer failure_handler]
      (let [push_handler_ref (java.lang.ref.WeakReference. push_handler)
            cleanup_ref (promise)
            done_ref (promise)
            starter (delay
                      (future-call
                        (fn fn__19044
                          ([]
                            (try
                              (let [m_19045 {:event :connector/notify-loop}
                                    ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.connector")]
                                                      (when (.isDebugEnabled
                                                              ^org.slf4j.Logger logger)
                                                        (.debug
                                                          ^org.slf4j.Logger logger
                                                          (logger/process
                                                            (assoc m_19045 :phase :begin))))
                                                      nil)
                                    start__8599__auto__ (java.lang.System/nanoTime)
                                    result__8600__auto__ (try
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
                                                                      [temp__5825__auto__
                                                                       (.get
                                                                         ^java.lang.ref.Reference push_handler_ref)]
                                                                      (when
                                                                        temp__5825__auto__
                                                                        (let 
                                                                          [push_handler
                                                                           temp__5825__auto__]
                                                                          (loop 
                                                                            [seq_19050
                                                                             (seq
                                                                               (aclient/read-batch
                                                                                 msges
                                                                                 tx/read-handlers))
                                                                             chunk_19051 nil
                                                                             count_19052 0
                                                                             i_19053 0]
                                                                            (if
                                                                              (<
                                                                                i_19053
                                                                                count_19052)
                                                                              (let 
                                                                                [msg
                                                                                 (.nth
                                                                                   ^clojure.lang.Indexed chunk_19051
                                                                                   (int i_19053))]
                                                                                (notify
                                                                                  msg
                                                                                  push_handler)
                                                                                (recur
                                                                                  seq_19050
                                                                                  chunk_19051
                                                                                  count_19052
                                                                                  (inc i_19053)))
                                                                              (let 
                                                                                [temp__5825__auto__
                                                                                 (seq seq_19050)]
                                                                                (when
                                                                                  temp__5825__auto__
                                                                                  (let 
                                                                                    [seq_19050
                                                                                     temp__5825__auto__]
                                                                                    (if
                                                                                      (chunked-seq?
                                                                                        seq_19050)
                                                                                      (let 
                                                                                        [c__6090__auto__
                                                                                         (chunk-first
                                                                                           seq_19050)]
                                                                                        (recur
                                                                                          (chunk-rest
                                                                                            seq_19050)
                                                                                          c__6090__auto__
                                                                                          (int
                                                                                            (count
                                                                                              c__6090__auto__))
                                                                                          (int 0)))
                                                                                      (let 
                                                                                        [msg
                                                                                         (first
                                                                                           seq_19050)]
                                                                                        (notify
                                                                                          msg
                                                                                          push_handler)
                                                                                        (recur
                                                                                          (next
                                                                                            seq_19050)
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
                                                             t__8601__auto__
                                                             {:threw t__8601__auto__}))
                                    elapsed_19046 (-
                                                    (java.lang.System/nanoTime)
                                                    start__8599__auto__)
                                    msec_19047 (logger/format-as-msec (long elapsed_19046))]
                                (let [endmsg__8602__auto__ (merge
                                                             (assoc
                                                               m_19045
                                                               :msec
                                                               msec_19047
                                                               :phase
                                                               :end)
                                                             (when
                                                               (:threw result__8600__auto__)
                                                               {:threw
                                                                (class
                                                                  (:threw result__8600__auto__))}))
                                      logger (org.slf4j.LoggerFactory/getLogger
                                               "datomic.connector")]
                                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                    (.debug
                                      ^org.slf4j.Logger logger
                                      (logger/process endmsg__8602__auto__)))
                                  nil)
                                (if (contains? result__8600__auto__ :returned)
                                  (:returned result__8600__auto__)
                                  (do (throw (:threw result__8600__auto__)) nil)))
                              (catch
                                java.lang.Throwable
                                t__8765__auto__
                                (do
                                  (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.connector")
                                        ex t__8765__auto__]
                                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                      (.warn
                                        ^org.slf4j.Logger logger
                                        (logger/process "error executing future")
                                        ^java.lang.Throwable ex)
                                      (logger/caused-by logger ex))
                                    nil)
                                  (datomic.monitor/alarm :UnhandledException)
                                  (throw ^java.lang.Throwable t__8765__auto__)
                                  nil)))))))
            cleanup (error/runonce
                      (fn fn__19066
                        ([]
                          (let [m_19067 {:event :connector/notify-cleanup}
                                ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                               "datomic.connector")]
                                                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                    (.debug
                                                      ^org.slf4j.Logger logger
                                                      (logger/process
                                                        (assoc m_19067 :phase :begin))))
                                                  nil)
                                start__8599__auto__ (java.lang.System/nanoTime)
                                result__8600__auto__ (try
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
                                                         t__8601__auto__
                                                         {:threw t__8601__auto__}))
                                elapsed_19068 (- (java.lang.System/nanoTime) start__8599__auto__)
                                msec_19069 (logger/format-as-msec (long elapsed_19068))]
                            (let [endmsg__8602__auto__ (merge
                                                         (assoc
                                                           m_19067
                                                           :msec
                                                           msec_19069
                                                           :phase
                                                           :end)
                                                         (when (:threw result__8600__auto__)
                                                           {:threw
                                                            (class
                                                              (:threw result__8600__auto__))}))
                                  logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process endmsg__8602__auto__)))
                              nil)
                            (if (contains? result__8600__auto__ :returned)
                              (:returned result__8600__auto__)
                              (do (throw (:threw result__8600__auto__)) nil))))))]
        (deliver cleanup_ref cleanup)
        (datomic.connector.HornetNotifier.
          push_handler_ref
          session
          result_queue
          hornet_consumer
          starter
          cleanup))))
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
      (let [map__19100 cluster_conf
            map__19100 (if (seq? map__19100)
                         (if (next map__19100)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19100))
                           (if (seq map__19100) (first map__19100) {}))
                         map__19100)
            db_id (get map__19100 :db-id)
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
                  (fn fn__19101
                    ([]
                      (try
                        (let [m_19102 {:event :connector/updater-loop}
                              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.connector")]
                                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                  (.debug
                                                    ^org.slf4j.Logger logger
                                                    (logger/process
                                                      (assoc m_19102 :phase :begin))))
                                                nil)
                              start__8599__auto__ (java.lang.System/nanoTime)
                              result__8600__auto__ (try
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
                                                       t__8601__auto__
                                                       {:threw t__8601__auto__}))
                              elapsed_19103 (- (java.lang.System/nanoTime) start__8599__auto__)
                              msec_19104 (logger/format-as-msec (long elapsed_19103))]
                          (let [endmsg__8602__auto__ (merge
                                                       (assoc m_19102 :msec msec_19104 :phase :end)
                                                       (when (:threw result__8600__auto__)
                                                         {:threw
                                                          (class (:threw result__8600__auto__))}))
                                logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                              (.debug
                                ^org.slf4j.Logger logger
                                (logger/process endmsg__8602__auto__)))
                            nil)
                          (if (contains? result__8600__auto__ :returned)
                            (:returned result__8600__auto__)
                            (do (throw (:threw result__8600__auto__)) nil)))
                        (catch
                          java.lang.Throwable
                          t__8765__auto__
                          (do
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")
                                  ex t__8765__auto__]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process "error executing future")
                                  ^java.lang.Throwable ex)
                                (logger/caused-by logger ex))
                              nil)
                            (datomic.monitor/alarm :UnhandledException)
                            (throw ^java.lang.Throwable t__8765__auto__)
                            nil))))))
            cleanup (delay
                      (let [m_19119 {:event :connector/updater-cleanup}
                            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                           "datomic.connector")]
                                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                (.debug
                                                  ^org.slf4j.Logger logger
                                                  (logger/process (assoc m_19119 :phase :begin))))
                                              nil)
                            start__8599__auto__ (java.lang.System/nanoTime)
                            result__8600__auto__ (try
                                                   (do
                                                     (future-cancel fut)
                                                     (deref
                                                       (common/async-shutdown hornet_producer))
                                                     {:returned
                                                      (deref (common/async-shutdown session))})
                                                   (catch
                                                     java.lang.Throwable
                                                     t__8601__auto__
                                                     {:threw t__8601__auto__}))
                            elapsed_19120 (- (java.lang.System/nanoTime) start__8599__auto__)
                            msec_19121 (logger/format-as-msec (long elapsed_19120))]
                        (let [endmsg__8602__auto__ (merge
                                                     (assoc m_19119 :msec msec_19121 :phase :end)
                                                     (when (:threw result__8600__auto__)
                                                       {:threw
                                                        (class (:threw result__8600__auto__))}))
                              logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                            (.debug
                              ^org.slf4j.Logger logger
                              (logger/process endmsg__8602__auto__)))
                          nil)
                        (if (contains? result__8600__auto__ :returned)
                          (:returned result__8600__auto__)
                          (do (throw (:threw result__8600__auto__)) nil))))]
        (reify
          datomic.common.AsyncShutdown
          (async-shutdown [this] (promise/delivered (deref cleanup))))))
    (create-notifier
      [this push_handler failure_handler]
      (let [map__19081 cluster_conf
            map__19081 (if (seq? map__19081)
                         (if (next map__19081)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19081))
                           (if (seq map__19081) (first map__19081) {}))
                         map__19081)
            db_id (get map__19081 :db-id)
            session (aclient/start-session
                      hornet_factory
                      transactor_endpoint
                      :on-failure
                      failure_handler
                      :pre-acknowledge
                      true)
            unused (error/runonce
                     (fn fn__19082
                       ([]
                         (try
                           (common/sync-shutdown session)
                           (catch
                             java.lang.Throwable
                             t__8574__auto__
                             (error/report t__8574__auto__)))
                         nil)))]
        (try
          (let [result_queue (aclient/create-temporary-queue session (tx/push-address db_id))
                unused (error/runonce
                         (fn fn__19086
                           ([]
                             (try
                               ((partial aclient/delete-queue session) result_queue)
                               (catch
                                 java.lang.Throwable
                                 t__8574__auto__
                                 (error/report t__8574__auto__)))
                             (^clojure.lang.IFn unused))))]
            (try
              (let [hornet_consumer (aclient/create-consumer session result_queue)
                    unused (error/runonce
                             (fn fn__19090
                               ([]
                                 (try
                                   (common/sync-shutdown hornet_consumer)
                                   (catch
                                     java.lang.Throwable
                                     t__8574__auto__
                                     (error/report t__8574__auto__)))
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
                    t__8575__auto__
                    (do
                      (try
                        (common/sync-shutdown hornet_consumer)
                        (catch java.lang.Throwable t__8574__auto__ (error/report t__8574__auto__)))
                      (throw ^java.lang.Throwable t__8575__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__8575__auto__
                (do
                  (try
                    ((partial aclient/delete-queue session) result_queue)
                    (catch java.lang.Throwable t__8574__auto__ (error/report t__8574__auto__)))
                  (throw ^java.lang.Throwable t__8575__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__8575__auto__
            (do
              (try
                (common/sync-shutdown session)
                (catch java.lang.Throwable t__8574__auto__ (error/report t__8574__auto__)))
              (throw ^java.lang.Throwable t__8575__auto__)
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
  (defn ->TransactorHornetConnector
    ([cluster_conf transactor_endpoint hornet_factory]
      (datomic.connector.TransactorHornetConnector.
        cluster_conf
        transactor_endpoint
        hornet_factory)))
  (reset-meta!
    #'->TransactorHornetConnector
    (assoc
      {:arglists (clojure.core/list ['cluster-conf 'transactor-endpoint 'hornet-factory]),
       :column (int 1)}
      :name
      '->TransactorHornetConnector
      :ns
      *ns*))
  (defn create-transactor-hornet-connector
    ([cluster_conf endpoint ttl]
      (let [hornet_factory (create-hornet-factory endpoint ttl)]
        (datomic.connector.TransactorHornetConnector. cluster_conf endpoint hornet_factory)))
    ([cluster_conf endpoint]
      (create-transactor-hornet-connector
        cluster_conf
        endpoint
        (config/property "datomic.peerConnectionTTLMsec"))))
  (reset-meta!
    #'create-transactor-hornet-connector
    (assoc
      {:arglists (clojure.core/list ['cluster-conf 'endpoint] ['cluster-conf 'endpoint 'ttl]),
       :column (int 1)}
      :name
      'create-transactor-hornet-connector
      :ns
      *ns*)))