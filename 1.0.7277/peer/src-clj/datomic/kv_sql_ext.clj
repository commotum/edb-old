(do
  (clojure.core/in-ns 'datomic.kv-sql-ext)
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
  (def driver-manager-lock (java.lang.Object.))
  (reset-meta!
    #'driver-manager-lock
    (assoc {:private true, :column 1} :name 'driver-manager-lock :ns *ns*))
  (defn provider ([url] (second (re-find #"[^:]*:([^:]*):.*" url))))
  (defmulti validation-query* provider)
  (defmethod validation-query* :default fn__11546 ([_] "select 1"))
  (defmethod validation-query* "oracle" fn__11548 ([_] "select 1 from dual"))
  (defn validation-query
    ([provider] (or (config/property "datomic.sqlValidationQuery") (validation-query* provider))))
  (defn try-validation-query
    ([sql_url spec]
      (let [q (validation-query sql_url)]
        (try
          (let [conn (sql/connect spec)]
            (try
              (let [stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String q)]
                (try
                  (when-not (.next (.executeQuery ^java.sql.PreparedStatement stmt))
                    (monitor/alarm :SQLValidationQueryFailed)
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-sql-ext")]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn
                          ^org.slf4j.Logger logger
                          (logger/process {:event :sql/validation-query-failed, :query q}))
                        nil)
                      nil))
                  (finally (do (.close ^java.sql.Statement stmt) nil))))
              (finally (do (.close ^java.sql.Connection conn) nil))))
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
  (def create-datasource
   (memoize
     (fn fn__11557
       ([p__11556]
         (let [map__11558 p__11556
               map__11558 (if (seq? map__11558)
                            (clojure.lang.PersistentHashMap/create (seq map__11558))
                            map__11558)
               sql_url (clojure.core/get map__11558 :sql-url)
               sql_user (clojure.core/get map__11558 :sql-user)
               sql_password (clojure.core/get map__11558 :sql-password)
               sql_driver_class (clojure.core/get map__11558 :sql-driver-class)
               sql_driver_params (clojure.core/get map__11558 :sql-driver-params)
               sql_initial_size (clojure.core/get map__11558 :sql-initial-size 2)
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
                       ((fn fn__11560
                          ([p1__11553#]
                            (when sql_user
                              (.setUsername
                                ^org.apache.tomcat.jdbc.pool.DataSourceProxy p1__11553#
                                ^java.lang.String sql_user)
                              nil))))
                       ((fn fn__11562
                          ([p1__11554#]
                            (when sql_password
                              (.setPassword
                                ^org.apache.tomcat.jdbc.pool.DataSourceProxy p1__11554#
                                ^java.lang.String sql_password)
                              nil))))
                       ((fn fn__11564
                          ([p1__11555#]
                            (when sql_driver_params
                              (.setConnectionProperties
                                ^org.apache.tomcat.jdbc.pool.DataSourceProxy p1__11555#
                                ^java.lang.String sql_driver_params)
                              nil)))))}]
           (try-validation-query sql_url spec)
           spec)))))
  (defn cluster-conf->spec
    ([p__11567]
      (let [map__11568 p__11567
            map__11568 (if (seq? map__11568)
                         (clojure.lang.PersistentHashMap/create (seq map__11568))
                         map__11568)
            cluster_conf map__11568
            sql_url (clojure.core/get map__11568 :sql-url)
            data_source (clojure.core/get map__11568 :data-source)
            factory (clojure.core/get map__11568 :factory)]
        (cond
          sql_url (locking driver-manager-lock (create-datasource cluster_conf))
          data_source {:datasource data_source}
          factory {:factory (fn fn__11569 ([_] (.call ^java.util.concurrent.Callable factory)))}
          :else (do
                  (error/arg
                    :db.error/invalid-sql-connection
                    "Must supply jdbc url in uri, or DataSource or Callable<Connection> in protocolObject arg to Peer.connect"))))))
  (defn kv-sql
    ([cluster_conf] ((resolve 'datomic.kv-sql/from-spec) (cluster-conf->spec cluster_conf)))))