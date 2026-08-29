(do
  (clojure.core/in-ns 'datomic.s3-api)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.datafy :as 'd] ['datomic.aws :as 'aws])
      (clojure.core/import 'com.amazonaws.services.s3.AmazonS3Client)
      (clojure.core/import 'com.amazonaws.services.s3.model.Bucket)
      (clojure.core/import 'com.amazonaws.services.s3.model.BucketPolicy)
      (clojure.core/import 'com.amazonaws.services.s3.model.Owner)
      (clojure.core/import 'com.amazonaws.services.s3.model.ObjectListing)
      (clojure.core/import 'com.amazonaws.services.s3.model.ObjectMetadata)
      (clojure.core/import 'com.amazonaws.services.s3.model.S3ObjectSummary)
      (clojure.core/import 'com.amazonaws.services.s3.model.GeneratePresignedUrlRequest)
      (clojure.core/import 'com.amazonaws.services.s3.model.ListObjectsRequest)
      (clojure.core/import 'com.amazonaws.services.s3.model.DeleteObjectsRequest)
      (clojure.core/import 'com.amazonaws.services.s3.model.DeleteObjectsResult)
      (clojure.core/import 'com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject)
      (clojure.core/import 'com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError)
      (clojure.core/import 'com.amazonaws.services.s3.model.PutObjectResult)))
  (when-not (.equals 'datomic.s3-api 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.s3-api))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.datafy :as 'd] ['datomic.aws :as 'aws])
        (clojure.core/import 'com.amazonaws.services.s3.AmazonS3Client)
        (clojure.core/import 'com.amazonaws.services.s3.model.Bucket)
        (clojure.core/import 'com.amazonaws.services.s3.model.BucketPolicy)
        (clojure.core/import 'com.amazonaws.services.s3.model.Owner)
        (clojure.core/import 'com.amazonaws.services.s3.model.ObjectListing)
        (clojure.core/import 'com.amazonaws.services.s3.model.ObjectMetadata)
        (clojure.core/import 'com.amazonaws.services.s3.model.S3ObjectSummary)
        (clojure.core/import 'com.amazonaws.services.s3.model.GeneratePresignedUrlRequest)
        (clojure.core/import 'com.amazonaws.services.s3.model.ListObjectsRequest)
        (clojure.core/import 'com.amazonaws.services.s3.model.DeleteObjectsRequest)
        (clojure.core/import 'com.amazonaws.services.s3.model.DeleteObjectsResult)
        (clojure.core/import 'com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject)
        (clojure.core/import
          'com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError)
        (clojure.core/import 'com.amazonaws.services.s3.model.PutObjectResult))))
  (set! *warn-on-reflection* true)
  (def client
   (fn client
     ([creds config]
       (if config
         (let [region (get config :region)
               override_endpoint (get config :override-endpoint)
               conf (d/data-to-object
                      (dissoc config :region :override-endpoint)
                      com.amazonaws.ClientConfiguration)
               conn (if creds
                      (if (instance?
                            com.amazonaws.auth.AWSCredentialsProvider
                            (aws/credentials creds))
                        (com.amazonaws.services.s3.AmazonS3Client.
                          (aws/credentials creds)
                          ^com.amazonaws.ClientConfiguration conf)
                        (com.amazonaws.services.s3.AmazonS3Client.
                          (aws/credentials creds)
                          ^com.amazonaws.ClientConfiguration conf))
                      (if (instance?
                            com.amazonaws.auth.AWSCredentialsProvider
                            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
                        (com.amazonaws.services.s3.AmazonS3Client.
                          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                          ^com.amazonaws.ClientConfiguration conf)
                        (com.amazonaws.services.s3.AmazonS3Client.
                          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                          ^com.amazonaws.ClientConfiguration conf)))]
           (cond
             override_endpoint (.setEndpoint
                                 ^com.amazonaws.services.s3.AmazonS3Client conn
                                 (str "http://" override_endpoint))
             region (do
                      (.setEndpoint
                        ^com.amazonaws.services.s3.AmazonS3Client conn
                        (aws/endpoint-for :s3 region))))
           conn)
         (client creds)))
     ([creds]
       (if creds
         (if (instance? com.amazonaws.auth.AWSCredentialsProvider (aws/credentials creds))
           (com.amazonaws.services.s3.AmazonS3Client. (aws/credentials creds))
           (com.amazonaws.services.s3.AmazonS3Client. (aws/credentials creds)))
         (if (instance?
               com.amazonaws.auth.AWSCredentialsProvider
               (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
           (com.amazonaws.services.s3.AmazonS3Client.
             (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
           (com.amazonaws.services.s3.AmazonS3Client.
             (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
     ([]
       (if (instance?
             com.amazonaws.auth.AWSCredentialsProvider
             (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
         (com.amazonaws.services.s3.AmazonS3Client.
           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
         (com.amazonaws.services.s3.AmazonS3Client.
           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))))))
  (reset-meta!
    #'client
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [] {:tag 'com.amazonaws.services.s3.AmazonS3Client})
         (.withMeta ['creds] {:tag 'com.amazonaws.services.s3.AmazonS3Client})
         (.withMeta ['creds 'config] {:tag 'com.amazonaws.services.s3.AmazonS3Client})),
       :column (int 1)}
      :name
      'client
      :ns
      *ns*))
  (extend
    com.amazonaws.services.s3.model.Bucket
    d/ObjectToData
    {:object-to-data
     (fn fn__26034
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getCreationDate ^com.amazonaws.services.s3.model.Bucket o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:creationDate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getName ^com.amazonaws.services.s3.model.Bucket o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:name (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getOwner ^com.amazonaws.services.s3.model.Bucket o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:owner (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.BucketPolicy
    d/ObjectToData
    {:object-to-data
     (fn fn__26042
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getPolicyText
                                        ^com.amazonaws.services.s3.model.BucketPolicy o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:policyText (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.DeleteObjectsResult
    d/ObjectToData
    {:object-to-data
     (fn fn__26046
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.isRequesterCharged
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requesterCharged (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDeletedObjects
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:deletedObjects (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject
    d/ObjectToData
    {:object-to-data
     (fn fn__26052
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getVersionId
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:versionId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isDeleteMarker
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:deleteMarker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDeleteMarkerVersionId
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:deleteMarkerVersionId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKey
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:key (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
    d/ObjectToData
    {:object-to-data
     (fn fn__26062
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getContentType
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:contentType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getExpiration
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:expiration (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getBucketName
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:bucketName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getVersionId
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:versionId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSSECustomerKey
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sSECustomerKey (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getResponseHeaders
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:responseHeaders (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSSEAlgorithm
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sSEAlgorithm (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isZeroByteContent
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:zeroByteContent (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequestParameters
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestParameters (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getContentMd5
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:contentMd5 (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKmsCmkId
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:kmsCmkId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getMethod
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:method (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKey
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:key (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequestMetricCollector
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestMetricCollector (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequestCredentials
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestCredentials (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequestCredentialsProvider
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestCredentialsProvider (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGeneralProgressListener
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:generalProgressListener (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCustomRequestHeaders
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:customRequestHeaders (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCustomQueryParameters
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:customQueryParameters (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getReadLimit ^com.amazonaws.AmazonWebServiceRequest o)]
               (when (java.lang.Integer/valueOf (int temp__5804__auto__))
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:readLimit
                    (d/object-to-data-wrapper
                      (java.lang.Integer/valueOf (int v__19409__auto__)))])))
             (let [temp__5804__auto__ (.getCloneSource ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:cloneSource (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCloneRoot ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:cloneRoot (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSdkRequestTimeout
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sdkRequestTimeout (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSdkClientExecutionTimeout
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sdkClientExecutionTimeout (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequestClientOptions
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestClientOptions (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.ListObjectsRequest
    d/ObjectToData
    {:object-to-data
     (fn fn__26114
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getMarker
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:marker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPrefix
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:prefix (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDelimiter
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:delimiter (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isRequesterPays
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requesterPays (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getBucketName
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:bucketName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getMaxKeys
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:maxKeys (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getEncodingType
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:encodingType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getOptionalObjectAttributes
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:optionalObjectAttributes (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getExpectedBucketOwner
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:expectedBucketOwner (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequestMetricCollector
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestMetricCollector (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequestCredentials
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestCredentials (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequestCredentialsProvider
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestCredentialsProvider (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGeneralProgressListener
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:generalProgressListener (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCustomRequestHeaders
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:customRequestHeaders (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCustomQueryParameters
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:customQueryParameters (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getReadLimit ^com.amazonaws.AmazonWebServiceRequest o)]
               (when (java.lang.Integer/valueOf (int temp__5804__auto__))
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:readLimit
                    (d/object-to-data-wrapper
                      (java.lang.Integer/valueOf (int v__19409__auto__)))])))
             (let [temp__5804__auto__ (.getCloneSource ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:cloneSource (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCloneRoot ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:cloneRoot (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSdkRequestTimeout
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sdkRequestTimeout (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSdkClientExecutionTimeout
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sdkClientExecutionTimeout (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequestClientOptions
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestClientOptions (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError
    d/ObjectToData
    {:object-to-data
     (fn fn__26158
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getVersionId
                                        ^com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:versionId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCode
                                        ^com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:code (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKey
                                        ^com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:key (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getMessage
                                        ^com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:message (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.ObjectListing
    d/ObjectToData
    {:object-to-data
     (fn fn__26168
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getMarker
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:marker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPrefix
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:prefix (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDelimiter
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:delimiter (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isTruncated
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:truncated (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getBucketName
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:bucketName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getMaxKeys
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when (java.lang.Integer/valueOf (int temp__5804__auto__))
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:maxKeys
                    (d/object-to-data-wrapper
                      (java.lang.Integer/valueOf (int v__19409__auto__)))])))
             (let [temp__5804__auto__ (.getEncodingType
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:encodingType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getNextMarker
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:nextMarker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isRequesterCharged
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requesterCharged (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getObjectSummaries
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:objectSummaries (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCommonPrefixes
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:commonPrefixes (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.ObjectMetadata
    d/ObjectToData
    {:object-to-data
     (fn fn__26192
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getExpirationTime
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:expirationTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getContentLength
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when (long temp__5804__auto__)
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:contentLength (d/object-to-data-wrapper (long v__19409__auto__))])))
             (let [temp__5804__auto__ (.getContentType
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:contentType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getContentEncoding
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:contentEncoding (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLastModified
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:lastModified (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getVersionId
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:versionId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getETag ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:eTag (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStorageClass
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:storageClass (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getBucketKeyEnabled
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:bucketKeyEnabled (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getObjectLockMode
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:objectLockMode (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getObjectLockRetainUntilDate
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:objectLockRetainUntilDate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getObjectLockLegalHoldStatus
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:objectLockLegalHoldStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getContentMD5
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:contentMD5 (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSSEAlgorithm
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sSEAlgorithm (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSSECustomerAlgorithm
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sSECustomerAlgorithm (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSSECustomerKeyMd5
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sSECustomerKeyMd5 (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getExpirationTimeRuleId
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:expirationTimeRuleId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isRequesterCharged
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requesterCharged (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCacheControl
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:cacheControl (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getContentDisposition
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:contentDisposition (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getContentLanguage
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:contentLanguage (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getContentRange
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:contentRange (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getReplicationStatus
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:replicationStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getServerSideEncryption
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:serverSideEncryption (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRawMetadata
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:rawMetadata (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getHttpExpiresDate
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:httpExpiresDate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getUserMetadata
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:userMetadata (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getInstanceLength
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when (long temp__5804__auto__)
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instanceLength (d/object-to-data-wrapper (long v__19409__auto__))])))
             (let [temp__5804__auto__ (.getRestoreExpirationTime
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:restoreExpirationTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getOngoingRestore
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ongoingRestore (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getArchiveStatus
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:archiveStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSSEAwsKmsKeyId
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sSEAwsKmsKeyId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSSEAwsKmsEncryptionContext
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sSEAwsKmsEncryptionContext (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPartCount
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:partCount (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.Owner
    d/ObjectToData
    {:object-to-data
     (fn fn__26262
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getDisplayName ^com.amazonaws.services.s3.model.Owner o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:displayName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getId ^com.amazonaws.services.s3.model.Owner o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:id (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.S3ObjectSummary
    d/ObjectToData
    {:object-to-data
     (fn fn__26268
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getLastModified
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:lastModified (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getBucketName
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:bucketName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getETag
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:eTag (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStorageClass
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:storageClass (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRestoreStatus
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:restoreStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKey ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:key (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSize
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when (long temp__5804__auto__)
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:size (d/object-to-data-wrapper (long v__19409__auto__))])))
             (let [temp__5804__auto__ (.getOwner
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:owner (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.s3.model.ListObjectsRequest]
    fn__26286
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:maxKeys :marker :optionalObjectAttributes :requestCredentials
                                     :sdkClientExecutionTimeout :encodingType :prefix :delimiter
                                     :generalProgressListener :expectedBucketOwner :bucketName
                                     :sdkRequestTimeout :requesterPays :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:maxKeys :marker :optionalObjectAttributes :requestCredentials
                   :sdkClientExecutionTimeout :encodingType :prefix :delimiter
                   :generalProgressListener :expectedBucketOwner :bucketName :sdkRequestTimeout
                   :requesterPays :requestMetricCollector :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.s3.model.ListObjectsRequest}))))
        nil)
      (let [o (com.amazonaws.services.s3.model.ListObjectsRequest.)]
        (when (contains? m :prefix)
          (let [v (:prefix m) k (d/property-to-object (class o) :prefix v java.lang.String)]
            (.setPrefix
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :bucketName)
          (let [v (:bucketName m)
                k (d/property-to-object (class o) :bucketName v java.lang.String)]
            (.setBucketName
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)))
        (when (contains? m :encodingType)
          (let [v (:encodingType m)
                k (d/property-to-object (class o) :encodingType v java.lang.String)]
            (.setEncodingType
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)))
        (when (contains? m :maxKeys)
          (let [v (:maxKeys m) k (d/property-to-object (class o) :maxKeys v java.lang.Integer)]
            (.setMaxKeys
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.Integer k)))
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)))
        (when (contains? m :expectedBucketOwner)
          (let [v (:expectedBucketOwner m)
                k (d/property-to-object (class o) :expectedBucketOwner v java.lang.String)]
            (.setExpectedBucketOwner
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :optionalObjectAttributes)
          (let [v (:optionalObjectAttributes m)
                k (d/property-to-object (class o) :optionalObjectAttributes v java.util.List)]
            (.setOptionalObjectAttributes
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.util.List k)))
        (when (contains? m :requesterPays)
          (let [v (:requesterPays m)
                k (d/property-to-object (class o) :requesterPays v java.lang.Boolean/TYPE)]
            (.setRequesterPays
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)))
        (when (contains? m :delimiter)
          (let [v (:delimiter m) k (d/property-to-object (class o) :delimiter v java.lang.String)]
            (.setDelimiter
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.s3.model.ObjectListing]
    fn__26289
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:nextMarker :maxKeys :marker :encodingType :prefix :delimiter
                                     :commonPrefixes :requesterCharged :truncated :bucketName}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:nextMarker :maxKeys :marker :encodingType :prefix :delimiter :commonPrefixes
                   :requesterCharged :truncated :bucketName},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.s3.model.ObjectListing}))))
        nil)
      (let [o (com.amazonaws.services.s3.model.ObjectListing.)]
        (when (contains? m :prefix)
          (let [v (:prefix m) k (d/property-to-object (class o) :prefix v java.lang.String)]
            (.setPrefix ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)))
        (when (contains? m :requesterCharged)
          (let [v (:requesterCharged m)
                k (d/property-to-object (class o) :requesterCharged v java.lang.Boolean/TYPE)]
            (.setRequesterCharged
              ^com.amazonaws.services.s3.model.ObjectListing o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :bucketName)
          (let [v (:bucketName m)
                k (d/property-to-object (class o) :bucketName v java.lang.String)]
            (.setBucketName ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)))
        (when (contains? m :encodingType)
          (let [v (:encodingType m)
                k (d/property-to-object (class o) :encodingType v java.lang.String)]
            (.setEncodingType
              ^com.amazonaws.services.s3.model.ObjectListing o
              ^java.lang.String k)))
        (when (contains? m :maxKeys)
          (let [v (:maxKeys m)
                k (d/property-to-object (class o) :maxKeys v java.lang.Integer/TYPE)]
            (.setMaxKeys
              ^com.amazonaws.services.s3.model.ObjectListing o
              (int ^java.lang.Number k))))
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)))
        (when (contains? m :nextMarker)
          (let [v (:nextMarker m)
                k (d/property-to-object (class o) :nextMarker v java.lang.String)]
            (.setNextMarker ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)))
        (when (contains? m :truncated)
          (let [v (:truncated m)
                k (d/property-to-object (class o) :truncated v java.lang.Boolean/TYPE)]
            (.setTruncated
              ^com.amazonaws.services.s3.model.ObjectListing o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :commonPrefixes)
          (let [v (:commonPrefixes m)
                k (d/property-to-object (class o) :commonPrefixes v java.util.List)]
            (.setCommonPrefixes
              ^com.amazonaws.services.s3.model.ObjectListing o
              ^java.util.List k)))
        (when (contains? m :delimiter)
          (let [v (:delimiter m) k (d/property-to-object (class o) :delimiter v java.lang.String)]
            (.setDelimiter ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.s3.model.ObjectMetadata]
    fn__26292
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:userMetadata :contentDisposition :serverSideEncryption
                                     :httpExpiresDate :contentEncoding :expirationTime
                                     :contentLanguage :cacheControl :lastModified
                                     :expirationTimeRuleId :bucketKeyEnabled :requesterCharged
                                     :sSECustomerAlgorithm :restoreExpirationTime :contentLength
                                     :contentType :contentMD5 :ongoingRestore :sSECustomerKeyMd5
                                     :sSEAlgorithm}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:userMetadata :contentDisposition :serverSideEncryption :httpExpiresDate
                   :contentEncoding :expirationTime :contentLanguage :cacheControl :lastModified
                   :expirationTimeRuleId :bucketKeyEnabled :requesterCharged :sSECustomerAlgorithm
                   :restoreExpirationTime :contentLength :contentType :contentMD5 :ongoingRestore
                   :sSECustomerKeyMd5 :sSEAlgorithm},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.s3.model.ObjectMetadata}))))
        nil)
      (let [o (com.amazonaws.services.s3.model.ObjectMetadata.)]
        (when (contains? m :contentEncoding)
          (let [v (:contentEncoding m)
                k (d/property-to-object (class o) :contentEncoding v java.lang.String)]
            (.setContentEncoding
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :expirationTimeRuleId)
          (let [v (:expirationTimeRuleId m)
                k (d/property-to-object (class o) :expirationTimeRuleId v java.lang.String)]
            (.setExpirationTimeRuleId
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :cacheControl)
          (let [v (:cacheControl m)
                k (d/property-to-object (class o) :cacheControl v java.lang.String)]
            (.setCacheControl
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :requesterCharged)
          (let [v (:requesterCharged m)
                k (d/property-to-object (class o) :requesterCharged v java.lang.Boolean/TYPE)]
            (.setRequesterCharged
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :restoreExpirationTime)
          (let [v (:restoreExpirationTime m)
                k (d/property-to-object (class o) :restoreExpirationTime v java.util.Date)]
            (.setRestoreExpirationTime
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.util.Date k)))
        (when (contains? m :contentType)
          (let [v (:contentType m)
                k (d/property-to-object (class o) :contentType v java.lang.String)]
            (.setContentType
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :contentLength)
          (let [v (:contentLength m)
                k (d/property-to-object (class o) :contentLength v java.lang.Long/TYPE)]
            (.setContentLength
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              (long ^java.lang.Number k))))
        (when (contains? m :sSECustomerKeyMd5)
          (let [v (:sSECustomerKeyMd5 m)
                k (d/property-to-object (class o) :sSECustomerKeyMd5 v java.lang.String)]
            (.setSSECustomerKeyMd5
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :ongoingRestore)
          (let [v (:ongoingRestore m)
                k (d/property-to-object (class o) :ongoingRestore v java.lang.Boolean/TYPE)]
            (.setOngoingRestore
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              (boolean (.booleanValue ^java.lang.Boolean k)))))
        (when (contains? m :expirationTime)
          (let [v (:expirationTime m)
                k (d/property-to-object (class o) :expirationTime v java.util.Date)]
            (.setExpirationTime
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.util.Date k)))
        (when (contains? m :userMetadata)
          (let [v (:userMetadata m)
                k (d/property-to-object (class o) :userMetadata v java.util.Map)]
            (.setUserMetadata ^com.amazonaws.services.s3.model.ObjectMetadata o ^java.util.Map k)))
        (when (contains? m :contentLanguage)
          (let [v (:contentLanguage m)
                k (d/property-to-object (class o) :contentLanguage v java.lang.String)]
            (.setContentLanguage
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :lastModified)
          (let [v (:lastModified m)
                k (d/property-to-object (class o) :lastModified v java.util.Date)]
            (.setLastModified
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.util.Date k)))
        (when (contains? m :contentMD5)
          (let [v (:contentMD5 m)
                k (d/property-to-object (class o) :contentMD5 v java.lang.String)]
            (.setContentMD5
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :contentDisposition)
          (let [v (:contentDisposition m)
                k (d/property-to-object (class o) :contentDisposition v java.lang.String)]
            (.setContentDisposition
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :serverSideEncryption)
          (let [v (:serverSideEncryption m)
                k (d/property-to-object (class o) :serverSideEncryption v java.lang.String)]
            (.setServerSideEncryption
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :sSEAlgorithm)
          (let [v (:sSEAlgorithm m)
                k (d/property-to-object (class o) :sSEAlgorithm v java.lang.String)]
            (.setSSEAlgorithm
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :sSECustomerAlgorithm)
          (let [v (:sSECustomerAlgorithm m)
                k (d/property-to-object (class o) :sSECustomerAlgorithm v java.lang.String)]
            (.setSSECustomerAlgorithm
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)))
        (when (contains? m :httpExpiresDate)
          (let [v (:httpExpiresDate m)
                k (d/property-to-object (class o) :httpExpiresDate v java.util.Date)]
            (.setHttpExpiresDate
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.util.Date k)))
        (when (contains? m :bucketKeyEnabled)
          (let [v (:bucketKeyEnabled m)
                k (d/property-to-object (class o) :bucketKeyEnabled v java.lang.Boolean)]
            (.setBucketKeyEnabled
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.Boolean k)))
        o)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.s3.model.DeleteObjectsResult :getDeletedObjects]
    com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.s3.model.DeleteObjectsResult :getDeletedObjects]
    fn__26295
    ([_ _ val _]
      (mapv
        (fn fn__26296
          ([item]
            (d/data-to-object
              item
              com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.s3.model.ObjectListing :getObjectSummaries]
    com.amazonaws.services.s3.model.S3ObjectSummary)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.s3.model.ObjectListing :getObjectSummaries]
    fn__26299
    ([_ _ val _]
      (mapv
        (fn fn__26300
          ([item] (d/data-to-object item com.amazonaws.services.s3.model.S3ObjectSummary)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.s3.AmazonS3Client :listBuckets]
    com.amazonaws.services.s3.model.Bucket)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.s3.AmazonS3Client :listBuckets]
    fn__26303
    ([_ _ val _]
      (mapv
        (fn fn__26304 ([item] (d/data-to-object item com.amazonaws.services.s3.model.Bucket)))
        val)))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "list-buckets")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "list-buckets")
    (fn list_buckets
      ([o] (d/object-to-data (.listBuckets ^com.amazonaws.services.s3.AmazonS3Client o)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "list-objects")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "list-objects")
    (fn list_objects
      ([o x1]
        (d/object-to-data
          (.listObjects
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 java.lang.String))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "list-next-batch-of-objects")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "list-next-batch-of-objects")
    (fn list_next_batch_of_objects
      ([o x1]
        (d/object-to-data
          (.listNextBatchOfObjects
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 com.amazonaws.services.s3.model.ObjectListing))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "list-objects-from-request")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "list-objects-from-request")
    (fn list_objects_from_request
      ([o x1]
        (d/object-to-data
          (.listObjects
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 com.amazonaws.services.s3.model.ListObjectsRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "create-bucket")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "create-bucket")
    (fn create_bucket
      ([o x1]
        (d/object-to-data
          (.createBucket
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 java.lang.String))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "create-bucket-in-region")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "create-bucket-in-region")
    (fn create_bucket_in_region
      ([o x1 x2]
        (d/object-to-data
          (.createBucket
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 java.lang.String)
            (d/data-to-object x2 java.lang.String))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "delete-bucket")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "delete-bucket")
    (fn delete_bucket
      ([o x1]
        (.deleteBucket
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String))
        :ok)))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "put-file")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2 'x3]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "put-file")
    (fn put_file
      ([o x1 x2 x3]
        (d/object-to-data
          (.putObject
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 java.lang.String)
            (d/data-to-object x2 java.lang.String)
            (d/data-to-object x3 java.io.File))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "put-object-with-canned-acl")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "put-object-with-canned-acl")
    (fn put_object_with_canned_acl
      ([o x1]
        (d/object-to-data
          (.putObject
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 com.amazonaws.services.s3.model.PutObjectRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "put-object")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2 'x3 'x4]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "put-object")
    (fn put_object
      ([o x1 x2 x3 x4]
        (d/object-to-data
          (.putObject
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 java.lang.String)
            (d/data-to-object x2 java.lang.String)
            (d/data-to-object x3 java.io.InputStream)
            (d/data-to-object x4 com.amazonaws.services.s3.model.ObjectMetadata))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "get-object")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "get-object")
    (fn get_object
      ([o x1 x2]
        (d/object-to-data
          (.getObject
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 java.lang.String)
            (d/data-to-object x2 java.lang.String))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "get-object-metadata")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "get-object-metadata")
    (fn get_object_metadata
      ([o x1 x2]
        (d/object-to-data
          (.getObjectMetadata
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 java.lang.String)
            (d/data-to-object x2 java.lang.String))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "set-bucket-policy")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "set-bucket-policy")
    (fn set_bucket_policy
      ([o x1 x2]
        (.setBucketPolicy
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)
          (d/data-to-object x2 java.lang.String))
        :ok)))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "get-bucket-policy")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "get-bucket-policy")
    (fn get_bucket_policy
      ([o x1]
        (d/object-to-data
          (.getBucketPolicy
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 java.lang.String))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "delete-object")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "delete-object")
    (fn delete_object
      ([o x1 x2]
        (.deleteObject
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)
          (d/data-to-object x2 java.lang.String))
        :ok)))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "delete-objects")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "delete-objects")
    (fn delete_objects
      ([o x1]
        (d/object-to-data
          (.deleteObjects
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 com.amazonaws.services.s3.model.DeleteObjectsRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3-api" "generate-presigned-url")
    {:related-class com.amazonaws.services.s3.AmazonS3Client,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2 'x3 'x4]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-api" "generate-presigned-url")
    (fn generate_presigned_url
      ([o x1 x2 x3 x4]
        (d/object-to-data
          (.generatePresignedUrl
            ^com.amazonaws.services.s3.AmazonS3Client o
            (d/data-to-object x1 java.lang.String)
            (d/data-to-object x2 java.lang.String)
            (d/data-to-object x3 java.util.Date)
            (d/data-to-object x4 com.amazonaws.HttpMethod)))))))