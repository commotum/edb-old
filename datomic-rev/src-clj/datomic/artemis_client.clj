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
                         (fn fn__20773
                           ([p__20772]
                             (let [vec__20774 p__20772
                                   k (nth vec__20774 (int 0) nil)
                                   v (nth vec__20774 (int 1) nil)]
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
        (fn fn__20808
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__9147__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client")
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
    (start-session*
      [this creds p__20804]
      (let [map__20806 p__20804
            map__20806 (if (seq? map__20806)
                         (clojure.lang.PersistentHashMap/create (seq map__20806))
                         map__20806)
            xa (get map__20806 :xa false)
            auto_commit_sends (get map__20806 :auto-commit-sends true)
            auto_commit_acks (get map__20806 :auto-commit-acks true)
            pre_acknowledge (get map__20806 :pre-acknowledge false)
            ack_batch_size (get map__20806 :ack-batch-size 1)
            on_failure (get map__20806 :on-failure)
            map__20807 (common/require-keys creds [:username :password])
            map__20807 (if (seq? map__20807)
                         (clojure.lang.PersistentHashMap/create (seq map__20807))
                         map__20807)
            username (get map__20807 :username)
            password (get map__20807 :password)
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
            (wrap-as-failure-listener on_failure))
          nil)
        (.start ^org.apache.activemq.artemis.api.core.client.ClientSession session)
        session)))
  (clojure.core/import 'datomic.artemis_client.SessionFactoryBundle)
  (defn ->SessionFactoryBundle
    ([locator factory cleanup]
      (datomic.artemis_client.SessionFactoryBundle. locator factory cleanup)))
  (defn create-server-locator
    ([connector p__20814]
      (let [map__20815 p__20814
            map__20815 (if (seq? map__20815)
                         (clojure.lang.PersistentHashMap/create (seq map__20815))
                         map__20815)
            ttl (get map__20815 :ttl)]
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
                      (fn fn__20818
                        ([]
                          (try
                            (do
                              (.close
                                ^org.apache.activemq.artemis.api.core.client.ServerLocator loc)
                              nil)
                            (catch
                              java.lang.Throwable
                              t__708__auto__
                              (error/report t__708__auto__)))
                          nil)))]
        (try
          (let [session_factory (.createSessionFactory
                                  ^org.apache.activemq.artemis.api.core.client.ServerLocator loc)
                cleanup (error/runonce
                          (fn fn__20822
                            ([]
                              (try
                                (do
                                  (.close
                                    ^org.apache.activemq.artemis.api.core.client.ClientSessionFactory session_factory)
                                  nil)
                                (catch
                                  java.lang.Throwable
                                  t__708__auto__
                                  (error/report t__708__auto__)))
                              (^clojure.lang.IFn cleanup))))]
            (try
              (datomic.artemis_client.SessionFactoryBundle. loc session_factory cleanup)
              (catch
                java.lang.Throwable
                t__709__auto__
                (do
                  (try
                    (do
                      (.close
                        ^org.apache.activemq.artemis.api.core.client.ClientSessionFactory session_factory)
                      nil)
                    (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
                  (throw ^java.lang.Throwable t__709__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__709__auto__
            (do
              (try
                (do (.close ^org.apache.activemq.artemis.api.core.client.ServerLocator loc) nil)
                (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
              (throw ^java.lang.Throwable t__709__auto__)
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
    {:async-shutdown (fn fn__20831 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ClientSessionFactory
    common/AsyncShutdown
    {:async-shutdown (fn fn__20833 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ClientConsumer
    common/AsyncShutdown
    {:async-shutdown (fn fn__20835 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ClientSession
    common/AsyncShutdown
    {:async-shutdown (fn fn__20837 ([this] (.close this) (promise/delivered true)))})
  (extend
    org.apache.activemq.artemis.api.core.client.ServerLocator
    common/AsyncShutdown
    {:async-shutdown (fn fn__20839 ([this] (.close this) (promise/delivered true)))})
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
    ([session queue_name & p__20844]
      (let [map__20845 p__20844
            map__20845 (if (seq? map__20845)
                         (clojure.lang.PersistentHashMap/create (seq map__20845))
                         map__20845)
            window_size (get map__20845 :window-size (long (* 1024 1024)))
            max_rate (get map__20845 :max-rate -1)
            browse (get map__20845 :browse false)]
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
     (fn fn__20847
       ([this]
         (let [temp__5455__auto__ (.receive this)]
           (if temp__5455__auto__
             (let [msg temp__5455__auto__] msg)
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
      (fn fn__20860
        ([obj msg]
          (let [fout (fressian/create-writer
                       (output-stream msg)
                       (merge fressian/clojure-write-handlers write_handlers))]
            (.writeObject ^org.fressian.Writer fout obj))))))
  (defn create-deserializer
    ([read_handlers]
      (fn fn__20863
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
        (fn fn__20871
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__9147__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client")
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
    ([session request_address response_address & p__20877]
      (let [vec__20878 p__20877
            map__20881 (nth vec__20878 (int 0) nil)
            map__20881 (if (seq? map__20881)
                         (clojure.lang.PersistentHashMap/create (seq map__20881))
                         map__20881)
            read_handlers (get map__20881 :read-handlers)
            write_handlers (get map__20881 :write-handlers)
            serializer (create-serializer write_handlers)
            deserializer (create-deserializer read_handlers)
            response_map (cache/create-response-map 2)
            cq (partial create-temporary-queue session)
            dq (partial delete-queue session)
            request_queue (^clojure.lang.IFn cq request_address)
            cleanup (error/runonce
                      (fn fn__20882
                        ([]
                          (try
                            (^clojure.lang.IFn dq request_queue)
                            (catch
                              java.lang.Throwable
                              t__708__auto__
                              (error/report t__708__auto__)))
                          nil)))]
        (try
          (let [response_queue (^clojure.lang.IFn cq response_address)
                cleanup (error/runonce
                          (fn fn__20886
                            ([]
                              (try
                                (^clojure.lang.IFn dq response_queue)
                                (catch
                                  java.lang.Throwable
                                  t__708__auto__
                                  (error/report t__708__auto__)))
                              (^clojure.lang.IFn cleanup))))]
            (try
              (let [producer (create-producer session request_address)
                    cleanup (error/runonce
                              (fn fn__20890
                                ([]
                                  (try
                                    (do
                                      (.close
                                        ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                                      nil)
                                    (catch
                                      java.lang.Throwable
                                      t__708__auto__
                                      (error/report t__708__auto__)))
                                  (^clojure.lang.IFn cleanup))))]
                (try
                  (let [consumer (create-consumer session response_queue)
                        cleanup (error/runonce
                                  (fn fn__20894
                                    ([]
                                      (try
                                        (do
                                          (.close
                                            ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                                          nil)
                                        (catch
                                          java.lang.Throwable
                                          t__708__auto__
                                          (error/report t__708__auto__)))
                                      (^clojure.lang.IFn cleanup))))]
                    (try
                      (do
                        (set-handler
                          consumer
                          (fn fn__20898
                            ([msg]
                              (let [map__20899 (^clojure.lang.IFn deserializer msg)
                                    map__20899 (if (seq? map__20899)
                                                 (clojure.lang.PersistentHashMap/create
                                                   (seq map__20899))
                                                 map__20899)
                                    result map__20899
                                    id (get map__20899 :id)
                                    temp__5457__auto__ (cache/remove response_map id)]
                                (when temp__5457__auto__
                                  (let [p temp__5457__auto__] (deliver p result)))))))
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
                        t__709__auto__
                        (do
                          (try
                            (do
                              (.close
                                ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                              nil)
                            (catch
                              java.lang.Throwable
                              t__708__auto__
                              (error/report t__708__auto__)))
                          (throw ^java.lang.Throwable t__709__auto__)
                          nil))))
                  (catch
                    java.lang.Throwable
                    t__709__auto__
                    (do
                      (try
                        (do
                          (.close
                            ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                          nil)
                        (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
                      (throw ^java.lang.Throwable t__709__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__709__auto__
                (do
                  (try
                    (^clojure.lang.IFn dq response_queue)
                    (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
                  (throw ^java.lang.Throwable t__709__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__709__auto__
            (do
              (try
                (^clojure.lang.IFn dq request_queue)
                (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
              (throw ^java.lang.Throwable t__709__auto__)
              nil))))))
  (deftype
    RpcServer
    [consumer producer cleanup]
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (future-call
        (fn fn__20912
          ([]
            (try
              (do (^clojure.lang.IFn cleanup) true)
              (catch
                java.lang.Throwable
                t__9147__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-client")
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
  (clojure.core/import 'datomic.artemis_client.RpcServer)
  (defn ->RpcServer
    ([consumer producer cleanup] (datomic.artemis_client.RpcServer. consumer producer cleanup)))
  (defn create-rpc-server
    ([session_fn request_address response_address handler & p__20918]
      (let [vec__20919 p__20918
            map__20922 (nth vec__20919 (int 0) nil)
            map__20922 (if (seq? map__20922)
                         (clojure.lang.PersistentHashMap/create (seq map__20922))
                         map__20922)
            read_handlers (get map__20922 :read-handlers)
            write_handlers (get map__20922 :write-handlers)
            session (^clojure.lang.IFn session_fn)
            serializer (create-serializer write_handlers)
            deserializer (create-deserializer read_handlers)
            cq (partial create-temporary-queue session)
            dq (partial delete-queue session)
            session session
            cleanup (error/runonce
                      (fn fn__20923
                        ([]
                          (try
                            (common/async-shutdown session)
                            (catch
                              java.lang.Throwable
                              t__708__auto__
                              (error/report t__708__auto__)))
                          nil)))]
        (try
          (let [request_queue (^clojure.lang.IFn cq request_address)
                cleanup (error/runonce
                          (fn fn__20927
                            ([]
                              (try
                                (^clojure.lang.IFn dq request_queue)
                                (catch
                                  java.lang.Throwable
                                  t__708__auto__
                                  (error/report t__708__auto__)))
                              (^clojure.lang.IFn cleanup))))]
            (try
              (let [response_queue (^clojure.lang.IFn cq response_address)
                    cleanup (error/runonce
                              (fn fn__20931
                                ([]
                                  (try
                                    (^clojure.lang.IFn dq response_queue)
                                    (catch
                                      java.lang.Throwable
                                      t__708__auto__
                                      (error/report t__708__auto__)))
                                  (^clojure.lang.IFn cleanup))))]
                (try
                  (let [consumer (create-consumer session request_queue)
                        cleanup (error/runonce
                                  (fn fn__20935
                                    ([]
                                      (try
                                        (do
                                          (.close
                                            ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                                          nil)
                                        (catch
                                          java.lang.Throwable
                                          t__708__auto__
                                          (error/report t__708__auto__)))
                                      (^clojure.lang.IFn cleanup))))]
                    (try
                      (let [producer (create-producer session response_address)
                            cleanup (error/runonce
                                      (fn fn__20939
                                        ([]
                                          (try
                                            (do
                                              (.close
                                                ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                                              nil)
                                            (catch
                                              java.lang.Throwable
                                              t__708__auto__
                                              (error/report t__708__auto__)))
                                          (^clojure.lang.IFn cleanup))))]
                        (try
                          (do
                            (set-handler
                              consumer
                              (fn fn__20943
                                ([msg]
                                  (let [map__20944 (^clojure.lang.IFn deserializer msg)
                                        map__20944 (if (seq? map__20944)
                                                     (clojure.lang.PersistentHashMap/create
                                                       (seq map__20944))
                                                     map__20944)
                                        id (get map__20944 :id)
                                        code (get map__20944 :code)
                                        response (create-message session false)
                                        result (try
                                                 (let [handler_result (^clojure.lang.IFn handler
                                                                        code)
                                                       temp__5455__auto__ (:failed handler_result)]
                                                   (if temp__5455__auto__
                                                     (let [failed temp__5455__auto__]
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
                            t__709__auto__
                            (do
                              (try
                                (do
                                  (.close
                                    ^org.apache.activemq.artemis.api.core.client.ClientProducer producer)
                                  nil)
                                (catch
                                  java.lang.Throwable
                                  t__708__auto__
                                  (error/report t__708__auto__)))
                              (throw ^java.lang.Throwable t__709__auto__)
                              nil))))
                      (catch
                        java.lang.Throwable
                        t__709__auto__
                        (do
                          (try
                            (do
                              (.close
                                ^org.apache.activemq.artemis.api.core.client.ClientConsumer consumer)
                              nil)
                            (catch
                              java.lang.Throwable
                              t__708__auto__
                              (error/report t__708__auto__)))
                          (throw ^java.lang.Throwable t__709__auto__)
                          nil))))
                  (catch
                    java.lang.Throwable
                    t__709__auto__
                    (do
                      (try
                        (^clojure.lang.IFn dq response_queue)
                        (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
                      (throw ^java.lang.Throwable t__709__auto__)
                      nil))))
              (catch
                java.lang.Throwable
                t__709__auto__
                (do
                  (try
                    (^clojure.lang.IFn dq request_queue)
                    (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
                  (throw ^java.lang.Throwable t__709__auto__)
                  nil))))
          (catch
            java.lang.Throwable
            t__709__auto__
            (do
              (try
                (common/async-shutdown session)
                (catch java.lang.Throwable t__708__auto__ (error/report t__708__auto__)))
              (throw ^java.lang.Throwable t__709__auto__)
              nil))))))
  (defn rpc-request
    ([conn request]
      (let [id (common/rand-uuid) p (promise)]
        (cache/put (.-response-map ^datomic.artemis_client.RpcClient conn) id p)
        (let [msg (create-message (.-session ^datomic.artemis_client.RpcClient conn) false)]
          ((.-serializer ^datomic.artemis_client.RpcClient conn) {:id id, :code request} msg)
          (.send
            (.-producer ^datomic.artemis_client.RpcClient conn)
            ^org.apache.activemq.artemis.api.core.Message msg)
          nil)
        p))))