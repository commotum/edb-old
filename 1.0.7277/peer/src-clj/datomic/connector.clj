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
  (defonce Startable {})
  (defprotocol Startable (start [_]))
  (defonce NotificationHandler {})
  (defprotocol
    NotificationHandler
    (notify-sync [_ id])
    (notify-data [_ msg])
    (notify-error [_ id error])
    (notify-db [_ db])
    (notify-index [_]))
  (defonce TransactorConnector {})
  (defprotocol
    TransactorConnector
    (endpoint [_])
    (admin-request* [_ request arg timeout-msec])
    (create-notifier [_ handler failure-handler])
    (start-updater [_ update-queue push-handler failure-handler]))
  (defn admin-request
    ([connector request arg timeout_msec]
      (let [result (admin-request* connector request arg timeout_msec)]
        (if (:db/error result)
          (do (throw (ex-info (:message result) (dissoc result :message))) nil)
          (:value result))))
    ([connector request arg] (admin-request connector request arg 60000)))
  (defn endpoint-error
    ([p__21140 cause]
      (let [map__21141 p__21140
            map__21141 (if (seq? map__21141)
                         (clojure.lang.PersistentHashMap/create (seq map__21141))
                         map__21141)
            endpoint map__21141
            host (get map__21141 :host)
            alt_host (get map__21141 :alt-host)
            port (get map__21141 :port)]
        (ex-info
          (str
            "Error communicating with HOST "
            host
            (if alt_host (str " or ALT_HOST " alt_host) "")
            " on PORT "
            port)
          (logger/redact endpoint #{:password})
          cause))))
  (def sfb-cache (cache/create-soft-limited 10))
  (defn stop-all-connectors
    ([]
      (loop [seq_21143 (seq (cache/cache-keys sfb-cache)) chunk_21144 nil count_21145 0 i_21146 0]
        (if (< i_21146 count_21145)
          (let [k (.nth ^clojure.lang.Indexed chunk_21144 (int i_21146))]
            (let [temp__5457__auto__ (cache/remove sfb-cache k)]
              (when temp__5457__auto__
                (let [sfb temp__5457__auto__] (deref (common/async-shutdown sfb)))))
            (recur seq_21143 chunk_21144 count_21145 (inc i_21146)))
          (let [temp__5457__auto__ (seq seq_21143)]
            (when temp__5457__auto__
              (let [seq_21143 temp__5457__auto__]
                (if (chunked-seq? seq_21143)
                  (let [c__5719__auto__ (chunk-first seq_21143)]
                    (recur
                      (chunk-rest seq_21143)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [k (first seq_21143)]
                    (let [temp__5457__auto__ (cache/remove sfb-cache k)]
                      (when temp__5457__auto__
                        (let [sfb temp__5457__auto__] (deref (common/async-shutdown sfb)))))
                    (recur (next seq_21143) nil 0 0))))))))))
  (defn try-hornet-connect
    ([conn_factory conn_args session_args]
      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
        (when (.isDebugEnabled ^org.slf4j.Logger logger)
          (.debug
            ^org.slf4j.Logger logger
            (logger/process {:event :peer/hornet-connect, :host (:host conn_args)}))
          nil)
        nil)
      (let [cache_key [conn_factory conn_args session_args]]
        (try
          (let [temp__5455__auto__ (get sfb-cache cache_key)]
            (if temp__5455__auto__
              (let [bundle temp__5455__auto__]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                    (.debug
                      ^org.slf4j.Logger logger
                      (logger/process
                        {:event :peer/hornet-reuse-factory, :host (:host conn_args)}))
                    nil)
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
                  (fn fn__21152
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
                    (logger/process {:event :peer/hornet-connect-failed, :host (:host conn_args)}))
                  nil)
                nil)
              e))))))
  (reset-meta!
    #'try-hornet-connect
    (assoc
      {:private true,
       :arglists (clojure.core/list ['conn-factory 'conn-args 'session-args]),
       :column 1}
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
      {:private true, :arglists (clojure.core/list ['host 'alt-host]), :column 1}
      :name
      'host-order
      :ns
      *ns*))
  (defn create-hornet-factory
    ([p__21157 ttl]
      (let [map__21158 p__21157
            map__21158 (if (seq? map__21158)
                         (clojure.lang.PersistentHashMap/create (seq map__21158))
                         map__21158)
            endpoint map__21158
            host (get map__21158 :host)
            port (get map__21158 :port)
            alt_host (get map__21158 :alt-host)
            encrypt_channel (get map__21158 :encrypt-channel)
            conn_args {:port port,
                       :sslEnabled encrypt_channel,
                       :keyStorePath "datomic/transactor-key.jks",
                       :keyStorePassword "transactor"}
            session_args {:ttl ttl}
            G__21162 (host-order host alt_host)
            vec__21163 G__21162
            seq__21164 (seq vec__21163)
            first__21165 (first seq__21164)
            seq__21164 (next seq__21164)
            host first__21165
            more seq__21164]
        (loop [G__21162 G__21162]
          (let [vec__21166 G__21162
                seq__21167 (seq vec__21166)
                first__21168 (first seq__21167)
                seq__21167 (next seq__21167)
                host first__21168
                more seq__21167
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
       :column 1}
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
        (fn fn__21171
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__9147__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")
                        ex t__9147__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (datomic.monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__9147__auto__)
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
  (defmulti notify (fn fn__21178 ([m conn] (tx/peer-message-type m))))
  (defmethod notify :tx fn__21183 ([msg conn] (notify-data conn msg)))
  (defmethod notify :error fn__21185 ([msg conn] (notify-error conn (:id msg) msg)))
  (defmethod notify :index fn__21187 ([msg conn] (notify-index conn)))
  (defmethod notify :sync fn__21189 ([msg conn] (notify-sync conn (:id msg))))
  (defn create-hornet-notifier
    ([push_handler session result_queue hornet_consumer failure_handler]
      (let [push_handler_ref (java.lang.ref.WeakReference. push_handler)
            cleanup_ref (promise)
            done_ref (promise)
            starter (delay
                      (future-call
                        (fn fn__21192
                          ([]
                            (try
                              (let [m_21193 {:event :connector/notify-loop}
                                    ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.connector")]
                                                      (when (.isDebugEnabled
                                                              ^org.slf4j.Logger logger)
                                                        (.debug
                                                          ^org.slf4j.Logger logger
                                                          (logger/process
                                                            (assoc m_21193 :phase :begin)))
                                                        nil)
                                                      nil)
                                    start__8981__auto__ (java.lang.System/nanoTime)
                                    result__8982__auto__ (try
                                                           {:returned
                                                            (try
                                                              (loop 
                                                                [msges
                                                                 (queue/take hornet_consumer)]
                                                                (when
                                                                  (and
                                                                    msges
                                                                    (not (realized? done_ref)))
                                                                  (let 
                                                                    [temp__5457__auto__
                                                                     (.get
                                                                       ^java.lang.ref.Reference push_handler_ref)]
                                                                    (when
                                                                      temp__5457__auto__
                                                                      (let 
                                                                        [push_handler
                                                                         temp__5457__auto__]
                                                                        (loop 
                                                                          [seq_21198
                                                                           (seq
                                                                             (aclient/read-batch
                                                                               msges
                                                                               tx/read-handlers))
                                                                           chunk_21199 nil
                                                                           count_21200 0
                                                                           i_21201 0]
                                                                          (if
                                                                            (< i_21201 count_21200)
                                                                            (let 
                                                                              [msg
                                                                               (.nth
                                                                                 ^clojure.lang.Indexed chunk_21199
                                                                                 (int i_21201))]
                                                                              (notify
                                                                                msg
                                                                                push_handler)
                                                                              (recur
                                                                                seq_21198
                                                                                chunk_21199
                                                                                count_21200
                                                                                (inc i_21201)))
                                                                            (let 
                                                                              [temp__5457__auto__
                                                                               (seq seq_21198)]
                                                                              (when
                                                                                temp__5457__auto__
                                                                                (let 
                                                                                  [seq_21198
                                                                                   temp__5457__auto__]
                                                                                  (if
                                                                                    (chunked-seq?
                                                                                      seq_21198)
                                                                                    (let 
                                                                                      [c__5719__auto__
                                                                                       (chunk-first
                                                                                         seq_21198)]
                                                                                      (recur
                                                                                        (chunk-rest
                                                                                          seq_21198)
                                                                                        c__5719__auto__
                                                                                        (int
                                                                                          (count
                                                                                            c__5719__auto__))
                                                                                        (int 0)))
                                                                                    (let 
                                                                                      [msg
                                                                                       (first
                                                                                         seq_21198)]
                                                                                      (notify
                                                                                        msg
                                                                                        push_handler)
                                                                                      (recur
                                                                                        (next
                                                                                          seq_21198)
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
                                                                  nil))
                                                              (finally ((deref cleanup_ref))))}
                                                           (catch
                                                             java.lang.Throwable
                                                             t__8983__auto__
                                                             {:threw t__8983__auto__}))
                                    elapsed_21194 (-
                                                    (java.lang.System/nanoTime)
                                                    start__8981__auto__)
                                    msec_21195 (logger/format-as-msec (long elapsed_21194))]
                                (let [endmsg__8984__auto__ (merge
                                                             (assoc
                                                               m_21193
                                                               :msec
                                                               msec_21195
                                                               :phase
                                                               :end)
                                                             (when
                                                               (:threw result__8982__auto__)
                                                               {:threw
                                                                (class
                                                                  (:threw result__8982__auto__))}))
                                      logger (org.slf4j.LoggerFactory/getLogger
                                               "datomic.connector")]
                                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                    (.debug
                                      ^org.slf4j.Logger logger
                                      (logger/process endmsg__8984__auto__))
                                    nil)
                                  nil)
                                (if (contains? result__8982__auto__ :returned)
                                  (:returned result__8982__auto__)
                                  (do (throw (:threw result__8982__auto__)) nil)))
                              (catch
                                java.lang.Throwable
                                t__9147__auto__
                                (do
                                  (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.connector")
                                        ex t__9147__auto__]
                                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                      (.warn
                                        ^org.slf4j.Logger logger
                                        (logger/process "error executing future")
                                        ^java.lang.Throwable ex)
                                      (logger/caused-by logger ex))
                                    nil)
                                  (datomic.monitor/alarm :UnhandledException)
                                  (throw ^java.lang.Throwable t__9147__auto__)
                                  nil)))))))
            cleanup (error/runonce
                      (fn fn__21214
                        ([]
                          (let [m_21215 {:event :connector/notify-cleanup}
                                ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                               "datomic.connector")]
                                                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                    (.debug
                                                      ^org.slf4j.Logger logger
                                                      (logger/process
                                                        (assoc m_21215 :phase :begin)))
                                                    nil)
                                                  nil)
                                start__8981__auto__ (java.lang.System/nanoTime)
                                result__8982__auto__ (try
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
                                                         t__8983__auto__
                                                         {:threw t__8983__auto__}))
                                elapsed_21216 (- (java.lang.System/nanoTime) start__8981__auto__)
                                msec_21217 (logger/format-as-msec (long elapsed_21216))]
                            (let [endmsg__8984__auto__ (merge
                                                         (assoc
                                                           m_21215
                                                           :msec
                                                           msec_21217
                                                           :phase
                                                           :end)
                                                         (when (:threw result__8982__auto__)
                                                           {:threw
                                                            (class
                                                              (:threw result__8982__auto__))}))
                                  logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process endmsg__8984__auto__))
                                nil)
                              nil)
                            (if (contains? result__8982__auto__ :returned)
                              (:returned result__8982__auto__)
                              (do (throw (:threw result__8982__auto__)) nil))))))]
        (deliver cleanup_ref cleanup)
        (datomic.connector.HornetNotifier.
          push_handler_ref
          session
          result_queue
          hornet_consumer
          starter
          cleanup))))
  (deftype
    TransactorHornetConnector
    [cluster_conf transactor_endpoint hornet_factory]
    datomic.connector.TransactorConnector
    datomic.common.AsyncShutdown
    (async-shutdown [this] nil)
    (endpoint [this] transactor_endpoint)
    (start-updater
      [this unsent_updates_queue push_handler failure_handler]
      (let [map__21248 cluster_conf
            map__21248 (if (seq? map__21248)
                         (clojure.lang.PersistentHashMap/create (seq map__21248))
                         map__21248)
            db_id (get map__21248 :db-id)
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
                  (fn fn__21249
                    ([]
                      (try
                        (let [m_21250 {:event :connector/updater-loop}
                              ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.connector")]
                                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                  (.debug
                                                    ^org.slf4j.Logger logger
                                                    (logger/process (assoc m_21250 :phase :begin)))
                                                  nil)
                                                nil)
                              start__8981__auto__ (java.lang.System/nanoTime)
                              result__8982__auto__ (try
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
                                                            (if
                                                              (instance? java.lang.Throwable msg)
                                                              (do
                                                                (notify-error
                                                                  push_handler
                                                                  (:id tx)
                                                                  msg)
                                                                (recur))
                                                              (let 
                                                                [send_result
                                                                 (try
                                                                   (do
                                                                     (.send
                                                                       ^org.apache.activemq.artemis.api.core.client.ClientProducer hornet_producer
                                                                       ^org.apache.activemq.artemis.api.core.Message msg)
                                                                     nil)
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     e
                                                                     e))]
                                                                (if
                                                                  (instance?
                                                                    java.lang.Throwable
                                                                    send_result)
                                                                  (^clojure.lang.IFn failure_handler
                                                                    send_result)
                                                                  (recur))))))
                                                        (catch
                                                          java.lang.InterruptedException
                                                          _
                                                          nil))}
                                                     (catch
                                                       java.lang.Throwable
                                                       t__8983__auto__
                                                       {:threw t__8983__auto__}))
                              elapsed_21251 (- (java.lang.System/nanoTime) start__8981__auto__)
                              msec_21252 (logger/format-as-msec (long elapsed_21251))]
                          (let [endmsg__8984__auto__ (merge
                                                       (assoc m_21250 :msec msec_21252 :phase :end)
                                                       (when (:threw result__8982__auto__)
                                                         {:threw
                                                          (class (:threw result__8982__auto__))}))
                                logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                              (.debug
                                ^org.slf4j.Logger logger
                                (logger/process endmsg__8984__auto__))
                              nil)
                            nil)
                          (if (contains? result__8982__auto__ :returned)
                            (:returned result__8982__auto__)
                            (do (throw (:threw result__8982__auto__)) nil)))
                        (catch
                          java.lang.Throwable
                          t__9147__auto__
                          (do
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")
                                  ex t__9147__auto__]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process "error executing future")
                                  ^java.lang.Throwable ex)
                                (logger/caused-by logger ex))
                              nil)
                            (datomic.monitor/alarm :UnhandledException)
                            (throw ^java.lang.Throwable t__9147__auto__)
                            nil))))))
            cleanup (delay
                      (let [m_21267 {:event :connector/updater-cleanup}
                            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                           "datomic.connector")]
                                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                (.debug
                                                  ^org.slf4j.Logger logger
                                                  (logger/process (assoc m_21267 :phase :begin)))
                                                nil)
                                              nil)
                            start__8981__auto__ (java.lang.System/nanoTime)
                            result__8982__auto__ (try
                                                   (do
                                                     (future-cancel fut)
                                                     (deref
                                                       (common/async-shutdown hornet_producer))
                                                     {:returned
                                                      (deref (common/async-shutdown session))})
                                                   (catch
                                                     java.lang.Throwable
                                                     t__8983__auto__
                                                     {:threw t__8983__auto__}))
                            elapsed_21268 (- (java.lang.System/nanoTime) start__8981__auto__)
                            msec_21269 (logger/format-as-msec (long elapsed_21268))]
                        (let [endmsg__8984__auto__ (merge
                                                     (assoc m_21267 :msec msec_21269 :phase :end)
                                                     (when (:threw result__8982__auto__)
                                                       {:threw
                                                        (class (:threw result__8982__auto__))}))
                              logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                            nil)
                          nil)
                        (if (contains? result__8982__auto__ :returned)
                          (:returned result__8982__auto__)
                          (do (throw (:threw result__8982__auto__)) nil))))]
        (reify
          datomic.common.AsyncShutdown
          (async-shutdown [this] (promise/delivered (deref cleanup))))))
    (create-notifier
      [this push_handler failure_handler]
      (let [map__21229 cluster_conf
            map__21229 (if (seq? map__21229)
                         (clojure.lang.PersistentHashMap/create (seq map__21229))
                         map__21229)
            db_id (get map__21229 :db-id)
            session (aclient/start-session
                      hornet_factory
                      transactor_endpoint
                      :on-failure
                      failure_handler
                      :pre-acknowledge
                      true)
            unused (error/runonce
                     (fn fn__21230
                       ([]
                         (try
                           (common/sync-shutdown session)
                           (catch
                             java.lang.Throwable
                             t__708__auto__
                             (error/report t__708__auto__)))
                         nil)))]
        (try
          (let [result_queue (aclient/create-temporary-queue session (tx/push-address db_id))
                unused (error/runonce
                         (fn fn__21234
                           ([]
                             (try
                               ((partial aclient/delete-queue session) result_queue)
                               (catch
                                 java.lang.Throwable
                                 t__708__auto__
                                 (error/report t__708__auto__)))
                             (^clojure.lang.IFn unused))))]
            (try
              (let [hornet_consumer (aclient/create-consumer session result_queue)
                    unused (error/runonce
                             (fn fn__21238
                               ([]
                                 (try
                                   (common/sync-shutdown hornet_consumer)
                                   (catch
                                     java.lang.Throwable
                                     t__708__auto__
                                     (error/report t__708__auto__)))
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
                    t__709__auto__
                    (do
                      (try
                        (common/sync-shutdown hornet_consumer)
                        (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
                      (throw ^java.lang.Throwable t__709__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__709__auto__
                (do
                  (try
                    ((partial aclient/delete-queue session) result_queue)
                    (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
                  (throw ^java.lang.Throwable t__709__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__709__auto__
            (do
              (try
                (common/sync-shutdown session)
                (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
              (throw ^java.lang.Throwable t__709__auto__)
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
                              :return (:value result)}))
                         nil)
                       nil)
                     result)))))
  (clojure.core/import 'datomic.connector.TransactorHornetConnector)
  (defn ->TransactorHornetConnector
    ([cluster_conf transactor_endpoint hornet_factory]
      (datomic.connector.TransactorHornetConnector.
        cluster_conf
        transactor_endpoint
        hornet_factory)))
  (defn create-transactor-hornet-connector
    ([cluster_conf endpoint ttl]
      (let [hornet_factory (create-hornet-factory endpoint ttl)]
        (datomic.connector.TransactorHornetConnector. cluster_conf endpoint hornet_factory)))
    ([cluster_conf endpoint]
      (create-transactor-hornet-connector
        cluster_conf
        endpoint
        (config/property "datomic.peerConnectionTTLMsec")))))