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
  (defn client
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
            override_endpoint (do
                                (.setEndpoint
                                  ^com.amazonaws.services.s3.AmazonS3Client conn
                                  (str "http://" override_endpoint))
                                nil)
            region (do
                     (.setEndpoint
                       ^com.amazonaws.services.s3.AmazonS3Client conn
                       (aws/endpoint-for :s3 region))
                     nil))
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
          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
  (extend
    com.amazonaws.services.s3.model.Bucket
    d/ObjectToData
    {:object-to-data
     (fn fn__22962
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getCreationDate ^com.amazonaws.services.s3.model.Bucket o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:creationDate (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getName ^com.amazonaws.services.s3.model.Bucket o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:name (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getOwner ^com.amazonaws.services.s3.model.Bucket o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:owner (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.BucketPolicy
    d/ObjectToData
    {:object-to-data
     (fn fn__22970
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getPolicyText
                                        ^com.amazonaws.services.s3.model.BucketPolicy o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:policyText (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.DeleteObjectsResult
    d/ObjectToData
    {:object-to-data
     (fn fn__22974
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.isRequesterCharged
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requesterCharged (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getDeletedObjects
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:deletedObjects (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject
    d/ObjectToData
    {:object-to-data
     (fn fn__22980
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.isDeleteMarker
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:deleteMarker (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getDeleteMarkerVersionId
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:deleteMarkerVersionId (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getVersionId
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:versionId (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getKey
                                        ^com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:key (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
    d/ObjectToData
    {:object-to-data
     (fn fn__22990
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getContentType
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:contentType (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getExpiration
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:expiration (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getBucketName
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:bucketName (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getVersionId
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:versionId (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSSECustomerKey
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sSECustomerKey (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getResponseHeaders
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:responseHeaders (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSSEAlgorithm
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sSEAlgorithm (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.isZeroByteContent
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:zeroByteContent (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRequestParameters
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestParameters (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getContentMd5
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:contentMd5 (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getKmsCmkId
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:kmsCmkId (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getMethod
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:method (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getKey
                                        ^com.amazonaws.services.s3.model.GeneratePresignedUrlRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:key (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRequestMetricCollector
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestMetricCollector (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRequestCredentials
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestCredentials (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRequestCredentialsProvider
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestCredentialsProvider (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getGeneralProgressListener
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:generalProgressListener (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getCustomRequestHeaders
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:customRequestHeaders (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getCustomQueryParameters
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:customQueryParameters (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getReadLimit ^com.amazonaws.AmazonWebServiceRequest o)]
               (when (java.lang.Integer/valueOf (int temp__5457__auto__))
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:readLimit
                    (d/object-to-data-wrapper
                      (java.lang.Integer/valueOf (int v__17285__auto__)))])))
             (let [temp__5457__auto__ (.getCloneSource ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:cloneSource (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getCloneRoot ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:cloneRoot (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkRequestTimeout
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkRequestTimeout (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkClientExecutionTimeout
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkClientExecutionTimeout (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRequestClientOptions
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestClientOptions (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.ListObjectsRequest
    d/ObjectToData
    {:object-to-data
     (fn fn__23042
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getPrefix
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:prefix (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getMarker
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:marker (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getBucketName
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:bucketName (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.isRequesterPays
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requesterPays (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getDelimiter
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:delimiter (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getMaxKeys
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:maxKeys (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getEncodingType
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:encodingType (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getOptionalObjectAttributes
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:optionalObjectAttributes (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getExpectedBucketOwner
                                        ^com.amazonaws.services.s3.model.ListObjectsRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:expectedBucketOwner (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRequestMetricCollector
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestMetricCollector (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRequestCredentials
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestCredentials (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRequestCredentialsProvider
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestCredentialsProvider (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getGeneralProgressListener
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:generalProgressListener (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getCustomRequestHeaders
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:customRequestHeaders (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getCustomQueryParameters
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:customQueryParameters (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getReadLimit ^com.amazonaws.AmazonWebServiceRequest o)]
               (when (java.lang.Integer/valueOf (int temp__5457__auto__))
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:readLimit
                    (d/object-to-data-wrapper
                      (java.lang.Integer/valueOf (int v__17285__auto__)))])))
             (let [temp__5457__auto__ (.getCloneSource ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:cloneSource (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getCloneRoot ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:cloneRoot (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkRequestTimeout
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkRequestTimeout (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkClientExecutionTimeout
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkClientExecutionTimeout (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRequestClientOptions
                                        ^com.amazonaws.AmazonWebServiceRequest o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestClientOptions (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError
    d/ObjectToData
    {:object-to-data
     (fn fn__23086
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getCode
                                        ^com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:code (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getVersionId
                                        ^com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:versionId (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getKey
                                        ^com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:key (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getMessage
                                        ^com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:message (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.ObjectListing
    d/ObjectToData
    {:object-to-data
     (fn fn__23096
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getPrefix
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:prefix (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getMarker
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:marker (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.isRequesterCharged
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requesterCharged (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getBucketName
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:bucketName (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getObjectSummaries
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:objectSummaries (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getCommonPrefixes
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:commonPrefixes (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.isTruncated
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:truncated (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getDelimiter
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:delimiter (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getMaxKeys
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when (java.lang.Integer/valueOf (int temp__5457__auto__))
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:maxKeys
                    (d/object-to-data-wrapper
                      (java.lang.Integer/valueOf (int v__17285__auto__)))])))
             (let [temp__5457__auto__ (.getEncodingType
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:encodingType (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getNextMarker
                                        ^com.amazonaws.services.s3.model.ObjectListing o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:nextMarker (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.ObjectMetadata
    d/ObjectToData
    {:object-to-data
     (fn fn__23120
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getExpirationTime
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:expirationTime (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getContentLength
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when (long temp__5457__auto__)
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:contentLength (d/object-to-data-wrapper (long v__17285__auto__))])))
             (let [temp__5457__auto__ (.getLastModified
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:lastModified (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getContentType
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:contentType (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getContentEncoding
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:contentEncoding (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.isRequesterCharged
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requesterCharged (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getInstanceLength
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when (long temp__5457__auto__)
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:instanceLength (d/object-to-data-wrapper (long v__17285__auto__))])))
             (let [temp__5457__auto__ (.getRestoreExpirationTime
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:restoreExpirationTime (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getOngoingRestore
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:ongoingRestore (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getArchiveStatus
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:archiveStatus (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSSEAwsKmsKeyId
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sSEAwsKmsKeyId (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSSEAwsKmsEncryptionContext
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sSEAwsKmsEncryptionContext (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getPartCount
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:partCount (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getVersionId
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:versionId (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getETag ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:eTag (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getStorageClass
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:storageClass (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getBucketKeyEnabled
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:bucketKeyEnabled (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getObjectLockMode
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:objectLockMode (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getObjectLockRetainUntilDate
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:objectLockRetainUntilDate (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getObjectLockLegalHoldStatus
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:objectLockLegalHoldStatus (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getContentMD5
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:contentMD5 (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSSEAlgorithm
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sSEAlgorithm (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSSECustomerAlgorithm
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sSECustomerAlgorithm (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSSECustomerKeyMd5
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sSECustomerKeyMd5 (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getExpirationTimeRuleId
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:expirationTimeRuleId (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getCacheControl
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:cacheControl (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getContentDisposition
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:contentDisposition (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getContentLanguage
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:contentLanguage (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getContentRange
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:contentRange (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getReplicationStatus
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:replicationStatus (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getServerSideEncryption
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:serverSideEncryption (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRawMetadata
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:rawMetadata (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getHttpExpiresDate
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:httpExpiresDate (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getUserMetadata
                                        ^com.amazonaws.services.s3.model.ObjectMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:userMetadata (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.Owner
    d/ObjectToData
    {:object-to-data
     (fn fn__23190
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getDisplayName ^com.amazonaws.services.s3.model.Owner o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:displayName (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getId ^com.amazonaws.services.s3.model.Owner o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:id (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.s3.model.S3ObjectSummary
    d/ObjectToData
    {:object-to-data
     (fn fn__23196
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getLastModified
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:lastModified (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getBucketName
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:bucketName (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRestoreStatus
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:restoreStatus (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getETag
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:eTag (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getStorageClass
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:storageClass (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getKey ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:key (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSize
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when (long temp__5457__auto__)
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:size (d/object-to-data-wrapper (long v__17285__auto__))])))
             (let [temp__5457__auto__ (.getOwner
                                        ^com.amazonaws.services.s3.model.S3ObjectSummary o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:owner (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.s3.model.ListObjectsRequest]
    fn__23214
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:maxKeys :marker :optionalObjectAttributes :requestCredentials
                                     :sdkClientExecutionTimeout :encodingType :prefix :delimiter
                                     :generalProgressListener :expectedBucketOwner :bucketName
                                     :sdkRequestTimeout :requesterPays :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:maxKeys :marker :optionalObjectAttributes :requestCredentials
                   :sdkClientExecutionTimeout :encodingType :prefix :delimiter
                   :generalProgressListener :expectedBucketOwner :bucketName :sdkRequestTimeout
                   :requesterPays :requestMetricCollector :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.s3.model.ListObjectsRequest})))))
      (let [o (com.amazonaws.services.s3.model.ListObjectsRequest.)]
        (when (contains? m :prefix)
          (let [v (:prefix m) k (d/property-to-object (class o) :prefix v java.lang.String)]
            (.setPrefix ^com.amazonaws.services.s3.model.ListObjectsRequest o ^java.lang.String k)
            nil))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :bucketName)
          (let [v (:bucketName m)
                k (d/property-to-object (class o) :bucketName v java.lang.String)]
            (.setBucketName
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :encodingType)
          (let [v (:encodingType m)
                k (d/property-to-object (class o) :encodingType v java.lang.String)]
            (.setEncodingType
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)
            nil))
        (when (contains? m :maxKeys)
          (let [v (:maxKeys m) k (d/property-to-object (class o) :maxKeys v java.lang.Integer)]
            (.setMaxKeys
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.Integer k)
            nil))
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker ^com.amazonaws.services.s3.model.ListObjectsRequest o ^java.lang.String k)
            nil))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)
            nil))
        (when (contains? m :expectedBucketOwner)
          (let [v (:expectedBucketOwner m)
                k (d/property-to-object (class o) :expectedBucketOwner v java.lang.String)]
            (.setExpectedBucketOwner
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :optionalObjectAttributes)
          (let [v (:optionalObjectAttributes m)
                k (d/property-to-object (class o) :optionalObjectAttributes v java.util.List)]
            (.setOptionalObjectAttributes
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.util.List k)
            nil))
        (when (contains? m :requesterPays)
          (let [v (:requesterPays m)
                k (d/property-to-object (class o) :requesterPays v java.lang.Boolean/TYPE)]
            (.setRequesterPays
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              (boolean (.booleanValue ^java.lang.Boolean k)))
            nil))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)
            nil))
        (when (contains? m :delimiter)
          (let [v (:delimiter m) k (d/property-to-object (class o) :delimiter v java.lang.String)]
            (.setDelimiter
              ^com.amazonaws.services.s3.model.ListObjectsRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.s3.model.ObjectListing]
    fn__23217
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:nextMarker :maxKeys :marker :encodingType :prefix :delimiter
                                     :commonPrefixes :requesterCharged :truncated :bucketName}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:nextMarker :maxKeys :marker :encodingType :prefix :delimiter :commonPrefixes
                   :requesterCharged :truncated :bucketName},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.s3.model.ObjectListing})))))
      (let [o (com.amazonaws.services.s3.model.ObjectListing.)]
        (when (contains? m :prefix)
          (let [v (:prefix m) k (d/property-to-object (class o) :prefix v java.lang.String)]
            (.setPrefix ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)
            nil))
        (when (contains? m :requesterCharged)
          (let [v (:requesterCharged m)
                k (d/property-to-object (class o) :requesterCharged v java.lang.Boolean/TYPE)]
            (.setRequesterCharged
              ^com.amazonaws.services.s3.model.ObjectListing o
              (boolean (.booleanValue ^java.lang.Boolean k)))
            nil))
        (when (contains? m :bucketName)
          (let [v (:bucketName m)
                k (d/property-to-object (class o) :bucketName v java.lang.String)]
            (.setBucketName ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)
            nil))
        (when (contains? m :encodingType)
          (let [v (:encodingType m)
                k (d/property-to-object (class o) :encodingType v java.lang.String)]
            (.setEncodingType ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)
            nil))
        (when (contains? m :maxKeys)
          (let [v (:maxKeys m)
                k (d/property-to-object (class o) :maxKeys v java.lang.Integer/TYPE)]
            (.setMaxKeys
              ^com.amazonaws.services.s3.model.ObjectListing o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)
            nil))
        (when (contains? m :nextMarker)
          (let [v (:nextMarker m)
                k (d/property-to-object (class o) :nextMarker v java.lang.String)]
            (.setNextMarker ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)
            nil))
        (when (contains? m :truncated)
          (let [v (:truncated m)
                k (d/property-to-object (class o) :truncated v java.lang.Boolean/TYPE)]
            (.setTruncated
              ^com.amazonaws.services.s3.model.ObjectListing o
              (boolean (.booleanValue ^java.lang.Boolean k)))
            nil))
        (when (contains? m :commonPrefixes)
          (let [v (:commonPrefixes m)
                k (d/property-to-object (class o) :commonPrefixes v java.util.List)]
            (.setCommonPrefixes ^com.amazonaws.services.s3.model.ObjectListing o ^java.util.List k)
            nil))
        (when (contains? m :delimiter)
          (let [v (:delimiter m) k (d/property-to-object (class o) :delimiter v java.lang.String)]
            (.setDelimiter ^com.amazonaws.services.s3.model.ObjectListing o ^java.lang.String k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.s3.model.ObjectMetadata]
    fn__23220
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:userMetadata :contentDisposition :serverSideEncryption
                                     :httpExpiresDate :contentEncoding :expirationTime
                                     :contentLanguage :cacheControl :lastModified
                                     :expirationTimeRuleId :bucketKeyEnabled :requesterCharged
                                     :sSECustomerAlgorithm :restoreExpirationTime :contentLength
                                     :contentType :contentMD5 :ongoingRestore :sSECustomerKeyMd5
                                     :sSEAlgorithm}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
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
                 :constructor com.amazonaws.services.s3.model.ObjectMetadata})))))
      (let [o (com.amazonaws.services.s3.model.ObjectMetadata.)]
        (when (contains? m :contentEncoding)
          (let [v (:contentEncoding m)
                k (d/property-to-object (class o) :contentEncoding v java.lang.String)]
            (.setContentEncoding
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)
            nil))
        (when (contains? m :expirationTimeRuleId)
          (let [v (:expirationTimeRuleId m)
                k (d/property-to-object (class o) :expirationTimeRuleId v java.lang.String)]
            (.setExpirationTimeRuleId
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)
            nil))
        (when (contains? m :cacheControl)
          (let [v (:cacheControl m)
                k (d/property-to-object (class o) :cacheControl v java.lang.String)]
            (.setCacheControl
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)
            nil))
        (when (contains? m :requesterCharged)
          (let [v (:requesterCharged m)
                k (d/property-to-object (class o) :requesterCharged v java.lang.Boolean/TYPE)]
            (.setRequesterCharged
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              (boolean (.booleanValue ^java.lang.Boolean k)))
            nil))
        (when (contains? m :restoreExpirationTime)
          (let [v (:restoreExpirationTime m)
                k (d/property-to-object (class o) :restoreExpirationTime v java.util.Date)]
            (.setRestoreExpirationTime
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.util.Date k)
            nil))
        (when (contains? m :contentType)
          (let [v (:contentType m)
                k (d/property-to-object (class o) :contentType v java.lang.String)]
            (.setContentType ^com.amazonaws.services.s3.model.ObjectMetadata o ^java.lang.String k)
            nil))
        (when (contains? m :contentLength)
          (let [v (:contentLength m)
                k (d/property-to-object (class o) :contentLength v java.lang.Long/TYPE)]
            (.setContentLength
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              (long ^java.lang.Number k))
            nil))
        (when (contains? m :sSECustomerKeyMd5)
          (let [v (:sSECustomerKeyMd5 m)
                k (d/property-to-object (class o) :sSECustomerKeyMd5 v java.lang.String)]
            (.setSSECustomerKeyMd5
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)
            nil))
        (when (contains? m :ongoingRestore)
          (let [v (:ongoingRestore m)
                k (d/property-to-object (class o) :ongoingRestore v java.lang.Boolean/TYPE)]
            (.setOngoingRestore
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              (boolean (.booleanValue ^java.lang.Boolean k)))
            nil))
        (when (contains? m :expirationTime)
          (let [v (:expirationTime m)
                k (d/property-to-object (class o) :expirationTime v java.util.Date)]
            (.setExpirationTime
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.util.Date k)
            nil))
        (when (contains? m :userMetadata)
          (let [v (:userMetadata m)
                k (d/property-to-object (class o) :userMetadata v java.util.Map)]
            (.setUserMetadata ^com.amazonaws.services.s3.model.ObjectMetadata o ^java.util.Map k)
            nil))
        (when (contains? m :contentLanguage)
          (let [v (:contentLanguage m)
                k (d/property-to-object (class o) :contentLanguage v java.lang.String)]
            (.setContentLanguage
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)
            nil))
        (when (contains? m :lastModified)
          (let [v (:lastModified m)
                k (d/property-to-object (class o) :lastModified v java.util.Date)]
            (.setLastModified ^com.amazonaws.services.s3.model.ObjectMetadata o ^java.util.Date k)
            nil))
        (when (contains? m :contentMD5)
          (let [v (:contentMD5 m)
                k (d/property-to-object (class o) :contentMD5 v java.lang.String)]
            (.setContentMD5 ^com.amazonaws.services.s3.model.ObjectMetadata o ^java.lang.String k)
            nil))
        (when (contains? m :contentDisposition)
          (let [v (:contentDisposition m)
                k (d/property-to-object (class o) :contentDisposition v java.lang.String)]
            (.setContentDisposition
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)
            nil))
        (when (contains? m :serverSideEncryption)
          (let [v (:serverSideEncryption m)
                k (d/property-to-object (class o) :serverSideEncryption v java.lang.String)]
            (.setServerSideEncryption
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)
            nil))
        (when (contains? m :sSEAlgorithm)
          (let [v (:sSEAlgorithm m)
                k (d/property-to-object (class o) :sSEAlgorithm v java.lang.String)]
            (.setSSEAlgorithm
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)
            nil))
        (when (contains? m :sSECustomerAlgorithm)
          (let [v (:sSECustomerAlgorithm m)
                k (d/property-to-object (class o) :sSECustomerAlgorithm v java.lang.String)]
            (.setSSECustomerAlgorithm
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.String k)
            nil))
        (when (contains? m :httpExpiresDate)
          (let [v (:httpExpiresDate m)
                k (d/property-to-object (class o) :httpExpiresDate v java.util.Date)]
            (.setHttpExpiresDate
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.util.Date k)
            nil))
        (when (contains? m :bucketKeyEnabled)
          (let [v (:bucketKeyEnabled m)
                k (d/property-to-object (class o) :bucketKeyEnabled v java.lang.Boolean)]
            (.setBucketKeyEnabled
              ^com.amazonaws.services.s3.model.ObjectMetadata o
              ^java.lang.Boolean k)
            nil))
        o)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.s3.model.DeleteObjectsResult :getDeletedObjects]
    com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.s3.model.DeleteObjectsResult :getDeletedObjects]
    fn__23223
    ([_ _ val _]
      (mapv
        (fn fn__23224
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
    fn__23227
    ([_ _ val _]
      (mapv
        (fn fn__23228
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
    fn__23231
    ([_ _ val _]
      (mapv
        (fn fn__23232 ([item] (d/data-to-object item com.amazonaws.services.s3.model.Bucket)))
        val)))
  (defn list-buckets
    ([o] (d/object-to-data (.listBuckets ^com.amazonaws.services.s3.AmazonS3Client o))))
  (reset-meta!
    #'list-buckets
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client})]),
       :column 1}
      :name
      'list-buckets
      :ns
      *ns*))
  (defn list-objects
    ([o x1]
      (d/object-to-data
        (.listObjects
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)))))
  (reset-meta!
    #'list-objects
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
       :column 1}
      :name
      'list-objects
      :ns
      *ns*))
  (defn list-next-batch-of-objects
    ([o x1]
      (d/object-to-data
        (.listNextBatchOfObjects
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 com.amazonaws.services.s3.model.ObjectListing)))))
  (reset-meta!
    #'list-next-batch-of-objects
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
       :column 1}
      :name
      'list-next-batch-of-objects
      :ns
      *ns*))
  (defn list-objects-from-request
    ([o x1]
      (d/object-to-data
        (.listObjects
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 com.amazonaws.services.s3.model.ListObjectsRequest)))))
  (reset-meta!
    #'list-objects-from-request
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
       :column 1}
      :name
      'list-objects-from-request
      :ns
      *ns*))
  (defn create-bucket
    ([o x1]
      (d/object-to-data
        (.createBucket
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)))))
  (reset-meta!
    #'create-bucket
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
       :column 1}
      :name
      'create-bucket
      :ns
      *ns*))
  (defn create-bucket-in-region
    ([o x1 x2]
      (d/object-to-data
        (.createBucket
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)
          (d/data-to-object x2 java.lang.String)))))
  (reset-meta!
    #'create-bucket-in-region
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
       :column 1}
      :name
      'create-bucket-in-region
      :ns
      *ns*))
  (defn delete-bucket
    ([o x1]
      (.deleteBucket
        ^com.amazonaws.services.s3.AmazonS3Client o
        (d/data-to-object x1 java.lang.String))
      :ok))
  (reset-meta!
    #'delete-bucket
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
       :column 1}
      :name
      'delete-bucket
      :ns
      *ns*))
  (defn put-file
    ([o x1 x2 x3]
      (d/object-to-data
        (.putObject
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)
          (d/data-to-object x2 java.lang.String)
          (d/data-to-object x3 java.io.File)))))
  (reset-meta!
    #'put-file
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2 'x3]),
       :column 1}
      :name
      'put-file
      :ns
      *ns*))
  (defn put-object-with-canned-acl
    ([o x1]
      (d/object-to-data
        (.putObject
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 com.amazonaws.services.s3.model.PutObjectRequest)))))
  (reset-meta!
    #'put-object-with-canned-acl
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
       :column 1}
      :name
      'put-object-with-canned-acl
      :ns
      *ns*))
  (defn put-object
    ([o x1 x2 x3 x4]
      (d/object-to-data
        (.putObject
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)
          (d/data-to-object x2 java.lang.String)
          (d/data-to-object x3 java.io.InputStream)
          (d/data-to-object x4 com.amazonaws.services.s3.model.ObjectMetadata)))))
  (reset-meta!
    #'put-object
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2 'x3 'x4]),
       :column 1}
      :name
      'put-object
      :ns
      *ns*))
  (defn get-object
    ([o x1 x2]
      (d/object-to-data
        (.getObject
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)
          (d/data-to-object x2 java.lang.String)))))
  (reset-meta!
    #'get-object
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
       :column 1}
      :name
      'get-object
      :ns
      *ns*))
  (defn get-object-metadata
    ([o x1 x2]
      (d/object-to-data
        (.getObjectMetadata
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)
          (d/data-to-object x2 java.lang.String)))))
  (reset-meta!
    #'get-object-metadata
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
       :column 1}
      :name
      'get-object-metadata
      :ns
      *ns*))
  (defn set-bucket-policy
    ([o x1 x2]
      (.setBucketPolicy
        ^com.amazonaws.services.s3.AmazonS3Client o
        (d/data-to-object x1 java.lang.String)
        (d/data-to-object x2 java.lang.String))
      :ok))
  (reset-meta!
    #'set-bucket-policy
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
       :column 1}
      :name
      'set-bucket-policy
      :ns
      *ns*))
  (defn get-bucket-policy
    ([o x1]
      (d/object-to-data
        (.getBucketPolicy
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)))))
  (reset-meta!
    #'get-bucket-policy
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
       :column 1}
      :name
      'get-bucket-policy
      :ns
      *ns*))
  (defn delete-object
    ([o x1 x2]
      (.deleteObject
        ^com.amazonaws.services.s3.AmazonS3Client o
        (d/data-to-object x1 java.lang.String)
        (d/data-to-object x2 java.lang.String))
      :ok))
  (reset-meta!
    #'delete-object
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2]),
       :column 1}
      :name
      'delete-object
      :ns
      *ns*))
  (defn delete-objects
    ([o x1]
      (d/object-to-data
        (.deleteObjects
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 com.amazonaws.services.s3.model.DeleteObjectsRequest)))))
  (reset-meta!
    #'delete-objects
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1]),
       :column 1}
      :name
      'delete-objects
      :ns
      *ns*))
  (defn generate-presigned-url
    ([o x1 x2 x3 x4]
      (d/object-to-data
        (.generatePresignedUrl
          ^com.amazonaws.services.s3.AmazonS3Client o
          (d/data-to-object x1 java.lang.String)
          (d/data-to-object x2 java.lang.String)
          (d/data-to-object x3 java.util.Date)
          (d/data-to-object x4 com.amazonaws.HttpMethod)))))
  (reset-meta!
    #'generate-presigned-url
    (assoc
      {:related-class com.amazonaws.services.s3.AmazonS3Client,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.s3.AmazonS3Client}) 'x1 'x2 'x3 'x4]),
       :column 1}
      :name
      'generate-presigned-url
      :ns
      *ns*)))