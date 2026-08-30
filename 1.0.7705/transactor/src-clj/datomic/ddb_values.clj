(do
  (clojure.core/in-ns 'datomic.ddb-values)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'chunk))
      (clojure.core/require
        ['datomic.core2.aws.helpers :as 'aws]
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
          ['datomic.core2.aws.helpers :as 'aws]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.io :as 'io]
          ['datomic.ddb :as 'ddb]
          ['datomic.future :as 'df]
          ['datomic.slf4j :as 'logger]))))
  (set! *warn-on-reflection* true)
  (.setDynamic (clojure.lang.RT/var "datomic.ddb-values" "*retry*") true)
  (.setMeta
    (.setDynamic (clojure.lang.RT/var "datomic.ddb-values" "*retry*") true)
    {:dynamic true, :column (int 1)})
  (def chunk
   (fn chunk
     ([s chunk_size]
       (loop [chunks [] s s]
         (if (<= (count s) chunk_size)
           (conj chunks s)
           (recur (conj chunks (subs s 0 chunk_size)) (subs s chunk_size)))))))
  (reset-meta!
    #'chunk
    (assoc
      {:arglists (clojure.core/list ['s 'chunk-size]), :column (int 1)}
      :name
      'chunk
      :ns
      *ns*))
  (defn unchunk ([chunks] (apply str chunks)))
  (reset-meta!
    #'unchunk
    (assoc {:arglists (clojure.core/list ['chunks]), :column (int 1)} :name 'unchunk :ns *ns*))
  (defn split-map-keys-with
    ([m pred]
      (reduce
        (fn fn__20454
          ([p__20452 p__20453]
            (let [vec__20455 p__20452
                  match (nth vec__20455 (int 0) nil)
                  nomatch (nth vec__20455 (int 1) nil)
                  vec__20458 p__20453
                  k (nth vec__20458 (int 0) nil)
                  v (nth vec__20458 (int 1) nil)]
              (if (^clojure.lang.IFn pred k)
                [(assoc match k v) nomatch]
                [match (assoc nomatch k v)]))))
        [{} {}]
        m)))
  (reset-meta!
    #'split-map-keys-with
    (assoc
      {:arglists (clojure.core/list ['m 'pred]), :column (int 1)}
      :name
      'split-map-keys-with
      :ns
      *ns*))
  (defn chunk-key ([k n] (str k "__" n)))
  (reset-meta!
    #'chunk-key
    (assoc {:arglists (clojure.core/list ['k 'n]), :column (int 1)} :name 'chunk-key :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.ddb-values" "chunk-pool") {:column (int 1)})
  (let [v__6837__auto__ #'chunk-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.ddb-values" "chunk-pool") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.ddb-values" "chunk-pool")
        (delay
          (common/thread-pool
            {:nthreads (config/property "datomic.writeConcurrency"), :name "ddb-chunk"})))
      #'chunk-pool))
  (def put-value
   (fn put_value
     ([ddb_client table value]
       (let [map__20470 value
             map__20470 (if (seq? map__20470)
                          (if (next map__20470)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__20470))
                            (if (seq map__20470) (first map__20470) {}))
                          map__20470)
             id (get map__20470 :id)
             v (get map__20470 :v)
             base (dissoc value :id :v)
             body (io/bbuf->base128 v)
             chunks (chunk body (long (* 62 1024)))
             n (count chunks)
             rets [(atom
                     (let [m_20471 {:event :ddb-values/put-value,
                                    :id id,
                                    :bufsize
                                    (java.lang.Integer/valueOf (int (count (nth chunks (int 0)))))}
                           ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                          "datomic.ddb-values")]
                                             (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                               (.debug
                                                 ^org.slf4j.Logger logger
                                                 (logger/process (assoc m_20471 :phase :begin))))
                                             nil)
                           start__8599__auto__ (java.lang.System/nanoTime)
                           result__8600__auto__ (try
                                                  {:returned
                                                   (aws/invoke
                                                     ddb_client
                                                     {:op :PutItem,
                                                      :req
                                                      {:TableName table,
                                                       :Item
                                                       (ddb/create-item
                                                         (assoc
                                                           base
                                                           :id
                                                           id
                                                           :v
                                                           (nth chunks (int 0))
                                                           :__n
                                                           (java.lang.Integer/valueOf
                                                             (int n))))}})}
                                                  (catch
                                                    java.lang.Throwable
                                                    t__8601__auto__
                                                    {:threw t__8601__auto__}))
                           elapsed_20472 (- (java.lang.System/nanoTime) start__8599__auto__)
                           msec_20473 (logger/format-as-msec (long elapsed_20472))]
                       (let [endmsg__8602__auto__ (merge
                                                    (assoc m_20471 :msec msec_20473 :phase :end)
                                                    (when (:threw result__8600__auto__)
                                                      {:threw
                                                       (class (:threw result__8600__auto__))}))
                             logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
                         (when (.isDebugEnabled ^org.slf4j.Logger logger)
                           (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                         nil)
                       (if (contains? result__8600__auto__ :returned)
                         (:returned result__8600__auto__)
                         (do (throw (:threw result__8600__auto__)) atom))))]]
         (let [rets (mapv
                      deref
                      (reduce
                        (fn fn__20476
                          ([rets n]
                            (conj
                              rets
                              (df/-future-with-channel-impl
                                (deref chunk-pool)
                                (fn fn__20477
                                  ([]
                                    (let [m_20478 {:event :ddb-values/put-value-chunk,
                                                   :id id,
                                                   :n n,
                                                   :bufsize
                                                   (java.lang.Integer/valueOf
                                                     (int
                                                       (count
                                                         (nth chunks (int ^java.lang.Number n)))))}
                                          ___8598__auto__ (let [logger
                                                                (org.slf4j.LoggerFactory/getLogger
                                                                  "datomic.ddb-values")]
                                                            (when
                                                              (.isDebugEnabled
                                                                ^org.slf4j.Logger logger)
                                                              (.debug
                                                                ^org.slf4j.Logger logger
                                                                (logger/process
                                                                  (assoc m_20478 :phase :begin))))
                                                            nil)
                                          start__8599__auto__ (java.lang.System/nanoTime)
                                          result__8600__auto__ (try
                                                                 {:returned
                                                                  (*retry*
                                                                    (fn 
                                                                      fn__20482
                                                                      ([]
                                                                        (aws/invoke
                                                                          ddb_client
                                                                          {:op :PutItem,
                                                                           :req
                                                                           {:TableName table,
                                                                            :Item
                                                                            (ddb/create-item
                                                                              {:id
                                                                               (chunk-key id n),
                                                                               :v
                                                                               (nth
                                                                                 chunks
                                                                                 (int
                                                                                   ^java.lang.Number n))})}}))))}
                                                                 (catch
                                                                   java.lang.Throwable
                                                                   t__8601__auto__
                                                                   {:threw t__8601__auto__}))
                                          elapsed_20479 (-
                                                          (java.lang.System/nanoTime)
                                                          start__8599__auto__)
                                          msec_20480 (logger/format-as-msec (long elapsed_20479))]
                                      (datomic.monitor/add-stat :DdbPutChunkMsec msec_20480)
                                      (let [endmsg__8602__auto__ (merge
                                                                   (assoc
                                                                     m_20478
                                                                     :msec
                                                                     msec_20480
                                                                     :phase
                                                                     :end)
                                                                   (when
                                                                     (:threw result__8600__auto__)
                                                                     {:threw
                                                                      (class
                                                                        (:threw
                                                                          result__8600__auto__))}))
                                            logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.ddb-values")]
                                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                          (.debug
                                            ^org.slf4j.Logger logger
                                            (logger/process endmsg__8602__auto__)))
                                        nil)
                                      (if (contains? result__8600__auto__ :returned)
                                        (:returned result__8600__auto__)
                                        (do (throw (:threw result__8600__auto__)) nil)))))))))
                        rets
                        (range 1 (java.lang.Integer/valueOf (int n)))))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
           (when (.isInfoEnabled ^org.slf4j.Logger logger)
             (.info
               ^org.slf4j.Logger logger
               (logger/process
                 {:ddb-values/put
                  (mapv
                    (fn fn__20491 ([p1__20469#] (select-keys p1__20469# [:aws/RequestId])))
                    rets),
                  :id id,
                  :bbuf-size (long (io/remaining v)),
                  :size (java.lang.Integer/valueOf (int (count body))),
                  :chunks (java.lang.Integer/valueOf (int n))})))
           nil)
         :created))))
  (reset-meta!
    #'put-value
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'table 'value]), :column (int 1)}
      :name
      'put-value
      :ns
      *ns*))
  (def get-deitem
   (fn get_deitem
     ([ddb_client table id]
       (let [lmap {:TableName table, :Key (ddb/create-key id)}
             temp__5825__auto__ (or
                                  (:Item (aws/invoke ddb_client {:op :GetItem, :req lmap}))
                                  (do
                                    (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.ddb-values")]
                                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                        (.info
                                          ^org.slf4j.Logger logger
                                          (logger/process
                                            #:ddb-values{:get-deitem-consistent-retry id})))
                                      nil)
                                    (:Item
                                      (aws/invoke
                                        ddb_client
                                        {:op :GetItem, :req (assoc lmap :ConsistentRead true)}))))]
         (when temp__5825__auto__ (let [result temp__5825__auto__] (ddb/deitem result)))))))
  (reset-meta!
    #'get-deitem
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'table 'id]), :column (int 1)}
      :name
      'get-deitem
      :ns
      *ns*))
  (def get-value
   (fn get_value
     ([ddb_client table id]
       (let [temp__5825__auto__ (let [m_20503 {:event :ddb-values/get-value, :id id}
                                      ___8598__auto__ (let [logger
                                                            (org.slf4j.LoggerFactory/getLogger
                                                              "datomic.ddb-values")]
                                                        (when (.isDebugEnabled
                                                                ^org.slf4j.Logger logger)
                                                          (.debug
                                                            ^org.slf4j.Logger logger
                                                            (logger/process
                                                              (assoc m_20503 :phase :begin))))
                                                        nil)
                                      start__8599__auto__ (java.lang.System/nanoTime)
                                      result__8600__auto__ (try
                                                             {:returned
                                                              (get-deitem ddb_client table id)}
                                                             (catch
                                                               java.lang.Throwable
                                                               t__8601__auto__
                                                               {:threw t__8601__auto__}))
                                      elapsed_20504 (-
                                                      (java.lang.System/nanoTime)
                                                      start__8599__auto__)
                                      msec_20505 (logger/format-as-msec (long elapsed_20504))]
                                  (let [endmsg__8602__auto__ (merge
                                                               (assoc
                                                                 m_20503
                                                                 :msec
                                                                 msec_20505
                                                                 :phase
                                                                 :end)
                                                               (when
                                                                 (:threw result__8600__auto__)
                                                                 {:threw
                                                                  (class
                                                                    (:threw
                                                                      result__8600__auto__))}))
                                        logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.ddb-values")]
                                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                      (.debug
                                        ^org.slf4j.Logger logger
                                        (logger/process endmsg__8602__auto__)))
                                    nil)
                                  (if (contains? result__8600__auto__ :returned)
                                    (:returned result__8600__auto__)
                                    (do (throw (:threw result__8600__auto__)) nil)))]
         (when temp__5825__auto__
           (let [result temp__5825__auto__
                 vec__20508 (split-map-keys-with
                              result
                              (fn fn__20511 ([p1__20501#] (.startsWith (name p1__20501#) "__"))))
                 impl (nth vec__20508 (int 0) nil)
                 user (nth vec__20508 (int 1) nil)
                 n (:__n impl)
                 v (:v user)]
             (if (= n 1)
               (and v (assoc user :v (io/base128->bbuf v)))
               (let [chunks (into
                              [v]
                              (pmap
                                (fn fn__20513
                                  ([p1__20502#]
                                    (:v
                                      (let [m_20514 {:event :ddb-values/get-value-chunk,
                                                     :id id,
                                                     :n p1__20502#}
                                            ___8598__auto__ (let 
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
                                                                      m_20514
                                                                      :phase
                                                                      :begin))))
                                                              nil)
                                            start__8599__auto__ (java.lang.System/nanoTime)
                                            result__8600__auto__ (try
                                                                   {:returned
                                                                    (*retry*
                                                                      (fn 
                                                                        fn__20518
                                                                        ([]
                                                                          (get-deitem
                                                                            ddb_client
                                                                            table
                                                                            (chunk-key
                                                                              id
                                                                              p1__20502#)))))}
                                                                   (catch
                                                                     java.lang.Throwable
                                                                     t__8601__auto__
                                                                     {:threw t__8601__auto__}))
                                            elapsed_20515 (-
                                                            (java.lang.System/nanoTime)
                                                            start__8599__auto__)
                                            msec_20516 (logger/format-as-msec
                                                         (long elapsed_20515))]
                                        (let [endmsg__8602__auto__ (merge
                                                                     (assoc
                                                                       m_20514
                                                                       :msec
                                                                       msec_20516
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
                                                       "datomic.ddb-values")]
                                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                            (.debug
                                              ^org.slf4j.Logger logger
                                              (logger/process endmsg__8602__auto__)))
                                          nil)
                                        (if (contains? result__8600__auto__ :returned)
                                          (:returned result__8600__auto__)
                                          (do (throw (:threw result__8600__auto__)) nil))))))
                                (range 1 n)))]
                 (when (every? identity chunks)
                   (assoc user :v (io/base128->bbuf (apply str chunks))))))))))))
  (reset-meta!
    #'get-value
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'table 'id]), :column (int 1)}
      :name
      'get-value
      :ns
      *ns*))
  (def delete-value
   (fn delete_value
     ([ddb_client table id]
       (let [m_20534 {:event :ddb-values/delete-value, :id id}
             ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
                               (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                 (.debug
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_20534 :phase :begin))))
                               nil)
             start__8599__auto__ (java.lang.System/nanoTime)
             result__8600__auto__ (try
                                    {:returned
                                     (let [temp__5825__auto__ (:N
                                                                (get
                                                                  (:Item
                                                                    (aws/invoke
                                                                      ddb_client
                                                                      {:op :GetItem,
                                                                       :req
                                                                       {:TableName table,
                                                                        :Key (ddb/create-key id),
                                                                        :ProjectionExpression "#n",
                                                                        :ExpressionAttributeNames
                                                                        {"#n" "__n"}}}))
                                                                  "__n"))]
                                       (when temp__5825__auto__
                                         (let [n temp__5825__auto__]
                                           (dorun
                                             (map
                                               (fn fn__20538
                                                 ([p1__20533#]
                                                   (let [m_20539 {:event
                                                                  :ddb-values/delete-value-chunk,
                                                                  :id id,
                                                                  :n p1__20533#}
                                                         ___8598__auto__ (let 
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
                                                                                   m_20539
                                                                                   :phase
                                                                                   :begin))))
                                                                           nil)
                                                         start__8599__auto__ (java.lang.System/nanoTime)
                                                         result__8600__auto__ (try
                                                                                {:returned
                                                                                 (aws/invoke
                                                                                   ddb_client
                                                                                   {:op
                                                                                    :DeleteItem,
                                                                                    :req
                                                                                    {:TableName
                                                                                     table,
                                                                                     :Key
                                                                                     (ddb/create-key
                                                                                       (chunk-key
                                                                                         id
                                                                                         p1__20533#)),
                                                                                     :ReturnValues
                                                                                     "NONE"}})}
                                                                                (catch
                                                                                  java.lang.Throwable
                                                                                  t__8601__auto__
                                                                                  {:threw
                                                                                   t__8601__auto__}))
                                                         elapsed_20540 (-
                                                                         (java.lang.System/nanoTime)
                                                                         start__8599__auto__)
                                                         msec_20541 (logger/format-as-msec
                                                                      (long elapsed_20540))]
                                                     (let [endmsg__8602__auto__ (merge
                                                                                  (assoc
                                                                                    m_20539
                                                                                    :msec
                                                                                    msec_20541
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
                                                                    "datomic.ddb-values")]
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
                                               (range
                                                 1
                                                 (long
                                                   (java.lang.Long/parseLong
                                                     ^java.lang.String n)))))
                                           (aws/invoke
                                             ddb_client
                                             {:op :DeleteItem,
                                              :req
                                              {:TableName table,
                                               :Key (ddb/create-key id),
                                               :ReturnValues "NONE"}}))))}
                                    (catch
                                      java.lang.Throwable
                                      t__8601__auto__
                                      {:threw t__8601__auto__}))
             elapsed_20535 (- (java.lang.System/nanoTime) start__8599__auto__)
             msec_20536 (logger/format-as-msec (long elapsed_20535))]
         (let [endmsg__8602__auto__ (merge
                                      (assoc m_20534 :msec msec_20536 :phase :end)
                                      (when (:threw result__8600__auto__)
                                        {:threw (class (:threw result__8600__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
           (when (.isDebugEnabled ^org.slf4j.Logger logger)
             (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
           nil)
         (if (contains? result__8600__auto__ :returned)
           (:returned result__8600__auto__)
           (do (throw (:threw result__8600__auto__)) nil))))))
  (reset-meta!
    #'delete-value
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'table 'id]), :column (int 1)}
      :name
      'delete-value
      :ns
      *ns*)))