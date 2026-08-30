(do
  (clojure.core/in-ns 'datomic.ec2)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.io :as 'io]
        ['datomic.aws :as 'aws]
        ['datomic.datafy :as 'd]
        ['datomic.slf4j :as 'logger]
        ['clojure.java.shell :as 'sh]
        ['clojure.string :as 'str])
      (clojure.core/import 'com.amazonaws.services.ec2.AmazonEC2Client)
      (clojure.core/import 'com.amazonaws.services.ec2.model.BlockDeviceMapping)
      (clojure.core/import 'com.amazonaws.services.ec2.model.CreateTagsRequest)
      (clojure.core/import 'com.amazonaws.services.ec2.model.DescribeInstancesRequest)
      (clojure.core/import 'com.amazonaws.services.ec2.model.DescribeInstancesResult)
      (clojure.core/import 'com.amazonaws.services.ec2.model.GroupIdentifier)
      (clojure.core/import 'com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest)
      (clojure.core/import 'com.amazonaws.services.ec2.model.Instance)
      (clojure.core/import 'com.amazonaws.services.ec2.model.Placement)
      (clojure.core/import 'com.amazonaws.services.ec2.model.Reservation)
      (clojure.core/import 'com.amazonaws.services.ec2.model.RunInstancesRequest)
      (clojure.core/import 'com.amazonaws.services.ec2.model.Tag)
      (clojure.core/import 'com.amazonaws.services.ec2.model.IpPermission)
      (clojure.core/import 'com.amazonaws.services.ec2.model.KeyPair)
      (clojure.core/import 'com.amazonaws.services.ec2.model.CreateKeyPairRequest)
      (clojure.core/import 'com.amazonaws.services.ec2.model.CreateKeyPairResult)
      (clojure.core/import 'com.amazonaws.services.ec2.model.CreateSecurityGroupRequest)
      (clojure.core/import 'com.amazonaws.services.ec2.model.CreateSecurityGroupResult)
      (clojure.core/import 'com.amazonaws.services.ec2.model.SecurityGroup)
      (clojure.core/import 'com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest)
      (clojure.core/import 'com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult)
      (clojure.core/import 'com.amazonaws.services.ec2.model.UserIdGroupPair)
      (clojure.core/import 'com.amazonaws.services.ec2.model.Filter)
      (clojure.core/import 'com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest)))
  (when-not (.equals 'datomic.ec2 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.ec2))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common]
          ['datomic.io :as 'io]
          ['datomic.aws :as 'aws]
          ['datomic.datafy :as 'd]
          ['datomic.slf4j :as 'logger]
          ['clojure.java.shell :as 'sh]
          ['clojure.string :as 'str])
        (clojure.core/import 'com.amazonaws.services.ec2.AmazonEC2Client)
        (clojure.core/import 'com.amazonaws.services.ec2.model.BlockDeviceMapping)
        (clojure.core/import 'com.amazonaws.services.ec2.model.CreateTagsRequest)
        (clojure.core/import 'com.amazonaws.services.ec2.model.DescribeInstancesRequest)
        (clojure.core/import 'com.amazonaws.services.ec2.model.DescribeInstancesResult)
        (clojure.core/import 'com.amazonaws.services.ec2.model.GroupIdentifier)
        (clojure.core/import
          'com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest)
        (clojure.core/import 'com.amazonaws.services.ec2.model.Instance)
        (clojure.core/import 'com.amazonaws.services.ec2.model.Placement)
        (clojure.core/import 'com.amazonaws.services.ec2.model.Reservation)
        (clojure.core/import 'com.amazonaws.services.ec2.model.RunInstancesRequest)
        (clojure.core/import 'com.amazonaws.services.ec2.model.Tag)
        (clojure.core/import 'com.amazonaws.services.ec2.model.IpPermission)
        (clojure.core/import 'com.amazonaws.services.ec2.model.KeyPair)
        (clojure.core/import 'com.amazonaws.services.ec2.model.CreateKeyPairRequest)
        (clojure.core/import 'com.amazonaws.services.ec2.model.CreateKeyPairResult)
        (clojure.core/import 'com.amazonaws.services.ec2.model.CreateSecurityGroupRequest)
        (clojure.core/import 'com.amazonaws.services.ec2.model.CreateSecurityGroupResult)
        (clojure.core/import 'com.amazonaws.services.ec2.model.SecurityGroup)
        (clojure.core/import 'com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest)
        (clojure.core/import 'com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult)
        (clojure.core/import 'com.amazonaws.services.ec2.model.UserIdGroupPair)
        (clojure.core/import 'com.amazonaws.services.ec2.model.Filter)
        (clojure.core/import 'com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest))))
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
                        (com.amazonaws.services.ec2.AmazonEC2Client.
                          (aws/credentials creds)
                          ^com.amazonaws.ClientConfiguration conf)
                        (com.amazonaws.services.ec2.AmazonEC2Client.
                          (aws/credentials creds)
                          ^com.amazonaws.ClientConfiguration conf))
                      (if (instance?
                            com.amazonaws.auth.AWSCredentialsProvider
                            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
                        (com.amazonaws.services.ec2.AmazonEC2Client.
                          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                          ^com.amazonaws.ClientConfiguration conf)
                        (com.amazonaws.services.ec2.AmazonEC2Client.
                          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                          ^com.amazonaws.ClientConfiguration conf)))]
           (cond
             override_endpoint (.setEndpoint
                                 ^com.amazonaws.AmazonWebServiceClient conn
                                 (str "http://" override_endpoint))
             region (do
                      (.setEndpoint
                        ^com.amazonaws.AmazonWebServiceClient conn
                        (aws/endpoint-for :ec2 region))))
           conn)
         (client creds)))
     ([creds]
       (if creds
         (if (instance? com.amazonaws.auth.AWSCredentialsProvider (aws/credentials creds))
           (com.amazonaws.services.ec2.AmazonEC2Client. (aws/credentials creds))
           (com.amazonaws.services.ec2.AmazonEC2Client. (aws/credentials creds)))
         (if (instance?
               com.amazonaws.auth.AWSCredentialsProvider
               (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
           (com.amazonaws.services.ec2.AmazonEC2Client.
             (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
           (com.amazonaws.services.ec2.AmazonEC2Client.
             (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
     ([]
       (if (instance?
             com.amazonaws.auth.AWSCredentialsProvider
             (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
         (com.amazonaws.services.ec2.AmazonEC2Client.
           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
         (com.amazonaws.services.ec2.AmazonEC2Client.
           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))))))
  (reset-meta!
    #'client
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [] {:tag 'com.amazonaws.services.ec2.AmazonEC2Client})
         (.withMeta ['creds] {:tag 'com.amazonaws.services.ec2.AmazonEC2Client})
         (.withMeta ['creds 'config] {:tag 'com.amazonaws.services.ec2.AmazonEC2Client})),
       :column (int 1)}
      :name
      'client
      :ns
      *ns*))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest :ipPermissions]
    com.amazonaws.services.ec2.model.IpPermission)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest :ipPermissions]
    fn__31651
    ([_ _ val _]
      (mapv
        (fn fn__31652
          ([item] (d/data-to-object item com.amazonaws.services.ec2.model.IpPermission)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.ec2.model.IpPermission :ipRanges]
    java.lang.String)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.ec2.model.IpPermission :ipRanges]
    fn__31655
    ([_ _ val _] (mapv (fn fn__31656 ([item] (d/data-to-object item java.lang.String))) val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.ec2.model.IpPermission :userIdGroupPairs]
    com.amazonaws.services.ec2.model.UserIdGroupPair)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.ec2.model.IpPermission :userIdGroupPairs]
    fn__31659
    ([_ _ val _]
      (mapv
        (fn fn__31660
          ([item] (d/data-to-object item com.amazonaws.services.ec2.model.UserIdGroupPair)))
        val)))
  (alter-var-root
    #'d/list-property-types
    assoc
    [com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest :filters]
    com.amazonaws.services.ec2.model.Filter)
  (defmethod
    d/property-to-object
    [com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest :filters]
    fn__31663
    ([_ _ val _]
      (mapv
        (fn fn__31664 ([item] (d/data-to-object item com.amazonaws.services.ec2.model.Filter)))
        val)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest]
    fn__31667
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:groupId :cidrIp :toPort :ipProtocol :fromPort :ipPermissions
                                     :groupName :requestCredentials :sourceSecurityGroupOwnerId
                                     :sdkClientExecutionTimeout :generalProgressListener
                                     :sourceSecurityGroupName :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:groupId :cidrIp :toPort :ipProtocol :fromPort :ipPermissions :groupName
                   :requestCredentials :sourceSecurityGroupOwnerId :sdkClientExecutionTimeout
                   :generalProgressListener :sourceSecurityGroupName :sdkRequestTimeout
                   :requestMetricCollector :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest}))))
        nil)
      (let [o (com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest.)]
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
        (when (contains? m :cidrIp)
          (let [v (:cidrIp m) k (d/property-to-object (class o) :cidrIp v java.lang.String)]
            (.setCidrIp
              ^com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest o
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
        (when (contains? m :groupId)
          (let [v (:groupId m) k (d/property-to-object (class o) :groupId v java.lang.String)]
            (.setGroupId
              ^com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest o
              ^java.lang.String k)))
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :toPort)
          (let [v (:toPort m) k (d/property-to-object (class o) :toPort v java.lang.Integer)]
            (.setToPort
              ^com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest o
              ^java.lang.Integer k)))
        (when (contains? m :sourceSecurityGroupOwnerId)
          (let [v (:sourceSecurityGroupOwnerId m)
                k (d/property-to-object (class o) :sourceSecurityGroupOwnerId v java.lang.String)]
            (.setSourceSecurityGroupOwnerId
              ^com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest o
              ^java.lang.String k)))
        (when (contains? m :ipPermissions)
          (let [v (:ipPermissions m)
                k (d/property-to-object (class o) :ipPermissions v java.util.Collection)]
            (.setIpPermissions
              ^com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest o
              ^java.util.Collection k)))
        (when (contains? m :sourceSecurityGroupName)
          (let [v (:sourceSecurityGroupName m)
                k (d/property-to-object (class o) :sourceSecurityGroupName v java.lang.String)]
            (.setSourceSecurityGroupName
              ^com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest o
              ^java.lang.String k)))
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest o
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
        (when (contains? m :ipProtocol)
          (let [v (:ipProtocol m)
                k (d/property-to-object (class o) :ipProtocol v java.lang.String)]
            (.setIpProtocol
              ^com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest o
              ^java.lang.String k)))
        (when (contains? m :fromPort)
          (let [v (:fromPort m) k (d/property-to-object (class o) :fromPort v java.lang.Integer)]
            (.setFromPort
              ^com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest o
              ^java.lang.Integer k)))
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
    [:map com.amazonaws.services.ec2.model.DescribeInstancesRequest]
    fn__31670
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:instanceIds :filters :requestCredentials
                                     :sdkClientExecutionTimeout :generalProgressListener
                                     :sdkRequestTimeout :nextToken :requestMetricCollector
                                     :requestCredentialsProvider :maxResults}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:instanceIds :filters :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :nextToken :requestMetricCollector
                   :requestCredentialsProvider :maxResults},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.ec2.model.DescribeInstancesRequest}))))
        nil)
      (let [o (com.amazonaws.services.ec2.model.DescribeInstancesRequest.)]
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
        (when (contains? m :sdkRequestTimeout)
          (let [v (:sdkRequestTimeout m)
                k (d/property-to-object (class o) :sdkRequestTimeout v java.lang.Integer/TYPE)]
            (.setSdkRequestTimeout
              ^com.amazonaws.AmazonWebServiceRequest o
              (int ^java.lang.Number k))))
        (when (contains? m :filters)
          (let [v (:filters m) k (d/property-to-object (class o) :filters v java.util.Collection)]
            (.setFilters
              ^com.amazonaws.services.ec2.model.DescribeInstancesRequest o
              ^java.util.Collection k)))
        (when (contains? m :instanceIds)
          (let [v (:instanceIds m)
                k (d/property-to-object (class o) :instanceIds v java.util.Collection)]
            (.setInstanceIds
              ^com.amazonaws.services.ec2.model.DescribeInstancesRequest o
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
        (when (contains? m :nextToken)
          (let [v (:nextToken m) k (d/property-to-object (class o) :nextToken v java.lang.String)]
            (.setNextToken
              ^com.amazonaws.services.ec2.model.DescribeInstancesRequest o
              ^java.lang.String k)))
        (when (contains? m :maxResults)
          (let [v (:maxResults m)
                k (d/property-to-object (class o) :maxResults v java.lang.Integer)]
            (.setMaxResults
              ^com.amazonaws.services.ec2.model.DescribeInstancesRequest o
              ^java.lang.Integer k)))
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
    [:map com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest]
    fn__31673
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:groupNames :filters :requestCredentials
                                     :sdkClientExecutionTimeout :groupIds :generalProgressListener
                                     :sdkRequestTimeout :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:groupNames :filters :requestCredentials :sdkClientExecutionTimeout :groupIds
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest}))))
        nil)
      (let [o (com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest.)]
        (when (contains? m :groupIds)
          (let [v (:groupIds m)
                k (d/property-to-object (class o) :groupIds v java.util.Collection)]
            (.setGroupIds
              ^com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest o
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
        (when (contains? m :groupNames)
          (let [v (:groupNames m)
                k (d/property-to-object (class o) :groupNames v java.util.Collection)]
            (.setGroupNames
              ^com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest o
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
        (when (contains? m :filters)
          (let [v (:filters m) k (d/property-to-object (class o) :filters v java.util.Collection)]
            (.setFilters
              ^com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest o
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
    [:map com.amazonaws.services.ec2.model.CreateSecurityGroupRequest]
    fn__31676
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:description :groupName :requestCredentials
                                     :sdkClientExecutionTimeout :generalProgressListener :vpcId
                                     :sdkRequestTimeout :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:description :groupName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :vpcId :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.ec2.model.CreateSecurityGroupRequest}))))
        nil)
      (let [o (com.amazonaws.services.ec2.model.CreateSecurityGroupRequest.)]
        (when (contains? m :description)
          (let [v (:description m)
                k (d/property-to-object (class o) :description v java.lang.String)]
            (.setDescription
              ^com.amazonaws.services.ec2.model.CreateSecurityGroupRequest o
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
        (when (contains? m :vpcId)
          (let [v (:vpcId m) k (d/property-to-object (class o) :vpcId v java.lang.String)]
            (.setVpcId
              ^com.amazonaws.services.ec2.model.CreateSecurityGroupRequest o
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
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.ec2.model.CreateSecurityGroupRequest o
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
    [:map com.amazonaws.services.ec2.model.CreateKeyPairRequest]
    fn__31679
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:keyName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:keyName :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.ec2.model.CreateKeyPairRequest}))))
        nil)
      (let [o (com.amazonaws.services.ec2.model.CreateKeyPairRequest.)]
        (when (contains? m :keyName)
          (let [v (:keyName m) k (d/property-to-object (class o) :keyName v java.lang.String)]
            (.setKeyName
              ^com.amazonaws.services.ec2.model.CreateKeyPairRequest o
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
    [:map com.amazonaws.services.ec2.model.Filter]
    fn__31682
    ([m _]
      (let [temp__5804__auto__ (seq (remove #{:name :values} (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys #{:name :values},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.ec2.model.Filter}))))
        nil)
      (let [o (com.amazonaws.services.ec2.model.Filter.)]
        (when (contains? m :values)
          (let [v (:values m) k (d/property-to-object (class o) :values v java.util.Collection)]
            (.setValues ^com.amazonaws.services.ec2.model.Filter o ^java.util.Collection k)))
        (when (contains? m :name)
          (let [v (:name m) k (d/property-to-object (class o) :name v java.lang.String)]
            (.setName ^com.amazonaws.services.ec2.model.Filter o ^java.lang.String k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.ec2.model.IpPermission]
    fn__31685
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:toPort :ipProtocol :fromPort :ipRanges :prefixListIds
                                     :userIdGroupPairs}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:toPort :ipProtocol :fromPort :ipRanges :prefixListIds :userIdGroupPairs},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.ec2.model.IpPermission}))))
        nil)
      (let [o (com.amazonaws.services.ec2.model.IpPermission.)]
        (when (contains? m :userIdGroupPairs)
          (let [v (:userIdGroupPairs m)
                k (d/property-to-object (class o) :userIdGroupPairs v java.util.Collection)]
            (.setUserIdGroupPairs
              ^com.amazonaws.services.ec2.model.IpPermission o
              ^java.util.Collection k)))
        (when (contains? m :ipRanges)
          (let [v (:ipRanges m)
                k (d/property-to-object (class o) :ipRanges v java.util.Collection)]
            (.setIpRanges
              ^com.amazonaws.services.ec2.model.IpPermission o
              ^java.util.Collection k)))
        (when (contains? m :prefixListIds)
          (let [v (:prefixListIds m)
                k (d/property-to-object (class o) :prefixListIds v java.util.Collection)]
            (.setPrefixListIds
              ^com.amazonaws.services.ec2.model.IpPermission o
              ^java.util.Collection k)))
        (when (contains? m :ipProtocol)
          (let [v (:ipProtocol m)
                k (d/property-to-object (class o) :ipProtocol v java.lang.String)]
            (.setIpProtocol ^com.amazonaws.services.ec2.model.IpPermission o ^java.lang.String k)))
        (when (contains? m :fromPort)
          (let [v (:fromPort m) k (d/property-to-object (class o) :fromPort v java.lang.Integer)]
            (.setFromPort ^com.amazonaws.services.ec2.model.IpPermission o ^java.lang.Integer k)))
        (when (contains? m :toPort)
          (let [v (:toPort m) k (d/property-to-object (class o) :toPort v java.lang.Integer)]
            (.setToPort ^com.amazonaws.services.ec2.model.IpPermission o ^java.lang.Integer k)))
        o)))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest]
    fn__31688
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:groupId :groupName :requestCredentials
                                     :sdkClientExecutionTimeout :generalProgressListener
                                     :sdkRequestTimeout :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:groupId :groupName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest}))))
        nil)
      (let [o (com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest.)]
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest o
              ^java.lang.String k)))
        (when (contains? m :groupId)
          (let [v (:groupId m) k (d/property-to-object (class o) :groupId v java.lang.String)]
            (.setGroupId
              ^com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest o
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
    [:map com.amazonaws.services.ec2.model.UserIdGroupPair]
    fn__31691
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:groupId :groupName :vpcPeeringConnectionId :vpcId :userId
                                     :peeringStatus}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:groupId :groupName :vpcPeeringConnectionId :vpcId :userId :peeringStatus},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.ec2.model.UserIdGroupPair}))))
        nil)
      (let [o (com.amazonaws.services.ec2.model.UserIdGroupPair.)]
        (when (contains? m :vpcPeeringConnectionId)
          (let [v (:vpcPeeringConnectionId m)
                k (d/property-to-object (class o) :vpcPeeringConnectionId v java.lang.String)]
            (.setVpcPeeringConnectionId
              ^com.amazonaws.services.ec2.model.UserIdGroupPair o
              ^java.lang.String k)))
        (when (contains? m :peeringStatus)
          (let [v (:peeringStatus m)
                k (d/property-to-object (class o) :peeringStatus v java.lang.String)]
            (.setPeeringStatus
              ^com.amazonaws.services.ec2.model.UserIdGroupPair o
              ^java.lang.String k)))
        (when (contains? m :vpcId)
          (let [v (:vpcId m) k (d/property-to-object (class o) :vpcId v java.lang.String)]
            (.setVpcId ^com.amazonaws.services.ec2.model.UserIdGroupPair o ^java.lang.String k)))
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.ec2.model.UserIdGroupPair o
              ^java.lang.String k)))
        (when (contains? m :groupId)
          (let [v (:groupId m) k (d/property-to-object (class o) :groupId v java.lang.String)]
            (.setGroupId ^com.amazonaws.services.ec2.model.UserIdGroupPair o ^java.lang.String k)))
        (when (contains? m :userId)
          (let [v (:userId m) k (d/property-to-object (class o) :userId v java.lang.String)]
            (.setUserId ^com.amazonaws.services.ec2.model.UserIdGroupPair o ^java.lang.String k)))
        o)))
  (extend
    com.amazonaws.services.ec2.model.CreateKeyPairResult
    d/ObjectToData
    {:object-to-data
     (fn fn__31694
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getKeyPair
                                        ^com.amazonaws.services.ec2.model.CreateKeyPairResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:keyPair (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.CreateSecurityGroupResult
    d/ObjectToData
    {:object-to-data
     (fn fn__31698
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getGroupId
                                        ^com.amazonaws.services.ec2.model.CreateSecurityGroupResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupId (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.DescribeInstancesResult
    d/ObjectToData
    {:object-to-data
     (fn fn__31702
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getNextToken
                                        ^com.amazonaws.services.ec2.model.DescribeInstancesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:nextToken (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getReservations
                                        ^com.amazonaws.services.ec2.model.DescribeInstancesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:reservations (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult
    d/ObjectToData
    {:object-to-data
     (fn fn__31708
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getSecurityGroups
                                        ^com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:securityGroups (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.GroupIdentifier
    d/ObjectToData
    {:object-to-data
     (fn fn__31712
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getGroupName
                                        ^com.amazonaws.services.ec2.model.GroupIdentifier o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGroupId
                                        ^com.amazonaws.services.ec2.model.GroupIdentifier o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupId (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.KeyPair
    d/ObjectToData
    {:object-to-data
     (fn fn__31718
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getKeyFingerprint
                                        ^com.amazonaws.services.ec2.model.KeyPair o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:keyFingerprint (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKeyMaterial
                                        ^com.amazonaws.services.ec2.model.KeyPair o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:keyMaterial (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKeyName ^com.amazonaws.services.ec2.model.KeyPair o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:keyName (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.Instance
    d/ObjectToData
    {:object-to-data
     (fn fn__31726
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTags ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tags (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getNetworkInterfaces
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:networkInterfaces (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPlatform ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:platform (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getClientToken
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:clientToken (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRootDeviceName
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:rootDeviceName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getBlockDeviceMappings
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:blockDeviceMappings (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getVirtualizationType
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:virtualizationType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getInstanceLifecycle
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instanceLifecycle (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSpotInstanceRequestId
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:spotInstanceRequestId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSecurityGroups
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:securityGroups (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSourceDestCheck
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sourceDestCheck (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isSourceDestCheck
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sourceDestCheck (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getHypervisor
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:hypervisor (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIamInstanceProfile
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:iamInstanceProfile (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getEbsOptimized
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ebsOptimized (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isEbsOptimized
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ebsOptimized (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSriovNetSupport
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:sriovNetSupport (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getEnaSupport
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:enaSupport (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isEnaSupport ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:enaSupport (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getInstanceId
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instanceId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getImageId ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:imageId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPrivateDnsName
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:privateDnsName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPublicDnsName
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:publicDnsName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStateTransitionReason
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stateTransitionReason (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKeyName ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:keyName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAmiLaunchIndex
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:amiLaunchIndex (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getProductCodes
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:productCodes (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getInstanceType
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instanceType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getLaunchTime
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:launchTime (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPlacement ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:placement (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getKernelId ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:kernelId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRamdiskId ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ramdiskId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getMonitoring
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:monitoring (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSubnetId ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:subnetId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getVpcId ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:vpcId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPrivateIpAddress
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:privateIpAddress (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPublicIpAddress
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:publicIpAddress (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getArchitecture
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:architecture (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRootDeviceType
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:rootDeviceType (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getStateReason
                                        ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:stateReason (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getState ^com.amazonaws.services.ec2.model.Instance o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:state (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.IpPermission
    d/ObjectToData
    {:object-to-data
     (fn fn__31810
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getUserIdGroupPairs
                                        ^com.amazonaws.services.ec2.model.IpPermission o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:userIdGroupPairs (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIpRanges
                                        ^com.amazonaws.services.ec2.model.IpPermission o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ipRanges (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPrefixListIds
                                        ^com.amazonaws.services.ec2.model.IpPermission o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:prefixListIds (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIpProtocol
                                        ^com.amazonaws.services.ec2.model.IpPermission o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ipProtocol (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getFromPort
                                        ^com.amazonaws.services.ec2.model.IpPermission o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:fromPort (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getToPort
                                        ^com.amazonaws.services.ec2.model.IpPermission o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:toPort (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.Reservation
    d/ObjectToData
    {:object-to-data
     (fn fn__31824
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getGroups ^com.amazonaws.services.ec2.model.Reservation o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groups (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getReservationId
                                        ^com.amazonaws.services.ec2.model.Reservation o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:reservationId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getOwnerId
                                        ^com.amazonaws.services.ec2.model.Reservation o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ownerId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRequesterId
                                        ^com.amazonaws.services.ec2.model.Reservation o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:requesterId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getInstances
                                        ^com.amazonaws.services.ec2.model.Reservation o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instances (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGroupNames
                                        ^com.amazonaws.services.ec2.model.Reservation o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupNames (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.SecurityGroup
    d/ObjectToData
    {:object-to-data
     (fn fn__31838
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTags ^com.amazonaws.services.ec2.model.SecurityGroup o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tags (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDescription
                                        ^com.amazonaws.services.ec2.model.SecurityGroup o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:description (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIpPermissionsEgress
                                        ^com.amazonaws.services.ec2.model.SecurityGroup o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ipPermissionsEgress (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getOwnerId
                                        ^com.amazonaws.services.ec2.model.SecurityGroup o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ownerId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIpPermissions
                                        ^com.amazonaws.services.ec2.model.SecurityGroup o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:ipPermissions (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getVpcId
                                        ^com.amazonaws.services.ec2.model.SecurityGroup o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:vpcId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGroupName
                                        ^com.amazonaws.services.ec2.model.SecurityGroup o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGroupId
                                        ^com.amazonaws.services.ec2.model.SecurityGroup o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupId (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.ec2.model.UserIdGroupPair
    d/ObjectToData
    {:object-to-data
     (fn fn__31856
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getVpcPeeringConnectionId
                                        ^com.amazonaws.services.ec2.model.UserIdGroupPair o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:vpcPeeringConnectionId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPeeringStatus
                                        ^com.amazonaws.services.ec2.model.UserIdGroupPair o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:peeringStatus (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getVpcId
                                        ^com.amazonaws.services.ec2.model.UserIdGroupPair o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:vpcId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGroupName
                                        ^com.amazonaws.services.ec2.model.UserIdGroupPair o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGroupId
                                        ^com.amazonaws.services.ec2.model.UserIdGroupPair o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getUserId
                                        ^com.amazonaws.services.ec2.model.UserIdGroupPair o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:userId (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (.setMeta
    (clojure.lang.RT/var "datomic.ec2" "create-keypair")
    {:related-class com.amazonaws.services.ec2.AmazonEC2Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.ec2.AmazonEC2Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ec2" "create-keypair")
    (fn create_keypair
      ([o x1]
        (d/object-to-data
          (.createKeyPair
            ^com.amazonaws.services.ec2.AmazonEC2Client o
            (d/data-to-object x1 com.amazonaws.services.ec2.model.CreateKeyPairRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ec2" "describe-instances")
    {:related-class com.amazonaws.services.ec2.AmazonEC2Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.ec2.AmazonEC2Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ec2" "describe-instances")
    (fn describe_instances
      ([o x1]
        (d/object-to-data
          (.describeInstances
            ^com.amazonaws.services.ec2.AmazonEC2Client o
            (d/data-to-object x1 com.amazonaws.services.ec2.model.DescribeInstancesRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ec2" "delete-security-group")
    {:related-class com.amazonaws.services.ec2.AmazonEC2Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.ec2.AmazonEC2Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ec2" "delete-security-group")
    (fn delete_security_group
      ([o x1]
        (d/object-to-data
          (.deleteSecurityGroup
            ^com.amazonaws.services.ec2.AmazonEC2Client o
            (d/data-to-object x1 com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ec2" "describe-security-groups")
    {:related-class com.amazonaws.services.ec2.AmazonEC2Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.ec2.AmazonEC2Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ec2" "describe-security-groups")
    (fn describe_security_groups
      ([o x1]
        (d/object-to-data
          (.describeSecurityGroups
            ^com.amazonaws.services.ec2.AmazonEC2Client o
            (d/data-to-object
              x1
              com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ec2" "create-security-group")
    {:related-class com.amazonaws.services.ec2.AmazonEC2Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.ec2.AmazonEC2Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ec2" "create-security-group")
    (fn create_security_group
      ([o x1]
        (d/object-to-data
          (.createSecurityGroup
            ^com.amazonaws.services.ec2.AmazonEC2Client o
            (d/data-to-object x1 com.amazonaws.services.ec2.model.CreateSecurityGroupRequest))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.ec2" "authorize-security-group-ingress")
    {:related-class com.amazonaws.services.ec2.AmazonEC2Client,
     :arglists
     (clojure.core/list [(.withMeta 'o {:tag 'com.amazonaws.services.ec2.AmazonEC2Client}) 'x1]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.ec2" "authorize-security-group-ingress")
    (fn authorize_security_group_ingress
      ([o x1]
        (d/object-to-data
          (.authorizeSecurityGroupIngress
            ^com.amazonaws.services.ec2.AmazonEC2Client o
            (d/data-to-object
              x1
              com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest))))))
  (defn create-tags
    ([m]
      (map
        (fn fn__31877
          ([p__31876]
            (let [vec__31878 p__31876
                  k (nth vec__31878 (int 0) nil)
                  v (nth vec__31878 (int 1) nil)]
              (com.amazonaws.services.ec2.model.Tag. (name k) (name v)))))
        m)))
  (reset-meta!
    #'create-tags
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'create-tags :ns *ns*))
  (def tag-resources
   (fn tag_resources
     ([client tags & resources]
       (.createTags
         ^com.amazonaws.services.ec2.AmazonEC2Client client
         (com.amazonaws.services.ec2.model.CreateTagsRequest.
           ^java.util.List resources
           (create-tags tags))))))
  (reset-meta!
    #'tag-resources
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'client {:tag 'AmazonEC2Client}) 'tags '& 'resources]),
       :column (int 1)}
      :name
      'tag-resources
      :ns
      *ns*))
  (def authorize-security-group-ingress-command
   (fn authorize_security_group_ingress_command
     ([p__31884]
       (let [map__31885 p__31884
             map__31885 (if (seq? map__31885)
                          (if (next map__31885)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31885))
                            (if (seq map__31885) (first map__31885) {}))
                          map__31885)
             group_name (get map__31885 :group-name)
             address (get map__31885 :address)
             protocol (get map__31885 :protocol)
             port (get map__31885 :port)
             s__6419__auto__ (java.io.StringWriter.)]
         (binding [*out* s__6419__auto__]
           (do
             (println
               (authorize-security-group-ingress
                 (client)
                 {:groupName group_name,
                  :ipPermissions
                  [{:ipProtocol protocol, :toPort port, :fromPort port, :ipRanges [address]}]}))
             (str s__6419__auto__)))))))
  (reset-meta!
    #'authorize-security-group-ingress-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['group-name 'address 'protocol 'port]}]),
       :column (int 1)}
      :name
      'authorize-security-group-ingress-command
      :ns
      *ns*))
  (def create-security-group-command
   (fn create_security_group_command
     ([p__31888]
       (let [map__31889 p__31888
             map__31889 (if (seq? map__31889)
                          (if (next map__31889)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31889))
                            (if (seq map__31889) (first map__31889) {}))
                          map__31889)
             group_name (get map__31889 :group-name)
             description (get map__31889 :description)
             s__6419__auto__ (java.io.StringWriter.)]
         (binding [*out* s__6419__auto__]
           (do
             (println
               (create-security-group (client) {:groupName group_name, :description description}))
             (str s__6419__auto__)))))))
  (reset-meta!
    #'create-security-group-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['group-name 'description]}]), :column (int 1)}
      :name
      'create-security-group-command
      :ns
      *ns*))
  (def security-group-exists?
   (fn security_group_exists_QMARK_
     ([ec2_client group_name]
       (boolean
         (seq
           (:securityGroups
             (describe-security-groups
               ec2_client
               {:filters [{:name "group-name", :values [group_name]}]})))))))
  (reset-meta!
    #'security-group-exists?
    (assoc
      {:arglists (clojure.core/list ['ec2-client 'group-name]), :column (int 1)}
      :name
      'security-group-exists?
      :ns
      *ns*))
  (def ensure-security-group
   (fn ensure_security_group
     ([ec2_client group_name description]
       (if (security-group-exists? ec2_client group_name)
         :already-exists
         (do
           (create-security-group ec2_client {:groupName group_name, :description description})
           :created)))))
  (reset-meta!
    #'ensure-security-group
    (assoc
      {:arglists (clojure.core/list ['ec2-client 'group-name 'description]), :column (int 1)}
      :name
      'ensure-security-group
      :ns
      *ns*))
  (def group-id->group-name
   (fn group_id__GT_group_name
     ([ec2_client group_id]
       (get-in
         (describe-security-groups ec2_client {:filters [{:name "group-id", :values [group_id]}]})
         [:securityGroups 0 :groupName]))))
  (reset-meta!
    #'group-id->group-name
    (assoc
      {:arglists (clojure.core/list ['ec2-client 'group-id]), :column (int 1)}
      :name
      'group-id->group-name
      :ns
      *ns*))
  (def ensure-group-name
   (fn ensure_group_name
     ([ec2_client ingress]
       (update-in
         ingress
         [:userIdGroupPairs]
         (fn fn__31896
           ([group]
             (mapv
               (fn fn__31897
                 ([p1__31895#]
                   (let [group_id (:groupId p1__31895#)]
                     (dissoc
                       (assoc
                         p1__31895#
                         :groupName
                         (:groupName p1__31895# (group-id->group-name ec2_client group_id)))
                       :groupId))))
               group)))))))
  (reset-meta!
    #'ensure-group-name
    (assoc
      {:arglists (clojure.core/list ['ec2-client 'ingress]), :column (int 1)}
      :name
      'ensure-group-name
      :ns
      *ns*))
  (def ingresses
   (fn ingresses
     ([ec2_client security_group_desc pred]
       (let [permissions (map
                           (partial ensure-group-name ec2_client)
                           (:ipPermissions (first (:securityGroups security_group_desc))))]
         (reduce
           (fn fn__31904
             ([m p__31903]
               (let [map__31905 p__31903
                     map__31905 (if (seq? map__31905)
                                  (if (next map__31905)
                                    (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                      (to-array map__31905))
                                    (if (seq map__31905) (first map__31905) {}))
                                  map__31905)
                     ipRanges (get map__31905 :ipRanges)
                     userIdGroupPairs (get map__31905 :userIdGroupPairs)]
                 (update-in
                   (update-in
                     m
                     [:ipRanges]
                     (fn fn__31906 ([p1__31901#] (into p1__31901# ipRanges))))
                   [:userIdGroupPairs]
                   (fn fn__31908 ([p1__31902#] (into p1__31902# userIdGroupPairs)))))))
           {:ipRanges #{}, :userIdGroupPairs #{}}
           (filter pred permissions))))))
  (reset-meta!
    #'ingresses
    (assoc
      {:arglists (clojure.core/list ['ec2-client 'security-group-desc 'pred]), :column (int 1)}
      :name
      'ingresses
      :ns
      *ns*))
  (defn novel-ingresses
    ([ingresses ingress]
      (let [new_cidr (fn new_cidr
                       ([p1__31912#] (not (contains? (:ipRanges ingresses) p1__31912#))))
            new_group (fn new_group
                        ([p1__31913#] (not (contains? (:userIdGroupPairs ingresses) p1__31913#))))
            novel (update-in
                    (update-in ingress [:ipRanges] (partial filterv new_cidr))
                    [:userIdGroupPairs]
                    (partial filterv new_group))]
        (when (or (seq (:ipRanges novel)) (seq (:userIdGroupPairs novel))) novel))))
  (reset-meta!
    #'novel-ingresses
    (assoc
      {:arglists (clojure.core/list ['ingresses 'ingress]), :column (int 1)}
      :name
      'novel-ingresses
      :ns
      *ns*))
  (def ensure-ingress
   (fn ensure_ingress
     ([ec2_client group_name ingress]
       (let [desc (describe-security-groups
                    ec2_client
                    {:filters [{:name "group-name", :values [group_name]}]})
             match_map (dissoc ingress :userIdGroupPairs :ipRanges)
             pred (fn pred ([p1__31920#] (= match_map (select-keys p1__31920# (keys match_map)))))
             temp__5804__auto__ (novel-ingresses (ingresses ec2_client desc pred) ingress)]
         (when temp__5804__auto__
           (let [novel temp__5804__auto__]
             (authorize-security-group-ingress
               ec2_client
               {:groupName group_name, :ipPermissions [ingress]})
             (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.ec2")]
               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                 (.info
                   ^org.slf4j.Logger logger
                   (logger/process {:event :ec2/ensure-ingress, :group group_name, :added novel})))
               nil)
             novel))))))
  (reset-meta!
    #'ensure-ingress
    (assoc
      {:arglists (clojure.core/list ['ec2-client 'group-name 'ingress]), :column (int 1)}
      :name
      'ensure-ingress
      :ns
      *ns*)))