(do
  (clojure.core/in-ns 'datomic.future)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'future 'future-call))
      (clojure.core/require
        ['clojure.core.async :as 'a :refer (clojure.core/list '>!! '<!! 'go 'alts! '<! '>!)]
        ['datomic.monitor :as 'monitor]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'clojure.lang.IPending)
      (clojure.core/import 'clojure.lang.IDeref)
      (clojure.core/import 'clojure.lang.IBlockingDeref)
      (clojure.core/import 'java.util.concurrent.Callable)
      (clojure.core/import 'java.util.concurrent.ExecutorService)
      (clojure.core/import 'java.util.concurrent.Future)
      (clojure.core/import 'java.util.concurrent.TimeoutException)))
  (when-not (.equals 'datomic.future 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.future))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'future 'future-call))
        (clojure.core/require
          ['clojure.core.async :as 'a :refer (clojure.core/list '>!! '<!! 'go 'alts! '<! '>!)]
          ['datomic.monitor :as 'monitor]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'clojure.lang.IPending)
        (clojure.core/import 'clojure.lang.IDeref)
        (clojure.core/import 'clojure.lang.IBlockingDeref)
        (clojure.core/import 'java.util.concurrent.Callable)
        (clojure.core/import 'java.util.concurrent.ExecutorService)
        (clojure.core/import 'java.util.concurrent.Future)
        (clojure.core/import 'java.util.concurrent.TimeoutException))))
  (set! *warn-on-reflection* true)
  (defn filling-promise
    ([f]
      (let [ch (a/promise-chan)
            f (fn ([]
                    (try
                      (let [v (^clojure.lang.IFn f)]
                        (if (nil? v) (a/>!! ch :datomic.future/nil) (a/>!! ch v))
                        v)
                      (catch
                        java.lang.Throwable
                        t
                        (do (a/>!! ch t) (throw ^java.lang.Throwable t) nil)))))]
        [f ch])))
  (reset-meta!
    #'filling-promise
    (assoc
      {:private true, :arglists (clojure.core/list ['f]), :column (int 1)}
      :name
      'filling-promise
      :ns
      *ns*))
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      GetChannel
      (get-channel
        [fut]
        "Returns a promise channel of return value (or exception) from fut.\nIf future returns nil, channel will get the special value ::nil."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.future" "GetChannel")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'GetChannel :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-channel
                                        {:arglists (clojure.core/list ['fut])}),
                                      :arglists (clojure.core/list ['fut]),
                                      :doc
                                      "Returns a promise channel of return value (or exception) from fut.\nIf future returns nil, channel will get the special value ::nil."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.future" "GetChannel"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.future" "get-channel")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (deftype
    JavaFutureWithChannel
    [fut ch]
    datomic.future.GetChannel
    clojure.lang.IPending
    clojure.lang.IBlockingDeref
    clojure.lang.IDeref
    (deref
      [this ^long timeout_ms timeout_val]
      (try
        (.get
          ^java.util.concurrent.Future fut
          (long timeout_ms)
          java.util.concurrent.TimeUnit/MILLISECONDS)
        (catch java.util.concurrent.TimeoutException e timeout_val)))
    (deref [this] (.get ^java.util.concurrent.Future fut))
    (^boolean isRealized [this] (.isDone ^java.util.concurrent.Future fut))
    (get-channel [this] ch))
  (clojure.core/import 'datomic.future.JavaFutureWithChannel)
  (defn ->JavaFutureWithChannel ([fut ch] (datomic.future.JavaFutureWithChannel. fut ch)))
  (reset-meta!
    #'->JavaFutureWithChannel
    (assoc
      {:arglists (clojure.core/list ['fut 'ch]), :column (int 1)}
      :name
      '->JavaFutureWithChannel
      :ns
      *ns*))
  (deftype
    ClojureFutureWithChannel
    [fut ch]
    datomic.future.GetChannel
    clojure.lang.IPending
    clojure.lang.IBlockingDeref
    clojure.lang.IDeref
    (deref [this ^long timeout_ms timeout_val] (deref fut (long timeout_ms) timeout_val))
    (deref [this] (.get ^java.util.concurrent.Future fut))
    (^boolean isRealized [this] (.isRealized ^clojure.lang.IPending fut))
    (get-channel [this] ch))
  (clojure.core/import 'datomic.future.ClojureFutureWithChannel)
  (defn ->ClojureFutureWithChannel ([fut ch] (datomic.future.ClojureFutureWithChannel. fut ch)))
  (reset-meta!
    #'->ClojureFutureWithChannel
    (assoc
      {:arglists (clojure.core/list ['fut 'ch]), :column (int 1)}
      :name
      '->ClojureFutureWithChannel
      :ns
      *ns*))
  (def -future-with-channel-impl
   (fn _future_with_channel_impl
     ([exec f]
       (let [vec__9711 (filling-promise ((deref #'clojure.core/binding-conveyor-fn) f))
             f (nth vec__9711 (int 0) nil)
             ch (nth vec__9711 (int 1) nil)
             fut (.submit
                   ^java.util.concurrent.ExecutorService exec
                   ^java.util.concurrent.Callable f)]
         (->JavaFutureWithChannel fut ch)))
     ([f]
       (let [vec__9708 (filling-promise f)
             f (nth vec__9708 (int 0) nil)
             ch (nth vec__9708 (int 1) nil)
             fut (clojure.core/future-call f)]
         (->ClojureFutureWithChannel fut ch)))))
  (reset-meta!
    #'-future-with-channel-impl
    (assoc
      {:arglists (clojure.core/list ['f] [(.withMeta 'exec {:tag 'ExecutorService}) 'f]),
       :column (int 1)}
      :name
      '-future-with-channel-impl
      :ns
      *ns*))
  (def add-bounding-warning
   (fn add_bounding_warning
     ([promise_ch context seconds]
       (let [c__5899__auto__ (a/chan 1)
             captured_bindings__5900__auto__ (clojure.lang.Var/getThreadBindingFrame)]
         (clojure.core.async.impl.dispatch/run
           (fn fn__9757
             ([]
               (let [G__9715 (fn G__9715 ([] promise_ch))
                     G__9716 (fn G__9716 ([] context))
                     G__9717 (fn G__9717 ([] seconds))
                     f__5901__auto__ (fn state_machine__5804__auto__
                                       ([]
                                         (let [statearr_9765 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 12))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_9765
                                             0
                                             state_machine__5804__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_9765
                                             1
                                             1)
                                           statearr_9765))
                                       ([state_9756]
                                         (let [old_frame__5805__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__5806__auto__ (try
                                                                         (try
                                                                           (do
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               (clojure.core.async.impl.ioc-macros/aget-object
                                                                                 state_9756
                                                                                 3))
                                                                             (loop 
                                                                               []
                                                                               (let 
                                                                                 [result__5807__auto__
                                                                                  (let 
                                                                                    [G__9767
                                                                                     (int
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_9756
                                                                                         1))]
                                                                                    (case
                                                                                      G__9767
                                                                                      5
                                                                                      (let 
                                                                                        [inst_9726
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           6)
                                                                                         inst_9735
                                                                                         (a/timeout
                                                                                           1000)
                                                                                         inst_9736
                                                                                         (vector
                                                                                           inst_9726
                                                                                           inst_9735)]
                                                                                        (a/ioc-alts!
                                                                                          state_9756
                                                                                          7
                                                                                          inst_9736))
                                                                                      3
                                                                                      (let 
                                                                                        [inst_9754
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           2)]
                                                                                        (clojure.core.async.impl.ioc-macros/return-chan
                                                                                          state_9756
                                                                                          inst_9754))
                                                                                      6
                                                                                      (let 
                                                                                        [inst_9752
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           2)]
                                                                                        (let 
                                                                                          [statearr_9773
                                                                                           state_9756]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9773
                                                                                            2
                                                                                            inst_9752)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9773
                                                                                            1
                                                                                            3))
                                                                                        :recur)
                                                                                      7
                                                                                      (let 
                                                                                        [inst_9726
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           6)
                                                                                         inst_9738
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           2)
                                                                                         inst_9739
                                                                                         (nth
                                                                                           inst_9738
                                                                                           (int 0)
                                                                                           nil)
                                                                                         inst_9740
                                                                                         (nth
                                                                                           inst_9738
                                                                                           (int 1)
                                                                                           nil)
                                                                                         inst_9741
                                                                                         inst_9738
                                                                                         inst_9742
                                                                                         inst_9739
                                                                                         inst_9743
                                                                                         inst_9740
                                                                                         inst_9744
                                                                                         (=
                                                                                           inst_9743
                                                                                           inst_9726)
                                                                                         state_9756
                                                                                         (let 
                                                                                           [statearr_9774
                                                                                            state_9756]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_9774
                                                                                             7
                                                                                             inst_9741)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_9774
                                                                                             8
                                                                                             inst_9742)
                                                                                           statearr_9774)]
                                                                                        (if
                                                                                          inst_9744
                                                                                          (let 
                                                                                            [statearr_9775
                                                                                             state_9756]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_9775
                                                                                              1
                                                                                              8))
                                                                                          (let 
                                                                                            [statearr_9776
                                                                                             state_9756]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_9776
                                                                                              1
                                                                                              9)))
                                                                                        :recur)
                                                                                      4
                                                                                      (let 
                                                                                        [inst_9726
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           6)
                                                                                         inst_9727
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           9)
                                                                                         inst_9728
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           10)
                                                                                         inst_9729
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           11)
                                                                                         promise_ch
                                                                                         inst_9726
                                                                                         context
                                                                                         inst_9727
                                                                                         seconds
                                                                                         inst_9728
                                                                                         n
                                                                                         inst_9729]
                                                                                        (monitor/add-stat
                                                                                          :FutureBoundExceeded
                                                                                          1)
                                                                                        (let 
                                                                                          [inst_9733
                                                                                           (let 
                                                                                             [logger
                                                                                              (org.slf4j.LoggerFactory/getLogger
                                                                                                "datomic.future")]
                                                                                             (when
                                                                                               (.isWarnEnabled
                                                                                                 ^org.slf4j.Logger logger)
                                                                                               (.warn
                                                                                                 ^org.slf4j.Logger logger
                                                                                                 (logger/process
                                                                                                   {:event
                                                                                                    :datomic.future/unfilled,
                                                                                                    :seconds
                                                                                                    seconds,
                                                                                                    :context
                                                                                                    context})))
                                                                                             nil)]
                                                                                          (let 
                                                                                            [statearr_9772
                                                                                             state_9756]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_9772
                                                                                              2
                                                                                              inst_9733)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_9772
                                                                                              1
                                                                                              6))
                                                                                          :recur))
                                                                                      1
                                                                                      (let 
                                                                                        [inst_9723
                                                                                         (^clojure.lang.IFn G__9715)
                                                                                         promise_ch
                                                                                         inst_9723
                                                                                         inst_9724
                                                                                         (^clojure.lang.IFn G__9716)
                                                                                         promise_ch
                                                                                         inst_9723
                                                                                         context
                                                                                         inst_9724
                                                                                         inst_9725
                                                                                         (^clojure.lang.IFn G__9717)
                                                                                         inst_9726
                                                                                         inst_9723
                                                                                         inst_9727
                                                                                         inst_9724
                                                                                         inst_9728
                                                                                         inst_9725
                                                                                         inst_9729
                                                                                         0
                                                                                         state_9756
                                                                                         (let 
                                                                                           [statearr_9768
                                                                                            state_9756]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_9768
                                                                                             6
                                                                                             inst_9726)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_9768
                                                                                             9
                                                                                             inst_9727)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_9768
                                                                                             10
                                                                                             inst_9728)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_9768
                                                                                             11
                                                                                             (long
                                                                                               inst_9729))
                                                                                           statearr_9768)]
                                                                                        (let 
                                                                                          [statearr_9769
                                                                                           state_9756]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9769
                                                                                            2
                                                                                            nil)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9769
                                                                                            1
                                                                                            2))
                                                                                        :recur)
                                                                                      2
                                                                                      (let 
                                                                                        [inst_9729
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           11)
                                                                                         inst_9728
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           10)
                                                                                         inst_9731
                                                                                         (=
                                                                                           inst_9729
                                                                                           inst_9728)]
                                                                                        (if
                                                                                          inst_9731
                                                                                          (let 
                                                                                            [statearr_9770
                                                                                             state_9756]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_9770
                                                                                              1
                                                                                              4))
                                                                                          (let 
                                                                                            [statearr_9771
                                                                                             state_9756]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_9771
                                                                                              1
                                                                                              5)))
                                                                                        :recur)
                                                                                      10
                                                                                      (let 
                                                                                        [inst_9750
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           2)]
                                                                                        (let 
                                                                                          [statearr_9780
                                                                                           state_9756]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9780
                                                                                            2
                                                                                            inst_9750)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9780
                                                                                            1
                                                                                            6))
                                                                                        :recur)
                                                                                      8
                                                                                      (do
                                                                                        (let 
                                                                                          [statearr_9777
                                                                                           state_9756]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9777
                                                                                            2
                                                                                            nil)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9777
                                                                                            1
                                                                                            10))
                                                                                        :recur)
                                                                                      9
                                                                                      (let 
                                                                                        [inst_9729
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_9756
                                                                                           11)
                                                                                         inst_9747
                                                                                         (inc
                                                                                           inst_9729)
                                                                                         inst_9729
                                                                                         inst_9747
                                                                                         state_9756
                                                                                         (let 
                                                                                           [statearr_9778
                                                                                            state_9756]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_9778
                                                                                             11
                                                                                             inst_9729)
                                                                                           statearr_9778)]
                                                                                        (let 
                                                                                          [statearr_9779
                                                                                           state_9756]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9779
                                                                                            2
                                                                                            nil)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_9779
                                                                                            1
                                                                                            2))
                                                                                        :recur)))]
                                                                                 (if
                                                                                   (identical?
                                                                                     result__5807__auto__
                                                                                     :recur)
                                                                                   (recur)
                                                                                   result__5807__auto__))))
                                                                           (catch
                                                                             java.lang.Throwable
                                                                             ex__5808__auto__
                                                                             (do
                                                                               (let 
                                                                                 [statearr_9781
                                                                                  state_9756]
                                                                                 (clojure.core.async.impl.ioc-macros/aset-object
                                                                                   statearr_9781
                                                                                   2
                                                                                   ex__5808__auto__))
                                                                               (if
                                                                                 (seq
                                                                                   (clojure.core.async.impl.ioc-macros/aget-object
                                                                                     state_9756
                                                                                     4))
                                                                                 (let 
                                                                                   [statearr_9782
                                                                                    state_9756]
                                                                                   (clojure.core.async.impl.ioc-macros/aset-object
                                                                                     statearr_9782
                                                                                     1
                                                                                     (first
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_9756
                                                                                         4))))
                                                                                 (throw
                                                                                   ^java.lang.Throwable ex__5808__auto__))
                                                                               :recur)))
                                                                         (finally
                                                                           (do
                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                               state_9756
                                                                               3
                                                                               (clojure.lang.Var/getThreadBindingFrame))
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               old_frame__5805__auto__))))]
                                           (if (identical? ret_value__5806__auto__ :recur)
                                             (recur state_9756)
                                             ret_value__5806__auto__))))
                     state__5902__auto__ (let [statearr_9788 (^clojure.lang.IFn f__5901__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_9788
                                             5
                                             c__5899__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_9788
                                             3
                                             captured_bindings__5900__auto__)
                                           statearr_9788)]
                 (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                   state__5902__auto__)))))
         c__5899__auto__))))
  (reset-meta!
    #'add-bounding-warning
    (assoc
      {:arglists (clojure.core/list ['promise-ch 'context 'seconds]), :column (int 1)}
      :name
      'add-bounding-warning
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.future" "bounding-warn-seconds") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.future" "bounding-warn-seconds") (atom 180))
  (def future
   (fn future
     ([&form &env & body]
       (seq
         (concat
           (clojure.core/list 'datomic.future/-future-with-channel-impl)
           (-> (with-meta
                 (.withMeta 'fn* {:once true})
                 (apply
                   hash-map
                   (seq (concat (clojure.core/list :once) (clojure.core/list true)))))
            (clojure.core/list)
            (concat (clojure.core/list (apply vector (seq (concat)))) body)
            (seq)
            (clojure.core/list)))))))
  (reset-meta!
    #'future
    (assoc {:arglists (clojure.core/list ['& 'body]), :column (int 1)} :name 'future :ns *ns*))
  (.setMacro #'future)
  (def future-call
   (fn future_call
     ([&form &env f]
       (seq
         (concat
           (clojure.core/list 'datomic.future/-future-with-channel-impl)
           (clojure.core/list f))))))
  (reset-meta!
    #'future-call
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name 'future-call :ns *ns*))
  (.setMacro #'future-call)
  (def pfuture
   (fn pfuture
     ([&form &env exec & body]
       (seq
         (concat
           (clojure.core/list 'datomic.future/-future-with-channel-impl)
           (clojure.core/list exec)
           (-> (with-meta
                 (.withMeta 'fn* {:once true})
                 (apply
                   hash-map
                   (seq (concat (clojure.core/list :once) (clojure.core/list true)))))
            (clojure.core/list)
            (concat (clojure.core/list (apply vector (seq (concat)))) body)
            (seq)
            (clojure.core/list)))))))
  (reset-meta!
    #'pfuture
    (assoc
      {:arglists (clojure.core/list ['exec '& 'body]), :column (int 1)}
      :name
      'pfuture
      :ns
      *ns*))
  (.setMacro #'pfuture)
  (def pfuture-call
   (fn pfuture_call
     ([&form &env exec f]
       (seq
         (concat
           (clojure.core/list 'datomic.future/-future-with-channel-impl)
           (clojure.core/list exec)
           (clojure.core/list f))))))
  (reset-meta!
    #'pfuture-call
    (assoc
      {:arglists (clojure.core/list ['exec 'f]), :column (int 1)}
      :name
      'pfuture-call
      :ns
      *ns*))
  (.setMacro #'pfuture-call))