(do
  (clojure.core/in-ns 'datomic.ddb-cluster)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.ddb-cluster)
    {:doc
     "Builds the Datomic clustered-store abstraction on DynamoDB. Selects region and endpoint configuration, uses explicit credentials when supplied or the AWS default provider chain, and applies Datomic timeout and concurrency policy."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.core2.aws.helpers :as 'aws-helpers]
        ['datomic.common :as 'common]
        ['datomic.cluster :as 'cluster]
        ['datomic.ddb :as 'ddb]
        ['datomic.io :as 'io]
        ['datomic.slf4j :as 'logger]
        ['datomic.ddb-values :as 'ddbv]
        ['datomic.kv-dynamo :as 'kvd]
        ['datomic.kv-cluster :as 'kvc]
        ['datomic.config :as 'config])
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'software.amazon.awssdk.core.exception.SdkClientException)
      (clojure.core/import
        'software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain)))
  (when-not (.equals 'datomic.ddb-cluster 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.ddb-cluster))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.core2.aws.helpers :as 'aws-helpers]
          ['datomic.common :as 'common]
          ['datomic.cluster :as 'cluster]
          ['datomic.ddb :as 'ddb]
          ['datomic.io :as 'io]
          ['datomic.slf4j :as 'logger]
          ['datomic.ddb-values :as 'ddbv]
          ['datomic.kv-dynamo :as 'kvd]
          ['datomic.kv-cluster :as 'kvc]
          ['datomic.config :as 'config])
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'software.amazon.awssdk.core.exception.SdkClientException)
        (clojure.core/import
          'software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain))))
  (defn key-path ([path_map k] (cluster/path (assoc path_map :key k))))
  (reset-meta!
    #'key-path
    (assoc
      {:private true, :arglists (clojure.core/list ['path-map 'k]), :column (int 1)}
      :name
      'key-path
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
  ;; Creates a table-scoped cluster using IAM/default credentials unless explicit DDB credentials are present.
  (defn create-connection
    ([p__10573]
      (let [map__10574 p__10573
            map__10574 (if (seq? map__10574)
                         (if (next map__10574)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__10574))
                           (if (seq map__10574) (first map__10574) {}))
                         map__10574)
            cluster_conf map__10574
            system_root (get map__10574 :system-root)
            params (get map__10574 :params)
            region (get map__10574 :region)
            override_endpoint (get map__10574 :override-endpoint)
            creds (when (get-in params [:ddb :aws-access-key-id])
                    (aws-helpers/static-credentials-provider (:ddb params)))
            override_endpoint (let [G__10575 override_endpoint]
                                (when-not (nil? G__10575) (str "http://" G__10575)))
            region (or region (default-aws-region))
            ddb_client (if creds
                         (ddb/client
                           creds
                           (config/ddb-client-args
                             {:region region, :override-endpoint override_endpoint}))
                         (ddb/client
                           (config/ddb-client-args
                             {:region region, :override-endpoint override_endpoint})))
            kvs (kvd/kv-dynamo ddb_client system_root)]
        (kvc/kv-cluster kvs cluster_conf))))
  (reset-meta!
    #'create-connection
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['system-root 'params 'region 'override-endpoint], :as 'cluster-conf}]),
       :column (int 1)}
      :name
      'create-connection
      :ns
      *ns*))
  (defn create-ddbx-connection
    ([p__10578]
      (let [map__10579 p__10578
            map__10579 (if (seq? map__10579)
                         (if (next map__10579)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__10579))
                           (if (seq map__10579) (first map__10579) {}))
                         map__10579)
            cluster_conf map__10579
            aws_dynamodb_table (get map__10579 :aws-dynamodb-table)]
        (create-connection
          (assoc
            cluster_conf
            :protocol
            :ddb
            :region
            (:region (aws-helpers/parse-arn aws_dynamodb_table)))))))
  (reset-meta!
    #'create-ddbx-connection
    (assoc
      {:arglists (clojure.core/list [{:keys ['aws-dynamodb-table], :as 'cluster-conf}]),
       :column (int 1)}
      :name
      'create-ddbx-connection
      :ns
      *ns*)))
