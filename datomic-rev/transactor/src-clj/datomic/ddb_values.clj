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
        (fn fn__30533
          ([p__30531 p__30532]
            (let [vec__30534 p__30531
                  match (nth vec__30534 (int 0) nil)
                  nomatch (nth vec__30534 (int 1) nil)
                  vec__30537 p__30532
                  k (nth vec__30537 (int 0) nil)
                  v (nth vec__30537 (int 1) nil)]
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
      (let [map__30548 value
            map__30548 (if (seq? map__30548)
                         (if (next map__30548)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30548))
                           (if (seq map__30548) (first map__30548) {}))
                         map__30548)
            id (get map__30548 :id)
            v (get map__30548 :v)
            base (dissoc value :id :v)
            body (io/bbuf->base128 v)
            chunks (chunk body (long (* 62 1024)))
            n (count chunks)
            rets [(atom
                    (let [m_30549 {:event :ddb-values/put-value,
                                   :id id,
                                   :bufsize
                                   (java.lang.Integer/valueOf (int (count (nth chunks (int 0)))))}
                          ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.ddb-values")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_30549 :phase :begin))))
                                            nil)
                          start__8584__auto__ (java.lang.System/nanoTime)
                          result__8585__auto__ (try
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
                                                   t__8586__auto__
                                                   {:threw t__8586__auto__}))
                          elapsed_30550 (- (java.lang.System/nanoTime) start__8584__auto__)
                          msec_30551 (logger/format-as-msec (long elapsed_30550))]
                      (let [endmsg__8587__auto__ (merge
                                                   (assoc m_30549 :msec msec_30551 :phase :end)
                                                   (when (:threw result__8585__auto__)
                                                     {:threw
                                                      (class (:threw result__8585__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                        nil)
                      (if (contains? result__8585__auto__ :returned)
                        (:returned result__8585__auto__)
                        (do (throw (:threw result__8585__auto__)) atom))))]]
        (let [rets (mapv
                     deref
                     (reduce
                       (fn fn__30554
                         ([rets n]
                           (conj
                             rets
                             (let [f__16176__auto__ (df/-future-with-channel-impl
                                                      (deref chunk-pool)
                                                      (fn fn__30555
                                                        ([]
                                                          (let [m_30556
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
                                                                ___8583__auto__
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
                                                                          m_30556
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
                                                                       fn__30560
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
                                                                    t__8586__auto__
                                                                    {:threw t__8586__auto__}))
                                                                elapsed_30557
                                                                (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8584__auto__)
                                                                msec_30558
                                                                (logger/format-as-msec
                                                                  (long elapsed_30557))]
                                                            (datomic.monitor/add-stat
                                                              :DdbPutChunkMsec
                                                              msec_30558)
                                                            (let 
                                                              [endmsg__8587__auto__
                                                               (merge
                                                                 (assoc
                                                                   m_30556
                                                                   :msec
                                                                   msec_30558
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
                                                                 "datomic.ddb-values")]
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
                                 {:line 80, :column 31, :file "datomic/ddb_values.clj"}
                                 (deref df/bounding-warn-seconds))
                               f__16176__auto__))))
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
                 :chunks (java.lang.Integer/valueOf (int n))})))
          nil)
        :created)))
  (defn get-deitem
    ([ddb_client table id]
      (let [lmap {:tableName table, :key (ddb/create-key id)}
            temp__5804__auto__ (or
                                 (:item (ddb/get-item ddb_client lmap))
                                 (do
                                   (let [logger (org.slf4j.LoggerFactory/getLogger
                                                  "datomic.ddb-values")]
                                     (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                       (.info
                                         ^org.slf4j.Logger logger
                                         (logger/process
                                           #:ddb-values{:get-deitem-consistent-retry id})))
                                     nil)
                                   (:item
                                     (ddb/get-item
                                       ddb_client
                                       (assoc lmap :consistentRead true)))))]
        (when temp__5804__auto__ (let [result temp__5804__auto__] (ddb/deitem result))))))
  (defn get-value
    ([ddb_client table id]
      (let [temp__5804__auto__ (let [m_30581 {:event :ddb-values/get-value, :id id}
                                     ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.ddb-values")]
                                                       (when (.isDebugEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.debug
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_30581 :phase :begin))))
                                                       nil)
                                     start__8584__auto__ (java.lang.System/nanoTime)
                                     result__8585__auto__ (try
                                                            {:returned
                                                             (get-deitem ddb_client table id)}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8586__auto__
                                                              {:threw t__8586__auto__}))
                                     elapsed_30582 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8584__auto__)
                                     msec_30583 (logger/format-as-msec (long elapsed_30582))]
                                 (let [endmsg__8587__auto__ (merge
                                                              (assoc
                                                                m_30581
                                                                :msec
                                                                msec_30583
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8585__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8585__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger
                                                "datomic.ddb-values")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8587__auto__)))
                                   nil)
                                 (if (contains? result__8585__auto__ :returned)
                                   (:returned result__8585__auto__)
                                   (do (throw (:threw result__8585__auto__)) nil)))]
        (when temp__5804__auto__
          (let [result temp__5804__auto__
                vec__30586 (split-map-keys-with
                             result
                             (fn fn__30589 ([p1__30579#] (.startsWith (name p1__30579#) "__"))))
                impl (nth vec__30586 (int 0) nil)
                user (nth vec__30586 (int 1) nil)
                n (:__n impl)
                v (:v user)]
            (if (= n 1)
              (and v (assoc user :v (io/base128->bbuf v)))
              (let [chunks (into
                             [v]
                             (pmap
                               (fn fn__30591
                                 ([p1__30580#]
                                   (:v
                                     (let [m_30592 {:event :ddb-values/get-value-chunk,
                                                    :id id,
                                                    :n p1__30580#}
                                           ___8583__auto__ (let 
                                                             [logger
                                                              (org.slf4j.LoggerFactory/getLogger
                                                                "datomic.ddb-values")]
                                                             (when
                                                               (.isDebugEnabled
                                                                 ^org.slf4j.Logger logger)
                                                               (.debug
                                                                 ^org.slf4j.Logger logger
                                                                 (logger/process
                                                                   (assoc m_30592 :phase :begin))))
                                                             nil)
                                           start__8584__auto__ (java.lang.System/nanoTime)
                                           result__8585__auto__ (try
                                                                  {:returned
                                                                   (*retry*
                                                                     (fn 
                                                                       fn__30596
                                                                       ([]
                                                                         (get-deitem
                                                                           ddb_client
                                                                           table
                                                                           (chunk-key
                                                                             id
                                                                             p1__30580#)))))}
                                                                  (catch
                                                                    java.lang.Throwable
                                                                    t__8586__auto__
                                                                    {:threw t__8586__auto__}))
                                           elapsed_30593 (-
                                                           (java.lang.System/nanoTime)
                                                           start__8584__auto__)
                                           msec_30594 (logger/format-as-msec (long elapsed_30593))]
                                       (let [endmsg__8587__auto__ (merge
                                                                    (assoc
                                                                      m_30592
                                                                      :msec
                                                                      msec_30594
                                                                      :phase
                                                                      :end)
                                                                    (when
                                                                      (:threw result__8585__auto__)
                                                                      {:threw
                                                                       (class
                                                                         (:threw
                                                                           result__8585__auto__))}))
                                             logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.ddb-values")]
                                         (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                           (.debug
                                             ^org.slf4j.Logger logger
                                             (logger/process endmsg__8587__auto__)))
                                         nil)
                                       (if (contains? result__8585__auto__ :returned)
                                         (:returned result__8585__auto__)
                                         (do (throw (:threw result__8585__auto__)) nil))))))
                               (range 1 n)))]
                (when (every? identity chunks)
                  (assoc user :v (io/base128->bbuf (apply str chunks)))))))))))
  (defn delete-value
    ([ddb_client table id]
      (let [m_30612 {:event :ddb-values/delete-value, :id id}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_30612 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [temp__5804__auto__ (:n
                                                               (get
                                                                 (:item
                                                                   (ddb/get-item
                                                                     ddb_client
                                                                     {:tableName table,
                                                                      :key (ddb/create-key id),
                                                                      :attributesToGet ["__n"]}))
                                                                 "__n"))]
                                      (when temp__5804__auto__
                                        (let [n temp__5804__auto__]
                                          (dorun
                                            (map
                                              (fn fn__30616
                                                ([p1__30611#]
                                                  (let [m_30617 {:event
                                                                 :ddb-values/delete-value-chunk,
                                                                 :id id,
                                                                 :n p1__30611#}
                                                        ___8583__auto__ (let 
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
                                                                                  m_30617
                                                                                  :phase
                                                                                  :begin))))
                                                                          nil)
                                                        start__8584__auto__ (java.lang.System/nanoTime)
                                                        result__8585__auto__ (try
                                                                               {:returned
                                                                                (ddb/delete-item
                                                                                  ddb_client
                                                                                  {:tableName
                                                                                   table,
                                                                                   :key
                                                                                   (ddb/create-key
                                                                                     (chunk-key
                                                                                       id
                                                                                       p1__30611#)),
                                                                                   :returnValues
                                                                                   "NONE"})}
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t__8586__auto__
                                                                                 {:threw
                                                                                  t__8586__auto__}))
                                                        elapsed_30618 (-
                                                                        (java.lang.System/nanoTime)
                                                                        start__8584__auto__)
                                                        msec_30619 (logger/format-as-msec
                                                                     (long elapsed_30618))]
                                                    (let [endmsg__8587__auto__ (merge
                                                                                 (assoc
                                                                                   m_30617
                                                                                   :msec
                                                                                   msec_30619
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
                                                                   "datomic.ddb-values")]
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
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_30613 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_30614 (logger/format-as-msec (long elapsed_30613))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_30612 :msec msec_30614 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-values")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil))))))