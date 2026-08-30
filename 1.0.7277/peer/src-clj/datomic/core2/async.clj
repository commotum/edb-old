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
            vec__19470 (a/alts!! [ch to])
            v (nth vec__19470 (int 0) nil)
            port (nth vec__19470 (int 1) nil)]
        (if (= port ch) v timeout_val)))
    ([ch timeout_ms]
      (aderef
        ch
        timeout_ms
        {:cognitect.anomalies/category :cognitect.anomalies/interrupted, :timeout-ms timeout_ms}))
    ([ch] (aderef ch 1000)))
  (defn aderef-n
    ([n ch timeout_ms timeout_val]
      (let [to (a/timeout (long ^java.lang.Number timeout_ms))]
        (loop [results []]
          (if (= (long (count results)) n)
            results
            (let [vec__19474 (a/alts!! [ch to])
                  v (nth vec__19474 (int 0) nil)
                  port (nth vec__19474 (int 1) nil)]
              (if (= port ch) (recur (conj results v)) (conj results timeout_val)))))))
    ([n ch timeout_ms]
      (aderef-n
        n
        ch
        timeout_ms
        {:cognitect.anomalies/category :cognitect.anomalies/interrupted, :timeout-ms timeout_ms}))
    ([n ch] (aderef-n n ch 1000)))
  (defn retry
    ([& p__19478]
      (let [map__19479 p__19478
            map__19479 (if (seq? map__19479)
                         (if (next map__19479)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19479))
                           (if (seq map__19479) (first map__19479) {}))
                         map__19479)
            f (get map__19479 :f)
            pred (get map__19479 :pred)
            backoff (get map__19479 :backoff)
            ch (get map__19479 :ch (a/chan 1))
            fail (get map__19479 :fail identity)]
        (let [c__10230__auto__ (a/chan 1)
              captured_bindings__10231__auto__ (clojure.lang.Var/getThreadBindingFrame)]
          (clojure.core.async.impl.dispatch/run
            (fn fn__19535
              ([]
                (let [G__19480 (fn G__19480 ([] p__19478))
                      G__19481 (fn G__19481 ([] map__19479))
                      G__19482 (fn G__19482 ([] f))
                      G__19483 (fn G__19483 ([] pred))
                      G__19484 (fn G__19484 ([] backoff))
                      G__19485 (fn G__19485 ([] ch))
                      G__19486 (fn G__19486 ([] fail))
                      f__10232__auto__ (fn state_machine__9975__auto__
                                         ([]
                                           (let [statearr_19551 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                  (int 17))]
                                             (clojure.core.async.impl.ioc-macros/aset-object
                                               statearr_19551
                                               0
                                               state_machine__9975__auto__)
                                             (clojure.core.async.impl.ioc-macros/aset-object
                                               statearr_19551
                                               1
                                               1)
                                             statearr_19551))
                                         ([state_19534]
                                           (let [old_frame__9976__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                                 ret_value__9977__auto__ (try
                                                                           (do
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               (clojure.core.async.impl.ioc-macros/aget-object
                                                                                 state_19534
                                                                                 3))
                                                                             (loop 
                                                                               []
                                                                               (let 
                                                                                 [result__9978__auto__
                                                                                  (let 
                                                                                    [G__19553
                                                                                     (int
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19534
                                                                                         1))]
                                                                                    (case
                                                                                      G__19553
                                                                                      10
                                                                                      (let 
                                                                                        [inst_19515
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           15)
                                                                                         inst_19496
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           6)
                                                                                         inst_19499
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           9)
                                                                                         inst_19503
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           13)
                                                                                         inst_19501
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           11)
                                                                                         inst_19502
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           12)
                                                                                         inst_19508
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           14)
                                                                                         inst_19497
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           7)
                                                                                         inst_19500
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           10)
                                                                                         inst_19498
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           8)
                                                                                         temp__5802__auto__
                                                                                         inst_19515
                                                                                         p__19478
                                                                                         inst_19496
                                                                                         pred
                                                                                         inst_19499
                                                                                         n
                                                                                         inst_19503
                                                                                         ch
                                                                                         inst_19501
                                                                                         fail
                                                                                         inst_19502
                                                                                         result
                                                                                         inst_19508
                                                                                         map__19479
                                                                                         inst_19497
                                                                                         backoff
                                                                                         inst_19500
                                                                                         f
                                                                                         inst_19498
                                                                                         inst_19524
                                                                                         (^clojure.lang.IFn fail
                                                                                           result)]
                                                                                        (clojure.core.async.impl.ioc-macros/put!
                                                                                          state_19534
                                                                                          13
                                                                                          inst_19501
                                                                                          inst_19524))
                                                                                      9
                                                                                      (let 
                                                                                        [inst_19515
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           15)
                                                                                         inst_19517
                                                                                         inst_19515
                                                                                         inst_19518
                                                                                         (a/timeout
                                                                                           (long
                                                                                             ^java.lang.Number inst_19517))]
                                                                                        (clojure.core.async.impl.ioc-macros/take!
                                                                                          state_19534
                                                                                          12
                                                                                          inst_19518))
                                                                                      11
                                                                                      (let 
                                                                                        [inst_19528
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           2)]
                                                                                        (let 
                                                                                          [statearr_19564
                                                                                           state_19534]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19564
                                                                                            2
                                                                                            inst_19528)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19564
                                                                                            1
                                                                                            7))
                                                                                        :recur)
                                                                                      7
                                                                                      (let 
                                                                                        [inst_19530
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           2)]
                                                                                        (let 
                                                                                          [statearr_19562
                                                                                           state_19534]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19562
                                                                                            2
                                                                                            inst_19530)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19562
                                                                                            1
                                                                                            3))
                                                                                        :recur)
                                                                                      13
                                                                                      (let 
                                                                                        [inst_19526
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           2)]
                                                                                        (let 
                                                                                          [statearr_19567
                                                                                           state_19534]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19567
                                                                                            2
                                                                                            inst_19526)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19567
                                                                                            1
                                                                                            11))
                                                                                        :recur)
                                                                                      6
                                                                                      (let 
                                                                                        [inst_19500
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           10)
                                                                                         inst_19503
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           13)
                                                                                         inst_19508
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           14)
                                                                                         inst_19515
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           15)
                                                                                         inst_19514
                                                                                         (^clojure.lang.IFn inst_19500
                                                                                           inst_19503
                                                                                           inst_19508)
                                                                                         inst_19515
                                                                                         inst_19514
                                                                                         state_19534
                                                                                         (let 
                                                                                           [statearr_19559
                                                                                            state_19534]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19559
                                                                                             15
                                                                                             inst_19515)
                                                                                           statearr_19559)]
                                                                                        (if
                                                                                          inst_19515
                                                                                          (let 
                                                                                            [statearr_19560
                                                                                             state_19534]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_19560
                                                                                              1
                                                                                              9))
                                                                                          (let 
                                                                                            [statearr_19561
                                                                                             state_19534]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_19561
                                                                                              1
                                                                                              10)))
                                                                                        :recur)
                                                                                      3
                                                                                      (let 
                                                                                        [inst_19532
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           2)]
                                                                                        (clojure.core.async.impl.ioc-macros/return-chan
                                                                                          state_19534
                                                                                          inst_19532))
                                                                                      2
                                                                                      (let 
                                                                                        [inst_19498
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           8)
                                                                                         inst_19505
                                                                                         (^clojure.lang.IFn inst_19498)]
                                                                                        (clojure.core.async.impl.ioc-macros/take!
                                                                                          state_19534
                                                                                          4
                                                                                          inst_19505))
                                                                                      4
                                                                                      (let 
                                                                                        [inst_19499
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           9)
                                                                                         inst_19508
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           14)
                                                                                         inst_19507
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           2)
                                                                                         inst_19508
                                                                                         inst_19507
                                                                                         inst_19509
                                                                                         (^clojure.lang.IFn inst_19499
                                                                                           inst_19508)
                                                                                         state_19534
                                                                                         (let 
                                                                                           [statearr_19556
                                                                                            state_19534]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19556
                                                                                             14
                                                                                             inst_19508)
                                                                                           statearr_19556)]
                                                                                        (if
                                                                                          inst_19509
                                                                                          (let 
                                                                                            [statearr_19557
                                                                                             state_19534]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_19557
                                                                                              1
                                                                                              5))
                                                                                          (let 
                                                                                            [statearr_19558
                                                                                             state_19534]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_19558
                                                                                              1
                                                                                              6)))
                                                                                        :recur)
                                                                                      1
                                                                                      (let 
                                                                                        [inst_19489
                                                                                         (^clojure.lang.IFn G__19480)
                                                                                         p__19478
                                                                                         inst_19489
                                                                                         inst_19490
                                                                                         (^clojure.lang.IFn G__19481)
                                                                                         p__19478
                                                                                         inst_19489
                                                                                         map__19479
                                                                                         inst_19490
                                                                                         inst_19491
                                                                                         (^clojure.lang.IFn G__19482)
                                                                                         p__19478
                                                                                         inst_19489
                                                                                         map__19479
                                                                                         inst_19490
                                                                                         f
                                                                                         inst_19491
                                                                                         inst_19492
                                                                                         (^clojure.lang.IFn G__19483)
                                                                                         p__19478
                                                                                         inst_19489
                                                                                         pred
                                                                                         inst_19492
                                                                                         map__19479
                                                                                         inst_19490
                                                                                         f
                                                                                         inst_19491
                                                                                         inst_19493
                                                                                         (^clojure.lang.IFn G__19484)
                                                                                         p__19478
                                                                                         inst_19489
                                                                                         pred
                                                                                         inst_19492
                                                                                         map__19479
                                                                                         inst_19490
                                                                                         backoff
                                                                                         inst_19493
                                                                                         f
                                                                                         inst_19491
                                                                                         inst_19494
                                                                                         (^clojure.lang.IFn G__19485)
                                                                                         p__19478
                                                                                         inst_19489
                                                                                         pred
                                                                                         inst_19492
                                                                                         ch
                                                                                         inst_19494
                                                                                         map__19479
                                                                                         inst_19490
                                                                                         backoff
                                                                                         inst_19493
                                                                                         f
                                                                                         inst_19491
                                                                                         inst_19495
                                                                                         (^clojure.lang.IFn G__19486)
                                                                                         inst_19496
                                                                                         inst_19489
                                                                                         inst_19497
                                                                                         inst_19490
                                                                                         inst_19498
                                                                                         inst_19491
                                                                                         inst_19499
                                                                                         inst_19492
                                                                                         inst_19500
                                                                                         inst_19493
                                                                                         inst_19501
                                                                                         inst_19494
                                                                                         inst_19502
                                                                                         inst_19495
                                                                                         inst_19503
                                                                                         1
                                                                                         state_19534
                                                                                         (let 
                                                                                           [statearr_19554
                                                                                            state_19534]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19554
                                                                                             6
                                                                                             inst_19496)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19554
                                                                                             7
                                                                                             inst_19497)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19554
                                                                                             8
                                                                                             inst_19498)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19554
                                                                                             9
                                                                                             inst_19499)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19554
                                                                                             10
                                                                                             inst_19500)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19554
                                                                                             11
                                                                                             inst_19501)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19554
                                                                                             12
                                                                                             inst_19502)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19554
                                                                                             13
                                                                                             (long
                                                                                               inst_19503))
                                                                                           statearr_19554)]
                                                                                        (let 
                                                                                          [statearr_19555
                                                                                           state_19534]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19555
                                                                                            2
                                                                                            nil)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19555
                                                                                            1
                                                                                            2))
                                                                                        :recur)
                                                                                      12
                                                                                      (let 
                                                                                        [inst_19503
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           13)
                                                                                         inst_19520
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           2)
                                                                                         inst_19521
                                                                                         (inc
                                                                                           inst_19503)
                                                                                         inst_19503
                                                                                         inst_19521
                                                                                         state_19534
                                                                                         (let 
                                                                                           [statearr_19565
                                                                                            state_19534]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19565
                                                                                             16
                                                                                             inst_19520)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_19565
                                                                                             13
                                                                                             inst_19503)
                                                                                           statearr_19565)]
                                                                                        (let 
                                                                                          [statearr_19566
                                                                                           state_19534]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19566
                                                                                            2
                                                                                            nil)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19566
                                                                                            1
                                                                                            2))
                                                                                        :recur)
                                                                                      5
                                                                                      (let 
                                                                                        [inst_19501
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           11)
                                                                                         inst_19508
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           14)]
                                                                                        (clojure.core.async.impl.ioc-macros/put!
                                                                                          state_19534
                                                                                          8
                                                                                          inst_19501
                                                                                          inst_19508))
                                                                                      8
                                                                                      (let 
                                                                                        [inst_19512
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_19534
                                                                                           2)]
                                                                                        (let 
                                                                                          [statearr_19563
                                                                                           state_19534]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19563
                                                                                            2
                                                                                            inst_19512)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19563
                                                                                            1
                                                                                            7))
                                                                                        :recur)))]
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
                                                                                 [statearr_19568
                                                                                  state_19534]
                                                                                 (clojure.core.async.impl.ioc-macros/aset-object
                                                                                   statearr_19568
                                                                                   2
                                                                                   ex__9979__auto__))
                                                                               (if
                                                                                 (seq
                                                                                   (clojure.core.async.impl.ioc-macros/aget-object
                                                                                     state_19534
                                                                                     4))
                                                                                 (let 
                                                                                   [statearr_19569
                                                                                    state_19534]
                                                                                   (clojure.core.async.impl.ioc-macros/aset-object
                                                                                     statearr_19569
                                                                                     1
                                                                                     (first
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19534
                                                                                         4))))
                                                                                 (throw
                                                                                   ^java.lang.Throwable ex__9979__auto__))
                                                                               :recur))
                                                                           (finally
                                                                             (do
                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                 state_19534
                                                                                 3
                                                                                 (clojure.lang.Var/getThreadBindingFrame))
                                                                               (clojure.lang.Var/resetThreadBindingFrame
                                                                                 old_frame__9976__auto__))))]
                                             (if (identical? ret_value__9977__auto__ :recur)
                                               (recur state_19534)
                                               ret_value__9977__auto__))))
                      state__10233__auto__ (let [statearr_19576 (^clojure.lang.IFn f__10232__auto__)]
                                             (clojure.core.async.impl.ioc-macros/aset-object
                                               statearr_19576
                                               5
                                               c__10230__auto__)
                                             (clojure.core.async.impl.ioc-macros/aset-object
                                               statearr_19576
                                               3
                                               captured_bindings__10231__auto__)
                                             statearr_19576)]
                  (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                    state__10233__auto__))))))
        ch)))
  (defn put-all!
    ([ch coll close?]
      (let [c__10230__auto__ (a/chan 1)
            captured_bindings__10231__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__19616
            ([]
              (let [G__19583 (fn G__19583 ([] ch))
                    G__19584 (fn G__19584 ([] coll))
                    G__19585 (fn G__19585 ([] close?))
                    f__10232__auto__ (fn state_machine__9975__auto__
                                       ([]
                                         (let [statearr_19624 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 10))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_19624
                                             0
                                             state_machine__9975__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_19624
                                             1
                                             1)
                                           statearr_19624))
                                       ([state_19615]
                                         (let [old_frame__9976__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__9977__auto__ (try
                                                                         (do
                                                                           (clojure.lang.Var/resetThreadBindingFrame
                                                                             (clojure.core.async.impl.ioc-macros/aget-object
                                                                               state_19615
                                                                               3))
                                                                           (loop 
                                                                             []
                                                                             (let 
                                                                               [result__9978__auto__
                                                                                (let 
                                                                                  [G__19626
                                                                                   (int
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_19615
                                                                                       1))]
                                                                                  (case
                                                                                    G__19626
                                                                                    9
                                                                                    (let 
                                                                                      [inst_19591
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         6)
                                                                                       inst_19592
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         7)
                                                                                       inst_19593
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         8)
                                                                                       inst_19595
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         9)
                                                                                       ch
                                                                                       inst_19591
                                                                                       coll
                                                                                       inst_19592
                                                                                       close?
                                                                                       inst_19593
                                                                                       vs
                                                                                       inst_19595
                                                                                       inst_19605
                                                                                       (when
                                                                                         close?
                                                                                         (a/close!
                                                                                           ch))]
                                                                                      (let 
                                                                                        [statearr_19637
                                                                                         state_19615]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19637
                                                                                          2
                                                                                          inst_19605)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19637
                                                                                          1
                                                                                          10))
                                                                                      :recur)
                                                                                    2
                                                                                    (let 
                                                                                      [inst_19595
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         9)]
                                                                                      (if
                                                                                        inst_19595
                                                                                        (let 
                                                                                          [statearr_19629
                                                                                           state_19615]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19629
                                                                                            1
                                                                                            4))
                                                                                        (let 
                                                                                          [statearr_19630
                                                                                           state_19615]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19630
                                                                                            1
                                                                                            5)))
                                                                                      :recur)
                                                                                    5
                                                                                    (let 
                                                                                      [inst_19591
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         6)
                                                                                       inst_19592
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         7)
                                                                                       inst_19593
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         8)
                                                                                       inst_19595
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         9)
                                                                                       ch
                                                                                       inst_19591
                                                                                       coll
                                                                                       inst_19592
                                                                                       close?
                                                                                       inst_19593
                                                                                       vs
                                                                                       inst_19595]
                                                                                      (when
                                                                                        close?
                                                                                        (a/close!
                                                                                          ch))
                                                                                      (let 
                                                                                        [inst_19609
                                                                                         true]
                                                                                        (let 
                                                                                          [statearr_19631
                                                                                           state_19615]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19631
                                                                                            2
                                                                                            inst_19609)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19631
                                                                                            1
                                                                                            6))
                                                                                        :recur))
                                                                                    7
                                                                                    (let 
                                                                                      [inst_19600
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         2)]
                                                                                      (if
                                                                                        inst_19600
                                                                                        (let 
                                                                                          [statearr_19633
                                                                                           state_19615]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19633
                                                                                            1
                                                                                            8))
                                                                                        (let 
                                                                                          [statearr_19634
                                                                                           state_19615]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_19634
                                                                                            1
                                                                                            9)))
                                                                                      :recur)
                                                                                    3
                                                                                    (let 
                                                                                      [inst_19613
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         2)]
                                                                                      (clojure.core.async.impl.ioc-macros/return-chan
                                                                                        state_19615
                                                                                        inst_19613))
                                                                                    8
                                                                                    (let 
                                                                                      [inst_19595
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         9)
                                                                                       inst_19602
                                                                                       (next
                                                                                         inst_19595)
                                                                                       inst_19595
                                                                                       inst_19602
                                                                                       state_19615
                                                                                       (let 
                                                                                         [statearr_19635
                                                                                          state_19615]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_19635
                                                                                           9
                                                                                           inst_19595)
                                                                                         statearr_19635)]
                                                                                      (let 
                                                                                        [statearr_19636
                                                                                         state_19615]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19636
                                                                                          2
                                                                                          nil)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19636
                                                                                          1
                                                                                          2))
                                                                                      :recur)
                                                                                    6
                                                                                    (let 
                                                                                      [inst_19611
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_19632
                                                                                         state_19615]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19632
                                                                                          2
                                                                                          inst_19611)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19632
                                                                                          1
                                                                                          3))
                                                                                      :recur)
                                                                                    1
                                                                                    (let 
                                                                                      [inst_19591
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         6)
                                                                                       inst_19592
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         7)
                                                                                       inst_19593
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         8)
                                                                                       inst_19588
                                                                                       (^clojure.lang.IFn G__19583)
                                                                                       ch
                                                                                       inst_19588
                                                                                       inst_19589
                                                                                       (^clojure.lang.IFn G__19584)
                                                                                       ch
                                                                                       inst_19588
                                                                                       coll
                                                                                       inst_19589
                                                                                       inst_19590
                                                                                       (^clojure.lang.IFn G__19585)
                                                                                       inst_19591
                                                                                       inst_19588
                                                                                       inst_19592
                                                                                       inst_19589
                                                                                       inst_19593
                                                                                       inst_19590
                                                                                       ch
                                                                                       inst_19591
                                                                                       coll
                                                                                       inst_19592
                                                                                       close?
                                                                                       inst_19593
                                                                                       inst_19594
                                                                                       (seq coll)
                                                                                       inst_19595
                                                                                       inst_19594
                                                                                       state_19615
                                                                                       (let 
                                                                                         [statearr_19627
                                                                                          state_19615]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_19627
                                                                                           6
                                                                                           inst_19591)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_19627
                                                                                           7
                                                                                           inst_19592)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_19627
                                                                                           8
                                                                                           inst_19593)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_19627
                                                                                           9
                                                                                           inst_19595)
                                                                                         statearr_19627)]
                                                                                      (let 
                                                                                        [statearr_19628
                                                                                         state_19615]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19628
                                                                                          2
                                                                                          nil)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19628
                                                                                          1
                                                                                          2))
                                                                                      :recur)
                                                                                    10
                                                                                    (let 
                                                                                      [inst_19607
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_19638
                                                                                         state_19615]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19638
                                                                                          2
                                                                                          inst_19607)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_19638
                                                                                          1
                                                                                          6))
                                                                                      :recur)
                                                                                    4
                                                                                    (let 
                                                                                      [inst_19595
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         9)
                                                                                       inst_19591
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_19615
                                                                                         6)
                                                                                       inst_19598
                                                                                       (first
                                                                                         inst_19595)]
                                                                                      (clojure.core.async.impl.ioc-macros/put!
                                                                                        state_19615
                                                                                        7
                                                                                        inst_19591
                                                                                        inst_19598))))]
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
                                                                               [statearr_19639
                                                                                state_19615]
                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                 statearr_19639
                                                                                 2
                                                                                 ex__9979__auto__))
                                                                             (if
                                                                               (seq
                                                                                 (clojure.core.async.impl.ioc-macros/aget-object
                                                                                   state_19615
                                                                                   4))
                                                                               (let 
                                                                                 [statearr_19640
                                                                                  state_19615]
                                                                                 (clojure.core.async.impl.ioc-macros/aset-object
                                                                                   statearr_19640
                                                                                   1
                                                                                   (first
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_19615
                                                                                       4))))
                                                                               (throw
                                                                                 ^java.lang.Throwable ex__9979__auto__))
                                                                             :recur))
                                                                         (finally
                                                                           (do
                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                               state_19615
                                                                               3
                                                                               (clojure.lang.Var/getThreadBindingFrame))
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               old_frame__9976__auto__))))]
                                           (if (identical? ret_value__9977__auto__ :recur)
                                             (recur state_19615)
                                             ret_value__9977__auto__))))
                    state__10233__auto__ (let [statearr_19646 (^clojure.lang.IFn f__10232__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_19646
                                             5
                                             c__10230__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_19646
                                             3
                                             captured_bindings__10231__auto__)
                                           statearr_19646)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__10233__auto__)))))
        c__10230__auto__))
    ([ch coll] (put-all! ch coll true)))
  (defn channel-closed-error
    ([x]
      (when-not x
        {:error "Channel closed",
         :cognitect.anomalies/message "Channel closed",
         :cognitect.anomalies/category :cognitect.anomalies/fault})))
  (defn <!x
    ([&form &env ch]
      (seq
        (concat
          (clojure.core/list 'clojure.core/let)
          (clojure.core/list
            (apply
              vector
              (seq
                (concat
                  (clojure.core/list 'v__19654__auto__)
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
                      (clojure.core/list 'v__19654__auto__))))
                (clojure.core/list 'v__19654__auto__))))))))
  (.setMacro #'<!x)
  (defn <!!x ([ch] (let [v (a/<!! ch)] (or (channel-closed-error v) v)))))