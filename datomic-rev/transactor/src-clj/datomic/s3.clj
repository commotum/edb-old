(do
  (clojure.core/in-ns 'datomic.s3)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.s3-api :as 'api]
        ['datomic.datafy :as 'datafy]
        ['datomic.io :as 'io]
        ['clojure.java.io :as 'jio]
        ['clojure.string :as 's])
      (clojure.core/import 'java.util.UUID)
      (clojure.core/import 'java.util.Date)
      (clojure.core/import 'com.amazonaws.auth.InstanceProfileCredentialsProvider)
      (clojure.core/import 'com.amazonaws.services.s3.model.S3Object)
      (clojure.core/import 'com.amazonaws.services.s3.model.AmazonS3Exception)
      (clojure.core/import 'com.amazonaws.services.s3.model.MultiObjectDeleteException)
      (clojure.core/import 'com.amazonaws.services.s3.model.DeleteObjectsRequest)
      (clojure.core/import 'com.amazonaws.services.s3.model.PutObjectRequest)
      (clojure.core/import 'com.amazonaws.services.s3.model.CannedAccessControlList)
      (clojure.core/import 'com.amazonaws.services.s3.model.ObjectMetadata)
      (clojure.core/import 'com.amazonaws.HttpMethod)
      (clojure.core/import 'java.io.ByteArrayInputStream)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.net.URL)
      (clojure.core/import 'java.net.URLConnection)
      (clojure.core/import 'java.net.HttpURLConnection)))
  (when-not (.equals 'datomic.s3 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.s3))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.s3-api :as 'api]
          ['datomic.datafy :as 'datafy]
          ['datomic.io :as 'io]
          ['clojure.java.io :as 'jio]
          ['clojure.string :as 's])
        (clojure.core/import 'java.util.UUID)
        (clojure.core/import 'java.util.Date)
        (clojure.core/import 'com.amazonaws.auth.InstanceProfileCredentialsProvider)
        (clojure.core/import 'com.amazonaws.services.s3.model.S3Object)
        (clojure.core/import 'com.amazonaws.services.s3.model.AmazonS3Exception)
        (clojure.core/import 'com.amazonaws.services.s3.model.MultiObjectDeleteException)
        (clojure.core/import 'com.amazonaws.services.s3.model.DeleteObjectsRequest)
        (clojure.core/import 'com.amazonaws.services.s3.model.PutObjectRequest)
        (clojure.core/import 'com.amazonaws.services.s3.model.CannedAccessControlList)
        (clojure.core/import 'com.amazonaws.services.s3.model.ObjectMetadata)
        (clojure.core/import 'com.amazonaws.HttpMethod)
        (clojure.core/import 'java.io.ByteArrayInputStream)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.net.URL)
        (clojure.core/import 'java.net.URLConnection)
        (clojure.core/import 'java.net.HttpURLConnection))))
  (set! *warn-on-reflection* true)
  (defn s3-service ([& args] (apply api/client args)))
  (reset-meta!
    #'s3-service
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name 's3-service :ns *ns*))
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol
      Name
      (s3-name
        [x]
        "Get the string name from an object that can be used as\n    a name in an S3 context."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.s3" "Name")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'Name :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 's3-name {:arglists (clojure.core/list ['x])}),
                                      :arglists (clojure.core/list ['x]),
                                      :doc
                                      "Get the string name from an object that can be used as\n    a name in an S3 context."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.s3" "Name"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.s3" "s3-name")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*))))
  (extend java.lang.String Name {:s3-name (fn fn__26343 ([item] item))})
  (extend clojure.lang.APersistentMap Name {:s3-name (fn fn__26345 ([item] (:name item)))})
  (defn years-from-now
    ([n]
      (java.util.Date.
        (long
          (+ (-> (* n 1000) (* 60) (* 60) (* 24) (* 365)) (java.lang.System/currentTimeMillis))))))
  (reset-meta!
    #'years-from-now
    (assoc {:arglists (clojure.core/list ['n]), :column (int 1)} :name 'years-from-now :ns *ns*))
  (def create-bucket
   (fn create_bucket
     ([s3 bucket region] (api/create-bucket s3 bucket region))
     ([s3 bucket] (api/create-bucket s3 bucket))))
  (reset-meta!
    #'create-bucket
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket] ['s3 'bucket 'region]), :column (int 1)}
      :name
      'create-bucket
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.s3" "get-bucket") {:declared true, :column (int 1)})
  (def ensure-bucket
   (fn ensure_bucket
     ([s3 bucket region] (when-not (get-bucket s3 bucket) (create-bucket s3 bucket region)))
     ([s3 bucket] (when-not (get-bucket s3 bucket) (create-bucket s3 bucket)))))
  (reset-meta!
    #'ensure-bucket
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket] ['s3 'bucket 'region]), :column (int 1)}
      :name
      'ensure-bucket
      :ns
      *ns*))
  (defn new-bucket ([s3 basename] (create-bucket s3 (str basename "-" (UUID/randomUUID)))))
  (reset-meta!
    #'new-bucket
    (assoc
      {:arglists (clojure.core/list ['s3 'basename]), :column (int 1)}
      :name
      'new-bucket
      :ns
      *ns*))
  (defn debug ([x] (clojure.pprint/pprint x) x))
  (reset-meta!
    #'debug
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'debug :ns *ns*))
  (def signed-url
   (fn signed_url
     ([s3 method bucket key expiry headers virtual_host?]
       (let [bucket_name (s3-name bucket)
             key_name (s3-name key)
             method (let [G__26352 method]
                      (case
                        G__26352
                        :get
                        HttpMethod/GET
                        :delete
                        HttpMethod/DELETE
                        :put
                        HttpMethod/PUT
                        :head
                        HttpMethod/HEAD))]
         (api/generate-presigned-url s3 bucket_name key_name expiry method)))
     ([s3 method bucket key expiry] (signed-url s3 method bucket key expiry {} false))))
  (reset-meta!
    #'signed-url
    (assoc
      {:arglists
       (clojure.core/list
         ['s3 'method 'bucket 'key 'expiry]
         ['s3 'method 'bucket 'key 'expiry 'headers 'virtual-host?]),
       :column (int 1)}
      :name
      'signed-url
      :ns
      *ns*))
  (defn -list-objects-seq
    ([s3 results]
      (if (:truncated results)
        (cons
          (:objectSummaries results)
          (lazy-seq
            (-list-objects-seq
              s3
              (api/list-next-batch-of-objects s3 (dissoc results :objectSummaries)))))
        (cons (:objectSummaries results) nil))))
  (reset-meta!
    #'-list-objects-seq
    (assoc
      {:arglists (clojure.core/list ['s3 'results]), :column (int 1)}
      :name
      '-list-objects-seq
      :ns
      *ns*))
  (def list-objects
   (fn list_objects
     ([s3 bucket prefix delimiter]
       (apply
         concat
         (-list-objects-seq
           s3
           (api/list-objects-from-request
             s3
             {:prefix prefix, :delimiter delimiter, :bucketName (s3-name bucket)}))))
     ([s3 bucket]
       (let [result (api/list-objects s3 (s3-name bucket))]
         (apply concat (-list-objects-seq s3 result))))))
  (reset-meta!
    #'list-objects
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket] ['s3 'bucket 'prefix 'delimiter]),
       :column (int 1)}
      :name
      'list-objects
      :ns
      *ns*))
  (def get-bucket
   (fn get_bucket
     ([s3 bucket_name]
       (first (filter (fn fn__26358 ([b] (= (:name b) bucket_name))) (api/list-buckets s3))))))
  (reset-meta!
    #'get-bucket
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket-name]), :column (int 1)}
      :name
      'get-bucket
      :ns
      *ns*))
  (defn list-buckets ([s3] (api/list-buckets s3)))
  (reset-meta!
    #'list-buckets
    (assoc {:arglists (clojure.core/list ['s3]), :column (int 1)} :name 'list-buckets :ns *ns*))
  (defn delete-object ([s3 bucket key] (api/delete-object s3 (s3-name bucket) (s3-name key))))
  (reset-meta!
    #'delete-object
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'key]), :column (int 1)}
      :name
      'delete-object
      :ns
      *ns*))
  (defn delete-objects
    ([s3 bucket keys]
      (try
        (let [str_keys (into-array java.lang.String (map s3-name keys))]
          (if (seq str_keys)
            (api/delete-objects
              s3
              (.withKeys
                (com.amazonaws.services.s3.model.DeleteObjectsRequest. (s3-name bucket))
                ^"[Ljava.lang.String;" str_keys))
            {:deletedObjects []}))
        (catch
          com.amazonaws.services.s3.model.MultiObjectDeleteException
          e
          {:deletedObjects
           (map
             datafy/object-to-data
             (.getDeletedObjects ^com.amazonaws.services.s3.model.MultiObjectDeleteException e)),
           :errors
           (map
             datafy/object-to-data
             (.getErrors ^com.amazonaws.services.s3.model.MultiObjectDeleteException e))}))))
  (reset-meta!
    #'delete-objects
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'keys]), :column (int 1)}
      :name
      'delete-objects
      :ns
      *ns*))
  (defn delete-all-objects
    ([s3 bucket] (delete-objects s3 bucket (map :key (list-objects s3 bucket)))))
  (reset-meta!
    #'delete-all-objects
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket]), :column (int 1)}
      :name
      'delete-all-objects
      :ns
      *ns*))
  (defn object-exists?
    ([s3 bucket k]
      (try
        (do (api/get-object-metadata s3 (s3-name bucket) k) {:exists true})
        (catch
          com.amazonaws.services.s3.model.AmazonS3Exception
          se
          (if (= 404 (.getStatusCode ^com.amazonaws.AmazonServiceException se))
            {:exists false}
            (do (throw ^java.lang.Throwable se) nil))))))
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
    CannedAccessControlList/BucketOwnerFullControl)
  (def put-file-as
   (fn put_file_as
     ([s3 bucket f path metadata canned_acl]
       (let [req (.withCannedAcl
                   (com.amazonaws.services.s3.model.PutObjectRequest.
                     (s3-name bucket)
                     ^java.lang.String path
                     (jio/input-stream (jio/file f))
                     (datafy/data-to-object
                       metadata
                       com.amazonaws.services.s3.model.ObjectMetadata))
                   ^com.amazonaws.services.s3.model.CannedAccessControlList canned_acl)]
         (api/put-object-with-canned-acl s3 req)))
     ([s3 bucket f path metadata]
       (api/put-object s3 (s3-name bucket) path (jio/input-stream (jio/file f)) metadata))
     ([s3 bucket f path] (api/put-file s3 (s3-name bucket) path (jio/file f)))))
  (reset-meta!
    #'put-file-as
    (assoc
      {:arglists
       (clojure.core/list
         ['s3 'bucket 'f 'path]
         ['s3 'bucket 'f 'path 'metadata]
         ['s3 'bucket 'f 'path 'metadata 'canned-acl]),
       :column (int 1)}
      :name
      'put-file-as
      :ns
      *ns*))
  (defn put-object
    ([s3 bucket path stream metadata] (api/put-object s3 (s3-name bucket) path stream metadata)))
  (reset-meta!
    #'put-object
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path 'stream 'metadata]), :column (int 1)}
      :name
      'put-object
      :ns
      *ns*))
  (defn get-object
    ([s3 bucket path]
      (try
        (api/get-object s3 (s3-name bucket) (s3-name path))
        (catch
          com.amazonaws.services.s3.model.AmazonS3Exception
          se
          (when-not (= 404 (.getStatusCode ^com.amazonaws.AmazonServiceException se))
            (throw ^java.lang.Throwable se)
            nil)))))
  (reset-meta!
    #'get-object
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path]), :column (int 1)}
      :name
      'get-object
      :ns
      *ns*))
  (defn get-direct-buffer
    ([s3 bucket path]
      (let [temp__5804__auto__ (get-object s3 (s3-name bucket) (s3-name path))]
        (when temp__5804__auto__
          (let [obj temp__5804__auto__]
            (with-open [is (.getObjectContent ^com.amazonaws.services.s3.model.S3Object obj)]
              (let [bb (ByteBuffer/allocateDirect
                         (int
                           (.getContentLength
                             (.getObjectMetadata ^com.amazonaws.services.s3.model.S3Object obj))))]
                (io/fill-buffer is bb)
                bb)))))))
  (reset-meta!
    #'get-direct-buffer
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path]), :column (int 1)}
      :name
      'get-direct-buffer
      :ns
      *ns*))
  (defn get-bytes
    ([s3 bucket path]
      (let [temp__5804__auto__ (get-object s3 (s3-name bucket) (s3-name path))]
        (when temp__5804__auto__
          (let [obj temp__5804__auto__]
            (with-open [is (.getObjectContent ^com.amazonaws.services.s3.model.S3Object obj)]
              (let [ba (byte-array
                         (long
                           (.getContentLength
                             (.getObjectMetadata ^com.amazonaws.services.s3.model.S3Object obj))))]
                (io/fill-array is ba)
                ba)))))))
  (reset-meta!
    #'get-bytes
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path]), :column (int 1)}
      :name
      'get-bytes
      :ns
      *ns*))
  (defn get-non-direct-buffer
    ([s3 bucket path]
      (let [temp__5804__auto__ (get-bytes s3 bucket path)]
        (when temp__5804__auto__ (let [b temp__5804__auto__] (ByteBuffer/wrap ^bytes b))))))
  (reset-meta!
    #'get-non-direct-buffer
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path]), :column (int 1)}
      :name
      'get-non-direct-buffer
      :ns
      *ns*))
  (defn delete-bucket ([s3 bucket] (api/delete-bucket s3 (s3-name bucket))))
  (reset-meta!
    #'delete-bucket
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket]), :column (int 1)}
      :name
      'delete-bucket
      :ns
      *ns*))
  (defn destroy-bucket ([s3 bucket] (delete-all-objects s3 bucket) (delete-bucket s3 bucket)))
  (reset-meta!
    #'destroy-bucket
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket]), :column (int 1)}
      :name
      'destroy-bucket
      :ns
      *ns*))
  (def signed-get-clj
   (fn signed_get_clj
     ([url]
       (with-open [is (.openStream ^java.net.URL url)]
         (read (java.io.PushbackReader. (jio/reader is)))))))
  (reset-meta!
    #'signed-get-clj
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'url {:tag 'URL})]), :column (int 1)}
      :name
      'signed-get-clj
      :ns
      *ns*))
  (def signed-get-file
   (fn signed_get_file
     ([url f] (with-open [is (.openStream ^java.net.URL url)] (jio/copy is (jio/as-file f))))))
  (reset-meta!
    #'signed-get-file
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'url {:tag 'URL}) 'f]), :column (int 1)}
      :name
      'signed-get-file
      :ns
      *ns*))
  (defn get-file
    ([s3 bucket path dest]
      (let [temp__5804__auto__ (get-object s3 (s3-name bucket) (s3-name path))]
        (when temp__5804__auto__
          (let [obj temp__5804__auto__]
            (with-open [is (.getObjectContent ^com.amazonaws.services.s3.model.S3Object obj)]
              (jio/copy is (jio/as-file dest))))))))
  (reset-meta!
    #'get-file
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'path 'dest]), :column (int 1)}
      :name
      'get-file
      :ns
      *ns*))
  (def signed-put-file
   (fn signed_put_file
     ([url f]
       (let [conn (.openConnection ^java.net.URL url)]
         (.setDoOutput ^java.net.URLConnection conn (boolean (.booleanValue true)))
         (.setRequestMethod ^java.net.HttpURLConnection conn "PUT")
         (with-open [os (.getOutputStream ^java.net.URLConnection conn)]
           (jio/copy (jio/as-file f) os))
         (with-open [is (.getInputStream ^java.net.URLConnection conn)] nil))
       nil)))
  (reset-meta!
    #'signed-put-file
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'url {:tag 'URL}) 'f]), :column (int 1)}
      :name
      'signed-put-file
      :ns
      *ns*))
  (def signed-put-clj
   (fn signed_put_clj
     ([url obj]
       (let [conn (.openConnection ^java.net.URL url)]
         (.setDoOutput ^java.net.URLConnection conn (boolean (.booleanValue true)))
         (.setRequestMethod ^java.net.HttpURLConnection conn "PUT")
         (with-open [os (.getOutputStream ^java.net.URLConnection conn)]
           (with-open [writer (jio/writer os)] (binding [*out* writer] (pr obj))))
         (with-open [is (.getInputStream ^java.net.URLConnection conn)] nil))
       nil)))
  (reset-meta!
    #'signed-put-clj
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'url {:tag 'URL}) 'obj]), :column (int 1)}
      :name
      'signed-put-clj
      :ns
      *ns*))
  (defn put-clj
    ([s3 bucket key obj]
      (binding [*print-length* nil *print-level* nil]
        (let [s (pr-str obj)
              bytes (.getBytes ^java.lang.String s "UTF-8")
              stream (java.io.ByteArrayInputStream. ^bytes bytes)]
          (put-object
            s3
            bucket
            key
            stream
            {:contentLength (java.lang.Integer/valueOf (int (count bytes))),
             :contentEncoding "text/plain; charset=utf-8"})))))
  (reset-meta!
    #'put-clj
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'key 'obj]), :column (int 1)}
      :name
      'put-clj
      :ns
      *ns*))
  (defn set-bucket-policy ([s3 bucket policy] (api/set-bucket-policy s3 (s3-name bucket) policy)))
  (reset-meta!
    #'set-bucket-policy
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'policy]), :column (int 1)}
      :name
      'set-bucket-policy
      :ns
      *ns*))
  (defn get-bucket-policy ([s3 bucket] (api/get-bucket-policy s3 (s3-name bucket))))
  (reset-meta!
    #'get-bucket-policy
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket]), :column (int 1)}
      :name
      'get-bucket-policy
      :ns
      *ns*)))