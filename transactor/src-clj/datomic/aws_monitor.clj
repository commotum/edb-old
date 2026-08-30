(do
  (clojure.core/in-ns 'datomic.aws-monitor)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.aws-monitor)
    {:doc "Functions to publish monitoring data to AWS."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.set :as 'set]
        ['datomic.cloudwatch :as 'cw]
        ['datomic.config :as 'config]
        ['datomic.slf4j :as 'logger]
        ['datomic.monitor :as 'monitor])))
  (when-not (.equals 'datomic.aws-monitor 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws-monitor))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.set :as 'set]
          ['datomic.cloudwatch :as 'cw]
          ['datomic.config :as 'config]
          ['datomic.slf4j :as 'logger]
          ['datomic.monitor :as 'monitor]))))
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol Qn (qualified-name [_]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.aws-monitor" "Qn")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'Qn :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'qualified-name
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.aws-monitor" "Qn"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.aws-monitor" "qualified-name")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*))))
  (extend
    clojure.lang.Keyword
    Qn
    {:qualified-name
     (fn fn__25944
       ([kw]
         (let [temp__5802__auto__ (namespace kw)]
           (if temp__5802__auto__
             (let [ns temp__5802__auto__] (str ns "/" (name kw)))
             (name kw)))))})
  (.setMeta (clojure.lang.RT/var "datomic.aws-monitor" "units") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.aws-monitor" "units")
    (merge
      {:WriterMemcachedPutFailedMusec "Microseconds",
       :MemoryIndexMB "Megabytes",
       :ReaderMemcachedPutFailedMusec "Microseconds",
       :HeartMonitorMsec "Milliseconds",
       :ValcacheGetFailedMsec "Milliseconds",
       :ValcachePutSucceededMsec "Milliseconds",
       :MemcachedGetFailedMsec "Milliseconds",
       :MemcachedPutSucceededMsec "Milliseconds",
       :TransactionBytes "Bytes",
       :AvailableMB "Megabytes",
       :ValueSize "Bytes",
       :ReaderMemcachedPutMusec "Microseconds",
       :HeartbeatMsec "Milliseconds",
       :MemcachedGetSucceededMsec "Milliseconds",
       :WriterMemcachedPutMusec "Microseconds",
       :StoragePutBytes "Bytes",
       :LogIngestMsec "Milliseconds",
       :ValcachePutFailedMsec "Milliseconds",
       :ValcacheGetSucceededMsec "Milliseconds",
       :MemoryIndexFillMsec "Milliseconds",
       :StoragePutBackoffMsec "Milliseconds",
       :LogIngestBytes "Bytes",
       :MemcachedPutFailedMsec "Milliseconds",
       :StorageGetBytes "Bytes",
       :StorageGetBackoffMsec "Milliseconds"}
      (zipmap (vals logger/event->timing) (repeat "Milliseconds"))))
  (let [protocol_metadata__7423 {:column (int 1)}]
    (defprotocol
      ToMetricData
      (to-metric-data-helper [v k] "argument flipping helper for to-metric-data"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.aws-monitor" "ToMetricData")
      (assoc (assoc protocol_metadata__7423 :doc nil) :name 'ToMetricData :ns *ns*))
    (let [protocol_signature__7424 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'to-metric-data-helper
                                        {:arglists (clojure.core/list ['v 'k])}),
                                      :arglists (clojure.core/list ['v 'k]),
                                      :doc "argument flipping helper for to-metric-data"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.aws-monitor" "ToMetricData"))
          protocol_method_name__7425 (with-meta
                                       (:name protocol_signature__7424)
                                       protocol_signature__7424)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.aws-monitor" "to-metric-data-helper")
        (assoc protocol_signature__7424 :name protocol_method_name__7425 :ns *ns*))))
  (extend
    java.util.Map
    ToMetricData
    {:to-metric-data-helper
     (fn fn__25965
       ([v k]
         (let [map__25966 v
               map__25966 (if (seq? map__25966)
                            (if (next map__25966)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__25966))
                              (if (seq map__25966) (first map__25966) {}))
                            map__25966)
               lo (get map__25966 :lo)
               hi (get map__25966 :hi)
               sum (get map__25966 :sum)
               count (get map__25966 :count)]
           {:unit (get units k "Count"),
            :metricName (qualified-name k),
            :statisticValues
            {:minimum (java.lang.Double/valueOf (double lo)),
             :maximum (java.lang.Double/valueOf (double hi)),
             :sampleCount (java.lang.Double/valueOf (double count)),
             :sum (java.lang.Double/valueOf (double sum))}})))})
  (extend
    java.lang.Number
    ToMetricData
    {:to-metric-data-helper
     (fn fn__25968
       ([v k]
         {:unit (get units k "None"),
          :metricName (qualified-name k),
          :value (java.lang.Double/valueOf (double v))}))})
  (defn to-metric-data
    ([statistics]
      (mapv
        (fn fn__25971
          ([p__25970]
            (let [vec__25972 p__25970
                  k (nth vec__25972 (int 0) nil)
                  v (nth vec__25972 (int 1) nil)]
              (to-metric-data-helper v k))))
        statistics)))
  (reset-meta!
    #'to-metric-data
    (assoc
      {:arglists (clojure.core/list ['statistics]), :column (int 1)}
      :name
      'to-metric-data
      :ns
      *ns*))
  (def AWS_METRIC_DATA_COUNT_LIMIT 20)
  (reset-meta!
    #'AWS_METRIC_DATA_COUNT_LIMIT
    (assoc {:const true, :column (int 1)} :name 'AWS_METRIC_DATA_COUNT_LIMIT :ns *ns*))
  (defn create-request-map
    ([dimensions metrics]
      {:namespace "Datomic",
       :metricData
       (mapv
         (fn fn__25978 ([p1__25977#] (assoc p1__25977# :dimensions dimensions)))
         (to-metric-data metrics))}))
  (reset-meta!
    #'create-request-map
    (assoc
      {:arglists (clojure.core/list ['dimensions 'metrics]), :column (int 1)}
      :name
      'create-request-map
      :ns
      *ns*))
  (def MILLION 1000000)
  (reset-meta! #'MILLION (assoc {:const true, :column (int 1)} :name 'MILLION :ns *ns*))
  (defn partitioned-metrics-requests
    ([dimensions metrics]
      (map (partial create-request-map dimensions) (partition-all 20 metrics))))
  (reset-meta!
    #'partitioned-metrics-requests
    (assoc
      {:arglists (clojure.core/list ['dimensions 'metrics]), :column (int 1)}
      :name
      'partitioned-metrics-requests
      :ns
      *ns*))
  (defn report-metrics
    ([client dimensions metrics]
      (let [mparts (partitioned-metrics-requests dimensions metrics)]
        (loop [seq_25982 (seq mparts) chunk_25983 nil count_25984 0 i_25985 0]
          (if (< i_25985 count_25984)
            (let [mpart (.nth ^clojure.lang.Indexed chunk_25983 (int i_25985))]
              (cw/put-metrics client mpart)
              (recur seq_25982 chunk_25983 count_25984 (inc i_25985)))
            (let [temp__5804__auto__ (seq seq_25982)]
              (when temp__5804__auto__
                (let [seq_25982 temp__5804__auto__]
                  (if (chunked-seq? seq_25982)
                    (let [c__6065__auto__ (chunk-first seq_25982)]
                      (recur
                        (chunk-rest seq_25982)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [mpart (first seq_25982)]
                      (cw/put-metrics client mpart)
                      (recur (next seq_25982) nil 0 0)))))))))))
  (reset-meta!
    #'report-metrics
    (assoc
      {:arglists (clojure.core/list ['client 'dimensions 'metrics]), :column (int 1)}
      :name
      'report-metrics
      :ns
      *ns*))
  (def create-cloudwatch-reporter
   (fn create_cloudwatch_reporter
     ([& p__25989]
       (let [map__25990 p__25989
             map__25990 (if (seq? map__25990)
                          (if (next map__25990)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__25990))
                            (if (seq map__25990) (first map__25990) {}))
                          map__25990)
             name (get map__25990 :name)
             creds (get map__25990 :creds)
             aws_cloudwatch_dimension_value (get map__25990 :aws-cloudwatch-dimension-value)
             aws_cloudwatch_region (get map__25990 :aws-cloudwatch-region)]
         (when (and aws_cloudwatch_dimension_value aws_cloudwatch_region)
           (let [client (cw/client creds {:region aws_cloudwatch_region})]
             (fn fn__25991
               ([metrics]
                 (report-metrics
                   client
                   [{:name name, :value aws_cloudwatch_dimension_value}]
                   metrics)))))))))
  (reset-meta!
    #'create-cloudwatch-reporter
    (assoc
      {:arglists
       (clojure.core/list
         ['& {:keys ['name 'creds 'aws-cloudwatch-dimension-value 'aws-cloudwatch-region]}]),
       :column (int 1)}
      :name
      'create-cloudwatch-reporter
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.aws-monitor" "cloudwatch-reporter-ref")
    {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.aws-monitor" "cloudwatch-reporter-ref")
    (delay
      (create-cloudwatch-reporter
        :name
        (config/property "datomic.cloudwatchName")
        :creds
        (let [temp__5804__auto__ (config/property "datomic.cloudwatchAccessKeyId")]
          (when temp__5804__auto__
            (let [key_id temp__5804__auto__]
              {:aws-access-key-id key_id,
               :aws-secret-key (config/property "datomic.cloudwatchSecretKey")})))
        :aws-cloudwatch-dimension-value
        (config/property "datomic.cloudwatchDimension")
        :aws-cloudwatch-region
        (config/property "datomic.cloudwatchRegion"))))
  (defn cloudwatch-reporter ([m] ((deref cloudwatch-reporter-ref) m)))
  (reset-meta!
    #'cloudwatch-reporter
    (assoc
      {:arglists (clojure.core/list ['m]), :column (int 1)}
      :name
      'cloudwatch-reporter
      :ns
      *ns*)))