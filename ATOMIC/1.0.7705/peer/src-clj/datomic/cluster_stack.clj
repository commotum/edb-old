(do
  (clojure.core/in-ns 'datomic.cluster-stack)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.cluster-stack)
    {:doc
     "Composes immutable value storage with process-local cache tiers. Near-store misses fall through to durable cluster storage, successful reads repair nearer tiers, and storage failures are normalized as anomaly maps."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :as 'a :refer (clojure.core/list 'go '<!)]
        ['cognitect.anomalies :as 'anom]
        ['datomic.cache :as 'cache]
        ['datomic.cluster :as 'cluster]
        ['datomic.combined-cluster :as 'combined-cluster]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.core2.anomalizer :as 'izer]
        ['datomic.core2.val-store.double-store :as 'double-store]
        ['datomic.core2.val-store.spi :as 'val-store-spi]
        ['datomic.future :as 'df]
        ['datomic.val-cluster :as 'val-cluster])))
  (when-not (.equals 'datomic.cluster-stack 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cluster-stack))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :as 'a :refer (clojure.core/list 'go '<!)]
          ['cognitect.anomalies :as 'anom]
          ['datomic.cache :as 'cache]
          ['datomic.cluster :as 'cluster]
          ['datomic.combined-cluster :as 'combined-cluster]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.core2.anomalizer :as 'izer]
          ['datomic.core2.val-store.double-store :as 'double-store]
          ['datomic.core2.val-store.spi :as 'val-store-spi]
          ['datomic.future :as 'df]
          ['datomic.val-cluster :as 'val-cluster]))))
  (set! *warn-on-reflection* true)
  (.setMeta (clojure.lang.RT/var "datomic.cluster-stack" "pool-ref") {:column (int 1)})
  (let [v__6812__auto__ #'pool-ref]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.cluster-stack" "pool-ref") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.cluster-stack" "pool-ref")
        (delay (common/cached-thread-pool {:name "KVCache"})))
      #'pool-ref))
  ;; Adapts the synchronous external-cache interface to the asynchronous value-store SPI.
  (deftype
    ValStoreOnKvCache
    [exec kv_cache]
    datomic.core2.val_store.spi.Get
    datomic.core2.val_store.spi.Delete
    datomic.core2.val_store.spi.Put
    (-delete
      [this k opts]
      (let [G__16912 (a/promise-chan)] (a/put! G__16912 {:result :no-op}) G__16912))
    (-get
      [this k opts]
      (df/get-channel
        (df/-future-with-channel-impl
          exec
          (fn fn__16910
            ([]
              (try
                {:val (get kv_cache k)}
                (catch java.lang.Throwable t (izer/throwable->anom t))))))))
    (-put
      [this k v opts]
      (do
        (cache/put kv_cache k (:val v))
        (let [G__16909 (a/promise-chan)] (a/put! G__16909 {:result :unknown}) G__16909))))
  (clojure.core/import 'datomic.cluster_stack.ValStoreOnKvCache)
  (defn ->ValStoreOnKvCache
    ([exec kv_cache] (datomic.cluster_stack.ValStoreOnKvCache. exec kv_cache)))
  (reset-meta!
    #'->ValStoreOnKvCache
    (assoc
      {:arglists (clojure.core/list ['exec 'kv-cache]), :column (int 1)}
      :name
      '->ValStoreOnKvCache
      :ns
      *ns*))
  (defn val-store-on-kv-cache ([exec kv_cache] (->ValStoreOnKvCache exec kv_cache)))
  (reset-meta!
    #'val-store-on-kv-cache
    (assoc
      {:arglists (clojure.core/list ['exec 'kv-cache]), :column (int 1)}
      :name
      'val-store-on-kv-cache
      :ns
      *ns*))
  ;; Normalizes unexpected cluster results and throwables to value-store anomaly maps.
  (defn result->anom
    ([result]
      (cond
        (:cognitect.anomalies/category result) result
        (instance? java.lang.Throwable result) (izer/throwable->anom result)
        :else (do
                {:cognitect.anomalies/category :cognitect.anomalies/fault,
                 :datomic.cluster-stack/cluster-result result}))))
  (reset-meta!
    #'result->anom
    (assoc
      {:arglists (clojure.core/list ['result]), :column (int 1)}
      :name
      'result->anom
      :ns
      *ns*))
  (deftype
    ValStoreOnCluster
    [cluster]
    datomic.core2.val_store.spi.Get
    datomic.core2.val_store.spi.Delete
    datomic.core2.val_store.spi.Put
    (-get
      [this k opts]
      (let [ch (df/get-channel (cluster/get-val cluster k))
            c__5899__auto__ (a/chan 1)
            captured_bindings__5900__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__17045
            ([]
              (let [G__17023 (fn G__17023 ([] cluster))
                    G__17024 (fn G__17024 ([] this))
                    G__17025 (fn G__17025 ([] k))
                    G__17026 (fn G__17026 ([] ch))
                    G__17027 (fn G__17027 ([] opts))
                    f__5901__auto__ (fn state_machine__5804__auto__
                                      ([]
                                        (let [statearr_17057 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 11))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_17057
                                            0
                                            state_machine__5804__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_17057
                                            1
                                            1)
                                          statearr_17057))
                                      ([state_17044]
                                        (let [old_frame__5805__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__5806__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_17044
                                                                                3))
                                                                            (loop
                                                                              []
                                                                              (let
                                                                                [result__5807__auto__
                                                                                 (let
                                                                                   [G__17059
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_17044
                                                                                        1))]
                                                                                   (case
                                                                                     G__17059
                                                                                     1
                                                                                     (let
                                                                                       [inst_17037
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_17044
                                                                                          6)
                                                                                        inst_17029
                                                                                        (^clojure.lang.IFn G__17023)
                                                                                        cluster
                                                                                        inst_17029
                                                                                        inst_17030
                                                                                        (^clojure.lang.IFn G__17024)
                                                                                        cluster
                                                                                        inst_17029
                                                                                        _
                                                                                        inst_17030
                                                                                        inst_17031
                                                                                        (^clojure.lang.IFn G__17025)
                                                                                        cluster
                                                                                        inst_17029
                                                                                        _
                                                                                        inst_17030
                                                                                        k
                                                                                        inst_17031
                                                                                        inst_17032
                                                                                        (^clojure.lang.IFn G__17026)
                                                                                        cluster
                                                                                        inst_17029
                                                                                        _
                                                                                        inst_17030
                                                                                        k
                                                                                        inst_17031
                                                                                        ch
                                                                                        inst_17032
                                                                                        inst_17033
                                                                                        (^clojure.lang.IFn G__17027)
                                                                                        inst_17034
                                                                                        inst_17029
                                                                                        inst_17035
                                                                                        inst_17030
                                                                                        inst_17036
                                                                                        inst_17031
                                                                                        inst_17037
                                                                                        inst_17032
                                                                                        inst_17038
                                                                                        inst_17033
                                                                                        state_17044
                                                                                        (let
                                                                                          [statearr_17060
                                                                                           state_17044]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17060
                                                                                            7
                                                                                            inst_17034)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17060
                                                                                            8
                                                                                            inst_17035)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17060
                                                                                            9
                                                                                            inst_17036)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17060
                                                                                            6
                                                                                            inst_17037)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17060
                                                                                            10
                                                                                            inst_17038)
                                                                                          statearr_17060)]
                                                                                       (clojure.core.async.impl.ioc-macros/take!
                                                                                         state_17044
                                                                                         2
                                                                                         inst_17037))
                                                                                     2
                                                                                     (let
                                                                                       [inst_17034
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_17044
                                                                                          7)
                                                                                        inst_17035
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_17044
                                                                                          8)
                                                                                        inst_17036
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_17044
                                                                                          9)
                                                                                        inst_17037
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_17044
                                                                                          6)
                                                                                        inst_17038
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_17044
                                                                                          10)
                                                                                        inst_17040
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_17044
                                                                                          2)
                                                                                        inst_17041
                                                                                        inst_17040
                                                                                        cluster
                                                                                        inst_17034
                                                                                        _
                                                                                        inst_17035
                                                                                        k
                                                                                        inst_17036
                                                                                        ch
                                                                                        inst_17037
                                                                                        result
                                                                                        inst_17041
                                                                                        opts
                                                                                        inst_17038
                                                                                        inst_17042
                                                                                        (cond
                                                                                          (=
                                                                                            :datomic.future/nil
                                                                                            result)
                                                                                          {:val
                                                                                           nil}
                                                                                          (:buf
                                                                                            result)
                                                                                          {:val
                                                                                           (:buf
                                                                                             result)}
                                                                                          :else
                                                                                          (do
                                                                                            (result->anom
                                                                                              result)))]
                                                                                       (clojure.core.async.impl.ioc-macros/return-chan
                                                                                         state_17044
                                                                                         inst_17042))))]
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
                                                                                [statearr_17061
                                                                                 state_17044]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_17061
                                                                                  2
                                                                                  ex__5808__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_17044
                                                                                    4))
                                                                                (let
                                                                                  [statearr_17062
                                                                                   state_17044]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_17062
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_17044
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__5808__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_17044
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__5805__auto__))))]
                                          (if (identical? ret_value__5806__auto__ :recur)
                                            (recur state_17044)
                                            ret_value__5806__auto__))))
                    state__5902__auto__ (let [statearr_17068 (^clojure.lang.IFn f__5901__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_17068
                                            5
                                            c__5899__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_17068
                                            3
                                            captured_bindings__5900__auto__)
                                          statearr_17068)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__5902__auto__)))))
        c__5899__auto__))
    (-delete
      [this k opts]
      (let [ch (df/get-channel (cluster/delete cluster k))
            c__5899__auto__ (a/chan 1)
            captured_bindings__5900__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__16996
            ([]
              (let [G__16974 (fn G__16974 ([] cluster))
                    G__16975 (fn G__16975 ([] this))
                    G__16976 (fn G__16976 ([] k))
                    G__16977 (fn G__16977 ([] ch))
                    G__16978 (fn G__16978 ([] opts))
                    f__5901__auto__ (fn state_machine__5804__auto__
                                      ([]
                                        (let [statearr_17008 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 11))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_17008
                                            0
                                            state_machine__5804__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_17008
                                            1
                                            1)
                                          statearr_17008))
                                      ([state_16995]
                                        (let [old_frame__5805__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__5806__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_16995
                                                                                3))
                                                                            (loop
                                                                              []
                                                                              (let
                                                                                [result__5807__auto__
                                                                                 (let
                                                                                   [G__17010
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16995
                                                                                        1))]
                                                                                   (case
                                                                                     G__17010
                                                                                     1
                                                                                     (let
                                                                                       [inst_16988
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16995
                                                                                          6)
                                                                                        inst_16980
                                                                                        (^clojure.lang.IFn G__16974)
                                                                                        cluster
                                                                                        inst_16980
                                                                                        inst_16981
                                                                                        (^clojure.lang.IFn G__16975)
                                                                                        cluster
                                                                                        inst_16980
                                                                                        _
                                                                                        inst_16981
                                                                                        inst_16982
                                                                                        (^clojure.lang.IFn G__16976)
                                                                                        cluster
                                                                                        inst_16980
                                                                                        _
                                                                                        inst_16981
                                                                                        k
                                                                                        inst_16982
                                                                                        inst_16983
                                                                                        (^clojure.lang.IFn G__16977)
                                                                                        cluster
                                                                                        inst_16980
                                                                                        _
                                                                                        inst_16981
                                                                                        k
                                                                                        inst_16982
                                                                                        ch
                                                                                        inst_16983
                                                                                        inst_16984
                                                                                        (^clojure.lang.IFn G__16978)
                                                                                        inst_16985
                                                                                        inst_16980
                                                                                        inst_16986
                                                                                        inst_16981
                                                                                        inst_16987
                                                                                        inst_16982
                                                                                        inst_16988
                                                                                        inst_16983
                                                                                        inst_16989
                                                                                        inst_16984
                                                                                        state_16995
                                                                                        (let
                                                                                          [statearr_17011
                                                                                           state_16995]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17011
                                                                                            7
                                                                                            inst_16985)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17011
                                                                                            8
                                                                                            inst_16986)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17011
                                                                                            9
                                                                                            inst_16987)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17011
                                                                                            6
                                                                                            inst_16988)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_17011
                                                                                            10
                                                                                            inst_16989)
                                                                                          statearr_17011)]
                                                                                       (clojure.core.async.impl.ioc-macros/take!
                                                                                         state_16995
                                                                                         2
                                                                                         inst_16988))
                                                                                     2
                                                                                     (let
                                                                                       [inst_16985
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16995
                                                                                          7)
                                                                                        inst_16986
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16995
                                                                                          8)
                                                                                        inst_16987
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16995
                                                                                          9)
                                                                                        inst_16988
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16995
                                                                                          6)
                                                                                        inst_16989
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16995
                                                                                          10)
                                                                                        inst_16991
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16995
                                                                                          2)
                                                                                        inst_16992
                                                                                        inst_16991
                                                                                        cluster
                                                                                        inst_16985
                                                                                        _
                                                                                        inst_16986
                                                                                        k
                                                                                        inst_16987
                                                                                        ch
                                                                                        inst_16988
                                                                                        result
                                                                                        inst_16992
                                                                                        opts
                                                                                        inst_16989
                                                                                        inst_16993
                                                                                        (if
                                                                                          (=
                                                                                            :ok
                                                                                            result)
                                                                                          {:result
                                                                                           :deleted}
                                                                                          (result->anom
                                                                                            result))]
                                                                                       (clojure.core.async.impl.ioc-macros/return-chan
                                                                                         state_16995
                                                                                         inst_16993))))]
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
                                                                                [statearr_17012
                                                                                 state_16995]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_17012
                                                                                  2
                                                                                  ex__5808__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_16995
                                                                                    4))
                                                                                (let
                                                                                  [statearr_17013
                                                                                   state_16995]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_17013
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16995
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__5808__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_16995
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__5805__auto__))))]
                                          (if (identical? ret_value__5806__auto__ :recur)
                                            (recur state_16995)
                                            ret_value__5806__auto__))))
                    state__5902__auto__ (let [statearr_17019 (^clojure.lang.IFn f__5901__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_17019
                                            5
                                            c__5899__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_17019
                                            3
                                            captured_bindings__5900__auto__)
                                          statearr_17019)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__5902__auto__)))))
        c__5899__auto__))
    (-put
      [this k v opts]
      (let [ch (df/get-channel (cluster/create-val cluster k (:val v)))
            c__5899__auto__ (a/chan 1)
            captured_bindings__5900__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__16945
            ([]
              (let [G__16920 (fn G__16920 ([] v))
                    G__16921 (fn G__16921 ([] cluster))
                    G__16922 (fn G__16922 ([] this))
                    G__16923 (fn G__16923 ([] k))
                    G__16924 (fn G__16924 ([] ch))
                    G__16925 (fn G__16925 ([] opts))
                    f__5901__auto__ (fn state_machine__5804__auto__
                                      ([]
                                        (let [statearr_16959 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 12))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16959
                                            0
                                            state_machine__5804__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16959
                                            1
                                            1)
                                          statearr_16959))
                                      ([state_16944]
                                        (let [old_frame__5805__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__5806__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_16944
                                                                                3))
                                                                            (loop
                                                                              []
                                                                              (let
                                                                                [result__5807__auto__
                                                                                 (let
                                                                                   [G__16961
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16944
                                                                                        1))]
                                                                                   (case
                                                                                     G__16961
                                                                                     1
                                                                                     (let
                                                                                       [inst_16937
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16944
                                                                                          6)
                                                                                        inst_16927
                                                                                        (^clojure.lang.IFn G__16920)
                                                                                        v
                                                                                        inst_16927
                                                                                        inst_16928
                                                                                        (^clojure.lang.IFn G__16921)
                                                                                        v
                                                                                        inst_16927
                                                                                        cluster
                                                                                        inst_16928
                                                                                        inst_16929
                                                                                        (^clojure.lang.IFn G__16922)
                                                                                        v
                                                                                        inst_16927
                                                                                        cluster
                                                                                        inst_16928
                                                                                        _
                                                                                        inst_16929
                                                                                        inst_16930
                                                                                        (^clojure.lang.IFn G__16923)
                                                                                        v
                                                                                        inst_16927
                                                                                        cluster
                                                                                        inst_16928
                                                                                        _
                                                                                        inst_16929
                                                                                        k
                                                                                        inst_16930
                                                                                        inst_16931
                                                                                        (^clojure.lang.IFn G__16924)
                                                                                        v
                                                                                        inst_16927
                                                                                        cluster
                                                                                        inst_16928
                                                                                        _
                                                                                        inst_16929
                                                                                        k
                                                                                        inst_16930
                                                                                        ch
                                                                                        inst_16931
                                                                                        inst_16932
                                                                                        (^clojure.lang.IFn G__16925)
                                                                                        inst_16933
                                                                                        inst_16927
                                                                                        inst_16934
                                                                                        inst_16928
                                                                                        inst_16935
                                                                                        inst_16929
                                                                                        inst_16936
                                                                                        inst_16930
                                                                                        inst_16937
                                                                                        inst_16931
                                                                                        inst_16938
                                                                                        inst_16932
                                                                                        state_16944
                                                                                        (let
                                                                                          [statearr_16962
                                                                                           state_16944]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16962
                                                                                            7
                                                                                            inst_16933)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16962
                                                                                            8
                                                                                            inst_16934)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16962
                                                                                            9
                                                                                            inst_16935)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16962
                                                                                            10
                                                                                            inst_16936)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16962
                                                                                            6
                                                                                            inst_16937)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16962
                                                                                            11
                                                                                            inst_16938)
                                                                                          statearr_16962)]
                                                                                       (clojure.core.async.impl.ioc-macros/take!
                                                                                         state_16944
                                                                                         2
                                                                                         inst_16937))
                                                                                     2
                                                                                     (let
                                                                                       [inst_16933
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16944
                                                                                          7)
                                                                                        inst_16934
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16944
                                                                                          8)
                                                                                        inst_16935
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16944
                                                                                          9)
                                                                                        inst_16936
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16944
                                                                                          10)
                                                                                        inst_16937
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16944
                                                                                          6)
                                                                                        inst_16938
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16944
                                                                                          11)
                                                                                        inst_16940
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16944
                                                                                          2)
                                                                                        inst_16941
                                                                                        inst_16940
                                                                                        v
                                                                                        inst_16933
                                                                                        cluster
                                                                                        inst_16934
                                                                                        _
                                                                                        inst_16935
                                                                                        k
                                                                                        inst_16936
                                                                                        ch
                                                                                        inst_16937
                                                                                        result
                                                                                        inst_16941
                                                                                        opts
                                                                                        inst_16938
                                                                                        inst_16942
                                                                                        (if
                                                                                          (=
                                                                                            result
                                                                                            :created)
                                                                                          {:result
                                                                                           :created}
                                                                                          (result->anom
                                                                                            result))]
                                                                                       (clojure.core.async.impl.ioc-macros/return-chan
                                                                                         state_16944
                                                                                         inst_16942))))]
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
                                                                                [statearr_16963
                                                                                 state_16944]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_16963
                                                                                  2
                                                                                  ex__5808__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_16944
                                                                                    4))
                                                                                (let
                                                                                  [statearr_16964
                                                                                   state_16944]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_16964
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16944
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__5808__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_16944
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__5805__auto__))))]
                                          (if (identical? ret_value__5806__auto__ :recur)
                                            (recur state_16944)
                                            ret_value__5806__auto__))))
                    state__5902__auto__ (let [statearr_16970 (^clojure.lang.IFn f__5901__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16970
                                            5
                                            c__5899__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16970
                                            3
                                            captured_bindings__5900__auto__)
                                          statearr_16970)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__5902__auto__)))))
        c__5899__auto__)))
  (clojure.core/import 'datomic.cluster_stack.ValStoreOnCluster)
  (defn ->ValStoreOnCluster ([cluster] (datomic.cluster_stack.ValStoreOnCluster. cluster)))
  (reset-meta!
    #'->ValStoreOnCluster
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      '->ValStoreOnCluster
      :ns
      *ns*))
  (defn val-store-on-cluster ([cluster] (->ValStoreOnCluster cluster)))
  (reset-meta!
    #'val-store-on-cluster
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      'val-store-on-cluster
      :ns
      *ns*))
  (deftype
    ValStoreWithClose
    [store close]
    datomic.core2.val_store.spi.Get
    datomic.core2.val_store.spi.Delete
    java.lang.AutoCloseable
    datomic.core2.val_store.spi.Put
    (-delete [this k opts] (val-store-spi/-delete store k opts))
    (-get [this k opts] (val-store-spi/-get store k opts))
    (-put [this k v opts] (val-store-spi/-put store k v opts))
    (^void close [this] (do (^clojure.lang.IFn close) nil)))
  (clojure.core/import 'datomic.cluster_stack.ValStoreWithClose)
  (defn ->ValStoreWithClose ([store close] (datomic.cluster_stack.ValStoreWithClose. store close)))
  (reset-meta!
    #'->ValStoreWithClose
    (assoc
      {:arglists (clojure.core/list ['store 'close]), :column (int 1)}
      :name
      '->ValStoreWithClose
      :ns
      *ns*))
  (defn val-store-with-close ([store close] (->ValStoreWithClose store close)))
  (reset-meta!
    #'val-store-with-close
    (assoc
      {:arglists (clojure.core/list ['store 'close]), :column (int 1)}
      :name
      'val-store-with-close
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cluster-stack" "kv-cache-ref") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cluster-stack" "kv-cache-ref")
    (let [G__17089 (atom nil)]
      (add-watch G__17089 :datomic.cluster-stack/closer common/closing-watch)
      G__17089))
  ;; Starts configured Memcached and Valcache tiers nearest-first and installs one closeable stack.
  ;; Any subset of tiers may be present; reads repair a nearer tier after a farther-tier hit.
  (defn start-kv-cache
    ([]
      (let [wrap (fn wrap ([x] (when x (val-store-on-kv-cache (deref pool-ref) x))))
            combine (fn combine
                      ([c2 p__17094]
                        (let [vec__17096 p__17094
                              c1 (nth vec__17096 (int 0) nil)
                              metric (nth vec__17096 (int 1) nil)
                              fallback_msec (nth vec__17096 (int 2) nil)]
                          (if (and c1 c2)
                            (double-store/create
                              {:near-store (^clojure.lang.IFn wrap c1),
                               :far-store c2,
                               :repair-metric metric,
                               :get-fallback-msec fallback_msec})
                            (or (^clojure.lang.IFn wrap c1) c2)))))
            memcached ((common/requiring-resolve! 'datomic.memcached/start-memcached-from-config))
            valcache_args (config/valcache-args)
            valcache (when valcache_args
                       ((common/requiring-resolve! 'datomic.valcache-direct/create) valcache_args))
            local_memcached ((common/requiring-resolve!
                               'datomic.memcached/start-local-memcached-from-config))
            stack (reduce
                    combine
                    nil
                    [[memcached :mc.repair 5]
                     [valcache :vc.repair 5]
                     [local_memcached :lmc.repair 5]])
            close (fn close
                    ([]
                      (loop [seq_17103 (seq [memcached valcache local_memcached])
                             chunk_17104 nil
                             count_17105 0
                             i_17106 0]
                        (if (< i_17106 count_17105)
                          (let [cache (.nth ^clojure.lang.Indexed chunk_17104 (int i_17106))]
                            (when cache (.close ^java.lang.AutoCloseable cache))
                            (recur seq_17103 chunk_17104 count_17105 (inc i_17106)))
                          (let [temp__5804__auto__ (seq seq_17103)]
                            (when temp__5804__auto__
                              (let [seq_17103 temp__5804__auto__]
                                (if (chunked-seq? seq_17103)
                                  (let [c__6065__auto__ (chunk-first seq_17103)]
                                    (recur
                                      (chunk-rest seq_17103)
                                      c__6065__auto__
                                      (int (count c__6065__auto__))
                                      (int 0)))
                                  (let [cache (first seq_17103)]
                                    (when cache (.close ^java.lang.AutoCloseable cache))
                                    (recur (next seq_17103) nil 0 0))))))))))
            stack (some-> stack (val-store-with-close close))]
        (reset! kv-cache-ref stack))))
  (reset-meta!
    #'start-kv-cache
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'start-kv-cache :ns *ns*))
  ;; Wraps durable cluster values with an optional near cache while preserving cluster ref and pod operations.
  (defn cluster-with-cache
    ([cluster cache opts]
      (let [cluster_store (val-store-on-cluster cluster)
            caching_store (double-store/create
                            (merge
                              {:repair-metric :kvc.repair}
                              opts
                              {:near-store cache, :far-store cluster_store}))
            val_cluster (val-cluster/val-cluster caching_store)]
        (combined-cluster/combined-cluster cluster val_cluster)))
    ([cluster cache] (cluster-with-cache cluster cache nil)))
  (reset-meta!
    #'cluster-with-cache
    (assoc
      {:arglists (clojure.core/list ['cluster 'cache] ['cluster 'cache 'opts]), :column (int 1)}
      :name
      'cluster-with-cache
      :ns
      *ns*)))
