(do
  (clojure.core/in-ns 'datomic.iam)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)])
      (clojure.core/require
        ['datomic.datafy :as 'd]
        ['datomic.common :as 'common]
        ['datomic.cli :as 'cli]
        ['datomic.aws :as 'aws]
        ['clojure.data.json :as 'json])
      (clojure.core/import
        'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient)
      (clojure.core/import 'com.amazonaws.services.identitymanagement.model.ListUsersRequest)
      (clojure.core/import 'com.amazonaws.services.identitymanagement.model.GetUserRequest)
      (clojure.core/import 'com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest)))
  (when-not (.equals 'datomic.iam 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.iam))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)])
        (clojure.core/require
          ['datomic.datafy :as 'd]
          ['datomic.common :as 'common]
          ['datomic.cli :as 'cli]
          ['datomic.aws :as 'aws]
          ['clojure.data.json :as 'json])
        (clojure.core/import
          'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient)
        (clojure.core/import 'com.amazonaws.services.identitymanagement.model.ListUsersRequest)
        (clojure.core/import 'com.amazonaws.services.identitymanagement.model.GetUserRequest)
        (clojure.core/import
          'com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest))))
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
                       (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
                         (aws/credentials creds)
                         ^com.amazonaws.ClientConfiguration conf)
                       (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
                         (aws/credentials creds)
                         ^com.amazonaws.ClientConfiguration conf))
                     (if (instance?
                           com.amazonaws.auth.AWSCredentialsProvider
                           (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
                       (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
                         (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                         ^com.amazonaws.ClientConfiguration conf)
                       (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
                         (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)
                         ^com.amazonaws.ClientConfiguration conf)))]
          (cond
            override_endpoint (.setEndpoint
                                ^com.amazonaws.AmazonWebServiceClient conn
                                (str "http://" override_endpoint))
            region (do
                     (.setEndpoint
                       ^com.amazonaws.AmazonWebServiceClient conn
                       (aws/endpoint-for :iam region))))
          conn)
        (client creds)))
    ([creds]
      (if creds
        (if (instance? com.amazonaws.auth.AWSCredentialsProvider (aws/credentials creds))
          (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
            (aws/credentials creds))
          (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
            (aws/credentials creds)))
        (if (instance?
              com.amazonaws.auth.AWSCredentialsProvider
              (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
          (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
          (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
    ([]
      (if (instance?
            com.amazonaws.auth.AWSCredentialsProvider
            (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
        (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
        (com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient.
          (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.)))))
  (defmethod
    d/data-to-object
    [:map com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest]
    fn__27076
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:groupName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:groupName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector :userName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest.)]
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest o
              ^java.lang.String k)))
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest]
    fn__27079
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:roleName :requestCredentials :instanceProfileName
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
                 #{:roleName :requestCredentials :instanceProfileName :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest.)]
        (when (contains? m :roleName)
          (let [v (:roleName m) k (d/property-to-object (class o) :roleName v java.lang.String)]
            (.setRoleName
              ^com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest o
              ^java.lang.String k)))
        (when (contains? m :instanceProfileName)
          (let [v (:instanceProfileName m)
                k (d/property-to-object (class o) :instanceProfileName v java.lang.String)]
            (.setInstanceProfileName
              ^com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.CreateUserRequest]
    fn__27082
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:path :tags :permissionsBoundary :requestCredentials
                                     :sdkClientExecutionTimeout :generalProgressListener
                                     :sdkRequestTimeout :requestMetricCollector :userName
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:path :tags :permissionsBoundary :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector :userName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.CreateUserRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.CreateUserRequest.)]
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
        (when (contains? m :permissionsBoundary)
          (let [v (:permissionsBoundary m)
                k (d/property-to-object (class o) :permissionsBoundary v java.lang.String)]
            (.setPermissionsBoundary
              ^com.amazonaws.services.identitymanagement.model.CreateUserRequest o
              ^java.lang.String k)))
        (when (contains? m :path)
          (let [v (:path m) k (d/property-to-object (class o) :path v java.lang.String)]
            (.setPath
              ^com.amazonaws.services.identitymanagement.model.CreateUserRequest o
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
        (when (contains? m :tags)
          (let [v (:tags m) k (d/property-to-object (class o) :tags v java.util.Collection)]
            (.setTags
              ^com.amazonaws.services.identitymanagement.model.CreateUserRequest o
              ^java.util.Collection k)))
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.CreateUserRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.CreateRoleRequest]
    fn__27085
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:roleName :description :path :tags :permissionsBoundary
                                     :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :assumeRolePolicyDocument
                                     :maxSessionDuration :sdkRequestTimeout :requestMetricCollector
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:roleName :description :path :tags :permissionsBoundary :requestCredentials
                   :sdkClientExecutionTimeout :generalProgressListener :assumeRolePolicyDocument
                   :maxSessionDuration :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.CreateRoleRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.CreateRoleRequest.)]
        (when (contains? m :description)
          (let [v (:description m)
                k (d/property-to-object (class o) :description v java.lang.String)]
            (.setDescription
              ^com.amazonaws.services.identitymanagement.model.CreateRoleRequest o
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
        (when (contains? m :permissionsBoundary)
          (let [v (:permissionsBoundary m)
                k (d/property-to-object (class o) :permissionsBoundary v java.lang.String)]
            (.setPermissionsBoundary
              ^com.amazonaws.services.identitymanagement.model.CreateRoleRequest o
              ^java.lang.String k)))
        (when (contains? m :path)
          (let [v (:path m) k (d/property-to-object (class o) :path v java.lang.String)]
            (.setPath
              ^com.amazonaws.services.identitymanagement.model.CreateRoleRequest o
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
        (when (contains? m :assumeRolePolicyDocument)
          (let [v (:assumeRolePolicyDocument m)
                k (d/property-to-object (class o) :assumeRolePolicyDocument v java.lang.String)]
            (.setAssumeRolePolicyDocument
              ^com.amazonaws.services.identitymanagement.model.CreateRoleRequest o
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
        (when (contains? m :tags)
          (let [v (:tags m) k (d/property-to-object (class o) :tags v java.util.Collection)]
            (.setTags
              ^com.amazonaws.services.identitymanagement.model.CreateRoleRequest o
              ^java.util.Collection k)))
        (when (contains? m :maxSessionDuration)
          (let [v (:maxSessionDuration m)
                k (d/property-to-object (class o) :maxSessionDuration v java.lang.Integer)]
            (.setMaxSessionDuration
              ^com.amazonaws.services.identitymanagement.model.CreateRoleRequest o
              ^java.lang.Integer k)))
        (when (contains? m :roleName)
          (let [v (:roleName m) k (d/property-to-object (class o) :roleName v java.lang.String)]
            (.setRoleName
              ^com.amazonaws.services.identitymanagement.model.CreateRoleRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest]
    fn__27088
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:path :tags :requestCredentials :instanceProfileName
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
                 #{:path :tags :requestCredentials :instanceProfileName :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest.)]
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
        (when (contains? m :path)
          (let [v (:path m) k (d/property-to-object (class o) :path v java.lang.String)]
            (.setPath
              ^com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest o
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
        (when (contains? m :tags)
          (let [v (:tags m) k (d/property-to-object (class o) :tags v java.util.Collection)]
            (.setTags
              ^com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest o
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
        (when (contains? m :instanceProfileName)
          (let [v (:instanceProfileName m)
                k (d/property-to-object (class o) :instanceProfileName v java.lang.String)]
            (.setInstanceProfileName
              ^com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest]
    fn__27091
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :userName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest.)]
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.CreateGroupRequest]
    fn__27094
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:path :groupName :requestCredentials
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
                 #{:path :groupName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.CreateGroupRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.CreateGroupRequest.)]
        (when (contains? m :path)
          (let [v (:path m) k (d/property-to-object (class o) :path v java.lang.String)]
            (.setPath
              ^com.amazonaws.services.identitymanagement.model.CreateGroupRequest o
              ^java.lang.String k)))
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.identitymanagement.model.CreateGroupRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest]
    fn__27097
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:accessKeyId :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:accessKeyId :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector :userName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest.)]
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest o
              ^java.lang.String k)))
        (when (contains? m :accessKeyId)
          (let [v (:accessKeyId m)
                k (d/property-to-object (class o) :accessKeyId v java.lang.String)]
            (.setAccessKeyId
              ^com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.DeleteGroupRequest]
    fn__27100
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:groupName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:groupName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.DeleteGroupRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.DeleteGroupRequest.)]
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.identitymanagement.model.DeleteGroupRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.DeleteUserRequest]
    fn__27103
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :userName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.DeleteUserRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.DeleteUserRequest.)]
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.DeleteUserRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest]
    fn__27106
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :policyName
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :userName :policyName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest.)]
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest o
              ^java.lang.String k)))
        (when (contains? m :policyName)
          (let [v (:policyName m)
                k (d/property-to-object (class o) :policyName v java.lang.String)]
            (.setPolicyName
              ^com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest]
    fn__27109
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:groupName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :policyName
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:groupName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector :policyName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest.)]
        (when (contains? m :policyName)
          (let [v (:policyName m)
                k (d/property-to-object (class o) :policyName v java.lang.String)]
            (.setPolicyName
              ^com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest o
              ^java.lang.String k)))
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest]
    fn__27112
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :instanceProfileName
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
                 #{:requestCredentials :instanceProfileName :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest.)]
        (when (contains? m :instanceProfileName)
          (let [v (:instanceProfileName m)
                k (d/property-to-object (class o) :instanceProfileName v java.lang.String)]
            (.setInstanceProfileName
              ^com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.GetUserRequest]
    fn__27115
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :userName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.identitymanagement.model.GetUserRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.GetUserRequest.)]
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.GetUserRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.GetRoleRequest]
    fn__27118
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:roleName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:roleName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.identitymanagement.model.GetRoleRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.GetRoleRequest.)]
        (when (contains? m :roleName)
          (let [v (:roleName m) k (d/property-to-object (class o) :roleName v java.lang.String)]
            (.setRoleName
              ^com.amazonaws.services.identitymanagement.model.GetRoleRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest]
    fn__27121
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:marker :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :requestCredentialsProvider
                                     :maxItems}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:marker :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :userName :requestCredentialsProvider
                   :maxItems},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest.)]
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
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker
              ^com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest o
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
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest o
              ^java.lang.String k)))
        (when (contains? m :maxItems)
          (let [v (:maxItems m) k (d/property-to-object (class o) :maxItems v java.lang.Integer)]
            (.setMaxItems
              ^com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.ListUserPoliciesRequest]
    fn__27124
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:marker :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :requestCredentialsProvider
                                     :maxItems}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:marker :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :userName :requestCredentialsProvider
                   :maxItems},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.ListUserPoliciesRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.ListUserPoliciesRequest.)]
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
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker
              ^com.amazonaws.services.identitymanagement.model.ListUserPoliciesRequest o
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
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.ListUserPoliciesRequest o
              ^java.lang.String k)))
        (when (contains? m :maxItems)
          (let [v (:maxItems m) k (d/property-to-object (class o) :maxItems v java.lang.Integer)]
            (.setMaxItems
              ^com.amazonaws.services.identitymanagement.model.ListUserPoliciesRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest]
    fn__27127
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:roleName :marker :requestCredentials
                                     :sdkClientExecutionTimeout :generalProgressListener
                                     :sdkRequestTimeout :requestMetricCollector
                                     :requestCredentialsProvider :maxItems}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:roleName :marker :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector
                   :requestCredentialsProvider :maxItems},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest.)]
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
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker
              ^com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest o
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
        (when (contains? m :roleName)
          (let [v (:roleName m) k (d/property-to-object (class o) :roleName v java.lang.String)]
            (.setRoleName
              ^com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest o
              ^java.lang.String k)))
        (when (contains? m :maxItems)
          (let [v (:maxItems m) k (d/property-to-object (class o) :maxItems v java.lang.Integer)]
            (.setMaxItems
              ^com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest]
    fn__27130
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:policyDocument :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :policyName
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:policyDocument :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector :userName
                   :policyName :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest.)]
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
        (when (contains? m :policyName)
          (let [v (:policyName m)
                k (d/property-to-object (class o) :policyName v java.lang.String)]
            (.setPolicyName
              ^com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest o
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
        (when (contains? m :policyDocument)
          (let [v (:policyDocument m)
                k (d/property-to-object (class o) :policyDocument v java.lang.String)]
            (.setPolicyDocument
              ^com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest o
              ^java.lang.String k)))
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest]
    fn__27133
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:roleName :policyDocument :requestCredentials
                                     :sdkClientExecutionTimeout :generalProgressListener
                                     :sdkRequestTimeout :requestMetricCollector :policyName
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:roleName :policyDocument :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector :policyName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest.)]
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
        (when (contains? m :policyName)
          (let [v (:policyName m)
                k (d/property-to-object (class o) :policyName v java.lang.String)]
            (.setPolicyName
              ^com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest o
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
        (when (contains? m :policyDocument)
          (let [v (:policyDocument m)
                k (d/property-to-object (class o) :policyDocument v java.lang.String)]
            (.setPolicyDocument
              ^com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest o
              ^java.lang.String k)))
        (when (contains? m :roleName)
          (let [v (:roleName m) k (d/property-to-object (class o) :roleName v java.lang.String)]
            (.setRoleName
              ^com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.PutGroupPolicyRequest]
    fn__27136
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:policyDocument :groupName :requestCredentials
                                     :sdkClientExecutionTimeout :generalProgressListener
                                     :sdkRequestTimeout :requestMetricCollector :policyName
                                     :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:policyDocument :groupName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector :policyName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.PutGroupPolicyRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.PutGroupPolicyRequest.)]
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
        (when (contains? m :policyName)
          (let [v (:policyName m)
                k (d/property-to-object (class o) :policyName v java.lang.String)]
            (.setPolicyName
              ^com.amazonaws.services.identitymanagement.model.PutGroupPolicyRequest o
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
        (when (contains? m :policyDocument)
          (let [v (:policyDocument m)
                k (d/property-to-object (class o) :policyDocument v java.lang.String)]
            (.setPolicyDocument
              ^com.amazonaws.services.identitymanagement.model.PutGroupPolicyRequest o
              ^java.lang.String k)))
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.identitymanagement.model.PutGroupPolicyRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest]
    fn__27139
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:groupName :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :userName :requestCredentialsProvider}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:groupName :requestCredentials :sdkClientExecutionTimeout
                   :generalProgressListener :sdkRequestTimeout :requestMetricCollector :userName
                   :requestCredentialsProvider},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest.)]
        (when (contains? m :userName)
          (let [v (:userName m) k (d/property-to-object (class o) :userName v java.lang.String)]
            (.setUserName
              ^com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest o
              ^java.lang.String k)))
        (when (contains? m :groupName)
          (let [v (:groupName m) k (d/property-to-object (class o) :groupName v java.lang.String)]
            (.setGroupName
              ^com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.ListUsersRequest]
    fn__27142
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:marker :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider :maxItems
                                     :pathPrefix}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:marker :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :requestCredentialsProvider :maxItems
                   :pathPrefix},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.identitymanagement.model.ListUsersRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.ListUsersRequest.)]
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
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker
              ^com.amazonaws.services.identitymanagement.model.ListUsersRequest o
              ^java.lang.String k)))
        (when (contains? m :pathPrefix)
          (let [v (:pathPrefix m)
                k (d/property-to-object (class o) :pathPrefix v java.lang.String)]
            (.setPathPrefix
              ^com.amazonaws.services.identitymanagement.model.ListUsersRequest o
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
        (when (contains? m :maxItems)
          (let [v (:maxItems m) k (d/property-to-object (class o) :maxItems v java.lang.Integer)]
            (.setMaxItems
              ^com.amazonaws.services.identitymanagement.model.ListUsersRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.ListRolesRequest]
    fn__27145
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:marker :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider :maxItems
                                     :pathPrefix}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:marker :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :requestCredentialsProvider :maxItems
                   :pathPrefix},
                 :keys bad_ks,
                 :constructor com.amazonaws.services.identitymanagement.model.ListRolesRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.ListRolesRequest.)]
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
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker
              ^com.amazonaws.services.identitymanagement.model.ListRolesRequest o
              ^java.lang.String k)))
        (when (contains? m :pathPrefix)
          (let [v (:pathPrefix m)
                k (d/property-to-object (class o) :pathPrefix v java.lang.String)]
            (.setPathPrefix
              ^com.amazonaws.services.identitymanagement.model.ListRolesRequest o
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
        (when (contains? m :maxItems)
          (let [v (:maxItems m) k (d/property-to-object (class o) :maxItems v java.lang.Integer)]
            (.setMaxItems
              ^com.amazonaws.services.identitymanagement.model.ListRolesRequest o
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
    [:map com.amazonaws.services.identitymanagement.model.ListInstanceProfilesRequest]
    fn__27148
    ([m _]
      (let [temp__5804__auto__ (seq
                                 (remove
                                   #{:marker :requestCredentials :sdkClientExecutionTimeout
                                     :generalProgressListener :sdkRequestTimeout
                                     :requestMetricCollector :requestCredentialsProvider :maxItems
                                     :pathPrefix}
                                   (keys m)))]
        (when temp__5804__auto__
          (let [bad_ks temp__5804__auto__]
            (throw
              (ex-info
                (apply str "Unexpected keys " bad_ks)
                {:legal-keys
                 #{:marker :requestCredentials :sdkClientExecutionTimeout :generalProgressListener
                   :sdkRequestTimeout :requestMetricCollector :requestCredentialsProvider :maxItems
                   :pathPrefix},
                 :keys bad_ks,
                 :constructor
                 com.amazonaws.services.identitymanagement.model.ListInstanceProfilesRequest}))))
        nil)
      (let [o (com.amazonaws.services.identitymanagement.model.ListInstanceProfilesRequest.)]
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
        (when (contains? m :marker)
          (let [v (:marker m) k (d/property-to-object (class o) :marker v java.lang.String)]
            (.setMarker
              ^com.amazonaws.services.identitymanagement.model.ListInstanceProfilesRequest o
              ^java.lang.String k)))
        (when (contains? m :pathPrefix)
          (let [v (:pathPrefix m)
                k (d/property-to-object (class o) :pathPrefix v java.lang.String)]
            (.setPathPrefix
              ^com.amazonaws.services.identitymanagement.model.ListInstanceProfilesRequest o
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
        (when (contains? m :maxItems)
          (let [v (:maxItems m) k (d/property-to-object (class o) :maxItems v java.lang.Integer)]
            (.setMaxItems
              ^com.amazonaws.services.identitymanagement.model.ListInstanceProfilesRequest o
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
  (extend
    com.amazonaws.services.identitymanagement.model.AccessKey
    d/ObjectToData
    {:object-to-data
     (fn fn__27151
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getStatus
                                        ^com.amazonaws.services.identitymanagement.model.AccessKey o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:status (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getUserName
                                        ^com.amazonaws.services.identitymanagement.model.AccessKey o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:userName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCreateDate
                                        ^com.amazonaws.services.identitymanagement.model.AccessKey o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:createDate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAccessKeyId
                                        ^com.amazonaws.services.identitymanagement.model.AccessKey o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:accessKeyId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getSecretAccessKey
                                        ^com.amazonaws.services.identitymanagement.model.AccessKey o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:secretAccessKey (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.identitymanagement.model.AccessKeyMetadata
    d/ObjectToData
    {:object-to-data
     (fn fn__27163
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getStatus
                                        ^com.amazonaws.services.identitymanagement.model.AccessKeyMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:status (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getUserName
                                        ^com.amazonaws.services.identitymanagement.model.AccessKeyMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:userName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCreateDate
                                        ^com.amazonaws.services.identitymanagement.model.AccessKeyMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:createDate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAccessKeyId
                                        ^com.amazonaws.services.identitymanagement.model.AccessKeyMetadata o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:accessKeyId (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.identitymanagement.model.CreateAccessKeyResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27173
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getAccessKey
                                        ^com.amazonaws.services.identitymanagement.model.CreateAccessKeyResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:accessKey (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.CreateGroupResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27181
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getGroup
                                        ^com.amazonaws.services.identitymanagement.model.CreateGroupResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:group (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.CreateUserResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27189
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getUser
                                        ^com.amazonaws.services.identitymanagement.model.CreateUserResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:user (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.CreateRoleResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27197
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getRole
                                        ^com.amazonaws.services.identitymanagement.model.CreateRoleResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:role (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.CreateInstanceProfileResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27205
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getInstanceProfile
                                        ^com.amazonaws.services.identitymanagement.model.CreateInstanceProfileResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instanceProfile (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.GetInstanceProfileResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27213
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getInstanceProfile
                                        ^com.amazonaws.services.identitymanagement.model.GetInstanceProfileResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instanceProfile (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.GetUserResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27221
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getUser
                                        ^com.amazonaws.services.identitymanagement.model.GetUserResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:user (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.GetRoleResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27229
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getRole
                                        ^com.amazonaws.services.identitymanagement.model.GetRoleResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:role (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.ListAccessKeysResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27237
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getMarker
                                        ^com.amazonaws.services.identitymanagement.model.ListAccessKeysResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:marker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListAccessKeysResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:truncated (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAccessKeyMetadata
                                        ^com.amazonaws.services.identitymanagement.model.ListAccessKeysResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:accessKeyMetadata (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIsTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListAccessKeysResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:isTruncated (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.ListUsersResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27251
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getMarker
                                        ^com.amazonaws.services.identitymanagement.model.ListUsersResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:marker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListUsersResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:truncated (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIsTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListUsersResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:isTruncated (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getUsers
                                        ^com.amazonaws.services.identitymanagement.model.ListUsersResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:users (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.ListRolesResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27265
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getMarker
                                        ^com.amazonaws.services.identitymanagement.model.ListRolesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:marker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRoles
                                        ^com.amazonaws.services.identitymanagement.model.ListRolesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:roles (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListRolesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:truncated (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIsTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListRolesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:isTruncated (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.ListInstanceProfilesResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27279
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getMarker
                                        ^com.amazonaws.services.identitymanagement.model.ListInstanceProfilesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:marker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListInstanceProfilesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:truncated (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getInstanceProfiles
                                        ^com.amazonaws.services.identitymanagement.model.ListInstanceProfilesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instanceProfiles (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIsTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListInstanceProfilesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:isTruncated (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.ListUserPoliciesResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27293
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getMarker
                                        ^com.amazonaws.services.identitymanagement.model.ListUserPoliciesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:marker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListUserPoliciesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:truncated (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPolicyNames
                                        ^com.amazonaws.services.identitymanagement.model.ListUserPoliciesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:policyNames (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIsTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListUserPoliciesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:isTruncated (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult
    d/ObjectToData
    {:object-to-data
     (fn fn__27307
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getMarker
                                        ^com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:marker (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.isTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:truncated (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPolicyNames
                                        ^com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:policyNames (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getIsTruncated
                                        ^com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:isTruncated (d/object-to-data-wrapper v__19409__auto__)])))
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
    com.amazonaws.services.identitymanagement.model.Group
    d/ObjectToData
    {:object-to-data
     (fn fn__27321
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getArn
                                        ^com.amazonaws.services.identitymanagement.model.Group o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:arn (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCreateDate
                                        ^com.amazonaws.services.identitymanagement.model.Group o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:createDate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGroupName
                                        ^com.amazonaws.services.identitymanagement.model.Group o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getGroupId
                                        ^com.amazonaws.services.identitymanagement.model.Group o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:groupId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPath
                                        ^com.amazonaws.services.identitymanagement.model.Group o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:path (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.identitymanagement.model.User
    d/ObjectToData
    {:object-to-data
     (fn fn__27333
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTags
                                        ^com.amazonaws.services.identitymanagement.model.User o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tags (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getUserName
                                        ^com.amazonaws.services.identitymanagement.model.User o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:userName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getArn
                                        ^com.amazonaws.services.identitymanagement.model.User o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:arn (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCreateDate
                                        ^com.amazonaws.services.identitymanagement.model.User o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:createDate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPermissionsBoundary
                                        ^com.amazonaws.services.identitymanagement.model.User o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:permissionsBoundary (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getUserId
                                        ^com.amazonaws.services.identitymanagement.model.User o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:userId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPasswordLastUsed
                                        ^com.amazonaws.services.identitymanagement.model.User o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:passwordLastUsed (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPath
                                        ^com.amazonaws.services.identitymanagement.model.User o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:path (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.identitymanagement.model.Role
    d/ObjectToData
    {:object-to-data
     (fn fn__27351
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTags
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tags (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getDescription
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:description (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getArn
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:arn (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCreateDate
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:createDate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRoleName
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:roleName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getAssumeRolePolicyDocument
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:assumeRolePolicyDocument (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getMaxSessionDuration
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:maxSessionDuration (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPermissionsBoundary
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:permissionsBoundary (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRoleId
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:roleId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRoleLastUsed
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:roleLastUsed (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPath
                                        ^com.amazonaws.services.identitymanagement.model.Role o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:path (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (extend
    com.amazonaws.services.identitymanagement.model.InstanceProfile
    d/ObjectToData
    {:object-to-data
     (fn fn__27375
       ([o]
         (apply
           hash-map
           (concat
             (let [temp__5804__auto__ (.getTags
                                        ^com.amazonaws.services.identitymanagement.model.InstanceProfile o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:tags (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getRoles
                                        ^com.amazonaws.services.identitymanagement.model.InstanceProfile o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:roles (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getArn
                                        ^com.amazonaws.services.identitymanagement.model.InstanceProfile o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:arn (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getInstanceProfileId
                                        ^com.amazonaws.services.identitymanagement.model.InstanceProfile o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instanceProfileId (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getCreateDate
                                        ^com.amazonaws.services.identitymanagement.model.InstanceProfile o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:createDate (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getInstanceProfileName
                                        ^com.amazonaws.services.identitymanagement.model.InstanceProfile o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:instanceProfileName (d/object-to-data-wrapper v__19409__auto__)])))
             (let [temp__5804__auto__ (.getPath
                                        ^com.amazonaws.services.identitymanagement.model.InstanceProfile o)]
               (when temp__5804__auto__
                 (let [v__19409__auto__ temp__5804__auto__]
                   [:path (d/object-to-data-wrapper v__19409__auto__)])))))))})
  (defn get-user
    ([o x1]
      (d/object-to-data
        (.getUser
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object x1 com.amazonaws.services.identitymanagement.model.GetUserRequest)))))
  (reset-meta!
    #'get-user
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'get-user
      :ns
      *ns*))
  (defn get-role
    ([o x1]
      (d/object-to-data
        (.getRole
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object x1 com.amazonaws.services.identitymanagement.model.GetRoleRequest)))))
  (reset-meta!
    #'get-role
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'get-role
      :ns
      *ns*))
  (defn get-instance-profile
    ([o x1]
      (d/object-to-data
        (.getInstanceProfile
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest)))))
  (reset-meta!
    #'get-instance-profile
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'get-instance-profile
      :ns
      *ns*))
  (defn create-instance-profile
    ([o x1]
      (d/object-to-data
        (.createInstanceProfile
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest)))))
  (reset-meta!
    #'create-instance-profile
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'create-instance-profile
      :ns
      *ns*))
  (defn create-user
    ([o x1]
      (d/object-to-data
        (.createUser
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.CreateUserRequest)))))
  (reset-meta!
    #'create-user
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'create-user
      :ns
      *ns*))
  (defn create-role
    ([o x1]
      (d/object-to-data
        (.createRole
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.CreateRoleRequest)))))
  (reset-meta!
    #'create-role
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'create-role
      :ns
      *ns*))
  (defn list-users
    ([o x1]
      (d/object-to-data
        (.listUsers
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.ListUsersRequest)))))
  (reset-meta!
    #'list-users
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'list-users
      :ns
      *ns*))
  (defn list-roles
    ([o x1]
      (d/object-to-data
        (.listRoles
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.ListRolesRequest)))))
  (reset-meta!
    #'list-roles
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'list-roles
      :ns
      *ns*))
  (defn list-instance-profiles
    ([o x1]
      (d/object-to-data
        (.listInstanceProfiles
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.ListInstanceProfilesRequest)))))
  (reset-meta!
    #'list-instance-profiles
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'list-instance-profiles
      :ns
      *ns*))
  (defn list-access-keys
    ([o x1]
      (d/object-to-data
        (.listAccessKeys
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest)))))
  (reset-meta!
    #'list-access-keys
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'list-access-keys
      :ns
      *ns*))
  (defn put-user-policy
    ([o x1]
      (d/object-to-data
        (.putUserPolicy
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest)))))
  (reset-meta!
    #'put-user-policy
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'put-user-policy
      :ns
      *ns*))
  (defn put-role-policy
    ([o x1]
      (d/object-to-data
        (.putRolePolicy
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest)))))
  (reset-meta!
    #'put-role-policy
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'put-role-policy
      :ns
      *ns*))
  (defn put-group-policy
    ([o x1]
      (d/object-to-data
        (.putGroupPolicy
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.PutGroupPolicyRequest)))))
  (reset-meta!
    #'put-group-policy
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'put-group-policy
      :ns
      *ns*))
  (defn create-access-key
    ([o x1]
      (d/object-to-data
        (.createAccessKey
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest)))))
  (reset-meta!
    #'create-access-key
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'create-access-key
      :ns
      *ns*))
  (defn delete-access-key
    ([o x1]
      (d/object-to-data
        (.deleteAccessKey
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest)))))
  (reset-meta!
    #'delete-access-key
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'delete-access-key
      :ns
      *ns*))
  (defn delete-user-policy
    ([o x1]
      (d/object-to-data
        (.deleteUserPolicy
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest)))))
  (reset-meta!
    #'delete-user-policy
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'delete-user-policy
      :ns
      *ns*))
  (defn delete-group-policy
    ([o x1]
      (d/object-to-data
        (.deleteGroupPolicy
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest)))))
  (reset-meta!
    #'delete-group-policy
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'delete-group-policy
      :ns
      *ns*))
  (defn list-user-policies
    ([o x1]
      (d/object-to-data
        (.listUserPolicies
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.ListUserPoliciesRequest)))))
  (reset-meta!
    #'list-user-policies
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'list-user-policies
      :ns
      *ns*))
  (defn list-role-policies
    ([o x1]
      (d/object-to-data
        (.listRolePolicies
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest)))))
  (reset-meta!
    #'list-role-policies
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'list-role-policies
      :ns
      *ns*))
  (defn add-role-to-instance-profile
    ([o x1]
      (d/object-to-data
        (.addRoleToInstanceProfile
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest)))))
  (reset-meta!
    #'add-role-to-instance-profile
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'add-role-to-instance-profile
      :ns
      *ns*))
  (defn create-group
    ([o x1]
      (d/object-to-data
        (.createGroup
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.CreateGroupRequest)))))
  (reset-meta!
    #'create-group
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'create-group
      :ns
      *ns*))
  (defn delete-group
    ([o x1]
      (d/object-to-data
        (.deleteGroup
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.DeleteGroupRequest)))))
  (reset-meta!
    #'delete-group
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'delete-group
      :ns
      *ns*))
  (defn delete-user
    ([o x1]
      (d/object-to-data
        (.deleteUser
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.DeleteUserRequest)))))
  (reset-meta!
    #'delete-user
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'delete-user
      :ns
      *ns*))
  (defn add-user-to-group
    ([o x1]
      (d/object-to-data
        (.addUserToGroup
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest)))))
  (reset-meta!
    #'add-user-to-group
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'add-user-to-group
      :ns
      *ns*))
  (defn remove-user-from-group
    ([o x1]
      (d/object-to-data
        (.removeUserFromGroup
          ^com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient o
          (d/data-to-object
            x1
            com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest)))))
  (reset-meta!
    #'remove-user-from-group
    (assoc
      {:related-class com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient,
       :arglists
       (clojure.core/list
         [(.withMeta
            'o
            {:tag 'com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient})
          'x1]),
       :column 1}
      :name
      'remove-user-from-group
      :ns
      *ns*))
  (defn arn->account-id ([arn] (second (re-find #"arn:aws:iam::(\d+)" arn))))
  (defn user-arn ([account_id user_name] (str "arn:aws:iam::" account_id ":user/" user_name)))
  (def accounts-ref (atom {}))
  (reset-meta! #'accounts-ref (assoc {:private true, :column 1} :name 'accounts-ref :ns *ns*))
  (defn get-account-id ([client] (arn->account-id (get-in (get-user client {}) [:user :arn]))))
  (defn delete-all-access-keys
    ([iam username]
      (let [ks (list-access-keys iam {:userName username})]
        (common/mapk
          (fn fn__27420
            ([p1__27419#]
              (try
                (delete-access-key iam {:userName username, :accessKeyId p1__27419#})
                (catch java.lang.Throwable e e))))
          (map :accessKeyId (:accessKeyMetadata ks))))))
  (defn delete-policies
    ([iam username policynames]
      (common/mapk
        (fn fn__27424
          ([p1__27423#]
            (try
              (delete-user-policy iam {:userName username, :policyName p1__27423#})
              (catch java.lang.Throwable e e))))
        policynames)))
  (defn delete-all-policies
    ([iam username]
      (let [ks (list-user-policies iam {:userName username})]
        (delete-policies iam username (:policyNames ks)))))
  (defn deep-delete-account
    ([iam username]
      (into
        []
        [:access-keys
         (try (delete-all-access-keys iam username) (catch java.lang.Throwable e e))
         :policies
         (try (delete-all-policies iam username) (catch java.lang.Throwable e e))
         :user
         (try (delete-user iam {:userName username}) (catch java.lang.Throwable e e))])))
  (defn get-account-id-command ([_] (get-account-id (client))))
  (defn create-user-command
    ([p__27436]
      (let [map__27437 p__27436
            map__27437 (if (seq? map__27437)
                         (if (next map__27437)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27437))
                           (if (seq map__27437) (first map__27437) {}))
                         map__27437)
            user_name (get map__27437 :user-name)]
        (try
          (let [s__6419__auto__ (java.io.StringWriter.)]
            (binding [*out* s__6419__auto__]
              (do
                (println (:user (create-user (client) {:userName user_name})))
                (str s__6419__auto__))))
          (catch
            com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException
            _
            nil)))))
  (defn create-group-command
    ([p__27440]
      (let [map__27441 p__27440
            map__27441 (if (seq? map__27441)
                         (if (next map__27441)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27441))
                           (if (seq map__27441) (first map__27441) {}))
                         map__27441)
            group_name (get map__27441 :group-name)
            results (:group (create-group (client) {:groupName group_name}))
            s__6419__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6419__auto__] (do (println results) (str s__6419__auto__))))))
  (defn create-access-key-command
    ([p__27444]
      (let [map__27445 p__27444
            map__27445 (if (seq? map__27445)
                         (if (next map__27445)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27445))
                           (if (seq map__27445) (first map__27445) {}))
                         map__27445)
            user_name (get map__27445 :user-name)]
        (try
          (let [new_creds (:accessKey (create-access-key (client) {:userName user_name}))
                s__6419__auto__ (java.io.StringWriter.)]
            (binding [*out* s__6419__auto__]
              (do
                (println (str (:accessKeyId new_creds) " " (:secretAccessKey new_creds)))
                (str s__6419__auto__))))
          (catch
            com.amazonaws.services.identitymanagement.model.LimitExceededException
            _
            (cli/fail
              (let [s__6419__auto__ (java.io.StringWriter.)]
                (binding [*out* s__6419__auto__]
                  (do
                    (println "**ERROR**")
                    (println "Cannot create additional access keys for user ${USERNAME}.")
                    (println
                      "Either delete one of the existing access keys, or use an existing access-key-id/secret-key pair.")
                    (println
                      "Go to the the IAM tab of your AWS Console: https://console.aws.amazon.com/iam/home#s=Users")
                    (println "and follow the path below to delete an access key:")
                    (println
                      "Iam Home > Users > ${USERNAME} > Security Credentials > Manage Access Keys")
                    (println "****")
                    (str s__6419__auto__))))))))))
  (defn create-credentials-command
    ([p__27451]
      (let [map__27452 p__27451
            map__27452 (if (seq? map__27452)
                         (if (next map__27452)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27452))
                           (if (seq map__27452) (first map__27452) {}))
                         map__27452)
            prefix (get map__27452 :prefix)
            iam_client (client)
            peer_user_name {:userName (str prefix "-peer")}
            dynamo_user_name {:userName (str prefix "-transactor-dynamo")}
            metrics_user_name {:userName (str prefix "-transactor-metrics")}
            s__6419__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6419__auto__]
          (do
            (create-user iam_client peer_user_name)
            (println (str "peer.username=" (:userName peer_user_name)))
            (let [peer_access_key (:accessKey (create-access-key iam_client peer_user_name))]
              (println (str "peer.aws-access-key-id=" (:accessKeyId peer_access_key)))
              (println (str "peer.aws-secret-key=" (:secretAccessKey peer_access_key))))
            (create-user iam_client dynamo_user_name)
            (println (str "transactor.dynamo.username=" (:userName dynamo_user_name)))
            (let [dynamo_access_key (:accessKey (create-access-key iam_client dynamo_user_name))]
              (println
                (str "transactor.dynamo.aws-access-key-id=" (:accessKeyId dynamo_access_key)))
              (println
                (str "transactor.dynamo.aws-secret-key=" (:secretAccessKey dynamo_access_key))))
            (create-user iam_client metrics_user_name)
            (println (str "transactor.metrics.username=" (:userName metrics_user_name)))
            (let [metrics_access_key (:accessKey (create-access-key iam_client metrics_user_name))]
              (println
                (str "transactor.metrics.aws-access-key-id=" (:accessKeyId metrics_access_key)))
              (println
                (str "transactor.metrics.aws-secret-key=" (:secretAccessKey metrics_access_key))))
            (str s__6419__auto__))))))
  (defn dynamo-r-policy-command
    ([p__27455]
      (let [map__27456 p__27455
            map__27456 (if (seq? map__27456)
                         (if (next map__27456)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27456))
                           (if (seq map__27456) (first map__27456) {}))
                         map__27456)
            account_id (get map__27456 :account-id)
            table_name (get map__27456 :table-name)
            arn (str "arn:aws:dynamodb:*:" account_id ":table/" table_name)
            s__6419__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6419__auto__]
          (do
            (json/pprint
              {"Statement"
               [{"Effect" "Allow",
                 "Action"
                 ["dynamodb:GetItem" "dynamodb:BatchGetItem" "dynamodb:Scan" "dynamodb:Query"],
                 "Resource" arn}]}
              :escape-slash
              false)
            (str s__6419__auto__))))))
  (defn dynamo-rw-policy-command
    ([p__27459]
      (let [map__27460 p__27459
            map__27460 (if (seq? map__27460)
                         (if (next map__27460)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27460))
                           (if (seq map__27460) (first map__27460) {}))
                         map__27460)
            account_id (get map__27460 :account-id)
            table_name (get map__27460 :table-name)
            arn (str "arn:aws:dynamodb:*:" account_id ":table/" table_name)
            s__6419__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6419__auto__]
          (do
            (json/pprint
              {"Statement" [{"Effect" "Allow", "Action" ["dynamodb:*"], "Resource" arn}]}
              :escape-slash
              false)
            (str s__6419__auto__))))))
  (defn metrics-w-policy-command
    ([_] (metrics-w-policy-command))
    ([]
      (let [s__6419__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6419__auto__]
          (do
            (json/pprint
              {"Statement"
               [{"Effect" "Allow",
                 "Action" ["cloudwatch:PutMetricData" "cloudwatch:PutMetricDataBatch"],
                 "Resource" "*",
                 "Condition" {"Bool" {"aws:SecureTransport" "true"}}}]}
              :escape-slash
              false)
            (str s__6419__auto__))))))
  (defn s3-w-policy-command
    ([p__27465]
      (let [map__27466 p__27465
            map__27466 (if (seq? map__27466)
                         (if (next map__27466)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27466))
                           (if (seq map__27466) (first map__27466) {}))
                         map__27466)
            bucket_name (get map__27466 :bucket-name)
            arn (str "arn:aws:s3:::" bucket_name)
            s__6419__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6419__auto__]
          (do
            (json/pprint
              {"Statement"
               [{"Effect" "Allow", "Action" ["s3:PutObject"], "Resource" [arn (str arn "/*")]}]}
              :escape-slash
              false)
            (str s__6419__auto__))))))
  (defn assign-peer-user-command
    ([p__27469]
      (let [map__27470 p__27469
            map__27470 (if (seq? map__27470)
                         (if (next map__27470)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27470))
                           (if (seq map__27470) (first map__27470) {}))
                         map__27470)
            user_name (get map__27470 :user-name)
            table_name (get map__27470 :table-name)
            policy_name (str user_name "-" table_name "-peer")
            iam_client (client)
            policy_doc (dynamo-r-policy-command
                         {:account-id (get-account-id client), :table-name table_name})]
        (put-user-policy
          iam_client
          {:userName user_name, :policyName policy_name, :policyDocument policy_doc}))))
  (defn assign-transactor-dynamo-user-command
    ([p__27472]
      (let [map__27473 p__27472
            map__27473 (if (seq? map__27473)
                         (if (next map__27473)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27473))
                           (if (seq map__27473) (first map__27473) {}))
                         map__27473)
            user_name (get map__27473 :user-name)
            table_name (get map__27473 :table-name)
            policy_name (str user_name "-" table_name "-transactor-dynamo")
            iam_client (client)
            policy_doc (dynamo-rw-policy-command
                         {:account-id (get-account-id iam_client), :table-name table_name})]
        (put-user-policy
          iam_client
          {:userName user_name, :policyName policy_name, :policyDocument policy_doc}))))
  (defn assign-transactor-log-user-command
    ([p__27475]
      (let [map__27476 p__27475
            map__27476 (if (seq? map__27476)
                         (if (next map__27476)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27476))
                           (if (seq map__27476) (first map__27476) {}))
                         map__27476)
            user_name (get map__27476 :user-name)
            bucket_name (get map__27476 :bucket-name)
            policy_name (str user_name "-transactor-s3")
            iam_client (client)
            policy_doc (s3-w-policy-command {:bucket-name bucket_name})]
        (put-user-policy
          iam_client
          {:userName user_name, :policyName policy_name, :policyDocument policy_doc}))))
  (defn assign-transactor-metrics-user-command
    ([p__27478]
      (let [map__27479 p__27478
            map__27479 (if (seq? map__27479)
                         (if (next map__27479)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27479))
                           (if (seq map__27479) (first map__27479) {}))
                         map__27479)
            user_name (get map__27479 :user-name)
            policy_name (str user_name "-transactor-metrics")
            iam_client (client)
            policy_doc (metrics-w-policy-command)]
        (put-user-policy
          iam_client
          {:userName user_name, :policyName policy_name, :policyDocument policy_doc})))))