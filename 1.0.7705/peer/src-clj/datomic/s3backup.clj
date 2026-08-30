(do
  (clojure.core/in-ns 'datomic.s3backup)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.backup :as 'backup]
        ['datomic.core2.aws.helpers :as 'aws-helpers]
        ['datomic.s3 :as 's3]
        ['datomic.slf4j :as 'logger]
        ['datomic.common :as 'common]
        ['datomic.io :as 'io]
        ['datomic.backup :as 'backup]
        ['datomic.uri :as 'uri]
        ['datomic.error :as 'error]
        ['datomic.measure.io-stats :as 'io-stats])
      (clojure.core/import 'java.net.URI)))
  (when-not (.equals 'datomic.s3backup 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.s3backup))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.backup :as 'backup]
          ['datomic.core2.aws.helpers :as 'aws-helpers]
          ['datomic.s3 :as 's3]
          ['datomic.slf4j :as 'logger]
          ['datomic.common :as 'common]
          ['datomic.io :as 'io]
          ['datomic.backup :as 'backup]
          ['datomic.uri :as 'uri]
          ['datomic.error :as 'error]
          ['datomic.measure.io-stats :as 'io-stats])
        (clojure.core/import 'java.net.URI))))
  (set! *warn-on-reflection* true)
  (defn s3-storage-path ([base k] (str base "/" k)))
  (reset-meta!
    #'s3-storage-path
    (assoc
      {:arglists (clojure.core/list ['base 'k]), :column (int 1)}
      :name
      's3-storage-path
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.s3backup" "s3-storage")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.s3backup" "->S3Storage")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.s3backup" "map->S3Storage")
    {:declared true, :column (int 1)})
  (defrecord
    S3Storage
    [s3 bucket base sse?]
    datomic.backup.Storage
    (retrieve
      [this k]
      (let [start (java.lang.System/nanoTime)
            ret (let [temp__5804__auto__ (s3/get-non-direct-buffer
                                           s3
                                           bucket
                                           (s3-storage-path base k))]
                  (when temp__5804__auto__ (let [buf temp__5804__auto__] {:v buf})))]
        (io-stats/inc! :s3)
        (io-stats/inc! :s3-ns (- (java.lang.System/nanoTime) start))
        ret))
    (list-keys
      [this prefix]
      {:ks
       (map
         (fn fn__21757
           ([obj]
             (subs
               (:Key obj)
               (java.lang.Integer/valueOf (int (count (s3-storage-path base "")))))))
         (s3/list-objects s3 bucket (s3-storage-path base prefix) nil))})
    (exists?
      [this k]
      (let [result (s3/object-exists? s3 bucket (s3-storage-path base k))]
        (if (contains? result :exists)
          (:exists result)
          (error/raise :s3backup "exists check failed" result))))
    (store
      [this k buf]
      (do
        (s3/put-object
          s3
          bucket
          (s3-storage-path base k)
          buf
          (merge
            {:ContentEncoding "gzip",
             :ContentLength (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer buf)))}
            (when sse? {:ServerSideEncryption "AES256"})))
        {:k k})))
  (clojure.core/import 'datomic.s3backup.S3Storage)
  (defn ->S3Storage ([s3 bucket base sse?] (datomic.s3backup.S3Storage. s3 bucket base sse?)))
  (reset-meta!
    #'->S3Storage
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'base 'sse?]), :column (int 1)}
      :name
      '->S3Storage
      :ns
      *ns*))
  (defn map->S3Storage
    ([m__7972__auto__]
      (S3Storage/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->S3Storage
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->S3Storage
      :ns
      *ns*))
  (defn s3-storage
    ([& p__21776]
      (let [map__21777 p__21776
            map__21777 (if (seq? map__21777)
                         (if (next map__21777)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21777))
                           (if (seq map__21777) (first map__21777) {}))
                         map__21777)
            s3 (get map__21777 :s3)
            bucket (get map__21777 :bucket)
            base (get map__21777 :base)
            sse? (get map__21777 :sse?)]
        (when-not (and s3 bucket base)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list 'and 's3 'bucket 'base))))))
        (datomic.s3backup.S3Storage. s3 bucket base sse?))))
  (reset-meta!
    #'s3-storage
    (assoc
      {:arglists (clojure.core/list ['& {:keys ['s3 'bucket 'base 'sse?]}]), :column (int 1)}
      :name
      's3-storage
      :ns
      *ns*))
  (defn validate-s3-uri
    ([uri]
      (let [G__21781 nil
            G__21781 (if (empty? (.getHost ^java.net.URI uri)) (cons :bucket G__21781) G__21781)]
        (if (empty? (.getPath ^java.net.URI uri)) (cons :prefix G__21781) G__21781))))
  (reset-meta!
    #'validate-s3-uri
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uri {:tag 'URI})]), :column (int 1)}
      :name
      'validate-s3-uri
      :ns
      *ns*))
  (defn storage-from-uri
    ([uri sse?]
      (let [temp__5804__auto__ (validate-s3-uri uri)]
        (when temp__5804__auto__
          (let [missing temp__5804__auto__]
            (throw
              (java.lang.IllegalArgumentException.
                (str "S3 URI is incomplete, missing " missing)))))
        nil)
      (let [bucket (.getHost ^java.net.URI uri)
            base (subs (.getPath ^java.net.URI uri) 1)
            map__21783 (uri/parse-query-string (.getQuery ^java.net.URI uri))
            map__21783 (if (seq? map__21783)
                         (if (next map__21783)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21783))
                           (if (seq map__21783) (first map__21783) {}))
                         map__21783)
            aws_access_key_id (get map__21783 "aws_access_key_id")
            aws_secret_key (get map__21783 "aws_secret_key")
            creds (when (and aws_access_key_id aws_secret_key)
                    (let [params {:aws-access-key-id aws_access_key_id,
                                  :aws-secret-key aws_secret_key}]
                      (uri/warn-creds params)
                      params))
            s3 (if creds
                 (s3/s3-service
                   (aws-helpers/static-credentials-provider creds)
                   {:maxConnections (long (* 64 1024)), :maxAttempts 11})
                 (s3/s3-service {:maxConnections (long (* 64 1024)), :maxAttempts 11}))]
        (s3-storage :s3 s3 :bucket bucket :base base :sse? sse?))))
  (reset-meta!
    #'storage-from-uri
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uri {:tag 'URI}) 'sse?]), :column (int 1)}
      :name
      'storage-from-uri
      :ns
      *ns*)))