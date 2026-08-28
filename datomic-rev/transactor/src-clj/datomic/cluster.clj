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
      (let [map__11960 args
            map__11960 (if (seq? map__11960)
                         (if (next map__11960)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__11960))
                           (if (seq map__11960) (first map__11960) {}))
                         map__11960)
            tenant (get map__11960 :tenant)
            db (get map__11960 :db)
            partition (get map__11960 :partition)
            key (get map__11960 :key)]
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
      (let [map__12161 (deref (get-ref cluster k))
            map__12161 (if (seq? map__12161)
                         (if (next map__12161)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__12161))
                           (if (seq map__12161) (first map__12161) {}))
                         map__12161)
            prev map__12161
            rev (get map__12161 :rev)
            m_12162 {:event :kv-cluster/reset-ref, :key k, :from (:key prev), :to v}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_12162 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned (set-ref cluster k (if rev (inc rev) 0) v)}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_12163 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_12164 (logger/format-as-msec (long elapsed_12163))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_12162 :msec msec_12164 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (defn touch-ref
    ([cluster k]
      (let [map__12172 (deref (get-ref cluster k))
            map__12172 (if (seq? map__12172)
                         (if (next map__12172)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__12172))
                           (if (seq map__12172) (first map__12172) {}))
                         map__12172)
            prev map__12172
            rev (get map__12172 :rev)
            m_12173 {:event :kv-cluster/touch-ref, :from prev}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_12173 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (when (= :ok (deref (set-ref cluster k (inc rev) (:key prev))))
                                      prev)}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_12174 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_12175 (logger/format-as-msec (long elapsed_12174))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_12173 :msec msec_12175 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isWarnEnabled ^org.slf4j.Logger logger)
            (.warn ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (defn reset-pod
    ([cluster k buf metamap]
      (let [map__12183 (deref (get-pod cluster k))
            map__12183 (if (seq? map__12183)
                         (if (next map__12183)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__12183))
                           (if (seq map__12183) (first map__12183) {}))
                         map__12183)
            rev (get map__12183 :rev)]
        (update-pod cluster k (if rev (inc rev) 0) nil buf metamap))))
  (defn touch-pod
    ([cs pod_key pod] (deref (update-pod cs pod_key (inc (:rev pod)) (:etag pod) nil))))
  (defn claim-pod
    ([cs pod_key msec]
      (let [start (java.lang.System/currentTimeMillis)]
        (loop []
          (let [temp__5804__auto__ (deref (get-pod cs pod_key))]
            (when temp__5804__auto__
              (let [pod temp__5804__auto__ touched (touch-pod cs pod_key pod)]
                (cond
                  (:rev touched) (assoc touched :buf (:buf pod))
                  (<= (- (java.lang.System/currentTimeMillis) start) msec) (do (recur))))))))
      nil))
  (defn clone-pod
    ([cs from_key to_key]
      (let [temp__5802__auto__ (deref (get-pod cs from_key))]
        (if temp__5802__auto__
          (let [map__12188 temp__5802__auto__
                map__12188 (if (seq? map__12188)
                             (if (next map__12188)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__12188))
                               (if (seq map__12188) (first map__12188) {}))
                             map__12188)
                rev (get map__12188 :rev)
                etag (get map__12188 :etag)
                buf (get map__12188 :buf)]
            (deref (update-pod cs to_key 0 nil buf)))
          {:failed :absent}))))
  (defn clone-ref
    ([cs from_key to_key]
      (let [temp__5802__auto__ (deref (get-ref cs from_key))]
        (when temp__5802__auto__
          (let [map__12191 temp__5802__auto__
                map__12191 (if (seq? map__12191)
                             (if (next map__12191)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__12191))
                               (if (seq map__12191) (first map__12191) {}))
                             map__12191)
                rev (get map__12191 :rev)
                key (get map__12191 :key)]
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
      (let [event (let [G__12197 source]
                    (case G__12197 :index :index/write-val :clusterfs :clusterfs/write-val))
            metric (let [G__12198 source]
                     (case G__12198 :index :IndexWriteNsec :clusterfs :FulltextWriteNsec))
            pacing (deref segment-pacing-msec)
            rets (mapv
                   (fn fn__12200
                     ([p__12199]
                       (let [vec__12201 p__12199
                             k (nth vec__12201 (int 0) nil)
                             v (nth vec__12201 (int 1) nil)]
                         (when pacing (java.lang.Thread/sleep (long ^java.lang.Number pacing)))
                         [(long (java.lang.System/nanoTime)) (create-val cs k v)])))
                   vmap)]
        (dorun
          (map
            (fn fn__12206
              ([p__12205]
                (let [vec__12207 p__12205
                      start (nth vec__12207 (int 0) nil)
                      ret (nth vec__12207 (int 1) nil)]
                  (if (= (common/bounded-deref ret BOUNDING_TIMEOUT_MSEC) :created)
                    (let [nsec (- (java.lang.System/nanoTime) start)
                          msec (logger/format-as-msec nsec)]
                      (swap! segment-writes inc)
                      (monitor/add-stat metric nsec)
                      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug
                            ^org.slf4j.Logger logger
                            (logger/process {:event event, :msec msec})))
                        nil))
                    (do (throw (java.lang.RuntimeException. "Cluster write failed")) nil)))))
            rets)))))
  (defn write-vals
    ([cs source vmap]
      (let [m_12212 {:event :cluster/write-vals,
                     :vcnt (java.lang.Integer/valueOf (int (count vmap)))}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_12212 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (write-vals*
                                      cs
                                      source
                                      (map
                                        (fn fn__12217
                                          ([p__12216]
                                            (let [vec__12218 p__12216
                                                  k (nth vec__12218 (int 0) nil)
                                                  v (nth vec__12218 (int 1) nil)]
                                              [(uuid->val-key k) v])))
                                        vmap))}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_12213 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_12214 (logger/format-as-msec (long elapsed_12213))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_12212 :msec msec_12214 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
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
          (fn fn__12262
            ([]
              (try
                (loop []
                  (when-not (realized? done_reason)
                    (let [map__12263 (.take ^java.util.concurrent.ArrayBlockingQueue queue)
                          map__12263 (if (seq? map__12263)
                                       (if (next map__12263)
                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                           (to-array map__12263))
                                         (if (seq map__12263) (first map__12263) {}))
                                       map__12263)
                          type (get map__12263 :type)
                          obj (get map__12263 :obj)
                          G__12264 type]
                      (case
                        G__12264
                        :sync-writes
                        (do (deliver obj :synced) (recur))
                        :create-val
                        (do
                          (^clojure.lang.IFn progress
                            (java.lang.Integer/valueOf (int (count queue))))
                          (let [create_result (deref obj)]
                            (when (= :created create_result) (recur))))
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