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
       (let [vec__10032 (filling-promise ((deref #'clojure.core/binding-conveyor-fn) f))
             f (nth vec__10032 (int 0) nil)
             ch (nth vec__10032 (int 1) nil)
             fut (.submit
                   ^java.util.concurrent.ExecutorService exec
                   ^java.util.concurrent.Callable f)]
         (->JavaFutureWithChannel fut ch)))
     ([f]
       (let [vec__10029 (filling-promise f)
             f (nth vec__10029 (int 0) nil)
             ch (nth vec__10029 (int 1) nil)
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
       (let [c__6135__auto__ (a/chan 1)
             captured_bindings__6136__auto__ (clojure.lang.Var/getThreadBindingFrame)]
         (clojure.core.async.impl.dispatch/run
           (fn fn__10078
             ([]
               (let [G__10036 (fn G__10036 ([] promise_ch))
                     G__10037 (fn G__10037 ([] context))
                     G__10038 (fn G__10038 ([] seconds))
                     f__6137__auto__ (fn state_machine__6040__auto__
                                       ([]
                                         (let [statearr_10086 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 12))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_10086
                                             0
                                             state_machine__6040__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_10086
                                             1
                                             1)
                                           statearr_10086))
                                       ([state_10077]
                                         (let [old_frame__6041__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__6042__auto__ (try
                                                                         (try
                                                                           (do
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               (clojure.core.async.impl.ioc-macros/aget-object
                                                                                 state_10077
                                                                                 3))
                                                                             (loop 
                                                                               []
                                                                               (let 
                                                                                 [result__6043__auto__
                                                                                  (let 
                                                                                    [G__10088
                                                                                     (int
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_10077
                                                                                         1))]
                                                                                    (case
                                                                                      G__10088
                                                                                      8
                                                                                      (do
                                                                                        (let 
                                                                                          [statearr_10098
                                                                                           state_10077]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10098
                                                                                            2
                                                                                            nil)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10098
                                                                                            1
                                                                                            10))
                                                                                        :recur)
                                                                                      1
                                                                                      (let 
                                                                                        [inst_10044
                                                                                         (^clojure.lang.IFn G__10036)
                                                                                         promise_ch
                                                                                         inst_10044
                                                                                         inst_10045
                                                                                         (^clojure.lang.IFn G__10037)
                                                                                         promise_ch
                                                                                         inst_10044
                                                                                         context
                                                                                         inst_10045
                                                                                         inst_10046
                                                                                         (^clojure.lang.IFn G__10038)
                                                                                         inst_10047
                                                                                         inst_10044
                                                                                         inst_10048
                                                                                         inst_10045
                                                                                         inst_10049
                                                                                         inst_10046
                                                                                         inst_10050
                                                                                         0
                                                                                         state_10077
                                                                                         (let 
                                                                                           [statearr_10089
                                                                                            state_10077]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_10089
                                                                                             6
                                                                                             inst_10047)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_10089
                                                                                             9
                                                                                             inst_10048)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_10089
                                                                                             10
                                                                                             inst_10049)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_10089
                                                                                             11
                                                                                             (long
                                                                                               inst_10050))
                                                                                           statearr_10089)]
                                                                                        (let 
                                                                                          [statearr_10090
                                                                                           state_10077]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10090
                                                                                            2
                                                                                            nil)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10090
                                                                                            1
                                                                                            2))
                                                                                        :recur)
                                                                                      6
                                                                                      (let 
                                                                                        [inst_10073
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           2)]
                                                                                        (let 
                                                                                          [statearr_10094
                                                                                           state_10077]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10094
                                                                                            2
                                                                                            inst_10073)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10094
                                                                                            1
                                                                                            3))
                                                                                        :recur)
                                                                                      3
                                                                                      (let 
                                                                                        [inst_10075
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           2)]
                                                                                        (clojure.core.async.impl.ioc-macros/return-chan
                                                                                          state_10077
                                                                                          inst_10075))
                                                                                      2
                                                                                      (let 
                                                                                        [inst_10050
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           11)
                                                                                         inst_10049
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           10)
                                                                                         inst_10052
                                                                                         (=
                                                                                           inst_10050
                                                                                           inst_10049)]
                                                                                        (if
                                                                                          inst_10052
                                                                                          (let 
                                                                                            [statearr_10091
                                                                                             state_10077]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_10091
                                                                                              1
                                                                                              4))
                                                                                          (let 
                                                                                            [statearr_10092
                                                                                             state_10077]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_10092
                                                                                              1
                                                                                              5)))
                                                                                        :recur)
                                                                                      9
                                                                                      (let 
                                                                                        [inst_10050
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           11)
                                                                                         inst_10068
                                                                                         (inc
                                                                                           inst_10050)
                                                                                         inst_10050
                                                                                         inst_10068
                                                                                         state_10077
                                                                                         (let 
                                                                                           [statearr_10099
                                                                                            state_10077]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_10099
                                                                                             11
                                                                                             inst_10050)
                                                                                           statearr_10099)]
                                                                                        (let 
                                                                                          [statearr_10100
                                                                                           state_10077]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10100
                                                                                            2
                                                                                            nil)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10100
                                                                                            1
                                                                                            2))
                                                                                        :recur)
                                                                                      10
                                                                                      (let 
                                                                                        [inst_10071
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           2)]
                                                                                        (let 
                                                                                          [statearr_10101
                                                                                           state_10077]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10101
                                                                                            2
                                                                                            inst_10071)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10101
                                                                                            1
                                                                                            6))
                                                                                        :recur)
                                                                                      5
                                                                                      (let 
                                                                                        [inst_10047
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           6)
                                                                                         inst_10056
                                                                                         (a/timeout
                                                                                           1000)
                                                                                         inst_10057
                                                                                         (vector
                                                                                           inst_10047
                                                                                           inst_10056)]
                                                                                        (a/ioc-alts!
                                                                                          state_10077
                                                                                          7
                                                                                          inst_10057))
                                                                                      4
                                                                                      (let 
                                                                                        [inst_10047
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           6)
                                                                                         inst_10048
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           9)
                                                                                         inst_10049
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           10)
                                                                                         inst_10050
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           11)
                                                                                         promise_ch
                                                                                         inst_10047
                                                                                         context
                                                                                         inst_10048
                                                                                         seconds
                                                                                         inst_10049
                                                                                         n
                                                                                         inst_10050]
                                                                                        (monitor/add-stat
                                                                                          :FutureBoundExceeded
                                                                                          1)
                                                                                        (let 
                                                                                          [inst_10054
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
                                                                                            [statearr_10093
                                                                                             state_10077]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_10093
                                                                                              2
                                                                                              inst_10054)
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_10093
                                                                                              1
                                                                                              6))
                                                                                          :recur))
                                                                                      7
                                                                                      (let 
                                                                                        [inst_10047
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           6)
                                                                                         inst_10059
                                                                                         (clojure.core.async.impl.ioc-macros/aget-object
                                                                                           state_10077
                                                                                           2)
                                                                                         inst_10060
                                                                                         (nth
                                                                                           inst_10059
                                                                                           (int 0)
                                                                                           nil)
                                                                                         inst_10061
                                                                                         (nth
                                                                                           inst_10059
                                                                                           (int 1)
                                                                                           nil)
                                                                                         inst_10062
                                                                                         inst_10059
                                                                                         inst_10063
                                                                                         inst_10060
                                                                                         inst_10064
                                                                                         inst_10061
                                                                                         inst_10065
                                                                                         (=
                                                                                           inst_10064
                                                                                           inst_10047)
                                                                                         state_10077
                                                                                         (let 
                                                                                           [statearr_10095
                                                                                            state_10077]
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_10095
                                                                                             7
                                                                                             inst_10062)
                                                                                           (clojure.core.async.impl.ioc-macros/aset-object
                                                                                             statearr_10095
                                                                                             8
                                                                                             inst_10063)
                                                                                           statearr_10095)]
                                                                                        (if
                                                                                          inst_10065
                                                                                          (let 
                                                                                            [statearr_10096
                                                                                             state_10077]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_10096
                                                                                              1
                                                                                              8))
                                                                                          (let 
                                                                                            [statearr_10097
                                                                                             state_10077]
                                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                                              statearr_10097
                                                                                              1
                                                                                              9)))
                                                                                        :recur)))]
                                                                                 (if
                                                                                   (identical?
                                                                                     result__6043__auto__
                                                                                     :recur)
                                                                                   (recur)
                                                                                   result__6043__auto__))))
                                                                           (catch
                                                                             java.lang.Throwable
                                                                             ex__6044__auto__
                                                                             (do
                                                                               (let 
                                                                                 [statearr_10102
                                                                                  state_10077]
                                                                                 (clojure.core.async.impl.ioc-macros/aset-object
                                                                                   statearr_10102
                                                                                   2
                                                                                   ex__6044__auto__))
                                                                               (if
                                                                                 (seq
                                                                                   (clojure.core.async.impl.ioc-macros/aget-object
                                                                                     state_10077
                                                                                     4))
                                                                                 (let 
                                                                                   [statearr_10103
                                                                                    state_10077]
                                                                                   (clojure.core.async.impl.ioc-macros/aset-object
                                                                                     statearr_10103
                                                                                     1
                                                                                     (first
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_10077
                                                                                         4))))
                                                                                 (throw
                                                                                   ^java.lang.Throwable ex__6044__auto__))
                                                                               :recur)))
                                                                         (finally
                                                                           (do
                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                               state_10077
                                                                               3
                                                                               (clojure.lang.Var/getThreadBindingFrame))
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               old_frame__6041__auto__))))]
                                           (if (identical? ret_value__6042__auto__ :recur)
                                             (recur state_10077)
                                             ret_value__6042__auto__))))
                     state__6138__auto__ (let [statearr_10109 (^clojure.lang.IFn f__6137__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_10109
                                             5
                                             c__6135__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_10109
                                             3
                                             captured_bindings__6136__auto__)
                                           statearr_10109)]
                 (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                   state__6138__auto__)))))
         c__6135__auto__))))
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