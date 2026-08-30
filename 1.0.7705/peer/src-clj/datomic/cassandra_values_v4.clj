(do
  (clojure.core/in-ns 'datomic.cassandra-values-v4)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.config :as 'config]
        ['datomic.cassandra-v4 :as 'cass]
        ['datomic.common :as 'common]
        ['datomic.future :as 'df]
        ['datomic.io :as 'io]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.cassandra-values-v4 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cassandra-values-v4))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.config :as 'config]
          ['datomic.cassandra-v4 :as 'cass]
          ['datomic.common :as 'common]
          ['datomic.future :as 'df]
          ['datomic.io :as 'io]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'java.nio.ByteBuffer))))
  (set! *warn-on-reflection* true)
  (.setMeta
    (clojure.lang.RT/var "datomic.cassandra-values-v4" "CHUNK_SIZE")
    {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra-values-v4" "CHUNK_SIZE") (long (* 350 1024)))
  (.setDynamic (clojure.lang.RT/var "datomic.cassandra-values-v4" "*retry*") true)
  (.setMeta
    (.setDynamic (clojure.lang.RT/var "datomic.cassandra-values-v4" "*retry*") true)
    {:dynamic true, :column (int 1)})
  (defn chunk-key ([k n] (str k "__" n)))
  (reset-meta!
    #'chunk-key
    (assoc {:arglists (clojure.core/list ['k 'n]), :column (int 1)} :name 'chunk-key :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra-values-v4" "chunk-pool") {:column (int 1)})
  (let [v__6812__auto__ #'chunk-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.cassandra-values-v4" "chunk-pool") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.cassandra-values-v4" "chunk-pool")
        (delay
          (common/thread-pool
            {:nthreads (config/property "datomic.writeConcurrency"), :name "cass3-chunk"})))
      #'chunk-pool))
  (def cql-keys [:id2 :rev :map :val :chunks])
  (reset-meta! #'cql-keys (assoc {:column (int 1)} :name 'cql-keys :ns *ns*))
  (defn put-value
    ([session table p__14940]
      (let [map__14941 p__14940
            map__14941 (if (seq? map__14941)
                         (if (next map__14941)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__14941))
                           (if (seq map__14941) (first map__14941) {}))
                         map__14941)
            v_map map__14941
            id (get map__14941 :id)
            rev (get map__14941 :rev)
            v (get map__14941 :v)
            m (dissoc v_map :id :rev :v)
            chunks (mapv
                     (fn fn__14942 ([bbuf] (.asReadOnlyBuffer ^java.nio.ByteBuffer bbuf)))
                     (io/chunk v 358400))
            n (count chunks)
            val_map {:chunks (java.lang.Integer/valueOf (int n)),
                     :id2 id,
                     :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                     :rev rev,
                     :val (nth chunks (int 0))}
            rets [(atom
                    (let [m_14944 {:event :cassandra-values/put-value,
                                   :id id,
                                   :bufsize
                                   (java.lang.Integer/valueOf
                                     (int (.remaining (nth chunks (int 0)))))}
                          ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.cassandra-values-v4")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_14944 :phase :begin))))
                                            nil)
                          start__8553__auto__ (java.lang.System/nanoTime)
                          result__8554__auto__ (try
                                                 {:returned
                                                  (cass/cql-insert
                                                    session
                                                    table
                                                    cql-keys
                                                    val_map
                                                    false)}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8555__auto__
                                                   {:threw t__8555__auto__}))
                          elapsed_14945 (- (java.lang.System/nanoTime) start__8553__auto__)
                          msec_14946 (logger/format-as-msec (long elapsed_14945))]
                      (let [endmsg__8556__auto__ (merge
                                                   (assoc m_14944 :msec msec_14946 :phase :end)
                                                   (when (:threw result__8554__auto__)
                                                     {:threw
                                                      (class (:threw result__8554__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger
                                     "datomic.cassandra-values-v4")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                        nil)
                      (if (contains? result__8554__auto__ :returned)
                        (:returned result__8554__auto__)
                        (do (throw (:threw result__8554__auto__)) atom))))]
            rets (mapv
                   deref
                   (reduce
                     (fn fn__14949
                       ([rets n]
                         (let [val_map {:id2 (chunk-key id n),
                                        :val (nth chunks (int ^java.lang.Number n))}]
                           (conj
                             rets
                             (df/-future-with-channel-impl
                               (deref chunk-pool)
                               (fn fn__14950
                                 ([]
                                   (let [m_14951 {:event :cassandra-values/put-value-chunk,
                                                  :id id,
                                                  :n n,
                                                  :bufsize
                                                  (java.lang.Integer/valueOf
                                                    (int
                                                      (.remaining
                                                        (nth chunks (int ^java.lang.Number n)))))}
                                         ___8552__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values-v4")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_14951 :phase :begin))))
                                                           nil)
                                         start__8553__auto__ (java.lang.System/nanoTime)
                                         result__8554__auto__ (try
                                                                {:returned
                                                                 (*retry*
                                                                   (fn
                                                                     fn__14955
                                                                     ([]
                                                                       (cass/cql-insert
                                                                         session
                                                                         table
                                                                         cql-keys
                                                                         val_map
                                                                         false))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8555__auto__
                                                                  {:threw t__8555__auto__}))
                                         elapsed_14952 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8553__auto__)
                                         msec_14953 (logger/format-as-msec (long elapsed_14952))]
                                     (let [endmsg__8556__auto__ (merge
                                                                  (assoc
                                                                    m_14951
                                                                    :msec
                                                                    msec_14953
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8554__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8554__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.cassandra-values-v4")]
                                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                         (.debug
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8556__auto__)))
                                       nil)
                                     (if (contains? result__8554__auto__ :returned)
                                       (:returned result__8554__auto__)
                                       (do (throw (:threw result__8554__auto__)) nil))))))))))
                     rets
                     (range 1 (java.lang.Integer/valueOf (int n)))))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values-v4")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:cassandra-values/put rets,
                 :size (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer v)))})))
          nil)
        :created)))
  (reset-meta!
    #'put-value
    (assoc
      {:arglists
       (clojure.core/list
         ['session 'table {:keys ['id 'rev (.withMeta 'v {:tag 'ByteBuffer})], :as 'v-map}]),
       :column (int 1)}
      :name
      'put-value
      :ns
      *ns*))
  (defn get-value
    ([session table id]
      (let [temp__5804__auto__ (let [m_14970 {:event :cassandra-values/get-value, :id id}
                                     ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values-v4")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_14970 :phase :begin))))
                                                       nil)
                                     start__8553__auto__ (java.lang.System/nanoTime)
                                     result__8554__auto__ (try
                                                            {:returned
                                                             (cass/cql-select
                                                               session
                                                               table
                                                               id
                                                               cql-keys
                                                               false)}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8555__auto__
                                                              {:threw t__8555__auto__}))
                                     elapsed_14971 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8553__auto__)
                                     msec_14972 (logger/format-as-msec (long elapsed_14971))]
                                 (let [endmsg__8556__auto__ (merge
                                                              (assoc
                                                                m_14970
                                                                :msec
                                                                msec_14972
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8554__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8554__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.cassandra-values-v4")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8556__auto__)))
                                   nil)
                                 (if (contains? result__8554__auto__ :returned)
                                   (:returned result__8554__auto__)
                                   (do (throw (:threw result__8554__auto__)) nil)))]
        (when temp__5804__auto__
          (let [map__14975 temp__5804__auto__
                map__14975 (if (seq? map__14975)
                             (if (next map__14975)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__14975))
                               (if (seq map__14975) (first map__14975) {}))
                             map__14975)
                ret map__14975
                chunks (get map__14975 :chunks)
                val (get map__14975 :val)
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val :chunks) m)]
            (if (= chunks 1)
              (and val (assoc ret :v val))
              (let [results (into
                              [val]
                              (pmap
                                (fn fn__14976
                                  ([p1__14969#]
                                    (:val
                                      (let [m_14977 {:event :cassandra-values/get-value-chunk,
                                                     :id id,
                                                     :n p1__14969#}
                                            ___8552__auto__ (let
                                                              [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values-v4")]
                                                              (when
                                                                (.isDebugEnabled
                                                                  ^org.slf4j.Logger logger)
                                                                (.debug
                                                                  ^org.slf4j.Logger logger
                                                                  (logger/process
                                                                    (assoc
                                                                      m_14977
                                                                      :phase
                                                                      :begin))))
                                                              nil)
                                            start__8553__auto__ (java.lang.System/nanoTime)
                                            result__8554__auto__ (try
                                                                   {:returned
                                                                    (*retry*
                                                                      (fn
                                                                        fn__14981
                                                                        ([]
                                                                          (cass/cql-select
                                                                            session
                                                                            table
                                                                            (chunk-key
                                                                              id
                                                                              p1__14969#)
                                                                            cql-keys
                                                                            false))))}
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     t__8555__auto__
                                                                     {:threw t__8555__auto__}))
                                            elapsed_14978 (-
                                                            (java.lang.System/nanoTime)
                                                            start__8553__auto__)
                                            msec_14979 (logger/format-as-msec
                                                         (long elapsed_14978))]
                                        (let [endmsg__8556__auto__ (merge
                                                                     (assoc
                                                                       m_14977
                                                                       :msec
                                                                       msec_14979
                                                                       :phase
                                                                       :end)
                                                                     (when
                                                                       (:threw
                                                                         result__8554__auto__)
                                                                       {:threw
                                                                        (class
                                                                          (:threw
                                                                            result__8554__auto__))}))
                                              logger (org.slf4j.LoggerFactory/getLogger
                                                       "datomic.cassandra-values-v4")]
                                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                            (.debug
                                              ^org.slf4j.Logger logger
                                              (logger/process endmsg__8556__auto__)))
                                          nil)
                                        (if (contains? result__8554__auto__ :returned)
                                          (:returned result__8554__auto__)
                                          (do (throw (:threw result__8554__auto__)) nil))))))
                                (range 1 chunks)))]
                (when (every? identity results) (assoc ret :v (io/unchunk results))))))))))
  (reset-meta!
    #'get-value
    (assoc
      {:arglists (clojure.core/list ['session 'table 'id]), :column (int 1)}
      :name
      'get-value
      :ns
      *ns*))
  (defn delete-value
    ([session table id]
      (let [m_14998 {:event :cassandra-values/delete-value, :id id}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.cassandra-values-v4")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_14998 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned
                                    (let [temp__5804__auto__ (let
                                                               [m_15002
                                                                {:event
                                                                 :cassandra-values/get-value,
                                                                 :id id}
                                                                ___8552__auto__
                                                                (let
                                                                  [logger
                                                                   (org.slf4j.LoggerFactory/getLogger
                                                                     "datomic.cassandra-values-v4")]
                                                                  (when
                                                                    (.isDebugEnabled
                                                                      ^org.slf4j.Logger logger)
                                                                    (.debug
                                                                      ^org.slf4j.Logger logger
                                                                      (logger/process
                                                                        (assoc
                                                                          m_15002
                                                                          :phase
                                                                          :begin))))
                                                                  nil)
                                                                start__8553__auto__
                                                                (java.lang.System/nanoTime)
                                                                result__8554__auto__
                                                                (try
                                                                  {:returned
                                                                   (cass/cql-select
                                                                     session
                                                                     table
                                                                     id
                                                                     cql-keys
                                                                     false)}
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8555__auto__
                                                                    {:threw t__8555__auto__}))
                                                                elapsed_15003
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8553__auto__)
                                                                msec_15004
                                                                (logger/format-as-msec
                                                                  (long elapsed_15003))]
                                                               (let
                                                                 [endmsg__8556__auto__
                                                                  (merge
                                                                    (assoc
                                                                      m_15002
                                                                      :msec
                                                                      msec_15004
                                                                      :phase
                                                                      :end)
                                                                    (when
                                                                      (:threw result__8554__auto__)
                                                                      {:threw
                                                                       (class
                                                                         (:threw
                                                                           result__8554__auto__))}))
                                                                  logger
                                                                  (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values-v4")]
                                                                 (when
                                                                   (.isDebugEnabled
                                                                     ^org.slf4j.Logger logger)
                                                                   (.debug
                                                                     ^org.slf4j.Logger logger
                                                                     (logger/process
                                                                       endmsg__8556__auto__)))
                                                                 nil)
                                                               (if
                                                                 (contains?
                                                                   result__8554__auto__
                                                                   :returned)
                                                                 (:returned result__8554__auto__)
                                                                 (do
                                                                   (throw
                                                                     (:threw result__8554__auto__))
                                                                   1)))]
                                      (when temp__5804__auto__
                                        (let [map__15007 temp__5804__auto__
                                              map__15007 (if (seq? map__15007)
                                                           (if
                                                             (next map__15007)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__15007))
                                                             (if
                                                               (seq map__15007)
                                                               (first map__15007)
                                                               {}))
                                                           map__15007)
                                              chunks (get map__15007 :chunks)]
                                          (dorun
                                            (map
                                              (fn fn__15008
                                                ([p1__14997#]
                                                  (let [m_15009 {:event
                                                                 :cassandra-values/delete-value-chunk,
                                                                 :id id,
                                                                 :n p1__14997#}
                                                        ___8552__auto__ (let
                                                                          [logger
                                                                           (org.slf4j.LoggerFactory/getLogger
                                                                             "datomic.cassandra-values-v4")]
                                                                          (when
                                                                            (.isDebugEnabled
                                                                              ^org.slf4j.Logger logger)
                                                                            (.debug
                                                                              ^org.slf4j.Logger logger
                                                                              (logger/process
                                                                                (assoc
                                                                                  m_15009
                                                                                  :phase
                                                                                  :begin))))
                                                                          nil)
                                                        start__8553__auto__ (java.lang.System/nanoTime)
                                                        result__8554__auto__ (try
                                                                               {:returned
                                                                                (cass/cql-delete
                                                                                  session
                                                                                  table
                                                                                  (chunk-key
                                                                                    id
                                                                                    p1__14997#)
                                                                                  cql-keys)}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8555__auto__
                                                                                 {:threw
                                                                                  t__8555__auto__}))
                                                        elapsed_15010 (-
                                                                        (java.lang.System/nanoTime)
                                                                        start__8553__auto__)
                                                        msec_15011 (logger/format-as-msec
                                                                     (long elapsed_15010))]
                                                    (let [endmsg__8556__auto__ (merge
                                                                                 (assoc
                                                                                   m_15009
                                                                                   :msec
                                                                                   msec_15011
                                                                                   :phase
                                                                                   :end)
                                                                                 (when
                                                                                   (:threw
                                                                                     result__8554__auto__)
                                                                                   {:threw
                                                                                    (class
                                                                                      (:threw
                                                                                        result__8554__auto__))}))
                                                          logger (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.cassandra-values-v4")]
                                                      (when (.isDebugEnabled
                                                              ^org.slf4j.Logger logger)
                                                        (.debug
                                                          ^org.slf4j.Logger logger
                                                          (logger/process endmsg__8556__auto__)))
                                                      nil)
                                                    (if (contains? result__8554__auto__ :returned)
                                                      (:returned result__8554__auto__)
                                                      (do
                                                        (throw (:threw result__8554__auto__))
                                                        nil)))))
                                              (range 1 chunks)))
                                          (cass/cql-delete session table id cql-keys))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_14999 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_15000 (logger/format-as-msec (long elapsed_14999))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_14998 :msec msec_15000 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values-v4")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'delete-value
    (assoc
      {:arglists (clojure.core/list ['session 'table 'id]), :column (int 1)}
      :name
      'delete-value
      :ns
      *ns*)))