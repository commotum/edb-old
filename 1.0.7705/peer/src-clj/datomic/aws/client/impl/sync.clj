(do
  (clojure.core/in-ns 'datomic.aws.client.impl.sync)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.aws.client.impl.sync)
    {:doc
     "Executing AWS ops synchronously.\n\n  There is one API: exec-op\n\n  Handles:\n  1. Building SDK request from Clojure data\n  2. Executing with proper variant (standard/blob)\n  3. Transforming SDK response to Clojure data\n  4. Returning transformed response or anomaly\n  \n  Variants:\n  - Standard: Direct request/response (non-streaming)\n  - Blob upload: Request with binary body (:body)\n  - Blob download: Response as bytes/stream (:response-as)\n  \n  Binary Support:\n  - Upload: String, bytes, ByteBuffer, Path, File\n  - Download: bytes or input-stream\n  \n  All ops complete with either:\n  - Success: Response as Clojure data\n  - Failure: Anomaly"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.aws.client.anomalizer :as 'anom]
        ['datomic.aws.client.datafy :as 'datafy])
      (clojure.core/import 'java.io.File)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.nio.file.Path)
      (clojure.core/import 'software.amazon.awssdk.awscore.AwsClient)
      (clojure.core/import 'software.amazon.awssdk.core.sync.RequestBody)
      (clojure.core/import 'software.amazon.awssdk.core.sync.ResponseTransformer)))
  (when-not (.equals 'datomic.aws.client.impl.sync 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws.client.impl.sync))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.aws.client.anomalizer :as 'anom]
          ['datomic.aws.client.datafy :as 'datafy])
        (clojure.core/import 'java.io.File)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.nio.file.Path)
        (clojure.core/import 'software.amazon.awssdk.awscore.AwsClient)
        (clojure.core/import 'software.amazon.awssdk.core.sync.RequestBody)
        (clojure.core/import 'software.amazon.awssdk.core.sync.ResponseTransformer))))
  (set! *warn-on-reflection* true)
  (defn request-body
    ([body]
      (cond
        (bytes? body) (RequestBody/fromBytes ^bytes body)
        (instance? java.nio.ByteBuffer body) (RequestBody/fromByteBuffer ^java.nio.ByteBuffer body)
        (instance? java.nio.file.Path body) (RequestBody/fromFile ^java.nio.file.Path body)
        (instance? java.io.File body) (RequestBody/fromFile ^java.io.File body)
        (instance? java.lang.String body) (do (RequestBody/fromString ^java.lang.String body)))))
  (reset-meta!
    #'request-body
    (assoc
      {:private true, :arglists (clojure.core/list ['body]), :column (int 1)}
      :name
      'request-body
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.aws.client.impl.sync" "response-xformers")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.aws.client.impl.sync" "response-xformers")
    {:bytes (fn fn__14432 ([] (ResponseTransformer/toBytes))),
     :input-stream (fn fn__14434 ([] (ResponseTransformer/toInputStream)))})
  (def process-result
   (fn process_result
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
           (assoc :aws/RequestId (datafy/response->request-id result)))))))
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
  (def exec-op
   (fn exec_op
     ([client p__14439 p__14440]
       (let [map__14441 p__14439
             map__14441 (if (seq? map__14441)
                          (if (next map__14441)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14441))
                            (if (seq map__14441) (first map__14441) {}))
                          map__14441)
             op_map map__14441
             req (get map__14441 :req)
             body (get map__14441 :body)
             meta (get map__14441 :meta)
             response_as (get map__14441 :response-as)
             overrides (get map__14441 :overrides)
             map__14442 p__14440
             map__14442 (if (seq? map__14442)
                          (if (next map__14442)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14442))
                            (if (seq map__14442) (first map__14442) {}))
                          map__14442)
             method (get map__14442 :method)
             request_builder (get map__14442 :request-builder)
             variant (get map__14442 :variant)
             service (.serviceName ^software.amazon.awssdk.core.SdkClient client)]
         (try
           (let [request (cond->
                           (^clojure.lang.IFn request_builder)
                           true
                           (datafy/clj->sdk req)
                           (seq overrides)
                           (datafy/override-config overrides))
                 ret (let [G__14444 variant]
                       (case
                         G__14444
                         :blob-response
                         (^clojure.lang.IFn method
                           client
                           request
                           ((get response-xformers response_as)))
                         :request
                         (^clojure.lang.IFn method client request)
                         :blob-request
                         (^clojure.lang.IFn method client request (request-body body))))]
             (process-result ret (= :blob-response variant) meta))
           (catch java.lang.Exception ex (anom/anomalize ex (assoc op_map :service service))))))))
  (reset-meta!
    #'exec-op
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'client {:tag 'AwsClient})
          {:keys ['req 'body 'meta 'response-as 'overrides], :as 'op-map}
          {:keys ['method 'request-builder 'variant]}]),
       :column (int 1)}
      :name
      'exec-op
      :ns
      *ns*)))