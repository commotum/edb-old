(do
  (clojure.core/in-ns 'datomic.cloudwatch)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.aws :as 'aws]
        ['datomic.datafy :as 'd])
      (clojure.core/import 'com.amazonaws.services.cloudwatch.AmazonCloudWatchClient)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.Dimension)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.DimensionFilter)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.ListMetricsRequest)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.Metric)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.MetricDatum)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.ListMetricsRequest)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.PutMetricDataRequest)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.StatisticSet)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.StateValue)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.MetricAlarm)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult)
      (clojure.core/import 'com.amazonaws.services.cloudwatch.model.StandardUnit)))
  (when-not (.equals 'datomic.cloudwatch 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cloudwatch))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common]
          ['datomic.aws :as 'aws]
          ['datomic.datafy :as 'd])
        (clojure.core/import 'com.amazonaws.services.cloudwatch.AmazonCloudWatchClient)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.Dimension)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.DimensionFilter)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.ListMetricsRequest)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.Metric)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.MetricDatum)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.ListMetricsRequest)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.PutMetricDataRequest)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.StatisticSet)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.StateValue)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.MetricAlarm)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult)
        (clojure.core/import 'com.amazonaws.services.cloudwatch.model.StandardUnit))))
  (set! *warn-on-reflection* true)
  (defn client
    ([creds config]
      (if config
        (let [region (get config :region)
              override_endpoint (get config :override-endpoint)
              conf (d/data-to-object
                     (dissoc config :region :override-endpoint)
                     com.amazonaws.ClientConfiguration)
              conn (if creds
                     (if (instance?
                           com.amazonaws.auth.AWSCredentialsProvider
                           (aws/credentials creds))
                       (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.
                         (aws/credentials creds)
                         ^com.amazonaws.ClientConfiguration conf)
                       (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.
                         (aws/credentials creds)
                         ^com.amazonaws.ClientConfiguration conf))
                     (if (instance?
                           com.amazonaws.auth.AWSCredentialsProvider
                           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
                       (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.
                         (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                         ^com.amazonaws.ClientConfiguration conf)
                       (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.
                         (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                         ^com.amazonaws.ClientConfiguration conf)))]
          (cond
            override_endpoint (.setEndpoint
                                ^com.amazonaws.AmazonWebServiceClient conn
                                (str "http://" override_endpoint))
            region (do
                     (.setEndpoint
                       ^com.amazonaws.AmazonWebServiceClient conn
                       (aws/endpoint-for :monitoring region))))
          conn)
        (client creds)))
    ([creds]
      (if creds
        (if (instance? com.amazonaws.auth.AWSCredentialsProvider (aws/credentials creds))
          (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient. (aws/credentials creds))
          (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient. (aws/credentials creds)))
        (if (instance?
              com.amazonaws.auth.AWSCredentialsProvider
              (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
          (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.
            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
          (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.
            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
    ([]
      (if (instance?
            com.amazonaws.auth.AWSCredentialsProvider
            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
        (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.
          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
        (com.amazonaws.services.cloudwatch.AmazonCloudWatchClient.
          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
  (defn list-metrics
    ([client request]
      (let [result (if request
                     (.listMetrics
                       ^com.amazonaws.services.cloudwatch.AmazonCloudWatchClient client
                       (d/data-to-object
                         request
                         com.amazonaws.services.cloudwatch.model.ListMetricsRequest))
                     (.listMetrics
                       ^com.amazonaws.services.cloudwatch.AmazonCloudWatchClient client))
            metrics (.getMetrics
                      ^com.amazonaws.services.cloudwatch.model.ListMetricsResult result)]
        (map
          d/object-to-data
          (let [temp__5802__auto__ (.getNextToken
                                     ^com.amazonaws.services.cloudwatch.model.ListMetricsResult result)]
            (if temp__5802__auto__
              (let [token temp__5802__auto__]
                (lazy-seq
                  (concat
                    metrics
                    (list-metrics
                      client
                      (let [G__25782 (com.amazonaws.services.cloudwatch.model.ListMetricsRequest.)]
                        (.setNextToken
                          ^com.amazonaws.services.cloudwatch.model.ListMetricsRequest G__25782
                          ^java.lang.String token)
                        G__25782)))))
              metrics)))))
    ([client] (list-metrics client nil)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest :dimensions]
    com.amazonaws.services.cloudwatch.model.Dimension)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest :dimensions]
    fn__25786
    ([_ _ val _]
      (mapv
        (fn fn__25787
          ([item] (d/data-to-object item com.amazonaws.services.cloudwatch.model.Dimension)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.cloudwatch.model.MetricDatum :dimensions]
    com.amazonaws.services.cloudwatch.model.Dimension)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.cloudwatch.model.MetricDatum :dimensions]
    fn__25790
    ([_ _ val _]
      (mapv
        (fn fn__25791
          ([item] (d/data-to-object item com.amazonaws.services.cloudwatch.model.Dimension)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.cloudwatch.model.ListMetricsRequest :dimensions]
    com.amazonaws.services.cloudwatch.model.DimensionFilter)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.cloudwatch.model.ListMetricsRequest :dimensions]
    fn__25794
    ([_ _ val _]
      (mapv
        (fn fn__25795
          ([item] (d/data-to-object item com.amazonaws.services.cloudwatch.model.DimensionFilter)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.cloudwatch.model.PutMetricDataRequest :metricData]
    com.amazonaws.services.cloudwatch.model.MetricDatum)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.cloudwatch.model.PutMetricDataRequest :metricData]
    fn__25798
    ([_ _ val _]
      (mapv
        (fn fn__25799
          ([item] (d/data-to-object item com.amazonaws.services.cloudwatch.model.MetricDatum)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest :alarmNames]
    java.lang.String)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest :alarmNames]
    fn__25802
    ([_ _ val _] (mapv (fn fn__25803 ([item] (d/data-to-object item java.lang.String))) val)))
  (extend
    com.amazonaws.services.cloudwatch.model.Dimension
    d/ObjectToData
    {:object-to-data
     (fn fn__25806
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getName
                                        ^com.amazonaws.services.cloudwatch.model.Dimension o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:name (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getValue
                                        ^com.amazonaws.services.cloudwatch.model.Dimension o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:value (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.cloudwatch.model.DimensionFilter
    d/ObjectToData
    {:object-to-data
     (fn fn__25812
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getName
                                        ^com.amazonaws.services.cloudwatch.model.DimensionFilter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:name (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getValue
                                        ^com.amazonaws.services.cloudwatch.model.DimensionFilter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:value (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.cloudwatch.model.MetricAlarm
    d/ObjectToData
    {:object-to-data
     (fn fn__25818
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getNamespace
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:namespace (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDimensions
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:dimensions (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPeriod
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:period (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getComparisonOperator
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:comparisonOperator (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getMetricName
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:metricName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getUnit
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:unit (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStateValue
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stateValue (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAlarmName
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:alarmName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAlarmArn
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:alarmArn (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAlarmDescription
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:alarmDescription (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAlarmConfigurationUpdatedTimestamp
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:alarmConfigurationUpdatedTimestamp
                    (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getActionsEnabled
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:actionsEnabled (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isActionsEnabled
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:actionsEnabled (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getOKActions
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:oKActions (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAlarmActions
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:alarmActions (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getInsufficientDataActions
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:insufficientDataActions (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStateReason
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stateReason (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStateReasonData
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stateReasonData (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStateUpdatedTimestamp
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stateUpdatedTimestamp (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStatistic
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:statistic (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getExtendedStatistic
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:extendedStatistic (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getEvaluationPeriods
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:evaluationPeriods (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDatapointsToAlarm
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:datapointsToAlarm (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getThreshold
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:threshold (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getTreatMissingData
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:treatMissingData (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getEvaluateLowSampleCountPercentile
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:evaluateLowSampleCountPercentile
                    (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getMetrics
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:metrics (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getThresholdMetricId
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:thresholdMetricId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getEvaluationState
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:evaluationState (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStateTransitionedTimestamp
                                        ^com.amazonaws.services.cloudwatch.model.MetricAlarm o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stateTransitionedTimestamp
                    (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult
    d/ObjectToData
    {:object-to-data
     (fn fn__25880
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getNextToken
                                        ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:nextToken (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCompositeAlarms
                                        ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:compositeAlarms (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getMetricAlarms
                                        ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:metricAlarms (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSdkResponseMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sdkResponseMetadata (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSdkHttpMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sdkHttpMetadata (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudwatch.model.Dimension]
    fn__25892
    ([m _]
      (let [temp__5804__auto__ (seq (remove #{:name :value} (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:name :value},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudwatch.model.Dimension}))))
        nil)
      (let [o (com.amazonaws.services.cloudwatch.model.Dimension.)]
        (when (contains? m :name)
          (let [v (:name m) k (d/property-to-object (class o) :name v java.lang.String)]
            (.setName ^com.amazonaws.services.cloudwatch.model.Dimension o ^java.lang.String k)))
        (when (contains? m :value)
          (let [v (:value m) k (d/property-to-object (class o) :value v java.lang.String)]
            (.setValue ^com.amazonaws.services.cloudwatch.model.Dimension o ^java.lang.String k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudwatch.model.DimensionFilter]
    fn__25895
    ([m _]
      (let [temp__5804__auto__ (seq (remove #{:name :value} (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:name :value},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudwatch.model.DimensionFilter}))))
        nil)
      (let [o (com.amazonaws.services.cloudwatch.model.DimensionFilter.)]
        (when (contains? m :name)
          (let [v (:name m) k (d/property-to-object (class o) :name v java.lang.String)]
            (.setName
              ^com.amazonaws.services.cloudwatch.model.DimensionFilter o
              ^java.lang.String k)))
        (when (contains? m :value)
          (let [v (:value m) k (d/property-to-object (class o) :value v java.lang.String)]
            (.setValue
              ^com.amazonaws.services.cloudwatch.model.DimensionFilter o
              ^java.lang.String k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest]
    fn__25898
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:startTime :unit :endTime :requestCredentials
                                     :sdkClientExecutionTimeout :dimensions
                                     :generalProgressListener :statistics :metricName
                                     :extendedStatistics :sdkRequestTimeout :period :namespace
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:startTime :unit :endTime :requestCredentials :sdkClientExecutionTimeout
                   :dimensions :generalProgressListener :statistics :metricName :extendedStatistics
                   :sdkRequestTimeout :period :namespace :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest.)]
        (when (contains? m :extendedStatistics)
          (let [v (:extendedStatistics m)
                k (d/property-to-object (class o) :extendedStatistics v java.util.Collection)]
            (.setExtendedStatistics
              ^com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest o
              ^java.util.Collection k)))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :unit)
          (let [v (:unit m)
                k (d/property-to-object
                    (class o)
                    :unit
                    v
                    com.amazonaws.services.cloudwatch.model.StandardUnit)]
            (.setUnit
              ^com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest o
              ^com.amazonaws.services.cloudwatch.model.StandardUnit k)))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)))
        (when (contains? m :metricName)
          (let [v (:metricName m)
                k (d/property-to-object (class o) :metricName v java.lang.String)]
            (.setMetricName
              ^com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest o
              ^java.lang.String k)))
        (when (contains? m :dimensions)
          (let [v (:dimensions m)
                k (d/property-to-object (class o) :dimensions v java.util.Collection)]
            (.setDimensions
              ^com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest o
              ^java.util.Collection k)))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)))
        (when (contains? m :startTime)
          (let [v (:startTime m) k (d/property-to-object (class o) :startTime v java.util.Date)]
            (.setStartTime
              ^com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest o
              ^java.util.Date k)))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :namespace)
          (let [v (:namespace m) k (d/property-to-object (class o) :namespace v java.lang.String)]
            (.setNamespace
              ^com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest o
              ^java.lang.String k)))
        (when (contains? m :endTime)
          (let [v (:endTime m) k (d/property-to-object (class o) :endTime v java.util.Date)]
            (.setEndTime
              ^com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest o
              ^java.util.Date k)))
        (when (contains? m :period)
          (let [v (:period m) k (d/property-to-object (class o) :period v java.lang.Integer)]
            (.setPeriod
              ^com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest o
              ^java.lang.Integer k)))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)))
        (when (contains? m :statistics)
          (let [v (:statistics m)
                k (d/property-to-object (class o) :statistics v java.util.Collection)]
            (.setStatistics
              ^com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest o
              ^java.util.Collection k)))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudwatch.model.ListMetricsRequest]
    fn__25901
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :owningAccount :sdkClientExecutionTimeout
                                     :dimensions :generalProgressListener :includeLinkedAccounts
                                     :metricName :recentlyActive :sdkRequestTimeout :nextToken
                                     :namespace :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :owningAccount :sdkClientExecutionTimeout :dimensions
                   :generalProgressListener :includeLinkedAccounts :metricName :recentlyActive
                   :sdkRequestTimeout :nextToken :namespace :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudwatch.model.ListMetricsRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudwatch.model.ListMetricsRequest.)]
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)))
        (when (contains? m :metricName)
          (let [v (:metricName m)
                k (d/property-to-object (class o) :metricName v java.lang.String)]
            (.setMetricName
              ^com.amazonaws.services.cloudwatch.model.ListMetricsRequest o
              ^java.lang.String k)))
        (when (contains? m :includeLinkedAccounts)
          (let [v (:includeLinkedAccounts m)
                k (d/property-to-object (class o) :includeLinkedAccounts v java.lang.Boolean)]
            (.setIncludeLinkedAccounts
              ^com.amazonaws.services.cloudwatch.model.ListMetricsRequest o
              ^java.lang.Boolean k)))
        (when (contains? m :dimensions)
          (let [v (:dimensions m)
                k (d/property-to-object (class o) :dimensions v java.util.Collection)]
            (.setDimensions
              ^com.amazonaws.services.cloudwatch.model.ListMetricsRequest o
              ^java.util.Collection k)))
        (when (contains? m :recentlyActive)
          (let [v (:recentlyActive m)
                k (d/property-to-object (class o) :recentlyActive v java.lang.String)]
            (.setRecentlyActive
              ^com.amazonaws.services.cloudwatch.model.ListMetricsRequest o
              ^java.lang.String k)))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :namespace)
          (let [v (:namespace m) k (d/property-to-object (class o) :namespace v java.lang.String)]
            (.setNamespace
              ^com.amazonaws.services.cloudwatch.model.ListMetricsRequest o
              ^java.lang.String k)))
        (when (contains? m :owningAccount)
          (let [v (:owningAccount m)
                k (d/property-to-object (class o) :owningAccount v java.lang.String)]
            (.setOwningAccount
              ^com.amazonaws.services.cloudwatch.model.ListMetricsRequest o
              ^java.lang.String k)))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)))
        (when (contains? m :nextToken)
          (let [v (:nextToken m) k (d/property-to-object (class o) :nextToken v java.lang.String)]
            (.setNextToken
              ^com.amazonaws.services.cloudwatch.model.ListMetricsRequest o
              ^java.lang.String k)))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudwatch.model.PutMetricDataRequest]
    fn__25904
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout :metricData
                                     :namespace :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :metricData :namespace :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudwatch.model.PutMetricDataRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudwatch.model.PutMetricDataRequest.)]
        (when (contains? m :namespace)
          (let [v (:namespace m) k (d/property-to-object (class o) :namespace v java.lang.String)]
            (.setNamespace
              ^com.amazonaws.services.cloudwatch.model.PutMetricDataRequest o
              ^java.lang.String k)))
        (when (contains? m :metricData)
          (let [v (:metricData m)
                k (d/property-to-object (class o) :metricData v java.util.Collection)]
            (.setMetricData
              ^com.amazonaws.services.cloudwatch.model.PutMetricDataRequest o
              ^java.util.Collection k)))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudwatch.model.MetricDatum]
    fn__25907
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:counts :unit :value :dimensions :storageResolution :values
                                     :metricName :timestamp :statisticValues}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:counts :unit :value :dimensions :storageResolution :values :metricName
                   :timestamp :statisticValues},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudwatch.model.MetricDatum}))))
        nil)
      (let [o (com.amazonaws.services.cloudwatch.model.MetricDatum.)]
        (when (contains? m :timestamp)
          (let [v (:timestamp m) k (d/property-to-object (class o) :timestamp v java.util.Date)]
            (.setTimestamp
              ^com.amazonaws.services.cloudwatch.model.MetricDatum o
              ^java.util.Date k)))
        (when (contains? m :unit)
          (let [v (:unit m)
                k (d/property-to-object
                    (class o)
                    :unit
                    v
                    com.amazonaws.services.cloudwatch.model.StandardUnit)]
            (.setUnit
              ^com.amazonaws.services.cloudwatch.model.MetricDatum o
              ^com.amazonaws.services.cloudwatch.model.StandardUnit k)))
        (when (contains? m :metricName)
          (let [v (:metricName m)
                k (d/property-to-object (class o) :metricName v java.lang.String)]
            (.setMetricName
              ^com.amazonaws.services.cloudwatch.model.MetricDatum o
              ^java.lang.String k)))
        (when (contains? m :statisticValues)
          (let [v (:statisticValues m)
                k (d/property-to-object
                    (class o)
                    :statisticValues
                    v
                    com.amazonaws.services.cloudwatch.model.StatisticSet)]
            (.setStatisticValues
              ^com.amazonaws.services.cloudwatch.model.MetricDatum o
              ^com.amazonaws.services.cloudwatch.model.StatisticSet k)))
        (when (contains? m :dimensions)
          (let [v (:dimensions m)
                k (d/property-to-object (class o) :dimensions v java.util.Collection)]
            (.setDimensions
              ^com.amazonaws.services.cloudwatch.model.MetricDatum o
              ^java.util.Collection k)))
        (when (contains? m :storageResolution)
          (let [v (:storageResolution m)
                k (d/property-to-object (class o) :storageResolution v java.lang.Integer)]
            (.setStorageResolution
              ^com.amazonaws.services.cloudwatch.model.MetricDatum o
              ^java.lang.Integer k)))
        (when (contains? m :counts)
          (let [v (:counts m) k (d/property-to-object (class o) :counts v java.util.Collection)]
            (.setCounts
              ^com.amazonaws.services.cloudwatch.model.MetricDatum o
              ^java.util.Collection k)))
        (when (contains? m :values)
          (let [v (:values m) k (d/property-to-object (class o) :values v java.util.Collection)]
            (.setValues
              ^com.amazonaws.services.cloudwatch.model.MetricDatum o
              ^java.util.Collection k)))
        (when (contains? m :value)
          (let [v (:value m) k (d/property-to-object (class o) :value v java.lang.Double)]
            (.setValue
              ^com.amazonaws.services.cloudwatch.model.MetricDatum o
              ^java.lang.Double k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudwatch.model.StatisticSet]
    fn__25910
    ([m _]
      (let [temp__5804__auto__ (seq (remove #{:maximum :minimum :sum :sampleCount} (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:maximum :minimum :sum :sampleCount},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudwatch.model.StatisticSet}))))
        nil)
      (let [o (com.amazonaws.services.cloudwatch.model.StatisticSet.)]
        (when (contains? m :maximum)
          (let [v (:maximum m) k (d/property-to-object (class o) :maximum v java.lang.Double)]
            (.setMaximum
              ^com.amazonaws.services.cloudwatch.model.StatisticSet o
              ^java.lang.Double k)))
        (when (contains? m :sampleCount)
          (let [v (:sampleCount m)
                k (d/property-to-object (class o) :sampleCount v java.lang.Double)]
            (.setSampleCount
              ^com.amazonaws.services.cloudwatch.model.StatisticSet o
              ^java.lang.Double k)))
        (when (contains? m :sum)
          (let [v (:sum m) k (d/property-to-object (class o) :sum v java.lang.Double)]
            (.setSum ^com.amazonaws.services.cloudwatch.model.StatisticSet o ^java.lang.Double k)))
        (when (contains? m :minimum)
          (let [v (:minimum m) k (d/property-to-object (class o) :minimum v java.lang.Double)]
            (.setMinimum
              ^com.amazonaws.services.cloudwatch.model.StatisticSet o
              ^java.lang.Double k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest]
    fn__25913
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:stateValue :alarmNames :alarmNamePrefix :alarmTypes
                                     :parentsOfAlarmName :requestCredentials :childrenOfAlarmName
                                     :sdkClientExecutionTimeout :generalProgressListener
                                     :actionPrefix :sdkRequestTimeout :maxRecords :nextToken
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:stateValue :alarmNames :alarmNamePrefix :alarmTypes :parentsOfAlarmName
                   :requestCredentials :childrenOfAlarmName :sdkClientExecutionTimeout
                   :generalProgressListener :actionPrefix :sdkRequestTimeout :maxRecords :nextToken
                   :requestMetricCollector :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest.)]
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)))
        (when (contains? m :actionPrefix)
          (let [v (:actionPrefix m)
                k (d/property-to-object (class o) :actionPrefix v java.lang.String)]
            (.setActionPrefix
              ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest o
              ^java.lang.String k)))
        (when (contains? m :childrenOfAlarmName)
          (let [v (:childrenOfAlarmName m)
                k (d/property-to-object (class o) :childrenOfAlarmName v java.lang.String)]
            (.setChildrenOfAlarmName
              ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest o
              ^java.lang.String k)))
        (when (contains? m :alarmTypes)
          (let [v (:alarmTypes m)
                k (d/property-to-object (class o) :alarmTypes v java.util.Collection)]
            (.setAlarmTypes
              ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest o
              ^java.util.Collection k)))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :alarmNames)
          (let [v (:alarmNames m)
                k (d/property-to-object (class o) :alarmNames v java.util.Collection)]
            (.setAlarmNames
              ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest o
              ^java.util.Collection k)))
        (when (contains? m :maxRecords)
          (let [v (:maxRecords m)
                k (d/property-to-object (class o) :maxRecords v java.lang.Integer)]
            (.setMaxRecords
              ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest o
              ^java.lang.Integer k)))
        (when (contains? m :alarmNamePrefix)
          (let [v (:alarmNamePrefix m)
                k (d/property-to-object (class o) :alarmNamePrefix v java.lang.String)]
            (.setAlarmNamePrefix
              ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest o
              ^java.lang.String k)))
        (when (contains? m :stateValue)
          (let [v (:stateValue m)
                k (d/property-to-object
                    (class o)
                    :stateValue
                    v
                    com.amazonaws.services.cloudwatch.model.StateValue)]
            (.setStateValue
              ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest o
              ^com.amazonaws.services.cloudwatch.model.StateValue k)))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)))
        (when (contains? m :nextToken)
          (let [v (:nextToken m) k (d/property-to-object (class o) :nextToken v java.lang.String)]
            (.setNextToken
              ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest o
              ^java.lang.String k)))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)))
        (when (contains? m :parentsOfAlarmName)
          (let [v (:parentsOfAlarmName m)
                k (d/property-to-object (class o) :parentsOfAlarmName v java.lang.String)]
            (.setParentsOfAlarmName
              ^com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest o
              ^java.lang.String k)))
        o)))
  (defmethod
    d/data-to-object
    [:atom com.amazonaws.services.cloudwatch.model.StateValue]
    fn__25916
    ([n _] (StateValue/valueOf (name n))))
  (defmethod
    d/data-to-object
    [:atom com.amazonaws.services.cloudwatch.model.StandardUnit]
    fn__25918
    ([n _] (StandardUnit/valueOf (name n))))
  (defn get-metric-statistics
    ([o x1]
      (d/object-to-data
        (.getMetricStatistics
          ^com.amazonaws.services.cloudwatch.AmazonCloudWatchClient o
          (d/data-to-object
            x1
            com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest)))))
  (reset-meta!
    #'get-metric-statistics
    (assoc
      {:related-class com.amazonaws.services.cloudwatch.AmazonCloudWatchClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.cloudwatch.AmazonCloudWatchClient}) 'x1]),
       :column 1}
      :name
      'get-metric-statistics
      :ns
      *ns*))
  (defn put-metrics
    ([o x1]
      (d/object-to-data
        (.putMetricData
          ^com.amazonaws.services.cloudwatch.AmazonCloudWatchClient o
          (d/data-to-object x1 com.amazonaws.services.cloudwatch.model.PutMetricDataRequest)))))
  (reset-meta!
    #'put-metrics
    (assoc
      {:related-class com.amazonaws.services.cloudwatch.AmazonCloudWatchClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.cloudwatch.AmazonCloudWatchClient}) 'x1]),
       :column 1}
      :name
      'put-metrics
      :ns
      *ns*))
  (defn describe-alarms
    ([o x1]
      (d/object-to-data
        (.describeAlarms
          ^com.amazonaws.services.cloudwatch.AmazonCloudWatchClient o
          (d/data-to-object x1 com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest)))))
  (reset-meta!
    #'describe-alarms
    (assoc
      {:related-class com.amazonaws.services.cloudwatch.AmazonCloudWatchClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.cloudwatch.AmazonCloudWatchClient}) 'x1]),
       :column 1}
      :name
      'describe-alarms
      :ns
      *ns*)))