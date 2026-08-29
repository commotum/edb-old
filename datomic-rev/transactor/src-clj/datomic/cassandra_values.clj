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
  (def put-value
   (fn put_value
     ([session table p__26681]
       (let [map__26682 p__26681
             map__26682 (if (seq? map__26682)
                          (if (next map__26682)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26682))
                            (if (seq map__26682) (first map__26682) {}))
                          map__26682)
             v_map map__26682
             id (get map__26682 :id)
             rev (get map__26682 :rev)
             v (get map__26682 :v)
             m (dissoc v_map :id :rev :v)
             chunks (io/chunk v 358400)
             n (count chunks)
             val_map {:chunks (java.lang.Integer/valueOf (int n)),
                      :id2 id,
                      :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                      :rev rev,
                      :val (nth chunks (int 0))}
             rets [(atom
                     (let [m_26683 {:event :cassandra-values/put-value,
                                    :id id,
                                    :bufsize
                                    (java.lang.Integer/valueOf
                                      (int (.remaining (nth chunks (int 0)))))}
                           ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                          "datomic.cassandra-values")]
                                             (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                               (.debug
                                                 ^org.slf4j.Logger logger
                                                 (logger/process (assoc m_26683 :phase :begin))))
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
                           elapsed_26684 (- (java.lang.System/nanoTime) start__8584__auto__)
                           msec_26685 (logger/format-as-msec (long elapsed_26684))]
                       (let [endmsg__8587__auto__ (merge
                                                    (assoc m_26683 :msec msec_26685 :phase :end)
                                                    (when (:threw result__8585__auto__)
                                                      {:threw
                                                       (class (:threw result__8585__auto__))}))
                             logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
                         (when (.isDebugEnabled ^org.slf4j.Logger logger)
                           (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                         nil)
                       (if (contains? result__8585__auto__ :returned)
                         (:returned result__8585__auto__)
                         (do (throw (:threw result__8585__auto__)) atom))))]
             rets (mapv
                    deref
                    (reduce
                      (fn fn__26688
                        ([rets n]
                          (let [val_map {:id2 (chunk-key id n),
                                         :val (nth chunks (int ^java.lang.Number n))}]
                            (conj
                              rets
                              (let [f__16176__auto__ (df/-future-with-channel-impl
                                                       (deref chunk-pool)
                                                       (fn fn__26689
                                                         ([]
                                                           (let 
                                                             [m_26690
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
                                                                   "datomic.cassandra-values")]
                                                                (when
                                                                  (.isDebugEnabled
                                                                    ^org.slf4j.Logger logger)
                                                                  (.debug
                                                                    ^org.slf4j.Logger logger
                                                                    (logger/process
                                                                      (assoc
                                                                        m_26690
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
                                                                     fn__26694
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
                                                              elapsed_26691
                                                              (-
                                                                (java.lang.System/nanoTime)
                                                                start__8584__auto__)
                                                              msec_26692
                                                              (logger/format-as-msec
                                                                (long elapsed_26691))]
                                                             (let 
                                                               [endmsg__8587__auto__
                                                                (merge
                                                                  (assoc
                                                                    m_26690
                                                                    :msec
                                                                    msec_26692
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
                                                                  "datomic.cassandra-values")]
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
                                  {:line 63, :column 34, :file "datomic/cassandra_values.clj"}
                                  (deref df/bounding-warn-seconds))
                                f__16176__auto__)))))
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
         :created))))
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
      (let [temp__5804__auto__ (let [m_26711 {:event :cassandra-values/get-value, :id id}
                                     ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_26711 :phase :begin))))
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
                                     elapsed_26712 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8584__auto__)
                                     msec_26713 (logger/format-as-msec (long elapsed_26712))]
                                 (let [endmsg__8587__auto__ (merge
                                                              (assoc
                                                                m_26711
                                                                :msec
                                                                msec_26713
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8585__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8585__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.cassandra-values")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8587__auto__)))
                                   nil)
                                 (if (contains? result__8585__auto__ :returned)
                                   (:returned result__8585__auto__)
                                   (do (throw (:threw result__8585__auto__)) nil)))]
        (when temp__5804__auto__
          (let [map__26716 temp__5804__auto__
                map__26716 (if (seq? map__26716)
                             (if (next map__26716)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__26716))
                               (if (seq map__26716) (first map__26716) {}))
                             map__26716)
                ret map__26716
                chunks (get map__26716 :chunks)
                val (get map__26716 :val)
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val :chunks) m)]
            (if (= chunks 1)
              (and val (assoc ret :v val))
              (let [results (into
                              [val]
                              (pmap
                                (fn fn__26717
                                  ([p1__26710#]
                                    (:val
                                      (let [m_26718 {:event :cassandra-values/get-value-chunk,
                                                     :id id,
                                                     :n p1__26710#}
                                            ___8583__auto__ (let 
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
                                                                      m_26718
                                                                      :phase
                                                                      :begin))))
                                                              nil)
                                            start__8584__auto__ (java.lang.System/nanoTime)
                                            result__8585__auto__ (try
                                                                   {:returned
                                                                    (*retry*
                                                                      (fn 
                                                                        fn__26722
                                                                        ([]
                                                                          (cass/cql-select
                                                                            session
                                                                            table
                                                                            (chunk-key
                                                                              id
                                                                              p1__26710#)
                                                                            cql-keys
                                                                            false))))}
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     t__8586__auto__
                                                                     {:threw t__8586__auto__}))
                                            elapsed_26719 (-
                                                            (java.lang.System/nanoTime)
                                                            start__8584__auto__)
                                            msec_26720 (logger/format-as-msec
                                                         (long elapsed_26719))]
                                        (let [endmsg__8587__auto__ (merge
                                                                     (assoc
                                                                       m_26718
                                                                       :msec
                                                                       msec_26720
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
                                                       "datomic.cassandra-values")]
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
  (reset-meta!
    #'get-value
    (assoc
      {:arglists (clojure.core/list ['session 'table 'id]), :column (int 1)}
      :name
      'get-value
      :ns
      *ns*))
  (def delete-value
   (fn delete_value
     ([session table id]
       (let [m_26739 {:event :cassandra-values/delete-value, :id id}
             ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                            "datomic.cassandra-values")]
                               (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                 (.debug
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_26739 :phase :begin))))
                               nil)
             start__8584__auto__ (java.lang.System/nanoTime)
             result__8585__auto__ (try
                                    {:returned
                                     (let [temp__5804__auto__ (let 
                                                                [m_26743
                                                                 {:event
                                                                  :cassandra-values/get-value,
                                                                  :id id}
                                                                 ___8583__auto__
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
                                                                           m_26743
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
                                                                 elapsed_26744
                                                                 (-
                                                                   (java.lang.System/nanoTime)
                                                                   start__8584__auto__)
                                                                 msec_26745
                                                                 (logger/format-as-msec
                                                                   (long elapsed_26744))]
                                                                (let 
                                                                  [endmsg__8587__auto__
                                                                   (merge
                                                                     (assoc
                                                                       m_26743
                                                                       :msec
                                                                       msec_26745
                                                                       :phase
                                                                       :end)
                                                                     (when
                                                                       (:threw
                                                                         result__8585__auto__)
                                                                       {:threw
                                                                        (class
                                                                          (:threw
                                                                            result__8585__auto__))}))
                                                                   logger
                                                                   (org.slf4j.LoggerFactory/getLogger
                                                                     "datomic.cassandra-values")]
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
                                                                      (:threw
                                                                        result__8585__auto__))
                                                                    1)))]
                                       (when temp__5804__auto__
                                         (let [map__26748 temp__5804__auto__
                                               map__26748 (if (seq? map__26748)
                                                            (if
                                                              (next map__26748)
                                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                (to-array map__26748))
                                                              (if
                                                                (seq map__26748)
                                                                (first map__26748)
                                                                {}))
                                                            map__26748)
                                               chunks (get map__26748 :chunks)]
                                           (dorun
                                             (map
                                               (fn fn__26749
                                                 ([p1__26738#]
                                                   (let [m_26750 {:event
                                                                  :cassandra-values/delete-value-chunk,
                                                                  :id id,
                                                                  :n p1__26738#}
                                                         ___8583__auto__ (let 
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
                                                                                   m_26750
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
                                                                                     p1__26738#)
                                                                                   cql-keys)}
                                                                                (catch
                                                                                  java.lang.Throwable
                                                                                  t__8586__auto__
                                                                                  {:threw
                                                                                   t__8586__auto__}))
                                                         elapsed_26751 (-
                                                                         (java.lang.System/nanoTime)
                                                                         start__8584__auto__)
                                                         msec_26752 (logger/format-as-msec
                                                                      (long elapsed_26751))]
                                                     (let [endmsg__8587__auto__ (merge
                                                                                  (assoc
                                                                                    m_26750
                                                                                    :msec
                                                                                    msec_26752
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
                                                                    "datomic.cassandra-values")]
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
             elapsed_26740 (- (java.lang.System/nanoTime) start__8584__auto__)
             msec_26741 (logger/format-as-msec (long elapsed_26740))]
         (let [endmsg__8587__auto__ (merge
                                      (assoc m_26739 :msec msec_26741 :phase :end)
                                      (when (:threw result__8585__auto__)
                                        {:threw (class (:threw result__8585__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
           (when (.isDebugEnabled ^org.slf4j.Logger logger)
             (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
           nil)
         (if (contains? result__8585__auto__ :returned)
           (:returned result__8585__auto__)
           (do (throw (:threw result__8585__auto__)) nil))))))
  (reset-meta!
    #'delete-value
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'id]),
       :column (int 1)}
      :name
      'delete-value
      :ns
      *ns*)))