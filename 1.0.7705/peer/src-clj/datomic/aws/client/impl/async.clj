(do
  (clojure.core/in-ns 'datomic.aws.client.impl.async)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.aws.client.impl.async)
    {:doc
     "Executing AWS ops asynchronously.\n\n  There is one API: exec-op\n\n  Handles:\n  1. Building SDK request from Clojure data\n  2. Executing with proper variant (standard/blob)\n  3. Transforming SDK response to Clojure data\n  4. Placing transformed response or anomaly on chan\n  \n  Variants:\n  - Standard: Direct request/response (non-streaming)\n  - Blob upload: Request with binary body (:body)\n  - Blob download: Response as bytes/stream (:response-as)\n  \n  Binary Support:\n  - Upload: String, bytes, ByteBuffer, Path, File\n  - Download: bytes or input-stream\n  \n  All ops complete with either:\n  - Success: Response on chan\n  - Failure: Anomaly on chan"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :as 'a]
        ['datomic.aws.client.anomalizer :as 'anom]
        ['datomic.aws.client.datafy :as 'datafy])
      (clojure.core/import 'java.io.File)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.nio.file.Path)
      (clojure.core/import 'software.amazon.awssdk.awscore.AwsClient)
      (clojure.core/import 'software.amazon.awssdk.core.async.AsyncRequestBody)
      (clojure.core/import 'software.amazon.awssdk.core.async.AsyncResponseTransformer)
      (clojure.core/import 'java.util.concurrent.CompletableFuture)
      (clojure.core/import 'java.util.concurrent.CompletionException)))
  (when-not (.equals 'datomic.aws.client.impl.async 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws.client.impl.async))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :as 'a]
          ['datomic.aws.client.anomalizer :as 'anom]
          ['datomic.aws.client.datafy :as 'datafy])
        (clojure.core/import 'java.io.File)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.nio.file.Path)
        (clojure.core/import 'software.amazon.awssdk.awscore.AwsClient)
        (clojure.core/import 'software.amazon.awssdk.core.async.AsyncRequestBody)
        (clojure.core/import 'software.amazon.awssdk.core.async.AsyncResponseTransformer)
        (clojure.core/import 'java.util.concurrent.CompletableFuture)
        (clojure.core/import 'java.util.concurrent.CompletionException))))
  (set! *warn-on-reflection* true)
  (defn async-request-body
    ([body]
      (cond
        (bytes? body) (AsyncRequestBody/fromBytes ^bytes body)
        (instance? java.nio.ByteBuffer body) (AsyncRequestBody/fromByteBuffer
                                               ^java.nio.ByteBuffer body)
        (instance? java.nio.file.Path body) (AsyncRequestBody/fromFile ^java.nio.file.Path body)
        (instance? java.io.File body) (AsyncRequestBody/fromFile ^java.io.File body)
        (instance? java.lang.String body) (do
                                            (AsyncRequestBody/fromString
                                              ^java.lang.String body)))))
  (reset-meta!
    #'async-request-body
    (assoc
      {:private true, :arglists (clojure.core/list ['body]), :column (int 1)}
      :name
      'async-request-body
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.aws.client.impl.async" "response-xformers")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.aws.client.impl.async" "response-xformers")
    {:bytes (fn fn__14411 ([] (AsyncResponseTransformer/toBytes))),
     :input-stream (fn fn__14413 ([] (AsyncResponseTransformer/toBlockingInputStream)))})
  (defn process-result
    ([result blob_response? meta]
      (if blob_response?
        (cond->
          {:ret (datafy/sdk->clj (datafy/response result)), :body (datafy/sdk->body result)}
          meta
          (assoc :datomic.aws.client.api/meta meta)
          true
          (update
            :ret
            assoc
            :aws/RequestId
            (datafy/response->request-id (datafy/response result))))
        (cond->
          (datafy/sdk->clj result)
          meta
          (assoc :datomic.aws.client.api/meta meta)
          true
          (assoc :aws/RequestId (datafy/response->request-id result))))))
  (reset-meta!
    #'process-result
    (assoc
      {:private true,
       :arglists (clojure.core/list ['result 'blob-response? 'meta]),
       :column (int 1)}
      :name
      'process-result
      :ns
      *ns*))
  (defn exec-op
    ([client p__14418 p__14419]
      (let [map__14420 p__14418
            map__14420 (if (seq? map__14420)
                         (if (next map__14420)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__14420))
                           (if (seq map__14420) (first map__14420) {}))
                         map__14420)
            op_map map__14420
            req (get map__14420 :req)
            body (get map__14420 :body)
            ch (get map__14420 :ch)
            meta (get map__14420 :meta)
            response_as (get map__14420 :response-as)
            overrides (get map__14420 :overrides)
            map__14421 p__14419
            map__14421 (if (seq? map__14421)
                         (if (next map__14421)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__14421))
                           (if (seq map__14421) (first map__14421) {}))
                         map__14421)
            method (get map__14421 :method)
            request_builder (get map__14421 :request-builder)
            variant (get map__14421 :variant)]
        (try
          (let [service (.serviceName ^software.amazon.awssdk.core.SdkClient client)
                request (cond->
                          (^clojure.lang.IFn request_builder)
                          true
                          (datafy/clj->sdk req)
                          (seq overrides)
                          (datafy/override-config overrides))
                fut (let [G__14423 variant]
                      (case
                        G__14423
                        :blob-response
                        (^clojure.lang.IFn method
                          client
                          request
                          ((get response-xformers response_as)))
                        :request
                        (^clojure.lang.IFn method client request)
                        :blob-request
                        (^clojure.lang.IFn method client request (async-request-body body))))]
            (.whenComplete
              ^java.util.concurrent.CompletableFuture fut
              (reify
                java.util.function.BiConsumer
                (^void accept
                  [this result ex]
                  (do
                    (try
                      (if ex
                        (let [t (if (instance? java.util.concurrent.CompletionException ex)
                                  (.getCause ^java.lang.Throwable ex)
                                  ex)]
                          (a/put! ch (anom/anomalize t (assoc op_map :service service))))
                        (a/put! ch (process-result result (= :blob-response variant) meta)))
                      (catch
                        java.lang.Throwable
                        t
                        (a/put!
                          ch
                          {:cognitect.anomalies/category :cognitect.anomalies/fault,
                           :throwable (Throwable->map t)})))
                    nil)))))
          (catch java.lang.Exception ex (a/put! ch (anom/anomalize ex op_map)))))))
  (reset-meta!
    #'exec-op
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'client {:tag 'AwsClient})
          {:keys ['req 'body 'ch 'meta 'response-as 'overrides], :as 'op-map}
          {:keys ['method 'request-builder 'variant]}]),
       :column (int 1)}
      :name
      'exec-op
      :ns
      *ns*)))