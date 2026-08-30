(do
  (clojure.core/in-ns 'datomic.core2.aws.s3.aws-api)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :refer (clojure.core/list 'go)]
        ['cognitect.aws.client.api :as 'aws]
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.core2.anomalizer :as 'izer]
        ['datomic.core2.async :refer (clojure.core/list '<!x)]
        ['datomic.java.io :as 'dio])
      (clojure.core/import 'java.io.InputStream)))
  (when-not (.equals 'datomic.core2.aws.s3.aws-api 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.aws.s3.aws-api))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :refer (clojure.core/list 'go)]
          ['cognitect.aws.client.api :as 'aws]
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.core2.anomalizer :as 'izer]
          ['datomic.core2.async :refer (clojure.core/list '<!x)]
          ['datomic.java.io :as 'dio])
        (clojure.core/import 'java.io.InputStream))))
  (set! *warn-on-reflection* true)
  (defn delete-object-request
    ([p__21298]
      (let [map__21299 p__21298
            map__21299 (if (seq? map__21299)
                         (if (next map__21299)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21299))
                           (if (seq map__21299) (first map__21299) {}))
                         map__21299)
            bucket (get map__21299 :bucket)
            key (get map__21299 :key)]
        {:op :DeleteObject, :request {:Bucket bucket, :Key key}})))
  (reset-meta!
    #'delete-object-request
    (assoc
      {:arglists (clojure.core/list [{:keys ['bucket 'key]}]), :column (int 1)}
      :name
      'delete-object-request
      :ns
      *ns*))
  (defn get-object-request
    ([p__21301]
      (let [map__21302 p__21301
            map__21302 (if (seq? map__21302)
                         (if (next map__21302)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21302))
                           (if (seq map__21302) (first map__21302) {}))
                         map__21302)
            bucket (get map__21302 :bucket)
            key (get map__21302 :key)]
        {:op :GetObject, :request {:Bucket bucket, :Key key}})))
  (reset-meta!
    #'get-object-request
    (assoc
      {:arglists (clojure.core/list [{:keys ['bucket 'key]}]), :column (int 1)}
      :name
      'get-object-request
      :ns
      *ns*))
  (defn put-object-request
    ([p__21304]
      (let [map__21305 p__21304
            map__21305 (if (seq? map__21305)
                         (if (next map__21305)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21305))
                           (if (seq map__21305) (first map__21305) {}))
                         map__21305)
            body (get map__21305 :body)
            bucket (get map__21305 :bucket)
            content_length (get map__21305 :content-length)
            key (get map__21305 :key)]
        {:op :PutObject,
         :request
         (cond->
           {:Bucket bucket, :Key key, :Body body}
           content_length
           (assoc :ContentLength content_length))})))
  (reset-meta!
    #'put-object-request
    (assoc
      {:arglists (clojure.core/list [{:keys ['body 'bucket 'content-length 'key]}]),
       :column (int 1)}
      :name
      'put-object-request
      :ns
      *ns*))
  (defn get-bytes
    ([p__21308]
      (let [map__21309 p__21308
            map__21309 (if (seq? map__21309)
                         (if (next map__21309)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21309))
                           (if (seq map__21309) (first map__21309) {}))
                         map__21309)
            bucket (get map__21309 :bucket)
            client (get map__21309 :client)
            key (get map__21309 :key)
            c__10363__auto__ (clojure.core.async/chan 1)
            captured_bindings__10364__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__21335
            ([]
              (let [G__21310 (fn G__21310 ([] p__21308))
                    G__21311 (fn G__21311 ([] map__21309))
                    G__21312 (fn G__21312 ([] bucket))
                    G__21313 (fn G__21313 ([] client))
                    G__21314 (fn G__21314 ([] key))
                    f__10365__auto__ (fn state_machine__10108__auto__
                                       ([]
                                         (let [statearr_21347 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 11))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21347
                                             0
                                             state_machine__10108__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21347
                                             1
                                             1)
                                           statearr_21347))
                                       ([state_21334]
                                         (let [old_frame__10109__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__10110__auto__ (try
                                                                          (try
                                                                            (do
                                                                              (clojure.lang.Var/resetThreadBindingFrame
                                                                                (clojure.core.async.impl.ioc-macros/aget-object
                                                                                  state_21334
                                                                                  3))
                                                                              (loop 
                                                                                []
                                                                                (let 
                                                                                  [result__10111__auto__
                                                                                   (let 
                                                                                     [G__21349
                                                                                      (int
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_21334
                                                                                          1))]
                                                                                     (case
                                                                                       G__21349
                                                                                       1
                                                                                       (let 
                                                                                         [inst_21323
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            6)
                                                                                          inst_21325
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            7)
                                                                                          inst_21321
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            8)
                                                                                          inst_21324
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            9)
                                                                                          inst_21322
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            10)
                                                                                          inst_21316
                                                                                          (^clojure.lang.IFn G__21310)
                                                                                          p__21308
                                                                                          inst_21316
                                                                                          inst_21317
                                                                                          (^clojure.lang.IFn G__21311)
                                                                                          p__21308
                                                                                          inst_21316
                                                                                          map__21309
                                                                                          inst_21317
                                                                                          inst_21318
                                                                                          (^clojure.lang.IFn G__21312)
                                                                                          p__21308
                                                                                          inst_21316
                                                                                          map__21309
                                                                                          inst_21317
                                                                                          bucket
                                                                                          inst_21318
                                                                                          inst_21319
                                                                                          (^clojure.lang.IFn G__21313)
                                                                                          bucket
                                                                                          inst_21318
                                                                                          p__21308
                                                                                          inst_21316
                                                                                          client
                                                                                          inst_21319
                                                                                          map__21309
                                                                                          inst_21317
                                                                                          inst_21320
                                                                                          (^clojure.lang.IFn G__21314)
                                                                                          inst_21321
                                                                                          inst_21316
                                                                                          inst_21322
                                                                                          inst_21317
                                                                                          inst_21323
                                                                                          inst_21318
                                                                                          inst_21324
                                                                                          inst_21319
                                                                                          inst_21325
                                                                                          inst_21320
                                                                                          bucket
                                                                                          inst_21323
                                                                                          key
                                                                                          inst_21325
                                                                                          p__21308
                                                                                          inst_21321
                                                                                          client
                                                                                          inst_21324
                                                                                          map__21309
                                                                                          inst_21322
                                                                                          inst_21326
                                                                                          (aws/invoke-async
                                                                                            client
                                                                                            (get-object-request
                                                                                              {:bucket
                                                                                               bucket,
                                                                                               :key
                                                                                               key}))
                                                                                          state_21334
                                                                                          (let 
                                                                                            [statearr_21350
                                                                                             state_21334]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21350
                                                                                              8
                                                                                              inst_21321)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21350
                                                                                              10
                                                                                              inst_21322)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21350
                                                                                              6
                                                                                              inst_21323)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21350
                                                                                              9
                                                                                              inst_21324)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21350
                                                                                              7
                                                                                              inst_21325)
                                                                                            statearr_21350)]
                                                                                         (clojure.core.async.impl.ioc-macros/take!
                                                                                           state_21334
                                                                                           2
                                                                                           inst_21326))
                                                                                       2
                                                                                       (let 
                                                                                         [inst_21323
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            6)
                                                                                          inst_21325
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            7)
                                                                                          inst_21321
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            8)
                                                                                          inst_21324
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            9)
                                                                                          inst_21322
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            10)
                                                                                          inst_21328
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21334
                                                                                            2)
                                                                                          inst_21329
                                                                                          inst_21328
                                                                                          v__20478__auto__
                                                                                          inst_21329
                                                                                          bucket
                                                                                          inst_21323
                                                                                          key
                                                                                          inst_21325
                                                                                          p__21308
                                                                                          inst_21321
                                                                                          client
                                                                                          inst_21324
                                                                                          map__21309
                                                                                          inst_21322
                                                                                          inst_21330
                                                                                          (or
                                                                                            (datomic.core2.async/channel-closed-error
                                                                                              v__20478__auto__)
                                                                                            v__20478__auto__)
                                                                                          inst_21331
                                                                                          inst_21330
                                                                                          bucket
                                                                                          inst_21323
                                                                                          key
                                                                                          inst_21325
                                                                                          p__21308
                                                                                          inst_21321
                                                                                          client
                                                                                          inst_21324
                                                                                          map__21309
                                                                                          inst_21322
                                                                                          result
                                                                                          inst_21331
                                                                                          inst_21332
                                                                                          (or
                                                                                            (canom/anom
                                                                                              result)
                                                                                            (with-open 
                                                                                              [is
                                                                                               (:Body
                                                                                                 result)]
                                                                                              (try
                                                                                                (let 
                                                                                                  [ba
                                                                                                   (byte-array
                                                                                                     (:ContentLength
                                                                                                       result))]
                                                                                                  (dio/fill-from-stream!
                                                                                                    ba
                                                                                                    is)
                                                                                                  {:value
                                                                                                   ba})
                                                                                                (catch
                                                                                                  java.lang.Throwable
                                                                                                  t
                                                                                                  (izer/throwable->anom
                                                                                                    t)))))]
                                                                                         (clojure.core.async.impl.ioc-macros/return-chan
                                                                                           state_21334
                                                                                           inst_21332))))]
                                                                                  (if
                                                                                    (identical?
                                                                                      result__10111__auto__
                                                                                      :recur)
                                                                                    (recur)
                                                                                    result__10111__auto__))))
                                                                            (catch
                                                                              java.lang.Throwable
                                                                              ex__10112__auto__
                                                                              (do
                                                                                (let 
                                                                                  [statearr_21353
                                                                                   state_21334]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_21353
                                                                                    2
                                                                                    ex__10112__auto__))
                                                                                (if
                                                                                  (seq
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_21334
                                                                                      4))
                                                                                  (let 
                                                                                    [statearr_21354
                                                                                     state_21334]
                                                                                    (clojure.core.async.impl.ioc-macros/aset-object
                                                                                      statearr_21354
                                                                                      1
                                                                                      (first
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_21334
                                                                                          4))))
                                                                                  (throw
                                                                                    ^java.lang.Throwable ex__10112__auto__))
                                                                                :recur)))
                                                                          (finally
                                                                            (do
                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                state_21334
                                                                                3
                                                                                (clojure.lang.Var/getThreadBindingFrame))
                                                                              (clojure.lang.Var/resetThreadBindingFrame
                                                                                old_frame__10109__auto__))))]
                                           (if (identical? ret_value__10110__auto__ :recur)
                                             (recur state_21334)
                                             ret_value__10110__auto__))))
                    state__10366__auto__ (let [statearr_21363 (^clojure.lang.IFn f__10365__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21363
                                             5
                                             c__10363__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21363
                                             3
                                             captured_bindings__10364__auto__)
                                           statearr_21363)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__10366__auto__)))))
        c__10363__auto__)))
  (reset-meta!
    #'get-bytes
    (assoc
      {:arglists (clojure.core/list [{:keys ['bucket 'client 'key]}]), :column (int 1)}
      :name
      'get-bytes
      :ns
      *ns*)))