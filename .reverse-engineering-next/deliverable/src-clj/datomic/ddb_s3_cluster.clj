(do
  (clojure.core/in-ns 'datomic.ddb-s3-cluster)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.ddb-s3-cluster)
    {:doc "A ClusteredStore that writes refs to Dynamo\nand values to EFS+S3."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.edn :as 'edn]
        ['clojure.string :as 'str]
        ['datomic.aws :as 'aws]
        ['datomic.cli :as 'cli]
        ['datomic.cluster :as 'cluster]
        ['datomic.combined-cluster :as 'cc]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.core2.aws.s3.sdkv1 :as 'sdkv1]
        ['datomic.core2.val-store.double-store :as 'dstore]
        ['datomic.core2.val-store.fs :as 'fs]
        ['datomic.core2.val-store.s3 :as 's3-vs]
        ['datomic.core2.val-store.s3.sdkv1 :as 's3-sdkv1]
        ['datomic.ddb :as 'ddb]
        ['datomic.error :as 'error]
        ['datomic.garbage.pod :as 'pod-garbage]
        ['datomic.io :as 'io]
        ['datomic.kv-cluster :as 'kvc]
        ['datomic.kv-dynamo :as 'kvd]
        ['datomic.slf4j :as 'logger]
        ['datomic.val-cluster :as 'vc])
      (clojure.core/import 'java.util.concurrent.Semaphore)))
  (when-not (.equals 'datomic.ddb-s3-cluster 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.ddb-s3-cluster))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.edn :as 'edn]
          ['clojure.string :as 'str]
          ['datomic.aws :as 'aws]
          ['datomic.cli :as 'cli]
          ['datomic.cluster :as 'cluster]
          ['datomic.combined-cluster :as 'cc]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.core2.aws.s3.sdkv1 :as 'sdkv1]
          ['datomic.core2.val-store.double-store :as 'dstore]
          ['datomic.core2.val-store.fs :as 'fs]
          ['datomic.core2.val-store.s3 :as 's3-vs]
          ['datomic.core2.val-store.s3.sdkv1 :as 's3-sdkv1]
          ['datomic.ddb :as 'ddb]
          ['datomic.error :as 'error]
          ['datomic.garbage.pod :as 'pod-garbage]
          ['datomic.io :as 'io]
          ['datomic.kv-cluster :as 'kvc]
          ['datomic.kv-dynamo :as 'kvd]
          ['datomic.slf4j :as 'logger]
          ['datomic.val-cluster :as 'vc])
        (clojure.core/import 'java.util.concurrent.Semaphore))))
  (def storage-config-key "ref-storage-config")
  (reset-meta!
    #'storage-config-key
    (assoc {:const true, :column 1} :name 'storage-config-key :ns *ns*))
  (defonce efs-delete-pool
   (delay
     (common/thread-pool
       {:nthreads (config/property "datomic.efsDeletePool"), :name "efs-delete"})))
  (reset-meta!
    #'efs-delete-pool
    (assoc {:private true, :column 1} :name 'efs-delete-pool :ns *ns*))
  (defonce efs-read-pool (delay (common/cached-thread-pool {:name "efs-read"})))
  (reset-meta! #'efs-read-pool (assoc {:private true, :column 1} :name 'efs-read-pool :ns *ns*))
  (defonce efs-write-pool
   (delay
     (common/handoff-thread-pool
       {:core-threads 2,
        :max-threads (config/property "datomic.efsWritePool"),
        :name "efs-write"})))
  (reset-meta! #'efs-write-pool (assoc {:private true, :column 1} :name 'efs-write-pool :ns *ns*))
  (defonce s3-read-pool (delay (common/cached-thread-pool {:name "s3-read"})))
  (reset-meta! #'s3-read-pool (assoc {:private true, :column 1} :name 's3-read-pool :ns *ns*))
  (defonce s3-write-pool (delay (common/cached-thread-pool {:name "s3-write"})))
  (reset-meta! #'s3-write-pool (assoc {:private true, :column 1} :name 's3-write-pool :ns *ns*))
  (defn storage-path
    ([p__22769]
      (let [map__22770 p__22769
            map__22770 (if (seq? map__22770)
                         (clojure.lang.PersistentHashMap/create (seq map__22770))
                         map__22770)
            system (get map__22770 :system)
            db_id (get map__22770 :db-id)]
        (str (format "/%s" system) "/data" (when db_id (format "/%s" db_id)) "/vals"))))
  (reset-meta!
    #'storage-path
    (assoc
      {:private true, :arglists (clojure.core/list [{:keys ['system 'db-id]}]), :column 1}
      :name
      'storage-path
      :ns
      *ns*))
  (defn get-config-refval
    ([kv_cluster]
      (let [refval (deref (cluster/get-ref kv_cluster "ref-storage-config"))
            conf (edn/read-string (:key refval))]
        {:val conf, :rev (:rev refval)})))
  (reset-meta!
    #'get-config-refval
    (assoc
      {:private true, :arglists (clojure.core/list ['kv-cluster]), :column 1}
      :name
      'get-config-refval
      :ns
      *ns*))
  (defn get-valid-config!
    ([kv_store cluster_conf]
      (let [cluster (kvc/kv-cluster kv_store cluster_conf) conf (:val (get-config-refval cluster))]
        (if (and map? conf (every? conf [:s3-vals-bucket :s3-vals-prefix :fs-vals-path]))
          conf
          (error/raise
            :ddb-s3-cluster/missing-configuration
            (str (:aws-dynamodb-table cluster_conf) " is not configured for ddb+s3 storage"))))))
  (reset-meta!
    #'get-valid-config!
    (assoc
      {:private true, :arglists (clojure.core/list ['kv-store 'cluster-conf]), :column 1}
      :name
      'get-valid-config!
      :ns
      *ns*))
  (defn ensure-config
    ([p__22776]
      (let [map__22777 p__22776
            map__22777 (if (seq? map__22777)
                         (clojure.lang.PersistentHashMap/create (seq map__22777))
                         map__22777)
            storage_config map__22777
            bucket (get map__22777 :bucket)
            bucket_prefix (get map__22777 :bucket-prefix)
            force (get map__22777 :force false)
            efs_path (get map__22777 :efs-path)
            region (get map__22777 :region)
            system (get map__22777 :system "_default")
            table_name (get map__22777 :table-name)
            ddb_client (ddb/client
                         nil
                         (dissoc
                           (config/ddb-client-args {:region region})
                           :clientExecutionTimeout
                           :connectionTimeout
                           :requestTimeout
                           :socketTimeout))
            kvs (kvd/kv-dynamo ddb_client table_name system)
            cluster (kvc/kv-cluster kvs {:protocol :ddb+s3})
            current_refval (get-config-refval cluster)
            new_conf {:s3-vals-bucket bucket,
                      :s3-vals-prefix (str/replace bucket_prefix #"/$" ""),
                      :fs-vals-path efs_path}]
        (if (= (:val current_refval) new_conf)
          {:success new_conf}
          (let [rev (if force (inc (or (:rev current_refval) -1)) (or (:rev current_refval) 0))]
            (push-thread-bindings (hash-map #'*print-length* nil #'*print-level* nil))
            (let [result (deref
                           (cluster/set-ref
                             cluster
                             "ref-storage-config"
                             rev
                             (try (pr-str new_conf) (finally (pop-thread-bindings)))))]
              (if (= :ok result) {:success new_conf} {:failed result})))))))
  (reset-meta!
    #'ensure-config
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [{:keys ['bucket 'bucket-prefix 'force 'efs-path 'region 'system 'table-name],
           :as 'storage-config,
           :or {'system 'common/DEFAULT_SYSTEM_NAME, 'force false}}]),
       :column 1}
      :name
      'ensure-config
      :ns
      *ns*))
  (defn create-s3-store
    ([p__22783]
      (let [map__22784 p__22783
            map__22784 (if (seq? map__22784)
                         (clojure.lang.PersistentHashMap/create (seq map__22784))
                         map__22784)
            cluster_conf map__22784
            aws_region (get map__22784 :aws-region)
            s3_vals_bucket (get map__22784 :s3-vals-bucket)
            s3_vals_prefix (get map__22784 :s3-vals-prefix)
            s3_client (sdkv1/s3-service
                        {:region aws_region,
                         :client-conf (aws/client-config (config/s3-client-args))})
            max_retries (config/property "datomic.s3MaxRetries")
            retry_opts {:base 2,
                        :backoff (config/property "datomic.s3RetryBaseDelay"),
                        :retriable?
                        (fn fn__22786
                          ([p__22785]
                            (let [map__22787 p__22785
                                  map__22787 (if (seq? map__22787)
                                               (clojure.lang.PersistentHashMap/create
                                                 (seq map__22787))
                                               map__22787)
                                  i (get map__22787 :i)
                                  result (get map__22787 :result)]
                              (and
                                (<= i max_retries)
                                (canom/anom result)
                                (not (canom/not-found? result))))))}
            retry_fn (fn retry_fn ([f op] (s3-vs/retry-handler f op retry_opts)))]
        (s3-sdkv1/create
          {:bucket s3_vals_bucket,
           :prefix (str s3_vals_prefix (storage-path cluster_conf)),
           :read-pool (deref s3-read-pool),
           :write-pool (deref s3-write-pool),
           :client s3_client,
           :retry-fn retry_fn}))))
  (reset-meta!
    #'create-s3-store
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [{:keys ['aws-region 's3-vals-bucket 's3-vals-prefix], :as 'cluster-conf}]),
       :column 1}
      :name
      'create-s3-store
      :ns
      *ns*))
  (defn create-connection-with-efs
    ([p__22794]
      (let [map__22795 p__22794
            map__22795 (if (seq? map__22795)
                         (clojure.lang.PersistentHashMap/create (seq map__22795))
                         map__22795)
            cluster_conf map__22795
            near_store_get_timeout_msec (get map__22795 :near-store-get-timeout-msec 20)
            aws_region (get map__22795 :aws-region)
            aws_dynamodb_table (get map__22795 :aws-dynamodb-table)
            system (get map__22795 :system)
            retrying_delete (partial
                              kvc/retry-fn
                              (java.util.concurrent.Semaphore.
                                (int (config/property "datomic.deleteConcurrency"))
                                (boolean (.booleanValue true)))
                              :StorageDeleteBackoffMsec
                              false
                              (atom {})
                              :linear)
            cluster_conf (merge
                           cluster_conf
                           {:retrying-delete retrying_delete,
                            :pod-garbage-handler pod-garbage/schedule-gc})
            ddb_client (ddb/client nil (config/ddb-client-args {:region aws_region}))
            kvs (kvd/kv-dynamo ddb_client aws_dynamodb_table system)
            map__22796 (get-valid-config! kvs cluster_conf)
            map__22796 (if (seq? map__22796)
                         (clojure.lang.PersistentHashMap/create (seq map__22796))
                         map__22796)
            store_config map__22796
            fs_vals_path (get map__22796 :fs-vals-path)
            s3_store (create-s3-store (merge cluster_conf store_config))
            fs_store (fs/create
                       {:delete-pool (deref efs-delete-pool),
                        :get-pool (deref efs-read-pool),
                        :path (str fs_vals_path (storage-path cluster_conf)),
                        :put-pool (deref efs-write-pool)})
            double_store (dstore/create
                           {:near-store fs_store,
                            :far-store s3_store,
                            :near-store-get-timeout-msec near_store_get_timeout_msec})
            ddb_cluster (kvc/kv-cluster kvs cluster_conf)
            s3+efs_cluster (vc/val-cluster double_store)]
        (cc/combined-cluster ddb_cluster s3+efs_cluster))))
  (reset-meta!
    #'create-connection-with-efs
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [{:keys ['near-store-get-timeout-msec 'aws-region 'aws-dynamodb-table 'system],
           :as 'cluster-conf,
           :or {'near-store-get-timeout-msec 20}}]),
       :column 1}
      :name
      'create-connection-with-efs
      :ns
      *ns*))
  (defn create-connection-without-efs
    ([p__22798]
      (let [map__22799 p__22798
            map__22799 (if (seq? map__22799)
                         (clojure.lang.PersistentHashMap/create (seq map__22799))
                         map__22799)
            cluster_conf map__22799
            aws_region (get map__22799 :aws-region)
            aws_dynamodb_table (get map__22799 :aws-dynamodb-table)
            system (get map__22799 :system)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-s3-cluster")]
          (when (.isWarnEnabled ^org.slf4j.Logger logger)
            (.warn ^org.slf4j.Logger logger (logger/process "Running without EFS!"))
            nil)
          nil)
        (let [retrying_delete (partial
                                kvc/retry-fn
                                (java.util.concurrent.Semaphore.
                                  (int (config/property "datomic.deleteConcurrency"))
                                  (boolean (.booleanValue true)))
                                :StorageDeleteBackoffMsec
                                false
                                (atom {})
                                :linear)
              cluster_conf (merge
                             cluster_conf
                             {:retrying-delete retrying_delete,
                              :pod-garbage-handler pod-garbage/schedule-gc})
              ddb_client (ddb/client nil (config/ddb-client-args {:region aws_region}))
              kvs (kvd/kv-dynamo ddb_client aws_dynamodb_table system)
              store_config (get-valid-config! kvs cluster_conf)
              s3_store (create-s3-store (merge cluster_conf store_config))
              ddb_cluster (kvc/kv-cluster kvs cluster_conf)
              s3_cluster (vc/val-cluster s3_store)]
          (cc/combined-cluster ddb_cluster s3_cluster)))))
  (reset-meta!
    #'create-connection-without-efs
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [{:keys ['aws-region 'aws-dynamodb-table 'system], :as 'cluster-conf}]),
       :column 1}
      :name
      'create-connection-without-efs
      :ns
      *ns*))
  (defn create-connection
    ([p__22801]
      (let [map__22802 p__22801
            map__22802 (if (seq? map__22802)
                         (clojure.lang.PersistentHashMap/create (seq map__22802))
                         map__22802)
            cluster_conf map__22802
            skip_efs (get map__22802 :skip-efs)]
        (if (boolean skip_efs)
          (create-connection-without-efs cluster_conf)
          (create-connection-with-efs cluster_conf)))))
  (defn ensure-system
    ([p__22804]
      (let [map__22805 p__22804
            map__22805 (if (seq? map__22805)
                         (clojure.lang.PersistentHashMap/create (seq map__22805))
                         map__22805)
            storage_config map__22805
            table_name (get map__22805 :table-name)]
        (try
          (do
            (when-not (every?
                        storage_config
                        [:bucket :bucket-prefix :efs-path :region :table-name])
              (throw
                (java.lang.AssertionError.
                  (str
                    "Assert failed: "
                    (pr-str
                      (clojure.core/list
                        'every?
                        'storage-config
                        [:bucket :bucket-prefix :efs-path :region :table-name]))))))
            (let [result (ensure-config storage_config)]
              (if (:success result)
                result
                {:failed
                 (cli/fail
                   (str
                     table_name
                     " is already configured for ddb+s3 storage. Use '--force true' to overwrite"))})))
          (catch
            java.lang.Throwable
            t
            (do
              (.printStackTrace ^java.lang.Throwable t)
              {:failed (cli/fail (.getMessage ^java.lang.Throwable t))})))))))