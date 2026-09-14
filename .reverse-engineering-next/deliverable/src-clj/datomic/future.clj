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
            f (fn f
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
      (let [vec__10177 (filling-promise ((deref #'clojure.core/binding-conveyor-fn) f))
            f (nth vec__10177 (int 0) nil)
            ch (nth vec__10177 (int 1) nil)
            fut (.submit
                  ^java.util.concurrent.ExecutorService exec
                  ^java.util.concurrent.Callable f)]
        (->JavaFutureWithChannel fut ch)))
    ([f]
      (let [vec__10174 (filling-promise f)
            f (nth vec__10174 (int 0) nil)
            ch (nth vec__10174 (int 1) nil)
            fut (clojure.core/future-call f)]
        (->ClojureFutureWithChannel fut ch))))
  (defn add-bounding-warning
    ([promise_ch context seconds]
      (let [c__6597__auto__ (a/chan 1)
            captured_bindings__6598__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__10223
            ([]
              (let [G__10181 (fn G__10181 ([] promise_ch))
                    G__10182 (fn G__10182 ([] context))
                    G__10183 (fn G__10183 ([] seconds))
                    f__6599__auto__ (fn state_machine__6360__auto__
                                      ([]
                                        (let [statearr_10231 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 12))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10231
                                            0
                                            state_machine__6360__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10231
                                            1
                                            1)
                                          statearr_10231))
                                      ([state_10222]
                                        (let [old_frame__6361__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__6362__auto__ (try
                                                                        (do
                                                                          (clojure.lang.Var/resetThreadBindingFrame
                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                              state_10222
                                                                              3))
                                                                          (loop 
                                                                            []
                                                                            (let 
                                                                              [result__6363__auto__
                                                                               (let 
                                                                                 [G__10233
                                                                                  (int
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_10222
                                                                                      1))]
                                                                                 (case
                                                                                   G__10233
                                                                                   6
                                                                                   (let 
                                                                                     [inst_10218
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        2)]
                                                                                     (let 
                                                                                       [statearr_10239
                                                                                        state_10222]
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10239
                                                                                         2
                                                                                         inst_10218)
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10239
                                                                                         1
                                                                                         3))
                                                                                     :recur)
                                                                                   8
                                                                                   (do
                                                                                     (let 
                                                                                       [statearr_10243
                                                                                        state_10222]
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10243
                                                                                         2
                                                                                         nil)
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10243
                                                                                         1
                                                                                         10))
                                                                                     :recur)
                                                                                   3
                                                                                   (let 
                                                                                     [inst_10220
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        2)]
                                                                                     (clojure.core.async.impl.ioc-macros/return-chan
                                                                                       state_10222
                                                                                       inst_10220))
                                                                                   9
                                                                                   (let 
                                                                                     [inst_10195
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        11)
                                                                                      inst_10213
                                                                                      (inc
                                                                                        inst_10195)
                                                                                      inst_10195
                                                                                      inst_10213
                                                                                      state_10222
                                                                                      (let 
                                                                                        [statearr_10244
                                                                                         state_10222]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_10244
                                                                                          11
                                                                                          inst_10195)
                                                                                        statearr_10244)]
                                                                                     (let 
                                                                                       [statearr_10245
                                                                                        state_10222]
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10245
                                                                                         2
                                                                                         nil)
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10245
                                                                                         1
                                                                                         2))
                                                                                     :recur)
                                                                                   5
                                                                                   (let 
                                                                                     [inst_10192
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        6)
                                                                                      inst_10201
                                                                                      (a/timeout
                                                                                        1000)
                                                                                      inst_10202
                                                                                      (vector
                                                                                        inst_10192
                                                                                        inst_10201)]
                                                                                     (a/ioc-alts!
                                                                                       state_10222
                                                                                       7
                                                                                       inst_10202))
                                                                                   7
                                                                                   (let 
                                                                                     [inst_10192
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        6)
                                                                                      inst_10204
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        2)
                                                                                      inst_10205
                                                                                      (nth
                                                                                        inst_10204
                                                                                        (int 0)
                                                                                        nil)
                                                                                      inst_10206
                                                                                      (nth
                                                                                        inst_10204
                                                                                        (int 1)
                                                                                        nil)
                                                                                      inst_10207
                                                                                      inst_10204
                                                                                      inst_10208
                                                                                      inst_10205
                                                                                      inst_10209
                                                                                      inst_10206
                                                                                      inst_10210
                                                                                      (=
                                                                                        inst_10209
                                                                                        inst_10192)
                                                                                      state_10222
                                                                                      (let 
                                                                                        [statearr_10240
                                                                                         state_10222]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_10240
                                                                                          7
                                                                                          inst_10207)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_10240
                                                                                          8
                                                                                          inst_10208)
                                                                                        statearr_10240)]
                                                                                     (if
                                                                                       inst_10210
                                                                                       (let 
                                                                                         [statearr_10241
                                                                                          state_10222]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_10241
                                                                                           1
                                                                                           8))
                                                                                       (let 
                                                                                         [statearr_10242
                                                                                          state_10222]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_10242
                                                                                           1
                                                                                           9)))
                                                                                     :recur)
                                                                                   4
                                                                                   (let 
                                                                                     [inst_10192
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        6)
                                                                                      inst_10193
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        9)
                                                                                      inst_10194
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        10)
                                                                                      inst_10195
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        11)
                                                                                      promise_ch
                                                                                      inst_10192
                                                                                      context
                                                                                      inst_10193
                                                                                      seconds
                                                                                      inst_10194
                                                                                      n inst_10195]
                                                                                     (monitor/add-stat
                                                                                       :FutureBoundExceeded
                                                                                       1)
                                                                                     (let 
                                                                                       [inst_10199
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
                                                                                                 context}))
                                                                                            nil)
                                                                                          nil)]
                                                                                       (let 
                                                                                         [statearr_10238
                                                                                          state_10222]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_10238
                                                                                           2
                                                                                           inst_10199)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_10238
                                                                                           1
                                                                                           6))
                                                                                       :recur))
                                                                                   2
                                                                                   (let 
                                                                                     [inst_10195
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        11)
                                                                                      inst_10194
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        10)
                                                                                      inst_10197
                                                                                      (=
                                                                                        inst_10195
                                                                                        inst_10194)]
                                                                                     (if
                                                                                       inst_10197
                                                                                       (let 
                                                                                         [statearr_10236
                                                                                          state_10222]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_10236
                                                                                           1
                                                                                           4))
                                                                                       (let 
                                                                                         [statearr_10237
                                                                                          state_10222]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_10237
                                                                                           1
                                                                                           5)))
                                                                                     :recur)
                                                                                   1
                                                                                   (let 
                                                                                     [inst_10189
                                                                                      (^clojure.lang.IFn G__10181)
                                                                                      promise_ch
                                                                                      inst_10189
                                                                                      inst_10190
                                                                                      (^clojure.lang.IFn G__10182)
                                                                                      promise_ch
                                                                                      inst_10189
                                                                                      context
                                                                                      inst_10190
                                                                                      inst_10191
                                                                                      (^clojure.lang.IFn G__10183)
                                                                                      inst_10192
                                                                                      inst_10189
                                                                                      inst_10193
                                                                                      inst_10190
                                                                                      inst_10194
                                                                                      inst_10191
                                                                                      inst_10195 0
                                                                                      state_10222
                                                                                      (let 
                                                                                        [statearr_10234
                                                                                         state_10222]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_10234
                                                                                          6
                                                                                          inst_10192)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_10234
                                                                                          9
                                                                                          inst_10193)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_10234
                                                                                          10
                                                                                          inst_10194)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_10234
                                                                                          11
                                                                                          (long
                                                                                            inst_10195))
                                                                                        statearr_10234)]
                                                                                     (let 
                                                                                       [statearr_10235
                                                                                        state_10222]
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10235
                                                                                         2
                                                                                         nil)
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10235
                                                                                         1
                                                                                         2))
                                                                                     :recur)
                                                                                   10
                                                                                   (let 
                                                                                     [inst_10216
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10222
                                                                                        2)]
                                                                                     (let 
                                                                                       [statearr_10246
                                                                                        state_10222]
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10246
                                                                                         2
                                                                                         inst_10216)
                                                                                       (clojure.core.async.impl.ioc-macros/aset-object
                                                                                         statearr_10246
                                                                                         1
                                                                                         6))
                                                                                     :recur)))]
                                                                              (if
                                                                                (identical?
                                                                                  result__6363__auto__
                                                                                  :recur)
                                                                                (recur)
                                                                                result__6363__auto__))))
                                                                        (catch
                                                                          java.lang.Throwable
                                                                          ex__6364__auto__
                                                                          (do
                                                                            (let 
                                                                              [statearr_10247
                                                                               state_10222]
                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                statearr_10247
                                                                                2
                                                                                ex__6364__auto__))
                                                                            (if
                                                                              (seq
                                                                                (clojure.core.async.impl.ioc-macros/aget-object
                                                                                  state_10222
                                                                                  4))
                                                                              (let 
                                                                                [statearr_10248
                                                                                 state_10222]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_10248
                                                                                  1
                                                                                  (first
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_10222
                                                                                      4))))
                                                                              (throw
                                                                                ^java.lang.Throwable ex__6364__auto__))
                                                                            :recur))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_10222
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__6361__auto__))))]
                                          (if (identical? ret_value__6362__auto__ :recur)
                                            (recur state_10222)
                                            ret_value__6362__auto__))))
                    state__6600__auto__ (let [statearr_10254 (^clojure.lang.IFn f__6599__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10254
                                            5
                                            c__6597__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10254
                                            3
                                            captured_bindings__6598__auto__)
                                          statearr_10254)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6600__auto__)))))
        c__6597__auto__)))
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
                    (clojure.core/list 'f__10261__auto__)
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
                    (clojure.core/list 'ch__10262__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/get-channel)
                          (clojure.core/list 'f__10261__auto__))))))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'datomic.future/add-bounding-warning)
                  (clojure.core/list 'ch__10262__auto__)
                  (clojure.core/list context)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/deref)
                        (clojure.core/list 'datomic.future/bounding-warn-seconds)))))))
            (clojure.core/list 'f__10261__auto__))))))
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
                    (clojure.core/list 'f__10264__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/-future-with-channel-impl)
                          (clojure.core/list f))))
                    (clojure.core/list 'ch__10265__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/get-channel)
                          (clojure.core/list 'f__10264__auto__))))))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'datomic.future/add-bounding-warning)
                  (clojure.core/list 'ch__10265__auto__)
                  (clojure.core/list context)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/deref)
                        (clojure.core/list 'datomic.future/bounding-warn-seconds)))))))
            (clojure.core/list 'f__10264__auto__))))))
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
                    (clojure.core/list 'f__10267__auto__)
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
                    (clojure.core/list 'ch__10268__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/get-channel)
                          (clojure.core/list 'f__10267__auto__))))))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'datomic.future/add-bounding-warning)
                  (clojure.core/list 'ch__10268__auto__)
                  (clojure.core/list context)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/deref)
                        (clojure.core/list 'datomic.future/bounding-warn-seconds)))))))
            (clojure.core/list 'f__10267__auto__))))))
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
                    (clojure.core/list 'f__10270__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/-future-with-channel-impl)
                          (clojure.core/list exec)
                          (clojure.core/list f))))
                    (clojure.core/list 'ch__10271__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.future/get-channel)
                          (clojure.core/list 'f__10270__auto__))))))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'datomic.future/add-bounding-warning)
                  (clojure.core/list 'ch__10271__auto__)
                  (clojure.core/list context)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/deref)
                        (clojure.core/list 'datomic.future/bounding-warn-seconds)))))))
            (clojure.core/list 'f__10270__auto__))))))
  (.setMacro #'pfuture-call))