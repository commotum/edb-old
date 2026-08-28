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
        (when (:db/error result) (throw (ex-info (:message result) (dissoc result :message))))
        (:value result)))
    ([connector request arg] (admin-request connector request arg 60000)))
  (defn endpoint-error
    ([p__18684 cause]
      (let [map__18685 p__18684
            map__18685 (if (seq? map__18685)
                         (if (next map__18685)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18685))
                           (if (seq map__18685) (first map__18685) {}))
                         map__18685)
            endpoint map__18685
            host (get map__18685 :host)
            alt_host (get map__18685 :alt-host)
            port (get map__18685 :port)]
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
      (loop [seq_18687 (seq (cache/cache-keys sfb-cache)) chunk_18688 nil count_18689 0 i_18690 0]
        (if (< i_18690 count_18689)
          (let [k (.nth ^clojure.lang.Indexed chunk_18688 (int i_18690))]
            (let [temp__5804__auto__ (cache/remove sfb-cache k)]
              (when temp__5804__auto__
                (let [sfb temp__5804__auto__] (deref (common/async-shutdown sfb)))))
            (recur seq_18687 chunk_18688 count_18689 (inc i_18690)))
          (let [temp__5804__auto__ (seq seq_18687)]
            (when temp__5804__auto__
              (let [seq_18687 temp__5804__auto__]
                (if (chunked-seq? seq_18687)
                  (let [c__6065__auto__ (chunk-first seq_18687)]
                    (recur
                      (chunk-rest seq_18687)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [k (first seq_18687)]
                    (let [temp__5804__auto__ (cache/remove sfb-cache k)]
                      (when temp__5804__auto__
                        (let [sfb temp__5804__auto__] (deref (common/async-shutdown sfb)))))
                    (recur (next seq_18687) nil 0 0))))))))))
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
                  (fn fn__18696
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
    ([p__18701 ttl]
      (let [map__18702 p__18701
            map__18702 (if (seq? map__18702)
                         (if (next map__18702)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18702))
                           (if (seq map__18702) (first map__18702) {}))
                         map__18702)
            endpoint map__18702
            host (get map__18702 :host)
            port (get map__18702 :port)
            alt_host (get map__18702 :alt-host)
            encrypt_channel (get map__18702 :encrypt-channel)
            conn_args {:port port,
                       :sslEnabled encrypt_channel,
                       :keyStorePath "datomic/transactor-key.jks",
                       :keyStorePassword "transactor"}
            session_args {:ttl ttl}
            G__18706 (host-order host alt_host)
            vec__18707 G__18706
            seq__18708 (seq vec__18707)
            first__18709 (first seq__18708)
            seq__18708 (next seq__18708)
            host first__18709
            more seq__18708]
        (loop [G__18706 G__18706]
          (let [vec__18710 G__18706
                seq__18711 (seq vec__18710)
                first__18712 (first seq__18711)
                seq__18711 (next seq__18711)
                host first__18712
                more seq__18711
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
        (fn fn__18715
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8829__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")
                        ex t__8829__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (datomic.monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__8829__auto__)
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
  (defmulti notify (fn fn__18722 ([m conn] (tx/peer-message-type m))))
  (defmethod notify :tx fn__18727 ([msg conn] (notify-data conn msg)))
  (defmethod notify :error fn__18729 ([msg conn] (notify-error conn (:id msg) msg)))
  (defmethod notify :index fn__18731 ([msg conn] (notify-index conn)))
  (defmethod notify :sync fn__18733 ([msg conn] (notify-sync conn (:id msg))))
  (defn create-hornet-notifier
    ([push_handler session result_queue hornet_consumer failure_handler]
      (let [push_handler_ref (java.lang.ref.WeakReference. push_handler)
            cleanup_ref (promise)
            done_ref (promise)
            starter (delay
                      (future-call
                        (fn fn__18736
                          ([]
                            (try
                              (let [m_18737 {:event :connector/notify-loop}
                                    ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.connector")]
                                                      (when (.isDebugEnabled
                                                              ^org.slf4j.Logger logger)
                                                        (.debug
                                                          ^org.slf4j.Logger logger
                                                          (logger/process
                                                            (assoc m_18737 :phase :begin))))
                                                      nil)
                                    start__8584__auto__ (java.lang.System/nanoTime)
                                    result__8585__auto__ (try
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
                                                                            [seq_18742
                                                                             (seq
                                                                               (aclient/read-batch
                                                                                 msges
                                                                                 tx/read-handlers))
                                                                             chunk_18743 nil
                                                                             count_18744 0
                                                                             i_18745 0]
                                                                            (if
                                                                              (<
                                                                                i_18745
                                                                                count_18744)
                                                                              (let 
                                                                                [msg
                                                                                 (.nth
                                                                                   ^clojure.lang.Indexed chunk_18743
                                                                                   (int i_18745))]
                                                                                (notify
                                                                                  msg
                                                                                  push_handler)
                                                                                (recur
                                                                                  seq_18742
                                                                                  chunk_18743
                                                                                  count_18744
                                                                                  (inc i_18745)))
                                                                              (let 
                                                                                [temp__5804__auto__
                                                                                 (seq seq_18742)]
                                                                                (when
                                                                                  temp__5804__auto__
                                                                                  (let 
                                                                                    [seq_18742
                                                                                     temp__5804__auto__]
                                                                                    (if
                                                                                      (chunked-seq?
                                                                                        seq_18742)
                                                                                      (let 
                                                                                        [c__6065__auto__
                                                                                         (chunk-first
                                                                                           seq_18742)]
                                                                                        (recur
                                                                                          (chunk-rest
                                                                                            seq_18742)
                                                                                          c__6065__auto__
                                                                                          (int
                                                                                            (count
                                                                                              c__6065__auto__))
                                                                                          (int 0)))
                                                                                      (let 
                                                                                        [msg
                                                                                         (first
                                                                                           seq_18742)]
                                                                                        (notify
                                                                                          msg
                                                                                          push_handler)
                                                                                        (recur
                                                                                          (next
                                                                                            seq_18742)
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
                                                             t__8586__auto__
                                                             {:threw t__8586__auto__}))
                                    elapsed_18738 (-
                                                    (java.lang.System/nanoTime)
                                                    start__8584__auto__)
                                    msec_18739 (logger/format-as-msec (long elapsed_18738))]
                                (let [endmsg__8587__auto__ (merge
                                                             (assoc
                                                               m_18737
                                                               :msec
                                                               msec_18739
                                                               :phase
                                                               :end)
                                                             (when
                                                               (:threw result__8585__auto__)
                                                               {:threw
                                                                (class
                                                                  (:threw result__8585__auto__))}))
                                      logger (org.slf4j.LoggerFactory/getLogger
                                               "datomic.connector")]
                                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                    (.debug
                                      ^org.slf4j.Logger logger
                                      (logger/process endmsg__8587__auto__)))
                                  nil)
                                (if (contains? result__8585__auto__ :returned)
                                  (:returned result__8585__auto__)
                                  (do (throw (:threw result__8585__auto__)) nil)))
                              (catch
                                java.lang.Throwable
                                t__8829__auto__
                                (do
                                  (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.connector")
                                        ex t__8829__auto__]
                                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                      (.warn
                                        ^org.slf4j.Logger logger
                                        (logger/process "error executing future")
                                        ^java.lang.Throwable ex)
                                      (logger/caused-by logger ex))
                                    nil)
                                  (datomic.monitor/alarm :UnhandledException)
                                  (throw ^java.lang.Throwable t__8829__auto__)
                                  nil)))))))
            cleanup (error/runonce
                      (fn fn__18758
                        ([]
                          (let [m_18759 {:event :connector/notify-cleanup}
                                ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                               "datomic.connector")]
                                                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                    (.debug
                                                      ^org.slf4j.Logger logger
                                                      (logger/process
                                                        (assoc m_18759 :phase :begin))))
                                                  nil)
                                start__8584__auto__ (java.lang.System/nanoTime)
                                result__8585__auto__ (try
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
                                                         t__8586__auto__
                                                         {:threw t__8586__auto__}))
                                elapsed_18760 (- (java.lang.System/nanoTime) start__8584__auto__)
                                msec_18761 (logger/format-as-msec (long elapsed_18760))]
                            (let [endmsg__8587__auto__ (merge
                                                         (assoc
                                                           m_18759
                                                           :msec
                                                           msec_18761
                                                           :phase
                                                           :end)
                                                         (when (:threw result__8585__auto__)
                                                           {:threw
                                                            (class
                                                              (:threw result__8585__auto__))}))
                                  logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process endmsg__8587__auto__)))
                              nil)
                            (if (contains? result__8585__auto__ :returned)
                              (:returned result__8585__auto__)
                              (do (throw (:threw result__8585__auto__)) nil))))))]
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
      (let [map__18792 cluster_conf
            map__18792 (if (seq? map__18792)
                         (if (next map__18792)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18792))
                           (if (seq map__18792) (first map__18792) {}))
                         map__18792)
            db_id (get map__18792 :db-id)
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
                  (fn fn__18793
                    ([]
                      (try
                        (let [m_18794 {:event :connector/updater-loop}
                              ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.connector")]
                                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                  (.debug
                                                    ^org.slf4j.Logger logger
                                                    (logger/process
                                                      (assoc m_18794 :phase :begin))))
                                                nil)
                              start__8584__auto__ (java.lang.System/nanoTime)
                              result__8585__auto__ (try
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
                                                       t__8586__auto__
                                                       {:threw t__8586__auto__}))
                              elapsed_18795 (- (java.lang.System/nanoTime) start__8584__auto__)
                              msec_18796 (logger/format-as-msec (long elapsed_18795))]
                          (let [endmsg__8587__auto__ (merge
                                                       (assoc m_18794 :msec msec_18796 :phase :end)
                                                       (when (:threw result__8585__auto__)
                                                         {:threw
                                                          (class (:threw result__8585__auto__))}))
                                logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                              (.debug
                                ^org.slf4j.Logger logger
                                (logger/process endmsg__8587__auto__)))
                            nil)
                          (if (contains? result__8585__auto__ :returned)
                            (:returned result__8585__auto__)
                            (do (throw (:threw result__8585__auto__)) nil)))
                        (catch
                          java.lang.Throwable
                          t__8829__auto__
                          (do
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")
                                  ex t__8829__auto__]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process "error executing future")
                                  ^java.lang.Throwable ex)
                                (logger/caused-by logger ex))
                              nil)
                            (datomic.monitor/alarm :UnhandledException)
                            (throw ^java.lang.Throwable t__8829__auto__)
                            nil))))))
            cleanup (delay
                      (let [m_18811 {:event :connector/updater-cleanup}
                            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                           "datomic.connector")]
                                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                (.debug
                                                  ^org.slf4j.Logger logger
                                                  (logger/process (assoc m_18811 :phase :begin))))
                                              nil)
                            start__8584__auto__ (java.lang.System/nanoTime)
                            result__8585__auto__ (try
                                                   (do
                                                     (future-cancel fut)
                                                     (deref
                                                       (common/async-shutdown hornet_producer))
                                                     {:returned
                                                      (deref (common/async-shutdown session))})
                                                   (catch
                                                     java.lang.Throwable
                                                     t__8586__auto__
                                                     {:threw t__8586__auto__}))
                            elapsed_18812 (- (java.lang.System/nanoTime) start__8584__auto__)
                            msec_18813 (logger/format-as-msec (long elapsed_18812))]
                        (let [endmsg__8587__auto__ (merge
                                                     (assoc m_18811 :msec msec_18813 :phase :end)
                                                     (when (:threw result__8585__auto__)
                                                       {:threw
                                                        (class (:threw result__8585__auto__))}))
                              logger (org.slf4j.LoggerFactory/getLogger "datomic.connector")]
                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                            (.debug
                              ^org.slf4j.Logger logger
                              (logger/process endmsg__8587__auto__)))
                          nil)
                        (if (contains? result__8585__auto__ :returned)
                          (:returned result__8585__auto__)
                          (do (throw (:threw result__8585__auto__)) nil))))]
        (reify
          datomic.common.AsyncShutdown
          (async-shutdown [this] (promise/delivered (deref cleanup))))))
    (create-notifier
      [this push_handler failure_handler]
      (let [map__18773 cluster_conf
            map__18773 (if (seq? map__18773)
                         (if (next map__18773)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18773))
                           (if (seq map__18773) (first map__18773) {}))
                         map__18773)
            db_id (get map__18773 :db-id)
            session (aclient/start-session
                      hornet_factory
                      transactor_endpoint
                      :on-failure
                      failure_handler
                      :pre-acknowledge
                      true)
            unused (error/runonce
                     (fn fn__18774
                       ([]
                         (try
                           (common/sync-shutdown session)
                           (catch
                             java.lang.Throwable
                             t__8540__auto__
                             (error/report t__8540__auto__)))
                         nil)))]
        (try
          (let [result_queue (aclient/create-temporary-queue session (tx/push-address db_id))
                unused (error/runonce
                         (fn fn__18778
                           ([]
                             (try
                               ((partial aclient/delete-queue session) result_queue)
                               (catch
                                 java.lang.Throwable
                                 t__8540__auto__
                                 (error/report t__8540__auto__)))
                             (^clojure.lang.IFn unused))))]
            (try
              (let [hornet_consumer (aclient/create-consumer session result_queue)
                    unused (error/runonce
                             (fn fn__18782
                               ([]
                                 (try
                                   (common/sync-shutdown hornet_consumer)
                                   (catch
                                     java.lang.Throwable
                                     t__8540__auto__
                                     (error/report t__8540__auto__)))
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
                    t__8541__auto__
                    (do
                      (try
                        (common/sync-shutdown hornet_consumer)
                        (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
                      (throw ^java.lang.Throwable t__8541__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__8541__auto__
                (do
                  (try
                    ((partial aclient/delete-queue session) result_queue)
                    (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
                  (throw ^java.lang.Throwable t__8541__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__8541__auto__
            (do
              (try
                (common/sync-shutdown session)
                (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
              (throw ^java.lang.Throwable t__8541__auto__)
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
  (defn create-transactor-hornet-connector
    ([cluster_conf endpoint ttl]
      (let [hornet_factory (create-hornet-factory endpoint ttl)]
        (datomic.connector.TransactorHornetConnector. cluster_conf endpoint hornet_factory)))
    ([cluster_conf endpoint]
      (create-transactor-hornet-connector
        cluster_conf
        endpoint
        (config/property "datomic.peerConnectionTTLMsec")))))