(do
  (clojure.core/in-ns 'datomic.config)
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
  (defonce Symbolish {})
  (defprotocol Symbolish (sym-name [s]) (sym-namespace [s]))
  (extend nil Symbolish {:sym-name (fn fn__746 ([_] nil))})
  (extend java.lang.Object Symbolish {:sym-name (fn fn__748 ([_] nil))})
  (extend
    clojure.lang.Named
    Symbolish
    {:sym-name (fn fn__750 ([k] (name k))), :sym-namespace (fn fn__752 ([k] (namespace k)))})
  (def datomic-edition :limited-edition)
  (defn set-pro ([] (alter-var-root #'datomic-edition (constantly :pro))))
  (defn pro? ([] (= :pro datomic-edition)))
  (defn limited-edition? ([] (= :limited-edition datomic-edition)))
  (defn parse-memory-string
    ([s]
      (when s
        (let [parsed (let [temp__5455__auto__ (re-matches #"(?i)(\d+)([gmk])" s)]
                       (if temp__5455__auto__
                         (let [vec__757 temp__5455__auto__
                               _ (nth vec__757 (int 0) nil)
                               mult (nth vec__757 (int 1) nil)
                               abbr (nth vec__757 (int 2) nil)]
                           (*
                             (edn/read-string mult)
                             (let [G__760 (.toLowerCase ^java.lang.String abbr)]
                               (case
                                 G__760
                                 "g"
                                 (long (* (* 1024 1024) 1024))
                                 "k"
                                 (* 1024)
                                 "m"
                                 (long (* 1024 1024))))))
                         (edn/read-string s)))]
          (when (integer? parsed) parsed)))))
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
  (def DEFAULT_MEMIDX_MAX 67108864)
  (reset-meta!
    #'DEFAULT_MEMIDX_MAX
    (assoc {:const true, :column 1} :name 'DEFAULT_MEMIDX_MAX :ns *ns*))
  (defn at-least ([minval] (fn fn__764 ([n] (and (integer? n) (<= minval n))))))
  (defn in-range ([lo hi] (fn fn__768 ([n] (and (integer? n) (<= lo n hi))))))
  (defn warn-deprecations
    ([]
      (when (seq (java.lang.System/getProperty "datomic.objectCacheBytes"))
        (binding [*out* *err*]
          (println
            "Warning: datomic.objectCacheBytes has been renamed to datomic.objectCacheMax, please change your configuration!")))))
  (defn version
    ([& _]
      (let [temp__5455__auto__ (io/resource "datomic/VERSION")]
        (if temp__5455__auto__
          (let [f temp__5455__auto__] (str/trim (slurp f)))
          (req/require-and-run 'datomic.cli/development-version)))))
  (reset-meta!
    #'version
    (assoc
      {:private true, :arglists (clojure.core/list ['& '_]), :column 1}
      :name
      'version
      :ns
      *ns*))
  (defn version-unique ([] (str/replace-first (version) #"([^.]+\.)([^.]+\.)" "")))
  (reset-meta!
    #'version-unique
    (assoc
      {:private true, :arglists (clojure.core/list []), :column 1}
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
      {:private true, :arglists (clojure.core/list ['props]), :column 1}
      :name
      'read-revision
      :ns
      *ns*))
  (defn bool? ([x] (instance? java.lang.Boolean x)))
  (reset-meta!
    #'bool?
    (assoc {:private true, :arglists (clojure.core/list ['x]), :column 1} :name 'bool? :ns *ns*))
  (def config-table
   [["datomic.heartbeatIntervalMsec" edn/read-string (at-least 1000) 5000]
    ["datomic.readAheadPool"
     (fn fn__785
       ([n]
         (let [n (try (edn/read-string n) (catch java.lang.RuntimeException _ nil))]
           (if ((at-least 0) n)
             n
             (long (+ 2 (.availableProcessors (java.lang.Runtime/getRuntime))))))))
     identity
     (long (+ 2 (.availableProcessors (java.lang.Runtime/getRuntime))))]
    ["datomic.queryPool" edn/read-string (at-least 2) 32]
    ["datomic.externalSortPool" edn/read-string (fn fn__789 ([p1__778#] (<= 1 p1__778# 32))) 4]
    ["datomic.defaultPartition" edn/read-string keyword? :db.part/user]
    ["datomic.indexMetrics" edn/read-string bool? true]
    ["datomic.pingPort" edn/read-string (at-least 0) nil]
    ["datomic.pingHost" identity string? nil]
    ["datomic.pingConcurrency" edn/read-string (at-least 1) nil]
    ["datomic.printConnectionInfo" edn/read-string (partial contains? #{true false}) false]
    ["datomic.writeConcurrency" edn/read-string (at-least 2) 4]
    ["datomic.efsWritePool" edn/read-string (at-least 2) 128]
    ["datomic.efsDeletePool" edn/read-string (at-least 2) 128]
    ["datomic.deleteConcurrency" edn/read-string (fn fn__791 ([p1__779#] (<= 1 p1__779# 128))) 1]
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
     (fn fn__793 ([props] (* 2 (get props "datomic.writeConcurrency"))))]
    ["datomic.ddbRequestTimeout" edn/read-string (at-least -1) 1000]
    ["datomic.ddbSocketTimeout"
     edn/read-string
     (at-least -1)
     (fn fn__795
       ([props]
         (long (java.lang.Math/round (double (* 0.9 (get props "datomic.ddbRequestTimeout")))))))]
    ["datomic.ddbClientExecutionTimeout"
     edn/read-string
     (at-least -1)
     (fn fn__797
       ([props]
         (long (java.lang.Math/round (double (* 1.1 (get props "datomic.ddbRequestTimeout")))))))]
    ["datomic.ddbConnectionTimeout"
     edn/read-string
     (at-least -1)
     (fn fn__799 ([props] (get props "datomic.ddbSocketTimeout")))]
    ["datomic.s3RequestTimeout" edn/read-string (at-least -1) 5000]
    ["datomic.s3SocketTimeout"
     edn/read-string
     (at-least -1)
     (fn fn__801
       ([props]
         (long (java.lang.Math/round (double (* 0.9 (get props "datomic.s3RequestTimeout")))))))]
    ["datomic.s3ClientExecutionTimeout"
     edn/read-string
     (at-least -1)
     (fn fn__803
       ([props]
         (long (java.lang.Math/round (double (* 1.1 (get props "datomic.s3RequestTimeout")))))))]
    ["datomic.s3ConnectionTimeout"
     edn/read-string
     (at-least -1)
     (fn fn__805 ([props] (get props "datomic.s3SocketTimeout")))]
    ["datomic.memoryIndexMax" parse-memory-string (at-least (long (* (* 32 1024) 1024))) 67108864]
    ["datomic.memoryIndexThreshold"
     parse-memory-string
     (at-least (long (* (* 8 1024) 1024)))
     (fn fn__807
       ([props] (long (min (* 0.5 67108864) (* 0.5 (get props "datomic.memoryIndexMax"))))))]
    ["datomic.allowLogOverlap" edn/read-string identity false]
    ["datomic.luceneLogFile" identity string? nil]
    ["datomic.pidFile" identity string? nil]
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
     (fn fn__809 ([v] (or (nil? v) (and (int? v) (<= 1 v 1000)))))
     100]
    ["datomic.dynamicIndexParallelism" edn/read-string bool? false]
    ["datomic.indexSegsPerSecond"
     edn/read-string
     (fn fn__813 ([p1__780#] (or (nil? p1__780#) (clojure.lang.Numbers/isPos p1__780#))))
     nil]
    ["datomic.indexParallelism" edn/read-string (in-range 1 8) 1]
    ["datomic.indexIOParallelism"
     edn/read-string
     (fn fn__816 ([v] (or (nil? v) (and (int? v) (<= 1 v 1000)))))
     100]
    ["datomic.indexWorkDir"
     io/file
     identity
     (fn fn__820 ([props] (io/file (get props "datomic.dataDir") "indexer")))]
    ["datomic.objectCacheMax"
     parse-memory-string
     (at-least (long (* (* 32 1024) 1024)))
     (long (java.lang.Math/round (double (* 0.5 (.maxMemory (java.lang.Runtime/getRuntime))))))]
    ["datomic.version" (constantly (version)) string? (constantly (version))]
    ["datomic.versionUnique" (constantly (version-unique)) string? (constantly (version-unique))]
    ["datomic.buildRevision" read-revision identity read-revision]
    ["datomic.cloudwatchName" identity string? nil]
    ["datomic.cloudwatchAccessKeyId" edn/read-string map? nil]
    ["datomic.cloudwatchSecretKey" edn/read-string map? nil]
    ["datomic.cloudwatchDimension" identity string? nil]
    ["datomic.cloudwatchRegion" identity string? nil]
    ["datomic.metricsCallback"
     edn/read-string
     symbol?
     (fn fn__822
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
     (fn fn__826 ([p1__781#] (and (integer? p1__781#) (<= 1 p1__781# 5))))
     1]
    ["datomic.valcachePutsPool" edn/read-string (fn fn__829 ([p1__782#] (<= 1 p1__782# 32))) 4]
    ["datomic.valcachePath" identity string? nil]
    ["datomic.valcacheMaxGb" edn/read-string (at-least 1) nil]
    ["datomic.useIndexArrayCaches" edn/read-string bool? true]
    ["datomic.prefetchConcurrency"
     edn/read-string
     pos-int?
     (long (max (quot (.availableProcessors (java.lang.Runtime/getRuntime)) 2) 1))]
    ["datomic.prefetchProbes"
     edn/read-string
     bool?
     (fn fn__831 ([_] (>= (.availableProcessors (java.lang.Runtime/getRuntime)) 2)))]
    ["datomic.memcachedLib" identity (fn fn__833 ([p1__783#] (= p1__783# "folsom"))) "spy"]
    ["datomic.memcachedGetTimeoutMsec" edn/read-string (at-least 5) 20]
    ["datomic.memcachedRepairFromSpyRatio"
     edn/read-string
     (fn fn__835 ([p1__784#] (and (number? p1__784#) (<= 0 p1__784# 1))))
     0.1]])
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
  (def properties-ref (atom nil))
  (defn reset!
    ([]
      (warn-deprecations)
      (clojure.core/reset!
        properties-ref
        (validate-properties
          (reduce
            (fn fn__840
              ([m p__839]
                (let [vec__841 p__839
                      prop (nth vec__841 (int 0) nil)
                      coerce (nth vec__841 (int 1) nil)
                      valid? (nth vec__841 (int 2) nil)
                      default (nth vec__841 (int 3) nil)]
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
  (def initialized (delay (reset!)))
  (reset-meta! #'initialized (assoc {:private true, :column 1} :name 'initialized :ns *ns*))
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
  (defn local-valcache-enabled?
    ([] (boolean (and (property "datomic.valcachePath") (property "datomic.valcacheMaxGb")))))
  (defn property-map
    ([m]
      (reduce-kv
        (fn fn__851
          ([m k v]
            (let [temp__5455__auto__ (property v)]
              (if temp__5455__auto__ (let [v temp__5455__auto__] (assoc m k v)) m))))
        {}
        m)))
  (defmulti edition-has-feature? identity)
  (defmethod edition-has-feature? :monitor/logrotate fn__859 ([_] (pro?)))
  (defmethod edition-has-feature? :monitor/metrics fn__861 ([_] (pro?)))
  (defmethod edition-has-feature? :protocol/dev fn__863 ([_] (pro?)))
  (defmethod edition-has-feature? :lifecycle/ha fn__865 ([_] (pro?)))
  (def is-folsom?
   (delay
     (=
       "folsom"
       (and
         (try
           (do (clojure.core/require 'datomic.memcached.folsom) true)
           (catch java.io.FileNotFoundException _ false))
         (property "datomic.memcachedLib")))))
  (reset-meta! #'is-folsom? (assoc {:private true, :column 1} :name 'is-folsom? :ns *ns*))
  (defn folsom? ([] (deref is-folsom?)))
  (defn local-memcached-args
    ([]
      (when (property "datomic.localMemcachedServers")
        (property-map
          {:servers "datomic.localMemcachedServers",
           :username "datomic.localMemcachedUsername",
           :password "datomic.localMemcachedPassword",
           :auto-discovery "datomic.localMemcachedAutoDiscovery",
           :config-timeout-msec "datomic.localMemcachedConfigTimeoutMsec"}))))
  (defn memcached-args
    ([]
      (when (property "datomic.memcachedServers")
        (property-map
          {:servers "datomic.memcachedServers",
           :username "datomic.memcachedUsername",
           :password "datomic.memcachedPassword",
           :auto-discovery "datomic.memcachedAutoDiscovery",
           :config-timeout-msec "datomic.memcachedConfigTimeoutMsec"}))))
  (defn max-gb->eviction-threshold-mb ([max_gb] (- (* max_gb 900) 500)))
  (reset-meta!
    #'max-gb->eviction-threshold-mb
    (assoc
      {:private true, :arglists (clojure.core/list ['max-gb]), :column 1}
      :name
      'max-gb->eviction-threshold-mb
      :ns
      *ns*))
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
  (defn ddb-client-args
    ([args]
      (merge
        (property-map
          {:clientExecutionTimeout "datomic.ddbClientExecutionTimeout",
           :connectionTimeout "datomic.ddbConnectionTimeout",
           :requestTimeout "datomic.ddbRequestTimeout",
           :socketTimeout "datomic.ddbSocketTimeout"})
        {:maxConnections 1024, :maxErrorRetry 0}
        args)))
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
  (def is-prefetch? (delay (boolean (property "datomic.prefetchProbes"))))
  (reset-meta! #'is-prefetch? (assoc {:private true, :column 1} :name 'is-prefetch? :ns *ns*))
  (defn prefetch-enabled? ([] (deref is-prefetch?))))