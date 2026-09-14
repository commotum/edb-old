(do
  (clojure.core/in-ns 'datomic.core2.aws.s3.sdkv2)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.core2.aws.helpers :as 'helpers])))
  (when-not (.equals 'datomic.core2.aws.s3.sdkv2 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.aws.s3.sdkv2))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.core2.aws.helpers :as 'helpers]))))
  (set! *warn-on-reflection* true)
  (defn wrap-ex-handler
    ([f context]
      (fn fn__21391
        ([& args] (try (apply f args) (catch java.lang.Throwable t (merge context (ex-data t)))))))
    ([f] (wrap-ex-handler f nil)))
  (reset-meta!
    #'wrap-ex-handler
    (assoc
      {:arglists (clojure.core/list ['f] ['f 'context]), :column (int 1)}
      :name
      'wrap-ex-handler
      :ns
      *ns*))
  (defn put-object
    ([s3 bucket path bytes opts]
      (helpers/invoke
        s3
        {:op :PutObject, :req (merge opts {:Bucket bucket, :Key path}), :body bytes})))
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
        (helpers/invoke
          s3
          {:op :GetObject, :req {:Bucket bucket, :Key path}, :response-as response_as})
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
  (defn get-bytes ([s3 bucket path] (get-object s3 bucket path :bytes)))
  (reset-meta!
    #'get-bytes
    (assoc
      {:arglists
       (clojure.core/list
         ['s3 (.withMeta 'bucket {:tag 'String}) (.withMeta 'path {:tag 'String})]),
       :column (int 1)}
      :name
      'get-bytes
      :ns
      *ns*))
  (defn delete-object
    ([s3 bucket key] (helpers/invoke s3 {:op :DeleteObject, :req {:Bucket bucket, :Key key}})))
  (reset-meta!
    #'delete-object
    (assoc
      {:arglists (clojure.core/list ['s3 'bucket 'key]), :column (int 1)}
      :name
      'delete-object
      :ns
      *ns*)))