(do
  (clojure.core/in-ns 'datomic.core2.aws.s3.sdkv1)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['cognitect.anomalies :as 'anom] ['datomic.java.io :as 'dio])
      (clojure.core/import 'com.amazonaws.services.s3.model.S3Object)
      (clojure.core/import 'com.amazonaws.services.s3.model.AmazonS3Exception)
      (clojure.core/import 'com.amazonaws.services.s3.AmazonS3Client)
      (clojure.core/import 'com.amazonaws.AmazonServiceException)
      (clojure.core/import 'com.amazonaws.ClientConfiguration)
      (clojure.core/import 'com.amazonaws.retry.PredefinedRetryPolicies)
      (clojure.core/import 'com.amazonaws.retry.RetryPolicy)
      (clojure.core/import 'com.amazonaws.auth.DefaultAWSCredentialsProviderChain)))
  (when-not (.equals 'datomic.core2.aws.s3.sdkv1 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.aws.s3.sdkv1))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['cognitect.anomalies :as 'anom] ['datomic.java.io :as 'dio])
        (clojure.core/import 'com.amazonaws.services.s3.model.S3Object)
        (clojure.core/import 'com.amazonaws.services.s3.model.AmazonS3Exception)
        (clojure.core/import 'com.amazonaws.services.s3.AmazonS3Client)
        (clojure.core/import 'com.amazonaws.AmazonServiceException)
        (clojure.core/import 'com.amazonaws.ClientConfiguration)
        (clojure.core/import 'com.amazonaws.retry.PredefinedRetryPolicies)
        (clojure.core/import 'com.amazonaws.retry.RetryPolicy)
        (clojure.core/import 'com.amazonaws.auth.DefaultAWSCredentialsProviderChain))))
  (set! *warn-on-reflection* true)
  (defn client
    ([p__20526]
      (let [map__20527 p__20526
            map__20527 (if (seq? map__20527)
                         (if (next map__20527)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20527))
                           (if (seq map__20527) (first map__20527) {}))
                         map__20527)
            region (get map__20527 :region)
            retryPolicy (get map__20527 :retryPolicy)
            client_conf (get map__20527 :client-conf (com.amazonaws.ClientConfiguration.))
            creds_provider (get
                             map__20527
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
            (str region))))))
  (defn s3-service
    ([args] (client (assoc args :retryPolicy PredefinedRetryPolicies/NO_RETRY_POLICY))))
  (defn s3-service-with-default-retry ([args] (client args)))
  (defn throwable-category
    ([t]
      (cond
        (instance? com.amazonaws.AmazonServiceException t) (let 
                                                             [se t]
                                                             (cond
                                                               (=
                                                                 404
                                                                 (.getStatusCode
                                                                   ^com.amazonaws.AmazonServiceException se))
                                                               :cognitect.anomalies/not-found
                                                               (=
                                                                 403
                                                                 (.getStatusCode
                                                                   ^com.amazonaws.AmazonServiceException se))
                                                               :cognitect.anomalies/forbidden
                                                               :default
                                                               (do :cognitect.anomalies/fault)))
        :default (do :cognitect.anomalies/fault))))
  (defn throwable->anom
    ([t]
      (let [cat (throwable-category t)]
        {:cognitect.anomalies/category cat,
         :cognitect.anomalies/message (.getMessage ^java.lang.Throwable t),
         :error (.getMessage ^java.lang.Throwable t)})))
  (defn wrap-ex-handler
    ([f context]
      (fn fn__20533
        ([& args]
          (try (apply f args) (catch java.lang.Throwable t (merge context (throwable->anom t)))))))
    ([f] (wrap-ex-handler f nil)))
  (defn put-object
    ([s3 bucket path stream metadata]
      (.putObject
        ^com.amazonaws.services.s3.AmazonS3Client s3
        ^java.lang.String bucket
        ^java.lang.String path
        ^java.io.InputStream stream
        ^com.amazonaws.services.s3.model.ObjectMetadata metadata)))
  (defn get-object
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
            nil)))))
  (defn get-bytes
    ([s3 bucket path]
      (let [temp__5804__auto__ (get-object s3 bucket path)]
        (when temp__5804__auto__
          (let [obj temp__5804__auto__]
            (with-open [is (.getObjectContent ^com.amazonaws.services.s3.model.S3Object obj)]
              (let [ba (byte-array
                         (long
                           (.getContentLength
                             (.getObjectMetadata ^com.amazonaws.services.s3.model.S3Object obj))))]
                (dio/fill-from-stream! ba is))))))))
  (defn delete-object
    ([s3 bucket path]
      (.deleteObject
        ^com.amazonaws.services.s3.AmazonS3Client s3
        ^java.lang.String bucket
        ^java.lang.String path)
      nil)))