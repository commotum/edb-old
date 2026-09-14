(do
  (clojure.core/in-ns 'datomic.artemis-client)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.artemis-client)
    {:doc
     "Peer-side Artemis transport for transactor requests, transaction results, and database novelty notifications. Sessions authenticate from the published endpoint and surface connection failure to the reconnection lifecycle."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.string :as 'string]
        ['datomic.cache :as 'cache]
        ['datomic.common :as 'common]
        ['datomic.error :as 'error]
        ['datomic.fressian :as 'fressian]
        ['datomic.promise :as 'promise]
        ['datomic.queue :as 'queue]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'org.apache.activemq.artemis.api.core.TransportConfiguration)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ActiveMQClient)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientConsumer)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientMessage)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientProducer)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientSession)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientSessionFactory)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.MessageHandler)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ServerLocator)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.SessionFailureListener)))
  (when-not (.equals 'datomic.artemis-client 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.artemis-client))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.string :as 'string]
          ['datomic.cache :as 'cache]
          ['datomic.common :as 'common]
          ['datomic.error :as 'error]
          ['datomic.fressian :as 'fressian]
          ['datomic.promise :as 'promise]
          ['datomic.queue :as 'queue]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'org.apache.activemq.artemis.api.core.TransportConfiguration)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ActiveMQClient)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientConsumer)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientMessage)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientProducer)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientSession)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientSessionFactory)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.client.MessageHandler)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ServerLocator)
        (clojure.core/import
          'org.apache.activemq.artemis.api.core.client.SessionFailureListener))))
  (set! *warn-on-reflection* true)
  (def netty-connector-factory
   "org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory")
  (reset-meta!
    #'netty-connector-factory
    (assoc {:column (int 1)} :name 'netty-connector-factory :ns *ns*))
  (def in-vm-connector-factory
   "org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory")
  (reset-meta!
    #'in-vm-connector-factory
    (assoc {:column (int 1)} :name 'in-vm-connector-factory :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-client" "create-transport")
    {:tag org.apache.activemq.artemis.api.core.TransportConfiguration,
     :arglists (clojure.core/list ['factory '& 'transport-opts]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-client" "create-transport")
    (fn create_transport
      ([factory & transport_opts]
        (let [opts_map (into
                         {}
                         (map
                           (fn fn__17347
                             ([p__17346]
                               (let [vec__17348 p__17346
                                     k (nth vec__17348 (int 0) nil)
                                     v (nth vec__17348 (int 1) nil)]
                                 [(name k)
                                  (if (integer? v) (java.lang.Integer/valueOf (int v)) v)])))
                           (partition 2 transport_opts)))]
          (org.apache.activemq.artemis.api.core.TransportConfiguration.
            ^java.lang.String factory
            ^java.util.Map opts_map)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-client" "create-connector")
    {:tag org.apache.activemq.artemis.api.core.TransportConfiguration,
     :arglists (clojure.core/list ['connector-class-name '& 'kvs]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-client" "create-connector")
    (fn create_connector
      ([connector_class_name & kvs] (apply create-transport connector_class_name kvs))))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol HornetImpl (start-session* [_ creds args]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.artemis-client" "HornetImpl")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'HornetImpl :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'start-session*
                                        {:arglists (clojure.core/list ['_ 'creds 'args])}),
                                      :arglists (clojure.core/list ['_ 'creds 'args]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.artemis-client" "HornetImpl"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.artemis-client" "start-session*")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (defn start-session ([factory creds & args] (start-session* factory creds args)))
  (reset-meta!
    #'start-session
    (assoc
      {:arglists (clojure.core/list ['factory 'creds '& 'args]), :column (int 1)}
      :name
      'start-session
      :ns
      *ns*))
  (defn wrap-as-failure-listener
    ([f]
      (reify
        org.apache.activemq.artemis.api.core.client.SessionFailureListener
        (^void connectionFailed
          [this
           ^org.apache.activemq.artemis.api.core.ActiveMQException ex
           ^boolean failed_over
           ^java.lang.String scale_down_target_node_id]
          (do (^clojure.lang.IFn f ex) nil))
        (^void connectionFailed
          [this ^org.apache.activemq.artemis.api.core.ActiveMQException ex ^boolean failed_over]
          (do (^clojure.lang.IFn f ex) nil))
        (^void beforeReconnect
          [this ^org.apache.activemq.artemis.api.core.ActiveMQException ex]
          nil))))
  (reset-meta!
    #'wrap-as-failure-listener
    (assoc
      {:private true, :arglists (clojure.core/list ['f]), :column (int 1)}
      :name
      'wrap-as-failure-listener
      :ns
      *ns*))
  (deftype
    SessionFactoryBundle
    [locator factory cleanup]
    datomic.artemis_client.HornetImpl
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (future-call
        (fn fn__17382
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8798__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client")
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
    (start-session*
      [this creds p__17378]
      (let [map__17380 p__17378
            map__17380 (if (seq? map__17380)
                         (if (next map__17380)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17380))
                           (if (seq map__17380) (first map__17380) {}))
                         map__17380)
            xa (get map__17380 :xa false)
            auto_commit_sends (get map__17380 :auto-commit-sends true)
            auto_commit_acks (get map__17380 :auto-commit-acks true)
            pre_acknowledge (get map__17380 :pre-acknowledge false)
            ack_batch_size (get map__17380 :ack-batch-size 1)
            on_failure (get map__17380 :on-failure)
            map__17381 (common/require-keys creds [:username :password])
            map__17381 (if (seq? map__17381)
                         (if (next map__17381)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17381))
                           (if (seq map__17381) (first map__17381) {}))
                         map__17381)
            username (get map__17381 :username)
            password (get map__17381 :password)
            session (.createSession
                      ^org.apache.activemq.artemis.api.core.client.ClientSessionFactory factory
                      ^java.lang.String username
                      ^java.lang.String password
                      (boolean (.booleanValue ^java.lang.Boolean xa))
                      (boolean (.booleanValue ^java.lang.Boolean auto_commit_sends))
                      (boolean (.booleanValue ^java.lang.Boolean auto_commit_acks))
                      (boolean (.booleanValue ^java.lang.Boolean pre_acknowledge))
                      (int ^java.lang.Number ack_batch_size))]
        (when on_failure
          (.addFailureListener
            ^org.apache.activemq.artemis.api.core.client.ClientSession session
            (wrap-as-failure-listener on_failure)))
        (.start ^org.apache.activemq.artemis.api.core.client.ClientSession session)
        session)))
  (clojure.core/import 'datomic.artemis_client.SessionFactoryBundle)
  (defn ->SessionFactoryBundle
    ([locator factory cleanup]
      (datomic.artemis_client.SessionFactoryBundle. locator factory cleanup)))
  (reset-meta!
    #'->SessionFactoryBundle
    (assoc
      {:arglists (clojure.core/list ['locator 'factory 'cleanup]), :column (int 1)}
      :name
      '->SessionFactoryBundle
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-client" "create-server-locator")
    {:tag org.apache.activemq.artemis.api.core.client.ServerLocator,
     :private true,
     :arglists (clojure.core/list ['connector {:keys ['ttl]}]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-client" "create-server-locator")
    (fn create_server_locator
      ([connector p__17388]
        (let [map__17389 p__17388
              map__17389 (if (seq? map__17389)
                           (if (next map__17389)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__17389))
                             (if (seq map__17389) (first map__17389) {}))
                           map__17389)
              ttl (get map__17389 :ttl)]
          (when-not ttl (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'ttl)))))
          (doto
            (ActiveMQClient/createServerLocatorWithoutHA
              (into-array org.apache.activemq.artemis.api.core.TransportConfiguration [connector]))
            (.setConnectionTTL (long ^java.lang.Number ttl))
            (.setProducerWindowSize (int (java.lang.Integer/valueOf (int (* 256 1024)))))
            (.setClientFailureCheckPeriod (long (if (= -1 ttl) ttl (/ ttl 2)))))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-client" "create-session-factory")
    {:tag datomic.artemis_client.SessionFactoryBundle,
     :arglists (clojure.core/list [(.withMeta 'connector {:tag 'TransportConfiguration}) 'opts]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-client" "create-session-factory")
    (fn create_session_factory
      ([connector opts]
        (let [loc (create-server-locator connector opts)
              cleanup (error/runonce
                        (fn fn__17392
                          ([]
                            (try
                              (do
                                (.close
                                  ^org.apache.activemq.artemis.api.core.client.ServerLocator loc)
                                nil)
                              (catch
                                java.lang.Throwable
                                t__8509__auto__
                                (error/report t__8509__auto__)))
                            nil)))]
          (try
            (let [session_factory (.createSessionFactory
                                    ^org.apache.activemq.artemis.api.core.client.ServerLocator loc)
                  cleanup (error/runonce
                            (fn fn__17396
                              ([]
                                (try
                                  (do
                                    (.close
                                      ^org.apache.activemq.artemis.api.core.client.ClientSessionFactory session_factory)
                                    nil)
                                  (catch
                                    java.lang.Throwable
                                    t__8509__auto__
                                    (error/report t__8509__auto__)))
                                (^clojure.lang.IFn cleanup))))]
              (try
                (datomic.artemis_client.SessionFactoryBundle. loc session_factory cleanup)
                (catch
                  java.lang.Throwable
                  t__8510__auto__
                  (do
                    (try
                      (do
                        (.close
                          ^org.apache.activemq.artemis.api.core.client.ClientSessionFactory session_factory)
                        nil)
                      (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
                    (throw ^java.lang.Throwable t__8510__auto__)
                    nil))))
            (catch
              java.lang.Throwable
              t__8510__auto__
              (do
                (try
                  (do (.close ^org.apache.activemq.artemis.api.core.client.ServerLocator loc) nil)
                  (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
                (throw ^java.lang.Throwable t__8510__auto__)
                nil)))))))
  (extend
    org.apache.activemq.artemis.api.core.client.ClientProducer
    common/AsyncShutdown
    {:async-shutdown (fn fn__17405 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ClientSessionFactory
    common/AsyncShutdown
    {:async-shutdown (fn fn__17407 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ClientConsumer
    common/AsyncShutdown
    {:async-shutdown (fn fn__17409 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ClientSession
    common/AsyncShutdown
    {:async-shutdown (fn fn__17411 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ServerLocator
    common/AsyncShutdown
    {:async-shutdown (fn fn__17413 ([this] (.close this) (promise/delivered true)))})
  (defn create-temporary-queue
    ([session address]
      (let [queue_name (str address (common/rand-uuid))]
        (.createTemporaryQueue
          ^org.apache.activemq.artemis.api.core.client.ClientSession session
          ^java.lang.String address
          ^java.lang.String queue_name)
        queue_name)))
  (reset-meta!
    #'create-temporary-queue
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'ClientSession}) (.withMeta 'address {:tag 'String})]),
       :column (int 1)}
      :name
      'create-temporary-queue
      :ns
      *ns*))
  (defn delete-queue
    ([session queue]
      (try
        (when-not (.isClosed ^org.apache.activemq.artemis.api.core.client.ClientSession session)
          (.deleteQueue
            ^org.apache.activemq.artemis.api.core.client.ClientSession session
            ^java.lang.String queue)
          nil)
        (catch java.lang.Throwable t (error/report t)))))
  (reset-meta!
    #'delete-queue
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'ClientSession}) (.withMeta 'queue {:tag 'String})]),
       :column (int 1)}
      :name
      'delete-queue
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-client" "create-producer")
    {:tag org.apache.activemq.artemis.api.core.client.ClientProducer,
     :arglists
     (clojure.core/list
       [(.withMeta 'session {:tag 'ClientSession}) (.withMeta 'address {:tag 'String})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-client" "create-producer")
    (fn create_producer
      ([session address]
        (.createProducer
          ^org.apache.activemq.artemis.api.core.client.ClientSession session
          ^java.lang.String address))))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-client" "create-consumer")
    {:tag org.apache.activemq.artemis.api.core.client.ClientConsumer,
     :arglists
     (clojure.core/list
       [(.withMeta 'session {:tag 'ClientSession})
        (.withMeta 'queue-name {:tag 'String})
        '&
        {:keys ['window-size 'max-rate 'browse],
         :or
         {'window-size (.withMeta (clojure.core/list '* 1024 1024) {:column (int 66)}),
          'max-rate -1,
          'browse false}}]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-client" "create-consumer")
    (fn create_consumer
      ([session queue_name & p__17418]
        (let [map__17419 p__17418
              map__17419 (if (seq? map__17419)
                           (if (next map__17419)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__17419))
                             (if (seq map__17419) (first map__17419) {}))
                           map__17419)
              window_size (get map__17419 :window-size (long (* 1024 1024)))
              max_rate (get map__17419 :max-rate -1)
              browse (get map__17419 :browse false)]
          (.createConsumer
            ^org.apache.activemq.artemis.api.core.client.ClientSession session
            ^java.lang.String queue_name
            nil
            (int window_size)
            (int max_rate)
            (boolean false))))))
  (extend
    org.apache.activemq.artemis.api.core.client.ClientConsumer
    queue/BlockingConsumer
    {:take
     (fn fn__17421
       ([this]
         (let [temp__5802__auto__ (.receive this)]
           (if temp__5802__auto__
             (let [msg temp__5802__auto__] msg)
             (do (throw (java.lang.InterruptedException.)) nil)))))})
  (defn set-handler
    ([consumer handler]
      (.setMessageHandler
        ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer
        (reify
          org.apache.activemq.artemis.api.core.client.MessageHandler
          (^void onMessage
            [this ^org.apache.activemq.artemis.api.core.client.ClientMessage msg]
            (do (^clojure.lang.IFn handler msg) nil))))))
  (reset-meta!
    #'set-handler
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'consumer {:tag 'ClientConsumer}) 'handler]),
       :column (int 1)}
      :name
      'set-handler
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-client" "create-message")
    {:tag org.apache.activemq.artemis.api.core.client.ClientMessage,
     :arglists
     (clojure.core/list
       [(.withMeta 'session {:tag 'ClientSession}) 'durable]
       [(.withMeta 'session {:tag 'ClientSession}) 'message-type 'durable]
       [(.withMeta 'session {:tag 'ClientSession})
        'message-type
        'durable
        'expiration
        'timestamp
        'priority]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-client" "create-message")
    (fn create_message
      ([session message_type durable expiration timestamp priority]
        (.createMessage
          ^org.apache.activemq.artemis.api.core.client.ClientSession session
          (byte message_type)
          (boolean durable)
          (long expiration)
          (long timestamp)
          (byte priority)))
      ([session message_type durable]
        (.createMessage
          ^org.apache.activemq.artemis.api.core.client.ClientSession session
          (byte message_type)
          (boolean durable)))
      ([session durable]
        (.createMessage
          ^org.apache.activemq.artemis.api.core.client.ClientSession session
          (boolean durable)))))
  (defn output-stream
    ([msg]
      (let [buf (.getBodyBuffer ^org.apache.activemq.artemis.api.core.client.ClientMessage msg)]
        (proxy
          [java.io.OutputStream]
          []
          (write
            ([b]
              (do
                (.writeByte
                  ^org.apache.activemq.artemis.api.core.ActiveMQBuffer buf
                  (unchecked-byte b))
                nil))
            ([bs off len]
              (do
                (.writeBytes
                  ^org.apache.activemq.artemis.api.core.ActiveMQBuffer buf
                  ^bytes bs
                  (int off)
                  (int len))
                nil)))))))
  (reset-meta!
    #'output-stream
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'msg {:tag 'ClientMessage})]), :column (int 1)}
      :name
      'output-stream
      :ns
      *ns*))
  (defn input-stream
    ([msg]
      (let [buf (.getBodyBuffer ^org.apache.activemq.artemis.api.core.client.ClientMessage msg)]
        (com.datomic.impl.peer.ActiveMQInputStream.
          ^org.apache.activemq.artemis.api.core.ActiveMQBuffer buf))))
  (reset-meta!
    #'input-stream
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'msg {:tag 'ClientMessage})]), :column (int 1)}
      :name
      'input-stream
      :ns
      *ns*))
  (defn create-fressian-message
    ([session lookup obj & message_args]
      (let [msg (apply create-message session message_args)
            fout (fressian/create-writer (output-stream msg) lookup)]
        (.writeObject ^org.fressian.Writer fout obj)
        msg)))
  (reset-meta!
    #'create-fressian-message
    (assoc
      {:arglists (clojure.core/list ['session 'lookup 'obj '& 'message-args]), :column (int 1)}
      :name
      'create-fressian-message
      :ns
      *ns*))
  (defn create-serializer
    ([write_handlers]
      (fn fn__17434
        ([obj msg]
          (let [fout (fressian/create-writer
                       (output-stream msg)
                       (merge fressian/clojure-write-handlers write_handlers))]
            (.writeObject ^org.fressian.Writer fout obj))))))
  (reset-meta!
    #'create-serializer
    (assoc
      {:arglists (clojure.core/list ['write-handlers]), :column (int 1)}
      :name
      'create-serializer
      :ns
      *ns*))
  (defn create-deserializer
    ([read_handlers]
      (fn fn__17437
        ([msg]
          (let [fin (fressian/create-reader
                      (input-stream msg)
                      (merge fressian/clojure-read-handlers read_handlers))]
            (.readObject ^org.fressian.Reader fin))))))
  (reset-meta!
    #'create-deserializer
    (assoc
      {:arglists (clojure.core/list ['read-handlers]), :column (int 1)}
      :name
      'create-deserializer
      :ns
      *ns*))
  (defn read-batch
    ([msg read_handlers]
      (fressian/read-batch (fressian/create-reader (input-stream msg) read_handlers))))
  (reset-meta!
    #'read-batch
    (assoc
      {:arglists (clojure.core/list ['msg 'read-handlers]), :column (int 1)}
      :name
      'read-batch
      :ns
      *ns*))
  (defn fressian-producer
    ([hornet_session hornet_producer write_handlers]
      (reify
        datomic.queue.BlockingProducer
        clojure.lang.IFn
        (put
          [this obj]
          (try
            (do
              (.send
                ^org.apache.activemq.artemis.api.core.client.ClientProducer hornet_producer
                (this obj))
              true)
            (catch
              java.lang.Throwable
              t
              (do
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client") ex t]
                  (when (.isWarnEnabled ^org.slf4j.Logger logger)
                    (.warn
                      ^org.slf4j.Logger logger
                      (logger/process "Hornet send failed")
                      ^java.lang.Throwable ex)
                    (logger/caused-by logger ex))
                  nil)
                false))))
        (invoke [this obj] (create-fressian-message hornet_session write_handlers obj false)))))
  (reset-meta!
    #'fressian-producer
    (assoc
      {:arglists
       (clojure.core/list
         ['hornet-session (.withMeta 'hornet-producer {:tag 'ClientProducer}) 'write-handlers]),
       :column (int 1)}
      :name
      'fressian-producer
      :ns
      *ns*))
  (deftype
    RpcClient
    [session producer consumer serializer response_map producer_queue consumer_queue cleanup]
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (future-call
        (fn fn__17445
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8798__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client")
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
  (clojure.core/import 'datomic.artemis_client.RpcClient)
  (defn ->RpcClient
    ([session producer consumer serializer response_map producer_queue consumer_queue cleanup]
      (datomic.artemis_client.RpcClient.
        session
        producer
        consumer
        serializer
        response_map
        producer_queue
        consumer_queue
        cleanup)))
  (reset-meta!
    #'->RpcClient
    (assoc
      {:arglists
       (clojure.core/list
         ['session
          'producer
          'consumer
          'serializer
          'response-map
          'producer-queue
          'consumer-queue
          'cleanup]),
       :column (int 1)}
      :name
      '->RpcClient
      :ns
      *ns*))
  (defn create-rpc-client
    ([session request_address response_address & p__17451]
      (let [vec__17452 p__17451
            map__17455 (nth vec__17452 (int 0) nil)
            map__17455 (if (seq? map__17455)
                         (if (next map__17455)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17455))
                           (if (seq map__17455) (first map__17455) {}))
                         map__17455)
            read_handlers (get map__17455 :read-handlers)
            write_handlers (get map__17455 :write-handlers)
            serializer (create-serializer write_handlers)
            deserializer (create-deserializer read_handlers)
            response_map (cache/create-response-map 2)
            cq (partial create-temporary-queue session)
            dq (partial delete-queue session)
            request_queue (^clojure.lang.IFn cq request_address)
            cleanup (error/runonce
                      (fn fn__17456
                        ([]
                          (try
                            (^clojure.lang.IFn dq request_queue)
                            (catch
                              java.lang.Throwable
                              t__8509__auto__
                              (error/report t__8509__auto__)))
                          nil)))]
        (try
          (let [response_queue (^clojure.lang.IFn cq response_address)
                cleanup (error/runonce
                          (fn fn__17460
                            ([]
                              (try
                                (^clojure.lang.IFn dq response_queue)
                                (catch
                                  java.lang.Throwable
                                  t__8509__auto__
                                  (error/report t__8509__auto__)))
                              (^clojure.lang.IFn cleanup))))]
            (try
              (let [producer (create-producer session request_address)
                    cleanup (error/runonce
                              (fn fn__17464
                                ([]
                                  (try
                                    (do
                                      (.close
                                        ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                                      nil)
                                    (catch
                                      java.lang.Throwable
                                      t__8509__auto__
                                      (error/report t__8509__auto__)))
                                  (^clojure.lang.IFn cleanup))))]
                (try
                  (let [consumer (create-consumer session response_queue)
                        cleanup (error/runonce
                                  (fn fn__17468
                                    ([]
                                      (try
                                        (do
                                          (.close
                                            ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                                          nil)
                                        (catch
                                          java.lang.Throwable
                                          t__8509__auto__
                                          (error/report t__8509__auto__)))
                                      (^clojure.lang.IFn cleanup))))]
                    (try
                      (do
                        (set-handler
                          consumer
                          (fn fn__17472
                            ([msg]
                              (let [map__17473 (^clojure.lang.IFn deserializer msg)
                                    map__17473 (if (seq? map__17473)
                                                 (if (next map__17473)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__17473))
                                                   (if (seq map__17473) (first map__17473) {}))
                                                 map__17473)
                                    result map__17473
                                    id (get map__17473 :id)
                                    temp__5804__auto__ (cache/remove response_map id)]
                                (when temp__5804__auto__
                                  (let [p temp__5804__auto__] (deliver p result)))))))
                        (datomic.artemis_client.RpcClient.
                          session
                          producer
                          consumer
                          serializer
                          response_map
                          request_queue
                          response_queue
                          cleanup))
                      (catch
                        java.lang.Throwable
                        t__8510__auto__
                        (do
                          (try
                            (do
                              (.close
                                ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                              nil)
                            (catch
                              java.lang.Throwable
                              t__8509__auto__
                              (error/report t__8509__auto__)))
                          (throw ^java.lang.Throwable t__8510__auto__)
                          nil))))
                  (catch
                    java.lang.Throwable
                    t__8510__auto__
                    (do
                      (try
                        (do
                          (.close
                            ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                          nil)
                        (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
                      (throw ^java.lang.Throwable t__8510__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__8510__auto__
                (do
                  (try
                    (^clojure.lang.IFn dq response_queue)
                    (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
                  (throw ^java.lang.Throwable t__8510__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__8510__auto__
            (do
              (try
                (^clojure.lang.IFn dq request_queue)
                (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
              (throw ^java.lang.Throwable t__8510__auto__)
              nil))))))
  (reset-meta!
    #'create-rpc-client
    (assoc
      {:arglists
       (clojure.core/list
         ['session
          'request-address
          'response-address
          '&
          [{:keys ['read-handlers 'write-handlers]}]]),
       :column (int 1)}
      :name
      'create-rpc-client
      :ns
      *ns*))
  (deftype
    RpcServer
    [consumer producer cleanup]
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (future-call
        (fn fn__17486
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8798__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client")
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
  (clojure.core/import 'datomic.artemis_client.RpcServer)
  (defn ->RpcServer
    ([consumer producer cleanup] (datomic.artemis_client.RpcServer. consumer producer cleanup)))
  (reset-meta!
    #'->RpcServer
    (assoc
      {:arglists (clojure.core/list ['consumer 'producer 'cleanup]), :column (int 1)}
      :name
      '->RpcServer
      :ns
      *ns*))
  (defn create-rpc-server
    ([session_fn request_address response_address handler & p__17492]
      (let [vec__17493 p__17492
            map__17496 (nth vec__17493 (int 0) nil)
            map__17496 (if (seq? map__17496)
                         (if (next map__17496)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17496))
                           (if (seq map__17496) (first map__17496) {}))
                         map__17496)
            read_handlers (get map__17496 :read-handlers)
            write_handlers (get map__17496 :write-handlers)
            session (^clojure.lang.IFn session_fn)
            serializer (create-serializer write_handlers)
            deserializer (create-deserializer read_handlers)
            cq (partial create-temporary-queue session)
            dq (partial delete-queue session)
            session session
            cleanup (error/runonce
                      (fn fn__17497
                        ([]
                          (try
                            (common/async-shutdown session)
                            (catch
                              java.lang.Throwable
                              t__8509__auto__
                              (error/report t__8509__auto__)))
                          nil)))]
        (try
          (let [request_queue (^clojure.lang.IFn cq request_address)
                cleanup (error/runonce
                          (fn fn__17501
                            ([]
                              (try
                                (^clojure.lang.IFn dq request_queue)
                                (catch
                                  java.lang.Throwable
                                  t__8509__auto__
                                  (error/report t__8509__auto__)))
                              (^clojure.lang.IFn cleanup))))]
            (try
              (let [response_queue (^clojure.lang.IFn cq response_address)
                    cleanup (error/runonce
                              (fn fn__17505
                                ([]
                                  (try
                                    (^clojure.lang.IFn dq response_queue)
                                    (catch
                                      java.lang.Throwable
                                      t__8509__auto__
                                      (error/report t__8509__auto__)))
                                  (^clojure.lang.IFn cleanup))))]
                (try
                  (let [consumer (create-consumer session request_queue)
                        cleanup (error/runonce
                                  (fn fn__17509
                                    ([]
                                      (try
                                        (do
                                          (.close
                                            ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                                          nil)
                                        (catch
                                          java.lang.Throwable
                                          t__8509__auto__
                                          (error/report t__8509__auto__)))
                                      (^clojure.lang.IFn cleanup))))]
                    (try
                      (let [producer (create-producer session response_address)
                            cleanup (error/runonce
                                      (fn fn__17513
                                        ([]
                                          (try
                                            (do
                                              (.close
                                                ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                                              nil)
                                            (catch
                                              java.lang.Throwable
                                              t__8509__auto__
                                              (error/report t__8509__auto__)))
                                          (^clojure.lang.IFn cleanup))))]
                        (try
                          (do
                            (set-handler
                              consumer
                              (fn fn__17517
                                ([msg]
                                  (let [map__17518 (^clojure.lang.IFn deserializer msg)
                                        map__17518 (if (seq? map__17518)
                                                     (if (next map__17518)
                                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                         (to-array map__17518))
                                                       (if (seq map__17518) (first map__17518) {}))
                                                     map__17518)
                                        id (get map__17518 :id)
                                        code (get map__17518 :code)
                                        response (create-message session false)
                                        result (try
                                                 (let [handler_result (^clojure.lang.IFn handler
                                                                        code)
                                                       temp__5802__auto__ (:failed handler_result)]
                                                   (if temp__5802__auto__
                                                     (let [failed temp__5802__auto__]
                                                       {:id id, :failed failed})
                                                     {:id id, :value handler_result}))
                                                 (catch
                                                   java.lang.Throwable
                                                   e
                                                   (do
                                                     (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.artemis-client")
                                                           ex e]
                                                       (when (.isWarnEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.warn
                                                           ^org.slf4j.Logger logger
                                                           (logger/process "command failed")
                                                           ^java.lang.Throwable ex)
                                                         (logger/caused-by logger ex))
                                                       nil)
                                                     {:id id,
                                                      :failed
                                                      (.getMessage ^java.lang.Throwable e)})))]
                                    (^clojure.lang.IFn serializer result response)
                                    (.send
                                      ^org.apache.activemq.artemis.api.core.client.ClientProducer producer
                                      ^org.apache.activemq.artemis.api.core.Message response)
                                    nil))))
                            (datomic.artemis_client.RpcServer. consumer producer cleanup))
                          (catch
                            java.lang.Throwable
                            t__8510__auto__
                            (do
                              (try
                                (do
                                  (.close
                                    ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                                  nil)
                                (catch
                                  java.lang.Throwable
                                  t__8509__auto__
                                  (error/report t__8509__auto__)))
                              (throw ^java.lang.Throwable t__8510__auto__)
                              nil))))
                      (catch
                        java.lang.Throwable
                        t__8510__auto__
                        (do
                          (try
                            (do
                              (.close
                                ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                              nil)
                            (catch
                              java.lang.Throwable
                              t__8509__auto__
                              (error/report t__8509__auto__)))
                          (throw ^java.lang.Throwable t__8510__auto__)
                          nil))))
                  (catch
                    java.lang.Throwable
                    t__8510__auto__
                    (do
                      (try
                        (^clojure.lang.IFn dq response_queue)
                        (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
                      (throw ^java.lang.Throwable t__8510__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__8510__auto__
                (do
                  (try
                    (^clojure.lang.IFn dq request_queue)
                    (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
                  (throw ^java.lang.Throwable t__8510__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__8510__auto__
            (do
              (try
                (common/async-shutdown session)
                (catch java.lang.Throwable t__8509__auto__ (error/report t__8509__auto__)))
              (throw ^java.lang.Throwable t__8510__auto__)
              nil))))))
  (reset-meta!
    #'create-rpc-server
    (assoc
      {:arglists
       (clojure.core/list
         ['session-fn
          'request-address
          'response-address
          'handler
          '&
          [{:keys ['read-handlers 'write-handlers]}]]),
       :column (int 1)}
      :name
      'create-rpc-server
      :ns
      *ns*))
  (defn rpc-request
    ([conn request]
      (let [id (common/rand-uuid) p (promise)]
        (cache/put (.-response-map ^datomic.artemis_client.RpcClient conn) id p)
        (let [msg (create-message (.-session ^datomic.artemis_client.RpcClient conn) false)]
          ((.-serializer ^datomic.artemis_client.RpcClient conn) {:id id, :code request} msg)
          (.send
            (.-producer ^datomic.artemis_client.RpcClient conn)
            ^org.apache.activemq.artemis.api.core.Message msg))
        p)))
  (reset-meta!
    #'rpc-request
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'conn {:tag 'RpcClient}) 'request]),
       :column (int 1)}
      :name
      'rpc-request
      :ns
      *ns*)))
