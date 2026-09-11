(do
  (clojure.core/in-ns 'datomic.kv-sql-ext)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.kv-sql-ext)
    {:doc
     "Builds SQL storage from a JDBC URL, DataSource, or Callable connection factory. Connection pools validate on borrow using a provider-specific query or datomic.sqlValidationQuery override."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require
        ['datomic.kv-store :as 'kv]
        ['datomic.sql :as 'sql]
        ['datomic.io :as 'io]
        ['datomic.error :as 'error]
        ['datomic.config :as 'config]
        ['datomic.monitor :as 'monitor]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.sql.Connection)
      (clojure.core/import 'java.sql.PreparedStatement)
      (clojure.core/import 'java.sql.SQLException)
      (clojure.core/import 'org.apache.tomcat.jdbc.pool.DataSource)))
  (when-not (.equals 'datomic.kv-sql-ext 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-sql-ext))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require
          ['datomic.kv-store :as 'kv]
          ['datomic.sql :as 'sql]
          ['datomic.io :as 'io]
          ['datomic.error :as 'error]
          ['datomic.config :as 'config]
          ['datomic.monitor :as 'monitor]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.sql.Connection)
        (clojure.core/import 'java.sql.PreparedStatement)
        (clojure.core/import 'java.sql.SQLException)
        (clojure.core/import 'org.apache.tomcat.jdbc.pool.DataSource))))
  (set! *warn-on-reflection* true)
  (.setMeta
    (clojure.lang.RT/var "datomic.kv-sql-ext" "driver-manager-lock")
    {:private true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.kv-sql-ext" "driver-manager-lock") (java.lang.Object.))
  (defn provider ([url] (second (re-find #"[^:]*:([^:]*):.*" url))))
  (reset-meta!
    #'provider
    (assoc {:arglists (clojure.core/list ['url]), :column (int 1)} :name 'provider :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.kv-sql-ext" "validation-query*") {:column (int 1)})
  (let [v__5813__auto__ #'validation-query*]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.kv-sql-ext" "validation-query*") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.kv-sql-ext" "validation-query*")
        (clojure.lang.MultiFn.
          "validation-query*"
          provider
          :default
          #'clojure.core/global-hierarchy))
      #'validation-query*))
  ;; ATOMIC-NOTE [observed] Default is select 1, Oracle is select 1 from dual.
  ;; The local Storage Services / Validation Query prose reverses these defaults;
  ;; record that evidence discrepancy rather than copying an unusable PostgreSQL query.
  (defmethod validation-query* :default fn__10777 ([_] "select 1"))
  (defmethod validation-query* "oracle" fn__10779 ([_] "select 1 from dual"))
  ;; Selects the configured JDBC validation query, with an Oracle-specific fallback.
  (defn validation-query
    ([provider] (or (config/property "datomic.sqlValidationQuery") (validation-query* provider))))
  (reset-meta!
    #'validation-query
    (assoc
      {:arglists (clojure.core/list ['provider]), :column (int 1)}
      :name
      'validation-query
      :ns
      *ns*))
  ;; Probes a new connection specification and records an alarm when the validation query fails.
  (defn try-validation-query
    ([sql_url spec]
      (let [q (validation-query sql_url)]
        (try
          (with-open [conn (sql/connect spec)]
            (with-open [stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String q)]
              (when-not (.next (.executeQuery ^java.sql.PreparedStatement stmt))
                (monitor/alarm :SQLValidationQueryFailed)
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-sql-ext")]
                  (when (.isWarnEnabled ^org.slf4j.Logger logger)
                    (.warn
                      ^org.slf4j.Logger logger
                      (logger/process {:event :sql/validation-query-failed, :query q})))
                  nil))))
          (catch
            java.lang.Throwable
            t
            (do
              (monitor/alarm :SQLValidationQueryFailed)
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-sql-ext") ex t]
                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                  (.warn
                    ^org.slf4j.Logger logger
                    (logger/process {:event :sql/validation-query-failed, :query q})
                    ^java.lang.Throwable ex)
                  (logger/caused-by logger ex))
                nil)))))))
  (reset-meta!
    #'try-validation-query
    (assoc
      {:arglists (clojure.core/list ['sql-url 'spec]), :column (int 1)}
      :name
      'try-validation-query
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed] A memoized connection specification owns the JDBC pool;
  ;; borrow validation and credentials live here, not in KVSql's value semantics.
  ;; [inferred] Pool reuse avoids per-block connection creation without merging
  ;; connection lifetime with the lifetime of immutable database values.
  (.setMeta (clojure.lang.RT/var "datomic.kv-sql-ext" "create-datasource") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.kv-sql-ext" "create-datasource")
    (memoize
      (fn fn__10788
        ([p__10787]
          (let [map__10789 p__10787
                map__10789 (if (seq? map__10789)
                             (if (next map__10789)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__10789))
                               (if (seq map__10789) (first map__10789) {}))
                             map__10789)
                sql_url (clojure.core/get map__10789 :sql-url)
                sql_user (clojure.core/get map__10789 :sql-user)
                sql_password (clojure.core/get map__10789 :sql-password)
                sql_driver_class (clojure.core/get map__10789 :sql-driver-class)
                sql_driver_params (clojure.core/get map__10789 :sql-driver-params)
                sql_initial_size (clojure.core/get map__10789 :sql-initial-size 2)
                vq (validation-query sql_url)
                spec {:datasource
                      (doto
                        (org.apache.tomcat.jdbc.pool.DataSource.)
                        (.setUrl ^java.lang.String sql_url)
                        (.setValidationQuery ^java.lang.String vq)
                        (.setValidationInterval
                          (long (config/property "datomic.heartbeatIntervalMsec")))
                        (.setTestOnBorrow (boolean (.booleanValue true)))
                        (.setInitialSize (int ^java.lang.Number sql_initial_size))
                        (.setDriverClassName
                          (if sql_driver_class
                            sql_driver_class
                            (.getName
                              (.getClass
                                (java.sql.DriverManager/getDriver ^java.lang.String sql_url)))))
                        ((fn fn__10791
                           ([p1__10784#]
                             (when sql_user
                               (.setUsername
                                 ^org.apache.tomcat.jdbc.pool.DataSourceProxy p1__10784#
                                 ^java.lang.String sql_user)
                               nil))))
                        ((fn fn__10793
                           ([p1__10785#]
                             (when sql_password
                               (.setPassword
                                 ^org.apache.tomcat.jdbc.pool.DataSourceProxy p1__10785#
                                 ^java.lang.String sql_password)
                               nil))))
                        ((fn fn__10795
                           ([p1__10786#]
                             (when sql_driver_params
                               (.setConnectionProperties
                                 ^org.apache.tomcat.jdbc.pool.DataSourceProxy p1__10786#
                                 ^java.lang.String sql_driver_params)
                               nil)))))}]
            (try-validation-query sql_url spec)
            spec)))))
  ;; Accepts one connection source: JDBC URL, DataSource, or Callable<Connection> factory.
  (defn cluster-conf->spec
    ([p__10798]
      (let [map__10799 p__10798
            map__10799 (if (seq? map__10799)
                         (if (next map__10799)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__10799))
                           (if (seq map__10799) (first map__10799) {}))
                         map__10799)
            cluster_conf map__10799
            sql_url (clojure.core/get map__10799 :sql-url)
            data_source (clojure.core/get map__10799 :data-source)
            factory (clojure.core/get map__10799 :factory)]
        (cond
          sql_url (locking driver-manager-lock (create-datasource cluster_conf))
          data_source {:datasource data_source}
          factory {:factory (fn fn__10800 ([] (.call ^java.util.concurrent.Callable factory)))}
          :else (do
                  (error/arg
                    :db.error/invalid-sql-connection
                    "Must supply jdbc url in uri, or DataSource or Callable<Connection> in protocolObject arg to Peer.connect"))))))
  (reset-meta!
    #'cluster-conf->spec
    (assoc
      {:arglists (clojure.core/list [{:keys ['sql-url 'data-source 'factory], :as 'cluster-conf}]),
       :column (int 1)}
      :name
      'cluster-conf->spec
      :ns
      *ns*))
  (defn kv-sql
    ([cluster_conf] ((resolve 'datomic.kv-sql/from-spec) (cluster-conf->spec cluster_conf))))
  (reset-meta!
    #'kv-sql
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'kv-sql
      :ns
      *ns*)))
