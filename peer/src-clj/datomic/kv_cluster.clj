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
               :cause (retry-cause result)}))
          nil)
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
              max_retries (let [G__10858 backoff] (case G__10858 :linear 20 :exponential 9))
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
                                  (logger/process {:event :kv-cluster/semaphore-timeout}))
                                nil)
                              nil)
                            (monitor/alarm :StorageSemaphoreTimeout))
                          permit?))]
          (try
            (loop [retries 0 elapsed 0]
              (do
                (when (= :exponential backoff) (swap! group_ref assoc (long tid) (long retries)))
                (let [delay (let [G__10862 backoff]
                              (case
                                G__10862
                                :linear
                                (linear-backoff (long retries))
                                :exponential
                                (exponential-backoff (apply max 0 (vals (deref group_ref))))))
                      _ (when (clojure.lang.Numbers/isPos delay)
                          (java.lang.Thread/sleep (long ^java.lang.Number delay))
                          nil)
                      vec__10859 (let [start (java.lang.System/nanoTime)
                                       result (try
                                                (^clojure.lang.IFn f)
                                                (catch java.lang.Throwable e e))]
                                   [(long (quot (- (java.lang.System/nanoTime) start) 1000000))
                                    result])
                      ms (nth vec__10859 (int 0) nil)
                      result (nth vec__10859 (int 1) nil)
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
                (when permit? (.release ^java.util.concurrent.Semaphore sem) nil)
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
            f__10261__auto__ (df/-future-with-channel-impl
                               (fn fn__11004
                                 ([]
                                   (let [m_11005 {:event :kv-cluster/update-pod,
                                                  :pod-key pod_key,
                                                  :tailid tailid,
                                                  :rev rev,
                                                  :etag etag,
                                                  :bufsize
                                                  (when buf
                                                    (java.lang.Integer/valueOf
                                                      (int (.remaining ^java.nio.Buffer buf))))}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_11005 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
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
                                                                            fn__11009
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
                                                                           fn__11011
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
                                                                             fn__11013
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
                                                                                 fn__11015
                                                                                 ([]
                                                                                   (let 
                                                                                     [tail_keys_ref
                                                                                      (delay
                                                                                        (loop 
                                                                                          [ks
                                                                                           [oldtail]]
                                                                                          (let 
                                                                                            [map__11017
                                                                                             (^clojure.lang.IFn retrying_read
                                                                                               :exponential
                                                                                               (fn 
                                                                                                 fn__11018
                                                                                                 ([]
                                                                                                   (kv/get
                                                                                                     kvs
                                                                                                     (peek
                                                                                                       ks)
                                                                                                     false))))
                                                                                             map__11017
                                                                                             (if
                                                                                               (seq?
                                                                                                 map__11017)
                                                                                               (clojure.lang.PersistentHashMap/create
                                                                                                 (seq
                                                                                                   map__11017))
                                                                                               map__11017)
                                                                                             v
                                                                                             (get
                                                                                               map__11017
                                                                                               :v)
                                                                                             prev
                                                                                             (get
                                                                                               map__11017
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
                                                                           [temp__5455__auto__
                                                                            (^clojure.lang.IFn retrying_read
                                                                              :linear
                                                                              (fn 
                                                                                fn__11022
                                                                                ([]
                                                                                  (kv/get
                                                                                    kvs
                                                                                    pod_key
                                                                                    true))))]
                                                                           (if
                                                                             temp__5455__auto__
                                                                             (let 
                                                                               [map__11024
                                                                                temp__5455__auto__
                                                                                map__11024
                                                                                (if
                                                                                  (seq? map__11024)
                                                                                  (clojure.lang.PersistentHashMap/create
                                                                                    (seq
                                                                                      map__11024))
                                                                                  map__11024)
                                                                                nrev
                                                                                (get
                                                                                  map__11024
                                                                                  :rev)
                                                                                ntail
                                                                                (get
                                                                                  map__11024
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
                                                                                            rev}))
                                                                                       nil)
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
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_11006 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_11007 (logger/format-as-msec (long elapsed_11006))]
                                     (monitor/add-stat :PodUpdateMsec msec_11007)
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_11005
                                                                    :msec
                                                                    msec_11007
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8984__auto__))
                                         nil)
                                       nil)
                                     (if (contains? result__8982__auto__ :returned)
                                       (:returned result__8982__auto__)
                                       (do (throw (:threw result__8982__auto__)) nil))))))
            ch__10262__auto__ (df/get-channel f__10261__auto__)]
        (df/add-bounding-warning
          ch__10262__auto__
          {:line 265, :column 6, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10261__auto__))
    (get-pod
      [this pod_key]
      (let [f__10261__auto__ (df/-future-with-channel-impl
                               (fn fn__10981
                                 ([]
                                   (let [m_10982 {:event :kv-cluster/get-pod, :pod-key pod_key}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_10982 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
                                                                {:returned
                                                                 (let 
                                                                   [temp__5457__auto__
                                                                    (^clojure.lang.IFn retrying_read
                                                                      :linear
                                                                      (fn 
                                                                        fn__10986
                                                                        ([]
                                                                          (kv/get
                                                                            kvs
                                                                            pod_key
                                                                            true))))]
                                                                   (when
                                                                     temp__5457__auto__
                                                                     (let 
                                                                       [pref temp__5457__auto__
                                                                        map__10988 pref
                                                                        map__10988
                                                                        (if
                                                                          (seq? map__10988)
                                                                          (clojure.lang.PersistentHashMap/create
                                                                            (seq map__10988))
                                                                          map__10988)
                                                                        rev (get map__10988 :rev)
                                                                        tail (get map__10988 :tail)
                                                                        bufs
                                                                        (loop 
                                                                          [tail tail ret nil]
                                                                          (let 
                                                                            [map__10990
                                                                             (^clojure.lang.IFn retrying_read
                                                                               :linear
                                                                               (fn 
                                                                                 fn__10991
                                                                                 ([]
                                                                                   (kv/get
                                                                                     kvs
                                                                                     tail
                                                                                     false))))
                                                                             map__10990
                                                                             (if
                                                                               (seq? map__10990)
                                                                               (clojure.lang.PersistentHashMap/create
                                                                                 (seq map__10990))
                                                                               map__10990)
                                                                             t map__10990
                                                                             v (get map__10990 :v)
                                                                             prev
                                                                             (get map__10990 :prev)
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
                                                                              fn__10994
                                                                              ([p1__10884#]
                                                                                (java.lang.Integer/valueOf
                                                                                  (int
                                                                                    (.remaining
                                                                                      ^java.nio.Buffer p1__10884#)))))
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
                                                                                      bufs)))}))
                                                                           nil)
                                                                         nil)
                                                                       (loop 
                                                                         [i 0 bs (seq bufs)]
                                                                         (let 
                                                                           [temp__5457__auto__
                                                                            (first bs)]
                                                                           (if
                                                                             temp__5457__auto__
                                                                             (let 
                                                                               [b
                                                                                temp__5457__auto__]
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
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_10983 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_10984 (logger/format-as-msec (long elapsed_10983))]
                                     (monitor/add-stat :PodGetMsec msec_10984)
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_10982
                                                                    :msec
                                                                    msec_10984
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8984__auto__))
                                         nil)
                                       nil)
                                     (if (contains? result__8982__auto__ :returned)
                                       (:returned result__8982__auto__)
                                       (do (throw (:threw result__8982__auto__)) nil))))))
            ch__10262__auto__ (df/get-channel f__10261__auto__)]
        (df/add-bounding-warning
          ch__10262__auto__
          {:line 237, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10261__auto__))
    (get-pod-meta
      [this pod_key]
      (let [f__10261__auto__ (df/-future-with-channel-impl
                               (fn fn__10968
                                 ([]
                                   (let [m_10969 {:event :kv-cluster/get-pod-meta,
                                                  :pod-key pod_key}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_10969 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
                                                                {:returned
                                                                 (^clojure.lang.IFn retrying_read
                                                                   :linear
                                                                   (fn 
                                                                     fn__10973
                                                                     ([]
                                                                       (kv/get
                                                                         kvs
                                                                         pod_key
                                                                         true))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_10970 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_10971 (logger/format-as-msec (long elapsed_10970))]
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_10969
                                                                    :msec
                                                                    msec_10971
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8984__auto__))
                                         nil)
                                       nil)
                                     (if (contains? result__8982__auto__ :returned)
                                       (:returned result__8982__auto__)
                                       (do (throw (:threw result__8982__auto__)) nil))))))
            ch__10262__auto__ (df/get-channel f__10261__auto__)]
        (df/add-bounding-warning
          ch__10262__auto__
          {:line 230, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10261__auto__))
    (set-ref
      [this ref_key rev vkey]
      (let [f__10261__auto__ (df/-future-with-channel-impl
                               (fn fn__10953
                                 ([]
                                   (let [m_10954 {:event :kv-cluster/set-ref,
                                                  :ref-key ref_key,
                                                  :rev rev,
                                                  :vkey vkey}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_10954 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
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
                                                                           fn__10958
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
                                                                             fn__10960
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
                                                                                  :rev rev}))
                                                                             nil)
                                                                           nil)
                                                                         :ok)
                                                                       :conflict)))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_10955 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_10956 (logger/format-as-msec (long elapsed_10955))]
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_10954
                                                                    :msec
                                                                    msec_10956
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                         (.debug
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8984__auto__))
                                         nil)
                                       nil)
                                     (if (contains? result__8982__auto__ :returned)
                                       (:returned result__8982__auto__)
                                       (do (throw (:threw result__8982__auto__)) nil))))))
            ch__10262__auto__ (df/get-channel f__10261__auto__)]
        (df/add-bounding-warning
          ch__10262__auto__
          {:line 206, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10261__auto__))
    (get-ref
      [this ref_key]
      (let [f__10261__auto__ (df/-future-with-channel-impl
                               (fn fn__10939
                                 ([]
                                   (let [m_10940 {:event :kv-cluster/get-ref, :ref-key ref_key}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_10940 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
                                                                {:returned
                                                                 (^clojure.lang.IFn retrying_read
                                                                   :linear
                                                                   (fn 
                                                                     fn__10944
                                                                     ([]
                                                                       (let 
                                                                         [temp__5457__auto__
                                                                          (kv/get
                                                                            kvs
                                                                            ref_key
                                                                            true)]
                                                                         (when
                                                                           temp__5457__auto__
                                                                           (let 
                                                                             [ret
                                                                              temp__5457__auto__]
                                                                             (select-keys
                                                                               ret
                                                                               [:key :rev])))))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_10941 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_10942 (logger/format-as-msec (long elapsed_10941))]
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_10940
                                                                    :msec
                                                                    msec_10942
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                         (.debug
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8984__auto__))
                                         nil)
                                       nil)
                                     (if (contains? result__8982__auto__ :returned)
                                       (:returned result__8982__auto__)
                                       (do (throw (:threw result__8982__auto__)) nil))))))
            ch__10262__auto__ (df/get-channel f__10261__auto__)]
        (df/add-bounding-warning
          ch__10262__auto__
          {:line 195, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10261__auto__))
    (delete-reference
      [this key]
      (let [f__10261__auto__ (df/-future-with-channel-impl
                               (fn fn__10926
                                 ([]
                                   (let [m_10927 {:event :kv-cluster/delete-reference, :key key}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_10927 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
                                                                {:returned
                                                                 (^clojure.lang.IFn retrying_delete
                                                                   (fn 
                                                                     fn__10931
                                                                     ([]
                                                                       (kv/delete kvs key true))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_10928 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_10929 (logger/format-as-msec (long elapsed_10928))]
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_10927
                                                                    :msec
                                                                    msec_10929
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8984__auto__))
                                         nil)
                                       nil)
                                     (if (contains? result__8982__auto__ :returned)
                                       (:returned result__8982__auto__)
                                       (do (throw (:threw result__8982__auto__)) nil))))))
            ch__10262__auto__ (df/get-channel f__10261__auto__)]
        (df/add-bounding-warning
          ch__10262__auto__
          {:line 188, :column 4, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10261__auto__))
    (delete
      [this key]
      (let [f__10267__auto__ (df/-future-with-channel-impl
                               (deref delete-pool-ref)
                               (fn fn__10913
                                 ([]
                                   (let [m_10914 {:event :kv-cluster/delete, :key key}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.kv-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_10914 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
                                                                {:returned
                                                                 (^clojure.lang.IFn retrying_delete
                                                                   (fn 
                                                                     fn__10918
                                                                     ([]
                                                                       (kv/delete
                                                                         kvs
                                                                         key
                                                                         false))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_10915 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_10916 (logger/format-as-msec (long elapsed_10915))]
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_10914
                                                                    :msec
                                                                    msec_10916
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.kv-cluster")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8984__auto__))
                                         nil)
                                       nil)
                                     (if (contains? result__8982__auto__ :returned)
                                       (:returned result__8982__auto__)
                                       (do (throw (:threw result__8982__auto__)) nil))))))
            ch__10268__auto__ (df/get-channel f__10267__auto__)]
        (df/add-bounding-warning
          ch__10268__auto__
          {:line 180, :column 5, :file "datomic/kv_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10267__auto__))
    (get-val
      [this val_key]
      (let [start (java.lang.System/nanoTime)]
        (io-stats/inc! protocol)
        (let [f__10261__auto__ (df/-future-with-channel-impl
                                 (fn fn__10898
                                   ([]
                                     (let [m_10899 {:event :kv-cluster/get-val, :val-key val_key}
                                           ___8980__auto__ (let 
                                                             [logger
                                                              (org.slf4j.LoggerFactory/getLogger
                                                                "datomic.kv-cluster")]
                                                             (when
                                                               (.isDebugEnabled
                                                                 ^org.slf4j.Logger logger)
                                                               (.debug
                                                                 ^org.slf4j.Logger logger
                                                                 (logger/process
                                                                   (assoc m_10899 :phase :begin)))
                                                               nil)
                                                             nil)
                                           start__8981__auto__ (java.lang.System/nanoTime)
                                           result__8982__auto__ (try
                                                                  {:returned
                                                                   (^clojure.lang.IFn retrying_read
                                                                     :exponential
                                                                     (fn 
                                                                       fn__10903
                                                                       ([]
                                                                         (let 
                                                                           [temp__5457__auto__
                                                                            (kv/get
                                                                              kvs
                                                                              val_key
                                                                              false)]
                                                                           (when
                                                                             temp__5457__auto__
                                                                             (let 
                                                                               [map__10904
                                                                                temp__5457__auto__
                                                                                map__10904
                                                                                (if
                                                                                  (seq? map__10904)
                                                                                  (clojure.lang.PersistentHashMap/create
                                                                                    (seq
                                                                                      map__10904))
                                                                                  map__10904)
                                                                                buf
                                                                                (get
                                                                                  map__10904
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
                                                                    t__8983__auto__
                                                                    {:threw t__8983__auto__}))
                                           elapsed_10900 (-
                                                           (java.lang.System/nanoTime)
                                                           start__8981__auto__)
                                           msec_10901 (logger/format-as-msec (long elapsed_10900))]
                                       (monitor/add-stat :StorageGetMsec msec_10901)
                                       (let [endmsg__8984__auto__ (merge
                                                                    (assoc
                                                                      m_10899
                                                                      :msec
                                                                      msec_10901
                                                                      :phase
                                                                      :end)
                                                                    (when
                                                                      (:threw result__8982__auto__)
                                                                      {:threw
                                                                       (class
                                                                         (:threw
                                                                           result__8982__auto__))}))
                                             logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.kv-cluster")]
                                         (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                           (.debug
                                             ^org.slf4j.Logger logger
                                             (logger/process endmsg__8984__auto__))
                                           nil)
                                         nil)
                                       (if (contains? result__8982__auto__ :returned)
                                         (:returned result__8982__auto__)
                                         (do (throw (:threw result__8982__auto__)) nil))))))
              ch__10262__auto__ (df/get-channel f__10261__auto__)]
          (df/add-bounding-warning
            ch__10262__auto__
            {:line 168, :column 7, :file "datomic/kv_cluster.clj"}
            (deref df/bounding-warn-seconds))
          f__10261__auto__)))
    (create-val [this val_key buf] (cluster/create-val this 3 val_key buf))
    (create-val
      [this priority val_key buf]
      (let [backoff (if (< priority 3) :linear :exponential)
            doit (fn doit
                   ([]
                     (monitor/add-stat
                       :StoragePutBytes
                       (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer buf))))
                     (let [m_10887 {:event :kv-cluster/create-val,
                                    :val-key val_key,
                                    :bufsize
                                    (java.lang.Integer/valueOf
                                      (int (.remaining ^java.nio.Buffer buf)))}
                           start__8981__auto__ (java.lang.System/nanoTime)
                           result__8982__auto__ (try
                                                  {:returned
                                                   (^clojure.lang.IFn retrying_write
                                                     backoff
                                                     (fn fn__10891
                                                       ([]
                                                         (when (kv/put kvs {:id val_key, :v buf})
                                                           :created))))}
                                                  (catch
                                                    java.lang.Throwable
                                                    t__8983__auto__
                                                    {:threw t__8983__auto__}))
                           elapsed_10888 (- (java.lang.System/nanoTime) start__8981__auto__)
                           msec_10889 (logger/format-as-msec (long elapsed_10888))]
                       (monitor/add-stat :StoragePutMsec msec_10889)
                       (let [endmsg__8984__auto__ (merge
                                                    (assoc m_10887 :msec msec_10889 :phase :end)
                                                    (when (:threw result__8982__auto__)
                                                      {:threw
                                                       (class (:threw result__8982__auto__))}))
                             logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-cluster")]
                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                           (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                           nil)
                         nil)
                       (if (contains? result__8982__auto__ :returned)
                         (:returned result__8982__auto__)
                         (do (throw (:threw result__8982__auto__)) nil)))))]
        (if (< priority 3)
          (let [f__10264__auto__ (df/-future-with-channel-impl doit)
                ch__10265__auto__ (df/get-channel f__10264__auto__)]
            (df/add-bounding-warning
              ch__10265__auto__
              {:line 157, :column 8, :file "datomic/kv_cluster.clj"}
              (deref df/bounding-warn-seconds))
            f__10264__auto__)
          (let [f__10270__auto__ (df/-future-with-channel-impl exec doit)
                ch__10271__auto__ (df/get-channel f__10270__auto__)]
            (df/add-bounding-warning
              ch__10271__auto__
              {:line 158, :column 8, :file "datomic/kv_cluster.clj"}
              (deref df/bounding-warn-seconds))
            f__10270__auto__))))
    (dbId [this] (:db path_map))
    (^void close
      [this]
      (do
        (when-not (= (shared-pool) exec)
          (.shutdown ^java.util.concurrent.ExecutorService exec)
          nil)
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
    ([kvs p__11061]
      (let [map__11062 p__11061
            map__11062 (if (seq? map__11062)
                         (clojure.lang.PersistentHashMap/create (seq map__11062))
                         map__11062)
            x map__11062
            tenant (get map__11062 :tenant)
            db_id (get map__11062 :db-id)
            write_concurrency (get map__11062 :write-concurrency)
            read_concurrency (get map__11062 :read-concurrency)
            shared_pool? (get map__11062 :shared-pool? true)
            protocol (get map__11062 :protocol :kvc)
            pod_garbage_handler (get map__11062 :pod-garbage-handler mark-pod-garbage)
            retrying_delete (get map__11062 :retrying-delete)
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