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
      (let [f__16170__auto__ (df/-future-with-channel-impl
                               (fn fn__16230
                                 ([]
                                   (let [m_16231 {:event :val-cluster/delete, :key key}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.val-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16231 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
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
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16232 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16233 (logger/format-as-msec (long elapsed_16232))]
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16231
                                                                    :msec
                                                                    msec_16233
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.val-cluster")]
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
          {:line 68, :column 5, :file "datomic/val_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16170__auto__))
    (get-val [this val_key] (-get this val_key nil))
    (create-val [this val_key buf] (cluster/create-val this 3 val_key buf))
    (create-val
      [this _ val_key buf]
      (let [f__16170__auto__ (df/-future-with-channel-impl
                               (fn fn__16219
                                 ([]
                                   (let [m_16220 {:event :val-cluster/create-val,
                                                  :val-key val_key,
                                                  :bufsize
                                                  (java.lang.Integer/valueOf
                                                    (int (.remaining ^java.nio.Buffer buf)))}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.val-cluster")]
                                                           (when
                                                             (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.info
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16220 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
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
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16221 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16222 (logger/format-as-msec (long elapsed_16221))]
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16220
                                                                    :msec
                                                                    msec_16222
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.val-cluster")]
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
          {:line 43, :column 5, :file "datomic/val_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16170__auto__))
    (-get
      [this val_key opts]
      (let [f__16170__auto__ (df/-future-with-channel-impl
                               (fn fn__16207
                                 ([]
                                   (let [m_16208 {:event :val-cluster/get-val,
                                                  :val-key val_key,
                                                  :opts opts}
                                         ___8583__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.val-cluster")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_16208 :phase :begin))))
                                                           nil)
                                         start__8584__auto__ (java.lang.System/nanoTime)
                                         result__8585__auto__ (try
                                                                {:returned
                                                                 (let 
                                                                   [map__16212
                                                                    (datomic.core2.async/<!!x
                                                                      (vs/get
                                                                        val_store
                                                                        val_key
                                                                        opts))
                                                                    map__16212
                                                                    (if
                                                                      (seq? map__16212)
                                                                      (if
                                                                        (next map__16212)
                                                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                          (to-array map__16212))
                                                                        (if
                                                                          (seq map__16212)
                                                                          (first map__16212)
                                                                          {}))
                                                                      map__16212)
                                                                    res map__16212
                                                                    val (get map__16212 :val)]
                                                                   (if
                                                                     (canom/anom res)
                                                                     (common/throw-anom res)
                                                                     (when-not
                                                                       (nil? val)
                                                                       (when :else {:buf val}))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8586__auto__
                                                                  {:threw t__8586__auto__}))
                                         elapsed_16209 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8584__auto__)
                                         msec_16210 (logger/format-as-msec (long elapsed_16209))]
                                     (let [endmsg__8587__auto__ (merge
                                                                  (assoc
                                                                    m_16208
                                                                    :msec
                                                                    msec_16210
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8585__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8585__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.val-cluster")]
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
          {:line 25, :column 5, :file "datomic/val_cluster.clj"}
          (deref df/bounding-warn-seconds))
        f__16170__auto__)))
  (clojure.core/import 'datomic.val_cluster.ValCluster)
  (defn ->ValCluster ([val_store] (datomic.val_cluster.ValCluster. val_store)))
  (defn val-cluster ([val_store] (datomic.val_cluster.ValCluster. val_store))))