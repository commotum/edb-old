(do
  (clojure.core/in-ns 'datomic.core2.anomalizer.aws.sdkv1)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.core2.anomalizer.aws.sdkv1)
    {:doc "datomic.core2.anomalizer extensions for AWS SDK v1"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'com.amazonaws.AmazonServiceException)
      (clojure.core/import 'com.amazonaws.services.s3.model.MultiObjectDeleteException)
      (clojure.core/import 'com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError)
      (clojure.core/require
        ['cognitect.anomalies :as 'anom]
        ['datomic.core2.anomalizer :as 'izer])))
  (when-not (.equals 'datomic.core2.anomalizer.aws.sdkv1 'clojure.core)
    (dosync
      (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.anomalizer.aws.sdkv1))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'com.amazonaws.AmazonServiceException)
        (clojure.core/import 'com.amazonaws.services.s3.model.MultiObjectDeleteException)
        (clojure.core/import
          'com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError)
        (clojure.core/require
          ['cognitect.anomalies :as 'anom]
          ['datomic.core2.anomalizer :as 'izer]))))
  (set! *warn-on-reflection* true)
  (defn vmap
    ([& kvs]
      (reduce
        (fn fn__20271
          ([m p__20270]
            (let [vec__20272 p__20270
                  k (nth vec__20272 (int 0) nil)
                  v (nth vec__20272 (int 1) nil)]
              (if v (assoc m k v) m))))
        {}
        (partition 2 kvs))))
  (reset-meta!
    #'vmap
    (assoc
      {:private true, :arglists (clojure.core/list ['& 'kvs]), :column (int 1)}
      :name
      'vmap
      :ns
      *ns*))
  (defn service-ex-as-data
    ([t]
      (vmap
        :errorCode
        (.getErrorCode ^com.amazonaws.AmazonServiceException t)
        :statusCode
        (java.lang.Integer/valueOf (int (.getStatusCode ^com.amazonaws.AmazonServiceException t)))
        :httpHeaders
        (.getHttpHeaders ^com.amazonaws.AmazonServiceException t)
        :proxyHost
        (.getProxyHost ^com.amazonaws.AmazonServiceException t)
        :requestId
        (.getRequestId ^com.amazonaws.AmazonServiceException t)
        :serviceName
        (.getServiceName ^com.amazonaws.AmazonServiceException t)
        :errorType
        (some-> (.getErrorType ^com.amazonaws.AmazonServiceException t) (str)))))
  (reset-meta!
    #'service-ex-as-data
    (assoc
      {:arglists (clojure.core/list [(.withMeta 't {:tag 'AmazonServiceException})]),
       :column (int 1)}
      :name
      'service-ex-as-data
      :ns
      *ns*))
  (def status-string->category {"AccessDenied" :cognitect.anomalies/forbidden})
  (reset-meta!
    #'status-string->category
    (assoc {:column (int 1)} :name 'status-string->category :ns *ns*))
  (def status-code->category
   {404 :cognitect.anomalies/not-found, 403 :cognitect.anomalies/forbidden})
  (reset-meta!
    #'status-code->category
    (assoc {:column (int 1)} :name 'status-code->category :ns *ns*))
  (defn service-exception-category
    ([t]
      (or
        (status-code->category
          (java.lang.Integer/valueOf
            (int (.getStatusCode ^com.amazonaws.AmazonServiceException t))))
        (izer/throwable-class-category (.getClass t)))))
  (reset-meta!
    #'service-exception-category
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 't {:tag 'AmazonServiceException})]),
       :column (int 1)}
      :name
      'service-exception-category
      :ns
      *ns*))
  (extend
    com.amazonaws.AmazonServiceException
    izer/ThrowableAnomData
    {:-throwable->anom-data (fn fn__20281 ([t] (service-ex-as-data t)))})
  (extend
    com.amazonaws.AmazonServiceException
    izer/ThrowableAnomCat
    {:-throwable-anom-category (fn fn__20283 ([t] (service-exception-category t)))})
  (extend
    com.amazonaws.services.s3.model.MultiObjectDeleteException
    izer/ThrowableAnomCat
    {:-throwable-anom-category
     (fn fn__20285
       ([t]
         (reduce
           (fn fn__20286
             ([cat err]
               (let [temp__5823__auto__ (status-string->category
                                          (.getCode
                                            ^com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError err))]
                 (if temp__5823__auto__ (let [cat temp__5823__auto__] (reduced cat)) cat))))
           (service-exception-category t)
           (.getErrors ^com.amazonaws.services.s3.model.MultiObjectDeleteException t))))})
  (swap!
    izer/throwable-categories
    merge
    {'com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
     :cognitect.anomalies/conflict,
     'com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
     :cognitect.anomalies/not-found}))