(do
  (clojure.core/in-ns 'datomic.core2.aws.s3.sdkv1)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.core2.anomalizer :as 'izer] ['datomic.java.io :as 'dio])
      (clojure.core/import 'com.amazonaws.services.s3.model.S3Object)
      (clojure.core/import 'com.amazonaws.services.s3.model.AmazonS3Exception)
      (clojure.core/import 'com.amazonaws.services.s3.AmazonS3Client)
      (clojure.core/import 'com.amazonaws.ClientConfiguration)
      (clojure.core/import 'com.amazonaws.retry.PredefinedRetryPolicies)
      (clojure.core/import 'com.amazonaws.retry.RetryPolicy)
      (clojure.core/import 'com.amazonaws.auth.DefaultAWSCredentialsProviderChain)))
  (when-not (.equals 'datomic.core2.aws.s3.sdkv1 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.aws.s3.sdkv1))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.core2.anomalizer :as 'izer] ['datomic.java.io :as 'dio])
        (clojure.core/import 'com.amazonaws.services.s3.model.S3Object)
        (clojure.core/import 'com.amazonaws.services.s3.model.AmazonS3Exception)
        (clojure.core/import 'com.amazonaws.services.s3.AmazonS3Client)
        (clojure.core/import 'com.amazonaws.ClientConfiguration)
        (clojure.core/import 'com.amazonaws.retry.PredefinedRetryPolicies)
        (clojure.core/import 'com.amazonaws.retry.RetryPolicy)
        (clojure.core/import 'com.amazonaws.auth.DefaultAWSCredentialsProviderChain))))
  (set! *warn-on-reflection* true)
  (def client
   (fn client
     ([p__21374]
       (let [map__21375 p__21374
             map__21375 (if (seq? map__21375)
                          (if (next map__21375)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21375))
                            (if (seq map__21375) (first map__21375) {}))
                          map__21375)
             region (get map__21375 :region)
             retryPolicy (get map__21375 :retryPolicy)
             client_conf (get map__21375 :client-conf (com.amazonaws.ClientConfiguration.))
             creds_provider (get
                              map__21375
                              :creds-provider
                              (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))]
         (when retryPolicy
           (.setRetryPolicy
             ^com.amazonaws.ClientConfiguration client_conf
             ^com.amazonaws.retry.RetryPolicy retryPolicy))
         (.build
           (.withRegion
             (.withCredentials
               (.withClientConfiguration
                 (AmazonS3Client/builder)
                 ^com.amazonaws.ClientConfiguration client_conf)
               ^com.amazonaws.auth.AWSCredentialsProvider creds_provider)
             (str region)))))))
  (reset-meta!
    #'client
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys
           ['region
            (.withMeta 'retryPolicy {:tag 'RetryPolicy})
            (.withMeta 'client-conf {:tag 'ClientConfiguration})
            'creds-provider],
           :or
           {'client-conf (.withMeta (clojure.core/list 'ClientConfiguration.) {:column (int 33)}),
            'creds-provider
            (.withMeta
              (clojure.core/list 'DefaultAWSCredentialsProviderChain.)
              {:column (int 27)})}}]),
       :column (int 1)}
      :name
      'client
      :ns
      *ns*))
  (defn s3-service
    ([args] (client (assoc args :retryPolicy PredefinedRetryPolicies/NO_RETRY_POLICY))))
  (reset-meta!
    #'s3-service
    (assoc {:arglists (clojure.core/list ['args]), :column (int 1)} :name 's3-service :ns *ns*))
  (defn s3-service-with-default-retry ([args] (client args)))
  (reset-meta!
    #'s3-service-with-default-retry
    (assoc
      {:arglists (clojure.core/list ['args]), :column (int 1)}
      :name
      's3-service-with-default-retry
      :ns
      *ns*))
  (def wrap-ex-handler
   (fn wrap_ex_handler
     ([f context]
       (fn fn__21379
         ([& args]
           (try
             (apply f args)
             (catch java.lang.Throwable t (merge context (izer/throwable->anom t)))))))
     ([f] (wrap-ex-handler f nil))))
  (reset-meta!
    #'wrap-ex-handler
    (assoc
      {:arglists (clojure.core/list ['f] ['f 'context]), :column (int 1)}
      :name
      'wrap-ex-handler
      :ns
      *ns*))
  (def put-object
   (fn put_object
     ([s3 bucket path stream metadata]
       (.putObject
         ^com.amazonaws.services.s3.AmazonS3Client s3
         ^java.lang.String bucket
         ^java.lang.String path
         ^java.io.InputStream stream
         ^com.amazonaws.services.s3.model.ObjectMetadata metadata))))
  (reset-meta!
    #'put-object
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 's3 {:tag 'AmazonS3Client})
          (.withMeta 'bucket {:tag 'String})
          (.withMeta 'path {:tag 'String})
          'stream
          'metadata]),
       :column (int 1)}
      :name
      'put-object
      :ns
      *ns*))
  (def get-object
   (fn get_object
     ([s3 bucket path]
       (try
         (.getObject
           ^com.amazonaws.services.s3.AmazonS3Client s3
           ^java.lang.String bucket
           ^java.lang.String path)
         (catch
           com.amazonaws.services.s3.model.AmazonS3Exception
           se
           (when-not (= 404 (.getStatusCode ^com.amazonaws.AmazonServiceException se))
             (throw ^java.lang.Throwable se)
             nil))))))
  (reset-meta!
    #'get-object
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 's3 {:tag 'AmazonS3Client})
          (.withMeta 'bucket {:tag 'String})
          (.withMeta 'path {:tag 'String})]),
       :column (int 1)}
      :name
      'get-object
      :ns
      *ns*))
  (def get-bytes
   (fn get_bytes
     ([s3 bucket path]
       (let [temp__5825__auto__ (get-object s3 bucket path)]
         (when temp__5825__auto__
           (let [obj temp__5825__auto__]
             (with-open [is (.getObjectContent ^com.amazonaws.services.s3.model.S3Object obj)]
               (let [ba (byte-array
                          (long
                            (.getContentLength
                              (.getObjectMetadata
                                ^com.amazonaws.services.s3.model.S3Object obj))))]
                 (dio/fill-from-stream! ba is)))))))))
  (reset-meta!
    #'get-bytes
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 's3 {:tag 'AmazonS3Client})
          (.withMeta 'bucket {:tag 'String})
          (.withMeta 'path {:tag 'String})]),
       :column (int 1)}
      :name
      'get-bytes
      :ns
      *ns*))
  (def delete-object
   (fn delete_object
     ([s3 bucket path]
       (.deleteObject
         ^com.amazonaws.services.s3.AmazonS3Client s3
         ^java.lang.String bucket
         ^java.lang.String path)
       nil)))
  (reset-meta!
    #'delete-object
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 's3 {:tag 'AmazonS3Client})
          (.withMeta 'bucket {:tag 'String})
          (.withMeta 'path {:tag 'String})]),
       :column (int 1)}
      :name
      'delete-object
      :ns
      *ns*)))