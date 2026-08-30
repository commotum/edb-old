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
  (defn chunk
    ([s chunk_size]
      (loop [chunks [] s s]
        (if (<= (count s) chunk_size)
          (conj chunks s)
          (recur (conj chunks (subs s 0 chunk_size)) (subs s chunk_size))))))
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
        (fn fn__9805
          ([p__9803 p__9804]
            (let [vec__9806 p__9803
                  match (nth vec__9806 (int 0) nil)
                  nomatch (nth vec__9806 (int 1) nil)
                  vec__9809 p__9804
                  k (nth vec__9809 (int 0) nil)
                  v (nth vec__9809 (int 1) nil)]
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
  (let [v__6812__auto__ #'chunk-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.ddb-values" "chunk-pool") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.ddb-values" "chunk-pool")
        (delay
          (common/thread-pool
            {:nthreads (config/property "datomic.writeConcurrency"), :name "ddb-chunk"})))
      #'chunk-pool))
  (defn put-value
    ([ddb_client table value]
      (let [map__9821 value
            map__9821 (if (seq? map__9821)
                        (if (next map__9821)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9821))
                          (if (seq map__9821) (first map__9821) {}))
                        map__9821)
            id (get map__9821 :id)
            v (get map__9821 :v)
            base (dissoc value :id :v)
            body (io/bbuf->base128 v)
            chunks (chunk body (long (* 62 1024)))
            n (count chunks)
            rets [(atom
                    (let [m_9822 {:event :ddb-values/put-value,
                                  :id id,
                                  :bufsize
                                  (java.lang.Integer/valueOf (int (count (nth chunks (int 0)))))}
                          ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.ddb-values")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_9822 :phase :begin))))
                                            nil)
                          start__8553__auto__ (java.lang.System/nanoTime)
                          result__8554__auto__ (try
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
                                                          (java.lang.Integer/valueOf (int n))))}})}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8555__auto__
                                                   {:threw t__8555__auto__}))
                          elapsed_9823 (- (java.lang.System/nanoTime) start__8553__auto__)
                          msec_9824 (logger/format-as-msec (long elapsed_9823))]
                      (let [endmsg__8556__auto__ (merge
                                                   (assoc m_9822 :msec msec_9824 :phase :end)
                                                   (when (:threw result__8554__auto__)
                                                     {:threw
                                                      (class (:threw result__8554__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                        nil)
                      (if (contains? result__8554__auto__ :returned)
                        (:returned result__8554__auto__)
                        (do (throw (:threw result__8554__auto__)) atom))))]]
        (let [rets (mapv
                     deref
                     (reduce
                       (fn fn__9827
                         ([rets n]
                           (conj
                             rets
                             (df/-future-with-channel-impl
                               (deref chunk-pool)
                               (fn fn__9828
                                 ([]
                                   (let [m_9829 {:event :ddb-values/put-value-chunk,
                                                 :id id,
                                                 :n n,
                                                 :bufsize
                                                 (java.lang.Integer/valueOf
                                                   (int
                                                     (count
                                                       (nth chunks (int ^java.lang.Number n)))))}
                                         ___8552__auto__ (let [logger
                                                               (org.slf4j.LoggerFactory/getLogger
                                                                 "datomic.ddb-values")]
                                                           (when
                                                             (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                             (.debug
                                                               ^org.slf4j.Logger logger
                                                               (logger/process
                                                                 (assoc m_9829 :phase :begin))))
                                                           nil)
                                         start__8553__auto__ (java.lang.System/nanoTime)
                                         result__8554__auto__ (try
                                                                {:returned
                                                                 (*retry*
                                                                   (fn 
                                                                     fn__9833
                                                                     ([]
                                                                       (aws/invoke
                                                                         ddb_client
                                                                         {:op :PutItem,
                                                                          :req
                                                                          {:TableName table,
                                                                           :Item
                                                                           (ddb/create-item
                                                                             {:id (chunk-key id n),
                                                                              :v
                                                                              (nth
                                                                                chunks
                                                                                (int
                                                                                  ^java.lang.Number n))})}}))))}
                                                                (catch
                                                                  java.lang.Throwable
                                                                  t__8555__auto__
                                                                  {:threw t__8555__auto__}))
                                         elapsed_9830 (-
                                                        (java.lang.System/nanoTime)
                                                        start__8553__auto__)
                                         msec_9831 (logger/format-as-msec (long elapsed_9830))]
                                     (datomic.monitor/add-stat :DdbPutChunkMsec msec_9831)
                                     (let [endmsg__8556__auto__ (merge
                                                                  (assoc
                                                                    m_9829
                                                                    :msec
                                                                    msec_9831
                                                                    :phase
                                                                    :end)
                                                                  (when
                                                                    (:threw result__8554__auto__)
                                                                    {:threw
                                                                     (class
                                                                       (:threw
                                                                         result__8554__auto__))}))
                                           logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.ddb-values")]
                                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                         (.debug
                                           ^org.slf4j.Logger logger
                                           (logger/process endmsg__8556__auto__)))
                                       nil)
                                     (if (contains? result__8554__auto__ :returned)
                                       (:returned result__8554__auto__)
                                       (do (throw (:threw result__8554__auto__)) nil)))))))))
                       rets
                       (range 1 (java.lang.Integer/valueOf (int n)))))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:ddb-values/put
                 (mapv (fn fn__9842 ([p1__9820#] (select-keys p1__9820# [:aws/RequestId]))) rets),
                 :id id,
                 :bbuf-size (long (io/remaining v)),
                 :size (java.lang.Integer/valueOf (int (count body))),
                 :chunks (java.lang.Integer/valueOf (int n))})))
          nil)
        :created)))
  (reset-meta!
    #'put-value
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'table 'value]), :column (int 1)}
      :name
      'put-value
      :ns
      *ns*))
  (defn get-deitem
    ([ddb_client table id]
      (let [lmap {:TableName table, :Key (ddb/create-key id)}
            temp__5804__auto__ (or
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
        (when temp__5804__auto__ (let [result temp__5804__auto__] (ddb/deitem result))))))
  (reset-meta!
    #'get-deitem
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'table 'id]), :column (int 1)}
      :name
      'get-deitem
      :ns
      *ns*))
  (defn get-value
    ([ddb_client table id]
      (let [temp__5804__auto__ (let [m_9854 {:event :ddb-values/get-value, :id id}
                                     ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.ddb-values")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_9854 :phase :begin))))
                                                       nil)
                                     start__8553__auto__ (java.lang.System/nanoTime)
                                     result__8554__auto__ (try
                                                            {:returned
                                                             (get-deitem ddb_client table id)}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8555__auto__
                                                              {:threw t__8555__auto__}))
                                     elapsed_9855 (-
                                                    (java.lang.System/nanoTime)
                                                    start__8553__auto__)
                                     msec_9856 (logger/format-as-msec (long elapsed_9855))]
                                 (let [endmsg__8556__auto__ (merge
                                                              (assoc
                                                                m_9854
                                                                :msec
                                                                msec_9856
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8554__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8554__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.ddb-values")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8556__auto__)))
                                   nil)
                                 (if (contains? result__8554__auto__ :returned)
                                   (:returned result__8554__auto__)
                                   (do (throw (:threw result__8554__auto__)) nil)))]
        (when temp__5804__auto__
          (let [result temp__5804__auto__
                vec__9859 (split-map-keys-with
                            result
                            (fn fn__9862 ([p1__9852#] (.startsWith (name p1__9852#) "__"))))
                impl (nth vec__9859 (int 0) nil)
                user (nth vec__9859 (int 1) nil)
                n (:__n impl)
                v (:v user)]
            (if (= n 1)
              (and v (assoc user :v (io/base128->bbuf v)))
              (let [chunks (into
                             [v]
                             (pmap
                               (fn fn__9864
                                 ([p1__9853#]
                                   (:v
                                     (let [m_9865 {:event :ddb-values/get-value-chunk,
                                                   :id id,
                                                   :n p1__9853#}
                                           ___8552__auto__ (let 
                                                             [logger
                                                              (org.slf4j.LoggerFactory/getLogger
                                                                "datomic.ddb-values")]
                                                             (when
                                                               (.isDebugEnabled
                                                                 ^org.slf4j.Logger logger)
                                                               (.debug
                                                                 ^org.slf4j.Logger logger
                                                                 (logger/process
                                                                   (assoc m_9865 :phase :begin))))
                                                             nil)
                                           start__8553__auto__ (java.lang.System/nanoTime)
                                           result__8554__auto__ (try
                                                                  {:returned
                                                                   (*retry*
                                                                     (fn 
                                                                       fn__9869
                                                                       ([]
                                                                         (get-deitem
                                                                           ddb_client
                                                                           table
                                                                           (chunk-key
                                                                             id
                                                                             p1__9853#)))))}
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8555__auto__
                                                                    {:threw t__8555__auto__}))
                                           elapsed_9866 (-
                                                          (java.lang.System/nanoTime)
                                                          start__8553__auto__)
                                           msec_9867 (logger/format-as-msec (long elapsed_9866))]
                                       (let [endmsg__8556__auto__ (merge
                                                                    (assoc
                                                                      m_9865
                                                                      :msec
                                                                      msec_9867
                                                                      :phase
                                                                      :end)
                                                                    (when
                                                                      (:threw result__8554__auto__)
                                                                      {:threw
                                                                       (class
                                                                         (:threw
                                                                           result__8554__auto__))}))
                                             logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.ddb-values")]
                                         (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                           (.debug
                                             ^org.slf4j.Logger logger
                                             (logger/process endmsg__8556__auto__)))
                                         nil)
                                       (if (contains? result__8554__auto__ :returned)
                                         (:returned result__8554__auto__)
                                         (do (throw (:threw result__8554__auto__)) nil))))))
                               (range 1 n)))]
                (when (every? identity chunks)
                  (assoc user :v (io/base128->bbuf (apply str chunks)))))))))))
  (reset-meta!
    #'get-value
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'table 'id]), :column (int 1)}
      :name
      'get-value
      :ns
      *ns*))
  (defn delete-value
    ([ddb_client table id]
      (let [m_9885 {:event :ddb-values/delete-value, :id id}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_9885 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned
                                    (let [temp__5804__auto__ (:N
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
                                      (when temp__5804__auto__
                                        (let [n temp__5804__auto__]
                                          (dorun
                                            (map
                                              (fn fn__9889
                                                ([p1__9884#]
                                                  (let [m_9890 {:event
                                                                :ddb-values/delete-value-chunk,
                                                                :id id,
                                                                :n p1__9884#}
                                                        ___8552__auto__ (let 
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
                                                                                  m_9890
                                                                                  :phase
                                                                                  :begin))))
                                                                          nil)
                                                        start__8553__auto__ (java.lang.System/nanoTime)
                                                        result__8554__auto__ (try
                                                                               {:returned
                                                                                (aws/invoke
                                                                                  ddb_client
                                                                                  {:op :DeleteItem,
                                                                                   :req
                                                                                   {:TableName
                                                                                    table,
                                                                                    :Key
                                                                                    (ddb/create-key
                                                                                      (chunk-key
                                                                                        id
                                                                                        p1__9884#)),
                                                                                    :ReturnValues
                                                                                    "NONE"}})}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8555__auto__
                                                                                 {:threw
                                                                                  t__8555__auto__}))
                                                        elapsed_9891 (-
                                                                       (java.lang.System/nanoTime)
                                                                       start__8553__auto__)
                                                        msec_9892 (logger/format-as-msec
                                                                    (long elapsed_9891))]
                                                    (let [endmsg__8556__auto__ (merge
                                                                                 (assoc
                                                                                   m_9890
                                                                                   :msec
                                                                                   msec_9892
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
                                                                   "datomic.ddb-values")]
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
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_9886 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_9887 (logger/format-as-msec (long elapsed_9886))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_9885 :msec msec_9887 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'delete-value
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'table 'id]), :column (int 1)}
      :name
      'delete-value
      :ns
      *ns*)))