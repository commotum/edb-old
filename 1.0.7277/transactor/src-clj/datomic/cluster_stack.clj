(do
  (clojure.core/in-ns 'datomic.cluster-stack)
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
        ['datomic.core2.anomalies :as 'canom]
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
          ['datomic.core2.anomalies :as 'canom]
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
  (deftype
    ValStoreOnKvCache
    [exec kv_cache]
    datomic.core2.val_store.spi.Get
    datomic.core2.val_store.spi.Delete
    datomic.core2.val_store.spi.Put
    (-delete
      [this k opts]
      (let [G__16263 (a/promise-chan)] (a/put! G__16263 {:result :no-op}) G__16263))
    (-get
      [this k opts]
      (df/get-channel
        (let [f__16176__auto__ (df/-future-with-channel-impl
                                 exec
                                 (fn fn__16261
                                   ([]
                                     (try
                                       {:val (get kv_cache k)}
                                       (catch java.lang.Throwable t (canom/fault t))))))
              ch__16177__auto__ (df/get-channel f__16176__auto__)]
          (df/add-bounding-warning
            ch__16177__auto__
            {:line 40, :column 9, :file "datomic/cluster_stack.clj"}
            (deref df/bounding-warn-seconds))
          f__16176__auto__)))
    (-put
      [this k v opts]
      (do
        (cache/put kv_cache k (:val v))
        (let [G__16260 (a/promise-chan)] (a/put! G__16260 {:result :unknown}) G__16260))))
  (clojure.core/import 'datomic.cluster_stack.ValStoreOnKvCache)
  (def ->ValStoreOnKvCache
   (fn __GT_ValStoreOnKvCache
     ([exec kv_cache] (datomic.cluster_stack.ValStoreOnKvCache. exec kv_cache))))
  (reset-meta!
    #'->ValStoreOnKvCache
    (assoc
      {:arglists (clojure.core/list ['exec 'kv-cache]), :column (int 1)}
      :name
      '->ValStoreOnKvCache
      :ns
      *ns*))
  (def val-store-on-kv-cache
   (fn val_store_on_kv_cache ([exec kv_cache] (->ValStoreOnKvCache exec kv_cache))))
  (reset-meta!
    #'val-store-on-kv-cache
    (assoc
      {:arglists (clojure.core/list ['exec 'kv-cache]), :column (int 1)}
      :name
      'val-store-on-kv-cache
      :ns
      *ns*))
  (defn result->anom
    ([result]
      (if (:cognitect.anomalies/category result)
        result
        {:cognitect.anomalies/category :cognitect.anomalies/fault,
         :datomic.cluster-stack/cluster-result result})))
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
            c__6079__auto__ (a/chan 1)
            captured_bindings__6080__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__16398
            ([]
              (let [G__16376 (fn G__16376 ([] cluster))
                    G__16377 (fn G__16377 ([] this))
                    G__16378 (fn G__16378 ([] k))
                    G__16379 (fn G__16379 ([] ch))
                    G__16380 (fn G__16380 ([] opts))
                    f__6081__auto__ (fn state_machine__5842__auto__
                                      ([]
                                        (let [statearr_16410 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 11))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16410
                                            0
                                            state_machine__5842__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16410
                                            1
                                            1)
                                          statearr_16410))
                                      ([state_16397]
                                        (let [old_frame__5843__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__5844__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_16397
                                                                                3))
                                                                            (loop 
                                                                              []
                                                                              (let 
                                                                                [result__5845__auto__
                                                                                 (let 
                                                                                   [G__16412
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16397
                                                                                        1))]
                                                                                   (case
                                                                                     G__16412
                                                                                     1
                                                                                     (let 
                                                                                       [inst_16390
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16397
                                                                                          6)
                                                                                        inst_16382
                                                                                        (^clojure.lang.IFn G__16376)
                                                                                        cluster
                                                                                        inst_16382
                                                                                        inst_16383
                                                                                        (^clojure.lang.IFn G__16377)
                                                                                        cluster
                                                                                        inst_16382
                                                                                        _
                                                                                        inst_16383
                                                                                        inst_16384
                                                                                        (^clojure.lang.IFn G__16378)
                                                                                        cluster
                                                                                        inst_16382
                                                                                        _
                                                                                        inst_16383
                                                                                        k
                                                                                        inst_16384
                                                                                        inst_16385
                                                                                        (^clojure.lang.IFn G__16379)
                                                                                        cluster
                                                                                        inst_16382
                                                                                        _
                                                                                        inst_16383
                                                                                        k
                                                                                        inst_16384
                                                                                        ch
                                                                                        inst_16385
                                                                                        inst_16386
                                                                                        (^clojure.lang.IFn G__16380)
                                                                                        inst_16387
                                                                                        inst_16382
                                                                                        inst_16388
                                                                                        inst_16383
                                                                                        inst_16389
                                                                                        inst_16384
                                                                                        inst_16390
                                                                                        inst_16385
                                                                                        inst_16391
                                                                                        inst_16386
                                                                                        state_16397
                                                                                        (let 
                                                                                          [statearr_16413
                                                                                           state_16397]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16413
                                                                                            7
                                                                                            inst_16387)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16413
                                                                                            8
                                                                                            inst_16388)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16413
                                                                                            9
                                                                                            inst_16389)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16413
                                                                                            6
                                                                                            inst_16390)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16413
                                                                                            10
                                                                                            inst_16391)
                                                                                          statearr_16413)]
                                                                                       (clojure.core.async.impl.ioc-macros/take!
                                                                                         state_16397
                                                                                         2
                                                                                         inst_16390))
                                                                                     2
                                                                                     (let 
                                                                                       [inst_16387
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16397
                                                                                          7)
                                                                                        inst_16388
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16397
                                                                                          8)
                                                                                        inst_16389
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16397
                                                                                          9)
                                                                                        inst_16390
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16397
                                                                                          6)
                                                                                        inst_16391
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16397
                                                                                          10)
                                                                                        inst_16393
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16397
                                                                                          2)
                                                                                        inst_16394
                                                                                        inst_16393
                                                                                        cluster
                                                                                        inst_16387
                                                                                        _
                                                                                        inst_16388
                                                                                        k
                                                                                        inst_16389
                                                                                        ch
                                                                                        inst_16390
                                                                                        result
                                                                                        inst_16394
                                                                                        opts
                                                                                        inst_16391
                                                                                        inst_16395
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
                                                                                         state_16397
                                                                                         inst_16395))))]
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
                                                                                [statearr_16414
                                                                                 state_16397]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_16414
                                                                                  2
                                                                                  ex__5846__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_16397
                                                                                    4))
                                                                                (let 
                                                                                  [statearr_16415
                                                                                   state_16397]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_16415
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16397
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__5846__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_16397
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__5843__auto__))))]
                                          (if (identical? ret_value__5844__auto__ :recur)
                                            (recur state_16397)
                                            ret_value__5844__auto__))))
                    state__6082__auto__ (let [statearr_16421 (^clojure.lang.IFn f__6081__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16421
                                            5
                                            c__6079__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16421
                                            3
                                            captured_bindings__6080__auto__)
                                          statearr_16421)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6082__auto__)))))
        c__6079__auto__))
    (-delete
      [this k opts]
      (let [ch (df/get-channel (cluster/delete cluster k))
            c__6079__auto__ (a/chan 1)
            captured_bindings__6080__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__16349
            ([]
              (let [G__16327 (fn G__16327 ([] cluster))
                    G__16328 (fn G__16328 ([] this))
                    G__16329 (fn G__16329 ([] k))
                    G__16330 (fn G__16330 ([] ch))
                    G__16331 (fn G__16331 ([] opts))
                    f__6081__auto__ (fn state_machine__5842__auto__
                                      ([]
                                        (let [statearr_16361 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 11))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16361
                                            0
                                            state_machine__5842__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16361
                                            1
                                            1)
                                          statearr_16361))
                                      ([state_16348]
                                        (let [old_frame__5843__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__5844__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_16348
                                                                                3))
                                                                            (loop 
                                                                              []
                                                                              (let 
                                                                                [result__5845__auto__
                                                                                 (let 
                                                                                   [G__16363
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16348
                                                                                        1))]
                                                                                   (case
                                                                                     G__16363
                                                                                     1
                                                                                     (let 
                                                                                       [inst_16341
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16348
                                                                                          6)
                                                                                        inst_16333
                                                                                        (^clojure.lang.IFn G__16327)
                                                                                        cluster
                                                                                        inst_16333
                                                                                        inst_16334
                                                                                        (^clojure.lang.IFn G__16328)
                                                                                        cluster
                                                                                        inst_16333
                                                                                        _
                                                                                        inst_16334
                                                                                        inst_16335
                                                                                        (^clojure.lang.IFn G__16329)
                                                                                        cluster
                                                                                        inst_16333
                                                                                        _
                                                                                        inst_16334
                                                                                        k
                                                                                        inst_16335
                                                                                        inst_16336
                                                                                        (^clojure.lang.IFn G__16330)
                                                                                        cluster
                                                                                        inst_16333
                                                                                        _
                                                                                        inst_16334
                                                                                        k
                                                                                        inst_16335
                                                                                        ch
                                                                                        inst_16336
                                                                                        inst_16337
                                                                                        (^clojure.lang.IFn G__16331)
                                                                                        inst_16338
                                                                                        inst_16333
                                                                                        inst_16339
                                                                                        inst_16334
                                                                                        inst_16340
                                                                                        inst_16335
                                                                                        inst_16341
                                                                                        inst_16336
                                                                                        inst_16342
                                                                                        inst_16337
                                                                                        state_16348
                                                                                        (let 
                                                                                          [statearr_16364
                                                                                           state_16348]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16364
                                                                                            7
                                                                                            inst_16338)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16364
                                                                                            8
                                                                                            inst_16339)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16364
                                                                                            9
                                                                                            inst_16340)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16364
                                                                                            6
                                                                                            inst_16341)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16364
                                                                                            10
                                                                                            inst_16342)
                                                                                          statearr_16364)]
                                                                                       (clojure.core.async.impl.ioc-macros/take!
                                                                                         state_16348
                                                                                         2
                                                                                         inst_16341))
                                                                                     2
                                                                                     (let 
                                                                                       [inst_16338
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16348
                                                                                          7)
                                                                                        inst_16339
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16348
                                                                                          8)
                                                                                        inst_16340
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16348
                                                                                          9)
                                                                                        inst_16341
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16348
                                                                                          6)
                                                                                        inst_16342
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16348
                                                                                          10)
                                                                                        inst_16344
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16348
                                                                                          2)
                                                                                        inst_16345
                                                                                        inst_16344
                                                                                        cluster
                                                                                        inst_16338
                                                                                        _
                                                                                        inst_16339
                                                                                        k
                                                                                        inst_16340
                                                                                        ch
                                                                                        inst_16341
                                                                                        result
                                                                                        inst_16345
                                                                                        opts
                                                                                        inst_16342
                                                                                        inst_16346
                                                                                        (if
                                                                                          (=
                                                                                            :ok
                                                                                            result)
                                                                                          {:result
                                                                                           :deleted}
                                                                                          (result->anom
                                                                                            result))]
                                                                                       (clojure.core.async.impl.ioc-macros/return-chan
                                                                                         state_16348
                                                                                         inst_16346))))]
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
                                                                                [statearr_16365
                                                                                 state_16348]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_16365
                                                                                  2
                                                                                  ex__5846__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_16348
                                                                                    4))
                                                                                (let 
                                                                                  [statearr_16366
                                                                                   state_16348]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_16366
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16348
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__5846__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_16348
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__5843__auto__))))]
                                          (if (identical? ret_value__5844__auto__ :recur)
                                            (recur state_16348)
                                            ret_value__5844__auto__))))
                    state__6082__auto__ (let [statearr_16372 (^clojure.lang.IFn f__6081__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16372
                                            5
                                            c__6079__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16372
                                            3
                                            captured_bindings__6080__auto__)
                                          statearr_16372)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6082__auto__)))))
        c__6079__auto__))
    (-put
      [this k v opts]
      (let [ch (df/get-channel (cluster/create-val cluster k (:val v)))
            c__6079__auto__ (a/chan 1)
            captured_bindings__6080__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__16298
            ([]
              (let [G__16273 (fn G__16273 ([] v))
                    G__16274 (fn G__16274 ([] cluster))
                    G__16275 (fn G__16275 ([] this))
                    G__16276 (fn G__16276 ([] k))
                    G__16277 (fn G__16277 ([] ch))
                    G__16278 (fn G__16278 ([] opts))
                    f__6081__auto__ (fn state_machine__5842__auto__
                                      ([]
                                        (let [statearr_16312 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 12))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16312
                                            0
                                            state_machine__5842__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16312
                                            1
                                            1)
                                          statearr_16312))
                                      ([state_16297]
                                        (let [old_frame__5843__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__5844__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_16297
                                                                                3))
                                                                            (loop 
                                                                              []
                                                                              (let 
                                                                                [result__5845__auto__
                                                                                 (let 
                                                                                   [G__16314
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16297
                                                                                        1))]
                                                                                   (case
                                                                                     G__16314
                                                                                     1
                                                                                     (let 
                                                                                       [inst_16290
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16297
                                                                                          6)
                                                                                        inst_16280
                                                                                        (^clojure.lang.IFn G__16273)
                                                                                        v
                                                                                        inst_16280
                                                                                        inst_16281
                                                                                        (^clojure.lang.IFn G__16274)
                                                                                        v
                                                                                        inst_16280
                                                                                        cluster
                                                                                        inst_16281
                                                                                        inst_16282
                                                                                        (^clojure.lang.IFn G__16275)
                                                                                        v
                                                                                        inst_16280
                                                                                        cluster
                                                                                        inst_16281
                                                                                        _
                                                                                        inst_16282
                                                                                        inst_16283
                                                                                        (^clojure.lang.IFn G__16276)
                                                                                        v
                                                                                        inst_16280
                                                                                        cluster
                                                                                        inst_16281
                                                                                        _
                                                                                        inst_16282
                                                                                        k
                                                                                        inst_16283
                                                                                        inst_16284
                                                                                        (^clojure.lang.IFn G__16277)
                                                                                        v
                                                                                        inst_16280
                                                                                        cluster
                                                                                        inst_16281
                                                                                        _
                                                                                        inst_16282
                                                                                        k
                                                                                        inst_16283
                                                                                        ch
                                                                                        inst_16284
                                                                                        inst_16285
                                                                                        (^clojure.lang.IFn G__16278)
                                                                                        inst_16286
                                                                                        inst_16280
                                                                                        inst_16287
                                                                                        inst_16281
                                                                                        inst_16288
                                                                                        inst_16282
                                                                                        inst_16289
                                                                                        inst_16283
                                                                                        inst_16290
                                                                                        inst_16284
                                                                                        inst_16291
                                                                                        inst_16285
                                                                                        state_16297
                                                                                        (let 
                                                                                          [statearr_16315
                                                                                           state_16297]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16315
                                                                                            7
                                                                                            inst_16286)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16315
                                                                                            8
                                                                                            inst_16287)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16315
                                                                                            9
                                                                                            inst_16288)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16315
                                                                                            10
                                                                                            inst_16289)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16315
                                                                                            6
                                                                                            inst_16290)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_16315
                                                                                            11
                                                                                            inst_16291)
                                                                                          statearr_16315)]
                                                                                       (clojure.core.async.impl.ioc-macros/take!
                                                                                         state_16297
                                                                                         2
                                                                                         inst_16290))
                                                                                     2
                                                                                     (let 
                                                                                       [inst_16286
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16297
                                                                                          7)
                                                                                        inst_16287
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16297
                                                                                          8)
                                                                                        inst_16288
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16297
                                                                                          9)
                                                                                        inst_16289
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16297
                                                                                          10)
                                                                                        inst_16290
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16297
                                                                                          6)
                                                                                        inst_16291
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16297
                                                                                          11)
                                                                                        inst_16293
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_16297
                                                                                          2)
                                                                                        inst_16294
                                                                                        inst_16293
                                                                                        v
                                                                                        inst_16286
                                                                                        cluster
                                                                                        inst_16287
                                                                                        _
                                                                                        inst_16288
                                                                                        k
                                                                                        inst_16289
                                                                                        ch
                                                                                        inst_16290
                                                                                        result
                                                                                        inst_16294
                                                                                        opts
                                                                                        inst_16291
                                                                                        inst_16295
                                                                                        (if
                                                                                          (=
                                                                                            result
                                                                                            :created)
                                                                                          {:result
                                                                                           :created}
                                                                                          (result->anom
                                                                                            result))]
                                                                                       (clojure.core.async.impl.ioc-macros/return-chan
                                                                                         state_16297
                                                                                         inst_16295))))]
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
                                                                                [statearr_16316
                                                                                 state_16297]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_16316
                                                                                  2
                                                                                  ex__5846__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_16297
                                                                                    4))
                                                                                (let 
                                                                                  [statearr_16317
                                                                                   state_16297]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_16317
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_16297
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__5846__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_16297
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__5843__auto__))))]
                                          (if (identical? ret_value__5844__auto__ :recur)
                                            (recur state_16297)
                                            ret_value__5844__auto__))))
                    state__6082__auto__ (let [statearr_16323 (^clojure.lang.IFn f__6081__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16323
                                            5
                                            c__6079__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_16323
                                            3
                                            captured_bindings__6080__auto__)
                                          statearr_16323)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6082__auto__)))))
        c__6079__auto__)))
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
    (let [G__16442 (atom nil)]
      (add-watch G__16442 :datomic.cluster-stack/closer common/closing-watch)
      G__16442))
  (defn start-kv-cache
    ([]
      (let [wrap (fn wrap ([x] (when x (val-store-on-kv-cache (deref pool-ref) x))))
            combine (fn combine
                      ([c2 p__16447]
                        (let [vec__16449 p__16447
                              c1 (nth vec__16449 (int 0) nil)
                              metric (nth vec__16449 (int 1) nil)
                              fallback_msec (nth vec__16449 (int 2) nil)]
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
                      (loop [seq_16456 (seq [memcached valcache local_memcached])
                             chunk_16457 nil
                             count_16458 0
                             i_16459 0]
                        (if (< i_16459 count_16458)
                          (let [cache (.nth ^clojure.lang.Indexed chunk_16457 (int i_16459))]
                            (when cache (.close ^java.lang.AutoCloseable cache))
                            (recur seq_16456 chunk_16457 count_16458 (inc i_16459)))
                          (let [temp__5804__auto__ (seq seq_16456)]
                            (when temp__5804__auto__
                              (let [seq_16456 temp__5804__auto__]
                                (if (chunked-seq? seq_16456)
                                  (let [c__6065__auto__ (chunk-first seq_16456)]
                                    (recur
                                      (chunk-rest seq_16456)
                                      c__6065__auto__
                                      (int (count c__6065__auto__))
                                      (int 0)))
                                  (let [cache (first seq_16456)]
                                    (when cache (.close ^java.lang.AutoCloseable cache))
                                    (recur (next seq_16456) nil 0 0))))))))))
            stack (some-> stack (val-store-with-close close))]
        (reset! kv-cache-ref stack))))
  (reset-meta!
    #'start-kv-cache
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'start-kv-cache :ns *ns*))
  (def cluster-with-cache
   (fn cluster_with_cache
     ([cluster cache opts]
       (let [cluster_store (val-store-on-cluster cluster)
             caching_store (double-store/create
                             (merge
                               {:repair-metric :kvc.repair}
                               opts
                               {:near-store cache, :far-store cluster_store}))
             val_cluster (val-cluster/val-cluster caching_store)]
         (combined-cluster/combined-cluster cluster val_cluster)))
     ([cluster cache] (cluster-with-cache cluster cache nil))))
  (reset-meta!
    #'cluster-with-cache
    (assoc
      {:arglists (clojure.core/list ['cluster 'cache] ['cluster 'cache 'opts]), :column (int 1)}
      :name
      'cluster-with-cache
      :ns
      *ns*)))