(do
  (clojure.core/in-ns 'datomic.client-server.auth)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.client-server.auth)
    {:doc
     "Peer Server request authentication using configured access-key and secret pairs. Credential comparison and request validation produce anomaly data suitable for the Client protocol."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'java.security.KeyStore)
      (clojure.core/require
        ['clojure.java.io :as 'io]
        ['cognitect.anomalies :as 'anom]
        ['cognitect.hmac-authn :as 'hmac]
        ['datomic.client-spi.next-token :as 'nt]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.client-server.auth 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.client-server.auth))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'java.security.KeyStore)
        (clojure.core/require
          ['clojure.java.io :as 'io]
          ['cognitect.anomalies :as 'anom]
          ['cognitect.hmac-authn :as 'hmac]
          ['datomic.client-spi.next-token :as 'nt]
          ['datomic.slf4j :as 'logger]))))
  (defn load-keystore
    ([resource pwd]
      (with-open [is (io/input-stream resource)]
        (let [G__26169 (KeyStore/getInstance (KeyStore/getDefaultType))]
          (.load ^java.security.KeyStore G__26169 ^java.io.InputStream is ^chars pwd)
          G__26169))))
  (reset-meta!
    #'load-keystore
    (assoc
      {:private true, :arglists (clojure.core/list ['resource 'pwd]), :column (int 1)}
      :name
      'load-keystore
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.client-server.auth" "ssl-config-ref")
    {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.client-server.auth" "ssl-config-ref")
    (delay
      {:keystore
       (load-keystore (io/resource "datomic/transactor-key.jks") (.toCharArray "transactor")),
       :key-password "transactor"}))
  (defn ssl-config ([] (deref ssl-config-ref)))
  (reset-meta!
    #'ssl-config
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'ssl-config :ns *ns*))
  ;; Verifies the request signature against the secret selected by its access-key identifier.
  (defn callback*
    ([access-key->secret req]
      (let [temp__5823__auto__ (some->
                                 req
                                 (get-in [:headers "authorization"])
                                 (hmac/parse-authorization-header)
                                 (:access-key-id))]
        (if temp__5823__auto__
          (let [akid temp__5823__auto__
                verify-params (merge
                                {:service "peer-server",
                                 :region "none",
                                 :access-key-id akid,
                                 :secret (get access-key->secret akid)})
                temp__5823__auto__ (hmac/verify-failure req verify-params)]
            (if temp__5823__auto__
              (let [failure temp__5823__auto__]
                #:cognitect.anomalies{:category :cognitect.anomalies/forbidden,
                                      :message (str failure)})
              req))
          #:cognitect.anomalies{:category :cognitect.anomalies/forbidden,
                                :message "Bad authorization header"}))))
  (reset-meta!
    #'callback*
    (assoc
      {:private true, :arglists (clojure.core/list ['access-key->secret 'req]), :column (int 1)}
      :name
      'callback*
      :ns
      *ns*))
  ;; Returns the authenticated request, logging and rejecting invalid credentials.
  (defn callback
    ([access-key->secret req]
      (let [result (callback* access-key->secret req)]
        (if (:cognitect.anomalies/category result)
          (do
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.client-server.auth")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process (assoc result :event :datomic.client-server.auth/auth-failed))))
              nil)
            nil)
          result))))
  (reset-meta!
    #'callback
    (assoc
      {:arglists (clojure.core/list ['access-key->secret 'req]), :column (int 1)}
      :name
      'callback
      :ns
      *ns*))
  (defn genkey
    ([cipher-name]
      (let [keygen (javax.crypto.KeyGenerator/getInstance ^java.lang.String cipher-name)]
        (.init ^javax.crypto.KeyGenerator keygen (int 128))
        (.generateKey ^javax.crypto.KeyGenerator keygen))))
  (reset-meta!
    #'genkey
    (assoc
      {:private true, :arglists (clojure.core/list ['cipher-name]), :column (int 1)}
      :name
      'genkey
      :ns
      *ns*))
  ;; Creates the short-lived continuation-token manager bound to this server address.
  (defn create-token-manager
    ([p__26180]
      (let [map__26181 p__26180
            map__26181 (if (seq? map__26181)
                         (if (next map__26181)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26181))
                           (if (seq map__26181) (first map__26181) {}))
                         map__26181)
            host (get map__26181 :host)
            port (get map__26181 :port)]
        (nt/create-manager {:secret-key (genkey "AES"), :host host, :port port}))))
  (reset-meta!
    #'create-token-manager
    (assoc
      {:arglists (clojure.core/list [{:keys ['host 'port]}]), :column (int 1)}
      :name
      'create-token-manager
      :ns
      *ns*)))
