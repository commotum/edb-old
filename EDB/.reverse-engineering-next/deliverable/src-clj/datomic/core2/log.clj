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
    ([log p__20560 body]
      (let [map__20561 p__20560
            map__20561 (if (seq? map__20561)
                         (if (next map__20561)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20561))
                           (if (seq map__20561) (first map__20561) {}))
                         map__20561)
            header map__20561
            t (get map__20561 :t)
            next_t (get map__20561 :next-t)]
        (when-not t (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 't)))))
        (when-not next_t
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'next-t)))))
        (spi/-append log header body)))
    ([log header] (append log header nil)))
  (def delete spi/-delete)
  (defn scan
    ([log opts]
      (let [map__20563 (spi/normalize-scan-opts opts)
            map__20563 (if (seq? map__20563)
                         (if (next map__20563)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20563))
                           (if (seq map__20563) (first map__20563) {}))
                         map__20563)
            nopts map__20563
            ch (get map__20563 :ch)]
        (spi/-scan log nopts)
        ch)))
  (def item-header spi/-item-header)
  (def item-body spi/-item-body)
  (defn ensure-tombstone
    ([log tombstone]
      (let [c__10230__auto__ (a/chan 1)
            captured_bindings__10231__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__20596
            ([]
              (let [G__20565 (fn G__20565 ([] log))
                    G__20566 (fn G__20566 ([] tombstone))
                    f__10232__auto__ (fn state_machine__9975__auto__
                                       ([]
                                         (let [statearr_20602 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 11))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20602
                                             0
                                             state_machine__9975__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20602
                                             1
                                             1)
                                           statearr_20602))
                                       ([state_20595]
                                         (let [old_frame__9976__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__9977__auto__ (try
                                                                         (do
                                                                           (clojure.lang.Var/resetThreadBindingFrame
                                                                             (clojure.core.async.impl.ioc-macros/aget-object
                                                                               state_20595
                                                                               3))
                                                                           (loop 
                                                                             []
                                                                             (let 
                                                                               [result__9978__auto__
                                                                                (let 
                                                                                  [G__20604
                                                                                   (int
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_20595
                                                                                       1))]
                                                                                  (case
                                                                                    G__20604
                                                                                    2
                                                                                    (let 
                                                                                      [inst_20570
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         6)
                                                                                       inst_20571
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         7)
                                                                                       inst_20575
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         8)
                                                                                       inst_20577
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         9)
                                                                                       inst_20574
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         2)
                                                                                       inst_20575
                                                                                       inst_20574
                                                                                       log
                                                                                       inst_20570
                                                                                       tombstone
                                                                                       inst_20571
                                                                                       item
                                                                                       inst_20575
                                                                                       inst_20576
                                                                                       (datomic.core2.anomalies/anom
                                                                                         item)
                                                                                       inst_20577
                                                                                       inst_20576
                                                                                       state_20595
                                                                                       (let 
                                                                                         [statearr_20606
                                                                                          state_20595]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20606
                                                                                           8
                                                                                           inst_20575)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20606
                                                                                           9
                                                                                           inst_20577)
                                                                                         statearr_20606)]
                                                                                      (if
                                                                                        inst_20577
                                                                                        (let 
                                                                                          [statearr_20607
                                                                                           state_20595]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20607
                                                                                            1
                                                                                            3))
                                                                                        (let 
                                                                                          [statearr_20608
                                                                                           state_20595]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20608
                                                                                            1
                                                                                            4)))
                                                                                      :recur)
                                                                                    6
                                                                                    (let 
                                                                                      [inst_20581
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         10)]
                                                                                      (let 
                                                                                        [statearr_20613
                                                                                         state_20595]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20613
                                                                                          2
                                                                                          inst_20581)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20613
                                                                                          1
                                                                                          8))
                                                                                      :recur)
                                                                                    1
                                                                                    (let 
                                                                                      [inst_20570
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         6)
                                                                                       inst_20571
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         7)
                                                                                       inst_20568
                                                                                       (^clojure.lang.IFn G__20565)
                                                                                       log
                                                                                       inst_20568
                                                                                       inst_20569
                                                                                       (^clojure.lang.IFn G__20566)
                                                                                       inst_20570
                                                                                       inst_20568
                                                                                       inst_20571
                                                                                       inst_20569
                                                                                       log
                                                                                       inst_20570
                                                                                       tombstone
                                                                                       inst_20571
                                                                                       inst_20572
                                                                                       (scan
                                                                                         log
                                                                                         {:direction
                                                                                          :backward,
                                                                                          :limit
                                                                                          1})
                                                                                       state_20595
                                                                                       (let 
                                                                                         [statearr_20605
                                                                                          state_20595]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20605
                                                                                           6
                                                                                           inst_20570)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20605
                                                                                           7
                                                                                           inst_20571)
                                                                                         statearr_20605)]
                                                                                      (clojure.core.async.impl.ioc-macros/take!
                                                                                        state_20595
                                                                                        2
                                                                                        inst_20572))
                                                                                    9
                                                                                    (let 
                                                                                      [inst_20589
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_20615
                                                                                         state_20595]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20615
                                                                                          2
                                                                                          inst_20589)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20615
                                                                                          1
                                                                                          8))
                                                                                      :recur)
                                                                                    3
                                                                                    (let 
                                                                                      [inst_20577
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         9)]
                                                                                      (let 
                                                                                        [statearr_20609
                                                                                         state_20595]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20609
                                                                                          2
                                                                                          inst_20577)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20609
                                                                                          1
                                                                                          5))
                                                                                      :recur)
                                                                                    8
                                                                                    (let 
                                                                                      [inst_20591
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_20614
                                                                                         state_20595]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20614
                                                                                          2
                                                                                          inst_20591)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20614
                                                                                          1
                                                                                          5))
                                                                                      :recur)
                                                                                    7
                                                                                    (let 
                                                                                      [inst_20570
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         6)
                                                                                       inst_20571
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         7)
                                                                                       inst_20575
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         8)
                                                                                       inst_20577
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         9)
                                                                                       inst_20581
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         10)
                                                                                       log
                                                                                       inst_20570
                                                                                       tombstone
                                                                                       inst_20571
                                                                                       item
                                                                                       inst_20575
                                                                                       or__5581__auto__
                                                                                       inst_20577
                                                                                       header
                                                                                       inst_20581
                                                                                       inst_20585
                                                                                       (assoc
                                                                                         (assoc
                                                                                           header
                                                                                           :t
                                                                                           (:next-t
                                                                                             header))
                                                                                         :tombstone
                                                                                         tombstone)
                                                                                       inst_20586
                                                                                       inst_20585
                                                                                       log
                                                                                       inst_20570
                                                                                       tombstone
                                                                                       inst_20571
                                                                                       item
                                                                                       inst_20575
                                                                                       or__5581__auto__
                                                                                       inst_20577
                                                                                       header
                                                                                       inst_20581
                                                                                       new_header
                                                                                       inst_20586
                                                                                       inst_20587
                                                                                       (append
                                                                                         log
                                                                                         new_header)]
                                                                                      (clojure.core.async.impl.ioc-macros/take!
                                                                                        state_20595
                                                                                        9
                                                                                        inst_20587))
                                                                                    4
                                                                                    (let 
                                                                                      [inst_20570
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         6)
                                                                                       inst_20571
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         7)
                                                                                       inst_20575
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         8)
                                                                                       inst_20577
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         9)
                                                                                       inst_20581
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         10)
                                                                                       log
                                                                                       inst_20570
                                                                                       tombstone
                                                                                       inst_20571
                                                                                       item
                                                                                       inst_20575
                                                                                       or__5581__auto__
                                                                                       inst_20577
                                                                                       inst_20580
                                                                                       (item-header
                                                                                         log
                                                                                         item)
                                                                                       inst_20581
                                                                                       inst_20580
                                                                                       log
                                                                                       inst_20570
                                                                                       tombstone
                                                                                       inst_20571
                                                                                       item
                                                                                       inst_20575
                                                                                       or__5581__auto__
                                                                                       inst_20577
                                                                                       header
                                                                                       inst_20581
                                                                                       inst_20582
                                                                                       (:tombstone
                                                                                         header)
                                                                                       state_20595
                                                                                       (let 
                                                                                         [statearr_20610
                                                                                          state_20595]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20610
                                                                                           10
                                                                                           inst_20581)
                                                                                         statearr_20610)]
                                                                                      (if
                                                                                        inst_20582
                                                                                        (let 
                                                                                          [statearr_20611
                                                                                           state_20595]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20611
                                                                                            1
                                                                                            6))
                                                                                        (let 
                                                                                          [statearr_20612
                                                                                           state_20595]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20612
                                                                                            1
                                                                                            7)))
                                                                                      :recur)
                                                                                    5
                                                                                    (let 
                                                                                      [inst_20593
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20595
                                                                                         2)]
                                                                                      (clojure.core.async.impl.ioc-macros/return-chan
                                                                                        state_20595
                                                                                        inst_20593))))]
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
                                                                               [statearr_20616
                                                                                state_20595]
                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                 statearr_20616
                                                                                 2
                                                                                 ex__9979__auto__))
                                                                             (if
                                                                               (seq
                                                                                 (clojure.core.async.impl.ioc-macros/aget-object
                                                                                   state_20595
                                                                                   4))
                                                                               (let 
                                                                                 [statearr_20617
                                                                                  state_20595]
                                                                                 (clojure.core.async.impl.ioc-macros/aset-object
                                                                                   statearr_20617
                                                                                   1
                                                                                   (first
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_20595
                                                                                       4))))
                                                                               (throw
                                                                                 ^java.lang.Throwable ex__9979__auto__))
                                                                             :recur))
                                                                         (finally
                                                                           (do
                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                               state_20595
                                                                               3
                                                                               (clojure.lang.Var/getThreadBindingFrame))
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               old_frame__9976__auto__))))]
                                           (if (identical? ret_value__9977__auto__ :recur)
                                             (recur state_20595)
                                             ret_value__9977__auto__))))
                    state__10233__auto__ (let [statearr_20627 (^clojure.lang.IFn f__10232__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20627
                                             5
                                             c__10230__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20627
                                             3
                                             captured_bindings__10231__auto__)
                                           statearr_20627)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__10233__auto__)))))
        c__10230__auto__))))