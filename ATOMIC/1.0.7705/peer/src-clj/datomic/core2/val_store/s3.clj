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
  (reset-meta!
    #'storage-key
    (assoc
      {:arglists (clojure.core/list ['prefix 'key 'opts]), :column (int 1)}
      :name
      'storage-key
      :ns
      *ns*))
  (def success-metrics
   {:create :s3.put.succeeded.msec,
    :get :s3.get.succeeded.msec,
    :delete :s3.delete.succeeded.msec})
  (reset-meta! #'success-metrics (assoc {:column (int 1)} :name 'success-metrics :ns *ns*))
  (def failure-metrics
   {:create :s3.put.failed.msec, :get :s3.get.failed.msec, :delete :s3.delete.failed.msec})
  (reset-meta! #'failure-metrics (assoc {:column (int 1)} :name 'failure-metrics :ns *ns*))
  (def retry-success-metrics
   {:create :s3.put.retry.succeeded,
    :get :s3.get.retry.succeeded,
    :delete :s3.delete.retry.succeeded})
  (reset-meta!
    #'retry-success-metrics
    (assoc {:column (int 1)} :name 'retry-success-metrics :ns *ns*))
  (def retry-failure-metrics
   {:create :s3.put.retry.failed, :get :s3.get.retry.failed, :delete :s3.delete.retry.failed})
  (reset-meta!
    #'retry-failure-metrics
    (assoc {:column (int 1)} :name 'retry-failure-metrics :ns *ns*))
  (defn go-with-metrics
    ([f k op context]
      (let [c__10363__auto__ (clojure.core.async/chan 1)
            captured_bindings__10364__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__22235
            ([]
              (let [G__22209 (fn G__22209 ([] f))
                    G__22210 (fn G__22210 ([] k))
                    G__22211 (fn G__22211 ([] op))
                    G__22212 (fn G__22212 ([] context))
                    f__10365__auto__ (fn state_machine__10108__auto__
                                       ([]
                                         (let [statearr_22245 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 11))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_22245
                                             0
                                             state_machine__10108__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_22245
                                             1
                                             1)
                                           statearr_22245))
                                       ([state_22234]
                                         (let [old_frame__10109__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__10110__auto__ (try
                                                                          (try
                                                                            (do
                                                                              (clojure.lang.Var/resetThreadBindingFrame
                                                                                (clojure.core.async.impl.ioc-macros/aget-object
                                                                                  state_22234
                                                                                  3))
                                                                              (loop
                                                                                []
                                                                                (let
                                                                                  [result__10111__auto__
                                                                                   (let
                                                                                     [G__22247
                                                                                      (int
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_22234
                                                                                          1))]
                                                                                     (case
                                                                                       G__22247
                                                                                       1
                                                                                       (let
                                                                                         [inst_22218
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            6)
                                                                                          inst_22219
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            7)
                                                                                          inst_22220
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            8)
                                                                                          inst_22221
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            9)
                                                                                          inst_22222
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            10)
                                                                                          inst_22214
                                                                                          (^clojure.lang.IFn G__22209)
                                                                                          f
                                                                                          inst_22214
                                                                                          inst_22215
                                                                                          (^clojure.lang.IFn G__22210)
                                                                                          f
                                                                                          inst_22214
                                                                                          k
                                                                                          inst_22215
                                                                                          inst_22216
                                                                                          (^clojure.lang.IFn G__22211)
                                                                                          f
                                                                                          inst_22214
                                                                                          k
                                                                                          inst_22215
                                                                                          op
                                                                                          inst_22216
                                                                                          inst_22217
                                                                                          (^clojure.lang.IFn G__22212)
                                                                                          inst_22218
                                                                                          inst_22214
                                                                                          inst_22219
                                                                                          inst_22215
                                                                                          inst_22220
                                                                                          inst_22216
                                                                                          inst_22221
                                                                                          inst_22217
                                                                                          f
                                                                                          inst_22218
                                                                                          k
                                                                                          inst_22219
                                                                                          op
                                                                                          inst_22220
                                                                                          context
                                                                                          inst_22221
                                                                                          inst_22222
                                                                                          (java.lang.System/nanoTime)
                                                                                          start_nsec
                                                                                          inst_22222
                                                                                          op
                                                                                          inst_22220
                                                                                          k
                                                                                          inst_22219
                                                                                          context
                                                                                          inst_22221
                                                                                          f
                                                                                          inst_22218
                                                                                          inst_22223
                                                                                          (^clojure.lang.IFn f)
                                                                                          state_22234
                                                                                          (let
                                                                                            [statearr_22248
                                                                                             state_22234]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_22248
                                                                                              6
                                                                                              inst_22218)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_22248
                                                                                              7
                                                                                              inst_22219)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_22248
                                                                                              8
                                                                                              inst_22220)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_22248
                                                                                              9
                                                                                              inst_22221)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_22248
                                                                                              10
                                                                                              (long
                                                                                                inst_22222))
                                                                                            statearr_22248)]
                                                                                         (clojure.core.async.impl.ioc-macros/take!
                                                                                           state_22234
                                                                                           2
                                                                                           inst_22223))
                                                                                       2
                                                                                       (let
                                                                                         [inst_22222
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            10)
                                                                                          inst_22220
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            8)
                                                                                          inst_22219
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            7)
                                                                                          inst_22221
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            9)
                                                                                          inst_22218
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            6)
                                                                                          inst_22225
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_22234
                                                                                            2)
                                                                                          inst_22226
                                                                                          inst_22225
                                                                                          v__20478__auto__
                                                                                          inst_22226
                                                                                          start_nsec
                                                                                          inst_22222
                                                                                          op
                                                                                          inst_22220
                                                                                          k
                                                                                          inst_22219
                                                                                          context
                                                                                          inst_22221
                                                                                          f
                                                                                          inst_22218
                                                                                          inst_22227
                                                                                          (or
                                                                                            (datomic.core2.async/channel-closed-error
                                                                                              v__20478__auto__)
                                                                                            v__20478__auto__)
                                                                                          start_nsec
                                                                                          inst_22222
                                                                                          op
                                                                                          inst_22220
                                                                                          k
                                                                                          inst_22219
                                                                                          result
                                                                                          inst_22227
                                                                                          context
                                                                                          inst_22221
                                                                                          f
                                                                                          inst_22218
                                                                                          inst_22228
                                                                                          (-
                                                                                            (java.lang.System/nanoTime)
                                                                                            start_nsec)
                                                                                          inst_22229
                                                                                          inst_22222
                                                                                          inst_22230
                                                                                          inst_22227
                                                                                          inst_22231
                                                                                          inst_22228
                                                                                          start_nsec
                                                                                          inst_22229
                                                                                          nsec
                                                                                          inst_22231
                                                                                          op
                                                                                          inst_22220
                                                                                          k
                                                                                          inst_22219
                                                                                          result
                                                                                          inst_22230
                                                                                          context
                                                                                          inst_22221
                                                                                          f
                                                                                          inst_22218]
                                                                                         (when
                                                                                           (=
                                                                                             op
                                                                                             :get)
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
                                                                                           [inst_22232
                                                                                            result]
                                                                                           (clojure.core.async.impl.ioc-macros/return-chan
                                                                                             state_22234
                                                                                             inst_22232)))))]
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
                                                                                  [statearr_22249
                                                                                   state_22234]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_22249
                                                                                    2
                                                                                    ex__10112__auto__))
                                                                                (if
                                                                                  (seq
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_22234
                                                                                      4))
                                                                                  (let
                                                                                    [statearr_22250
                                                                                     state_22234]
                                                                                    (clojure.core.async.impl.ioc-macros/aset-object
                                                                                      statearr_22250
                                                                                      1
                                                                                      (first
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_22234
                                                                                          4))))
                                                                                  (throw
                                                                                    ^java.lang.Throwable ex__10112__auto__))
                                                                                :recur)))
                                                                          (finally
                                                                            (do
                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                state_22234
                                                                                3
                                                                                (clojure.lang.Var/getThreadBindingFrame))
                                                                              (clojure.lang.Var/resetThreadBindingFrame
                                                                                old_frame__10109__auto__))))]
                                           (if (identical? ret_value__10110__auto__ :recur)
                                             (recur state_22234)
                                             ret_value__10110__auto__))))
                    state__10366__auto__ (let [statearr_22258 (^clojure.lang.IFn f__10365__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_22258
                                             5
                                             c__10363__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_22258
                                             3
                                             captured_bindings__10364__auto__)
                                           statearr_22258)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__10366__auto__)))))
        c__10363__auto__)))
  (reset-meta!
    #'go-with-metrics
    (assoc
      {:arglists (clojure.core/list ['f 'k 'op 'context]), :column (int 1)}
      :name
      'go-with-metrics
      :ns
      *ns*))
  (defn wrap-metric-handler
    ([f k op context]
      (let [start_nsec (java.lang.System/nanoTime)]
        (fn fn__22265
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
  (reset-meta!
    #'wrap-metric-handler
    (assoc
      {:arglists (clojure.core/list ['f 'k 'op 'context]), :column (int 1)}
      :name
      'wrap-metric-handler
      :ns
      *ns*))
  (defn retry-handler
    ([f op p__22268]
      (let [map__22269 p__22268
            map__22269 (if (seq? map__22269)
                         (if (next map__22269)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22269))
                           (if (seq map__22269) (first map__22269) {}))
                         map__22269)
            backoff (get map__22269 :backoff 200)
            base (get map__22269 :base 2)
            retriable? (get map__22269 :retriable? (partial retry/limiting-retry 5))
            metric_cb (fn metric_cb
                        ([p__22270]
                          (let [map__22272 p__22270
                                map__22272 (if (seq? map__22272)
                                             (if (next map__22272)
                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                 (to-array map__22272))
                                               (if (seq map__22272) (first map__22272) {}))
                                             map__22272)
                                m map__22272
                                ok? (get map__22272 :ok?)
                                i (get map__22272 :i)
                                result (get map__22272 :result)
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
        (fn fn__22274
          ([]
            (retry/retry
              f
              canom/ok?
              retriable?
              (fn fn__22275
                ([round_map]
                  (retry/full-jitter
                    (long
                      (retry/calc-exp-backoff
                        (long ^java.lang.Number backoff)
                        (long ^java.lang.Number base)
                        round_map)))))
              {:on-success metric_cb, :on-failure metric_cb}))))))
  (reset-meta!
    #'retry-handler
    (assoc
      {:arglists
       (clojure.core/list
         ['f
          'op
          {:keys ['backoff 'base 'retriable?],
           :or
           {'backoff 200,
            'base 2,
            'retriable?
            (.withMeta
              (clojure.core/list 'partial 'retry/limiting-retry 5)
              {:column (int 45)})}}]),
       :column (int 1)}
      :name
      'retry-handler
      :ns
      *ns*)))