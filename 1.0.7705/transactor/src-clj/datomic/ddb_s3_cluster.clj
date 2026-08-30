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
        ['datomic.cli :as 'cli]
        ['datomic.cluster :as 'cluster]
        ['datomic.combined-cluster :as 'cc]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.s3 :as 's3]
        ['datomic.core2.val-store.double-store :as 'dstore]
        ['datomic.core2.val-store.fs :as 'fs]
        ['datomic.core2.val-store.s3 :as 's3-vs]
        ['datomic.core2.val-store.s3.sdkv2 :as 's3-sdkv2]
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
          ['datomic.cli :as 'cli]
          ['datomic.cluster :as 'cluster]
          ['datomic.combined-cluster :as 'cc]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.s3 :as 's3]
          ['datomic.core2.val-store.double-store :as 'dstore]
          ['datomic.core2.val-store.fs :as 'fs]
          ['datomic.core2.val-store.s3 :as 's3-vs]
          ['datomic.core2.val-store.s3.sdkv2 :as 's3-sdkv2]
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
    (assoc {:const true, :column (int 1)} :name 'storage-config-key :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb-s3-cluster" "efs-delete-pool")
    {:private true, :column (int 1)})
  (let [v__6837__auto__ #'efs-delete-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "efs-delete-pool")
        {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "efs-delete-pool")
        (delay
          (common/thread-pool
            {:nthreads (config/property "datomic.efsDeletePool"), :name "efs-delete"})))
      #'efs-delete-pool))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb-s3-cluster" "efs-read-pool")
    {:private true, :column (int 1)})
  (let [v__6837__auto__ #'efs-read-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "efs-read-pool")
        {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "efs-read-pool")
        (delay (common/cached-thread-pool {:name "efs-read"})))
      #'efs-read-pool))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb-s3-cluster" "efs-write-pool")
    {:private true, :column (int 1)})
  (let [v__6837__auto__ #'efs-write-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "efs-write-pool")
        {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "efs-write-pool")
        (delay
          (common/handoff-thread-pool
            {:core-threads 2,
             :max-threads (config/property "datomic.efsWritePool"),
             :name "efs-write"})))
      #'efs-write-pool))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb-s3-cluster" "s3-read-pool")
    {:private true, :column (int 1)})
  (let [v__6837__auto__ #'s3-read-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "s3-read-pool")
        {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "s3-read-pool")
        (delay (common/cached-thread-pool {:name "s3-read"})))
      #'s3-read-pool))
  (.setMeta
    (clojure.lang.RT/var "datomic.ddb-s3-cluster" "s3-write-pool")
    {:private true, :column (int 1)})
  (let [v__6837__auto__ #'s3-write-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "s3-write-pool")
        {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.ddb-s3-cluster" "s3-write-pool")
        (delay (common/cached-thread-pool {:name "s3-write"})))
      #'s3-write-pool))
  (defn storage-path
    ([p__30582]
      (let [map__30583 p__30582
            map__30583 (if (seq? map__30583)
                         (if (next map__30583)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30583))
                           (if (seq map__30583) (first map__30583) {}))
                         map__30583)
            system (get map__30583 :system)
            db_id (get map__30583 :db-id)]
        (str (format "/%s" system) "/data" (when db_id (format "/%s" db_id)) "/vals"))))
  (reset-meta!
    #'storage-path
    (assoc
      {:private true, :arglists (clojure.core/list [{:keys ['system 'db-id]}]), :column (int 1)}
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
      {:private true, :arglists (clojure.core/list ['kv-cluster]), :column (int 1)}
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
      {:private true, :arglists (clojure.core/list ['kv-store 'cluster-conf]), :column (int 1)}
      :name
      'get-valid-config!
      :ns
      *ns*))
  (defn ensure-config
    ([p__30589]
      (let [map__30590 p__30589
            map__30590 (if (seq? map__30590)
                         (if (next map__30590)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30590))
                           (if (seq map__30590) (first map__30590) {}))
                         map__30590)
            storage_config map__30590
            bucket (get map__30590 :bucket)
            bucket_prefix (get map__30590 :bucket-prefix)
            force (get map__30590 :force false)
            efs_path (get map__30590 :efs-path)
            region (get map__30590 :region)
            system (get map__30590 :system "_default")
            table_name (get map__30590 :table-name)
            ddb_client (ddb/client
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
       :column (int 1)}
      :name
      'ensure-config
      :ns
      *ns*))
  (defn create-s3-store
    ([p__30596]
      (let [map__30597 p__30596
            map__30597 (if (seq? map__30597)
                         (if (next map__30597)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30597))
                           (if (seq map__30597) (first map__30597) {}))
                         map__30597)
            cluster_conf map__30597
            aws_region (get map__30597 :aws-region)
            s3_vals_bucket (get map__30597 :s3-vals-bucket)
            s3_vals_prefix (get map__30597 :s3-vals-prefix)
            s3_client (s3/s3-service (config/s3-client-args {:region aws_region, :maxAttempts 1}))
            max_retries (config/property "datomic.s3MaxRetries")
            retry_opts {:base 2,
                        :backoff (config/property "datomic.s3RetryBaseDelay"),
                        :retriable?
                        (fn fn__30599
                          ([p__30598]
                            (let [map__30600 p__30598
                                  map__30600 (if (seq? map__30600)
                                               (if (next map__30600)
                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                   (to-array map__30600))
                                                 (if (seq map__30600) (first map__30600) {}))
                                               map__30600)
                                  i (get map__30600 :i)
                                  result (get map__30600 :result)]
                              (and
                                (<= i max_retries)
                                (canom/anom result)
                                (not (canom/not-found? result))))))}
            retry_fn (fn retry_fn ([f op] (s3-vs/retry-handler f op retry_opts)))]
        (s3-sdkv2/create
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
       :column (int 1)}
      :name
      'create-s3-store
      :ns
      *ns*))
  (defn create-connection-with-efs
    ([p__30607]
      (let [map__30608 p__30607
            map__30608 (if (seq? map__30608)
                         (if (next map__30608)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30608))
                           (if (seq map__30608) (first map__30608) {}))
                         map__30608)
            cluster_conf map__30608
            near_store_get_timeout_msec (get map__30608 :near-store-get-timeout-msec 20)
            aws_region (get map__30608 :aws-region)
            aws_dynamodb_table (get map__30608 :aws-dynamodb-table)
            system (get map__30608 :system)
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
            map__30609 (get-valid-config! kvs cluster_conf)
            map__30609 (if (seq? map__30609)
                         (if (next map__30609)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30609))
                           (if (seq map__30609) (first map__30609) {}))
                         map__30609)
            store_config map__30609
            fs_vals_path (get map__30609 :fs-vals-path)
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
       :column (int 1)}
      :name
      'create-connection-with-efs
      :ns
      *ns*))
  (defn create-connection-without-efs
    ([p__30611]
      (let [map__30612 p__30611
            map__30612 (if (seq? map__30612)
                         (if (next map__30612)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30612))
                           (if (seq map__30612) (first map__30612) {}))
                         map__30612)
            cluster_conf map__30612
            aws_region (get map__30612 :aws-region)
            aws_dynamodb_table (get map__30612 :aws-dynamodb-table)
            system (get map__30612 :system)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.ddb-s3-cluster")]
          (when (.isWarnEnabled ^org.slf4j.Logger logger)
            (.warn ^org.slf4j.Logger logger (logger/process "Running without EFS!")))
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
              ddb_client (ddb/client (config/ddb-client-args {:region aws_region}))
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
       :column (int 1)}
      :name
      'create-connection-without-efs
      :ns
      *ns*))
  (defn create-connection
    ([p__30614]
      (let [map__30615 p__30614
            map__30615 (if (seq? map__30615)
                         (if (next map__30615)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30615))
                           (if (seq map__30615) (first map__30615) {}))
                         map__30615)
            cluster_conf map__30615
            skip_efs (get map__30615 :skip-efs)]
        (if (boolean skip_efs)
          (create-connection-without-efs cluster_conf)
          (create-connection-with-efs cluster_conf)))))
  (reset-meta!
    #'create-connection
    (assoc
      {:arglists (clojure.core/list [{:keys ['skip-efs], :as 'cluster-conf}]), :column (int 1)}
      :name
      'create-connection
      :ns
      *ns*))
  (defn ensure-system
    ([p__30617]
      (let [map__30618 p__30617
            map__30618 (if (seq? map__30618)
                         (if (next map__30618)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30618))
                           (if (seq map__30618) (first map__30618) {}))
                         map__30618)
            storage_config map__30618
            table_name (get map__30618 :table-name)]
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
              {:failed (cli/fail (.getMessage ^java.lang.Throwable t))}))))))
  (reset-meta!
    #'ensure-system
    (assoc
      {:arglists (clojure.core/list [{:keys ['table-name], :as 'storage-config}]), :column (int 1)}
      :name
      'ensure-system
      :ns
      *ns*)))