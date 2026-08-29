(do
  (clojure.core/in-ns 'datomic.cloudformation)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.datafy :as 'd] ['datomic.aws :as 'aws])
      (clojure.core/import 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.CreateStackRequest)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.CreateStackResult)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.DescribeStackEventsResult)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.DescribeStacksRequest)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.DescribeStacksResult)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.DeleteStackRequest)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.ListStacksRequest)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.ListStacksResult)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.Parameter)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.Stack)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.StackEvent)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.StackSummary)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.TemplateParameter)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.UpdateStackRequest)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.UpdateStackResult)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.ValidateTemplateRequest)
      (clojure.core/import 'com.amazonaws.services.cloudformation.model.ValidateTemplateResult)))
  (when-not (.equals 'datomic.cloudformation 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cloudformation))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.datafy :as 'd] ['datomic.aws :as 'aws])
        (clojure.core/import 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.CreateStackRequest)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.CreateStackResult)
        (clojure.core/import
          'com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest)
        (clojure.core/import
          'com.amazonaws.services.cloudformation.model.DescribeStackEventsResult)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.DescribeStacksRequest)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.DescribeStacksResult)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.DeleteStackRequest)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.ListStacksRequest)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.ListStacksResult)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.Parameter)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.Stack)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.StackEvent)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.StackSummary)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.TemplateParameter)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.UpdateStackRequest)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.UpdateStackResult)
        (clojure.core/import 'com.amazonaws.services.cloudformation.model.ValidateTemplateRequest)
        (clojure.core/import
          'com.amazonaws.services.cloudformation.model.ValidateTemplateResult))))
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
                        (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
                          (aws/credentials creds)
                          ^com.amazonaws.ClientConfiguration conf)
                        (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
                          (aws/credentials creds)
                          ^com.amazonaws.ClientConfiguration conf))
                      (if (instance?
                            com.amazonaws.auth.AWSCredentialsProvider
                            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
                        (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
                          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                          ^com.amazonaws.ClientConfiguration conf)
                        (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
                          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                          ^com.amazonaws.ClientConfiguration conf)))]
           (cond
             override_endpoint (.setEndpoint
                                 ^com.amazonaws.AmazonWebServiceClient conn
                                 (str "http://" override_endpoint))
             region (do
                      (.setEndpoint
                        ^com.amazonaws.AmazonWebServiceClient conn
                        (aws/endpoint-for :cloudformation region))))
           conn)
         (client creds)))
     ([creds]
       (if creds
         (if (instance? com.amazonaws.auth.AWSCredentialsProvider (aws/credentials creds))
           (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
             (aws/credentials creds))
           (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
             (aws/credentials creds)))
         (if (instance?
               com.amazonaws.auth.AWSCredentialsProvider
               (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
           (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
             (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
           (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
             (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
     ([]
       (if (instance?
             com.amazonaws.auth.AWSCredentialsProvider
             (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
         (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
         (com.amazonaws.services.cloudformation.AmazonCloudFormationClient.
           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))))))
  (reset-meta!
    #'client
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [] {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})
         (.withMeta
           ['creds]
           {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})
         (.withMeta
           ['creds 'config]
           {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})),
       :column (int 1)}
      :name
      'client
      :ns
      *ns*))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.cloudformation.model.CreateStackRequest :parameters]
    com.amazonaws.services.cloudformation.model.Parameter)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.cloudformation.model.CreateStackRequest :parameters]
    fn__30259
    ([_ _ val _]
      (mapv
        (fn fn__30260
          ([item] (d/data-to-object item com.amazonaws.services.cloudformation.model.Parameter)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.cloudformation.model.CreateStackRequest :capabilities]
    java.lang.String)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.cloudformation.model.CreateStackRequest :capabilities]
    fn__30263
    ([_ _ val _] (mapv (fn fn__30264 ([item] (d/data-to-object item java.lang.String))) val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.cloudformation.model.CreateStackRequest :notificationARNs]
    java.lang.String)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.cloudformation.model.CreateStackRequest :notificationARNs]
    fn__30267
    ([_ _ val _] (mapv (fn fn__30268 ([item] (d/data-to-object item java.lang.String))) val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.cloudformation.model.UpdateStackRequest :parameters]
    com.amazonaws.services.cloudformation.model.Parameter)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.cloudformation.model.UpdateStackRequest :parameters]
    fn__30271
    ([_ _ val _]
      (mapv
        (fn fn__30272
          ([item] (d/data-to-object item com.amazonaws.services.cloudformation.model.Parameter)))
        val)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudformation.model.CreateStackRequest]
    fn__30275
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:capabilities :tags :disableRollback :notificationARNs
                                     :rollbackConfiguration :roleARN :resourceTypes :onFailure
                                     :requestCredentials :sdkClientExecutionTimeout :templateURL
                                     :timeoutInMinutes :generalProgressListener :clientRequestToken
                                     :stackPolicyURL :retainExceptOnCreate :sdkRequestTimeout
                                     :enableTerminationProtection :templateBody :stackName
                                     :stackPolicyBody :parameters :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:capabilities :tags :disableRollback :notificationARNs :rollbackConfiguration
                   :roleARN :resourceTypes :onFailure :requestCredentials
                   :sdkClientExecutionTimeout :templateURL :timeoutInMinutes
                   :generalProgressListener :clientRequestToken :stackPolicyURL
                   :retainExceptOnCreate :sdkRequestTimeout :enableTerminationProtection
                   :templateBody :stackName :stackPolicyBody :parameters :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudformation.model.CreateStackRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudformation.model.CreateStackRequest.)]
        (when (contains? m :stackName)
          (let [v (:stackName m) k (d/property-to-object (class o) :stackName v java.lang.String)]
            (.setStackName
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.String k)))
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
        (when (contains? m :roleARN)
          (let [v (:roleARN m) k (d/property-to-object (class o) :roleARN v java.lang.String)]
            (.setRoleARN
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :retainExceptOnCreate)
          (let [v (:retainExceptOnCreate m)
                k (d/property-to-object (class o) :retainExceptOnCreate v java.lang.Boolean)]
            (.setRetainExceptOnCreate
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.Boolean k)))
        (when (contains? m :notificationARNs)
          (let [v (:notificationARNs m)
                k (d/property-to-object (class o) :notificationARNs v java.util.Collection)]
            (.setNotificationARNs
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.util.Collection k)))
        (when (contains? m :parameters)
          (let [v (:parameters m)
                k (d/property-to-object (class o) :parameters v java.util.Collection)]
            (.setParameters
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.util.Collection k)))
        (when (contains? m :timeoutInMinutes)
          (let [v (:timeoutInMinutes m)
                k (d/property-to-object (class o) :timeoutInMinutes v java.lang.Integer)]
            (.setTimeoutInMinutes
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.Integer k)))
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
        (when (contains? m :rollbackConfiguration)
          (let [v (:rollbackConfiguration m)
                k (d/property-to-object
                    (class o)
                    :rollbackConfiguration
                    v
                    com.amazonaws.services.cloudformation.model.RollbackConfiguration)]
            (.setRollbackConfiguration
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^com.amazonaws.services.cloudformation.model.RollbackConfiguration k)))
        (when (contains? m :enableTerminationProtection)
          (let [v (:enableTerminationProtection m)
                k (d/property-to-object
                    (class o)
                    :enableTerminationProtection
                    v
                    java.lang.Boolean)]
            (.setEnableTerminationProtection
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.Boolean k)))
        (when (contains? m :disableRollback)
          (let [v (:disableRollback m)
                k (d/property-to-object (class o) :disableRollback v java.lang.Boolean)]
            (.setDisableRollback
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.Boolean k)))
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
        (when (contains? m :templateURL)
          (let [v (:templateURL m)
                k (d/property-to-object (class o) :templateURL v java.lang.String)]
            (.setTemplateURL
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :stackPolicyURL)
          (let [v (:stackPolicyURL m)
                k (d/property-to-object (class o) :stackPolicyURL v java.lang.String)]
            (.setStackPolicyURL
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :stackPolicyBody)
          (let [v (:stackPolicyBody m)
                k (d/property-to-object (class o) :stackPolicyBody v java.lang.String)]
            (.setStackPolicyBody
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :tags)
          (let [v (:tags m) k (d/property-to-object (class o) :tags v java.util.Collection)]
            (.setTags
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.util.Collection k)))
        (when (contains? m :clientRequestToken)
          (let [v (:clientRequestToken m)
                k (d/property-to-object (class o) :clientRequestToken v java.lang.String)]
            (.setClientRequestToken
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :templateBody)
          (let [v (:templateBody m)
                k (d/property-to-object (class o) :templateBody v java.lang.String)]
            (.setTemplateBody
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :resourceTypes)
          (let [v (:resourceTypes m)
                k (d/property-to-object (class o) :resourceTypes v java.util.Collection)]
            (.setResourceTypes
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^java.util.Collection k)))
        (when (contains? m :capabilities)
          (let [v (:capabilities m)
                k (d/property-to-object (class o) :capabilities v java.util.Collection)]
            (.setCapabilities
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
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
        (when (contains? m :onFailure)
          (let [v (:onFailure m)
                k (d/property-to-object
                    (class o)
                    :onFailure
                    v
                    com.amazonaws.services.cloudformation.model.OnFailure)]
            (.setOnFailure
              ^com.amazonaws.services.cloudformation.model.CreateStackRequest o
              ^com.amazonaws.services.cloudformation.model.OnFailure k)))
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
    [:map com.amazonaws.services.cloudformation.model.DeleteStackRequest]
    fn__30278
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:roleARN :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :clientRequestToken :retainResources
                                     :sdkRequestTimeout :stackName :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:roleARN :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :clientRequestToken :retainResources :sdkRequestTimeout :stackName
                   :requestMetricCollector :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudformation.model.DeleteStackRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudformation.model.DeleteStackRequest.)]
        (when (contains? m :stackName)
          (let [v (:stackName m) k (d/property-to-object (class o) :stackName v java.lang.String)]
            (.setStackName
              ^com.amazonaws.services.cloudformation.model.DeleteStackRequest o
              ^java.lang.String k)))
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
        (when (contains? m :roleARN)
          (let [v (:roleARN m) k (d/property-to-object (class o) :roleARN v java.lang.String)]
            (.setRoleARN
              ^com.amazonaws.services.cloudformation.model.DeleteStackRequest o
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
        (when (contains? m :clientRequestToken)
          (let [v (:clientRequestToken m)
                k (d/property-to-object (class o) :clientRequestToken v java.lang.String)]
            (.setClientRequestToken
              ^com.amazonaws.services.cloudformation.model.DeleteStackRequest o
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
        (when (contains? m :retainResources)
          (let [v (:retainResources m)
                k (d/property-to-object (class o) :retainResources v java.util.Collection)]
            (.setRetainResources
              ^com.amazonaws.services.cloudformation.model.DeleteStackRequest o
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
    [:map com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest]
    fn__30281
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout :nextToken
                                     :stackName :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :nextToken :stackName :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest.)]
        (when (contains? m :nextToken)
          (let [v (:nextToken m) k (d/property-to-object (class o) :nextToken v java.lang.String)]
            (.setNextToken
              ^com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest o
              ^java.lang.String k)))
        (when (contains? m :stackName)
          (let [v (:stackName m) k (d/property-to-object (class o) :stackName v java.lang.String)]
            (.setStackName
              ^com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest o
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
    [:map com.amazonaws.services.cloudformation.model.DescribeStacksRequest]
    fn__30284
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout :nextToken
                                     :stackName :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :nextToken :stackName :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.cloudformation.model.DescribeStacksRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudformation.model.DescribeStacksRequest.)]
        (when (contains? m :nextToken)
          (let [v (:nextToken m) k (d/property-to-object (class o) :nextToken v java.lang.String)]
            (.setNextToken
              ^com.amazonaws.services.cloudformation.model.DescribeStacksRequest o
              ^java.lang.String k)))
        (when (contains? m :stackName)
          (let [v (:stackName m) k (d/property-to-object (class o) :stackName v java.lang.String)]
            (.setStackName
              ^com.amazonaws.services.cloudformation.model.DescribeStacksRequest o
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
    [:map com.amazonaws.services.cloudformation.model.ListStacksRequest]
    fn__30287
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :stackStatusFilters
                                     :sdkRequestTimeout :nextToken :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :stackStatusFilters :sdkRequestTimeout :nextToken :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudformation.model.ListStacksRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudformation.model.ListStacksRequest.)]
        (when (contains? m :nextToken)
          (let [v (:nextToken m) k (d/property-to-object (class o) :nextToken v java.lang.String)]
            (.setNextToken
              ^com.amazonaws.services.cloudformation.model.ListStacksRequest o
              ^java.lang.String k)))
        (when (contains? m :stackStatusFilters)
          (let [v (:stackStatusFilters m)
                k (d/property-to-object (class o) :stackStatusFilters v java.util.Collection)]
            (.setStackStatusFilters
              ^com.amazonaws.services.cloudformation.model.ListStacksRequest o
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
    [:map com.amazonaws.services.cloudformation.model.Parameter]
    fn__30290
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:parameterValue :resolvedValue :usePreviousValue
                                     :parameterKey}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:parameterValue :resolvedValue :usePreviousValue :parameterKey},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudformation.model.Parameter}))))
        nil)
      (let [o (com.amazonaws.services.cloudformation.model.Parameter.)]
        (when (contains? m :parameterKey)
          (let [v (:parameterKey m)
                k (d/property-to-object (class o) :parameterKey v java.lang.String)]
            (.setParameterKey
              ^com.amazonaws.services.cloudformation.model.Parameter o
              ^java.lang.String k)))
        (when (contains? m :parameterValue)
          (let [v (:parameterValue m)
                k (d/property-to-object (class o) :parameterValue v java.lang.String)]
            (.setParameterValue
              ^com.amazonaws.services.cloudformation.model.Parameter o
              ^java.lang.String k)))
        (when (contains? m :usePreviousValue)
          (let [v (:usePreviousValue m)
                k (d/property-to-object (class o) :usePreviousValue v java.lang.Boolean)]
            (.setUsePreviousValue
              ^com.amazonaws.services.cloudformation.model.Parameter o
              ^java.lang.Boolean k)))
        (when (contains? m :resolvedValue)
          (let [v (:resolvedValue m)
                k (d/property-to-object (class o) :resolvedValue v java.lang.String)]
            (.setResolvedValue
              ^com.amazonaws.services.cloudformation.model.Parameter o
              ^java.lang.String k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.cloudformation.model.UpdateStackRequest]
    fn__30293
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:capabilities :tags :disableRollback :notificationARNs
                                     :rollbackConfiguration :roleARN :resourceTypes
                                     :requestCredentials :sdkClientExecutionTimeout :templateURL
                                     :generalProgressListener :usePreviousTemplate
                                     :clientRequestToken :stackPolicyURL :retainExceptOnCreate
                                     :stackPolicyDuringUpdateBody :sdkRequestTimeout :templateBody
                                     :stackName :stackPolicyBody :parameters
                                     :requestMetricCollector :requestCredentialsProvider
                                     :stackPolicyDuringUpdateURL}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:capabilities :tags :disableRollback :notificationARNs :rollbackConfiguration
                   :roleARN :resourceTypes :requestCredentials :sdkClientExecutionTimeout
                   :templateURL :generalProgressListener :usePreviousTemplate :clientRequestToken
                   :stackPolicyURL :retainExceptOnCreate :stackPolicyDuringUpdateBody
                   :sdkRequestTimeout :templateBody :stackName :stackPolicyBody :parameters
                   :requestMetricCollector :requestCredentialsProvider
                   :stackPolicyDuringUpdateURL},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.cloudformation.model.UpdateStackRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudformation.model.UpdateStackRequest.)]
        (when (contains? m :usePreviousTemplate)
          (let [v (:usePreviousTemplate m)
                k (d/property-to-object (class o) :usePreviousTemplate v java.lang.Boolean)]
            (.setUsePreviousTemplate
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.Boolean k)))
        (when (contains? m :stackName)
          (let [v (:stackName m) k (d/property-to-object (class o) :stackName v java.lang.String)]
            (.setStackName
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.String k)))
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
        (when (contains? m :roleARN)
          (let [v (:roleARN m) k (d/property-to-object (class o) :roleARN v java.lang.String)]
            (.setRoleARN
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :retainExceptOnCreate)
          (let [v (:retainExceptOnCreate m)
                k (d/property-to-object (class o) :retainExceptOnCreate v java.lang.Boolean)]
            (.setRetainExceptOnCreate
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.Boolean k)))
        (when (contains? m :notificationARNs)
          (let [v (:notificationARNs m)
                k (d/property-to-object (class o) :notificationARNs v java.util.Collection)]
            (.setNotificationARNs
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.util.Collection k)))
        (when (contains? m :parameters)
          (let [v (:parameters m)
                k (d/property-to-object (class o) :parameters v java.util.Collection)]
            (.setParameters
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.util.Collection k)))
        (when (contains? m :stackPolicyDuringUpdateBody)
          (let [v (:stackPolicyDuringUpdateBody m)
                k (d/property-to-object (class o) :stackPolicyDuringUpdateBody v java.lang.String)]
            (.setStackPolicyDuringUpdateBody
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
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
        (when (contains? m :rollbackConfiguration)
          (let [v (:rollbackConfiguration m)
                k (d/property-to-object
                    (class o)
                    :rollbackConfiguration
                    v
                    com.amazonaws.services.cloudformation.model.RollbackConfiguration)]
            (.setRollbackConfiguration
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^com.amazonaws.services.cloudformation.model.RollbackConfiguration k)))
        (when (contains? m :disableRollback)
          (let [v (:disableRollback m)
                k (d/property-to-object (class o) :disableRollback v java.lang.Boolean)]
            (.setDisableRollback
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.Boolean k)))
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
        (when (contains? m :templateURL)
          (let [v (:templateURL m)
                k (d/property-to-object (class o) :templateURL v java.lang.String)]
            (.setTemplateURL
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :stackPolicyURL)
          (let [v (:stackPolicyURL m)
                k (d/property-to-object (class o) :stackPolicyURL v java.lang.String)]
            (.setStackPolicyURL
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :stackPolicyBody)
          (let [v (:stackPolicyBody m)
                k (d/property-to-object (class o) :stackPolicyBody v java.lang.String)]
            (.setStackPolicyBody
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :tags)
          (let [v (:tags m) k (d/property-to-object (class o) :tags v java.util.Collection)]
            (.setTags
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.util.Collection k)))
        (when (contains? m :clientRequestToken)
          (let [v (:clientRequestToken m)
                k (d/property-to-object (class o) :clientRequestToken v java.lang.String)]
            (.setClientRequestToken
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :stackPolicyDuringUpdateURL)
          (let [v (:stackPolicyDuringUpdateURL m)
                k (d/property-to-object (class o) :stackPolicyDuringUpdateURL v java.lang.String)]
            (.setStackPolicyDuringUpdateURL
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :templateBody)
          (let [v (:templateBody m)
                k (d/property-to-object (class o) :templateBody v java.lang.String)]
            (.setTemplateBody
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.lang.String k)))
        (when (contains? m :resourceTypes)
          (let [v (:resourceTypes m)
                k (d/property-to-object (class o) :resourceTypes v java.util.Collection)]
            (.setResourceTypes
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
              ^java.util.Collection k)))
        (when (contains? m :capabilities)
          (let [v (:capabilities m)
                k (d/property-to-object (class o) :capabilities v java.util.Collection)]
            (.setCapabilities
              ^com.amazonaws.services.cloudformation.model.UpdateStackRequest o
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
    [:map com.amazonaws.services.cloudformation.model.ValidateTemplateRequest]
    fn__30296
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :sdkClientExecutionTimeout :templateURL
                                     :generalProgressListener :sdkRequestTimeout :templateBody
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :sdkClientExecutionTimeout :templateURL
                   :generalProgressListener :sdkRequestTimeout :templateBody
                   :requestMetricCollector :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.cloudformation.model.ValidateTemplateRequest}))))
        nil)
      (let [o (com.amazonaws.services.cloudformation.model.ValidateTemplateRequest.)]
        (when (contains? m :templateBody)
          (let [v (:templateBody m)
                k (d/property-to-object (class o) :templateBody v java.lang.String)]
            (.setTemplateBody
              ^com.amazonaws.services.cloudformation.model.ValidateTemplateRequest o
              ^java.lang.String k)))
        (when (contains? m :templateURL)
          (let [v (:templateURL m)
                k (d/property-to-object (class o) :templateURL v java.lang.String)]
            (.setTemplateURL
              ^com.amazonaws.services.cloudformation.model.ValidateTemplateRequest o
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
    com.amazonaws.services.cloudformation.model.CreateStackResult
    d/ObjectToData
    {:object-to-data
     (fn fn__30299
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getStackId
                                        ^com.amazonaws.services.cloudformation.model.CreateStackResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackId (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.cloudformation.model.DescribeStackEventsResult
    d/ObjectToData
    {:object-to-data
     (fn fn__30307
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getNextToken
                                        ^com.amazonaws.services.cloudformation.model.DescribeStackEventsResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:nextToken (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackEvents
                                        ^com.amazonaws.services.cloudformation.model.DescribeStackEventsResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackEvents (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.cloudformation.model.DescribeStacksResult
    d/ObjectToData
    {:object-to-data
     (fn fn__30317
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getNextToken
                                        ^com.amazonaws.services.cloudformation.model.DescribeStacksResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:nextToken (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStacks
                                        ^com.amazonaws.services.cloudformation.model.DescribeStacksResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stacks (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.cloudformation.model.ListStacksResult
    d/ObjectToData
    {:object-to-data
     (fn fn__30327
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getNextToken
                                        ^com.amazonaws.services.cloudformation.model.ListStacksResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:nextToken (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackSummaries
                                        ^com.amazonaws.services.cloudformation.model.ListStacksResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackSummaries (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.cloudformation.model.Parameter
    d/ObjectToData
    {:object-to-data
     (fn fn__30337
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getParameterKey
                                        ^com.amazonaws.services.cloudformation.model.Parameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:parameterKey (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getParameterValue
                                        ^com.amazonaws.services.cloudformation.model.Parameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:parameterValue (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getUsePreviousValue
                                        ^com.amazonaws.services.cloudformation.model.Parameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:usePreviousValue (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isUsePreviousValue
                                        ^com.amazonaws.services.cloudformation.model.Parameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:usePreviousValue (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getResolvedValue
                                        ^com.amazonaws.services.cloudformation.model.Parameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:resolvedValue (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.cloudformation.model.Stack
    d/ObjectToData
    {:object-to-data
     (fn fn__30349
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTags
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tags (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCapabilities
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:capabilities (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCreationTime
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:creationTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDescription
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:description (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRoleARN
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:roleARN (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackName
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackId
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getChangeSetId
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:changeSetId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDeletionTime
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:deletionTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getOutputs
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:outputs (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getParentId
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:parentId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRootId
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:rootId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDriftInformation
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:driftInformation (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLastUpdatedTime
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:lastUpdatedTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackStatus
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackStatusReason
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackStatusReason (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDisableRollback
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:disableRollback (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isDisableRollback
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:disableRollback (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRollbackConfiguration
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:rollbackConfiguration (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getTimeoutInMinutes
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:timeoutInMinutes (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getNotificationARNs
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:notificationARNs (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getEnableTerminationProtection
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:enableTerminationProtection (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isEnableTerminationProtection
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:enableTerminationProtection (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRetainExceptOnCreate
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:retainExceptOnCreate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isRetainExceptOnCreate
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:retainExceptOnCreate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getParameters
                                        ^com.amazonaws.services.cloudformation.model.Stack o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:parameters (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.cloudformation.model.StackEvent
    d/ObjectToData
    {:object-to-data
     (fn fn__30403
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTimestamp
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:timestamp (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getClientRequestToken
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:clientRequestToken (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getEventId
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:eventId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLogicalResourceId
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:logicalResourceId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPhysicalResourceId
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:physicalResourceId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getResourceType
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:resourceType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getResourceStatus
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:resourceStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getResourceStatusReason
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:resourceStatusReason (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getResourceProperties
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:resourceProperties (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getHookType
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:hookType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getHookStatus
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:hookStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getHookStatusReason
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:hookStatusReason (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getHookInvocationPoint
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:hookInvocationPoint (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getHookFailureMode
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:hookFailureMode (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackName
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackId
                                        ^com.amazonaws.services.cloudformation.model.StackEvent o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackId (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.cloudformation.model.StackSummary
    d/ObjectToData
    {:object-to-data
     (fn fn__30437
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getCreationTime
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:creationTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getTemplateDescription
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:templateDescription (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackName
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackId
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDeletionTime
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:deletionTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getParentId
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:parentId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRootId
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:rootId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDriftInformation
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:driftInformation (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLastUpdatedTime
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:lastUpdatedTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackStatus
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStackStatusReason
                                        ^com.amazonaws.services.cloudformation.model.StackSummary o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackStatusReason (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.cloudformation.model.TemplateParameter
    d/ObjectToData
    {:object-to-data
     (fn fn__30461
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getDescription
                                        ^com.amazonaws.services.cloudformation.model.TemplateParameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:description (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getNoEcho
                                        ^com.amazonaws.services.cloudformation.model.TemplateParameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:noEcho (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isNoEcho
                                        ^com.amazonaws.services.cloudformation.model.TemplateParameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:noEcho (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getParameterKey
                                        ^com.amazonaws.services.cloudformation.model.TemplateParameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:parameterKey (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDefaultValue
                                        ^com.amazonaws.services.cloudformation.model.TemplateParameter o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:defaultValue (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.cloudformation.model.UpdateStackResult
    d/ObjectToData
    {:object-to-data
     (fn fn__30473
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getStackId
                                        ^com.amazonaws.services.cloudformation.model.UpdateStackResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stackId (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.cloudformation.model.ValidateTemplateResult
    d/ObjectToData
    {:object-to-data
     (fn fn__30481
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getCapabilities
                                        ^com.amazonaws.services.cloudformation.model.ValidateTemplateResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:capabilities (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDescription
                                        ^com.amazonaws.services.cloudformation.model.ValidateTemplateResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:description (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCapabilitiesReason
                                        ^com.amazonaws.services.cloudformation.model.ValidateTemplateResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:capabilitiesReason (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDeclaredTransforms
                                        ^com.amazonaws.services.cloudformation.model.ValidateTemplateResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:declaredTransforms (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getParameters
                                        ^com.amazonaws.services.cloudformation.model.ValidateTemplateResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:parameters (d/object-to-data-wrapper v__19409__auto__)])))
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
  (.setMeta
    (clojure.lang.RT/var "datomic.cloudformation" "list-stacks")
    {:related-class com.amazonaws.services.cloudformation.AmazonCloudFormationClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})
        'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cloudformation" "list-stacks")
    (fn list_stacks
      ([o x1]
        (d/object-to-data
          (.listStacks
            ^com.amazonaws.services.cloudformation.AmazonCloudFormationClient o
            (d/data-to-object
              x1
              com.amazonaws.services.cloudformation.model.ListStacksRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.cloudformation" "validate-template")
    {:related-class com.amazonaws.services.cloudformation.AmazonCloudFormationClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})
        'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cloudformation" "validate-template")
    (fn validate_template
      ([o x1]
        (d/object-to-data
          (.validateTemplate
            ^com.amazonaws.services.cloudformation.AmazonCloudFormationClient o
            (d/data-to-object
              x1
              com.amazonaws.services.cloudformation.model.ValidateTemplateRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.cloudformation" "create-stack")
    {:related-class com.amazonaws.services.cloudformation.AmazonCloudFormationClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})
        'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cloudformation" "create-stack")
    (fn create_stack
      ([o x1]
        (d/object-to-data
          (.createStack
            ^com.amazonaws.services.cloudformation.AmazonCloudFormationClient o
            (d/data-to-object
              x1
              com.amazonaws.services.cloudformation.model.CreateStackRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.cloudformation" "delete-stack")
    {:related-class com.amazonaws.services.cloudformation.AmazonCloudFormationClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})
        'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cloudformation" "delete-stack")
    (fn delete_stack
      ([o x1]
        (d/object-to-data
          (.deleteStack
            ^com.amazonaws.services.cloudformation.AmazonCloudFormationClient o
            (d/data-to-object
              x1
              com.amazonaws.services.cloudformation.model.DeleteStackRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.cloudformation" "update-stack")
    {:related-class com.amazonaws.services.cloudformation.AmazonCloudFormationClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})
        'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cloudformation" "update-stack")
    (fn update_stack
      ([o x1]
        (d/object-to-data
          (.updateStack
            ^com.amazonaws.services.cloudformation.AmazonCloudFormationClient o
            (d/data-to-object
              x1
              com.amazonaws.services.cloudformation.model.UpdateStackRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.cloudformation" "describe-stack-events")
    {:related-class com.amazonaws.services.cloudformation.AmazonCloudFormationClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})
        'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cloudformation" "describe-stack-events")
    (fn describe_stack_events
      ([o x1]
        (d/object-to-data
          (.describeStackEvents
            ^com.amazonaws.services.cloudformation.AmazonCloudFormationClient o
            (d/data-to-object
              x1
              com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.cloudformation" "describe-stacks")
    {:related-class com.amazonaws.services.cloudformation.AmazonCloudFormationClient,
     :arglists
     (clojure.core/list
       [(.withMeta 'o {:tag 'com.amazonaws.services.cloudformation.AmazonCloudFormationClient})
        'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cloudformation" "describe-stacks")
    (fn describe_stacks
      ([o x1]
        (d/object-to-data
          (.describeStacks
            ^com.amazonaws.services.cloudformation.AmazonCloudFormationClient o
            (d/data-to-object
              x1
              com.amazonaws.services.cloudformation.model.DescribeStacksRequest))))))
  (defn map->parameters
    ([m]
      (reduce
        (fn fn__30505
          ([coll p__30504]
            (let [vec__30506 p__30504
                  k (nth vec__30506 (int 0) nil)
                  v (nth vec__30506 (int 1) nil)]
              (conj coll {:parameterKey (name k), :parameterValue v}))))
        []
        m)))
  (reset-meta!
    #'map->parameters
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'map->parameters :ns *ns*))
  (defn parameters->map
    ([pkv]
      (reduce
        (fn fn__30511 ([m pkv] (assoc m (keyword (:parameterKey pkv)) (:parameterValue pkv))))
        {}
        pkv)))
  (reset-meta!
    #'parameters->map
    (assoc
      {:arglists (clojure.core/list ['pkv]), :column (int 1)}
      :name
      'parameters->map
      :ns
      *ns*))
  (defn stack-parameters
    ([client name]
      (-> (describe-stacks client {:stackName name})
       (:stacks)
       (first)
       (:parameters)
       (parameters->map))))
  (reset-meta!
    #'stack-parameters
    (assoc
      {:arglists (clojure.core/list ['client 'name]), :column (int 1)}
      :name
      'stack-parameters
      :ns
      *ns*))
  (def delete-stack-command
   (fn delete_stack_command
     ([p__30515]
       (let [map__30516 p__30515
             map__30516 (if (seq? map__30516)
                          (if (next map__30516)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__30516))
                            (if (seq map__30516) (first map__30516) {}))
                          map__30516)
             region (get map__30516 :region)
             stack_name (get map__30516 :stack-name)
             s__6419__auto__ (java.io.StringWriter.)]
         (binding [*out* s__6419__auto__]
           (do
             (println (delete-stack (client nil {:region region}) {:stackName stack_name}))
             (str s__6419__auto__)))))))
  (reset-meta!
    #'delete-stack-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['region 'stack-name]}]), :column (int 1)}
      :name
      'delete-stack-command
      :ns
      *ns*))
  (def create-stack-command
   (fn create_stack_command
     ([p__30519]
       (let [map__30520 p__30519
             map__30520 (if (seq? map__30520)
                          (if (next map__30520)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__30520))
                            (if (seq map__30520) (first map__30520) {}))
                          map__30520)
             region (get map__30520 :region)
             stack_name (get map__30520 :stack-name)
             template_file (get map__30520 :template-file)
             s__6419__auto__ (java.io.StringWriter.)]
         (binding [*out* s__6419__auto__]
           (do
             (println
               (create-stack
                 (client nil {:region region})
                 {:stackName stack_name, :templateBody (slurp template_file)}))
             (str s__6419__auto__)))))))
  (reset-meta!
    #'create-stack-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['region 'stack-name 'template-file]}]),
       :column (int 1)}
      :name
      'create-stack-command
      :ns
      *ns*)))