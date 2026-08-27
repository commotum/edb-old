(do
  (clojure.core/in-ns 'datomic.ddb)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.datafy :as 'd]
        ['datomic.aws :as 'aws]
        ['clojure.edn :as 'edn]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ListTablesRequest)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ListTablesResult)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.CreateTableRequest)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.CreateTableResult)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.KeySchemaElement)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.KeyType)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ScalarAttributeType)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.PutItemRequest)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.AttributeValue)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.AttributeDefinition)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ReturnValue)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.PutItemResult)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.GetItemRequest)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.GetItemResult)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DeleteItemRequest)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DeleteItemResult)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DeleteTableRequest)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DeleteTableResult)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.TableDescription)
      (clojure.core/import
        'com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.UpdateTableRequest)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.UpdateTableResult)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DescribeTableRequest)
      (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DescribeTableResult)))
  (when-not (.equals 'datomic.ddb 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.ddb))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.datafy :as 'd]
          ['datomic.aws :as 'aws]
          ['clojure.edn :as 'edn]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ListTablesRequest)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ListTablesResult)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.CreateTableRequest)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.CreateTableResult)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.KeySchemaElement)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.KeyType)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ScalarAttributeType)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.PutItemRequest)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.AttributeValue)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.AttributeDefinition)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.ReturnValue)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.PutItemResult)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.GetItemRequest)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.GetItemResult)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DeleteItemRequest)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DeleteItemResult)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DeleteTableRequest)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DeleteTableResult)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.TableDescription)
        (clojure.core/import
          'com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.UpdateTableRequest)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.UpdateTableResult)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DescribeTableRequest)
        (clojure.core/import 'com.amazonaws.services.dynamodbv2.model.DescribeTableResult))))
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
                       (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient.
                         (aws/credentials creds)
                         ^com.amazonaws.ClientConfiguration conf)
                       (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient.
                         (aws/credentials creds)
                         ^com.amazonaws.ClientConfiguration conf))
                     (if (instance?
                           com.amazonaws.auth.AWSCredentialsProvider
                           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
                       (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient.
                         (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                         ^com.amazonaws.ClientConfiguration conf)
                       (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient.
                         (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                         ^com.amazonaws.ClientConfiguration conf)))]
          (cond
            override_endpoint (do
                                (.setEndpoint
                                  ^com.amazonaws.AmazonWebServiceClient conn
                                  (str "http://" override_endpoint))
                                nil)
            region (do
                     (.setEndpoint
                       ^com.amazonaws.AmazonWebServiceClient conn
                       (aws/endpoint-for :dynamodb region))
                     nil))
          conn)
        (client creds)))
    ([creds]
      (if creds
        (if (instance? com.amazonaws.auth.AWSCredentialsProvider (aws/credentials creds))
          (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient. (aws/credentials creds))
          (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient. (aws/credentials creds)))
        (if (instance?
              com.amazonaws.auth.AWSCredentialsProvider
              (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
          (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient.
            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
          (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient.
            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
    ([]
      (if (instance?
            com.amazonaws.auth.AWSCredentialsProvider
            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
        (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient.
          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
        (com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient.
          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
  (alter-var-root
    #'d/map-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.PutItemRequest :item]
    [java.lang.String com.amazonaws.services.dynamodbv2.model.AttributeValue])
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.PutItemRequest :item]
    fn__17405
    ([_ _ val _]
      (reduce
        (fn fn__17407
          ([m p__17406]
            (let [vec__17408 p__17406
                  k (nth vec__17408 (int 0) nil)
                  v (nth vec__17408 (int 1) nil)]
              (assoc
                m
                k
                (d/data-to-object v com.amazonaws.services.dynamodbv2.model.AttributeValue)))))
        {}
        val)))
  (alter-var-root
    #'d/map-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.PutItemRequest :expected]
    [java.lang.String com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue])
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.PutItemRequest :expected]
    fn__17413
    ([_ _ val _]
      (reduce
        (fn fn__17415
          ([m p__17414]
            (let [vec__17416 p__17414
                  k (nth vec__17416 (int 0) nil)
                  v (nth vec__17416 (int 1) nil)]
              (assoc
                m
                k
                (d/data-to-object
                  v
                  com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue)))))
        {}
        val)))
  (alter-var-root
    #'d/map-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.GetItemRequest :key]
    [java.lang.String com.amazonaws.services.dynamodbv2.model.AttributeValue])
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.GetItemRequest :key]
    fn__17421
    ([_ _ val _]
      (reduce
        (fn fn__17423
          ([m p__17422]
            (let [vec__17424 p__17422
                  k (nth vec__17424 (int 0) nil)
                  v (nth vec__17424 (int 1) nil)]
              (assoc
                m
                k
                (d/data-to-object v com.amazonaws.services.dynamodbv2.model.AttributeValue)))))
        {}
        val)))
  (alter-var-root
    #'d/map-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.GetItemResult :item]
    [java.lang.String com.amazonaws.services.dynamodbv2.model.AttributeValue])
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.GetItemResult :item]
    fn__17429
    ([_ _ val _]
      (reduce
        (fn fn__17431
          ([m p__17430]
            (let [vec__17432 p__17430
                  k (nth vec__17432 (int 0) nil)
                  v (nth vec__17432 (int 1) nil)]
              (assoc
                m
                k
                (d/data-to-object v com.amazonaws.services.dynamodbv2.model.AttributeValue)))))
        {}
        val)))
  (alter-var-root
    #'d/map-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.DeleteItemResult :attributes]
    [java.lang.String com.amazonaws.services.dynamodbv2.model.AttributeValue])
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.DeleteItemResult :attributes]
    fn__17437
    ([_ _ val _]
      (reduce
        (fn fn__17439
          ([m p__17438]
            (let [vec__17440 p__17438
                  k (nth vec__17440 (int 0) nil)
                  v (nth vec__17440 (int 1) nil)]
              (assoc
                m
                k
                (d/data-to-object v com.amazonaws.services.dynamodbv2.model.AttributeValue)))))
        {}
        val)))
  (alter-var-root
    #'d/map-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.DeleteItemRequest :expected]
    [java.lang.String com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue])
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.DeleteItemRequest :expected]
    fn__17445
    ([_ _ val _]
      (reduce
        (fn fn__17447
          ([m p__17446]
            (let [vec__17448 p__17446
                  k (nth vec__17448 (int 0) nil)
                  v (nth vec__17448 (int 1) nil)]
              (assoc
                m
                k
                (d/data-to-object
                  v
                  com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue)))))
        {}
        val)))
  (alter-var-root
    #'d/map-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.DeleteItemRequest :key]
    [java.lang.String com.amazonaws.services.dynamodbv2.model.AttributeValue])
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.DeleteItemRequest :key]
    fn__17453
    ([_ _ val _]
      (reduce
        (fn fn__17455
          ([m p__17454]
            (let [vec__17456 p__17454
                  k (nth vec__17456 (int 0) nil)
                  v (nth vec__17456 (int 1) nil)]
              (assoc
                m
                k
                (d/data-to-object v com.amazonaws.services.dynamodbv2.model.AttributeValue)))))
        {}
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.CreateTableRequest :keySchema]
    com.amazonaws.services.dynamodbv2.model.KeySchemaElement)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.CreateTableRequest :keySchema]
    fn__17461
    ([_ _ val _]
      (mapv
        (fn fn__17462
          ([item]
            (d/data-to-object item com.amazonaws.services.dynamodbv2.model.KeySchemaElement)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.CreateTableRequest :attributeDefinitions]
    com.amazonaws.services.dynamodbv2.model.AttributeDefinition)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.CreateTableRequest :attributeDefinitions]
    fn__17465
    ([_ _ val _]
      (mapv
        (fn fn__17466
          ([item]
            (d/data-to-object item com.amazonaws.services.dynamodbv2.model.AttributeDefinition)))
        val)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.UpdateTableRequest]
    fn__17469
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:provisionedThroughput :replicaUpdates :tableName
                                     :requestCredentials :sdkClientExecutionTimeout
                                     :deletionProtectionEnabled :generalProgressListener
                                     :billingMode :tableClass :globalSecondaryIndexUpdates
                                     :sdkRequestTimeout :streamSpecification
                                     :requestMetricCollector :sSESpecification
                                     :requestCredentialsProvider :attributeDefinitions}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:provisionedThroughput :replicaUpdates :tableName :requestCredentials
                   :sdkClientExecutionTimeout :deletionProtectionEnabled :generalProgressListener
                   :billingMode :tableClass :globalSecondaryIndexUpdates :sdkRequestTimeout
                   :streamSpecification :requestMetricCollector :sSESpecification
                   :requestCredentialsProvider :attributeDefinitions},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.UpdateTableRequest})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.UpdateTableRequest.)]
        (when (contains? m :streamSpecification)
          (let [v (:streamSpecification m)
                k (d/property-to-object
                    (class o)
                    :streamSpecification
                    v
                    com.amazonaws.services.dynamodbv2.model.StreamSpecification)]
            (.setStreamSpecification
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.StreamSpecification k)
            nil))
        (when (contains? m :deletionProtectionEnabled)
          (let [v (:deletionProtectionEnabled m)
                k (d/property-to-object (class o) :deletionProtectionEnabled v java.lang.Boolean)]
            (.setDeletionProtectionEnabled
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.lang.Boolean k)
            nil))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)
            nil))
        (when (contains? m :globalSecondaryIndexUpdates)
          (let [v (:globalSecondaryIndexUpdates m)
                k (d/property-to-object
                    (class o)
                    :globalSecondaryIndexUpdates
                    v
                    java.util.Collection)]
            (.setGlobalSecondaryIndexUpdates
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.util.Collection k)
            nil))
        (when (contains? m :replicaUpdates)
          (let [v (:replicaUpdates m)
                k (d/property-to-object (class o) :replicaUpdates v java.util.Collection)]
            (.setReplicaUpdates
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.util.Collection k)
            nil))
        (when (contains? m :provisionedThroughput)
          (let [v (:provisionedThroughput m)
                k (d/property-to-object
                    (class o)
                    :provisionedThroughput
                    v
                    com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput)]
            (.setProvisionedThroughput
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput k)
            nil))
        (when (contains? m :attributeDefinitions)
          (let [v (:attributeDefinitions m)
                k (d/property-to-object (class o) :attributeDefinitions v java.util.Collection)]
            (.setAttributeDefinitions
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.util.Collection k)
            nil))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)
            nil))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :tableClass)
          (let [v (:tableClass m)
                k (d/property-to-object (class o) :tableClass v java.lang.String)]
            (.setTableClass
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :billingMode)
          (let [v (:billingMode m)
                k (d/property-to-object (class o) :billingMode v java.lang.String)]
            (.setBillingMode
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)
            nil))
        (when (contains? m :sSESpecification)
          (let [v (:sSESpecification m)
                k (d/property-to-object
                    (class o)
                    :sSESpecification
                    v
                    com.amazonaws.services.dynamodbv2.model.SSESpecification)]
            (.setSSESpecification
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.SSESpecification k)
            nil))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.GetItemRequest]
    fn__17472
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:returnConsumedCapacity :projectionExpression :key :tableName
                                     :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :expressionAttributeNames
                                     :sdkRequestTimeout :requestMetricCollector
                                     :requestCredentialsProvider :consistentRead :attributesToGet}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:returnConsumedCapacity :projectionExpression :key :tableName
                   :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :expressionAttributeNames :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider :consistentRead :attributesToGet},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.GetItemRequest})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.GetItemRequest.)]
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)
            nil))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)
            nil))
        (when (contains? m :consistentRead)
          (let [v (:consistentRead m)
                k (d/property-to-object (class o) :consistentRead v java.lang.Boolean)]
            (.setConsistentRead
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.lang.Boolean k)
            nil))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :attributesToGet)
          (let [v (:attributesToGet m)
                k (d/property-to-object (class o) :attributesToGet v java.util.Collection)]
            (.setAttributesToGet
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.util.Collection k)
            nil))
        (when (contains? m :key)
          (let [v (:key m) k (d/property-to-object (class o) :key v java.util.Map)]
            (.setKey ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o ^java.util.Map k)
            nil))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)
            nil))
        (when (contains? m :expressionAttributeNames)
          (let [v (:expressionAttributeNames m)
                k (d/property-to-object (class o) :expressionAttributeNames v java.util.Map)]
            (.setExpressionAttributeNames
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.util.Map k)
            nil))
        (when (contains? m :projectionExpression)
          (let [v (:projectionExpression m)
                k (d/property-to-object (class o) :projectionExpression v java.lang.String)]
            (.setProjectionExpression
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :returnConsumedCapacity)
          (let [v (:returnConsumedCapacity m)
                k (d/property-to-object
                    (class o)
                    :returnConsumedCapacity
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity)]
            (.setReturnConsumedCapacity
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity k)
            nil))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.ListTablesRequest]
    fn__17475
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:limit :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :exclusiveStartTableName
                                     :sdkRequestTimeout :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:limit :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :exclusiveStartTableName :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.ListTablesRequest})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.ListTablesRequest.)]
        (when (contains? m :exclusiveStartTableName)
          (let [v (:exclusiveStartTableName m)
                k (d/property-to-object (class o) :exclusiveStartTableName v java.lang.String)]
            (.setExclusiveStartTableName
              ^com.amazonaws.services.dynamodbv2.model.ListTablesRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :limit)
          (let [v (:limit m) k (d/property-to-object (class o) :limit v java.lang.Integer)]
            (.setLimit
              ^com.amazonaws.services.dynamodbv2.model.ListTablesRequest o
              ^java.lang.Integer k)
            nil))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)
            nil))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)
            nil))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)
            nil))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)
            nil))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.CreateTableRequest]
    fn__17478
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:provisionedThroughput :tags :tableName :keySchema
                                     :requestCredentials :sdkClientExecutionTimeout
                                     :localSecondaryIndexes :deletionProtectionEnabled
                                     :generalProgressListener :billingMode :tableClass
                                     :sdkRequestTimeout :streamSpecification
                                     :requestMetricCollector :globalSecondaryIndexes
                                     :sSESpecification :requestCredentialsProvider
                                     :attributeDefinitions}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:provisionedThroughput :tags :tableName :keySchema :requestCredentials
                   :sdkClientExecutionTimeout :localSecondaryIndexes :deletionProtectionEnabled
                   :generalProgressListener :billingMode :tableClass :sdkRequestTimeout
                   :streamSpecification :requestMetricCollector :globalSecondaryIndexes
                   :sSESpecification :requestCredentialsProvider :attributeDefinitions},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.CreateTableRequest})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.CreateTableRequest.)]
        (when (contains? m :streamSpecification)
          (let [v (:streamSpecification m)
                k (d/property-to-object
                    (class o)
                    :streamSpecification
                    v
                    com.amazonaws.services.dynamodbv2.model.StreamSpecification)]
            (.setStreamSpecification
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.StreamSpecification k)
            nil))
        (when (contains? m :deletionProtectionEnabled)
          (let [v (:deletionProtectionEnabled m)
                k (d/property-to-object (class o) :deletionProtectionEnabled v java.lang.Boolean)]
            (.setDeletionProtectionEnabled
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.lang.Boolean k)
            nil))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :keySchema)
          (let [v (:keySchema m)
                k (d/property-to-object (class o) :keySchema v java.util.Collection)]
            (.setKeySchema
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.util.Collection k)
            nil))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)
            nil))
        (when (contains? m :provisionedThroughput)
          (let [v (:provisionedThroughput m)
                k (d/property-to-object
                    (class o)
                    :provisionedThroughput
                    v
                    com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput)]
            (.setProvisionedThroughput
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput k)
            nil))
        (when (contains? m :attributeDefinitions)
          (let [v (:attributeDefinitions m)
                k (d/property-to-object (class o) :attributeDefinitions v java.util.Collection)]
            (.setAttributeDefinitions
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.util.Collection k)
            nil))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)
            nil))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :tableClass)
          (let [v (:tableClass m)
                k (d/property-to-object (class o) :tableClass v java.lang.String)]
            (.setTableClass
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :localSecondaryIndexes)
          (let [v (:localSecondaryIndexes m)
                k (d/property-to-object (class o) :localSecondaryIndexes v java.util.Collection)]
            (.setLocalSecondaryIndexes
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.util.Collection k)
            nil))
        (when (contains? m :tags)
          (let [v (:tags m) k (d/property-to-object (class o) :tags v java.util.Collection)]
            (.setTags
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.util.Collection k)
            nil))
        (when (contains? m :billingMode)
          (let [v (:billingMode m)
                k (d/property-to-object (class o) :billingMode v java.lang.String)]
            (.setBillingMode
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :globalSecondaryIndexes)
          (let [v (:globalSecondaryIndexes m)
                k (d/property-to-object (class o) :globalSecondaryIndexes v java.util.Collection)]
            (.setGlobalSecondaryIndexes
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.util.Collection k)
            nil))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)
            nil))
        (when (contains? m :sSESpecification)
          (let [v (:sSESpecification m)
                k (d/property-to-object
                    (class o)
                    :sSESpecification
                    v
                    com.amazonaws.services.dynamodbv2.model.SSESpecification)]
            (.setSSESpecification
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.SSESpecification k)
            nil))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.PutItemRequest]
    fn__17481
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:returnConsumedCapacity :conditionalOperator
                                     :conditionExpression :tableName :item
                                     :expressionAttributeValues :requestCredentials
                                     :sdkClientExecutionTimeout :returnItemCollectionMetrics
                                     :returnValuesOnConditionCheckFailure :generalProgressListener
                                     :expressionAttributeNames :expected :sdkRequestTimeout
                                     :requestMetricCollector :returnValues
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:returnConsumedCapacity :conditionalOperator :conditionExpression :tableName
                   :item :expressionAttributeValues :requestCredentials :sdkClientExecutionTimeout
                   :returnItemCollectionMetrics :returnValuesOnConditionCheckFailure
                   :generalProgressListener :expressionAttributeNames :expected :sdkRequestTimeout
                   :requestMetricCollector :returnValues :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.PutItemRequest})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.PutItemRequest.)]
        (when (contains? m :returnValuesOnConditionCheckFailure)
          (let [v (:returnValuesOnConditionCheckFailure m)
                k (d/property-to-object
                    (class o)
                    :returnValuesOnConditionCheckFailure
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure)]
            (.setReturnValuesOnConditionCheckFailure
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure k)
            nil))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :conditionExpression)
          (let [v (:conditionExpression m)
                k (d/property-to-object (class o) :conditionExpression v java.lang.String)]
            (.setConditionExpression
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)
            nil))
        (when (contains? m :returnItemCollectionMetrics)
          (let [v (:returnItemCollectionMetrics m)
                k (d/property-to-object
                    (class o)
                    :returnItemCollectionMetrics
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnItemCollectionMetrics)]
            (.setReturnItemCollectionMetrics
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnItemCollectionMetrics k)
            nil))
        (when (contains? m :expected)
          (let [v (:expected m) k (d/property-to-object (class o) :expected v java.util.Map)]
            (.setExpected
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.util.Map k)
            nil))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)
            nil))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :conditionalOperator)
          (let [v (:conditionalOperator m)
                k (d/property-to-object
                    (class o)
                    :conditionalOperator
                    v
                    com.amazonaws.services.dynamodbv2.model.ConditionalOperator)]
            (.setConditionalOperator
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ConditionalOperator k)
            nil))
        (when (contains? m :item)
          (let [v (:item m) k (d/property-to-object (class o) :item v java.util.Map)]
            (.setItem ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o ^java.util.Map k)
            nil))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)
            nil))
        (when (contains? m :returnValues)
          (let [v (:returnValues m)
                k (d/property-to-object
                    (class o)
                    :returnValues
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnValue)]
            (.setReturnValues
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnValue k)
            nil))
        (when (contains? m :expressionAttributeNames)
          (let [v (:expressionAttributeNames m)
                k (d/property-to-object (class o) :expressionAttributeNames v java.util.Map)]
            (.setExpressionAttributeNames
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.util.Map k)
            nil))
        (when (contains? m :expressionAttributeValues)
          (let [v (:expressionAttributeValues m)
                k (d/property-to-object (class o) :expressionAttributeValues v java.util.Map)]
            (.setExpressionAttributeValues
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.util.Map k)
            nil))
        (when (contains? m :returnConsumedCapacity)
          (let [v (:returnConsumedCapacity m)
                k (d/property-to-object
                    (class o)
                    :returnConsumedCapacity
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity)]
            (.setReturnConsumedCapacity
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity k)
            nil))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue]
    fn__17484
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:exists :comparisonOperator :attributeValueList :value}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:exists :comparisonOperator :attributeValueList :value},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue.)]
        (when (contains? m :exists)
          (let [v (:exists m) k (d/property-to-object (class o) :exists v java.lang.Boolean)]
            (.setExists
              ^com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue o
              ^java.lang.Boolean k)
            nil))
        (when (contains? m :comparisonOperator)
          (let [v (:comparisonOperator m)
                k (d/property-to-object
                    (class o)
                    :comparisonOperator
                    v
                    com.amazonaws.services.dynamodbv2.model.ComparisonOperator)]
            (.setComparisonOperator
              ^com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue o
              ^com.amazonaws.services.dynamodbv2.model.ComparisonOperator k)
            nil))
        (when (contains? m :attributeValueList)
          (let [v (:attributeValueList m)
                k (d/property-to-object (class o) :attributeValueList v java.util.Collection)]
            (.setAttributeValueList
              ^com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue o
              ^java.util.Collection k)
            nil))
        (when (contains? m :value)
          (let [v (:value m)
                k (d/property-to-object
                    (class o)
                    :value
                    v
                    com.amazonaws.services.dynamodbv2.model.AttributeValue)]
            (.setValue
              ^com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue o
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.AttributeValue]
    fn__17487
    ([m _]
      (let [temp__5457__auto__ (seq (remove #{:bOOL :nS :n :m :s :l :nULL :bS :b :sS} (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:bOOL :nS :n :m :s :l :nULL :bS :b :sS},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.AttributeValue})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.AttributeValue.)]
        (when (contains? m :n)
          (let [v (:n m) k (d/property-to-object (class o) :n v java.lang.String)]
            (.setN ^com.amazonaws.services.dynamodbv2.model.AttributeValue o ^java.lang.String k)
            nil))
        (when (contains? m :bOOL)
          (let [v (:bOOL m) k (d/property-to-object (class o) :bOOL v java.lang.Boolean)]
            (.setBOOL
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.lang.Boolean k)
            nil))
        (when (contains? m :nS)
          (let [v (:nS m) k (d/property-to-object (class o) :nS v java.util.Collection)]
            (.setNS
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.util.Collection k)
            nil))
        (when (contains? m :b)
          (let [v (:b m) k (d/property-to-object (class o) :b v java.nio.ByteBuffer)]
            (.setB
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.nio.ByteBuffer k)
            nil))
        (when (contains? m :bS)
          (let [v (:bS m) k (d/property-to-object (class o) :bS v java.util.Collection)]
            (.setBS
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.util.Collection k)
            nil))
        (when (contains? m :nULL)
          (let [v (:nULL m) k (d/property-to-object (class o) :nULL v java.lang.Boolean)]
            (.setNULL
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.lang.Boolean k)
            nil))
        (when (contains? m :m)
          (let [v (:m m) k (d/property-to-object (class o) :m v java.util.Map)]
            (.setM ^com.amazonaws.services.dynamodbv2.model.AttributeValue o ^java.util.Map k)
            nil))
        (when (contains? m :l)
          (let [v (:l m) k (d/property-to-object (class o) :l v java.util.Collection)]
            (.setL
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.util.Collection k)
            nil))
        (when (contains? m :sS)
          (let [v (:sS m) k (d/property-to-object (class o) :sS v java.util.Collection)]
            (.setSS
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.util.Collection k)
            nil))
        (when (contains? m :s)
          (let [v (:s m) k (d/property-to-object (class o) :s v java.lang.String)]
            (.setS ^com.amazonaws.services.dynamodbv2.model.AttributeValue o ^java.lang.String k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.AttributeDefinition]
    fn__17490
    ([m _]
      (let [temp__5457__auto__ (seq (remove #{:attributeType :attributeName} (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:attributeType :attributeName},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.AttributeDefinition})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.AttributeDefinition.)]
        (when (contains? m :attributeName)
          (let [v (:attributeName m)
                k (d/property-to-object (class o) :attributeName v java.lang.String)]
            (.setAttributeName
              ^com.amazonaws.services.dynamodbv2.model.AttributeDefinition o
              ^java.lang.String k)
            nil))
        (when (contains? m :attributeType)
          (let [v (:attributeType m)
                k (d/property-to-object
                    (class o)
                    :attributeType
                    v
                    com.amazonaws.services.dynamodbv2.model.ScalarAttributeType)]
            (.setAttributeType
              ^com.amazonaws.services.dynamodbv2.model.AttributeDefinition o
              ^com.amazonaws.services.dynamodbv2.model.ScalarAttributeType k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:atom com.amazonaws.services.dynamodbv2.model.ReturnValue]
    fn__17493
    ([n _] (ReturnValue/valueOf (name n))))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput]
    fn__17495
    ([m _]
      (let [temp__5457__auto__ (seq (remove #{:readCapacityUnits :writeCapacityUnits} (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:readCapacityUnits :writeCapacityUnits},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput.)]
        (when (contains? m :readCapacityUnits)
          (let [v (:readCapacityUnits m)
                k (d/property-to-object (class o) :readCapacityUnits v java.lang.Long)]
            (.setReadCapacityUnits
              ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput o
              ^java.lang.Long k)
            nil))
        (when (contains? m :writeCapacityUnits)
          (let [v (:writeCapacityUnits m)
                k (d/property-to-object (class o) :writeCapacityUnits v java.lang.Long)]
            (.setWriteCapacityUnits
              ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput o
              ^java.lang.Long k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.KeySchemaElement]
    fn__17498
    ([m _]
      (let [temp__5457__auto__ (seq (remove #{:keyType :attributeName} (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:keyType :attributeName},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.KeySchemaElement})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.KeySchemaElement.)]
        (when (contains? m :attributeName)
          (let [v (:attributeName m)
                k (d/property-to-object (class o) :attributeName v java.lang.String)]
            (.setAttributeName
              ^com.amazonaws.services.dynamodbv2.model.KeySchemaElement o
              ^java.lang.String k)
            nil))
        (when (contains? m :keyType)
          (let [v (:keyType m)
                k (d/property-to-object
                    (class o)
                    :keyType
                    v
                    com.amazonaws.services.dynamodbv2.model.KeyType)]
            (.setKeyType
              ^com.amazonaws.services.dynamodbv2.model.KeySchemaElement o
              ^com.amazonaws.services.dynamodbv2.model.KeyType k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:atom com.amazonaws.services.dynamodbv2.model.ScalarAttributeType]
    fn__17501
    ([n _] (ScalarAttributeType/valueOf (name n))))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.DeleteItemRequest]
    fn__17503
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:returnConsumedCapacity :conditionalOperator :key
                                     :conditionExpression :tableName :expressionAttributeValues
                                     :requestCredentials :sdkClientExecutionTimeout
                                     :returnItemCollectionMetrics
                                     :returnValuesOnConditionCheckFailure :generalProgressListener
                                     :expressionAttributeNames :expected :sdkRequestTimeout
                                     :requestMetricCollector :returnValues
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:returnConsumedCapacity :conditionalOperator :key :conditionExpression
                   :tableName :expressionAttributeValues :requestCredentials
                   :sdkClientExecutionTimeout :returnItemCollectionMetrics
                   :returnValuesOnConditionCheckFailure :generalProgressListener
                   :expressionAttributeNames :expected :sdkRequestTimeout :requestMetricCollector
                   :returnValues :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.DeleteItemRequest})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.DeleteItemRequest.)]
        (when (contains? m :returnValuesOnConditionCheckFailure)
          (let [v (:returnValuesOnConditionCheckFailure m)
                k (d/property-to-object
                    (class o)
                    :returnValuesOnConditionCheckFailure
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure)]
            (.setReturnValuesOnConditionCheckFailure
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure k)
            nil))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :conditionExpression)
          (let [v (:conditionExpression m)
                k (d/property-to-object (class o) :conditionExpression v java.lang.String)]
            (.setConditionExpression
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)
            nil))
        (when (contains? m :returnItemCollectionMetrics)
          (let [v (:returnItemCollectionMetrics m)
                k (d/property-to-object
                    (class o)
                    :returnItemCollectionMetrics
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnItemCollectionMetrics)]
            (.setReturnItemCollectionMetrics
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnItemCollectionMetrics k)
            nil))
        (when (contains? m :expected)
          (let [v (:expected m) k (d/property-to-object (class o) :expected v java.util.Map)]
            (.setExpected
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.util.Map k)
            nil))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)
            nil))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :conditionalOperator)
          (let [v (:conditionalOperator m)
                k (d/property-to-object
                    (class o)
                    :conditionalOperator
                    v
                    com.amazonaws.services.dynamodbv2.model.ConditionalOperator)]
            (.setConditionalOperator
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ConditionalOperator k)
            nil))
        (when (contains? m :key)
          (let [v (:key m) k (d/property-to-object (class o) :key v java.util.Map)]
            (.setKey ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o ^java.util.Map k)
            nil))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)
            nil))
        (when (contains? m :returnValues)
          (let [v (:returnValues m)
                k (d/property-to-object
                    (class o)
                    :returnValues
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnValue)]
            (.setReturnValues
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnValue k)
            nil))
        (when (contains? m :expressionAttributeNames)
          (let [v (:expressionAttributeNames m)
                k (d/property-to-object (class o) :expressionAttributeNames v java.util.Map)]
            (.setExpressionAttributeNames
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.util.Map k)
            nil))
        (when (contains? m :expressionAttributeValues)
          (let [v (:expressionAttributeValues m)
                k (d/property-to-object (class o) :expressionAttributeValues v java.util.Map)]
            (.setExpressionAttributeValues
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.util.Map k)
            nil))
        (when (contains? m :returnConsumedCapacity)
          (let [v (:returnConsumedCapacity m)
                k (d/property-to-object
                    (class o)
                    :returnConsumedCapacity
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity)]
            (.setReturnConsumedCapacity
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity k)
            nil))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.DeleteTableRequest]
    fn__17506
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:tableName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:tableName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.DeleteTableRequest})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.DeleteTableRequest.)]
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.DeleteTableRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)
            nil))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)
            nil))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)
            nil))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)
            nil))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.DescribeTableRequest]
    fn__17509
    ([m _]
      (let [temp__5457__auto__ (seq
                                 (remove
                                   #{:tableName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5457__auto__
          (let [bad_ks temp__5457__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:tableName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.DescribeTableRequest})))))
      (let [o (com.amazonaws.services.dynamodbv2.model.DescribeTableRequest.)]
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.DescribeTableRequest o
              ^java.lang.String k)
            nil))
        (when (contains? m :requestCredentials)
          (let [v (:requestCredentials m)
                k (d/property-to-object
                    (class o)
                    :requestCredentials
                    v
                    com.amazonaws.auth.AWSCredentials)]
            (.setRequestCredentials
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentials k)
            nil))
        (when (contains? m :requestCredentialsProvider)
          (let [v (:requestCredentialsProvider m)
                k (d/property-to-object
                    (class o)
                    :requestCredentialsProvider
                    v
                    com.amazonaws.auth.AWSCredentialsProvider)]
            (.setRequestCredentialsProvider
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.auth.AWSCredentialsProvider k)
            nil))
        (when (contains? m :requestMetricCollector)
          (let [v (:requestMetricCollector m)
                k (d/property-to-object
                    (class o)
                    :requestMetricCollector
                    v
                    com.amazonaws.metrics.RequestMetricCollector)]
            (.setRequestMetricCollector
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.metrics.RequestMetricCollector k)
            nil))
        (when (contains? m :generalProgressListener)
          (let [v (:generalProgressListener m)
                k (d/property-to-object
                    (class o)
                    :generalProgressListener
                    v
                    com.amazonaws.event.ProgressListener)]
            (.setGeneralProgressListener
              ^com.amazonaws.AmazonWebServiceRequest o
              ^com.amazonaws.event.ProgressListener k)
            nil))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        (when (contains? m :sdkClientExecutionTimeout)
          (let [v (:sdkClientExecutionTimeout m)
                k (d/property-to-object
                    (class o)
                    :sdkClientExecutionTimeout
                    v
                    java.lang.Integer/TYPE)]
            (.setSdkClientExecutionTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))
            nil))
        o)))
  (extend
    com.amazonaws.ResponseMetadata
    d/ObjectToData
    {:object-to-data
     (fn fn__17512
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getRequestId ^com.amazonaws.ResponseMetadata o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:requestId (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.UpdateTableResult
    d/ObjectToData
    {:object-to-data
     (fn fn__17516
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getTableDescription
                                        ^com.amazonaws.services.dynamodbv2.model.UpdateTableResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableDescription (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkResponseMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkResponseMetadata (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkHttpMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkHttpMetadata (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.ListTablesResult
    d/ObjectToData
    {:object-to-data
     (fn fn__17524
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getTableNames
                                        ^com.amazonaws.services.dynamodbv2.model.ListTablesResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableNames (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getLastEvaluatedTableName
                                        ^com.amazonaws.services.dynamodbv2.model.ListTablesResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:lastEvaluatedTableName (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkResponseMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkResponseMetadata (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkHttpMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkHttpMetadata (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.PutItemResult
    d/ObjectToData
    {:object-to-data
     (fn fn__17534
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getConsumedCapacity
                                        ^com.amazonaws.services.dynamodbv2.model.PutItemResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:consumedCapacity (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getItemCollectionMetrics
                                        ^com.amazonaws.services.dynamodbv2.model.PutItemResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:itemCollectionMetrics (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getAttributes
                                        ^com.amazonaws.services.dynamodbv2.model.PutItemResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:attributes (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkResponseMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkResponseMetadata (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkHttpMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkHttpMetadata (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.GetItemResult
    d/ObjectToData
    {:object-to-data
     (fn fn__17546
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getItem
                                        ^com.amazonaws.services.dynamodbv2.model.GetItemResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:item (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getConsumedCapacity
                                        ^com.amazonaws.services.dynamodbv2.model.GetItemResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:consumedCapacity (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkResponseMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkResponseMetadata (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkHttpMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkHttpMetadata (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.AttributeValue
    d/ObjectToData
    {:object-to-data
     (fn fn__17556
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getB
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:b (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getM
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:m (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getS
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:s (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getN
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:n (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSS
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sS (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getNS
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:nS (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getBS
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:bS (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:l (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getNULL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:nULL (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.isNULL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:nULL (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getBOOL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:bOOL (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.isBOOL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:bOOL (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.AttributeDefinition
    d/ObjectToData
    {:object-to-data
     (fn fn__17582
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getAttributeName
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeDefinition o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:attributeName (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getAttributeType
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeDefinition o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:attributeType (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.DeleteItemResult
    d/ObjectToData
    {:object-to-data
     (fn fn__17588
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getConsumedCapacity
                                        ^com.amazonaws.services.dynamodbv2.model.DeleteItemResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:consumedCapacity (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getItemCollectionMetrics
                                        ^com.amazonaws.services.dynamodbv2.model.DeleteItemResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:itemCollectionMetrics (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getAttributes
                                        ^com.amazonaws.services.dynamodbv2.model.DeleteItemResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:attributes (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkResponseMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkResponseMetadata (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkHttpMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkHttpMetadata (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.DeleteTableResult
    d/ObjectToData
    {:object-to-data
     (fn fn__17600
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getTableDescription
                                        ^com.amazonaws.services.dynamodbv2.model.DeleteTableResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableDescription (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkResponseMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkResponseMetadata (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkHttpMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkHttpMetadata (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.DescribeTableResult
    d/ObjectToData
    {:object-to-data
     (fn fn__17608
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getTable
                                        ^com.amazonaws.services.dynamodbv2.model.DescribeTableResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:table (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkResponseMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkResponseMetadata (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkHttpMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkHttpMetadata (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.TableDescription
    d/ObjectToData
    {:object-to-data
     (fn fn__17616
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getItemCount
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:itemCount (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getTableName
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableName (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getTableStatus
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableStatus (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getCreationDateTime
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:creationDateTime (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getTableSizeBytes
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableSizeBytes (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getTableArn
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableArn (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getTableId
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableId (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getBillingModeSummary
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:billingModeSummary (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getLatestStreamLabel
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:latestStreamLabel (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getLatestStreamArn
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:latestStreamArn (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getGlobalTableVersion
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:globalTableVersion (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getReplicas
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:replicas (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getRestoreSummary
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:restoreSummary (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSSEDescription
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sSEDescription (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getArchivalSummary
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:archivalSummary (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getTableClassSummary
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableClassSummary (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getAttributeDefinitions
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:attributeDefinitions (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getKeySchema
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:keySchema (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getLocalSecondaryIndexes
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:localSecondaryIndexes (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getGlobalSecondaryIndexes
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:globalSecondaryIndexes (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getProvisionedThroughput
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:provisionedThroughput (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getStreamSpecification
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:streamSpecification (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getDeletionProtectionEnabled
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:deletionProtectionEnabled (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.isDeletionProtectionEnabled
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:deletionProtectionEnabled
                    (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription
    d/ObjectToData
    {:object-to-data
     (fn fn__17666
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getLastIncreaseDateTime
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:lastIncreaseDateTime (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getLastDecreaseDateTime
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:lastDecreaseDateTime (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getNumberOfDecreasesToday
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:numberOfDecreasesToday (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getReadCapacityUnits
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:readCapacityUnits (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getWriteCapacityUnits
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:writeCapacityUnits (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.CreateTableResult
    d/ObjectToData
    {:object-to-data
     (fn fn__17678
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getTableDescription
                                        ^com.amazonaws.services.dynamodbv2.model.CreateTableResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:tableDescription (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkResponseMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkResponseMetadata (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getSdkHttpMetadata
                                        ^com.amazonaws.AmazonWebServiceResult o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:sdkHttpMetadata (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.KeySchemaElement
    d/ObjectToData
    {:object-to-data
     (fn fn__17686
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5457__auto__ (.getKeyType
                                        ^com.amazonaws.services.dynamodbv2.model.KeySchemaElement o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:keyType (d/object-to-data-wrapper v__17285__auto__)])))
             (let [temp__5457__auto__ (.getAttributeName
                                        ^com.amazonaws.services.dynamodbv2.model.KeySchemaElement o)]
               (when temp__5457__auto__
                 (let [v__17285__auto__ temp__5457__auto__]
                   [:attributeName (d/object-to-data-wrapper v__17285__auto__)])))))))})
  (defn list-tables
    ([o x1]
      (d/object-to-data
        (.listTables
          ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
          (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.ListTablesRequest)))))
  (reset-meta!
    #'list-tables
    (assoc
      {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
       :column 1}
      :name
      'list-tables
      :ns
      *ns*))
  (defn create-table
    ([o x1]
      (d/object-to-data
        (.createTable
          ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
          (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.CreateTableRequest)))))
  (reset-meta!
    #'create-table
    (assoc
      {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
       :column 1}
      :name
      'create-table
      :ns
      *ns*))
  (defn update-table
    ([o x1]
      (d/object-to-data
        (.updateTable
          ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
          (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.UpdateTableRequest)))))
  (reset-meta!
    #'update-table
    (assoc
      {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
       :column 1}
      :name
      'update-table
      :ns
      *ns*))
  (defn delete-table
    ([o x1]
      (d/object-to-data
        (.deleteTable
          ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
          (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.DeleteTableRequest)))))
  (reset-meta!
    #'delete-table
    (assoc
      {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
       :column 1}
      :name
      'delete-table
      :ns
      *ns*))
  (defn describe-table
    ([o x1]
      (d/object-to-data
        (.describeTable
          ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
          (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.DescribeTableRequest)))))
  (reset-meta!
    #'describe-table
    (assoc
      {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
       :column 1}
      :name
      'describe-table
      :ns
      *ns*))
  (defn put-item*
    ([o x1]
      (d/object-to-data
        (.putItem
          ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
          (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.PutItemRequest)))))
  (reset-meta!
    #'put-item*
    (assoc
      {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
       :column 1}
      :name
      'put-item*
      :ns
      *ns*))
  (defn get-item*
    ([o x1]
      (d/object-to-data
        (.getItem
          ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
          (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.GetItemRequest)))))
  (reset-meta!
    #'get-item*
    (assoc
      {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
       :column 1}
      :name
      'get-item*
      :ns
      *ns*))
  (defn delete-item
    ([o x1]
      (d/object-to-data
        (.deleteItem
          ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
          (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.DeleteItemRequest)))))
  (reset-meta!
    #'delete-item
    (assoc
      {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
       :arglists
       (clojure.core/list
         [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
       :column 1}
      :name
      'delete-item
      :ns
      *ns*))
  (defn log-errors
    ([f & args]
      (try
        (apply f args)
        (catch
          com.amazonaws.AmazonServiceException
          ase
          (do
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb") ex ase]
              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                (.warn
                  ^org.slf4j.Logger logger
                  (logger/process
                    {:event :aws/error,
                     :exception
                     {:errorCode (.getErrorCode ^com.amazonaws.AmazonServiceException ase),
                      :serviceName (.getServiceName ^com.amazonaws.AmazonServiceException ase),
                      :statusCode
                      (java.lang.Integer/valueOf
                        (int (.getStatusCode ^com.amazonaws.AmazonServiceException ase)))},
                     :args (drop 1 args)})
                  ^java.lang.Throwable ex)
                (logger/caused-by logger ex))
              nil)
            (throw ^java.lang.Throwable ase)
            nil)))))
  (defn put-item ([ddb_client request] (put-item* ddb_client request)))
  (defn get-item ([ddb_client request] (get-item* ddb_client request)))
  (defn fullname ([s] (cond (keyword? s) (subs (str s) 1) :default (do (str s)))))
  (defn create-item
    ([m]
      (reduce
        (fn fn__17705
          ([m p__17704]
            (let [vec__17706 p__17704
                  k (nth vec__17706 (int 0) nil)
                  v (nth vec__17706 (int 1) nil)]
              (assoc m (fullname k) (if (number? v) {:n (str v)} {:s v})))))
        {}
        m)))
  (defn deattr
    ([m]
      (let [G__17711 (set (keys m))]
        (case
          G__17711
          #{:n}
          (edn/read-string (:n m))
          #{:s}
          (:s m)
          (do
            (throw (ex-info "Could not parse DDB item " m))
            (clojure.lang.Util/hash G__17711))))))
  (defn deitem
    ([m]
      (reduce
        (fn fn__17714
          ([m p__17713]
            (let [vec__17715 p__17713
                  k (nth vec__17715 (int 0) nil)
                  v (nth vec__17715 (int 1) nil)]
              (assoc m (keyword k) (deattr v)))))
        {}
        m)))
  (defn create-key ([k] {"id" {(if (number? k) :n :s) k}})))