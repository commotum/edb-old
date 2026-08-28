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
            f (fn
                ([]
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
      {:private true, :arglists (clojure.core/list ['f]), :column 1}
      :name
      'filling-promise
      :ns
      *ns*))
  (defonce GetChannel {})
  (defprotocol GetChannel (get-channel [fut]))
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
  (defn -future-with-channel-impl
    ([exec f]
      (let [vec__16086 (filling-promise ((deref #'clojure.core/binding-conveyor-fn) f))
            f (nth vec__16086 (int 0) nil)
            ch (nth vec__16086 (int 1) nil)
            fut (.submit
                  ^java.util.concurrent.ExecutorService exec
                  ^java.util.concurrent.Callable f)]
        (->JavaFutureWithChannel fut ch)))
    ([f]
      (let [vec__16083 (filling-promise f)
            f (nth vec__16083 (int 0) nil)
            ch (nth vec__16083 (int 1) nil)
            fut (clojure.core/future-call f)]
        (->ClojureFutureWithChannel fut ch))))
  (defn add-bounding-warning
    ([promise_ch context seconds]
      (let [c__6079__auto__ (a/chan 1)
            captured_bindings__6080__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__16132
            ([]
              (let [G__16090 (fn G__16090 ([] promise_ch))
                    G__16091 (fn G__16091 ([] context))
                    G__16092 (fn G__16092 ([] seconds))
                    f__6081__auto__ (fn state_machine__5842__auto__
                                      ([]
                                        (let [statearr_16140 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 12))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16140
                                            0
                                            state_machine__5842__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16140
                                            1
                                            1)
                                          statearr_16140))
                                      ([state_16131]
                                        (let [old_frame__5843__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__5844__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_16131
                                                                                3))
                                                                            (loop 
                                                                              []
                                                                              (let 
                                                                                [result__5845__auto__
                                                                                 (let 
                                                                                   [G__16142
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16131
                                                                                        1))]
                                                                                   (case
                                                                                     G__16142
                                                                                     8
                                                                                     (do
                                                                                       (let 
                                                                                         [statearr_16152
                                                                                          state_16131]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16152
                                                                                           2
                                                                                           nil)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16152
                                                                                           1
                                                                                           10))
                                                                                       :recur)
                                                                                     7
                                                                                     (let 
                                                                                       [inst_16101
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          6)
                                                                                        inst_16113
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          2)
                                                                                        inst_16114
                                                                                        (nth
                                                                                          inst_16113
                                                                                          (int 0)
                                                                                          nil)
                                                                                        inst_16115
                                                                                        (nth
                                                                                          inst_16113
                                                                                          (int 1)
                                                                                          nil)
                                                                                        inst_16116
                                                                                        inst_16113
                                                                                        inst_16117
                                                                                        inst_16114
                                                                                        inst_16118
                                                                                        inst_16115
                                                                                        inst_16119
                                                                                        (=
                                                                                          inst_16118
                                                                                          inst_16101)
                                                                                        state_16131
                                                                                        (let 
                                                                                          [statearr_16149
                                                                                           state_16131]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16149
                                                                                            7
                                                                                            inst_16116)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16149
                                                                                            8
                                                                                            inst_16117)
                                                                                          statearr_16149)]
                                                                                       (if
                                                                                         inst_16119
                                                                                         (let 
                                                                                           [statearr_16150
                                                                                            state_16131]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_16150
                                                                                             1
                                                                                             8))
                                                                                         (let 
                                                                                           [statearr_16151
                                                                                            state_16131]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_16151
                                                                                             1
                                                                                             9)))
                                                                                       :recur)
                                                                                     9
                                                                                     (let 
                                                                                       [inst_16104
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          11)
                                                                                        inst_16122
                                                                                        (inc
                                                                                          inst_16104)
                                                                                        inst_16104
                                                                                        inst_16122
                                                                                        state_16131
                                                                                        (let 
                                                                                          [statearr_16153
                                                                                           state_16131]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16153
                                                                                            11
                                                                                            inst_16104)
                                                                                          statearr_16153)]
                                                                                       (let 
                                                                                         [statearr_16154
                                                                                          state_16131]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16154
                                                                                           2
                                                                                           nil)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16154
                                                                                           1
                                                                                           2))
                                                                                       :recur)
                                                                                     1
                                                                                     (let 
                                                                                       [inst_16098
                                                                                        (^clojure.lang.IFn G__16090)
                                                                                        promise_ch
                                                                                        inst_16098
                                                                                        inst_16099
                                                                                        (^clojure.lang.IFn G__16091)
                                                                                        promise_ch
                                                                                        inst_16098
                                                                                        context
                                                                                        inst_16099
                                                                                        inst_16100
                                                                                        (^clojure.lang.IFn G__16092)
                                                                                        inst_16101
                                                                                        inst_16098
                                                                                        inst_16102
                                                                                        inst_16099
                                                                                        inst_16103
                                                                                        inst_16100
                                                                                        inst_16104
                                                                                        0
                                                                                        state_16131
                                                                                        (let 
                                                                                          [statearr_16143
                                                                                           state_16131]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16143
                                                                                            6
                                                                                            inst_16101)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16143
                                                                                            9
                                                                                            inst_16102)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16143
                                                                                            10
                                                                                            inst_16103)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16143
                                                                                            11
                                                                                            (long
                                                                                              inst_16104))
                                                                                          statearr_16143)]
                                                                                       (let 
                                                                                         [statearr_16144
                                                                                          state_16131]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16144
                                                                                           2
                                                                                           nil)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16144
                                                                                           1
                                                                                           2))
                                                                                       :recur)
                                                                                     6
                                                                                     (let 
                                                                                       [inst_16127
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          2)]
                                                                                       (let 
                                                                                         [statearr_16148
                                                                                          state_16131]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16148
                                                                                           2
                                                                                           inst_16127)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16148
                                                                                           1
                                                                                           3))
                                                                                       :recur)
                                                                                     4
                                                                                     (let 
                                                                                       [inst_16101
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          6)
                                                                                        inst_16102
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          9)
                                                                                        inst_16103
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          10)
                                                                                        inst_16104
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          11)
                                                                                        promise_ch
                                                                                        inst_16101
                                                                                        context
                                                                                        inst_16102
                                                                                        seconds
                                                                                        inst_16103
                                                                                        n
                                                                                        inst_16104]
                                                                                       (monitor/add-stat
                                                                                         :FutureBoundExceeded
                                                                                         1)
                                                                                       (let 
                                                                                         [inst_16108
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
                                                                                           [statearr_16147
                                                                                            state_16131]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_16147
                                                                                             2
                                                                                             inst_16108)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_16147
                                                                                             1
                                                                                             6))
                                                                                         :recur))
                                                                                     5
                                                                                     (let 
                                                                                       [inst_16101
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          6)
                                                                                        inst_16110
                                                                                        (a/timeout
                                                                                          1000)
                                                                                        inst_16111
                                                                                        (vector
                                                                                          inst_16101
                                                                                          inst_16110)]
                                                                                       (a/ioc-alts!
                                                                                         state_16131
                                                                                         7
                                                                                         inst_16111))
                                                                                     2
                                                                                     (let 
                                                                                       [inst_16104
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          11)
                                                                                        inst_16103
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          10)
                                                                                        inst_16106
                                                                                        (=
                                                                                          inst_16104
                                                                                          inst_16103)]
                                                                                       (if
                                                                                         inst_16106
                                                                                         (let 
                                                                                           [statearr_16145
                                                                                            state_16131]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_16145
                                                                                             1
                                                                                             4))
                                                                                         (let 
                                                                                           [statearr_16146
                                                                                            state_16131]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_16146
                                                                                             1
                                                                                             5)))
                                                                                       :recur)
                                                                                     10
                                                                                     (let 
                                                                                       [inst_16125
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          2)]
                                                                                       (let 
                                                                                         [statearr_16155
                                                                                          state_16131]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16155
                                                                                           2
                                                                                           inst_16125)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_16155
                                                                                           1
                                                                                           6))
                                                                                       :recur)
                                                                                     3
                                                                                     (let 
                                                                                       [inst_16129
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16131
                                                                                          2)]
                                                                                       (clojure.core.async.impl.ioc-macros/return-chan
                                                                                         state_16131
                                                                                         inst_16129))))]
                                                                                (if
                                                                                  (identical?
                                                                                    result__5845__auto__
                                                                                    :recur)
                                                                                  (recur)
                                                                                  result__5845__auto__))))
                                                                          (catch
                                                                            java.lang.Throwable
                                                                            ex__5846__auto__
                                                                            (do
                                                                              (let 
                                                                                [statearr_16156
                                                                                 state_16131]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_16156
                                                                                  2
                                                                                  ex__5846__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_16131
                                                                                    4))
                                                                                (let 
                                                                                  [statearr_16157
                                                                                   state_16131]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_16157
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16131
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__5846__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_16131
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__5843__auto__))))]
                                          (if (identical? ret_value__5844__auto__ :recur)
                                            (recur state_16131)
                                            ret_value__5844__auto__))))
                    state__6082__auto__ (let [statearr_16163 (^clojure.lang.IFn f__6081__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16163
                                            5
                                            c__6079__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16163
                                            3
                                            captured_bindings__6080__auto__)
                                          statearr_16163)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6082__auto__)))))
        c__6079__auto__)))
  (def bounding-warn-seconds (atom 180))
  (defn future
    ([&form &env & body]
      (let [context (assoc (meta &form) :file *file*)]
        (seq
          (concat
            (clojure.core/list 'clojure.core/let)
            (clojure.core/list
              (apply
                vector
                (seq
                  (concat
                    (clojure.core/list 'f__16170__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/-future-with-channel-impl)
                          (-> (with-meta
                                (.withMeta 'fn* {:once true})
                                (apply
                                  hash-map
                                  (seq
                                    (concat (clojure.core/list :once) (clojure.core/list true)))))
                           (clojure.core/list)
                           (concat (clojure.core/list (apply vector (seq (concat)))) body)
                           (seq)
                           (clojure.core/list)))))
                    (clojure.core/list 'ch__16171__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/get-channel)
                          (clojure.core/list 'f__16170__auto__))))))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'datomic.future/add-bounding-warning)
                  (clojure.core/list 'ch__16171__auto__)
                  (clojure.core/list context)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/deref)
                        (clojure.core/list 'datomic.future/bounding-warn-seconds)))))))
            (clojure.core/list 'f__16170__auto__))))))
  (.setMacro #'future)
  (defn future-call
    ([&form &env f]
      (let [context (assoc (meta &form) :file *file*)]
        (seq
          (concat
            (clojure.core/list 'clojure.core/let)
            (clojure.core/list
              (apply
                vector
                (seq
                  (concat
                    (clojure.core/list 'f__16173__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/-future-with-channel-impl)
                          (clojure.core/list f))))
                    (clojure.core/list 'ch__16174__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/get-channel)
                          (clojure.core/list 'f__16173__auto__))))))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'datomic.future/add-bounding-warning)
                  (clojure.core/list 'ch__16174__auto__)
                  (clojure.core/list context)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/deref)
                        (clojure.core/list 'datomic.future/bounding-warn-seconds)))))))
            (clojure.core/list 'f__16173__auto__))))))
  (.setMacro #'future-call)
  (defn pfuture
    ([&form &env exec & body]
      (let [context (assoc (meta &form) :file *file*)]
        (seq
          (concat
            (clojure.core/list 'clojure.core/let)
            (clojure.core/list
              (apply
                vector
                (seq
                  (concat
                    (clojure.core/list 'f__16176__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/-future-with-channel-impl)
                          (clojure.core/list exec)
                          (-> (with-meta
                                (.withMeta 'fn* {:once true})
                                (apply
                                  hash-map
                                  (seq
                                    (concat (clojure.core/list :once) (clojure.core/list true)))))
                           (clojure.core/list)
                           (concat (clojure.core/list (apply vector (seq (concat)))) body)
                           (seq)
                           (clojure.core/list)))))
                    (clojure.core/list 'ch__16177__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/get-channel)
                          (clojure.core/list 'f__16176__auto__))))))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'datomic.future/add-bounding-warning)
                  (clojure.core/list 'ch__16177__auto__)
                  (clojure.core/list context)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/deref)
                        (clojure.core/list 'datomic.future/bounding-warn-seconds)))))))
            (clojure.core/list 'f__16176__auto__))))))
  (.setMacro #'pfuture)
  (defn pfuture-call
    ([&form &env exec f]
      (let [context (assoc (meta &form) :file *file*)]
        (seq
          (concat
            (clojure.core/list 'clojure.core/let)
            (clojure.core/list
              (apply
                vector
                (seq
                  (concat
                    (clojure.core/list 'f__16179__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/-future-with-channel-impl)
                          (clojure.core/list exec)
                          (clojure.core/list f))))
                    (clojure.core/list 'ch__16180__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/get-channel)
                          (clojure.core/list 'f__16179__auto__))))))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'datomic.future/add-bounding-warning)
                  (clojure.core/list 'ch__16180__auto__)
                  (clojure.core/list context)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/deref)
                        (clojure.core/list 'datomic.future/bounding-warn-seconds)))))))
            (clojure.core/list 'f__16179__auto__))))))
  (.setMacro #'pfuture-call))
