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
  (defn client
    ([creds opts] (aws-helpers/sync-client (S3Client/builder) creds opts))
    ([opts] (aws-helpers/sync-client (S3Client/builder) opts))
    ([] (aws-helpers/sync-client (S3Client/builder) {})))
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
    fn__26602
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26603
         ([client26600 G__26601]
           (.listBuckets
             ^software.amazon.awssdk.services.s3.S3Client client26600
             ^software.amazon.awssdk.services.s3.model.ListBucketsRequest G__26601))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26605 ([] (software.amazon.awssdk.services.s3.model.ListBucketsRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :ListObjectsV2]
    fn__26610
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26611
         ([client26608 G__26609]
           (.listObjectsV2
             ^software.amazon.awssdk.services.s3.S3Client client26608
             ^software.amazon.awssdk.services.s3.model.ListObjectsV2Request G__26609))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26613
         ([] (software.amazon.awssdk.services.s3.model.ListObjectsV2Request/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :CreateBucket]
    fn__26618
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26619
         ([client26616 G__26617]
           (.createBucket
             ^software.amazon.awssdk.services.s3.S3Client client26616
             ^software.amazon.awssdk.services.s3.model.CreateBucketRequest G__26617))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26621
         ([] (software.amazon.awssdk.services.s3.model.CreateBucketRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :DeleteBucket]
    fn__26626
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26627
         ([client26624 G__26625]
           (.deleteBucket
             ^software.amazon.awssdk.services.s3.S3Client client26624
             ^software.amazon.awssdk.services.s3.model.DeleteBucketRequest G__26625))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26629
         ([] (software.amazon.awssdk.services.s3.model.DeleteBucketRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :PutObject]
    fn__26635
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :blob-request,
       :method
       (fn fn__26636
         ([client26632 G__26633 G__26634]
           (.putObject
             ^software.amazon.awssdk.services.s3.S3Client client26632
             ^software.amazon.awssdk.services.s3.model.PutObjectRequest G__26633
             ^software.amazon.awssdk.core.sync.RequestBody G__26634))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26638 ([] (software.amazon.awssdk.services.s3.model.PutObjectRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :GetObject]
    fn__26644
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :blob-response,
       :method
       (fn fn__26645
         ([client26641 G__26642 G__26643]
           (.getObject
             ^software.amazon.awssdk.services.s3.model.GetObjectRequest G__26642
             G__26643
             (if (instance? clojure.lang.IFn G__26643)
               (instance? software.amazon.awssdk.core.sync.ResponseTransformer G__26643)
               G__26643)))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26647 ([] (software.amazon.awssdk.services.s3.model.GetObjectRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :HeadObject]
    fn__26652
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26653
         ([client26650 G__26651]
           (.headObject
             ^software.amazon.awssdk.services.s3.S3Client client26650
             ^software.amazon.awssdk.services.s3.model.HeadObjectRequest G__26651))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26655 ([] (software.amazon.awssdk.services.s3.model.HeadObjectRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :GetBucketPolicy]
    fn__26660
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26661
         ([client26658 G__26659]
           (.getBucketPolicy
             ^software.amazon.awssdk.services.s3.S3Client client26658
             ^software.amazon.awssdk.services.s3.model.GetBucketPolicyRequest G__26659))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26663
         ([] (software.amazon.awssdk.services.s3.model.GetBucketPolicyRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.s3.S3Client :DeleteObject]
    fn__26668
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26669
         ([client26666 G__26667]
           (.deleteObject
             ^software.amazon.awssdk.services.s3.S3Client client26666
             ^software.amazon.awssdk.services.s3.model.DeleteObjectRequest G__26667))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26671
         ([] (software.amazon.awssdk.services.s3.model.DeleteObjectRequest/builder)))})))