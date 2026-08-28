(do
  (clojure.core/in-ns 'datomic.artemis-client)
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
  (def in-vm-connector-factory
   "org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory")
  (defn create-transport
    ([factory & transport_opts]
      (let [opts_map (into
                       {}
                       (map
                         (fn fn__18317
                           ([p__18316]
                             (let [vec__18318 p__18316
                                   k (nth vec__18318 (int 0) nil)
                                   v (nth vec__18318 (int 1) nil)]
                               [(name k)
                                (if (integer? v) (java.lang.Integer/valueOf (int v)) v)])))
                         (partition 2 transport_opts)))]
        (org.apache.activemq.artemis.api.core.TransportConfiguration.
          ^java.lang.String factory
          ^java.util.Map opts_map))))
  (reset-meta!
    #'create-transport
    (assoc
      {:tag org.apache.activemq.artemis.api.core.TransportConfiguration,
       :arglists (clojure.core/list ['factory '& 'transport-opts]),
       :column 1}
      :name
      'create-transport
      :ns
      *ns*))
  (defn create-connector
    ([connector_class_name & kvs] (apply create-transport connector_class_name kvs)))
  (reset-meta!
    #'create-connector
    (assoc
      {:tag org.apache.activemq.artemis.api.core.TransportConfiguration,
       :arglists (clojure.core/list ['connector-class-name '& 'kvs]),
       :column 1}
      :name
      'create-connector
      :ns
      *ns*))
  (defonce HornetImpl {})
  (defprotocol HornetImpl (start-session* [_ creds args]))
  (defn start-session ([factory creds & args] (start-session* factory creds args)))
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
      {:private true, :arglists (clojure.core/list ['f]), :column 1}
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
        (fn fn__18352
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8829__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client")
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
    (start-session*
      [this creds p__18348]
      (let [map__18350 p__18348
            map__18350 (if (seq? map__18350)
                         (if (next map__18350)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18350))
                           (if (seq map__18350) (first map__18350) {}))
                         map__18350)
            xa (get map__18350 :xa false)
            auto_commit_sends (get map__18350 :auto-commit-sends true)
            auto_commit_acks (get map__18350 :auto-commit-acks true)
            pre_acknowledge (get map__18350 :pre-acknowledge false)
            ack_batch_size (get map__18350 :ack-batch-size 1)
            on_failure (get map__18350 :on-failure)
            map__18351 (common/require-keys creds [:username :password])
            map__18351 (if (seq? map__18351)
                         (if (next map__18351)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18351))
                           (if (seq map__18351) (first map__18351) {}))
                         map__18351)
            username (get map__18351 :username)
            password (get map__18351 :password)
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
  (defn create-server-locator
    ([connector p__18358]
      (let [map__18359 p__18358
            map__18359 (if (seq? map__18359)
                         (if (next map__18359)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18359))
                           (if (seq map__18359) (first map__18359) {}))
                         map__18359)
            ttl (get map__18359 :ttl)]
        (when-not ttl (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'ttl)))))
        (doto
          (ActiveMQClient/createServerLocatorWithoutHA
            (into-array org.apache.activemq.artemis.api.core.TransportConfiguration [connector]))
          (.setConnectionTTL (long ^java.lang.Number ttl))
          (.setProducerWindowSize (int (java.lang.Integer/valueOf (int (* 256 1024)))))
          (.setClientFailureCheckPeriod (long (if (= -1 ttl) ttl (/ ttl 2))))))))
  (reset-meta!
    #'create-server-locator
    (assoc
      {:tag org.apache.activemq.artemis.api.core.client.ServerLocator,
       :private true,
       :arglists (clojure.core/list ['connector {:keys ['ttl]}]),
       :column 1}
      :name
      'create-server-locator
      :ns
      *ns*))
  (defn create-session-factory
    ([connector opts]
      (let [loc (create-server-locator connector opts)
            cleanup (error/runonce
                      (fn fn__18362
                        ([]
                          (try
                            (do
                              (.close
                                ^org.apache.activemq.artemis.api.core.client.ServerLocator loc)
                              nil)
                            (catch
                              java.lang.Throwable
                              t__8540__auto__
                              (error/report t__8540__auto__)))
                          nil)))]
        (try
          (let [session_factory (.createSessionFactory
                                  ^org.apache.activemq.artemis.api.core.client.ServerLocator loc)
                cleanup (error/runonce
                          (fn fn__18366
                            ([]
                              (try
                                (do
                                  (.close
                                    ^org.apache.activemq.artemis.api.core.client.ClientSessionFactory session_factory)
                                  nil)
                                (catch
                                  java.lang.Throwable
                                  t__8540__auto__
                                  (error/report t__8540__auto__)))
                              (^clojure.lang.IFn cleanup))))]
            (try
              (datomic.artemis_client.SessionFactoryBundle. loc session_factory cleanup)
              (catch
                java.lang.Throwable
                t__8541__auto__
                (do
                  (try
                    (do
                      (.close
                        ^org.apache.activemq.artemis.api.core.client.ClientSessionFactory session_factory)
                      nil)
                    (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
                  (throw ^java.lang.Throwable t__8541__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__8541__auto__
            (do
              (try
                (do (.close ^org.apache.activemq.artemis.api.core.client.ServerLocator loc) nil)
                (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
              (throw ^java.lang.Throwable t__8541__auto__)
              nil))))))
  (reset-meta!
    #'create-session-factory
    (assoc
      {:tag datomic.artemis_client.SessionFactoryBundle,
       :arglists (clojure.core/list [(.withMeta 'connector {:tag 'TransportConfiguration}) 'opts]),
       :column 1}
      :name
      'create-session-factory
      :ns
      *ns*))
  (extend
    org.apache.activemq.artemis.api.core.client.ClientProducer
    common/AsyncShutdown
    {:async-shutdown (fn fn__18375 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ClientSessionFactory
    common/AsyncShutdown
    {:async-shutdown (fn fn__18377 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ClientConsumer
    common/AsyncShutdown
    {:async-shutdown (fn fn__18379 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ClientSession
    common/AsyncShutdown
    {:async-shutdown (fn fn__18381 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ServerLocator
    common/AsyncShutdown
    {:async-shutdown (fn fn__18383 ([this] (.close this) (promise/delivered true)))})
  (defn create-temporary-queue
    ([session address]
      (let [queue_name (str address (common/rand-uuid))]
        (.createTemporaryQueue
          ^org.apache.activemq.artemis.api.core.client.ClientSession session
          ^java.lang.String address
          ^java.lang.String queue_name)
        queue_name)))
  (defn delete-queue
    ([session queue]
      (try
        (when-not (.isClosed ^org.apache.activemq.artemis.api.core.client.ClientSession session)
          (.deleteQueue
            ^org.apache.activemq.artemis.api.core.client.ClientSession session
            ^java.lang.String queue)
          nil)
        (catch java.lang.Throwable t (error/report t)))))
  (defn create-producer
    ([session address]
      (.createProducer
        ^org.apache.activemq.artemis.api.core.client.ClientSession session
        ^java.lang.String address)))
  (reset-meta!
    #'create-producer
    (assoc
      {:tag org.apache.activemq.artemis.api.core.client.ClientProducer,
       :arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'ClientSession}) (.withMeta 'address {:tag 'String})]),
       :column 1}
      :name
      'create-producer
      :ns
      *ns*))
  (defn create-consumer
    ([session queue_name & p__18388]
      (let [map__18389 p__18388
            map__18389 (if (seq? map__18389)
                         (if (next map__18389)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18389))
                           (if (seq map__18389) (first map__18389) {}))
                         map__18389)
            window_size (get map__18389 :window-size (long (* 1024 1024)))
            max_rate (get map__18389 :max-rate -1)
            browse (get map__18389 :browse false)]
        (.createConsumer
          ^org.apache.activemq.artemis.api.core.client.ClientSession session
          ^java.lang.String queue_name
          nil
          (int window_size)
          (int max_rate)
          (boolean false)))))
  (reset-meta!
    #'create-consumer
    (assoc
      {:tag org.apache.activemq.artemis.api.core.client.ClientConsumer,
       :arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'ClientSession})
          (.withMeta 'queue-name {:tag 'String})
          '&
          {:keys ['window-size 'max-rate 'browse],
           :or {'window-size (clojure.core/list '* 1024 1024), 'max-rate -1, 'browse false}}]),
       :column 1}
      :name
      'create-consumer
      :ns
      *ns*))
  (extend
    org.apache.activemq.artemis.api.core.client.ClientConsumer
    queue/BlockingConsumer
    {:take
     (fn fn__18391
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
  (defn create-message
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
        (boolean durable))))
  (reset-meta!
    #'create-message
    (assoc
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
       :column 1}
      :name
      'create-message
      :ns
      *ns*))
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
  (defn input-stream
    ([msg]
      (let [buf (.getBodyBuffer ^org.apache.activemq.artemis.api.core.client.ClientMessage msg)]
        (com.datomic.impl.peer.ActiveMQInputStream.
          ^org.apache.activemq.artemis.api.core.ActiveMQBuffer buf))))
  (defn create-fressian-message
    ([session lookup obj & message_args]
      (let [msg (apply create-message session message_args)
            fout (fressian/create-writer (output-stream msg) lookup)]
        (.writeObject ^org.fressian.Writer fout obj)
        msg)))
  (defn create-serializer
    ([write_handlers]
      (fn fn__18404
        ([obj msg]
          (let [fout (fressian/create-writer
                       (output-stream msg)
                       (merge fressian/clojure-write-handlers write_handlers))]
            (.writeObject ^org.fressian.Writer fout obj))))))
  (defn create-deserializer
    ([read_handlers]
      (fn fn__18407
        ([msg]
          (let [fin (fressian/create-reader
                      (input-stream msg)
                      (merge fressian/clojure-read-handlers read_handlers))]
            (.readObject ^org.fressian.Reader fin))))))
  (defn read-batch
    ([msg read_handlers]
      (fressian/read-batch (fressian/create-reader (input-stream msg) read_handlers))))
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
  (deftype
    RpcClient
    [session producer consumer serializer response_map producer_queue consumer_queue cleanup]
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (future-call
        (fn fn__18415
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8829__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client")
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
  (defn create-rpc-client
    ([session request_address response_address & p__18421]
      (let [vec__18422 p__18421
            map__18425 (nth vec__18422 (int 0) nil)
            map__18425 (if (seq? map__18425)
                         (if (next map__18425)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18425))
                           (if (seq map__18425) (first map__18425) {}))
                         map__18425)
            read_handlers (get map__18425 :read-handlers)
            write_handlers (get map__18425 :write-handlers)
            serializer (create-serializer write_handlers)
            deserializer (create-deserializer read_handlers)
            response_map (cache/create-response-map 2)
            cq (partial create-temporary-queue session)
            dq (partial delete-queue session)
            request_queue (^clojure.lang.IFn cq request_address)
            cleanup (error/runonce
                      (fn fn__18426
                        ([]
                          (try
                            (^clojure.lang.IFn dq request_queue)
                            (catch
                              java.lang.Throwable
                              t__8540__auto__
                              (error/report t__8540__auto__)))
                          nil)))]
        (try
          (let [response_queue (^clojure.lang.IFn cq response_address)
                cleanup (error/runonce
                          (fn fn__18430
                            ([]
                              (try
                                (^clojure.lang.IFn dq response_queue)
                                (catch
                                  java.lang.Throwable
                                  t__8540__auto__
                                  (error/report t__8540__auto__)))
                              (^clojure.lang.IFn cleanup))))]
            (try
              (let [producer (create-producer session request_address)
                    cleanup (error/runonce
                              (fn fn__18434
                                ([]
                                  (try
                                    (do
                                      (.close
                                        ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                                      nil)
                                    (catch
                                      java.lang.Throwable
                                      t__8540__auto__
                                      (error/report t__8540__auto__)))
                                  (^clojure.lang.IFn cleanup))))]
                (try
                  (let [consumer (create-consumer session response_queue)
                        cleanup (error/runonce
                                  (fn fn__18438
                                    ([]
                                      (try
                                        (do
                                          (.close
                                            ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                                          nil)
                                        (catch
                                          java.lang.Throwable
                                          t__8540__auto__
                                          (error/report t__8540__auto__)))
                                      (^clojure.lang.IFn cleanup))))]
                    (try
                      (do
                        (set-handler
                          consumer
                          (fn fn__18442
                            ([msg]
                              (let [map__18443 (^clojure.lang.IFn deserializer msg)
                                    map__18443 (if (seq? map__18443)
                                                 (if (next map__18443)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__18443))
                                                   (if (seq map__18443) (first map__18443) {}))
                                                 map__18443)
                                    result map__18443
                                    id (get map__18443 :id)
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
                        t__8541__auto__
                        (do
                          (try
                            (do
                              (.close
                                ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                              nil)
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
                        (do
                          (.close
                            ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                          nil)
                        (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
                      (throw ^java.lang.Throwable t__8541__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__8541__auto__
                (do
                  (try
                    (^clojure.lang.IFn dq response_queue)
                    (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
                  (throw ^java.lang.Throwable t__8541__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__8541__auto__
            (do
              (try
                (^clojure.lang.IFn dq request_queue)
                (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
              (throw ^java.lang.Throwable t__8541__auto__)
              nil))))))
  (deftype
    RpcServer
    [consumer producer cleanup]
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (future-call
        (fn fn__18456
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__8829__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client")
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
  (clojure.core/import 'datomic.artemis_client.RpcServer)
  (defn ->RpcServer
    ([consumer producer cleanup] (datomic.artemis_client.RpcServer. consumer producer cleanup)))
  (defn create-rpc-server
    ([session_fn request_address response_address handler & p__18462]
      (let [vec__18463 p__18462
            map__18466 (nth vec__18463 (int 0) nil)
            map__18466 (if (seq? map__18466)
                         (if (next map__18466)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18466))
                           (if (seq map__18466) (first map__18466) {}))
                         map__18466)
            read_handlers (get map__18466 :read-handlers)
            write_handlers (get map__18466 :write-handlers)
            session (^clojure.lang.IFn session_fn)
            serializer (create-serializer write_handlers)
            deserializer (create-deserializer read_handlers)
            cq (partial create-temporary-queue session)
            dq (partial delete-queue session)
            session session
            cleanup (error/runonce
                      (fn fn__18467
                        ([]
                          (try
                            (common/async-shutdown session)
                            (catch
                              java.lang.Throwable
                              t__8540__auto__
                              (error/report t__8540__auto__)))
                          nil)))]
        (try
          (let [request_queue (^clojure.lang.IFn cq request_address)
                cleanup (error/runonce
                          (fn fn__18471
                            ([]
                              (try
                                (^clojure.lang.IFn dq request_queue)
                                (catch
                                  java.lang.Throwable
                                  t__8540__auto__
                                  (error/report t__8540__auto__)))
                              (^clojure.lang.IFn cleanup))))]
            (try
              (let [response_queue (^clojure.lang.IFn cq response_address)
                    cleanup (error/runonce
                              (fn fn__18475
                                ([]
                                  (try
                                    (^clojure.lang.IFn dq response_queue)
                                    (catch
                                      java.lang.Throwable
                                      t__8540__auto__
                                      (error/report t__8540__auto__)))
                                  (^clojure.lang.IFn cleanup))))]
                (try
                  (let [consumer (create-consumer session request_queue)
                        cleanup (error/runonce
                                  (fn fn__18479
                                    ([]
                                      (try
                                        (do
                                          (.close
                                            ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                                          nil)
                                        (catch
                                          java.lang.Throwable
                                          t__8540__auto__
                                          (error/report t__8540__auto__)))
                                      (^clojure.lang.IFn cleanup))))]
                    (try
                      (let [producer (create-producer session response_address)
                            cleanup (error/runonce
                                      (fn fn__18483
                                        ([]
                                          (try
                                            (do
                                              (.close
                                                ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                                              nil)
                                            (catch
                                              java.lang.Throwable
                                              t__8540__auto__
                                              (error/report t__8540__auto__)))
                                          (^clojure.lang.IFn cleanup))))]
                        (try
                          (do
                            (set-handler
                              consumer
                              (fn fn__18487
                                ([msg]
                                  (let [map__18488 (^clojure.lang.IFn deserializer msg)
                                        map__18488 (if (seq? map__18488)
                                                     (if (next map__18488)
                                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                         (to-array map__18488))
                                                       (if (seq map__18488) (first map__18488) {}))
                                                     map__18488)
                                        id (get map__18488 :id)
                                        code (get map__18488 :code)
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
                            t__8541__auto__
                            (do
                              (try
                                (do
                                  (.close
                                    ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                                  nil)
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
                            (do
                              (.close
                                ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                              nil)
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
                        (^clojure.lang.IFn dq response_queue)
                        (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
                      (throw ^java.lang.Throwable t__8541__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__8541__auto__
                (do
                  (try
                    (^clojure.lang.IFn dq request_queue)
                    (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
                  (throw ^java.lang.Throwable t__8541__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__8541__auto__
            (do
              (try
                (common/async-shutdown session)
                (catch java.lang.Throwable t__8540__auto__ (error/report t__8540__auto__)))
              (throw ^java.lang.Throwable t__8541__auto__)
              nil))))))
  (defn rpc-request
    ([conn request]
      (let [id (common/rand-uuid) p (promise)]
        (cache/put (.-response-map ^datomic.artemis_client.RpcClient conn) id p)
        (let [msg (create-message (.-session ^datomic.artemis_client.RpcClient conn) false)]
          ((.-serializer ^datomic.artemis_client.RpcClient conn) {:id id, :code request} msg)
          (.send
            (.-producer ^datomic.artemis_client.RpcClient conn)
            ^org.apache.activemq.artemis.api.core.Message msg))
        p))))