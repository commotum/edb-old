(do
  (clojure.core/in-ns 'datomic.s3)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.s3)
    {:doc
     "Amazon S3 operations used by backup, value storage, and log rotation. Provides bucket discovery, paginated listing, object transfer, signed reads, and optional server-side encryption."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.core2.aws.helpers :as 'aws]
        ['datomic.aws.client.datafy :as 'datafy]
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.s3-api :as 'api]
        ['datomic.io :as 'io]
        ['clojure.java.io :as 'jio])
      (clojure.core/import 'java.time.Duration)
      (clojure.core/import 'java.io.InputStream)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'software.amazon.awssdk.auth.credentials.AwsCredentialsProvider)
      (clojure.core/import 'software.amazon.awssdk.services.s3.S3Client)
      (clojure.core/import 'software.amazon.awssdk.services.s3.model.GetObjectRequest)
      (clojure.core/import 'software.amazon.awssdk.services.s3.presigner.S3Presigner)
      (clojure.core/import
        'software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest)))
  (when-not (.equals 'datomic.s3 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.s3))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.core2.aws.helpers :as 'aws]
          ['datomic.aws.client.datafy :as 'datafy]
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.s3-api :as 'api]
          ['datomic.io :as 'io]
          ['clojure.java.io :as 'jio])
        (clojure.core/import 'java.time.Duration)
        (clojure.core/import 'java.io.InputStream)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'software.amazon.awssdk.auth.credentials.AwsCredentialsProvider)
        (clojure.core/import 'software.amazon.awssdk.services.s3.S3Client)
        (clojure.core/import 'software.amazon.awssdk.services.s3.model.GetObjectRequest)
        (clojure.core/import 'software.amazon.awssdk.services.s3.presigner.S3Presigner)
        (clojure.core/import
          'software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest))))
  (set! *warn-on-reflection* true)
  (defn s3-service ([& args] (apply api/client args)))
  (reset-meta!
    #'s3-service
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name 's3-service :ns *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      Name
      (s3-name
        [x]
        "Get the string name from an object that can be used as\n    a name in an S3 context."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.s3" "Name")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Name :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 's3-name {:arglists (clojure.core/list ['x])}),
                                      :arglists (clojure.core/list ['x]),
                                      :doc
                                      "Get the string name from an object that can be used as\n    a name in an S3 context."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.s3" "Name"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.s3" "s3-name")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (extend java.lang.String Name {:s3-name (fn fn__21717 ([item] item))})
  (extend clojure.lang.APersistentMap Name {:s3-name (fn fn__21719 ([item] (:Name item)))})
  ;; Creates a time-limited signed GET URL using the client's configured region and credentials.
  (defn signed-get-url
    ([s3 bucket path duration_days]
      (let [conf (.serviceClientConfiguration ^software.amazon.awssdk.services.s3.S3Client s3)
            presigner (.build
                        (.credentialsProvider
                          (.region
                            (S3Presigner/builder)
                            (.region
                              ^software.amazon.awssdk.awscore.AwsServiceClientConfiguration conf))
                          (.credentialsProvider
                            ^software.amazon.awssdk.awscore.AwsServiceClientConfiguration conf)))
            presign_request (.build
                              (.signatureDuration
                                (.getObjectRequest
                                  (GetObjectPresignRequest/builder)
                                  (datafy/clj->sdk
                                    (GetObjectRequest/builder)
                                    {:Bucket bucket, :Key path}))
                                (Duration/ofDays (long ^java.lang.Number duration_days))))
            presigned_url (.presignGetObject
                            ^software.amazon.awssdk.services.s3.presigner.S3Presigner presigner
                            ^software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest presign_request)]
        (.close ^software.amazon.awssdk.awscore.presigner.SdkPresigner presigner)
        (let [_ nil]
          (.url ^software.amazon.awssdk.awscore.presigner.PresignedRequest presigned_url)))))
  (reset-meta!
    #'signed-get-url
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 's3 {:tag 'S3Client}) 'bucket 'path 'duration-days]),
       :column (int 1)}
      :name
      'signed-get-url
      :ns
      *ns*))
  (defn get-bucket
    ([s3 bucket_name]
      (first
        (filter
          (fn fn__21722 ([b] (= (:Name b) bucket_name)))
          (:Buckets (aws/invoke s3 {:op :ListBuckets, :req {:Prefix bucket_name}}))))))
  (reset-meta!
    #'get-bucket
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket-name]), :column (int 1)}
      :name
      'get-bucket
      :ns
      *ns*))
  ;; Returns an existing bucket or creates it in the client's configured region.
  (defn ensure-bucket
    ([s3 bucket]
      (when-not (get-bucket s3 bucket)
        (aws/invoke s3 {:op :CreateBucket, :req {:Bucket bucket}}))))
  (reset-meta!
    #'ensure-bucket
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket]), :column (int 1)}
      :name
      'ensure-bucket
      :ns
      *ns*))
  ;; Lazily follows continuation tokens and returns every object beneath the requested prefix.
  (defn list-objects
    ([s3 bucket prefix delimiter]
      (let [s3_bucket (s3-name bucket)]
        (sequence
          cat
          (iteration
            (fn fn__21726
              ([token]
                (aws/invoke
                  s3
                  {:op :ListObjectsV2,
                   :req
                   (cond->
                     {:Bucket s3_bucket}
                     prefix
                     (assoc :Prefix prefix)
                     delimiter
                     (assoc :Delimiter delimiter)
                     token
                     (assoc :ContinuationToken token))})))
            :kf
            :NextContinuationToken
            :vf
            :Contents))))
    ([s3 bucket] (list-objects s3 bucket nil nil)))
  (reset-meta!
    #'list-objects
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket] ['s3 'bucket 'prefix 'delimiter]),
       :column (int 1)}
      :name
      'list-objects
      :ns
      *ns*))
  (defn delete-object
    ([s3 bucket key]
      (aws/invoke s3 {:op :DeleteObject, :req {:Bucket (s3-name bucket), :Key (s3-name key)}})))
  (reset-meta!
    #'delete-object
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'key]), :column (int 1)}
      :name
      'delete-object
      :ns
      *ns*))
  (defn object-exists?
    ([s3 bucket k]
      (try
        (do
          (aws/invoke s3 {:op :HeadObject, :req {:Bucket (s3-name bucket), :Key k}})
          {:exists true})
        (catch
          java.lang.Exception
          ex
          (if (canom/not-found? (ex-data ex))
            {:exists false}
            (do (throw ^java.lang.Throwable ex) nil))))))
  (reset-meta!
    #'object-exists?
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'k]), :column (int 1)}
      :name
      'object-exists?
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3" "BUCKET_OWNER_FULL_ACCESS_CANNED_ACL")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3" "BUCKET_OWNER_FULL_ACCESS_CANNED_ACL")
    (str software.amazon.awssdk.services.s3.model.ObjectCannedACL/BUCKET_OWNER_FULL_CONTROL))
  (defn put-file-as
    ([s3 bucket f path opts]
      (aws/invoke
        s3
        {:op :PutObject,
         :req (merge opts {:Bucket (s3-name bucket), :Key path}),
         :body (jio/file f)}))
    ([s3 bucket f path] (put-file-as s3 bucket f path nil)))
  (reset-meta!
    #'put-file-as
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'f 'path] ['s3 'bucket 'f 'path 'opts]),
       :column (int 1)}
      :name
      'put-file-as
      :ns
      *ns*))
  ;; Uploads a ByteBuffer and optionally requests S3 server-side encryption.
  (defn put-object
    ([s3 bucket path bytes opts]
      (aws/invoke
        s3
        {:op :PutObject, :req (merge opts {:Bucket (s3-name bucket), :Key path}), :body bytes})))
  (reset-meta!
    #'put-object
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path 'bytes 'opts]), :column (int 1)}
      :name
      'put-object
      :ns
      *ns*))
  (defn get-object
    ([s3 bucket path response_as]
      (try
        (aws/invoke
          s3
          {:op :GetObject,
           :req {:Bucket (s3-name bucket), :Key (s3-name path)},
           :response-as response_as})
        (catch
          java.lang.Exception
          ex
          (when-not (canom/not-found? (ex-data ex)) (throw ^java.lang.Throwable ex) nil))))
    ([s3 bucket path] (get-object s3 bucket path :bytes)))
  (reset-meta!
    #'get-object
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path] ['s3 'bucket 'path 'response-as]),
       :column (int 1)}
      :name
      'get-object
      :ns
      *ns*))
  (defn get-non-direct-buffer
    ([s3 bucket path]
      (let [temp__5804__auto__ (:body (get-object s3 bucket path :bytes))]
        (when temp__5804__auto__ (let [b temp__5804__auto__] (ByteBuffer/wrap ^bytes b))))))
  (reset-meta!
    #'get-non-direct-buffer
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path]), :column (int 1)}
      :name
      'get-non-direct-buffer
      :ns
      *ns*))
  (defn get-file
    ([s3 bucket path dest]
      (let [temp__5804__auto__ (get-object s3 (s3-name bucket) (s3-name path) :input-stream)]
        (when temp__5804__auto__
          (let [obj temp__5804__auto__]
            (with-open [is (:body obj)] (jio/copy is (jio/as-file dest))))))))
  (reset-meta!
    #'get-file
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path 'dest]), :column (int 1)}
      :name
      'get-file
      :ns
      *ns*))
  (defn put-clj
    ([s3 bucket key obj]
      (binding [*print-length* nil *print-level* nil]
        (let [s (pr-str obj) bytes (.getBytes ^java.lang.String s "UTF-8")]
          (put-object
            s3
            bucket
            key
            bytes
            {:ContentLength (java.lang.Integer/valueOf (int (count bytes))),
             :ContentEncoding "text/plain; charset=utf-8"})))))
  (reset-meta!
    #'put-clj
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'key 'obj]), :column (int 1)}
      :name
      'put-clj
      :ns
      *ns*)))
