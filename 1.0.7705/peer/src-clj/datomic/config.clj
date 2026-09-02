(do
  (clojure.core/in-ns 'datomic.config)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.config)
    {:doc
     "Loads, coerces, validates, and exposes Datomic process configuration. Memory values accept byte counts or k, m, and g suffixes; invalid configured values raise information-bearing errors."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'reset!))
      (clojure.core/require
        ['clojure.string :as 'str]
        ['clojure.edn :as 'edn]
        ['clojure.java.io :as 'io]
        ['datomic.require :as 'req]
        ['datomic.error :as 'error])))
  (when-not (.equals 'datomic.config 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.config))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'reset!))
        (clojure.core/require
          ['clojure.string :as 'str]
          ['clojure.edn :as 'edn]
          ['clojure.java.io :as 'io]
          ['datomic.require :as 'req]
          ['datomic.error :as 'error]))))
  (set! *warn-on-reflection* true)
  (defn protocol-supports-ha? ([protocol] (not (contains? #{:limited-edition :dev} protocol))))
  (reset-meta!
    #'protocol-supports-ha?
    (assoc
      {:arglists (clojure.core/list ['protocol]), :column (int 1)}
      :name
      'protocol-supports-ha?
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Symbolish (sym-name [s]) (sym-namespace [s]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.config" "Symbolish")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Symbolish :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'sym-name {:arglists (clojure.core/list ['s])}),
                                      :arglists (clojure.core/list ['s]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.config" "Symbolish"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.config" "sym-name")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'sym-namespace
                                        {:arglists (clojure.core/list ['s])}),
                                      :arglists (clojure.core/list ['s]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.config" "Symbolish"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.config" "sym-namespace")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*))))
  (extend nil Symbolish {:sym-name (fn fn__8974 ([_] nil))})
  (extend java.lang.Object Symbolish {:sym-name (fn fn__8976 ([_] nil))})
  (extend
    clojure.lang.Named
    Symbolish
    {:sym-name (fn fn__8978 ([k] (name k))), :sym-namespace (fn fn__8980 ([k] (namespace k)))})
  (def datomic-edition :limited-edition)
  (reset-meta! #'datomic-edition (assoc {:column (int 1)} :name 'datomic-edition :ns *ns*))
  (defn set-pro ([] (alter-var-root #'datomic-edition (constantly :pro))))
  (reset-meta!
    #'set-pro
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'set-pro :ns *ns*))
  (defn pro? ([] (= :pro datomic-edition)))
  (reset-meta!
    #'pro?
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'pro? :ns *ns*))
  (defn limited-edition? ([] (= :limited-edition datomic-edition)))
  (reset-meta!
    #'limited-edition?
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'limited-edition? :ns *ns*))
  ;; Parses a byte count or a base-1024 k, m, or g quantity.
  (defn parse-memory-string
    ([s]
      (when s
        (let [parsed (let [temp__5802__auto__ (re-matches #"(?i)(\d+)([gmk])" s)]
                       (if temp__5802__auto__
                         (let [vec__8985 temp__5802__auto__
                               _ (nth vec__8985 (int 0) nil)
                               mult (nth vec__8985 (int 1) nil)
                               abbr (nth vec__8985 (int 2) nil)]
                           (*
                             (edn/read-string mult)
                             (let [G__8988 (.toLowerCase ^java.lang.String abbr)]
                               (case
                                 G__8988
                                 "g"
                                 (long (* (* 1024 1024) 1024))
                                 "k"
                                 (* 1024)
                                 "m"
                                 (long (* 1024 1024))))))
                         (edn/read-string s)))]
          (when (integer? parsed) parsed)))))
  (reset-meta!
    #'parse-memory-string
    (assoc
      {:arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'parse-memory-string
      :ns
      *ns*))
  ;; Reads one Java system property, applies its coercion and validator, and raises
  ;; :db.error/invalid-config-value when an explicitly supplied value is invalid.
  (defn read-system-property
    ([prop coerce valid? default]
      (let [s (java.lang.System/getProperty ^java.lang.String prop)]
        (if (seq s)
          (let [v (^clojure.lang.IFn coerce s)]
            (if (^clojure.lang.IFn valid? v)
              v
              (error/arg
                :db.error/invalid-config-value
                (str "Invalid value '" s "' for system property '" prop "'")
                {:property prop, :value v})))
          default))))
  (reset-meta!
    #'read-system-property
    (assoc
      {:arglists (clojure.core/list ['prop 'coerce 'valid? 'default]), :column (int 1)}
      :name
      'read-system-property
      :ns
      *ns*))
  (def DEFAULT_MEMIDX_MAX 67108864)
  (reset-meta!
    #'DEFAULT_MEMIDX_MAX
    (assoc {:const true, :column (int 1)} :name 'DEFAULT_MEMIDX_MAX :ns *ns*))
  (defn at-least ([minval] (fn fn__8992 ([n] (and (integer? n) (<= minval n))))))
  (reset-meta!
    #'at-least
    (assoc {:arglists (clojure.core/list ['minval]), :column (int 1)} :name 'at-least :ns *ns*))
  (defn in-range ([lo hi] (fn fn__8996 ([n] (and (integer? n) (<= lo n hi))))))
  (reset-meta!
    #'in-range
    (assoc {:arglists (clojure.core/list ['lo 'hi]), :column (int 1)} :name 'in-range :ns *ns*))
  (defn warn-deprecations
    ([]
      (when (seq (java.lang.System/getProperty "datomic.objectCacheBytes"))
        (binding [*out* *err*]
          (println
            "Warning: datomic.objectCacheBytes has been renamed to datomic.objectCacheMax, please change your configuration!")))))
  (reset-meta!
    #'warn-deprecations
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'warn-deprecations :ns *ns*))
  (defn version
    ([& _]
      (let [temp__5802__auto__ (io/resource "datomic/VERSION")]
        (if temp__5802__auto__
          (let [f temp__5802__auto__] (str/trim (slurp f)))
          (req/require-and-run 'datomic.cli/development-version)))))
  (reset-meta!
    #'version
    (assoc
      {:private true, :arglists (clojure.core/list ['& '_]), :column (int 1)}
      :name
      'version
      :ns
      *ns*))
  (defn version-unique ([] (str/replace-first (version) #"([^.]+\.)([^.]+\.)" "")))
  (reset-meta!
    #'version-unique
    (assoc
      {:private true, :arglists (clojure.core/list []), :column (int 1)}
      :name
      'version-unique
      :ns
      *ns*))
  (defn read-revision
    ([props]
      (try
        (let [n (edn/read-string
                  (first (str/split (^clojure.lang.IFn props "datomic.versionUnique") #"\." 2)))]
          (if (integer? n) n 999999))
        (catch java.lang.Throwable t 999999))))
  (reset-meta!
    #'read-revision
    (assoc
      {:private true, :arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'read-revision
      :ns
      *ns*))
  (defn bool? ([x] (instance? java.lang.Boolean x)))
  (reset-meta!
    #'bool?
    (assoc
      {:private true, :arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'bool?
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.config" "config-table") {:column (int 1)})
  ;; Each property entry is [name coercion validation default]. Memory settings are bytes after
  ;; coercion; names ending in Msec are milliseconds; concurrency settings are worker limits.
  ;; A default function receives properties already resolved earlier in this table.
  (.bindRoot
    (clojure.lang.RT/var "datomic.config" "config-table")
    [;; Process coordination, query execution, health checks, and storage write limits.
     ["datomic.heartbeatIntervalMsec" edn/read-string (at-least 1000) 5000]
     ["datomic.readAheadPool"
      (fn fn__9013
        ([n]
          (let [n (try (edn/read-string n) (catch java.lang.RuntimeException _ nil))]
            (if ((at-least 0) n)
              n
              (long (+ 2 (.availableProcessors (java.lang.Runtime/getRuntime))))))))
      identity
      (long (+ 2 (.availableProcessors (java.lang.Runtime/getRuntime))))]
     ["datomic.queryPool" edn/read-string (at-least 2) 32]
     ["datomic.defaultPartition" edn/read-string keyword? :db.part/user]
     ["datomic.indexMetrics" edn/read-string bool? true]
     ["datomic.pingPort" edn/read-string (at-least 0) nil]
     ["datomic.pingHost" identity string? nil]
     ["datomic.pingConcurrency" edn/read-string (at-least 1) nil]
     ["datomic.printConnectionInfo" edn/read-string (partial contains? #{true false}) false]
     ["datomic.writeConcurrency" edn/read-string (at-least 2) 4]
     ["datomic.efsWritePool" edn/read-string (at-least 2) 128]
     ["datomic.efsDeletePool" edn/read-string (at-least 2) 128]
     ;; Backup, restore, deletion concurrency, cache behavior, and garbage-collection pacing.
     ["datomic.deleteConcurrency"
      edn/read-string
      (fn fn__9017 ([p1__9006#] (<= 1 p1__9006# 128)))
      1]
     ["datomic.backupPaceMsec" edn/read-string (at-least 5) nil]
     ["datomic.gcStoragePaceMsec" edn/read-string (at-least 5) nil]
     ["datomic.podGcDelayMsec" edn/read-string (at-least 1) 60000]
     ["datomic.fileBackupConcurrency" edn/read-string (at-least 2) 5]
     ["datomic.sqlValidationQuery" identity string? nil]
     ["datomic.backupBranchConcurrency" edn/read-string (at-least 1) 32]
     ["datomic.s3BackupConcurrency" edn/read-string (at-least 2) 25]
     ["datomic.s3MaxRetries" edn/read-string (at-least 2) 9]
     ["datomic.s3RetryBaseDelay" edn/read-string (at-least 2) 100]
     ["datomic.backupUseSegsetStorage" edn/read-string bool? true]
     ["datomic.readConcurrency"
      edn/read-string
      (at-least 2)
      (fn fn__9019 ([props] (* 2 (get props "datomic.writeConcurrency"))))]
     ;; Remote-storage timeouts; -1 delegates timeout behavior to the client implementation.
     ["datomic.ddbRequestTimeout" edn/read-string (at-least -1) 1000]
     ["datomic.ddbSocketTimeout"
      edn/read-string
      (at-least -1)
      (fn fn__9021
        ([props]
          (long (java.lang.Math/round (double (* 0.9 (get props "datomic.ddbRequestTimeout")))))))]
     ["datomic.ddbClientExecutionTimeout"
      edn/read-string
      (at-least -1)
      (fn fn__9023
        ([props]
          (long (java.lang.Math/round (double (* 1.1 (get props "datomic.ddbRequestTimeout")))))))]
     ["datomic.ddbConnectionTimeout"
      edn/read-string
      (at-least -1)
      (fn fn__9025 ([props] (get props "datomic.ddbSocketTimeout")))]
     ["datomic.s3RequestTimeout" edn/read-string (at-least -1) 5000]
     ["datomic.s3SocketTimeout"
      edn/read-string
      (at-least -1)
      (fn fn__9027
        ([props]
          (long (java.lang.Math/round (double (* 0.9 (get props "datomic.s3RequestTimeout")))))))]
     ["datomic.s3ClientExecutionTimeout"
      edn/read-string
      (at-least -1)
      (fn fn__9029
        ([props]
          (long (java.lang.Math/round (double (* 1.1 (get props "datomic.s3RequestTimeout")))))))]
     ["datomic.s3ConnectionTimeout"
      edn/read-string
      (at-least -1)
      (fn fn__9031 ([props] (get props "datomic.s3SocketTimeout")))]
     ;; Heap allocation and indexing thresholds. Indexing starts at the threshold and transaction
     ;; back pressure begins at the maximum.
     ["datomic.memoryIndexMax" parse-memory-string (at-least (long (* (* 32 1024) 1024))) 67108864]
     ["datomic.memoryIndexThreshold"
      parse-memory-string
      (at-least (long (* (* 8 1024) 1024)))
      (fn fn__9033
        ([props] (long (min (* 0.5 67108864) (* 0.5 (get props "datomic.memoryIndexMax"))))))]
     ["datomic.allowLogOverlap" edn/read-string identity false]
     ["datomic.logCoalesceThreshold" edn/read-string (at-least 500) 1000]
     ["datomic.logDirSizeLimit" edn/read-string (at-least 1000) 50000]
     ["datomic.luceneLogFile" identity string? nil]
     ["datomic.pidFile" identity string? nil]
     ;; Peer connection, transaction, and external cache behavior.
     ["datomic.peerConnectionTTLMsec" edn/read-string (at-least 10000) 10000]
     ["datomic.txTimeoutMsec" edn/read-string (at-least 0) 10000]
     ["datomic.memcachedExpirationDays" edn/read-string (in-range 0 30) 30]
     ["datomic.localMemcachedServers" identity string? nil]
     ["datomic.localMemcachedUsername" identity string? nil]
     ["datomic.localMemcachedPassword" identity string? nil]
     ["datomic.localMemcachedAutoDiscovery" edn/read-string bool? false]
     ["datomic.localMemcachedConfigTimeoutMsec" edn/read-string (at-least 1) 100]
     ["datomic.memcachedServers" identity string? nil]
     ["datomic.memcachedUsername" identity string? nil]
     ["datomic.memcachedPassword" identity string? nil]
     ["datomic.memcachedAutoDiscovery" edn/read-string bool? false]
     ["datomic.memcachedConfigTimeoutMsec" edn/read-string (at-least 1) 100]
     ["datomic.dataDir" io/file identity (io/file "./data")]
     ["datomic.exciseIOParallelism"
      edn/read-string
      (fn fn__9035 ([v] (or (nil? v) (and (int? v) (<= 1 v 1000)))))
      100]
     ;; Background index construction and its filesystem workspace.
     ["datomic.dynamicIndexParallelism" edn/read-string bool? false]
     ["datomic.indexSegsPerSecond"
      edn/read-string
      (fn fn__9039 ([p1__9007#] (or (nil? p1__9007#) (clojure.lang.Numbers/isPos p1__9007#))))
      nil]
     ["datomic.indexParallelism" edn/read-string (in-range 1 8) 1]
     ["datomic.indexIOParallelism"
      edn/read-string
      (fn fn__9042 ([v] (or (nil? v) (and (int? v) (<= 1 v 1000)))))
      100]
     ["datomic.indexParallelMerges" edn/read-string bool? false]
     ["datomic.externalSortPool"
      edn/read-string
      (fn fn__9046 ([p1__9008#] (<= 1 p1__9008# 32)))
      (fn fn__9048 ([props] (get props "datomic.indexParallelism")))]
     ["datomic.indexWorkDir"
      io/file
      identity
      (fn fn__9050 ([props] (io/file (get props "datomic.dataDir") "indexer")))]
     ["datomic.objectCacheMax"
      parse-memory-string
      (at-least (long (* (* 32 1024) 1024)))
      (long (java.lang.Math/round (double (* 0.5 (.maxMemory (java.lang.Runtime/getRuntime))))))]
     ["datomic.version" (constantly (version)) string? (constantly (version))]
     ["datomic.versionUnique" (constantly (version-unique)) string? (constantly (version-unique))]
     ["datomic.buildRevision" read-revision identity read-revision]
     ;; Monitoring callback and optional CloudWatch destination.
     ["datomic.cloudwatchName" identity string? nil]
     ["datomic.cloudwatchAccessKeyId" edn/read-string map? nil]
     ["datomic.cloudwatchSecretKey" edn/read-string map? nil]
     ["datomic.cloudwatchDimension" identity string? nil]
     ["datomic.cloudwatchRegion" identity string? nil]
     ["datomic.metricsCallback"
      edn/read-string
      symbol?
      (fn fn__9052
        ([props]
          (if (and
                (^clojure.lang.IFn props "datomic.cloudwatchName")
                (^clojure.lang.IFn props "datomic.cloudwatchDimension")
                (^clojure.lang.IFn props "datomic.cloudwatchRegion"))
            'datomic.aws-monitor/cloudwatch-reporter
            'clojure.core/identity)))]
     ["datomic.cassandraClusterCallback" edn/read-string symbol? nil]
     ["datomic.indexDirScale"
      edn/read-string
      (fn fn__9056 ([p1__9009#] (and (integer? p1__9009#) (<= 1 p1__9009# 5))))
      1]
     ;; Local SSD cache and transaction-read prefetching.
     ["datomic.valcachePutsPool" edn/read-string (fn fn__9059 ([p1__9010#] (<= 1 p1__9010# 32))) 4]
     ["datomic.valcachePath" identity string? nil]
     ["datomic.valcacheMaxGb" edn/read-string (at-least 1) nil]
     ["datomic.prefetchConcurrency"
      edn/read-string
      pos-int?
      (long (max (quot (.availableProcessors (java.lang.Runtime/getRuntime)) 2) 1))]
     ["datomic.prefetchProbes"
      edn/read-string
      bool?
      (fn fn__9061 ([_] (>= (.availableProcessors (java.lang.Runtime/getRuntime)) 2)))]
     ["datomic.memcachedLib" identity (fn fn__9063 ([p1__9011#] (= p1__9011# "folsom"))) "spy"]
     ["datomic.memcachedGetTimeoutMsec" edn/read-string (at-least 5) 20]
     ["datomic.memcachedRepairFromSpyRatio"
      edn/read-string
      (fn fn__9065 ([p1__9012#] (and (number? p1__9012#) (<= 0 p1__9012# 1))))
      0.1]])
  ;; Enforces the memory-index ordering and reserves at least one quarter of the heap
  ;; for transaction processing, application work, and runtime overhead.
  (defn validate-properties
    ([m]
      (when (< (get m "datomic.memoryIndexMax") (get m "datomic.memoryIndexThreshold"))
        (error/arg
          :db.error/invalid-memory-config
          "datomic.memoryIndexMax must be >= datomic.memoryIndexThreshold"
          m))
      (when (>
              (+ (get m "datomic.memoryIndexMax") (get m "datomic.objectCacheMax"))
              (* 0.75 (.maxMemory (java.lang.Runtime/getRuntime))))
        (error/arg
          :db.error/not-enough-memory
          "(datomic.objectCacheMax + datomic.memoryIndexMax) exceeds 75% of JVM RAM"
          m))
      m))
  (reset-meta!
    #'validate-properties
    (assoc
      {:arglists (clojure.core/list ['m]), :column (int 1)}
      :name
      'validate-properties
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.config" "properties-ref") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.config" "properties-ref") (atom nil))
  (defn reset!
    ([]
      (warn-deprecations)
      (clojure.core/reset!
        properties-ref
        (validate-properties
          (reduce
            (fn fn__9070
              ([m p__9069]
                (let [vec__9071 p__9069
                      prop (nth vec__9071 (int 0) nil)
                      coerce (nth vec__9071 (int 1) nil)
                      valid? (nth vec__9071 (int 2) nil)
                      default (nth vec__9071 (int 3) nil)]
                  (assoc
                    m
                    prop
                    (read-system-property
                      prop
                      coerce
                      valid?
                      (if (fn? default) (^clojure.lang.IFn default m) default))))))
            {}
            config-table)))))
  (reset-meta!
    #'reset!
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'reset! :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.config" "initialized") {:private true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.config" "initialized") (delay (reset!)))
  ;; Returns a validated process property, initializing the configuration table on demand.
  (defn property
    ([name]
      (deref initialized)
      (let [props (deref properties-ref)]
        (if (contains? props name)
          (get props name)
          (error/arg
            :db.error/not-a-config-property
            (str "No config property named '" name "'")
            {:property name})))))
  (reset-meta!
    #'property
    (assoc {:arglists (clojure.core/list ['name]), :column (int 1)} :name 'property :ns *ns*))
  ;; Valcache is enabled only when both its SSD path and capacity are configured.
  (defn local-valcache-enabled?
    ([] (boolean (and (property "datomic.valcachePath") (property "datomic.valcacheMaxGb")))))
  (reset-meta!
    #'local-valcache-enabled?
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'local-valcache-enabled?
      :ns
      *ns*))
  (defn property-map
    ([m]
      (reduce-kv
        (fn fn__9081
          ([m k v]
            (let [temp__5802__auto__ (property v)]
              (if temp__5802__auto__ (let [v temp__5802__auto__] (assoc m k v)) m))))
        {}
        m)))
  (reset-meta!
    #'property-map
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'property-map :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.config" "edition-has-feature?") {:column (int 1)})
  (let [v__5792__auto__ #'edition-has-feature?]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.config" "edition-has-feature?") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.config" "edition-has-feature?")
        (clojure.lang.MultiFn.
          "edition-has-feature?"
          identity
          :default
          #'clojure.core/global-hierarchy))
      #'edition-has-feature?))
  (defmethod edition-has-feature? :monitor/logrotate fn__9089 ([_] (pro?)))
  (defmethod edition-has-feature? :monitor/metrics fn__9091 ([_] (pro?)))
  (defmethod edition-has-feature? :protocol/dev fn__9093 ([_] (pro?)))
  (defmethod edition-has-feature? :lifecycle/ha fn__9095 ([_] (pro?)))
  (.setMeta (clojure.lang.RT/var "datomic.config" "is-folsom?") {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.config" "is-folsom?")
    (delay
      (=
        "folsom"
        (and
          (try
            (do (clojure.core/require 'datomic.memcached.folsom) true)
            (catch java.io.FileNotFoundException _ false))
          (property "datomic.memcachedLib")))))
  (defn folsom? ([] (deref is-folsom?)))
  (reset-meta!
    #'folsom?
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'folsom? :ns *ns*))
  (defn local-memcached-args
    ([]
      (when (property "datomic.localMemcachedServers")
        (property-map
          {:servers "datomic.localMemcachedServers",
           :username "datomic.localMemcachedUsername",
           :password "datomic.localMemcachedPassword",
           :auto-discovery "datomic.localMemcachedAutoDiscovery",
           :config-timeout-msec "datomic.localMemcachedConfigTimeoutMsec"}))))
  (reset-meta!
    #'local-memcached-args
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'local-memcached-args
      :ns
      *ns*))
  (defn memcached-args
    ([]
      (when (property "datomic.memcachedServers")
        (property-map
          {:servers "datomic.memcachedServers",
           :username "datomic.memcachedUsername",
           :password "datomic.memcachedPassword",
           :auto-discovery "datomic.memcachedAutoDiscovery",
           :config-timeout-msec "datomic.memcachedConfigTimeoutMsec"}))))
  (reset-meta!
    #'memcached-args
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'memcached-args :ns *ns*))
  (defn max-gb->eviction-threshold-mb ([max-gb] (- (* max-gb 900) 500)))
  (reset-meta!
    #'max-gb->eviction-threshold-mb
    (assoc
      {:private true, :arglists (clojure.core/list ['max-gb]), :column (int 1)}
      :name
      'max-gb->eviction-threshold-mb
      :ns
      *ns*))
  ;; Produces a complete Valcache configuration and reserves headroom below the requested disk cap.
  (defn valcache-args
    ([]
      (let [required {:path "datomic.valcachePath", :max-gb "datomic.valcacheMaxGb"}
            m (property-map required)]
        (when (= (count m) (count required))
          (assoc
            m
            :eviction-interval-secs
            10
            :eviction-threshold-mb
            (max-gb->eviction-threshold-mb (:max-gb m)))))))
  (reset-meta!
    #'valcache-args
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'valcache-args :ns *ns*))
  ;; Supplies bounded DynamoDB client timeouts and disables SDK retries so storage retry policy
  ;; remains centralized in the Datomic storage layer.
  (defn ddb-client-args
    ([args]
      (merge
        (property-map
          {:clientExecutionTimeout "datomic.ddbClientExecutionTimeout",
           :connectionTimeout "datomic.ddbConnectionTimeout",
           :requestTimeout "datomic.ddbRequestTimeout",
           :socketTimeout "datomic.ddbSocketTimeout"})
        {:maxConnections 1024, :maxAttempts 1}
        args)))
  (reset-meta!
    #'ddb-client-args
    (assoc
      {:arglists (clojure.core/list ['args]), :column (int 1)}
      :name
      'ddb-client-args
      :ns
      *ns*))
  ;; Supplies S3 client timeouts and a large connection pool for parallel backup and restore I/O.
  (defn s3-client-args
    ([args]
      (merge
        (property-map
          {:clientExecutionTimeout "datomic.s3ClientExecutionTimeout",
           :connectionTimeout "datomic.s3ConnectionTimeout",
           :requestTimeout "datomic.s3RequestTimeout",
           :socketTimeout "datomic.s3SocketTimeout"})
        {:maxConnections (long (* 64 1024))}
        args))
    ([] (s3-client-args nil)))
  (reset-meta!
    #'s3-client-args
    (assoc
      {:arglists (clojure.core/list [] ['args]), :column (int 1)}
      :name
      's3-client-args
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.config" "is-prefetch?") {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.config" "is-prefetch?")
    (delay (boolean (property "datomic.prefetchProbes"))))
  (defn prefetch-enabled? ([] (deref is-prefetch?)))
  (reset-meta!
    #'prefetch-enabled?
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'prefetch-enabled? :ns *ns*)))
