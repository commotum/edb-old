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
  (let [v__6837__auto__ #'chunk-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
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
    ([session table p__27211]
      (let [map__27212 p__27211
            map__27212 (if (seq? map__27212)
                         (if (next map__27212)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27212))
                           (if (seq map__27212) (first map__27212) {}))
                         map__27212)
            v_map map__27212
            id (get map__27212 :id)
            rev (get map__27212 :rev)
            v (get map__27212 :v)
            m (dissoc v_map :id :rev :v)
            chunks (mapv
                     (fn fn__27213 ([bbuf] (.asReadOnlyBuffer ^java.nio.ByteBuffer bbuf)))
                     (io/chunk v 358400))
            n (count chunks)
            val_map {:chunks (java.lang.Integer/valueOf (int n)),
                     :id2 id,
                     :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                     :rev rev,
                     :val (nth chunks (int 0))}
            rets [(atom
                    (let [m_27215 {:event :cassandra-values/put-value,
                                   :id id,
                                   :bufsize
                                   (java.lang.Integer/valueOf
                                     (int (.remaining (nth chunks (int 0)))))}
                          ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.cassandra-values-v4")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_27215 :phase :begin))))
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
                          elapsed_27216 (- (java.lang.System/nanoTime) start__8599__auto__)
                          msec_27217 (logger/format-as-msec (long elapsed_27216))]
                      (let [endmsg__8602__auto__ (merge
                                                   (assoc m_27215 :msec msec_27217 :phase :end)
                                                   (when (:threw result__8600__auto__)
                                                     {:threw
                                                      (class (:threw result__8600__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger
                                     "datomic.cassandra-values-v4")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                        nil)
                      (if (contains? result__8600__auto__ :returned)
                        (:returned result__8600__auto__)
                        (do (throw (:threw result__8600__auto__)) atom))))]
            rets (mapv
                   deref
                   (reduce
                     (fn fn__27220
                       ([rets n]
                         (let [val_map {:id2 (chunk-key id n),
                                        :val (nth chunks (int ^java.lang.Number n))}]
                           (conj
                             rets
                             (df/-future-with-channel-impl
                               (deref chunk-pool)
                               (fn fn__27221
                                 ([]
                                   (let [m_27222 {:event :cassandra-values/put-value-chunk,
                                                  :id id,
                                                  :n n,
                                                  :bufsize
                                                  (java.lang.Integer/valueOf
                                                    (int
                                                      (.remaining
                                                        (nth chunks (int ^java.lang.Number n)))))}
                                         ___8598__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values-v4")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_27222 :phase :begin))))
                                                           nil)
                                         start__8599__auto__ (java.lang.System/nanoTime)
                                         result__8600__auto__ (try
                                                                {:returned
                                                                 (*retry*
                                                                   (fn 
                                                                     fn__27226
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
                                         elapsed_27223 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8599__auto__)
                                         msec_27224 (logger/format-as-msec (long elapsed_27223))]
                                     (let [endmsg__8602__auto__ (merge
                                                                  (assoc
                                                                    m_27222
                                                                    :msec
                                                                    msec_27224
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8600__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8600__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.cassandra-values-v4")]
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
      (let [temp__5825__auto__ (let [m_27241 {:event :cassandra-values/get-value, :id id}
                                     ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values-v4")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_27241 :phase :begin))))
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
                                     elapsed_27242 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8599__auto__)
                                     msec_27243 (logger/format-as-msec (long elapsed_27242))]
                                 (let [endmsg__8602__auto__ (merge
                                                              (assoc
                                                                m_27241
                                                                :msec
                                                                msec_27243
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8600__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8600__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.cassandra-values-v4")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8602__auto__)))
                                   nil)
                                 (if (contains? result__8600__auto__ :returned)
                                   (:returned result__8600__auto__)
                                   (do (throw (:threw result__8600__auto__)) nil)))]
        (when temp__5825__auto__
          (let [map__27246 temp__5825__auto__
                map__27246 (if (seq? map__27246)
                             (if (next map__27246)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__27246))
                               (if (seq map__27246) (first map__27246) {}))
                             map__27246)
                ret map__27246
                chunks (get map__27246 :chunks)
                val (get map__27246 :val)
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val :chunks) m)]
            (if (= chunks 1)
              (and val (assoc ret :v val))
              (let [results (into
                              [val]
                              (pmap
                                (fn fn__27247
                                  ([p1__27240#]
                                    (:val
                                      (let [m_27248 {:event :cassandra-values/get-value-chunk,
                                                     :id id,
                                                     :n p1__27240#}
                                            ___8598__auto__ (let 
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
                                                                      m_27248
                                                                      :phase
                                                                      :begin))))
                                                              nil)
                                            start__8599__auto__ (java.lang.System/nanoTime)
                                            result__8600__auto__ (try
                                                                   {:returned
                                                                    (*retry*
                                                                      (fn 
                                                                        fn__27252
                                                                        ([]
                                                                          (cass/cql-select
                                                                            session
                                                                            table
                                                                            (chunk-key
                                                                              id
                                                                              p1__27240#)
                                                                            cql-keys
                                                                            false))))}
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     t__8601__auto__
                                                                     {:threw t__8601__auto__}))
                                            elapsed_27249 (-
                                                            (java.lang.System/nanoTime)
                                                            start__8599__auto__)
                                            msec_27250 (logger/format-as-msec
                                                         (long elapsed_27249))]
                                        (let [endmsg__8602__auto__ (merge
                                                                     (assoc
                                                                       m_27248
                                                                       :msec
                                                                       msec_27250
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
                                                       "datomic.cassandra-values-v4")]
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
      (let [m_27269 {:event :cassandra-values/delete-value, :id id}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.cassandra-values-v4")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_27269 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned
                                    (let [temp__5825__auto__ (let 
                                                               [m_27273
                                                                {:event
                                                                 :cassandra-values/get-value,
                                                                 :id id}
                                                                ___8598__auto__
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
                                                                          m_27273
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
                                                                elapsed_27274
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8599__auto__)
                                                                msec_27275
                                                                (logger/format-as-msec
                                                                  (long elapsed_27274))]
                                                               (let 
                                                                 [endmsg__8602__auto__
                                                                  (merge
                                                                    (assoc
                                                                      m_27273
                                                                      :msec
                                                                      msec_27275
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
                                                                    "datomic.cassandra-values-v4")]
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
                                        (let [map__27278 temp__5825__auto__
                                              map__27278 (if (seq? map__27278)
                                                           (if
                                                             (next map__27278)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__27278))
                                                             (if
                                                               (seq map__27278)
                                                               (first map__27278)
                                                               {}))
                                                           map__27278)
                                              chunks (get map__27278 :chunks)]
                                          (dorun
                                            (map
                                              (fn fn__27279
                                                ([p1__27268#]
                                                  (let [m_27280 {:event
                                                                 :cassandra-values/delete-value-chunk,
                                                                 :id id,
                                                                 :n p1__27268#}
                                                        ___8598__auto__ (let 
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
                                                                                  m_27280
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
                                                                                    p1__27268#)
                                                                                  cql-keys)}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8601__auto__
                                                                                 {:threw
                                                                                  t__8601__auto__}))
                                                        elapsed_27281 (-
                                                                        (java.lang.System/nanoTime)
                                                                        start__8599__auto__)
                                                        msec_27282 (logger/format-as-msec
                                                                     (long elapsed_27281))]
                                                    (let [endmsg__8602__auto__ (merge
                                                                                 (assoc
                                                                                   m_27280
                                                                                   :msec
                                                                                   msec_27282
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
                                                                   "datomic.cassandra-values-v4")]
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
            elapsed_27270 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_27271 (logger/format-as-msec (long elapsed_27270))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_27269 :msec msec_27271 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values-v4")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
  (reset-meta!
    #'delete-value
    (assoc
      {:arglists (clojure.core/list ['session 'table 'id]), :column (int 1)}
      :name
      'delete-value
      :ns
      *ns*)))