(do
  (clojure.core/in-ns 'datomic.aws-monitor)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.aws-monitor)
    {:doc
     "Publishes process metrics to Amazon CloudWatch. Converts scalar and bounded-statistics values to CloudWatch metric data, attaches deployment dimensions, and partitions requests to the service limit of twenty metrics."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.core2.aws.helpers :as 'aws]
        ['datomic.cloudwatch :as 'cw]
        ['datomic.config :as 'config]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.aws-monitor 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws-monitor))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.core2.aws.helpers :as 'aws]
          ['datomic.cloudwatch :as 'cw]
          ['datomic.config :as 'config]
          ['datomic.slf4j :as 'logger]))))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Qn (qualified-name [_]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.aws-monitor" "Qn")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Qn :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'qualified-name
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.aws-monitor" "Qn"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.aws-monitor" "qualified-name")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (extend
    clojure.lang.Keyword
    Qn
    {:qualified-name
     (fn fn__21908
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
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol
      ToMetricData
      (to-metric-data-helper [v k] "argument flipping helper for to-metric-data"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.aws-monitor" "ToMetricData")
      (assoc (assoc protocol_metadata__7466 :doc nil) :name 'ToMetricData :ns *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'to-metric-data-helper
                                        {:arglists (clojure.core/list ['v 'k])}),
                                      :arglists (clojure.core/list ['v 'k]),
                                      :doc "argument flipping helper for to-metric-data"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.aws-monitor" "ToMetricData"))
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.aws-monitor" "to-metric-data-helper")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*))))
  (extend
    java.util.Map
    ToMetricData
    {:to-metric-data-helper
     (fn fn__21929
       ([v k]
         (let [map__21930 v
               map__21930 (if (seq? map__21930)
                            (if (next map__21930)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__21930))
                              (if (seq map__21930) (first map__21930) {}))
                            map__21930)
               lo (get map__21930 :lo)
               hi (get map__21930 :hi)
               sum (get map__21930 :sum)
               count (get map__21930 :count)]
           {:Unit (get units k "Count"),
            :MetricName (qualified-name k),
            :StatisticValues
            {:Minimum (java.lang.Double/valueOf (double lo)),
             :Maximum (java.lang.Double/valueOf (double hi)),
             :SampleCount (java.lang.Double/valueOf (double count)),
             :Sum (java.lang.Double/valueOf (double sum))}})))})
  (extend
    java.lang.Number
    ToMetricData
    {:to-metric-data-helper
     (fn fn__21932
       ([v k]
         {:Unit (get units k "None"),
          :MetricName (qualified-name k),
          :Value (java.lang.Double/valueOf (double v))}))})
  ;; Converts scalar samples and {:lo :hi :sum :count} accumulators to CloudWatch metric values.
  (defn to-metric-data
    ([statistics]
      (mapv
        (fn fn__21935
          ([p__21934]
            (let [vec__21936 p__21934
                  k (nth vec__21936 (int 0) nil)
                  v (nth vec__21936 (int 1) nil)]
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
      {:Namespace "Datomic",
       :MetricData
       (mapv
         (fn fn__21942 ([p1__21941#] (assoc p1__21941# :Dimensions dimensions)))
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
  ;; Attaches dimensions and partitions metric data into CloudWatch's twenty-item request limit.
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
  ;; Submits every request partition synchronously; service failures propagate to the reporter.
  (defn report-metrics
    ([client dimensions metrics]
      (let [mparts (partitioned-metrics-requests dimensions metrics)]
        (loop [seq_21946 (seq mparts) chunk_21947 nil count_21948 0 i_21949 0]
          (if (< i_21949 count_21948)
            (let [mpart (.nth ^clojure.lang.Indexed chunk_21947 (int i_21949))]
              (aws/invoke client {:op :PutMetricData, :req mpart})
              (recur seq_21946 chunk_21947 count_21948 (inc i_21949)))
            (let [temp__5804__auto__ (seq seq_21946)]
              (when temp__5804__auto__
                (let [seq_21946 temp__5804__auto__]
                  (if (chunked-seq? seq_21946)
                    (let [c__6065__auto__ (chunk-first seq_21946)]
                      (recur
                        (chunk-rest seq_21946)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [mpart (first seq_21946)]
                      (aws/invoke client {:op :PutMetricData, :req mpart})
                      (recur (next seq_21946) nil 0 0)))))))))))
  (reset-meta!
    #'report-metrics
    (assoc
      {:arglists (clojure.core/list ['client 'dimensions 'metrics]), :column (int 1)}
      :name
      'report-metrics
      :ns
      *ns*))
  ;; Creates a callback when both dimension and region are configured. Static credentials are
  ;; accepted for compatibility; otherwise the AWS SDK default credentials provider is used.
  (defn create-cloudwatch-reporter
    ([& p__21953]
      (let [map__21954 p__21953
            map__21954 (if (seq? map__21954)
                         (if (next map__21954)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21954))
                           (if (seq map__21954) (first map__21954) {}))
                         map__21954)
            name (get map__21954 :name)
            creds (get map__21954 :creds)
            aws_cloudwatch_dimension_value (get map__21954 :aws-cloudwatch-dimension-value)
            aws_cloudwatch_region (get map__21954 :aws-cloudwatch-region)]
        (when (and aws_cloudwatch_dimension_value aws_cloudwatch_region)
          (let [client (if (:aws-access-key-id creds)
                         (cw/client
                           (aws/static-credentials-provider creds)
                           {:region aws_cloudwatch_region})
                         (cw/client {:region aws_cloudwatch_region}))]
            (fn fn__21955
              ([metrics]
                (report-metrics
                  client
                  [{:Name name, :Value aws_cloudwatch_dimension_value}]
                  metrics))))))))
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
