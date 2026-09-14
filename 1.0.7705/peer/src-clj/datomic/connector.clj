(do
  (clojure.core/in-ns 'datomic.connector)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.connector)
    {:doc
     "Peer-side transactor transport over ActiveMQ Artemis. Connectors consume coordination-published endpoints, choose primary or alternate hosts for the deployment environment, cache session factories, own producer and temporary notification-queue lifecycles, dispatch transaction, error, index, and sync messages, and perform bounded administrative RPCs. Failure handlers return endpoint loss to the connection recovery loop."})
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
      (assoc
        (assoc protocol_metadata__7463 :doc "Idempotent activation of a lazily prepared transport task.")
        :name
        'Startable
        :ns
        *ns*))
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
      (notify-sync [_ id] "Reports completion of the synchronization request identified by id.")
      (notify-data
        [_ msg]
        "Reports a completed transaction. msg carries :id, :data, :tempids, and :io-stats.")
      (notify-error [_ id error] "Reports that transaction id failed with error.")
      (notify-db [_ db] "Installs a database value while catching up during connection or recovery.")
      (notify-index [_] "Reports that a newer durable index is available."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.connector" "NotificationHandler")
      (assoc
        (assoc
          protocol_metadata__7466
          :doc
          "Callbacks that apply transactor results and database-state notifications to a live connection.")
        :name
        'NotificationHandler
        :ns
        *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'notify-sync
                                        {:arglists (clojure.core/list ['_ 'id])}),
                                      :arglists (clojure.core/list ['_ 'id]),
                                      :doc
                                      "Reports completion of the synchronization request identified by id."}
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
                                      "Reports a completed transaction. msg carries :id, :data, :tempids, and :io-stats."}
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
                                      :doc "Reports that transaction id failed with error."}
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
                                      "Installs a database value while catching up during connection or recovery."}
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
                                      :doc "Reports that a newer durable index is available."}
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
      (endpoint [_] "Returns the transactor endpoint, or nil when unavailable.")
      (admin-request*
        [_ request arg timeout-msec]
        "Sends a bounded request to the transactor and returns a response envelope. Error envelopes contain :db/error and :message.")
      (create-notifier
        [_ handler failure-handler]
        "Creates an inactive notification consumer implementing Startable and AsyncShutdown.")
      (start-updater
        [_ update-queue push-handler failure-handler]
        "Starts a transaction updater over update-queue and returns an AsyncShutdown handle. Transport failures reach failure-handler; serialization failures reach push-handler as transaction errors."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.connector" "TransactorConnector")
      (assoc
        (assoc
          protocol_metadata__7477
          :doc
          "Transport operations connecting a peer's transaction, notification, synchronization, and administrative paths to one transactor endpoint.")
        :name
        'TransactorConnector
        :ns
        *ns*))
    (let [protocol_signature__7478 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'endpoint {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Returns the transactor endpoint, or nil when unavailable."}
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
                                      "Sends a bounded request to the transactor and returns a response envelope. Error envelopes contain :db/error and :message."}
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
                                      "Creates an inactive notification consumer implementing Startable and AsyncShutdown."}
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
                                      "Starts a transaction updater over update-queue and returns an AsyncShutdown handle. Transport failures reach failure-handler; serialization failures reach push-handler as transaction errors."}
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
    ([connector request arg timeout-ms]
      (let [result (admin-request* connector request arg timeout-ms)]
        (when (:db/error result) (throw (ex-info (:message result) (dissoc result :message))))
        (:value result)))
    ([connector request arg] (admin-request connector request arg 60000)))
  (reset-meta!
    #'admin-request
    (assoc
      {:arglists
       (clojure.core/list ['connector 'request 'arg] ['connector 'request 'arg 'timeout-msec]),
       :doc
       "Sends an administrative request and returns the response :value. The default timeout is 60 seconds. Error envelopes are raised as ExceptionInfo with the response data and without the display message.",
       :column (int 1)}
      :name
      'admin-request
      :ns
      *ns*))
  (defn endpoint-error
    ([{:keys [host alt-host port], :as endpoint} cause]
      (ex-info
        (str
          "Error communicating with HOST "
          host
          (if alt-host (str " or ALT_HOST " alt-host) "")
          " on PORT "
          port)
        (logger/redact endpoint #{:password})
        cause)))
  (reset-meta!
    #'endpoint-error
    (assoc
      {:arglists (clojure.core/list [{:keys ['host 'alt-host 'port], :as 'endpoint} 'cause]),
       :doc
       "Returns ExceptionInfo describing a failed endpoint while redacting its password from attached data and preserving cause.",
       :column (int 1)}
      :name
      'endpoint-error
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.connector" "sfb-cache")
    {:doc "Soft, size-limited cache of reusable Artemis session-factory bundles.", :column (int 1)})
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
      :doc
      "Removes every cached Artemis session-factory bundle and waits for each bundle to shut down."
      :name
      'stop-all-connectors
      :ns
      *ns*))
  (defn try-hornet-connect
    ([connector-factory connection-args session-args]
      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
        (when (.isDebugEnabled ^org.slf4j.Logger logger)
          (.debug
            ^org.slf4j.Logger logger
            (logger/process {:event :peer/hornet-connect, :host (:host connection-args)})))
        nil)
      (let [cache-key [connector-factory connection-args session-args]]
        (try
          (let [temp__5802__auto__ (get sfb-cache cache-key)]
            (if temp__5802__auto__
              (let [bundle temp__5802__auto__]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                    (.debug
                      ^org.slf4j.Logger logger
                      (logger/process
                        {:event :peer/hornet-reuse-factory, :host (:host connection-args)})))
                  nil)
                bundle)
              (let [connector (apply
                                aclient/create-connector
                                connector-factory
                                :verifyHost
                                false
                                :trustStorePath
                                "datomic/transactor-trust.jks"
                                :trustStorePassword
                                "transactor"
                                (mapcat identity connection-args))
                    bundle (aclient/create-session-factory connector session-args)]
                (cleanup/register-cleanup
                  (deref cleanup/shared-manager-ref)
                  bundle
                  (fn fn__20019
                    ([] ((.-cleanup ^datomic.artemis_client.SessionFactoryBundle bundle)))))
                (cache/put sfb-cache cache-key bundle)
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
                      {:event :peer/hornet-connect-failed, :host (:host connection-args)})))
                nil)
              e))))))
  (reset-meta!
    #'try-hornet-connect
    (assoc
      {:private true,
       :arglists
       (clojure.core/list ['connector-factory 'connection-args 'session-args]),
       :doc
       "Returns a cached or newly created Artemis session-factory bundle for connection-args. Connection failures are returned as Throwable values so endpoint fallback can continue.",
       :column (int 1)}
      :name
      'try-hornet-connect
      :ns
      *ns*))
  (defn host-order
    ([host alt-host]
      (cond
        (nil? host) [alt-host]
        (nil? alt-host) [host]
        :default (do
                   (remove
                     nil?
                     (if (aws-detect/running-in-ec2?) [host alt-host] [alt-host host]))))))
  (reset-meta!
    #'host-order
    (assoc
      {:private true,
       :arglists (clojure.core/list ['host 'alt-host]),
       :doc
       "Returns endpoint hosts in connection-attempt order. EC2 environments prefer host when both inputs are present; other environments prefer alt-host. With either input nil, returns a one-element vector containing the other input.",
       :column (int 1)}
      :name
      'host-order
      :ns
      *ns*))
  (defn create-hornet-factory
    ([{:keys [host port alt-host encrypt-channel], :as endpoint} ttl]
      (let [connection-args {:port port,
                             :sslEnabled encrypt-channel,
                             :keyStorePath "datomic/transactor-key.jks",
                             :keyStorePassword "transactor"}
            session-args {:ttl ttl}]
        (loop [[host & more] (host-order host alt-host)]
          (let [result (try-hornet-connect
                         aclient/netty-connector-factory
                         (assoc connection-args :host host)
                         session-args)]
            (if (instance? java.lang.Throwable result)
              (if more (recur more) (do (throw (endpoint-error endpoint result)) nil))
              result))))))
  (reset-meta!
    #'create-hornet-factory
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [{:keys ['host 'port 'alt-host 'encrypt-channel], :as 'endpoint} 'ttl]),
       :doc
       "Creates or reuses an Artemis session factory for endpoint. Host preference follows the deployment environment, alternate hosts are attempted after connection failures, and the session-factory connection TTL is ttl milliseconds. Throws endpoint-error after every host fails.",
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
  (defn ->HornetNotifier
    ([push-handler-ref session result-queue hornet-consumer starter cleanup]
      (datomic.connector.HornetNotifier.
        push-handler-ref
        session
        result-queue
        hornet-consumer
        starter
        cleanup)))
  (reset-meta!
    #'->HornetNotifier
    (assoc
      {:arglists
       (clojure.core/list
         ['push-handler-ref 'session 'result-queue 'hornet-consumer 'starter 'cleanup]),
       :doc
       "Constructs a notification-loop handle from its weak handler reference, Artemis session and consumer, lazy starter, and idempotent cleanup function.",
       :column (int 1)}
      :name
      '->HornetNotifier
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.connector" "notify")
    {:doc
     "Dispatches a decoded transactor push message to the corresponding NotificationHandler callback.",
     :column (int 1)})
  (let [v__5792__auto__ #'notify]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta
        (clojure.lang.RT/var "datomic.connector" "notify")
        {:doc
         "Dispatches a decoded transactor push message to the corresponding NotificationHandler callback.",
         :column (int 1)})
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
  ;; ATOMIC-NOTE [observed/why]: Construction does not consume notifications.
  ;; Peer initialization controls the delayed start after its load/register/load
  ;; sequence. The loop decodes batches then dispatches tx/error/index/sync to
  ;; peer callbacks; idempotent cleanup closes transport, not database values.
  ;; Native notices trigger authenticated durable observation, not acceptance
  ;; of broker messages as transaction authority; Artemis wire parity is out.
  (defn create-hornet-notifier
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
                                                                                          (int 0)))
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
                                                                  (:threw result__8554__auto__))}))
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
          cleanup))))
  (reset-meta!
    #'create-hornet-notifier
    (assoc
      {:arglists
       (clojure.core/list
         ['push-handler 'session 'result-queue 'hornet-consumer 'failure-handler]),
       :doc
       "Creates an inactive notification consumer. Starting it reads Fressian message batches from the temporary result queue and dispatches each message to push-handler. Transport failures invoke failure-handler. Shutdown stops the loop, closes the consumer, deletes the queue, and closes the session exactly once.",
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
      [this request arg timeout-ms]
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
                               timeout-ms
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
    ([cluster-conf transactor-endpoint hornet-factory]
      (datomic.connector.TransactorHornetConnector.
        cluster-conf
        transactor-endpoint
        hornet-factory)))
  (reset-meta!
    #'->TransactorHornetConnector
    (assoc
      {:arglists (clojure.core/list ['cluster-conf 'transactor-endpoint 'hornet-factory]),
       :doc
       "Constructs an Artemis-backed connector for cluster-conf and transactor-endpoint using hornet-factory.",
       :column (int 1)}
      :name
      '->TransactorHornetConnector
      :ns
      *ns*))
  (defn create-transactor-hornet-connector
    ([cluster-conf endpoint ttl]
      (let [hornet-factory (create-hornet-factory endpoint ttl)]
        (datomic.connector.TransactorHornetConnector. cluster-conf endpoint hornet-factory)))
    ([cluster-conf endpoint]
      (create-transactor-hornet-connector
        cluster-conf
        endpoint
        (config/property "datomic.peerConnectionTTLMsec"))))
  (reset-meta!
    #'create-transactor-hornet-connector
    (assoc
      {:arglists (clojure.core/list ['cluster-conf 'endpoint] ['cluster-conf 'endpoint 'ttl]),
       :doc
       "Creates a connector for the coordination-published transactor endpoint. The two-argument form uses the configured peer connection TTL; the three-argument form uses ttl milliseconds.",
       :column (int 1)}
      :name
      'create-transactor-hornet-connector
      :ns
      *ns*)))
