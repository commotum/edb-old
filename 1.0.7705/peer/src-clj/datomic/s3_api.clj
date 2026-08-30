(do
  (clojure.core/in-ns 'datomic.s3-api)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.core2.aws.helpers :as 'aws-helpers]
        ['datomic.aws.client.api :as 'aws])
      (clojure.core/import 'software.amazon.awssdk.services.s3.S3Client)))
  (when-not (.equals 'datomic.s3-api 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.s3-api))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.core2.aws.helpers :as 'aws-helpers]
          ['datomic.aws.client.api :as 'aws])
        (clojure.core/import 'software.amazon.awssdk.services.s3.S3Client))))
  (set! *warn-on-reflection* true)
  (def client
   (fn client
     ([creds opts] (aws-helpers/sync-client (S3Client/builder) creds opts))
     ([opts] (aws-helpers/sync-client (S3Client/builder) opts))
     ([] (aws-helpers/sync-client (S3Client/builder) {}))))
  (reset-meta!
    #'client
    (assoc
      {:arglists (clojure.core/list [] ['opts] ['creds 'opts]), :column (int 1)}
      :name
      'client
      :ns
      *ns*))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :ListBuckets]
    fn__20620
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__20621
         ([client20618 G__20619]
           (.listBuckets
             ^software.amazon.awssdk.services.s3.S3Client client20618
             ^software.amazon.awssdk.services.s3.model.ListBucketsRequest G__20619))),
       :ret-mode :sync,
       :request-builder
       (fn fn__20623 ([] (software.amazon.awssdk.services.s3.model.ListBucketsRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :ListObjectsV2]
    fn__20628
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__20629
         ([client20626 G__20627]
           (.listObjectsV2
             ^software.amazon.awssdk.services.s3.S3Client client20626
             ^software.amazon.awssdk.services.s3.model.ListObjectsV2Request G__20627))),
       :ret-mode :sync,
       :request-builder
       (fn fn__20631
         ([] (software.amazon.awssdk.services.s3.model.ListObjectsV2Request/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :CreateBucket]
    fn__20636
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__20637
         ([client20634 G__20635]
           (.createBucket
             ^software.amazon.awssdk.services.s3.S3Client client20634
             ^software.amazon.awssdk.services.s3.model.CreateBucketRequest G__20635))),
       :ret-mode :sync,
       :request-builder
       (fn fn__20639
         ([] (software.amazon.awssdk.services.s3.model.CreateBucketRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :DeleteBucket]
    fn__20644
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__20645
         ([client20642 G__20643]
           (.deleteBucket
             ^software.amazon.awssdk.services.s3.S3Client client20642
             ^software.amazon.awssdk.services.s3.model.DeleteBucketRequest G__20643))),
       :ret-mode :sync,
       :request-builder
       (fn fn__20647
         ([] (software.amazon.awssdk.services.s3.model.DeleteBucketRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :PutObject]
    fn__20653
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :blob-request,
       :method
       (fn fn__20654
         ([client20650 G__20651 G__20652]
           (.putObject
             ^software.amazon.awssdk.services.s3.S3Client client20650
             ^software.amazon.awssdk.services.s3.model.PutObjectRequest G__20651
             ^software.amazon.awssdk.core.sync.RequestBody G__20652))),
       :ret-mode :sync,
       :request-builder
       (fn fn__20656 ([] (software.amazon.awssdk.services.s3.model.PutObjectRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :GetObject]
    fn__20662
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :blob-response,
       :method
       (fn fn__20663
         ([client20659 G__20660 G__20661]
           (.getObject
             ^software.amazon.awssdk.services.s3.S3Client client20659
             ^software.amazon.awssdk.services.s3.model.GetObjectRequest G__20660
             ^software.amazon.awssdk.core.sync.ResponseTransformer G__20661))),
       :ret-mode :sync,
       :request-builder
       (fn fn__20665 ([] (software.amazon.awssdk.services.s3.model.GetObjectRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :HeadObject]
    fn__20670
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__20671
         ([client20668 G__20669]
           (.headObject
             ^software.amazon.awssdk.services.s3.S3Client client20668
             ^software.amazon.awssdk.services.s3.model.HeadObjectRequest G__20669))),
       :ret-mode :sync,
       :request-builder
       (fn fn__20673 ([] (software.amazon.awssdk.services.s3.model.HeadObjectRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :GetBucketPolicy]
    fn__20678
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__20679
         ([client20676 G__20677]
           (.getBucketPolicy
             ^software.amazon.awssdk.services.s3.S3Client client20676
             ^software.amazon.awssdk.services.s3.model.GetBucketPolicyRequest G__20677))),
       :ret-mode :sync,
       :request-builder
       (fn fn__20681
         ([] (software.amazon.awssdk.services.s3.model.GetBucketPolicyRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :DeleteObject]
    fn__20686
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__20687
         ([client20684 G__20685]
           (.deleteObject
             ^software.amazon.awssdk.services.s3.S3Client client20684
             ^software.amazon.awssdk.services.s3.model.DeleteObjectRequest G__20685))),
       :ret-mode :sync,
       :request-builder
       (fn fn__20689
         ([] (software.amazon.awssdk.services.s3.model.DeleteObjectRequest/builder)))})))