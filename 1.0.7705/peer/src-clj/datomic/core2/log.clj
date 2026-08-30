(do
  (clojure.core/in-ns 'datomic.core2.log)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.core2.log)
    {:doc
     "API for an append-only log. The log can be used for e.g. Datomic \ntransactions or for a \"durable atom\" that maintains history.\n\nItems in the log have a required header and an optional body. The\nheader is a Clojure map with keys\n\nt          required long, must be montonically ascending\nnext-t     optional long, value for the next t appended\ntombstone  optional string, explains why log can no longer be written\n\nExactly one of next-t/tombstone must be present.\n\nCallers and implementers are free to extend the header map with\nnamespaced keys.\n\nThe body is an optional ByteBuffer.\n\nCallers and implementers share responsibility for correct append\nsemantics. Implementers must fail writes to an existing t with a \nconflict anomaly. Callers must use next-t as the t for a successor\nappend, and can never append after an item with a tombstone.\n"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :as 'a :refer (clojure.core/list 'go '<! '>!)]
        ['datomic.core2.anomalies :refer (clojure.core/list 'anom)]
        ['datomic.core2.log.spi :as 'spi])))
  (when-not (.equals 'datomic.core2.log 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.log))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :as 'a :refer (clojure.core/list 'go '<! '>!)]
          ['datomic.core2.anomalies :refer (clojure.core/list 'anom)]
          ['datomic.core2.log.spi :as 'spi]))))
  (defn append
    ([log p__21417 body]
      (let [map__21418 p__21417
            map__21418 (if (seq? map__21418)
                         (if (next map__21418)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21418))
                           (if (seq map__21418) (first map__21418) {}))
                         map__21418)
            header map__21418
            t (get map__21418 :t)
            next_t (get map__21418 :next-t)]
        (when-not t (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 't)))))
        (when-not next_t
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'next-t)))))
        (spi/-append log header body)))
    ([log header] (append log header nil)))
  (reset-meta!
    #'append
    (assoc
      {:arglists (clojure.core/list ['log 'header] ['log {:keys ['t 'next-t], :as 'header} 'body]),
       :column (int 1)}
      :name
      'append
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.core2.log" "delete") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.core2.log" "delete") spi/-delete)
  (defn scan
    ([log opts]
      (let [map__21420 (spi/normalize-scan-opts opts)
            map__21420 (if (seq? map__21420)
                         (if (next map__21420)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21420))
                           (if (seq map__21420) (first map__21420) {}))
                         map__21420)
            nopts map__21420
            ch (get map__21420 :ch)]
        (spi/-scan log nopts)
        ch)))
  (reset-meta!
    #'scan
    (assoc {:arglists (clojure.core/list ['log 'opts]), :column (int 1)} :name 'scan :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.core2.log" "item-header") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.core2.log" "item-header") spi/-item-header)
  (.setMeta (clojure.lang.RT/var "datomic.core2.log" "item-body") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.core2.log" "item-body") spi/-item-body)
  (defn ensure-tombstone
    ([log tombstone]
      (let [c__10363__auto__ (a/chan 1)
            captured_bindings__10364__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__21453
            ([]
              (let [G__21422 (fn G__21422 ([] log))
                    G__21423 (fn G__21423 ([] tombstone))
                    f__10365__auto__ (fn state_machine__10108__auto__
                                       ([]
                                         (let [statearr_21459 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 11))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21459
                                             0
                                             state_machine__10108__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21459
                                             1
                                             1)
                                           statearr_21459))
                                       ([state_21452]
                                         (let [old_frame__10109__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__10110__auto__ (try
                                                                          (try
                                                                            (do
                                                                              (clojure.lang.Var/resetThreadBindingFrame
                                                                                (clojure.core.async.impl.ioc-macros/aget-object
                                                                                  state_21452
                                                                                  3))
                                                                              (loop 
                                                                                []
                                                                                (let 
                                                                                  [result__10111__auto__
                                                                                   (let 
                                                                                     [G__21461
                                                                                      (int
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_21452
                                                                                          1))]
                                                                                     (case
                                                                                       G__21461
                                                                                       3
                                                                                       (let 
                                                                                         [inst_21434
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            9)]
                                                                                         (let 
                                                                                           [statearr_21466
                                                                                            state_21452]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_21466
                                                                                             2
                                                                                             inst_21434)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_21466
                                                                                             1
                                                                                             5))
                                                                                         :recur)
                                                                                       9
                                                                                       (let 
                                                                                         [inst_21446
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            2)]
                                                                                         (let 
                                                                                           [statearr_21472
                                                                                            state_21452]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_21472
                                                                                             2
                                                                                             inst_21446)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_21472
                                                                                             1
                                                                                             8))
                                                                                         :recur)
                                                                                       4
                                                                                       (let 
                                                                                         [inst_21427
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            6)
                                                                                          inst_21428
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            7)
                                                                                          inst_21432
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            8)
                                                                                          inst_21434
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            9)
                                                                                          inst_21438
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            10)
                                                                                          log
                                                                                          inst_21427
                                                                                          tombstone
                                                                                          inst_21428
                                                                                          item
                                                                                          inst_21432
                                                                                          or__5602__auto__
                                                                                          inst_21434
                                                                                          inst_21437
                                                                                          (item-header
                                                                                            log
                                                                                            item)
                                                                                          inst_21438
                                                                                          inst_21437
                                                                                          log
                                                                                          inst_21427
                                                                                          tombstone
                                                                                          inst_21428
                                                                                          item
                                                                                          inst_21432
                                                                                          or__5602__auto__
                                                                                          inst_21434
                                                                                          header
                                                                                          inst_21438
                                                                                          inst_21439
                                                                                          (:tombstone
                                                                                            header)
                                                                                          state_21452
                                                                                          (let 
                                                                                            [statearr_21467
                                                                                             state_21452]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21467
                                                                                              10
                                                                                              inst_21438)
                                                                                            statearr_21467)]
                                                                                         (if
                                                                                           inst_21439
                                                                                           (let 
                                                                                             [statearr_21468
                                                                                              state_21452]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_21468
                                                                                               1
                                                                                               6))
                                                                                           (let 
                                                                                             [statearr_21469
                                                                                              state_21452]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_21469
                                                                                               1
                                                                                               7)))
                                                                                         :recur)
                                                                                       5
                                                                                       (let 
                                                                                         [inst_21450
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            2)]
                                                                                         (clojure.core.async.impl.ioc-macros/return-chan
                                                                                           state_21452
                                                                                           inst_21450))
                                                                                       8
                                                                                       (let 
                                                                                         [inst_21448
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            2)]
                                                                                         (let 
                                                                                           [statearr_21471
                                                                                            state_21452]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_21471
                                                                                             2
                                                                                             inst_21448)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_21471
                                                                                             1
                                                                                             5))
                                                                                         :recur)
                                                                                       6
                                                                                       (let 
                                                                                         [inst_21438
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            10)]
                                                                                         (let 
                                                                                           [statearr_21470
                                                                                            state_21452]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_21470
                                                                                             2
                                                                                             inst_21438)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_21470
                                                                                             1
                                                                                             8))
                                                                                         :recur)
                                                                                       2
                                                                                       (let 
                                                                                         [inst_21427
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            6)
                                                                                          inst_21428
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            7)
                                                                                          inst_21432
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            8)
                                                                                          inst_21434
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            9)
                                                                                          inst_21431
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            2)
                                                                                          inst_21432
                                                                                          inst_21431
                                                                                          log
                                                                                          inst_21427
                                                                                          tombstone
                                                                                          inst_21428
                                                                                          item
                                                                                          inst_21432
                                                                                          inst_21433
                                                                                          (datomic.core2.anomalies/anom
                                                                                            item)
                                                                                          inst_21434
                                                                                          inst_21433
                                                                                          state_21452
                                                                                          (let 
                                                                                            [statearr_21463
                                                                                             state_21452]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21463
                                                                                              8
                                                                                              inst_21432)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21463
                                                                                              9
                                                                                              inst_21434)
                                                                                            statearr_21463)]
                                                                                         (if
                                                                                           inst_21434
                                                                                           (let 
                                                                                             [statearr_21464
                                                                                              state_21452]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_21464
                                                                                               1
                                                                                               3))
                                                                                           (let 
                                                                                             [statearr_21465
                                                                                              state_21452]
                                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                                               statearr_21465
                                                                                               1
                                                                                               4)))
                                                                                         :recur)
                                                                                       1
                                                                                       (let 
                                                                                         [inst_21427
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            6)
                                                                                          inst_21428
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            7)
                                                                                          inst_21425
                                                                                          (^clojure.lang.IFn G__21422)
                                                                                          log
                                                                                          inst_21425
                                                                                          inst_21426
                                                                                          (^clojure.lang.IFn G__21423)
                                                                                          inst_21427
                                                                                          inst_21425
                                                                                          inst_21428
                                                                                          inst_21426
                                                                                          log
                                                                                          inst_21427
                                                                                          tombstone
                                                                                          inst_21428
                                                                                          inst_21429
                                                                                          (scan
                                                                                            log
                                                                                            {:direction
                                                                                             :backward,
                                                                                             :limit
                                                                                             1})
                                                                                          state_21452
                                                                                          (let 
                                                                                            [statearr_21462
                                                                                             state_21452]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21462
                                                                                              6
                                                                                              inst_21427)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_21462
                                                                                              7
                                                                                              inst_21428)
                                                                                            statearr_21462)]
                                                                                         (clojure.core.async.impl.ioc-macros/take!
                                                                                           state_21452
                                                                                           2
                                                                                           inst_21429))
                                                                                       7
                                                                                       (let 
                                                                                         [inst_21427
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            6)
                                                                                          inst_21428
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            7)
                                                                                          inst_21432
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            8)
                                                                                          inst_21434
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            9)
                                                                                          inst_21438
                                                                                          (clojure.core.async.impl.ioc-macros/aget-object
                                                                                            state_21452
                                                                                            10)
                                                                                          log
                                                                                          inst_21427
                                                                                          tombstone
                                                                                          inst_21428
                                                                                          item
                                                                                          inst_21432
                                                                                          or__5602__auto__
                                                                                          inst_21434
                                                                                          header
                                                                                          inst_21438
                                                                                          inst_21442
                                                                                          (assoc
                                                                                            (assoc
                                                                                              header
                                                                                              :t
                                                                                              (:next-t
                                                                                                header))
                                                                                            :tombstone
                                                                                            tombstone)
                                                                                          inst_21443
                                                                                          inst_21442
                                                                                          log
                                                                                          inst_21427
                                                                                          tombstone
                                                                                          inst_21428
                                                                                          item
                                                                                          inst_21432
                                                                                          or__5602__auto__
                                                                                          inst_21434
                                                                                          header
                                                                                          inst_21438
                                                                                          new_header
                                                                                          inst_21443
                                                                                          inst_21444
                                                                                          (append
                                                                                            log
                                                                                            new_header)]
                                                                                         (clojure.core.async.impl.ioc-macros/take!
                                                                                           state_21452
                                                                                           9
                                                                                           inst_21444))))]
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
                                                                                  [statearr_21473
                                                                                   state_21452]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_21473
                                                                                    2
                                                                                    ex__10112__auto__))
                                                                                (if
                                                                                  (seq
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_21452
                                                                                      4))
                                                                                  (let 
                                                                                    [statearr_21474
                                                                                     state_21452]
                                                                                    (clojure.core.async.impl.ioc-macros/aset-object
                                                                                      statearr_21474
                                                                                      1
                                                                                      (first
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_21452
                                                                                          4))))
                                                                                  (throw
                                                                                    ^java.lang.Throwable ex__10112__auto__))
                                                                                :recur)))
                                                                          (finally
                                                                            (do
                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                state_21452
                                                                                3
                                                                                (clojure.lang.Var/getThreadBindingFrame))
                                                                              (clojure.lang.Var/resetThreadBindingFrame
                                                                                old_frame__10109__auto__))))]
                                           (if (identical? ret_value__10110__auto__ :recur)
                                             (recur state_21452)
                                             ret_value__10110__auto__))))
                    state__10366__auto__ (let [statearr_21484 (^clojure.lang.IFn f__10365__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21484
                                             5
                                             c__10363__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_21484
                                             3
                                             captured_bindings__10364__auto__)
                                           statearr_21484)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__10366__auto__)))))
        c__10363__auto__)))
  (reset-meta!
    #'ensure-tombstone
    (assoc
      {:arglists (clojure.core/list ['log 'tombstone]), :column (int 1)}
      :name
      'ensure-tombstone
      :ns
      *ns*)))