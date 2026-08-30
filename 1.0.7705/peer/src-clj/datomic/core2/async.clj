(do
  (clojure.core/in-ns 'datomic.core2.async)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async
         :as
         'a
         :refer
         (clojure.core/list
           '<!!
           '>!!
           'alts!
           'alts!!
           'alt!!
           'chan
           'offer!
           'timeout
           'go
           'go-loop
           'put!
           '<!
           'close!
           '>!)]
        ['cognitect.anomalies :as 'anom])))
  (when-not (.equals 'datomic.core2.async 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.async))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async
           :as
           'a
           :refer
           (clojure.core/list
             '<!!
             '>!!
             'alts!
             'alts!!
             'alt!!
             'chan
             'offer!
             'timeout
             'go
             'go-loop
             'put!
             '<!
             'close!
             '>!)]
          ['cognitect.anomalies :as 'anom]))))
  (defn aderef
    ([ch timeout_ms timeout_val]
      (let [to (a/timeout (long ^java.lang.Number timeout_ms))
            vec__20294 (a/alts!! [ch to])
            v (nth vec__20294 (int 0) nil)
            port (nth vec__20294 (int 1) nil)]
        (if (= port ch) v timeout_val)))
    ([ch timeout_ms]
      (aderef
        ch
        timeout_ms
        {:cognitect.anomalies/category :cognitect.anomalies/interrupted, :timeout-ms timeout_ms}))
    ([ch] (aderef ch 1000)))
  (reset-meta!
    #'aderef
    (assoc
      {:arglists (clojure.core/list ['ch] ['ch 'timeout-ms] ['ch 'timeout-ms 'timeout-val]),
       :column (int 1)}
      :name
      'aderef
      :ns
      *ns*))
  (defn aderef-n
    ([n ch timeout_ms timeout_val]
      (let [to (a/timeout (long ^java.lang.Number timeout_ms))]
        (loop [results []]
          (if (= (long (count results)) n)
            results
            (let [vec__20298 (a/alts!! [ch to])
                  v (nth vec__20298 (int 0) nil)
                  port (nth vec__20298 (int 1) nil)]
              (if (= port ch) (recur (conj results v)) (conj results timeout_val)))))))
    ([n ch timeout_ms]
      (aderef-n
        n
        ch
        timeout_ms
        {:cognitect.anomalies/category :cognitect.anomalies/interrupted, :timeout-ms timeout_ms}))
    ([n ch] (aderef-n n ch 1000)))
  (reset-meta!
    #'aderef-n
    (assoc
      {:arglists
       (clojure.core/list ['n 'ch] ['n 'ch 'timeout-ms] ['n 'ch 'timeout-ms 'timeout-val]),
       :column (int 1)}
      :name
      'aderef-n
      :ns
      *ns*))
  (defn retry
    ([& p__20302]
      (let [map__20303 p__20302
            map__20303 (if (seq? map__20303)
                         (if (next map__20303)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20303))
                           (if (seq map__20303) (first map__20303) {}))
                         map__20303)
            f (get map__20303 :f)
            pred (get map__20303 :pred)
            backoff (get map__20303 :backoff)
            ch (get map__20303 :ch (a/chan 1))
            fail (get map__20303 :fail identity)]
        (let [c__10363__auto__ (a/chan 1)
              captured_bindings__10364__auto__ (clojure.lang.Var/getThreadBindingFrame)]
          (clojure.core.async.impl.dispatch/run
            (fn fn__20359
              ([]
                (let [G__20304 (fn G__20304 ([] p__20302))
                      G__20305 (fn G__20305 ([] map__20303))
                      G__20306 (fn G__20306 ([] f))
                      G__20307 (fn G__20307 ([] pred))
                      G__20308 (fn G__20308 ([] backoff))
                      G__20309 (fn G__20309 ([] ch))
                      G__20310 (fn G__20310 ([] fail))
                      f__10365__auto__ (fn state_machine__10108__auto__
                                         ([]
                                           (let [statearr_20375 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                  (int 17))]
                                             (clojure.core.async.impl.ioc-macros/aset-object
                                               statearr_20375
                                               0
                                               state_machine__10108__auto__)
                                             (clojure.core.async.impl.ioc-macros/aset-object
                                               statearr_20375
                                               1
                                               1)
                                             statearr_20375))
                                         ([state_20358]
                                           (let [old_frame__10109__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                                 ret_value__10110__auto__ (try
                                                                            (try
                                                                              (do
                                                                                (clojure.lang.Var/resetThreadBindingFrame
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_20358
                                                                                    3))
                                                                                (loop 
                                                                                  []
                                                                                  (let 
                                                                                    [result__10111__auto__
                                                                                     (let 
                                                                                       [G__20377
                                                                                        (int
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20358
                                                                                            1))]
                                                                                       (case
                                                                                         G__20377
                                                                                         11
                                                                                         (let 
                                                                                           [inst_20352
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              2)]
                                                                                           (let 
                                                                                             [statearr_20388
                                                                                              state_20358]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20388
                                                                                               2
                                                                                               inst_20352)
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20388
                                                                                               1
                                                                                               7))
                                                                                           :recur)
                                                                                         9
                                                                                         (let 
                                                                                           [inst_20339
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              15)
                                                                                            inst_20341
                                                                                            inst_20339
                                                                                            inst_20342
                                                                                            (a/timeout
                                                                                              (long
                                                                                                ^java.lang.Number inst_20341))]
                                                                                           (clojure.core.async.impl.ioc-macros/take!
                                                                                             state_20358
                                                                                             12
                                                                                             inst_20342))
                                                                                         6
                                                                                         (let 
                                                                                           [inst_20324
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              10)
                                                                                            inst_20327
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              13)
                                                                                            inst_20332
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              14)
                                                                                            inst_20339
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              15)
                                                                                            inst_20338
                                                                                            (^clojure.lang.IFn inst_20324
                                                                                              inst_20327
                                                                                              inst_20332)
                                                                                            inst_20339
                                                                                            inst_20338
                                                                                            state_20358
                                                                                            (let 
                                                                                              [statearr_20383
                                                                                               state_20358]
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20383
                                                                                                15
                                                                                                inst_20339)
                                                                                              statearr_20383)]
                                                                                           (if
                                                                                             inst_20339
                                                                                             (let 
                                                                                               [statearr_20384
                                                                                                state_20358]
                                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                 statearr_20384
                                                                                                 1
                                                                                                 9))
                                                                                             (let 
                                                                                               [statearr_20385
                                                                                                state_20358]
                                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                 statearr_20385
                                                                                                 1
                                                                                                 10)))
                                                                                           :recur)
                                                                                         4
                                                                                         (let 
                                                                                           [inst_20323
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              9)
                                                                                            inst_20332
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              14)
                                                                                            inst_20331
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              2)
                                                                                            inst_20332
                                                                                            inst_20331
                                                                                            inst_20333
                                                                                            (^clojure.lang.IFn inst_20323
                                                                                              inst_20332)
                                                                                            state_20358
                                                                                            (let 
                                                                                              [statearr_20380
                                                                                               state_20358]
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20380
                                                                                                14
                                                                                                inst_20332)
                                                                                              statearr_20380)]
                                                                                           (if
                                                                                             inst_20333
                                                                                             (let 
                                                                                               [statearr_20381
                                                                                                state_20358]
                                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                 statearr_20381
                                                                                                 1
                                                                                                 5))
                                                                                             (let 
                                                                                               [statearr_20382
                                                                                                state_20358]
                                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                 statearr_20382
                                                                                                 1
                                                                                                 6)))
                                                                                           :recur)
                                                                                         1
                                                                                         (let 
                                                                                           [inst_20313
                                                                                            (^clojure.lang.IFn G__20304)
                                                                                            p__20302
                                                                                            inst_20313
                                                                                            inst_20314
                                                                                            (^clojure.lang.IFn G__20305)
                                                                                            map__20303
                                                                                            inst_20314
                                                                                            p__20302
                                                                                            inst_20313
                                                                                            inst_20315
                                                                                            (^clojure.lang.IFn G__20306)
                                                                                            map__20303
                                                                                            inst_20314
                                                                                            p__20302
                                                                                            inst_20313
                                                                                            f
                                                                                            inst_20315
                                                                                            inst_20316
                                                                                            (^clojure.lang.IFn G__20307)
                                                                                            pred
                                                                                            inst_20316
                                                                                            map__20303
                                                                                            inst_20314
                                                                                            p__20302
                                                                                            inst_20313
                                                                                            f
                                                                                            inst_20315
                                                                                            inst_20317
                                                                                            (^clojure.lang.IFn G__20308)
                                                                                            pred
                                                                                            inst_20316
                                                                                            map__20303
                                                                                            inst_20314
                                                                                            backoff
                                                                                            inst_20317
                                                                                            p__20302
                                                                                            inst_20313
                                                                                            f
                                                                                            inst_20315
                                                                                            inst_20318
                                                                                            (^clojure.lang.IFn G__20309)
                                                                                            pred
                                                                                            inst_20316
                                                                                            ch
                                                                                            inst_20318
                                                                                            map__20303
                                                                                            inst_20314
                                                                                            backoff
                                                                                            inst_20317
                                                                                            p__20302
                                                                                            inst_20313
                                                                                            f
                                                                                            inst_20315
                                                                                            inst_20319
                                                                                            (^clojure.lang.IFn G__20310)
                                                                                            inst_20320
                                                                                            inst_20313
                                                                                            inst_20321
                                                                                            inst_20314
                                                                                            inst_20322
                                                                                            inst_20315
                                                                                            inst_20323
                                                                                            inst_20316
                                                                                            inst_20324
                                                                                            inst_20317
                                                                                            inst_20325
                                                                                            inst_20318
                                                                                            inst_20326
                                                                                            inst_20319
                                                                                            inst_20327
                                                                                            1
                                                                                            state_20358
                                                                                            (let 
                                                                                              [statearr_20378
                                                                                               state_20358]
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20378
                                                                                                6
                                                                                                inst_20320)
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20378
                                                                                                7
                                                                                                inst_20321)
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20378
                                                                                                8
                                                                                                inst_20322)
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20378
                                                                                                9
                                                                                                inst_20323)
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20378
                                                                                                10
                                                                                                inst_20324)
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20378
                                                                                                11
                                                                                                inst_20325)
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20378
                                                                                                12
                                                                                                inst_20326)
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20378
                                                                                                13
                                                                                                (long
                                                                                                  inst_20327))
                                                                                              statearr_20378)]
                                                                                           (let 
                                                                                             [statearr_20379
                                                                                              state_20358]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20379
                                                                                               2
                                                                                               nil)
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20379
                                                                                               1
                                                                                               2))
                                                                                           :recur)
                                                                                         2
                                                                                         (let 
                                                                                           [inst_20322
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              8)
                                                                                            inst_20329
                                                                                            (^clojure.lang.IFn inst_20322)]
                                                                                           (clojure.core.async.impl.ioc-macros/take!
                                                                                             state_20358
                                                                                             4
                                                                                             inst_20329))
                                                                                         7
                                                                                         (let 
                                                                                           [inst_20354
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              2)]
                                                                                           (let 
                                                                                             [statearr_20386
                                                                                              state_20358]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20386
                                                                                               2
                                                                                               inst_20354)
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20386
                                                                                               1
                                                                                               3))
                                                                                           :recur)
                                                                                         12
                                                                                         (let 
                                                                                           [inst_20327
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              13)
                                                                                            inst_20344
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              2)
                                                                                            inst_20345
                                                                                            (inc
                                                                                              inst_20327)
                                                                                            inst_20327
                                                                                            inst_20345
                                                                                            state_20358
                                                                                            (let 
                                                                                              [statearr_20389
                                                                                               state_20358]
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20389
                                                                                                16
                                                                                                inst_20344)
                                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                                statearr_20389
                                                                                                13
                                                                                                inst_20327)
                                                                                              statearr_20389)]
                                                                                           (let 
                                                                                             [statearr_20390
                                                                                              state_20358]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20390
                                                                                               2
                                                                                               nil)
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20390
                                                                                               1
                                                                                               2))
                                                                                           :recur)
                                                                                         13
                                                                                         (let 
                                                                                           [inst_20350
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              2)]
                                                                                           (let 
                                                                                             [statearr_20391
                                                                                              state_20358]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20391
                                                                                               2
                                                                                               inst_20350)
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20391
                                                                                               1
                                                                                               11))
                                                                                           :recur)
                                                                                         10
                                                                                         (let 
                                                                                           [inst_20339
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              15)
                                                                                            inst_20323
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              9)
                                                                                            inst_20327
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              13)
                                                                                            inst_20325
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              11)
                                                                                            inst_20321
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              7)
                                                                                            inst_20326
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              12)
                                                                                            inst_20332
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              14)
                                                                                            inst_20324
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              10)
                                                                                            inst_20320
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              6)
                                                                                            inst_20322
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              8)
                                                                                            temp__5823__auto__
                                                                                            inst_20339
                                                                                            pred
                                                                                            inst_20323
                                                                                            n
                                                                                            inst_20327
                                                                                            ch
                                                                                            inst_20325
                                                                                            map__20303
                                                                                            inst_20321
                                                                                            fail
                                                                                            inst_20326
                                                                                            result
                                                                                            inst_20332
                                                                                            backoff
                                                                                            inst_20324
                                                                                            p__20302
                                                                                            inst_20320
                                                                                            f
                                                                                            inst_20322
                                                                                            inst_20348
                                                                                            (^clojure.lang.IFn fail
                                                                                              result)]
                                                                                           (clojure.core.async.impl.ioc-macros/put!
                                                                                             state_20358
                                                                                             13
                                                                                             inst_20325
                                                                                             inst_20348))
                                                                                         3
                                                                                         (let 
                                                                                           [inst_20356
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              2)]
                                                                                           (clojure.core.async.impl.ioc-macros/return-chan
                                                                                             state_20358
                                                                                             inst_20356))
                                                                                         5
                                                                                         (let 
                                                                                           [inst_20325
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              11)
                                                                                            inst_20332
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              14)]
                                                                                           (clojure.core.async.impl.ioc-macros/put!
                                                                                             state_20358
                                                                                             8
                                                                                             inst_20325
                                                                                             inst_20332))
                                                                                         8
                                                                                         (let 
                                                                                           [inst_20336
                                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                                              state_20358
                                                                                              2)]
                                                                                           (let 
                                                                                             [statearr_20387
                                                                                              state_20358]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20387
                                                                                               2
                                                                                               inst_20336)
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20387
                                                                                               1
                                                                                               7))
                                                                                           :recur)))]
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
                                                                                    [statearr_20392
                                                                                     state_20358]
                                                                                    (clojure.core.async.impl.ioc-macros/aset-object
                                                                                      statearr_20392
                                                                                      2
                                                                                      ex__10112__auto__))
                                                                                  (if
                                                                                    (seq
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_20358
                                                                                        4))
                                                                                    (let 
                                                                                      [statearr_20393
                                                                                       state_20358]
                                                                                      (clojure.core.async.impl.ioc-macros/aset-object
                                                                                        statearr_20393
                                                                                        1
                                                                                        (first
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20358
                                                                                            4))))
                                                                                    (throw
                                                                                      ^java.lang.Throwable ex__10112__auto__))
                                                                                  :recur)))
                                                                            (finally
                                                                              (do
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  state_20358
                                                                                  3
                                                                                  (clojure.lang.Var/getThreadBindingFrame))
                                                                                (clojure.lang.Var/resetThreadBindingFrame
                                                                                  old_frame__10109__auto__))))]
                                             (if (identical? ret_value__10110__auto__ :recur)
                                               (recur state_20358)
                                               ret_value__10110__auto__))))
                      state__10366__auto__ (let [statearr_20400 (^clojure.lang.IFn f__10365__auto__)]
                                             (clojure.core.async.impl.ioc-macros/aset-object
                                               statearr_20400
                                               5
                                               c__10363__auto__)
                                             (clojure.core.async.impl.ioc-macros/aset-object
                                               statearr_20400
                                               3
                                               captured_bindings__10364__auto__)
                                             statearr_20400)]
                  (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                    state__10366__auto__))))))
        ch)))
  (reset-meta!
    #'retry
    (assoc
      {:arglists
       (clojure.core/list
         ['&
          {:keys ['f 'pred 'backoff 'ch 'fail],
           :or
           {'ch (.withMeta (clojure.core/list 'a/chan 1) {:column (int 15)}), 'fail 'identity}}]),
       :column (int 1)}
      :name
      'retry
      :ns
      *ns*))
  (defn put-all!
    ([ch coll close?]
      (let [c__10363__auto__ (a/chan 1)
            captured_bindings__10364__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__20440
            ([]
              (let [G__20407 (fn G__20407 ([] ch))
                    G__20408 (fn G__20408 ([] coll))
                    G__20409 (fn G__20409 ([] close?))
                    f__10365__auto__ (fn state_machine__10108__auto__
                                       ([]
                                         (let [statearr_20448 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 10))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20448
                                             0
                                             state_machine__10108__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20448
                                             1
                                             1)
                                           statearr_20448))
                                       ([state_20439]
                                         (let [old_frame__10109__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__10110__auto__ (try
                                                                          (try
                                                                            (do
                                                                              (clojure.lang.Var/resetThreadBindingFrame
                                                                                (clojure.core.async.impl.ioc-macros/aget-object
                                                                                  state_20439
                                                                                  3))
                                                                              (loop 
                                                                                []
                                                                                (let 
                                                                                  [result__10111__auto__
                                                                                   (let 
                                                                                     [G__20450
                                                                                      (int
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_20439
                                                                                          1))]
                                                                                     (case
                                                                                       G__20450
                                                                                       2
                                                                                       (let 
                                                                                         [inst_20419
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            9)]
                                                                                         (if
                                                                                           inst_20419
                                                                                           (let 
                                                                                             [statearr_20453
                                                                                              state_20439]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20453
                                                                                               1
                                                                                               4))
                                                                                           (let 
                                                                                             [statearr_20454
                                                                                              state_20439]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20454
                                                                                               1
                                                                                               5)))
                                                                                         :recur)
                                                                                       5
                                                                                       (let 
                                                                                         [inst_20415
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            6)
                                                                                          inst_20416
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            7)
                                                                                          inst_20417
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            8)
                                                                                          inst_20419
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            9)
                                                                                          ch
                                                                                          inst_20415
                                                                                          coll
                                                                                          inst_20416
                                                                                          close?
                                                                                          inst_20417
                                                                                          vs
                                                                                          inst_20419]
                                                                                         (when
                                                                                           close?
                                                                                           (a/close!
                                                                                             ch))
                                                                                         (let 
                                                                                           [inst_20433
                                                                                            true]
                                                                                           (let 
                                                                                             [statearr_20455
                                                                                              state_20439]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20455
                                                                                               2
                                                                                               inst_20433)
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20455
                                                                                               1
                                                                                               6))
                                                                                           :recur))
                                                                                       4
                                                                                       (let 
                                                                                         [inst_20419
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            9)
                                                                                          inst_20415
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            6)
                                                                                          inst_20422
                                                                                          (first
                                                                                            inst_20419)]
                                                                                         (clojure.core.async.impl.ioc-macros/put!
                                                                                           state_20439
                                                                                           7
                                                                                           inst_20415
                                                                                           inst_20422))
                                                                                       6
                                                                                       (let 
                                                                                         [inst_20435
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            2)]
                                                                                         (let 
                                                                                           [statearr_20456
                                                                                            state_20439]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20456
                                                                                             2
                                                                                             inst_20435)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20456
                                                                                             1
                                                                                             3))
                                                                                         :recur)
                                                                                       9
                                                                                       (let 
                                                                                         [inst_20415
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            6)
                                                                                          inst_20416
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            7)
                                                                                          inst_20417
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            8)
                                                                                          inst_20419
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            9)
                                                                                          ch
                                                                                          inst_20415
                                                                                          coll
                                                                                          inst_20416
                                                                                          close?
                                                                                          inst_20417
                                                                                          vs
                                                                                          inst_20419
                                                                                          inst_20429
                                                                                          (when
                                                                                            close?
                                                                                            (a/close!
                                                                                              ch))]
                                                                                         (let 
                                                                                           [statearr_20461
                                                                                            state_20439]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20461
                                                                                             2
                                                                                             inst_20429)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20461
                                                                                             1
                                                                                             10))
                                                                                         :recur)
                                                                                       8
                                                                                       (let 
                                                                                         [inst_20419
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            9)
                                                                                          inst_20426
                                                                                          (next
                                                                                            inst_20419)
                                                                                          inst_20419
                                                                                          inst_20426
                                                                                          state_20439
                                                                                          (let 
                                                                                            [statearr_20459
                                                                                             state_20439]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_20459
                                                                                              9
                                                                                              inst_20419)
                                                                                            statearr_20459)]
                                                                                         (let 
                                                                                           [statearr_20460
                                                                                            state_20439]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20460
                                                                                             2
                                                                                             nil)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20460
                                                                                             1
                                                                                             2))
                                                                                         :recur)
                                                                                       1
                                                                                       (let 
                                                                                         [inst_20415
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            6)
                                                                                          inst_20416
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            7)
                                                                                          inst_20417
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            8)
                                                                                          inst_20412
                                                                                          (^clojure.lang.IFn G__20407)
                                                                                          ch
                                                                                          inst_20412
                                                                                          inst_20413
                                                                                          (^clojure.lang.IFn G__20408)
                                                                                          ch
                                                                                          inst_20412
                                                                                          coll
                                                                                          inst_20413
                                                                                          inst_20414
                                                                                          (^clojure.lang.IFn G__20409)
                                                                                          inst_20415
                                                                                          inst_20412
                                                                                          inst_20416
                                                                                          inst_20413
                                                                                          inst_20417
                                                                                          inst_20414
                                                                                          ch
                                                                                          inst_20415
                                                                                          coll
                                                                                          inst_20416
                                                                                          close?
                                                                                          inst_20417
                                                                                          inst_20418
                                                                                          (seq
                                                                                            coll)
                                                                                          inst_20419
                                                                                          inst_20418
                                                                                          state_20439
                                                                                          (let 
                                                                                            [statearr_20451
                                                                                             state_20439]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_20451
                                                                                              6
                                                                                              inst_20415)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_20451
                                                                                              7
                                                                                              inst_20416)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_20451
                                                                                              8
                                                                                              inst_20417)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_20451
                                                                                              9
                                                                                              inst_20419)
                                                                                            statearr_20451)]
                                                                                         (let 
                                                                                           [statearr_20452
                                                                                            state_20439]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20452
                                                                                             2
                                                                                             nil)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20452
                                                                                             1
                                                                                             2))
                                                                                         :recur)
                                                                                       10
                                                                                       (let 
                                                                                         [inst_20431
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            2)]
                                                                                         (let 
                                                                                           [statearr_20462
                                                                                            state_20439]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20462
                                                                                             2
                                                                                             inst_20431)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_20462
                                                                                             1
                                                                                             6))
                                                                                         :recur)
                                                                                       7
                                                                                       (let 
                                                                                         [inst_20424
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            2)]
                                                                                         (if
                                                                                           inst_20424
                                                                                           (let 
                                                                                             [statearr_20457
                                                                                              state_20439]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20457
                                                                                               1
                                                                                               8))
                                                                                           (let 
                                                                                             [statearr_20458
                                                                                              state_20439]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_20458
                                                                                               1
                                                                                               9)))
                                                                                         :recur)
                                                                                       3
                                                                                       (let 
                                                                                         [inst_20437
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_20439
                                                                                            2)]
                                                                                         (clojure.core.async.impl.ioc-macros/return-chan
                                                                                           state_20439
                                                                                           inst_20437))))]
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
                                                                                  [statearr_20463
                                                                                   state_20439]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_20463
                                                                                    2
                                                                                    ex__10112__auto__))
                                                                                (if
                                                                                  (seq
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_20439
                                                                                      4))
                                                                                  (let 
                                                                                    [statearr_20464
                                                                                     state_20439]
                                                                                    (clojure.core.async.impl.ioc-macros/aset-object
                                                                                      statearr_20464
                                                                                      1
                                                                                      (first
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_20439
                                                                                          4))))
                                                                                  (throw
                                                                                    ^java.lang.Throwable ex__10112__auto__))
                                                                                :recur)))
                                                                          (finally
                                                                            (do
                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                state_20439
                                                                                3
                                                                                (clojure.lang.Var/getThreadBindingFrame))
                                                                              (clojure.lang.Var/resetThreadBindingFrame
                                                                                old_frame__10109__auto__))))]
                                           (if (identical? ret_value__10110__auto__ :recur)
                                             (recur state_20439)
                                             ret_value__10110__auto__))))
                    state__10366__auto__ (let [statearr_20470 (^clojure.lang.IFn f__10365__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20470
                                             5
                                             c__10363__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20470
                                             3
                                             captured_bindings__10364__auto__)
                                           statearr_20470)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__10366__auto__)))))
        c__10363__auto__))
    ([ch coll] (put-all! ch coll true)))
  (reset-meta!
    #'put-all!
    (assoc
      {:arglists (clojure.core/list ['ch 'coll] ['ch 'coll 'close?]), :column (int 1)}
      :name
      'put-all!
      :ns
      *ns*))
  (defn channel-closed-error
    ([x]
      (when-not x
        {:error "Channel closed",
         :cognitect.anomalies/message "Channel closed",
         :cognitect.anomalies/category :cognitect.anomalies/fault})))
  (reset-meta!
    #'channel-closed-error
    (assoc
      {:arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'channel-closed-error
      :ns
      *ns*))
  (def <!x
   (fn _LT__BANG_x
     ([&form &env ch]
       (seq
         (concat
           (clojure.core/list 'clojure.core/let)
           (clojure.core/list
             (apply
               vector
               (seq
                 (concat
                   (clojure.core/list 'v__20478__auto__)
                   (clojure.core/list
                     (seq
                       (concat
                         (clojure.core/list 'clojure.core.async/<!)
                         (clojure.core/list ch))))))))
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'clojure.core/or)
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'datomic.core2.async/channel-closed-error)
                       (clojure.core/list 'v__20478__auto__))))
                 (clojure.core/list 'v__20478__auto__)))))))))
  (reset-meta!
    #'<!x
    (assoc {:arglists (clojure.core/list ['ch]), :column (int 1)} :name '<!x :ns *ns*))
  (.setMacro #'<!x)
  (defn <!!x ([ch] (let [v (a/<!! ch)] (or (channel-closed-error v) v))))
  (reset-meta!
    #'<!!x
    (assoc {:arglists (clojure.core/list ['ch]), :column (int 1)} :name '<!!x :ns *ns*)))