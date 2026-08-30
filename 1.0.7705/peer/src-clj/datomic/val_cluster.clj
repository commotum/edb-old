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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      Impl
      (-get [_ val-key opts] "Impl of cluster/ClusteredStore that takes nilable opts map."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.val-cluster" "Impl")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Impl :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-get
                                        {:arglists (clojure.core/list ['_ 'val-key 'opts])}),
                                      :arglists (clojure.core/list ['_ 'val-key 'opts]),
                                      :doc
                                      "Impl of cluster/ClusteredStore that takes nilable opts map."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.val-cluster" "Impl"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.val-cluster" "-get")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
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
      (df/-future-with-channel-impl
        (fn fn__16885
          ([]
            (let [m_16886 {:event :val-cluster/delete, :key key}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.val-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_16886 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (let [res (datomic.core2.async/<!!x
                                                      (vs/delete val_store key))]
                                            (if (canom/anom res) (common/throw-anom res) :ok))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_16887 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_16888 (logger/format-as-msec (long elapsed_16887))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_16886 :msec msec_16888 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.val-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (do (throw (:threw result__8554__auto__)) nil)))))))
    (get-val [this val_key] (-get this val_key nil))
    (create-val [this val_key buf] (cluster/create-val this 3 val_key buf))
    (create-val
      [this _ val_key buf]
      (df/-future-with-channel-impl
        (fn fn__16874
          ([]
            (let [m_16875 {:event :val-cluster/create-val,
                           :val-key val_key,
                           :bufsize
                           (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer buf)))}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.val-cluster")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_16875 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (let [res (datomic.core2.async/<!!x
                                                      (vs/put val_store val_key {:val buf}))]
                                            (if (canom/anom res)
                                              (common/throw-anom res)
                                              (when-not (nil? res) (when :else :created))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_16876 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_16877 (logger/format-as-msec (long elapsed_16876))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_16875 :msec msec_16877 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.val-cluster")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (do (throw (:threw result__8554__auto__)) nil)))))))
    (-get
      [this val_key opts]
      (df/-future-with-channel-impl
        (fn fn__16862
          ([]
            (let [m_16863 {:event :val-cluster/get-val, :val-key val_key, :opts opts}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.val-cluster")]
                                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                      (.debug
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_16863 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (let [map__16867 (datomic.core2.async/<!!x
                                                             (vs/get val_store val_key opts))
                                                map__16867 (if
                                                             (seq? map__16867)
                                                             (if
                                                               (next map__16867)
                                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                 (to-array map__16867))
                                                               (if
                                                                 (seq map__16867)
                                                                 (first map__16867)
                                                                 {}))
                                                             map__16867)
                                                res map__16867
                                                val (get map__16867 :val)]
                                            (if (canom/anom res)
                                              (common/throw-anom res)
                                              (when-not (nil? val) (when :else {:buf val}))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_16864 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_16865 (logger/format-as-msec (long elapsed_16864))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_16863 :msec msec_16865 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.val-cluster")]
                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                  (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (do (throw (:threw result__8554__auto__)) nil))))))))
  (clojure.core/import 'datomic.val_cluster.ValCluster)
  (defn ->ValCluster ([val_store] (datomic.val_cluster.ValCluster. val_store)))
  (reset-meta!
    #'->ValCluster
    (assoc
      {:arglists (clojure.core/list ['val-store]), :column (int 1)}
      :name
      '->ValCluster
      :ns
      *ns*))
  (defn val-cluster ([val_store] (datomic.val_cluster.ValCluster. val_store)))
  (reset-meta!
    #'val-cluster
    (assoc
      {:arglists (clojure.core/list ['val-store]), :column (int 1)}
      :name
      'val-cluster
      :ns
      *ns*)))