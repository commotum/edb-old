(do
  (clojure.core/in-ns 'datomic.cluster)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common :refer (clojure.core/list 'bounded-deref)]
        ['datomic.config :as 'config]
        ['datomic.monitor :as 'monitor]
        ['datomic.promise :as 'promise]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'java.lang.AutoCloseable)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.UUID)
      (clojure.core/import 'java.util.concurrent.TimeoutException)
      (clojure.core/import 'java.util.concurrent.TimeUnit)))
  (when-not (.equals 'datomic.cluster 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cluster))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common :refer (clojure.core/list 'bounded-deref)]
          ['datomic.config :as 'config]
          ['datomic.monitor :as 'monitor]
          ['datomic.promise :as 'promise]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'java.lang.AutoCloseable)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.UUID)
        (clojure.core/import 'java.util.concurrent.TimeoutException)
        (clojure.core/import 'java.util.concurrent.TimeUnit))))
  (set! *warn-on-reflection* true)
  (defn uuid->val-key ([uuid] (str uuid)))
  (defn val-key->uuid ([val_key] (UUID/fromString ^java.lang.String val_key)))
  (defn uuid->pod-key ([uuid] (str "pod-" uuid)))
  (defn pod-key->uuid ([pod_key] (UUID/fromString (subs pod_key 4))))
  (defn new-val-key ([] (uuid->val-key (common/rand-uuid))))
  (defn new-pod-key ([] (str "pod-" (common/rand-uuid))))
  (defn new-ref-key ([name] (str "ref-" name)))
  (defn new-pod-key ([] (str "pod-" (common/rand-uuid))))
  (defn path
    ([args]
      (let [map__10420 args
            map__10420 (if (seq? map__10420)
                         (clojure.lang.PersistentHashMap/create (seq map__10420))
                         map__10420)
            tenant (get map__10420 :tenant)
            db (get map__10420 :db)
            partition (get map__10420 :partition)
            key (get map__10420 :key)]
        (when-not key (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'key)))))
        (str
          (when tenant (format "%s/" tenant))
          (when db (format "%s/" db))
          (when partition (format "%03X/" partition))
          (format "%s" key)))))
  (reset-meta!
    #'path
    (assoc
      {:tag java.lang.String, :arglists (clojure.core/list ['args]), :column 1}
      :name
      'path
      :ns
      *ns*))
  (def PRIORITY_HIGH 2)
  (reset-meta! #'PRIORITY_HIGH (assoc {:const true, :column 1} :name 'PRIORITY_HIGH :ns *ns*))
  (def PRIORITY_MID 3)
  (reset-meta! #'PRIORITY_MID (assoc {:const true, :column 1} :name 'PRIORITY_MID :ns *ns*))
  (def PRIORITY_LOW 4)
  (reset-meta! #'PRIORITY_LOW (assoc {:const true, :column 1} :name 'PRIORITY_LOW :ns *ns*))
  (defonce Dbid {})
  (defprotocol Dbid (dbId [c]))
  (defonce ClusteredStore {})
  (alter-meta!
    #'ClusteredStore
    assoc
    :doc
    "An interface to a clustered store. All fns might throw an exception on deref if no quorum is available.")
  (defprotocol
    ClusteredStore
    (get-pod-meta [cs pod-key])
    (delete [cs key])
    (get-val [cs val-key])
    (get-ref [cs ref-key])
    (update-pod* [cs pod-key rev etag buf metamap])
    (create-val [cs val-key buf] [cs priority val-key buf])
    (delete-reference [cs key])
    (set-ref [cs ref-key rev vkey])
    (get-pod [cs pod-key]))
  (defonce Get2 {})
  (alter-meta! #'Get2 assoc :doc "Enhanced ClusteredStore/get-val that accepts opts map.")
  (defprotocol Get2 (get-val2 [cs val-key opts]))
  (defn update-pod
    ([cs pod_key rev etag buf metamap]
      (do
        (when-not (every? namespace (keys metamap))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str
                  (clojure.core/list 'every? 'namespace (clojure.core/list 'keys 'metamap)))))))
        (update-pod* cs pod_key rev etag buf metamap)))
    ([cs pod_key rev etag buf] (update-pod cs pod_key rev etag buf nil)))
  (defn reset-ref
    ([cluster k v]
      (let [map__10621 (deref (get-ref cluster k))
            map__10621 (if (seq? map__10621)
                         (clojure.lang.PersistentHashMap/create (seq map__10621))
                         map__10621)
            prev map__10621
            rev (get map__10621 :rev)
            m_10622 {:event :kv-cluster/reset-ref, :key k, :from (:key prev), :to v}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_10622 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned (set-ref cluster k (if rev (inc rev) 0) v)}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_10623 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_10624 (logger/format-as-msec (long elapsed_10623))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_10622 :msec msec_10624 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn touch-ref
    ([cluster k]
      (let [map__10632 (deref (get-ref cluster k))
            map__10632 (if (seq? map__10632)
                         (clojure.lang.PersistentHashMap/create (seq map__10632))
                         map__10632)
            prev map__10632
            rev (get map__10632 :rev)
            m_10633 {:event :kv-cluster/touch-ref, :from prev}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_10633 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (when (= :ok (deref (set-ref cluster k (inc rev) (:key prev))))
                                      prev)}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_10634 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_10635 (logger/format-as-msec (long elapsed_10634))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_10633 :msec msec_10635 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isWarnEnabled ^org.slf4j.Logger logger)
            (.warn ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn reset-pod
    ([cluster k buf metamap]
      (let [map__10643 (deref (get-pod cluster k))
            map__10643 (if (seq? map__10643)
                         (clojure.lang.PersistentHashMap/create (seq map__10643))
                         map__10643)
            rev (get map__10643 :rev)]
        (update-pod cluster k (if rev (inc rev) 0) nil buf metamap))))
  (defn touch-pod
    ([cs pod_key pod] (deref (update-pod cs pod_key (inc (:rev pod)) (:etag pod) nil))))
  (defn claim-pod
    ([cs pod_key msec]
      (let [start (java.lang.System/currentTimeMillis)]
        (loop []
          (let [temp__5457__auto__ (deref (get-pod cs pod_key))]
            (when temp__5457__auto__
              (let [pod temp__5457__auto__ touched (touch-pod cs pod_key pod)]
                (cond
                  (:rev touched) (assoc touched :buf (:buf pod))
                  (<= (- (java.lang.System/currentTimeMillis) start) msec) (do (recur))))))))
      nil))
  (defn clone-pod
    ([cs from_key to_key]
      (let [temp__5455__auto__ (deref (get-pod cs from_key))]
        (if temp__5455__auto__
          (let [map__10648 temp__5455__auto__
                map__10648 (if (seq? map__10648)
                             (clojure.lang.PersistentHashMap/create (seq map__10648))
                             map__10648)
                rev (get map__10648 :rev)
                etag (get map__10648 :etag)
                buf (get map__10648 :buf)]
            (deref (update-pod cs to_key 0 nil buf)))
          {:failed :absent}))))
  (defn clone-ref
    ([cs from_key to_key]
      (let [temp__5455__auto__ (deref (get-ref cs from_key))]
        (when temp__5455__auto__
          (let [map__10651 temp__5455__auto__
                map__10651 (if (seq? map__10651)
                             (clojure.lang.PersistentHashMap/create (seq map__10651))
                             map__10651)
                rev (get map__10651 :rev)
                key (get map__10651 :key)]
            (if (= :ok (deref (set-ref cs to_key 0 key))) {:rev 0} {:failed :conflict}))))))
  (defn uncached-val-lookup
    ([cs]
      (reify
        clojure.lang.ILookup
        (valAt [this k not_found] (let [ret (deref (get-val cs k))] (if ret (:buf ret) not_found)))
        (valAt [this k] (.valAt this k nil)))))
  (def pacer (java.lang.Object.))
  (def segment-writes (atom 0))
  (def segment-pacing-msec (atom nil))
  (def BOUNDING_TIMEOUT_MSEC 300000)
  (defn write-vals*
    ([cs source vmap]
      (let [event (let [G__10657 source]
                    (case G__10657 :index :index/write-val :clusterfs :clusterfs/write-val))
            metric (let [G__10658 source]
                     (case G__10658 :index :IndexWriteNsec :clusterfs :FulltextWriteNsec))
            pacing (deref segment-pacing-msec)
            rets (mapv
                   (fn fn__10660
                     ([p__10659]
                       (let [vec__10661 p__10659
                             k (nth vec__10661 (int 0) nil)
                             v (nth vec__10661 (int 1) nil)]
                         (when pacing (java.lang.Thread/sleep (long ^java.lang.Number pacing)))
                         [(long (java.lang.System/nanoTime)) (create-val cs k v)])))
                   vmap)]
        (dorun
          (map
            (fn fn__10666
              ([p__10665]
                (let [vec__10667 p__10665
                      start (nth vec__10667 (int 0) nil)
                      ret (nth vec__10667 (int 1) nil)]
                  (if (= (common/bounded-deref ret BOUNDING_TIMEOUT_MSEC) :created)
                    (let [nsec (- (java.lang.System/nanoTime) start)
                          msec (logger/format-as-msec nsec)]
                      (swap! segment-writes inc)
                      (monitor/add-stat metric nsec)
                      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug
                            ^org.slf4j.Logger logger
                            (logger/process {:event event, :msec msec}))
                          nil)
                        nil))
                    (do (throw (java.lang.RuntimeException. "Cluster write failed")) nil)))))
            rets)))))
  (defn write-vals
    ([cs source vmap]
      (let [m_10672 {:event :cluster/write-vals,
                     :vcnt (java.lang.Integer/valueOf (int (count vmap)))}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_10672 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (write-vals*
                                      cs
                                      source
                                      (map
                                        (fn fn__10677
                                          ([p__10676]
                                            (let [vec__10678 p__10676
                                                  k (nth vec__10678 (int 0) nil)
                                                  v (nth vec__10678 (int 1) nil)]
                                              [(uuid->val-key k) v])))
                                        vmap))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_10673 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_10674 (logger/format-as-msec (long elapsed_10673))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_10672 :msec msec_10674 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defonce AsyncWriter {})
  (defprotocol AsyncWriter (finish-writer [_]) (sync-writes [_]))
  (deftype
    QueueingWriter
    [cluster done_reason bounding_timeout_msec queue]
    datomic.cluster.AsyncWriter
    datomic.cluster.ClusteredStore
    (finish-writer
      [this]
      (do
        (when-not (.offer
                    ^java.util.concurrent.BlockingQueue queue
                    {:type :finish}
                    (long ^java.lang.Number bounding_timeout_msec)
                    TimeUnit/MILLISECONDS)
          (throw (java.util.concurrent.TimeoutException. "Timed out waiting for queue")))
        done_reason))
    (sync-writes
      [this]
      (let [prom (promise)]
        (when-not (.offer
                    ^java.util.concurrent.BlockingQueue queue
                    {:type :sync-writes, :obj prom}
                    (long ^java.lang.Number bounding_timeout_msec)
                    TimeUnit/MILLISECONDS)
          (throw (java.util.concurrent.TimeoutException. "Timed out waiting for queue")))
        prom))
    (create-val [this k v] (create-val this 3 k v))
    (create-val
      [this priority k v]
      (if (realized? done_reason)
        done_reason
        (if (.offer
              ^java.util.concurrent.BlockingQueue queue
              {:type :create-val, :obj (create-val cluster priority k v)}
              (long ^java.lang.Number bounding_timeout_msec)
              TimeUnit/MILLISECONDS)
          (promise/delivered :created)
          (do
            (throw (java.util.concurrent.TimeoutException. "Timed out waiting for queue"))
            nil)))))
  (clojure.core/import 'datomic.cluster.QueueingWriter)
  (defn ->QueueingWriter
    ([cluster done_reason bounding_timeout_msec queue]
      (datomic.cluster.QueueingWriter. cluster done_reason bounding_timeout_msec queue)))
  (defn queueing-writer
    ([cluster par bounding_timeout_msec progress]
      (let [queue (java.util.concurrent.ArrayBlockingQueue. (int ^java.lang.Number par))
            done_reason (promise/settable-future)
            writer (datomic.cluster.QueueingWriter.
                     cluster
                     done_reason
                     bounding_timeout_msec
                     queue)]
        (future-call
          (fn fn__10722
            ([]
              (try
                (loop []
                  (when-not (realized? done_reason)
                    (let [map__10723 (.take ^java.util.concurrent.ArrayBlockingQueue queue)
                          map__10723 (if (seq? map__10723)
                                       (clojure.lang.PersistentHashMap/create (seq map__10723))
                                       map__10723)
                          type (get map__10723 :type)
                          obj (get map__10723 :obj)
                          G__10724 type]
                      (case
                        G__10724
                        :sync-writes
                        (do (deliver obj :synced) (recur))
                        :create-val
                        (do
                          (^clojure.lang.IFn progress
                            (java.lang.Integer/valueOf (int (count queue))))
                          (let [create_result (deref obj)]
                            (if (= :created create_result)
                              (recur)
                              (do
                                (throw (java.lang.RuntimeException. "Queued write failed."))
                                (bit-and
                                  (bit-shift-right (clojure.lang.Util/hash G__10724) 0)
                                  3)))))
                        :finish
                        (^clojure.lang.IFn done_reason :done)))))
                (catch java.lang.Throwable t (^clojure.lang.IFn done_reason t))))))
        writer)))
  (defonce RefClusterStore {})
  (alter-meta! #'RefClusterStore assoc :doc "Helper for getting a ref store from a cluster")
  (defprotocol RefClusterStore (-get-ref-store [cs]))
  (defn get-ref-store ([cs] (-get-ref-store cs)))
  (defn close
    ([x]
      (when (and x (instance? java.lang.AutoCloseable x))
        (.close ^java.lang.AutoCloseable x)
        nil))))