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
  (let [v__6812__auto__ #'chunk-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
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
    ([session table p__22677]
      (let [map__22678 p__22677
            map__22678 (if (seq? map__22678)
                         (if (next map__22678)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22678))
                           (if (seq map__22678) (first map__22678) {}))
                         map__22678)
            v_map map__22678
            id (get map__22678 :id)
            rev (get map__22678 :rev)
            v (get map__22678 :v)
            m (dissoc v_map :id :rev :v)
            chunks (io/chunk v 358400)
            n (count chunks)
            val_map {:chunks (java.lang.Integer/valueOf (int n)),
                     :id2 id,
                     :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                     :rev rev,
                     :val (nth chunks (int 0))}
            rets [(atom
                    (let [m_22679 {:event :cassandra-values/put-value,
                                   :id id,
                                   :bufsize
                                   (java.lang.Integer/valueOf
                                     (int (.remaining (nth chunks (int 0)))))}
                          ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.cassandra-values")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_22679 :phase :begin))))
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
                          elapsed_22680 (- (java.lang.System/nanoTime) start__8553__auto__)
                          msec_22681 (logger/format-as-msec (long elapsed_22680))]
                      (let [endmsg__8556__auto__ (merge
                                                   (assoc m_22679 :msec msec_22681 :phase :end)
                                                   (when (:threw result__8554__auto__)
                                                     {:threw
                                                      (class (:threw result__8554__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                        nil)
                      (if (contains? result__8554__auto__ :returned)
                        (:returned result__8554__auto__)
                        (do (throw (:threw result__8554__auto__)) atom))))]
            rets (mapv
                   deref
                   (reduce
                     (fn fn__22684
                       ([rets n]
                         (let [val_map {:id2 (chunk-key id n),
                                        :val (nth chunks (int ^java.lang.Number n))}]
                           (conj
                             rets
                             (df/-future-with-channel-impl
                               (deref chunk-pool)
                               (fn fn__22685
                                 ([]
                                   (let [m_22686 {:event :cassandra-values/put-value-chunk,
                                                  :id id,
                                                  :n n,
                                                  :bufsize
                                                  (java.lang.Integer/valueOf
                                                    (int
                                                      (.remaining
                                                        (nth chunks (int ^java.lang.Number n)))))}
                                         ___8552__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_22686 :phase :begin))))
                                                           nil)
                                         start__8553__auto__ (java.lang.System/nanoTime)
                                         result__8554__auto__ (try
                                                                {:returned
                                                                 (*retry*
                                                                   (fn 
                                                                     fn__22690
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
                                         elapsed_22687 (-
                                                         (java.lang.System/nanoTime)
                                                         start__8553__auto__)
                                         msec_22688 (logger/format-as-msec (long elapsed_22687))]
                                     (let [endmsg__8556__auto__ (merge
                                                                  (assoc
                                                                    m_22686
                                                                    :msec
                                                                    msec_22688
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8554__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8554__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.cassandra-values")]
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
      (let [temp__5804__auto__ (let [m_22705 {:event :cassandra-values/get-value, :id id}
                                     ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_22705 :phase :begin))))
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
                                     elapsed_22706 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8553__auto__)
                                     msec_22707 (logger/format-as-msec (long elapsed_22706))]
                                 (let [endmsg__8556__auto__ (merge
                                                              (assoc
                                                                m_22705
                                                                :msec
                                                                msec_22707
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8554__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8554__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.cassandra-values")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8556__auto__)))
                                   nil)
                                 (if (contains? result__8554__auto__ :returned)
                                   (:returned result__8554__auto__)
                                   (do (throw (:threw result__8554__auto__)) nil)))]
        (when temp__5804__auto__
          (let [map__22710 temp__5804__auto__
                map__22710 (if (seq? map__22710)
                             (if (next map__22710)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__22710))
                               (if (seq map__22710) (first map__22710) {}))
                             map__22710)
                ret map__22710
                chunks (get map__22710 :chunks)
                val (get map__22710 :val)
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val :chunks) m)]
            (if (= chunks 1)
              (and val (assoc ret :v val))
              (let [results (into
                              [val]
                              (pmap
                                (fn fn__22711
                                  ([p1__22704#]
                                    (:val
                                      (let [m_22712 {:event :cassandra-values/get-value-chunk,
                                                     :id id,
                                                     :n p1__22704#}
                                            ___8552__auto__ (let 
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
                                                                      m_22712
                                                                      :phase
                                                                      :begin))))
                                                              nil)
                                            start__8553__auto__ (java.lang.System/nanoTime)
                                            result__8554__auto__ (try
                                                                   {:returned
                                                                    (*retry*
                                                                      (fn 
                                                                        fn__22716
                                                                        ([]
                                                                          (cass/cql-select
                                                                            session
                                                                            table
                                                                            (chunk-key
                                                                              id
                                                                              p1__22704#)
                                                                            cql-keys
                                                                            false))))}
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     t__8555__auto__
                                                                     {:threw t__8555__auto__}))
                                            elapsed_22713 (-
                                                            (java.lang.System/nanoTime)
                                                            start__8553__auto__)
                                            msec_22714 (logger/format-as-msec
                                                         (long elapsed_22713))]
                                        (let [endmsg__8556__auto__ (merge
                                                                     (assoc
                                                                       m_22712
                                                                       :msec
                                                                       msec_22714
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
                                                       "datomic.cassandra-values")]
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
      (let [m_22733 {:event :cassandra-values/delete-value, :id id}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.cassandra-values")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_22733 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned
                                    (let [temp__5804__auto__ (let 
                                                               [m_22737
                                                                {:event
                                                                 :cassandra-values/get-value,
                                                                 :id id}
                                                                ___8552__auto__
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
                                                                          m_22737
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
                                                                elapsed_22738
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8553__auto__)
                                                                msec_22739
                                                                (logger/format-as-msec
                                                                  (long elapsed_22738))]
                                                               (let 
                                                                 [endmsg__8556__auto__
                                                                  (merge
                                                                    (assoc
                                                                      m_22737
                                                                      :msec
                                                                      msec_22739
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
                                                                    "datomic.cassandra-values")]
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
                                        (let [map__22742 temp__5804__auto__
                                              map__22742 (if (seq? map__22742)
                                                           (if
                                                             (next map__22742)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__22742))
                                                             (if
                                                               (seq map__22742)
                                                               (first map__22742)
                                                               {}))
                                                           map__22742)
                                              chunks (get map__22742 :chunks)]
                                          (dorun
                                            (map
                                              (fn fn__22743
                                                ([p1__22732#]
                                                  (let [m_22744 {:event
                                                                 :cassandra-values/delete-value-chunk,
                                                                 :id id,
                                                                 :n p1__22732#}
                                                        ___8552__auto__ (let 
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
                                                                                  m_22744
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
                                                                                    p1__22732#)
                                                                                  cql-keys)}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8555__auto__
                                                                                 {:threw
                                                                                  t__8555__auto__}))
                                                        elapsed_22745 (-
                                                                        (java.lang.System/nanoTime)
                                                                        start__8553__auto__)
                                                        msec_22746 (logger/format-as-msec
                                                                     (long elapsed_22745))]
                                                    (let [endmsg__8556__auto__ (merge
                                                                                 (assoc
                                                                                   m_22744
                                                                                   :msec
                                                                                   msec_22746
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
                                                                   "datomic.cassandra-values")]
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
            elapsed_22734 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_22735 (logger/format-as-msec (long elapsed_22734))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_22733 :msec msec_22735 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'delete-value
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'id]),
       :column (int 1)}
      :name
      'delete-value
      :ns
      *ns*)))