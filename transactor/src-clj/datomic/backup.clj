(do
  (clojure.core/in-ns 'datomic.backup)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.set :as 'set]
        'datomic.db
        ['datomic.cache :as 'cache]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['clojure.string :as 'str]
        ['datomic.api :as 'd]
        ['datomic.io :as 'io]
        ['datomic.slf4j :as 'logger]
        ['datomic.iter :as 'iter]
        ['datomic.cluster :as 'cluster]
        ['datomic.garbage :as 'garbage]
        ['datomic.index :as 'index]
        ['datomic.io :as 'io]
        ['datomic.log :as 'log]
        ['datomic.fressian :as 'fressian]
        ['datomic.error :as 'error]
        ['datomic.require :as 'req]
        ['datomic.domain :as 'domain]
        ['datomic.uri :as 'uri]
        ['datomic.coordination :as 'coord]
        ['datomic.catalog :as 'catalog]
        ['datomic.promise :as 'promise]
        ['datomic.monitor :as 'monitor]
        ['datomic.queue :as 'queue]
        ['datomic.treewalk :as 'treewalk])
      (clojure.core/import 'java.util.concurrent.Future)
      (clojure.core/import 'java.util.concurrent.Semaphore)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.net.URI)))
  (when-not (.equals 'datomic.backup 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.backup))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.set :as 'set]
          'datomic.db
          ['datomic.cache :as 'cache]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['clojure.string :as 'str]
          ['datomic.api :as 'd]
          ['datomic.io :as 'io]
          ['datomic.slf4j :as 'logger]
          ['datomic.iter :as 'iter]
          ['datomic.cluster :as 'cluster]
          ['datomic.garbage :as 'garbage]
          ['datomic.index :as 'index]
          ['datomic.io :as 'io]
          ['datomic.log :as 'log]
          ['datomic.fressian :as 'fressian]
          ['datomic.error :as 'error]
          ['datomic.require :as 'req]
          ['datomic.domain :as 'domain]
          ['datomic.uri :as 'uri]
          ['datomic.coordination :as 'coord]
          ['datomic.catalog :as 'catalog]
          ['datomic.promise :as 'promise]
          ['datomic.monitor :as 'monitor]
          ['datomic.queue :as 'queue]
          ['datomic.treewalk :as 'treewalk])
        (clojure.core/import 'java.util.concurrent.Future)
        (clojure.core/import 'java.util.concurrent.Semaphore)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.net.URI))))
  (set! *warn-on-reflection* true)
  (.setMeta (clojure.lang.RT/var "datomic.backup" "pool-size") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.backup" "pool-size")
    (max
      (config/property "datomic.fileBackupConcurrency")
      (config/property "datomic.s3BackupConcurrency")))
  (.setMeta (clojure.lang.RT/var "datomic.backup" "backup-branch-size") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.backup" "backup-branch-size")
    (config/property "datomic.backupBranchConcurrency"))
  (.setMeta (clojure.lang.RT/var "datomic.backup" "thread-pool") {:column (int 1)})
  (let [v__6812__auto__ #'thread-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.backup" "thread-pool") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.backup" "thread-pool")
        (delay (common/thread-pool {:nthreads pool-size, :name "backup"})))
      #'thread-pool))
  (.setMeta (clojure.lang.RT/var "datomic.backup" "backup-branch-pool") {:column (int 1)})
  (let [v__6812__auto__ #'backup-branch-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.backup" "backup-branch-pool") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.backup" "backup-branch-pool")
        (delay (common/thread-pool {:nthreads backup-branch-size, :name "backup-branch"})))
      #'backup-branch-pool))
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol
      Storage
      (store
        [_ k buf]
        "Place a byte buffer into storage under pathkey.\n    Returns {:k k} on success.")
      (list-keys [_ prefix] "Returns {:ks keys} on success.")
      (exists? [_ k] "Returns a boolean")
      (retrieve [_ k] "Returns {:v buf} if path exists."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.backup" "Storage")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'Storage :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'store
                                        {:arglists (clojure.core/list ['_ 'k 'buf])}),
                                      :arglists (clojure.core/list ['_ 'k 'buf]),
                                      :doc
                                      "Place a byte buffer into storage under pathkey.\n    Returns {:k k} on success."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "Storage"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "store")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*)))
    (let [protocol_signature__7423 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'list-keys
                                        {:arglists (clojure.core/list ['_ 'prefix])}),
                                      :arglists (clojure.core/list ['_ 'prefix]),
                                      :doc "Returns {:ks keys} on success."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "Storage"))
          protocol_method_name__7424 (with-meta
                                       (:name protocol_signature__7423)
                                       protocol_signature__7423)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "list-keys")
        (assoc protocol_signature__7423 :name protocol_method_name__7424 :ns *ns*)))
    (let [protocol_signature__7425 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'exists? {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc "Returns a boolean"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "Storage"))
          protocol_method_name__7426 (with-meta
                                       (:name protocol_signature__7425)
                                       protocol_signature__7425)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "exists?")
        (assoc protocol_signature__7425 :name protocol_method_name__7426 :ns *ns*)))
    (let [protocol_signature__7427 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'retrieve
                                        {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc "Returns {:v buf} if path exists."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "Storage"))
          protocol_method_name__7428 (with-meta
                                       (:name protocol_signature__7427)
                                       protocol_signature__7427)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "retrieve")
        (assoc protocol_signature__7427 :name protocol_method_name__7428 :ns *ns*))))
  (defn subkey ([prefix k] (str prefix "/" k)))
  (reset-meta!
    #'subkey
    (assoc
      {:private true, :arglists (clojure.core/list ['prefix 'k]), :column (int 1)}
      :name
      'subkey
      :ns
      *ns*))
  (defn strip-prefix ([prefix k] (subs k (long (inc (count prefix))))))
  (reset-meta!
    #'strip-prefix
    (assoc
      {:private true, :arglists (clojure.core/list ['prefix 'k]), :column (int 1)}
      :name
      'strip-prefix
      :ns
      *ns*))
  (defn add-key-prefix ([s] (str (subs s (long (- (count s) 2))) "/" s)))
  (reset-meta!
    #'add-key-prefix
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'add-key-prefix
      :ns
      *ns*))
  (def backup-k-factory
   (fn backup_k_factory
     ([^long backup_version]
       (let [G__31253 backup_version] (case G__31253 (1 2) identity 3 add-key-prefix)))))
  (reset-meta!
    #'backup-k-factory
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'backup-version {:tag 'long})]), :column (int 1)}
      :name
      'backup-k-factory
      :ns
      *ns*))
  (deftype
    Substorage
    [storage prefix]
    datomic.backup.Storage
    (retrieve [this k] (retrieve storage (subkey prefix k)))
    (exists? [this k] (exists? storage (subkey prefix k)))
    (list-keys
      [this pre]
      (let [result (list-keys storage (subkey prefix pre))]
        (if (:ks result)
          (update-in
            result
            [:ks]
            (fn fn__31257 ([p1__31255#] (map (partial strip-prefix prefix) p1__31255#))))
          result)))
    (store [this k buf] (store storage (subkey prefix k) buf)))
  (clojure.core/import 'datomic.backup.Substorage)
  (defn ->Substorage ([storage prefix] (datomic.backup.Substorage. storage prefix)))
  (reset-meta!
    #'->Substorage
    (assoc
      {:arglists (clojure.core/list ['storage 'prefix]), :column (int 1)}
      :name
      '->Substorage
      :ns
      *ns*))
  (defn substorage ([storage prefix] (->Substorage storage prefix)))
  (reset-meta!
    #'substorage
    (assoc
      {:arglists (clojure.core/list ['storage 'prefix]), :column (int 1)}
      :name
      'substorage
      :ns
      *ns*))
  (defn claim ([storage id] (store storage "owner" (io/string->bbuf id))))
  (reset-meta!
    #'claim
    (assoc {:arglists (clojure.core/list ['storage 'id]), :column (int 1)} :name 'claim :ns *ns*))
  (defn claimed-by
    ([storage]
      (let [temp__5804__auto__ (retrieve storage "owner")]
        (when temp__5804__auto__
          (let [map__31265 temp__5804__auto__
                map__31265 (if (seq? map__31265)
                             (if (next map__31265)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__31265))
                               (if (seq map__31265) (first map__31265) {}))
                             map__31265)
                v (get map__31265 :v)]
            (io/bbuf->string v))))))
  (reset-meta!
    #'claimed-by
    (assoc {:arglists (clojure.core/list ['storage]), :column (int 1)} :name 'claimed-by :ns *ns*))
  (defn ensure-claim
    ([storage id]
      (let [temp__5802__auto__ (claimed-by storage)]
        (if temp__5802__auto__
          (let [claimant temp__5802__auto__]
            (when-not (= claimant id)
              (error/arg :backup/claim-failed (str "Backup storage already used by " claimant)))
            :ok)
          (let [k (:k (claim storage id))]
            (when-not k (error/raise :backup/claim-failed "Unable to write to backup storage"))
            (recur storage id))))))
  (reset-meta!
    #'ensure-claim
    (assoc
      {:arglists (clojure.core/list ['storage 'id]), :column (int 1)}
      :name
      'ensure-claim
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.backup" "create-storage*") {:column (int 1)})
  (let [v__5792__auto__ #'create-storage*]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.backup" "create-storage*") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.backup" "create-storage*")
        (clojure.lang.MultiFn.
          "create-storage*"
          (fn fn__31271 ([uri & _] (.getScheme ^java.net.URI uri)))
          :default
          #'clojure.core/global-hierarchy))
      #'create-storage*))
  (defmethod
    create-storage*
    "file"
    fn__31276
    ([uri sse?]
      (when sse?
        (error/arg
          :storage/sse-not-available
          "Server side encryption not available for file storage"))
      (req/require-and-run 'datomic.fsbackup/storage-from-uri uri)))
  (defmethod
    create-storage*
    "s3"
    fn__31278
    ([uri sse?] (req/require-and-run 'datomic.s3backup/storage-from-uri uri sse?)))
  (defmethod
    create-storage*
    :default
    fn__31280
    ([uri sse?]
      (error/arg
        :storage/invalid-uri
        (str "Unsupported protocol: " (.getScheme ^java.net.URI uri)))))
  (def create-storage
   (fn create_storage
     ([storage_uri sse?] (create-storage* (io/as-uri storage_uri) sse?))
     ([storage_uri] (create-storage storage_uri false))))
  (reset-meta!
    #'create-storage
    (assoc
      {:arglists (clojure.core/list ['storage-uri] ['storage-uri 'sse?]), :column (int 1)}
      :name
      'create-storage
      :ns
      *ns*))
  (defn retry
    ([f]
      (common/retry-fn
        f
        :pred
        (fn fn__31285 ([p1__31283#] (instance? java.lang.Throwable p1__31283#)))
        :backoff
        (fn fn__31287
          ([p1__31284#]
            (java.lang.Double/valueOf
              (double
                (*
                  (+ 50 (rand-int 50))
                  (java.lang.Math/pow (double 2) (double ^java.lang.Number p1__31284#)))))))
        :log-retry
        common/log-retry
        :max-retries
        8)))
  (reset-meta!
    #'retry
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name 'retry :ns *ns*))
  (defn uncached-storage-lookup
    ([storage]
      (reify
        clojure.lang.ILookup
        (valAt
          [this k not_found]
          (let [ret (retry (fn fn__31291 ([] (retrieve storage k))))] (if ret (:v ret) not_found)))
        (valAt [this k] (.valAt this k nil)))))
  (reset-meta!
    #'uncached-storage-lookup
    (assoc
      {:arglists (clojure.core/list ['storage]), :column (int 1)}
      :name
      'uncached-storage-lookup
      :ns
      *ns*))
  (def storage-olookup
   (fn storage_olookup
     ([storage backup_version]
       (cache/lookup-transformer
         (domain/peer-object-lookup
           (uncached-storage-lookup storage)
           domain/common-read-handlers
           (domain/system-cache))
         :key-fn
         (comp
           (backup-k-factory (long ^java.lang.Number backup_version))
           cluster/uuid->val-key)))))
  (reset-meta!
    #'storage-olookup
    (assoc
      {:arglists (clojure.core/list ['storage 'backup-version]), :column (int 1)}
      :name
      'storage-olookup
      :ns
      *ns*))
  (let [protocol_metadata__7429 {:column (int 1)}]
    (defprotocol
      IValueBackup
      (backup-val [_ k backup-k] "Backup a segment from storage k to backup backup-k.")
      (backup-node [_ node] "Backup a node's children, then the node."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.backup" "IValueBackup")
      (assoc (assoc protocol_metadata__7429 :doc nil) :name 'IValueBackup :ns *ns*))
    (let [protocol_signature__7430 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'backup-val
                                        {:arglists (clojure.core/list ['_ 'k 'backup-k])}),
                                      :arglists (clojure.core/list ['_ 'k 'backup-k]),
                                      :doc "Backup a segment from storage k to backup backup-k."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "IValueBackup"))
          protocol_method_name__7431 (with-meta
                                       (:name protocol_signature__7430)
                                       protocol_signature__7430)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "backup-val")
        (assoc protocol_signature__7430 :name protocol_method_name__7431 :ns *ns*)))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'backup-node
                                        {:arglists (clojure.core/list ['_ 'node])}),
                                      :arglists (clojure.core/list ['_ 'node]),
                                      :doc "Backup a node's children, then the node."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "IValueBackup"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "backup-node")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (deftype
    ValueBackup
    [from_cluster value_storage progress incremental? ids_>nodes throttle sem]
    datomic.backup.IValueBackup
    (backup-node
      [this node]
      (let [k (treewalk/node-id node)
            backup_k (add-key-prefix k)
            node_exists? (retry (fn fn__31341 ([] (exists? value_storage backup_k))))]
        (when-not (and node_exists? incremental?)
          (let [temp__5802__auto__ (treewalk/subtrees node ids_>nodes)]
            (if temp__5802__auto__
              (let [branch_nodes temp__5802__auto__
                    futs (mapv
                           (fn fn__31343
                             ([branch]
                               (common/pfuture
                                 (deref backup-branch-pool)
                                 (fn fn__31344 ([] (backup-node this branch))))))
                           branch_nodes)]
                (loop [seq_31347 (seq futs) chunk_31348 nil count_31349 0 i_31350 0]
                  (if (< i_31350 count_31349)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_31348 (int i_31350))]
                      (deref fut)
                      (recur seq_31347 chunk_31348 count_31349 (inc i_31350)))
                    (let [temp__5804__auto__ (seq seq_31347)]
                      (when temp__5804__auto__
                        (let [seq_31347 temp__5804__auto__]
                          (if (chunked-seq? seq_31347)
                            (let [c__6065__auto__ (chunk-first seq_31347)]
                              (recur
                                (chunk-rest seq_31347)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [fut (first seq_31347)]
                              (deref fut)
                              (recur (next seq_31347) nil 0 0)))))))))
              (let [futs (mapv
                           (fn fn__31351
                             ([leaf_id]
                               (when throttle (^clojure.lang.IFn throttle))
                               (common/pfuture
                                 (deref thread-pool)
                                 (fn fn__31352
                                   ([]
                                     (let [backup_leaf_id (add-key-prefix leaf_id)]
                                       (if (retry
                                             (fn fn__31353
                                               ([] (exists? value_storage backup_leaf_id))))
                                         (^clojure.lang.IFn progress :skipped)
                                         (backup-val this leaf_id backup_leaf_id))))))))
                           (treewalk/child-node-ids node))]
                (loop [seq_31357 (seq futs) chunk_31358 nil count_31359 0 i_31360 0]
                  (if (< i_31360 count_31359)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_31358 (int i_31360))]
                      (deref fut)
                      (recur seq_31357 chunk_31358 count_31359 (inc i_31360)))
                    (let [temp__5804__auto__ (seq seq_31357)]
                      (when temp__5804__auto__
                        (let [seq_31357 temp__5804__auto__]
                          (if (chunked-seq? seq_31357)
                            (let [c__6065__auto__ (chunk-first seq_31357)]
                              (recur
                                (chunk-rest seq_31357)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [fut (first seq_31357)]
                              (deref fut)
                              (recur (next seq_31357) nil 0 0))))))))))))
        (if node_exists? (^clojure.lang.IFn progress :skipped) (backup-val this k backup_k))))
    (backup-val
      [this k backup_k]
      (do
        (.acquire ^java.util.concurrent.Semaphore sem)
        (try
          (let [m_31332 {:event :backup/segment, :k k}
                ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process (assoc m_31332 :phase :begin))))
                                  nil)
                start__8584__auto__ (java.lang.System/nanoTime)
                result__8585__auto__ (try
                                       {:returned
                                        (let [temp__5802__auto__ (deref
                                                                   (cluster/get-val
                                                                     from_cluster
                                                                     k))]
                                          (if temp__5802__auto__
                                            (let [map__31336 temp__5802__auto__
                                                  map__31336 (if
                                                               (seq? map__31336)
                                                               (if
                                                                 (next map__31336)
                                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                   (to-array map__31336))
                                                                 (if
                                                                   (seq map__31336)
                                                                   (first map__31336)
                                                                   {}))
                                                               map__31336)
                                                  buf (get map__31336 :buf)
                                                  result (retry
                                                           (fn 
                                                             fn__31337
                                                             ([]
                                                               (store
                                                                 value_storage
                                                                 backup_k
                                                                 buf))))]
                                              (if (:k result)
                                                (^clojure.lang.IFn progress :copied)
                                                (error/raise
                                                  :backup/value-failed
                                                  (str "Unable to backup " k)
                                                  result)))
                                            (error/raise
                                              :backup/read-failed
                                              (str "Unable to read " k)
                                              {:key k})))}
                                       (catch
                                         java.lang.Throwable
                                         t__8586__auto__
                                         {:threw t__8586__auto__}))
                elapsed_31333 (- (java.lang.System/nanoTime) start__8584__auto__)
                msec_31334 (logger/format-as-msec (long elapsed_31333))]
            (let [endmsg__8587__auto__ (merge
                                         (assoc m_31332 :msec msec_31334 :phase :end)
                                         (when (:threw result__8585__auto__)
                                           {:threw (class (:threw result__8585__auto__))}))
                  logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
              nil)
            (if (contains? result__8585__auto__ :returned)
              (:returned result__8585__auto__)
              (do (throw (:threw result__8585__auto__)) nil)))
          (finally (.release ^java.util.concurrent.Semaphore sem))))))
  (clojure.core/import 'datomic.backup.ValueBackup)
  (def ->ValueBackup
   (fn __GT_ValueBackup
     ([from_cluster value_storage progress incremental? ids_>nodes throttle sem]
       (datomic.backup.ValueBackup.
         from_cluster
         value_storage
         progress
         incremental?
         ids_>nodes
         throttle
         sem))))
  (reset-meta!
    #'->ValueBackup
    (assoc
      {:arglists
       (clojure.core/list
         ['from-cluster 'value-storage 'progress 'incremental? 'ids->nodes 'throttle 'sem]),
       :column (int 1)}
      :name
      '->ValueBackup
      :ns
      *ns*))
  (def create-value-backup
   (fn create_value_backup
     ([& p__31375]
       (let [map__31376 p__31375
             map__31376 (if (seq? map__31376)
                          (if (next map__31376)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31376))
                            (if (seq map__31376) (first map__31376) {}))
                          map__31376)
             from_cluster (get map__31376 :from-cluster)
             to_storage (get map__31376 :to-storage)
             progress (get map__31376 :progress)
             incremental? (get map__31376 :incremental?)
             ids_>nodes (get map__31376 :ids->nodes)
             concurrency (get map__31376 :concurrency)]
         (when-not from_cluster
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'from-cluster)))))
         (when-not to_storage
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'to-storage)))))
         (when-not progress
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'progress)))))
         (when-not ids_>nodes
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'ids->nodes)))))
         (when-not concurrency
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'concurrency)))))
         (->ValueBackup
           from_cluster
           (substorage to_storage "values")
           progress
           incremental?
           ids_>nodes
           (let [temp__5804__auto__ (config/property "datomic.backupPaceMsec")]
             (when temp__5804__auto__
               (let [pace temp__5804__auto__]
                 (fn fn__31377 ([] (java.lang.Thread/sleep (long ^java.lang.Number pace)) nil)))))
           (java.util.concurrent.Semaphore. (int ^java.lang.Number concurrency)))))))
  (reset-meta!
    #'create-value-backup
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['&
            {:keys ['from-cluster 'to-storage 'progress 'incremental? 'ids->nodes 'concurrency]}]
           {:pre ['from-cluster 'to-storage 'progress 'ids->nodes 'concurrency]})),
       :column (int 1)}
      :name
      'create-value-backup
      :ns
      *ns*))
  (let [protocol_metadata__7434 {:column (int 1)}]
    (defprotocol
      IValueRestore
      (restore-val [_ k] "Restore segment with key k from backup to storage.")
      (restore-node [_ node] "Restores a node's children, then the node"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.backup" "IValueRestore")
      (assoc (assoc protocol_metadata__7434 :doc nil) :name 'IValueRestore :ns *ns*))
    (let [protocol_signature__7435 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'restore-val
                                        {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc "Restore segment with key k from backup to storage."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "IValueRestore"))
          protocol_method_name__7436 (with-meta
                                       (:name protocol_signature__7435)
                                       protocol_signature__7435)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "restore-val")
        (assoc protocol_signature__7435 :name protocol_method_name__7436 :ns *ns*)))
    (let [protocol_signature__7437 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'restore-node
                                        {:arglists (clojure.core/list ['_ 'node])}),
                                      :arglists (clojure.core/list ['_ 'node]),
                                      :doc "Restores a node's children, then the node"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "IValueRestore"))
          protocol_method_name__7438 (with-meta
                                       (:name protocol_signature__7437)
                                       protocol_signature__7437)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "restore-node")
        (assoc protocol_signature__7437 :name protocol_method_name__7438 :ns *ns*))))
  (deftype
    ValueRestore
    [value_storage to_cluster progress incremental? k_>backup_k ids_>nodes sem]
    datomic.backup.IValueRestore
    (restore-node
      [this node]
      (let [k (treewalk/node-id node)
            backup_k (^clojure.lang.IFn k_>backup_k k)
            cluster_v (deref
                        (cluster/get-val2
                          to_cluster
                          k
                          #:datomic.core2.val-store.opts{:skip-cache true}))]
        (when-not (and cluster_v incremental?)
          (let [temp__5802__auto__ (treewalk/subtrees node ids_>nodes)]
            (if temp__5802__auto__
              (let [branch_nodes temp__5802__auto__
                    futs (mapv
                           (fn fn__31424
                             ([branch]
                               (common/pfuture
                                 (deref backup-branch-pool)
                                 (fn fn__31425 ([] (restore-node this branch))))))
                           branch_nodes)]
                (loop [seq_31428 (seq futs) chunk_31429 nil count_31430 0 i_31431 0]
                  (if (< i_31431 count_31430)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_31429 (int i_31431))]
                      (deref fut)
                      (recur seq_31428 chunk_31429 count_31430 (inc i_31431)))
                    (let [temp__5804__auto__ (seq seq_31428)]
                      (when temp__5804__auto__
                        (let [seq_31428 temp__5804__auto__]
                          (if (chunked-seq? seq_31428)
                            (let [c__6065__auto__ (chunk-first seq_31428)]
                              (recur
                                (chunk-rest seq_31428)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [fut (first seq_31428)]
                              (deref fut)
                              (recur (next seq_31428) nil 0 0)))))))))
              (let [futs (mapv
                           (fn fn__31432
                             ([leaf_id]
                               (common/pfuture
                                 (deref thread-pool)
                                 (fn fn__31433
                                   ([]
                                     (if (deref
                                           (cluster/get-val2
                                             to_cluster
                                             leaf_id
                                             #:datomic.core2.val-store.opts{:skip-cache true}))
                                       (^clojure.lang.IFn progress :skipped)
                                       (restore-val this leaf_id)))))))
                           (treewalk/child-node-ids node))]
                (loop [seq_31436 (seq futs) chunk_31437 nil count_31438 0 i_31439 0]
                  (if (< i_31439 count_31438)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_31437 (int i_31439))]
                      (deref fut)
                      (recur seq_31436 chunk_31437 count_31438 (inc i_31439)))
                    (let [temp__5804__auto__ (seq seq_31436)]
                      (when temp__5804__auto__
                        (let [seq_31436 temp__5804__auto__]
                          (if (chunked-seq? seq_31436)
                            (let [c__6065__auto__ (chunk-first seq_31436)]
                              (recur
                                (chunk-rest seq_31436)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [fut (first seq_31436)]
                              (deref fut)
                              (recur (next seq_31436) nil 0 0))))))))))))
        (if cluster_v (^clojure.lang.IFn progress :skipped) (restore-val this k))))
    (restore-val
      [this k]
      (do
        (.acquire ^java.util.concurrent.Semaphore sem)
        (try
          (let [m_31415 {:event :restore/segment, :k k}
                ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process (assoc m_31415 :phase :begin))))
                                  nil)
                start__8584__auto__ (java.lang.System/nanoTime)
                result__8585__auto__ (try
                                       {:returned
                                        (let [temp__5802__auto__ (retry
                                                                   (fn 
                                                                     fn__31419
                                                                     ([]
                                                                       (retrieve
                                                                         value_storage
                                                                         (^clojure.lang.IFn k_>backup_k
                                                                           k)))))]
                                          (if temp__5802__auto__
                                            (let [map__31421 temp__5802__auto__
                                                  map__31421 (if
                                                               (seq? map__31421)
                                                               (if
                                                                 (next map__31421)
                                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                   (to-array map__31421))
                                                                 (if
                                                                   (seq map__31421)
                                                                   (first map__31421)
                                                                   {}))
                                                               map__31421)
                                                  v (get map__31421 :v)]
                                              (deref (cluster/create-val to_cluster k v))
                                              (^clojure.lang.IFn progress :copied))
                                            (error/raise
                                              :restore/read-failed
                                              (str "Unable to read " k)
                                              {:key k})))}
                                       (catch
                                         java.lang.Throwable
                                         t__8586__auto__
                                         {:threw t__8586__auto__}))
                elapsed_31416 (- (java.lang.System/nanoTime) start__8584__auto__)
                msec_31417 (logger/format-as-msec (long elapsed_31416))]
            (let [endmsg__8587__auto__ (merge
                                         (assoc m_31415 :msec msec_31417 :phase :end)
                                         (when (:threw result__8585__auto__)
                                           {:threw (class (:threw result__8585__auto__))}))
                  logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
              nil)
            (if (contains? result__8585__auto__ :returned)
              (:returned result__8585__auto__)
              (do (throw (:threw result__8585__auto__)) nil)))
          (finally (.release ^java.util.concurrent.Semaphore sem))))))
  (clojure.core/import 'datomic.backup.ValueRestore)
  (def ->ValueRestore
   (fn __GT_ValueRestore
     ([value_storage to_cluster progress incremental? k_>backup_k ids_>nodes sem]
       (datomic.backup.ValueRestore.
         value_storage
         to_cluster
         progress
         incremental?
         k_>backup_k
         ids_>nodes
         sem))))
  (reset-meta!
    #'->ValueRestore
    (assoc
      {:arglists
       (clojure.core/list
         ['value-storage 'to-cluster 'progress 'incremental? 'k->backup-k 'ids->nodes 'sem]),
       :column (int 1)}
      :name
      '->ValueRestore
      :ns
      *ns*))
  (def create-value-restore
   (fn create_value_restore
     ([& p__31454]
       (let [map__31455 p__31454
             map__31455 (if (seq? map__31455)
                          (if (next map__31455)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31455))
                            (if (seq map__31455) (first map__31455) {}))
                          map__31455)
             from_storage (get map__31455 :from-storage)
             to_cluster (get map__31455 :to-cluster)
             backup_version (get map__31455 :backup-version)
             progress (get map__31455 :progress)
             incremental? (get map__31455 :incremental?)
             concurrency (get map__31455 :concurrency)
             ids_>nodes (get map__31455 :ids->nodes)]
         (when-not from_storage
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'from-storage)))))
         (when-not to_cluster
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'to-cluster)))))
         (when-not progress
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'progress)))))
         (when-not concurrency
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'concurrency)))))
         (when-not ids_>nodes
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'ids->nodes)))))
         (->ValueRestore
           (substorage from_storage "values")
           to_cluster
           progress
           incremental?
           (backup-k-factory (long ^java.lang.Number backup_version))
           ids_>nodes
           (java.util.concurrent.Semaphore. (int ^java.lang.Number concurrency)))))))
  (reset-meta!
    #'create-value-restore
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['&
            {:keys
             ['from-storage
              'to-cluster
              'backup-version
              'progress
              'incremental?
              'concurrency
              'ids->nodes]}]
           {:pre ['from-storage 'to-cluster 'progress 'concurrency 'ids->nodes]})),
       :column (int 1)}
      :name
      'create-value-restore
      :ns
      *ns*))
  (defn roots-path ([t] (str/join "/" ["roots" t])))
  (reset-meta!
    #'roots-path
    (assoc {:arglists (clojure.core/list ['t]), :column (int 1)} :name 'roots-path :ns *ns*))
  (def v1->v2-keymap
   {:index-root-id :index/root-id, :log-root-id :log/root-id, :log-tail :log/tail, :db-id :db/id})
  (reset-meta! #'v1->v2-keymap (assoc {:column (int 1)} :name 'v1->v2-keymap :ns *ns*))
  (def BACKUP_VERSION 3)
  (reset-meta!
    #'BACKUP_VERSION
    (assoc {:const true, :column (int 1)} :name 'BACKUP_VERSION :ns *ns*))
  (def mem-keys
   [:index-root-id :log-root-id :log-tail :db-id :log/version :index/version :backup/version])
  (reset-meta! #'mem-keys (assoc {:private true, :column (int 1)} :name 'mem-keys :ns *ns*))
  (defn mem->backup ([roots] (set/rename-keys (common/require-keys roots mem-keys) v1->v2-keymap)))
  (reset-meta!
    #'mem->backup
    (assoc
      {:private true, :arglists (clojure.core/list ['roots]), :column (int 1)}
      :name
      'mem->backup
      :ns
      *ns*))
  (defn backup->mem
    ([roots]
      (let [roots (if (:backup/version roots) roots (assoc roots :backup/version 1))
            version (long (:backup/version roots))]
        (common/require-keys
          (let [G__31459 version]
            (case
              G__31459
              1
              (assoc roots :log/version :unknown :index/version :unknown)
              (2 3)
              (set/rename-keys roots (set/map-invert v1->v2-keymap))
              (error/state
                :db.error/backup-version
                (str "This version of Datomic cannot read backup version " (long version)))))
          mem-keys))))
  (reset-meta!
    #'backup->mem
    (assoc
      {:private true, :arglists (clojure.core/list ['roots]), :column (int 1)}
      :name
      'backup->mem
      :ns
      *ns*))
  (def backup-roots
   (fn backup_roots
     ([job t to_storage]
       (store
         to_storage
         (roots-path t)
         (fressian/byte-buf (mem->backup job) :handlers fressian/user-write-handlers)))))
  (reset-meta!
    #'backup-roots
    (assoc
      {:arglists (clojure.core/list ['job 't 'to-storage]), :column (int 1)}
      :name
      'backup-roots
      :ns
      *ns*))
  (def read-roots
   (fn read_roots
     ([t from_storage]
       (let [temp__5804__auto__ (:v (retrieve from_storage (roots-path t)))]
         (when temp__5804__auto__
           (let [buf temp__5804__auto__]
             (backup->mem (fressian/defressian buf :handlers fressian/user-read-handlers))))))))
  (reset-meta!
    #'read-roots
    (assoc
      {:arglists (clojure.core/list ['t 'from-storage]), :column (int 1)}
      :name
      'read-roots
      :ns
      *ns*))
  (defn describe-backups
    ([storage]
      (let [storage (if (string? storage) (create-storage storage) storage)]
        {:db-id (claimed-by storage),
         :ts
         (sort
           >
           (mapv
             (fn fn__31466
               ([p1__31465#] (long (java.lang.Long/parseLong ^java.lang.String p1__31465#))))
             (filter
               (fn fn__31468 ([p1__31464#] (re-matches #"\d+" p1__31464#)))
               (:ks (list-keys (substorage storage "roots") "")))))})))
  (reset-meta!
    #'describe-backups
    (assoc
      {:arglists (clojure.core/list ['storage]), :column (int 1)}
      :name
      'describe-backups
      :ns
      *ns*))
  (defn restore-roots
    ([job cluster]
      (let [map__31471 job
            map__31471 (if (seq? map__31471)
                         (if (next map__31471)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31471))
                           (if (seq map__31471) (first map__31471) {}))
                         map__31471)
            index_root_id (get map__31471 :index-root-id)
            log_root_id (get map__31471 :log-root-id)
            log_tail (get map__31471 :log-tail)]
        (when-not (=
                    :ok
                    (deref
                      (cluster/reset-ref
                        cluster
                        (index/index-ref-key-name cluster)
                        index_root_id)))
          (error/raise :backup/roots-failed "Unable to restore index root"))
        (when (=
                :conflict
                (deref
                  (cluster/reset-pod
                    cluster
                    (log/tail-pod-key cluster)
                    (ByteBuffer/wrap ^bytes log_tail)
                    #:d{:r log_root_id, :v (config/property "datomic.versionUnique"), :l 3})))
          (error/raise :backup/roots-failed "Unable to restore log tail pod"))
        :succeeded)))
  (reset-meta!
    #'restore-roots
    (assoc
      {:arglists (clojure.core/list ['job 'cluster]), :column (int 1)}
      :name
      'restore-roots
      :ns
      *ns*))
  (defn create-backup-job
    ([cluster lookup]
      (let [vec__31474 (log/read-tail-descriptor cluster)
            desc (nth vec__31474 (int 0) nil)
            buf (nth vec__31474 (int 1) nil)
            log_tail (io/alias-buf-bytes buf)
            tail_txes (fressian/defressian log_tail :handlers log/read-handlers)
            log_root_id (:d/r desc)
            t (max
                (log/max-t (:data (log/last-tree-tx lookup log_root_id)))
                (log/max-t (:data (last tail_txes))))
            index_root_id (:key
                            (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))
            index_root_map (common/getx lookup index_root_id)
            index_top_node (treewalk/create-parent-node
                             index_root_id
                             treewalk/index-top-walker
                             lookup)
            log_root_node (treewalk/create-parent-node
                            log_root_id
                            (fn fn__31477
                              ([p1__31473#] (treewalk/log-root-walker p1__31473# lookup false)))
                            lookup)]
        {:backup/version 3,
         :db-id (cluster/dbId cluster),
         :log-root-node log_root_node,
         :log-root-id log_root_id,
         :index-top-node index_top_node,
         :index/version (long (index/valid-version index_root_map)),
         :t t,
         :index-root-id index_root_id,
         :log-tail log_tail,
         :log/version (common/getx desc :d/l)})))
  (reset-meta!
    #'create-backup-job
    (assoc
      {:arglists (clojure.core/list ['cluster 'lookup]), :column (int 1)}
      :name
      'create-backup-job
      :ns
      *ns*))
  (defn create-restore-job
    ([storage t]
      (let [roots (or
                    (read-roots t storage)
                    (error/raise :restore/roots-missing (str "No database root for t " t)))
            value_storage (substorage storage "values")
            lookup (storage-olookup value_storage (:backup/version roots))
            map__31481 roots
            map__31481 (if (seq? map__31481)
                         (if (next map__31481)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31481))
                           (if (seq map__31481) (first map__31481) {}))
                         map__31481)
            index_root_id (get map__31481 :index-root-id)
            log_root_id (get map__31481 :log-root-id)]
        (assoc
          roots
          :lookup
          lookup
          :index-top-node
          (treewalk/create-parent-node index_root_id treewalk/index-top-walker lookup)
          :log-root-node
          (treewalk/create-parent-node
            log_root_id
            (fn fn__31482 ([p1__31480#] (treewalk/log-root-walker p1__31480# lookup false)))
            lookup)))))
  (reset-meta!
    #'create-restore-job
    (assoc
      {:arglists (clojure.core/list ['storage 't]), :column (int 1)}
      :name
      'create-restore-job
      :ns
      *ns*))
  (def create-cluster
   (fn create_cluster
     ([cluster_conf concurrency]
       (coord/create-db-cluster
         (assoc
           cluster_conf
           :read-concurrency
           (max concurrency (config/property "datomic.readConcurrency"))
           :write-concurrency
           (max concurrency (config/property "datomic.writeConcurrency"))
           :shared-pool?
           false)))))
  (reset-meta!
    #'create-cluster
    (assoc
      {:arglists (clojure.core/list ['cluster-conf 'concurrency]), :column (int 1)}
      :name
      'create-cluster
      :ns
      *ns*))
  (defn create-restore-target
    ([uri desc concurrency]
      (let [map__31487 (uri/parse-db uri)
            map__31487 (if (seq? map__31487)
                         (if (next map__31487)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31487))
                           (if (seq map__31487) (first map__31487) {}))
                         map__31487)
            cluster_conf map__31487
            db_name (get map__31487 :db-name)
            system_cluster (coord/create-system-cluster cluster_conf)
            catalog_resp (catalog/create-database system_cluster (assoc desc :db-name db_name))]
        (cond
          (or (:exists catalog_resp) (:created catalog_resp)) (create-cluster
                                                                (coord/cluster-conf->resolved-conf
                                                                  cluster_conf)
                                                                concurrency)
          (:name-conflict catalog_resp) (error/arg
                                          :restore/collision
                                          (str
                                            "The name '"
                                            db_name
                                            "' is already in use by a different database"))
          (:id-conflict catalog_resp) (error/arg
                                        :restore/collision
                                        (str
                                          "The database already exists under the name '"
                                          (:id-conflict catalog_resp)
                                          "'"))
          (:invalid-db-name catalog_resp) (error/arg
                                            :restore/invalid-db-name
                                            (str "The name '" db_name "'is an invalid name"))
          :default (do
                     (error/raise
                       :restore/create-db-failed
                       "Unable to create database"
                       catalog_resp))))))
  (reset-meta!
    #'create-restore-target
    (assoc
      {:arglists (clojure.core/list ['uri 'desc 'concurrency]), :column (int 1)}
      :name
      'create-restore-target
      :ns
      *ns*))
  (defn latest-t
    ([uri]
      (let [map__31490 (describe-backups uri)
            map__31490 (if (seq? map__31490)
                         (if (next map__31490)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31490))
                           (if (seq map__31490) (first map__31490) {}))
                         map__31490)
            ts (get map__31490 :ts)]
        (first ts))))
  (reset-meta!
    #'latest-t
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'latest-t :ns *ns*))
  (defn require-latest-t
    ([uri]
      (or
        (latest-t uri)
        (error/raise :restore/no-roots (str "No restore points available at " uri) {:uri uri}))))
  (reset-meta!
    #'require-latest-t
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'require-latest-t
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.backup" "create-ids->nodes") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.backup" "create-ids->nodes") treewalk/create-ids->nodes)
  (def restore-db
   (fn restore_db
     ([p__31494 p__31495 progress concurrency incremental?]
       (let [map__31496 p__31494
             map__31496 (if (seq? map__31496)
                          (if (next map__31496)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31496))
                            (if (seq map__31496) (first map__31496) {}))
                          map__31496)
             from_storage (get map__31496 :from-storage)
             t (get map__31496 :t)
             map__31497 p__31495
             map__31497 (if (seq? map__31497)
                          (if (next map__31497)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31497))
                            (if (seq map__31497) (first map__31497) {}))
                          map__31497)
             to_uri (get map__31497 :to-uri)
             job (create-restore-job from_storage t)
             backup_version (:backup/version job)
             map__31498 job
             map__31498 (if (seq? map__31498)
                          (if (next map__31498)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31498))
                            (if (seq map__31498) (first map__31498) {}))
                          map__31498)
             index_top_node (get map__31498 :index-top-node)
             log_root_node (get map__31498 :log-root-node)
             db_id (get map__31498 :db-id)
             lookup (get map__31498 :lookup)
             to_cluster (create-restore-target to_uri {:db-id db_id} concurrency)
             restore (create-value-restore
                       :from-storage
                       from_storage
                       :to-cluster
                       to_cluster
                       :backup-version
                       backup_version
                       :progress
                       progress
                       :ids->nodes
                       (create-ids->nodes lookup)
                       :incremental?
                       incremental?
                       :concurrency
                       concurrency)]
         (future-call
           (fn fn__31499
             ([]
               (try
                 (let [m_31500 {:event :restore/db, :t t, :db-id db_id}
                       ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.backup")]
                                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                           (.info
                                             ^org.slf4j.Logger logger
                                             (logger/process (assoc m_31500 :phase :begin))))
                                         nil)
                       start__8584__auto__ (java.lang.System/nanoTime)
                       result__8585__auto__ (try
                                              (do
                                                (when-not (garbage/ensure-root-ref
                                                            to_cluster
                                                            :forget-garbage)
                                                  (throw
                                                    (java.lang.Error.
                                                      "Unable to clear garbage root")))
                                                (restore-node restore log_root_node)
                                                (restore-node restore index_top_node)
                                                {:returned (restore-roots job to_cluster)})
                                              (catch
                                                java.lang.Throwable
                                                t__8586__auto__
                                                {:threw t__8586__auto__}))
                       elapsed_31501 (- (java.lang.System/nanoTime) start__8584__auto__)
                       msec_31502 (logger/format-as-msec (long elapsed_31501))]
                   (let [endmsg__8587__auto__ (merge
                                                (assoc m_31500 :msec msec_31502 :phase :end)
                                                (when (:threw result__8585__auto__)
                                                  {:threw (class (:threw result__8585__auto__))}))
                         logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                     (when (.isInfoEnabled ^org.slf4j.Logger logger)
                       (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                     nil)
                   (if (contains? result__8585__auto__ :returned)
                     (:returned result__8585__auto__)
                     (do (throw (:threw result__8585__auto__)) nil)))
                 (catch
                   java.lang.Throwable
                   t__8829__auto__
                   (do
                     (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")
                           ex t__8829__auto__]
                       (when (.isWarnEnabled ^org.slf4j.Logger logger)
                         (.warn
                           ^org.slf4j.Logger logger
                           (logger/process "error executing future")
                           ^java.lang.Throwable ex)
                         (logger/caused-by logger ex))
                       nil)
                     (monitor/alarm :UnhandledException)
                     (throw ^java.lang.Throwable t__8829__auto__)
                     nil))))))))))
  (reset-meta!
    #'restore-db
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['from-storage 't]} {:keys ['to-uri]} 'progress 'concurrency 'incremental?]),
       :column (int 1)}
      :name
      'restore-db
      :ns
      *ns*))
  (def backup-key->seg-id
   (fn backup_key__GT_seg_id
     ([s]
       (let [idx (.lastIndexOf ^java.lang.String s "/")]
         (if (<= 0 idx)
           (subs s (long (inc idx)) (java.lang.Integer/valueOf (int (count s))))
           s)))))
  (reset-meta!
    #'backup-key->seg-id
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 's {:tag 'String})]),
       :column (int 1)}
      :name
      'backup-key->seg-id
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.backup" "->SegSetStorage")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.backup" "map->SegSetStorage")
    {:declared true, :column (int 1)})
  (defrecord
    SegSetStorage
    [storage seg-id-set]
    datomic.backup.Storage
    (retrieve [this k] (retrieve storage k))
    (exists? [this k] (contains? seg-id-set (backup-key->seg-id k)))
    (list-keys [this prefix] (list-keys storage prefix))
    (store [this k buf] (store storage k buf)))
  (clojure.core/import 'datomic.backup.SegSetStorage)
  (def ->SegSetStorage
   (fn __GT_SegSetStorage
     ([storage seg_id_set] (datomic.backup.SegSetStorage. storage seg_id_set))))
  (reset-meta!
    #'->SegSetStorage
    (assoc
      {:arglists (clojure.core/list ['storage 'seg-id-set]), :column (int 1)}
      :name
      '->SegSetStorage
      :ns
      *ns*))
  (defn map->SegSetStorage
    ([m__7972__auto__]
      (SegSetStorage/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->SegSetStorage
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->SegSetStorage
      :ns
      *ns*))
  (def backup-seg-ids
   (fn backup_seg_ids
     ([backup_storage t]
       (let [map__31536 (create-restore-job backup_storage t)
             map__31536 (if (seq? map__31536)
                          (if (next map__31536)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31536))
                            (if (seq map__31536) (first map__31536) {}))
                          map__31536)
             lookup (get map__31536 :lookup)
             index_top_node (get map__31536 :index-top-node)
             log_root_node (get map__31536 :log-root-node)]
         (treewalk/db-seq log_root_node index_top_node lookup)))))
  (reset-meta!
    #'backup-seg-ids
    (assoc
      {:arglists (clojure.core/list ['backup-storage 't]), :column (int 1)}
      :name
      'backup-seg-ids
      :ns
      *ns*))
  (def segset-storage
   (fn segset_storage
     ([backup_storage seg_id_set]
       (when seg_id_set
         (when-not (set? seg_id_set)
           (throw
             (java.lang.AssertionError.
               (str "Assert failed: " (pr-str (clojure.core/list 'set? 'seg-id-set)))))))
       (->SegSetStorage backup_storage seg_id_set))))
  (reset-meta!
    #'segset-storage
    (assoc
      {:arglists (clojure.core/list ['backup-storage 'seg-id-set]), :column (int 1)}
      :name
      'segset-storage
      :ns
      *ns*))
  (def maybe-segset-storage
   (fn maybe_segset_storage
     ([backup_storage]
       (if (config/property "datomic.backupUseSegsetStorage")
         (segset-storage
           backup_storage
           (let [temp__5804__auto__ (latest-t backup_storage)]
             (when temp__5804__auto__
               (let [prior_backup_t temp__5804__auto__]
                 (into #{} (backup-seg-ids backup_storage prior_backup_t))))))
         backup_storage))))
  (reset-meta!
    #'maybe-segset-storage
    (assoc
      {:arglists (clojure.core/list ['backup-storage]), :column (int 1)}
      :name
      'maybe-segset-storage
      :ns
      *ns*))
  (def backup-db
   (fn backup_db
     ([from_uri to_storage progress concurrency incremental?]
       (let [cluster_conf (uri/parse-db from_uri)
             resolved_cluster_conf (or
                                     (coord/resolve-db-name cluster_conf)
                                     (error/raise
                                       :catalog/db-does-not-exist
                                       (str "Database does not exist: " from_uri)))
             from_cluster (create-cluster resolved_cluster_conf concurrency)
             to_storage (maybe-segset-storage to_storage)
             _ (ensure-claim to_storage (cluster/dbId from_cluster))
             olookup (domain/system-cache-olookup from_cluster)
             job (create-backup-job from_cluster olookup)
             map__31541 job
             map__31541 (if (seq? map__31541)
                          (if (next map__31541)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31541))
                            (if (seq map__31541) (first map__31541) {}))
                          map__31541)
             index_top_node (get map__31541 :index-top-node)
             log_root_node (get map__31541 :log-root-node)
             db_id (get map__31541 :db-id)
             t (get map__31541 :t)
             backup (create-value-backup
                      :from-cluster
                      from_cluster
                      :to-storage
                      to_storage
                      :progress
                      progress
                      :ids->nodes
                      (create-ids->nodes olookup)
                      :concurrency
                      concurrency
                      :incremental?
                      incremental?)]
         (future-call
           (fn fn__31542
             ([]
               (try
                 (let [m_31543 {:event :backup/db, :t t, :db-id (:db-id job)}
                       ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.backup")]
                                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                           (.info
                                             ^org.slf4j.Logger logger
                                             (logger/process (assoc m_31543 :phase :begin))))
                                         nil)
                       start__8584__auto__ (java.lang.System/nanoTime)
                       result__8585__auto__ (try
                                              (do
                                                (backup-node backup index_top_node)
                                                (backup-node backup log_root_node)
                                                (when-not (:k (backup-roots job t to_storage))
                                                  (throw
                                                    (java.lang.RuntimeException.
                                                      "Backup roots failed")))
                                                {:returned :succeeded})
                                              (catch
                                                java.lang.Throwable
                                                t__8586__auto__
                                                {:threw t__8586__auto__}))
                       elapsed_31544 (- (java.lang.System/nanoTime) start__8584__auto__)
                       msec_31545 (logger/format-as-msec (long elapsed_31544))]
                   (let [endmsg__8587__auto__ (merge
                                                (assoc m_31543 :msec msec_31545 :phase :end)
                                                (when (:threw result__8585__auto__)
                                                  {:threw (class (:threw result__8585__auto__))}))
                         logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                     (when (.isInfoEnabled ^org.slf4j.Logger logger)
                       (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                     nil)
                   (if (contains? result__8585__auto__ :returned)
                     (:returned result__8585__auto__)
                     (do (throw (:threw result__8585__auto__)) nil)))
                 (catch
                   java.lang.Throwable
                   t__8829__auto__
                   (do
                     (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")
                           ex t__8829__auto__]
                       (when (.isWarnEnabled ^org.slf4j.Logger logger)
                         (.warn
                           ^org.slf4j.Logger logger
                           (logger/process "error executing future")
                           ^java.lang.Throwable ex)
                         (logger/caused-by logger ex))
                       nil)
                     (monitor/alarm :UnhandledException)
                     (throw ^java.lang.Throwable t__8829__auto__)
                     nil))))))))))
  (reset-meta!
    #'backup-db
    (assoc
      {:arglists (clojure.core/list ['from-uri 'to-storage 'progress 'concurrency 'incremental?]),
       :column (int 1)}
      :name
      'backup-db
      :ns
      *ns*))
  (defn backup-concurrency
    ([uri]
      (let [scheme (.getScheme (io/as-uri uri)) G__31555 scheme]
        (case
          G__31555
          "s3"
          (config/property "datomic.s3BackupConcurrency")
          "file"
          (config/property "datomic.fileBackupConcurrency")))))
  (reset-meta!
    #'backup-concurrency
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'backup-concurrency
      :ns
      *ns*))
  (def backup
   (fn backup
     ([from_conn_uri to_storage_uri sse? progress incremental?]
       (let [storage (create-storage to_storage_uri sse?)]
         (backup-db
           from_conn_uri
           storage
           progress
           (backup-concurrency to_storage_uri)
           incremental?)))))
  (reset-meta!
    #'backup
    (assoc
      {:arglists
       (clojure.core/list ['from-conn-uri 'to-storage-uri 'sse? 'progress 'incremental?]),
       :column (int 1)}
      :name
      'backup
      :ns
      *ns*))
  (def restore
   (fn restore
     ([from_storage_uri to_uri progress t incremental?]
       (restore-db
         {:from-storage (create-storage from_storage_uri), :t t}
         {:to-uri to_uri}
         progress
         (backup-concurrency from_storage_uri)
         incremental?))))
  (reset-meta!
    #'restore
    (assoc
      {:arglists (clojure.core/list ['from-storage-uri 'to-uri 'progress 't 'incremental?]),
       :column (int 1)}
      :name
      'restore
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.backup" "gc-deleted-dbs") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.backup" "gc-deleted-dbs") garbage/gc-deleted-dbs)
  (.setMeta (clojure.lang.RT/var "datomic.backup" "prefixes") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.backup" "prefixes")
    (into
      (sorted-set)
      (mapcat
        (fn fn__31560
          ([p1__31559#]
            (vector (format "%02x" p1__31559#) (str/upper-case (format "%02x" p1__31559#)))))
        (range 256))))
  (def missing-seg-ids
   (fn missing_seg_ids
     ([backup_storage seg_id_set]
       (when-not (set? seg_id_set)
         (throw
           (java.lang.AssertionError.
             (str "Assert failed: " (pr-str (clojure.core/list 'set? 'seg-id-set))))))
       (let [value_substorage (substorage backup_storage "values")
             map__31563 (queue/queue-seq (long (* (* (count prefixes) 1000) 10)))
             map__31563 (if (seq? map__31563)
                          (if (next map__31563)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31563))
                            (if (seq map__31563) (first map__31563) {}))
                          map__31563)
             fill (get map__31563 :fill)
             done (get map__31563 :done)
             drain (get map__31563 :drain)
             ms_fut (future-call
                      (fn fn__31564
                        ([]
                          (reduce
                            (fn fn__31565 ([acc k] (disj acc k)))
                            seg_id_set
                            (^clojure.lang.IFn drain)))))]
         (let [futs (mapv
                      (fn fn__31568
                        ([prefix]
                          (common/pfuture
                            (deref thread-pool)
                            (fn fn__31569
                              ([]
                                (let [m_31570 {:event :verify/list-prefix, :prefix prefix}
                                      ___8583__auto__ (let [logger
                                                            (org.slf4j.LoggerFactory/getLogger
                                                              "datomic.backup")]
                                                        (when (.isInfoEnabled
                                                                ^org.slf4j.Logger logger)
                                                          (.info
                                                            ^org.slf4j.Logger logger
                                                            (logger/process
                                                              (assoc m_31570 :phase :begin))))
                                                        nil)
                                      start__8584__auto__ (java.lang.System/nanoTime)
                                      result__8585__auto__ (try
                                                             {:returned
                                                              (run!
                                                                fill
                                                                (filter
                                                                  (fn 
                                                                    fn__31574
                                                                    ([p1__31562#]
                                                                      (contains?
                                                                        seg_id_set
                                                                        p1__31562#)))
                                                                  (:ks
                                                                    (list-keys
                                                                      (substorage
                                                                        value_substorage
                                                                        prefix)
                                                                      ""))))}
                                                             (catch
                                                               java.lang.Throwable
                                                               t__8586__auto__
                                                               {:threw t__8586__auto__}))
                                      elapsed_31571 (-
                                                      (java.lang.System/nanoTime)
                                                      start__8584__auto__)
                                      msec_31572 (logger/format-as-msec (long elapsed_31571))]
                                  (let [endmsg__8587__auto__ (merge
                                                               (assoc
                                                                 m_31570
                                                                 :msec
                                                                 msec_31572
                                                                 :phase
                                                                 :end)
                                                               (when
                                                                 (:threw result__8585__auto__)
                                                                 {:threw
                                                                  (class
                                                                    (:threw
                                                                      result__8585__auto__))}))
                                        logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.backup")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process endmsg__8587__auto__)))
                                    nil)
                                  (if (contains? result__8585__auto__ :returned)
                                    (:returned result__8585__auto__)
                                    (do (throw (:threw result__8585__auto__)) nil))))))))
                      prefixes)]
           (loop [seq_31583 (seq futs) chunk_31584 nil count_31585 0 i_31586 0]
             (if (< i_31586 count_31585)
               (let [fut (.nth ^clojure.lang.Indexed chunk_31584 (int i_31586))]
                 (deref fut)
                 (recur seq_31583 chunk_31584 count_31585 (inc i_31586)))
               (let [temp__5804__auto__ (seq seq_31583)]
                 (when temp__5804__auto__
                   (let [seq_31583 temp__5804__auto__]
                     (if (chunked-seq? seq_31583)
                       (let [c__6065__auto__ (chunk-first seq_31583)]
                         (recur
                           (chunk-rest seq_31583)
                           c__6065__auto__
                           (int (count c__6065__auto__))
                           (int 0)))
                       (let [fut (first seq_31583)]
                         (deref fut)
                         (recur (next seq_31583) nil 0 0))))))))
           (^clojure.lang.IFn done))
         (deref ms_fut)))))
  (reset-meta!
    #'missing-seg-ids
    (assoc
      {:arglists (clojure.core/list ['backup-storage 'seg-id-set]), :column (int 1)}
      :name
      'missing-seg-ids
      :ns
      *ns*))
  (def unreadable-seg-ids
   (fn unreadable_seg_ids
     ([lookup seg_ids]
       (let [unreadable? (fn unreadable_QMARK_
                           ([p1__31590#]
                             (try
                               (do (get lookup p1__31590#) false)
                               (catch java.lang.Throwable _ true))))]
         (keep
           deref
           (seque
             pool-size
             (map
               (fn fn__31593
                 ([k]
                   (common/pfuture
                     (deref thread-pool)
                     (fn fn__31594
                       ([]
                         (when (^clojure.lang.IFn unreadable? k)
                           (common/log-and-print {:event :verify-backup/unreadable-segment, :k k})
                           k))))))
               seg_ids)))))))
  (reset-meta!
    #'unreadable-seg-ids
    (assoc
      {:arglists (clojure.core/list ['lookup 'seg-ids]), :column (int 1)}
      :name
      'unreadable-seg-ids
      :ns
      *ns*))
  (def verify-backup
   (fn verify_backup
     ([p__31598]
       (let [map__31599 p__31598
             map__31599 (if (seq? map__31599)
                          (if (next map__31599)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31599))
                            (if (seq map__31599) (first map__31599) {}))
                          map__31599)
             backup_uri (get map__31599 :backup-uri)
             t (get map__31599 :t)
             read_all (get map__31599 :read-all)
             backup_storage (create-storage backup_uri nil)
             map__31600 (or
                          (read-roots t backup_storage)
                          (error/raise :verify/roots-missing (str "No database root for t " t)))
             map__31600 (if (seq? map__31600)
                          (if (next map__31600)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31600))
                            (if (seq map__31600) (first map__31600) {}))
                          map__31600)
             version (get map__31600 :backup/version)]
         (when (< version 3)
           (throw
             (java.lang.RuntimeException. "Verify not supported for pre-2015 backup format.")))
         (let [_ nil
               seg_id_set (into #{} (backup-seg-ids backup_storage t))
               missing (missing-seg-ids backup_storage seg_id_set)
               segcount (count seg_id_set)
               progress (fn progress
                          ([x]
                            (when (= (mod x 1000) 0)
                              (println
                                (format
                                  "%s of %s"
                                  x
                                  (java.lang.Integer/valueOf (int segcount)))))))]
           (merge
             {:t t,
              :total-segments (java.lang.Integer/valueOf (int segcount)),
              :missing-segments missing}
             (when read_all
               {:unreadable-segments
                (doall
                  (unreadable-seg-ids
                    (:lookup (create-restore-job backup_storage t))
                    (map-indexed
                      (fn fn__31603 ([idx arg] (^clojure.lang.IFn progress idx) arg))
                      seg_id_set)))})))))))
  (reset-meta!
    #'verify-backup
    (assoc
      {:arglists (clojure.core/list [{:keys ['backup-uri 't 'read-all]}]), :column (int 1)}
      :name
      'verify-backup
      :ns
      *ns*)))