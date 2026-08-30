(do
  (clojure.core/in-ns 'datomic.aws.client.datafy)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.aws.client.datafy)
    {:doc
     "Transforms data between Clojure and AWS SDK v2.\n  \n  Capabilities:\n  - Clojure maps as requests/responses\n  - Handle binary data uniformly\n  - Detect operation types automatically\n  \n  Data Flow:\n  Clojure map → SDK request → AWS service → SDK response → Clojure map\n  \n  Operation Types:\n  - Standard: Maps in/out\n  - Blob upload: Send binary content\n  - Blob download: Receive binary content"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'java.lang.reflect.Method)
      (clojure.core/import 'java.time.Duration)
      (clojure.core/import 'java.util.List)
      (clojure.core/import 'java.util.Map)
      (clojure.core/import 'java.util.concurrent.CompletableFuture)
      (clojure.core/import 'software.amazon.awssdk.awscore.AwsRequest)
      (clojure.core/import 'software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration)
      (clojure.core/import 'software.amazon.awssdk.awscore.AwsResponse)
      (clojure.core/import 'software.amazon.awssdk.core.ResponseBytes)
      (clojure.core/import 'software.amazon.awssdk.core.ResponseInputStream)
      (clojure.core/import 'software.amazon.awssdk.core.SdkBytes)
      (clojure.core/import 'software.amazon.awssdk.core.SdkField)
      (clojure.core/import 'software.amazon.awssdk.core.SdkPojo)
      (clojure.core/import 'software.amazon.awssdk.core.async.AsyncRequestBody)
      (clojure.core/import 'software.amazon.awssdk.core.async.AsyncResponseTransformer)
      (clojure.core/import 'software.amazon.awssdk.core.sync.RequestBody)
      (clojure.core/import 'software.amazon.awssdk.core.sync.ResponseTransformer)
      (clojure.core/import 'software.amazon.awssdk.core.protocol.MarshallingType)
      (clojure.core/import 'software.amazon.awssdk.core.traits.ListTrait)
      (clojure.core/import 'software.amazon.awssdk.core.traits.MapTrait)
      (clojure.core/import 'software.amazon.awssdk.utils.builder.CopyableBuilder)))
  (when-not (.equals 'datomic.aws.client.datafy 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws.client.datafy))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'java.lang.reflect.Method)
        (clojure.core/import 'java.time.Duration)
        (clojure.core/import 'java.util.List)
        (clojure.core/import 'java.util.Map)
        (clojure.core/import 'java.util.concurrent.CompletableFuture)
        (clojure.core/import 'software.amazon.awssdk.awscore.AwsRequest)
        (clojure.core/import 'software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration)
        (clojure.core/import 'software.amazon.awssdk.awscore.AwsResponse)
        (clojure.core/import 'software.amazon.awssdk.core.ResponseBytes)
        (clojure.core/import 'software.amazon.awssdk.core.ResponseInputStream)
        (clojure.core/import 'software.amazon.awssdk.core.SdkBytes)
        (clojure.core/import 'software.amazon.awssdk.core.SdkField)
        (clojure.core/import 'software.amazon.awssdk.core.SdkPojo)
        (clojure.core/import 'software.amazon.awssdk.core.async.AsyncRequestBody)
        (clojure.core/import 'software.amazon.awssdk.core.async.AsyncResponseTransformer)
        (clojure.core/import 'software.amazon.awssdk.core.sync.RequestBody)
        (clojure.core/import 'software.amazon.awssdk.core.sync.ResponseTransformer)
        (clojure.core/import 'software.amazon.awssdk.core.protocol.MarshallingType)
        (clojure.core/import 'software.amazon.awssdk.core.traits.ListTrait)
        (clojure.core/import 'software.amazon.awssdk.core.traits.MapTrait)
        (clojure.core/import 'software.amazon.awssdk.utils.builder.CopyableBuilder))))
  (set! *warn-on-reflection* true)
  (def lowerize
   (fn lowerize
     ([s]
       (if (< (count s) 2)
         (.toLowerCase ^java.lang.String s)
         (str (.toLowerCase (subs s 0 1)) (subs s 1))))))
  (reset-meta!
    #'lowerize
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 's {:tag 'String})]),
       :column (int 1)}
      :name
      'lowerize
      :ns
      *ns*))
  (def select-method
   (fn select_method
     ([client_class op]
       (let [method_name (lowerize (name op))
             methods (sort-by
                       (fn fn__14296
                         ([p1__14295#]
                           (java.lang.Integer/valueOf
                             (int (.getParameterCount ^java.lang.reflect.Method p1__14295#)))))
                       >
                       (filter
                         (fn fn__14298
                           ([m]
                             (let [vec__14299 (.getParameterTypes ^java.lang.reflect.Method m)
                                   arg1 (nth vec__14299 (int 0) nil)
                                   arg2 (nth vec__14299 (int 1) nil)]
                               (and
                                 (isa? arg1 software.amazon.awssdk.core.SdkPojo)
                                 (or
                                   (nil? arg2)
                                   (#{software.amazon.awssdk.core.sync.ResponseTransformer
                                      software.amazon.awssdk.core.sync.RequestBody
                                      software.amazon.awssdk.core.async.AsyncResponseTransformer
                                      software.amazon.awssdk.core.async.AsyncRequestBody}
                                     arg2))))))
                         (filter
                           (fn fn__14305
                             ([m]
                               (#{1 2}
                                 (java.lang.Integer/valueOf
                                   (int (.getParameterCount ^java.lang.reflect.Method m))))))
                           (filter
                             (fn fn__14307
                               ([m] (= (.getName ^java.lang.reflect.Method m) method_name)))
                             (.getMethods ^java.lang.Class client_class)))))]
         (when-not (first methods)
           (throw
             (java.lang.AssertionError.
               (str
                 "Assert failed: "
                 "No method eligible for registration found"
                 "\n"
                 (pr-str (clojure.core/list 'first 'methods))))))
         (first methods)))))
  (reset-meta!
    #'select-method
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'client-class {:tag 'Class}) 'op]),
       :column (int 1)}
      :name
      'select-method
      :ns
      *ns*))
  (def variant
   (fn variant
     ([m]
       (let [vec__14310 (.getParameterTypes ^java.lang.reflect.Method m)
             arg1 (nth vec__14310 (int 0) nil)
             arg2 (nth vec__14310 (int 1) nil)]
         (cond
           (#{software.amazon.awssdk.core.sync.ResponseTransformer
              software.amazon.awssdk.core.async.AsyncResponseTransformer}
             arg2) :blob-response
           (#{software.amazon.awssdk.core.sync.RequestBody
              software.amazon.awssdk.core.async.AsyncRequestBody}
             arg2) :blob-request
           :else (do :request))))))
  (reset-meta!
    #'variant
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'm {:tag 'Method})]), :column (int 1)}
      :name
      'variant
      :ns
      *ns*))
  (def ret-mode
   (fn ret_mode
     ([m]
       (let [rt (.getReturnType ^java.lang.reflect.Method m)]
         (if (isa? rt java.util.concurrent.CompletableFuture) :async :sync)))))
  (reset-meta!
    #'ret-mode
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'm {:tag 'Method})]), :column (int 1)}
      :name
      'ret-mode
      :ns
      *ns*))
  (def override-config
   (fn override_config
     ([request p__14315]
       (let [map__14316 p__14315
             map__14316 (if (seq? map__14316)
                          (if (next map__14316)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14316))
                            (if (seq map__14316) (first map__14316) {}))
                          map__14316)
             timeout (get map__14316 :timeout)
             builder (.toBuilder ^software.amazon.awssdk.awscore.AwsRequest request)
             override_config (.build
                               (.apiCallTimeout
                                 (AwsRequestOverrideConfiguration/builder)
                                 (Duration/ofMillis (long ^java.lang.Number timeout))))]
         (.overrideConfiguration
           ^software.amazon.awssdk.awscore.AwsRequest$Builder builder
           ^software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration override_config)
         (.build ^software.amazon.awssdk.awscore.AwsRequest$Builder builder)))))
  (reset-meta!
    #'override-config
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'request {:tag 'AwsRequest}) {:keys ['timeout]}]),
       :column (int 1)}
      :name
      'override-config
      :ns
      *ns*))
  (def clj->sdk
   (fn clj__GT_sdk
     ([builder v]
       (let [member_name_>field (into
                                  {}
                                  (map
                                    (fn fn__14320
                                      ([field]
                                        [(.memberName ^software.amazon.awssdk.core.SdkField field)
                                         field]))
                                    (.sdkFields ^software.amazon.awssdk.core.SdkPojo builder)))]
         (reduce-kv
           (fn fn__14322
             ([builder k v']
               (let [temp__5806__auto__ (^clojure.lang.IFn member_name_>field (name k))]
                 (when (nil? temp__5806__auto__)
                   (throw
                     (ex-info
                       (str
                         "No corresponding field for key "
                         k
                         " in builder "
                         (.getCanonicalName (class builder)))
                       {:field k, :value v', :builder builder, :builder-class (class builder)})))
                 (let [field temp__5806__auto__
                       pred__14323 =
                       expr__14324 (.marshallingType ^software.amazon.awssdk.core.SdkField field)]
                   (if (^clojure.lang.IFn pred__14323 MarshallingType/SDK_POJO expr__14324)
                     (let [field_ctor (.get
                                        (.constructor
                                          ^software.amazon.awssdk.core.SdkField field))]
                       (.set
                         ^software.amazon.awssdk.core.SdkField field
                         builder
                         (clj->sdk field_ctor v')))
                     (if (^clojure.lang.IFn pred__14323 MarshallingType/MAP expr__14324)
                       (let [map_trait (.getTrait
                                         ^software.amazon.awssdk.core.SdkField field
                                         software.amazon.awssdk.core.traits.MapTrait)
                             value_field (.valueFieldInfo
                                           ^software.amazon.awssdk.core.traits.MapTrait map_trait)]
                         (if (=
                               MarshallingType/STRING
                               (.marshallingType
                                 ^software.amazon.awssdk.core.SdkField value_field))
                           (.set ^software.amazon.awssdk.core.SdkField field builder v')
                           (.set
                             ^software.amazon.awssdk.core.SdkField field
                             builder
                             (update-vals
                               v'
                               (fn fn__14325
                                 ([p1__14318#]
                                   (clj->sdk
                                     (.get
                                       (.constructor
                                         ^software.amazon.awssdk.core.SdkField value_field))
                                     p1__14318#)))))))
                       (if (^clojure.lang.IFn pred__14323 MarshallingType/LIST expr__14324)
                         (let [member_field (.memberFieldInfo
                                              (.getTrait
                                                ^software.amazon.awssdk.core.SdkField field
                                                software.amazon.awssdk.core.traits.ListTrait))]
                           (.set
                             ^software.amazon.awssdk.core.SdkField field
                             builder
                             (mapv
                               (fn fn__14327
                                 ([p1__14319#]
                                   (clj->sdk
                                     (.get
                                       (.constructor
                                         ^software.amazon.awssdk.core.SdkField member_field))
                                     p1__14319#)))
                               v')))
                         (if (^clojure.lang.IFn pred__14323 MarshallingType/INTEGER expr__14324)
                           (.set
                             ^software.amazon.awssdk.core.SdkField field
                             builder
                             (java.lang.Integer/valueOf (int v')))
                           (if (^clojure.lang.IFn pred__14323 MarshallingType/LONG expr__14324)
                             (.set ^software.amazon.awssdk.core.SdkField field builder (long v'))
                             (.set ^software.amazon.awssdk.core.SdkField field builder v'))))))))
               builder))
           builder
           v))
       (.build ^software.amazon.awssdk.utils.builder.SdkBuilder builder))))
  (reset-meta!
    #'clj->sdk
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'builder {:tag 'SdkPojo}) 'v]), :column (int 1)}
      :name
      'clj->sdk
      :ns
      *ns*))
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      SdkPojoConverter
      "Protocol for converting SdkPojo objects to Clojure data"
      (sdk->clj [this]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.aws.client.datafy" "SdkPojoConverter")
      (assoc
        (assoc
          protocol_metadata__7431
          :doc
          "Protocol for converting SdkPojo objects to Clojure data")
        :name
        'SdkPojoConverter
        :ns
        *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'sdk->clj
                                        {:arglists (clojure.core/list ['this])}),
                                      :arglists (clojure.core/list ['this]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.aws.client.datafy"
                                       "SdkPojoConverter"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.aws.client.datafy" "sdk->clj")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (let [protocol_metadata__7434 {:column (int 1)}]
    (defprotocol
      AsyncResponseBodyConverter
      "Protocol for converting AsyncResponseBody objects"
      (sdk->body [this])
      (response [this]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.aws.client.datafy" "AsyncResponseBodyConverter")
      (assoc
        (assoc protocol_metadata__7434 :doc "Protocol for converting AsyncResponseBody objects")
        :name
        'AsyncResponseBodyConverter
        :ns
        *ns*))
    (let [protocol_signature__7435 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'sdk->body
                                        {:arglists (clojure.core/list ['this])}),
                                      :arglists (clojure.core/list ['this]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.aws.client.datafy"
                                       "AsyncResponseBodyConverter"))
          protocol_method_name__7436 (with-meta
                                       (:name protocol_signature__7435)
                                       protocol_signature__7435)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.aws.client.datafy" "sdk->body")
        (assoc protocol_signature__7435 :name protocol_method_name__7436 :ns *ns*)))
    (let [protocol_signature__7437 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'response
                                        {:arglists (clojure.core/list ['this])}),
                                      :arglists (clojure.core/list ['this]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.aws.client.datafy"
                                       "AsyncResponseBodyConverter"))
          protocol_method_name__7438 (with-meta
                                       (:name protocol_signature__7437)
                                       protocol_signature__7437)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.aws.client.datafy" "response")
        (assoc protocol_signature__7437 :name protocol_method_name__7438 :ns *ns*))))
  (extend
    software.amazon.awssdk.core.SdkPojo
    SdkPojoConverter
    {:sdk->clj
     (fn fn__14377
       ([pojo]
         (into
           {}
           (comp
             (keep
               (fn fn__14378
                 ([field]
                   (let [temp__5808__auto__ (.getValueOrDefault
                                              ^software.amazon.awssdk.core.SdkField field
                                              pojo)]
                     (when-not (nil? temp__5808__auto__)
                       (let [v temp__5808__auto__]
                         [(keyword (.memberName ^software.amazon.awssdk.core.SdkField field))
                          (sdk->clj v)]))))))
             (remove
               (fn fn__14382
                 ([p__14381]
                   (let [vec__14383 p__14381
                         _ (nth vec__14383 (int 0) nil)
                         v (nth vec__14383 (int 1) nil)]
                     (and (seqable? v) (empty? v)))))))
           (.sdkFields ^software.amazon.awssdk.core.SdkPojo pojo))))})
  (extend java.util.List SdkPojoConverter {:sdk->clj (fn fn__14389 ([coll] (mapv sdk->clj coll)))})
  (extend java.util.Map SdkPojoConverter {:sdk->clj (fn fn__14391 ([m] (update-vals m sdk->clj)))})
  (extend
    software.amazon.awssdk.core.SdkBytes
    SdkPojoConverter
    {:sdk->clj
     (fn fn__14393 ([bytes] (.asByteArray ^software.amazon.awssdk.core.BytesWrapper bytes)))})
  (extend java.lang.Object SdkPojoConverter {:sdk->clj (fn fn__14395 ([obj] obj))})
  (extend
    software.amazon.awssdk.core.ResponseBytes
    AsyncResponseBodyConverter
    {:sdk->body
     (fn fn__14397
       ([response_bytes] (.asByteArray ^software.amazon.awssdk.core.BytesWrapper response_bytes))),
     :response
     (fn fn__14399
       ([response_bytes] (.response ^software.amazon.awssdk.core.ResponseBytes response_bytes)))})
  (extend
    software.amazon.awssdk.core.ResponseInputStream
    AsyncResponseBodyConverter
    {:sdk->body (fn fn__14401 ([response_is] response_is)),
     :response
     (fn fn__14403
       ([response_is] (.response ^software.amazon.awssdk.core.ResponseInputStream response_is)))})
  (def response->request-id
   (fn response__GT_request_id
     ([response]
       (.requestId (.responseMetadata ^software.amazon.awssdk.awscore.AwsResponse response)))))
  (reset-meta!
    #'response->request-id
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'response {:tag 'AwsResponse})]), :column (int 1)}
      :name
      'response->request-id
      :ns
      *ns*)))