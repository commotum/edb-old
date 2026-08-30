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
  (def client
   (fn client
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
             override_endpoint (.setEndpoint
                                 ^com.amazonaws.AmazonWebServiceClient conn
                                 (str "http://" override_endpoint))
             region (do
                      (.setEndpoint
                        ^com.amazonaws.AmazonWebServiceClient conn
                        (aws/endpoint-for :dynamodb region))))
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
           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))))))
  (reset-meta!
    #'client
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [] {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient})
         (.withMeta ['creds] {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient})
         (.withMeta
           ['creds 'config]
           {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient})),
       :column (int 1)}
      :name
      'client
      :ns
      *ns*))
  (alter-var-root
    #'d/map-property-types
    assoc
    [com.amazonaws.services.dynamodbv2.model.PutItemRequest :item]
    [java.lang.String com.amazonaws.services.dynamodbv2.model.AttributeValue])
  (defmethod
    d/property-to-object
    [com.amazonaws.services.dynamodbv2.model.PutItemRequest :item]
    fn__19529
    ([_ _ val _]
      (reduce
        (fn fn__19531
          ([m p__19530]
            (let [vec__19532 p__19530
                  k (nth vec__19532 (int 0) nil)
                  v (nth vec__19532 (int 1) nil)]
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
    fn__19537
    ([_ _ val _]
      (reduce
        (fn fn__19539
          ([m p__19538]
            (let [vec__19540 p__19538
                  k (nth vec__19540 (int 0) nil)
                  v (nth vec__19540 (int 1) nil)]
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
    fn__19545
    ([_ _ val _]
      (reduce
        (fn fn__19547
          ([m p__19546]
            (let [vec__19548 p__19546
                  k (nth vec__19548 (int 0) nil)
                  v (nth vec__19548 (int 1) nil)]
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
    fn__19553
    ([_ _ val _]
      (reduce
        (fn fn__19555
          ([m p__19554]
            (let [vec__19556 p__19554
                  k (nth vec__19556 (int 0) nil)
                  v (nth vec__19556 (int 1) nil)]
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
    fn__19561
    ([_ _ val _]
      (reduce
        (fn fn__19563
          ([m p__19562]
            (let [vec__19564 p__19562
                  k (nth vec__19564 (int 0) nil)
                  v (nth vec__19564 (int 1) nil)]
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
    fn__19569
    ([_ _ val _]
      (reduce
        (fn fn__19571
          ([m p__19570]
            (let [vec__19572 p__19570
                  k (nth vec__19572 (int 0) nil)
                  v (nth vec__19572 (int 1) nil)]
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
    fn__19577
    ([_ _ val _]
      (reduce
        (fn fn__19579
          ([m p__19578]
            (let [vec__19580 p__19578
                  k (nth vec__19580 (int 0) nil)
                  v (nth vec__19580 (int 1) nil)]
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
    fn__19585
    ([_ _ val _]
      (mapv
        (fn fn__19586
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
    fn__19589
    ([_ _ val _]
      (mapv
        (fn fn__19590
          ([item]
            (d/data-to-object item com.amazonaws.services.dynamodbv2.model.AttributeDefinition)))
        val)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.UpdateTableRequest]
    fn__19593
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:provisionedThroughput :replicaUpdates :tableName
                                     :requestCredentials :sdkClientExecutionTimeout
                                     :deletionProtectionEnabled :generalProgressListener
                                     :billingMode :tableClass :globalSecondaryIndexUpdates
                                     :sdkRequestTimeout :streamSpecification
                                     :requestMetricCollector :sSESpecification
                                     :requestCredentialsProvider :attributeDefinitions}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
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
                 :constructor com.amazonaws.services.dynamodbv2.model.UpdateTableRequest}))))
        nil)
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
              ^com.amazonaws.services.dynamodbv2.model.StreamSpecification k)))
        (when (contains? m :deletionProtectionEnabled)
          (let [v (:deletionProtectionEnabled m)
                k (d/property-to-object (class o) :deletionProtectionEnabled v java.lang.Boolean)]
            (.setDeletionProtectionEnabled
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.lang.Boolean k)))
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
        (when (contains? m :globalSecondaryIndexUpdates)
          (let [v (:globalSecondaryIndexUpdates m)
                k (d/property-to-object
                    (class o)
                    :globalSecondaryIndexUpdates
                    v
                    java.util.Collection)]
            (.setGlobalSecondaryIndexUpdates
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.util.Collection k)))
        (when (contains? m :replicaUpdates)
          (let [v (:replicaUpdates m)
                k (d/property-to-object (class o) :replicaUpdates v java.util.Collection)]
            (.setReplicaUpdates
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.util.Collection k)))
        (when (contains? m :provisionedThroughput)
          (let [v (:provisionedThroughput m)
                k (d/property-to-object
                    (class o)
                    :provisionedThroughput
                    v
                    com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput)]
            (.setProvisionedThroughput
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput k)))
        (when (contains? m :attributeDefinitions)
          (let [v (:attributeDefinitions m)
                k (d/property-to-object (class o) :attributeDefinitions v java.util.Collection)]
            (.setAttributeDefinitions
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
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
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.lang.String k)))
        (when (contains? m :tableClass)
          (let [v (:tableClass m)
                k (d/property-to-object (class o) :tableClass v java.lang.String)]
            (.setTableClass
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^java.lang.String k)))
        (when (contains? m :billingMode)
          (let [v (:billingMode m)
                k (d/property-to-object (class o) :billingMode v java.lang.String)]
            (.setBillingMode
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
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
        (when (contains? m :sSESpecification)
          (let [v (:sSESpecification m)
                k (d/property-to-object
                    (class o)
                    :sSESpecification
                    v
                    com.amazonaws.services.dynamodbv2.model.SSESpecification)]
            (.setSSESpecification
              ^com.amazonaws.services.dynamodbv2.model.UpdateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.SSESpecification k)))
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
    [:map com.amazonaws.services.dynamodbv2.model.GetItemRequest]
    fn__19596
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:returnConsumedCapacity :projectionExpression :key :tableName
                                     :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :expressionAttributeNames
                                     :sdkRequestTimeout :requestMetricCollector
                                     :requestCredentialsProvider :consistentRead :attributesToGet}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:returnConsumedCapacity :projectionExpression :key :tableName
                   :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :expressionAttributeNames :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider :consistentRead :attributesToGet},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.GetItemRequest}))))
        nil)
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
        (when (contains? m :consistentRead)
          (let [v (:consistentRead m)
                k (d/property-to-object (class o) :consistentRead v java.lang.Boolean)]
            (.setConsistentRead
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.lang.Boolean k)))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.lang.String k)))
        (when (contains? m :attributesToGet)
          (let [v (:attributesToGet m)
                k (d/property-to-object (class o) :attributesToGet v java.util.Collection)]
            (.setAttributesToGet
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.util.Collection k)))
        (when (contains? m :key)
          (let [v (:key m) k (d/property-to-object (class o) :key v java.util.Map)]
            (.setKey ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o ^java.util.Map k)))
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
        (when (contains? m :expressionAttributeNames)
          (let [v (:expressionAttributeNames m)
                k (d/property-to-object (class o) :expressionAttributeNames v java.util.Map)]
            (.setExpressionAttributeNames
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.util.Map k)))
        (when (contains? m :projectionExpression)
          (let [v (:projectionExpression m)
                k (d/property-to-object (class o) :projectionExpression v java.lang.String)]
            (.setProjectionExpression
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^java.lang.String k)))
        (when (contains? m :returnConsumedCapacity)
          (let [v (:returnConsumedCapacity m)
                k (d/property-to-object
                    (class o)
                    :returnConsumedCapacity
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity)]
            (.setReturnConsumedCapacity
              ^com.amazonaws.services.dynamodbv2.model.GetItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity k)))
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
    [:map com.amazonaws.services.dynamodbv2.model.ListTablesRequest]
    fn__19599
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:limit :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :exclusiveStartTableName
                                     :sdkRequestTimeout :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:limit :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :exclusiveStartTableName :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.ListTablesRequest}))))
        nil)
      (let [o (com.amazonaws.services.dynamodbv2.model.ListTablesRequest.)]
        (when (contains? m :exclusiveStartTableName)
          (let [v (:exclusiveStartTableName m)
                k (d/property-to-object (class o) :exclusiveStartTableName v java.lang.String)]
            (.setExclusiveStartTableName
              ^com.amazonaws.services.dynamodbv2.model.ListTablesRequest o
              ^java.lang.String k)))
        (when (contains? m :limit)
          (let [v (:limit m) k (d/property-to-object (class o) :limit v java.lang.Integer)]
            (.setLimit
              ^com.amazonaws.services.dynamodbv2.model.ListTablesRequest o
              ^java.lang.Integer k)))
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
    [:map com.amazonaws.services.dynamodbv2.model.CreateTableRequest]
    fn__19602
    ([m _]
      (let [temp__5804__auto__ (seq
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
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
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
                 :constructor com.amazonaws.services.dynamodbv2.model.CreateTableRequest}))))
        nil)
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
              ^com.amazonaws.services.dynamodbv2.model.StreamSpecification k)))
        (when (contains? m :deletionProtectionEnabled)
          (let [v (:deletionProtectionEnabled m)
                k (d/property-to-object (class o) :deletionProtectionEnabled v java.lang.Boolean)]
            (.setDeletionProtectionEnabled
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.lang.Boolean k)))
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
        (when (contains? m :keySchema)
          (let [v (:keySchema m)
                k (d/property-to-object (class o) :keySchema v java.util.Collection)]
            (.setKeySchema
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.util.Collection k)))
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
        (when (contains? m :provisionedThroughput)
          (let [v (:provisionedThroughput m)
                k (d/property-to-object
                    (class o)
                    :provisionedThroughput
                    v
                    com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput)]
            (.setProvisionedThroughput
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput k)))
        (when (contains? m :attributeDefinitions)
          (let [v (:attributeDefinitions m)
                k (d/property-to-object (class o) :attributeDefinitions v java.util.Collection)]
            (.setAttributeDefinitions
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
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
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.lang.String k)))
        (when (contains? m :tableClass)
          (let [v (:tableClass m)
                k (d/property-to-object (class o) :tableClass v java.lang.String)]
            (.setTableClass
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.lang.String k)))
        (when (contains? m :localSecondaryIndexes)
          (let [v (:localSecondaryIndexes m)
                k (d/property-to-object (class o) :localSecondaryIndexes v java.util.Collection)]
            (.setLocalSecondaryIndexes
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.util.Collection k)))
        (when (contains? m :tags)
          (let [v (:tags m) k (d/property-to-object (class o) :tags v java.util.Collection)]
            (.setTags
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.util.Collection k)))
        (when (contains? m :billingMode)
          (let [v (:billingMode m)
                k (d/property-to-object (class o) :billingMode v java.lang.String)]
            (.setBillingMode
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.lang.String k)))
        (when (contains? m :globalSecondaryIndexes)
          (let [v (:globalSecondaryIndexes m)
                k (d/property-to-object (class o) :globalSecondaryIndexes v java.util.Collection)]
            (.setGlobalSecondaryIndexes
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^java.util.Collection k)))
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
        (when (contains? m :sSESpecification)
          (let [v (:sSESpecification m)
                k (d/property-to-object
                    (class o)
                    :sSESpecification
                    v
                    com.amazonaws.services.dynamodbv2.model.SSESpecification)]
            (.setSSESpecification
              ^com.amazonaws.services.dynamodbv2.model.CreateTableRequest o
              ^com.amazonaws.services.dynamodbv2.model.SSESpecification k)))
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
    [:map com.amazonaws.services.dynamodbv2.model.PutItemRequest]
    fn__19605
    ([m _]
      (let [temp__5804__auto__ (seq
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
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
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
                 :constructor com.amazonaws.services.dynamodbv2.model.PutItemRequest}))))
        nil)
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
              ^com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure k)))
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
        (when (contains? m :conditionExpression)
          (let [v (:conditionExpression m)
                k (d/property-to-object (class o) :conditionExpression v java.lang.String)]
            (.setConditionExpression
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.lang.String k)))
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
        (when (contains? m :returnItemCollectionMetrics)
          (let [v (:returnItemCollectionMetrics m)
                k (d/property-to-object
                    (class o)
                    :returnItemCollectionMetrics
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnItemCollectionMetrics)]
            (.setReturnItemCollectionMetrics
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnItemCollectionMetrics k)))
        (when (contains? m :expected)
          (let [v (:expected m) k (d/property-to-object (class o) :expected v java.util.Map)]
            (.setExpected
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.util.Map k)))
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
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.lang.String k)))
        (when (contains? m :conditionalOperator)
          (let [v (:conditionalOperator m)
                k (d/property-to-object
                    (class o)
                    :conditionalOperator
                    v
                    com.amazonaws.services.dynamodbv2.model.ConditionalOperator)]
            (.setConditionalOperator
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ConditionalOperator k)))
        (when (contains? m :item)
          (let [v (:item m) k (d/property-to-object (class o) :item v java.util.Map)]
            (.setItem ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o ^java.util.Map k)))
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
        (when (contains? m :returnValues)
          (let [v (:returnValues m)
                k (d/property-to-object
                    (class o)
                    :returnValues
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnValue)]
            (.setReturnValues
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnValue k)))
        (when (contains? m :expressionAttributeNames)
          (let [v (:expressionAttributeNames m)
                k (d/property-to-object (class o) :expressionAttributeNames v java.util.Map)]
            (.setExpressionAttributeNames
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.util.Map k)))
        (when (contains? m :expressionAttributeValues)
          (let [v (:expressionAttributeValues m)
                k (d/property-to-object (class o) :expressionAttributeValues v java.util.Map)]
            (.setExpressionAttributeValues
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^java.util.Map k)))
        (when (contains? m :returnConsumedCapacity)
          (let [v (:returnConsumedCapacity m)
                k (d/property-to-object
                    (class o)
                    :returnConsumedCapacity
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity)]
            (.setReturnConsumedCapacity
              ^com.amazonaws.services.dynamodbv2.model.PutItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity k)))
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
    [:map com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue]
    fn__19608
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:exists :comparisonOperator :attributeValueList :value}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:exists :comparisonOperator :attributeValueList :value},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue}))))
        nil)
      (let [o (com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue.)]
        (when (contains? m :exists)
          (let [v (:exists m) k (d/property-to-object (class o) :exists v java.lang.Boolean)]
            (.setExists
              ^com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue o
              ^java.lang.Boolean k)))
        (when (contains? m :comparisonOperator)
          (let [v (:comparisonOperator m)
                k (d/property-to-object
                    (class o)
                    :comparisonOperator
                    v
                    com.amazonaws.services.dynamodbv2.model.ComparisonOperator)]
            (.setComparisonOperator
              ^com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue o
              ^com.amazonaws.services.dynamodbv2.model.ComparisonOperator k)))
        (when (contains? m :attributeValueList)
          (let [v (:attributeValueList m)
                k (d/property-to-object (class o) :attributeValueList v java.util.Collection)]
            (.setAttributeValueList
              ^com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue o
              ^java.util.Collection k)))
        (when (contains? m :value)
          (let [v (:value m)
                k (d/property-to-object
                    (class o)
                    :value
                    v
                    com.amazonaws.services.dynamodbv2.model.AttributeValue)]
            (.setValue
              ^com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue o
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.AttributeValue]
    fn__19611
    ([m _]
      (let [temp__5804__auto__ (seq (remove #{:bOOL :nS :n :m :s :l :nULL :bS :b :sS} (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:bOOL :nS :n :m :s :l :nULL :bS :b :sS},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.AttributeValue}))))
        nil)
      (let [o (com.amazonaws.services.dynamodbv2.model.AttributeValue.)]
        (when (contains? m :n)
          (let [v (:n m) k (d/property-to-object (class o) :n v java.lang.String)]
            (.setN ^com.amazonaws.services.dynamodbv2.model.AttributeValue o ^java.lang.String k)))
        (when (contains? m :bOOL)
          (let [v (:bOOL m) k (d/property-to-object (class o) :bOOL v java.lang.Boolean)]
            (.setBOOL
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.lang.Boolean k)))
        (when (contains? m :nS)
          (let [v (:nS m) k (d/property-to-object (class o) :nS v java.util.Collection)]
            (.setNS
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.util.Collection k)))
        (when (contains? m :b)
          (let [v (:b m) k (d/property-to-object (class o) :b v java.nio.ByteBuffer)]
            (.setB
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.nio.ByteBuffer k)))
        (when (contains? m :bS)
          (let [v (:bS m) k (d/property-to-object (class o) :bS v java.util.Collection)]
            (.setBS
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.util.Collection k)))
        (when (contains? m :nULL)
          (let [v (:nULL m) k (d/property-to-object (class o) :nULL v java.lang.Boolean)]
            (.setNULL
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.lang.Boolean k)))
        (when (contains? m :m)
          (let [v (:m m) k (d/property-to-object (class o) :m v java.util.Map)]
            (.setM ^com.amazonaws.services.dynamodbv2.model.AttributeValue o ^java.util.Map k)))
        (when (contains? m :l)
          (let [v (:l m) k (d/property-to-object (class o) :l v java.util.Collection)]
            (.setL
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.util.Collection k)))
        (when (contains? m :sS)
          (let [v (:sS m) k (d/property-to-object (class o) :sS v java.util.Collection)]
            (.setSS
              ^com.amazonaws.services.dynamodbv2.model.AttributeValue o
              ^java.util.Collection k)))
        (when (contains? m :s)
          (let [v (:s m) k (d/property-to-object (class o) :s v java.lang.String)]
            (.setS ^com.amazonaws.services.dynamodbv2.model.AttributeValue o ^java.lang.String k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.AttributeDefinition]
    fn__19614
    ([m _]
      (let [temp__5804__auto__ (seq (remove #{:attributeType :attributeName} (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:attributeType :attributeName},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.AttributeDefinition}))))
        nil)
      (let [o (com.amazonaws.services.dynamodbv2.model.AttributeDefinition.)]
        (when (contains? m :attributeName)
          (let [v (:attributeName m)
                k (d/property-to-object (class o) :attributeName v java.lang.String)]
            (.setAttributeName
              ^com.amazonaws.services.dynamodbv2.model.AttributeDefinition o
              ^java.lang.String k)))
        (when (contains? m :attributeType)
          (let [v (:attributeType m)
                k (d/property-to-object
                    (class o)
                    :attributeType
                    v
                    com.amazonaws.services.dynamodbv2.model.ScalarAttributeType)]
            (.setAttributeType
              ^com.amazonaws.services.dynamodbv2.model.AttributeDefinition o
              ^com.amazonaws.services.dynamodbv2.model.ScalarAttributeType k)))
        o)))
  (defmethod
    d/data-to-object
    [:atom com.amazonaws.services.dynamodbv2.model.ReturnValue]
    fn__19617
    ([n _] (ReturnValue/valueOf (name n))))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput]
    fn__19619
    ([m _]
      (let [temp__5804__auto__ (seq (remove #{:readCapacityUnits :writeCapacityUnits} (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:readCapacityUnits :writeCapacityUnits},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput}))))
        nil)
      (let [o (com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput.)]
        (when (contains? m :readCapacityUnits)
          (let [v (:readCapacityUnits m)
                k (d/property-to-object (class o) :readCapacityUnits v java.lang.Long)]
            (.setReadCapacityUnits
              ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput o
              ^java.lang.Long k)))
        (when (contains? m :writeCapacityUnits)
          (let [v (:writeCapacityUnits m)
                k (d/property-to-object (class o) :writeCapacityUnits v java.lang.Long)]
            (.setWriteCapacityUnits
              ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput o
              ^java.lang.Long k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.KeySchemaElement]
    fn__19622
    ([m _]
      (let [temp__5804__auto__ (seq (remove #{:keyType :attributeName} (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:keyType :attributeName},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.KeySchemaElement}))))
        nil)
      (let [o (com.amazonaws.services.dynamodbv2.model.KeySchemaElement.)]
        (when (contains? m :attributeName)
          (let [v (:attributeName m)
                k (d/property-to-object (class o) :attributeName v java.lang.String)]
            (.setAttributeName
              ^com.amazonaws.services.dynamodbv2.model.KeySchemaElement o
              ^java.lang.String k)))
        (when (contains? m :keyType)
          (let [v (:keyType m)
                k (d/property-to-object
                    (class o)
                    :keyType
                    v
                    com.amazonaws.services.dynamodbv2.model.KeyType)]
            (.setKeyType
              ^com.amazonaws.services.dynamodbv2.model.KeySchemaElement o
              ^com.amazonaws.services.dynamodbv2.model.KeyType k)))
        o)))
  (defmethod
    d/data-to-object
    [:atom com.amazonaws.services.dynamodbv2.model.ScalarAttributeType]
    fn__19625
    ([n _] (ScalarAttributeType/valueOf (name n))))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.dynamodbv2.model.DeleteItemRequest]
    fn__19627
    ([m _]
      (let [temp__5804__auto__ (seq
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
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
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
                 :constructor com.amazonaws.services.dynamodbv2.model.DeleteItemRequest}))))
        nil)
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
              ^com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure k)))
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
        (when (contains? m :conditionExpression)
          (let [v (:conditionExpression m)
                k (d/property-to-object (class o) :conditionExpression v java.lang.String)]
            (.setConditionExpression
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.lang.String k)))
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
        (when (contains? m :returnItemCollectionMetrics)
          (let [v (:returnItemCollectionMetrics m)
                k (d/property-to-object
                    (class o)
                    :returnItemCollectionMetrics
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnItemCollectionMetrics)]
            (.setReturnItemCollectionMetrics
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnItemCollectionMetrics k)))
        (when (contains? m :expected)
          (let [v (:expected m) k (d/property-to-object (class o) :expected v java.util.Map)]
            (.setExpected
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.util.Map k)))
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
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.lang.String k)))
        (when (contains? m :conditionalOperator)
          (let [v (:conditionalOperator m)
                k (d/property-to-object
                    (class o)
                    :conditionalOperator
                    v
                    com.amazonaws.services.dynamodbv2.model.ConditionalOperator)]
            (.setConditionalOperator
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ConditionalOperator k)))
        (when (contains? m :key)
          (let [v (:key m) k (d/property-to-object (class o) :key v java.util.Map)]
            (.setKey
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.util.Map k)))
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
        (when (contains? m :returnValues)
          (let [v (:returnValues m)
                k (d/property-to-object
                    (class o)
                    :returnValues
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnValue)]
            (.setReturnValues
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnValue k)))
        (when (contains? m :expressionAttributeNames)
          (let [v (:expressionAttributeNames m)
                k (d/property-to-object (class o) :expressionAttributeNames v java.util.Map)]
            (.setExpressionAttributeNames
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.util.Map k)))
        (when (contains? m :expressionAttributeValues)
          (let [v (:expressionAttributeValues m)
                k (d/property-to-object (class o) :expressionAttributeValues v java.util.Map)]
            (.setExpressionAttributeValues
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^java.util.Map k)))
        (when (contains? m :returnConsumedCapacity)
          (let [v (:returnConsumedCapacity m)
                k (d/property-to-object
                    (class o)
                    :returnConsumedCapacity
                    v
                    com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity)]
            (.setReturnConsumedCapacity
              ^com.amazonaws.services.dynamodbv2.model.DeleteItemRequest o
              ^com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity k)))
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
    [:map com.amazonaws.services.dynamodbv2.model.DeleteTableRequest]
    fn__19630
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:tableName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:tableName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.DeleteTableRequest}))))
        nil)
      (let [o (com.amazonaws.services.dynamodbv2.model.DeleteTableRequest.)]
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.DeleteTableRequest o
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
    [:map com.amazonaws.services.dynamodbv2.model.DescribeTableRequest]
    fn__19633
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:tableName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:tableName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.dynamodbv2.model.DescribeTableRequest}))))
        nil)
      (let [o (com.amazonaws.services.dynamodbv2.model.DescribeTableRequest.)]
        (when (contains? m :tableName)
          (let [v (:tableName m) k (d/property-to-object (class o) :tableName v java.lang.String)]
            (.setTableName
              ^com.amazonaws.services.dynamodbv2.model.DescribeTableRequest o
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
  (extend
    com.amazonaws.ResponseMetadata
    d/ObjectToData
    {:object-to-data
     (fn fn__19636
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getRequestId ^com.amazonaws.ResponseMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requestId (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.UpdateTableResult
    d/ObjectToData
    {:object-to-data
     (fn fn__19640
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTableDescription
                                        ^com.amazonaws.services.dynamodbv2.model.UpdateTableResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableDescription (d/object-to-data-wrapper v__19409__auto__)])))
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
  (extend
    com.amazonaws.services.dynamodbv2.model.ListTablesResult
    d/ObjectToData
    {:object-to-data
     (fn fn__19648
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTableNames
                                        ^com.amazonaws.services.dynamodbv2.model.ListTablesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableNames (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLastEvaluatedTableName
                                        ^com.amazonaws.services.dynamodbv2.model.ListTablesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:lastEvaluatedTableName (d/object-to-data-wrapper v__19409__auto__)])))
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
  (extend
    com.amazonaws.services.dynamodbv2.model.PutItemResult
    d/ObjectToData
    {:object-to-data
     (fn fn__19658
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getItemCollectionMetrics
                                        ^com.amazonaws.services.dynamodbv2.model.PutItemResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:itemCollectionMetrics (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getConsumedCapacity
                                        ^com.amazonaws.services.dynamodbv2.model.PutItemResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:consumedCapacity (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAttributes
                                        ^com.amazonaws.services.dynamodbv2.model.PutItemResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:attributes (d/object-to-data-wrapper v__19409__auto__)])))
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
  (extend
    com.amazonaws.services.dynamodbv2.model.GetItemResult
    d/ObjectToData
    {:object-to-data
     (fn fn__19670
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getItem
                                        ^com.amazonaws.services.dynamodbv2.model.GetItemResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:item (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getConsumedCapacity
                                        ^com.amazonaws.services.dynamodbv2.model.GetItemResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:consumedCapacity (d/object-to-data-wrapper v__19409__auto__)])))
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
  (extend
    com.amazonaws.services.dynamodbv2.model.AttributeValue
    d/ObjectToData
    {:object-to-data
     (fn fn__19680
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getB
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:b (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getM
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:m (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getS
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:s (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getN
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:n (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSS
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sS (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getNS
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:nS (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getBS
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:bS (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:l (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getNULL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:nULL (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isNULL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:nULL (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getBOOL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:bOOL (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isBOOL
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeValue o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:bOOL (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.AttributeDefinition
    d/ObjectToData
    {:object-to-data
     (fn fn__19706
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getAttributeName
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeDefinition o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:attributeName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAttributeType
                                        ^com.amazonaws.services.dynamodbv2.model.AttributeDefinition o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:attributeType (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.DeleteItemResult
    d/ObjectToData
    {:object-to-data
     (fn fn__19712
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getItemCollectionMetrics
                                        ^com.amazonaws.services.dynamodbv2.model.DeleteItemResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:itemCollectionMetrics (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getConsumedCapacity
                                        ^com.amazonaws.services.dynamodbv2.model.DeleteItemResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:consumedCapacity (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAttributes
                                        ^com.amazonaws.services.dynamodbv2.model.DeleteItemResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:attributes (d/object-to-data-wrapper v__19409__auto__)])))
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
  (extend
    com.amazonaws.services.dynamodbv2.model.DeleteTableResult
    d/ObjectToData
    {:object-to-data
     (fn fn__19724
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTableDescription
                                        ^com.amazonaws.services.dynamodbv2.model.DeleteTableResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableDescription (d/object-to-data-wrapper v__19409__auto__)])))
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
  (extend
    com.amazonaws.services.dynamodbv2.model.DescribeTableResult
    d/ObjectToData
    {:object-to-data
     (fn fn__19732
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTable
                                        ^com.amazonaws.services.dynamodbv2.model.DescribeTableResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:table (d/object-to-data-wrapper v__19409__auto__)])))
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
  (extend
    com.amazonaws.services.dynamodbv2.model.TableDescription
    d/ObjectToData
    {:object-to-data
     (fn fn__19740
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getItemCount
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:itemCount (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getTableName
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getTableStatus
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCreationDateTime
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:creationDateTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getTableSizeBytes
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableSizeBytes (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getTableArn
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableArn (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getTableId
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getBillingModeSummary
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:billingModeSummary (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLatestStreamLabel
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:latestStreamLabel (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLatestStreamArn
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:latestStreamArn (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGlobalTableVersion
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:globalTableVersion (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getReplicas
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:replicas (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRestoreSummary
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:restoreSummary (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSSEDescription
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sSEDescription (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getArchivalSummary
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:archivalSummary (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getTableClassSummary
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableClassSummary (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAttributeDefinitions
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:attributeDefinitions (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKeySchema
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:keySchema (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLocalSecondaryIndexes
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:localSecondaryIndexes (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGlobalSecondaryIndexes
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:globalSecondaryIndexes (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getProvisionedThroughput
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:provisionedThroughput (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStreamSpecification
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:streamSpecification (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDeletionProtectionEnabled
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:deletionProtectionEnabled (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isDeletionProtectionEnabled
                                        ^com.amazonaws.services.dynamodbv2.model.TableDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:deletionProtectionEnabled
                    (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription
    d/ObjectToData
    {:object-to-data
     (fn fn__19790
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getLastIncreaseDateTime
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:lastIncreaseDateTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLastDecreaseDateTime
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:lastDecreaseDateTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getNumberOfDecreasesToday
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:numberOfDecreasesToday (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getReadCapacityUnits
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:readCapacityUnits (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getWriteCapacityUnits
                                        ^com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:writeCapacityUnits (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.dynamodbv2.model.CreateTableResult
    d/ObjectToData
    {:object-to-data
     (fn fn__19802
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTableDescription
                                        ^com.amazonaws.services.dynamodbv2.model.CreateTableResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tableDescription (d/object-to-data-wrapper v__19409__auto__)])))
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
  (extend
    com.amazonaws.services.dynamodbv2.model.KeySchemaElement
    d/ObjectToData
    {:object-to-data
     (fn fn__19810
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getKeyType
                                        ^com.amazonaws.services.dynamodbv2.model.KeySchemaElement o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:keyType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAttributeName
                                        ^com.amazonaws.services.dynamodbv2.model.KeySchemaElement o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:attributeName (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb" "list-tables")
    {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ddb" "list-tables")
    (fn list_tables
      ([o x1]
        (d/object-to-data
          (.listTables
            ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
            (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.ListTablesRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb" "create-table")
    {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ddb" "create-table")
    (fn create_table
      ([o x1]
        (d/object-to-data
          (.createTable
            ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
            (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.CreateTableRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb" "update-table")
    {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ddb" "update-table")
    (fn update_table
      ([o x1]
        (d/object-to-data
          (.updateTable
            ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
            (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.UpdateTableRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb" "delete-table")
    {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ddb" "delete-table")
    (fn delete_table
      ([o x1]
        (d/object-to-data
          (.deleteTable
            ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
            (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.DeleteTableRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb" "describe-table")
    {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ddb" "describe-table")
    (fn describe_table
      ([o x1]
        (d/object-to-data
          (.describeTable
            ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
            (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.DescribeTableRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb" "put-item*")
    {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ddb" "put-item*")
    (fn put_item_STAR_
      ([o x1]
        (d/object-to-data
          (.putItem
            ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
            (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.PutItemRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb" "get-item*")
    {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ddb" "get-item*")
    (fn get_item_STAR_
      ([o x1]
        (d/object-to-data
          (.getItem
            ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
            (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.GetItemRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb" "delete-item")
    {:related-class com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ddb" "delete-item")
    (fn delete_item
      ([o x1]
        (d/object-to-data
          (.deleteItem
            ^com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient o
            (d/data-to-object x1 com.amazonaws.services.dynamodbv2.model.DeleteItemRequest))))))
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
  (reset-meta!
    #'log-errors
    (assoc
      {:arglists (clojure.core/list ['f '& 'args]), :column (int 1)}
      :name
      'log-errors
      :ns
      *ns*))
  (def put-item (fn put_item ([ddb_client request] (put-item* ddb_client request))))
  (reset-meta!
    #'put-item
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'request]), :column (int 1)}
      :name
      'put-item
      :ns
      *ns*))
  (def get-item (fn get_item ([ddb_client request] (get-item* ddb_client request))))
  (reset-meta!
    #'get-item
    (assoc
      {:arglists (clojure.core/list ['ddb-client 'request]), :column (int 1)}
      :name
      'get-item
      :ns
      *ns*))
  (defn fullname ([s] (cond (keyword? s) (subs (str s) 1) :default (do (str s)))))
  (reset-meta!
    #'fullname
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'fullname :ns *ns*))
  (defn create-item
    ([m]
      (reduce
        (fn fn__19829
          ([m p__19828]
            (let [vec__19830 p__19828
                  k (nth vec__19830 (int 0) nil)
                  v (nth vec__19830 (int 1) nil)]
              (assoc m (fullname k) (if (number? v) {:n (str v)} {:s v})))))
        {}
        m)))
  (reset-meta!
    #'create-item
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'create-item :ns *ns*))
  (defn deattr
    ([m]
      (let [G__19835 (set (keys m))]
        (case
          G__19835
          #{:n}
          (edn/read-string (:n m))
          #{:s}
          (:s m)
          (do
            (throw (ex-info "Could not parse DDB item " m))
            (clojure.lang.Util/hash G__19835))))))
  (reset-meta!
    #'deattr
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'deattr :ns *ns*))
  (defn deitem
    ([m]
      (reduce
        (fn fn__19838
          ([m p__19837]
            (let [vec__19839 p__19837
                  k (nth vec__19839 (int 0) nil)
                  v (nth vec__19839 (int 1) nil)]
              (assoc m (keyword k) (deattr v)))))
        {}
        m)))
  (reset-meta!
    #'deitem
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'deitem :ns *ns*))
  (defn create-key ([k] {"id" {(if (number? k) :n :s) k}}))
  (reset-meta!
    #'create-key
    (assoc {:arglists (clojure.core/list ['k]), :column (int 1)} :name 'create-key :ns *ns*)))