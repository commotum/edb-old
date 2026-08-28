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
  (defn retry-cause ([result] (.getName (class (root-cause result)))))
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
  (defn exponential-backoff
    ([n]
      (if (zero? n)
        0
        (java.lang.Double/valueOf
          (double (* 50 (java.lang.Math/pow (double 2) (double (dec n)))))))))
  (reset-meta!
    #'exponential-backoff
    (assoc
      {:private true, :arglists (clojure.core/list ['n]), :column 1}
      :name
      'exponential-backoff
      :ns
      *ns*))
  (defn linear-backoff ([n] (* 50 n)))
  (reset-meta!
    #'linear-backoff
    (assoc
      {:private true, :arglists (clojure.core/list ['n]), :column 1}
      :name
      'linear-backoff
      :ns
      *ns*))
  (defn retry-fn
    ([sem metric nested group_ref backoff f]
      (binding [kv/*retry* (partial retry-fn sem metric true group_ref backoff)]
        (let [sem? (and (not nested) (= :exponential backoff))
              max_retries (let [G__16756 backoff] (case G__16756 :linear 20 :exponential 9))
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
                (let [delay (let [G__16760 backoff]
                              (case
                                G__16760
                                :linear
                                (linear-backoff (long retries))
                                :exponential
                                (exponential-backoff (apply max 0 (vals (deref group_ref))))))
                      _ (when (clojure.lang.Numbers/isPos delay)
                          (java.lang.Thread/sleep (long ^java.lang.Number delay))
                          nil)
                      vec__16757 (let [start (java.lang.System/nanoTime)
                                       result (try
                                                (^clojure.lang.IFn f)
                                                (catch java.lang.Throwable e e))]
                                   [(long (quot (- (java.lang.System/nanoTime) start) 1000000))
                                    result])
                      ms (nth vec__16757 (int 0) nil)
                      result (nth vec__16757 (int 1) nil)
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
  (defonce shared-pool-ref
   (delay
     (common/thread-pool
       {:nthreads (config/property "datomic.writeConcurrency"), :name "shared-storage"})))
  (reset-meta!
    #'shared-pool-ref
    (assoc {:private true, :column 1} :name 'shared-pool-ref :ns *ns*))
  (defn shared-pool ([] (deref shared-pool-ref)))
  (defonce delete-pool-ref
   (delay
     (common/thread-pool
       {:nthreads (config/property "datomic.deleteConcurrency"), :name "delete"})))
  (reset-meta!
    #'delete-pool-ref
    (assoc {:private true, :column 1} :name 'delete-pool-ref :ns *ns*))
  (def val-gets-ref (atom 0))
  (def ref-identity-keys [:id :rev :key])
  (defn same-ref?
    ([r1 r2]
      (= (common/require-keys r1 ref-identity-keys) (common/require-keys r2 ref-identity-keys))))
  (defn mark-pod-garbage
    ([cs tail_keys_ref]
      (events/publish {:key :datomic.garbage/mark, :cluster cs, :garbage (deref tail_keys_ref)})))
  (reset-meta!
    #'mark-pod-garbage
    (assoc
      {:private true, :arglists (clojure.core/list ['cs 'tail-keys-ref]), :column 1}
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
      (let [tailid (if buf (str (common/rand-uuid)) etag)
            f__16170__auto__ (df/-future-with-channel-impl
                               (fn fn__16902
                                 ([]
                                   (let [m_16903 {:event :kv-cluster/update-pod,
                                                  :pod-key pod_key,
                                                  :tailid tailid,
                                                  :rev rev,
                                                  :etag etag,
                                                  :bufsize
                                                  (when buf
                                                    (java.lang.Integer/valueOf
                                                      (int (.remaining ^java.nio.Buffer buf))))}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16903 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
                                                                {:returned
                                                                 (let 
                                                                   [tail {:id tailid, :v buf}
                                                                    tail
                                                                    (if
                                                                      etag
                                                                      (assoc tail :prev etag)
                                                                      tail)
                                                                    reset?
                                                                    (and
                                                                      (nil? etag)
                                                                      (not (zero? rev)))
                                                                    oldtail
                                                                    (when
                                                                      reset?
                                                                      (:tail
                                                                        (^clojure.lang.IFn retrying_read
                                                                          :linear
                                                                          (fn 
                                                                            fn__16907
                                                                            ([]
                                                                              (kv/get
                                                                                kvs
                                                                                pod_key
                                                                                true))))))]
                                                                   (when-not
                                                                     (or buf etag)
                                                                     (throw
                                                                       (java.lang.AssertionError.
                                                                         (str
                                                                           "Assert failed: "
                                                                           (pr-str
                                                                             (clojure.core/list
                                                                               'or
                                                                               'buf
                                                                               'etag))))))
                                                                   (if
                                                                     (or
                                                                       (nil? buf)
                                                                       (^clojure.lang.IFn retrying_write
                                                                         :linear
                                                                         (fn 
                                                                           fn__16909
                                                                           ([]
                                                                             (kv/put kvs tail)))))
                                                                     (let 
                                                                       [item
                                                                        (merge
                                                                          metamap
                                                                          {:id pod_key,
                                                                           :rev rev,
                                                                           :tail tailid})]
                                                                       (if
                                                                         (^clojure.lang.IFn retrying_write
                                                                           :linear
                                                                           (fn 
                                                                             fn__16911
                                                                             ([]
                                                                               (kv/put
                                                                                 kvs
                                                                                 (merge
                                                                                   item
                                                                                   {:ensure
                                                                                    (merge
                                                                                      (if
                                                                                        (nil? etag)
                                                                                        {}
                                                                                        {:tail
                                                                                         etag})
                                                                                      (if
                                                                                        (zero? rev)
                                                                                        {:id nil}
                                                                                        {:rev
                                                                                         (dec
                                                                                           rev)}))})))))
                                                                         (do
                                                                           (when
                                                                             (and reset? oldtail)
                                                                             (future-call
                                                                               (fn 
                                                                                 fn__16913
                                                                                 ([]
                                                                                   (let 
                                                                                     [tail_keys_ref
                                                                                      (delay
                                                                                        (loop 
                                                                                          [ks
                                                                                           [oldtail]]
                                                                                          (let 
                                                                                            [map__16915
                                                                                             (^clojure.lang.IFn retrying_read
                                                                                               :exponential
                                                                                               (fn 
                                                                                                 fn__16916
                                                                                                 ([]
                                                                                                   (kv/get
                                                                                                     kvs
                                                                                                     (peek
                                                                                                       ks)
                                                                                                     false))))
                                                                                             map__16915
                                                                                             (if
                                                                                               (seq?
                                                                                                 map__16915)
                                                                                               (if
                                                                                                 (next
                                                                                                   map__16915)
                                                                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                                                   (to-array
                                                                                                     map__16915))
                                                                                                 (if
                                                                                                   (seq
                                                                                                     map__16915)
                                                                                                   (first
                                                                                                     map__16915)
                                                                                                   {}))
                                                                                               map__16915)
                                                                                             v
                                                                                             (get
                                                                                               map__16915
                                                                                               :v)
                                                                                             prev
                                                                                             (get
                                                                                               map__16915
                                                                                               :prev)]
                                                                                            (if
                                                                                              prev
                                                                                              (recur
                                                                                                (conj
                                                                                                  ks
                                                                                                  prev))
                                                                                              ks))))]
                                                                                     (^clojure.lang.IFn pod_garbage_handler
                                                                                       this
                                                                                       tail_keys_ref))))))
                                                                           {:rev rev,
                                                                            :etag tailid,
                                                                            :buf buf})
                                                                         (let 
                                                                           [temp__5802__auto__
                                                                            (^clojure.lang.IFn retrying_read
                                                                              :linear
                                                                              (fn 
                                                                                fn__16920
                                                                                ([]
                                                                                  (kv/get
                                                                                    kvs
                                                                                    pod_key
                                                                                    true))))]
                                                                           (if
                                                                             temp__5802__auto__
                                                                             (let 
                                                                               [map__16922
                                                                                temp__5802__auto__
                                                                                map__16922
                                                                                (if
                                                                                  (seq? map__16922)
                                                                                  (if
                                                                                    (next
                                                                                      map__16922)
                                                                                    (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                                      (to-array
                                                                                        map__16922))
                                                                                    (if
                                                                                      (seq
                                                                                        map__16922)
                                                                                      (first
                                                                                        map__16922)
                                                                                      {}))
                                                                                  map__16922)
                                                                                nrev
                                                                                (get
                                                                                  map__16922
                                                                                  :rev)
                                                                                ntail
                                                                                (get
                                                                                  map__16922
                                                                                  :tail)]
                                                                               (if
                                                                                 (and
                                                                                   (= rev nrev)
                                                                                   (=
                                                                                     tailid
                                                                                     ntail))
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
                                                                                            :pod-update-resume
                                                                                            true,
                                                                                            :pod-key
                                                                                            pod_key,
                                                                                            :rev
                                                                                            rev})))
                                                                                     nil)
                                                                                   {:rev rev,
                                                                                    :etag tailid,
                                                                                    :buf buf})
                                                                                 {:failed
                                                                                  :conflict}))
                                                                             {:failed
                                                                              :conflict}))))
                                                                     (do
                                                                       (throw
                                                                         (java.lang.Error.
                                                                           "kv write failed in update-pod"))
                                                                       1)))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16904 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16905 (logger/format-as-msec (long elapsed_16904))]
                                     (monitor/add-stat :PodUpdateMsec msec_16905)
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16903
                                                                    :msec
                                                                    msec_16905
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8587__auto__)))
                                       nil)
                                     (if (contains? result__8585__auto__ :returned)
                                       (:returned result__8585__auto__)
                                       (do (throw (:threw result__8585__auto__)) nil))))))
            ch__16171__auto__ (df/get-channel f__16170__auto__)]
        (df/add-bounding-warning
          ch__16171__auto__
          {:line 265, :column 6, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16170__auto__))
    (get-pod
      [this pod_key]
      (let [f__16170__auto__ (df/-future-with-channel-impl
                               (fn fn__16879
                                 ([]
                                   (let [m_16880 {:event :kv-cluster/get-pod, :pod-key pod_key}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16880 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
                                                                {:returned
                                                                 (let 
                                                                   [temp__5804__auto__
                                                                    (^clojure.lang.IFn retrying_read
                                                                      :linear
                                                                      (fn 
                                                                        fn__16884
                                                                        ([]
                                                                          (kv/get
                                                                            kvs
                                                                            pod_key
                                                                            true))))]
                                                                   (when
                                                                     temp__5804__auto__
                                                                     (let 
                                                                       [pref temp__5804__auto__
                                                                        map__16886 pref
                                                                        map__16886
                                                                        (if
                                                                          (seq? map__16886)
                                                                          (if
                                                                            (next map__16886)
                                                                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                              (to-array
                                                                                map__16886))
                                                                            (if
                                                                              (seq map__16886)
                                                                              (first map__16886)
                                                                              {}))
                                                                          map__16886)
                                                                        rev (get map__16886 :rev)
                                                                        tail (get map__16886 :tail)
                                                                        bufs
                                                                        (loop 
                                                                          [tail tail ret nil]
                                                                          (let 
                                                                            [map__16888
                                                                             (^clojure.lang.IFn retrying_read
                                                                               :linear
                                                                               (fn 
                                                                                 fn__16889
                                                                                 ([]
                                                                                   (kv/get
                                                                                     kvs
                                                                                     tail
                                                                                     false))))
                                                                             map__16888
                                                                             (if
                                                                               (seq? map__16888)
                                                                               (if
                                                                                 (next map__16888)
                                                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                                   (to-array
                                                                                     map__16888))
                                                                                 (if
                                                                                   (seq map__16888)
                                                                                   (first
                                                                                     map__16888)
                                                                                   {}))
                                                                               map__16888)
                                                                             t map__16888
                                                                             v (get map__16888 :v)
                                                                             prev
                                                                             (get map__16888 :prev)
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
                                                                            (if
                                                                              prev
                                                                              (recur prev ret)
                                                                              ret)))
                                                                        len
                                                                        (reduce
                                                                          +
                                                                          (map
                                                                            (fn 
                                                                              fn__16892
                                                                              ([p1__16782#]
                                                                                (java.lang.Integer/valueOf
                                                                                  (int
                                                                                    (.remaining
                                                                                      ^java.nio.Buffer p1__16782#)))))
                                                                            bufs))
                                                                        arr (byte-array len)]
                                                                       (let 
                                                                         [logger
                                                                          (org.slf4j.LoggerFactory/getLogger
                                                                            "datomic.kv-cluster")]
                                                                         (when
                                                                           (.isDebugEnabled
                                                                             ^org.slf4j.Logger logger)
                                                                           (.debug
                                                                             ^org.slf4j.Logger logger
                                                                             (logger/process
                                                                               {:event
                                                                                :kv-cluster/pod-size,
                                                                                :bytes len,
                                                                                :chunks
                                                                                (java.lang.Integer/valueOf
                                                                                  (int
                                                                                    (count
                                                                                      bufs)))})))
                                                                         nil)
                                                                       (loop 
                                                                         [i 0 bs (seq bufs)]
                                                                         (let 
                                                                           [temp__5804__auto__
                                                                            (first bs)]
                                                                           (if
                                                                             temp__5804__auto__
                                                                             (let 
                                                                               [b
                                                                                temp__5804__auto__]
                                                                               (.get
                                                                                 (.duplicate
                                                                                   ^java.nio.ByteBuffer b)
                                                                                 ^bytes arr
                                                                                 (int i)
                                                                                 (int
                                                                                   (.remaining
                                                                                     ^java.nio.Buffer b)))
                                                                               (recur
                                                                                 (+
                                                                                   i
                                                                                   (.remaining
                                                                                     ^java.nio.Buffer b))
                                                                                 (next bs)))
                                                                             1)))
                                                                       (merge
                                                                         (dissoc pref :id :tail)
                                                                         {:rev rev,
                                                                          :etag tail,
                                                                          :buf
                                                                          (ByteBuffer/wrap
                                                                            ^bytes arr)}))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16881 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16882 (logger/format-as-msec (long elapsed_16881))]
                                     (monitor/add-stat :PodGetMsec msec_16882)
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16880
                                                                    :msec
                                                                    msec_16882
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8587__auto__)))
                                       nil)
                                     (if (contains? result__8585__auto__ :returned)
                                       (:returned result__8585__auto__)
                                       (do (throw (:threw result__8585__auto__)) nil))))))
            ch__16171__auto__ (df/get-channel f__16170__auto__)]
        (df/add-bounding-warning
          ch__16171__auto__
          {:line 237, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16170__auto__))
    (get-pod-meta
      [this pod_key]
      (let [f__16170__auto__ (df/-future-with-channel-impl
                               (fn fn__16866
                                 ([]
                                   (let [m_16867 {:event :kv-cluster/get-pod-meta,
                                                  :pod-key pod_key}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16867 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
                                                                {:returned
                                                                 (^clojure.lang.IFn retrying_read
                                                                   :linear
                                                                   (fn 
                                                                     fn__16871
                                                                     ([]
                                                                       (kv/get
                                                                         kvs
                                                                         pod_key
                                                                         true))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16868 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16869 (logger/format-as-msec (long elapsed_16868))]
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16867
                                                                    :msec
                                                                    msec_16869
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8587__auto__)))
                                       nil)
                                     (if (contains? result__8585__auto__ :returned)
                                       (:returned result__8585__auto__)
                                       (do (throw (:threw result__8585__auto__)) nil))))))
            ch__16171__auto__ (df/get-channel f__16170__auto__)]
        (df/add-bounding-warning
          ch__16171__auto__
          {:line 230, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16170__auto__))
    (set-ref
      [this ref_key rev vkey]
      (let [f__16170__auto__ (df/-future-with-channel-impl
                               (fn fn__16851
                                 ([]
                                   (let [m_16852 {:event :kv-cluster/set-ref,
                                                  :ref-key ref_key,
                                                  :rev rev,
                                                  :vkey vkey}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16852 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
                                                                {:returned
                                                                 (let 
                                                                   [item
                                                                    {:id ref_key,
                                                                     :rev rev,
                                                                     :key vkey}]
                                                                   (if
                                                                     (=
                                                                       :ok
                                                                       (^clojure.lang.IFn retrying_write
                                                                         :linear
                                                                         (fn 
                                                                           fn__16856
                                                                           ([]
                                                                             (kv/put
                                                                               kvs
                                                                               (merge
                                                                                 item
                                                                                 {:ensure
                                                                                  (if
                                                                                    (zero? rev)
                                                                                    {:id nil}
                                                                                    {:rev
                                                                                     (dec
                                                                                       rev)})}))))))
                                                                     :ok
                                                                     (if
                                                                       (same-ref?
                                                                         item
                                                                         (^clojure.lang.IFn retrying_read
                                                                           :linear
                                                                           (fn 
                                                                             fn__16858
                                                                             ([]
                                                                               (kv/get
                                                                                 kvs
                                                                                 ref_key
                                                                                 true)))))
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
                                                                                  :kv-cluster/set-ref-resume,
                                                                                  :ref-key ref_key,
                                                                                  :rev rev})))
                                                                           nil)
                                                                         :ok)
                                                                       :conflict)))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16853 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16854 (logger/format-as-msec (long elapsed_16853))]
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16852
                                                                    :msec
                                                                    msec_16854
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                         (.debug
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8587__auto__)))
                                       nil)
                                     (if (contains? result__8585__auto__ :returned)
                                       (:returned result__8585__auto__)
                                       (do (throw (:threw result__8585__auto__)) nil))))))
            ch__16171__auto__ (df/get-channel f__16170__auto__)]
        (df/add-bounding-warning
          ch__16171__auto__
          {:line 206, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16170__auto__))
    (get-ref
      [this ref_key]
      (let [f__16170__auto__ (df/-future-with-channel-impl
                               (fn fn__16837
                                 ([]
                                   (let [m_16838 {:event :kv-cluster/get-ref, :ref-key ref_key}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16838 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
                                                                {:returned
                                                                 (^clojure.lang.IFn retrying_read
                                                                   :linear
                                                                   (fn 
                                                                     fn__16842
                                                                     ([]
                                                                       (let 
                                                                         [temp__5804__auto__
                                                                          (kv/get
                                                                            kvs
                                                                            ref_key
                                                                            true)]
                                                                         (when
                                                                           temp__5804__auto__
                                                                           (let 
                                                                             [ret
                                                                              temp__5804__auto__]
                                                                             (select-keys
                                                                               ret
                                                                               [:key :rev])))))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16839 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16840 (logger/format-as-msec (long elapsed_16839))]
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16838
                                                                    :msec
                                                                    msec_16840
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                         (.debug
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8587__auto__)))
                                       nil)
                                     (if (contains? result__8585__auto__ :returned)
                                       (:returned result__8585__auto__)
                                       (do (throw (:threw result__8585__auto__)) nil))))))
            ch__16171__auto__ (df/get-channel f__16170__auto__)]
        (df/add-bounding-warning
          ch__16171__auto__
          {:line 195, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16170__auto__))
    (delete-reference
      [this key]
      (let [f__16170__auto__ (df/-future-with-channel-impl
                               (fn fn__16824
                                 ([]
                                   (let [m_16825 {:event :kv-cluster/delete-reference, :key key}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16825 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
                                                                {:returned
                                                                 (^clojure.lang.IFn retrying_delete
                                                                   (fn 
                                                                     fn__16829
                                                                     ([]
                                                                       (kv/delete kvs key true))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16826 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16827 (logger/format-as-msec (long elapsed_16826))]
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16825
                                                                    :msec
                                                                    msec_16827
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8587__auto__)))
                                       nil)
                                     (if (contains? result__8585__auto__ :returned)
                                       (:returned result__8585__auto__)
                                       (do (throw (:threw result__8585__auto__)) nil))))))
            ch__16171__auto__ (df/get-channel f__16170__auto__)]
        (df/add-bounding-warning
          ch__16171__auto__
          {:line 188, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16170__auto__))
    (delete
      [this key]
      (let [f__16176__auto__ (df/-future-with-channel-impl
                               (deref delete-pool-ref)
                               (fn fn__16811
                                 ([]
                                   (let [m_16812 {:event :kv-cluster/delete, :key key}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16812 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
                                                                {:returned
                                                                 (^clojure.lang.IFn retrying_delete
                                                                   (fn 
                                                                     fn__16816
                                                                     ([]
                                                                       (kv/delete
                                                                         kvs
                                                                         key
                                                                         false))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16813 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16814 (logger/format-as-msec (long elapsed_16813))]
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16812
                                                                    :msec
                                                                    msec_16814
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8587__auto__)))
                                       nil)
                                     (if (contains? result__8585__auto__ :returned)
                                       (:returned result__8585__auto__)
                                       (do (throw (:threw result__8585__auto__)) nil))))))
            ch__16177__auto__ (df/get-channel f__16176__auto__)]
        (df/add-bounding-warning
          ch__16177__auto__
          {:line 180, :column 5, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16176__auto__))
    (get-val
      [this val_key]
      (let [start (java.lang.System/nanoTime)]
        (io-stats/inc! protocol)
        (let [f__16170__auto__ (df/-future-with-channel-impl
                                 (fn fn__16796
                                   ([]
                                     (let [m_16797 {:event :kv-cluster/get-val, :val-key val_key}
                                           ___8583__auto__ (let 
                                                             [logger
                                                              (org.slf4j.LoggerFactory/getLogger
                                                                "datomic.kv-cluster")]
                                                             (when
                                                               (.isDebugEnabled
                                                                 ^org.slf4j.Logger logger)
                                                               (.debug
                                                                 ^org.slf4j.Logger logger
                                                                 (logger/process
                                                                   (assoc m_16797 :phase :begin))))
                                                             nil)
                                           start__8584__auto__ (java.lang.System/nanoTime)
                                           result__8585__auto__ (try
                                                                  {:returned
                                                                   (^clojure.lang.IFn retrying_read
                                                                     :exponential
                                                                     (fn 
                                                                       fn__16801
                                                                       ([]
                                                                         (let 
                                                                           [temp__5804__auto__
                                                                            (kv/get
                                                                              kvs
                                                                              val_key
                                                                              false)]
                                                                           (when
                                                                             temp__5804__auto__
                                                                             (let 
                                                                               [map__16802
                                                                                temp__5804__auto__
                                                                                map__16802
                                                                                (if
                                                                                  (seq? map__16802)
                                                                                  (if
                                                                                    (next
                                                                                      map__16802)
                                                                                    (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                                      (to-array
                                                                                        map__16802))
                                                                                    (if
                                                                                      (seq
                                                                                        map__16802)
                                                                                      (first
                                                                                        map__16802)
                                                                                      {}))
                                                                                  map__16802)
                                                                                buf
                                                                                (get
                                                                                  map__16802
                                                                                  :v)]
                                                                               (io-stats/inc!
                                                                                 protocol_nsec_k
                                                                                 (-
                                                                                   (java.lang.System/nanoTime)
                                                                                   start))
                                                                               (monitor/add-stat
                                                                                 :StorageGetBytes
                                                                                 (java.lang.Integer/valueOf
                                                                                   (int
                                                                                     (.remaining
                                                                                       ^java.nio.Buffer buf))))
                                                                               (swap!
                                                                                 val-gets-ref
                                                                                 inc)
                                                                               {:buf buf}))))))}
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8586__auto__
                                                                    {:threw t__8586__auto__}))
                                           elapsed_16798 (-
                                                           (java.lang.System/nanoTime)
                                                           start__8584__auto__)
                                           msec_16799 (logger/format-as-msec (long elapsed_16798))]
                                       (monitor/add-stat :StorageGetMsec msec_16799)
                                       (let [endmsg__8587__auto__ (merge
                                                                    (assoc
                                                                      m_16797
                                                                      :msec
                                                                      msec_16799
                                                                      :phase
                                                                      :end)
                                                                    (when
                                                                      (:threw result__8585__auto__)
                                                                      {:threw
                                                                       (class
                                                                         (:threw
                                                                           result__8585__auto__))}))
                                             logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.kv-cluster")]
                                         (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                           (.debug
                                             ^org.slf4j.Logger logger
                                             (logger/process endmsg__8587__auto__)))
                                         nil)
                                       (if (contains? result__8585__auto__ :returned)
                                         (:returned result__8585__auto__)
                                         (do (throw (:threw result__8585__auto__)) nil))))))
              ch__16171__auto__ (df/get-channel f__16170__auto__)]
          (df/add-bounding-warning
            ch__16171__auto__
            {:line 168, :column 7, :file "datomic/kv_cluster.clj"}
            (deref df/bounding-warn-seconds))
          f__16170__auto__)))
    (create-val [this val_key buf] (cluster/create-val this 3 val_key buf))
    (create-val
      [this priority val_key buf]
      (let [backoff (if (< priority 3) :linear :exponential)
            doit (fn doit
                   ([]
                     (monitor/add-stat
                       :StoragePutBytes
                       (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer buf))))
                     (let [m_16785 {:event :kv-cluster/create-val,
                                    :val-key val_key,
                                    :bufsize
                                    (java.lang.Integer/valueOf
                                      (int (.remaining ^java.nio.Buffer buf)))}
                           start__8584__auto__ (java.lang.System/nanoTime)
                           result__8585__auto__ (try
                                                  {:returned
                                                   (^clojure.lang.IFn retrying_write
                                                     backoff
                                                     (fn fn__16789
                                                       ([]
                                                         (when (kv/put kvs {:id val_key, :v buf})
                                                           :created))))}
                                                  (catch
                                                    java.lang.Throwable
                                                    t__8586__auto__
                                                    {:threw t__8586__auto__}))
                           elapsed_16786 (- (java.lang.System/nanoTime) start__8584__auto__)
                           msec_16787 (logger/format-as-msec (long elapsed_16786))]
                       (monitor/add-stat :StoragePutMsec msec_16787)
                       (let [endmsg__8587__auto__ (merge
                                                    (assoc m_16785 :msec msec_16787 :phase :end)
                                                    (when (:threw result__8585__auto__)
                                                      {:threw
                                                       (class (:threw result__8585__auto__))}))
                             logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                           (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                         nil)
                       (if (contains? result__8585__auto__ :returned)
                         (:returned result__8585__auto__)
                         (do (throw (:threw result__8585__auto__)) nil)))))]
        (if (< priority 3)
          (let [f__16173__auto__ (df/-future-with-channel-impl doit)
                ch__16174__auto__ (df/get-channel f__16173__auto__)]
            (df/add-bounding-warning
              ch__16174__auto__
              {:line 157, :column 8, :file "datomic/kv_cluster.clj"}
              (deref df/bounding-warn-seconds))
            f__16173__auto__)
          (let [f__16179__auto__ (df/-future-with-channel-impl exec doit)
                ch__16180__auto__ (df/get-channel f__16179__auto__)]
            (df/add-bounding-warning
              ch__16180__auto__
              {:line 158, :column 8, :file "datomic/kv_cluster.clj"}
              (deref df/bounding-warn-seconds))
            f__16179__auto__))))
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
  (def PRIORITY_WRITE_CONCURRENCY 2)
  (reset-meta!
    #'PRIORITY_WRITE_CONCURRENCY
    (assoc {:const true, :column 1} :name 'PRIORITY_WRITE_CONCURRENCY :ns *ns*))
  (defn kv-cluster
    ([kvs p__16959]
      (let [map__16960 p__16959
            map__16960 (if (seq? map__16960)
                         (if (next map__16960)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16960))
                           (if (seq map__16960) (first map__16960) {}))
                         map__16960)
            x map__16960
            tenant (get map__16960 :tenant)
            db_id (get map__16960 :db-id)
            write_concurrency (get map__16960 :write-concurrency)
            read_concurrency (get map__16960 :read-concurrency)
            shared_pool? (get map__16960 :shared-pool? true)
            protocol (get map__16960 :protocol :kvc)
            pod_garbage_handler (get map__16960 :pod-garbage-handler mark-pod-garbage)
            retrying_delete (get map__16960 :retrying-delete)
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
