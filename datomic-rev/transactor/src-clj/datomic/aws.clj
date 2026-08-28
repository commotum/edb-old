(do
  (clojure.core/in-ns 'datomic.aws)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)])
      (clojure.core/require ['datomic.datafy :as 'd])
      (clojure.core/import 'com.amazonaws.auth.AWSCredentials)
      (clojure.core/import 'com.amazonaws.auth.BasicAWSCredentials)
      (clojure.core/import 'com.amazonaws.auth.AWSCredentialsProvider)
      (clojure.core/import 'com.amazonaws.auth.DefaultAWSCredentialsProviderChain)))
  (when-not (.equals 'datomic.aws 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)])
        (clojure.core/require ['datomic.datafy :as 'd])
        (clojure.core/import 'com.amazonaws.auth.AWSCredentials)
        (clojure.core/import 'com.amazonaws.auth.BasicAWSCredentials)
        (clojure.core/import 'com.amazonaws.auth.AWSCredentialsProvider)
        (clojure.core/import 'com.amazonaws.auth.DefaultAWSCredentialsProviderChain))))
  (set! *warn-on-reflection* true)
  (defn aws-access-key-id? ([s] (and (string? s) (= 20 (long (count s))))))
  (defn aws-secret-key? ([s] (and (string? s) (= 40 (long (count s))))))
  (defn credentials? ([m] (and (map? m) (:aws-access-key-id m) (:aws-secret-key m))))
  (defn set-endpoint
    ([client endpoint]
      (.setEndpoint ^com.amazonaws.AmazonWebServiceClient client ^java.lang.String endpoint)
      nil))
  (defn credentials
    ([creds]
      (cond
        (map? creds) (let [map__19510 creds
                           map__19510 (if (seq? map__19510)
                                        (if (next map__19510)
                                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                            (to-array map__19510))
                                          (if (seq map__19510) (first map__19510) {}))
                                        map__19510)
                           aws_access_key_id (get map__19510 :aws-access-key-id)
                           aws_secret_key (get map__19510 :aws-secret-key)]
                       (when-not (and aws_access_key_id aws_secret_key)
                         (throw
                           (java.lang.AssertionError.
                             (str
                               "Assert failed: "
                               (pr-str
                                 (clojure.core/list 'and 'aws-access-key-id 'aws-secret-key))))))
                       (com.amazonaws.auth.BasicAWSCredentials.
                         ^java.lang.String aws_access_key_id
                         ^java.lang.String aws_secret_key))
        creds creds
        :default (do (.getCredentials (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))))))
  (defn endpoint-for
    ([service region]
      (let [sname (name service) rname (name region) G__19513 sname]
        (case G__19513 "iam" "iam.amazonaws.com" (str sname "." rname ".amazonaws.com")))))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.ClientConfiguration]
    fn__19515
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:proxyAuthenticationMethods :localAddress
                                     :responseMetadataCacheSize :validateAfterInactivityMillis
                                     :protocol :socketTimeout :useTcpKeepAlive :proxyWorkstation
                                     :userAgentSuffix :connectionTTL :retryPolicy :maxErrorRetry
                                     :maxConnections :disableHostPrefixInjection :secureRandom
                                     :retryMode :disableSocketProxy :tlsKeyManagersProvider
                                     :connectionMaxIdleMillis :connectionTimeout :useGzip
                                     :requestTimeout :cacheResponseMetadata :useReaper :userAgent
                                     :clientExecutionTimeout :nonProxyHosts
                                     :maxConsecutiveRetriesBeforeThrottling :signerOverride
                                     :useThrottleRetries :proxyProtocol :proxyDomain
                                     :preemptiveBasicProxyAuth :userAgentPrefix :proxyUsername
                                     :proxyHost :useExpectContinue :proxyPassword :proxyPort
                                     :dnsResolver}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:proxyAuthenticationMethods :localAddress :responseMetadataCacheSize
                   :validateAfterInactivityMillis :protocol :socketTimeout :useTcpKeepAlive
                   :proxyWorkstation :userAgentSuffix :connectionTTL :retryPolicy :maxErrorRetry
                   :maxConnections :disableHostPrefixInjection :secureRandom :retryMode
                   :disableSocketProxy :tlsKeyManagersProvider :connectionMaxIdleMillis
                   :connectionTimeout :useGzip :requestTimeout :cacheResponseMetadata :useReaper
                   :userAgent :clientExecutionTimeout :nonProxyHosts
                   :maxConsecutiveRetriesBeforeThrottling :signerOverride :useThrottleRetries
                   :proxyProtocol :proxyDomain :preemptiveBasicProxyAuth :userAgentPrefix
                   :proxyUsername :proxyHost :useExpectContinue :proxyPassword :proxyPort
                   :dnsResolver},
                 :keys bad_ks,
                 :constructor com.amazonaws.ClientConfiguration}))))
        nil)
      (let [o (com.amazonaws.ClientConfiguration.)]
        (when (contains? m :signerOverride)
          (let [v (:signerOverride m)
                k (d/property-to-object (class o) :signerOverride v java.lang.String)]
            (.setSignerOverride ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :maxConnections)
          (let [v (:maxConnections m)
                k (d/property-to-object (class o) :maxConnections v java.lang.Integer/TYPE)]
            (.setMaxConnections ^com.amazonaws.ClientConfiguration o (int ^java.lang.Number k))))
        (when (contains? m :socketTimeout)
          (let [v (:socketTimeout m)
                k (d/property-to-object (class o) :socketTimeout v java.lang.Integer/TYPE)]
            (.setSocketTimeout ^com.amazonaws.ClientConfiguration o (int ^java.lang.Number k))))
        (when (contains? m :responseMetadataCacheSize)
          (let [v (:responseMetadataCacheSize m)
                k (d/property-to-object
                    (class o)
                    :responseMetadataCacheSize
                    v
                    java.lang.Integer/TYPE)]
            (.setResponseMetadataCacheSize
              ^com.amazonaws.ClientConfiguration o
              (int ^java.lang.Number k))))
        (when (contains? m :useReaper)
          (let [v (:useReaper m)
                k (d/property-to-object (class o) :useReaper v java.lang.Boolean/TYPE)]
            (.setUseReaper
              ^com.amazonaws.ClientConfiguration o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :connectionMaxIdleMillis)
          (let [v (:connectionMaxIdleMillis m)
                k (d/property-to-object (class o) :connectionMaxIdleMillis v java.lang.Long/TYPE)]
            (.setConnectionMaxIdleMillis
              ^com.amazonaws.ClientConfiguration o
              (long ^java.lang.Number k))))
        (when (contains? m :proxyDomain)
          (let [v (:proxyDomain m)
                k (d/property-to-object (class o) :proxyDomain v java.lang.String)]
            (.setProxyDomain ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :proxyWorkstation)
          (let [v (:proxyWorkstation m)
                k (d/property-to-object (class o) :proxyWorkstation v java.lang.String)]
            (.setProxyWorkstation ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :validateAfterInactivityMillis)
          (let [v (:validateAfterInactivityMillis m)
                k (d/property-to-object
                    (class o)
                    :validateAfterInactivityMillis
                    v
                    java.lang.Integer/TYPE)]
            (.setValidateAfterInactivityMillis
              ^com.amazonaws.ClientConfiguration o
              (int ^java.lang.Number k))))
        (when (contains? m :maxConsecutiveRetriesBeforeThrottling)
          (let [v (:maxConsecutiveRetriesBeforeThrottling m)
                k (d/property-to-object
                    (class o)
                    :maxConsecutiveRetriesBeforeThrottling
                    v
                    java.lang.Integer/TYPE)]
            (.setMaxConsecutiveRetriesBeforeThrottling
              ^com.amazonaws.ClientConfiguration o
              (int ^java.lang.Number k))))
        (when (contains? m :proxyUsername)
          (let [v (:proxyUsername m)
                k (d/property-to-object (class o) :proxyUsername v java.lang.String)]
            (.setProxyUsername ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :useTcpKeepAlive)
          (let [v (:useTcpKeepAlive m)
                k (d/property-to-object (class o) :useTcpKeepAlive v java.lang.Boolean/TYPE)]
            (.setUseTcpKeepAlive
              ^com.amazonaws.ClientConfiguration o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :userAgentPrefix)
          (let [v (:userAgentPrefix m)
                k (d/property-to-object (class o) :userAgentPrefix v java.lang.String)]
            (.setUserAgentPrefix ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :cacheResponseMetadata)
          (let [v (:cacheResponseMetadata m)
                k (d/property-to-object (class o) :cacheResponseMetadata v java.lang.Boolean/TYPE)]
            (.setCacheResponseMetadata
              ^com.amazonaws.ClientConfiguration o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :retryPolicy)
          (let [v (:retryPolicy m)
                k (d/property-to-object (class o) :retryPolicy v com.amazonaws.retry.RetryPolicy)]
            (.setRetryPolicy
              ^com.amazonaws.ClientConfiguration o
              ^com.amazonaws.retry.RetryPolicy k)))
        (when (contains? m :proxyPassword)
          (let [v (:proxyPassword m)
                k (d/property-to-object (class o) :proxyPassword v java.lang.String)]
            (.setProxyPassword ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :proxyAuthenticationMethods)
          (let [v (:proxyAuthenticationMethods m)
                k (d/property-to-object (class o) :proxyAuthenticationMethods v java.util.List)]
            (.setProxyAuthenticationMethods
              ^com.amazonaws.ClientConfiguration o
              ^java.util.List k)))
        (when (contains? m :connectionTimeout)
          (let [v (:connectionTimeout m)
                k (d/property-to-object (class o) :connectionTimeout v java.lang.Integer/TYPE)]
            (.setConnectionTimeout
              ^com.amazonaws.ClientConfiguration o
              (int ^java.lang.Number k))))
        (when (contains? m :tlsKeyManagersProvider)
          (let [v (:tlsKeyManagersProvider m)
                k (d/property-to-object
                    (class o)
                    :tlsKeyManagersProvider
                    v
                    com.amazonaws.http.TlsKeyManagersProvider)]
            (.setTlsKeyManagersProvider
              ^com.amazonaws.ClientConfiguration o
              ^com.amazonaws.http.TlsKeyManagersProvider k)))
        (when (contains? m :proxyPort)
          (let [v (:proxyPort m)
                k (d/property-to-object (class o) :proxyPort v java.lang.Integer/TYPE)]
            (.setProxyPort ^com.amazonaws.ClientConfiguration o (int ^java.lang.Number k))))
        (when (contains? m :disableHostPrefixInjection)
          (let [v (:disableHostPrefixInjection m)
                k (d/property-to-object
                    (class o)
                    :disableHostPrefixInjection
                    v
                    java.lang.Boolean/TYPE)]
            (.setDisableHostPrefixInjection
              ^com.amazonaws.ClientConfiguration o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :userAgent)
          (let [v (:userAgent m) k (d/property-to-object (class o) :userAgent v java.lang.String)]
            (.setUserAgent ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :nonProxyHosts)
          (let [v (:nonProxyHosts m)
                k (d/property-to-object (class o) :nonProxyHosts v java.lang.String)]
            (.setNonProxyHosts ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :useThrottleRetries)
          (let [v (:useThrottleRetries m)
                k (d/property-to-object (class o) :useThrottleRetries v java.lang.Boolean/TYPE)]
            (.setUseThrottleRetries
              ^com.amazonaws.ClientConfiguration o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :requestTimeout)
          (let [v (:requestTimeout m)
                k (d/property-to-object (class o) :requestTimeout v java.lang.Integer/TYPE)]
            (.setRequestTimeout ^com.amazonaws.ClientConfiguration o (int ^java.lang.Number k))))
        (when (contains? m :secureRandom)
          (let [v (:secureRandom m)
                k (d/property-to-object (class o) :secureRandom v java.security.SecureRandom)]
            (.setSecureRandom ^com.amazonaws.ClientConfiguration o ^java.security.SecureRandom k)))
        (when (contains? m :userAgentSuffix)
          (let [v (:userAgentSuffix m)
                k (d/property-to-object (class o) :userAgentSuffix v java.lang.String)]
            (.setUserAgentSuffix ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :proxyHost)
          (let [v (:proxyHost m) k (d/property-to-object (class o) :proxyHost v java.lang.String)]
            (.setProxyHost ^com.amazonaws.ClientConfiguration o ^java.lang.String k)))
        (when (contains? m :maxErrorRetry)
          (let [v (:maxErrorRetry m)
                k (d/property-to-object (class o) :maxErrorRetry v java.lang.Integer/TYPE)]
            (.setMaxErrorRetry ^com.amazonaws.ClientConfiguration o (int ^java.lang.Number k))))
        (when (contains? m :disableSocketProxy)
          (let [v (:disableSocketProxy m)
                k (d/property-to-object (class o) :disableSocketProxy v java.lang.Boolean/TYPE)]
            (.setDisableSocketProxy
              ^com.amazonaws.ClientConfiguration o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :preemptiveBasicProxyAuth)
          (let [v (:preemptiveBasicProxyAuth m)
                k (d/property-to-object (class o) :preemptiveBasicProxyAuth v java.lang.Boolean)]
            (.setPreemptiveBasicProxyAuth
              ^com.amazonaws.ClientConfiguration o
              ^java.lang.Boolean k)))
        (when (contains? m :clientExecutionTimeout)
          (let [v (:clientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :clientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setClientExecutionTimeout
              ^com.amazonaws.ClientConfiguration o
              (int ^java.lang.Number k))))
        (when (contains? m :useExpectContinue)
          (let [v (:useExpectContinue m)
                k (d/property-to-object (class o) :useExpectContinue v java.lang.Boolean/TYPE)]
            (.setUseExpectContinue
              ^com.amazonaws.ClientConfiguration o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :protocol)
          (let [v (:protocol m)
                k (d/property-to-object (class o) :protocol v com.amazonaws.Protocol)]
            (.setProtocol ^com.amazonaws.ClientConfiguration o ^com.amazonaws.Protocol k)))
        (when (contains? m :localAddress)
          (let [v (:localAddress m)
                k (d/property-to-object (class o) :localAddress v java.net.InetAddress)]
            (.setLocalAddress ^com.amazonaws.ClientConfiguration o ^java.net.InetAddress k)))
        (when (contains? m :retryMode)
          (let [v (:retryMode m)
                k (d/property-to-object (class o) :retryMode v com.amazonaws.retry.RetryMode)]
            (.setRetryMode ^com.amazonaws.ClientConfiguration o ^com.amazonaws.retry.RetryMode k)))
        (when (contains? m :dnsResolver)
          (let [v (:dnsResolver m)
                k (d/property-to-object (class o) :dnsResolver v com.amazonaws.DnsResolver)]
            (.setDnsResolver ^com.amazonaws.ClientConfiguration o ^com.amazonaws.DnsResolver k)))
        (when (contains? m :proxyProtocol)
          (let [v (:proxyProtocol m)
                k (d/property-to-object (class o) :proxyProtocol v com.amazonaws.Protocol)]
            (.setProxyProtocol ^com.amazonaws.ClientConfiguration o ^com.amazonaws.Protocol k)))
        (when (contains? m :useGzip)
          (let [v (:useGzip m)
                k (d/property-to-object (class o) :useGzip v java.lang.Boolean/TYPE)]
            (.setUseGzip
              ^com.amazonaws.ClientConfiguration o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :connectionTTL)
          (let [v (:connectionTTL m)
                k (d/property-to-object (class o) :connectionTTL v java.lang.Long/TYPE)]
            (.setConnectionTTL ^com.amazonaws.ClientConfiguration o (long ^java.lang.Number k))))
        o)))
  (defn newclient
    ([&form &env cls creds conf]
      (seq
        (concat
          (clojure.core/list 'if)
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'clojure.core/instance?)
                (clojure.core/list 'com.amazonaws.auth.AWSCredentialsProvider)
                (clojure.core/list creds))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'new)
                (clojure.core/list cls)
                (clojure.core/list
                  (with-meta creds {:tag 'com.amazonaws.auth.AWSCredentialsProvider}))
                (clojure.core/list (with-meta conf {:tag 'com.amazonaws.ClientConfiguration})))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'new)
                (clojure.core/list cls)
                (clojure.core/list (with-meta creds {:tag 'com.amazonaws.auth.AWSCredentials}))
                (clojure.core/list
                  (with-meta conf {:tag 'com.amazonaws.ClientConfiguration}))))))))
    ([&form &env cls creds]
      (seq
        (concat
          (clojure.core/list 'if)
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'clojure.core/instance?)
                (clojure.core/list 'com.amazonaws.auth.AWSCredentialsProvider)
                (clojure.core/list creds))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'new)
                (clojure.core/list cls)
                (clojure.core/list
                  (with-meta creds {:tag 'com.amazonaws.auth.AWSCredentialsProvider})))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'new)
                (clojure.core/list cls)
                (clojure.core/list
                  (with-meta creds {:tag 'com.amazonaws.auth.AWSCredentials})))))))))
  (.setMacro #'newclient)
  (defn defclient
    ([&form &env cls service]
      (let [docstr (str
                     "Create a client. Config options are:\n\n:region    String or Keyword\n:override-endpoint String (Optional, to override the default AWS endpoint).\n\nPlus any of the following:\n"
                     (let [s__6419__auto__ (java.io.StringWriter.)]
                       (binding [*out* s__6419__auto__]
                         (do
                           (push-thread-bindings (hash-map #'*print-length* nil))
                           (try
                             (clojure.pprint/pprint
                               (d/type-descriptor com.amazonaws.ClientConfiguration))
                             (finally (pop-thread-bindings)))
                           (str s__6419__auto__)))))
            ns (resolve cls)
            tag (symbol (.getName ^java.lang.Class ns))]
        (seq
          (concat
            (clojure.core/list 'clojure.core/defn)
            (clojure.core/list 'client)
            (clojure.core/list docstr)
            (-> (with-meta [] {:tag tag})
             (clojure.core/list)
             (clojure.core/list 'datomic.aws/newclient)
             (concat
               (clojure.core/list cls)
               (clojure.core/list
                 (seq
                   (concat
                     (clojure.core/list
                       'com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
             (seq)
             (clojure.core/list)
             (concat)
             (seq)
             (clojure.core/list))
            (-> (with-meta ['creds] {:tag tag})
             (clojure.core/list)
             (clojure.core/list 'if)
             (concat
               (clojure.core/list 'creds)
               (clojure.core/list
                 (seq
                   (concat
                     (clojure.core/list 'datomic.aws/newclient)
                     (clojure.core/list cls)
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'datomic.aws/credentials)
                           (clojure.core/list 'creds)))))))
               (clojure.core/list
                 (seq
                   (concat
                     (clojure.core/list 'datomic.aws/newclient)
                     (clojure.core/list cls)
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list
                             'com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))))))))
             (seq)
             (clojure.core/list)
             (concat)
             (seq)
             (clojure.core/list))
            (-> (with-meta ['creds 'config] {:tag tag})
             (clojure.core/list)
             (clojure.core/list 'if)
             (concat
               (clojure.core/list 'config)
               (clojure.core/list
                 (seq
                   (concat
                     (clojure.core/list 'clojure.core/let)
                     (clojure.core/list
                       (apply
                         vector
                         (seq
                           (concat
                             (clojure.core/list 'region)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'clojure.core/get)
                                   (clojure.core/list 'config)
                                   (clojure.core/list :region))))
                             (clojure.core/list 'override-endpoint)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'clojure.core/get)
                                   (clojure.core/list 'config)
                                   (clojure.core/list :override-endpoint))))
                             (clojure.core/list 'conf)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'datomic.datafy/data-to-object)
                                   (clojure.core/list
                                     (seq
                                       (concat
                                         (clojure.core/list 'clojure.core/dissoc)
                                         (clojure.core/list 'config)
                                         (clojure.core/list :region)
                                         (clojure.core/list :override-endpoint))))
                                   (clojure.core/list 'com.amazonaws.ClientConfiguration))))
                             (clojure.core/list 'conn)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'if)
                                   (clojure.core/list 'creds)
                                   (clojure.core/list
                                     (seq
                                       (concat
                                         (clojure.core/list 'datomic.aws/newclient)
                                         (clojure.core/list cls)
                                         (clojure.core/list
                                           (seq
                                             (concat
                                               (clojure.core/list 'datomic.aws/credentials)
                                               (clojure.core/list 'creds))))
                                         (clojure.core/list 'conf))))
                                   (clojure.core/list
                                     (seq
                                       (concat
                                         (clojure.core/list 'datomic.aws/newclient)
                                         (clojure.core/list cls)
                                         (clojure.core/list
                                           (seq
                                             (concat
                                               (clojure.core/list
                                                 'com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))))
                                         (clojure.core/list 'conf)))))))))))
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'clojure.core/cond)
                           (clojure.core/list 'override-endpoint)
                           (clojure.core/list
                             (seq
                               (concat
                                 (clojure.core/list '.setEndpoint)
                                 (clojure.core/list 'conn)
                                 (clojure.core/list
                                   (seq
                                     (concat
                                       (clojure.core/list 'clojure.core/str)
                                       (clojure.core/list "http://")
                                       (clojure.core/list 'override-endpoint)))))))
                           (clojure.core/list 'region)
                           (clojure.core/list
                             (seq
                               (concat
                                 (clojure.core/list '.setEndpoint)
                                 (clojure.core/list 'conn)
                                 (clojure.core/list
                                   (seq
                                     (concat
                                       (clojure.core/list 'datomic.aws/endpoint-for)
                                       (clojure.core/list service)
                                       (clojure.core/list 'region))))))))))
                     (clojure.core/list 'conn))))
               (clojure.core/list
                 (seq (concat (clojure.core/list 'client) (clojure.core/list 'creds)))))
             (seq)
             (clojure.core/list)
             (concat)
             (seq)
             (clojure.core/list)))))))
  (.setMacro #'defclient)
  (defn client-config ([args] (d/data-to-object args com.amazonaws.ClientConfiguration))))