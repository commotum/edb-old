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
  (def CHUNK_SIZE (long (* 350 1024)))
  (reset-meta! #'CHUNK_SIZE (assoc {:const true, :column 1} :name 'CHUNK_SIZE :ns *ns*))
  (def ^{:dynamic true} *retry*)
  (reset-meta! #'*retry* (assoc {:dynamic true, :column 1} :name '*retry* :ns *ns*))
  (defn chunk-key ([k n] (str k "__" n)))
  (defonce chunk-pool
   (delay
     (common/thread-pool
       {:nthreads (config/property "datomic.writeConcurrency"), :name "cass2-chunk"})))
  (def cql-keys [:id2 :rev :map :val :chunks])
  (defn put-value
    ([session table p__20525]
      (let [map__20526 p__20525
            map__20526 (if (seq? map__20526)
                         (clojure.lang.PersistentHashMap/create (seq map__20526))
                         map__20526)
            v_map map__20526
            id (get map__20526 :id)
            rev (get map__20526 :rev)
            v (get map__20526 :v)
            m (dissoc v_map :id :rev :v)
            chunks (io/chunk v 358400)
            n (count chunks)
            val_map {:chunks (java.lang.Integer/valueOf (int n)),
                     :id2 id,
                     :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                     :rev rev,
                     :val (nth chunks (int 0))}
            rets [(atom
                    (let [m_20527 {:event :cassandra-values/put-value,
                                   :id id,
                                   :bufsize
                                   (java.lang.Integer/valueOf
                                     (int (.remaining (nth chunks (int 0)))))}
                          ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.cassandra-values")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_20527 :phase :begin)))
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
                          elapsed_20528 (- (java.lang.System/nanoTime) start__8981__auto__)
                          msec_20529 (logger/format-as-msec (long elapsed_20528))]
                      (let [endmsg__8984__auto__ (merge
                                                   (assoc m_20527 :msec msec_20529 :phase :end)
                                                   (when (:threw result__8982__auto__)
                                                     {:threw
                                                      (class (:threw result__8982__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
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
                     (fn fn__20532
                       ([rets n]
                         (let [val_map {:id2 (chunk-key id n),
                                        :val (nth chunks (int ^java.lang.Number n))}]
                           (conj
                             rets
                             (let [f__10267__auto__ (df/-future-with-channel-impl
                                                      (deref chunk-pool)
                                                      (fn fn__20533
                                                        ([]
                                                          (let [m_20534
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
                                                                     "datomic.cassandra-values")]
                                                                  (when
                                                                    (.isDebugEnabled
                                                                      ^org.slf4j.Logger logger)
                                                                    (.debug
                                                                      ^org.slf4j.Logger logger
                                                                      (logger/process
                                                                        (assoc
                                                                          m_20534
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
                                                                       fn__20538
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
                                                                elapsed_20535
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8981__auto__)
                                                                msec_20536
                                                                (logger/format-as-msec
                                                                  (long elapsed_20535))]
                                                            (let 
                                                              [endmsg__8984__auto__
                                                               (merge
                                                                 (assoc
                                                                   m_20534
                                                                   :msec
                                                                   msec_20536
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
                                                                 "datomic.cassandra-values")]
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
                                 {:line 63, :column 34, :file "datomic/cassandra_values.clj"}
                                 (deref df/bounding-warn-seconds))
                               f__10267__auto__)))))
                     rets
                     (range 1 (java.lang.Integer/valueOf (int n)))))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
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
      (let [temp__5457__auto__ (let [m_20555 {:event :cassandra-values/get-value, :id id}
                                     ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.cassandra-values")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_20555 :phase :begin)))
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
                                     elapsed_20556 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8981__auto__)
                                     msec_20557 (logger/format-as-msec (long elapsed_20556))]
                                 (let [endmsg__8984__auto__ (merge
                                                              (assoc
                                                                m_20555
                                                                :msec
                                                                msec_20557
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8982__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8982__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.cassandra-values")]
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
          (let [map__20560 temp__5457__auto__
                map__20560 (if (seq? map__20560)
                             (clojure.lang.PersistentHashMap/create (seq map__20560))
                             map__20560)
                ret map__20560
                chunks (get map__20560 :chunks)
                val (get map__20560 :val)
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val :chunks) m)]
            (if (= chunks 1)
              (and val (assoc ret :v val))
              (let [results (into
                              [val]
                              (pmap
                                (fn fn__20561
                                  ([p1__20554#]
                                    (:val
                                      (let [m_20562 {:event :cassandra-values/get-value-chunk,
                                                     :id id,
                                                     :n p1__20554#}
                                            ___8980__auto__ (let 
                                                              [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.cassandra-values")]
                                                              (when
                                                                (.isDebugEnabled
                                                                  ^org.slf4j.Logger logger)
                                                                (.debug
                                                                  ^org.slf4j.Logger logger
                                                                  (logger/process
                                                                    (assoc m_20562 :phase :begin)))
                                                                nil)
                                                              nil)
                                            start__8981__auto__ (java.lang.System/nanoTime)
                                            result__8982__auto__ (try
                                                                   {:returned
                                                                    (*retry*
                                                                      (fn 
                                                                        fn__20566
                                                                        ([]
                                                                          (cass/cql-select
                                                                            session
                                                                            table
                                                                            (chunk-key
                                                                              id
                                                                              p1__20554#)
                                                                            cql-keys
                                                                            false))))}
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     t__8983__auto__
                                                                     {:threw t__8983__auto__}))
                                            elapsed_20563 (-
                                                            (java.lang.System/nanoTime)
                                                            start__8981__auto__)
                                            msec_20564 (logger/format-as-msec
                                                         (long elapsed_20563))]
                                        (let [endmsg__8984__auto__ (merge
                                                                     (assoc
                                                                       m_20562
                                                                       :msec
                                                                       msec_20564
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
                                                       "datomic.cassandra-values")]
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
      (let [m_20583 {:event :cassandra-values/delete-value, :id id}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.cassandra-values")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_20583 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (let [temp__5457__auto__ (let 
                                                               [m_20587
                                                                {:event
                                                                 :cassandra-values/get-value,
                                                                 :id id}
                                                                ___8980__auto__
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
                                                                          m_20587
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
                                                                elapsed_20588
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8981__auto__)
                                                                msec_20589
                                                                (logger/format-as-msec
                                                                  (long elapsed_20588))]
                                                               (let 
                                                                 [endmsg__8984__auto__
                                                                  (merge
                                                                    (assoc
                                                                      m_20587
                                                                      :msec
                                                                      msec_20589
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
                                                                    "datomic.cassandra-values")]
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
                                        (let [map__20592 temp__5457__auto__
                                              map__20592 (if (seq? map__20592)
                                                           (clojure.lang.PersistentHashMap/create
                                                             (seq map__20592))
                                                           map__20592)
                                              chunks (get map__20592 :chunks)]
                                          (dorun
                                            (map
                                              (fn fn__20593
                                                ([p1__20582#]
                                                  (let [m_20594 {:event
                                                                 :cassandra-values/delete-value-chunk,
                                                                 :id id,
                                                                 :n p1__20582#}
                                                        ___8980__auto__ (let 
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
                                                                                  m_20594
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
                                                                                    p1__20582#)
                                                                                  cql-keys)}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8983__auto__
                                                                                 {:threw
                                                                                  t__8983__auto__}))
                                                        elapsed_20595 (-
                                                                        (java.lang.System/nanoTime)
                                                                        start__8981__auto__)
                                                        msec_20596 (logger/format-as-msec
                                                                     (long elapsed_20595))]
                                                    (let [endmsg__8984__auto__ (merge
                                                                                 (assoc
                                                                                   m_20594
                                                                                   :msec
                                                                                   msec_20596
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
                                                                   "datomic.cassandra-values")]
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
            elapsed_20584 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_20585 (logger/format-as-msec (long elapsed_20584))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_20583 :msec msec_20585 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-values")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil))))))