(do
  (clojure.core/in-ns 'datomic.iam)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)])
      (clojure.core/require
        ['datomic.cli :as 'cli]
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.aws.client.anomalizer :as 'izer]
        ['datomic.aws.client.api :as 'aws]
        ['datomic.core2.aws.helpers :as 'aws-helpers]
        ['clojure.data.json :as 'json])
      (clojure.core/import 'software.amazon.awssdk.services.iam.IamClient)))
  (when-not (.equals 'datomic.iam 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.iam))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)])
        (clojure.core/require
          ['datomic.cli :as 'cli]
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.aws.client.anomalizer :as 'izer]
          ['datomic.aws.client.api :as 'aws]
          ['datomic.core2.aws.helpers :as 'aws-helpers]
          ['clojure.data.json :as 'json])
        (clojure.core/import 'software.amazon.awssdk.services.iam.IamClient))))
  (defn client ([] (aws-helpers/sync-client (IamClient/builder) {})))
  (reset-meta!
    #'client
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'client :ns *ns*))
  (swap!
    izer/error-code-categories
    assoc
    "LimitExceededException"
    :cognitect.anomalies/busy
    "EntityAlreadyExistsException"
    :cognitect.anomalies/incorrect)
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :GetUser]
    fn__26810
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26811
         ([client26808 G__26809]
           (.getUser
             ^software.amazon.awssdk.services.iam.IamClient client26808
             ^software.amazon.awssdk.services.iam.model.GetUserRequest G__26809))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26813 ([] (software.amazon.awssdk.services.iam.model.GetUserRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :GetRole]
    fn__26818
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26819
         ([client26816 G__26817]
           (.getRole
             ^software.amazon.awssdk.services.iam.IamClient client26816
             ^software.amazon.awssdk.services.iam.model.GetRoleRequest G__26817))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26821 ([] (software.amazon.awssdk.services.iam.model.GetRoleRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :CreateInstanceProfile]
    fn__26826
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26827
         ([client26824 G__26825]
           (.createInstanceProfile
             ^software.amazon.awssdk.services.iam.IamClient client26824
             ^software.amazon.awssdk.services.iam.model.CreateInstanceProfileRequest G__26825))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26829
         ([] (software.amazon.awssdk.services.iam.model.CreateInstanceProfileRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :CreateUser]
    fn__26834
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26835
         ([client26832 G__26833]
           (.createUser
             ^software.amazon.awssdk.services.iam.IamClient client26832
             ^software.amazon.awssdk.services.iam.model.CreateUserRequest G__26833))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26837 ([] (software.amazon.awssdk.services.iam.model.CreateUserRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :CreateRole]
    fn__26842
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26843
         ([client26840 G__26841]
           (.createRole
             ^software.amazon.awssdk.services.iam.IamClient client26840
             ^software.amazon.awssdk.services.iam.model.CreateRoleRequest G__26841))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26845 ([] (software.amazon.awssdk.services.iam.model.CreateRoleRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :ListUsers]
    fn__26850
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26851
         ([client26848 G__26849]
           (.listUsers
             ^software.amazon.awssdk.services.iam.IamClient client26848
             ^software.amazon.awssdk.services.iam.model.ListUsersRequest G__26849))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26853 ([] (software.amazon.awssdk.services.iam.model.ListUsersRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :ListRoles]
    fn__26858
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26859
         ([client26856 G__26857]
           (.listRoles
             ^software.amazon.awssdk.services.iam.IamClient client26856
             ^software.amazon.awssdk.services.iam.model.ListRolesRequest G__26857))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26861 ([] (software.amazon.awssdk.services.iam.model.ListRolesRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :ListAccessKeys]
    fn__26866
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26867
         ([client26864 G__26865]
           (.listAccessKeys
             ^software.amazon.awssdk.services.iam.IamClient client26864
             ^software.amazon.awssdk.services.iam.model.ListAccessKeysRequest G__26865))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26869
         ([] (software.amazon.awssdk.services.iam.model.ListAccessKeysRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :PutUserPolicy]
    fn__26874
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26875
         ([client26872 G__26873]
           (.putUserPolicy
             ^software.amazon.awssdk.services.iam.IamClient client26872
             ^software.amazon.awssdk.services.iam.model.PutUserPolicyRequest G__26873))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26877
         ([] (software.amazon.awssdk.services.iam.model.PutUserPolicyRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :PutRolePolicy]
    fn__26882
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26883
         ([client26880 G__26881]
           (.putRolePolicy
             ^software.amazon.awssdk.services.iam.IamClient client26880
             ^software.amazon.awssdk.services.iam.model.PutRolePolicyRequest G__26881))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26885
         ([] (software.amazon.awssdk.services.iam.model.PutRolePolicyRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :CreateAccessKey]
    fn__26890
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26891
         ([client26888 G__26889]
           (.createAccessKey
             ^software.amazon.awssdk.services.iam.IamClient client26888
             ^software.amazon.awssdk.services.iam.model.CreateAccessKeyRequest G__26889))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26893
         ([] (software.amazon.awssdk.services.iam.model.CreateAccessKeyRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :DeleteAccessKey]
    fn__26898
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26899
         ([client26896 G__26897]
           (.deleteAccessKey
             ^software.amazon.awssdk.services.iam.IamClient client26896
             ^software.amazon.awssdk.services.iam.model.DeleteAccessKeyRequest G__26897))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26901
         ([] (software.amazon.awssdk.services.iam.model.DeleteAccessKeyRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :ListUserPolicies]
    fn__26906
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26907
         ([client26904 G__26905]
           (.listUserPolicies
             ^software.amazon.awssdk.services.iam.IamClient client26904
             ^software.amazon.awssdk.services.iam.model.ListUserPoliciesRequest G__26905))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26909
         ([] (software.amazon.awssdk.services.iam.model.ListUserPoliciesRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :ListRolePolicies]
    fn__26914
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26915
         ([client26912 G__26913]
           (.listRolePolicies
             ^software.amazon.awssdk.services.iam.IamClient client26912
             ^software.amazon.awssdk.services.iam.model.ListRolePoliciesRequest G__26913))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26917
         ([] (software.amazon.awssdk.services.iam.model.ListRolePoliciesRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :AddRoleToInstanceProfile]
    fn__26922
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26923
         ([client26920 G__26921]
           (.addRoleToInstanceProfile
             ^software.amazon.awssdk.services.iam.IamClient client26920
             ^software.amazon.awssdk.services.iam.model.AddRoleToInstanceProfileRequest G__26921))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26925
         ([]
           (software.amazon.awssdk.services.iam.model.AddRoleToInstanceProfileRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.iam.IamClient :CreateGroup]
    fn__26930
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__26931
         ([client26928 G__26929]
           (.createGroup
             ^software.amazon.awssdk.services.iam.IamClient client26928
             ^software.amazon.awssdk.services.iam.model.CreateGroupRequest G__26929))),
       :ret-mode :sync,
       :request-builder
       (fn fn__26933
         ([] (software.amazon.awssdk.services.iam.model.CreateGroupRequest/builder)))}))
  (defn arn->account-id ([arn] (second (re-find #"arn:aws:iam::(\d+)" arn))))
  (reset-meta!
    #'arn->account-id
    (assoc
      {:arglists (clojure.core/list ['arn]), :column (int 1)}
      :name
      'arn->account-id
      :ns
      *ns*))
  (defn get-account-id
    ([client] (arn->account-id (get-in (aws-helpers/invoke client {:op :GetUser}) [:User :Arn]))))
  (reset-meta!
    #'get-account-id
    (assoc
      {:arglists (clojure.core/list ['client]), :column (int 1)}
      :name
      'get-account-id
      :ns
      *ns*))
  (defn get-account-id-command ([_] (get-account-id (client))))
  (reset-meta!
    #'get-account-id-command
    (assoc
      {:arglists (clojure.core/list ['_]), :column (int 1)}
      :name
      'get-account-id-command
      :ns
      *ns*))
  (defn create-user-command
    ([p__26939]
      (let [map__26940 p__26939
            map__26940 (if (seq? map__26940)
                         (if (next map__26940)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26940))
                           (if (seq map__26940) (first map__26940) {}))
                         map__26940)
            user_name (get map__26940 :user-name)]
        (try
          (let [s__6444__auto__ (java.io.StringWriter.)]
            (binding [*out* s__6444__auto__]
              (do
                (println
                  (:User
                    (aws-helpers/invoke (client) {:op :CreateUser, :req {:UserName user_name}})))
                (str s__6444__auto__))))
          (catch
            java.lang.Exception
            ex
            (when-not (canom/conflict? (ex-data ex)) (throw ^java.lang.Throwable ex) nil))))))
  (reset-meta!
    #'create-user-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['user-name]}]), :column (int 1)}
      :name
      'create-user-command
      :ns
      *ns*))
  (defn create-group-command
    ([p__26943]
      (let [map__26944 p__26943
            map__26944 (if (seq? map__26944)
                         (if (next map__26944)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26944))
                           (if (seq map__26944) (first map__26944) {}))
                         map__26944)
            group_name (get map__26944 :group-name)
            results (:Group
                      (aws-helpers/invoke
                        (client)
                        {:op :CreateGroup, :req {:GroupName group_name}}))
            s__6444__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6444__auto__] (do (println results) (str s__6444__auto__))))))
  (reset-meta!
    #'create-group-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['group-name]}]), :column (int 1)}
      :name
      'create-group-command
      :ns
      *ns*))
  (defn create-access-key-command
    ([p__26947]
      (let [map__26948 p__26947
            map__26948 (if (seq? map__26948)
                         (if (next map__26948)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26948))
                           (if (seq map__26948) (first map__26948) {}))
                         map__26948)
            user_name (get map__26948 :user-name)]
        (try
          (let [new_creds (:AccessKey
                            (aws-helpers/invoke
                              (client)
                              {:op :CreateAccessKey, :req {:UserName user_name}}))
                s__6444__auto__ (java.io.StringWriter.)]
            (binding [*out* s__6444__auto__]
              (do
                (println (str (:AccessKeyId new_creds) " " (:SecretAccessKey new_creds)))
                (str s__6444__auto__))))
          (catch
            java.lang.Exception
            ex
            (if (canom/conflict? (ex-data ex))
              (cli/fail
                (let [s__6444__auto__ (java.io.StringWriter.)]
                  (binding [*out* s__6444__auto__]
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
                      (str s__6444__auto__)))))
              (do (throw ^java.lang.Throwable ex) nil)))))))
  (reset-meta!
    #'create-access-key-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['user-name]}]), :column (int 1)}
      :name
      'create-access-key-command
      :ns
      *ns*))
  (defn create-credentials-command
    ([p__26954]
      (let [map__26955 p__26954
            map__26955 (if (seq? map__26955)
                         (if (next map__26955)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26955))
                           (if (seq map__26955) (first map__26955) {}))
                         map__26955)
            prefix (get map__26955 :prefix)
            iam_client (client)
            peer_user_name {:UserName (str prefix "-peer")}
            dynamo_user_name {:UserName (str prefix "-transactor-dynamo")}
            metrics_user_name {:UserName (str prefix "-transactor-metrics")}
            s__6444__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6444__auto__]
          (do
            (aws-helpers/invoke iam_client {:op :CreateUser, :req peer_user_name})
            (println (str "peer.username=" (:UserName peer_user_name)))
            (let [peer_access_key (:AccessKey
                                    (aws-helpers/invoke
                                      iam_client
                                      {:op :CreateAccessKey, :req peer_user_name}))]
              (println (str "peer.aws-access-key-id=" (:AccessKeyId peer_access_key)))
              (println (str "peer.aws-secret-key=" (:SecretAccessKey peer_access_key))))
            (aws-helpers/invoke iam_client {:op :CreateUser, :req dynamo_user_name})
            (println (str "transactor.dynamo.username=" (:UserName dynamo_user_name)))
            (let [dynamo_access_key (:AccessKey
                                      (aws-helpers/invoke
                                        iam_client
                                        {:op :CreateAccessKey, :req dynamo_user_name}))]
              (println
                (str "transactor.dynamo.aws-access-key-id=" (:AccessKeyId dynamo_access_key)))
              (println
                (str "transactor.dynamo.aws-secret-key=" (:SecretAccessKey dynamo_access_key))))
            (aws-helpers/invoke iam_client {:op :CreateUser, :req metrics_user_name})
            (println (str "transactor.metrics.username=" (:UserName metrics_user_name)))
            (let [metrics_access_key (:AccessKey
                                       (aws-helpers/invoke
                                         iam_client
                                         {:op :CreateAccessKey, :req metrics_user_name}))]
              (println
                (str "transactor.metrics.aws-access-key-id=" (:AccessKeyId metrics_access_key)))
              (println
                (str "transactor.metrics.aws-secret-key=" (:SecretAccessKey metrics_access_key))))
            (str s__6444__auto__))))))
  (reset-meta!
    #'create-credentials-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['prefix]}]), :column (int 1)}
      :name
      'create-credentials-command
      :ns
      *ns*))
  (defn dynamo-r-policy-command
    ([p__26958]
      (let [map__26959 p__26958
            map__26959 (if (seq? map__26959)
                         (if (next map__26959)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26959))
                           (if (seq map__26959) (first map__26959) {}))
                         map__26959)
            account_id (get map__26959 :account-id)
            table_name (get map__26959 :table-name)
            arn (str "arn:aws:dynamodb:*:" account_id ":table/" table_name)
            s__6444__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6444__auto__]
          (do
            (json/pprint
              {"Statement"
               [{"Effect" "Allow",
                 "Action"
                 ["dynamodb:GetItem" "dynamodb:BatchGetItem" "dynamodb:Scan" "dynamodb:Query"],
                 "Resource" arn}]}
              :escape-slash
              false)
            (str s__6444__auto__))))))
  (reset-meta!
    #'dynamo-r-policy-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['account-id 'table-name]}]), :column (int 1)}
      :name
      'dynamo-r-policy-command
      :ns
      *ns*))
  (defn dynamo-rw-policy-command
    ([p__26962]
      (let [map__26963 p__26962
            map__26963 (if (seq? map__26963)
                         (if (next map__26963)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26963))
                           (if (seq map__26963) (first map__26963) {}))
                         map__26963)
            account_id (get map__26963 :account-id)
            table_name (get map__26963 :table-name)
            arn (str "arn:aws:dynamodb:*:" account_id ":table/" table_name)
            s__6444__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6444__auto__]
          (do
            (json/pprint
              {"Statement" [{"Effect" "Allow", "Action" ["dynamodb:*"], "Resource" arn}]}
              :escape-slash
              false)
            (str s__6444__auto__))))))
  (reset-meta!
    #'dynamo-rw-policy-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['account-id 'table-name]}]), :column (int 1)}
      :name
      'dynamo-rw-policy-command
      :ns
      *ns*))
  (defn metrics-w-policy-command
    ([_] (metrics-w-policy-command))
    ([]
      (let [s__6444__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6444__auto__]
          (do
            (json/pprint
              {"Statement"
               [{"Effect" "Allow",
                 "Action" ["cloudwatch:PutMetricData" "cloudwatch:PutMetricDataBatch"],
                 "Resource" "*",
                 "Condition" {"Bool" {"aws:SecureTransport" "true"}}}]}
              :escape-slash
              false)
            (str s__6444__auto__))))))
  (reset-meta!
    #'metrics-w-policy-command
    (assoc
      {:arglists (clojure.core/list [] ['_]), :column (int 1)}
      :name
      'metrics-w-policy-command
      :ns
      *ns*))
  (defn s3-w-policy-command
    ([p__26968]
      (let [map__26969 p__26968
            map__26969 (if (seq? map__26969)
                         (if (next map__26969)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26969))
                           (if (seq map__26969) (first map__26969) {}))
                         map__26969)
            bucket_name (get map__26969 :bucket-name)
            arn (str "arn:aws:s3:::" bucket_name)
            s__6444__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6444__auto__]
          (do
            (json/pprint
              {"Statement"
               [{"Effect" "Allow", "Action" ["s3:PutObject"], "Resource" [arn (str arn "/*")]}]}
              :escape-slash
              false)
            (str s__6444__auto__))))))
  (reset-meta!
    #'s3-w-policy-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['bucket-name]}]), :column (int 1)}
      :name
      's3-w-policy-command
      :ns
      *ns*))
  (defn assign-peer-user-command
    ([p__26972]
      (let [map__26973 p__26972
            map__26973 (if (seq? map__26973)
                         (if (next map__26973)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26973))
                           (if (seq map__26973) (first map__26973) {}))
                         map__26973)
            user_name (get map__26973 :user-name)
            table_name (get map__26973 :table-name)
            policy_name (str user_name "-" table_name "-peer")
            iam_client (client)
            policy_doc (dynamo-r-policy-command
                         {:account-id (get-account-id client), :table-name table_name})]
        (aws-helpers/invoke
          iam_client
          {:op :PutUserPolicy,
           :req {:UserName user_name, :PolicyName policy_name, :PolicyDocument policy_doc}}))))
  (reset-meta!
    #'assign-peer-user-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['user-name 'table-name]}]), :column (int 1)}
      :name
      'assign-peer-user-command
      :ns
      *ns*))
  (defn assign-transactor-dynamo-user-command
    ([p__26975]
      (let [map__26976 p__26975
            map__26976 (if (seq? map__26976)
                         (if (next map__26976)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26976))
                           (if (seq map__26976) (first map__26976) {}))
                         map__26976)
            user_name (get map__26976 :user-name)
            table_name (get map__26976 :table-name)
            policy_name (str user_name "-" table_name "-transactor-dynamo")
            iam_client (client)
            policy_doc (dynamo-rw-policy-command
                         {:account-id (get-account-id iam_client), :table-name table_name})]
        (aws-helpers/invoke
          iam_client
          {:op :PutUserPolicy,
           :req {:UserName user_name, :PolicyName policy_name, :PolicyDocument policy_doc}}))))
  (reset-meta!
    #'assign-transactor-dynamo-user-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['user-name 'table-name]}]), :column (int 1)}
      :name
      'assign-transactor-dynamo-user-command
      :ns
      *ns*))
  (defn assign-transactor-log-user-command
    ([p__26978]
      (let [map__26979 p__26978
            map__26979 (if (seq? map__26979)
                         (if (next map__26979)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26979))
                           (if (seq map__26979) (first map__26979) {}))
                         map__26979)
            user_name (get map__26979 :user-name)
            bucket_name (get map__26979 :bucket-name)
            policy_name (str user_name "-transactor-s3")
            iam_client (client)
            policy_doc (s3-w-policy-command {:bucket-name bucket_name})]
        (aws-helpers/invoke
          iam_client
          {:op :PutUserPolicy,
           :req {:UserName user_name, :PolicyName policy_name, :PolicyDocument policy_doc}}))))
  (reset-meta!
    #'assign-transactor-log-user-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['user-name 'bucket-name]}]), :column (int 1)}
      :name
      'assign-transactor-log-user-command
      :ns
      *ns*))
  (defn assign-transactor-metrics-user-command
    ([p__26981]
      (let [map__26982 p__26981
            map__26982 (if (seq? map__26982)
                         (if (next map__26982)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26982))
                           (if (seq map__26982) (first map__26982) {}))
                         map__26982)
            user_name (get map__26982 :user-name)
            policy_name (str user_name "-transactor-metrics")
            iam_client (client)
            policy_doc (metrics-w-policy-command)]
        (aws-helpers/invoke
          iam_client
          {:op :PutUserPolicy,
           :req {:UserName user_name, :PolicyName policy_name, :PolicyDocument policy_doc}}))))
  (reset-meta!
    #'assign-transactor-metrics-user-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['user-name]}]), :column (int 1)}
      :name
      'assign-transactor-metrics-user-command
      :ns
      *ns*)))