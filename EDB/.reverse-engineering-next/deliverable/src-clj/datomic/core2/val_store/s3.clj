(do
  (clojure.core/in-ns 'datomic.core2.val-store.s3)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :refer (clojure.core/list 'go)]
        ['cognitect.caster :as 'cast]
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.core2.retry :as 'retry]
        ['datomic.core2.async :refer (clojure.core/list '<!x)]
        ['datomic.core2.val-store.spi :as 'spi]
        ['datomic.measure.io-stats :as 'io-stats])))
  (when-not (.equals 'datomic.core2.val-store.s3 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.val-store.s3))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :refer (clojure.core/list 'go)]
          ['cognitect.caster :as 'cast]
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.core2.retry :as 'retry]
          ['datomic.core2.async :refer (clojure.core/list '<!x)]
          ['datomic.core2.val-store.spi :as 'spi]
          ['datomic.measure.io-stats :as 'io-stats]))))
  (set! *warn-on-reflection* true)
  (defn storage-key
    ([prefix key opts]
      (str
        prefix
        "/"
        (if (= :skip (:datomic.core2.val-store.opts/partition opts))
          key
          (spi/splice-partition-key key (spi/partition-key key))))))
  (def success-metrics
   {:create :s3.put.succeeded.msec,
    :get :s3.get.succeeded.msec,
    :delete :s3.delete.succeeded.msec})
  (def failure-metrics
   {:create :s3.put.failed.msec, :get :s3.get.failed.msec, :delete :s3.delete.failed.msec})
  (def retry-success-metrics
   {:create :s3.put.retry.succeeded,
    :get :s3.get.retry.succeeded,
    :delete :s3.delete.retry.succeeded})
  (def retry-failure-metrics
   {:create :s3.put.retry.failed, :get :s3.get.retry.failed, :delete :s3.delete.retry.failed})
  (defn go-with-metrics
    ([f k op context]
      (let [c__10230__auto__ (clojure.core.async/chan 1)
            captured_bindings__10231__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__21378
            ([]
              (let [G__21352 (fn G__21352 ([] f))
                    G__21353 (fn G__21353 ([] k))
                    G__21354 (fn G__21354 ([] op))
                    G__21355 (fn G__21355 ([] context))
                    f__10232__auto__ (fn state_machine__9975__auto__
                                       ([]
                                         (let [statearr_21388 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 11))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21388
                                             0
                                             state_machine__9975__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21388
                                             1
                                             1)
                                           statearr_21388))
                                       ([state_21377]
                                         (let [old_frame__9976__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__9977__auto__ (try
                                                                         (do
                                                                           (clojure.lang.Var/resetThreadBindingFrame
                                                                             (clojure.core.async.impl.ioc-macros/aget-object
                                                                               state_21377
                                                                               3))
                                                                           (loop 
                                                                             []
                                                                             (let 
                                                                               [result__9978__auto__
                                                                                (let 
                                                                                  [G__21390
                                                                                   (int
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_21377
                                                                                       1))]
                                                                                  (case
                                                                                    G__21390
                                                                                    1
                                                                                    (let 
                                                                                      [inst_21361
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         6)
                                                                                       inst_21362
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         7)
                                                                                       inst_21363
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         8)
                                                                                       inst_21364
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         9)
                                                                                       inst_21365
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         10)
                                                                                       inst_21357
                                                                                       (^clojure.lang.IFn G__21352)
                                                                                       f inst_21357
                                                                                       inst_21358
                                                                                       (^clojure.lang.IFn G__21353)
                                                                                       f inst_21357
                                                                                       k inst_21358
                                                                                       inst_21359
                                                                                       (^clojure.lang.IFn G__21354)
                                                                                       f inst_21357
                                                                                       k inst_21358
                                                                                       op
                                                                                       inst_21359
                                                                                       inst_21360
                                                                                       (^clojure.lang.IFn G__21355)
                                                                                       inst_21361
                                                                                       inst_21357
                                                                                       inst_21362
                                                                                       inst_21358
                                                                                       inst_21363
                                                                                       inst_21359
                                                                                       inst_21364
                                                                                       inst_21360
                                                                                       f inst_21361
                                                                                       k inst_21362
                                                                                       op
                                                                                       inst_21363
                                                                                       context
                                                                                       inst_21364
                                                                                       inst_21365
                                                                                       (java.lang.System/nanoTime)
                                                                                       start_nsec
                                                                                       inst_21365
                                                                                       op
                                                                                       inst_21363
                                                                                       k inst_21362
                                                                                       context
                                                                                       inst_21364
                                                                                       f inst_21361
                                                                                       inst_21366
                                                                                       (^clojure.lang.IFn f)
                                                                                       state_21377
                                                                                       (let 
                                                                                         [statearr_21391
                                                                                          state_21377]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_21391
                                                                                           6
                                                                                           inst_21361)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_21391
                                                                                           7
                                                                                           inst_21362)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_21391
                                                                                           8
                                                                                           inst_21363)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_21391
                                                                                           9
                                                                                           inst_21364)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_21391
                                                                                           10
                                                                                           (long
                                                                                             inst_21365))
                                                                                         statearr_21391)]
                                                                                      (clojure.core.async.impl.ioc-macros/take!
                                                                                        state_21377
                                                                                        2
                                                                                        inst_21366))
                                                                                    2
                                                                                    (let 
                                                                                      [inst_21365
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         10)
                                                                                       inst_21363
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         8)
                                                                                       inst_21362
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         7)
                                                                                       inst_21364
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         9)
                                                                                       inst_21361
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         6)
                                                                                       inst_21368
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_21377
                                                                                         2)
                                                                                       inst_21369
                                                                                       inst_21368
                                                                                       v__19654__auto__
                                                                                       inst_21369
                                                                                       start_nsec
                                                                                       inst_21365
                                                                                       op
                                                                                       inst_21363
                                                                                       k inst_21362
                                                                                       context
                                                                                       inst_21364
                                                                                       f inst_21361
                                                                                       inst_21370
                                                                                       (or
                                                                                         (datomic.core2.async/channel-closed-error
                                                                                           v__19654__auto__)
                                                                                         v__19654__auto__)
                                                                                       start_nsec
                                                                                       inst_21365
                                                                                       op
                                                                                       inst_21363
                                                                                       k inst_21362
                                                                                       result
                                                                                       inst_21370
                                                                                       context
                                                                                       inst_21364
                                                                                       f inst_21361
                                                                                       inst_21371
                                                                                       (-
                                                                                         (java.lang.System/nanoTime)
                                                                                         start_nsec)
                                                                                       inst_21372
                                                                                       inst_21365
                                                                                       inst_21373
                                                                                       inst_21370
                                                                                       inst_21374
                                                                                       inst_21371
                                                                                       start_nsec
                                                                                       inst_21372
                                                                                       nsec
                                                                                       inst_21374
                                                                                       op
                                                                                       inst_21363
                                                                                       k inst_21362
                                                                                       result
                                                                                       inst_21373
                                                                                       context
                                                                                       inst_21364
                                                                                       f
                                                                                       inst_21361]
                                                                                      (when
                                                                                        (= op :get)
                                                                                        (io-stats/inc!
                                                                                          :s3-ns
                                                                                          (long
                                                                                            ^java.lang.Number nsec)))
                                                                                      (cast/metric*
                                                                                        cast/instance
                                                                                        {:name
                                                                                         (get
                                                                                           (if
                                                                                             (spi/val-op-succeeded?
                                                                                               result)
                                                                                             success-metrics
                                                                                             failure-metrics)
                                                                                           op),
                                                                                         :value
                                                                                         (java.lang.Double/valueOf
                                                                                           (double
                                                                                             (io-stats/ns->ms
                                                                                               (long
                                                                                                 ^java.lang.Number nsec)))),
                                                                                         :units
                                                                                         :msec,
                                                                                         :datomic.core2.val-store.s3/key
                                                                                         k})
                                                                                      (when
                                                                                        (canom/anom
                                                                                          result)
                                                                                        (cast/event*
                                                                                          cast/instance
                                                                                          (merge
                                                                                            result
                                                                                            context
                                                                                            {:msg
                                                                                             "S3 op failed"})))
                                                                                      (let 
                                                                                        [inst_21375
                                                                                         result]
                                                                                        (clojure.core.async.impl.ioc-macros/return-chan
                                                                                          state_21377
                                                                                          inst_21375)))))]
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
                                                                               [statearr_21392
                                                                                state_21377]
                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                 statearr_21392
                                                                                 2
                                                                                 ex__9979__auto__))
                                                                             (if
                                                                               (seq
                                                                                 (clojure.core.async.impl.ioc-macros/aget-object
                                                                                   state_21377
                                                                                   4))
                                                                               (let 
                                                                                 [statearr_21393
                                                                                  state_21377]
                                                                                 (clojure.core.async.impl.ioc-macros/aset-object
                                                                                   statearr_21393
                                                                                   1
                                                                                   (first
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_21377
                                                                                       4))))
                                                                               (throw
                                                                                 ^java.lang.Throwable ex__9979__auto__))
                                                                             :recur))
                                                                         (finally
                                                                           (do
                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                               state_21377
                                                                               3
                                                                               (clojure.lang.Var/getThreadBindingFrame))
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               old_frame__9976__auto__))))]
                                           (if (identical? ret_value__9977__auto__ :recur)
                                             (recur state_21377)
                                             ret_value__9977__auto__))))
                    state__10233__auto__ (let [statearr_21401 (^clojure.lang.IFn f__10232__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21401
                                             5
                                             c__10230__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21401
                                             3
                                             captured_bindings__10231__auto__)
                                           statearr_21401)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__10233__auto__)))))
        c__10230__auto__)))
  (defn wrap-metric-handler
    ([f k op context]
      (let [start_nsec (java.lang.System/nanoTime)]
        (fn fn__21408
          ([]
            (let [result (^clojure.lang.IFn f k)
                  ok? (spi/val-op-succeeded? result)
                  nsec (- (java.lang.System/nanoTime) start_nsec)]
              (when (= op :get)
                (io-stats/inc! :s3-ns nsec)
                (cast/metric* cast/instance {:name :s3.hits, :value (if ok? 1 0), :units :count}))
              (cast/metric*
                cast/instance
                {:name (get (if ok? success-metrics failure-metrics) op),
                 :value (java.lang.Double/valueOf (double (io-stats/ns->ms nsec))),
                 :units :msec,
                 :datomic.core2.val-store.s3/key k})
              (when (canom/anom result)
                (cast/event* cast/instance (merge result context {:msg "S3 op failed"})))
              result))))))
  (defn retry-handler
    ([f op p__21411]
      (let [map__21412 p__21411
            map__21412 (if (seq? map__21412)
                         (if (next map__21412)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21412))
                           (if (seq map__21412) (first map__21412) {}))
                         map__21412)
            backoff (get map__21412 :backoff 200)
            base (get map__21412 :base 2)
            retriable? (get map__21412 :retriable? (partial retry/limiting-retry 5))
            metric_cb (fn metric_cb
                        ([p__21413]
                          (let [map__21415 p__21413
                                map__21415 (if (seq? map__21415)
                                             (if (next map__21415)
                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                 (to-array map__21415))
                                               (if (seq map__21415) (first map__21415) {}))
                                             map__21415)
                                m map__21415
                                ok? (get map__21415 :ok?)
                                i (get map__21415 :i)
                                result (get map__21415 :result)
                                metric_name (get
                                              (if ok? retry-success-metrics retry-failure-metrics)
                                              op)]
                            (if ok?
                              (when (> (long ^java.lang.Number i) 1)
                                (cast/metric*
                                  cast/instance
                                  {:name metric_name, :value i, :units :count}))
                              (do
                                (cast/metric*
                                  cast/instance
                                  {:name metric_name, :value i, :units :count})
                                (cast/event*
                                  cast/instance
                                  (merge
                                    result
                                    {:msg "S3 op failed",
                                     :datomic.core2.val-store.s3/op op,
                                     :datomic.core2.val-store.s3/retry i})))))))]
        (fn fn__21417
          ([]
            (retry/retry
              f
              canom/ok?
              retriable?
              (fn fn__21418
                ([round_map]
                  (retry/full-jitter
                    (long
                      (retry/calc-exp-backoff
                        (long ^java.lang.Number backoff)
                        (long ^java.lang.Number base)
                        round_map)))))
              {:on-success metric_cb, :on-failure metric_cb})))))))