(do
  (clojure.core/in-ns 'datomic.future)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.future)
    {:doc
     "Asynchronous tasks paired with core.async completion channels. Each wrapper remains dereferenceable as a future while exposing a promise channel containing its value, a sentinel for nil, or the original Throwable. Optional watchers record and log operations that remain unfilled beyond a configured bound."})
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
      {:private true,
       :arglists (clojure.core/list ['f]),
       :doc
       "Returns [wrapped-task completion-channel]. The task publishes its value to a promise channel, using :datomic.future/nil for nil; a Throwable is published and then rethrown for the future to retain.",
       :column (int 1)}
      :name
      'filling-promise
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      GetChannel
      (get-channel
        [fut]
        "Returns the promise channel carrying fut's value or Throwable. Nil is represented by :datomic.future/nil."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.future" "GetChannel")
      (assoc
        (assoc protocol_metadata__7463 :doc "Access to a future's core.async completion channel.")
        :name
        'GetChannel
        :ns
        *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-channel
                                        {:arglists (clojure.core/list ['fut])}),
                                      :arglists (clojure.core/list ['fut]),
                                      :doc
                                      "Returns the promise channel carrying fut's value or Throwable. Nil is represented by :datomic.future/nil."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.future" "GetChannel"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.future" "get-channel")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (deftype
    JavaFutureWithChannel
    [fut ch]
    datomic.future.GetChannel
    clojure.lang.IPending
    clojure.lang.IBlockingDeref
    clojure.lang.IDeref
    (deref
      [this ^long timeout-ms timeout-value]
      (try
        (.get
          ^java.util.concurrent.Future fut
          (long timeout-ms)
          java.util.concurrent.TimeUnit/MILLISECONDS)
        (catch java.util.concurrent.TimeoutException e timeout-value)))
    (deref [this] (.get ^java.util.concurrent.Future fut))
    (^boolean isRealized [this] (.isDone ^java.util.concurrent.Future fut))
    (get-channel [this] ch))
  (clojure.core/import 'datomic.future.JavaFutureWithChannel)
  (defn ->JavaFutureWithChannel ([fut ch] (datomic.future.JavaFutureWithChannel. fut ch)))
  (reset-meta!
    #'->JavaFutureWithChannel
    (assoc
      {:arglists (clojure.core/list ['fut 'ch]),
       :doc "Wraps a Java Future with its completion channel and Clojure dereference interfaces.",
       :column (int 1)}
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
    (deref [this ^long timeout-ms timeout-value]
      (deref fut (long timeout-ms) timeout-value))
    (deref [this] (.get ^java.util.concurrent.Future fut))
    (^boolean isRealized [this] (.isRealized ^clojure.lang.IPending fut))
    (get-channel [this] ch))
  (clojure.core/import 'datomic.future.ClojureFutureWithChannel)
  (defn ->ClojureFutureWithChannel ([fut ch] (datomic.future.ClojureFutureWithChannel. fut ch)))
  (reset-meta!
    #'->ClojureFutureWithChannel
    (assoc
      {:arglists (clojure.core/list ['fut 'ch]),
       :doc "Wraps a Clojure future with its completion channel.",
       :column (int 1)}
      :name
      '->ClojureFutureWithChannel
      :ns
      *ns*))
  (defn -future-with-channel-impl
    ([executor f]
      (let [vec__9711 (filling-promise ((deref #'clojure.core/binding-conveyor-fn) f))
            f (nth vec__9711 (int 0) nil)
            ch (nth vec__9711 (int 1) nil)
            fut (.submit
                  ^java.util.concurrent.ExecutorService executor
                  ^java.util.concurrent.Callable f)]
        (->JavaFutureWithChannel fut ch)))
    ([f]
      (let [vec__9708 (filling-promise f)
            f (nth vec__9708 (int 0) nil)
            ch (nth vec__9708 (int 1) nil)
            fut (clojure.core/future-call f)]
        (->ClojureFutureWithChannel fut ch))))
  (reset-meta!
    #'-future-with-channel-impl
    (assoc
      {:arglists (clojure.core/list ['f] [(.withMeta 'executor {:tag 'ExecutorService}) 'f]),
       :doc
       "Submits f to Clojure's future executor or to executor and returns a dereferenceable future with a completion channel. The explicit-executor form conveys the caller's dynamic bindings.",
       :column (int 1)}
      :name
      '-future-with-channel-impl
      :ns
      *ns*))
  (defn add-bounding-warning
    ([promise-ch context seconds]
      (let [c__5899__auto__ (a/chan 1)
            captured_bindings__5900__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__9757
            ([]
              (let [G__9715 (fn G__9715 ([] promise-ch))
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
                                                                                        promise-ch
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
                                                                                        promise-ch
                                                                                        inst_9723
                                                                                        inst_9724
                                                                                        (^clojure.lang.IFn G__9716)
                                                                                        promise-ch
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
                                                                                        inst_9729 0
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
        c__5899__auto__)))
  (reset-meta!
    #'add-bounding-warning
    (assoc
      {:arglists (clojure.core/list ['promise-ch 'context 'seconds]),
       :doc
       "Starts an asynchronous watcher for promise-ch. If the channel remains unfilled for seconds, records :FutureBoundExceeded and logs one :datomic.future/unfilled warning with context, then continues waiting. Returns a channel that completes with nil when promise-ch is filled.",
       :column (int 1)}
      :name
      'add-bounding-warning
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.future" "bounding-warn-seconds")
    {:doc "Default future-bound warning threshold in seconds.", :column (int 1)})
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
    (assoc
      {:arglists (clojure.core/list ['& 'body]),
       :doc "Evaluates body asynchronously and returns a dereferenceable future with a completion channel.",
       :column (int 1)}
      :name
      'future
      :ns
      *ns*))
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
    (assoc
      {:arglists (clojure.core/list ['f]),
       :doc "Submits the zero-argument function f and returns a dereferenceable future with a completion channel.",
       :column (int 1)}
      :name
      'future-call
      :ns
      *ns*))
  (.setMacro #'future-call)
  (def pfuture
   (fn pfuture
     ([&form &env executor & body]
       (seq
         (concat
           (clojure.core/list 'datomic.future/-future-with-channel-impl)
           (clojure.core/list executor)
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
      {:arglists (clojure.core/list ['executor '& 'body]),
       :doc
       "Evaluates body asynchronously on executor and returns a dereferenceable future with a completion channel.",
       :column (int 1)}
      :name
      'pfuture
      :ns
      *ns*))
  (.setMacro #'pfuture)
  (def pfuture-call
   (fn pfuture_call
     ([&form &env executor f]
       (seq
         (concat
           (clojure.core/list 'datomic.future/-future-with-channel-impl)
           (clojure.core/list executor)
           (clojure.core/list f))))))
  (reset-meta!
    #'pfuture-call
    (assoc
      {:arglists (clojure.core/list ['executor 'f]),
       :doc
       "Submits the zero-argument function f to executor and returns a dereferenceable future with a completion channel.",
       :column (int 1)}
      :name
      'pfuture-call
      :ns
      *ns*))
  (.setMacro #'pfuture-call))
