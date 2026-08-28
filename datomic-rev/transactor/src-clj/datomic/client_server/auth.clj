(do
  (clojure.core/in-ns 'datomic.client-server.auth)
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
        (let [G__24131 (KeyStore/getInstance (KeyStore/getDefaultType))]
          (.load ^java.security.KeyStore G__24131 ^java.io.InputStream is ^chars pwd)
          G__24131))))
  (reset-meta!
    #'load-keystore
    (assoc
      {:private true, :arglists (clojure.core/list ['resource 'pwd]), :column 1}
      :name
      'load-keystore
      :ns
      *ns*))
  (def ssl-config-ref
   (delay
     {:keystore
      (load-keystore (io/resource "datomic/transactor-key.jks") (.toCharArray "transactor")),
      :key-password "transactor"}))
  (reset-meta! #'ssl-config-ref (assoc {:private true, :column 1} :name 'ssl-config-ref :ns *ns*))
  (defn ssl-config ([] (deref ssl-config-ref)))
  (defn callback*
    ([access_key_>secret req]
      (let [temp__5802__auto__ (some->
                                 req
                                 (get-in [:headers "authorization"])
                                 (hmac/parse-authorization-header)
                                 (:access-key-id))]
        (if temp__5802__auto__
          (let [akid temp__5802__auto__
                verify_params (merge
                                {:service "peer-server",
                                 :region "none",
                                 :access-key-id akid,
                                 :secret (get access_key_>secret akid)})
                temp__5802__auto__ (hmac/verify-failure req verify_params)]
            (if temp__5802__auto__
              (let [failure temp__5802__auto__]
                #:cognitect.anomalies{:category :cognitect.anomalies/forbidden,
                                      :message (str failure)})
              req))
          #:cognitect.anomalies{:category :cognitect.anomalies/forbidden,
                                :message "Bad authorization header"}))))
  (reset-meta!
    #'callback*
    (assoc
      {:private true, :arglists (clojure.core/list ['access-key->secret 'req]), :column 1}
      :name
      'callback*
      :ns
      *ns*))
  (defn callback
    ([access_key_>secret req]
      (let [result (callback* access_key_>secret req)]
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
  (defn genkey
    ([cipher_name]
      (let [keygen (javax.crypto.KeyGenerator/getInstance ^java.lang.String cipher_name)]
        (.init ^javax.crypto.KeyGenerator keygen (int 128))
        (.generateKey ^javax.crypto.KeyGenerator keygen))))
  (reset-meta!
    #'genkey
    (assoc
      {:private true, :arglists (clojure.core/list ['cipher-name]), :column 1}
      :name
      'genkey
      :ns
      *ns*))
  (defn create-token-manager
    ([p__24142]
      (let [map__24143 p__24142
            map__24143 (if (seq? map__24143)
                         (if (next map__24143)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24143))
                           (if (seq map__24143) (first map__24143) {}))
                         map__24143)
            host (get map__24143 :host)
            port (get map__24143 :port)]
        (nt/create-manager {:secret-key (genkey "AES"), :host host, :port port})))))