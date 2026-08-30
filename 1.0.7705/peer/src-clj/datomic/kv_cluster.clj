(do
  (clojure.core/in-ns 'datomic.kv-cluster)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.cache :as 'cache]
        ['datomic.config :as 'config]
        ['datomic.cluster :as 'cluster]
        ['datomic.future :as 'df]
        ['datomic.kv-store :as 'kv]
        ['datomic.monitor :as 'monitor]
        ['datomic.process.events :as 'events]
        ['datomic.require :as 'req]
        ['datomic.slf4j :as 'logger]
        ['datomic.measure.io-stats :as 'io-stats])
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.concurrent.ExecutorService)
      (clojure.core/import 'java.util.concurrent.Semaphore)
      (clojure.core/import 'java.util.concurrent.TimeUnit)
      (clojure.core/import 'java.io.Closeable)))
  (when-not (.equals 'datomic.kv-cluster 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-cluster))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common]
          ['datomic.cache :as 'cache]
          ['datomic.config :as 'config]
          ['datomic.cluster :as 'cluster]
          ['datomic.future :as 'df]
          ['datomic.kv-store :as 'kv]
          ['datomic.monitor :as 'monitor]
          ['datomic.process.events :as 'events]
          ['datomic.require :as 'req]
          ['datomic.slf4j :as 'logger]
          ['datomic.measure.io-stats :as 'io-stats])
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.concurrent.ExecutorService)
        (clojure.core/import 'java.util.concurrent.Semaphore)
        (clojure.core/import 'java.util.concurrent.TimeUnit)
        (clojure.core/import 'java.io.Closeable))))
  (set! *warn-on-reflection* true)
  (defn root-cause
    ([x]
      (if (instance? java.lang.Throwable x)
        (let [cause (.getCause ^java.lang.Throwable x)] (if cause (recur cause) x))
        x)))
  (reset-meta!
    #'root-cause
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'root-cause :ns *ns*))
  (defn retry-cause ([result] (.getName (class (root-cause result)))))
  (reset-meta!
    #'retry-cause
    (assoc {:arglists (clojure.core/list ['result]), :column (int 1)} :name 'retry-cause :ns *ns*))
  (def notify-retry
   (fn notify_retry
     ([result metric backoff attempts max_retries]
       (monitor/add-stat metric backoff)
       (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
         (when (.isInfoEnabled ^org.slf4j.Logger logger)
           (.info
             ^org.slf4j.Logger logger
             (logger/process
               {:event :kv-cluster/retry,
                metric backoff,
                :attempts attempts,
                :max-retries max_retries,
                :cause (retry-cause result)})))
         nil))))
  (reset-meta!
    #'notify-retry
    (assoc
      {:arglists (clojure.core/list ['result 'metric 'backoff 'attempts 'max-retries]),
       :column (int 1)}
      :name
      'notify-retry
      :ns
      *ns*))
  (defn exponential-backoff
    ([n]
      (if (zero? n)
        0
        (java.lang.Double/valueOf
          (double (* 50 (java.lang.Math/pow (double 2) (double (dec n)))))))))
  (reset-meta!
    #'exponential-backoff
    (assoc
      {:private true, :arglists (clojure.core/list ['n]), :column (int 1)}
      :name
      'exponential-backoff
      :ns
      *ns*))
  (defn linear-backoff ([n] (* 50 n)))
  (reset-meta!
    #'linear-backoff
    (assoc
      {:private true, :arglists (clojure.core/list ['n]), :column (int 1)}
      :name
      'linear-backoff
      :ns
      *ns*))
  (def retry-fn
   (fn retry_fn
     ([sem metric nested group_ref backoff f]
       (binding [kv/*retry* (partial retry-fn sem metric true group_ref backoff)]
         (let [sem? (and (not nested) (= :exponential backoff))
               max_retries (let [G__10379 backoff] (case G__10379 :linear 20 :exponential 9))
               tid (.getId (java.lang.Thread/currentThread))
               permit? (when sem?
                         (let [permit? (.tryAcquire
                                         ^java.util.concurrent.Semaphore sem
                                         20
                                         TimeUnit/SECONDS)]
                           (when-not permit?
                             (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                 (.info
                                   ^org.slf4j.Logger logger
                                   (logger/process {:event :kv-cluster/semaphore-timeout})))
                               nil)
                             (monitor/alarm :StorageSemaphoreTimeout))
                           permit?))]
           (try
             (loop [retries 0 elapsed 0]
               (do
                 (when (= :exponential backoff) (swap! group_ref assoc (long tid) (long retries)))
                 (let [delay (let [G__10383 backoff]
                               (case
                                 G__10383
                                 :linear
                                 (linear-backoff (long retries))
                                 :exponential
                                 (exponential-backoff (apply max 0 (vals (deref group_ref))))))
                       _ (when (clojure.lang.Numbers/isPos delay)
                           (java.lang.Thread/sleep (long ^java.lang.Number delay))
                           nil)
                       vec__10380 (let [start (java.lang.System/nanoTime)
                                        result (try
                                                 (^clojure.lang.IFn f)
                                                 (catch java.lang.Throwable e e))]
                                    [(long (quot (- (java.lang.System/nanoTime) start) 1000000))
                                     result])
                       ms (nth vec__10380 (int 0) nil)
                       result (nth vec__10380 (int 1) nil)
                       elapsed (+ elapsed (long ^java.lang.Number ms))]
                   (if (and
                         (instance? java.lang.Throwable result)
                         (kv/retryable? result)
                         (< retries max_retries)
                         (or (< retries 3) (< elapsed 10000)))
                     (do
                       (notify-retry result metric delay (long retries) (long max_retries))
                       (recur (inc retries) elapsed))
                     (common/return-or-throw result)))))
             (finally
               (do
                 (when permit? (.release ^java.util.concurrent.Semaphore sem))
                 (swap! group_ref dissoc (long tid))))))))))
  (reset-meta!
    #'retry-fn
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'sem {:tag 'Semaphore}) 'metric 'nested 'group-ref 'backoff 'f]),
       :column (int 1)}
      :name
      'retry-fn
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.kv-cluster" "shared-pool-ref")
    {:private true, :column (int 1)})
  (let [v__6812__auto__ #'shared-pool-ref]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.kv-cluster" "shared-pool-ref")
        {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.kv-cluster" "shared-pool-ref")
        (delay
          (common/thread-pool
            {:nthreads (config/property "datomic.writeConcurrency"), :name "shared-storage"})))
      #'shared-pool-ref))
  (defn shared-pool ([] (deref shared-pool-ref)))
  (reset-meta!
    #'shared-pool
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'shared-pool :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.kv-cluster" "delete-pool-ref")
    {:private true, :column (int 1)})
  (let [v__6812__auto__ #'delete-pool-ref]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.kv-cluster" "delete-pool-ref")
        {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.kv-cluster" "delete-pool-ref")
        (delay
          (common/thread-pool
            {:nthreads (config/property "datomic.deleteConcurrency"), :name "delete"})))
      #'delete-pool-ref))
  (.setMeta (clojure.lang.RT/var "datomic.kv-cluster" "val-gets-ref") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.kv-cluster" "val-gets-ref") (atom 0))
  (def ref-identity-keys [:id :rev :key])
  (reset-meta! #'ref-identity-keys (assoc {:column (int 1)} :name 'ref-identity-keys :ns *ns*))
  (defn same-ref?
    ([r1 r2]
      (= (common/require-keys r1 ref-identity-keys) (common/require-keys r2 ref-identity-keys))))
  (reset-meta!
    #'same-ref?
    (assoc {:arglists (clojure.core/list ['r1 'r2]), :column (int 1)} :name 'same-ref? :ns *ns*))
  (def mark-pod-garbage
   (fn mark_pod_garbage
     ([cs tail_keys_ref]
       (events/publish
         {:key :datomic.garbage/mark, :cluster cs, :garbage (deref tail_keys_ref)}))))
  (reset-meta!
    #'mark-pod-garbage
    (assoc
      {:private true, :arglists (clojure.core/list ['cs 'tail-keys-ref]), :column (int 1)}
      :name
      'mark-pod-garbage
      :ns
      *ns*))
  (deftype
    KVCluster
    [kvs
     path_map
     exec
     retrying_write
     retrying_read
     retrying_delete
     protocol
     protocol_nsec_k
     pod_garbage_handler]
    datomic.cluster.Dbid
    datomic.cluster.Get2
    datomic.cluster.ClusteredStore
    java.io.Closeable
    datomic.cluster.RefClusterStore
    (get-val2 [this val_key _] (cluster/get-val this val_key))
    (-get-ref-store [this] kvs)
    (update-pod*
      [this pod_key rev etag buf metamap]
      (let [tailid (if buf (str (common/rand-uuid)) etag)]
        (df/-future-with-channel-impl
          (fn fn__10525
            ([]
              (let [m_10526 {:event :kv-cluster/update-pod,
                             :pod-key pod_key,
                             :tailid tailid,
                             :rev rev,
                             :etag etag,
                             :bufsize
                             (when buf
                               (java.lang.Integer/valueOf
                                 (int (.remaining ^java.nio.Buffer buf))))}
                    ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.kv-cluster")]
                                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                        (.info
                                          ^org.slf4j.Logger logger
                                          (logger/process (assoc m_10526 :phase :begin))))
                                      nil)
                    start__8553__auto__ (java.lang.System/nanoTime)
                    result__8554__auto__ (try
                                           {:returned
                                            (let [tail {:id tailid, :v buf}
                                                  tail (if etag (assoc tail :prev etag) tail)
                                                  reset? (and (nil? etag) (not (zero? rev)))
                                                  oldtail (when reset?
                                                            (:tail
                                                              (^clojure.lang.IFn retrying_read
                                                                :linear
                                                                (fn 
                                                                  fn__10530
                                                                  ([]
                                                                    (kv/get kvs pod_key true))))))]
                                              (when-not (or buf etag)
                                                (throw
                                                  (java.lang.AssertionError.
                                                    (str
                                                      "Assert failed: "
                                                      (pr-str
                                                        (clojure.core/list 'or 'buf 'etag))))))
                                              (if (or
                                                    (nil? buf)
                                                    (^clojure.lang.IFn retrying_write
                                                      :linear
                                                      (fn fn__10532 ([] (kv/put kvs tail)))))
                                                (let [item (merge
                                                             metamap
                                                             {:id pod_key,
                                                              :rev rev,
                                                              :tail tailid})]
                                                  (if (^clojure.lang.IFn retrying_write
                                                        :linear
                                                        (fn fn__10534
                                                          ([]
                                                            (kv/put
                                                              kvs
                                                              (merge
                                                                item
                                                                {:ensure
                                                                 (merge
                                                                   (if (nil? etag) {} {:tail etag})
                                                                   (if
                                                                     (zero? rev)
                                                                     {:id nil}
                                                                     {:rev (dec rev)}))})))))
                                                    (do
                                                      (when (and reset? oldtail)
                                                        (future-call
                                                          (fn fn__10536
                                                            ([]
                                                              (let 
                                                                [tail_keys_ref
                                                                 (delay
                                                                   (loop 
                                                                     [ks [oldtail]]
                                                                     (let 
                                                                       [map__10538
                                                                        (^clojure.lang.IFn retrying_read
                                                                          :exponential
                                                                          (fn 
                                                                            fn__10539
                                                                            ([]
                                                                              (kv/get
                                                                                kvs
                                                                                (peek ks)
                                                                                false))))
                                                                        map__10538
                                                                        (if
                                                                          (seq? map__10538)
                                                                          (if
                                                                            (next map__10538)
                                                                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                              (to-array
                                                                                map__10538))
                                                                            (if
                                                                              (seq map__10538)
                                                                              (first map__10538)
                                                                              {}))
                                                                          map__10538)
                                                                        v (get map__10538 :v)
                                                                        prev
                                                                        (get map__10538 :prev)]
                                                                       (if
                                                                         prev
                                                                         (recur (conj ks prev))
                                                                         ks))))]
                                                                (^clojure.lang.IFn pod_garbage_handler
                                                                  this
                                                                  tail_keys_ref))))))
                                                      {:rev rev, :etag tailid, :buf buf})
                                                    (let [temp__5802__auto__ (^clojure.lang.IFn retrying_read
                                                                               :linear
                                                                               (fn 
                                                                                 fn__10543
                                                                                 ([]
                                                                                   (kv/get
                                                                                     kvs
                                                                                     pod_key
                                                                                     true))))]
                                                      (if temp__5802__auto__
                                                        (let [map__10545 temp__5802__auto__
                                                              map__10545
                                                              (if
                                                                (seq? map__10545)
                                                                (if
                                                                  (next map__10545)
                                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                    (to-array map__10545))
                                                                  (if
                                                                    (seq map__10545)
                                                                    (first map__10545)
                                                                    {}))
                                                                map__10545)
                                                              nrev (get map__10545 :rev)
                                                              ntail (get map__10545 :tail)]
                                                          (if (and (= rev nrev) (= tailid ntail))
                                                            (do
                                                              (let 
                                                                [logger
                                                                 (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.kv-cluster")]
                                                                (when
                                                                  (.isInfoEnabled
                                                                    ^org.slf4j.Logger logger)
                                                                  (.info
                                                                    ^org.slf4j.Logger logger
                                                                    (logger/process
                                                                      {:event
                                                                       :kv-cluster/update-pod,
                                                                       :pod-update-resume true,
                                                                       :pod-key pod_key,
                                                                       :rev rev})))
                                                                nil)
                                                              {:rev rev, :etag tailid, :buf buf})
                                                            {:failed :conflict}))
                                                        {:failed :conflict}))))
                                                (do
                                                  (throw
                                                    (java.lang.Error.
                                                      "kv write failed in update-pod"))
                                                  1)))}
                                           (catch
                                             java.lang.Throwable
                                             t__8555__auto__
                                             {:threw t__8555__auto__}))
                    elapsed_10527 (- (java.lang.System/nanoTime) start__8553__auto__)
                    msec_10528 (logger/format-as-msec (long elapsed_10527))]
                (monitor/add-stat :PodUpdateMsec msec_10528)
                (let [endmsg__8556__auto__ (merge
                                             (assoc m_10526 :msec msec_10528 :phase :end)
                                             (when (:threw result__8554__auto__)
                                               {:threw (class (:threw result__8554__auto__))}))
                      logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                  nil)
                (if (contains? result__8554__auto__ :returned)
                  (:returned result__8554__auto__)
                  (do (throw (:threw result__8554__auto__)) nil))))))))
    (get-pod
      [this pod_key]
      (df/-future-with-channel-impl
        (fn fn__10502
          ([]
            (let [m_10503 {:event :kv-cluster/get-pod, :pod-key pod_key}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10503 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (let [temp__5804__auto__ (^clojure.lang.IFn retrying_read
                                                                     :linear
                                                                     (fn 
                                                                       fn__10507
                                                                       ([]
                                                                         (kv/get
                                                                           kvs
                                                                           pod_key
                                                                           true))))]
                                            (when temp__5804__auto__
                                              (let [pref temp__5804__auto__
                                                    map__10509 pref
                                                    map__10509 (if
                                                                 (seq? map__10509)
                                                                 (if
                                                                   (next map__10509)
                                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                     (to-array map__10509))
                                                                   (if
                                                                     (seq map__10509)
                                                                     (first map__10509)
                                                                     {}))
                                                                 map__10509)
                                                    rev (get map__10509 :rev)
                                                    tail (get map__10509 :tail)
                                                    bufs (loop [tail tail ret nil]
                                                           (let 
                                                             [map__10511
                                                              (^clojure.lang.IFn retrying_read
                                                                :linear
                                                                (fn 
                                                                  fn__10512
                                                                  ([] (kv/get kvs tail false))))
                                                              map__10511
                                                              (if
                                                                (seq? map__10511)
                                                                (if
                                                                  (next map__10511)
                                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                    (to-array map__10511))
                                                                  (if
                                                                    (seq map__10511)
                                                                    (first map__10511)
                                                                    {}))
                                                                map__10511)
                                                              t map__10511
                                                              v (get map__10511 :v)
                                                              prev (get map__10511 :prev)
                                                              _
                                                              (when-not
                                                                v
                                                                (throw
                                                                  (ex-info
                                                                    "Key missing in storage"
                                                                    {:tail tail,
                                                                     :t t,
                                                                     :v v,
                                                                     :prev prev}))
                                                                nil)
                                                              ret (cons v ret)]
                                                             (if prev (recur prev ret) ret)))
                                                    len (reduce
                                                          +
                                                          (map
                                                            (fn 
                                                              fn__10515
                                                              ([p1__10405#]
                                                                (java.lang.Integer/valueOf
                                                                  (int
                                                                    (.remaining
                                                                      ^java.nio.Buffer p1__10405#)))))
                                                            bufs))
                                                    arr (byte-array len)]
                                                (let [logger (org.slf4j.LoggerFactory/getLogger
                                                               "datomic.kv-cluster")]
                                                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                    (.debug
                                                      ^org.slf4j.Logger logger
                                                      (logger/process
                                                        {:event :kv-cluster/pod-size,
                                                         :bytes len,
                                                         :chunks
                                                         (java.lang.Integer/valueOf
                                                           (int (count bufs)))})))
                                                  nil)
                                                (loop [i 0 bs (seq bufs)]
                                                  (let [temp__5804__auto__ (first bs)]
                                                    (if temp__5804__auto__
                                                      (let [b temp__5804__auto__]
                                                        (.get
                                                          (.duplicate ^java.nio.ByteBuffer b)
                                                          ^bytes arr
                                                          (int i)
                                                          (int (.remaining ^java.nio.Buffer b)))
                                                        (recur
                                                          (+ i (.remaining ^java.nio.Buffer b))
                                                          (next bs)))
                                                      1)))
                                                (merge
                                                  (dissoc pref :id :tail)
                                                  {:rev rev,
                                                   :etag tail,
                                                   :buf (ByteBuffer/wrap ^bytes arr)}))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_10504 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_10505 (logger/format-as-msec (long elapsed_10504))]
              (monitor/add-stat :PodGetMsec msec_10505)
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_10503 :msec msec_10505 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (do (throw (:threw result__8554__auto__)) nil)))))))
    (get-pod-meta
      [this pod_key]
      (df/-future-with-channel-impl
        (fn fn__10489
          ([]
            (let [m_10490 {:event :kv-cluster/get-pod-meta, :pod-key pod_key}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10490 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (^clojure.lang.IFn retrying_read
                                            :linear
                                            (fn fn__10494 ([] (kv/get kvs pod_key true))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_10491 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_10492 (logger/format-as-msec (long elapsed_10491))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_10490 :msec msec_10492 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (do (throw (:threw result__8554__auto__)) nil)))))))
    (set-ref
      [this ref_key rev vkey]
      (df/-future-with-channel-impl
        (fn fn__10474
          ([]
            (let [m_10475 {:event :kv-cluster/set-ref, :ref-key ref_key, :rev rev, :vkey vkey}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                      (.debug
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10475 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (let [item {:id ref_key, :rev rev, :key vkey}]
                                            (if (=
                                                  :ok
                                                  (^clojure.lang.IFn retrying_write
                                                    :linear
                                                    (fn fn__10479
                                                      ([]
                                                        (kv/put
                                                          kvs
                                                          (merge
                                                            item
                                                            {:ensure
                                                             (if
                                                               (zero? rev)
                                                               {:id nil}
                                                               {:rev (dec rev)})}))))))
                                              :ok
                                              (if (same-ref?
                                                    item
                                                    (^clojure.lang.IFn retrying_read
                                                      :linear
                                                      (fn fn__10481
                                                        ([] (kv/get kvs ref_key true)))))
                                                (do
                                                  (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                      (.info
                                                        ^org.slf4j.Logger logger
                                                        (logger/process
                                                          {:event :kv-cluster/set-ref-resume,
                                                           :ref-key ref_key,
                                                           :rev rev})))
                                                    nil)
                                                  :ok)
                                                :conflict)))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_10476 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_10477 (logger/format-as-msec (long elapsed_10476))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_10475 :msec msec_10477 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                  (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (do (throw (:threw result__8554__auto__)) nil)))))))
    (get-ref
      [this ref_key]
      (df/-future-with-channel-impl
        (fn fn__10460
          ([]
            (let [m_10461 {:event :kv-cluster/get-ref, :ref-key ref_key}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                      (.debug
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10461 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (^clojure.lang.IFn retrying_read
                                            :linear
                                            (fn fn__10465
                                              ([]
                                                (let [temp__5804__auto__ (kv/get kvs ref_key true)]
                                                  (when temp__5804__auto__
                                                    (let [ret temp__5804__auto__]
                                                      (select-keys ret [:key :rev])))))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_10462 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_10463 (logger/format-as-msec (long elapsed_10462))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_10461 :msec msec_10463 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                  (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (do (throw (:threw result__8554__auto__)) nil)))))))
    (delete-reference
      [this key]
      (df/-future-with-channel-impl
        (fn fn__10447
          ([]
            (let [m_10448 {:event :kv-cluster/delete-reference, :key key}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10448 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (^clojure.lang.IFn retrying_delete
                                            (fn fn__10452 ([] (kv/delete kvs key true))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_10449 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_10450 (logger/format-as-msec (long elapsed_10449))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_10448 :msec msec_10450 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (do (throw (:threw result__8554__auto__)) nil)))))))
    (delete
      [this key]
      (df/-future-with-channel-impl
        (deref delete-pool-ref)
        (fn fn__10434
          ([]
            (let [m_10435 {:event :kv-cluster/delete, :key key}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10435 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (^clojure.lang.IFn retrying_delete
                                            (fn fn__10439 ([] (kv/delete kvs key false))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_10436 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_10437 (logger/format-as-msec (long elapsed_10436))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_10435 :msec msec_10437 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (do (throw (:threw result__8554__auto__)) nil)))))))
    (get-val
      [this val_key]
      (let [start (java.lang.System/nanoTime)]
        (io-stats/inc! protocol)
        (df/-future-with-channel-impl
          (fn fn__10419
            ([]
              (let [m_10420 {:event :kv-cluster/get-val, :val-key val_key}
                    ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.kv-cluster")]
                                      (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                        (.debug
                                          ^org.slf4j.Logger logger
                                          (logger/process (assoc m_10420 :phase :begin))))
                                      nil)
                    start__8553__auto__ (java.lang.System/nanoTime)
                    result__8554__auto__ (try
                                           {:returned
                                            (^clojure.lang.IFn retrying_read
                                              :exponential
                                              (fn fn__10424
                                                ([]
                                                  (let [temp__5804__auto__ (kv/get
                                                                             kvs
                                                                             val_key
                                                                             false)]
                                                    (when temp__5804__auto__
                                                      (let [map__10425 temp__5804__auto__
                                                            map__10425
                                                            (if
                                                              (seq? map__10425)
                                                              (if
                                                                (next map__10425)
                                                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                  (to-array map__10425))
                                                                (if
                                                                  (seq map__10425)
                                                                  (first map__10425)
                                                                  {}))
                                                              map__10425)
                                                            buf (get map__10425 :v)]
                                                        (io-stats/inc!
                                                          protocol_nsec_k
                                                          (- (java.lang.System/nanoTime) start))
                                                        (monitor/add-stat
                                                          :StorageGetBytes
                                                          (java.lang.Integer/valueOf
                                                            (int
                                                              (.remaining ^java.nio.Buffer buf))))
                                                        (swap! val-gets-ref inc)
                                                        {:buf buf}))))))}
                                           (catch
                                             java.lang.Throwable
                                             t__8555__auto__
                                             {:threw t__8555__auto__}))
                    elapsed_10421 (- (java.lang.System/nanoTime) start__8553__auto__)
                    msec_10422 (logger/format-as-msec (long elapsed_10421))]
                (monitor/add-stat :StorageGetMsec msec_10422)
                (let [endmsg__8556__auto__ (merge
                                             (assoc m_10420 :msec msec_10422 :phase :end)
                                             (when (:threw result__8554__auto__)
                                               {:threw (class (:threw result__8554__auto__))}))
                      logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                    (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                  nil)
                (if (contains? result__8554__auto__ :returned)
                  (:returned result__8554__auto__)
                  (do (throw (:threw result__8554__auto__)) nil))))))))
    (create-val [this val_key buf] (cluster/create-val this 3 val_key buf))
    (create-val
      [this priority val_key buf]
      (let [backoff (if (< priority 3) :linear :exponential)
            doit (fn doit
                   ([]
                     (monitor/add-stat
                       :StoragePutBytes
                       (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer buf))))
                     (let [m_10408 {:event :kv-cluster/create-val,
                                    :val-key val_key,
                                    :bufsize
                                    (java.lang.Integer/valueOf
                                      (int (.remaining ^java.nio.Buffer buf)))}
                           start__8553__auto__ (java.lang.System/nanoTime)
                           result__8554__auto__ (try
                                                  {:returned
                                                   (^clojure.lang.IFn retrying_write
                                                     backoff
                                                     (fn fn__10412
                                                       ([]
                                                         (when (kv/put kvs {:id val_key, :v buf})
                                                           :created))))}
                                                  (catch
                                                    java.lang.Throwable
                                                    t__8555__auto__
                                                    {:threw t__8555__auto__}))
                           elapsed_10409 (- (java.lang.System/nanoTime) start__8553__auto__)
                           msec_10410 (logger/format-as-msec (long elapsed_10409))]
                       (monitor/add-stat :StoragePutMsec msec_10410)
                       (let [endmsg__8556__auto__ (merge
                                                    (assoc m_10408 :msec msec_10410 :phase :end)
                                                    (when (:threw result__8554__auto__)
                                                      {:threw
                                                       (class (:threw result__8554__auto__))}))
                             logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                           (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                         nil)
                       (if (contains? result__8554__auto__ :returned)
                         (:returned result__8554__auto__)
                         (do (throw (:threw result__8554__auto__)) nil)))))]
        (if (< priority 3)
          (df/-future-with-channel-impl doit)
          (df/-future-with-channel-impl exec doit))))
    (dbId [this] (:db path_map))
    (^void close
      [this]
      (do
        (when-not (= (shared-pool) exec) (.shutdown ^java.util.concurrent.ExecutorService exec))
        (cluster/close kvs)
        nil)))
  (clojure.core/import 'datomic.kv_cluster.KVCluster)
  (def ->KVCluster
   (fn __GT_KVCluster
     ([kvs
       path_map
       exec
       retrying_write
       retrying_read
       retrying_delete
       protocol
       protocol_nsec_k
       pod_garbage_handler]
       (datomic.kv_cluster.KVCluster.
         kvs
         path_map
         exec
         retrying_write
         retrying_read
         retrying_delete
         protocol
         protocol_nsec_k
         pod_garbage_handler))))
  (reset-meta!
    #'->KVCluster
    (assoc
      {:arglists
       (clojure.core/list
         ['kvs
          'path-map
          'exec
          'retrying-write
          'retrying-read
          'retrying-delete
          'protocol
          'protocol-nsec-k
          'pod-garbage-handler]),
       :column (int 1)}
      :name
      '->KVCluster
      :ns
      *ns*))
  (def PRIORITY_WRITE_CONCURRENCY 2)
  (reset-meta!
    #'PRIORITY_WRITE_CONCURRENCY
    (assoc {:const true, :column (int 1)} :name 'PRIORITY_WRITE_CONCURRENCY :ns *ns*))
  (def kv-cluster
   (fn kv_cluster
     ([kvs p__10562]
       (let [map__10563 p__10562
             map__10563 (if (seq? map__10563)
                          (if (next map__10563)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__10563))
                            (if (seq map__10563) (first map__10563) {}))
                          map__10563)
             x map__10563
             tenant (get map__10563 :tenant)
             db_id (get map__10563 :db-id)
             write_concurrency (get map__10563 :write-concurrency)
             read_concurrency (get map__10563 :read-concurrency)
             shared_pool? (get map__10563 :shared-pool? true)
             protocol (get map__10563 :protocol :kvc)
             pod_garbage_handler (get map__10563 :pod-garbage-handler mark-pod-garbage)
             retrying_delete (get map__10563 :retrying-delete)
             write_concurrency (or write_concurrency (config/property "datomic.writeConcurrency"))
             read_concurrency (or read_concurrency (config/property "datomic.readConcurrency"))
             retrying_delete (or
                               retrying_delete
                               (partial
                                 retry-fn
                                 (java.util.concurrent.Semaphore.
                                   (int (config/property "datomic.deleteConcurrency"))
                                   (boolean (.booleanValue true)))
                                 :StorageDeleteBackoffMsec
                                 false
                                 (atom {})
                                 :exponential))
             protocol_nsec_k (keyword (str (name protocol) "-ns"))
             idx (atom 0)
             exec (if shared_pool?
                    (shared-pool)
                    (common/thread-pool {:nthreads write_concurrency, :name "storage"}))]
         (datomic.kv_cluster.KVCluster.
           kvs
           {:tenant tenant, :db db_id}
           exec
           (partial
             retry-fn
             (java.util.concurrent.Semaphore.
               (int (+ write_concurrency 2))
               (boolean (.booleanValue true)))
             :StoragePutBackoffMsec
             false
             (atom {}))
           (partial
             retry-fn
             (java.util.concurrent.Semaphore.
               (int ^java.lang.Number read_concurrency)
               (boolean (.booleanValue true)))
             :StorageGetBackoffMsec
             false
             (atom {}))
           retrying_delete
           protocol
           protocol_nsec_k
           pod_garbage_handler)))))
  (reset-meta!
    #'kv-cluster
    (assoc
      {:arglists
       (clojure.core/list
         ['kvs
          {:keys
           ['tenant
            'db-id
            'write-concurrency
            'read-concurrency
            'shared-pool?
            'protocol
            'pod-garbage-handler
            'retrying-delete],
           :or {'shared-pool? true, 'protocol :kvc, 'pod-garbage-handler 'mark-pod-garbage},
           :as 'x}]),
       :column (int 1)}
      :name
      'kv-cluster
      :ns
      *ns*)))