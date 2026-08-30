(do
  (clojure.core/in-ns 'datomic.artemis-server)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.string :as 'string]
        ['datomic.artemis-client :as 'client]
        ['datomic.common :as 'common]
        ['datomic.crypto :as 'crypto]
        ['datomic.error :as 'error]
        ['datomic.promise :as 'promise]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'java.util.Set)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.TransportConfiguration)
      (clojure.core/import 'org.apache.activemq.artemis.core.config.Configuration)
      (clojure.core/import 'org.apache.activemq.artemis.core.config.impl.ConfigurationImpl)
      (clojure.core/import 'org.apache.activemq.artemis.core.server.ActiveMQServer)
      (clojure.core/import 'org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl)
      (clojure.core/import 'org.apache.activemq.artemis.core.settings.impl.AddressSettings)
      (clojure.core/import
        'org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager)))
  (when-not (.equals 'datomic.artemis-server 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.artemis-server))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.string :as 'string]
          ['datomic.artemis-client :as 'client]
          ['datomic.common :as 'common]
          ['datomic.crypto :as 'crypto]
          ['datomic.error :as 'error]
          ['datomic.promise :as 'promise]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'java.util.Set)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.TransportConfiguration)
        (clojure.core/import 'org.apache.activemq.artemis.core.config.Configuration)
        (clojure.core/import 'org.apache.activemq.artemis.core.config.impl.ConfigurationImpl)
        (clojure.core/import 'org.apache.activemq.artemis.core.server.ActiveMQServer)
        (clojure.core/import 'org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl)
        (clojure.core/import 'org.apache.activemq.artemis.core.settings.impl.AddressSettings)
        (clojure.core/import
          'org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager))))
  (set! *warn-on-reflection* true)
  (.setMeta (clojure.lang.RT/var "datomic.artemis-server" "work-dir") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.artemis-server" "work-dir") (atom "tmp/hornet"))
  (def netty-acceptor-factory
   "org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory")
  (reset-meta!
    #'netty-acceptor-factory
    (assoc {:column (int 1)} :name 'netty-acceptor-factory :ns *ns*))
  (def in-vm-acceptor-factory
   "org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory")
  (reset-meta!
    #'in-vm-acceptor-factory
    (assoc {:column (int 1)} :name 'in-vm-acceptor-factory :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-server" "create-acceptor")
    {:tag org.apache.activemq.artemis.api.core.TransportConfiguration,
     :arglists (clojure.core/list ['acceptor-class-name '& 'kvs]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-server" "create-acceptor")
    (fn create_acceptor
      ([acceptor_class_name & kvs] (apply client/create-transport acceptor_class_name kvs))))
  (defn create-configuration
    ([host port encrypt_channel]
      (let [acfgs #{(create-acceptor in-vm-acceptor-factory :serverId port)
                    (create-acceptor
                      netty-acceptor-factory
                      :port
                      port
                      :host
                      host
                      :sslEnabled
                      encrypt_channel
                      :verifyHost
                      false
                      :keyStorePath
                      (or
                        (java.lang.System/getProperty "javax.net.ssl.keyStore")
                        "datomic/transactor-key.jks")
                      :keyStorePassword
                      (or
                        (java.lang.System/getProperty "javax.net.ssl.keyStorePassword")
                        "transactor")
                      :trustStorePath
                      (or
                        (java.lang.System/getProperty "javax.net.ssl.trustStore")
                        "datomic/transactor-trust.jks")
                      :trustStorePassword
                      (or
                        (java.lang.System/getProperty "javax.net.ssl.trustStorePassword")
                        "transactor"))}]
        (doto
          (org.apache.activemq.artemis.core.config.impl.ConfigurationImpl.)
          (.setPersistenceEnabled (boolean (.booleanValue false)))
          (.setSecurityEnabled (boolean (.booleanValue true)))
          (.setClusterUser "HORNETQ.MANAGEMENT.ADMIN.USER")
          (.setClusterPassword (crypto/random-string 256))
          (.setJournalSyncTransactional (boolean (.booleanValue false)))
          (.setJournalDirectory (deref work-dir))
          (.setJournalSyncNonTransactional (boolean (.booleanValue false)))
          (.setJournalType org.apache.activemq.artemis.core.server.JournalType/NIO)
          (.setAcceptorConfigurations ^java.util.Set acfgs)))))
  (reset-meta!
    #'create-configuration
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['host 'port 'encrypt-channel]
           {:tag 'org.apache.activemq.artemis.core.config.impl.ConfigurationImpl})),
       :column (int 1)}
      :name
      'create-configuration
      :ns
      *ns*))
  (defn remote-ips
    ([server]
      (let [local_addrs #{"invm"}
            control (.getActiveMQServerControl
                      ^org.apache.activemq.artemis.core.server.ActiveMQServer server)
            trim_addr (fn trim_addr
                        ([addr] (string/replace (string/replace addr #":.*" "") #"^/" "")))
            addrs (map
                    trim_addr
                    (when control
                      (.listRemoteAddresses
                        ^org.apache.activemq.artemis.core.management.impl.ActiveMQServerControlImpl control)))]
        (into #{} (remove local_addrs addrs)))))
  (reset-meta!
    #'remote-ips
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'server {:tag 'ActiveMQServer})]), :column (int 1)}
      :name
      'remote-ips
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-server" "create-security-manager")
    {:tag org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager,
     :arglists (clojure.core/list ['server-ref 'endpoint 'n]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-server" "create-security-manager")
    (fn create_security_manager
      ([server_ref endpoint n]
        (let [map__27596 (common/require-keys endpoint [:username :password])
              map__27596 (if (seq? map__27596)
                           (if (next map__27596)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__27596))
                             (if (seq map__27596) (first map__27596) {}))
                           map__27596)
              username (get map__27596 :username)
              password (get map__27596 :password)]
          (reify
            org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager
            (^boolean validateUserAndRole
              [this
               ^java.lang.String username
               ^java.lang.String password
               ^java.util.Set roles
               ^org.apache.activemq.artemis.core.security.CheckType checktype]
              (.validateUser this ^java.lang.String username ^java.lang.String password))
            (^boolean validateUser
              [this ^java.lang.String user ^java.lang.String pass]
              (.booleanValue
                (let [ips (remote-ips (deref server_ref))]
                  (if (> (count ips) n)
                    (do
                      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.artemis-server")]
                        (when (.isWarnEnabled ^org.slf4j.Logger logger)
                          (.warn
                            ^org.slf4j.Logger logger
                            (logger/process
                              {:message "Connection limit exceeded", :limit n, :ips ips})))
                        nil)
                      false)
                    (and (= username user) (= password pass)))))))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.artemis-server" "start-server")
    {:tag org.apache.activemq.artemis.core.server.ActiveMQServer,
     :arglists
     (clojure.core/list
       [{:keys ['port 'host 'encrypt-channel], :as 'endpoint, :or {'port 8444, 'host "0.0.0.0"}}
        'n]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.artemis-server" "start-server")
    (fn start_server
      ([p__27601 n]
        (let [map__27602 p__27601
              map__27602 (if (seq? map__27602)
                           (if (next map__27602)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__27602))
                             (if (seq map__27602) (first map__27602) {}))
                           map__27602)
              endpoint map__27602
              port (get map__27602 :port 8444)
              host (get map__27602 :host "0.0.0.0")
              encrypt_channel (get map__27602 :encrypt-channel)
              server_ref (atom nil)
              server (org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl.
                       (create-configuration host port encrypt_channel)
                       (create-security-manager server_ref endpoint (or n 2)))]
          (reset! server_ref server)
          (.start ^org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl server)
          server))))
  (extend
    org.apache.activemq.artemis.core.server.ActiveMQServer
    common/AsyncShutdown
    {:async-shutdown (fn fn__27605 ([this] (.stop this) (promise/delivered true)))})
  (defn add-address-settings
    ([server address & p__27607]
      (let [map__27608 p__27607
            map__27608 (if (seq? map__27608)
                         (if (next map__27608)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27608))
                           (if (seq map__27608) (first map__27608) {}))
                         map__27608)
            maxSizeBytes (get map__27608 :maxSizeBytes)
            addressFullMessagePolicy (get map__27608 :addressFullMessagePolicy)]
        (when-not maxSizeBytes
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'maxSizeBytes)))))
        (when-not addressFullMessagePolicy
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str 'addressFullMessagePolicy)))))
        (.addMatch
          (.getAddressSettingsRepository
            ^org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl server)
          ^java.lang.String address
          (doto
            (org.apache.activemq.artemis.core.settings.impl.AddressSettings.)
            (.setMaxSizeBytes (long ^java.lang.Number maxSizeBytes))
            (.setAddressFullMessagePolicy
              ^org.apache.activemq.artemis.core.settings.impl.AddressFullMessagePolicy addressFullMessagePolicy)))
        nil)))
  (reset-meta!
    #'add-address-settings
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'server {:tag 'ActiveMQServerImpl})
          (.withMeta 'address {:tag 'String})
          '&
          {:keys ['maxSizeBytes 'addressFullMessagePolicy]}]),
       :column (int 1)}
      :name
      'add-address-settings
      :ns
      *ns*)))