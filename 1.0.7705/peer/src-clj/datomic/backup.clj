(do
  (clojure.core/in-ns 'datomic.backup)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.backup)
    {:doc
     "Differential backup, point-in-time restore, backup discovery, direct backup reads, and integrity verification. A backup location is claimed by one database identity and stores immutable segments shared by successive snapshots."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :as 'async]
        ['clojure.set :as 'set]
        ['cognitect.anomalies :as-alias 'anom]
        ['datomic.db :as 'db]
        ['datomic.cache :as 'cache]
        ['datomic.cluster-stack :as 'cluster-stack]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.core2.anomalizer :as 'izer]
        ['datomic.core2.val-store :as 'vs]
        ['datomic.core2.val-store.spi :as 'vsspi]
        ['datomic.core2.val-store.double-store :as 'double-store]
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
      (clojure.core/import 'java.util.concurrent.Semaphore)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.net.URI)))
  (when-not (.equals 'datomic.backup 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.backup))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :as 'async]
          ['clojure.set :as 'set]
          ['cognitect.anomalies :as-alias 'anom]
          ['datomic.db :as 'db]
          ['datomic.cache :as 'cache]
          ['datomic.cluster-stack :as 'cluster-stack]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.core2.anomalizer :as 'izer]
          ['datomic.core2.val-store :as 'vs]
          ['datomic.core2.val-store.spi :as 'vsspi]
          ['datomic.core2.val-store.double-store :as 'double-store]
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
  (let [protocol_metadata__7463 {:column (int 1)}]
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
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Storage :ns *ns*))
    (let [protocol_signature__7464 (assoc
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
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "store")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'list-keys
                                        {:arglists (clojure.core/list ['_ 'prefix])}),
                                      :arglists (clojure.core/list ['_ 'prefix]),
                                      :doc "Returns {:ks keys} on success."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "Storage"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "list-keys")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*)))
    (let [protocol_signature__7468 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'exists? {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc "Returns a boolean"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "Storage"))
          protocol_method_name__7469 (with-meta
                                       (:name protocol_signature__7468)
                                       protocol_signature__7468)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "exists?")
        (assoc protocol_signature__7468 :name protocol_method_name__7469 :ns *ns*)))
    (let [protocol_signature__7470 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'retrieve
                                        {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc "Returns {:v buf} if path exists."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "Storage"))
          protocol_method_name__7471 (with-meta
                                       (:name protocol_signature__7470)
                                       protocol_signature__7470)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "retrieve")
        (assoc protocol_signature__7470 :name protocol_method_name__7471 :ns *ns*))))
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
  (defn backup-k-factory
    ([^long backup_version]
      (let [G__21315 backup_version] (case G__21315 (1 2) identity 3 add-key-prefix))))
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
            (fn fn__21319 ([p1__21317#] (map (partial strip-prefix prefix) p1__21317#))))
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
          (let [map__21327 temp__5804__auto__
                map__21327 (if (seq? map__21327)
                             (if (next map__21327)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__21327))
                               (if (seq map__21327) (first map__21327) {}))
                             map__21327)
                v (get map__21327 :v)]
            (io/bbuf->string v))))))
  (reset-meta!
    #'claimed-by
    (assoc {:arglists (clojure.core/list ['storage]), :column (int 1)} :name 'claimed-by :ns *ns*))
  ;; Claims an empty backup location for one database identity and rejects cross-database reuse.
  ;; ATOMIC-NOTE [observed/adaptation] Repository ownership prevents cross-lineage reuse; this read/write/read
  ;; check is not an exclusive backup writer lock. Native repository::claim
  ;; uses no-clobber publication and authenticates the retained owner.
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
  (defn ->connect-uri ([backup-uri t] (str "datomic:backup:" backup-uri (when t (str "?t=" t)))))
  (reset-meta!
    #'->connect-uri
    (assoc
      {:arglists (clojure.core/list ['backup-uri 't]), :column (int 1)}
      :name
      '->connect-uri
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
          (fn fn__21334 ([uri & _] (.getScheme ^java.net.URI uri)))
          :default
          #'clojure.core/global-hierarchy))
      #'create-storage*))
  (defmethod
    create-storage*
    "file"
    fn__21339
    ([uri sse?]
      (when sse?
        (error/arg
          :storage/sse-not-available
          "Server side encryption not available for file storage"))
      (req/require-and-run 'datomic.fsbackup/storage-from-uri uri)))
  (defmethod
    create-storage*
    "s3"
    fn__21341
    ([uri sse?] (req/require-and-run 'datomic.s3backup/storage-from-uri uri sse?)))
  (defmethod
    create-storage*
    :default
    fn__21343
    ([uri sse?]
      (error/arg
        :storage/invalid-uri
        (str "Unsupported protocol: " (.getScheme ^java.net.URI uri)))))
  (defn create-storage
    ([storage_uri sse?] (create-storage* (io/as-uri storage_uri) sse?))
    ([storage_uri] (create-storage storage_uri false)))
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
        (fn fn__21348 ([p1__21346#] (instance? java.lang.Throwable p1__21346#)))
        :backoff
        (fn fn__21350
          ([p1__21347#]
            (java.lang.Double/valueOf
              (double
                (*
                  (+ 50 (rand-int 50))
                  (java.lang.Math/pow (double 2) (double ^java.lang.Number p1__21347#)))))))
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
          (let [ret (retry (fn fn__21354 ([] (retrieve storage k))))] (if ret (:v ret) not_found)))
        (valAt [this k] (.valAt this k nil)))))
  (reset-meta!
    #'uncached-storage-lookup
    (assoc
      {:arglists (clojure.core/list ['storage]), :column (int 1)}
      :name
      'uncached-storage-lookup
      :ns
      *ns*))
  (defn storage-olookup
    ([storage backup_version]
      (cache/lookup-transformer
        (domain/peer-object-lookup
          (uncached-storage-lookup storage)
          domain/common-read-handlers
          (domain/system-cache))
        :key-fn
        (comp (backup-k-factory (long ^java.lang.Number backup_version)) cluster/uuid->val-key))))
  (reset-meta!
    #'storage-olookup
    (assoc
      {:arglists (clojure.core/list ['storage 'backup-version]), :column (int 1)}
      :name
      'storage-olookup
      :ns
      *ns*))
  (defn val-store-on-backup
    ([storage backup_version]
      (let [kf (backup-k-factory (long ^java.lang.Number backup_version))]
        (reify
          datomic.core2.val_store.spi.Get
          datomic.core2.val_store.spi.Delete
          datomic.core2.val_store.spi.Put
          (-delete
            [this k opts]
            (let [G__21367 (async/chan 1)]
              (async/>!!
                G__21367
                #:cognitect.anomalies{:category :cognitect.anomalies/unsupported,
                                      :message "Backup storage does not support delete"})
              G__21367))
          (-put
            [this k v opts]
            (let [G__21366 (async/chan 1)]
              (async/>!!
                G__21366
                #:cognitect.anomalies{:category :cognitect.anomalies/unsupported,
                                      :message "Backup storage does not support put"})
              G__21366))
          (-get
            [this k opts]
            (async/thread-call
              (fn fn__21360
                ([]
                  (try
                    (let [temp__5802__auto__ (retry
                                               (fn fn__21361
                                                 ([]
                                                   (retrieve storage (^clojure.lang.IFn kf k)))))]
                      (if temp__5802__auto__
                        (let [map__21363 temp__5802__auto__
                              map__21363 (if (seq? map__21363)
                                           (if (next map__21363)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__21363))
                                             (if (seq map__21363) (first map__21363) {}))
                                           map__21363)
                              v (get map__21363 :v)]
                          {:val v})
                        {:cognitect.anomalies/category :cognitect.anomalies/not-found,
                         :cognitect.anomalies/message "Backup cannot find key",
                         :key k}))
                    (catch java.lang.Throwable t (izer/throwable->anom t)))))
              :io))))))
  (reset-meta!
    #'val-store-on-backup
    (assoc
      {:arglists (clojure.core/list ['storage 'backup-version]), :column (int 1)}
      :name
      'val-store-on-backup
      :ns
      *ns*))
  (defn lookup-over-vs
    ([val_store]
      (reify
        clojure.lang.ILookup
        (valAt
          [this k not_found]
          (let [map__21371 (async/<!! (vs/get val_store k))
                map__21371 (if (seq? map__21371)
                             (if (next map__21371)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__21371))
                               (if (seq map__21371) (first map__21371) {}))
                             map__21371)
                ret map__21371
                val (get map__21371 :val)]
            (or val (common/throw-anom ret))))
        (valAt [this k] (.valAt this k nil)))))
  (reset-meta!
    #'lookup-over-vs
    (assoc
      {:arglists (clojure.core/list ['val-store]), :column (int 1)}
      :name
      'lookup-over-vs
      :ns
      *ns*))
  (defn caching-olookup
    ([storage backup_version]
      (let [vs (val-store-on-backup storage backup_version)
            vs (let [temp__5802__auto__ (deref cluster-stack/kv-cache-ref)]
                 (if temp__5802__auto__
                   (let [kvc temp__5802__auto__]
                     (double-store/create {:near-store kvc, :far-store vs}))
                   vs))]
        (domain/peer-object-lookup
          (lookup-over-vs vs)
          domain/common-read-handlers
          (domain/system-cache)))))
  (reset-meta!
    #'caching-olookup
    (assoc
      {:arglists (clojure.core/list ['storage 'backup-version]), :column (int 1)}
      :name
      'caching-olookup
      :ns
      *ns*))
  (let [protocol_metadata__7472 {:column (int 1)}]
    (defprotocol
      IValueBackup
      (backup-val [_ k backup-k] "Backup a segment from storage k to backup backup-k.")
      (backup-node [_ node] "Backup a node's children, then the node."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.backup" "IValueBackup")
      (assoc (assoc protocol_metadata__7472 :doc nil) :name 'IValueBackup :ns *ns*))
    (let [protocol_signature__7473 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'backup-val
                                        {:arglists (clojure.core/list ['_ 'k 'backup-k])}),
                                      :arglists (clojure.core/list ['_ 'k 'backup-k]),
                                      :doc "Backup a segment from storage k to backup backup-k."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "IValueBackup"))
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "backup-val")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*)))
    (let [protocol_signature__7475 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'backup-node
                                        {:arglists (clojure.core/list ['_ 'node])}),
                                      :arglists (clojure.core/list ['_ 'node]),
                                      :doc "Backup a node's children, then the node."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "IValueBackup"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "backup-node")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*))))
  ;; ATOMIC-NOTE [observed/adaptation] Child work is awaited before copying this parent. Incremental subtree
  ;; skips rely on prior child-closed copies. The semaphore bounds active
  ;; value transfers, not total traversal memory; native capture authenticates
  ;; reused objects and publishes the manifest only after the graph is copied.
  (deftype
    ValueBackup
    [from_cluster value_storage progress incremental? ids_>nodes throttle sem]
    datomic.backup.IValueBackup
    (backup-node
      [this node]
      (let [k (treewalk/node-id node)
            backup_k (add-key-prefix k)
            node_exists? (retry (fn fn__21422 ([] (exists? value_storage backup_k))))]
        (when-not (and node_exists? incremental?)
          (let [temp__5802__auto__ (treewalk/subtrees node ids_>nodes)]
            (if temp__5802__auto__
              (let [branch_nodes temp__5802__auto__
                    futs (mapv
                           (fn fn__21424
                             ([branch]
                               (common/pfuture
                                 (deref backup-branch-pool)
                                 (fn fn__21425 ([] (backup-node this branch))))))
                           branch_nodes)]
                (loop [seq_21428 (seq futs) chunk_21429 nil count_21430 0 i_21431 0]
                  (if (< i_21431 count_21430)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_21429 (int i_21431))]
                      (deref fut)
                      (recur seq_21428 chunk_21429 count_21430 (inc i_21431)))
                    (let [temp__5804__auto__ (seq seq_21428)]
                      (when temp__5804__auto__
                        (let [seq_21428 temp__5804__auto__]
                          (if (chunked-seq? seq_21428)
                            (let [c__6065__auto__ (chunk-first seq_21428)]
                              (recur
                                (chunk-rest seq_21428)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [fut (first seq_21428)]
                              (deref fut)
                              (recur (next seq_21428) nil 0 0)))))))))
              (let [futs (mapv
                           (fn fn__21432
                             ([leaf_id]
                               (when throttle (^clojure.lang.IFn throttle))
                               (common/pfuture
                                 (deref thread-pool)
                                 (fn fn__21433
                                   ([]
                                     (let [backup_leaf_id (add-key-prefix leaf_id)]
                                       (if (retry
                                             (fn fn__21434
                                               ([] (exists? value_storage backup_leaf_id))))
                                         (^clojure.lang.IFn progress :skipped)
                                         (backup-val this leaf_id backup_leaf_id))))))))
                           (treewalk/child-node-ids node))]
                (loop [seq_21438 (seq futs) chunk_21439 nil count_21440 0 i_21441 0]
                  (if (< i_21441 count_21440)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_21439 (int i_21441))]
                      (deref fut)
                      (recur seq_21438 chunk_21439 count_21440 (inc i_21441)))
                    (let [temp__5804__auto__ (seq seq_21438)]
                      (when temp__5804__auto__
                        (let [seq_21438 temp__5804__auto__]
                          (if (chunked-seq? seq_21438)
                            (let [c__6065__auto__ (chunk-first seq_21438)]
                              (recur
                                (chunk-rest seq_21438)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [fut (first seq_21438)]
                              (deref fut)
                              (recur (next seq_21438) nil 0 0))))))))))))
        (if node_exists? (^clojure.lang.IFn progress :skipped) (backup-val this k backup_k))))
    (backup-val
      [this k backup_k]
      (do
        (.acquire ^java.util.concurrent.Semaphore sem)
        (try
          (let [m_21413 {:event :backup/segment, :k k}
                ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process (assoc m_21413 :phase :begin))))
                                  nil)
                start__8553__auto__ (java.lang.System/nanoTime)
                result__8554__auto__ (try
                                       {:returned
                                        (let [temp__5802__auto__ (deref
                                                                   (cluster/get-val
                                                                     from_cluster
                                                                     k))]
                                          (if temp__5802__auto__
                                            (let [map__21417 temp__5802__auto__
                                                  map__21417 (if
                                                               (seq? map__21417)
                                                               (if
                                                                 (next map__21417)
                                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                   (to-array map__21417))
                                                                 (if
                                                                   (seq map__21417)
                                                                   (first map__21417)
                                                                   {}))
                                                               map__21417)
                                                  buf (get map__21417 :buf)
                                                  result (retry
                                                           (fn
                                                             fn__21418
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
                                         t__8555__auto__
                                         {:threw t__8555__auto__}))
                elapsed_21414 (- (java.lang.System/nanoTime) start__8553__auto__)
                msec_21415 (logger/format-as-msec (long elapsed_21414))]
            (let [endmsg__8556__auto__ (merge
                                         (assoc m_21413 :msec msec_21415 :phase :end)
                                         (when (:threw result__8554__auto__)
                                           {:threw (class (:threw result__8554__auto__))}))
                  logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
              nil)
            (if (contains? result__8554__auto__ :returned)
              (:returned result__8554__auto__)
              (do (throw (:threw result__8554__auto__)) nil)))
          (finally (.release ^java.util.concurrent.Semaphore sem))))))
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
  (defn create-value-backup
    ([& p__21456]
      (let [map__21457 p__21456
            map__21457 (if (seq? map__21457)
                         (if (next map__21457)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21457))
                           (if (seq map__21457) (first map__21457) {}))
                         map__21457)
            from_cluster (get map__21457 :from-cluster)
            to_storage (get map__21457 :to-storage)
            progress (get map__21457 :progress)
            incremental? (get map__21457 :incremental?)
            ids_>nodes (get map__21457 :ids->nodes)
            concurrency (get map__21457 :concurrency)]
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
                (fn fn__21458 ([] (java.lang.Thread/sleep (long ^java.lang.Number pace)) nil)))))
          (java.util.concurrent.Semaphore. (int ^java.lang.Number concurrency))))))
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
  (let [protocol_metadata__7477 {:column (int 1)}]
    (defprotocol
      IValueRestore
      (restore-val [_ k] "Restore segment with key k from backup to storage.")
      (restore-node [_ node] "Restores a node's children, then the node"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.backup" "IValueRestore")
      (assoc (assoc protocol_metadata__7477 :doc nil) :name 'IValueRestore :ns *ns*))
    (let [protocol_signature__7478 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'restore-val
                                        {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc "Restore segment with key k from backup to storage."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "IValueRestore"))
          protocol_method_name__7479 (with-meta
                                       (:name protocol_signature__7478)
                                       protocol_signature__7478)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "restore-val")
        (assoc protocol_signature__7478 :name protocol_method_name__7479 :ns *ns*)))
    (let [protocol_signature__7480 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'restore-node
                                        {:arglists (clojure.core/list ['_ 'node])}),
                                      :arglists (clojure.core/list ['_ 'node]),
                                      :doc "Restores a node's children, then the node"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.backup" "IValueRestore"))
          protocol_method_name__7481 (with-meta
                                       (:name protocol_signature__7480)
                                       protocol_signature__7480)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.backup" "restore-node")
        (assoc protocol_signature__7480 :name protocol_method_name__7481 :ns *ns*))))
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
                           (fn fn__21505
                             ([branch]
                               (common/pfuture
                                 (deref backup-branch-pool)
                                 (fn fn__21506 ([] (restore-node this branch))))))
                           branch_nodes)]
                (loop [seq_21509 (seq futs) chunk_21510 nil count_21511 0 i_21512 0]
                  (if (< i_21512 count_21511)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_21510 (int i_21512))]
                      (deref fut)
                      (recur seq_21509 chunk_21510 count_21511 (inc i_21512)))
                    (let [temp__5804__auto__ (seq seq_21509)]
                      (when temp__5804__auto__
                        (let [seq_21509 temp__5804__auto__]
                          (if (chunked-seq? seq_21509)
                            (let [c__6065__auto__ (chunk-first seq_21509)]
                              (recur
                                (chunk-rest seq_21509)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [fut (first seq_21509)]
                              (deref fut)
                              (recur (next seq_21509) nil 0 0)))))))))
              (let [futs (mapv
                           (fn fn__21513
                             ([leaf_id]
                               (common/pfuture
                                 (deref thread-pool)
                                 (fn fn__21514
                                   ([]
                                     (if (deref
                                           (cluster/get-val2
                                             to_cluster
                                             leaf_id
                                             #:datomic.core2.val-store.opts{:skip-cache true}))
                                       (^clojure.lang.IFn progress :skipped)
                                       (restore-val this leaf_id)))))))
                           (treewalk/child-node-ids node))]
                (loop [seq_21517 (seq futs) chunk_21518 nil count_21519 0 i_21520 0]
                  (if (< i_21520 count_21519)
                    (let [fut (.nth ^clojure.lang.Indexed chunk_21518 (int i_21520))]
                      (deref fut)
                      (recur seq_21517 chunk_21518 count_21519 (inc i_21520)))
                    (let [temp__5804__auto__ (seq seq_21517)]
                      (when temp__5804__auto__
                        (let [seq_21517 temp__5804__auto__]
                          (if (chunked-seq? seq_21517)
                            (let [c__6065__auto__ (chunk-first seq_21517)]
                              (recur
                                (chunk-rest seq_21517)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [fut (first seq_21517)]
                              (deref fut)
                              (recur (next seq_21517) nil 0 0))))))))))))
        (if cluster_v (^clojure.lang.IFn progress :skipped) (restore-val this k))))
    (restore-val
      [this k]
      (do
        (.acquire ^java.util.concurrent.Semaphore sem)
        (try
          (let [m_21496 {:event :restore/segment, :k k}
                ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process (assoc m_21496 :phase :begin))))
                                  nil)
                start__8553__auto__ (java.lang.System/nanoTime)
                result__8554__auto__ (try
                                       {:returned
                                        (let [temp__5802__auto__ (retry
                                                                   (fn
                                                                     fn__21500
                                                                     ([]
                                                                       (retrieve
                                                                         value_storage
                                                                         (^clojure.lang.IFn k_>backup_k
                                                                           k)))))]
                                          (if temp__5802__auto__
                                            (let [map__21502 temp__5802__auto__
                                                  map__21502 (if
                                                               (seq? map__21502)
                                                               (if
                                                                 (next map__21502)
                                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                   (to-array map__21502))
                                                                 (if
                                                                   (seq map__21502)
                                                                   (first map__21502)
                                                                   {}))
                                                               map__21502)
                                                  v (get map__21502 :v)]
                                              (deref (cluster/create-val to_cluster k v))
                                              (^clojure.lang.IFn progress :copied))
                                            (error/raise
                                              :restore/read-failed
                                              (str "Unable to read " k)
                                              {:key k})))}
                                       (catch
                                         java.lang.Throwable
                                         t__8555__auto__
                                         {:threw t__8555__auto__}))
                elapsed_21497 (- (java.lang.System/nanoTime) start__8553__auto__)
                msec_21498 (logger/format-as-msec (long elapsed_21497))]
            (let [endmsg__8556__auto__ (merge
                                         (assoc m_21496 :msec msec_21498 :phase :end)
                                         (when (:threw result__8554__auto__)
                                           {:threw (class (:threw result__8554__auto__))}))
                  logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
              nil)
            (if (contains? result__8554__auto__ :returned)
              (:returned result__8554__auto__)
              (do (throw (:threw result__8554__auto__)) nil)))
          (finally (.release ^java.util.concurrent.Semaphore sem))))))
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
  (defn create-value-restore
    ([& p__21535]
      (let [map__21536 p__21535
            map__21536 (if (seq? map__21536)
                         (if (next map__21536)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21536))
                           (if (seq map__21536) (first map__21536) {}))
                         map__21536)
            from_storage (get map__21536 :from-storage)
            to_cluster (get map__21536 :to-cluster)
            backup_version (get map__21536 :backup-version)
            progress (get map__21536 :progress)
            incremental? (get map__21536 :incremental?)
            concurrency (get map__21536 :concurrency)
            ids_>nodes (get map__21536 :ids->nodes)]
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
          (let [G__21540 version]
            (case
              G__21540
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
  ;; Publishes the snapshot root descriptor after all referenced immutable values have been copied.
  (defn backup-roots
    ([job t to_storage]
      (store
        to_storage
        (roots-path t)
        (fressian/byte-buf (mem->backup job) :handlers fressian/user-write-handlers))))
  (reset-meta!
    #'backup-roots
    (assoc
      {:arglists (clojure.core/list ['job 't 'to-storage]), :column (int 1)}
      :name
      'backup-roots
      :ns
      *ns*))
  (defn read-roots
    ([t from_storage]
      (let [temp__5804__auto__ (:v (retrieve from_storage (roots-path t)))]
        (when temp__5804__auto__
          (let [buf temp__5804__auto__]
            (backup->mem (fressian/defressian buf :handlers fressian/user-read-handlers)))))))
  (reset-meta!
    #'read-roots
    (assoc
      {:arglists (clojure.core/list ['t 'from-storage]), :column (int 1)}
      :name
      'read-roots
      :ns
      *ns*))
  (defn list-roots
    ([storage]
      (sort
        >
        (mapv
          (fn fn__21547
            ([p1__21546#] (long (java.lang.Long/parseLong ^java.lang.String p1__21546#))))
          (filter
            (fn fn__21549 ([p1__21545#] (re-matches #"\d+" p1__21545#)))
            (:ks (list-keys (substorage storage "roots") "")))))))
  (reset-meta!
    #'list-roots
    (assoc {:arglists (clojure.core/list ['storage]), :column (int 1)} :name 'list-roots :ns *ns*))
  (defn describe-backups
    ([storage]
      (let [storage (if (string? storage) (create-storage storage) storage)]
        {:db-id (claimed-by storage), :ts (list-roots storage)})))
  (reset-meta!
    #'describe-backups
    (assoc
      {:arglists (clojure.core/list ['storage]), :column (int 1)}
      :name
      'describe-backups
      :ns
      *ns*))
  ;; Returns available snapshots newest first, with a directly connectable URI for each t.
  (defn list-backups
    ([backup-uri]
      (let [storage (create-storage backup-uri) ts (list-roots storage)]
        {:backups
         (mapv (fn fn__21553 ([t] {:t t, :connect-uri (->connect-uri backup-uri t)})) ts)})))
  (reset-meta!
    #'list-backups
    (assoc
      {:arglists (clojure.core/list ['backup-uri]), :column (int 1)}
      :name
      'list-backups
      :ns
      *ns*))
  ;; Installs the restored index and log roots only after their immutable values are available.
  ;; ATOMIC-NOTE [observed/adaptation] Index reset and tail reset are separate publications here. Pro restore
  ;; requires stopped live users; this is not Atomic's guarded activation of
  ;; root, lease, completion and checkpoint in one batch. Retain that native
  ;; protocol rather than copying these separate publication steps.
  (defn restore-roots
    ([job cluster]
      (let [map__21556 job
            map__21556 (if (seq? map__21556)
                         (if (next map__21556)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21556))
                           (if (seq map__21556) (first map__21556) {}))
                         map__21556)
            index_root_id (get map__21556 :index-root-id)
            log_root_id (get map__21556 :log-root-id)
            log_tail (get map__21556 :log-tail)]
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
  ;; Captures one consistent index root, log root, and log tail from a live database.
  ;; ATOMIC-NOTE [observed/adaptation] Tail descriptor/data are captured first; the index reference is read
  ;; separately. Immutable selected values do not establish an atomic
  ;; multi-reference capture. Native backup/capture reads one guarded
  ;; DatabaseRoot containing both coordinates.
  (defn create-backup-job
    ([cluster lookup]
      (let [vec__21559 (log/read-tail-descriptor cluster)
            desc (nth vec__21559 (int 0) nil)
            buf (nth vec__21559 (int 1) nil)
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
                            (fn fn__21562
                              ([p1__21558#] (treewalk/log-root-walker p1__21558# lookup false)))
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
            map__21566 roots
            map__21566 (if (seq? map__21566)
                         (if (next map__21566)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21566))
                           (if (seq map__21566) (first map__21566) {}))
                         map__21566)
            index_root_id (get map__21566 :index-root-id)
            log_root_id (get map__21566 :log-root-id)]
        (assoc
          roots
          :lookup
          lookup
          :index-top-node
          (treewalk/create-parent-node index_root_id treewalk/index-top-walker lookup)
          :log-root-node
          (treewalk/create-parent-node
            log_root_id
            (fn fn__21567 ([p1__21565#] (treewalk/log-root-walker p1__21565# lookup false)))
            lookup)))))
  (reset-meta!
    #'create-restore-job
    (assoc
      {:arglists (clojure.core/list ['storage 't]), :column (int 1)}
      :name
      'create-restore-job
      :ns
      *ns*))
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
      (let [map__21572 (uri/parse-db uri)
            map__21572 (if (seq? map__21572)
                         (if (next map__21572)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21572))
                           (if (seq map__21572) (first map__21572) {}))
                         map__21572)
            cluster_conf map__21572
            db_name (get map__21572 :db-name)
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
      (let [map__21575 (describe-backups uri)
            map__21575 (if (seq? map__21575)
                         (if (next map__21575)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21575))
                           (if (seq map__21575) (first map__21575) {}))
                         map__21575)
            ts (get map__21575 :ts)]
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
  ;; Copies all values reachable from one backup root before publishing that root in the target.
  (defn restore-db
    ([p__21579 p__21580 progress concurrency incremental?]
      (let [map__21581 p__21579
            map__21581 (if (seq? map__21581)
                         (if (next map__21581)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21581))
                           (if (seq map__21581) (first map__21581) {}))
                         map__21581)
            from_storage (get map__21581 :from-storage)
            t (get map__21581 :t)
            map__21582 p__21580
            map__21582 (if (seq? map__21582)
                         (if (next map__21582)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21582))
                           (if (seq map__21582) (first map__21582) {}))
                         map__21582)
            to_uri (get map__21582 :to-uri)
            job (create-restore-job from_storage t)
            backup_version (:backup/version job)
            map__21583 job
            map__21583 (if (seq? map__21583)
                         (if (next map__21583)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21583))
                           (if (seq map__21583) (first map__21583) {}))
                         map__21583)
            index_top_node (get map__21583 :index-top-node)
            log_root_node (get map__21583 :log-root-node)
            db_id (get map__21583 :db-id)
            lookup (get map__21583 :lookup)
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
          (fn fn__21584
            ([]
              (try
                (let [m_21585 {:event :restore/db, :t t, :db-id db_id}
                      ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.backup")]
                                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                          (.info
                                            ^org.slf4j.Logger logger
                                            (logger/process (assoc m_21585 :phase :begin))))
                                        nil)
                      start__8553__auto__ (java.lang.System/nanoTime)
                      result__8554__auto__ (try
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
                                               t__8555__auto__
                                               {:threw t__8555__auto__}))
                      elapsed_21586 (- (java.lang.System/nanoTime) start__8553__auto__)
                      msec_21587 (logger/format-as-msec (long elapsed_21586))]
                  (let [endmsg__8556__auto__ (merge
                                               (assoc m_21585 :msec msec_21587 :phase :end)
                                               (when (:threw result__8554__auto__)
                                                 {:threw (class (:threw result__8554__auto__))}))
                        logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                      (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                    nil)
                  (if (contains? result__8554__auto__ :returned)
                    (:returned result__8554__auto__)
                    (do (throw (:threw result__8554__auto__)) nil)))
                (catch
                  java.lang.Throwable
                  t__8798__auto__
                  (do
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")
                          ex t__8798__auto__]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn
                          ^org.slf4j.Logger logger
                          (logger/process "error executing future")
                          ^java.lang.Throwable ex)
                        (logger/caused-by logger ex))
                      nil)
                    (monitor/alarm :UnhandledException)
                    (throw ^java.lang.Throwable t__8798__auto__)
                    nil)))))))))
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
  (defn backup-key->seg-id
    ([s]
      (let [idx (.lastIndexOf ^java.lang.String s "/")]
        (if (<= 0 idx) (subs s (long (inc idx)) (java.lang.Integer/valueOf (int (count s)))) s))))
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
  (defn ->SegSetStorage ([storage seg_id_set] (datomic.backup.SegSetStorage. storage seg_id_set)))
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
  (defn backup-seg-ids
    ([backup_storage t]
      (let [map__21621 (create-restore-job backup_storage t)
            map__21621 (if (seq? map__21621)
                         (if (next map__21621)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21621))
                           (if (seq map__21621) (first map__21621) {}))
                         map__21621)
            lookup (get map__21621 :lookup)
            index_top_node (get map__21621 :index-top-node)
            log_root_node (get map__21621 :log-root-node)]
        (treewalk/db-seq log_root_node index_top_node lookup))))
  (reset-meta!
    #'backup-seg-ids
    (assoc
      {:arglists (clojure.core/list ['backup-storage 't]), :column (int 1)}
      :name
      'backup-seg-ids
      :ns
      *ns*))
  (defn segset-storage
    ([backup_storage seg_id_set]
      (when seg_id_set
        (when-not (set? seg_id_set)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list 'set? 'seg-id-set)))))))
      (->SegSetStorage backup_storage seg_id_set)))
  (reset-meta!
    #'segset-storage
    (assoc
      {:arglists (clojure.core/list ['backup-storage 'seg-id-set]), :column (int 1)}
      :name
      'segset-storage
      :ns
      *ns*))
  (defn maybe-segset-storage
    ([backup_storage]
      (if (config/property "datomic.backupUseSegsetStorage")
        (segset-storage
          backup_storage
          (let [temp__5804__auto__ (latest-t backup_storage)]
            (when temp__5804__auto__
              (let [prior_backup_t temp__5804__auto__]
                (into #{} (backup-seg-ids backup_storage prior_backup_t))))))
        backup_storage)))
  (reset-meta!
    #'maybe-segset-storage
    (assoc
      {:arglists (clojure.core/list ['backup-storage]), :column (int 1)}
      :name
      'maybe-segset-storage
      :ns
      *ns*))
  ;; Copies values reachable from a live basis and writes the root descriptor last. Existing values
  ;; in a repeatedly used backup location are skipped, making later snapshots differential.
  ;; ATOMIC-NOTE [observed/adaptation] Both immutable trees finish before the roots record advertises this
  ;; point. Interrupted copies may leave reusable values, not completed points.
  ;; See 08_operations/01_capacity_and_reliability/02_backup_and_restore.atomic.md
  ;; for native backup/{capture,repository,restore,verify} owners and bounds.
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
            map__21626 job
            map__21626 (if (seq? map__21626)
                         (if (next map__21626)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21626))
                           (if (seq map__21626) (first map__21626) {}))
                         map__21626)
            index_top_node (get map__21626 :index-top-node)
            log_root_node (get map__21626 :log-root-node)
            db_id (get map__21626 :db-id)
            t (get map__21626 :t)
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
          (fn fn__21627
            ([]
              (try
                (let [m_21628 {:event :backup/db, :t t, :db-id (:db-id job)}
                      ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.backup")]
                                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                          (.info
                                            ^org.slf4j.Logger logger
                                            (logger/process (assoc m_21628 :phase :begin))))
                                        nil)
                      start__8553__auto__ (java.lang.System/nanoTime)
                      result__8554__auto__ (try
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
                                               t__8555__auto__
                                               {:threw t__8555__auto__}))
                      elapsed_21629 (- (java.lang.System/nanoTime) start__8553__auto__)
                      msec_21630 (logger/format-as-msec (long elapsed_21629))]
                  (let [endmsg__8556__auto__ (merge
                                               (assoc m_21628 :msec msec_21630 :phase :end)
                                               (when (:threw result__8554__auto__)
                                                 {:threw (class (:threw result__8554__auto__))}))
                        logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                      (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                    nil)
                  (if (contains? result__8554__auto__ :returned)
                    (:returned result__8554__auto__)
                    (do (throw (:threw result__8554__auto__)) nil)))
                (catch
                  java.lang.Throwable
                  t__8798__auto__
                  (do
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")
                          ex t__8798__auto__]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn
                          ^org.slf4j.Logger logger
                          (logger/process "error executing future")
                          ^java.lang.Throwable ex)
                        (logger/caused-by logger ex))
                      nil)
                    (monitor/alarm :UnhandledException)
                    (throw ^java.lang.Throwable t__8798__auto__)
                    nil)))))))))
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
      (let [scheme (.getScheme (io/as-uri uri)) G__21640 scheme]
        (case
          G__21640
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
  ;; Starts a differential backup and returns a future reporting :succeeded or throwing on failure.
  (defn backup
    ([from-conn-uri to-storage-uri sse? progress incremental?]
      (let [storage (create-storage to-storage-uri sse?)]
        (backup-db
          from-conn-uri
          storage
          progress
          (backup-concurrency to-storage-uri)
          incremental?))))
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
  ;; Restores the latest snapshot, or the requested t, into a compatible target database URI.
  (defn restore
    ([from-storage-uri to-uri progress t incremental?]
      (restore-db
        {:from-storage (create-storage from-storage-uri), :t t}
        {:to-uri to-uri}
        progress
        (backup-concurrency from-storage-uri)
        incremental?)))
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
        (fn fn__21645
          ([p1__21644#]
            (vector (format "%02x" p1__21644#) (str/upper-case (format "%02x" p1__21644#)))))
        (range 256))))
  (defn missing-seg-ids
    ([backup_storage seg_id_set]
      (when-not (set? seg_id_set)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'set? 'seg-id-set))))))
      (let [value_substorage (substorage backup_storage "values")
            map__21648 (queue/queue-seq (long (* (* (count prefixes) 1000) 10)))
            map__21648 (if (seq? map__21648)
                         (if (next map__21648)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21648))
                           (if (seq map__21648) (first map__21648) {}))
                         map__21648)
            fill (get map__21648 :fill)
            done (get map__21648 :done)
            drain (get map__21648 :drain)
            ms_fut (future-call
                     (fn fn__21649
                       ([]
                         (reduce
                           (fn fn__21650 ([acc k] (disj acc k)))
                           seg_id_set
                           (^clojure.lang.IFn drain)))))]
        (let [futs (mapv
                     (fn fn__21653
                       ([prefix]
                         (common/pfuture
                           (deref thread-pool)
                           (fn fn__21654
                             ([]
                               (let [m_21655 {:event :verify/list-prefix, :prefix prefix}
                                     ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.backup")]
                                                       (when (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.info
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_21655 :phase :begin))))
                                                       nil)
                                     start__8553__auto__ (java.lang.System/nanoTime)
                                     result__8554__auto__ (try
                                                            {:returned
                                                             (run!
                                                               fill
                                                               (filter
                                                                 (fn
                                                                   fn__21659
                                                                   ([p1__21647#]
                                                                     (contains?
                                                                       seg_id_set
                                                                       p1__21647#)))
                                                                 (:ks
                                                                   (list-keys
                                                                     (substorage
                                                                       value_substorage
                                                                       prefix)
                                                                     ""))))}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8555__auto__
                                                              {:threw t__8555__auto__}))
                                     elapsed_21656 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8553__auto__)
                                     msec_21657 (logger/format-as-msec (long elapsed_21656))]
                                 (let [endmsg__8556__auto__ (merge
                                                              (assoc
                                                                m_21655
                                                                :msec
                                                                msec_21657
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8554__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8554__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger "datomic.backup")]
                                   (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                     (.info
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8556__auto__)))
                                   nil)
                                 (if (contains? result__8554__auto__ :returned)
                                   (:returned result__8554__auto__)
                                   (do (throw (:threw result__8554__auto__)) nil))))))))
                     prefixes)]
          (loop [seq_21668 (seq futs) chunk_21669 nil count_21670 0 i_21671 0]
            (if (< i_21671 count_21670)
              (let [fut (.nth ^clojure.lang.Indexed chunk_21669 (int i_21671))]
                (deref fut)
                (recur seq_21668 chunk_21669 count_21670 (inc i_21671)))
              (let [temp__5804__auto__ (seq seq_21668)]
                (when temp__5804__auto__
                  (let [seq_21668 temp__5804__auto__]
                    (if (chunked-seq? seq_21668)
                      (let [c__6065__auto__ (chunk-first seq_21668)]
                        (recur
                          (chunk-rest seq_21668)
                          c__6065__auto__
                          (int (count c__6065__auto__))
                          (int 0)))
                      (let [fut (first seq_21668)]
                        (deref fut)
                        (recur (next seq_21668) nil 0 0))))))))
          (^clojure.lang.IFn done))
        (deref ms_fut))))
  (reset-meta!
    #'missing-seg-ids
    (assoc
      {:arglists (clojure.core/list ['backup-storage 'seg-id-set]), :column (int 1)}
      :name
      'missing-seg-ids
      :ns
      *ns*))
  (defn unreadable-seg-ids
    ([lookup seg_ids]
      (let [unreadable? (fn unreadable_QMARK_
                          ([p1__21675#]
                            (try
                              (do (get lookup p1__21675#) false)
                              (catch java.lang.Throwable _ true))))]
        (keep
          deref
          (seque
            pool-size
            (map
              (fn fn__21678
                ([k]
                  (common/pfuture
                    (deref thread-pool)
                    (fn fn__21679
                      ([]
                        (when (^clojure.lang.IFn unreadable? k)
                          (common/log-and-print {:event :verify-backup/unreadable-segment, :k k})
                          k))))))
              seg_ids))))))
  (reset-meta!
    #'unreadable-seg-ids
    (assoc
      {:arglists (clojure.core/list ['lookup 'seg-ids]), :column (int 1)}
      :name
      'unreadable-seg-ids
      :ns
      *ns*))
  ;; Verifies that every referenced log and index segment exists; read-all also reads each value.
  ;; ATOMIC-NOTE [observed/adaptation] Presence/read-all checks establish segment availability/readability,
  ;; not replay-equivalent schema and index content. Atomic's explicit verifier
  ;; also authenticates program edges, receipts/frontiers and projections
  ;; against one canonical replay. Ordinary backup reads do not run that audit.
  (defn verify-backup
    ([p__21683]
      (let [map__21684 p__21683
            map__21684 (if (seq? map__21684)
                         (if (next map__21684)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21684))
                           (if (seq map__21684) (first map__21684) {}))
                         map__21684)
            backup_uri (get map__21684 :backup-uri)
            t (get map__21684 :t)
            read_all (get map__21684 :read-all)
            backup_storage (create-storage backup_uri nil)
            map__21685 (or
                         (read-roots t backup_storage)
                         (error/raise :verify/roots-missing (str "No database root for t " t)))
            map__21685 (if (seq? map__21685)
                         (if (next map__21685)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21685))
                           (if (seq map__21685) (first map__21685) {}))
                         map__21685)
            version (get map__21685 :backup/version)]
        (when (< version 3)
          (throw (java.lang.RuntimeException. "Verify not supported for pre-2015 backup format.")))
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
                     (fn fn__21688 ([idx arg] (^clojure.lang.IFn progress idx) arg))
                     seg_id_set)))}))))))
  (reset-meta!
    #'verify-backup
    (assoc
      {:arglists (clojure.core/list [{:keys ['backup-uri 't 'read-all]}]), :column (int 1)}
      :name
      'verify-backup
      :ns
      *ns*))
  ;; Materializes a fixed database and log value directly from a backup without a transactor.
  (defn load-database
    ([backup_uri t]
      (let [storage (create-storage backup_uri)
            map__21692 (or
                         (read-roots (or t (require-latest-t backup_uri)) storage)
                         (error/raise
                           :restore/roots-missing
                           (str "No database root for t " t " in backup")))
            map__21692 (if (seq? map__21692)
                         (if (next map__21692)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21692))
                           (if (seq map__21692) (first map__21692) {}))
                         map__21692)
            roots map__21692
            db_id (get map__21692 :db-id)
            index_root_id (get map__21692 :index-root-id)
            log_root_id (get map__21692 :log-root-id)
            log_tail (get map__21692 :log-tail)
            olookup (caching-olookup (substorage storage "values") (:backup/version roots))
            index (index/load-index olookup index_root_id)
            tail (log/load-tail (ByteBuffer/wrap ^bytes log_tail))
            idxdb (db/db db_id index)
            log (log/->LogValue nil olookup log_root_id tail)
            db (:db (log/catchup idxdb log true))
            log (log/->LogValue db olookup log_root_id (:memlog db))]
        {:db-id db_id, :db db, :log log})))
  (reset-meta!
    #'load-database
    (assoc
      {:arglists (clojure.core/list ['backup-uri 't]), :column (int 1)}
      :name
      'load-database
      :ns
      *ns*)))
