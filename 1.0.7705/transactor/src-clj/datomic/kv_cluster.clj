(do
  (clojure.core/in-ns 'datomic.kv-cluster)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.kv-cluster)
    {:doc
     "Adapts a KVStore to Datomic's clustered-store contract. Bounds concurrent reads and writes, retries transient failures with coordinated backoff, records storage latency and throughput, and implements immutable values plus revisioned refs and appendable pods."})
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
  (defn notify-retry
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
        nil)))
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
  ;; Bounds the outer storage call, retries retryable failures, and coordinates exponential delay
  ;; across concurrent writers so throttled storage is not flooded by independent retries.
  (defn retry-fn
    ([sem metric nested group_ref backoff f]
      (binding [kv/*retry* (partial retry-fn sem metric true group_ref backoff)]
        (let [sem? (and (not nested) (= :exponential backoff))
              max_retries (let [G__10519 backoff] (case G__10519 :linear 20 :exponential 9))
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
                (let [delay (let [G__10523 backoff]
                              (case
                                G__10523
                                :linear
                                (linear-backoff (long retries))
                                :exponential
                                (exponential-backoff (apply max 0 (vals (deref group_ref))))))
                      _ (when (clojure.lang.Numbers/isPos delay)
                          (java.lang.Thread/sleep (long ^java.lang.Number delay))
                          nil)
                      vec__10520 (let [start (java.lang.System/nanoTime)
                                       result (try
                                                (^clojure.lang.IFn f)
                                                (catch java.lang.Throwable e e))]
                                   [(long (quot (- (java.lang.System/nanoTime) start) 1000000))
                                    result])
                      ms (nth vec__10520 (int 0) nil)
                      result (nth vec__10520 (int 1) nil)
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
                (swap! group_ref dissoc (long tid)))))))))
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
  (let [v__6837__auto__ #'shared-pool-ref]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
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
  (let [v__6837__auto__ #'delete-pool-ref]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
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
  (defn mark-pod-garbage
    ([cs tail_keys_ref]
      (events/publish {:key :datomic.garbage/mark, :cluster cs, :garbage (deref tail_keys_ref)})))
  (reset-meta!
    #'mark-pod-garbage
    (assoc
      {:private true, :arglists (clojure.core/list ['cs 'tail-keys-ref]), :column (int 1)}
      :name
      'mark-pod-garbage
      :ns
      *ns*))
  ;; Implements values, refs, and pods over a KVStore. Pod updates write a new immutable tail and
  ;; conditionally publish it by revision, leaving replaced tails for asynchronous garbage marking.
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
          (fn fn__10665
            ([]
              (let [m_10666 {:event :kv-cluster/update-pod,
                             :pod-key pod_key,
                             :tailid tailid,
                             :rev rev,
                             :etag etag,
                             :bufsize
                             (when buf
                               (java.lang.Integer/valueOf
                                 (int (.remaining ^java.nio.Buffer buf))))}
                    ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.kv-cluster")]
                                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                        (.info
                                          ^org.slf4j.Logger logger
                                          (logger/process (assoc m_10666 :phase :begin))))
                                      nil)
                    start__8599__auto__ (java.lang.System/nanoTime)
                    result__8600__auto__ (try
                                           {:returned
                                            (let [tail {:id tailid, :v buf}
                                                  tail (if etag (assoc tail :prev etag) tail)
                                                  reset? (and (nil? etag) (not (zero? rev)))
                                                  oldtail (when reset?
                                                            (:tail
                                                              (^clojure.lang.IFn retrying_read
                                                                :linear
                                                                (fn
                                                                  fn__10670
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
                                                      (fn fn__10672 ([] (kv/put kvs tail)))))
                                                (let [item (merge
                                                             metamap
                                                             {:id pod_key,
                                                              :rev rev,
                                                              :tail tailid})]
                                                  (if (^clojure.lang.IFn retrying_write
                                                        :linear
                                                        (fn fn__10674
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
                                                          (fn fn__10676
                                                            ([]
                                                              (let
                                                                [tail_keys_ref
                                                                 (delay
                                                                   (loop
                                                                     [ks [oldtail]]
                                                                     (let
                                                                       [map__10678
                                                                        (^clojure.lang.IFn retrying_read
                                                                          :exponential
                                                                          (fn
                                                                            fn__10679
                                                                            ([]
                                                                              (kv/get
                                                                                kvs
                                                                                (peek ks)
                                                                                false))))
                                                                        map__10678
                                                                        (if
                                                                          (seq? map__10678)
                                                                          (if
                                                                            (next map__10678)
                                                                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                              (to-array
                                                                                map__10678))
                                                                            (if
                                                                              (seq map__10678)
                                                                              (first map__10678)
                                                                              {}))
                                                                          map__10678)
                                                                        v (get map__10678 :v)
                                                                        prev
                                                                        (get map__10678 :prev)]
                                                                       (if
                                                                         prev
                                                                         (recur (conj ks prev))
                                                                         ks))))]
                                                                (^clojure.lang.IFn pod_garbage_handler
                                                                  this
                                                                  tail_keys_ref))))))
                                                      {:rev rev, :etag tailid, :buf buf})
                                                    (let [temp__5823__auto__ (^clojure.lang.IFn retrying_read
                                                                               :linear
                                                                               (fn
                                                                                 fn__10683
                                                                                 ([]
                                                                                   (kv/get
                                                                                     kvs
                                                                                     pod_key
                                                                                     true))))]
                                                      (if temp__5823__auto__
                                                        (let [map__10685 temp__5823__auto__
                                                              map__10685
                                                              (if
                                                                (seq? map__10685)
                                                                (if
                                                                  (next map__10685)
                                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                    (to-array map__10685))
                                                                  (if
                                                                    (seq map__10685)
                                                                    (first map__10685)
                                                                    {}))
                                                                map__10685)
                                                              nrev (get map__10685 :rev)
                                                              ntail (get map__10685 :tail)]
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
                                             t__8601__auto__
                                             {:threw t__8601__auto__}))
                    elapsed_10667 (- (java.lang.System/nanoTime) start__8599__auto__)
                    msec_10668 (logger/format-as-msec (long elapsed_10667))]
                (monitor/add-stat :PodUpdateMsec msec_10668)
                (let [endmsg__8602__auto__ (merge
                                             (assoc m_10666 :msec msec_10668 :phase :end)
                                             (when (:threw result__8600__auto__)
                                               {:threw (class (:threw result__8600__auto__))}))
                      logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                  nil)
                (if (contains? result__8600__auto__ :returned)
                  (:returned result__8600__auto__)
                  (do (throw (:threw result__8600__auto__)) nil))))))))
    (get-pod
      [this pod_key]
      (df/-future-with-channel-impl
        (fn fn__10642
          ([]
            (let [m_10643 {:event :kv-cluster/get-pod, :pod-key pod_key}
                  ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10643 :phase :begin))))
                                    nil)
                  start__8599__auto__ (java.lang.System/nanoTime)
                  result__8600__auto__ (try
                                         {:returned
                                          (let [temp__5825__auto__ (^clojure.lang.IFn retrying_read
                                                                     :linear
                                                                     (fn
                                                                       fn__10647
                                                                       ([]
                                                                         (kv/get
                                                                           kvs
                                                                           pod_key
                                                                           true))))]
                                            (when temp__5825__auto__
                                              (let [pref temp__5825__auto__
                                                    map__10649 pref
                                                    map__10649 (if
                                                                 (seq? map__10649)
                                                                 (if
                                                                   (next map__10649)
                                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                     (to-array map__10649))
                                                                   (if
                                                                     (seq map__10649)
                                                                     (first map__10649)
                                                                     {}))
                                                                 map__10649)
                                                    rev (get map__10649 :rev)
                                                    tail (get map__10649 :tail)
                                                    bufs (loop [tail tail ret nil]
                                                           (let
                                                             [map__10651
                                                              (^clojure.lang.IFn retrying_read
                                                                :linear
                                                                (fn
                                                                  fn__10652
                                                                  ([] (kv/get kvs tail false))))
                                                              map__10651
                                                              (if
                                                                (seq? map__10651)
                                                                (if
                                                                  (next map__10651)
                                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                    (to-array map__10651))
                                                                  (if
                                                                    (seq map__10651)
                                                                    (first map__10651)
                                                                    {}))
                                                                map__10651)
                                                              t map__10651
                                                              v (get map__10651 :v)
                                                              prev (get map__10651 :prev)
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
                                                              fn__10655
                                                              ([p1__10545#]
                                                                (java.lang.Integer/valueOf
                                                                  (int
                                                                    (.remaining
                                                                      ^java.nio.Buffer p1__10545#)))))
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
                                                  (let [temp__5825__auto__ (first bs)]
                                                    (if temp__5825__auto__
                                                      (let [b temp__5825__auto__]
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
                                           t__8601__auto__
                                           {:threw t__8601__auto__}))
                  elapsed_10644 (- (java.lang.System/nanoTime) start__8599__auto__)
                  msec_10645 (logger/format-as-msec (long elapsed_10644))]
              (monitor/add-stat :PodGetMsec msec_10645)
              (let [endmsg__8602__auto__ (merge
                                           (assoc m_10643 :msec msec_10645 :phase :end)
                                           (when (:threw result__8600__auto__)
                                             {:threw (class (:threw result__8600__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                nil)
              (if (contains? result__8600__auto__ :returned)
                (:returned result__8600__auto__)
                (do (throw (:threw result__8600__auto__)) nil)))))))
    (get-pod-meta
      [this pod_key]
      (df/-future-with-channel-impl
        (fn fn__10629
          ([]
            (let [m_10630 {:event :kv-cluster/get-pod-meta, :pod-key pod_key}
                  ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10630 :phase :begin))))
                                    nil)
                  start__8599__auto__ (java.lang.System/nanoTime)
                  result__8600__auto__ (try
                                         {:returned
                                          (^clojure.lang.IFn retrying_read
                                            :linear
                                            (fn fn__10634 ([] (kv/get kvs pod_key true))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8601__auto__
                                           {:threw t__8601__auto__}))
                  elapsed_10631 (- (java.lang.System/nanoTime) start__8599__auto__)
                  msec_10632 (logger/format-as-msec (long elapsed_10631))]
              (let [endmsg__8602__auto__ (merge
                                           (assoc m_10630 :msec msec_10632 :phase :end)
                                           (when (:threw result__8600__auto__)
                                             {:threw (class (:threw result__8600__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                nil)
              (if (contains? result__8600__auto__ :returned)
                (:returned result__8600__auto__)
                (do (throw (:threw result__8600__auto__)) nil)))))))
    (set-ref
      [this ref_key rev vkey]
      (df/-future-with-channel-impl
        (fn fn__10614
          ([]
            (let [m_10615 {:event :kv-cluster/set-ref, :ref-key ref_key, :rev rev, :vkey vkey}
                  ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                      (.debug
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10615 :phase :begin))))
                                    nil)
                  start__8599__auto__ (java.lang.System/nanoTime)
                  result__8600__auto__ (try
                                         {:returned
                                          (let [item {:id ref_key, :rev rev, :key vkey}]
                                            (if (=
                                                  :ok
                                                  (^clojure.lang.IFn retrying_write
                                                    :linear
                                                    (fn fn__10619
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
                                                      (fn fn__10621
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
                                           t__8601__auto__
                                           {:threw t__8601__auto__}))
                  elapsed_10616 (- (java.lang.System/nanoTime) start__8599__auto__)
                  msec_10617 (logger/format-as-msec (long elapsed_10616))]
              (let [endmsg__8602__auto__ (merge
                                           (assoc m_10615 :msec msec_10617 :phase :end)
                                           (when (:threw result__8600__auto__)
                                             {:threw (class (:threw result__8600__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                  (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                nil)
              (if (contains? result__8600__auto__ :returned)
                (:returned result__8600__auto__)
                (do (throw (:threw result__8600__auto__)) nil)))))))
    (get-ref
      [this ref_key]
      (df/-future-with-channel-impl
        (fn fn__10600
          ([]
            (let [m_10601 {:event :kv-cluster/get-ref, :ref-key ref_key}
                  ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                      (.debug
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10601 :phase :begin))))
                                    nil)
                  start__8599__auto__ (java.lang.System/nanoTime)
                  result__8600__auto__ (try
                                         {:returned
                                          (^clojure.lang.IFn retrying_read
                                            :linear
                                            (fn fn__10605
                                              ([]
                                                (let [temp__5825__auto__ (kv/get kvs ref_key true)]
                                                  (when temp__5825__auto__
                                                    (let [ret temp__5825__auto__]
                                                      (select-keys ret [:key :rev])))))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8601__auto__
                                           {:threw t__8601__auto__}))
                  elapsed_10602 (- (java.lang.System/nanoTime) start__8599__auto__)
                  msec_10603 (logger/format-as-msec (long elapsed_10602))]
              (let [endmsg__8602__auto__ (merge
                                           (assoc m_10601 :msec msec_10603 :phase :end)
                                           (when (:threw result__8600__auto__)
                                             {:threw (class (:threw result__8600__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                  (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                nil)
              (if (contains? result__8600__auto__ :returned)
                (:returned result__8600__auto__)
                (do (throw (:threw result__8600__auto__)) nil)))))))
    (delete-reference
      [this key]
      (df/-future-with-channel-impl
        (fn fn__10587
          ([]
            (let [m_10588 {:event :kv-cluster/delete-reference, :key key}
                  ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10588 :phase :begin))))
                                    nil)
                  start__8599__auto__ (java.lang.System/nanoTime)
                  result__8600__auto__ (try
                                         {:returned
                                          (^clojure.lang.IFn retrying_delete
                                            (fn fn__10592 ([] (kv/delete kvs key true))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8601__auto__
                                           {:threw t__8601__auto__}))
                  elapsed_10589 (- (java.lang.System/nanoTime) start__8599__auto__)
                  msec_10590 (logger/format-as-msec (long elapsed_10589))]
              (let [endmsg__8602__auto__ (merge
                                           (assoc m_10588 :msec msec_10590 :phase :end)
                                           (when (:threw result__8600__auto__)
                                             {:threw (class (:threw result__8600__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                nil)
              (if (contains? result__8600__auto__ :returned)
                (:returned result__8600__auto__)
                (do (throw (:threw result__8600__auto__)) nil)))))))
    (delete
      [this key]
      (df/-future-with-channel-impl
        (deref delete-pool-ref)
        (fn fn__10574
          ([]
            (let [m_10575 {:event :kv-cluster/delete, :key key}
                  ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.kv-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_10575 :phase :begin))))
                                    nil)
                  start__8599__auto__ (java.lang.System/nanoTime)
                  result__8600__auto__ (try
                                         {:returned
                                          (^clojure.lang.IFn retrying_delete
                                            (fn fn__10579 ([] (kv/delete kvs key false))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8601__auto__
                                           {:threw t__8601__auto__}))
                  elapsed_10576 (- (java.lang.System/nanoTime) start__8599__auto__)
                  msec_10577 (logger/format-as-msec (long elapsed_10576))]
              (let [endmsg__8602__auto__ (merge
                                           (assoc m_10575 :msec msec_10577 :phase :end)
                                           (when (:threw result__8600__auto__)
                                             {:threw (class (:threw result__8600__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                nil)
              (if (contains? result__8600__auto__ :returned)
                (:returned result__8600__auto__)
                (do (throw (:threw result__8600__auto__)) nil)))))))
    (get-val
      [this val_key]
      (let [start (java.lang.System/nanoTime)]
        (io-stats/inc! protocol)
        (df/-future-with-channel-impl
          (fn fn__10559
            ([]
              (let [m_10560 {:event :kv-cluster/get-val, :val-key val_key}
                    ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.kv-cluster")]
                                      (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                        (.debug
                                          ^org.slf4j.Logger logger
                                          (logger/process (assoc m_10560 :phase :begin))))
                                      nil)
                    start__8599__auto__ (java.lang.System/nanoTime)
                    result__8600__auto__ (try
                                           {:returned
                                            (^clojure.lang.IFn retrying_read
                                              :exponential
                                              (fn fn__10564
                                                ([]
                                                  (let [temp__5825__auto__ (kv/get
                                                                             kvs
                                                                             val_key
                                                                             false)]
                                                    (when temp__5825__auto__
                                                      (let [map__10565 temp__5825__auto__
                                                            map__10565
                                                            (if
                                                              (seq? map__10565)
                                                              (if
                                                                (next map__10565)
                                                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                  (to-array map__10565))
                                                                (if
                                                                  (seq map__10565)
                                                                  (first map__10565)
                                                                  {}))
                                                              map__10565)
                                                            buf (get map__10565 :v)]
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
                                             t__8601__auto__
                                             {:threw t__8601__auto__}))
                    elapsed_10561 (- (java.lang.System/nanoTime) start__8599__auto__)
                    msec_10562 (logger/format-as-msec (long elapsed_10561))]
                (monitor/add-stat :StorageGetMsec msec_10562)
                (let [endmsg__8602__auto__ (merge
                                             (assoc m_10560 :msec msec_10562 :phase :end)
                                             (when (:threw result__8600__auto__)
                                               {:threw (class (:threw result__8600__auto__))}))
                      logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                    (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                  nil)
                (if (contains? result__8600__auto__ :returned)
                  (:returned result__8600__auto__)
                  (do (throw (:threw result__8600__auto__)) nil))))))))
    (create-val [this val_key buf] (cluster/create-val this 3 val_key buf))
    (create-val
      [this priority val_key buf]
      (let [backoff (if (< priority 3) :linear :exponential)
            doit (fn doit
                   ([]
                     (monitor/add-stat
                       :StoragePutBytes
                       (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer buf))))
                     (let [m_10548 {:event :kv-cluster/create-val,
                                    :val-key val_key,
                                    :bufsize
                                    (java.lang.Integer/valueOf
                                      (int (.remaining ^java.nio.Buffer buf)))}
                           start__8599__auto__ (java.lang.System/nanoTime)
                           result__8600__auto__ (try
                                                  {:returned
                                                   (^clojure.lang.IFn retrying_write
                                                     backoff
                                                     (fn fn__10552
                                                       ([]
                                                         (when (kv/put kvs {:id val_key, :v buf})
                                                           :created))))}
                                                  (catch
                                                    java.lang.Throwable
                                                    t__8601__auto__
                                                    {:threw t__8601__auto__}))
                           elapsed_10549 (- (java.lang.System/nanoTime) start__8599__auto__)
                           msec_10550 (logger/format-as-msec (long elapsed_10549))]
                       (monitor/add-stat :StoragePutMsec msec_10550)
                       (let [endmsg__8602__auto__ (merge
                                                    (assoc m_10548 :msec msec_10550 :phase :end)
                                                    (when (:threw result__8600__auto__)
                                                      {:threw
                                                       (class (:threw result__8600__auto__))}))
                             logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                           (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                         nil)
                       (if (contains? result__8600__auto__ :returned)
                         (:returned result__8600__auto__)
                         (do (throw (:threw result__8600__auto__)) nil)))))]
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
  (defn ->KVCluster
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
        pod_garbage_handler)))
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
  ;; Creates a database-scoped cluster with independent soft limits for reads, writes, and deletes.
  (defn kv-cluster
    ([kvs p__10702]
      (let [map__10703 p__10702
            map__10703 (if (seq? map__10703)
                         (if (next map__10703)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__10703))
                           (if (seq map__10703) (first map__10703) {}))
                         map__10703)
            x map__10703
            tenant (get map__10703 :tenant)
            db_id (get map__10703 :db-id)
            write_concurrency (get map__10703 :write-concurrency)
            read_concurrency (get map__10703 :read-concurrency)
            shared_pool? (get map__10703 :shared-pool? true)
            protocol (get map__10703 :protocol :kvc)
            pod_garbage_handler (get map__10703 :pod-garbage-handler mark-pod-garbage)
            retrying_delete (get map__10703 :retrying-delete)
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
          pod_garbage_handler))))
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
