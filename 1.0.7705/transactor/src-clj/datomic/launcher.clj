(do
  (clojure.core/in-ns 'datomic.launcher)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.java.io :as 'io])
      (clojure.core/import 'org.slf4j.LoggerFactory)
      (clojure.core/import 'java.util.Properties)
      (clojure.core/import 'ch.qos.logback.classic.LoggerContext)
      (clojure.core/import 'ch.qos.logback.classic.joran.JoranConfigurator)))
  (when-not (.equals 'datomic.launcher 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.launcher))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.java.io :as 'io])
        (clojure.core/import 'org.slf4j.LoggerFactory)
        (clojure.core/import 'java.util.Properties)
        (clojure.core/import 'ch.qos.logback.classic.LoggerContext)
        (clojure.core/import 'ch.qos.logback.classic.joran.JoranConfigurator))))
  (set! *warn-on-reflection* true)
  (def ambient-props-map
   {:license-key "datomic.licenseKey",
    :sql-user "datomic.sqlUser",
    :sql-password "datomic.sqlPassword"})
  (reset-meta! #'ambient-props-map (assoc {:column (int 1)} :name 'ambient-props-map :ns *ns*))
  (def init-log-dir
   (fn init_log_dir
     ([log_dir]
       (java.lang.System/setProperty "DATOMIC_LOG_DIR" ^java.lang.String log_dir)
       (let [context (LoggerFactory/getILoggerFactory)
             cfg (ch.qos.logback.classic.joran.JoranConfigurator.)]
         (.setContext
           ^ch.qos.logback.core.spi.ContextAwareBase cfg
           ^ch.qos.logback.core.Context context)
         (.reset ^ch.qos.logback.classic.LoggerContext context)
         (.doConfigure ^ch.qos.logback.core.joran.GenericXMLConfigurator cfg "bin/logback.xml")
         nil))))
  (reset-meta!
    #'init-log-dir
    (assoc
      {:arglists (clojure.core/list ['log-dir]), :column (int 1)}
      :name
      'init-log-dir
      :ns
      *ns*))
  (def run-transactor
   (fn run_transactor
     ([props props_file]
       (java.lang.System/setProperty "datomic.cloudwatchName" "Transactor")
       (let [props_map (reduce
                         (fn fn__20433
                           ([m k]
                             (assoc
                               m
                               (keyword k)
                               (.getProperty ^java.util.Properties props ^java.lang.String k))))
                         {}
                         (keys props))
             props_map (reduce
                         (fn fn__20436
                           ([m p__20435]
                             (let [vec__20437 p__20435
                                   k (nth vec__20437 (int 0) nil)
                                   pk (nth vec__20437 (int 1) nil)]
                               (if (contains? m k)
                                 m
                                 (let [temp__5823__auto__ (java.lang.System/getProperty
                                                            ^java.lang.String pk)]
                                   (if temp__5823__auto__
                                     (let [prop temp__5823__auto__] (assoc m k prop))
                                     m))))))
                         props_map
                         ambient-props-map)]
         (let [temp__5825__auto__ (get props_map :log-dir)]
           (when temp__5825__auto__ (let [log_dir temp__5825__auto__] (init-log-dir log_dir))))
         (clojure.core/require 'datomic.transactor)
         ((resolve 'datomic.transactor/run) props_map props_file)))))
  (reset-meta!
    #'run-transactor
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'props {:tag 'Properties}) 'props-file]),
       :column (int 1)}
      :name
      'run-transactor
      :ns
      *ns*))
  (def -main
   (fn _main
     ([props_file]
       (run-transactor
         (let [G__20444 (java.util.Properties.)]
           (.load ^java.util.Properties G__20444 (io/input-stream (io/file props_file)))
           G__20444)
         props_file))))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['props-file]), :column (int 1)} :name '-main :ns *ns*)))