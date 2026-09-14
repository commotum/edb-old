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
  (def pool-size
   (max
     (config/property "datomic.fileBackupConcurrency")
     (config/property "datomic.s3BackupConcurrency")))
  (def backup-branch-size (config/property "datomic.backupBranchConcurrency"))
  (defonce thread-pool (delay (common/thread-pool {:nthreads pool-size, :name "backup"})))
  (defonce backup-branch-pool
   (delay (common/thread-pool {:nthreads backup-branch-size, :name "backup-branch"})))
  (defonce Storage {})
  (defprotocol Storage (store [_ k buf]) (list-keys [_ prefix]) (exists? [_ k]) (retrieve [_ k]))
  (defn subkey ([prefix k] (str prefix "/" k)))
  (reset-meta!
    #'subkey
    (assoc
      {:private true, :arglists (clojure.core/list ['prefix 'k]), :column 1}
      :name
      'subkey
      :ns
      *ns*))
  (defn strip-prefix ([prefix k] (subs k (long (inc (count prefix))))))
  (reset-meta!
    #'strip-prefix
    (assoc
      {:private true, :arglists (clojure.core/list ['prefix 'k]), :column 1}
      :name
      'strip-prefix
      :ns
      *ns*))
  (defn add-key-prefix ([s] (str (subs s (long (- (count s) 2))) "/" s)))
  (reset-meta!
    #'add-key-prefix
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column 1}
      :name
      'add-key-prefix
      :ns
      *ns*))
  (defn backup-k-factory
    ([^long backup_version]
      (let [G__20009 backup_version] (case G__20009 (1 2) identity 3 add-key-prefix))))
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
            (fn fn__20013 ([p1__20011#] (map (partial strip-prefix prefix) p1__20011#))))
          result)))
    (store [this k buf] (store storage (subkey prefix k) buf)))
  (clojure.core/import 'datomic.backup.Substorage)
  (defn ->Substorage ([storage prefix] (datomic.backup.Substorage. storage prefix)))
  (defn substorage ([storage prefix] (->Substorage storage prefix)))
  (defn claim ([storage id] (store storage "owner" (io/string->bbuf id))))
  (defn claimed-by
    ([storage]
      (let [temp__5457__auto__ (retrieve storage "owner")]
        (when temp__5457__auto__
          (let [map__20021 temp__5457__auto__
                map__20021 (if (seq? map__20021)
                             (clojure.lang.PersistentHashMap/create (seq map__20021))
                             map__20021)
                v (get map__20021 :v)]
            (io/bbuf->string v))))))
  (defn ensure-claim
    ([storage id]
      (let [temp__5455__auto__ (claimed-by storage)]
        (if temp__5455__auto__
          (let [claimant temp__5455__auto__]
            (when-not (= claimant id)
              (error/arg :backup/claim-failed (str "Backup storage already used by " claimant)))
            :ok)
          (let [k (:k (claim storage id))]
            (when-not k (error/raise :backup/claim-failed "Unable to write to backup storage"))
            (recur storage id))))))
  (defmulti create-storage* (fn fn__20027 ([uri & _] (.getScheme ^java.net.URI uri))))
  (defmethod
    create-storage*
    "file"
    fn__20032
    ([uri sse?]
      (when sse?
        (error/arg
          :storage/sse-not-available
          "Server side encryption not available for file storage"))
      (req/require-and-run 'datomic.fsbackup/storage-from-uri uri)))
  (defmethod
    create-storage*
    "s3"
    fn__20034
    ([uri sse?] (req/require-and-run 'datomic.s3backup/storage-from-uri uri sse?)))
  (defmethod
    create-storage*
    :default
    fn__20036
    ([uri sse?]
      (error/arg
        :storage/invalid-uri
        (str "Unsupported protocol: " (.getScheme ^java.net.URI uri)))))
  (defn create-storage
    ([storage_uri sse?] (create-storage* (io/as-uri storage_uri) sse?))
    ([storage_uri] (create-storage storage_uri false)))
  (defn retry
    ([f]
      (common/retry-fn
        f
        :pred
        (fn fn__20041 ([p1__20039#] (instance? java.lang.Throwable p1__20039#)))
        :backoff
        (fn fn__20043
          ([p1__20040#]
            (java.lang.Double/valueOf
              (double
                (*
                  (+ 50 (rand-int 50))
                  (java.lang.Math/pow (double 2) (double ^java.lang.Number p1__20040#)))))))
        :log-retry
        common/log-retry
        :max-retries
        8)))
  (defn uncached-storage-lookup
    ([storage]
      (reify
        clojure.lang.ILookup
        (valAt
          [this k not_found]
          (let [ret (retry (fn fn__20047 ([] (retrieve storage k))))] (if ret (:v ret) not_found)))
        (valAt [this k] (.valAt this k nil)))))
  (defn storage-olookup
    ([storage backup_version]
      (cache/lookup-transformer
        (domain/peer-object-lookup
          (uncached-storage-lookup storage)
          domain/common-read-handlers
          (domain/system-cache))
        :key-fn
        (comp (backup-k-factory (long ^java.lang.Number backup_version)) cluster/uuid->val-key))))
  (defonce IValueBackup {})
  (defprotocol IValueBackup (backup-val [_ k backup-k]) (backup-node [_ node]))
  (deftype
    ValueBackup
    [from_cluster value_storage progress incremental? ids_>nodes throttle sem]
    datomic.backup.IValueBackup
    (backup-node
      [this node]
      (let [k (treewalk/node-id node)
            backup_k (add-key-prefix k)
            node_exists? (retry (fn fn__20097 ([] (exists? value_storage backup_k))))]
        (when-not (and node_exists? incremental?)
          (let [temp__5455__auto__ (treewalk/subtrees node ids_>nodes)]
            (if temp__5455__auto__
              (let [branch_nodes temp__5455__auto__
                    futs (mapv
                           (fn fn__20099
                             ([branch]
                               (common/pfuture
                                 (deref backup-branch-pool)
                                 (fn fn__20100 ([] (backup-node this branch))))))
                           branch_nodes)]
                (loop [seq_20103 (seq futs) chunk_20104 nil count_20105 0 i_20106 0]
                  (if (< i_20106 count_20105)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_20104 (int i_20106))]
                      (deref fut)
                      (recur seq_20103 chunk_20104 count_20105 (inc i_20106)))
                    (let [temp__5457__auto__ (seq seq_20103)]
                      (when temp__5457__auto__
                        (let [seq_20103 temp__5457__auto__]
                          (if (chunked-seq? seq_20103)
                            (let [c__5719__auto__ (chunk-first seq_20103)]
                              (recur
                                (chunk-rest seq_20103)
                                c__5719__auto__
                                (int (count c__5719__auto__))
                                (int 0)))
                            (let [fut (first seq_20103)]
                              (deref fut)
                              (recur (next seq_20103) nil 0 0)))))))))
              (let [futs (mapv
                           (fn fn__20107
                             ([leaf_id]
                               (when throttle (^clojure.lang.IFn throttle))
                               (common/pfuture
                                 (deref thread-pool)
                                 (fn fn__20108
                                   ([]
                                     (let [backup_leaf_id (add-key-prefix leaf_id)]
                                       (if (retry
                                             (fn fn__20109
                                               ([] (exists? value_storage backup_leaf_id))))
                                         (^clojure.lang.IFn progress :skipped)
                                         (backup-val this leaf_id backup_leaf_id))))))))
                           (treewalk/child-node-ids node))]
                (loop [seq_20113 (seq futs) chunk_20114 nil count_20115 0 i_20116 0]
                  (if (< i_20116 count_20115)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_20114 (int i_20116))]
                      (deref fut)
                      (recur seq_20113 chunk_20114 count_20115 (inc i_20116)))
                    (let [temp__5457__auto__ (seq seq_20113)]
                      (when temp__5457__auto__
                        (let [seq_20113 temp__5457__auto__]
                          (if (chunked-seq? seq_20113)
                            (let [c__5719__auto__ (chunk-first seq_20113)]
                              (recur
                                (chunk-rest seq_20113)
                                c__5719__auto__
                                (int (count c__5719__auto__))
                                (int 0)))
                            (let [fut (first seq_20113)]
                              (deref fut)
                              (recur (next seq_20113) nil 0 0))))))))))))
        (if node_exists? (^clojure.lang.IFn progress :skipped) (backup-val this k backup_k))))
    (backup-val
      [this k backup_k]
      (do
        (.acquire ^java.util.concurrent.Semaphore sem)
        (try
          (let [m_20088 {:event :backup/segment, :k k}
                ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process (assoc m_20088 :phase :begin)))
                                    nil)
                                  nil)
                start__8981__auto__ (java.lang.System/nanoTime)
                result__8982__auto__ (try
                                       {:returned
                                        (let [temp__5455__auto__ (deref
                                                                   (cluster/get-val
                                                                     from_cluster
                                                                     k))]
                                          (if temp__5455__auto__
                                            (let [map__20092 temp__5455__auto__
                                                  map__20092 (if
                                                               (seq? map__20092)
                                                               (clojure.lang.PersistentHashMap/create
                                                                 (seq map__20092))
                                                               map__20092)
                                                  buf (get map__20092 :buf)
                                                  result (retry
                                                           (fn 
                                                             fn__20093
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
                                         t__8983__auto__
                                         {:threw t__8983__auto__}))
                elapsed_20089 (- (java.lang.System/nanoTime) start__8981__auto__)
                msec_20090 (logger/format-as-msec (long elapsed_20089))]
            (let [endmsg__8984__auto__ (merge
                                         (assoc m_20088 :msec msec_20090 :phase :end)
                                         (when (:threw result__8982__auto__)
                                           {:threw (class (:threw result__8982__auto__))}))
                  logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                nil)
              nil)
            (if (contains? result__8982__auto__ :returned)
              (:returned result__8982__auto__)
              (do (throw (:threw result__8982__auto__)) nil)))
          (finally (do (.release ^java.util.concurrent.Semaphore sem) nil))))))
  (clojure.core/import 'datomic.backup.ValueBackup)
  (defn ->ValueBackup
    ([from_cluster value_storage progress incremental? ids_>nodes throttle sem]
      (datomic.backup.ValueBackup.
        from_cluster
        value_storage
        progress
        incremental?
        ids_>nodes
        throttle
        sem)))
  (defn create-value-backup
    ([& p__20131]
      (let [map__20132 p__20131
            map__20132 (if (seq? map__20132)
                         (clojure.lang.PersistentHashMap/create (seq map__20132))
                         map__20132)
            from_cluster (get map__20132 :from-cluster)
            to_storage (get map__20132 :to-storage)
            progress (get map__20132 :progress)
            incremental? (get map__20132 :incremental?)
            ids_>nodes (get map__20132 :ids->nodes)
            concurrency (get map__20132 :concurrency)]
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
          (let [temp__5457__auto__ (config/property "datomic.backupPaceMsec")]
            (when temp__5457__auto__
              (let [pace temp__5457__auto__]
                (fn fn__20133 ([] (java.lang.Thread/sleep (long ^java.lang.Number pace)) nil)))))
          (java.util.concurrent.Semaphore. (int ^java.lang.Number concurrency))))))
  (defonce IValueRestore {})
  (defprotocol IValueRestore (restore-val [_ k]) (restore-node [_ node]))
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
          (let [temp__5455__auto__ (treewalk/subtrees node ids_>nodes)]
            (if temp__5455__auto__
              (let [branch_nodes temp__5455__auto__
                    futs (mapv
                           (fn fn__20180
                             ([branch]
                               (common/pfuture
                                 (deref backup-branch-pool)
                                 (fn fn__20181 ([] (restore-node this branch))))))
                           branch_nodes)]
                (loop [seq_20184 (seq futs) chunk_20185 nil count_20186 0 i_20187 0]
                  (if (< i_20187 count_20186)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_20185 (int i_20187))]
                      (deref fut)
                      (recur seq_20184 chunk_20185 count_20186 (inc i_20187)))
                    (let [temp__5457__auto__ (seq seq_20184)]
                      (when temp__5457__auto__
                        (let [seq_20184 temp__5457__auto__]
                          (if (chunked-seq? seq_20184)
                            (let [c__5719__auto__ (chunk-first seq_20184)]
                              (recur
                                (chunk-rest seq_20184)
                                c__5719__auto__
                                (int (count c__5719__auto__))
                                (int 0)))
                            (let [fut (first seq_20184)]
                              (deref fut)
                              (recur (next seq_20184) nil 0 0)))))))))
              (let [futs (mapv
                           (fn fn__20188
                             ([leaf_id]
                               (common/pfuture
                                 (deref thread-pool)
                                 (fn fn__20189
                                   ([]
                                     (if (deref
                                           (cluster/get-val2
                                             to_cluster
                                             leaf_id
                                             #:datomic.core2.val-store.opts{:skip-cache true}))
                                       (^clojure.lang.IFn progress :skipped)
                                       (restore-val this leaf_id)))))))
                           (treewalk/child-node-ids node))]
                (loop [seq_20192 (seq futs) chunk_20193 nil count_20194 0 i_20195 0]
                  (if (< i_20195 count_20194)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_20193 (int i_20195))]
                      (deref fut)
                      (recur seq_20192 chunk_20193 count_20194 (inc i_20195)))
                    (let [temp__5457__auto__ (seq seq_20192)]
                      (when temp__5457__auto__
                        (let [seq_20192 temp__5457__auto__]
                          (if (chunked-seq? seq_20192)
                            (let [c__5719__auto__ (chunk-first seq_20192)]
                              (recur
                                (chunk-rest seq_20192)
                                c__5719__auto__
                                (int (count c__5719__auto__))
                                (int 0)))
                            (let [fut (first seq_20192)]
                              (deref fut)
                              (recur (next seq_20192) nil 0 0))))))))))))
        (if cluster_v (^clojure.lang.IFn progress :skipped) (restore-val this k))))
    (restore-val
      [this k]
      (do
        (.acquire ^java.util.concurrent.Semaphore sem)
        (try
          (let [m_20171 {:event :restore/segment, :k k}
                ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process (assoc m_20171 :phase :begin)))
                                    nil)
                                  nil)
                start__8981__auto__ (java.lang.System/nanoTime)
                result__8982__auto__ (try
                                       {:returned
                                        (let [temp__5455__auto__ (retry
                                                                   (fn 
                                                                     fn__20175
                                                                     ([]
                                                                       (retrieve
                                                                         value_storage
                                                                         (^clojure.lang.IFn k_>backup_k
                                                                           k)))))]
                                          (if temp__5455__auto__
                                            (let [map__20177 temp__5455__auto__
                                                  map__20177 (if
                                                               (seq? map__20177)
                                                               (clojure.lang.PersistentHashMap/create
                                                                 (seq map__20177))
                                                               map__20177)
                                                  v (get map__20177 :v)]
                                              (deref (cluster/create-val to_cluster k v))
                                              (^clojure.lang.IFn progress :copied))
                                            (error/raise
                                              :restore/read-failed
                                              (str "Unable to read " k)
                                              {:key k})))}
                                       (catch
                                         java.lang.Throwable
                                         t__8983__auto__
                                         {:threw t__8983__auto__}))
                elapsed_20172 (- (java.lang.System/nanoTime) start__8981__auto__)
                msec_20173 (logger/format-as-msec (long elapsed_20172))]
            (let [endmsg__8984__auto__ (merge
                                         (assoc m_20171 :msec msec_20173 :phase :end)
                                         (when (:threw result__8982__auto__)
                                           {:threw (class (:threw result__8982__auto__))}))
                  logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                nil)
              nil)
            (if (contains? result__8982__auto__ :returned)
              (:returned result__8982__auto__)
              (do (throw (:threw result__8982__auto__)) nil)))
          (finally (do (.release ^java.util.concurrent.Semaphore sem) nil))))))
  (clojure.core/import 'datomic.backup.ValueRestore)
  (defn ->ValueRestore
    ([value_storage to_cluster progress incremental? k_>backup_k ids_>nodes sem]
      (datomic.backup.ValueRestore.
        value_storage
        to_cluster
        progress
        incremental?
        k_>backup_k
        ids_>nodes
        sem)))
  (defn create-value-restore
    ([& p__20210]
      (let [map__20211 p__20210
            map__20211 (if (seq? map__20211)
                         (clojure.lang.PersistentHashMap/create (seq map__20211))
                         map__20211)
            from_storage (get map__20211 :from-storage)
            to_cluster (get map__20211 :to-cluster)
            backup_version (get map__20211 :backup-version)
            progress (get map__20211 :progress)
            incremental? (get map__20211 :incremental?)
            concurrency (get map__20211 :concurrency)
            ids_>nodes (get map__20211 :ids->nodes)]
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
          (java.util.concurrent.Semaphore. (int ^java.lang.Number concurrency))))))
  (defn roots-path ([t] (str/join "/" ["roots" t])))
  (def v1->v2-keymap
   {:index-root-id :index/root-id, :log-root-id :log/root-id, :log-tail :log/tail, :db-id :db/id})
  (def BACKUP_VERSION 3)
  (reset-meta! #'BACKUP_VERSION (assoc {:const true, :column 1} :name 'BACKUP_VERSION :ns *ns*))
  (def mem-keys
   [:index-root-id :log-root-id :log-tail :db-id :log/version :index/version :backup/version])
  (reset-meta! #'mem-keys (assoc {:private true, :column 1} :name 'mem-keys :ns *ns*))
  (defn mem->backup ([roots] (set/rename-keys (common/require-keys roots mem-keys) v1->v2-keymap)))
  (reset-meta!
    #'mem->backup
    (assoc
      {:private true, :arglists (clojure.core/list ['roots]), :column 1}
      :name
      'mem->backup
      :ns
      *ns*))
  (defn backup->mem
    ([roots]
      (let [roots (if (:backup/version roots) roots (assoc roots :backup/version 1))
            version (long (:backup/version roots))]
        (common/require-keys
          (let [G__20215 version]
            (case
              G__20215
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
      {:private true, :arglists (clojure.core/list ['roots]), :column 1}
      :name
      'backup->mem
      :ns
      *ns*))
  (defn backup-roots
    ([job t to_storage]
      (store
        to_storage
        (roots-path t)
        (fressian/byte-buf (mem->backup job) :handlers fressian/user-write-handlers))))
  (defn read-roots
    ([t from_storage]
      (let [temp__5457__auto__ (:v (retrieve from_storage (roots-path t)))]
        (when temp__5457__auto__
          (let [buf temp__5457__auto__]
            (backup->mem (fressian/defressian buf :handlers fressian/user-read-handlers)))))))
  (defn describe-backups
    ([storage]
      (let [storage (if (string? storage) (create-storage storage) storage)]
        {:db-id (claimed-by storage),
         :ts
         (sort
           >
           (mapv
             (fn fn__20222
               ([p1__20221#] (long (java.lang.Long/parseLong ^java.lang.String p1__20221#))))
             (filter
               (fn fn__20224 ([p1__20220#] (re-matches #"\d+" p1__20220#)))
               (:ks (list-keys (substorage storage "roots") "")))))})))
  (defn restore-roots
    ([job cluster]
      (let [map__20227 job
            map__20227 (if (seq? map__20227)
                         (clojure.lang.PersistentHashMap/create (seq map__20227))
                         map__20227)
            index_root_id (get map__20227 :index-root-id)
            log_root_id (get map__20227 :log-root-id)
            log_tail (get map__20227 :log-tail)]
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
  (defn create-backup-job
    ([cluster lookup]
      (let [vec__20230 (log/read-tail-descriptor cluster)
            desc (nth vec__20230 (int 0) nil)
            buf (nth vec__20230 (int 1) nil)
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
                            (fn fn__20233
                              ([p1__20229#] (treewalk/log-root-walker p1__20229# lookup false)))
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
  (defn create-restore-job
    ([storage t]
      (let [roots (or
                    (read-roots t storage)
                    (error/raise :restore/roots-missing (str "No database root for t " t)))
            value_storage (substorage storage "values")
            lookup (storage-olookup value_storage (:backup/version roots))
            map__20237 roots
            map__20237 (if (seq? map__20237)
                         (clojure.lang.PersistentHashMap/create (seq map__20237))
                         map__20237)
            index_root_id (get map__20237 :index-root-id)
            log_root_id (get map__20237 :log-root-id)]
        (assoc
          roots
          :lookup
          lookup
          :index-top-node
          (treewalk/create-parent-node index_root_id treewalk/index-top-walker lookup)
          :log-root-node
          (treewalk/create-parent-node
            log_root_id
            (fn fn__20238 ([p1__20236#] (treewalk/log-root-walker p1__20236# lookup false)))
            lookup)))))
  (defn create-cluster
    ([cluster_conf concurrency]
      (coord/create-db-cluster
        (assoc
          cluster_conf
          :read-concurrency
          (max concurrency (config/property "datomic.readConcurrency"))
          :write-concurrency
          (max concurrency (config/property "datomic.writeConcurrency"))
          :shared-pool?
          false))))
  (defn create-restore-target
    ([uri desc concurrency]
      (let [map__20243 (uri/parse-db uri)
            map__20243 (if (seq? map__20243)
                         (clojure.lang.PersistentHashMap/create (seq map__20243))
                         map__20243)
            cluster_conf map__20243
            db_name (get map__20243 :db-name)
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
  (defn latest-t
    ([uri]
      (let [map__20246 (describe-backups uri)
            map__20246 (if (seq? map__20246)
                         (clojure.lang.PersistentHashMap/create (seq map__20246))
                         map__20246)
            ts (get map__20246 :ts)]
        (first ts))))
  (defn require-latest-t
    ([uri]
      (or
        (latest-t uri)
        (error/raise :restore/no-roots (str "No restore points available at " uri) {:uri uri}))))
  (def create-ids->nodes treewalk/create-ids->nodes)
  (defn restore-db
    ([p__20250 p__20251 progress concurrency incremental?]
      (let [map__20252 p__20250
            map__20252 (if (seq? map__20252)
                         (clojure.lang.PersistentHashMap/create (seq map__20252))
                         map__20252)
            from_storage (get map__20252 :from-storage)
            t (get map__20252 :t)
            map__20253 p__20251
            map__20253 (if (seq? map__20253)
                         (clojure.lang.PersistentHashMap/create (seq map__20253))
                         map__20253)
            to_uri (get map__20253 :to-uri)
            job (create-restore-job from_storage t)
            backup_version (:backup/version job)
            map__20254 job
            map__20254 (if (seq? map__20254)
                         (clojure.lang.PersistentHashMap/create (seq map__20254))
                         map__20254)
            index_top_node (get map__20254 :index-top-node)
            log_root_node (get map__20254 :log-root-node)
            db_id (get map__20254 :db-id)
            lookup (get map__20254 :lookup)
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
          (fn fn__20255
            ([]
              (try
                (let [m_20256 {:event :restore/db, :t t, :db-id db_id}
                      ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.backup")]
                                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                          (.info
                                            ^org.slf4j.Logger logger
                                            (logger/process (assoc m_20256 :phase :begin)))
                                          nil)
                                        nil)
                      start__8981__auto__ (java.lang.System/nanoTime)
                      result__8982__auto__ (try
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
                                               t__8983__auto__
                                               {:threw t__8983__auto__}))
                      elapsed_20257 (- (java.lang.System/nanoTime) start__8981__auto__)
                      msec_20258 (logger/format-as-msec (long elapsed_20257))]
                  (let [endmsg__8984__auto__ (merge
                                               (assoc m_20256 :msec msec_20258 :phase :end)
                                               (when (:threw result__8982__auto__)
                                                 {:threw (class (:threw result__8982__auto__))}))
                        logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                      (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                      nil)
                    nil)
                  (if (contains? result__8982__auto__ :returned)
                    (:returned result__8982__auto__)
                    (do (throw (:threw result__8982__auto__)) nil)))
                (catch
                  java.lang.Throwable
                  t__9147__auto__
                  (do
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")
                          ex t__9147__auto__]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn
                          ^org.slf4j.Logger logger
                          (logger/process "error executing future")
                          ^java.lang.Throwable ex)
                        (logger/caused-by logger ex))
                      nil)
                    (monitor/alarm :UnhandledException)
                    (throw ^java.lang.Throwable t__9147__auto__)
                    nil)))))))))
  (defn backup-key->seg-id
    ([s]
      (let [idx (.lastIndexOf ^java.lang.String s "/")]
        (if (<= 0 idx) (subs s (long (inc idx)) (java.lang.Integer/valueOf (int (count s)))) s))))
  (reset-meta!
    #'backup-key->seg-id
    (assoc
      {:private true, :arglists (clojure.core/list [(.withMeta 's {:tag 'String})]), :column 1}
      :name
      'backup-key->seg-id
      :ns
      *ns*))
  (declare ->SegSetStorage)
  (declare map->SegSetStorage)
  (defrecord
    SegSetStorage
    [storage seg-id-set]
    datomic.backup.Storage
    (retrieve [this k] (retrieve storage k))
    (exists? [this k] (contains? seg-id-set (backup-key->seg-id k)))
    (list-keys [this prefix] (list-keys storage prefix))
    (store [this k buf] (store storage k buf)))
  (clojure.core/import 'datomic.backup.SegSetStorage)
  (defn ->SegSetStorage ([storage seg_id_set] (datomic.backup.SegSetStorage. storage seg_id_set)))
  (defn map->SegSetStorage
    ([m__7585__auto__]
      (SegSetStorage/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn backup-seg-ids
    ([backup_storage t]
      (let [map__20292 (create-restore-job backup_storage t)
            map__20292 (if (seq? map__20292)
                         (clojure.lang.PersistentHashMap/create (seq map__20292))
                         map__20292)
            lookup (get map__20292 :lookup)
            index_top_node (get map__20292 :index-top-node)
            log_root_node (get map__20292 :log-root-node)]
        (treewalk/db-seq log_root_node index_top_node lookup))))
  (defn segset-storage
    ([backup_storage seg_id_set]
      (when seg_id_set
        (when-not (set? seg_id_set)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list 'set? 'seg-id-set)))))))
      (->SegSetStorage backup_storage seg_id_set)))
  (defn maybe-segset-storage
    ([backup_storage]
      (if (config/property "datomic.backupUseSegsetStorage")
        (segset-storage
          backup_storage
          (let [temp__5457__auto__ (latest-t backup_storage)]
            (when temp__5457__auto__
              (let [prior_backup_t temp__5457__auto__]
                (into #{} (backup-seg-ids backup_storage prior_backup_t))))))
        backup_storage)))
  (defn backup-db
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
            map__20297 job
            map__20297 (if (seq? map__20297)
                         (clojure.lang.PersistentHashMap/create (seq map__20297))
                         map__20297)
            index_top_node (get map__20297 :index-top-node)
            log_root_node (get map__20297 :log-root-node)
            db_id (get map__20297 :db-id)
            t (get map__20297 :t)
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
          (fn fn__20298
            ([]
              (try
                (let [m_20299 {:event :backup/db, :t t, :db-id (:db-id job)}
                      ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.backup")]
                                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                          (.info
                                            ^org.slf4j.Logger logger
                                            (logger/process (assoc m_20299 :phase :begin)))
                                          nil)
                                        nil)
                      start__8981__auto__ (java.lang.System/nanoTime)
                      result__8982__auto__ (try
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
                                               t__8983__auto__
                                               {:threw t__8983__auto__}))
                      elapsed_20300 (- (java.lang.System/nanoTime) start__8981__auto__)
                      msec_20301 (logger/format-as-msec (long elapsed_20300))]
                  (let [endmsg__8984__auto__ (merge
                                               (assoc m_20299 :msec msec_20301 :phase :end)
                                               (when (:threw result__8982__auto__)
                                                 {:threw (class (:threw result__8982__auto__))}))
                        logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                      (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                      nil)
                    nil)
                  (if (contains? result__8982__auto__ :returned)
                    (:returned result__8982__auto__)
                    (do (throw (:threw result__8982__auto__)) nil)))
                (catch
                  java.lang.Throwable
                  t__9147__auto__
                  (do
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")
                          ex t__9147__auto__]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn
                          ^org.slf4j.Logger logger
                          (logger/process "error executing future")
                          ^java.lang.Throwable ex)
                        (logger/caused-by logger ex))
                      nil)
                    (monitor/alarm :UnhandledException)
                    (throw ^java.lang.Throwable t__9147__auto__)
                    nil)))))))))
  (defn backup-concurrency
    ([uri]
      (let [scheme (.getScheme (io/as-uri uri)) G__20311 scheme]
        (case
          G__20311
          "s3"
          (config/property "datomic.s3BackupConcurrency")
          "file"
          (config/property "datomic.fileBackupConcurrency")))))
  (defn backup
    ([from_conn_uri to_storage_uri sse? progress incremental?]
      (let [storage (create-storage to_storage_uri sse?)]
        (backup-db
          from_conn_uri
          storage
          progress
          (backup-concurrency to_storage_uri)
          incremental?))))
  (defn restore
    ([from_storage_uri to_uri progress t incremental?]
      (restore-db
        {:from-storage (create-storage from_storage_uri), :t t}
        {:to-uri to_uri}
        progress
        (backup-concurrency from_storage_uri)
        incremental?)))
  (def gc-deleted-dbs garbage/gc-deleted-dbs)
  (def prefixes
   (into
     (sorted-set)
     (mapcat
       (fn fn__20316
         ([p1__20315#]
           (vector (format "%02x" p1__20315#) (str/upper-case (format "%02x" p1__20315#)))))
       (range 256))))
  (defn missing-seg-ids
    ([backup_storage seg_id_set]
      (when-not (set? seg_id_set)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'set? 'seg-id-set))))))
      (let [value_substorage (substorage backup_storage "values")
            map__20319 (queue/queue-seq (long (* (* (count prefixes) 1000) 10)))
            map__20319 (if (seq? map__20319)
                         (clojure.lang.PersistentHashMap/create (seq map__20319))
                         map__20319)
            fill (get map__20319 :fill)
            done (get map__20319 :done)
            drain (get map__20319 :drain)
            ms_fut (future-call
                     (fn fn__20320
                       ([]
                         (reduce
                           (fn fn__20321 ([acc k] (disj acc k)))
                           seg_id_set
                           (^clojure.lang.IFn drain)))))]
        (let [futs (mapv
                     (fn fn__20324
                       ([prefix]
                         (common/pfuture
                           (deref thread-pool)
                           (fn fn__20325
                             ([]
                               (let [m_20326 {:event :verify/list-prefix, :prefix prefix}
                                     ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.backup")]
                                                       (when (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.info
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_20326 :phase :begin)))
                                                         nil)
                                                       nil)
                                     start__8981__auto__ (java.lang.System/nanoTime)
                                     result__8982__auto__ (try
                                                            {:returned
                                                             (run!
                                                               fill
                                                               (filter
                                                                 (fn 
                                                                   fn__20330
                                                                   ([p1__20318#]
                                                                     (contains?
                                                                       seg_id_set
                                                                       p1__20318#)))
                                                                 (:ks
                                                                   (list-keys
                                                                     (substorage
                                                                       value_substorage
                                                                       prefix)
                                                                     ""))))}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8983__auto__
                                                              {:threw t__8983__auto__}))
                                     elapsed_20327 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8981__auto__)
                                     msec_20328 (logger/format-as-msec (long elapsed_20327))]
                                 (let [endmsg__8984__auto__ (merge
                                                              (assoc
                                                                m_20326
                                                                :msec
                                                                msec_20328
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8982__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8982__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                                   (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                     (.info
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8984__auto__))
                                     nil)
                                   nil)
                                 (if (contains? result__8982__auto__ :returned)
                                   (:returned result__8982__auto__)
                                   (do (throw (:threw result__8982__auto__)) nil))))))))
                     prefixes)]
          (loop [seq_20339 (seq futs) chunk_20340 nil count_20341 0 i_20342 0]
            (if (< i_20342 count_20341)
              (let [fut (.nth ^clojure.lang.Indexed chunk_20340 (int i_20342))]
                (deref fut)
                (recur seq_20339 chunk_20340 count_20341 (inc i_20342)))
              (let [temp__5457__auto__ (seq seq_20339)]
                (when temp__5457__auto__
                  (let [seq_20339 temp__5457__auto__]
                    (if (chunked-seq? seq_20339)
                      (let [c__5719__auto__ (chunk-first seq_20339)]
                        (recur
                          (chunk-rest seq_20339)
                          c__5719__auto__
                          (int (count c__5719__auto__))
                          (int 0)))
                      (let [fut (first seq_20339)]
                        (deref fut)
                        (recur (next seq_20339) nil 0 0))))))))
          (^clojure.lang.IFn done))
        (deref ms_fut))))
  (defn unreadable-seg-ids
    ([lookup seg_ids]
      (let [unreadable? (fn unreadable_QMARK_
                          ([p1__20346#]
                            (try
                              (do (get lookup p1__20346#) false)
                              (catch java.lang.Throwable _ true))))]
        (keep
          deref
          (seque
            pool-size
            (map
              (fn fn__20349
                ([k]
                  (common/pfuture
                    (deref thread-pool)
                    (fn fn__20350
                      ([]
                        (when (^clojure.lang.IFn unreadable? k)
                          (common/log-and-print {:event :verify-backup/unreadable-segment, :k k})
                          k))))))
              seg_ids))))))
  (defn verify-backup
    ([p__20354]
      (let [map__20355 p__20354
            map__20355 (if (seq? map__20355)
                         (clojure.lang.PersistentHashMap/create (seq map__20355))
                         map__20355)
            backup_uri (get map__20355 :backup-uri)
            t (get map__20355 :t)
            read_all (get map__20355 :read-all)
            backup_storage (create-storage backup_uri nil)
            map__20356 (or
                         (read-roots t backup_storage)
                         (error/raise :verify/roots-missing (str "No database root for t " t)))
            map__20356 (if (seq? map__20356)
                         (clojure.lang.PersistentHashMap/create (seq map__20356))
                         map__20356)
            version (get map__20356 :backup/version)
            _ (when (< version 3)
                (throw
                  (java.lang.RuntimeException. "Verify not supported for pre-2015 backup format."))
                nil)
            seg_id_set (into #{} (backup-seg-ids backup_storage t))
            missing (missing-seg-ids backup_storage seg_id_set)
            segcount (count seg_id_set)
            progress (fn progress
                       ([x]
                         (when (= (mod x 1000) 0)
                           (println
                             (format "%s of %s" x (java.lang.Integer/valueOf (int segcount)))))))]
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
                   (fn fn__20359 ([idx arg] (^clojure.lang.IFn progress idx) arg))
                   seg_id_set)))}))))))