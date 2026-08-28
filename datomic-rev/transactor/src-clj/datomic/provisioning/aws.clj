(do
  (clojure.core/in-ns 'datomic.provisioning.aws)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.aws :as 'aws]
        ['clojure.java.io :as 'io]
        ['clojure.edn :as 'edn]
        ['clojure.set :as 'set]
        ['clojure.string :as 'str]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['clojure.pprint :as 'pprint]
        ['clojure.data.json :as 'json]
        ['datomic.memory :as 'memory]
        ['datomic.cli :as 'cli]
        ['datomic.ddb :as 'ddb]
        ['datomic.error :as 'error]
        ['datomic.s3 :as 's3]
        ['datomic.ec2 :as 'ec2]
        ['datomic.iam :as 'iam]
        ['datomic.slf4j :as 'logger]
        ['datomic.uri :as 'uri]
        ['clojure.string :as 'str])
      (clojure.core/import 'java.io.PushbackReader)
      (clojure.core/import 'java.io.StringReader)))
  (when-not (.equals 'datomic.provisioning.aws 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.provisioning.aws))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.aws :as 'aws]
          ['clojure.java.io :as 'io]
          ['clojure.edn :as 'edn]
          ['clojure.set :as 'set]
          ['clojure.string :as 'str]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['clojure.pprint :as 'pprint]
          ['clojure.data.json :as 'json]
          ['datomic.memory :as 'memory]
          ['datomic.cli :as 'cli]
          ['datomic.ddb :as 'ddb]
          ['datomic.error :as 'error]
          ['datomic.s3 :as 's3]
          ['datomic.ec2 :as 'ec2]
          ['datomic.iam :as 'iam]
          ['datomic.slf4j :as 'logger]
          ['datomic.uri :as 'uri]
          ['clojure.string :as 'str])
        (clojure.core/import 'java.io.PushbackReader)
        (clojure.core/import 'java.io.StringReader))))
  (def ec2-service-assume-role-policy-doc
   "{\"Version\":\"2008-10-17\",\"Statement\":[{\"Sid\":\"\",\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"ec2.amazonaws.com\"},\"Action\":\"sts:AssumeRole\"}]}")
  (def cloudformation-validators
   {"aws-instance-monitoring" identity, "datomic-deploy-s3-bucket" identity})
  (def transactor-validators
   {"protocol" #{"ddb-local" "mdev" "inf" "ddb" "dev" "sql" "olddev"},
    "port" identity,
    "host" identity})
  (defn ddb? ([props] (contains? #{"ddb-local" "ddb"} (get props "protocol"))))
  (defn comment? ([prop_line] (= (first prop_line) \#)))
  (defn trailing-slash? ([s] (boolean (re-find #"\\\s*$" s))))
  (defn prop-line-seq
    ([rdr]
      (loop [line (.readLine ^java.io.BufferedReader rdr)]
        (when line
          (if (trailing-slash? line)
            (recur (str line "\n" (.readLine ^java.io.BufferedReader rdr)))
            (cons line (lazy-seq (prop-line-seq rdr))))))))
  (defn read-prop-line
    ([prop_line]
      (if (or (comment? prop_line) (empty? prop_line))
        [prop_line]
        (let [temp__5802__auto__ (re-matches #"^([^=]+)=(.*)" prop_line)]
          (if temp__5802__auto__
            (let [vec__32787 temp__5802__auto__
                  _ (nth vec__32787 (int 0) nil)
                  k (nth vec__32787 (int 1) nil)
                  v (nth vec__32787 (int 2) nil)]
              [k (re-find #"^[\S].*" v)])
            [prop_line])))))
  (defn load-properties
    ([prop_file] (let [lines (prop-line-seq (io/reader prop_file))] (map read-prop-line lines))))
  (defn extract-prop-map
    ([prop_lines]
      (into {} (filter (fn fn__32795 ([p1__32794#] (= (long (count p1__32794#)) 2))) prop_lines))))
  (defn merge-prop-line
    ([prop_line props_map]
      (let [pred__32798 = expr__32799 (count prop_line)]
        (if (^clojure.lang.IFn pred__32798 1 (java.lang.Integer/valueOf (int expr__32799)))
          (first prop_line)
          (if (^clojure.lang.IFn pred__32798 2 (java.lang.Integer/valueOf (int expr__32799)))
            (let [vec__32800 prop_line
                  k (nth vec__32800 (int 0) nil)
                  v (nth vec__32800 (int 1) nil)]
              (str k "=" (get props_map k)))
            (do
              (throw
                (java.lang.IllegalArgumentException.
                  (str "No matching clause: " (java.lang.Integer/valueOf (int expr__32799)))))
              nil))))))
  (defn merge-prop-lines
    ([prop_lines prop_map new_keys]
      (concat
        (map (fn fn__32805 ([p1__32804#] (merge-prop-line p1__32804# prop_map))) prop_lines)
        (map (fn fn__32807 ([k] (str k "=" (get prop_map k)))) new_keys))))
  (defn save-properties
    ([prop_file prop_lines]
      (with-open [wrtr (io/writer prop_file)]
        (binding [*out* wrtr]
          (loop [seq_32956 (seq prop_lines) chunk_32957 nil count_32958 0 i_32959 0]
            (if (< i_32959 count_32958)
              (let [line (.nth ^clojure.lang.Indexed chunk_32957 (int i_32959))]
                (println line)
                (recur seq_32956 chunk_32957 count_32958 (inc i_32959)))
              (let [temp__5804__auto__ (seq seq_32956)]
                (when temp__5804__auto__
                  (let [seq_32956 temp__5804__auto__]
                    (if (chunked-seq? seq_32956)
                      (let [c__6065__auto__ (chunk-first seq_32956)]
                        (recur
                          (chunk-rest seq_32956)
                          c__6065__auto__
                          (int (count c__6065__auto__))
                          (int 0)))
                      (let [line (first seq_32956)]
                        (println line)
                        (recur (next seq_32956) nil 0 0))))))))))))
  (defn add-errors ([props & errors] (assoc props :errors (into (get props :errors []) errors))))
  (defn add-aws-exception
    ([props ex]
      (if (instance? com.amazonaws.AmazonServiceException ex)
        (add-errors props (str (.getServiceName ex) (.getErrorCode ex) (.getMessage ex)))
        (add-errors props (.getMessage ex)))))
  (defn record-aws-exception
    ([&form &env props & forms]
      (seq
        (concat
          (clojure.core/list 'try)
          forms
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'catch)
                (clojure.core/list 'com.amazonaws.AmazonClientException)
                (clojure.core/list 'e)
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'datomic.provisioning.aws/add-aws-exception)
                      (clojure.core/list props)
                      (clojure.core/list 'e)))))))))))
  (.setMacro #'record-aws-exception)
  (defn ensure-required-props
    ([props validators]
      (let [errors (reduce
                     (fn fn__32821
                       ([errors p__32820]
                         (let [vec__32822 p__32820
                               k (nth vec__32822 (int 0) nil)
                               v (nth vec__32822 (int 1) nil)
                               temp__5802__auto__ (get props k)]
                           (if temp__5802__auto__
                             (let [prop temp__5802__auto__]
                               (if (^clojure.lang.IFn v prop)
                                 errors
                                 (conj errors (str "Invalid value for property: " k "=" prop))))
                             (conj errors (str "Missing property: " k))))))
                     []
                     validators)]
        (apply add-errors props errors))))
  (defn check-existing-table-schema
    ([props client table_name]
      (let [desc (ddb/describe-table client {:tableName table_name})
            attribute_definition (get-in desc [:table :attributeDefinitions 0])
            map__32828 attribute_definition
            map__32828 (if (seq? map__32828)
                         (if (next map__32828)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32828))
                           (if (seq map__32828) (first map__32828) {}))
                         map__32828)
            attributeName (get map__32828 :attributeName)
            attributeType (get map__32828 :attributeType)]
        (if (and (= attributeName "id") (= attributeType "S"))
          props
          (add-errors
            props
            (str "Invalid attribute definition: " attributeName ", " attributeType))))))
  (defn create-cluster-table
    ([ddb_client table_name read_units write_units]
      (ddb/create-table
        ddb_client
        {:tableName table_name,
         :provisionedThroughput {:readCapacityUnits read_units, :writeCapacityUnits write_units},
         :keySchema
         [{:attributeName "id", :keyType com.amazonaws.services.dynamodbv2.model.KeyType/HASH}],
         :attributeDefinitions [{:attributeType "S", :attributeName "id"}]}))
    ([ddb_client table_name] (create-cluster-table ddb_client table_name 100 50)))
  (defn ensure-ddb-table
    ([props]
      (if (ddb? props)
        (try
          (let [client (ddb/client
                         nil
                         (merge
                           (let [temp__5804__auto__ (get props "aws-dynamodb-region")]
                             (when temp__5804__auto__
                               (let [region temp__5804__auto__] {:region region})))
                           (let [temp__5804__auto__ (get props "aws-dynamodb-override-endpoint")]
                             (when temp__5804__auto__
                               (let [override_endpoint temp__5804__auto__]
                                 {:override-endpoint override_endpoint})))))
                table_name (get props "aws-dynamodb-table")
                tables (:tableNames (ddb/list-tables client {}))]
            (if (some #{table_name} tables)
              (check-existing-table-schema props client table_name)
              (create-cluster-table client table_name))
            props)
          (catch com.amazonaws.AmazonClientException e (add-aws-exception props e)))
        props)))
  (defn ensure-ddb-table-name
    ([props]
      (if (ddb? props)
        (assoc props "aws-dynamodb-table" (get props "aws-dynamodb-table" "datomic"))
        props)))
  (defn create-system-command
    ([p__32836]
      (let [map__32837 p__32836
            map__32837 (if (seq? map__32837)
                         (if (next map__32837)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32837))
                           (if (seq map__32837) (first map__32837) {}))
                         map__32837)
            region (get map__32837 :region)
            override_endpoint (get map__32837 :override-endpoint)
            table_name (get map__32837 :table-name)
            read_capacity (get map__32837 :read-capacity)
            write_capacity (get map__32837 :write-capacity)
            s__6419__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6419__auto__]
          (do
            (pprint/pprint
              (create-cluster-table
                (ddb/client nil {:region region, :override-endpoint override_endpoint})
                table_name
                read_capacity
                write_capacity))
            (str s__6419__auto__))))))
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
        (catch com.amazonaws.AmazonClientException e (add-aws-exception props e)))))
  (defn user-prop-names
    ([prefix]
      (map
        (fn fn__32843 ([p1__32842#] (str prefix p1__32842#)))
        ["-user" "-access-key-id" "-secret-key"])))
  (defn ensure-unique-user
    ([user_name]
      (let [client (iam/client) user_names (map :userName (:users (iam/list-users client {})))]
        (loop [i 1 gen_user_name user_name]
          (if (some #{gen_user_name} user_names)
            (recur (inc i) (str user_name "-" (long i)))
            gen_user_name)))))
  (defn ensure-unique-role
    ([role_name]
      (let [client (iam/client)
            res (iam/list-roles client {})
            role_names (map :roleName (:roles res))]
        (loop [i 1 gen_role_name role_name]
          (if (some #{gen_role_name} role_names)
            (recur (inc i) (str role_name "-" (long i)))
            gen_role_name)))))
  (defn add-user ([props user] (assoc props :users (merge (get props :users {}) user))))
  (defn ensure-new-user
    ([props user_name prefix]
      (let [client (iam/client)
            vec__32849 (user-prop-names prefix)
            user (nth vec__32849 (int 0) nil)
            access (nth vec__32849 (int 1) nil)
            secret (nth vec__32849 (int 2) nil)
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
              (iam/create-user client {:userName user_name})
              (catch
                com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException
                _
                nil))
            (try
              (let [map__32854 (:accessKey (iam/create-access-key client {:userName user_name}))
                    map__32854 (if (seq? map__32854)
                                 (if (next map__32854)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__32854))
                                   (if (seq map__32854) (first map__32854) {}))
                                 map__32854)
                    accessKeyId (get map__32854 :accessKeyId)
                    secretAccessKey (get map__32854 :secretAccessKey)]
                (assoc
                  (add-user
                    props
                    {user_name
                     {:aws-access-key-id accessKeyId, :aws-secret-access-key secretAccessKey}})
                  user
                  user_name
                  access
                  accessKeyId
                  secret
                  secretAccessKey))
              (catch
                com.amazonaws.services.identitymanagement.model.LimitExceededException
                _
                (add-errors
                  props
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
                        (str s__6419__auto__))))))))))))
  (defn ensure-existing-user
    ([props user_name]
      (let [client (iam/client)] (iam/get-user client {:userName user_name}) props)))
  (defn ensure-user-policy
    ([props prefix policy]
      (let [client (iam/client)
            account_id (iam/get-account-id client)
            map__32860 policy
            map__32860 (if (seq? map__32860)
                         (if (next map__32860)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32860))
                           (if (seq map__32860) (first map__32860) {}))
                         map__32860)
            policy_fn (get map__32860 :fn)
            args (get map__32860 :args)
            policy_name_suffix (get map__32860 :name)
            resolved_args (into
                            {}
                            (map
                              (fn fn__32862
                                ([p__32861]
                                  (let [vec__32863 p__32861
                                        k (nth vec__32863 (int 0) nil)
                                        v (nth vec__32863 (int 1) nil)]
                                    [k (get props v)])))
                              args))
            policy_doc (^clojure.lang.IFn policy_fn (merge resolved_args {:account-id account_id}))
            policy_name (str/join "-" (concat (vals resolved_args) [policy_name_suffix]))
            user_name (get props (str prefix "-user"))
            user_policies (iam/list-user-policies client {:userName user_name})]
        (when-not (some #{policy_name} user_policies)
          (iam/put-user-policy
            client
            {:userName user_name, :policyName policy_name, :policyDocument policy_doc})))
      props))
  (defn ensure-user
    ([props prefix required policy]
      (try
        (let [ks (user-prop-names prefix)
              vec__32868 ks
              user (nth vec__32868 (int 0) nil)
              access (nth vec__32868 (int 1) nil)
              secret (nth vec__32868 (int 2) nil)
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
        (catch com.amazonaws.AmazonClientException e (add-aws-exception props e)))))
  (defn ensure-role-policy
    ([props prefix policy]
      (let [client (iam/client)
            account_id (iam/get-account-id client)
            map__32875 policy
            map__32875 (if (seq? map__32875)
                         (if (next map__32875)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32875))
                           (if (seq map__32875) (first map__32875) {}))
                         map__32875)
            policy_fn (get map__32875 :fn)
            args (get map__32875 :args)
            policy_name_suffix (get map__32875 :name)
            resolved_args (into
                            {}
                            (map
                              (fn fn__32877
                                ([p__32876]
                                  (let [vec__32878 p__32876
                                        k (nth vec__32878 (int 0) nil)
                                        v (nth vec__32878 (int 1) nil)]
                                    [k (get props v)])))
                              args))
            policy_doc (^clojure.lang.IFn policy_fn (merge resolved_args {:account-id account_id}))
            policy_name (str/join "-" (concat (vals resolved_args) [policy_name_suffix]))
            role_name (get props (str prefix "-role"))
            role_policies (iam/list-role-policies client {:roleName role_name})]
        (when-not (some #{policy_name} role_policies)
          (iam/put-role-policy
            client
            {:roleName role_name, :policyName policy_name, :policyDocument policy_doc})))
      props))
  (defn ensure-new-role
    ([name]
      (let [client (iam/client)
            role (:role
                   (iam/create-role
                     client
                     {:roleName name,
                      :assumeRolePolicyDocument ec2-service-assume-role-policy-doc}))
            instance_profile (:instanceProfile
                               (iam/create-instance-profile client {:instanceProfileName name}))]
        (iam/add-role-to-instance-profile client {:roleName name, :instanceProfileName name})
        role)))
  (defn ensure-role-policies
    ([props prefix]
      (let [pred__32884 = expr__32885 prefix]
        (if (^clojure.lang.IFn pred__32884 "aws-transactor" expr__32885)
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
          (if (^clojure.lang.IFn pred__32884 "aws-peer" expr__32885)
            (ensure-role-policy
              props
              "aws-peer"
              {:fn iam/dynamo-r-policy-command,
               :name "peer",
               :args {:table-name "aws-dynamodb-table"}})
            (throw (ex-info "Unrecognized prefix" {:prefix prefix})))))
      props))
  (defn get-role
    ([name]
      (let [client (iam/client)]
        (try
          (iam/get-role client {:roleName name})
          (catch com.amazonaws.services.identitymanagement.model.NoSuchEntityException _ nil)))))
  (defn ensure-role
    ([props prefix]
      (let [role_key (str prefix "-role")
            role_val (^clojure.lang.IFn props role_key)
            role_name (if (not (empty? role_val))
                        role_val
                        (ensure-unique-role (str "datomic-" prefix)))
            role (or (get-role role_name) (ensure-new-role role_name))]
        (ensure-role-policies (assoc props role_key role_name) prefix))))
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
  (defmulti ensure-transactor* (fn fn__32893 ([props] (keyword (get props "protocol")))))
  (defmethod
    ensure-transactor*
    :ddb
    fn__32898
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
    fn__32900
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
  (defn ensure-autoscaling-group-size
    ([props]
      (let [v (get props "aws-autoscaling-group-size")
            temp__5802__auto__ (read-pred
                                 v
                                 (fn fn__32907
                                   ([p1__32906#]
                                     (and
                                       (integer? p1__32906#)
                                       (clojure.lang.Numbers/isPos p1__32906#))))
                                 nil)]
        (if temp__5802__auto__
          (let [size temp__5802__auto__] props)
          (add-errors props (str "Invalid value for aws-autoscaling-group-size: " v))))))
  (defn ensure-java-xmx
    ([props]
      (if (get props "java-xmx")
        props
        (let [type (common/getx props "aws-instance-type")]
          (assoc props "java-xmx" (:xmx (memory/aws-transactor-settings type)))))))
  (defn ensure-java-opts
    ([props]
      (let [temp__5802__auto__ (get props "java-opts")]
        (if temp__5802__auto__
          (let [opts temp__5802__auto__] props)
          (assoc props "java-opts" "")))))
  (defn ensure-datomic-version
    ([props]
      (let [temp__5802__auto__ (get props "datomic-version")]
        (if temp__5802__auto__
          (let [v temp__5802__auto__] props)
          (assoc props "datomic-version" (config/property "datomic.version"))))))
  (defn ensure-security-group
    ([props]
      (let [temp__5802__auto__ (get props "aws-security-group")]
        (if temp__5802__auto__
          (let [group temp__5802__auto__]
            (ec2/ensure-security-group
              (ec2/client nil {:region (get props "aws-region")})
              group
              "Provisioned by Datomic")
            props)
          (add-errors props (str "Missing property: aws-security-group"))))))
  (defn parse-groups
    ([groups_str account_id]
      (map
        (fn fn__32921
          ([p1__32920#]
            (let [G__32922 (count p1__32920#)]
              (case
                G__32922
                1
                {:userId account_id, :groupName (first p1__32920#)}
                2
                {:userId (first p1__32920#), :groupName (second p1__32920#)}
                (do
                  (throw
                    (java.lang.RuntimeException. (str p1__32920# " is not an AWS security group")))
                  G__32922)))))
        (map
          (fn fn__32924 ([p1__32919#] (str/split p1__32919# #":")))
          (str/split groups_str #",")))))
  (defn ensure-ingress-groups
    ([props]
      (let [port 4334]
        (let [temp__5804__auto__ (get props "aws-security-group")]
          (when temp__5804__auto__
            (let [transactor_group temp__5804__auto__
                  temp__5804__auto__ (get props "aws-ingress-groups")]
              (when temp__5804__auto__
                (let [groups temp__5804__auto__]
                  (ec2/ensure-ingress
                    (ec2/client nil {:region (get props "aws-region")})
                    transactor_group
                    {:ipProtocol "tcp",
                     :toPort (long port),
                     :fromPort (long port),
                     :userIdGroupPairs (parse-groups groups (iam/get-account-id (iam/client))),
                     :ipRanges []}))))))
        props)))
  (defn ensure-ingress-cidrs
    ([props]
      (let [port 4334]
        (let [temp__5804__auto__ (get props "aws-security-group")]
          (when temp__5804__auto__
            (let [group temp__5804__auto__ temp__5804__auto__ (get props "aws-ingress-cidrs")]
              (when temp__5804__auto__
                (let [cidrs temp__5804__auto__]
                  (ec2/ensure-ingress
                    (ec2/client nil {:region (get props "aws-region")})
                    group
                    {:ipProtocol "tcp",
                     :toPort (long port),
                     :fromPort (long port),
                     :userIdGroupPairs [],
                     :ipRanges (str/split cidrs #",")}))))))
        props)))
  (defn denil
    ([m]
      (reduce
        (fn fn__32934
          ([m p__32933]
            (let [vec__32935 p__32933
                  k (nth vec__32935 (int 0) nil)
                  v (nth vec__32935 (int 1) nil)]
              (if (nil? v) m (assoc m k v)))))
        {}
        m)))
  (defn ensure-cf*
    ([props]
      (let [account_id (iam/get-account-id (iam/client))]
        (ensure-ingress-cidrs
          (-> (denil props)
           (ensure-required-props cloudformation-validators)
           (ensure-autoscaling-group-size)
           (ensure-java-xmx)
           (ensure-java-opts)
           (ensure-datomic-version)
           (ensure-security-group)
           (ensure-ingress-groups))))))
  (defn print-errors
    ([errors dest]
      (binding [*out* dest]
        (loop [seq_32941 (seq errors) chunk_32942 nil count_32943 0 i_32944 0]
          (if (< i_32944 count_32943)
            (let [error (.nth ^clojure.lang.Indexed chunk_32942 (int i_32944))]
              (println error)
              (recur seq_32941 chunk_32942 count_32943 (inc i_32944)))
            (let [temp__5804__auto__ (seq seq_32941)]
              (when temp__5804__auto__
                (let [seq_32941 temp__5804__auto__]
                  (if (chunked-seq? seq_32941)
                    (let [c__6065__auto__ (chunk-first seq_32941)]
                      (recur
                        (chunk-rest seq_32941)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [error (first seq_32941)]
                      (println error)
                      (recur (next seq_32941) nil 0 0)))))))))))
  (defn new-keys
    ([ensured_map orig_map] (filter string? (keys (apply dissoc ensured_map (keys orig_map))))))
  (defn ensure-cf
    ([p__32949]
      (let [map__32950 p__32949
            map__32950 (if (seq? map__32950)
                         (if (next map__32950)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32950))
                           (if (seq map__32950) (first map__32950) {}))
                         map__32950)
            input_file (get map__32950 :input-file)
            output_file (get map__32950 :output-file)
            lines (load-properties input_file)
            props (extract-prop-map lines)]
        (try
          (let [new_props (ensure-cf* props)
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
  (defn ensure-transactor
    ([p__32952]
      (let [map__32953 p__32952
            map__32953 (if (seq? map__32953)
                         (if (next map__32953)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32953))
                           (if (seq map__32953) (first map__32953) {}))
                         map__32953)
            input_file (get map__32953 :input-file)
            output_file (get map__32953 :output-file)
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
  (defn to-json
    ([x template_file]
      (with-open [wrtr (io/writer template_file)] (binding [*out* wrtr] (json/pprint x)))))
  (defn save-properties
    ([prop_file prop_lines]
      (with-open [wrtr (io/writer prop_file)]
        (binding [*out* wrtr]
          (loop [seq_32956 (seq prop_lines) chunk_32957 nil count_32958 0 i_32959 0]
            (if (< i_32959 count_32958)
              (let [line (.nth ^clojure.lang.Indexed chunk_32957 (int i_32959))]
                (println line)
                (recur seq_32956 chunk_32957 count_32958 (inc i_32959)))
              (let [temp__5804__auto__ (seq seq_32956)]
                (when temp__5804__auto__
                  (let [seq_32956 temp__5804__auto__]
                    (if (chunked-seq? seq_32956)
                      (let [c__6065__auto__ (chunk-first seq_32956)]
                        (recur
                          (chunk-rest seq_32956)
                          c__6065__auto__
                          (int (count c__6065__auto__))
                          (int 0)))
                      (let [line (first seq_32956)]
                        (println line)
                        (recur (next seq_32956) nil 0 0))))))))))))
  (defn eval-with
    ([form m]
      (let [makedef (fn makedef
                      ([p__32963]
                        (let [vec__32965 p__32963
                              k (nth vec__32965 (int 0) nil)
                              v (nth vec__32965 (int 1) nil)]
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
  (defn load-edn
    ([resource]
      (or
        (some-> resource (io/resource) (slurp) (edn/read-string))
        (do
          (throw (java.lang.RuntimeException. (str "Unable to load resource: " resource)))
          nil))))
  (def aws-instance-arch (load-edn "datomic/aws/instance-arch.edn"))
  (def cf-template-template
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
                     (fn fn__32974
                       ([agg p__32973]
                         (let [vec__32975 p__32973
                               itype (nth vec__32975 (int 0) nil)
                               arch (nth vec__32975 (int 1) nil)]
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
  (def cf-role-template-template
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
  (defn symbolize-keys
    ([m]
      (reduce
        (fn fn__32981
          ([m p__32980]
            (let [vec__32982 p__32980
                  k (nth vec__32982 (int 0) nil)
                  v (nth vec__32982 (int 1) nil)]
              (assoc m (symbol (name k)) v))))
        {}
        m)))
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
              (fn fn__32988
                ([p__32987]
                  (let [vec__32989 p__32987
                        k (nth vec__32989 (int 0) nil)
                        v (nth vec__32989 (int 1) nil)]
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
  (defn create-cf-template*
    ([ddb_properties cf_properties]
      (let [args (cf-template-args ddb_properties cf_properties)
            template (if (not (^clojure.lang.IFn args 'transactor-role))
                       cf-template-template
                       (deep-merge cf-template-template cf-role-template-template))]
        (eval-with template args))))
  (defn propmap
    ([filename]
      (let [props (common/load-properties filename)]
        (reduce
          (fn fn__32997
            ([m p__32996]
              (let [vec__32998 p__32996
                    k (nth vec__32998 (int 0) nil)
                    v (nth vec__32998 (int 1) nil)]
                (assoc m (keyword k) v))))
          {}
          props))))
  (defn create-cf-template
    ([p__33003]
      (let [map__33004 p__33003
            map__33004 (if (seq? map__33004)
                         (if (next map__33004)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__33004))
                           (if (seq map__33004) (first map__33004) {}))
                         map__33004)
            ddb_properties (get map__33004 :ddb-properties)
            cf_properties (get map__33004 :cf-properties)
            json_template (get map__33004 :json-template)
            ddb_props (propmap ddb_properties)
            cf_props (propmap cf_properties)]
        (to-json (create-cf-template* ddb_props cf_props) json_template)))))