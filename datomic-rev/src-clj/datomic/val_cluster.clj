(do
  (clojure.core/in-ns 'datomic.val-cluster)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.cluster :as 'cluster]
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.core2.async :refer (clojure.core/list '<!!x)]
        ['datomic.core2.val-store :as 'vs]
        ['datomic.future :as 'df]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.val-cluster 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.val-cluster))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common]
          ['datomic.cluster :as 'cluster]
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.core2.async :refer (clojure.core/list '<!!x)]
          ['datomic.core2.val-store :as 'vs]
          ['datomic.future :as 'df]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'java.nio.ByteBuffer))))
  (set! *warn-on-reflection* true)
  (defonce Impl {})
  (defprotocol Impl (-get [_ val-key opts]))
  (deftype
    ValCluster
    [val_store]
    datomic.cluster.Get2
    datomic.cluster.ClusteredStore
    datomic.val_cluster.Impl
    datomic.cluster.RefClusterStore
    (-get-ref-store [this] nil)
    (get-val2 [this val_key opts] (-get this val_key opts))
    (delete
      [this key]
      (let [f__10261__auto__ (df/-future-with-channel-impl
                               (fn fn__11230
                                 ([]
                                   (let [m_11231 {:event :val-cluster/delete, :key key}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.val-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_11231 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
                                                                {:returned
                                                                 (let 
                                                                   [res
                                                                    (datomic.core2.async/<!!x
                                                                      (vs/delete val_store key))]
                                                                   (if
                                                                     (canom/anom res)
                                                                     (common/throw-anom res)
                                                                     :ok))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_11232 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_11233 (logger/format-as-msec (long elapsed_11232))]
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_11231
                                                                    :msec
                                                                    msec_11233
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.val-cluster")]
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
          {:line 68, :column 5, :file "datomic/val_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10261__auto__))
    (get-val [this val_key] (-get this val_key nil))
    (create-val [this val_key buf] (cluster/create-val this 3 val_key buf))
    (create-val
      [this _ val_key buf]
      (let [f__10261__auto__ (df/-future-with-channel-impl
                               (fn fn__11219
                                 ([]
                                   (let [m_11220 {:event :val-cluster/create-val,
                                                  :val-key val_key,
                                                  :bufsize
                                                  (java.lang.Integer/valueOf
                                                    (int (.remaining ^java.nio.Buffer buf)))}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.val-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_11220 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
                                                                {:returned
                                                                 (let 
                                                                   [res
                                                                    (datomic.core2.async/<!!x
                                                                      (vs/put
                                                                        val_store
                                                                        val_key
                                                                        {:val buf}))]
                                                                   (if
                                                                     (canom/anom res)
                                                                     (common/throw-anom res)
                                                                     (when-not
                                                                       (nil? res)
                                                                       (when :else :created))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_11221 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_11222 (logger/format-as-msec (long elapsed_11221))]
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_11220
                                                                    :msec
                                                                    msec_11222
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.val-cluster")]
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
          {:line 43, :column 5, :file "datomic/val_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10261__auto__))
    (-get
      [this val_key opts]
      (let [f__10261__auto__ (df/-future-with-channel-impl
                               (fn fn__11207
                                 ([]
                                   (let [m_11208 {:event :val-cluster/get-val,
                                                  :val-key val_key,
                                                  :opts opts}
                                         ___8980__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.val-cluster")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_11208 :phase :begin)))
                                                             nil)
                                                           nil)
                                         start__8981__auto__ (java.lang.System/nanoTime)
                                         result__8982__auto__ (try
                                                                {:returned
                                                                 (let 
                                                                   [map__11212
                                                                    (datomic.core2.async/<!!x
                                                                      (vs/get
                                                                        val_store
                                                                        val_key
                                                                        opts))
                                                                    map__11212
                                                                    (if
                                                                      (seq? map__11212)
                                                                      (clojure.lang.PersistentHashMap/create
                                                                        (seq map__11212))
                                                                      map__11212)
                                                                    res map__11212
                                                                    val (get map__11212 :val)]
                                                                   (if
                                                                     (canom/anom res)
                                                                     (common/throw-anom res)
                                                                     (when-not
                                                                       (nil? val)
                                                                       (when :else {:buf val}))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8983__auto__
                                                                  {:threw t__8983__auto__}))
                                         elapsed_11209 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8981__auto__)
                                         msec_11210 (logger/format-as-msec (long elapsed_11209))]
                                     (let [endmsg__8984__auto__ (merge
                                                                  (assoc
                                                                    m_11208
                                                                    :msec
                                                                    msec_11210
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8982__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8982__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.val-cluster")]
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
          {:line 25, :column 5, :file "datomic/val_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__10261__auto__)))
  (clojure.core/import 'datomic.val_cluster.ValCluster)
  (defn ->ValCluster ([val_store] (datomic.val_cluster.ValCluster. val_store)))
  (defn val-cluster ([val_store] (datomic.val_cluster.ValCluster. val_store))))