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
    ([session table p__10281]
      (let [map__10282 p__10281
            map__10282 (if (seq? map__10282)
                         (clojure.lang.PersistentHashMap/create (seq map__10282))
                         map__10282)
            v_map map__10282
            id (get map__10282 :id)
            rev (get map__10282 :rev)
            v (get map__10282 :v)
            m (dissoc v_map :id :rev :v)
            chunks (mapv
                     (fn fn__10283 ([bbuf] (.asReadOnlyBuffer ^java.nio.ByteBuffer bbuf)))
                     (io/chunk v 358400))
            n (count chunks)
            val_map {:chunks (java.lang.Integer/valueOf (int n)),
                     :id2 id,
                     :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                     :rev rev,
                     :val (nth chunks (int 0))}
            rets [(atom
                    (let [m_10285 {:event :cassandra-values/put-value,
                                   :id id,
                                   :bufsize
                                   (java.lang.Integer/valueOf
                                     (int (.remaining (nth chunks (int 0)))))}
                          ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.cassandra-values-v4")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_10285 :phase :begin)))
                                              nil)
                                            nil)
                          start__8981__auto__ (java.lang.System/nanoTime)
                          result__8982__auto__ (try
                                                 {:returned
                                                  (cass/cql-insert
                                                    session
                                                    table
                                                    cql-keys
                                                    val_map
                                                    false)}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8983__auto__
                                                   {:threw t__8983__auto__}))
                          elapsed_10286 (- (java.lang.System/nanoTime) start__8981__auto__)
                          msec_10287 (logger/format-as-msec (long elapsed_10286))]
                      (let [endmsg__8984__auto__ (merge
                                                   (assoc m_10285 :msec msec_10287 :phase :end)
                                                   (when (:threw result__8982__auto__)
                                                     {:threw
                                                      (class (:threw result__8982__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger
                                     "datomic.cassandra-values-v4")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                          nil)
                        nil)
                      (if (contains? result__8982__auto__ :returned)
                        (:returned result__8982__auto__)
                        (do (throw (:threw result__8982__auto__)) atom))))]
            rets (mapv
                   deref
                   (reduce
                     (fn fn__10290
                       ([rets n]
                         (let [val_map {:id2 (chunk-key id n),
                                        :val (nth chunks (int ^java.lang.Number n))}]
                           (conj
                             rets
                             (let [f__10267__auto__ (df/-future-with-channel-impl
                                                      (deref chunk-pool)
                                                      (fn fn__10291
                                                        ([]
                                                          (let [m_10292
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
                                                                ___8980__auto__
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
                                                                          m_10292
                                                                          :phase
                                                                          :begin)))
                                                                    nil)
                                                                  nil)
                                                                start__8981__auto__
                                                                (java.lang.System/nanoTime)
                                                                result__8982__auto__
                                                                (try
                                                                  {:returned
                                                                   (*retry*
                                                                     (fn 
                                                                       fn__10296
                                                                       ([]
                                                                         (cass/cql-insert
                                                                           session
                                                                           table
                                                                           cql-keys
                                                                           val_map
                                                                           false))))}
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8983__auto__
                                                                    {:threw t__8983__auto__}))
                                                                elapsed_10293
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8981__auto__)
                                                                msec_10294
                                                                (logger/format-as-msec
                                                                  (long elapsed_10293))]
                                                            (let 
                                                              [endmsg__8984__auto__
                                                               (merge
                                                                 (assoc
                                                                   m_10292
                                                                   :msec
                                                                   msec_10294
                                                                   :phase
                                                                   :end)
                                                                 (when
                                                                   (:threw result__8982__auto__)
                                                                   {:threw
                                                                    (class
                                                                      (:threw
                                                                        result__8982__auto__))}))
                                                               logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values-v4")]
                                                              (when
                                                                (.isDebugEnabled
                                                                  ^org.slf4j.Logger logger)
                                                                (.debug
                                                                  ^org.slf4j.Logger logger
                                                                  (logger/process
                                                                    endmsg__8984__auto__))
                                                                nil)
                                                              nil)
                                                            (if
                                                              (contains?
                                                                result__8982__auto__
                                                                :returned)
                                                              (:returned result__8982__auto__)
                                                              (do
                                                                (throw
                                                                  (:threw result__8982__auto__))
                                                                nil))))))
                                   ch__10268__auto__ (df/get-channel f__10267__auto__)]
                               (df/add-bounding-warning
                                 ch__10268__auto__
                                 {:line 63, :column 34, :file "datomic/cassandra_values_v4.clj"}
                                 (deref df/bounding-warn-seconds))
                               f__10267__auto__)))))
                     rets
                     (range 1 (java.lang.Integer/valueOf (int n)))))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values-v4")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:cassandra-values/put rets,
                 :size (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer v)))}))
            nil)
          nil)
        :created)))
  (defn get-value
    ([session table id]
      (let [temp__5457__auto__ (let [m_10313 {:event :cassandra-values/get-value, :id id}
                                     ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values-v4")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_10313 :phase :begin)))
                                                         nil)
                                                       nil)
                                     start__8981__auto__ (java.lang.System/nanoTime)
                                     result__8982__auto__ (try
                                                            {:returned
                                                             (cass/cql-select
                                                               session
                                                               table
                                                               id
                                                               cql-keys
                                                               false)}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8983__auto__
                                                              {:threw t__8983__auto__}))
                                     elapsed_10314 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8981__auto__)
                                     msec_10315 (logger/format-as-msec (long elapsed_10314))]
                                 (let [endmsg__8984__auto__ (merge
                                                              (assoc
                                                                m_10313
                                                                :msec
                                                                msec_10315
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8982__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8982__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.cassandra-values-v4")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8984__auto__))
                                     nil)
                                   nil)
                                 (if (contains? result__8982__auto__ :returned)
                                   (:returned result__8982__auto__)
                                   (do (throw (:threw result__8982__auto__)) nil)))]
        (when temp__5457__auto__
          (let [map__10318 temp__5457__auto__
                map__10318 (if (seq? map__10318)
                             (clojure.lang.PersistentHashMap/create (seq map__10318))
                             map__10318)
                ret map__10318
                chunks (get map__10318 :chunks)
                val (get map__10318 :val)
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val :chunks) m)]
            (if (= chunks 1)
              (and val (assoc ret :v val))
              (let [results (into
                              [val]
                              (pmap
                                (fn fn__10319
                                  ([p1__10312#]
                                    (:val
                                      (let [m_10320 {:event :cassandra-values/get-value-chunk,
                                                     :id id,
                                                     :n p1__10312#}
                                            ___8980__auto__ (let 
                                                              [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values-v4")]
                                                              (when
                                                                (.isDebugEnabled
                                                                  ^org.slf4j.Logger logger)
                                                                (.debug
                                                                  ^org.slf4j.Logger logger
                                                                  (logger/process
                                                                    (assoc m_10320 :phase :begin)))
                                                                nil)
                                                              nil)
                                            start__8981__auto__ (java.lang.System/nanoTime)
                                            result__8982__auto__ (try
                                                                   {:returned
                                                                    (*retry*
                                                                      (fn 
                                                                        fn__10324
                                                                        ([]
                                                                          (cass/cql-select
                                                                            session
                                                                            table
                                                                            (chunk-key
                                                                              id
                                                                              p1__10312#)
                                                                            cql-keys
                                                                            false))))}
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     t__8983__auto__
                                                                     {:threw t__8983__auto__}))
                                            elapsed_10321 (-
                                                            (java.lang.System/nanoTime)
                                                            start__8981__auto__)
                                            msec_10322 (logger/format-as-msec
                                                         (long elapsed_10321))]
                                        (let [endmsg__8984__auto__ (merge
                                                                     (assoc
                                                                       m_10320
                                                                       :msec
                                                                       msec_10322
                                                                       :phase
                                                                       :end)
                                                                     (when
                                                                       (:threw
                                                                         result__8982__auto__)
                                                                       {:threw
                                                                        (class
                                                                          (:threw
                                                                            result__8982__auto__))}))
                                              logger (org.slf4j.LoggerFactory/getLogger
                                                       "datomic.cassandra-values-v4")]
                                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                            (.debug
                                              ^org.slf4j.Logger logger
                                              (logger/process endmsg__8984__auto__))
                                            nil)
                                          nil)
                                        (if (contains? result__8982__auto__ :returned)
                                          (:returned result__8982__auto__)
                                          (do (throw (:threw result__8982__auto__)) nil))))))
                                (range 1 chunks)))]
                (when (every? identity results) (assoc ret :v (io/unchunk results))))))))))
  (defn delete-value
    ([session table id]
      (let [m_10341 {:event :cassandra-values/delete-value, :id id}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.cassandra-values-v4")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_10341 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (let [temp__5457__auto__ (let 
                                                               [m_10345
                                                                {:event
                                                                 :cassandra-values/get-value,
                                                                 :id id}
                                                                ___8980__auto__
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
                                                                          m_10345
                                                                          :phase
                                                                          :begin)))
                                                                    nil)
                                                                  nil)
                                                                start__8981__auto__
                                                                (java.lang.System/nanoTime)
                                                                result__8982__auto__
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
                                                                    t__8983__auto__
                                                                    {:threw t__8983__auto__}))
                                                                elapsed_10346
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8981__auto__)
                                                                msec_10347
                                                                (logger/format-as-msec
                                                                  (long elapsed_10346))]
                                                               (let 
                                                                 [endmsg__8984__auto__
                                                                  (merge
                                                                    (assoc
                                                                      m_10345
                                                                      :msec
                                                                      msec_10347
                                                                      :phase
                                                                      :end)
                                                                    (when
                                                                      (:threw result__8982__auto__)
                                                                      {:threw
                                                                       (class
                                                                         (:threw
                                                                           result__8982__auto__))}))
                                                                  logger
                                                                  (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values-v4")]
                                                                 (when
                                                                   (.isDebugEnabled
                                                                     ^org.slf4j.Logger logger)
                                                                   (.debug
                                                                     ^org.slf4j.Logger logger
                                                                     (logger/process
                                                                       endmsg__8984__auto__))
                                                                   nil)
                                                                 nil)
                                                               (if
                                                                 (contains?
                                                                   result__8982__auto__
                                                                   :returned)
                                                                 (:returned result__8982__auto__)
                                                                 (do
                                                                   (throw
                                                                     (:threw result__8982__auto__))
                                                                   1)))]
                                      (when temp__5457__auto__
                                        (let [map__10350 temp__5457__auto__
                                              map__10350 (if (seq? map__10350)
                                                           (clojure.lang.PersistentHashMap/create
                                                             (seq map__10350))
                                                           map__10350)
                                              chunks (get map__10350 :chunks)]
                                          (dorun
                                            (map
                                              (fn fn__10351
                                                ([p1__10340#]
                                                  (let [m_10352 {:event
                                                                 :cassandra-values/delete-value-chunk,
                                                                 :id id,
                                                                 :n p1__10340#}
                                                        ___8980__auto__ (let 
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
                                                                                  m_10352
                                                                                  :phase
                                                                                  :begin)))
                                                                            nil)
                                                                          nil)
                                                        start__8981__auto__ (java.lang.System/nanoTime)
                                                        result__8982__auto__ (try
                                                                               {:returned
                                                                                (cass/cql-delete
                                                                                  session
                                                                                  table
                                                                                  (chunk-key
                                                                                    id
                                                                                    p1__10340#)
                                                                                  cql-keys)}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8983__auto__
                                                                                 {:threw
                                                                                  t__8983__auto__}))
                                                        elapsed_10353 (-
                                                                        (java.lang.System/nanoTime)
                                                                        start__8981__auto__)
                                                        msec_10354 (logger/format-as-msec
                                                                     (long elapsed_10353))]
                                                    (let [endmsg__8984__auto__ (merge
                                                                                 (assoc
                                                                                   m_10352
                                                                                   :msec
                                                                                   msec_10354
                                                                                   :phase
                                                                                   :end)
                                                                                 (when
                                                                                   (:threw
                                                                                     result__8982__auto__)
                                                                                   {:threw
                                                                                    (class
                                                                                      (:threw
                                                                                        result__8982__auto__))}))
                                                          logger (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.cassandra-values-v4")]
                                                      (when (.isDebugEnabled
                                                              ^org.slf4j.Logger logger)
                                                        (.debug
                                                          ^org.slf4j.Logger logger
                                                          (logger/process endmsg__8984__auto__))
                                                        nil)
                                                      nil)
                                                    (if (contains? result__8982__auto__ :returned)
                                                      (:returned result__8982__auto__)
                                                      (do
                                                        (throw (:threw result__8982__auto__))
                                                        nil)))))
                                              (range 1 chunks)))
                                          (cass/cql-delete session table id cql-keys))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_10342 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_10343 (logger/format-as-msec (long elapsed_10342))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_10341 :msec msec_10343 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values-v4")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil))))))