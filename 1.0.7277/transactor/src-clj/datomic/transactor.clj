(do
  (clojure.core/in-ns 'datomic.transactor)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.string :as 'str]
        ['clojure.edn :as 'edn]
        'clojure.main
        ['datomic.cluster-stack :as 'cluster-stack]
        ['datomic.config :as 'config]
        ['datomic.common :as 'common]
        ['datomic.crypto :as 'crypto]
        ['datomic.cluster :as 'cluster]
        ['datomic.db :as 'db]
        ['datomic.monitor :as 'monitor]
        ['datomic.slf4j :as 'logger]
        ['datomic.update :as 'update]
        ['datomic.fulltext :as 'ft]
        ['datomic.garbage :as 'garbage]
        ['datomic.coordination :as 'coord]
        ['datomic.artemis-server :as 'aserver]
        ['datomic.uri :as 'uri]
        ['datomic.domain :as 'domain]
        ['datomic.process :as 'process]
        ['datomic.error :as 'error]
        ['datomic.lifecycle :as 'lifecycle]
        ['datomic.log-gc :as 'log-gc]
        ['datomic.memory :as 'memory]
        ['datomic.require :as 'req]
        ['datomic.slf4j.bridge :as 'bridge]
        ['datomic.transactor-ext :as 'transactor-ext]
        ['datomic.cast2slf4j :as 'cast2slf4j])
      (clojure.core/import 'java.net.URLEncoder)
      (clojure.core/import 'java.nio.charset.StandardCharsets)))
  (when-not (.equals 'datomic.transactor 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.transactor))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.string :as 'str]
          ['clojure.edn :as 'edn]
          'clojure.main
          ['datomic.cluster-stack :as 'cluster-stack]
          ['datomic.config :as 'config]
          ['datomic.common :as 'common]
          ['datomic.crypto :as 'crypto]
          ['datomic.cluster :as 'cluster]
          ['datomic.db :as 'db]
          ['datomic.monitor :as 'monitor]
          ['datomic.slf4j :as 'logger]
          ['datomic.update :as 'update]
          ['datomic.fulltext :as 'ft]
          ['datomic.garbage :as 'garbage]
          ['datomic.coordination :as 'coord]
          ['datomic.artemis-server :as 'aserver]
          ['datomic.uri :as 'uri]
          ['datomic.domain :as 'domain]
          ['datomic.process :as 'process]
          ['datomic.error :as 'error]
          ['datomic.lifecycle :as 'lifecycle]
          ['datomic.log-gc :as 'log-gc]
          ['datomic.memory :as 'memory]
          ['datomic.require :as 'req]
          ['datomic.slf4j.bridge :as 'bridge]
          ['datomic.transactor-ext :as 'transactor-ext]
          ['datomic.cast2slf4j :as 'cast2slf4j])
        (clojure.core/import 'java.net.URLEncoder)
        (clojure.core/import 'java.nio.charset.StandardCharsets))))
  (set! *warn-on-reflection* true)
  (req/maybe-require 'datomic.lifecycle-ext)
  (defn aws-creds ([ak sk] (when (and ak sk) {:aws-access-key-id ak, :aws-secret-key sk})))
  (reset-meta!
    #'aws-creds
    (assoc {:arglists (clojure.core/list ['ak 'sk]), :column (int 1)} :name 'aws-creds :ns *ns*))
  (def start-lifecycle
   (fn start_lifecycle
     ([p__26517]
       (let [map__26518 p__26517
             map__26518 (if (seq? map__26518)
                          (if (next map__26518)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26518))
                            (if (seq map__26518) (first map__26518) {}))
                          map__26518)
             cluster_map (get map__26518 :cluster-map)
             master_endpoint (get map__26518 :master-endpoint)
             process (get map__26518 :process)
             td (get map__26518 :td)
             status (get map__26518 :status)
             system_conn (coord/create-system-cluster cluster_map)
             serve (fn serve
                     ([]
                       (reset!
                         status
                         (str
                           "active/"
                           (.format
                             (java.text.SimpleDateFormat. "yyyy-MM-dd-kk-mm-ss")
                             (java.util.Date.))))
                       (let [master (update/create-master
                                      :system-cluster-conf
                                      cluster_map
                                      :olookup-factory
                                      domain/system-cache-olookup
                                      :endpoint
                                      master_endpoint
                                      :td
                                      td)]
                         (common/schedule
                           "Datomic Metrics Tracker"
                           (fn fn__26520
                             ([]
                               (let [logger (org.slf4j.LoggerFactory/getLogger
                                              "datomic.transactor")]
                                 (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                   (.info
                                     ^org.slf4j.Logger logger
                                     (logger/process
                                       {:event :transactor/remote-ips,
                                        :ips (update/remote-ips master)})))
                                 nil)
                               (let [stat (monitor/metrics master)]
                                 (loop [seq_26521 (seq
                                                    [:MemoryIndexMB
                                                     :MemoryIndexFillMsec
                                                     :RemotePeers])
                                        chunk_26522 nil
                                        count_26523 0
                                        i_26524 0]
                                   (if (< i_26524 count_26523)
                                     (let [k (.nth
                                               ^clojure.lang.Indexed chunk_26522
                                               (int i_26524))]
                                       (let [G__26525 stat
                                             G__26525 (some-> G__26525 (^clojure.lang.IFn k))]
                                         (when-not (nil? G__26525) (monitor/add-stat k G__26525)))
                                       (recur seq_26521 chunk_26522 count_26523 (inc i_26524)))
                                     (let [temp__5804__auto__ (seq seq_26521)]
                                       (when temp__5804__auto__
                                         (let [seq_26521 temp__5804__auto__]
                                           (if (chunked-seq? seq_26521)
                                             (let [c__6065__auto__ (chunk-first seq_26521)]
                                               (recur
                                                 (chunk-rest seq_26521)
                                                 c__6065__auto__
                                                 (int (count c__6065__auto__))
                                                 (int 0)))
                                             (let [k (first seq_26521)]
                                               (let [G__26526 stat
                                                     G__26526 (some->
                                                                G__26526
                                                                (^clojure.lang.IFn k))]
                                                 (when-not (nil? G__26526)
                                                   (monitor/add-stat k G__26526)))
                                               (recur (next seq_26521) nil 0 0)))))))))))
                           60000)
                         (process/add-fail-handler
                           process
                           (fn fn__26530 ([] (common/async-shutdown master)))))))
             lc (lifecycle/start
                  :cluster
                  system_conn
                  :endpoint
                  master_endpoint
                  :tick
                  (config/property "datomic.heartbeatIntervalMsec")
                  :serve
                  serve
                  :ha?
                  (config/protocol-supports-ha? (common/getx cluster_map :protocol)))]
         nil))))
  (reset-meta!
    #'start-lifecycle
    (assoc
      {:arglists
       (clojure.core/list [{:keys ['cluster-map 'master-endpoint 'process 'td 'status]}]),
       :column (int 1)}
      :name
      'start-lifecycle
      :ns
      *ns*))
  (def tprop->sysprop
   {:memcached "datomic.memcachedServers",
    :aws-cloudwatch-region "datomic.cloudwatchRegion",
    :write-concurrency "datomic.writeConcurrency",
    :local-memcached-password "datomic.localMemcachedPassword",
    :index-segs-per-second "datomic.indexSegsPerSecond",
    :heartbeat-interval-msec "datomic.heartbeatIntervalMsec",
    :ping-host "datomic.pingHost",
    :cassandra-session-callback "datomic.cassandraSessionCallback",
    :default-partition "datomic.defaultPartition",
    :memcached-config-timeout-msec "datomic.memcachedConfigTimeoutMsec",
    :cassandra-cluster-callback "datomic.cassandraClusterCallback",
    :local-memcached-auto-discovery "datomic.localMemcachedAutoDiscovery",
    :memcached-username "datomic.memcachedUsername",
    :memory-index-max "datomic.memoryIndexMax",
    :aws-cloudwatch-secret-key "datomic.cloudwatchSecretKey",
    :memory-index-threshold "datomic.memoryIndexThreshold",
    :aws-cloudwatch-dimension-value "datomic.cloudwatchDimension",
    :delete-concurrency "datomic.deleteConcurrency",
    :valcache-max-gb "datomic.valcacheMaxGb",
    :data-dir "datomic.dataDir",
    :object-cache-max "datomic.objectCacheMax",
    :memcached-password "datomic.memcachedPassword",
    :ping-concurrency "datomic.pingConcurrency",
    :pid-file "datomic.pidFile",
    :ping-port "datomic.pingPort",
    :metrics-callback "datomic.metricsCallback",
    :index-parallelism "datomic.indexParallelism",
    :read-concurrency "datomic.readConcurrency",
    :local-memcached "datomic.localMemcachedServers",
    :local-memcached-config-timeout-msec "datomic.localMemcachedConfigTimeoutMsec",
    :valcache-path "datomic.valcachePath",
    :sql-validation-query "datomic.sqlValidationQuery",
    :dynamic-index-parallelism "datomic.dynamicIndexParallelism",
    :memcached-expiration-days "datomic.memcachedExpirationDays",
    :aws-cloudwatch-access-key-id "datomic.cloudwatchAccesKeyId",
    :memcached-auto-discovery "datomic.memcachedAutoDiscovery",
    :local-memcached-username "datomic.localMemcachedUsername"})
  (reset-meta! #'tprop->sysprop (assoc {:column (int 1)} :name 'tprop->sysprop :ns *ns*))
  (defn convert-transactor-props-to-system-props
    ([props]
      (loop [seq_26534 (seq tprop->sysprop) chunk_26535 nil count_26536 0 i_26537 0]
        (if (< i_26537 count_26536)
          (let [vec__26538 (.nth ^clojure.lang.Indexed chunk_26535 (int i_26537))
                tp (nth vec__26538 (int 0) nil)
                sp (nth vec__26538 (int 1) nil)]
            (let [temp__5804__auto__ (get props tp)]
              (when temp__5804__auto__
                (let [v temp__5804__auto__]
                  (java.lang.System/setProperty ^java.lang.String sp ^java.lang.String v))))
            (recur seq_26534 chunk_26535 count_26536 (inc i_26537)))
          (let [temp__5804__auto__ (seq seq_26534)]
            (when temp__5804__auto__
              (let [seq_26534 temp__5804__auto__]
                (if (chunked-seq? seq_26534)
                  (let [c__6065__auto__ (chunk-first seq_26534)]
                    (recur
                      (chunk-rest seq_26534)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [vec__26541 (first seq_26534)
                        tp (nth vec__26541 (int 0) nil)
                        sp (nth vec__26541 (int 1) nil)]
                    (let [temp__5804__auto__ (get props tp)]
                      (when temp__5804__auto__
                        (let [v temp__5804__auto__]
                          (java.lang.System/setProperty
                            ^java.lang.String sp
                            ^java.lang.String v))))
                    (recur (next seq_26534) nil 0 0))))))))))
  (reset-meta!
    #'convert-transactor-props-to-system-props
    (assoc
      {:arglists (clojure.core/list ['props]), :column (int 1)}
      :name
      'convert-transactor-props-to-system-props
      :ns
      *ns*))
  (defn ensure-args
    ([props propsfile]
      (let [props (merge {:data-dir "./data", :log-dir "log"} props)
            _ (when-not (not (realized? (deref #'config/initialized)))
                (throw
                  (java.lang.AssertionError.
                    (str
                      "Assert failed: "
                      (pr-str
                        (clojure.core/list
                          'not
                          (clojure.core/list
                            'realized?
                            (clojure.core/list
                              'clojure.core/deref
                              (clojure.core/list 'var 'config/initialized))))))))
                nil)
            _ (convert-transactor-props-to-system-props props)
            _ (config/reset!)
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.transactor")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process
                      (logger/redact
                        (reduce
                          (fn fn__26550
                            ([m p__26549]
                              (let [vec__26551 p__26549
                                    k (nth vec__26551 (int 0) nil)
                                    v (nth vec__26551 (int 1) nil)]
                                (if (nil? v) m (assoc m k v)))))
                          {:event :config/properties}
                          (deref config/properties-ref))
                        #{"datomic.memcachedPassword"}))))
                nil)
            protocol (keyword
                       (or
                         (:protocol props)
                         (do
                           (throw (java.lang.Exception. "'protocol' property not set"))
                           keyword)))
            required_props #{:protocol :memory-index-max :port :memory-index-threshold
                             :object-cache-max :host}
            required_map {:limited-edition required_props,
                          :ddb+s3 (conj required_props :aws-region :aws-dynamodb-table),
                          :ddb (conj required_props :aws-dynamodb-table :aws-dynamodb-region),
                          :ddb-local
                          (conj
                            required_props
                            :aws-dynamodb-table
                            :aws-dynamodb-override-endpoint),
                          :cass3 (conj required_props :cassandra-host :cassandra-table),
                          :cass2 (conj required_props :cassandra-host :cassandra-table),
                          :couchbase (conj required_props :couchbase-host :couchbase-bucket),
                          :inf (conj required_props :inf-host :inf-port),
                          :sql (conj required_props :sql-url :sql-driver-class),
                          :cass (conj required_props :cassandra-host :cassandra-table),
                          :s3 (conj required_props :aws-s3-bucket-id :aws-s3-path),
                          :dev required_props}
            required_params (or
                              (get required_map protocol)
                              (error/arg
                                :db.error/invalid-storage-protocol
                                (str
                                  "Unsupported storage protocol [protocol="
                                  (name protocol)
                                  "] in transactor properties "
                                  propsfile)))]
        (loop [seq_26555 (seq required_params) chunk_26556 nil count_26557 0 i_26558 0]
          (if (< i_26558 count_26557)
            (let [p (.nth ^clojure.lang.Indexed chunk_26556 (int i_26558))]
              (when-not (seq (get props p))
                (error/arg
                  :db.error/missing-transactor-property
                  (str
                    "'"
                    (name p)
                    "' property not set, required for protocol: "
                    (name protocol)
                    " in "
                    propsfile)))
              (recur seq_26555 chunk_26556 count_26557 (inc i_26558)))
            (let [temp__5804__auto__ (seq seq_26555)]
              (when temp__5804__auto__
                (let [seq_26555 temp__5804__auto__]
                  (if (chunked-seq? seq_26555)
                    (let [c__6065__auto__ (chunk-first seq_26555)]
                      (recur
                        (chunk-rest seq_26555)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [p (first seq_26555)]
                      (when-not (seq (get props p))
                        (error/arg
                          :db.error/missing-transactor-property
                          (str
                            "'"
                            (name p)
                            "' property not set, required for protocol: "
                            (name protocol)
                            " in "
                            propsfile)))
                      (recur (next seq_26555) nil 0 0))))))))
        (let [args (assoc
                     props
                     :protocol
                     protocol
                     :version
                     (config/property "datomic.version")
                     :encrypt-channel
                     (let [temp__5802__auto__ (:encrypt-channel props)]
                       (if temp__5802__auto__
                         (let [v temp__5802__auto__] (edn/read-string v))
                         true))
                     :rest-alias
                     (or (:rest-alias props) (name protocol)))
              args (reduce
                     (fn fn__26559
                       ([args k]
                         (let [temp__5802__auto__ (^clojure.lang.IFn args k)]
                           (if temp__5802__auto__
                             (let [port temp__5802__auto__]
                               (assoc
                                 args
                                 k
                                 (java.lang.Integer/valueOf
                                   (int (java.lang.Integer/parseInt ^java.lang.String port)))))
                             args))))
                     args
                     [:port :inf-port :h2-port :cassandra-port])]
          args))))
  (reset-meta!
    #'ensure-args
    (assoc
      {:arglists (clojure.core/list ['props 'propsfile]), :column (int 1)}
      :name
      'ensure-args
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.transactor" "create-cluster-map") {:column (int 1)})
  (let [v__5792__auto__ #'create-cluster-map]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.transactor" "create-cluster-map") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.transactor" "create-cluster-map")
        (clojure.lang.MultiFn.
          "create-cluster-map"
          :protocol
          :default
          #'clojure.core/global-hierarchy))
      #'create-cluster-map))
  (defn supported-protocol?
    ([protocol]
      (or
        (and (= :limited-edition protocol) (config/limited-edition?))
        (and (not= :limited-edition protocol) (config/pro?)))))
  (reset-meta!
    #'supported-protocol?
    (assoc
      {:arglists (clojure.core/list ['protocol]), :column (int 1)}
      :name
      'supported-protocol?
      :ns
      *ns*))
  (def aws-cred-query-params
   (fn aws_cred_query_params
     ([p__26577]
       (let [map__26578 p__26577
             map__26578 (if (seq? map__26578)
                          (if (next map__26578)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26578))
                            (if (seq map__26578) (first map__26578) {}))
                          map__26578)
             access_key_id (get map__26578 :aws-dynamodb-peer-access-key-id)
             secret_key (get map__26578 :aws-dynamodb-peer-secret-key)]
         (when (or access_key_id secret_key)
           (str "?aws_access_key_id=" access_key_id "&aws_secret_key=" secret_key))))))
  (reset-meta!
    #'aws-cred-query-params
    (assoc
      {:arglists
       (clojure.core/list
         [{'access-key-id :aws-dynamodb-peer-access-key-id,
           'secret-key :aws-dynamodb-peer-secret-key}]),
       :column (int 1)}
      :name
      'aws-cred-query-params
      :ns
      *ns*))
  (def cassandra-query-params
   (fn cassandra_query_params
     ([cluster_map]
       (let [params (select-keys cluster_map [:user :password :ssl :local-datacenter])]
         (when-not (empty? params)
           (str
             "?"
             (str/join
               "&"
               (map
                 (fn fn__26582
                   ([p__26581]
                     (let [vec__26583 p__26581
                           k (nth vec__26583 (int 0) nil)
                           v (nth vec__26583 (int 1) nil)]
                       (str (name k) "=" v))))
                 params))))))))
  (reset-meta!
    #'cassandra-query-params
    (assoc
      {:arglists (clojure.core/list ['cluster-map]), :column (int 1)}
      :name
      'cassandra-query-params
      :ns
      *ns*))
  (def ddb+s3-uri
   (fn ddb_PLUS_s3_uri
     ([p__26588 dbname]
       (let [map__26589 p__26588
             map__26589 (if (seq? map__26589)
                          (if (next map__26589)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26589))
                            (if (seq map__26589) (first map__26589) {}))
                          map__26589)
             cluster_map map__26589
             system (get map__26589 :system)
             aws_dynamodb_table (get map__26589 :aws-dynamodb-table)
             system_root (get map__26589 :system-root)
             root_or_table (if (= system "_default") aws_dynamodb_table system_root)]
         (str "datomic:ddb+s3://" (:aws-region cluster_map) "/" root_or_table "/" dbname)))))
  (reset-meta!
    #'ddb+s3-uri
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [{:keys ['system 'aws-dynamodb-table 'system-root], :as 'cluster-map} 'dbname]),
       :column (int 1)}
      :name
      'ddb+s3-uri
      :ns
      *ns*))
  (def connection-uri
   (fn connection_uri
     ([cluster_map args]
       (let [dbname "<DB-NAME>" G__26591 (:protocol cluster_map)]
         (case
           G__26591
           :couchbase
           (str
             "datomic:couchbase://"
             (:host cluster_map)
             "/"
             (:bucket cluster_map)
             "/"
             dbname
             (let [temp__5804__auto__ (:password cluster_map)]
               (when temp__5804__auto__ (let [pwd temp__5804__auto__] (str "?password=" pwd)))))
           :cass3
           (str
             "datomic:cass3://"
             (:system-root cluster_map)
             "/"
             dbname
             (cassandra-query-params cluster_map))
           :cass2
           (str
             "datomic:cass2://"
             (:system-root cluster_map)
             "/"
             dbname
             (cassandra-query-params cluster_map))
           :s3
           (str
             "datomic:s3://"
             (:system-root cluster_map)
             "/"
             (:aws-s3-path cluster_map)
             "/"
             dbname
             (aws-cred-query-params args))
           :sql
           (str
             "datomic:sql://"
             dbname
             "?"
             (:system-root cluster_map)
             (when (:sql-user args) (str "?user=" (:sql-user args)))
             (when (:sql-password args) (str "&password=" (:sql-password args)))
             (let [temp__5804__auto__ (:sql-driver-params args)]
               (when temp__5804__auto__
                 (let [sql_driver_params temp__5804__auto__] (str "&" sql_driver_params)))))
           :cass
           (str
             "datomic:cass://"
             (:system-root cluster_map)
             "/"
             dbname
             (cassandra-query-params cluster_map))
           :ddb-local
           (str
             "datomic:ddb-local://"
             (:override-endpoint cluster_map)
             "/"
             (:system-root cluster_map)
             "/"
             dbname
             (aws-cred-query-params args))
           :inf
           (str "datomic:inf://" (:system-root cluster_map) "/" dbname)
           :ddb
           (str
             "datomic:ddb://"
             (:region cluster_map)
             "/"
             (:system-root cluster_map)
             "/"
             dbname
             (aws-cred-query-params args))
           :dev
           (str
             "datomic:dev://"
             (:system-root cluster_map)
             "/"
             dbname
             (when-not (=
                         {:h2-port (inc (:port cluster_map))}
                         (select-keys cluster_map [:h2-port]))
               (str "?" (uri/map->query-string (select-keys cluster_map [:h2-port])))))
           :mem
           (str "datomic:mem://" dbname)
           :limited-edition
           (str "datomic:limited-edition://" (:system-root cluster_map) "/" dbname)
           :ddb+s3
           (ddb+s3-uri cluster_map dbname))))))
  (reset-meta!
    #'connection-uri
    (assoc
      {:arglists (clojure.core/list ['cluster-map 'args]), :column (int 1)}
      :name
      'connection-uri
      :ns
      *ns*))
  (def connection-uri-message
   (fn connection_uri_message
     ([cluster_map args]
       (let [G__26595 (:protocol cluster_map)]
         (case
           G__26595
           (:ddb+s3 :cass3 :ddb-local :inf :ddb :couchbase :cass :s3 :cass2 :mem)
           (connection-uri cluster_map args)
           :sql
           (str
             (connection-uri cluster_map args)
             ", you may need to change the user and password parameters to work with your jdbc driver")
           (:dev :limited-edition)
           (str (connection-uri cluster_map args) ", storing data in: " (:data-dir args)))))))
  (reset-meta!
    #'connection-uri-message
    (assoc
      {:arglists (clojure.core/list ['cluster-map 'args]), :column (int 1)}
      :name
      'connection-uri-message
      :ns
      *ns*))
  (defn h2-cluster-map
    ([args]
      (let [map__26597 args
            map__26597 (if (seq? map__26597)
                         (if (next map__26597)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26597))
                           (if (seq map__26597) (first map__26597) {}))
                         map__26597)
            host (get map__26597 :host)
            port (get map__26597 :port)
            h2_port (get map__26597 :h2-port)
            storage_access (get map__26597 :storage-access)
            ks [:host
                :port
                :storage-access
                :storage-admin-password
                :old-storage-admin-password
                :storage-datomic-password
                :old-storage-datomic-password]]
        (loop [seq_26598 (seq ks) chunk_26599 nil count_26600 0 i_26601 0]
          (if (< i_26601 count_26600)
            (let [k (.nth ^clojure.lang.Indexed chunk_26599 (int i_26601))]
              (when (= "" (get args k))
                (throw (java.lang.IllegalArgumentException. (str (name k) " cannot be empty"))))
              (recur seq_26598 chunk_26599 count_26600 (inc i_26601)))
            (let [temp__5804__auto__ (seq seq_26598)]
              (when temp__5804__auto__
                (let [seq_26598 temp__5804__auto__]
                  (if (chunked-seq? seq_26598)
                    (let [c__6065__auto__ (chunk-first seq_26598)]
                      (recur
                        (chunk-rest seq_26598)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [k (first seq_26598)]
                      (when (= "" (get args k))
                        (throw
                          (java.lang.IllegalArgumentException. (str (name k) " cannot be empty"))))
                      (recur (next seq_26598) nil 0 0))))))))
        (merge
          {:storage-access "local"}
          (assoc
            (select-keys args ks)
            :system-root
            (str host ":" port)
            :h2-port
            (or h2_port (inc port)))))))
  (reset-meta!
    #'h2-cluster-map
    (assoc
      {:arglists (clojure.core/list ['args]), :column (int 1)}
      :name
      'h2-cluster-map
      :ns
      *ns*))
  (def ddb+s3-cluster-map
   (fn ddb_PLUS_s3_cluster_map
     ([p__26606]
       (let [map__26607 p__26606
             map__26607 (if (seq? map__26607)
                          (if (next map__26607)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26607))
                            (if (seq map__26607) (first map__26607) {}))
                          map__26607)
             args map__26607
             system (get map__26607 :system)
             system (or system "_default")]
         (when (and system (not= system "_default") (re-find #"[^a-zA-Z0-9-]" system))
           (throw
             (java.lang.IllegalArgumentException.
               (str
                 "Invalid system: "
                 system
                 ". System can only contain alphanumerical characters and dashes"))))
         (merge
           (select-keys args [:aws-dynamodb-table :aws-region])
           {:system system, :system-root (str (:aws-dynamodb-table args) "/" system)})))))
  (reset-meta!
    #'ddb+s3-cluster-map
    (assoc
      {:private true,
       :arglists (clojure.core/list [{:keys ['system], :as 'args}]),
       :column (int 1)}
      :name
      'ddb+s3-cluster-map
      :ns
      *ns*))
  (defn run*
    ([args]
      (db/use-transactor-tempid-range)
      (process/claim-pid-file)
      (garbage/install-mark-handler)
      (let [protocol (:protocol args)
            cluster_map (merge
                          {:protocol protocol}
                          (let [G__26612 protocol]
                            (case
                              G__26612
                              :ddb-local
                              {:params
                               {:ddb-local
                                (aws-creds
                                  (:aws-dynamodb-access-key-id args)
                                  (:aws-dynamodb-secret-key args))},
                               :override-endpoint (:aws-dynamodb-override-endpoint args),
                               :system-root (:aws-dynamodb-table args)}
                              :inf
                              (let [map__26615 args
                                    map__26615 (if (seq? map__26615)
                                                 (if (next map__26615)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__26615))
                                                   (if (seq map__26615) (first map__26615) {}))
                                                 map__26615)
                                    inf_host (get map__26615 :inf-host)
                                    inf_port (get map__26615 :inf-port)]
                                {:system-root (str inf_host ":" inf_port),
                                 :host inf_host,
                                 :port inf_port})
                              :sql
                              (let [map__26616 args
                                    map__26616 (if (seq? map__26616)
                                                 (if (next map__26616)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__26616))
                                                   (if (seq map__26616) (first map__26616) {}))
                                                 map__26616)
                                    sql_url (get map__26616 :sql-url)
                                    sql_user (get map__26616 :sql-user)
                                    sql_password (get map__26616 :sql-password)
                                    sql_driver_class (get map__26616 :sql-driver-class)
                                    sql_driver_params (get map__26616 :sql-driver-params)]
                                {:system-root sql_url,
                                 :sql-url sql_url,
                                 :sql-user sql_user,
                                 :sql-password sql_password,
                                 :sql-driver-class sql_driver_class,
                                 :sql-driver-params sql_driver_params,
                                 :sql-initial-size 4})
                              :s3
                              (let [map__26619 args
                                    map__26619 (if (seq? map__26619)
                                                 (if (next map__26619)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__26619))
                                                   (if (seq map__26619) (first map__26619) {}))
                                                 map__26619)
                                    aws_s3_access_key_id (get map__26619 :aws-s3-access-key-id)
                                    aws_s3_secret_key (get map__26619 :aws-s3-secret-key)
                                    aws_s3_bucket_id (get map__26619 :aws-s3-bucket-id)
                                    aws_s3_path (get map__26619 :aws-s3-path)]
                                {:system-root aws_s3_bucket_id,
                                 :aws-s3-path aws_s3_path,
                                 :aws-access-key-id aws_s3_access_key_id,
                                 :aws-secret-key aws_s3_secret_key})
                              :cass
                              (let [map__26618 args
                                    map__26618 (if (seq? map__26618)
                                                 (if (next map__26618)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__26618))
                                                   (if (seq map__26618) (first map__26618) {}))
                                                 map__26618)
                                    cassandra_host (get map__26618 :cassandra-host)
                                    cassandra_port (get map__26618 :cassandra-port)
                                    cassandra_ssl (get map__26618 :cassandra-ssl)
                                    cassandra_cluster_callback (get
                                                                 map__26618
                                                                 :cassandra-cluster-callback)
                                    cassandra_table (get map__26618 :cassandra-table)
                                    cassandra_user (get map__26618 :cassandra-user)
                                    cassandra_password (get map__26618 :cassandra-password)]
                                {:system-root
                                 (str
                                   cassandra_host
                                   (when cassandra_port (str ":" cassandra_port))
                                   "/"
                                   cassandra_table),
                                 :cluster-callback cassandra_cluster_callback,
                                 :host cassandra_host,
                                 :port cassandra_port,
                                 :table cassandra_table,
                                 :user cassandra_user,
                                 :password cassandra_password,
                                 :ssl cassandra_ssl})
                              :couchbase
                              (let [map__26617 args
                                    map__26617 (if (seq? map__26617)
                                                 (if (next map__26617)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__26617))
                                                   (if (seq map__26617) (first map__26617) {}))
                                                 map__26617)
                                    couchbase_host (get map__26617 :couchbase-host)
                                    couchbase_bucket (get map__26617 :couchbase-bucket)
                                    couchbase_password (get map__26617 :couchbase-password)]
                                {:system-root couchbase_bucket,
                                 :host couchbase_host,
                                 :bucket couchbase_bucket,
                                 :password couchbase_password})
                              :ddb
                              {:params
                               {:ddb
                                (aws-creds
                                  (:aws-dynamodb-access-key-id args)
                                  (:aws-dynamodb-secret-key args))},
                               :region (:aws-dynamodb-region args),
                               :system-root (:aws-dynamodb-table args)}
                              :mdev
                              (let [map__26614 args
                                    map__26614 (if (seq? map__26614)
                                                 (if (next map__26614)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__26614))
                                                   (if (seq map__26614) (first map__26614) {}))
                                                 map__26614)
                                    host (get map__26614 :host)
                                    port (get map__26614 :port)]
                                {:system-root (str host ":" port), :host host, :port port})
                              :cass2
                              (let [map__26620 args
                                    map__26620 (if (seq? map__26620)
                                                 (if (next map__26620)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__26620))
                                                   (if (seq map__26620) (first map__26620) {}))
                                                 map__26620)
                                    cassandra_host (get map__26620 :cassandra-host)
                                    cassandra_port (get map__26620 :cassandra-port)
                                    cassandra_ssl (get map__26620 :cassandra-ssl)
                                    cassandra_cluster_callback (get
                                                                 map__26620
                                                                 :cassandra-cluster-callback)
                                    cassandra_table (get map__26620 :cassandra-table)
                                    cassandra_user (get map__26620 :cassandra-user)
                                    cassandra_password (get map__26620 :cassandra-password)]
                                {:system-root
                                 (str
                                   cassandra_host
                                   (when cassandra_port (str ":" cassandra_port))
                                   "/"
                                   cassandra_table),
                                 :cluster-callback cassandra_cluster_callback,
                                 :host cassandra_host,
                                 :port cassandra_port,
                                 :table cassandra_table,
                                 :user cassandra_user,
                                 :password cassandra_password,
                                 :ssl cassandra_ssl})
                              :cass3
                              (let [map__26613 args
                                    map__26613 (if (seq? map__26613)
                                                 (if (next map__26613)
                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                     (to-array map__26613))
                                                   (if (seq map__26613) (first map__26613) {}))
                                                 map__26613)
                                    cassandra_host (get map__26613 :cassandra-host)
                                    cassandra_port (get map__26613 :cassandra-port)
                                    cassandra_ssl (get map__26613 :cassandra-ssl)
                                    cassandra_session_callback (get
                                                                 map__26613
                                                                 :cassandra-session-callback)
                                    cassandra_table (get map__26613 :cassandra-table)
                                    cassandra_user (get map__26613 :cassandra-user)
                                    cassandra_password (get map__26613 :cassandra-password)
                                    cassandra_local_datacenter (get
                                                                 map__26613
                                                                 :cassandra-local-datacenter)]
                                {:ssl cassandra_ssl,
                                 :table cassandra_table,
                                 :password cassandra_password,
                                 :local-datacenter cassandra_local_datacenter,
                                 :port cassandra_port,
                                 :host cassandra_host,
                                 :session-callback cassandra_session_callback,
                                 :system-root
                                 (str
                                   cassandra_host
                                   (when cassandra_port (str ":" cassandra_port))
                                   "/"
                                   cassandra_table),
                                 :user cassandra_user})
                              (:dev :limited-edition)
                              (h2-cluster-map args)
                              :ddb+s3
                              (ddb+s3-cluster-map args))))
            process process/instance
            status (atom
                     (str
                       "standby/"
                       (.format
                         (java.text.SimpleDateFormat. "yyyy-MM-dd-kk-mm-ss")
                         (java.util.Date.))))
            log_path_fn (fn log_path_fn
                          ([]
                            (str
                              (str/replace (:system-root cluster_map) #"\W" "-")
                              "/"
                              (deref status))))
            transactor_descriptor (merge {:version (:version args), :type :production})
            username (crypto/random-string 256)
            password (crypto/random-string 256)]
        (when (config/property "datomic.printConnectionInfo")
          (println (str "Starting " (connection-uri-message cluster_map args) " ...")))
        (when-not (supported-protocol? (:protocol cluster_map))
          (process/fail
            process
            (str "Cannot use " protocol " protocol with this edition of Datomic.")))
        (reset! ft/work-dir (str (:data-dir args) "/fulltext"))
        (reset! aserver/work-dir (str (:data-dir args) "/artemis"))
        (coord/init-protocol cluster_map (:data-dir args))
        (transactor-ext/start-ping-endpoint)
        (when (config/edition-has-feature? :monitor/logrotate)
          ((resolve 'datomic.transactor-ext/start-logrotate)
            (merge
              (select-keys args [:log-dir :aws-s3-log-bucket-id])
              {:log-path-fn log_path_fn,
               :process process,
               :creds
               (aws-creds (:aws-s3-log-access-key-id args) (:aws-s3-log-secret-key args))})))
        (when (config/edition-has-feature? :monitor/metrics)
          (clojure.core/require 'datomic.process-monitor)
          ((resolve 'datomic.process-monitor/start-metrics)))
        (domain/preload-extension-resolver!)
        (start-lifecycle
          {:master-endpoint
           (assoc
             (select-keys args [:host :port :alt-host :encrypt-channel :version])
             :username
             username
             :password
             password),
           :status status,
           :cluster-map cluster_map,
           :process process,
           :td transactor_descriptor})
        (cluster-stack/start-kv-cache)
        (when (and (:rest-port args) (:rest-alias args))
          (clojure.core/require 'datomic.rest)
          (let [alias (.replace (connection-uri cluster_map args) "<DB-NAME>" "")]
            ((resolve 'datomic.rest/-main) "-p" (:rest-port args) (:rest-alias args) alias)))
        (if (config/property "datomic.printConnectionInfo")
          (println (str "System started " (connection-uri-message cluster_map args)))
          (println "System started"))
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.transactor")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:event :transactor/start,
                 :args
                 (dissoc
                   args
                   :storage-admin-password
                   :old-storage-admin-password
                   :storage-datomic-password
                   :old-storage-datomic-password
                   :cassandra-user
                   :cassandra-password
                   :sql-user
                   :sql-password
                   :sql-url
                   :password
                   :memcached-password
                   :aws-dynamodb-secret-key
                   :aws-cloudwatch-secret-key
                   :aws-s3-log-secret-key)})))
          nil))))
  (reset-meta!
    #'run*
    (assoc {:arglists (clojure.core/list ['args]), :column (int 1)} :name 'run* :ns *ns*))
  (defn prepare-run
    ([]
      (bridge/install)
      (cast2slf4j/redirect)
      (logger/log-uncaught-exceptions)
      (log-gc/log-gc-events)))
  (reset-meta!
    #'prepare-run
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'prepare-run :ns *ns*))
  (defn run
    ([props propsfile]
      (prepare-run)
      (future-call
        (fn fn__26626
          ([]
            (try
              (run* (ensure-args props propsfile))
              (catch
                java.lang.Throwable
                t
                (process/fail process/instance "Error starting transactor" t))))))))
  (reset-meta!
    #'run
    (assoc
      {:arglists (clojure.core/list ['props 'propsfile]), :column (int 1)}
      :name
      'run
      :ns
      *ns*)))