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
  (defonce pool-ref (delay (common/cached-thread-pool {:name "KVCache"})))
  (deftype
    ValStoreOnKvCache
    [exec kv_cache]
    datomic.core2.val_store.spi.Get
    datomic.core2.val_store.spi.Delete
    datomic.core2.val_store.spi.Put
    (-delete
      [this k opts]
      (let [G__11263 (a/promise-chan)] (a/put! G__11263 {:result :no-op}) G__11263))
    (-get
      [this k opts]
      (df/get-channel
        (let [f__10267__auto__ (df/-future-with-channel-impl
                                 exec
                                 (fn fn__11261
                                   ([]
                                     (try
                                       {:val (get kv_cache k)}
                                       (catch java.lang.Throwable t (canom/fault t))))))
              ch__10268__auto__ (df/get-channel f__10267__auto__)]
          (df/add-bounding-warning
            ch__10268__auto__
            {:line 40, :column 9, :file "datomic/cluster_stack.clj"}
            (deref df/bounding-warn-seconds))
          f__10267__auto__)))
    (-put
      [this k v opts]
      (do
        (cache/put kv_cache k (:val v))
        (let [G__11260 (a/promise-chan)] (a/put! G__11260 {:result :unknown}) G__11260))))
  (clojure.core/import 'datomic.cluster_stack.ValStoreOnKvCache)
  (defn ->ValStoreOnKvCache
    ([exec kv_cache] (datomic.cluster_stack.ValStoreOnKvCache. exec kv_cache)))
  (defn val-store-on-kv-cache ([exec kv_cache] (->ValStoreOnKvCache exec kv_cache)))
  (defn result->anom
    ([result]
      (if (:cognitect.anomalies/category result)
        result
        {:cognitect.anomalies/category :cognitect.anomalies/fault,
         :datomic.cluster-stack/cluster-result result})))
  (deftype
    ValStoreOnCluster
    [cluster]
    datomic.core2.val_store.spi.Get
    datomic.core2.val_store.spi.Delete
    datomic.core2.val_store.spi.Put
    (-get
      [this k opts]
      (let [ch (df/get-channel (cluster/get-val cluster k))
            c__6597__auto__ (a/chan 1)
            captured_bindings__6598__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__11398
            ([]
              (let [G__11376 (fn G__11376 ([] cluster))
                    G__11377 (fn G__11377 ([] this))
                    G__11378 (fn G__11378 ([] k))
                    G__11379 (fn G__11379 ([] ch))
                    G__11380 (fn G__11380 ([] opts))
                    f__6599__auto__ (fn state_machine__6360__auto__
                                      ([]
                                        (let [statearr_11410 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 11))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11410
                                            0
                                            state_machine__6360__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11410
                                            1
                                            1)
                                          statearr_11410))
                                      ([state_11397]
                                        (let [old_frame__6361__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__6362__auto__ (try
                                                                        (do
                                                                          (clojure.lang.Var/resetThreadBindingFrame
                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                              state_11397
                                                                              3))
                                                                          (loop 
                                                                            []
                                                                            (let 
                                                                              [result__6363__auto__
                                                                               (let 
                                                                                 [G__11412
                                                                                  (int
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_11397
                                                                                      1))]
                                                                                 (case
                                                                                   G__11412
                                                                                   1
                                                                                   (let 
                                                                                     [inst_11390
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11397
                                                                                        6)
                                                                                      inst_11382
                                                                                      (^clojure.lang.IFn G__11376)
                                                                                      cluster
                                                                                      inst_11382
                                                                                      inst_11383
                                                                                      (^clojure.lang.IFn G__11377)
                                                                                      cluster
                                                                                      inst_11382
                                                                                      _ inst_11383
                                                                                      inst_11384
                                                                                      (^clojure.lang.IFn G__11378)
                                                                                      cluster
                                                                                      inst_11382
                                                                                      _ inst_11383
                                                                                      k inst_11384
                                                                                      inst_11385
                                                                                      (^clojure.lang.IFn G__11379)
                                                                                      cluster
                                                                                      inst_11382
                                                                                      _ inst_11383
                                                                                      k inst_11384
                                                                                      ch inst_11385
                                                                                      inst_11386
                                                                                      (^clojure.lang.IFn G__11380)
                                                                                      inst_11387
                                                                                      inst_11382
                                                                                      inst_11388
                                                                                      inst_11383
                                                                                      inst_11389
                                                                                      inst_11384
                                                                                      inst_11390
                                                                                      inst_11385
                                                                                      inst_11391
                                                                                      inst_11386
                                                                                      state_11397
                                                                                      (let 
                                                                                        [statearr_11413
                                                                                         state_11397]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11413
                                                                                          7
                                                                                          inst_11387)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11413
                                                                                          8
                                                                                          inst_11388)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11413
                                                                                          9
                                                                                          inst_11389)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11413
                                                                                          6
                                                                                          inst_11390)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11413
                                                                                          10
                                                                                          inst_11391)
                                                                                        statearr_11413)]
                                                                                     (clojure.core.async.impl.ioc-macros/take!
                                                                                       state_11397
                                                                                       2
                                                                                       inst_11390))
                                                                                   2
                                                                                   (let 
                                                                                     [inst_11387
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11397
                                                                                        7)
                                                                                      inst_11388
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11397
                                                                                        8)
                                                                                      inst_11389
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11397
                                                                                        9)
                                                                                      inst_11390
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11397
                                                                                        6)
                                                                                      inst_11391
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11397
                                                                                        10)
                                                                                      inst_11393
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11397
                                                                                        2)
                                                                                      inst_11394
                                                                                      inst_11393
                                                                                      cluster
                                                                                      inst_11387
                                                                                      _ inst_11388
                                                                                      k inst_11389
                                                                                      ch inst_11390
                                                                                      result
                                                                                      inst_11394
                                                                                      opts
                                                                                      inst_11391
                                                                                      inst_11395
                                                                                      (cond
                                                                                        (=
                                                                                          :datomic.future/nil
                                                                                          result)
                                                                                        {:val nil}
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
                                                                                       state_11397
                                                                                       inst_11395))))]
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
                                                                              [statearr_11414
                                                                               state_11397]
                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                statearr_11414
                                                                                2
                                                                                ex__6364__auto__))
                                                                            (if
                                                                              (seq
                                                                                (clojure.core.async.impl.ioc-macros/aget-object
                                                                                  state_11397
                                                                                  4))
                                                                              (let 
                                                                                [statearr_11415
                                                                                 state_11397]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_11415
                                                                                  1
                                                                                  (first
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_11397
                                                                                      4))))
                                                                              (throw
                                                                                ^java.lang.Throwable ex__6364__auto__))
                                                                            :recur))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_11397
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__6361__auto__))))]
                                          (if (identical? ret_value__6362__auto__ :recur)
                                            (recur state_11397)
                                            ret_value__6362__auto__))))
                    state__6600__auto__ (let [statearr_11421 (^clojure.lang.IFn f__6599__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11421
                                            5
                                            c__6597__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11421
                                            3
                                            captured_bindings__6598__auto__)
                                          statearr_11421)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6600__auto__)))))
        c__6597__auto__))
    (-delete
      [this k opts]
      (let [ch (df/get-channel (cluster/delete cluster k))
            c__6597__auto__ (a/chan 1)
            captured_bindings__6598__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__11349
            ([]
              (let [G__11327 (fn G__11327 ([] cluster))
                    G__11328 (fn G__11328 ([] this))
                    G__11329 (fn G__11329 ([] k))
                    G__11330 (fn G__11330 ([] ch))
                    G__11331 (fn G__11331 ([] opts))
                    f__6599__auto__ (fn state_machine__6360__auto__
                                      ([]
                                        (let [statearr_11361 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 11))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11361
                                            0
                                            state_machine__6360__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11361
                                            1
                                            1)
                                          statearr_11361))
                                      ([state_11348]
                                        (let [old_frame__6361__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__6362__auto__ (try
                                                                        (do
                                                                          (clojure.lang.Var/resetThreadBindingFrame
                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                              state_11348
                                                                              3))
                                                                          (loop 
                                                                            []
                                                                            (let 
                                                                              [result__6363__auto__
                                                                               (let 
                                                                                 [G__11363
                                                                                  (int
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_11348
                                                                                      1))]
                                                                                 (case
                                                                                   G__11363
                                                                                   1
                                                                                   (let 
                                                                                     [inst_11341
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11348
                                                                                        6)
                                                                                      inst_11333
                                                                                      (^clojure.lang.IFn G__11327)
                                                                                      cluster
                                                                                      inst_11333
                                                                                      inst_11334
                                                                                      (^clojure.lang.IFn G__11328)
                                                                                      cluster
                                                                                      inst_11333
                                                                                      _ inst_11334
                                                                                      inst_11335
                                                                                      (^clojure.lang.IFn G__11329)
                                                                                      cluster
                                                                                      inst_11333
                                                                                      _ inst_11334
                                                                                      k inst_11335
                                                                                      inst_11336
                                                                                      (^clojure.lang.IFn G__11330)
                                                                                      cluster
                                                                                      inst_11333
                                                                                      _ inst_11334
                                                                                      k inst_11335
                                                                                      ch inst_11336
                                                                                      inst_11337
                                                                                      (^clojure.lang.IFn G__11331)
                                                                                      inst_11338
                                                                                      inst_11333
                                                                                      inst_11339
                                                                                      inst_11334
                                                                                      inst_11340
                                                                                      inst_11335
                                                                                      inst_11341
                                                                                      inst_11336
                                                                                      inst_11342
                                                                                      inst_11337
                                                                                      state_11348
                                                                                      (let 
                                                                                        [statearr_11364
                                                                                         state_11348]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11364
                                                                                          7
                                                                                          inst_11338)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11364
                                                                                          8
                                                                                          inst_11339)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11364
                                                                                          9
                                                                                          inst_11340)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11364
                                                                                          6
                                                                                          inst_11341)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11364
                                                                                          10
                                                                                          inst_11342)
                                                                                        statearr_11364)]
                                                                                     (clojure.core.async.impl.ioc-macros/take!
                                                                                       state_11348
                                                                                       2
                                                                                       inst_11341))
                                                                                   2
                                                                                   (let 
                                                                                     [inst_11338
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11348
                                                                                        7)
                                                                                      inst_11339
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11348
                                                                                        8)
                                                                                      inst_11340
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11348
                                                                                        9)
                                                                                      inst_11341
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11348
                                                                                        6)
                                                                                      inst_11342
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11348
                                                                                        10)
                                                                                      inst_11344
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11348
                                                                                        2)
                                                                                      inst_11345
                                                                                      inst_11344
                                                                                      cluster
                                                                                      inst_11338
                                                                                      _ inst_11339
                                                                                      k inst_11340
                                                                                      ch inst_11341
                                                                                      result
                                                                                      inst_11345
                                                                                      opts
                                                                                      inst_11342
                                                                                      inst_11346
                                                                                      (if
                                                                                        (=
                                                                                          :ok
                                                                                          result)
                                                                                        {:result
                                                                                         :deleted}
                                                                                        (result->anom
                                                                                          result))]
                                                                                     (clojure.core.async.impl.ioc-macros/return-chan
                                                                                       state_11348
                                                                                       inst_11346))))]
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
                                                                              [statearr_11365
                                                                               state_11348]
                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                statearr_11365
                                                                                2
                                                                                ex__6364__auto__))
                                                                            (if
                                                                              (seq
                                                                                (clojure.core.async.impl.ioc-macros/aget-object
                                                                                  state_11348
                                                                                  4))
                                                                              (let 
                                                                                [statearr_11366
                                                                                 state_11348]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_11366
                                                                                  1
                                                                                  (first
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_11348
                                                                                      4))))
                                                                              (throw
                                                                                ^java.lang.Throwable ex__6364__auto__))
                                                                            :recur))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_11348
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__6361__auto__))))]
                                          (if (identical? ret_value__6362__auto__ :recur)
                                            (recur state_11348)
                                            ret_value__6362__auto__))))
                    state__6600__auto__ (let [statearr_11372 (^clojure.lang.IFn f__6599__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11372
                                            5
                                            c__6597__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11372
                                            3
                                            captured_bindings__6598__auto__)
                                          statearr_11372)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6600__auto__)))))
        c__6597__auto__))
    (-put
      [this k v opts]
      (let [ch (df/get-channel (cluster/create-val cluster k (:val v)))
            c__6597__auto__ (a/chan 1)
            captured_bindings__6598__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__11298
            ([]
              (let [G__11273 (fn G__11273 ([] v))
                    G__11274 (fn G__11274 ([] cluster))
                    G__11275 (fn G__11275 ([] this))
                    G__11276 (fn G__11276 ([] k))
                    G__11277 (fn G__11277 ([] ch))
                    G__11278 (fn G__11278 ([] opts))
                    f__6599__auto__ (fn state_machine__6360__auto__
                                      ([]
                                        (let [statearr_11312 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 12))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11312
                                            0
                                            state_machine__6360__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11312
                                            1
                                            1)
                                          statearr_11312))
                                      ([state_11297]
                                        (let [old_frame__6361__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__6362__auto__ (try
                                                                        (do
                                                                          (clojure.lang.Var/resetThreadBindingFrame
                                                                            (clojure.core.async.impl.ioc-macros/aget-object
                                                                              state_11297
                                                                              3))
                                                                          (loop 
                                                                            []
                                                                            (let 
                                                                              [result__6363__auto__
                                                                               (let 
                                                                                 [G__11314
                                                                                  (int
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_11297
                                                                                      1))]
                                                                                 (case
                                                                                   G__11314
                                                                                   1
                                                                                   (let 
                                                                                     [inst_11290
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11297
                                                                                        6)
                                                                                      inst_11280
                                                                                      (^clojure.lang.IFn G__11273)
                                                                                      v inst_11280
                                                                                      inst_11281
                                                                                      (^clojure.lang.IFn G__11274)
                                                                                      v inst_11280
                                                                                      cluster
                                                                                      inst_11281
                                                                                      inst_11282
                                                                                      (^clojure.lang.IFn G__11275)
                                                                                      v inst_11280
                                                                                      cluster
                                                                                      inst_11281
                                                                                      _ inst_11282
                                                                                      inst_11283
                                                                                      (^clojure.lang.IFn G__11276)
                                                                                      v inst_11280
                                                                                      cluster
                                                                                      inst_11281
                                                                                      _ inst_11282
                                                                                      k inst_11283
                                                                                      inst_11284
                                                                                      (^clojure.lang.IFn G__11277)
                                                                                      v inst_11280
                                                                                      cluster
                                                                                      inst_11281
                                                                                      _ inst_11282
                                                                                      k inst_11283
                                                                                      ch inst_11284
                                                                                      inst_11285
                                                                                      (^clojure.lang.IFn G__11278)
                                                                                      inst_11286
                                                                                      inst_11280
                                                                                      inst_11287
                                                                                      inst_11281
                                                                                      inst_11288
                                                                                      inst_11282
                                                                                      inst_11289
                                                                                      inst_11283
                                                                                      inst_11290
                                                                                      inst_11284
                                                                                      inst_11291
                                                                                      inst_11285
                                                                                      state_11297
                                                                                      (let 
                                                                                        [statearr_11315
                                                                                         state_11297]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11315
                                                                                          7
                                                                                          inst_11286)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11315
                                                                                          8
                                                                                          inst_11287)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11315
                                                                                          9
                                                                                          inst_11288)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11315
                                                                                          10
                                                                                          inst_11289)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11315
                                                                                          6
                                                                                          inst_11290)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_11315
                                                                                          11
                                                                                          inst_11291)
                                                                                        statearr_11315)]
                                                                                     (clojure.core.async.impl.ioc-macros/take!
                                                                                       state_11297
                                                                                       2
                                                                                       inst_11290))
                                                                                   2
                                                                                   (let 
                                                                                     [inst_11286
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11297
                                                                                        7)
                                                                                      inst_11287
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11297
                                                                                        8)
                                                                                      inst_11288
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11297
                                                                                        9)
                                                                                      inst_11289
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11297
                                                                                        10)
                                                                                      inst_11290
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11297
                                                                                        6)
                                                                                      inst_11291
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11297
                                                                                        11)
                                                                                      inst_11293
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_11297
                                                                                        2)
                                                                                      inst_11294
                                                                                      inst_11293
                                                                                      v inst_11286
                                                                                      cluster
                                                                                      inst_11287
                                                                                      _ inst_11288
                                                                                      k inst_11289
                                                                                      ch inst_11290
                                                                                      result
                                                                                      inst_11294
                                                                                      opts
                                                                                      inst_11291
                                                                                      inst_11295
                                                                                      (if
                                                                                        (=
                                                                                          result
                                                                                          :created)
                                                                                        {:result
                                                                                         :created}
                                                                                        (result->anom
                                                                                          result))]
                                                                                     (clojure.core.async.impl.ioc-macros/return-chan
                                                                                       state_11297
                                                                                       inst_11295))))]
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
                                                                              [statearr_11316
                                                                               state_11297]
                                                                              (clojure.core.async.impl.ioc-macros/aset-object
                                                                                statearr_11316
                                                                                2
                                                                                ex__6364__auto__))
                                                                            (if
                                                                              (seq
                                                                                (clojure.core.async.impl.ioc-macros/aget-object
                                                                                  state_11297
                                                                                  4))
                                                                              (let 
                                                                                [statearr_11317
                                                                                 state_11297]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_11317
                                                                                  1
                                                                                  (first
                                                                                    (clojure.core.async.impl.ioc-macros/aget-object
                                                                                      state_11297
                                                                                      4))))
                                                                              (throw
                                                                                ^java.lang.Throwable ex__6364__auto__))
                                                                            :recur))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_11297
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__6361__auto__))))]
                                          (if (identical? ret_value__6362__auto__ :recur)
                                            (recur state_11297)
                                            ret_value__6362__auto__))))
                    state__6600__auto__ (let [statearr_11323 (^clojure.lang.IFn f__6599__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11323
                                            5
                                            c__6597__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_11323
                                            3
                                            captured_bindings__6598__auto__)
                                          statearr_11323)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6600__auto__)))))
        c__6597__auto__)))
  (clojure.core/import 'datomic.cluster_stack.ValStoreOnCluster)
  (defn ->ValStoreOnCluster ([cluster] (datomic.cluster_stack.ValStoreOnCluster. cluster)))
  (defn val-store-on-cluster ([cluster] (->ValStoreOnCluster cluster)))
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
  (defn val-store-with-close ([store close] (->ValStoreWithClose store close)))
  (def kv-cache-ref
   (let [G__11442 (atom nil)]
     (add-watch G__11442 :datomic.cluster-stack/closer common/closing-watch)
     G__11442))
  (defn start-kv-cache
    ([]
      (let [wrap (fn wrap ([x] (when x (val-store-on-kv-cache (deref pool-ref) x))))
            combine (fn combine
                      ([c2 p__11447]
                        (let [vec__11449 p__11447
                              c1 (nth vec__11449 (int 0) nil)
                              metric (nth vec__11449 (int 1) nil)
                              fallback_msec (nth vec__11449 (int 2) nil)]
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
                      (loop [seq_11456 (seq [memcached valcache local_memcached])
                             chunk_11457 nil
                             count_11458 0
                             i_11459 0]
                        (if (< i_11459 count_11458)
                          (let [cache (.nth ^clojure.lang.Indexed chunk_11457 (int i_11459))]
                            (when cache (.close ^java.lang.AutoCloseable cache) nil)
                            (recur seq_11456 chunk_11457 count_11458 (inc i_11459)))
                          (let [temp__5457__auto__ (seq seq_11456)]
                            (when temp__5457__auto__
                              (let [seq_11456 temp__5457__auto__]
                                (if (chunked-seq? seq_11456)
                                  (let [c__5719__auto__ (chunk-first seq_11456)]
                                    (recur
                                      (chunk-rest seq_11456)
                                      c__5719__auto__
                                      (int (count c__5719__auto__))
                                      (int 0)))
                                  (let [cache (first seq_11456)]
                                    (when cache (.close ^java.lang.AutoCloseable cache) nil)
                                    (recur (next seq_11456) nil 0 0))))))))))
            stack (some-> stack (val-store-with-close close))]
        (reset! kv-cache-ref stack))))
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
    ([cluster cache] (cluster-with-cache cluster cache nil))))