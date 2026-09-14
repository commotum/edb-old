(do
  (clojure.core/in-ns 'datomic.ddb-values)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'chunk))
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.io :as 'io]
        ['datomic.ddb :as 'ddb]
        ['datomic.future :as 'df]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.ddb-values 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.ddb-values))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'chunk))
        (clojure.core/require
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.io :as 'io]
          ['datomic.ddb :as 'ddb]
          ['datomic.future :as 'df]
          ['datomic.slf4j :as 'logger]))))
  (set! *warn-on-reflection* true)
  (def ^{:dynamic true} *retry*)
  (reset-meta! #'*retry* (assoc {:dynamic true, :column 1} :name '*retry* :ns *ns*))
  (defn chunk
    ([s chunk_size]
      (loop [chunks [] s s]
        (if (<= (count s) chunk_size)
          (conj chunks s)
          (recur (conj chunks (subs s 0 chunk_size)) (subs s chunk_size))))))
  (defn unchunk ([chunks] (apply str chunks)))
  (defn split-map-keys-with
    ([m pred]
      (reduce
        (fn fn__20373
          ([p__20371 p__20372]
            (let [vec__20374 p__20371
                  match (nth vec__20374 (int 0) nil)
                  nomatch (nth vec__20374 (int 1) nil)
                  vec__20377 p__20372
                  k (nth vec__20377 (int 0) nil)
                  v (nth vec__20377 (int 1) nil)]
              (if (^clojure.lang.IFn pred k)
                [(assoc match k v) nomatch]
                [match (assoc nomatch k v)]))))
        [{} {}]
        m)))
  (defn chunk-key ([k n] (str k "__" n)))
  (defonce chunk-pool
   (delay
     (common/thread-pool
       {:nthreads (config/property "datomic.writeConcurrency"), :name "ddb-chunk"})))
  (defn put-value
    ([ddb_client table value]
      (let [map__20388 value
            map__20388 (if (seq? map__20388)
                         (clojure.lang.PersistentHashMap/create (seq map__20388))
                         map__20388)
            id (get map__20388 :id)
            v (get map__20388 :v)
            base (dissoc value :id :v)
            body (io/bbuf->base128 v)
            chunks (chunk body (long (* 62 1024)))
            n (count chunks)
            rets [(atom
                    (let [m_20389 {:event :ddb-values/put-value,
                                   :id id,
                                   :bufsize
                                   (java.lang.Integer/valueOf (int (count (nth chunks (int 0)))))}
                          ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.ddb-values")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_20389 :phase :begin)))
                                              nil)
                                            nil)
                          start__8981__auto__ (java.lang.System/nanoTime)
                          result__8982__auto__ (try
                                                 {:returned
                                                  (ddb/put-item
                                                    ddb_client
                                                    {:tableName table,
                                                     :item
                                                     (ddb/create-item
                                                       (assoc
                                                         base
                                                         :id
                                                         id
                                                         :v
                                                         (nth chunks (int 0))
                                                         :__n
                                                         (java.lang.Integer/valueOf (int n))))})}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8983__auto__
                                                   {:threw t__8983__auto__}))
                          elapsed_20390 (- (java.lang.System/nanoTime) start__8981__auto__)
                          msec_20391 (logger/format-as-msec (long elapsed_20390))]
                      (let [endmsg__8984__auto__ (merge
                                                   (assoc m_20389 :msec msec_20391 :phase :end)
                                                   (when (:threw result__8982__auto__)
                                                     {:threw
                                                      (class (:threw result__8982__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                          nil)
                        nil)
                      (if (contains? result__8982__auto__ :returned)
                        (:returned result__8982__auto__)
                        (do (throw (:threw result__8982__auto__)) atom))))]]
        (let [rets (mapv
                     deref
                     (reduce
                       (fn fn__20394
                         ([rets n]
                           (conj
                             rets
                             (let [f__10267__auto__ (df/-future-with-channel-impl
                                                      (deref chunk-pool)
                                                      (fn fn__20395
                                                        ([]
                                                          (let [m_20396
                                                                {:event
                                                                 :ddb-values/put-value-chunk,
                                                                 :id id,
                                                                 :n n,
                                                                 :bufsize
                                                                 (java.lang.Integer/valueOf
                                                                   (int
                                                                     (count
                                                                       (nth
                                                                         chunks
                                                                         (int
                                                                           ^java.lang.Number n)))))}
                                                                ___8980__auto__
                                                                (let 
                                                                  [logger
                                                                   (org.slf4j.LoggerFactory/getLogger
                                                                     "datomic.ddb-values")]
                                                                  (when
                                                                    (.isDebugEnabled
                                                                      ^org.slf4j.Logger logger)
                                                                    (.debug
                                                                      ^org.slf4j.Logger logger
                                                                      (logger/process
                                                                        (assoc
                                                                          m_20396
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
                                                                       fn__20400
                                                                       ([]
                                                                         (ddb/put-item
                                                                           ddb_client
                                                                           {:tableName table,
                                                                            :item
                                                                            (ddb/create-item
                                                                              {:id
                                                                               (chunk-key id n),
                                                                               :v
                                                                               (nth
                                                                                 chunks
                                                                                 (int
                                                                                   ^java.lang.Number n))})}))))}
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8983__auto__
                                                                    {:threw t__8983__auto__}))
                                                                elapsed_20397
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8981__auto__)
                                                                msec_20398
                                                                (logger/format-as-msec
                                                                  (long elapsed_20397))]
                                                            (datomic.monitor/add-stat
                                                              :DdbPutChunkMsec
                                                              msec_20398)
                                                            (let 
                                                              [endmsg__8984__auto__
                                                               (merge
                                                                 (assoc
                                                                   m_20396
                                                                   :msec
                                                                   msec_20398
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
                                                                 "datomic.ddb-values")]
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
                                 {:line 80, :column 31, :file "datomic/ddb_values.clj"}
                                 (deref df/bounding-warn-seconds))
                               f__10267__auto__))))
                       rets
                       (range 1 (java.lang.Integer/valueOf (int n)))))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:ddb-values/put (mapv :sdkResponseMetadata rets),
                 :id id,
                 :bbuf-size (long (io/remaining v)),
                 :size (java.lang.Integer/valueOf (int (count body))),
                 :chunks (java.lang.Integer/valueOf (int n))}))
            nil)
          nil)
        :created)))
  (defn get-deitem
    ([ddb_client table id]
      (let [lmap {:tableName table, :key (ddb/create-key id)}
            temp__5457__auto__ (or
                                 (:item (ddb/get-item ddb_client lmap))
                                 (do
                                   (let [logger (org.slf4j.LoggerFactory/getLogger
                                                  "datomic.ddb-values")]
                                     (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                       (.info
                                         ^org.slf4j.Logger logger
                                         (logger/process
                                           #:ddb-values{:get-deitem-consistent-retry id}))
                                       nil)
                                     nil)
                                   (:item
                                     (ddb/get-item
                                       ddb_client
                                       (assoc lmap :consistentRead true)))))]
        (when temp__5457__auto__ (let [result temp__5457__auto__] (ddb/deitem result))))))
  (defn get-value
    ([ddb_client table id]
      (let [temp__5457__auto__ (let [m_20421 {:event :ddb-values/get-value, :id id}
                                     ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.ddb-values")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_20421 :phase :begin)))
                                                         nil)
                                                       nil)
                                     start__8981__auto__ (java.lang.System/nanoTime)
                                     result__8982__auto__ (try
                                                            {:returned
                                                             (get-deitem ddb_client table id)}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8983__auto__
                                                              {:threw t__8983__auto__}))
                                     elapsed_20422 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8981__auto__)
                                     msec_20423 (logger/format-as-msec (long elapsed_20422))]
                                 (let [endmsg__8984__auto__ (merge
                                                              (assoc
                                                                m_20421
                                                                :msec
                                                                msec_20423
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8982__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8982__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.ddb-values")]
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
          (let [result temp__5457__auto__
                vec__20426 (split-map-keys-with
                             result
                             (fn fn__20429 ([p1__20419#] (.startsWith (name p1__20419#) "__"))))
                impl (nth vec__20426 (int 0) nil)
                user (nth vec__20426 (int 1) nil)
                n (:__n impl)
                v (:v user)]
            (if (= n 1)
              (and v (assoc user :v (io/base128->bbuf v)))
              (let [chunks (into
                             [v]
                             (pmap
                               (fn fn__20431
                                 ([p1__20420#]
                                   (:v
                                     (let [m_20432 {:event :ddb-values/get-value-chunk,
                                                    :id id,
                                                    :n p1__20420#}
                                           ___8980__auto__ (let 
                                                             [logger
                                                              (org.slf4j.LoggerFactory/getLogger
                                                                "datomic.ddb-values")]
                                                             (when
                                                               (.isDebugEnabled
                                                                 ^org.slf4j.Logger logger)
                                                               (.debug
                                                                 ^org.slf4j.Logger logger
                                                                 (logger/process
                                                                   (assoc m_20432 :phase :begin)))
                                                               nil)
                                                             nil)
                                           start__8981__auto__ (java.lang.System/nanoTime)
                                           result__8982__auto__ (try
                                                                  {:returned
                                                                   (*retry*
                                                                     (fn 
                                                                       fn__20436
                                                                       ([]
                                                                         (get-deitem
                                                                           ddb_client
                                                                           table
                                                                           (chunk-key
                                                                             id
                                                                             p1__20420#)))))}
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8983__auto__
                                                                    {:threw t__8983__auto__}))
                                           elapsed_20433 (-
                                                           (java.lang.System/nanoTime)
                                                           start__8981__auto__)
                                           msec_20434 (logger/format-as-msec (long elapsed_20433))]
                                       (let [endmsg__8984__auto__ (merge
                                                                    (assoc
                                                                      m_20432
                                                                      :msec
                                                                      msec_20434
                                                                      :phase
                                                                      :end)
                                                                    (when
                                                                      (:threw result__8982__auto__)
                                                                      {:threw
                                                                       (class
                                                                         (:threw
                                                                           result__8982__auto__))}))
                                             logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.ddb-values")]
                                         (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                           (.debug
                                             ^org.slf4j.Logger logger
                                             (logger/process endmsg__8984__auto__))
                                           nil)
                                         nil)
                                       (if (contains? result__8982__auto__ :returned)
                                         (:returned result__8982__auto__)
                                         (do (throw (:threw result__8982__auto__)) nil))))))
                               (range 1 n)))]
                (when (every? identity chunks)
                  (assoc user :v (io/base128->bbuf (apply str chunks)))))))))))
  (defn delete-value
    ([ddb_client table id]
      (let [m_20452 {:event :ddb-values/delete-value, :id id}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_20452 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (let [temp__5457__auto__ (:n
                                                               (get
                                                                 (:item
                                                                   (ddb/get-item
                                                                     ddb_client
                                                                     {:tableName table,
                                                                      :key (ddb/create-key id),
                                                                      :attributesToGet ["__n"]}))
                                                                 "__n"))]
                                      (when temp__5457__auto__
                                        (let [n temp__5457__auto__]
                                          (dorun
                                            (map
                                              (fn fn__20456
                                                ([p1__20451#]
                                                  (let [m_20457 {:event
                                                                 :ddb-values/delete-value-chunk,
                                                                 :id id,
                                                                 :n p1__20451#}
                                                        ___8980__auto__ (let 
                                                                          [logger
                                                                           (org.slf4j.LoggerFactory/getLogger
                                                                             "datomic.ddb-values")]
                                                                          (when
                                                                            (.isDebugEnabled
                                                                              ^org.slf4j.Logger logger)
                                                                            (.debug
                                                                              ^org.slf4j.Logger logger
                                                                              (logger/process
                                                                                (assoc
                                                                                  m_20457
                                                                                  :phase
                                                                                  :begin)))
                                                                            nil)
                                                                          nil)
                                                        start__8981__auto__ (java.lang.System/nanoTime)
                                                        result__8982__auto__ (try
                                                                               {:returned
                                                                                (ddb/delete-item
                                                                                  ddb_client
                                                                                  {:tableName
                                                                                   table,
                                                                                   :key
                                                                                   (ddb/create-key
                                                                                     (chunk-key
                                                                                       id
                                                                                       p1__20451#)),
                                                                                   :returnValues
                                                                                   "NONE"})}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8983__auto__
                                                                                 {:threw
                                                                                  t__8983__auto__}))
                                                        elapsed_20458 (-
                                                                        (java.lang.System/nanoTime)
                                                                        start__8981__auto__)
                                                        msec_20459 (logger/format-as-msec
                                                                     (long elapsed_20458))]
                                                    (let [endmsg__8984__auto__ (merge
                                                                                 (assoc
                                                                                   m_20457
                                                                                   :msec
                                                                                   msec_20459
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
                                                                   "datomic.ddb-values")]
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
                                              (range
                                                1
                                                (long
                                                  (java.lang.Long/parseLong
                                                    ^java.lang.String n)))))
                                          (ddb/delete-item
                                            ddb_client
                                            {:tableName table,
                                             :key (ddb/create-key id),
                                             :returnValues "NONE"}))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_20453 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_20454 (logger/format-as-msec (long elapsed_20453))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_20452 :msec msec_20454 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil))))))