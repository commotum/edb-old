(do
  (clojure.core/in-ns 'datomic.s3-kv)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.s3 :as 's3]
        ['datomic.io :as 'io]
        ['datomic.simple-kv :as 'skv])))
  (when-not (.equals 'datomic.s3-kv 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.s3-kv))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.s3 :as 's3]
          ['datomic.io :as 'io]
          ['datomic.simple-kv :as 'skv]))))
  (set! *warn-on-reflection* true)
  (defn s3-storage-path
    ([base k] (str base "/" (when-not (.contains ^java.lang.String k "/") "data/") k)))
  (deftype
    S3Storage
    [s3 bucket base]
    datomic.simple_kv.KV
    (delete [this k] (do (s3/delete-object s3 bucket (s3-storage-path base k)) :ok))
    (get [this k] (s3/get-direct-buffer s3 bucket (s3-storage-path base k)))
    (put
      [this id v]
      (try
        (do
          (s3/put-object
            s3
            bucket
            (s3-storage-path base id)
            (org.fressian.impl.ByteBufferInputStream. ^java.nio.ByteBuffer v)
            {:contentLength (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer v)))})
          :ok)
        (catch java.lang.Exception ex nil))))
  (clojure.core/import 'datomic.s3_kv.S3Storage)
  (defn ->S3Storage ([s3 bucket base] (datomic.s3_kv.S3Storage. s3 bucket base)))
  (defn s3-storage
    ([& p__23332]
      (let [map__23333 p__23332
            map__23333 (if (seq? map__23333)
                         (clojure.lang.PersistentHashMap/create (seq map__23333))
                         map__23333)
            s3 (get map__23333 :s3)
            bucket (get map__23333 :bucket)
            base (get map__23333 :base)]
        (when-not (and s3 bucket base)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list 'and 's3 'bucket 'base))))))
        (datomic.s3_kv.S3Storage. s3 bucket base))))
  (defn storage-from-conf-
    ([conf]
      (s3-storage :s3 (s3/s3-service conf) :bucket (:system-root conf) :base (:aws-s3-path conf))))
  (def storage-from-conf (memoize storage-from-conf-)))