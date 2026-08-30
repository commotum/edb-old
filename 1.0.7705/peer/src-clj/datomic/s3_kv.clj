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
  (reset-meta!
    #'s3-storage-path
    (assoc
      {:arglists (clojure.core/list ['base (.withMeta 'k {:tag 'String})]), :column (int 1)}
      :name
      's3-storage-path
      :ns
      *ns*))
  (deftype
    S3Storage
    [s3 bucket base]
    datomic.simple_kv.KV
    (delete [this k] (do (s3/delete-object s3 bucket (s3-storage-path base k)) :ok))
    (get [this k] (s3/get-object s3 bucket (s3-storage-path base k) :bytes))
    (put
      [this id v]
      (try
        (do
          (s3/put-object
            s3
            bucket
            (s3-storage-path base id)
            v
            {:contentLength (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer v)))})
          :ok)
        (catch java.lang.Exception ex nil))))
  (clojure.core/import 'datomic.s3_kv.S3Storage)
  (defn ->S3Storage ([s3 bucket base] (datomic.s3_kv.S3Storage. s3 bucket base)))
  (reset-meta!
    #'->S3Storage
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'base]), :column (int 1)}
      :name
      '->S3Storage
      :ns
      *ns*))
  (defn s3-storage
    ([& p__23372]
      (let [map__23373 p__23372
            map__23373 (if (seq? map__23373)
                         (if (next map__23373)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__23373))
                           (if (seq map__23373) (first map__23373) {}))
                         map__23373)
            s3 (get map__23373 :s3)
            bucket (get map__23373 :bucket)
            base (get map__23373 :base)]
        (when-not (and s3 bucket base)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list 'and 's3 'bucket 'base))))))
        (datomic.s3_kv.S3Storage. s3 bucket base))))
  (reset-meta!
    #'s3-storage
    (assoc
      {:arglists (clojure.core/list ['& {:keys ['s3 'bucket 'base]}]), :column (int 1)}
      :name
      's3-storage
      :ns
      *ns*))
  (defn storage-from-conf-
    ([conf]
      (s3-storage :s3 (s3/s3-service conf) :bucket (:system-root conf) :base (:aws-s3-path conf))))
  (reset-meta!
    #'storage-from-conf-
    (assoc
      {:arglists (clojure.core/list ['conf]), :column (int 1)}
      :name
      'storage-from-conf-
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.s3-kv" "storage-from-conf") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.s3-kv" "storage-from-conf")
    (memoize storage-from-conf-)))