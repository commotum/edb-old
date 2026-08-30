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
  (let [v__6837__auto__ #'pool-ref]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
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
      (let [G__10195 (a/promise-chan)] (a/put! G__10195 {:result :no-op}) G__10195))
    (-get
      [this k opts]
      (df/get-channel
        (df/-future-with-channel-impl
          exec
          (fn fn__10193
            ([]
              (try
                {:val (get kv_cache k)}
                (catch java.lang.Throwable t (izer/throwable->anom t))))))))
    (-put
      [this k v opts]
      (do
        (cache/put kv_cache k (:val v))
        (let [G__10192 (a/promise-chan)] (a/put! G__10192 {:result :unknown}) G__10192))))
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
            c__6135__auto__ (a/chan 1)
            captured_bindings__6136__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__10328
            ([]
              (let [G__10306 (fn G__10306 ([] cluster))
                    G__10307 (fn G__10307 ([] this))
                    G__10308 (fn G__10308 ([] k))
                    G__10309 (fn G__10309 ([] ch))
                    G__10310 (fn G__10310 ([] opts))
                    f__6137__auto__ (fn state_machine__6040__auto__
                                      ([]
                                        (let [statearr_10340 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 11))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10340
                                            0
                                            state_machine__6040__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10340
                                            1
                                            1)
                                          statearr_10340))
                                      ([state_10327]
                                        (let [old_frame__6041__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__6042__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_10327
                                                                                3))
                                                                            (loop
                                                                              []
                                                                              (let
                                                                                [result__6043__auto__
                                                                                 (let
                                                                                   [G__10342
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10327
                                                                                        1))]
                                                                                   (case
                                                                                     G__10342
                                                                                     1
                                                                                     (let
                                                                                       [inst_10320
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10327
                                                                                          6)
                                                                                        inst_10312
                                                                                        (^clojure.lang.IFn G__10306)
                                                                                        cluster
                                                                                        inst_10312
                                                                                        inst_10313
                                                                                        (^clojure.lang.IFn G__10307)
                                                                                        cluster
                                                                                        inst_10312
                                                                                        _
                                                                                        inst_10313
                                                                                        inst_10314
                                                                                        (^clojure.lang.IFn G__10308)
                                                                                        cluster
                                                                                        inst_10312
                                                                                        _
                                                                                        inst_10313
                                                                                        k
                                                                                        inst_10314
                                                                                        inst_10315
                                                                                        (^clojure.lang.IFn G__10309)
                                                                                        cluster
                                                                                        inst_10312
                                                                                        _
                                                                                        inst_10313
                                                                                        k
                                                                                        inst_10314
                                                                                        ch
                                                                                        inst_10315
                                                                                        inst_10316
                                                                                        (^clojure.lang.IFn G__10310)
                                                                                        inst_10317
                                                                                        inst_10312
                                                                                        inst_10318
                                                                                        inst_10313
                                                                                        inst_10319
                                                                                        inst_10314
                                                                                        inst_10320
                                                                                        inst_10315
                                                                                        inst_10321
                                                                                        inst_10316
                                                                                        state_10327
                                                                                        (let
                                                                                          [statearr_10343
                                                                                           state_10327]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10343
                                                                                            7
                                                                                            inst_10317)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10343
                                                                                            8
                                                                                            inst_10318)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10343
                                                                                            9
                                                                                            inst_10319)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10343
                                                                                            6
                                                                                            inst_10320)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10343
                                                                                            10
                                                                                            inst_10321)
                                                                                          statearr_10343)]
                                                                                       (clojure.core.async.impl.ioc-macros/take!
                                                                                         state_10327
                                                                                         2
                                                                                         inst_10320))
                                                                                     2
                                                                                     (let
                                                                                       [inst_10317
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10327
                                                                                          7)
                                                                                        inst_10318
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10327
                                                                                          8)
                                                                                        inst_10319
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10327
                                                                                          9)
                                                                                        inst_10320
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10327
                                                                                          6)
                                                                                        inst_10321
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10327
                                                                                          10)
                                                                                        inst_10323
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10327
                                                                                          2)
                                                                                        inst_10324
                                                                                        inst_10323
                                                                                        cluster
                                                                                        inst_10317
                                                                                        _
                                                                                        inst_10318
                                                                                        k
                                                                                        inst_10319
                                                                                        ch
                                                                                        inst_10320
                                                                                        result
                                                                                        inst_10324
                                                                                        opts
                                                                                        inst_10321
                                                                                        inst_10325
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
                                                                                         state_10327
                                                                                         inst_10325))))]
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
                                                                                [statearr_10344
                                                                                 state_10327]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_10344
                                                                                  2
                                                                                  ex__6044__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_10327
                                                                                    4))
                                                                                (let
                                                                                  [statearr_10345
                                                                                   state_10327]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_10345
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10327
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__6044__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_10327
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__6041__auto__))))]
                                          (if (identical? ret_value__6042__auto__ :recur)
                                            (recur state_10327)
                                            ret_value__6042__auto__))))
                    state__6138__auto__ (let [statearr_10351 (^clojure.lang.IFn f__6137__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10351
                                            5
                                            c__6135__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10351
                                            3
                                            captured_bindings__6136__auto__)
                                          statearr_10351)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6138__auto__)))))
        c__6135__auto__))
    (-delete
      [this k opts]
      (let [ch (df/get-channel (cluster/delete cluster k))
            c__6135__auto__ (a/chan 1)
            captured_bindings__6136__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__10279
            ([]
              (let [G__10257 (fn G__10257 ([] cluster))
                    G__10258 (fn G__10258 ([] this))
                    G__10259 (fn G__10259 ([] k))
                    G__10260 (fn G__10260 ([] ch))
                    G__10261 (fn G__10261 ([] opts))
                    f__6137__auto__ (fn state_machine__6040__auto__
                                      ([]
                                        (let [statearr_10291 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 11))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10291
                                            0
                                            state_machine__6040__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10291
                                            1
                                            1)
                                          statearr_10291))
                                      ([state_10278]
                                        (let [old_frame__6041__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__6042__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_10278
                                                                                3))
                                                                            (loop
                                                                              []
                                                                              (let
                                                                                [result__6043__auto__
                                                                                 (let
                                                                                   [G__10293
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10278
                                                                                        1))]
                                                                                   (case
                                                                                     G__10293
                                                                                     1
                                                                                     (let
                                                                                       [inst_10271
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10278
                                                                                          6)
                                                                                        inst_10263
                                                                                        (^clojure.lang.IFn G__10257)
                                                                                        cluster
                                                                                        inst_10263
                                                                                        inst_10264
                                                                                        (^clojure.lang.IFn G__10258)
                                                                                        cluster
                                                                                        inst_10263
                                                                                        _
                                                                                        inst_10264
                                                                                        inst_10265
                                                                                        (^clojure.lang.IFn G__10259)
                                                                                        cluster
                                                                                        inst_10263
                                                                                        _
                                                                                        inst_10264
                                                                                        k
                                                                                        inst_10265
                                                                                        inst_10266
                                                                                        (^clojure.lang.IFn G__10260)
                                                                                        cluster
                                                                                        inst_10263
                                                                                        _
                                                                                        inst_10264
                                                                                        k
                                                                                        inst_10265
                                                                                        ch
                                                                                        inst_10266
                                                                                        inst_10267
                                                                                        (^clojure.lang.IFn G__10261)
                                                                                        inst_10268
                                                                                        inst_10263
                                                                                        inst_10269
                                                                                        inst_10264
                                                                                        inst_10270
                                                                                        inst_10265
                                                                                        inst_10271
                                                                                        inst_10266
                                                                                        inst_10272
                                                                                        inst_10267
                                                                                        state_10278
                                                                                        (let
                                                                                          [statearr_10294
                                                                                           state_10278]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10294
                                                                                            7
                                                                                            inst_10268)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10294
                                                                                            8
                                                                                            inst_10269)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10294
                                                                                            9
                                                                                            inst_10270)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10294
                                                                                            6
                                                                                            inst_10271)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10294
                                                                                            10
                                                                                            inst_10272)
                                                                                          statearr_10294)]
                                                                                       (clojure.core.async.impl.ioc-macros/take!
                                                                                         state_10278
                                                                                         2
                                                                                         inst_10271))
                                                                                     2
                                                                                     (let
                                                                                       [inst_10268
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10278
                                                                                          7)
                                                                                        inst_10269
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10278
                                                                                          8)
                                                                                        inst_10270
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10278
                                                                                          9)
                                                                                        inst_10271
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10278
                                                                                          6)
                                                                                        inst_10272
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10278
                                                                                          10)
                                                                                        inst_10274
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10278
                                                                                          2)
                                                                                        inst_10275
                                                                                        inst_10274
                                                                                        cluster
                                                                                        inst_10268
                                                                                        _
                                                                                        inst_10269
                                                                                        k
                                                                                        inst_10270
                                                                                        ch
                                                                                        inst_10271
                                                                                        result
                                                                                        inst_10275
                                                                                        opts
                                                                                        inst_10272
                                                                                        inst_10276
                                                                                        (if
                                                                                          (=
                                                                                            :ok
                                                                                            result)
                                                                                          {:result
                                                                                           :deleted}
                                                                                          (result->anom
                                                                                            result))]
                                                                                       (clojure.core.async.impl.ioc-macros/return-chan
                                                                                         state_10278
                                                                                         inst_10276))))]
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
                                                                                [statearr_10295
                                                                                 state_10278]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_10295
                                                                                  2
                                                                                  ex__6044__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_10278
                                                                                    4))
                                                                                (let
                                                                                  [statearr_10296
                                                                                   state_10278]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_10296
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10278
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__6044__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_10278
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__6041__auto__))))]
                                          (if (identical? ret_value__6042__auto__ :recur)
                                            (recur state_10278)
                                            ret_value__6042__auto__))))
                    state__6138__auto__ (let [statearr_10302 (^clojure.lang.IFn f__6137__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10302
                                            5
                                            c__6135__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10302
                                            3
                                            captured_bindings__6136__auto__)
                                          statearr_10302)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6138__auto__)))))
        c__6135__auto__))
    (-put
      [this k v opts]
      (let [ch (df/get-channel (cluster/create-val cluster k (:val v)))
            c__6135__auto__ (a/chan 1)
            captured_bindings__6136__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__10228
            ([]
              (let [G__10203 (fn G__10203 ([] v))
                    G__10204 (fn G__10204 ([] cluster))
                    G__10205 (fn G__10205 ([] this))
                    G__10206 (fn G__10206 ([] k))
                    G__10207 (fn G__10207 ([] ch))
                    G__10208 (fn G__10208 ([] opts))
                    f__6137__auto__ (fn state_machine__6040__auto__
                                      ([]
                                        (let [statearr_10242 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                               (int 12))]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10242
                                            0
                                            state_machine__6040__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10242
                                            1
                                            1)
                                          statearr_10242))
                                      ([state_10227]
                                        (let [old_frame__6041__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                              ret_value__6042__auto__ (try
                                                                        (try
                                                                          (do
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              (clojure.core.async.impl.ioc-macros/aget-object
                                                                                state_10227
                                                                                3))
                                                                            (loop
                                                                              []
                                                                              (let
                                                                                [result__6043__auto__
                                                                                 (let
                                                                                   [G__10244
                                                                                    (int
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10227
                                                                                        1))]
                                                                                   (case
                                                                                     G__10244
                                                                                     1
                                                                                     (let
                                                                                       [inst_10220
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10227
                                                                                          6)
                                                                                        inst_10210
                                                                                        (^clojure.lang.IFn G__10203)
                                                                                        v
                                                                                        inst_10210
                                                                                        inst_10211
                                                                                        (^clojure.lang.IFn G__10204)
                                                                                        v
                                                                                        inst_10210
                                                                                        cluster
                                                                                        inst_10211
                                                                                        inst_10212
                                                                                        (^clojure.lang.IFn G__10205)
                                                                                        v
                                                                                        inst_10210
                                                                                        cluster
                                                                                        inst_10211
                                                                                        _
                                                                                        inst_10212
                                                                                        inst_10213
                                                                                        (^clojure.lang.IFn G__10206)
                                                                                        v
                                                                                        inst_10210
                                                                                        cluster
                                                                                        inst_10211
                                                                                        _
                                                                                        inst_10212
                                                                                        k
                                                                                        inst_10213
                                                                                        inst_10214
                                                                                        (^clojure.lang.IFn G__10207)
                                                                                        v
                                                                                        inst_10210
                                                                                        cluster
                                                                                        inst_10211
                                                                                        _
                                                                                        inst_10212
                                                                                        k
                                                                                        inst_10213
                                                                                        ch
                                                                                        inst_10214
                                                                                        inst_10215
                                                                                        (^clojure.lang.IFn G__10208)
                                                                                        inst_10216
                                                                                        inst_10210
                                                                                        inst_10217
                                                                                        inst_10211
                                                                                        inst_10218
                                                                                        inst_10212
                                                                                        inst_10219
                                                                                        inst_10213
                                                                                        inst_10220
                                                                                        inst_10214
                                                                                        inst_10221
                                                                                        inst_10215
                                                                                        state_10227
                                                                                        (let
                                                                                          [statearr_10245
                                                                                           state_10227]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10245
                                                                                            7
                                                                                            inst_10216)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10245
                                                                                            8
                                                                                            inst_10217)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10245
                                                                                            9
                                                                                            inst_10218)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10245
                                                                                            10
                                                                                            inst_10219)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10245
                                                                                            6
                                                                                            inst_10220)
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_10245
                                                                                            11
                                                                                            inst_10221)
                                                                                          statearr_10245)]
                                                                                       (clojure.core.async.impl.ioc-macros/take!
                                                                                         state_10227
                                                                                         2
                                                                                         inst_10220))
                                                                                     2
                                                                                     (let
                                                                                       [inst_10216
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10227
                                                                                          7)
                                                                                        inst_10217
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10227
                                                                                          8)
                                                                                        inst_10218
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10227
                                                                                          9)
                                                                                        inst_10219
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10227
                                                                                          10)
                                                                                        inst_10220
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10227
                                                                                          6)
                                                                                        inst_10221
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10227
                                                                                          11)
                                                                                        inst_10223
                                                                                        (clojure.core.async.impl.ioc-macros/aget-object
                                                                                          state_10227
                                                                                          2)
                                                                                        inst_10224
                                                                                        inst_10223
                                                                                        v
                                                                                        inst_10216
                                                                                        cluster
                                                                                        inst_10217
                                                                                        _
                                                                                        inst_10218
                                                                                        k
                                                                                        inst_10219
                                                                                        ch
                                                                                        inst_10220
                                                                                        result
                                                                                        inst_10224
                                                                                        opts
                                                                                        inst_10221
                                                                                        inst_10225
                                                                                        (if
                                                                                          (=
                                                                                            result
                                                                                            :created)
                                                                                          {:result
                                                                                           :created}
                                                                                          (result->anom
                                                                                            result))]
                                                                                       (clojure.core.async.impl.ioc-macros/return-chan
                                                                                         state_10227
                                                                                         inst_10225))))]
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
                                                                                [statearr_10246
                                                                                 state_10227]
                                                                                (clojure.core.async.impl.ioc-macros/aset-object
                                                                                  statearr_10246
                                                                                  2
                                                                                  ex__6044__auto__))
                                                                              (if
                                                                                (seq
                                                                                  (clojure.core.async.impl.ioc-macros/aget-object
                                                                                    state_10227
                                                                                    4))
                                                                                (let
                                                                                  [statearr_10247
                                                                                   state_10227]
                                                                                  (clojure.core.async.impl.ioc-macros/aset-object
                                                                                    statearr_10247
                                                                                    1
                                                                                    (first
                                                                                      (clojure.core.async.impl.ioc-macros/aget-object
                                                                                        state_10227
                                                                                        4))))
                                                                                (throw
                                                                                  ^java.lang.Throwable ex__6044__auto__))
                                                                              :recur)))
                                                                        (finally
                                                                          (do
                                                                            (clojure.core.async.impl.ioc-macros/aset-object
                                                                              state_10227
                                                                              3
                                                                              (clojure.lang.Var/getThreadBindingFrame))
                                                                            (clojure.lang.Var/resetThreadBindingFrame
                                                                              old_frame__6041__auto__))))]
                                          (if (identical? ret_value__6042__auto__ :recur)
                                            (recur state_10227)
                                            ret_value__6042__auto__))))
                    state__6138__auto__ (let [statearr_10253 (^clojure.lang.IFn f__6137__auto__)]
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10253
                                            5
                                            c__6135__auto__)
                                          (clojure.core.async.impl.ioc-macros/aset-object
                                            statearr_10253
                                            3
                                            captured_bindings__6136__auto__)
                                          statearr_10253)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__6138__auto__)))))
        c__6135__auto__)))
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
    (let [G__10372 (atom nil)]
      (add-watch G__10372 :datomic.cluster-stack/closer common/closing-watch)
      G__10372))
  (defn start-kv-cache
    ([]
      (let [wrap (fn wrap ([x] (when x (val-store-on-kv-cache (deref pool-ref) x))))
            combine (fn combine
                      ([c2 p__10377]
                        (let [vec__10379 p__10377
                              c1 (nth vec__10379 (int 0) nil)
                              metric (nth vec__10379 (int 1) nil)
                              fallback_msec (nth vec__10379 (int 2) nil)]
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
                      (loop [seq_10386 (seq [memcached valcache local_memcached])
                             chunk_10387 nil
                             count_10388 0
                             i_10389 0]
                        (if (< i_10389 count_10388)
                          (let [cache (.nth ^clojure.lang.Indexed chunk_10387 (int i_10389))]
                            (when cache (.close ^java.lang.AutoCloseable cache))
                            (recur seq_10386 chunk_10387 count_10388 (inc i_10389)))
                          (let [temp__5825__auto__ (seq seq_10386)]
                            (when temp__5825__auto__
                              (let [seq_10386 temp__5825__auto__]
                                (if (chunked-seq? seq_10386)
                                  (let [c__6090__auto__ (chunk-first seq_10386)]
                                    (recur
                                      (chunk-rest seq_10386)
                                      c__6090__auto__
                                      (int (count c__6090__auto__))
                                      (int 0)))
                                  (let [cache (first seq_10386)]
                                    (when cache (.close ^java.lang.AutoCloseable cache))
                                    (recur (next seq_10386) nil 0 0))))))))))
            stack (some-> stack (val-store-with-close close))]
        (reset! kv-cache-ref stack))))
  (reset-meta!
    #'start-kv-cache
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'start-kv-cache :ns *ns*))
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