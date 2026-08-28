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
  (def CHUNK_SIZE (long (* 350 1024)))
  (reset-meta! #'CHUNK_SIZE (assoc {:const true, :column 1} :name 'CHUNK_SIZE :ns *ns*))
  (def ^{:dynamic true} *retry*)
  (reset-meta! #'*retry* (assoc {:dynamic true, :column 1} :name '*retry* :ns *ns*))
  (defn chunk-key ([k n] (str k "__" n)))
  (defonce chunk-pool
   (delay
     (common/thread-pool
       {:nthreads (config/property "datomic.writeConcurrency"), :name "cass3-chunk"})))
  (def cql-keys [:id2 :rev :map :val :chunks])
  (defn put-value
    ([session table p__30162]
      (let [map__30163 p__30162
            map__30163 (if (seq? map__30163)
                         (if (next map__30163)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30163))
                           (if (seq map__30163) (first map__30163) {}))
                         map__30163)
            v_map map__30163
            id (get map__30163 :id)
            rev (get map__30163 :rev)
            v (get map__30163 :v)
            m (dissoc v_map :id :rev :v)
            chunks (mapv
                     (fn fn__30164 ([bbuf] (.asReadOnlyBuffer ^java.nio.ByteBuffer bbuf)))
                     (io/chunk v 358400))
            n (count chunks)
            val_map {:chunks (java.lang.Integer/valueOf (int n)),
                     :id2 id,
                     :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                     :rev rev,
                     :val (nth chunks (int 0))}
            rets [(atom
                    (let [m_30166 {:event :cassandra-values/put-value,
                                   :id id,
                                   :bufsize
                                   (java.lang.Integer/valueOf
                                     (int (.remaining (nth chunks (int 0)))))}
                          ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.cassandra-values-v4")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_30166 :phase :begin))))
                                            nil)
                          start__8584__auto__ (java.lang.System/nanoTime)
                          result__8585__auto__ (try
                                                 {:returned
                                                  (cass/cql-insert
                                                    session
                                                    table
                                                    cql-keys
                                                    val_map
                                                    false)}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8586__auto__
                                                   {:threw t__8586__auto__}))
                          elapsed_30167 (- (java.lang.System/nanoTime) start__8584__auto__)
                          msec_30168 (logger/format-as-msec (long elapsed_30167))]
                      (let [endmsg__8587__auto__ (merge
                                                   (assoc m_30166 :msec msec_30168 :phase :end)
                                                   (when (:threw result__8585__auto__)
                                                     {:threw
                                                      (class (:threw result__8585__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger
                                     "datomic.cassandra-values-v4")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                        nil)
                      (if (contains? result__8585__auto__ :returned)
                        (:returned result__8585__auto__)
                        (do (throw (:threw result__8585__auto__)) atom))))]
            rets (mapv
                   deref
                   (reduce
                     (fn fn__30171
                       ([rets n]
                         (let [val_map {:id2 (chunk-key id n),
                                        :val (nth chunks (int ^java.lang.Number n))}]
                           (conj
                             rets
                             (let [f__16176__auto__ (df/-future-with-channel-impl
                                                      (deref chunk-pool)
                                                      (fn fn__30172
                                                        ([]
                                                          (let [m_30173
                                                                {:event
                                                                 :cassandra-values/put-value-chunk,
                                                                 :id id,
                                                                 :n n,
                                                                 :bufsize
                                                                 (java.lang.Integer/valueOf
                                                                   (int
                                                                     (.remaining
                                                                       (nth
                                                                         chunks
                                                                         (int
                                                                           ^java.lang.Number n)))))}
                                                                ___8583__auto__
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
                                                                          m_30173
                                                                          :phase
                                                                          :begin))))
                                                                  nil)
                                                                start__8584__auto__
                                                                (java.lang.System/nanoTime)
                                                                result__8585__auto__
                                                                (try
                                                                  {:returned
                                                                   (*retry*
                                                                     (fn 
                                                                       fn__30177
                                                                       ([]
                                                                         (cass/cql-insert
                                                                           session
                                                                           table
                                                                           cql-keys
                                                                           val_map
                                                                           false))))}
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8586__auto__
                                                                    {:threw t__8586__auto__}))
                                                                elapsed_30174
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8584__auto__)
                                                                msec_30175
                                                                (logger/format-as-msec
                                                                  (long elapsed_30174))]
                                                            (let 
                                                              [endmsg__8587__auto__
                                                               (merge
                                                                 (assoc
                                                                   m_30173
                                                                   :msec
                                                                   msec_30175
                                                                   :phase
                                                                   :end)
                                                                 (when
                                                                   (:threw result__8585__auto__)
                                                                   {:threw
                                                                    (class
                                                                      (:threw
                                                                        result__8585__auto__))}))
                                                               logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values-v4")]
                                                              (when
                                                                (.isDebugEnabled
                                                                  ^org.slf4j.Logger logger)
                                                                (.debug
                                                                  ^org.slf4j.Logger logger
                                                                  (logger/process
                                                                    endmsg__8587__auto__)))
                                                              nil)
                                                            (if
                                                              (contains?
                                                                result__8585__auto__
                                                                :returned)
                                                              (:returned result__8585__auto__)
                                                              (do
                                                                (throw
                                                                  (:threw result__8585__auto__))
                                                                nil))))))
                                   ch__16177__auto__ (df/get-channel f__16176__auto__)]
                               (df/add-bounding-warning
                                 ch__16177__auto__
                                 {:line 63, :column 34, :file "datomic/cassandra_values_v4.clj"}
                                 (deref df/bounding-warn-seconds))
                               f__16176__auto__)))))
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
  (defn get-value
    ([session table id]
      (let [temp__5804__auto__ (let [m_30194 {:event :cassandra-values/get-value, :id id}
                                     ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values-v4")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_30194 :phase :begin))))
                                                       nil)
                                     start__8584__auto__ (java.lang.System/nanoTime)
                                     result__8585__auto__ (try
                                                            {:returned
                                                             (cass/cql-select
                                                               session
                                                               table
                                                               id
                                                               cql-keys
                                                               false)}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8586__auto__
                                                              {:threw t__8586__auto__}))
                                     elapsed_30195 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8584__auto__)
                                     msec_30196 (logger/format-as-msec (long elapsed_30195))]
                                 (let [endmsg__8587__auto__ (merge
                                                              (assoc
                                                                m_30194
                                                                :msec
                                                                msec_30196
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8585__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8585__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.cassandra-values-v4")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8587__auto__)))
                                   nil)
                                 (if (contains? result__8585__auto__ :returned)
                                   (:returned result__8585__auto__)
                                   (do (throw (:threw result__8585__auto__)) nil)))]
        (when temp__5804__auto__
          (let [map__30199 temp__5804__auto__
                map__30199 (if (seq? map__30199)
                             (if (next map__30199)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__30199))
                               (if (seq map__30199) (first map__30199) {}))
                             map__30199)
                ret map__30199
                chunks (get map__30199 :chunks)
                val (get map__30199 :val)
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val :chunks) m)]
            (if (= chunks 1)
              (and val (assoc ret :v val))
              (let [results (into
                              [val]
                              (pmap
                                (fn fn__30200
                                  ([p1__30193#]
                                    (:val
                                      (let [m_30201 {:event :cassandra-values/get-value-chunk,
                                                     :id id,
                                                     :n p1__30193#}
                                            ___8583__auto__ (let 
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
                                                                      m_30201
                                                                      :phase
                                                                      :begin))))
                                                              nil)
                                            start__8584__auto__ (java.lang.System/nanoTime)
                                            result__8585__auto__ (try
                                                                   {:returned
                                                                    (*retry*
                                                                      (fn 
                                                                        fn__30205
                                                                        ([]
                                                                          (cass/cql-select
                                                                            session
                                                                            table
                                                                            (chunk-key
                                                                              id
                                                                              p1__30193#)
                                                                            cql-keys
                                                                            false))))}
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     t__8586__auto__
                                                                     {:threw t__8586__auto__}))
                                            elapsed_30202 (-
                                                            (java.lang.System/nanoTime)
                                                            start__8584__auto__)
                                            msec_30203 (logger/format-as-msec
                                                         (long elapsed_30202))]
                                        (let [endmsg__8587__auto__ (merge
                                                                     (assoc
                                                                       m_30201
                                                                       :msec
                                                                       msec_30203
                                                                       :phase
                                                                       :end)
                                                                     (when
                                                                       (:threw
                                                                         result__8585__auto__)
                                                                       {:threw
                                                                        (class
                                                                          (:threw
                                                                            result__8585__auto__))}))
                                              logger (org.slf4j.LoggerFactory/getLogger
                                                       "datomic.cassandra-values-v4")]
                                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                            (.debug
                                              ^org.slf4j.Logger logger
                                              (logger/process endmsg__8587__auto__)))
                                          nil)
                                        (if (contains? result__8585__auto__ :returned)
                                          (:returned result__8585__auto__)
                                          (do (throw (:threw result__8585__auto__)) nil))))))
                                (range 1 chunks)))]
                (when (every? identity results) (assoc ret :v (io/unchunk results))))))))))
  (defn delete-value
    ([session table id]
      (let [m_30222 {:event :cassandra-values/delete-value, :id id}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.cassandra-values-v4")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_30222 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [temp__5804__auto__ (let 
                                                               [m_30226
                                                                {:event
                                                                 :cassandra-values/get-value,
                                                                 :id id}
                                                                ___8583__auto__
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
                                                                          m_30226
                                                                          :phase
                                                                          :begin))))
                                                                  nil)
                                                                start__8584__auto__
                                                                (java.lang.System/nanoTime)
                                                                result__8585__auto__
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
                                                                    t__8586__auto__
                                                                    {:threw t__8586__auto__}))
                                                                elapsed_30227
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8584__auto__)
                                                                msec_30228
                                                                (logger/format-as-msec
                                                                  (long elapsed_30227))]
                                                               (let 
                                                                 [endmsg__8587__auto__
                                                                  (merge
                                                                    (assoc
                                                                      m_30226
                                                                      :msec
                                                                      msec_30228
                                                                      :phase
                                                                      :end)
                                                                    (when
                                                                      (:threw result__8585__auto__)
                                                                      {:threw
                                                                       (class
                                                                         (:threw
                                                                           result__8585__auto__))}))
                                                                  logger
                                                                  (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values-v4")]
                                                                 (when
                                                                   (.isDebugEnabled
                                                                     ^org.slf4j.Logger logger)
                                                                   (.debug
                                                                     ^org.slf4j.Logger logger
                                                                     (logger/process
                                                                       endmsg__8587__auto__)))
                                                                 nil)
                                                               (if
                                                                 (contains?
                                                                   result__8585__auto__
                                                                   :returned)
                                                                 (:returned result__8585__auto__)
                                                                 (do
                                                                   (throw
                                                                     (:threw result__8585__auto__))
                                                                   1)))]
                                      (when temp__5804__auto__
                                        (let [map__30231 temp__5804__auto__
                                              map__30231 (if (seq? map__30231)
                                                           (if
                                                             (next map__30231)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__30231))
                                                             (if
                                                               (seq map__30231)
                                                               (first map__30231)
                                                               {}))
                                                           map__30231)
                                              chunks (get map__30231 :chunks)]
                                          (dorun
                                            (map
                                              (fn fn__30232
                                                ([p1__30221#]
                                                  (let [m_30233 {:event
                                                                 :cassandra-values/delete-value-chunk,
                                                                 :id id,
                                                                 :n p1__30221#}
                                                        ___8583__auto__ (let 
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
                                                                                  m_30233
                                                                                  :phase
                                                                                  :begin))))
                                                                          nil)
                                                        start__8584__auto__ (java.lang.System/nanoTime)
                                                        result__8585__auto__ (try
                                                                               {:returned
                                                                                (cass/cql-delete
                                                                                  session
                                                                                  table
                                                                                  (chunk-key
                                                                                    id
                                                                                    p1__30221#)
                                                                                  cql-keys)}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8586__auto__
                                                                                 {:threw
                                                                                  t__8586__auto__}))
                                                        elapsed_30234 (-
                                                                        (java.lang.System/nanoTime)
                                                                        start__8584__auto__)
                                                        msec_30235 (logger/format-as-msec
                                                                     (long elapsed_30234))]
                                                    (let [endmsg__8587__auto__ (merge
                                                                                 (assoc
                                                                                   m_30233
                                                                                   :msec
                                                                                   msec_30235
                                                                                   :phase
                                                                                   :end)
                                                                                 (when
                                                                                   (:threw
                                                                                     result__8585__auto__)
                                                                                   {:threw
                                                                                    (class
                                                                                      (:threw
                                                                                        result__8585__auto__))}))
                                                          logger (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.cassandra-values-v4")]
                                                      (when (.isDebugEnabled
                                                              ^org.slf4j.Logger logger)
                                                        (.debug
                                                          ^org.slf4j.Logger logger
                                                          (logger/process endmsg__8587__auto__)))
                                                      nil)
                                                    (if (contains? result__8585__auto__ :returned)
                                                      (:returned result__8585__auto__)
                                                      (do
                                                        (throw (:threw result__8585__auto__))
                                                        nil)))))
                                              (range 1 chunks)))
                                          (cass/cql-delete session table id cql-keys))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_30223 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_30224 (logger/format-as-msec (long elapsed_30223))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_30222 :msec msec_30224 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values-v4")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil))))))