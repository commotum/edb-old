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
  (defn init-log-dir
    ([log_dir]
      (java.lang.System/setProperty "DATOMIC_LOG_DIR" ^java.lang.String log_dir)
      (let [context (LoggerFactory/getILoggerFactory)
            cfg (ch.qos.logback.classic.joran.JoranConfigurator.)]
        (.setContext
          ^ch.qos.logback.core.spi.ContextAwareBase cfg
          ^ch.qos.logback.core.Context context)
        (.reset ^ch.qos.logback.classic.LoggerContext context)
        (.doConfigure ^ch.qos.logback.core.joran.GenericConfigurator cfg "bin/logback.xml")
        nil)))
  (defn run-transactor
    ([props props_file]
      (java.lang.System/setProperty "datomic.cloudwatchName" "Transactor")
      (let [props_map (reduce
                        (fn fn__33053
                          ([m k]
                            (assoc
                              m
                              (keyword k)
                              (.getProperty ^java.util.Properties props ^java.lang.String k))))
                        {}
                        (keys props))
            props_map (reduce
                        (fn fn__33056
                          ([m p__33055]
                            (let [vec__33057 p__33055
                                  k (nth vec__33057 (int 0) nil)
                                  pk (nth vec__33057 (int 1) nil)]
                              (if (contains? m k)
                                m
                                (let [temp__5802__auto__ (java.lang.System/getProperty
                                                           ^java.lang.String pk)]
                                  (if temp__5802__auto__
                                    (let [prop temp__5802__auto__] (assoc m k prop))
                                    m))))))
                        props_map
                        ambient-props-map)]
        (let [temp__5804__auto__ (get props_map :log-dir)]
          (when temp__5804__auto__ (let [log_dir temp__5804__auto__] (init-log-dir log_dir))))
        (clojure.core/require 'datomic.transactor)
        ((resolve 'datomic.transactor/run) props_map props_file))))
  (defn -main
    ([props_file]
      (run-transactor
        (let [G__33064 (java.util.Properties.)]
          (.load ^java.util.Properties G__33064 (io/input-stream (io/file props_file)))
          G__33064)
        props_file))))