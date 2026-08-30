(do
  (clojure.core/in-ns 'datomic.core2.aws.s3.aws-api)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :refer (clojure.core/list 'go)]
        ['cognitect.aws.client.api :as 'aws]
        ['datomic.core2.anomalies :as 'canom]
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
          ['datomic.core2.async :refer (clojure.core/list '<!x)]
          ['datomic.java.io :as 'dio])
        (clojure.core/import 'java.io.InputStream))))
  (set! *warn-on-reflection* true)
  (defn delete-object-request
    ([p__20450]
      (let [map__20451 p__20450
            map__20451 (if (seq? map__20451)
                         (if (next map__20451)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20451))
                           (if (seq map__20451) (first map__20451) {}))
                         map__20451)
            bucket (get map__20451 :bucket)
            key (get map__20451 :key)]
        {:op :DeleteObject, :request {:Bucket bucket, :Key key}})))
  (defn get-object-request
    ([p__20453]
      (let [map__20454 p__20453
            map__20454 (if (seq? map__20454)
                         (if (next map__20454)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20454))
                           (if (seq map__20454) (first map__20454) {}))
                         map__20454)
            bucket (get map__20454 :bucket)
            key (get map__20454 :key)]
        {:op :GetObject, :request {:Bucket bucket, :Key key}})))
  (defn put-object-request
    ([p__20456]
      (let [map__20457 p__20456
            map__20457 (if (seq? map__20457)
                         (if (next map__20457)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20457))
                           (if (seq map__20457) (first map__20457) {}))
                         map__20457)
            body (get map__20457 :body)
            bucket (get map__20457 :bucket)
            content_length (get map__20457 :content-length)
            key (get map__20457 :key)]
        {:op :PutObject,
         :request
         (cond->
           {:Bucket bucket, :Key key, :Body body}
           content_length
           (assoc :ContentLength content_length))})))
  (defn get-bytes
    ([p__20460]
      (let [map__20461 p__20460
            map__20461 (if (seq? map__20461)
                         (if (next map__20461)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20461))
                           (if (seq map__20461) (first map__20461) {}))
                         map__20461)
            bucket (get map__20461 :bucket)
            client (get map__20461 :client)
            key (get map__20461 :key)
            c__10230__auto__ (clojure.core.async/chan 1)
            captured_bindings__10231__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__20487
            ([]
              (let [G__20462 (fn G__20462 ([] p__20460))
                    G__20463 (fn G__20463 ([] map__20461))
                    G__20464 (fn G__20464 ([] bucket))
                    G__20465 (fn G__20465 ([] client))
                    G__20466 (fn G__20466 ([] key))
                    f__10232__auto__ (fn state_machine__9975__auto__
                                       ([]
                                         (let [statearr_20499 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 11))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20499
                                             0
                                             state_machine__9975__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20499
                                             1
                                             1)
                                           statearr_20499))
                                       ([state_20486]
                                         (let [old_frame__9976__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__9977__auto__ (try
                                                                         (do
                                                                           (clojure.lang.Var/resetThreadBindingFrame
                                                                             (clojure.core.async.impl.ioc-macros/aget-object
                                                                               state_20486
                                                                               3))
                                                                           (loop 
                                                                             []
                                                                             (let 
                                                                               [result__9978__auto__
                                                                                (let 
                                                                                  [G__20501
                                                                                   (int
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_20486
                                                                                       1))]
                                                                                  (case
                                                                                    G__20501
                                                                                    1
                                                                                    (let 
                                                                                      [inst_20474
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         6)
                                                                                       inst_20475
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         7)
                                                                                       inst_20477
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         8)
                                                                                       inst_20476
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         9)
                                                                                       inst_20473
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         10)
                                                                                       inst_20468
                                                                                       (^clojure.lang.IFn G__20462)
                                                                                       p__20460
                                                                                       inst_20468
                                                                                       inst_20469
                                                                                       (^clojure.lang.IFn G__20463)
                                                                                       p__20460
                                                                                       inst_20468
                                                                                       map__20461
                                                                                       inst_20469
                                                                                       inst_20470
                                                                                       (^clojure.lang.IFn G__20464)
                                                                                       p__20460
                                                                                       inst_20468
                                                                                       map__20461
                                                                                       inst_20469
                                                                                       bucket
                                                                                       inst_20470
                                                                                       inst_20471
                                                                                       (^clojure.lang.IFn G__20465)
                                                                                       map__20461
                                                                                       inst_20469
                                                                                       bucket
                                                                                       inst_20470
                                                                                       client
                                                                                       inst_20471
                                                                                       p__20460
                                                                                       inst_20468
                                                                                       inst_20472
                                                                                       (^clojure.lang.IFn G__20466)
                                                                                       inst_20473
                                                                                       inst_20468
                                                                                       inst_20474
                                                                                       inst_20469
                                                                                       inst_20475
                                                                                       inst_20470
                                                                                       inst_20476
                                                                                       inst_20471
                                                                                       inst_20477
                                                                                       inst_20472
                                                                                       map__20461
                                                                                       inst_20474
                                                                                       bucket
                                                                                       inst_20475
                                                                                       key
                                                                                       inst_20477
                                                                                       client
                                                                                       inst_20476
                                                                                       p__20460
                                                                                       inst_20473
                                                                                       inst_20478
                                                                                       (aws/invoke-async
                                                                                         client
                                                                                         (get-object-request
                                                                                           {:bucket
                                                                                            bucket,
                                                                                            :key
                                                                                            key}))
                                                                                       state_20486
                                                                                       (let 
                                                                                         [statearr_20502
                                                                                          state_20486]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20502
                                                                                           10
                                                                                           inst_20473)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20502
                                                                                           6
                                                                                           inst_20474)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20502
                                                                                           7
                                                                                           inst_20475)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20502
                                                                                           9
                                                                                           inst_20476)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20502
                                                                                           8
                                                                                           inst_20477)
                                                                                         statearr_20502)]
                                                                                      (clojure.core.async.impl.ioc-macros/take!
                                                                                        state_20486
                                                                                        2
                                                                                        inst_20478))
                                                                                    2
                                                                                    (let 
                                                                                      [inst_20474
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         6)
                                                                                       inst_20475
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         7)
                                                                                       inst_20477
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         8)
                                                                                       inst_20476
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         9)
                                                                                       inst_20473
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         10)
                                                                                       inst_20480
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20486
                                                                                         2)
                                                                                       inst_20481
                                                                                       inst_20480
                                                                                       map__20461
                                                                                       inst_20474
                                                                                       v__19654__auto__
                                                                                       inst_20481
                                                                                       bucket
                                                                                       inst_20475
                                                                                       key
                                                                                       inst_20477
                                                                                       client
                                                                                       inst_20476
                                                                                       p__20460
                                                                                       inst_20473
                                                                                       inst_20482
                                                                                       (or
                                                                                         (datomic.core2.async/channel-closed-error
                                                                                           v__19654__auto__)
                                                                                         v__19654__auto__)
                                                                                       inst_20483
                                                                                       inst_20482
                                                                                       map__20461
                                                                                       inst_20474
                                                                                       bucket
                                                                                       inst_20475
                                                                                       key
                                                                                       inst_20477
                                                                                       client
                                                                                       inst_20476
                                                                                       p__20460
                                                                                       inst_20473
                                                                                       result
                                                                                       inst_20483
                                                                                       inst_20484
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
                                                                                               (canom/fault
                                                                                                 t)))))]
                                                                                      (clojure.core.async.impl.ioc-macros/return-chan
                                                                                        state_20486
                                                                                        inst_20484))))]
                                                                               (if
                                                                                 (identical?
                                                                                   result__9978__auto__
                                                                                   :recur)
                                                                                 (recur)
                                                                                 result__9978__auto__))))
                                                                         (catch
                                                                           java.lang.Throwable
                                                                           ex__9979__auto__
                                                                           (do
                                                                             (let 
                                                                               [statearr_20505
                                                                                state_20486]
                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                 statearr_20505
                                                                                 2
                                                                                 ex__9979__auto__))
                                                                             (if
                                                                               (seq
                                                                                 (clojure.core.async.impl.ioc-macros/aget-object
                                                                                   state_20486
                                                                                   4))
                                                                               (let 
                                                                                 [statearr_20506
                                                                                  state_20486]
                                                                                 (clojure.core.async.impl.ioc-macros/aset-object
                                                                                   statearr_20506
                                                                                   1
                                                                                   (first
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_20486
                                                                                       4))))
                                                                               (throw
                                                                                 ^java.lang.Throwable ex__9979__auto__))
                                                                             :recur))
                                                                         (finally
                                                                           (do
                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                               state_20486
                                                                               3
                                                                               (clojure.lang.Var/getThreadBindingFrame))
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               old_frame__9976__auto__))))]
                                           (if (identical? ret_value__9977__auto__ :recur)
                                             (recur state_20486)
                                             ret_value__9977__auto__))))
                    state__10233__auto__ (let [statearr_20515 (^clojure.lang.IFn f__10232__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20515
                                             5
                                             c__10230__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20515
                                             3
                                             captured_bindings__10231__auto__)
                                           statearr_20515)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__10233__auto__)))))
        c__10230__auto__))))