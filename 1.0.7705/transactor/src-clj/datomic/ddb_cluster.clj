(do
  (clojure.core/in-ns 'datomic.ddb-cluster)
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
  (defn create-connection
    ([p__27618]
      (let [map__27619 p__27618
            map__27619 (if (seq? map__27619)
                         (if (next map__27619)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27619))
                           (if (seq map__27619) (first map__27619) {}))
                         map__27619)
            cluster_conf map__27619
            system_root (get map__27619 :system-root)
            params (get map__27619 :params)
            region (get map__27619 :region)
            override_endpoint (get map__27619 :override-endpoint)
            creds (when (get-in params [:ddb :aws-access-key-id])
                    (aws-helpers/static-credentials-provider (:ddb params)))
            override_endpoint (let [G__27620 override_endpoint]
                                (when-not (nil? G__27620) (str "http://" G__27620)))
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
    ([p__27623]
      (let [map__27624 p__27623
            map__27624 (if (seq? map__27624)
                         (if (next map__27624)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27624))
                           (if (seq map__27624) (first map__27624) {}))
                         map__27624)
            cluster_conf map__27624
            aws_dynamodb_table (get map__27624 :aws-dynamodb-table)]
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