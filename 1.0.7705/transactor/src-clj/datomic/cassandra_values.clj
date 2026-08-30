(do
  (clojure.core/in-ns 'datomic.cassandra-values)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.config :as 'config]
        ['datomic.cassandra :as 'cass]
        ['datomic.common :as 'common]
        ['datomic.future :as 'df]
        ['datomic.io :as 'io]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'com.datastax.driver.core.Session)
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.cassandra-values 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cassandra-values))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.config :as 'config]
          ['datomic.cassandra :as 'cass]
          ['datomic.common :as 'common]
          ['datomic.future :as 'df]
          ['datomic.io :as 'io]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'com.datastax.driver.core.Session)
        (clojure.core/import 'java.nio.ByteBuffer))))
  (set! *warn-on-reflection* true)
  (.setMeta
    (clojure.lang.RT/var "datomic.cassandra-values" "CHUNK_SIZE")
    {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra-values" "CHUNK_SIZE") (long (* 350 1024)))
  (.setDynamic (clojure.lang.RT/var "datomic.cassandra-values" "*retry*") true)
  (.setMeta
    (.setDynamic (clojure.lang.RT/var "datomic.cassandra-values" "*retry*") true)
    {:dynamic true, :column (int 1)})
  (defn chunk-key ([k n] (str k "__" n)))
  (reset-meta!
    #'chunk-key
    (assoc {:arglists (clojure.core/list ['k 'n]), :column (int 1)} :name 'chunk-key :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra-values" "chunk-pool") {:column (int 1)})
  (let [v__6837__auto__ #'chunk-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.cassandra-values" "chunk-pool") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.cassandra-values" "chunk-pool")
        (delay
          (common/thread-pool
            {:nthreads (config/property "datomic.writeConcurrency"), :name "cass2-chunk"})))
      #'chunk-pool))
  (def cql-keys [:id2 :rev :map :val :chunks])
  (reset-meta! #'cql-keys (assoc {:column (int 1)} :name 'cql-keys :ns *ns*))
  (defn put-value
    ([session table p__27038]
      (let [map__27039 p__27038
            map__27039 (if (seq? map__27039)
                         (if (next map__27039)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27039))
                           (if (seq map__27039) (first map__27039) {}))
                         map__27039)
            v_map map__27039
            id (get map__27039 :id)
            rev (get map__27039 :rev)
            v (get map__27039 :v)
            m (dissoc v_map :id :rev :v)
            chunks (io/chunk v 358400)
            n (count chunks)
            val_map {:chunks (java.lang.Integer/valueOf (int n)),
                     :id2 id,
                     :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                     :rev rev,
                     :val (nth chunks (int 0))}
            rets [(atom
                    (let [m_27040 {:event :cassandra-values/put-value,
                                   :id id,
                                   :bufsize
                                   (java.lang.Integer/valueOf
                                     (int (.remaining (nth chunks (int 0)))))}
                          ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.cassandra-values")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_27040 :phase :begin))))
                                            nil)
                          start__8599__auto__ (java.lang.System/nanoTime)
                          result__8600__auto__ (try
                                                 {:returned
                                                  (cass/cql-insert
                                                    session
                                                    table
                                                    cql-keys
                                                    val_map
                                                    false)}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8601__auto__
                                                   {:threw t__8601__auto__}))
                          elapsed_27041 (- (java.lang.System/nanoTime) start__8599__auto__)
                          msec_27042 (logger/format-as-msec (long elapsed_27041))]
                      (let [endmsg__8602__auto__ (merge
                                                   (assoc m_27040 :msec msec_27042 :phase :end)
                                                   (when (:threw result__8600__auto__)
                                                     {:threw
                                                      (class (:threw result__8600__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                        nil)
                      (if (contains? result__8600__auto__ :returned)
                        (:returned result__8600__auto__)
                        (do (throw (:threw result__8600__auto__)) atom))))]
            rets (mapv
                   deref
                   (reduce
                     (fn fn__27045
                       ([rets n]
                         (let [val_map {:id2 (chunk-key id n),
                                        :val (nth chunks (int ^java.lang.Number n))}]
                           (conj
                             rets
                             (df/-future-with-channel-impl
                               (deref chunk-pool)
                               (fn fn__27046
                                 ([]
                                   (let [m_27047 {:event :cassandra-values/put-value-chunk,
                                                  :id id,
                                                  :n n,
                                                  :bufsize
                                                  (java.lang.Integer/valueOf
                                                    (int
                                                      (.remaining
                                                        (nth chunks (int ^java.lang.Number n)))))}
                                         ___8598__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_27047 :phase :begin))))
                                                           nil)
                                         start__8599__auto__ (java.lang.System/nanoTime)
                                         result__8600__auto__ (try
                                                                {:returned
                                                                 (*retry*
                                                                   (fn
                                                                     fn__27051
                                                                     ([]
                                                                       (cass/cql-insert
                                                                         session
                                                                         table
                                                                         cql-keys
                                                                         val_map
                                                                         false))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8601__auto__
                                                                  {:threw t__8601__auto__}))
                                         elapsed_27048 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8599__auto__)
                                         msec_27049 (logger/format-as-msec (long elapsed_27048))]
                                     (let [endmsg__8602__auto__ (merge
                                                                  (assoc
                                                                    m_27047
                                                                    :msec
                                                                    msec_27049
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8600__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8600__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.cassandra-values")]
                                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                         (.debug
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8602__auto__)))
                                       nil)
                                     (if (contains? result__8600__auto__ :returned)
                                       (:returned result__8600__auto__)
                                       (do (throw (:threw result__8600__auto__)) nil))))))))))
                     rets
                     (range 1 (java.lang.Integer/valueOf (int n)))))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
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
      (let [temp__5825__auto__ (let [m_27066 {:event :cassandra-values/get-value, :id id}
                                     ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_27066 :phase :begin))))
                                                       nil)
                                     start__8599__auto__ (java.lang.System/nanoTime)
                                     result__8600__auto__ (try
                                                            {:returned
                                                             (cass/cql-select
                                                               session
                                                               table
                                                               id
                                                               cql-keys
                                                               false)}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8601__auto__
                                                              {:threw t__8601__auto__}))
                                     elapsed_27067 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8599__auto__)
                                     msec_27068 (logger/format-as-msec (long elapsed_27067))]
                                 (let [endmsg__8602__auto__ (merge
                                                              (assoc
                                                                m_27066
                                                                :msec
                                                                msec_27068
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8600__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8600__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.cassandra-values")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8602__auto__)))
                                   nil)
                                 (if (contains? result__8600__auto__ :returned)
                                   (:returned result__8600__auto__)
                                   (do (throw (:threw result__8600__auto__)) nil)))]
        (when temp__5825__auto__
          (let [map__27071 temp__5825__auto__
                map__27071 (if (seq? map__27071)
                             (if (next map__27071)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__27071))
                               (if (seq map__27071) (first map__27071) {}))
                             map__27071)
                ret map__27071
                chunks (get map__27071 :chunks)
                val (get map__27071 :val)
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val :chunks) m)]
            (if (= chunks 1)
              (and val (assoc ret :v val))
              (let [results (into
                              [val]
                              (pmap
                                (fn fn__27072
                                  ([p1__27065#]
                                    (:val
                                      (let [m_27073 {:event :cassandra-values/get-value-chunk,
                                                     :id id,
                                                     :n p1__27065#}
                                            ___8598__auto__ (let
                                                              [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values")]
                                                              (when
                                                                (.isDebugEnabled
                                                                  ^org.slf4j.Logger logger)
                                                                (.debug
                                                                  ^org.slf4j.Logger logger
                                                                  (logger/process
                                                                    (assoc
                                                                      m_27073
                                                                      :phase
                                                                      :begin))))
                                                              nil)
                                            start__8599__auto__ (java.lang.System/nanoTime)
                                            result__8600__auto__ (try
                                                                   {:returned
                                                                    (*retry*
                                                                      (fn
                                                                        fn__27077
                                                                        ([]
                                                                          (cass/cql-select
                                                                            session
                                                                            table
                                                                            (chunk-key
                                                                              id
                                                                              p1__27065#)
                                                                            cql-keys
                                                                            false))))}
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     t__8601__auto__
                                                                     {:threw t__8601__auto__}))
                                            elapsed_27074 (-
                                                            (java.lang.System/nanoTime)
                                                            start__8599__auto__)
                                            msec_27075 (logger/format-as-msec
                                                         (long elapsed_27074))]
                                        (let [endmsg__8602__auto__ (merge
                                                                     (assoc
                                                                       m_27073
                                                                       :msec
                                                                       msec_27075
                                                                       :phase
                                                                       :end)
                                                                     (when
                                                                       (:threw
                                                                         result__8600__auto__)
                                                                       {:threw
                                                                        (class
                                                                          (:threw
                                                                            result__8600__auto__))}))
                                              logger (org.slf4j.LoggerFactory/getLogger
                                                       "datomic.cassandra-values")]
                                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                            (.debug
                                              ^org.slf4j.Logger logger
                                              (logger/process endmsg__8602__auto__)))
                                          nil)
                                        (if (contains? result__8600__auto__ :returned)
                                          (:returned result__8600__auto__)
                                          (do (throw (:threw result__8600__auto__)) nil))))))
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
      (let [m_27094 {:event :cassandra-values/delete-value, :id id}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.cassandra-values")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_27094 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned
                                    (let [temp__5825__auto__ (let
                                                               [m_27098
                                                                {:event
                                                                 :cassandra-values/get-value,
                                                                 :id id}
                                                                ___8598__auto__
                                                                (let
                                                                  [logger
                                                                   (org.slf4j.LoggerFactory/getLogger
                                                                     "datomic.cassandra-values")]
                                                                  (when
                                                                    (.isDebugEnabled
                                                                      ^org.slf4j.Logger logger)
                                                                    (.debug
                                                                      ^org.slf4j.Logger logger
                                                                      (logger/process
                                                                        (assoc
                                                                          m_27098
                                                                          :phase
                                                                          :begin))))
                                                                  nil)
                                                                start__8599__auto__
                                                                (java.lang.System/nanoTime)
                                                                result__8600__auto__
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
                                                                    t__8601__auto__
                                                                    {:threw t__8601__auto__}))
                                                                elapsed_27099
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8599__auto__)
                                                                msec_27100
                                                                (logger/format-as-msec
                                                                  (long elapsed_27099))]
                                                               (let
                                                                 [endmsg__8602__auto__
                                                                  (merge
                                                                    (assoc
                                                                      m_27098
                                                                      :msec
                                                                      msec_27100
                                                                      :phase
                                                                      :end)
                                                                    (when
                                                                      (:threw result__8600__auto__)
                                                                      {:threw
                                                                       (class
                                                                         (:threw
                                                                           result__8600__auto__))}))
                                                                  logger
                                                                  (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values")]
                                                                 (when
                                                                   (.isDebugEnabled
                                                                     ^org.slf4j.Logger logger)
                                                                   (.debug
                                                                     ^org.slf4j.Logger logger
                                                                     (logger/process
                                                                       endmsg__8602__auto__)))
                                                                 nil)
                                                               (if
                                                                 (contains?
                                                                   result__8600__auto__
                                                                   :returned)
                                                                 (:returned result__8600__auto__)
                                                                 (do
                                                                   (throw
                                                                     (:threw result__8600__auto__))
                                                                   1)))]
                                      (when temp__5825__auto__
                                        (let [map__27103 temp__5825__auto__
                                              map__27103 (if (seq? map__27103)
                                                           (if
                                                             (next map__27103)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__27103))
                                                             (if
                                                               (seq map__27103)
                                                               (first map__27103)
                                                               {}))
                                                           map__27103)
                                              chunks (get map__27103 :chunks)]
                                          (dorun
                                            (map
                                              (fn fn__27104
                                                ([p1__27093#]
                                                  (let [m_27105 {:event
                                                                 :cassandra-values/delete-value-chunk,
                                                                 :id id,
                                                                 :n p1__27093#}
                                                        ___8598__auto__ (let
                                                                          [logger
                                                                           (org.slf4j.LoggerFactory/getLogger
                                                                             "datomic.cassandra-values")]
                                                                          (when
                                                                            (.isDebugEnabled
                                                                              ^org.slf4j.Logger logger)
                                                                            (.debug
                                                                              ^org.slf4j.Logger logger
                                                                              (logger/process
                                                                                (assoc
                                                                                  m_27105
                                                                                  :phase
                                                                                  :begin))))
                                                                          nil)
                                                        start__8599__auto__ (java.lang.System/nanoTime)
                                                        result__8600__auto__ (try
                                                                               {:returned
                                                                                (cass/cql-delete
                                                                                  session
                                                                                  table
                                                                                  (chunk-key
                                                                                    id
                                                                                    p1__27093#)
                                                                                  cql-keys)}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8601__auto__
                                                                                 {:threw
                                                                                  t__8601__auto__}))
                                                        elapsed_27106 (-
                                                                        (java.lang.System/nanoTime)
                                                                        start__8599__auto__)
                                                        msec_27107 (logger/format-as-msec
                                                                     (long elapsed_27106))]
                                                    (let [endmsg__8602__auto__ (merge
                                                                                 (assoc
                                                                                   m_27105
                                                                                   :msec
                                                                                   msec_27107
                                                                                   :phase
                                                                                   :end)
                                                                                 (when
                                                                                   (:threw
                                                                                     result__8600__auto__)
                                                                                   {:threw
                                                                                    (class
                                                                                      (:threw
                                                                                        result__8600__auto__))}))
                                                          logger (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.cassandra-values")]
                                                      (when (.isDebugEnabled
                                                              ^org.slf4j.Logger logger)
                                                        (.debug
                                                          ^org.slf4j.Logger logger
                                                          (logger/process endmsg__8602__auto__)))
                                                      nil)
                                                    (if (contains? result__8600__auto__ :returned)
                                                      (:returned result__8600__auto__)
                                                      (do
                                                        (throw (:threw result__8600__auto__))
                                                        nil)))))
                                              (range 1 chunks)))
                                          (cass/cql-delete session table id cql-keys))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8601__auto__
                                     {:threw t__8601__auto__}))
            elapsed_27095 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_27096 (logger/format-as-msec (long elapsed_27095))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_27094 :msec msec_27096 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
  (reset-meta!
    #'delete-value
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'id]),
       :column (int 1)}
      :name
      'delete-value
      :ns
      *ns*)))