(do
  (clojure.core/in-ns 'datomic.provisioning.aws)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.provisioning.aws)
    {:doc
     "AWS provisioning commands for Datomic transactor configuration. Ensures DynamoDB tables, S3 log buckets, IAM roles and policies, preserves comments while completing properties files, and renders the legacy EC2 CloudFormation template."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.core2.aws.helpers :as 'aws]
        ['datomic.core2.anomalies :as 'canom]
        ['clojure.java.io :as 'io]
        ['clojure.edn :as 'edn]
        ['clojure.string :as 'str]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['clojure.pprint :as 'pprint]
        ['clojure.data.json :as 'json]
        ['datomic.memory :as 'memory]
        ['datomic.cli :as 'cli]
        ['datomic.ddb :as 'ddb]
        ['datomic.s3 :as 's3]
        ['datomic.iam :as 'iam]
        ['clojure.string :as 'str])
      (clojure.core/import 'java.io.PushbackReader)
      (clojure.core/import 'java.io.StringReader)
      (clojure.core/import 'software.amazon.awssdk.core.exception.SdkClientException)
      (clojure.core/import
        'software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain)))
  (when-not (.equals 'datomic.provisioning.aws 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.provisioning.aws))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.core2.aws.helpers :as 'aws]
          ['datomic.core2.anomalies :as 'canom]
          ['clojure.java.io :as 'io]
          ['clojure.edn :as 'edn]
          ['clojure.string :as 'str]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['clojure.pprint :as 'pprint]
          ['clojure.data.json :as 'json]
          ['datomic.memory :as 'memory]
          ['datomic.cli :as 'cli]
          ['datomic.ddb :as 'ddb]
          ['datomic.s3 :as 's3]
          ['datomic.iam :as 'iam]
          ['clojure.string :as 'str])
        (clojure.core/import 'java.io.PushbackReader)
        (clojure.core/import 'java.io.StringReader)
        (clojure.core/import 'software.amazon.awssdk.core.exception.SdkClientException)
        (clojure.core/import
          'software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain))))
  (def ec2-service-assume-role-policy-doc
   "{\"Version\":\"2008-10-17\",\"Statement\":[{\"Sid\":\"\",\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"ec2.amazonaws.com\"},\"Action\":\"sts:AssumeRole\"}]}")
  (reset-meta!
    #'ec2-service-assume-role-policy-doc
    (assoc {:column (int 1)} :name 'ec2-service-assume-role-policy-doc :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.provisioning.aws" "cloudformation-validators")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.provisioning.aws" "cloudformation-validators")
    {"aws-instance-monitoring" identity, "datomic-deploy-s3-bucket" identity})
  (.setMeta
    (clojure.lang.RT/var "datomic.provisioning.aws" "transactor-validators")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.provisioning.aws" "transactor-validators")
    {"protocol" #{"ddb-local" "mdev" "inf" "ddb" "dev" "sql" "olddev"},
     "port" identity,
     "host" identity})
  (defn ddb? ([props] (contains? #{"ddb-local" "ddb"} (get props "protocol"))))
  (reset-meta!
    #'ddb?
    (assoc {:arglists (clojure.core/list ['props]), :column (int 1)} :name 'ddb? :ns *ns*))
  (defn comment? ([prop_line] (= (first prop_line) \#)))
  (reset-meta!
    #'comment?
    (assoc {:arglists (clojure.core/list ['prop-line]), :column (int 1)} :name 'comment? :ns *ns*))
  (defn trailing-slash? ([s] (boolean (re-find #"\\\s*$" s))))
  (reset-meta!
    #'trailing-slash?
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'trailing-slash? :ns *ns*))
  (defn prop-line-seq
    ([rdr]
      (loop [line (.readLine ^java.io.BufferedReader rdr)]
        (when line
          (if (trailing-slash? line)
            (recur (str line "\n" (.readLine ^java.io.BufferedReader rdr)))
            (cons line (lazy-seq (prop-line-seq rdr))))))))
  (reset-meta!
    #'prop-line-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'rdr {:tag 'java.io.BufferedReader})]),
       :column (int 1)}
      :name
      'prop-line-seq
      :ns
      *ns*))
  (defn read-prop-line
    ([prop_line]
      (if (or (comment? prop_line) (empty? prop_line))
        [prop_line]
        (let [temp__5823__auto__ (re-matches #"^([^=]+)=(.*)" prop_line)]
          (if temp__5823__auto__
            (let [vec__32107 temp__5823__auto__
                  _ (nth vec__32107 (int 0) nil)
                  k (nth vec__32107 (int 1) nil)
                  v (nth vec__32107 (int 2) nil)]
              [k (re-find #"^[\S].*" v)])
            [prop_line])))))
  (reset-meta!
    #'read-prop-line
    (assoc
      {:arglists (clojure.core/list ['prop-line]), :column (int 1)}
      :name
      'read-prop-line
      :ns
      *ns*))
  (defn load-properties
    ([prop_file] (let [lines (prop-line-seq (io/reader prop_file))] (map read-prop-line lines))))
  (reset-meta!
    #'load-properties
    (assoc
      {:arglists (clojure.core/list ['prop-file]), :column (int 1)}
      :name
      'load-properties
      :ns
      *ns*))
  (defn extract-prop-map
    ([prop_lines]
      (into {} (filter (fn fn__32115 ([p1__32114#] (= (long (count p1__32114#)) 2))) prop_lines))))
  (reset-meta!
    #'extract-prop-map
    (assoc
      {:arglists (clojure.core/list ['prop-lines]), :column (int 1)}
      :name
      'extract-prop-map
      :ns
      *ns*))
  (defn merge-prop-line
    ([prop_line props_map]
      (let [pred__32118 = expr__32119 (count prop_line)]
        (if (^clojure.lang.IFn pred__32118 1 (java.lang.Integer/valueOf (int expr__32119)))
          (first prop_line)
          (if (^clojure.lang.IFn pred__32118 2 (java.lang.Integer/valueOf (int expr__32119)))
            (let [vec__32120 prop_line
                  k (nth vec__32120 (int 0) nil)
                  v (nth vec__32120 (int 1) nil)]
              (str k "=" (get props_map k)))
            (do
              (throw
                (java.lang.IllegalArgumentException.
                  (str "No matching clause: " (java.lang.Integer/valueOf (int expr__32119)))))
              nil))))))
  (reset-meta!
    #'merge-prop-line
    (assoc
      {:arglists (clojure.core/list ['prop-line 'props-map]), :column (int 1)}
      :name
      'merge-prop-line
      :ns
      *ns*))
  (defn merge-prop-lines
    ([prop_lines prop_map new_keys]
      (concat
        (map (fn fn__32125 ([p1__32124#] (merge-prop-line p1__32124# prop_map))) prop_lines)
        (map (fn fn__32127 ([k] (str k "=" (get prop_map k)))) new_keys))))
  (reset-meta!
    #'merge-prop-lines
    (assoc
      {:arglists (clojure.core/list ['prop-lines 'prop-map 'new-keys]), :column (int 1)}
      :name
      'merge-prop-lines
      :ns
      *ns*))
  (defn save-properties
    ([prop_file prop_lines]
      (with-open [wrtr (io/writer prop_file)]
        (binding [*out* wrtr]
          (loop [seq_32260 (seq prop_lines) chunk_32261 nil count_32262 0 i_32263 0]
            (if (< i_32263 count_32262)
              (let [line (.nth ^clojure.lang.Indexed chunk_32261 (int i_32263))]
                (println line)
                (recur seq_32260 chunk_32261 count_32262 (inc i_32263)))
              (let [temp__5825__auto__ (seq seq_32260)]
                (when temp__5825__auto__
                  (let [seq_32260 temp__5825__auto__]
                    (if (chunked-seq? seq_32260)
                      (let [c__6090__auto__ (chunk-first seq_32260)]
                        (recur
                          (chunk-rest seq_32260)
                          c__6090__auto__
                          (int (count c__6090__auto__))
                          (int 0)))
                      (let [line (first seq_32260)]
                        (println line)
                        (recur (next seq_32260) nil 0 0))))))))))))
  (reset-meta!
    #'save-properties
    (assoc
      {:arglists (clojure.core/list ['prop-file 'prop-lines]), :column (int 1)}
      :name
      'save-properties
      :ns
      *ns*))
  (defn add-errors ([props & errors] (assoc props :errors (into (get props :errors []) errors))))
  (reset-meta!
    #'add-errors
    (assoc
      {:arglists (clojure.core/list ['props '& 'errors]), :column (int 1)}
      :name
      'add-errors
      :ns
      *ns*))
  (defn add-aws-exception
    ([props ex]
      (let [data (ex-data ex)]
        (if (canom/anom data)
          (add-errors
            props
            (str
              (:datomic.aws.client.api/service data)
              (:datomic.aws.client.api/error-code data)
              (:datomic.aws.client.api/error-message data)))
          (do (throw ^java.lang.Throwable ex) nil)))))
  (reset-meta!
    #'add-aws-exception
    (assoc
      {:arglists (clojure.core/list ['props 'ex]), :column (int 1)}
      :name
      'add-aws-exception
      :ns
      *ns*))
  (def record-aws-exception
   (fn record_aws_exception
     ([&form &env props & forms]
       (seq
         (concat
           (clojure.core/list 'try)
           forms
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'catch)
                 (clojure.core/list 'java.lang.Exception)
                 (clojure.core/list 'e)
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'datomic.provisioning.aws/add-aws-exception)
                       (clojure.core/list props)
                       (clojure.core/list 'e))))))))))))
  (reset-meta!
    #'record-aws-exception
    (assoc
      {:arglists (clojure.core/list ['props '& 'forms]), :column (int 1)}
      :name
      'record-aws-exception
      :ns
      *ns*))
  (.setMacro #'record-aws-exception)
  ;; Accumulates missing and invalid property errors so an ensure run can report them together.
  (defn ensure-required-props
    ([props validators]
      (let [errors (reduce
                     (fn fn__32141
                       ([errors p__32140]
                         (let [vec__32142 p__32140
                               k (nth vec__32142 (int 0) nil)
                               v (nth vec__32142 (int 1) nil)
                               temp__5823__auto__ (get props k)]
                           (if temp__5823__auto__
                             (let [prop temp__5823__auto__]
                               (if (^clojure.lang.IFn v prop)
                                 errors
                                 (conj errors (str "Invalid value for property: " k "=" prop))))
                             (conj errors (str "Missing property: " k))))))
                     []
                     validators)]
        (apply add-errors props errors))))
  (reset-meta!
    #'ensure-required-props
    (assoc
      {:arglists (clojure.core/list ['props 'validators]), :column (int 1)}
      :name
      'ensure-required-props
      :ns
      *ns*))
  ;; Accepts only the Datomic DynamoDB key shape: a string id attribute used as the hash key.
  (defn check-existing-table-schema
    ([props client table_name]
      (let [desc (aws/invoke client {:op :DescribeTable, :req {:TableName table_name}})
            attribute_definition (get-in desc [:Table :AttributeDefinitions 0])
            map__32148 attribute_definition
            map__32148 (if (seq? map__32148)
                         (if (next map__32148)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32148))
                           (if (seq map__32148) (first map__32148) {}))
                         map__32148)
            AttributeName (get map__32148 :AttributeName)
            AttributeType (get map__32148 :AttributeType)]
        (if (and (= AttributeName "id") (= AttributeType "S"))
          props
          (add-errors
            props
            (str "Invalid attribute definition: " AttributeName ", " AttributeType))))))
  (reset-meta!
    #'check-existing-table-schema
    (assoc
      {:arglists (clojure.core/list ['props 'client 'table-name]), :column (int 1)}
      :name
      'check-existing-table-schema
      :ns
      *ns*))
  ;; Creates an unindexed DynamoDB table keyed by the string attribute id.
  (defn create-cluster-table
    ([ddb_client table_name read_units write_units]
      (aws/invoke
        ddb_client
        {:op :CreateTable,
         :req
         {:TableName table_name,
          :ProvisionedThroughput {:ReadCapacityUnits read_units, :WriteCapacityUnits write_units},
          :KeySchema [{:AttributeName "id", :KeyType "HASH"}],
          :AttributeDefinitions [{:AttributeType "S", :AttributeName "id"}]}}))
    ([ddb_client table_name] (create-cluster-table ddb_client table_name 100 50)))
  (reset-meta!
    #'create-cluster-table
    (assoc
      {:arglists
       (clojure.core/list
         ['ddb-client 'table-name]
         ['ddb-client 'table-name 'read-units 'write-units]),
       :column (int 1)}
      :name
      'create-cluster-table
      :ns
      *ns*))
  (defn default-aws-region
    ([]
      (try
        (.id
          (.getRegion (software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain.)))
        (catch software.amazon.awssdk.core.exception.SdkClientException _ "us-east-1"))))
  (reset-meta!
    #'default-aws-region
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'default-aws-region :ns *ns*))
  ;; Reuses a compatible table or creates one with low-volume initial provisioning.
  (defn ensure-ddb-table
    ([props]
      (if (ddb? props)
        (let [client (ddb/client
                       nil
                       (merge
                         (let [temp__5827__auto__ (get props "aws-dynamodb-region")]
                           (if (nil? temp__5827__auto__)
                             {:region (default-aws-region)}
                             (let [region temp__5827__auto__] {:region region})))
                         (let [temp__5825__auto__ (get props "aws-dynamodb-override-endpoint")]
                           (when temp__5825__auto__
                             (let [override_endpoint temp__5825__auto__]
                               {:override-endpoint (str "http://" override_endpoint)})))))
              table_name (get props "aws-dynamodb-table")]
          (try
            (check-existing-table-schema props client table_name)
            (catch
              java.lang.Exception
              ex
              (if (canom/not-found? (ex-data ex))
                (create-cluster-table client table_name)
                (do (throw ^java.lang.Throwable ex) nil))))
          props)
        props)))
  (reset-meta!
    #'ensure-ddb-table
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'ensure-ddb-table
      :ns
      *ns*))
  (defn ensure-ddb-table-name
    ([props]
      (if (ddb? props)
        (assoc props "aws-dynamodb-table" (get props "aws-dynamodb-table" "datomic"))
        props)))
  (reset-meta!
    #'ensure-ddb-table-name
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'ensure-ddb-table-name
      :ns
      *ns*))
  (defn create-system-command
    ([p__32159]
      (let [map__32160 p__32159
            map__32160 (if (seq? map__32160)
                         (if (next map__32160)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32160))
                           (if (seq map__32160) (first map__32160) {}))
                         map__32160)
            region (get map__32160 :region)
            override_endpoint (get map__32160 :override-endpoint)
            table_name (get map__32160 :table-name)
            read_capacity (get map__32160 :read-capacity)
            write_capacity (get map__32160 :write-capacity)
            s__6444__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6444__auto__]
          (do
            (pprint/pprint
              (create-cluster-table
                (ddb/client
                  nil
                  {:region region,
                   :override-endpoint
                   (let [G__32161 override_endpoint]
                     (when-not (nil? G__32161) (str "http://" G__32161)))})
                table_name
                read_capacity
                write_capacity))
            (str s__6444__auto__))))))
  (reset-meta!
    #'create-system-command
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['region 'override-endpoint 'table-name 'read-capacity 'write-capacity]}]),
       :column (int 1)}
      :name
      'create-system-command
      :ns
      *ns*))
  ;; Ensures the optional S3 log bucket, generating a globally unique name when its value is blank.
  (defn ensure-log-bucket
    ([props]
      (try
        (if (contains? props "aws-s3-log-bucket-id")
          (let [bucket_name (or
                              (get props "aws-s3-log-bucket-id")
                              (str "datomic-logs-" (common/rand-uuid)))]
            (s3/ensure-bucket (s3/s3-service) bucket_name)
            (assoc props "aws-s3-log-bucket-id" bucket_name))
          props)
        (catch java.lang.Exception e (add-aws-exception props e)))))
  (reset-meta!
    #'ensure-log-bucket
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'ensure-log-bucket
      :ns
      *ns*))
  (defn user-prop-names
    ([prefix]
      (map
        (fn fn__32167 ([p1__32166#] (str prefix p1__32166#)))
        ["-user" "-access-key-id" "-secret-key"])))
  (reset-meta!
    #'user-prop-names
    (assoc
      {:arglists (clojure.core/list ['prefix]), :column (int 1)}
      :name
      'user-prop-names
      :ns
      *ns*))
  (defn ensure-unique-user
    ([user_name]
      (let [client (iam/client)
            user_names (map :UserName (:Users (aws/invoke client {:op :ListUsers})))]
        (loop [i 1 gen_user_name user_name]
          (if (some #{gen_user_name} user_names)
            (recur (inc i) (str user_name "-" (long i)))
            gen_user_name)))))
  (reset-meta!
    #'ensure-unique-user
    (assoc
      {:arglists (clojure.core/list ['user-name]), :column (int 1)}
      :name
      'ensure-unique-user
      :ns
      *ns*))
  (defn ensure-unique-role
    ([role_name]
      (let [client (iam/client)
            res (aws/invoke client {:op :ListRoles})
            role_names (map :roleName (:roles res))]
        (loop [i 1 gen_role_name role_name]
          (if (some #{gen_role_name} role_names)
            (recur (inc i) (str role_name "-" (long i)))
            gen_role_name)))))
  (reset-meta!
    #'ensure-unique-role
    (assoc
      {:arglists (clojure.core/list ['role-name]), :column (int 1)}
      :name
      'ensure-unique-role
      :ns
      *ns*))
  (defn add-user ([props user] (assoc props :users (merge (get props :users {}) user))))
  (reset-meta!
    #'add-user
    (assoc
      {:arglists (clojure.core/list ['props 'user]), :column (int 1)}
      :name
      'add-user
      :ns
      *ns*))
  (defn ensure-new-user
    ([props user_name prefix]
      (let [client (iam/client)
            vec__32173 (user-prop-names prefix)
            user (nth vec__32173 (int 0) nil)
            access (nth vec__32173 (int 1) nil)
            secret (nth vec__32173 (int 2) nil)
            account_id (iam/get-account-id client)
            user_creds (get-in props [:users user_name])]
        (if user_creds
          (assoc
            props
            user
            user_name
            access
            (:aws-access-key-id user_creds)
            secret
            (:aws-secret-access-key user_creds))
          (do
            (try
              (aws/invoke client {:op :CreateUser, :req {:UserName user_name}})
              (catch
                java.lang.Exception
                ex
                (when-not (canom/conflict? (ex-data ex)) (throw ^java.lang.Throwable ex) nil)))
            (try
              (let [map__32178 (:AccessKey
                                 (aws/invoke
                                   client
                                   {:op :CreateAccessKey, :req {:UserName user_name}}))
                    map__32178 (if (seq? map__32178)
                                 (if (next map__32178)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__32178))
                                   (if (seq map__32178) (first map__32178) {}))
                                 map__32178)
                    AccessKeyId (get map__32178 :AccessKeyId)
                    SecretAccessKey (get map__32178 :SecretAccessKey)]
                (assoc
                  (add-user
                    props
                    {user_name
                     {:aws-access-key-id AccessKeyId, :aws-secret-access-key SecretAccessKey}})
                  user
                  user_name
                  access
                  AccessKeyId
                  secret
                  SecretAccessKey))
              (catch
                java.lang.Exception
                ex
                (if (canom/conflict? (ex-data ex))
                  (add-errors
                    props
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
                  (do (throw ^java.lang.Throwable ex) nil)))))))))
  (reset-meta!
    #'ensure-new-user
    (assoc
      {:arglists (clojure.core/list ['props 'user-name 'prefix]), :column (int 1)}
      :name
      'ensure-new-user
      :ns
      *ns*))
  (defn ensure-existing-user
    ([props user_name]
      (let [client (iam/client)]
        (aws/invoke client {:op :GetUser, :req {:UserName user_name}})
        props)))
  (reset-meta!
    #'ensure-existing-user
    (assoc
      {:arglists (clojure.core/list ['props 'user-name]), :column (int 1)}
      :name
      'ensure-existing-user
      :ns
      *ns*))
  (defn ensure-user-policy
    ([props prefix policy]
      (let [client (iam/client)
            account_id (iam/get-account-id client)
            map__32184 policy
            map__32184 (if (seq? map__32184)
                         (if (next map__32184)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32184))
                           (if (seq map__32184) (first map__32184) {}))
                         map__32184)
            policy_fn (get map__32184 :fn)
            args (get map__32184 :args)
            policy_name_suffix (get map__32184 :name)
            resolved_args (into
                            {}
                            (map
                              (fn fn__32186
                                ([p__32185]
                                  (let [vec__32187 p__32185
                                        k (nth vec__32187 (int 0) nil)
                                        v (nth vec__32187 (int 1) nil)]
                                    [k (get props v)])))
                              args))
            policy_doc (^clojure.lang.IFn policy_fn (merge resolved_args {:account-id account_id}))
            policy_name (str/join "-" (concat (vals resolved_args) [policy_name_suffix]))
            user_name (get props (str prefix "-user"))
            user_policies (aws/invoke client {:op :ListUserPolicies, :req {:UserName user_name}})]
        (when-not (some #{policy_name} user_policies)
          (aws/invoke
            client
            {:op :PutUserPolicy,
             :req {:UserName user_name, :PolicyName policy_name, :PolicyDocument policy_doc}})))
      props))
  (reset-meta!
    #'ensure-user-policy
    (assoc
      {:arglists (clojure.core/list ['props 'prefix 'policy]), :column (int 1)}
      :name
      'ensure-user-policy
      :ns
      *ns*))
  (defn ensure-user
    ([props prefix required policy]
      (try
        (let [ks (user-prop-names prefix)
              vec__32192 ks
              user (nth vec__32192 (int 0) nil)
              access (nth vec__32192 (int 1) nil)
              secret (nth vec__32192 (int 2) nil)
              user_map (select-keys props [user access secret])]
          (cond
            (and required (not= 3 (java.lang.Integer/valueOf (int (count user_map))))) (add-errors
                                                                                         props
                                                                                         (str
                                                                                           "Properties "
                                                                                           (str/join
                                                                                             ","
                                                                                             ks)
                                                                                           " are required."))
            (= (count user_map) 0) props
            (< (count user_map) 3) (add-errors
                                     props
                                     (str
                                       "Properties "
                                       (str/join "," ks)
                                       " must be specified as group."))
            (every? nil? (vals user_map)) (ensure-user-policy
                                            (ensure-new-user
                                              props
                                              (ensure-unique-user (str "datomic-" prefix))
                                              prefix)
                                            prefix
                                            policy)
            (and
              (not (nil? (get user_map user)))
              (nil? (get user_map access))
              (nil? (get user_map secret))) (ensure-user-policy
                                              (ensure-new-user props (get user_map user) prefix)
                                              prefix
                                              policy)
            (every? identity (vals user_map)) (ensure-user-policy
                                                (ensure-existing-user props (get user_map user))
                                                prefix
                                                policy)
            :else (do
                    (add-errors
                      props
                      (str
                        "Properties "
                        (str/join "," [access secret])
                        " must be both set, or both unset.")))))
        (catch java.lang.Exception e (add-aws-exception props e)))))
  (reset-meta!
    #'ensure-user
    (assoc
      {:arglists (clojure.core/list ['props 'prefix 'required 'policy]), :column (int 1)}
      :name
      'ensure-user
      :ns
      *ns*))
  (defn ensure-role-policy
    ([props prefix policy]
      (let [client (iam/client)
            account_id (iam/get-account-id client)
            map__32199 policy
            map__32199 (if (seq? map__32199)
                         (if (next map__32199)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32199))
                           (if (seq map__32199) (first map__32199) {}))
                         map__32199)
            policy_fn (get map__32199 :fn)
            args (get map__32199 :args)
            policy_name_suffix (get map__32199 :name)
            resolved_args (into
                            {}
                            (map
                              (fn fn__32201
                                ([p__32200]
                                  (let [vec__32202 p__32200
                                        k (nth vec__32202 (int 0) nil)
                                        v (nth vec__32202 (int 1) nil)]
                                    [k (get props v)])))
                              args))
            policy_doc (^clojure.lang.IFn policy_fn (merge resolved_args {:account-id account_id}))
            policy_name (str/join "-" (concat (vals resolved_args) [policy_name_suffix]))
            role_name (get props (str prefix "-role"))
            role_policies (aws/invoke client {:op :ListRolePolicies, :req {:RoleName role_name}})]
        (when-not (some #{policy_name} role_policies)
          (aws/invoke
            client
            {:op :PutRolePolicy,
             :req {:RoleName role_name, :PolicyName policy_name, :PolicyDocument policy_doc}})))
      props))
  (reset-meta!
    #'ensure-role-policy
    (assoc
      {:arglists (clojure.core/list ['props 'prefix 'policy]), :column (int 1)}
      :name
      'ensure-role-policy
      :ns
      *ns*))
  (defn ensure-new-role
    ([name]
      (let [client (iam/client)
            role (:Role
                   (aws/invoke
                     client
                     {:op :CreateRole,
                      :req
                      {:RoleName name,
                       :AssumeRolePolicyDocument ec2-service-assume-role-policy-doc}}))
            instance_profile (:InstanceProfile
                               (aws/invoke
                                 client
                                 {:op :CreateInstanceProfile, :req {:InstanceProfileName name}}))]
        (aws/invoke
          client
          {:op :AddRoleToInstanceProfile, :req {:RoleName name, :InstanceProfileName name}})
        role)))
  (reset-meta!
    #'ensure-new-role
    (assoc
      {:arglists (clojure.core/list ['name]), :column (int 1)}
      :name
      'ensure-new-role
      :ns
      *ns*))
  ;; Installs table-scoped peer/transactor policies plus optional log and metric publishing policies.
  (defn ensure-role-policies
    ([props prefix]
      (let [pred__32208 = expr__32209 prefix]
        (if (^clojure.lang.IFn pred__32208 "aws-transactor" expr__32209)
          (do
            (when-not (empty? (^clojure.lang.IFn props "aws-s3-log-bucket-id"))
              (ensure-role-policy
                props
                "aws-transactor"
                {:fn iam/s3-w-policy-command,
                 :name "transactor-logs",
                 :args {:bucket-name "aws-s3-log-bucket-id"}}))
            (when-not (empty? (^clojure.lang.IFn props "aws-cloudwatch-region"))
              (ensure-role-policy
                props
                "aws-transactor"
                {:fn iam/metrics-w-policy-command, :name "transactor-metrics", :args {}}))
            (ensure-role-policy
              props
              "aws-transactor"
              {:fn iam/dynamo-rw-policy-command,
               :name "transactor",
               :args {:table-name "aws-dynamodb-table"}}))
          (if (^clojure.lang.IFn pred__32208 "aws-peer" expr__32209)
            (ensure-role-policy
              props
              "aws-peer"
              {:fn iam/dynamo-r-policy-command,
               :name "peer",
               :args {:table-name "aws-dynamodb-table"}})
            (throw (ex-info "Unrecognized prefix" {:prefix prefix})))))
      props))
  (reset-meta!
    #'ensure-role-policies
    (assoc
      {:arglists (clojure.core/list ['props 'prefix]), :column (int 1)}
      :name
      'ensure-role-policies
      :ns
      *ns*))
  (defn get-role
    ([name]
      (let [client (iam/client)]
        (try
          (aws/invoke client {:op :GetRole, :req {:RoleName name}})
          (catch
            java.lang.Exception
            ex
            (when-not (canom/not-found? (ex-data ex)) (throw ^java.lang.Throwable ex) nil))))))
  (reset-meta!
    #'get-role
    (assoc {:arglists (clojure.core/list ['name]), :column (int 1)} :name 'get-role :ns *ns*))
  (defn ensure-role
    ([props prefix]
      (let [role_key (str prefix "-role")
            role_val (^clojure.lang.IFn props role_key)
            role_name (if (not (empty? role_val))
                        role_val
                        (ensure-unique-role (str "datomic-" prefix)))
            role (or (get-role role_name) (ensure-new-role role_name))]
        (ensure-role-policies (assoc props role_key role_name) prefix))))
  (reset-meta!
    #'ensure-role
    (assoc
      {:arglists (clojure.core/list ['props 'prefix]), :column (int 1)}
      :name
      'ensure-role
      :ns
      *ns*))
  ;; Requires one transactor identity strategy and provisions its permissions.
  ;; Instance roles avoid embedded long-lived keys; IAM users remain available for existing deployments.
  (defn ensure-transactor-identity
    ([props]
      (let [id_map (select-keys props ["aws-transactor-role" "aws-dynamodb-user"])]
        (cond
          (not= 1 (java.lang.Integer/valueOf (int (count id_map)))) (add-errors
                                                                      props
                                                                      (str
                                                                        "Cannot use aws-transactor-role property and aws-dynamodb-user, aws-dynamodb-access-key-id and aws-dynamodb-secret-key properties together."))
          (contains? id_map "aws-transactor-role") (ensure-role props "aws-transactor")
          (contains? id_map "aws-dynamodb-user") (ensure-user
                                                   (ensure-user
                                                     (ensure-user
                                                       props
                                                       "aws-dynamodb"
                                                       (ddb? props)
                                                       {:fn iam/dynamo-rw-policy-command,
                                                        :name "transactor",
                                                        :args {:table-name "aws-dynamodb-table"}})
                                                     "aws-cloudwatch"
                                                     false
                                                     {:fn iam/metrics-w-policy-command,
                                                      :name "transactor-metrics",
                                                      :args {}})
                                                   "aws-s3-log"
                                                   false
                                                   {:fn iam/s3-w-policy-command,
                                                    :name "transactor-logs",
                                                    :args {:bucket-name "aws-s3-log-bucket-id"}})
          :else (do
                  (add-errors
                    props
                    (str "Must specify either aws-transactor-role or aws-dynamodb-user.")))))))
  (reset-meta!
    #'ensure-transactor-identity
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'ensure-transactor-identity
      :ns
      *ns*))
  ;; Requires one peer identity strategy and grants read access to the configured table.
  ;; Instance roles avoid embedded long-lived keys; IAM users remain available for existing deployments.
  (defn ensure-peer-identity
    ([props]
      (let [id_map (select-keys props ["aws-peer-role" "aws-dynamodb-peer-user"])]
        (cond
          (not= 1 (java.lang.Integer/valueOf (int (count id_map)))) (add-errors
                                                                      props
                                                                      (str
                                                                        "Cannot use aws-peer-role property and aws-dynamodb-peer-user, aws-dynamodb-peer-access-key-id and aws-dynamodb-peer-secret-key properties together"))
          (contains? id_map "aws-peer-role") (ensure-role props "aws-peer")
          (contains? id_map "aws-dynamodb-peer-user") (ensure-user
                                                        props
                                                        "aws-dynamodb-peer"
                                                        (ddb? props)
                                                        {:fn iam/dynamo-r-policy-command,
                                                         :name "peer",
                                                         :args {:table-name "aws-dynamodb-table"}})
          :else (do
                  (add-errors
                    props
                    (str "Must specify either aws-peer-role or aws-dynamodb-peer-user.")))))))
  (reset-meta!
    #'ensure-peer-identity
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'ensure-peer-identity
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.provisioning.aws" "ensure-transactor*")
    {:column (int 1)})
  (let [v__5813__auto__ #'ensure-transactor*]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta
        (clojure.lang.RT/var "datomic.provisioning.aws" "ensure-transactor*")
        {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.provisioning.aws" "ensure-transactor*")
        (clojure.lang.MultiFn.
          "ensure-transactor*"
          (fn fn__32217 ([props] (keyword (get props "protocol"))))
          :default
          #'clojure.core/global-hierarchy))
      #'ensure-transactor*))
  (defmethod
    ensure-transactor*
    :ddb
    fn__32222
    ([props]
      (let [account_id (iam/get-account-id (iam/client))]
        (ensure-peer-identity
          (-> (ensure-required-props props transactor-validators)
           (ensure-ddb-table-name)
           (ensure-ddb-table)
           (ensure-log-bucket)
           (ensure-transactor-identity))))))
  (defmethod
    ensure-transactor*
    :ddb-local
    fn__32224
    ([props]
      (ensure-ddb-table
        (ensure-ddb-table-name (ensure-required-props props transactor-validators)))))
  (defn read-pred
    ([s pred otherwise]
      (if s
        (let [o (java.lang.Object.)
              s (java.io.PushbackReader. (java.io.StringReader. ^java.lang.String s))]
          (push-thread-bindings (hash-map #'*read-eval* false))
          (let [v (try (read s false o) (finally (pop-thread-bindings)))]
            (if (and (not= o v) (^clojure.lang.IFn pred v)) v otherwise)))
        otherwise)))
  (reset-meta!
    #'read-pred
    (assoc
      {:arglists (clojure.core/list ['s 'pred 'otherwise]), :column (int 1)}
      :name
      'read-pred
      :ns
      *ns*))
  (defn ensure-autoscaling-group-size
    ([props]
      (let [v (get props "aws-autoscaling-group-size")
            temp__5823__auto__ (read-pred
                                 v
                                 (fn fn__32231
                                   ([p1__32230#]
                                     (and
                                       (integer? p1__32230#)
                                       (clojure.lang.Numbers/isPos p1__32230#))))
                                 nil)]
        (if temp__5823__auto__
          (let [size temp__5823__auto__] props)
          (add-errors props (str "Invalid value for aws-autoscaling-group-size: " v))))))
  (reset-meta!
    #'ensure-autoscaling-group-size
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'ensure-autoscaling-group-size
      :ns
      *ns*))
  (defn ensure-java-xmx
    ([props]
      (if (get props "java-xmx")
        props
        (let [type (common/getx props "aws-instance-type")]
          (assoc props "java-xmx" (:xmx (memory/aws-transactor-settings type)))))))
  (reset-meta!
    #'ensure-java-xmx
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'ensure-java-xmx
      :ns
      *ns*))
  (defn ensure-java-opts
    ([props]
      (let [temp__5823__auto__ (get props "java-opts")]
        (if temp__5823__auto__
          (let [opts temp__5823__auto__] props)
          (assoc props "java-opts" "")))))
  (reset-meta!
    #'ensure-java-opts
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'ensure-java-opts
      :ns
      *ns*))
  (defn ensure-datomic-version
    ([props]
      (let [temp__5823__auto__ (get props "datomic-version")]
        (if temp__5823__auto__
          (let [v temp__5823__auto__] props)
          (assoc props "datomic-version" (config/property "datomic.version"))))))
  (reset-meta!
    #'ensure-datomic-version
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'ensure-datomic-version
      :ns
      *ns*))
  (defn denil
    ([m]
      (reduce
        (fn fn__32242
          ([m p__32241]
            (let [vec__32243 p__32241
                  k (nth vec__32243 (int 0) nil)
                  v (nth vec__32243 (int 1) nil)]
              (if (nil? v) m (assoc m k v)))))
        {}
        m)))
  (reset-meta!
    #'denil
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'denil :ns *ns*))
  (defn print-errors
    ([errors dest]
      (binding [*out* dest]
        (loop [seq_32248 (seq errors) chunk_32249 nil count_32250 0 i_32251 0]
          (if (< i_32251 count_32250)
            (let [error (.nth ^clojure.lang.Indexed chunk_32249 (int i_32251))]
              (println error)
              (recur seq_32248 chunk_32249 count_32250 (inc i_32251)))
            (let [temp__5825__auto__ (seq seq_32248)]
              (when temp__5825__auto__
                (let [seq_32248 temp__5825__auto__]
                  (if (chunked-seq? seq_32248)
                    (let [c__6090__auto__ (chunk-first seq_32248)]
                      (recur
                        (chunk-rest seq_32248)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [error (first seq_32248)]
                      (println error)
                      (recur (next seq_32248) nil 0 0)))))))))))
  (reset-meta!
    #'print-errors
    (assoc
      {:arglists (clojure.core/list ['errors 'dest]), :column (int 1)}
      :name
      'print-errors
      :ns
      *ns*))
  (defn new-keys
    ([ensured_map orig_map] (filter string? (keys (apply dissoc ensured_map (keys orig_map))))))
  (reset-meta!
    #'new-keys
    (assoc
      {:arglists (clojure.core/list ['ensured-map 'orig-map]), :column (int 1)}
      :name
      'new-keys
      :ns
      *ns*))
  ;; Completes an input properties file idempotently, preserves its layout and comments, writes the
  ;; requested output file, and reports all provisioning errors after the ensure pass.
  (defn ensure-transactor
    ([p__32256]
      (let [map__32257 p__32256
            map__32257 (if (seq? map__32257)
                         (if (next map__32257)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32257))
                           (if (seq map__32257) (first map__32257) {}))
                         map__32257)
            input_file (get map__32257 :input-file)
            output_file (get map__32257 :output-file)
            lines (load-properties input_file)
            props (extract-prop-map lines)]
        (try
          (let [new_props (ensure-transactor* props)
                errors (:errors new_props)
                new_lines (merge-prop-lines lines new_props (new-keys new_props props))]
            (save-properties output_file new_lines)
            (print-errors errors *err*)
            (if (seq errors)
              {:failed
               (cli/fail (str (java.lang.Integer/valueOf (int (count errors))) " errors."))}
              {:success output_file}))
          (catch
            clojure.lang.ExceptionInfo
            ei
            {:failed (cli/fail (.getMessage ^java.lang.Throwable ei))})))))
  (reset-meta!
    #'ensure-transactor
    (assoc
      {:arglists (clojure.core/list [{:keys ['input-file 'output-file]}]), :column (int 1)}
      :name
      'ensure-transactor
      :ns
      *ns*))
  (defn to-json
    ([x template_file]
      (with-open [wrtr (io/writer template_file)] (binding [*out* wrtr] (json/pprint x)))))
  (reset-meta!
    #'to-json
    (assoc
      {:arglists (clojure.core/list ['x 'template-file]), :column (int 1)}
      :name
      'to-json
      :ns
      *ns*))
  (defn save-properties
    ([prop_file prop_lines]
      (with-open [wrtr (io/writer prop_file)]
        (binding [*out* wrtr]
          (loop [seq_32260 (seq prop_lines) chunk_32261 nil count_32262 0 i_32263 0]
            (if (< i_32263 count_32262)
              (let [line (.nth ^clojure.lang.Indexed chunk_32261 (int i_32263))]
                (println line)
                (recur seq_32260 chunk_32261 count_32262 (inc i_32263)))
              (let [temp__5825__auto__ (seq seq_32260)]
                (when temp__5825__auto__
                  (let [seq_32260 temp__5825__auto__]
                    (if (chunked-seq? seq_32260)
                      (let [c__6090__auto__ (chunk-first seq_32260)]
                        (recur
                          (chunk-rest seq_32260)
                          c__6090__auto__
                          (int (count c__6090__auto__))
                          (int 0)))
                      (let [line (first seq_32260)]
                        (println line)
                        (recur (next seq_32260) nil 0 0))))))))))))
  (reset-meta!
    #'save-properties
    (assoc
      {:arglists (clojure.core/list ['prop-file 'prop-lines]), :column (int 1)}
      :name
      'save-properties
      :ns
      *ns*))
  (defn eval-with
    ([form m]
      (let [makedef (fn makedef
                      ([p__32267]
                        (let [vec__32269 p__32267
                              k (nth vec__32269 (int 0) nil)
                              v (nth vec__32269 (int 1) nil)]
                          (clojure.core/list 'def k v))))
            form (seq
                   (concat
                     (clojure.core/list 'clojure.core/binding)
                     (clojure.core/list
                       (apply
                         vector
                         (seq
                           (concat
                             (clojure.core/list 'clojure.core/*ns*)
                             (clojure.core/list
                               (seq (concat (clojure.core/list 'clojure.core/gensym))))))))
                     (map makedef m)
                     (clojure.core/list form)))]
        (eval form))))
  (reset-meta!
    #'eval-with
    (assoc {:arglists (clojure.core/list ['form 'm]), :column (int 1)} :name 'eval-with :ns *ns*))
  (defn load-edn
    ([resource]
      (or
        (some-> resource (io/resource) (slurp) (edn/read-string))
        (do
          (throw (java.lang.RuntimeException. (str "Unable to load resource: " resource)))
          nil))))
  (reset-meta!
    #'load-edn
    (assoc {:arglists (clojure.core/list ['resource]), :column (int 1)} :name 'load-edn :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.provisioning.aws" "aws-instance-arch") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.provisioning.aws" "aws-instance-arch")
    (load-edn "datomic/aws/instance-arch.edn"))
  (.setMeta
    (clojure.lang.RT/var "datomic.provisioning.aws" "cf-template-template")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.provisioning.aws" "cf-template-template")
    (apply
      hash-map
      (seq
        (concat
          (clojure.core/list :Description)
          (clojure.core/list "Datomic Transactor Template")
          (clojure.core/list :Mappings)
          (clojure.core/list
            (apply
              hash-map
              (seq
                (concat
                  (clojure.core/list :AWSInstanceType2Arch)
                  (clojure.core/list
                    (reduce
                      (fn fn__32278
                        ([agg p__32277]
                          (let [vec__32279 p__32277
                                itype (nth vec__32279 (int 0) nil)
                                arch (nth vec__32279 (int 1) nil)]
                            (assoc agg (keyword itype) {:Arch arch}))))
                      {}
                      aws-instance-arch))
                  (clojure.core/list :AWSRegionArch2AMI)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'datomic.provisioning.aws/load-edn)
                        (clojure.core/list "datomic/aws/region-arch-ami.edn"))))))))
          (clojure.core/list :Parameters)
          (clojure.core/list
            (apply
              hash-map
              (seq
                (concat
                  (clojure.core/list :JavaOpts)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Description)
                          (clojure.core/list "Options passed to Java launcher")
                          (clojure.core/list :Type)
                          (clojure.core/list "String")
                          (clojure.core/list :Default)
                          (clojure.core/list 'java-opts)))))
                  (clojure.core/list :Xmx)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Description)
                          (clojure.core/list "Xmx setting for the JVM")
                          (clojure.core/list :AllowedPattern)
                          (clojure.core/list "\\d+[GgMm]")
                          (clojure.core/list :Type)
                          (clojure.core/list "String")
                          (clojure.core/list :Default)
                          (clojure.core/list 'xmx)))))
                  (clojure.core/list :InstanceType)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Description)
                          (clojure.core/list "Type of EC2 instance to launch")
                          (clojure.core/list :Type)
                          (clojure.core/list "String")
                          (clojure.core/list :Default)
                          (clojure.core/list 'aws-instance-type)))))
                  (clojure.core/list :InstanceMonitoring)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Description)
                          (clojure.core/list "Detailed monitoring for store instances?")
                          (clojure.core/list :Type)
                          (clojure.core/list "String")
                          (clojure.core/list :Default)
                          (clojure.core/list 'aws-instance-monitoring)))))
                  (clojure.core/list :GroupSize)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Description)
                          (clojure.core/list "Size of machine group")
                          (clojure.core/list :Type)
                          (clojure.core/list "String")
                          (clojure.core/list :Default)
                          (clojure.core/list 'aws-autoscaling-group-size)))))
                  (clojure.core/list :SecurityGroups)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Default)
                          (clojure.core/list 'aws-security-group)
                          (clojure.core/list :Description)
                          (clojure.core/list "Preexisting security groups.")
                          (clojure.core/list :Type)
                          (clojure.core/list "CommaDelimitedList")))))
                  (clojure.core/list :DatomicDeployBucket)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Default)
                          (clojure.core/list 'datomic-deploy-s3-bucket)
                          (clojure.core/list :Type)
                          (clojure.core/list "String")))))
                  (clojure.core/list :DatomicVersion)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Default)
                          (clojure.core/list 'datomic-version)
                          (clojure.core/list :Type)
                          (clojure.core/list "String")))))))))
          (clojure.core/list :Resources)
          (clojure.core/list
            (apply
              hash-map
              (seq
                (concat
                  (clojure.core/list :LaunchGroup)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Type)
                          (clojure.core/list "AWS::AutoScaling::AutoScalingGroup")
                          (clojure.core/list :Properties)
                          (clojure.core/list
                            (apply
                              hash-map
                              (seq
                                (concat
                                  (clojure.core/list :AvailabilityZones)
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list "Fn::GetAZs")
                                          (clojure.core/list "")))))
                                  (clojure.core/list :LaunchConfigurationName)
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list :Ref)
                                          (clojure.core/list "LaunchConfig")))))
                                  (clojure.core/list :Tags)
                                  (clojure.core/list
                                    (apply
                                      vector
                                      (seq
                                        (concat
                                          (clojure.core/list
                                            (apply
                                              hash-map
                                              (seq
                                                (concat
                                                  (clojure.core/list :Key)
                                                  (clojure.core/list "Name")
                                                  (clojure.core/list :Value)
                                                  (clojure.core/list
                                                    (apply
                                                      hash-map
                                                      (seq
                                                        (concat
                                                          (clojure.core/list :Ref)
                                                          (clojure.core/list "AWS::StackName")))))
                                                  (clojure.core/list :PropagateAtLaunch)
                                                  (clojure.core/list "true")))))))))
                                  (clojure.core/list :MaxSize)
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list :Ref)
                                          (clojure.core/list "GroupSize")))))
                                  (clojure.core/list :MinSize)
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list :Ref)
                                          (clojure.core/list "GroupSize")))))))))))))
                  (clojure.core/list :LaunchConfig)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Type)
                          (clojure.core/list "AWS::AutoScaling::LaunchConfiguration")
                          (clojure.core/list :Properties)
                          (clojure.core/list
                            (apply
                              hash-map
                              (seq
                                (concat
                                  (clojure.core/list :InstanceType)
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list :Ref)
                                          (clojure.core/list "InstanceType")))))
                                  (clojure.core/list :SecurityGroups)
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list :Ref)
                                          (clojure.core/list "SecurityGroups")))))
                                  (clojure.core/list :InstanceMonitoring)
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list :Ref)
                                          (clojure.core/list "InstanceMonitoring")))))
                                  (clojure.core/list :BlockDeviceMappings)
                                  (clojure.core/list
                                    (apply
                                      vector
                                      (seq
                                        (concat
                                          (clojure.core/list
                                            (apply
                                              hash-map
                                              (seq
                                                (concat
                                                  (clojure.core/list :DeviceName)
                                                  (clojure.core/list "/dev/sdb")
                                                  (clojure.core/list :VirtualName)
                                                  (clojure.core/list "ephemeral0")))))))))
                                  (clojure.core/list :ImageId)
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list "Fn::FindInMap")
                                          (clojure.core/list
                                            (apply
                                              vector
                                              (seq
                                                (concat
                                                  (clojure.core/list "AWSRegionArch2AMI")
                                                  (clojure.core/list
                                                    (apply
                                                      hash-map
                                                      (seq
                                                        (concat
                                                          (clojure.core/list :Ref)
                                                          (clojure.core/list "AWS::Region")))))
                                                  (clojure.core/list
                                                    (apply
                                                      hash-map
                                                      (seq
                                                        (concat
                                                          (clojure.core/list "Fn::FindInMap")
                                                          (clojure.core/list
                                                            (apply
                                                              vector
                                                              (seq
                                                                (concat
                                                                  (clojure.core/list
                                                                    "AWSInstanceType2Arch")
                                                                  (clojure.core/list
                                                                    (apply
                                                                      hash-map
                                                                      (seq
                                                                        (concat
                                                                          (clojure.core/list :Ref)
                                                                          (clojure.core/list
                                                                            "InstanceType")))))
                                                                  (clojure.core/list
                                                                    "Arch")))))))))))))))))
                                  (clojure.core/list :UserData)
                                  (clojure.core/list 'user-data)))))))))))))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.provisioning.aws" "cf-role-template-template")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.provisioning.aws" "cf-role-template-template")
    (apply
      hash-map
      (seq
        (concat
          (clojure.core/list :Parameters)
          (clojure.core/list
            (apply
              hash-map
              (seq
                (concat
                  (clojure.core/list :InstanceProfile)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Description)
                          (clojure.core/list "Preexisting IAM role / instance profile")
                          (clojure.core/list :Type)
                          (clojure.core/list "String")
                          (clojure.core/list :Default)
                          (clojure.core/list 'transactor-role)))))))))
          (clojure.core/list :Resources)
          (clojure.core/list
            (apply
              hash-map
              (seq
                (concat
                  (clojure.core/list :LaunchConfig)
                  (clojure.core/list
                    (apply
                      hash-map
                      (seq
                        (concat
                          (clojure.core/list :Properties)
                          (clojure.core/list
                            (apply
                              hash-map
                              (seq
                                (concat
                                  (clojure.core/list :IamInstanceProfile)
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list :Ref)
                                          (clojure.core/list "InstanceProfile")))))))))))))))))))))
  (defn deep-merge
    ([& vals] (if (every? map? vals) (apply merge-with deep-merge vals) (last vals))))
  (reset-meta!
    #'deep-merge
    (assoc {:arglists (clojure.core/list ['& 'vals]), :column (int 1)} :name 'deep-merge :ns *ns*))
  (defn symbolize-keys
    ([m]
      (reduce
        (fn fn__32285
          ([m p__32284]
            (let [vec__32286 p__32284
                  k (nth vec__32286 (int 0) nil)
                  v (nth vec__32286 (int 1) nil)]
              (assoc m (symbol (name k)) v))))
        {}
        m)))
  (reset-meta!
    #'symbolize-keys
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'symbolize-keys :ns *ns*))
  (defn cf-user-data
    ([ddb_properties cf_properties]
      {"Fn::Base64"
       {"Fn::Join"
        ["\n"
         ["exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1"
          {"Fn::Join" ["=" ["export XMX" {:Ref "Xmx"}]]}
          {"Fn::Join" ["=" ["export JAVA_OPTS" {:Ref "JavaOpts"}]]}
          {"Fn::Join" ["=" ["export DATOMIC_DEPLOY_BUCKET" {:Ref "DatomicDeployBucket"}]]}
          {"Fn::Join" ["=" ["export DATOMIC_VERSION" {:Ref "DatomicVersion"}]]}
          "cd /datomic"
          "cat <<EOF >aws.properties"
          "host=`curl http://169.254.169.254/latest/meta-data/local-ipv4`"
          "alt-host=`curl http://169.254.169.254/latest/meta-data/public-ipv4`"
          (str/join
            "\n"
            (map
              (fn fn__32292
                ([p__32291]
                  (let [vec__32293 p__32291
                        k (nth vec__32293 (int 0) nil)
                        v (nth vec__32293 (int 1) nil)]
                    (str (name k) "=" v))))
              (dissoc ddb_properties :host :alt-host)))
          "EOF"
          "chmod 744 aws.properties"
          (str
            "AWS_ACCESS_KEY_ID=\"${DATOMIC_READ_DEPLOY_ACCESS_KEY_ID}\""
            " AWS_SECRET_ACCESS_KEY=\"${DATOMIC_READ_DEPLOY_AWS_SECRET_KEY}\""
            " aws s3 cp \"s3://${DATOMIC_DEPLOY_BUCKET}/${DATOMIC_VERSION}/startup.sh\" startup.sh")
          "chmod 500 startup.sh"
          "./startup.sh"]]}}))
  (reset-meta!
    #'cf-user-data
    (assoc
      {:arglists (clojure.core/list ['ddb-properties 'cf-properties]), :column (int 1)}
      :name
      'cf-user-data
      :ns
      *ns*))
  (defn cf-template-args
    ([ddb_properties cf_properties]
      (let [account_id (iam/get-account-id (iam/client))
            type (common/getx cf_properties :aws-instance-type)
            xmx (common/getx cf_properties :java-xmx)
            java_opts (get cf_properties :java-opts "")
            user_data (cf-user-data ddb_properties cf_properties)
            transactor_role (get ddb_properties :aws-transactor-role)]
        (symbolize-keys
          (assoc
            cf_properties
            :xmx
            xmx
            :user-data
            user_data
            :java-opts
            java_opts
            :transactor-role
            transactor_role)))))
  (reset-meta!
    #'cf-template-args
    (assoc
      {:arglists (clojure.core/list ['ddb-properties 'cf-properties]), :column (int 1)}
      :name
      'cf-template-args
      :ns
      *ns*))
  (defn create-cf-template*
    ([ddb_properties cf_properties]
      (let [args (cf-template-args ddb_properties cf_properties)
            template (if (not (^clojure.lang.IFn args 'transactor-role))
                       cf-template-template
                       (deep-merge cf-template-template cf-role-template-template))]
        (eval-with template args))))
  (reset-meta!
    #'create-cf-template*
    (assoc
      {:arglists (clojure.core/list ['ddb-properties 'cf-properties]), :column (int 1)}
      :name
      'create-cf-template*
      :ns
      *ns*))
  (defn propmap
    ([filename]
      (let [props (common/load-properties filename)]
        (reduce
          (fn fn__32301
            ([m p__32300]
              (let [vec__32302 p__32300
                    k (nth vec__32302 (int 0) nil)
                    v (nth vec__32302 (int 1) nil)]
                (assoc m (keyword k) v))))
          {}
          props))))
  (reset-meta!
    #'propmap
    (assoc {:arglists (clojure.core/list ['filename]), :column (int 1)} :name 'propmap :ns *ns*))
  ;; Renders the EC2 autoscaling template from completed transactor and deployment properties.
  (defn create-cf-template
    ([p__32307]
      (let [map__32308 p__32307
            map__32308 (if (seq? map__32308)
                         (if (next map__32308)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32308))
                           (if (seq map__32308) (first map__32308) {}))
                         map__32308)
            ddb_properties (get map__32308 :ddb-properties)
            cf_properties (get map__32308 :cf-properties)
            json_template (get map__32308 :json-template)
            ddb_props (propmap ddb_properties)
            cf_props (propmap cf_properties)]
        (to-json (create-cf-template* ddb_props cf_props) json_template))))
  (reset-meta!
    #'create-cf-template
    (assoc
      {:arglists (clojure.core/list [{:keys ['ddb-properties 'cf-properties 'json-template]}]),
       :column (int 1)}
      :name
      'create-cf-template
      :ns
      *ns*)))
