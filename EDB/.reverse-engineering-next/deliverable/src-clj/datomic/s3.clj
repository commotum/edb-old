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
  (defonce Name {})
  (defprotocol Name (s3-name [x]))
  (extend java.lang.String Name {:s3-name (fn fn__23271 ([item] item))})
  (extend clojure.lang.APersistentMap Name {:s3-name (fn fn__23273 ([item] (:name item)))})
  (defn years-from-now
    ([n]
      (java.util.Date.
        (long
          (+ (-> (* n 1000) (* 60) (* 60) (* 24) (* 365)) (java.lang.System/currentTimeMillis))))))
  (defn create-bucket
    ([s3 bucket region] (api/create-bucket s3 bucket region))
    ([s3 bucket] (api/create-bucket s3 bucket)))
  (declare get-bucket)
  (defn ensure-bucket
    ([s3 bucket region] (when-not (get-bucket s3 bucket) (create-bucket s3 bucket region)))
    ([s3 bucket] (when-not (get-bucket s3 bucket) (create-bucket s3 bucket))))
  (defn new-bucket ([s3 basename] (create-bucket s3 (str basename "-" (UUID/randomUUID)))))
  (defn debug ([x] (clojure.pprint/pprint x) x))
  (defn signed-url
    ([s3 method bucket key expiry headers virtual_host?]
      (let [bucket_name (s3-name bucket)
            key_name (s3-name key)
            method (let [G__23280 method]
                     (case
                       G__23280
                       :get
                       HttpMethod/GET
                       :delete
                       HttpMethod/DELETE
                       :put
                       HttpMethod/PUT
                       :head
                       HttpMethod/HEAD))]
        (api/generate-presigned-url s3 bucket_name key_name expiry method)))
    ([s3 method bucket key expiry] (signed-url s3 method bucket key expiry {} false)))
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
  (defn list-objects
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
        (apply concat (-list-objects-seq s3 result)))))
  (defn get-bucket
    ([s3 bucket_name]
      (first (filter (fn fn__23286 ([b] (= (:name b) bucket_name))) (api/list-buckets s3)))))
  (defn list-buckets ([s3] (api/list-buckets s3)))
  (defn delete-object ([s3 bucket key] (api/delete-object s3 (s3-name bucket) (s3-name key))))
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
  (defn delete-all-objects
    ([s3 bucket] (delete-objects s3 bucket (map :key (list-objects s3 bucket)))))
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
  (def BUCKET_OWNER_FULL_ACCESS_CANNED_ACL CannedAccessControlList/BucketOwnerFullControl)
  (defn put-file-as
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
    ([s3 bucket f path] (api/put-file s3 (s3-name bucket) path (jio/file f))))
  (defn put-object
    ([s3 bucket path stream metadata] (api/put-object s3 (s3-name bucket) path stream metadata)))
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
  (defn get-direct-buffer
    ([s3 bucket path]
      (let [temp__5457__auto__ (get-object s3 (s3-name bucket) (s3-name path))]
        (when temp__5457__auto__
          (let [obj temp__5457__auto__
                is (.getObjectContent ^com.amazonaws.services.s3.model.S3Object obj)]
            (try
              (let [bb (ByteBuffer/allocateDirect
                         (int
                           (.getContentLength
                             (.getObjectMetadata ^com.amazonaws.services.s3.model.S3Object obj))))]
                (io/fill-buffer is bb)
                bb)
              (finally
                (do (.close ^com.amazonaws.services.s3.model.S3ObjectInputStream is) nil))))))))
  (defn get-bytes
    ([s3 bucket path]
      (let [temp__5457__auto__ (get-object s3 (s3-name bucket) (s3-name path))]
        (when temp__5457__auto__
          (let [obj temp__5457__auto__
                is (.getObjectContent ^com.amazonaws.services.s3.model.S3Object obj)]
            (try
              (let [ba (byte-array
                         (long
                           (.getContentLength
                             (.getObjectMetadata ^com.amazonaws.services.s3.model.S3Object obj))))]
                (io/fill-array is ba)
                ba)
              (finally
                (do (.close ^com.amazonaws.services.s3.model.S3ObjectInputStream is) nil))))))))
  (defn get-non-direct-buffer
    ([s3 bucket path]
      (let [temp__5457__auto__ (get-bytes s3 bucket path)]
        (when temp__5457__auto__ (let [b temp__5457__auto__] (ByteBuffer/wrap ^bytes b))))))
  (defn delete-bucket ([s3 bucket] (api/delete-bucket s3 (s3-name bucket))))
  (defn destroy-bucket ([s3 bucket] (delete-all-objects s3 bucket) (delete-bucket s3 bucket)))
  (defn signed-get-clj
    ([url]
      (let [is (.openStream ^java.net.URL url)]
        (try
          (read (java.io.PushbackReader. (jio/reader is)))
          (finally (do (.close ^java.io.InputStream is) nil))))))
  (defn signed-get-file
    ([url f]
      (let [is (.openStream ^java.net.URL url)]
        (try (jio/copy is (jio/as-file f)) (finally (do (.close ^java.io.InputStream is) nil))))))
  (defn get-file
    ([s3 bucket path dest]
      (let [temp__5457__auto__ (get-object s3 (s3-name bucket) (s3-name path))]
        (when temp__5457__auto__
          (let [obj temp__5457__auto__
                is (.getObjectContent ^com.amazonaws.services.s3.model.S3Object obj)]
            (try
              (jio/copy is (jio/as-file dest))
              (finally
                (do (.close ^com.amazonaws.services.s3.model.S3ObjectInputStream is) nil))))))))
  (defn signed-put-file
    ([url f]
      (let [conn (.openConnection ^java.net.URL url)]
        (.setDoOutput ^java.net.URLConnection conn (boolean (.booleanValue true)))
        (.setRequestMethod ^java.net.HttpURLConnection conn "PUT")
        (let [os (.getOutputStream ^java.net.URLConnection conn)]
          (try (jio/copy (jio/as-file f) os) (finally (do (.close ^java.io.OutputStream os) nil))))
        (let [is (.getInputStream ^java.net.URLConnection conn)]
          (try nil (finally (do (.close ^java.io.InputStream is) nil)))))
      nil))
  (defn signed-put-clj
    ([url obj]
      (let [conn (.openConnection ^java.net.URL url)]
        (.setDoOutput ^java.net.URLConnection conn (boolean (.booleanValue true)))
        (.setRequestMethod ^java.net.HttpURLConnection conn "PUT")
        (let [os (.getOutputStream ^java.net.URLConnection conn)]
          (try
            (let [writer (jio/writer os)]
              (try
                (binding [*out* writer] (pr obj))
                (finally (do (.close ^java.io.Writer writer) nil))))
            (finally (do (.close ^java.io.OutputStream os) nil))))
        (let [is (.getInputStream ^java.net.URLConnection conn)]
          (try nil (finally (do (.close ^java.io.InputStream is) nil)))))
      nil))
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
  (defn set-bucket-policy ([s3 bucket policy] (api/set-bucket-policy s3 (s3-name bucket) policy)))
  (defn get-bucket-policy ([s3 bucket] (api/get-bucket-policy s3 (s3-name bucket)))))