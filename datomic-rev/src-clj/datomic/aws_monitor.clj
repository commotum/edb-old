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
  (defonce Qn {})
  (defprotocol Qn (qualified-name [_]))
  (extend
    clojure.lang.Keyword
    Qn
    {:qualified-name
     (fn fn__23585
       ([kw]
         (let [temp__5455__auto__ (namespace kw)]
           (if temp__5455__auto__
             (let [ns temp__5455__auto__] (str ns "/" (name kw)))
             (name kw)))))})
  (def units
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
  (defonce ToMetricData {})
  (defprotocol ToMetricData (to-metric-data-helper [v k]))
  (extend
    java.util.Map
    ToMetricData
    {:to-metric-data-helper
     (fn fn__23606
       ([v k]
         (let [map__23607 v
               map__23607 (if (seq? map__23607)
                            (clojure.lang.PersistentHashMap/create (seq map__23607))
                            map__23607)
               lo (get map__23607 :lo)
               hi (get map__23607 :hi)
               sum (get map__23607 :sum)
               count (get map__23607 :count)]
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
     (fn fn__23609
       ([v k]
         {:unit (get units k "None"),
          :metricName (qualified-name k),
          :value (java.lang.Double/valueOf (double v))}))})
  (defn to-metric-data
    ([statistics]
      (mapv
        (fn fn__23612
          ([p__23611]
            (let [vec__23613 p__23611
                  k (nth vec__23613 (int 0) nil)
                  v (nth vec__23613 (int 1) nil)]
              (to-metric-data-helper v k))))
        statistics)))
  (def AWS_METRIC_DATA_COUNT_LIMIT 20)
  (reset-meta!
    #'AWS_METRIC_DATA_COUNT_LIMIT
    (assoc {:const true, :column 1} :name 'AWS_METRIC_DATA_COUNT_LIMIT :ns *ns*))
  (defn create-request-map
    ([dimensions metrics]
      {:namespace "Datomic",
       :metricData
       (mapv
         (fn fn__23619 ([p1__23618#] (assoc p1__23618# :dimensions dimensions)))
         (to-metric-data metrics))}))
  (def MILLION 1000000)
  (reset-meta! #'MILLION (assoc {:const true, :column 1} :name 'MILLION :ns *ns*))
  (defn partitioned-metrics-requests
    ([dimensions metrics]
      (map (partial create-request-map dimensions) (partition-all 20 metrics))))
  (defn report-metrics
    ([client dimensions metrics]
      (let [mparts (partitioned-metrics-requests dimensions metrics)]
        (loop [seq_23623 (seq mparts) chunk_23624 nil count_23625 0 i_23626 0]
          (if (< i_23626 count_23625)
            (let [mpart (.nth ^clojure.lang.Indexed chunk_23624 (int i_23626))]
              (cw/put-metrics client mpart)
              (recur seq_23623 chunk_23624 count_23625 (inc i_23626)))
            (let [temp__5457__auto__ (seq seq_23623)]
              (when temp__5457__auto__
                (let [seq_23623 temp__5457__auto__]
                  (if (chunked-seq? seq_23623)
                    (let [c__5719__auto__ (chunk-first seq_23623)]
                      (recur
                        (chunk-rest seq_23623)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [mpart (first seq_23623)]
                      (cw/put-metrics client mpart)
                      (recur (next seq_23623) nil 0 0)))))))))))
  (defn create-cloudwatch-reporter
    ([& p__23630]
      (let [map__23631 p__23630
            map__23631 (if (seq? map__23631)
                         (clojure.lang.PersistentHashMap/create (seq map__23631))
                         map__23631)
            name (get map__23631 :name)
            creds (get map__23631 :creds)
            aws_cloudwatch_dimension_value (get map__23631 :aws-cloudwatch-dimension-value)
            aws_cloudwatch_region (get map__23631 :aws-cloudwatch-region)]
        (when (and aws_cloudwatch_dimension_value aws_cloudwatch_region)
          (let [client (cw/client creds {:region aws_cloudwatch_region})]
            (fn fn__23632
              ([metrics]
                (report-metrics
                  client
                  [{:name name, :value aws_cloudwatch_dimension_value}]
                  metrics))))))))
  (def cloudwatch-reporter-ref
   (delay
     (create-cloudwatch-reporter
       :name
       (config/property "datomic.cloudwatchName")
       :creds
       (let [temp__5457__auto__ (config/property "datomic.cloudwatchAccessKeyId")]
         (when temp__5457__auto__
           (let [key_id temp__5457__auto__]
             {:aws-access-key-id key_id,
              :aws-secret-key (config/property "datomic.cloudwatchSecretKey")})))
       :aws-cloudwatch-dimension-value
       (config/property "datomic.cloudwatchDimension")
       :aws-cloudwatch-region
       (config/property "datomic.cloudwatchRegion"))))
  (reset-meta!
    #'cloudwatch-reporter-ref
    (assoc {:private true, :column 1} :name 'cloudwatch-reporter-ref :ns *ns*))
  (defn cloudwatch-reporter ([m] ((deref cloudwatch-reporter-ref) m))))