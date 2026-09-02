(do
  (clojure.core/in-ns 'datomic.jetty)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.jetty)
    {:doc
     "Ring adapter and lifecycle helpers for embedded Jetty servers. Builds HTTP and optional TLS connectors, configures client-certificate policy, adapts Ring request and response maps, and starts a server with optional caller customization and joining."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'datomic_jetty.impl.ProxyHandler)
      (clojure.core/import 'org.eclipse.jetty.server.Server)
      (clojure.core/import 'org.eclipse.jetty.server.Request)
      (clojure.core/import 'org.eclipse.jetty.server.Response)
      (clojure.core/import 'org.eclipse.jetty.server.ServerConnector)
      (clojure.core/import 'org.eclipse.jetty.server.HttpConfiguration)
      (clojure.core/import 'org.eclipse.jetty.server.HttpConnectionFactory)
      (clojure.core/import 'org.eclipse.jetty.server.handler.AbstractHandler)
      (clojure.core/import 'org.eclipse.jetty.util.thread.QueuedThreadPool)
      (clojure.core/import 'org.eclipse.jetty.util.ssl.SslContextFactory)
      (clojure.core/require ['ring.util.servlet :as 'servlet])))
  (when-not (.equals 'datomic.jetty 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.jetty))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'datomic_jetty.impl.ProxyHandler)
        (clojure.core/import 'org.eclipse.jetty.server.Server)
        (clojure.core/import 'org.eclipse.jetty.server.Request)
        (clojure.core/import 'org.eclipse.jetty.server.Response)
        (clojure.core/import 'org.eclipse.jetty.server.ServerConnector)
        (clojure.core/import 'org.eclipse.jetty.server.HttpConfiguration)
        (clojure.core/import 'org.eclipse.jetty.server.HttpConnectionFactory)
        (clojure.core/import 'org.eclipse.jetty.server.handler.AbstractHandler)
        (clojure.core/import 'org.eclipse.jetty.util.thread.QueuedThreadPool)
        (clojure.core/import 'org.eclipse.jetty.util.ssl.SslContextFactory)
        (clojure.core/require ['ring.util.servlet :as 'servlet]))))
  (set! *warn-on-reflection* true)
  (defn handle
    ([handler base_request request response]
      (let [request_map (servlet/build-request-map request)
            response_map (^clojure.lang.IFn handler request_map)]
        (when response_map
          (.setCharacterEncoding ^org.eclipse.jetty.server.Response response "UTF-8")
          (servlet/update-servlet-response response response_map)
          (.setHandled
            ^org.eclipse.jetty.server.Request base_request
            (boolean (.booleanValue true)))
          nil))))
  (reset-meta!
    #'handle
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         ['handler
          (.withMeta 'base-request {:tag 'Request})
          'request
          (.withMeta 'response {:tag 'Response})]),
       :doc
       "Runs a Ring handler for one Jetty exchange, writes a non-nil Ring response as UTF-8, and marks the base request handled.",
       :column (int 1)}
      :name
      'handle
      :ns
      *ns*))
  (defn proxy-handler
    ([handler]
      (proxy
        [org.eclipse.jetty.server.handler.AbstractHandler]
        []
        (handle
          [_ base_request request response]
          (handle handler base_request request response)))))
  (reset-meta!
    #'proxy-handler
    (assoc
      {:private true, :arglists (clojure.core/list ['handler]), :column (int 1)}
      :name
      'proxy-handler
      :ns
      *ns*))
  (defn ssl-context-factory
    ([options]
      (let [context (org.eclipse.jetty.util.ssl.SslContextFactory.)]
        (if (string? (^clojure.lang.IFn options :keystore))
          (.setKeyStorePath
            ^org.eclipse.jetty.util.ssl.SslContextFactory context
            (^clojure.lang.IFn options :keystore))
          (.setKeyStore
            ^org.eclipse.jetty.util.ssl.SslContextFactory context
            (^clojure.lang.IFn options :keystore)))
        (.setKeyStorePassword
          ^org.eclipse.jetty.util.ssl.SslContextFactory context
          (^clojure.lang.IFn options :key-password))
        (when (^clojure.lang.IFn options :truststore)
          (.setTrustStore
            ^org.eclipse.jetty.util.ssl.SslContextFactory context
            (^clojure.lang.IFn options :truststore)))
        (when (^clojure.lang.IFn options :trust-password)
          (.setTrustStorePassword
            ^org.eclipse.jetty.util.ssl.SslContextFactory context
            (^clojure.lang.IFn options :trust-password)))
        (let [G__21990 (^clojure.lang.IFn options :client-auth)]
          (case
            G__21990
            :want
            (do
              (.setWantClientAuth
                ^org.eclipse.jetty.util.ssl.SslContextFactory context
                (boolean (.booleanValue true)))
              nil)
            :need
            (do
              (.setNeedClientAuth
                ^org.eclipse.jetty.util.ssl.SslContextFactory context
                (boolean (.booleanValue true)))
              nil)
            nil))
        context)))
  (reset-meta!
    #'ssl-context-factory
    (assoc
      {:private true,
       :arglists (clojure.core/list ['options]),
       :doc
       "Builds the TLS context from :keystore and :key-password. A keystore may be a path or KeyStore object; :truststore and :trust-password configure trust material, and :client-auth may request (:want) or require (:need) client certificates.",
       :column (int 1)}
      :name
      'ssl-context-factory
      :ns
      *ns*))
  (defn ssl-connector
    ([server options]
      (doto
        (org.eclipse.jetty.server.ServerConnector.
          ^org.eclipse.jetty.server.Server server
          (ssl-context-factory options))
        (.setPort (int (^clojure.lang.IFn options :ssl-port 443)))
        (.setHost (^clojure.lang.IFn options :host)))))
  (reset-meta!
    #'ssl-connector
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'server {:tag 'Server}) 'options]),
       :column (int 1)}
      :name
      'ssl-connector
      :ns
      *ns*))
  (defn http-configuration
    ([options]
      (let [map__21994 options
            map__21994 (if (seq? map__21994)
                         (if (next map__21994)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21994))
                           (if (seq map__21994) (first map__21994) {}))
                         map__21994)
            ssl? (get map__21994 :ssl?)
            ssl_port (get map__21994 :ssl-port)
            http_conf (org.eclipse.jetty.server.HttpConfiguration.)]
        (when ssl?
          (.setSecurePort
            ^org.eclipse.jetty.server.HttpConfiguration http_conf
            (int ^java.lang.Number ssl_port)))
        (let [G__21995 http_conf]
          (.setSendDateHeader
            ^org.eclipse.jetty.server.HttpConfiguration G__21995
            (boolean (.booleanValue true)))
          G__21995))))
  (reset-meta!
    #'http-configuration
    (assoc
      {:private true,
       :arglists (clojure.core/list ['options]),
       :doc
       "Creates HTTP connection settings with date headers enabled. When :ssl? is true, :ssl-port is required and is recorded as the secure redirect port.",
       :column (int 1)}
      :name
      'http-configuration
      :ns
      *ns*))
  (defn create-server
    ([options]
      (let [server (org.eclipse.jetty.server.Server.
                     (org.eclipse.jetty.util.thread.QueuedThreadPool.
                       (int (^clojure.lang.IFn options :max-threads 50))))
            connector (doto
                        (org.eclipse.jetty.server.ServerConnector.
                          ^org.eclipse.jetty.server.Server server)
                        (.addConnectionFactory
                          (org.eclipse.jetty.server.HttpConnectionFactory.
                            (http-configuration options)))
                        (.setPort (int (^clojure.lang.IFn options :port 80)))
                        (.setHost (^clojure.lang.IFn options :host)))]
        (.addConnector
          ^org.eclipse.jetty.server.Server server
          ^org.eclipse.jetty.server.Connector connector)
        (when (or (^clojure.lang.IFn options :ssl?) (^clojure.lang.IFn options :ssl-port))
          (.addConnector ^org.eclipse.jetty.server.Server server (ssl-connector server options)))
        server)))
  (reset-meta!
    #'create-server
    (assoc
      {:arglists (clojure.core/list ['options]),
       :doc
       "Creates an unstarted Jetty Server. :max-threads defaults to 50, the HTTP :port defaults to 80, and :host optionally restricts the bind address. A non-nil :ssl-port adds a TLS connector at that port. Setting :ssl? also requests TLS and requires :ssl-port for the HTTP secure-port configuration.",
       :column (int 1)}
      :name
      'create-server
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.jetty" "run-jetty")
    {:tag org.eclipse.jetty.server.Server,
     :arglists (clojure.core/list ['handler 'options]),
     :doc
     "Creates and starts a Jetty server for handler. An optional :configurator function receives the server before it starts. The call joins the server thread unless :join? is false and returns the Server after startup or join completion.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.jetty" "run-jetty")
    (fn run_jetty
      ([handler options]
        (let [s (create-server (dissoc options :configurator))]
          (let [G__22000 s]
            (.setHandler
              ^org.eclipse.jetty.server.handler.HandlerWrapper G__22000
              (datomic_jetty.impl.ProxyHandler. (partial #'handle handler))))
          (let [temp__5825__auto__ (:configurator options)]
            (when temp__5825__auto__
              (let [configurator temp__5825__auto__] (^clojure.lang.IFn configurator s))))
          (.start ^org.eclipse.jetty.util.component.AbstractLifeCycle s)
          (when (:join? options true) (.join ^org.eclipse.jetty.server.Server s))
          s)))))
