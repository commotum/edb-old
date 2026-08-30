(do
  (clojure.core/in-ns 'datomic.aws.client.anomalizer)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.aws.client.anomalizer)
    {:doc
     "AWS SDK v2 specific exception to anomaly conversion.\n\n  Implements protocol extensions for AWS SDK v2 exception types,\n  providing anomaly category determination and data extraction for:\n\n  - AwsServiceException: error-code → HTTP status → ::anom/fault\n  - ApiCallTimeoutException: ::anom/unavailable\n  - SdkClientException: ::anom/fault\n\n  Error Code Mapping:\n  The error-code-categories atom maps AWS error codes to anomaly categories.\n  This is the primary extension point for service-specific error handling.\n\n  Example:\n  (swap! error-code-categories assoc\n    \"ThrottlingException\" ::anom/busy\n    \"NoSuchBucket\" ::anom/not-found)\n\n  The anomalize function combines the exception conversion with operation\n  context, preserving request details for debugging and retry logic."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.spec.alpha :as 's]
        ['cognitect.anomalies :as 'anom]
        ['datomic.anomalizer :as 'izer]
        ['datomic.aws.client.util :as 'util])
      (clojure.core/import 'software.amazon.awssdk.awscore.exception.AwsServiceException)
      (clojure.core/import 'software.amazon.awssdk.core.exception.ApiCallTimeoutException)
      (clojure.core/import 'software.amazon.awssdk.core.exception.SdkClientException)))
  (when-not (.equals 'datomic.aws.client.anomalizer 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws.client.anomalizer))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.spec.alpha :as 's]
          ['cognitect.anomalies :as 'anom]
          ['datomic.anomalizer :as 'izer]
          ['datomic.aws.client.util :as 'util])
        (clojure.core/import 'software.amazon.awssdk.awscore.exception.AwsServiceException)
        (clojure.core/import 'software.amazon.awssdk.core.exception.ApiCallTimeoutException)
        (clojure.core/import 'software.amazon.awssdk.core.exception.SdkClientException))))
  (set! *warn-on-reflection* true)
  (defn spec-validator
    ([s]
      (fn fn__14232
        ([v]
          (let [temp__5802__auto__ (s/explain-data s v)]
            (when temp__5802__auto__
              (let [d temp__5802__auto__]
                (throw
                  (ex-info
                    "Data not to spec"
                    {:cognitect.anomalies/message "Data not to spec",
                     :cognitect.anomalies/category :cognitect.anomalies/incorrect,
                     :explain-data d}))))
            true)))))
  (reset-meta!
    #'spec-validator
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'spec-validator
      :ns
      *ns*))
  (def anom-categories
   #{:cognitect.anomalies/unavailable :cognitect.anomalies/unsupported
     :cognitect.anomalies/interrupted :cognitect.anomalies/conflict :cognitect.anomalies/incorrect
     :cognitect.anomalies/not-found :cognitect.anomalies/busy :cognitect.anomalies/forbidden
     :cognitect.anomalies/fault})
  (reset-meta!
    #'anom-categories
    (assoc {:private true, :column (int 1)} :name 'anom-categories :ns *ns*))
  (s/def-impl
    :datomic.aws.client.anomalizer/error-code-categories
    (clojure.core/list
      'clojure.spec.alpha/map-of
      'clojure.core/string?
      'datomic.aws.client.anomalizer/anom-categories)
    (s/every-impl
      (clojure.core/list 'clojure.spec.alpha/tuple 'string? 'anom-categories)
      (s/tuple-impl
        ['clojure.core/string? 'datomic.aws.client.anomalizer/anom-categories]
        [string? anom-categories])
      {:clojure.spec.alpha/kfn
       (fn fn__14237 ([i__1934__auto__ v__1935__auto__] (nth v__1935__auto__ (int 0)))),
       :into {},
       :clojure.spec.alpha/conform-all true,
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?,
       :clojure.spec.alpha/describe
       (clojure.core/list
         'clojure.spec.alpha/map-of
         'clojure.core/string?
         'datomic.aws.client.anomalizer/anom-categories),
       :clojure.spec.alpha/cpred (fn fn__14239 ([G__14236] (map? G__14236)))}
      nil))
  (.setMeta
    (clojure.lang.RT/var "datomic.aws.client.anomalizer" "error-code-categories")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.aws.client.anomalizer" "error-code-categories")
    (atom {} :validator (spec-validator :datomic.aws.client.anomalizer/error-code-categories)))
  (defn status->anomaly-cat
    ([^long status]
      (let [G__14241 status]
        (case
          G__14241
          304
          :cognitect.anomalies/conflict
          403
          :cognitect.anomalies/forbidden
          404
          :cognitect.anomalies/not-found
          (429 503)
          :cognitect.anomalies/busy
          504
          :cognitect.anomalies/unavailable
          (if (<= 300 (long status) 499)
            :cognitect.anomalies/incorrect
            :cognitect.anomalies/fault)))))
  (reset-meta!
    #'status->anomaly-cat
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'status {:tag 'long})]),
       :column (int 1)}
      :name
      'status->anomaly-cat
      :ns
      *ns*))
  (defn error-code->anomaly-cat ([error_code] (get (deref error-code-categories) error_code)))
  (reset-meta!
    #'error-code->anomaly-cat
    (assoc
      {:private true, :arglists (clojure.core/list ['error-code]), :column (int 1)}
      :name
      'error-code->anomaly-cat
      :ns
      *ns*))
  (extend
    software.amazon.awssdk.awscore.exception.AwsServiceException
    izer/ThrowableAnomCat
    {:-throwable-anom-category
     (fn fn__14244
       ([ex]
         (let [error_code (some-> ex (.awsErrorDetails) (.errorCode))]
           (or
             (error-code->anomaly-cat error_code)
             (status->anomaly-cat
               (.statusCode ^software.amazon.awssdk.core.exception.SdkServiceException ex))
             :cognitect.anomalies/fault))))})
  (extend
    software.amazon.awssdk.core.exception.ApiCallTimeoutException
    izer/ThrowableAnomCat
    {:-throwable-anom-category (fn fn__14249 ([ex] :cognitect.anomalies/unavailable))})
  (extend
    software.amazon.awssdk.core.exception.SdkClientException
    izer/ThrowableAnomCat
    {:-throwable-anom-category (fn fn__14251 ([ex] :cognitect.anomalies/fault))})
  (extend
    software.amazon.awssdk.awscore.exception.AwsServiceException
    izer/ThrowableAnomData
    {:-throwable->anom-data
     (fn fn__14253
       ([ex]
         (util/vmap
           :datomic.aws.client.api/error-message
           (some-> ex (.awsErrorDetails) (.errorMessage))
           :datomic.aws.client.api/service
           (some-> ex (.awsErrorDetails) (.serviceName))
           :datomic.aws.client.api/error-code
           (some-> ex (.awsErrorDetails) (.errorCode))
           :datomic.aws.client.api/request-id
           (.requestId ^software.amazon.awssdk.core.exception.SdkServiceException ex)
           :datomic.aws.client.api/extended-request-id
           (.extendedRequestId ^software.amazon.awssdk.core.exception.SdkServiceException ex)
           :datomic.aws.client.api/retryable?
           (.isRetryableException ^software.amazon.awssdk.core.exception.SdkServiceException ex)
           :datomic.aws.client.api/throttled?
           (.isThrottlingException
             ^software.amazon.awssdk.awscore.exception.AwsServiceException ex)
           :datomic.aws.client.api/attempts
           (.numAttempts ^software.amazon.awssdk.core.exception.SdkException ex))))})
  (extend
    software.amazon.awssdk.core.exception.ApiCallTimeoutException
    izer/ThrowableAnomData
    {:-throwable->anom-data
     (fn fn__14258
       ([ex]
         (util/vmap
           :datomic.aws.client.api/retryable?
           (.retryable ^software.amazon.awssdk.core.exception.SdkException ex)
           :datomic.aws.client.api/attempts
           (.numAttempts ^software.amazon.awssdk.core.exception.SdkException ex))))})
  (defn anomalize
    ([t op_map]
      (with-meta
        (update
          (izer/throwable->anom t)
          :data
          merge
          (util/vmap
            :datomic.aws.client.api/op
            (:op op_map)
            :datomic.aws.client.api/service
            (:service op_map)
            :datomic.aws.client.api/meta
            (:meta op_map)))
        #:datomic.aws.client.api{:op-map op_map})))
  (reset-meta!
    #'anomalize
    (assoc
      {:arglists (clojure.core/list ['t 'op-map]), :column (int 1)}
      :name
      'anomalize
      :ns
      *ns*)))