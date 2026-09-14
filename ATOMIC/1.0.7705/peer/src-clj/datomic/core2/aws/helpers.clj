(do
  (clojure.core/in-ns 'datomic.core2.aws.helpers)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.aws.client.api :as 'aws]
        ['datomic.core2.anomalies :as 'canom])
      (clojure.core/import 'java.net.URI)
      (clojure.core/import 'java.time.Duration)
      (clojure.core/import 'software.amazon.awssdk.auth.credentials.AwsBasicCredentials)
      (clojure.core/import 'software.amazon.awssdk.auth.credentials.StaticCredentialsProvider)
      (clojure.core/import 'software.amazon.awssdk.auth.credentials.AwsCredentialsProvider)
      (clojure.core/import 'software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider)
      (clojure.core/import 'software.amazon.awssdk.awscore.client.builder.AwsClientBuilder)
      (clojure.core/import 'software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder)
      (clojure.core/import 'software.amazon.awssdk.core.client.config.ClientOverrideConfiguration)
      (clojure.core/import 'software.amazon.awssdk.http.apache.ApacheHttpClient)
      (clojure.core/import 'software.amazon.awssdk.metrics.LoggingMetricPublisher)
      (clojure.core/import 'software.amazon.awssdk.regions.Region)
      (clojure.core/import 'software.amazon.awssdk.retries.StandardRetryStrategy)
      (clojure.core/import 'software.amazon.awssdk.retries.api.RetryStrategy)))
  (when-not (.equals 'datomic.core2.aws.helpers 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.aws.helpers))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.aws.client.api :as 'aws]
          ['datomic.core2.anomalies :as 'canom])
        (clojure.core/import 'java.net.URI)
        (clojure.core/import 'java.time.Duration)
        (clojure.core/import 'software.amazon.awssdk.auth.credentials.AwsBasicCredentials)
        (clojure.core/import 'software.amazon.awssdk.auth.credentials.StaticCredentialsProvider)
        (clojure.core/import 'software.amazon.awssdk.auth.credentials.AwsCredentialsProvider)
        (clojure.core/import 'software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider)
        (clojure.core/import 'software.amazon.awssdk.awscore.client.builder.AwsClientBuilder)
        (clojure.core/import 'software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder)
        (clojure.core/import
          'software.amazon.awssdk.core.client.config.ClientOverrideConfiguration)
        (clojure.core/import 'software.amazon.awssdk.http.apache.ApacheHttpClient)
        (clojure.core/import 'software.amazon.awssdk.metrics.LoggingMetricPublisher)
        (clojure.core/import 'software.amazon.awssdk.regions.Region)
        (clojure.core/import 'software.amazon.awssdk.retries.StandardRetryStrategy)
        (clojure.core/import 'software.amazon.awssdk.retries.api.RetryStrategy))))
  (set! *warn-on-reflection* true)
  (defn static-credentials-provider
    ([p__21274]
      (let [map__21275 p__21274
            map__21275 (if (seq? map__21275)
                         (if (next map__21275)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21275))
                           (if (seq map__21275) (first map__21275) {}))
                         map__21275)
            aws_access_key_id (get map__21275 :aws-access-key-id)
            aws_secret_access_key (get map__21275 :aws-secret-access-key)
            aws_secret_key (get map__21275 :aws-secret-key)
            secret_key (or aws_secret_access_key aws_secret_key)
            credentials (AwsBasicCredentials/create
                          ^java.lang.String aws_access_key_id
                          ^java.lang.String secret_key)]
        (StaticCredentialsProvider/create
          ^software.amazon.awssdk.auth.credentials.AwsCredentials credentials))))
  (reset-meta!
    #'static-credentials-provider
    (assoc
      {:arglists
       (clojure.core/list [{:keys ['aws-access-key-id 'aws-secret-access-key 'aws-secret-key]}]),
       :column (int 1)}
      :name
      'static-credentials-provider
      :ns
      *ns*))
  (defn standard-retry-n ([n] (.build (.maxAttempts (StandardRetryStrategy/builder) (int n)))))
  (reset-meta!
    #'standard-retry-n
    (assoc {:arglists (clojure.core/list ['n]), :column (int 1)} :name 'standard-retry-n :ns *ns*))
  (defn sync-http-builder
    ([p__21279]
      (let [map__21280 p__21279
            map__21280 (if (seq? map__21280)
                         (if (next map__21280)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21280))
                           (if (seq map__21280) (first map__21280) {}))
                         map__21280)
            maxConnections (get map__21280 :maxConnections)
            connectionTimeout (get map__21280 :connectionTimeout)
            socketTimeout (get map__21280 :socketTimeout)]
        (cond->
          (ApacheHttpClient/builder)
          maxConnections
          (.maxConnections (java.lang.Integer/valueOf (int maxConnections)))
          socketTimeout
          (.socketTimeout (Duration/ofMillis (long ^java.lang.Number socketTimeout)))
          connectionTimeout
          (.connectionTimeout (Duration/ofMillis (long ^java.lang.Number connectionTimeout)))))))
  (reset-meta!
    #'sync-http-builder
    (assoc
      {:arglists (clojure.core/list [{:keys ['maxConnections 'connectionTimeout 'socketTimeout]}]),
       :column (int 1)}
      :name
      'sync-http-builder
      :ns
      *ns*))
  (defn configure-overrides
    ([p__21283]
      (let [map__21284 p__21283
            map__21284 (if (seq? map__21284)
                         (if (next map__21284)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21284))
                           (if (seq map__21284) (first map__21284) {}))
                         map__21284)
            maxAttempts (get map__21284 :maxAttempts)
            clientExecutionTimeout (get map__21284 :clientExecutionTimeout)
            requestTimeout (get map__21284 :requestTimeout)]
        (cond->
          (ClientOverrideConfiguration/builder)
          maxAttempts
          (.retryStrategy (standard-retry-n maxAttempts))
          clientExecutionTimeout
          (.apiCallTimeout (Duration/ofMillis (long ^java.lang.Number clientExecutionTimeout)))
          requestTimeout
          (.apiCallAttemptTimeout (Duration/ofMillis (long ^java.lang.Number requestTimeout)))
          true
          (.addMetricPublisher (LoggingMetricPublisher/create))
          true
          (.build)))))
  (reset-meta!
    #'configure-overrides
    (assoc
      {:arglists
       (clojure.core/list [{:keys ['maxAttempts 'clientExecutionTimeout 'requestTimeout]}]),
       :column (int 1)}
      :name
      'configure-overrides
      :ns
      *ns*))
  (defn sync-client
    ([builder creds p__21287]
      (let [map__21288 p__21287
            map__21288 (if (seq? map__21288)
                         (if (next map__21288)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21288))
                           (if (seq map__21288) (first map__21288) {}))
                         map__21288)
            opts map__21288
            region (get map__21288 :region)
            override_endpoint (get map__21288 :override-endpoint)
            http_builder (sync-http-builder opts)
            override_conf (configure-overrides opts)]
        (.build
          (cond->
            (.overrideConfiguration
              (cond->
                (.credentialsProvider
                  creds
                  (if (instance? clojure.lang.IFn creds)
                    (instance?
                      software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
                      creds)
                    creds))
                region
                (.region (Region/of ^java.lang.String region)))
              ^software.amazon.awssdk.core.client.config.ClientOverrideConfiguration override_conf)
            override_endpoint
            (.endpointOverride (URI/create ^java.lang.String override_endpoint))))))
    ([builder opts] (sync-client builder (DefaultCredentialsProvider/create) opts)))
  (reset-meta!
    #'sync-client
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'builder {:tag 'AwsSyncClientBuilder}) 'opts]
         [(.withMeta 'builder {:tag 'AwsSyncClientBuilder})
          (.withMeta 'creds {:tag 'AwsCredentialsProvider})
          {:keys ['region 'override-endpoint], :as 'opts}]),
       :column (int 1)}
      :name
      'sync-client
      :ns
      *ns*))
  (defn parse-arn
    ([arn]
      (clojure.core/import 'software.amazon.awssdk.arns.Arn)
      (let [arn (Arn/fromString ^java.lang.String arn)]
        {:account-id (.get (.accountId ^software.amazon.awssdk.arns.Arn arn)),
         :partition (.partition ^software.amazon.awssdk.arns.Arn arn),
         :region (.get (.region ^software.amazon.awssdk.arns.Arn arn)),
         :service (.service ^software.amazon.awssdk.arns.Arn arn),
         :resource (.resourceAsString ^software.amazon.awssdk.arns.Arn arn)})))
  (reset-meta!
    #'parse-arn
    (assoc {:arglists (clojure.core/list ['arn]), :column (int 1)} :name 'parse-arn :ns *ns*))
  (defn invoke
    ([client op_map]
      (let [ret (aws/invoke client op_map)] (if (not (canom/ok? ret)) (canom/athrow ret) ret))))
  (reset-meta!
    #'invoke
    (assoc
      {:arglists (clojure.core/list ['client 'op-map]), :column (int 1)}
      :name
      'invoke
      :ns
      *ns*)))